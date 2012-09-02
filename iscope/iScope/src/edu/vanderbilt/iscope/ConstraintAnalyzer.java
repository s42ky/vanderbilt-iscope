package edu.vanderbilt.iscope;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import edu.vanderbilt.iscope.model.SigProfile;
import edu.vanderbilt.iscope.util.QueryParser;
import edu.vanderbilt.iscope.util.Parser;
import edu.vanderbilt.iscope.util.SQLSymbolizer;
import edu.vanderbilt.iscope.util.StateConstructor;

class ConstraintAnalyzer {
	public static Logger LOGGER = Logger.getLogger(ConstraintAnalyzer.class.toString());
	
	public static void main(String args[]) {
		BasicConfigurator.configure(new ConsoleAppender(new PatternLayout("%-5p %35.35C{2}: %m%n")));
		LOGGER.getRootLogger().setLevel(Level.WARN);
		
		//String dir = "C:/Users/xiaowei/Desktop/acsac/";
		String dir = Portal.workingDir;
		String project = Portal.project;
		
		ConstraintAnalyzer _analyzer = new ConstraintAnalyzer(dir, project);
		try {
			_analyzer.profileVariables();
			
			_analyzer.collectTraceForSig();
			_analyzer.inferConstraints();
			_analyzer.outputConstraints();
			_analyzer.outputTraceSummary();
			
			//Output query list
			System.out.println("\nQueries:");
			for(String q : QueryParser.sql_queries.keySet())
				System.out.println(q + " (" + QueryParser.sql_queries.get(q) + "occurrances)");
			
			//for(String q : Parser_JSql.request_profiles)
			//	System.out.println(q);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public ConstraintAnalyzer(String dir, String proj) {
		workingDir = dir;
		project = proj;
		traceDir = workingDir + project + "/";
		_sigConstructor = new SQLSymbolizer(workingDir, project);
		_stateConstructor = new StateConstructor();
	}
	
	private String workingDir;
	private String project;
	private String traceDir;
		
	SQLSymbolizer _sigConstructor;
	StateConstructor _stateConstructor;
	QueryParser _parser = new QueryParser();
	
	private HashMap<String, SigProfile> sigProfiles = new HashMap<String, SigProfile>();
	
	public void profileVariables() throws Exception {
		
		BufferedReader br = new BufferedReader(new FileReader(traceDir + project + ".log"));
		
		try {
			BasicConfigurator.configure(new FileAppender(
					new PatternLayout("%-7r %-5p %35.35C{2}: %m%n"),
								traceDir+"ConstraintAnalyzerTrace.log", false));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		int queryNum = 0;
		int requestNum = 0;
		
		String line;
		//int lineCount = 0;	
		while ((line=br.readLine())!=null){
			//System.out.println(++lineCount);
						
			StringTokenizer st = new StringTokenizer(line, "[]");
			if(!st.hasMoreTokens()) continue;
			
			String head = st.nextToken();          // [QUERY] or [REQUEST]
			
			if (head.equals("REQUEST")) {
				requestNum ++;
				st.nextToken();    // GET or POST
				st.nextToken();                    // [SCRIPT]
				String script = st.nextToken();    // script
				System.out.println(script);
				
				try {
					script = script.substring(script.indexOf(project)+project.length()+1, script.indexOf(".php"));
				} catch(StringIndexOutOfBoundsException e) {
					script = "index";
				}
				script = script.replaceAll("/", ":"); // multi level.
				
				st.nextToken();                    // [SESSION]
				String session = st.nextToken();
				_stateConstructor.addTrace(_parser.parseSession(session));
				
				try {
					st.nextToken(); //[TIMESTAMP]
					st.nextToken();
					
					//Cache request parameters for following queries
					st.nextToken(); //[PARA]
					String params = st.nextToken();
					_parser.parseInputParameters(params);
				} catch(NoSuchElementException e) {
					//Do nothing for now
				}
			} else if (head.equals("QUERY")) {
				LOGGER.debug("Found a query on '"+line+"'");
				queryNum ++;
				String queryStatement = st.nextToken();

				if (_parser.checkNonAsciiExists(queryStatement)) {
					System.out.println("{"+queryStatement+"}");
					LOGGER.warn("NON-ASCII");
					continue;
				}
				if (queryStatement.equals("_")||queryStatement.equals("__"))  {
					System.out.println("{"+queryStatement+"}");
					LOGGER.warn("BLANK STATEMENT");
					continue;                                        // dnscript filtering non-ascii
				}
				
				//if(!st.hasMoreTokens()) continue;
				
				st.nextToken();                     // [SCRIPT]
				String script = st.nextToken();     // script
				
				if (script.equals("") || 
						!(script.contains(Portal.wwwroot) || script.contains(Portal.host)))  {
					
					LOGGER.warn("Script '"+script+"' not valid for line '"+line+"' . Aborting.");
					continue;
				}
				_parser.parseGetParameters(script);				
				
				st.nextToken();                     // [SESSION]
				String session = st.nextToken();    // session
				
				HashMap<String,String> sessionVars = _parser.parseSession(session);
				_stateConstructor.addTrace(sessionVars);
				
				try {
					LOGGER.info("Trying add trace.");
					_sigConstructor.addTrace(queryStatement);
				} catch(Exception e) {
					System.err.println("Error adding query:");
					System.err.println(queryStatement);
					e.printStackTrace();
				}
			} else {
				System.err.println("U:"+head);
			}
		}
		br.close();
		
		_stateConstructor.analyzeSessionVariable();
		_stateConstructor.outputXMLSessionProfile(new FileOutputStream(traceDir+"sessionProfile.xml"));
		
		_sigConstructor.analyzeQueryParameter();
		_sigConstructor.printSchema();
		System.out.println("Number of web requests: " + requestNum);
		System.out.println("Number of sql queries: " + queryNum);
	}
	
	
	public void collectTraceForSig() throws Exception {
		_stateConstructor.loadXMLSessionProfile(new File(traceDir+"sessionProfile.xml"));
		_sigConstructor.loadSchema();
		
		BufferedReader br = new BufferedReader(new FileReader(traceDir + project + ".log"));
		
		String requestKey = "";
		String entryState = "";
		String inputParas = "";
		HashSet<String> requestKeySet = new HashSet<String>();
		
		String line;
		int lineCount = 0;
		while ((line=br.readLine())!=null){
			lineCount ++;
			
			StringTokenizer st = new StringTokenizer(line, "[]");
			if(!st.hasMoreTokens()) continue;
			String head = st.nextToken();          // [QUERY] or [REQUEST]	
			
			if (head.equals("REQUEST")) {
				String method = st.nextToken();    // GET or POST
				st.nextToken();                    // [SCRIPT]
				String script = st.nextToken();    // script

				// parse parameters in script here.
				if (script.indexOf(".php?") != -1) {
					inputParas = script.substring(script.indexOf(".php?")+5);
					inputParas = inputParas.replaceAll("&", "||");
					inputParas = inputParas.replaceAll("=", ":");
					inputParas += "||";
					//System.out.println("inputPARA: " + inputParas);
				} else {
					inputParas = "";
				}
				try {
					script = script.substring(script.indexOf(project)+project.length()+1, script.indexOf(".php"));
				} catch(StringIndexOutOfBoundsException e) {
					script = "index";
				}
				script = script.replaceAll("/", ":"); // multi level
				requestKey = method + "-" + script;
				requestKeySet.add(requestKey);
				//System.out.println(requestKey);
				
				st.nextToken();                    // [SESSION]
				String entrySession = st.nextToken();
				entryState = _stateConstructor.genStateSig(entrySession);
				_stateConstructor.addToStateSet(entryState);
				//System.out.println(entrySession + "\n" + entryState);
				
				st.nextToken();                   // [TIMESTAMP]
				st.nextToken();
				
				st.nextToken();                    // [PARA]
				if (st.hasMoreTokens()) {
					inputParas += st.nextToken();  // A string of parameters.
				}
				
			} else if (head.equals("QUERY")) {
				String queryStatement = st.nextToken();
				if (_parser.checkNonAsciiExists(queryStatement)) {
					continue;
				}
				if (queryStatement.equals("_")||queryStatement.equals("__"))  {
					continue;          // dnscript filtering non-ascii
				}
				st.nextToken();                     // [SCRIPT]
				String script = st.nextToken();     // script
				if (script.equals("") || 
						!(script.contains(Portal.wwwroot) || script.contains(Portal.host)))  continue;
				
				st.nextToken();                     // [SESSION]
				String sessionVars = st.nextToken();    // session		
				_stateConstructor.addToStateSet(_stateConstructor.genStateSig(sessionVars));
				//System.out.println(sessionVars + "\n" + _stateConstructor.genStateSig(sessionVars));
				
				String signature = _sigConstructor.genSQLSig(queryStatement, script);  // generate sql signature from query.	
				if (signature.equals("")) {
					System.err.println("ERROR: symbolizing... " + queryStatement);
					continue;
				}
				//System.out.println(signature);
				
				if (!sigProfiles.containsKey(signature)) {
					sigProfiles.put(signature, new SigProfile(signature));
				}
				if (!requestKey.equals("")) {
					sigProfiles.get(signature).addTrace(requestKey, inputParas, sessionVars, entryState);
				}
			}
		}
		br.close();
		_stateConstructor.outputStateSet(traceDir + "stateSet");
		System.out.println("Number of SQL signatures: " + sigProfiles.size());
		System.out.println("Number of request keys: " + requestKeySet.size());
	}
		
	public void inferConstraints() {
		// infer constraints for each signature.
		for (String sig : sigProfiles.keySet()) {
			sigProfiles.get(sig).genConstraints();
		}
	}
	
	public void outputConstraints() {
		try {
			Element root = new Element("SigConstProfile");
			for (String sig: sigProfiles.keySet()) {
				Element profile = sigProfiles.get(sig).genXMLSigConstProfileElement();
				root.addContent(profile);
			}
			Document doc = new Document(root);
			XMLOutputter serializer = new XMLOutputter(Format.getPrettyFormat());
		    serializer.output(doc, new FileOutputStream(traceDir+"SigConstProfile.xml"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void outputTraceSummary() {
		try {
			Element root = new Element("TraceSummary");
			for (String sig: sigProfiles.keySet()) {
				Element summary = sigProfiles.get(sig).genXMLTraceSumElement();
				root.addContent(summary);
			}
			Document doc = new Document(root);
			XMLOutputter serializer = new XMLOutputter(Format.getPrettyFormat());
		    serializer.output(doc, new FileOutputStream(traceDir+"TraceSummary.xml"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}