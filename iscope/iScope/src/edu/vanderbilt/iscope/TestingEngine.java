package edu.vanderbilt.iscope;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.util.Vector;

import org.owasp.webscarab.httpclient.HTTPClient;
import org.owasp.webscarab.httpclient.HTTPClientFactory;
import org.owasp.webscarab.model.Request;
import org.owasp.webscarab.model.Response;

import edu.vanderbilt.iscope.model.QueryMessage;
import edu.vanderbilt.iscope.model.RequestMessage;
import edu.vanderbilt.iscope.util.RequestGenerator;
import edu.vanderbilt.iscope.util.SQLSymbolizer;
import edu.vanderbilt.iscope.util.SessionController;
import edu.vanderbilt.iscope.util.TestOracle;

public class TestingEngine {
	
	public TestingEngine(String dir, String proj, String url) {
		workingDir = dir;
		project = proj;
		host = url;
		cacheDir = workingDir + project + "/cache/";
		try {
			new File(cacheDir).mkdirs();
        	bw = new BufferedWriter(new FileWriter(workingDir + project + "/report", true));
			_oracle = new TestOracle(workingDir, project);
			_controller = new SessionController();
			_generator = new RequestGenerator(host, project);
			_symbolizer = new SQLSymbolizer(workingDir, project);
			_oracle.loadTestProfiles();
			_symbolizer.loadSigProfile();
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	
	private String workingDir;
	private String project;
	private String cacheDir;
	private String host;
	
	private int interaction_id = 0;
	private String session_id = null;
	private String cookie = null;
	private String curTestSig = null;     // current sql signature under test.
	private String curTestEntry = null;   // current entry (request key) under test.
	private Request request = null;
	
	private static int TEST_PARAMETER = 1;
	private static int TEST_SESSION = 2;
	private int test_const = 0;
	private boolean running = false;
	private boolean debug = false;
	
	private int reported_para = 0;
	private int reported_sess = 0;
	private int inputParaTest = 0;
	private int sessVarTest = 0;
	
	private TestOracle _oracle;
	private SessionController _controller;
	private RequestGenerator _generator;
	private SQLSymbolizer _symbolizer;
	
	private BufferedWriter bw;
	
	// maintain the set of query messages.
	private static Vector<QueryMessage> messageSet = new Vector<QueryMessage>();
	public static void addQueryMessage(QueryMessage message) {
		messageSet.add(message);
	}
	
	// submit web request to application.
	private void submitRequest(Request request) {
		HTTPClient client = HTTPClientFactory.getInstance().getHTTPClient();
		try {
			interaction_id ++;
			request.write(new FileOutputStream(cacheDir+interaction_id+"-request"));
			//System.out.println(request);
			
			//PROCESS REQUEST
			
			Response response = client.fetchResponse(request);
            response.flushContentStream();
            responseReceived(response);
            
            //PROCESS RESPONSE
            
            
        } catch (Exception ioe) {
        	ioe.printStackTrace();
        }
	}
	
	
	public void debug(String sig, String entry) throws Exception {
		System.out.println("====Debugging Start...====");
		submitRequest(_generator.constructBaseRequest());   // ping the app and get cookie/session id.
		System.out.println("Debug Signature: " + sig);
		curTestSig = sig;
		System.out.println("Debug entry: " + entry);
		curTestEntry = entry;
		debug = true;
		running = false;
		
		RequestMessage seed = _oracle.selectSeedRequestMessage(sig, entry);
		System.out.println("Seed message: ");
		System.out.println(seed.getInputParas() + "\n");
		System.out.println(seed.getSessionVars() + "\n");
		System.out.println("(1) Testing Input Parameter Constraints...");
		test_const = TEST_PARAMETER;
		Vector<String> testParas = _oracle.genTestParasSet(sig, entry, seed);
		for (String testPara: testParas) {
			System.out.println(testPara);
			debugAtom(sig, entry, testPara, seed.getSessionVars());
		}
		
		System.out.println("(2) Testing Session Variable Constraints...");
		test_const = TEST_SESSION;
		Vector<String> testSessionVars = _oracle.genTestSessionVarsSet(sig, entry, seed.getInputParas());
		for (String sessionVars: testSessionVars) {
			System.out.println(sessionVars);
			debugAtom(sig, entry, seed.getInputParas(), sessionVars);
		}
	}
	
	
	public void debugAtom(String sig, String entry, String inputParas, String sessVars) throws Exception {		
		submitRequest(_generator.constructBaseRequest());   // ping the app and get cookie/session id.
		curTestSig = sig;
		curTestEntry = entry;
		debug = true;
		running = false;
		request = _generator.constructRequest(entry, inputParas, cookie);
		if (session_id != null) {
			//System.out.println("HERE0:::");
			_controller.setSessionVars(session_id, sessVars);
			if (!_controller.inspect(session_id).equals(sessVars)) {
				System.out.println("Error: fail to set session state: " + sessVars);
			} else {
				//System.out.println("HERE2:::");
				System.out.println(request);
				submitRequest(request);
			}
		} else {
			System.out.println("HERE1:::");
			System.out.println(request);
			submitRequest(request);
		}
	}
	
	
	// MAIN function.
	public void run() throws Exception {
		System.out.println("====Testing Start...====");
		bw.write("====Testing Start...====\n");
		submitRequest(_generator.constructBaseRequest());   // ping the app and get cookie/session id.
		running = true;
		debug = false;
		
		for (String sig: _oracle.getSignatureSet()) {           // test each sql signature.
			//System.out.println("Testing Signature: " + sig);
			bw.write("Cur Sig: " + sig +"\n");
			curTestSig = sig;
			
			for (String entry: _oracle.getEntrySetBySig(sig)) {
								
				//System.out.println("  Current entry: " + entry);    // test each entry.
				bw.write("Cur entry: " + entry +"\n");
				curTestEntry = entry;
				
				// Randomly select an entrance (i.e., RequestMessage): a set of input parameters and session variables as the testing seed.
				// in the future, we can test multiple request message in a statistical way.
				RequestMessage seed = _oracle.selectSeedRequestMessage(sig, entry);
				//System.out.println("Seed message:\n" + seed.getInputParas() + "\n" + seed.getSessionVars());
				bw.write("Seed message: \n");
				bw.write(seed.getInputParas() + "\n");
				bw.write(seed.getSessionVars() + "\n");
				
				// first test input parameter constraints.
				//System.out.println("(1) Testing Input Parameter Constraints...");
				bw.write("(1) Testing Input Parameter Constraints...\n");
				test_const = TEST_PARAMETER;
				// sessionVars unchanged. manipulate input parameters.
				Vector<String> testParas = _oracle.genTestParasSet(sig, entry, seed);
				//inputParaTest++;                             // ADD ONLY ONCE FOR PARAMETER VARIATION
				for (String testPara: testParas) {
					bw.write(testPara + "\n");
					if (!running) {
						System.out.println("Abort testing 0...");
						bw.write("Abort testing here...\n");
						break;
					}
					request = _generator.constructRequest(entry, testPara, cookie);
					if (session_id != null && !session_id.equals("null")) { 
						_controller.setSessionVars(session_id, seed.getSessionVars());
						if (!_controller.inspect(session_id).equals(seed.getSessionVars())) {
							System.out.println("Error: fail to set session state 0: " + seed.getSessionVars());
							bw.write("Error: fail to set session state: " + seed.getSessionVars() +"\n");
							break;
						}
						inputParaTest++;
						submitRequest(request);
					} else {
						running = false;
						System.out.println("Error: session id is null.0");
						break;
					}
				}
				
				// second test session variable constraints.
				//System.out.println("(2) Testing Session Variable Constraints...");
				bw.write("(2) Testing Session Variable Constraints...\n");
				test_const = TEST_SESSION;
				request = _generator.constructRequest(entry, seed.getInputParas(), cookie);
				// inputParas unchanged. manipulate session variables.
				boolean counter = false;
				Vector<String> testSessionVars = _oracle.genTestSessionVarsSet(sig, entry, seed.getInputParas());  // concrete session variables.
				for (String sessionVars: testSessionVars) {
					if (counter && sessionVars.equals("null")) continue;
					if (sessionVars.equals("null")) counter = true;
					//System.out.println("Cur session var: " + sessionVars);
					bw.write(sessionVars + "\n");
					if (!running) {
						System.out.println("Abort testing...1");
						bw.write("Abort testing here...\n");
						break;
					}
					if (session_id != null) { 
						_controller.setSessionVars(session_id, sessionVars);
						if (!_controller.inspect(session_id).equals(sessionVars)) {
							System.out.println("Error: fail to set session state 1: " + sessionVars);
							bw.write("Error: fail to set session state: " + sessionVars +"\n");
							break;
						}
						sessVarTest++;
						submitRequest(request);
					} else {
						running = false;
						System.out.println("Error: session id is null.1");
						break;
					}
				}
				if (running) {
					//System.out.println("  Current entry: " + entry + " test complete!");    // test each entry.
				} else {
					//System.out.println("  Error: Current entry: " + entry + " test INCOMPLETE! Test next entry.");    // test each entry.
					running = true;
				}
			}
			if (running) {
				//System.out.println("Current signature: " + sig + " test complete!");    // test each entry.
			} else {
				//System.out.println("Error: Current Signature: " + sig + " test INCOMPLETE! Test next signature.");    // test each entry.
				running = true;
			}
		}
		System.out.println("====Congratulations! Testing Complete...====");
		System.out.println("Number of input para tests: " + inputParaTest +
				           "\nNumber of session var tests: " + sessVarTest +
				           "\nNumber of reported para: " + reported_para +
				           "\nNumber of reported sess: " + reported_sess + "\n");
		bw.write("====Congratulations! Testing Complete...====\n");
		bw.write("Number of input para tests: " + inputParaTest +
		           "\nNumber of session var tests: " + sessVarTest +
		           "\nNumber of reported para: " + reported_para +
		           "\nNumber of reported sess: " + reported_sess + "\n");
		bw.close();
	}
		
	// End of current interaction: Evaluation.
	private void responseReceived(Response response) throws Exception {
		if (response == null) System.out.println("NULL RESPONSE");
		if (response != null) {
			//System.out.println("Response received.");
			if (response.getStatus().equals("400") || response.getStatus().equals("404")) {    // Bad request || Not found.
				System.out.println("Bad request NOT FOUND here...\n" + request);
				bw.write("Bad request here...\n");
				running = false;
				//messageSet.clear();    // clear current message set for next interaction.
				return;
			}  // ignore redirection here.
			
			// retrieve cookie and session id;
			if (response.getHeader("Set-Cookie") != null) {
				cookie = response.getHeader("Set-Cookie");
				System.out.println("Cookie: " + cookie);
			} else {
				//System.out.println("COOKIE NULL!!");
				//System.out.println(response);
			}
			
			if (session_id == null && cookie != null) {
			//if (session_id == null && cookie != null && cookie.contains("PHPSESSID")) {  // change session id here.
				session_id = cookie.substring(cookie.indexOf("=")+1, cookie.indexOf(";"));
				System.out.println("Set the PHP session id: " + session_id);
			}
			
			// evaluate: 
			// currently no evaluation.
			if (running || debug) {
				if (debug) System.out.println("Message set size: " + messageSet.size());
				for (QueryMessage message: messageSet) {
					String signature = _symbolizer.genSQLSig(message.getQueryStatement(), message.getScript());
					if (signature.equals(curTestSig)) {        // signature matches...
						if (debug) System.out.println("Signature matches here..");
						if (test_const == TEST_PARAMETER) {
							System.out.println("\nREPORT A Potential Logic Flaw!");
							bw.write("\nREPORT A Potential Logic Flaw!\n");
							reported_para ++;
							break;
						} else if (test_const == TEST_SESSION) {
							// check if any session constraint is violated.
							if (_oracle.checkSessionConstViolated(curTestSig, curTestEntry, message.getSessionVars())) {
								System.out.println("\nREPORT A Potential Logic Flaw!");
								bw.write("\nREPORT A Potential Logic Flaw!\n");
								reported_sess ++;
								break;
							}
						}
					}
				}
			}
			messageSet.clear();    // clear current message set for next interaction.
		}
	}
}