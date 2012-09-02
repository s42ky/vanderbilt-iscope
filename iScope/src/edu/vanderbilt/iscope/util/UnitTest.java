package edu.vanderbilt.iscope.util;

import java.util.Vector;

import org.owasp.webscarab.model.Request;

import edu.vanderbilt.iscope.model.RequestMessage;


public class UnitTest {
	
	//private static String dir = "C:/users/xiaowei/Desktop/acsac/";
	private static String dir = "/home/likewise-open/VANDERBILT/lix12/acsac/";
	
	public static void main(String[] args) {
		//testRequestGenerator();
		testTestOracle();
		//testTestingEngine();
	}
	
	public static void testRequestGenerator() {
		try {
			RequestGenerator _generator = new RequestGenerator("129.59.89.23", "securephoto");
			//String entry = "POST-useroptions";
			String entry = "POST-comments:add_comment";
			//String inputParas = "userid:NOT{5,2,3,4,6,}||firstname:Wei||email:NOT{Wei.Yan@qq.com}||showemail:on||submit:Save changes||password2:12345678||email2:||lastname:Yan||password:12345678||";
			String inputParas = "Conference Name:Vanderbilt+Conference+Management+System||Is Forum Moderated (emails the admins on every post):0||";
			Request request = _generator.constructRequest(entry, inputParas, "");
			System.out.println(request);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void testTestOracle() {
		try {			
			String project = "scarf";
			TestOracle _oracle = new TestOracle(dir, project);
			_oracle.loadTestProfiles();
			
			String sig = "[/var/www/scarf/index.php][SELECT paper_id, title FROM papers WHERE session_id=papers.session_id.CONSTANT ORDER BY order1;]";
			String entry = "GET-index";
			//String sig = "[/var/www/scarf/generaloptions.php][SELECT user_id, email, firstname, lastname, affiliation, privilege, showemail FROM users ORDER BY lastname;]";
			//String entry = "GET-generaloptions";
			/*
			// select a seed message:
			RequestMessage seed = _oracle.selectSeedRequestMessage(sig, entry);
			System.out.println("seed message: " + seed.toString());		
			// generate test paras set:
			Vector<String> testParas = _oracle.genTestParasSet(sig, entry, seed);
			System.out.println("test paras: ");
			for (String paras: testParas) {
				System.out.println(paras);
			}
			*/
			// generate test state set:
			String inputParas = "email:Craig.Verzosa@msn.com||password:12345678||";
			Vector<String> testStates = _oracle.genTestSessionVarsSet(sig, entry, inputParas);
			for (String state: testStates) {
				System.out.println(state);
			}
			// check equality constraint violated:
			//String inputParas = "affiliation:TJU||firstname:Wei||email:Wei.Yan@qq.com||showemail:on||submit:Save changes||password2:12345678||email2:||lastname:Yan||password:12345678||";
			String session = "email|s:14:\"Wei.Yan@qq.com\";privilege|s:5:\"user\";user_id|s:2:\"65\";";
			//String session = "email|s:14:\"Wei.Yan@qq.com\";user_id|s:2:\"65\";";
			//System.out.println("check equality const: " + _oracle.checkEqualityConstViolated(sig, entry, inputParas, session));
			// check session constraint violated:
			//System.out.println("check session const: " + _oracle.checkSessionConstViolated(sig, entry, session));
			
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	
	// 
	public static void testTestingEngine() {
		
		
		
		
		
		
	}
}