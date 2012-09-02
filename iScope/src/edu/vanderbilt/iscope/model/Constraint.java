package edu.vanderbilt.iscope.model;

// This class models a specific invariant: input parameter == session variable. One template.
public class Constraint {
	
	private String parameter;
	private String sessionVariable;
	private String invariant;
	
	public Constraint(String p, String sv) {
		parameter = p;
		sessionVariable = sv;
		invariant = p + "==" + sv;                // invariant.
	}
	
	public String getParameter() {
		return parameter;
	}
	
	public String getSessionVariable() {
		return sessionVariable;
	}
	
	public String getConstraint() {
		return invariant;
	}
	
	public boolean equals(Object o) {
		if (o instanceof Constraint) {
			Constraint constraint = (Constraint)o;
			return (parameter.equals(constraint.getParameter()) && sessionVariable.equals(constraint.getSessionVariable()));
		}
		return false;
	}
	
	public int hashCode() {
		return 0;
	}
}