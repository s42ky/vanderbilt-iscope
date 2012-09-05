package edu.vanderbilt.webtest.util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.owasp.webscarab.model.Request;

public class SqlLogger {
	public final Logger LOGGER = Logger.getLogger(SqlLogger.class.getName());
	
	
	SessionInspector _inspector = new SessionInspector();
	BufferedReader input;
	private Boolean sql_input_log_ready = false;
	
	private Request lastRequest = null;
	private String lastRequestSession = null;
	private String lastRequestSID = null;
	
	public String getSID() { return lastRequestSID; }
	public String getLastScript() {
		if(lastRequest==null) return null;
		return lastRequest.getURL().toString();
	}
	
	public SqlLogger(String sqlLog) {
		try {
			input = new BufferedReader(new FileReader(sqlLog));
			
			flushLog();
			sql_input_log_ready = true;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			sql_input_log_ready = false;
		}
	}
	
	
	public void processRequest(Request request) {
		String cookie = request.getHeader("Cookie");
		String ssindex = "";
		if (cookie != null) {
			StringTokenizer st = new StringTokenizer(cookie, " ;");
			//Scan for PHPSESSID
			while(st.hasMoreTokens()) {
				String c = st.nextToken();
				if(c.substring(0,c.indexOf("=")).equals("PHPSESSID")) {
					ssindex = c.substring(c.indexOf("=")+1).trim();
					break;
				}
			}
			//System.out.println(c);
		}
		
		//Cache values for when processing queries
	    lastRequestSID = ssindex;
	    lastRequestSession = _inspector.getSession(ssindex);
	}
	
	public void flushLog() {
		try {
			while (input.readLine() != null) {
				continue;
			}
		} catch (IOException e) {
			e.printStackTrace();
			sql_input_log_ready = false;
		}
	}
	
	public Vector<String> getQueriesLogged() {
		Vector<String> queryTraces = new Vector<String>();
		
		if(!sql_input_log_ready) {
			LOGGER.error("SQL log not available for reading.");
			return queryTraces;
		}
		
		String line = null;
		Boolean needSpace = false;
		
		Boolean buildingQuery = false;
		StringBuilder curQuery = new StringBuilder("");
		
		try {
			while ((line = input.readLine()) != null) {
				if(line.equals("{")) {
					buildingQuery = true;
					needSpace = false;
					curQuery = new StringBuilder("");
				} else if(line.equals("}")) {
					buildingQuery = false;
					
					//Ignore empty queries
					if(curQuery.length()==0) continue;

					String session = "null";
					String sid = lastRequestSID;
					if (sid!= null && sid!="null") {
					    session = _inspector.inspect(sid);
					}
					
					curQuery.append("][SCRIPT][");
					curQuery.append((lastRequest==null)?"null":lastRequest.getURL().toString());
					curQuery.append("][SESSION][");
					curQuery.append(session);
					curQuery.append("][TIMESTAMP][");
					curQuery.append(String.valueOf(System.currentTimeMillis()));
					curQuery.append("]");
					
					queryTraces.add("[QUERY]["+curQuery.toString());
				} else if(buildingQuery) {
					if(needSpace) curQuery.append(" ");
					curQuery.append(line);
					needSpace = (line.length()>0) && (!line.substring(line.length()-1).equals(" "));
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			sql_input_log_ready = false;
		}
		
		return queryTraces;
	}
}
