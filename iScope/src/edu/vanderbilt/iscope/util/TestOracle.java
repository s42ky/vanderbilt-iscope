package edu.vanderbilt.iscope.util;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Vector;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import edu.vanderbilt.iscope.model.RequestMessage;
import edu.vanderbilt.iscope.model.SigProfile;

public class TestOracle {
	
	private String workingDir;
	private String project;
	private String traceDir;
	
	public TestOracle(String dir, String proj){
		workingDir = dir;
		project = proj;
		traceDir = workingDir + project + "/";
		try {
			_parser = new QueryParser();
			_stateConstructor = new StateConstructor();
			_stateConstructor.loadStateSet(traceDir + "stateSet");
			_stateConstructor.loadXMLSessionProfile(new File(traceDir+"sessionProfile.xml"));
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	
	private HashMap<String, SigProfile> testProfiles = new HashMap<String, SigProfile>();
	private HashMap<String, HashSet<String>> sessionVarsSet = new HashMap<String, HashSet<String>>();
	
	private StateConstructor _stateConstructor;
	private QueryParser _parser;
	
	public void loadTestProfiles() throws IOException, JDOMException {
		SAXBuilder parser = new SAXBuilder();
		
		// load constraint profile.
		Document doc1 = parser.build(new File(traceDir+"SigConstProfile.xml"));
		for (Object o: doc1.getContent()) {
			Element root = (Element) o;
			if (!root.getName().equals("SigConstProfile")) continue;
			for (Object s: root.getContent()) {                // for each SQL signature.
				if (!(s instanceof Element)) continue;
				Element sigConstElement = (Element) s;
				if (!sigConstElement.getName().equals("Signature")) continue;
				String sig = sigConstElement.getAttributeValue("Sig");
				SigProfile profile = new SigProfile(sig);
				profile.procXMLSigConstProfileElement(sigConstElement);
				testProfiles.put(sig, profile);
			}
		}
		// load trace summary.
		Document doc2 = parser.build(new File(traceDir+"TraceSummary.xml"));
		for (Object o: doc2.getContent()) {
			Element root = (Element) o;
			if (!root.getName().equals("TraceSummary")) continue;
			for (Object s: root.getContent()) {                // for each SQL signature.
				if (!(s instanceof Element)) continue;
				Element summary = (Element) s;
				if (!summary.getName().equals("Signature")) continue;
				String sig = summary.getAttributeValue("Sig");
				SigProfile profile = testProfiles.get(sig);
				profile.procXMLTraceSumElement(summary);
			}
		}
		// prepare sessionVars set.
		for (String sig: testProfiles.keySet()) {
			for (String entry: testProfiles.get(sig).getEntries().keySet()) {
				//System.out.println(sig + " " + entry);
				Vector<RequestMessage> messageList = testProfiles.get(sig).getMessagesByEntry(entry);
				//System.out.println(messageList.size());
				for (RequestMessage message: messageList) {
					String stateSig = _stateConstructor.genStateSig(message.getSessionVars());
					if (!sessionVarsSet.containsKey(stateSig)){
						if (!_stateConstructor.getStateSet().contains(stateSig)) {
							continue;
						}
						System.out.println("Loading state: " + stateSig);
						sessionVarsSet.put(stateSig, new HashSet<String>());
					}
					sessionVarsSet.get(stateSig).add(message.getSessionVars());
				}
			}
		}
	}
	
	public HashSet<String> getSignatureSet() {
		HashSet<String> sigSet = new HashSet<String>();
		for (String sig: testProfiles.keySet()) {
			sigSet.add(sig);
		}
		return sigSet;
	}
	
	public HashSet<String> getEntrySetBySig (String sig) {
		HashSet<String> entrySet = new HashSet<String>();
		for (String entry: testProfiles.get(sig).getEntries().keySet()) {
			entrySet.add(entry);
		}
		return entrySet;
	}
	
	// CORE TESTING MECHANISM:::
	
	// randomly select a request message for manipulation.
	public RequestMessage selectSeedRequestMessage(String sig, String entry) {
		Vector<RequestMessage> messages = testProfiles.get(sig).getMessagesByEntry(entry);
		Random r = new Random();
		int index = r.nextInt(messages.size());
		return messages.get(index);
	}
	
	// generate the set of test request messages.
	public Vector<String> genTestParasSet(String sig, String entry, RequestMessage seed) {
		HashMap<String, String> parameters = _parser.parseInputParameters(seed.getInputParas());
		HashMap<String, String> sessionVars = _parser.parseSession(seed.getSessionVars());
		Vector<String> testParas = testProfiles.get(sig).getEntries().get(entry).genViolatedParas(parameters, sessionVars);
		return testParas;
	}
	
	// generate the set of test states/session variables. utilize existing traces...
	public Vector<String> genTestSessionVarsSet(String sig, String entry, String inputParas) {
		Vector<String> testStates = new Vector<String>();        // abstract states.
		Vector<String> testSessionVars = new Vector<String>();   // concrete session variables.
		for (String state: _stateConstructor.getStateSet()) {
			if (!testProfiles.get(sig).getStatesByEntry(entry).contains(state)) {
				//System.out.println(state);
				testStates.add(state);
			}
		}
		for (String state: testStates) {
			String sessionVar = selectSessionVarsByState(sig, entry, state, inputParas);  // randomly pick a set of concrete session variables.
			testSessionVars.add(sessionVar);    // include empty state.
		}
		return testSessionVars;
	}
	
	public boolean checkEqualityConstViolated(String sig, String entry, String inputParas, String session) {
		HashMap<String, String> parameters = _parser.parseInputParameters(inputParas);
		HashMap<String, String> sessionVars = _parser.parseSession(session);
		return (!testProfiles.get(sig).getEntries().get(entry).checkEqualityConst(parameters, sessionVars));
	}
	
	public boolean checkSessionConstViolated(String sig, String entry, String session){
		HashMap<String, String> sessionVars = _parser.parseSession(session);
		return (!testProfiles.get(sig).getEntries().get(entry).checkSessionConst(sessionVars));
	}
	
	private String selectSessionVarsByState(String sig, String entry, String targetState, String inputParas) {
		String sessionVars = "null";
		//String sessionVars = "";
		boolean found = false;
		Vector<String> sessionVarsList = new Vector<String>();
		if (!sessionVarsSet.containsKey(targetState)) {
			//System.out.println("Warning: no session vars are selected for: " + targetState);
			return sessionVars;                                // no desired states are available.
		}		
		for (String sv: sessionVarsSet.get(targetState)) {
			sessionVarsList.add(sv);
		}
		for (int i=0; i < sessionVarsList.size(); i++){
			if (!checkEqualityConstViolated(sig, entry, inputParas, sessionVarsList.get(i))) {  // check if equality constraint violated.
				sessionVars = sessionVarsList.get(i);
				found = true;
				break;
			}
		}
		//if (!found) System.out.println("Warning: don't find the desired session variables.");
		return sessionVars;
	}
}