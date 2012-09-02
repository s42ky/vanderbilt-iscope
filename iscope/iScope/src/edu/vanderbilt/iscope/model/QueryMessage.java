package edu.vanderbilt.iscope.model;

import java.util.StringTokenizer;

public class QueryMessage {

	private String queryStatement;
	private String script;
	private String sessionVars;
	
	public QueryMessage(String query, String spt, String vars) {
		queryStatement = query;
		script = spt;
		sessionVars = vars;
	}
	
	public QueryMessage(String line) {
		StringTokenizer st = new StringTokenizer(line, "[]");
		st.nextToken();
		queryStatement = st.nextToken();
		st.nextToken();
		script = st.nextToken();
		st.nextToken();
		sessionVars = st.nextToken();
	}
	
	public String getQueryStatement() {
		return queryStatement;
	}
	
	public String getScript() {
		return script;
	}
	
	public String getSessionVars() {
		return sessionVars;
	}
	
	public boolean equals(Object o) {
		if (o instanceof QueryMessage) {
			QueryMessage message = (QueryMessage)o;
			return (queryStatement.equals(message.getQueryStatement()) && script.equals(message.getScript()) && sessionVars.equals(message.getSessionVars()));
		}
		return false;
	}
	
	public int hashCode() {
		return 0;
	}
}