package edu.vanderbilt.iscope.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;

import org.jdom.Element;

import edu.vanderbilt.iscope.util.InferenceEngine;

public class Entry {
	
	private String requestKey;     // requestKey
	
	public static int PARSE_SESSVAR = 1;
	public static int PARSE_INPUTPARA = 2;
	
	private InferenceEngine _engine = new InferenceEngine();
	
	public Entry(String key) {
		requestKey = key;
	}
	
	private Vector<String> sessionVarsSet = new Vector<String>();
	private Vector<String> inputParasSet = new Vector<String>();
	
	private HashSet<String> persistentParas = new HashSet<String>();
	private HashSet<String> persistentSessVars = new HashSet<String>();
	private HashSet<Variable> boundedParas = new HashSet<Variable>();
	private HashSet<Variable> boundedSessVars = new HashSet<Variable>();
	private HashSet<Constraint> equalityConsts = new HashSet<Constraint>();
	
	public void addTrace(String inputParas, String sessionVars) {
		sessionVarsSet.add(sessionVars);
		inputParasSet.add(inputParas);
	}
	
	public void genConstraints() {
		_engine.inferVariableConstraint(sessionVarsSet, PARSE_SESSVAR, persistentSessVars, boundedSessVars);
		_engine.inferVariableConstraint(inputParasSet, PARSE_INPUTPARA, persistentParas, boundedParas);
		//System.out.println("Cur Entry: " + requestKey);
		_engine.inferEqualityConstraint(inputParasSet, sessionVarsSet, equalityConsts);
	}
	
	public boolean checkEqualityConst(HashMap<String, String> parameters, HashMap<String, String> sessionVars) {
		boolean result = true;
		for (Constraint constraint: equalityConsts) {
			//System.out.println(constraint.getConstraint());
			String para = constraint.getParameter();
			String sessVar = constraint.getSessionVariable();
			if (parameters.containsKey(para) && sessionVars.containsKey(sessVar) && !para.equals(sessVar)){
				result = false;
				return result;
			}
		}
		return result;
	}
	
	public boolean checkSessionConst(HashMap<String, String> sessionVars) {
		boolean result = true;
		for (String sessVar: persistentSessVars) {
			if (!sessionVars.containsKey(sessVar)) {
				//System.out.println("HERE" + sessVar);
				result = false;
				return result;
			}
		}
		for (Variable var: boundedSessVars) {
			String value = sessionVars.get(var.getName());
			if (!var.getValueDomain().contains(value)) {
				//System.out.println("HERE " + var.getName() + " " + value);
				result = false;
				return result;
			}
		}
		return result;
	}
	
	// assemble a set of input parameters into a string.
	private String assembleParas(HashMap<String, String> paras) {
		String inputParas = "";
		for (String para: paras.keySet()) {
			inputParas += para + ":" + paras.get(para) + "||";
		}
		return inputParas;
	}
		
	public Vector<String> genViolatedParas(HashMap<String, String> parameters, HashMap<String, String> sessionVars) {
		Vector<String> violatedParas = new Vector<String>();
		/*  DON'T change the web request syntax!!
		for (String para: persistentParas) {
			HashMap<String, String> newParas = new HashMap<String, String>(parameters);
			newParas.remove(para);
			violatedParas.add(assembleParas(newParas));
		}
		*/
		for (Variable var: boundedParas) {
			HashMap<String, String> newParas = new HashMap<String, String>(parameters);
			//out of bound.
			String valueSet = "";
			for (String value: var.getValueDomain()) {
				valueSet += value + ",";
			}
			newParas.put(var.getName(), "NOT{"+valueSet+"}");    // TOKEN HERE.
			violatedParas.add(assembleParas(newParas));
		}
		for (Constraint constraint: equalityConsts) {
			//System.out.println(requestKey + " " + constraint.getConstraint());
			HashMap<String, String> newParas = new HashMap<String, String>(parameters);
			String para = constraint.getParameter();
			String sessVar = constraint.getSessionVariable();
			if (newParas.containsKey(para) && sessionVars.containsKey(sessVar) && newParas.get(para).equals(sessionVars.get(sessVar))){
				newParas.put(para, "NOT{,"+sessionVars.get(sessVar)+",}");     // TOKEN HERE.
				violatedParas.add(assembleParas(newParas));
			}
		}
		return violatedParas;
	}
	
	
	// generate the XML entrance element (a set of constraints)
	public Element genXMLEntryConstElement() {
		Element entryConst = new Element("Entry");
		entryConst.setAttribute("Key", requestKey);
		entryConst.addContent(genXMLPersistentParaElement());
		entryConst.addContent(genXMLPersistentSessVarElement());
		entryConst.addContent(genXMLBoundedParaElement());
		entryConst.addContent(genXMLBoundedSessVarElement());
		entryConst.addContent(genXMLEqualityConstElement());
		return entryConst;
	}
	
