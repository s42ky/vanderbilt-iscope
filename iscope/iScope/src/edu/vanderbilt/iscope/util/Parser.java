package edu.vanderbilt.iscope.util;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.*;

import org.gibello.zql.ZDelete;
import org.gibello.zql.ZExp;
import org.gibello.zql.ZExpression;
import org.gibello.zql.ZFromItem;
import org.gibello.zql.ZInsert;
import org.gibello.zql.ZQuery;
import org.gibello.zql.ZSelectItem;
import org.gibello.zql.ZStatement;
import org.gibello.zql.ZUpdate;
import org.gibello.zql.ZqlParser;

import edu.vanderbilt.iscope.model.*;

public class Parser {
	
	public boolean printOn = false;
	private ZqlParser zql;
	
	public Parser() {
		zql = new ZqlParser();
	}
	
	public boolean checkNonAsciiExists(String s) {
		CharsetEncoder asciiEncoder = Charset.forName("US-ASCII").newEncoder(); // or "ISO-8859-1" for ISO Latin 1
		return !asciiEncoder.canEncode(s);
	}	
	
	// parse a string of input parameters.
	public HashMap<String, String> parseInputParameters(String paras) {
		HashMap<String, String> parameters = new HashMap<String, String>();
		if (paras.equals("")) return parameters;
		StringTokenizer st = new StringTokenizer(paras, "||");
		while (st.hasMoreTokens()) {
			String para = st.nextToken();
			StringTokenizer st1 = new StringTokenizer(para, ":");
			String name = st1.nextToken();
			if (st1.hasMoreTokens()) {
				String value = st1.nextToken();
				parameters.put(name, value);
			} else {
				parameters.put(name, "");
			}
			if (Character.isDigit(name.charAt(0))) parameters.remove(name);    // customized for scarf app.
		}
		return parameters;
	}
	
	public ZExp checkWhetherHaveWhere(String queryStatement) throws Exception {
		zql.initParser(new DataInputStream(new ByteArrayInputStream(queryStatement.getBytes("UTF-8"))));
		ZStatement zst = zql.readStatement();
		
		if(zst instanceof ZQuery) {
			ZQuery sel = (ZQuery)zst;
			return sel.getWhere();
		} else if(zst instanceof ZInsert) {
			return null;
		} else if(zst instanceof ZUpdate) {
			ZUpdate upd = (ZUpdate)zst;
			return upd.getWhere();
		} else if(zst instanceof ZDelete) {
			ZDelete del = (ZDelete)zst;
			return del.getWhere();
		} else {
			return null;
		}
	}
	
