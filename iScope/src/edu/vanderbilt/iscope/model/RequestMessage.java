package edu.vanderbilt.iscope.model;

import java.util.StringTokenizer;

public class RequestMessage {
	
	private String inputParas;
	private String sessionVars;
	
	public RequestMessage(String paras, String vars){
		inputParas = paras;
		sessionVars = vars;
	}
	
	public RequestMessage(String line) {
		//System.out.println(line);
		StringTokenizer st = new StringTokenizer(line, "[]");
		st.nextToken();               // [PARA]
		String next = st.nextToken();
		if (next.equals("SESSION")) {
			inputParas = "";
			sessionVars = st.nextToken();
		} else {
			inputParas = next;
			st.nextToken();
			sessionVars = st.nextToken();
		}
	}
	
	public String getInputParas(){
		return inputParas;
	}
	
	public String getSessionVars() {
		return sessionVars;
	}
	
	public String toString() {
		return "[PARA]["+inputParas+"][SESSION]["+sessionVars+"]";
	}
	
	public void parseMessage(String message) {
		if (message.equals("") || !message.contains("[") || !message.contains("]")) return;
		StringTokenizer st = new StringTokenizer(message, "[]");
		st.nextToken();
		inputParas = st.nextToken();
		st.nextToken();
		sessionVars = st.nextToken();
	}
	
	public boolean equals(Object o) {
		if (o instanceof RequestMessage) {
			RequestMessage message = (RequestMessage)o;
			return (inputParas.equals(message.getInputParas()) && sessionVars.equals(message.getSessionVars()));
		}
		return false;
	}
	
	public int hashCode() {
		return 0;
	}
}