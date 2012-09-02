package edu.vanderbilt.iscope.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;
import java.util.regex.Pattern;

// From logicScope. Refer to Field in SENTINEL.
public class Variable {	
	
	// variable domain type;
	public static int UNDETERMINED = -1;
	public static int BOUNDED = 0;
	public static int UNBOUNDED = 1;
	
	// variable value type;
	public static int NUMBER = 0;    // +/- 0-9
	public static int NAME = 1;     // no space in string.
	public static int EMAIL = 2;    // pattern: xx@xxx.com/edu/org/net.
	public static int PHONE = 3;    // 10 digit number.
	public static int STRING = 4;   // general string containing spaces, e.g., addr, paper name.
	
	private static Pattern pattern_num = Pattern.compile("^[-+]?[0-9]*\\.?[0-9]+$");
	private static Pattern pattern_email = Pattern.compile("([\\w-]+(?:\\.[\\w-]+)*@(?:[\\w-]+\\.)+\\w{2,7})\\b");
	private static Pattern pattern_phone = Pattern.compile("^\\(?(\\d{3})\\)?[- ]?(\\d{3})[- ]?(\\d{4})$");
	
	private String name;       // parameter name.
	private int domain_type;
	private int value_type;
	private HashSet<String> valueDomain;
	
	// temp usage;
	private int occurence;
	private HashMap<String, Integer> valueSet;
	
	public Variable (String n) {
		name = n;
		domain_type = UNDETERMINED;
		value_type = UNDETERMINED;
		valueDomain = new HashSet<String>();
		occurence = 0;
		valueSet = new HashMap<String, Integer>();
	}
	
	public String getName() {
		return name;
	}
	
	public int getValueType() {
		return value_type;
	}
	
	public int getDomainType() {
		return domain_type;
	}
	
	public void setValueType(int t) {
		value_type = t;
	}
	
	public void setDomainType(int t) {
		domain_type = t;
	}
	
	public HashSet<String> getValueDomain() {
		return valueDomain;
	}
	
	public void setValueDomain(HashSet<String> values) {
		valueDomain.clear();
		valueDomain.addAll(values);
	}
	
	public void addDomainValue(String value) {
		valueDomain.add(value);
	}
	
	public void compValueDomain () {
		valueDomain.clear();
		if (domain_type == BOUNDED) {
			valueDomain.add("null");
			//valueDomain.add("outofbound");
			valueDomain.addAll(valueSet.keySet());
		} else if (domain_type == UNBOUNDED) {
			valueDomain.add("null");
			valueDomain.add("nonnull");
		}
		occurence = 0;
		valueSet.clear();
	}
	
	public void addValue (String value) {
		occurence ++;
		if (!valueSet.containsKey(value)) {
			valueSet.put(value, new Integer(0));
		}
		int c = valueSet.get(value);
		valueSet.put(value, c+1);
	}
	
	public void testValueType() {
		int type = UNDETERMINED;
		HashSet<String> values = new HashSet<String>();
		values.addAll(valueSet.keySet());
		Iterator<String> it = values.iterator();
		while (it.hasNext()) {
			String value = it.next();
			//if (name.equals("email")) System.out.println("Value " + value + " type: " + patternMatch(value));
			if (type == UNDETERMINED) {
				type = patternMatch(value);
			} else if (type != patternMatch(value)) {
				type = STRING;
			}
		}
		value_type = type;
	}
	
	
	private int patternMatch(String value) {
		if (value.contains(" ")){    // general string contain space.
			return STRING;
		} else {
			if (pattern_email.matcher(value).matches()) {       // check if email.
				return EMAIL;
			} else if (pattern_phone.matcher(value).matches()) {    // check if phone number.
				return PHONE;
			} else if (pattern_num.matcher(value).matches()){   // check number: integer/double; positive/both.
				return NUMBER;
			} else {
				return NAME;    // no space string.
			}
		}
	}
	
	public void testDomainType() {
		if (name.equals("Edit") || name.equals("List") || name.equals("View") || name.equals("Remove")) {     // for OpenIT case.
			domain_type = BOUNDED;  
		}else {
			KSTest();
		}		
	}
	
	// determine the type of session variable.
	public void KSTest() {
		
		Vector<Integer> values = new Vector<Integer>();
		values.addAll(valueSet.values());
		Collections.sort(values);
		Vector<Double> n2 = new Vector<Double>();
		double m = 1.0;
		for (int i = 0; i < values.size(); i++){
			int c = values.get(i);
			for (int j = 0; j < c; j++){
				n2.add(m);
			}
			m = m + 1;
		}
		int vnum = valueSet.size();
		double max = 0.0;
		for (int i = 0; i < occurence-1; i ++){
			double r = Math.abs((double)i/occurence -  n2.get(i)/vnum);
			if ( r > max)  max = r;
		}
		// significance level: 0.9
		//System.out.println(key + " occurence: " + occurence + " valueSet size: " + valueSet.size() + " " + max);
		//if (max > 0.563) {
		
		if (max > 0.53) {
			domain_type = BOUNDED;
		} else {
			domain_type = UNBOUNDED;
		}
	}
}