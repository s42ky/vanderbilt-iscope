package edu.vanderbilt.iscope.util;

import java.io.*;
import java.util.*;

import org.apache.log4j.Logger;

import edu.vanderbilt.iscope.model.SQLQuery;
import edu.vanderbilt.iscope.model.Table;
import edu.vanderbilt.iscope.model.Variable;
import edu.vanderbilt.iscope.util.Parser;

public class SQLSymbolizer {
	public static Logger LOGGER = Logger.getLogger(SQLSymbolizer.class.toString());
	
	private String workingDir;
	private String project;
	private String traceDir;
	
	private HashMap<String, Table> dataStructs = new HashMap<String, Table>();
	private HashMap<String, Integer> varProfiles = new HashMap<String, Integer>();

	// SQL Parser
	private QueryParser _parser = new QueryParser();
	
	public SQLSymbolizer(String dir, String proj) {
		workingDir = dir;
		project = proj;
		traceDir = workingDir + project + "/";
	}
	
	public void addTrace(String queryStatement) throws Exception {
		LOGGER.info("Calling addTrace.");
		//System.out.println("filter BEFORE: " + queryStatement);
		queryStatement = _parser.filter(queryStatement);     // preprocess query
		//System.out.println("filter AFTER0: " + queryStatement);
		SQLQuery sqlQuery = _parser.parseQuery(queryStatement);       // parse SQL query
		if(sqlQuery.getType() == SQLQuery.SQL_QUERY_TYPE_UNDEFINED)	 {
			System.err.println("Unknown query type.");
			return;
		}
		/* table */
		for(String table : sqlQuery.getTables()) {
			if(!dataStructs.containsKey(table)) {
				dataStructs.put(table, new Table(table));
				LOGGER.info("Add new table: " + table);
			}
		}
		/* field */
		for(String field : sqlQuery.getFields()) {                // field = table.column
			String t = field.substring(0, field.indexOf("."));
			String f = field.substring(field.indexOf(".")+1);
			if (!dataStructs.containsKey(t)) {
				dataStructs.put(t, new Table(t));
				LOGGER.info("Add new table: " + t + " orig field: " + field);
			}
			if (! f.equals("*")){
				if (! dataStructs.get(t).getFields().containsKey(field)){
					dataStructs.get(t).getFields().put(field, new Variable(field));
					LOGGER.info("Add new field: " + field);
				}
			}
		}
		/* values */
		Iterator<String> it = sqlQuery.getValues().keySet().iterator();     // Corresponds to getFields().
		while(it.hasNext()) {
			String field = it.next();
			String value = sqlQuery.getValues().get(field);
			String t = field.substring(0, field.indexOf("."));				
			Variable fd = dataStructs.get(t).getFields().get(field);
			fd.addValue(value);
		}	
	}
		
	public void analyzeQueryParameter() {		
		for (String table: dataStructs.keySet()) {
			for (String field: dataStructs.get(table).getFields().keySet()) {
				Variable parameter = dataStructs.get(table).getFields().get(field);
				parameter.testDomainType();
				if (parameter.getDomainType() == Variable.BOUNDED) {
					System.out.println(parameter.getName() + " : bounded"); // cart_items.cart_id, comments.id, comments_preview.id, users.tradebux, own.id, pictures.user_id, cart.id
				} else if (parameter.getDomainType() == Variable.UNBOUNDED) {
					System.out.println(parameter.getName() + " : unbounded");
				} else {
					System.out.println(parameter.getName() + " : unspecified");
				}
			}			
		}
	}
	
	
	/*
	 * Print database schema to both file and screen.
	 */
	public void printSchema() throws Exception {
		BufferedWriter bw = new BufferedWriter(new FileWriter(traceDir + project + ".schema"));
		for (String table: dataStructs.keySet()) {
			System.out.println("Table: " + table);
			for (String field: dataStructs.get(table).getFields().keySet()) {
				bw.write(field + " " + dataStructs.get(table).getFields().get(field).getDomainType() + "\n");
				System.out.print(" | " + field);
			}
			System.out.print(" |\n");
		}
		bw.close();
	}
	