	/**
	 * parse a query statement into a SQLQuery obj
	 * @param queryStatement	"select * from ...."
	 * @param sqlQuery
	 */
	public SQLQuery parseQuery(String queryStatement) throws Exception {
		SQLQuery sqlQuery = new SQLQuery();		
		//System.out.println(queryStatement);
		if(queryStatement.contains("BEGIN"))	return sqlQuery;
		zql.initParser(new DataInputStream(new ByteArrayInputStream(queryStatement.getBytes("UTF-8"))));
		ZStatement zst = zql.readStatement();
		
		if(zst instanceof ZQuery) {
			sqlQuery.setType(SQLQuery.SQL_QUERY_TYPE_SELECT);
			ZQuery sel = (ZQuery)zst;
			
			/* get table names */
			Vector<ZFromItem> ts = sel.getFrom();
			for(ZFromItem zfi : ts) {
				sqlQuery.getTables().add(zfi.toString());
			}
			/* fields */
			Vector<ZSelectItem> fs = sel.getSelect();
			for(ZSelectItem zsi : fs) {
				String fieldStr = zsi.toString();
				if(fieldStr.equals("*")) {
					sqlQuery.getFields().add(sqlQuery.getTables().first() + ".*");
					continue;
				}
				// how about other operations like MIN, TODO
				if (fieldStr.contains("SUM")){       // Other operations: MIN, etc.
					fieldStr = fieldStr.substring(4, fieldStr.length()-1);
				}
				if (fieldStr.contains(".")) {
					sqlQuery.getFields().add(fieldStr);
				} else {
					sqlQuery.getFields().add(sqlQuery.getTables().first() + "." + fieldStr);
				}
			}
			parseWhereClause(sel.getWhere(), sqlQuery);
		} else if(zst instanceof ZInsert) {
			sqlQuery.setType(SQLQuery.SQL_QUERY_TYPE_INSERT);
			ZInsert ins = (ZInsert)zst;
			/* get insert objective */
			String table = ins.getTable();
			sqlQuery.getTables().add(table);
			/* field and value */
			Vector<String> columns = ins.getColumns();
			Vector<ZExp> values = ins.getValues();
			for(int i=0; i<columns.size(); i++) {
	        	String field = columns.elementAt(i);
	        	String value = values.elementAt(i).toString();
	        	sqlQuery.getFields().add(table+"."+field);
	        	sqlQuery.getValues().put(table+"."+field, value);
	        }
		} else if(zst instanceof ZUpdate) {
			sqlQuery.setType(SQLQuery.SQL_QUERY_TYPE_UPDATE);
			ZUpdate upd = (ZUpdate)zst;
			/* table */
			String table = upd.getTable();
			sqlQuery.getTables().add(table);
			/* field and values */
			Hashtable<String, ZExp> set = upd.getSet();
			Iterator<String> it = set.keySet().iterator();
			while (it.hasNext()){
				String field = it.next();
				sqlQuery.getFields().add(table+"."+field);
				String value = "";
				ZExp zp = set.get(field);
				if (zp instanceof ZExpression){
					String op1 = ((ZExpression) zp).getOperand(0).toString();
					String op2 = ((ZExpression) zp).getOperand(1).toString();
					value = op2;
				} else {
					value = zp.toString();
				}
				if (!value.equals("")){						
		        	sqlQuery.getValues().put(table+"."+field, value);
				}	        	
			}
			parseWhereClause(upd.getWhere(), sqlQuery);
		} else if(zst instanceof ZDelete) {
			sqlQuery.setType(SQLQuery.SQL_QUERY_TYPE_DELETE);
			ZDelete del = (ZDelete)zst;
			String table = del.getTable();
			sqlQuery.getTables().add(table);
			parseWhereClause(del.getWhere(), sqlQuery);
		}
		return sqlQuery;
	}
	
	/**
	 * parse "where ..."
	 * @param zp
	 * @param sqlQuery
	 */
	private void parseWhereClause(ZExp zp, SQLQuery sqlQuery){
		if (zp instanceof ZExpression) {
			ZExpression zex = (ZExpression) zp;
			if (zex.getOperator().toLowerCase().equals("and") || zex.getOperator().toLowerCase().equals("or")){   // recursive expression.
				parseWhereClause(zex.getOperand(0), sqlQuery);
				parseWhereClause(zex.getOperand(1), sqlQuery);
			} else {
				String field = zex.getOperand(0).toString();
				String value = "";
				if(zex.getOperands().size() > 1) {
					value = zex.getOperand(1).toString();
				}
				if (field.contains(".")) {
					sqlQuery.getFields().add(field);
				} else {
					field = sqlQuery.getTables().first()+"."+field;
					sqlQuery.getFields().add(field);
				}
				if (!value.contains("'") && value.contains(".")){
					sqlQuery.getFields().add(value);
				} else if(!value.equals("")){
					sqlQuery.getValues().put(field, value);
				}
			}
		} else if (zp instanceof ZQuery){               // recursive query.
			System.out.println("Recursive query: " + zp.toString());
		}
	}
	