	// Output each set of constraints in XML format.	
	public Element genXMLPersistentParaElement(){
		Element consts = new Element("PersistentPara");
		for (String para: persistentParas) {
			Element var = new Element("Variable");
			var.setAttribute("name", para);
			consts.addContent(var);
		}
		return consts;
	}
	
	public Element genXMLPersistentSessVarElement(){
		Element consts = new Element("PersistentSessVar");
		for (String sessVar: persistentSessVars) {
			Element var = new Element("Variable");
			var.setAttribute("name", sessVar);
			consts.addContent(var);
		}		
		return consts;
	}
	
	public Element genXMLBoundedParaElement() {
		Element consts = new Element("BoundedPara");
		for (Variable para: boundedParas) {
			Element var = new Element("Variable");
			var.setAttribute("name", para.getName());
			for (String value: para.getValueDomain()) {
				Element val = new Element("DomainValue");
				val.setAttribute("value", value);
				var.addContent(val);
			}
			consts.addContent(var);
		}
		return consts;
	}
	
	public Element genXMLBoundedSessVarElement() {           // NOTE HERE: if refer to the global domain (bounded or not).
		Element consts = new Element("BoundedSessVar");
		for (Variable sessVar: boundedSessVars) {
			Element var = new Element("Variable");
			var.setAttribute("name", sessVar.getName());
			for (String value: sessVar.getValueDomain()) {
				Element val = new Element("DomainValue");
				val.setAttribute("value", value);
				var.addContent(val);
			}
			consts.addContent(var);
		}
		return consts;
	}
	
	public Element genXMLEqualityConstElement() {
		Element consts = new Element("EqualityConsts");
		for (Constraint constraint: equalityConsts){
			Element equalityConst = new Element("Constraint");
			equalityConst.setAttribute("Parameter", constraint.getParameter());
			equalityConst.setAttribute("SessionVariable", constraint.getSessionVariable());
			consts.addContent(equalityConst);
		}
		return consts;
	}
	
	// load/parse XML element	
	public void procXMLPersistentParaElement(Element e) {
		for (Object v: e.getContent()){
			if (!(v instanceof Element)) continue;
			Element var = (Element) v;
			if (!var.getName().equals("Variable")) continue;
			String name = var.getAttributeValue("name");
			persistentParas.add(name);
		}
	}

	public void procXMLPersistentSessVarElement(Element e) {
		for (Object v: e.getContent()){
			if (!(v instanceof Element)) continue;
			Element var = (Element) v;
			if (!var.getName().equals("Variable")) continue;
			String name = var.getAttributeValue("name");
			persistentSessVars.add(name);
		}
	}
	
	public void procXMLBoundedParaElement(Element e) {
		for (Object v: e.getContent()){
			if (!(v instanceof Element)) continue;
			Element var = (Element) v;
			if (!var.getName().equals("Variable")) continue;
			String name = var.getAttributeValue("name");
			Variable boundedVar = new Variable(name);
			for (Object m: var.getContent()) {
				if (!(m instanceof Element)) continue;
				Element val = (Element) m;
				if (!val.getName().equals("DomainValue")) continue;
				String value = val.getAttributeValue("value");
				boundedVar.addDomainValue(value);
			}
			boundedParas.add(boundedVar);
		}
	}
	
	public void procXMLBoundedSessVarElement(Element e) {
		for (Object v: e.getContent()){
			if (!(v instanceof Element)) continue;
			Element var = (Element) v;
			if (!var.getName().equals("Variable")) continue;
			String name = var.getAttributeValue("name");
			Variable boundedVar = new Variable(name);
			for (Object m: var.getContent()) {
				if (!(m instanceof Element)) continue;
				Element val = (Element) m;
				if (!val.getName().equals("DomainValue")) continue;
				String value = val.getAttributeValue("value");
				boundedVar.addDomainValue(value);				
			}
			boundedSessVars.add(boundedVar);
		}
	}
		
	public void procXMLEqualityConstElement(Element e) {
		for (Object c: e.getContent()){
			if (!(c instanceof Element)) continue;
			Element constraint = (Element) c;
			if (!constraint.getName().equals("Constraint")) continue;
			String para = constraint.getAttributeValue("Parameter");
			String sessVar = constraint.getAttributeValue("SessionVariable");
			equalityConsts.add(new Constraint(para, sessVar));
		}
	}
}