	/*
	 * Load database schema from file. (and if the field is bounded)
	 */
	
	public HashMap<String, Integer> loadSchema() throws Exception {
		BufferedReader br = new BufferedReader(new FileReader(traceDir + project + ".schema"));
		String line;
		while((line=br.readLine())!=null){
			StringTokenizer st = new StringTokenizer(line);
			String field = st.nextToken();
			Integer flag = Integer.parseInt(st.nextToken());
			varProfiles.put(field, flag);
		}
		br.close();
		return varProfiles;
	}
	
	public void loadSigProfile() throws Exception {
		varProfiles = new HashMap<String, Integer>();
		BufferedReader br = new BufferedReader(new FileReader(traceDir + project + ".schema"));
		String line;
		while((line=br.readLine())!=null){
			StringTokenizer st = new StringTokenizer(line);
			String field = st.nextToken();
			Integer flag = Integer.parseInt(st.nextToken());
			varProfiles.put(field, flag);
		}
		br.close();		
	}
	
	// Construct SQL signature given a SQL query statement.
	public String genSQLSig(String queryStatement, String script) throws Exception {
		String signature = "";
		
		if (_parser.checkNonAsciiExists(queryStatement)) return signature;
		if (queryStatement.equals("_")||queryStatement.equals("__"))  return signature;
		
		//queryStatement = _parser.filter(queryStatement);
		//SQLQuery sqlQuery = _parser.parseQuery(queryStatement);
		queryStatement = _parser.parse_to_sig(queryStatement);
		//if (sqlQuery.getType() == SQLQuery.SQL_QUERY_TYPE_UNDEFINED)  return signature;
		
		/*for (String field : sqlQuery.getValues().keySet()) {
			String value = sqlQuery.getValues().get(field);
			int flag = -1;
			if(varProfiles.get(field) != null) {
				flag = varProfiles.get(field);
			} else {
				System.err.println("Couldn't find field '"+field+"'");
			}
			
			if (flag == 1){           // unbounded.
				String token = field + ".CONSTANT";
				//if (lineCount == 3671) System.out.println(queryStatement + field + value);
				queryStatement = queryStatement.replace(value, token);
			//} else if (flag == 0){
				//System.out.println("Keep bounded field: " + field);
			}
		}//*/
		// For openInvoice special case:
		/*
		if (queryStatement.contains("INSERT INTO")){
			//System.out.println(queryStatement);
			String valuestr = queryStatement.substring(queryStatement.indexOf("("), queryStatement.indexOf(")")+1);
			queryStatement = queryStatement.replace(valuestr, "( VALUES.CONSTANT )");
		}
		*/
		signature = "[" + script + "][" + queryStatement + "]";
		return signature;
	}
	
	/*
	public void preprocess() throws Exception {  //openInvoice.
		BufferedReader br = new BufferedReader(new FileReader(workingDir + project + file));
		BufferedWriter bw = new BufferedWriter(new FileWriter(path + project + ".log.filter"));
		int lineCount = 0;
		String line;
		while((line=br.readLine()) != null) {
			lineCount ++;
			// pre-process.
			line = line.replace("[SESSION][]", "[SESSION][null]");          // openInvoice.			
			//if (line.contains("INSERT INTO `oi_customers`") || line.contains("INSERT INTO `oi_invoices`") || line.contains("INSERT INTO `oi_invoice_items`")){               // openInvoice: fix new line.
			//if (line.contains("INSERT INTO") && !line.contains("[SESSION]")){
			if (!line.contains("[SESSION]") && line.contains("[QUERY]")){   // concatenation.
				//System.out.println(line);
				String newline;
				do {
					newline = br.readLine();
			     	line = line.trim() + newline;
					lineCount++;
				} while (!newline.contains("[SESSION]"));
				//System.out.println(line);
			}
			bw.write(line+"\n");
		}
		file = ".log.filter";
		br.close();
		bw.close();
	}
	*/
}