	/**
	 * parse response
	 * @param response
	 * @param sqlQuery
	 * @return sqlResponse
	 */
	/*
	public SQLResponse parseResponse(String response, SQLQuery sqlQuery){
		SQLResponse sqlResponse = new SQLResponse();
		
		
		if(response.equalsIgnoreCase("SUCCESS") || response.equalsIgnoreCase("FALSE")) {
			sqlResponse.setType(SQLResponse.SQL_RESPONSE_TYPE_BOOLEAN);
			return sqlResponse;
		}
		
		
		StringTokenizer st = new StringTokenizer(response, "|");
		int row = -1, pt = 0;			// changed by yanwei, from 0 to -1
		while (st.hasMoreTokens()){
			String s = st.nextToken();
			if (s.equals(";")) {
				row ++;
				pt = 0;
				continue;
			}
			
			if (row == -1){       // column name.	
				if (s.equals("created_on_unix")) continue;
				if (s.contains("SUM")){                                     // Other operations: MIN, etc.
					s = s.substring(4, s.length()-1);
				}
				
				String col = sqlQuery.getFields().elementAt(pt);
				String table = col.substring(0, col.indexOf("."));
				String field = col.substring(col.indexOf(".")+1);
				if (field.equals("*")){
					sqlResponse.getColumns().add(table+"."+s);
					sqlResponse.getValues().put(table+"."+s, new Vector<String>());
				} else {
					if (s.contains("."))  s = s.substring(s.indexOf(".")+1);
					if (s.contains("max")) s = s.replace("max", "max1");
					if (field.equals(s)){
						sqlResponse.getColumns().add(table+"."+s);
						sqlResponse.getValues().put(table+"."+s, new Vector<String>());
						pt++;
					} else {
						//System.out.println("ERROR: column doesn't match."+ field + " " + s);
						///System.out.println(sqlQuery.getFields());
						//System.out.println(response+"\n");
					}
				}
			} else {               //column value.
				if (pt < sqlResponse.getColumns().size()){
					sqlResponse.getValues().get(sqlResponse.getColumns().get(pt++)).add(s);
					// here I change "pt" to "pt++"
				} else {
					//if (!response.contains("created_on_unix"))
						//System.out.println("Unseen field" +sqlResponse.getColumns().size() + " " + pt + " " + response);
				}
			}
		}
		if (row <= 1) {
			sqlResponse.setType(SQLResponse.SQL_RESPONSE_TYPE_ONE);
		} else if (row > 1){
			sqlResponse.setType(SQLResponse.SQL_RESPONSE_TYPE_MORE);
		}
		return sqlResponse;
	}	
	*/
	/**
	 * pre filter the query, to remove some uncared sequence
	 * @param query
	 * @return
	 */
	public String filter(String query){
		query = query.replaceAll("`", "");
		
		query = query.replace("COUNT(*)", "*");             // added for OpenIT
		query = query.replaceAll(" _", " ");                   // added for OpenIT
				
		
		if (query.contains("CONCAT")
				&& query.contains("SHA1") && query.contains("salt"))  {		// added by yanwei (two new conditions)
			String orig = query.substring(query.indexOf("SHA1"), query.indexOf("salt))")+6);
			String pwd = orig.substring(orig.indexOf('\''), orig.lastIndexOf('\'')+1);
			query = query.replace(orig, pwd);
		}
		//printOn = (query.substring(0, 6).equals("update"));
		
		if (printOn) System.out.println("0: " + query);

		if (query.contains("UNIX_TIMESTAMP")) {
			// the follow line has been changed by yanwei, query.indexOf("unix")+4 ---> query.indexOf("UNIX")+5
			String orig = query.substring(query.indexOf("UNIX")-1, query.indexOf("UNIX")+5);        // created_on_unix.
			query = query.replace(orig, "");
			if (query.contains("*,")) query = query.replace("*,", "* ");
			if (query.contains(",  from")) query = query.replace(",  from", " from");
		}
		
		if (printOn) System.out.println("1: " + query);
		
		if (query.contains("NOW"))   {
			query = query.replace("NOW", "TIME");
			query = query.replace("()", "");
			query = query.replace("( )", "");
		}
		if (query.contains(" LIMIT "))  query = query.substring(0, query.indexOf(" LIMIT "));
		if (query.contains(" limit "))  query = query.substring(0, query.indexOf(" limit "));
		if (!query.endsWith(";"))   query = query + ";";
		if (query.contains(" RAND "))  query = query.replace("order by RAND()", "");
		
		if (printOn) System.out.println("2: " + query);

		// added by yanwei, remove all pre parts of "AS"
		while(query.contains("TIMESTAMP") || query.contains("CONCAT")) {
			if(query.contains("TIMESTAMP")) {
				if (query.contains("AS")) query = this.removeASPart("TIMESTAMP", query);
			} else if(query.contains("CONCAT")) {
				if (query.contains("AS")) query = this.removeASPart("CONCAT", query);
				else if (query.contains("CONCAT(firstname, ' ', lastname)"))
					query = query.replace("CONCAT(firstname, ' ', lastname)", "firstname, lastname");
			}
		}
		
		if (query.contains("CAST")){                                 // added for OpenIT
			String sub = query.substring(query.indexOf("CAST"));
			//System.out.println(sub + "\n" + sub.indexOf(" AS "));
			String time = sub.substring(5, sub.indexOf(" AS "));
			query = query.replace("CAST("+time+" AS DATETIME)", time);
		}
				
		if (printOn) System.out.println("3: " + query);
		
		if(query.contains("LEFT JOIN ")) {
			String tempStr2 = query;
			while(tempStr2.contains("LEFT JOIN")) {
				int index1 = tempStr2.indexOf("LEFT JOIN");
				int index2 = tempStr2.substring(index1).indexOf(" ON") + index1;
				// get string like "LEFT JOIN phpbb_topics_watch tw ON"
				String tempStr1 = tempStr2.substring(index1, index2 + 3);
				String tempStrs1[] = tempStr1.split(" ");
				if(tempStrs1.length == 5) {
					query = query.replaceAll(" " + tempStrs1[3] + "\\.", " " + tempStrs1[2] + ".");
					query = query.replaceAll("\\(" + tempStrs1[3] + "\\.", "(" + tempStrs1[2] + ".");
				}
				// next
				tempStr2 = tempStr2.substring(index1 + 6);
			}
			
			int index1 = query.indexOf("LEFT JOIN");
			index1 = query.indexOf("LEFT JOIN"); 
			int index3 = query.indexOf("WHERE");
			if(index3 != -1) {
				/* query contains "where" */
				query = query.substring(0, index1) + query.substring(index3);
			} else {
				/* do not care about the left words */
				query = query.substring(0, index1-1) + ";";
			}
		}
		
		if (printOn) System.out.println("4: " + query);
		
		if(query.contains("INNER JOIN")) {
			int index1 = query.indexOf("INNER JOIN");
			int index2 = query.indexOf("WHERE");
			if(index2 != -1) {
				/* query contains "where" */
				query = query.substring(0, index1) + query.substring(index2);
			} else {
				/* do not care about the left words */
				query = query.substring(0, index1-1) + ";";
			}
		}
		
		if (printOn) System.out.println("5: " + query);
		
		if(query.contains("COUNT")) {
			query = this.removeASPart("COUNT", query);
		}
		if(query.contains("UNT")) {
			query = this.removeASPart("UNT", query);
		}
		if(query.contains("SUM")) {
			query = this.removeASPart("SUM", query);
		}
		//SELECT MAX( `order` ) as max FROM `papers` WHERE session_id = '12'
		if(query.contains("MAX")) {
			query = this.removeASPart("MAX", query);
		}
		if(query.contains("MIN")) {
			query = this.removeASPart("MIN", query);
		}
		//SELECT f.* FROM (phpbb_forums f) ORDER BY f.left_id
		if(query.contains("FROM (")) {
			int index1 = query.indexOf("FROM") + 6;
			int index2 = query.substring(index1).indexOf(")");
			String tableNames = query.substring(index1, index1+index2);
			if(!tableNames.contains(",")) {
				String firstTable = tableNames.split(" ")[0];
				String secondTable = tableNames.split(" ")[1];
				query = query.replaceAll("\\("+tableNames+"\\)", firstTable);
				query = query.replaceAll(" " + secondTable + "\\.", " " + firstTable + ".");
				query = query.replaceAll("\\(" + secondTable + "\\.", "(" + firstTable + ".");
			} else {
				String[] tablePairs = tableNames.split(", ");
				String str = "";
				for(int i=0; i<tablePairs.length; i++) {
					String firstTable = tablePairs[i].trim().split(" ")[0];
					String secondTable = tablePairs[i].trim().split(" ")[1];
					query = query.replaceAll(" " + secondTable + "\\.", " " + firstTable + ".");
					query = query.replaceAll(" \\(" + secondTable + "\\.", " (" + firstTable + ".");
					str += firstTable + ", ";
				}
				str = str.substring(0, str.length()-2);
				query = query.replaceAll("\\("+tableNames+"\\)", str);
			}
		}
		
		if (printOn) System.out.println("6: " + query);
		
		query = query.replaceAll("select", "SELECT");
		query = query.replaceAll("from", "FROM");
		query = query.replaceAll("where", "WHERE");
		query = query.replaceAll("update", "UPDATE");
		query = query.replaceAll("insert", "INSERT");
		//query = query.replaceAll("", "");
		//*
		query = query.replaceAll("order", "order1");
		query = query.replaceAll(" comment ", " comment1 ");
		query = query.replaceAll(" comment,", " comment1,");
		query = query.replaceAll("comment=", "comment1=");
		query = query.replaceAll("\\.comment ", ".comment1 ");
		query = query.replaceAll("date", "date1");
		query = query.replaceAll("max", "max1");
		query = query.replaceAll("LENGTH\\(code\\)", "code");
		query = query.replaceAll("FROM u, WHERE", "FROM u WHERE");
		query = query.replaceAll("FROM f, WHERE", "FROM f WHERE");
		
		query = query.replace("INSERT INTO admin VALUES", "INSERT INTO admin (id, uname, pword) VALUES");    // for events case.
		query = query.replace("INSERT INTO events VALUES", "INSERT INTO events (event, hour, minute, ampm, hour_end, minute_end, ampm_end, " +
				"month, day, year, month_end, day_end, year_end, month_show, day_show, year_show, location, email, phone, link, " +
				"link_name, description, html) VALUES");
		//*/
		return query;
	}
	
