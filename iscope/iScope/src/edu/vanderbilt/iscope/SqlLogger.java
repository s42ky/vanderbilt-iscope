/**
 * @author Scott Yeskie
 * 
 * @brief Java implementation of UNIX tail command
 * 		Writes to project log file
 */

package edu.vanderbilt.iscope;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.StringTokenizer;

import edu.vanderbilt.iscope.model.QueryMessage;
import edu.vanderbilt.iscope.util.SessionInspector;

public class SqlLogger {
	private static SessionInspector _inspector;
	//private static String sqlLog = "/tmp/mysqld.sql";
	private static String sqlLog = "/tmp/rtlog.sql";
	static long sleepTime = 10;
	
	private static String workingDir = Portal.workingDir;
	private static String project = Portal.project;
	private static String dbname = Portal.dbname;
	private static String traceDir = workingDir + project + "/";
	
	private static HashMap<String, String> connections = new HashMap<String,String>();
	
	private static void writeLog(String query) throws IOException {
		if(query.length()==0) return; //Don't log empty strings
		
		String script = "{null}";
		String session = "null";
		String session_req = "null";
		String sid = null;
		try {
			BufferedReader br = new BufferedReader(new FileReader(traceDir + "last_req.tmp.log"));
			script = br.readLine();
			sid = br.readLine();
		    session_req = br.readLine();
		    //String time = br.readLine();
		    if (sid!= null && sid!="null") {
			    session = _inspector.inspect(sid);
			}
		    if(! session.equals(session_req)) {
		    	System.err.println("Session values have changed.");
		    }
		    
		    br.close();
		} catch(FileNotFoundException e) {
			System.err.println("Request info not found.");			
		}
		
	    Date now = new Date();
	    String time = Long.toString(now.getTime());
	    
	    //String session = "null";
	    //if (sid!= null) {
	    //	//session = _controller.inspect(sid);
	    //	session = _inspector.inspect(sid);
	    //}
	    //if (script!=null && script.equals(wwwroot+"test.php")) return;
	    
	    if (Portal.getMode() == 0) {
	    	BufferedWriter bw = new BufferedWriter(new FileWriter(traceDir + project + ".log", true));
			if (session != null && query != null && script != null) {    // QUERY + SCRIPT + SESSION + TIME.
				bw.write("[QUERY][" + query + "]");
				bw.write("[SCRIPT][" + script + "]");
				bw.write("[SESSION][" + session + "]");
				if (time != null) bw.write("[TIMESTAMP][" + time + "]");
				bw.write("\n");
				System.out.println(project + " " + query + " time " + time);
			}
			bw.close();
	    } else if (Portal.getMode() == 1) {
	    	QueryMessage message = new QueryMessage(query, script, session);
	    	TestingEngine.addQueryMessage(message);
	    }
	}
	
	public static void main(String args[]) throws Exception {
		System.out.println("Query logger startup...");
		//Startup
		BufferedReader input = new BufferedReader(new FileReader(sqlLog));
		_inspector = new SessionInspector();
		
		String line = null;
		String id = null;
		String cmd = null;
		String arg = null;
		Boolean startup = true;
		
		Boolean needSpace = false;
		
		Boolean buildingQuery = false;
		String curQuery = "";
		
		
		while(true) {
			
			
			if ((line = input.readLine()) != null) {
				//Ignore lines already existing in log
				if(startup) continue;
				
				if(line.equals("{")) {
					buildingQuery = true;
					needSpace = false;
					curQuery = "";
				} else if(line.equals("}")) {
					buildingQuery = false;
					writeLog(curQuery);
				} else if(buildingQuery) {
					if(needSpace) curQuery += " ";
					curQuery += line;
					needSpace = (line.length()>0) && (!line.substring(line.length()-1).equals(" "));
				}
				/*
				//Kill log timestamps
				line = line.replaceFirst("[0-9]{6} [0-9:]{8}","");
				
				StringTokenizer st = new StringTokenizer(line);
				id = st.nextToken();
				cmd = st.nextToken();
				
				
				if(connections.containsKey(id)) {
					if(buildingQuery) {
						buildingQuery = false;
						writeLog(curQuery);
					}
					
					if(cmd.equals("Query")) {
					
						//TODO make this work with multiple spaces
						arg=line.substring(line.indexOf("Query")+5).trim();
						
						buildingQuery = true;
						curQuery = arg;
					} else if(cmd.equals("Quit")) {
						connections.remove(id);
						System.out.println("Closing connection: #"+id);
					}
				} else {
					if(cmd.equals("Init")) {
						st.nextToken(); //Throw away "DB"
						arg = st.nextToken();
						if(arg.equals(dbname)) {
							System.out.println("Connection logged: #"+id);
							connections.put(id, arg);
						} else {
							System.out.println("Connection to untracked DB");
						}
					} else if(cmd.equals("Quit") || cmd.equals("Connect")) {
						if(buildingQuery) {
							buildingQuery = false;
							writeLog(curQuery);
						}
					} else {
						//Query overflow
						if(buildingQuery) {
							curQuery += " " + line;
						}
					}
				}//*/
				continue;
			}

	        try {
	        	if(startup) {
	        		startup = false;
	        		System.out.println("Logger ready.");
	        	}
	        	Thread.sleep(sleepTime);
	        } catch (InterruptedException e) {
	        	Thread.currentThread().interrupt();
	        	break;
	        }

	      }
	      input.close();
	
	}

}
