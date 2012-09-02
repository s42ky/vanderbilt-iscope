package edu.vanderbilt.iscope.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import edu.vanderbilt.iscope.model.Variable;

public class StateConstructor {
	
	private ArrayList<String> sessionKeys;         // the sorted set of session variable keys;
	private HashMap<String, Variable> sessionVars;  // the set of session variables.
	private String initStateSig = null;	
	private HashSet<String> stateSet = new HashSet<String>();
	
	private static QueryParser _parser = new QueryParser();
	
	public StateConstructor() {
		sessionVars = new HashMap<String, Variable>();
		sessionKeys = new ArrayList<String>();
	}
	
	// Generate the initial state signature;
	public String getInitStateSig() {
		if (initStateSig == null) {
			for (int i = 0; i < sessionKeys.size(); i++) {
				String name = sessionKeys.get(i);
				if (i == 0) initStateSig = "[" + name + ":null]";
				else initStateSig += "[" + name + ":null]";
			}
		}
		return initStateSig;
	}
		
	public void addTrace(HashMap<String, String> values) {
		Iterator<String> it = values.keySet().iterator();
		while (it.hasNext()){
			String key = it.next();
			if (!sessionVars.containsKey(key)) {
				sessionVars.put(key, new Variable(key));
			}
			String value = values.get(key);
			sessionVars.get(key).addValue(value);
		}
	}
	
	public void addToStateSet(String state) {
		stateSet.add(state);
	}
	
	public HashSet<String> getStateSet() {
		return stateSet;
	}
	
	public void outputStateSet(String file) throws IOException {
		BufferedWriter bw = new BufferedWriter(new FileWriter(file));
		for (String state: stateSet) {
			bw.write(state + "\n");
		}
		bw.close();
	}
	
	public void loadStateSet(String file) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(file));
		String line;
		while ((line=br.readLine())!=null) {
			stateSet.add(line);
		}
		br.close();
	}

	// Test each session variable.
	public void analyzeSessionVariable() {
		System.out.println("====Test each session variable====");
		Iterator<String> it = sessionVars.keySet().iterator();
		while (it.hasNext()){
			String key = it.next();
			Variable sv = sessionVars.get(key);
			sv.testDomainType();                              // Test parameter.
			//sv.testValueType();
			sv.compValueDomain();			
		}
		sessionKeys.addAll(sessionVars.keySet());
		Collections.sort(sessionKeys);
		System.out.println("====Finished.====");
	}	
	
	public void outputXMLSessionProfile(OutputStream out) {
		try {
			Element root = new Element("SessionProfile");
			Iterator<String> it = sessionVars.keySet().iterator();
			while (it.hasNext()) {
				String sv = it.next();
				Element session_var = new Element("SessionVariable");
				session_var.setAttribute("name", sv);
				session_var.setAttribute("type", Integer.toString(sessionVars.get(sv).getDomainType()));
				
				Iterator<String> it2 = sessionVars.get(sv).getValueDomain().iterator();
				while (it2.hasNext()) {
					String v = it2.next();
					Element session_val = new Element("value");
					session_val.setAttribute("v", v);
					session_var.addContent(session_val);
				}
				root.addContent(session_var);
			}
			Document doc = new Document(root);
			XMLOutputter serializer = new XMLOutputter(Format.getPrettyFormat());
		    serializer.output(doc, out);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
		
	// Load session variable profile.
	public void loadXMLSessionProfile(File spec) throws JDOMException, IOException {
		SAXBuilder parser = new SAXBuilder();
		Document doc = parser.build(spec);
		//List l = doc.getContent();
		for (Object o: doc.getContent()) {
			Element e = (Element) o;
			if (!e.getName().equals("SessionProfile")) continue;
			//List var_list = e.getContent();
			for (Object v: e.getContent()) {                // for each session variable;
				if (!(v instanceof Element)) continue;
				Element sv = (Element) v;
				if (!sv.getName().equals("SessionVariable")) continue;
				String name = sv.getAttributeValue("name");
				int type = Integer.parseInt(sv.getAttributeValue("type"));
				sessionVars.put(name, new Variable(name));
				sessionVars.get(name).setDomainType(type);
				
				HashSet<String> valueSet = new HashSet<String>();
				//List val_list = sv.getContent();
				for (Object val : sv.getContent()) {
					if (!(val instanceof Element)) continue;
					Element sv_val = (Element) val;
					if (!sv_val.getName().equals("value")) continue;
					String value = sv.getAttributeValue("v");
					valueSet.add(value);
				}
				sessionVars.get(name).setValueDomain(valueSet);
			}
		}
		sessionKeys.addAll(sessionVars.keySet());
		Collections.sort(sessionKeys);
	}
	
	
	// Generate state signature.
	public String genStateSig(String session) {
		if (session == null)   return null;
		//HashMap<String, String> sessionVals = HTTPMessage.parseSessionToMap(session);
		HashMap<String, String> sessionVals = _parser.parseSession(session);
		
		/*
		if (sessionVals.containsKey("session.login")) {                   // customized for bloggit.
			return "[session.login:ok]";
		} else {
			return "[session.login:null]";
		}
		*/
		
		return genStateSig(sessionVals);
	}
	
	public String genStateSig(HashMap<String, String> sessionVals) {
		String stateSig = "";
		for (int i = 0; i < sessionKeys.size(); i++) {
			String name = sessionKeys.get(i);
			int type = sessionVars.get(name).getDomainType();
			if (type == Variable.UNBOUNDED){
				if (sessionVals.containsKey(name)) {
					stateSig += "[" + name + ":nonnull]";
				} else {
					stateSig += "[" + name + ":null]";
				}
			} else if (type == Variable.BOUNDED){
				if (sessionVals.containsKey(name)) {
					stateSig += "[" + name + ":" + sessionVals.get(name) + "]";
				} else {
					stateSig += "[" + name + ":null]";
				}
			}
		}
		//System.out.println("Current State Signature: " + stateSig);
		return stateSig;
	}
}