	/*
	private String removeASPart(String methodName, String query) {
		int index1 = query.indexOf(methodName);
		int index2 = query.substring(index1 + 1).indexOf("AS");
		return query.substring(0, index1) + query.substring(index1 + index2 + 3);
	}
	*/
	
	
	private String removeASPart(String methodName, String query) {
		int index1 = query.indexOf(methodName);
		int index2 = query.substring(index1 + 1).indexOf(" AS ");
		int index3 = query.substring(index1 + 1).indexOf(" as ");
		
		if(index2 == -1) {
			index2 = index3;
		} else {
			if(index3 == -1) {
				index2 = index2;
			} else {
				index2 = Math.min(index2, index3);
			}
		}
		
		index2 = (index2 == -1)? query.substring(index1+1).indexOf(" as ") : index2;
		return query.substring(0, index1) + 
				query.substring(index1 + index2 + 4);
	}
	
	
	/**
	 * 
	 * @param session
	 * @return
	 */
	public HashMap<String, String> parseSession(String session) {
		HashMap<String, String> sessionMap = new HashMap<String, String>();
		/* check null */
//		System.out.println(session);
		if(session==null || session.trim().equals("") || session.equals("null")) {
			return sessionMap;
		}
		
		session = sessfilter(session);
		StringTokenizer st = new StringTokenizer(session, ";");
		while (st.hasMoreTokens()){
			String var = st.nextToken();
			if (var.contains("*")){
				continue;
			}
			
			String key = var.substring(0, var.indexOf("|"));         //  Customized for dnscript, wackopicko
			String value = "";
			if (var.contains("\"")){
				value = var.substring(var.indexOf("\"")+1, var.lastIndexOf("\""));
			} else {
				value = var.substring(var.indexOf(":")+1);
			}
			if (!key.equals("") && !value.equals("")){
				sessionMap.put("session."+key, value);
				//System.out.println(key + " : " + value);
			}
		}
		
		return sessionMap;
	}
	
