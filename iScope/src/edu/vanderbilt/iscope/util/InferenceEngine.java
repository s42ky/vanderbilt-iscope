package edu.vanderbilt.iscope.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;

import edu.vanderbilt.iscope.model.Constraint;
import edu.vanderbilt.iscope.model.Variable;
import edu.vanderbilt.iscope.model.Entry;

public class InferenceEngine {
	
	QueryParser _parser = new QueryParser();
	
	public void inferVariableConstraint(Vector<String> varsSet, int parser_option, HashSet<String> persistentVars, HashSet<Variable> boundedVars){
		boolean start = false;
		HashMap<String, Variable> variableSet = new HashMap<String, Variable>();
		for (String vars: varsSet) {
			HashMap<String, String> pairs = new HashMap<String, String>();
			if (parser_option == Entry.PARSE_SESSVAR) {
				pairs = _parser.parseSession(vars);
			} else if (parser_option == Entry.PARSE_INPUTPARA){
				//System.out.println(vars);
				pairs = _parser.parseInputParameters(vars);
			}
			if (persistentVars.isEmpty())  {
				if (!start) {
					persistentVars.addAll(pairs.keySet());
					start = true;
				} else {
					return;      // no persistent/bounded variables.
				}
			}
			HashSet<String> toRemove = new HashSet<String>();
			for (String var: persistentVars) {
				if (!pairs.containsKey(var)) toRemove.add(var); // retain persistent variables.
			}
			persistentVars.removeAll(toRemove);
			for (String name: pairs.keySet()) {
				if (! variableSet.containsKey(name)) {
					variableSet.put(name, new Variable(name));
				}
				String value = pairs.get(name);
				variableSet.get(name).addValue(value);
			}
		}
		for (String persistentVar: persistentVars){
			variableSet.get(persistentVar).testDomainType();
			variableSet.get(persistentVar).compValueDomain();
			if (variableSet.get(persistentVar).getDomainType() == Variable.BOUNDED) {
				boundedVars.add(variableSet.get(persistentVar));     // identify bounded variables.
			}
		}
	}
	
	// temp storage.
	private HashSet<Constraint> equalityConsts = new HashSet<Constraint>();
	private HashSet<Constraint> blacklist = new HashSet<Constraint>();
	
	public void inferEqualityConstraint(Vector<String> inputParasSet, Vector<String> sessionVarsSet, HashSet<Constraint> constraints) {
		equalityConsts.clear();
		blacklist.clear();
		if (sessionVarsSet.size() != inputParasSet.size()) {
			System.out.println("Error: sample size doesn't match...");
			return;
		}
		for (int i = 0; i < sessionVarsSet.size(); i++) {
			iterInferEqualityConstraint(_parser.parseInputParameters(inputParasSet.get(i)), _parser.parseSession(sessionVarsSet.get(i)));
		}
		for (Constraint c : equalityConsts) {
			//System.out.println(c.getConstraint());
			constraints.add(c);
		}
	}
	
	// iteratively learn at each inference point.
	public void iterInferEqualityConstraint(HashMap<String, String> parameters, HashMap<String, String> sessionVars) {		
		// verify existing candidates:
		verifyCandidates(parameters, sessionVars);
		
		// infer new candidates:
		Iterator<String> it = parameters.keySet().iterator();
		while (it.hasNext()) {
			String parameter = it.next();
			String para_value = parameters.get(parameter);
			
			if (Character.isDigit(parameter.charAt(0))) continue;     // filter certain parameters for Scarf app.
			
			Iterator<String> it2 = sessionVars.keySet().iterator();
			while (it2.hasNext()) {
				String session_var = it2.next();
				String sess_value = sessionVars.get(session_var);				
				
				Constraint constraint = new Constraint(parameter, session_var);
				
				if (blacklist.contains(constraint))  continue;			
				
				if (para_value.equals(sess_value)) {
					if (parameter.equals("userid")&&session_var.equals("session.userid")){
						//System.out.println("ADD TO EQUALITY 0");
						//System.out.println("para value: "+ para_value + " sess value: " + sess_value);
					}
					equalityConsts.add(constraint);
				} else {
					if (parameter.equals("userid")&&session_var.equals("session.userid")){
						//System.out.println("ADD TO BLACKLIST 0");
						//System.out.println("para value: "+ para_value + " sess value: " + sess_value);
					}
					blacklist.add(constraint);
				}
			}
		}
	}

	private void verifyCandidates(HashMap<String, String> parameters, HashMap<String, String> sessionVars) {
		Iterator<Constraint> it = equalityConsts.iterator();
		HashSet<Constraint> toRemove = new HashSet<Constraint>();
		while (it.hasNext()) {
			Constraint constraint = it.next();
			String parameter = constraint.getParameter();
			String session_var = constraint.getSessionVariable();
			if (parameters.containsKey(parameter)) {
				if (!(sessionVars.containsKey(session_var) && sessionVars.get(session_var).equals(parameters.get(parameter)))){
					if (parameter.equals("userid")&&session_var.equals("session.userid")){
						//System.out.println("ADD TO BLACKLIST 1");
					}
					toRemove.add(constraint);
					blacklist.add(constraint);
				}
			}
		}
		equalityConsts.removeAll(toRemove);
	}
}