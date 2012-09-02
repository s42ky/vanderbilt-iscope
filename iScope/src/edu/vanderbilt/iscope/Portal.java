package edu.vanderbilt.iscope;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.vanderbilt.iscope.model.QueryMessage;
import edu.vanderbilt.iscope.util.SessionInspector;
/**
 * Servlet implementation class Portal
 */
public class Portal extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	public static final String project = "scarf";
	public static final String dbname = project;
	private static final int mode = 0;    // 0: Training mode; 1: Testing mode
	
	
	public static final String workingDir = "/srv/logger/";
	public static final String host = "localhost:80";
	public static final String wwwroot = "/srv/htdocs/";

	private static String traceDir = workingDir + project + "/";
	public static int getMode() { return mode; }	
	
	private static SessionInspector _inspector;
	private static TestingEngine _engine = null;
    
	public static void main(String[] args) {
		//Let's just try running on local JVM and not Tomcat
		if (getMode() == 1 && _engine==null) {
        	_engine = new TestingEngine(workingDir, project, host);
        	try {
        		_engine.run();
        		
        	} catch( Exception e) {
        		e.printStackTrace();
        	}
		}
	}
	
	
	
	
	/**
     * @see HttpServlet#HttpServlet()
     */
    public Portal() throws Exception {
        super();
        _inspector = new SessionInspector();
        /*
        if (_engine==null) {
        	_engine = new TestingEngine(workingDir, project, host);
        	_engine.run();
        }
        */
    }
    
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		if (getMode() == 1 && _engine==null) {
        	_engine = new TestingEngine(workingDir, project, host);
        	try {
        		_engine.run();
        		
        		/*
        		String sig = "["+wwwroot+"securephoto/comments/preview_comment.php][INSERT INTO comments_preview (id, text, user_id, picture_id, created_on) VALUES (NULL, comments_preview.text.CONSTANT, comments_preview.picture_id.CONSTANT, comments_preview.picture_id.CONSTANT, TIME);]";
        		String entry = "POST-comments:preview_comment";
        		String inputParas = "picid:13||text:Comments from lixw||";
        		String sessVars = "userid|s:2:\"13\";";
            	_engine.debugAtom(sig, entry, inputParas, sessVars);
            	*/
        		/*
        		String sig = "["+wwwroot+"scarf/generaloptions.php][UPDATE options SET value=options.value.CONSTANT WHERE name='Conference Name';]";
        		String entry = "POST-generaloptions";
        		String inputParas = "Conference Name:Vanderbilt Conference Management System||Is Forum Moderated (emails the admins on every post):0||submit:Save Options||";
        		String sessVars = "null";
            	_engine.debugAtom(sig, entry, inputParas, sessVars);
            	*/
        		/*
        		String sig = "["+wwwroot+"openIT/index.php][UPDATE  Employees  SET Contractor = 0 , ITTechnician = 0 , FirstName = Employees.FirstName.CONSTANT , LastName = Employees.LastName.CONSTANT , MiddleInitial = '' , WorkPhone = '' , HomePhone = '' , MobilePhone = '' , Fax = '' , EmailAddress = '' , Notes = '' , HomeStreetAddress = '' , HomePOBox = '' , HomeCity = '' , HomeState = '' , HomeZip = '' , Password = '7c222fb2927d828af22f5921Employees.GroupID.CONSTANT4e89Employees.GroupID.CONSTANT24806Employees.GroupID.CONSTANT7c0d' , GroupID = Employees.GroupID.CONSTANT , Language = 'en-US' , JabberAccount = '' , PreferredNotification = 'none' , data = 'a:0:{}' , Flag = 0   WHERE (  Employees.EmployeeID = Employees.EmployeeID.CONSTANT )  ;]";
        		String entry = "POST-index";
        		String inputParas = "employee_LastName:Kaiser||employee_HomeStreetAddress:||Edit:Employees||employee_Language:en-US||employee_JabberAccount:||employee_Department:0||employee_WorkPhone:||employee_MiddleInitial:||employee_Fax:||employee_ClearPassword:12345678||employee_HomeZip:||employee_Campus:0||KeepPassword:1||employee_HomePhone:||employee_Notes:||employee_MobilePhone:||employee_HomePOBox:||employee_EmployeeID:NOT{,26,}||ID:26||employee_HomeState:||employee_FirstName:Evan||employee_HomeCity:||employee_EmailAddress:||";
        		String sessVars = "Language|s:5:\"en-US\";EmployeeID|s:2:\"26\";Group|s:1:\"3\";EmployeeName|s:12:\"Kaiser, Evan\";Department|N;SignedOn|i:1338411496;Login_Text|s:34:\"Already logged in as Kaiser, Evan.\";";
        		//String sessVars = "null";
        		_engine.debugAtom(sig, entry, inputParas, sessVars);
        		
        		/*
        		String sig = "["+wwwroot+"events/admin/add.php][INSERT INTO events (event, hour, minute, ampm, hour_end, minute_end, ampm_end, month, day, year, month_end, day_end, year_end, month_show, day_show, year_show, location, email, phone, link, link_name, description, html) VALUES (events.year_end.CONSTANT,events.hour.CONSTANT,events.minute.CONSTANT,events.ampm.CONSTANT,events.hour_end.CONSTANT,events.year_end.CONSTANT,events.year_end.CONSTANT,events.year_end.CONSTANT,events.minute.CONSTANT,events.year.CONSTANT,events.month_end.CONSTANT,events.year_end.CONSTANT,events.year_end.CONSTANT,events.year_end.CONSTANT,events.year_end.CONSTANT,events.year_end.CONSTANT,events.year_end.CONSTANT,events.email.CONSTANT,events.year_end.CONSTANT,events.year_end.CONSTANT,events.year_end.CONSTANT,events.year_end.CONSTANT,events.html.CONSTANT,'0');]";
        		String entry = "POST-admin:add";
        		String inputParas = "ud_year_show:||ud_description: Note Tue May 29 17:55:46 CDT 2012||ud_day_show:||ud_minute:15||ud_link_name:||ud_minute_end:||ud_hour_end:||ud_phone:||ud_year_end:||submit:Add Event||ud_location:FGH Tue May 29 17:55:46 CDT 2012||ud_month_show:||ud_hour:06||ud_link:||ud_month_end:||ud_email:||ud_ampm_end:||ud_html:0||ud_year:2013||ud_day_end:||ud_event:event Tue May 29 17:55:45 CDT 2012||ud_ampm:AM||ud_month:06||ud_day:18||";
        		String sessVars = "userName|s:14:\"test@email.com\";validUser|b:1;";
		        _engine.debugAtom(sig, entry, inputParas, sessVars);
        		*/
        		/*
        		String sig = "["+wwwroot+"events/admin/message.php][UPDATE no_events SET description = no_events.description.CONSTANT WHERE id = no_events.id.CONSTANT;]";
        		String entry = "POST-admin:message";
        		String inputParas = "ud_description:There are no events scheduled at this time.||submit:Update||";
		        String sessVars = "null";
		        _engine.debugAtom(sig, entry, inputParas, sessVars);
        		*/
        		
        	} catch (Exception e){
        		e.printStackTrace();
        	}
        }
		
		// Process SQL Queries/Responses.
		String sid = request.getParameter("sid");
		String query = request.getParameter("query");
	    //String result = request.getParameter("response");
	    String script = request.getParameter("script");
	    String time = request.getParameter("time");
	    
	    
	    
	    String session = "null";
	    if (sid!= null) {
	    	//session = _controller.inspect(sid);
	    	System.out.println("Inspecting Session ID: "+sid);
	    	session = _inspector.inspect(sid);
	    } else System.out.println("Null Session ID");
	    
	    if (script!=null && script.equals(wwwroot+"test.php")) return;
	    
	        
	    if (getMode() == 0) {
	    	BufferedWriter bw = new BufferedWriter(new FileWriter(traceDir + project + ".log", true));
			if (sid != null && query != null && script != null) {    // QUERY + SCRIPT + SESSION + TIME.
				bw.write("[QUERY][" + query + "]");
				bw.write("[SCRIPT][" + script + "]");
				_inspector.write(bw, sid);
				if (time != null) bw.write("[TIMESTAMP][" + time + "]");
				bw.write("\n");
				System.out.println(project + " " + query + " time " + time);
			}
			bw.close();
	    } else if (getMode() == 1) {
	    	QueryMessage message = new QueryMessage(query, script, session);
	    	TestingEngine.addQueryMessage(message);
	    }
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// Process HTTP Requests/Responses.
		String method = request.getParameter("method");
		String sid = request.getParameter("sid");
		String script = request.getParameter("script");
		String session = request.getParameter("session");
		String time = request.getParameter("timestamp");
		//System.out.println("method:"+method+" script:"+script);
		
		System.out.println("Session: "+session);
		
		
		//Output values for SqlLogger
	    BufferedWriter sv = new BufferedWriter(new FileWriter(traceDir + "last_req.tmp.log", false));
	    sv.write(script+"\n");
	    sv.write(sid+"\n");
	    sv.write(session+"\n");
	    sv.write(time+"\n");
	    sv.close();
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(traceDir + project + ".log", true));
		bw.write("[REQUEST]["+method+"][SCRIPT]["+script+"][SESSION]["+session+"][TIMESTAMP]["+time+"][PARA][");
		System.out.println("[REQUEST]["+method+"][SCRIPT]["+script+"][SESSION]["+session+"][TIMESTAMP]["+time+"][PARA][");
		// POST parameters.
		Enumeration<String> parameters = request.getParameterNames();
		while (parameters.hasMoreElements()){
			String para = (String)parameters.nextElement();
			String value= request.getParameterValues(para)[0];    // get the first parameter value.
			if (!para.equals("method") && !para.equals("script") && !para.equals("session") && !para.equals("timestamp")){
				bw.write(para + ":" + value + "||");
				System.out.println("para: " + para + " value: " + value);
			}
		}
		bw.write("]\n");
		bw.close();
	}
}