	private String sessfilter(String session){
		Stack<Integer> positions = new Stack<Integer>();
		boolean flag = false;
		int pt = 0;
		do {
			flag = false;
			int start = 0;
			int end = 0;
			StringBuffer result = new StringBuffer();
			char[] array = session.toCharArray();
			for (int i = pt; i < array.length; i++){
				if (array[i] == '{') {
					positions.push(i);
				} else if (array[i] == '}'){
					start = positions.pop();
					end = i;
					pt = start;
					flag = true;
					break;
				}
			}
			if (flag){
				result.append(session.substring(0, start));
				result.append("*;");
				result.append(session.substring(end+1));
				session = result.toString();
				//System.out.println(session.length() + " " + pt + " " + session);
			}
		} while (flag);
		return session;
	}
	
	public static void main(String args[]) throws Exception {
		Vector<String> tests = new Vector<String>();
		tests.add("SELECT * FROM events WHERE (year >= 2012 AND month > 05) OR (year >= 2012 AND month = 05 AND day >= 29) OR (year_end >= 2012 AND month_end = 05 AND day_end >= 29) OR (year > 2012) OR (year_show >= 2012 AND month_show > 05)OR (year_show >= 2012 AND month_show = 05 AND day_show >= 29) OR (year_show > 2012) ORDER BY year, month, day LIMIT 0, 10");
		tests.add("SELECT  News.* ,  `Departments`.`Description` as `dl_Description` ,  CONCAT(Employees.FirstName,' ',Employees.LastName) AS Employee_Display   FROM `News`  LEFT JOIN `openIT`.`News_Departments`   ON `openIT`.`News_Departments`.`NewsID`=`News`.`NewsID`  LEFT JOIN `openIT`.`Departments`   ON `openIT`.`Departments`.`ID`=`News_Departments`.`DepartmentID`    LEFT JOIN `openIT`.`Employees`   ON `openIT`.`Employees`.`EmployeeID`=`News`.`EmployeeID`    WHERE ( (ExpirationTime IS NULL OR ExpirationTime > CAST('2012-05-30 15:57:28' AS DATETIME)) )  AND ( News.Flag != 1 ) AND ( (News_Departments.NewsID IS NULL) )  GROUP BY News.NewsID    ORDER BY NewsID DESC, ExpirationTime  ");
		tests.add("SELECT MAX( `order` ) as max FROM `papers` WHERE session_id = '136'");
		tests.add("INSERT INTO papers (title, abstract, pdf, pdfname, session_id, `order`) VALUES ('(1) Security Analysis of India\'s Electronic Voting Machines', 'Abstract: (1) Security Analysis of India\'s Electronic Voting Machines', '', '', '157', '1')");
		tests.add("SELECT session_id, name, UNIX_TIMESTAMP(starttime) AS starttime, UNIX_TIMESTAMP(starttime + INTERVAL duration MINUTE) AS endtime, CONCAT(firstname, ' ', lastname) AS fullname FROM sessions LEFT JOIN users ON sessions.user_id = users.user_id ORDER BY starttime");
		
		for(String test : tests) {
			Parser p = new Parser();
			p.printOn = true;
			
			System.out.println(test);
			test = p.filter(test);
			System.out.println(test);
			try {
				SQLQuery query = p.parseQuery(test);
				System.out.println(query.getValues());
				System.out.println(query.getFields());
			} catch (Exception e){
				//e.printStackTrace();
				System.err.println("Nope.");
				continue;
			}
			System.out.println("\n------------------------------\n");
		}
	}
}