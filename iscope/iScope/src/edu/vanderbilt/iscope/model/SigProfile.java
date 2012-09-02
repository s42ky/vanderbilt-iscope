package edu.vanderbilt.iscope.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;

import org.jdom.Element;

public class SigProfile {
	
	public SigProfile(String s) {
		sig = s;
	}
	
	private String sig;    // signature.
	private HashSet<String> entrySet = new HashSet<String>();
	private HashMap<String, Entry> entries = new HashMap<String, Entry>();
	// trace summary for test generation.
	private HashMap<String, HashSet<RequestMessage>> messages = new HashMap<String, HashSet<RequestMessage>>();
	private HashMap<String, HashSet<String>> entryStates = new HashMap<String, HashSet<String>>();
	
	public void addTrace(String requestKey, String inputParas, String sessionVars, String entryState){		
		entrySet.add(requestKey);
		if (!entries.containsKey(requestKey)){
			entries.put(requestKey, new Entry(requestKey));
		}
		if (!messages.containsKey(requestKey)) {
			messages.put(requestKey, new HashSet<RequestMessage>());
		}
		if (!entryStates.containsKey(requestKey)) {
			entryStates.put(requestKey, new HashSet<String>());
		}
		entries.get(requestKey).addTrace(inputParas, sessionVars);
		messages.get(requestKey).add(new RequestMessage(inputParas, sessionVars));
		entryStates.get(requestKey).add(entryState);            // remember the set of observable states at each entry.
	}
	
	public HashMap<String, Entry> getEntries() {
		return entries;
	}
	
	public Vector<RequestMessage> getMessagesByEntry(String entry) {
		HashSet<RequestMessage> messageSet = messages.get(entry);
		Vector<RequestMessage> messageList = new Vector<RequestMessage>();
		for (RequestMessage message: messageSet) {
			messageList.add(message);
		}
		return messageList;
	}
	
	public HashSet<String> getStatesByEntry(String entry) {
		return entryStates.get(entry);
	}
	
	public void genConstraints(){
		// infer constraints at each entry.
		//System.out.println("CURRENT SIG:" + sig);
		for (String entry: entries.keySet()) {
			entries.get(entry).genConstraints();
		}
	}
	
	public Element genXMLSigConstProfileElement() {
		Element profile = new Element("Signature");
		profile.setAttribute("Sig", sig);
		for (String entry: entries.keySet()) {
			profile.addContent(entries.get(entry).genXMLEntryConstElement());
		}
		return profile;
	}
	
	public void procXMLSigConstProfileElement(Element e) {
		for (Object o: e.getContent()) {
			if (!(o instanceof Element)) continue;
			Element entry = (Element) o;
			if (!entry.getName().equals("Entry")) continue;
			String key = entry.getAttributeValue("Key");			
			Entry entryConsts = new Entry(key);
			entries.put(key, entryConsts);
			for (Object en: entry.getContent()){
				if (!(en instanceof Element)) continue;
				Element consts = (Element) en;
				if (consts.getName().equals("PersistentPara")) {
					entryConsts.procXMLPersistentParaElement(consts);
				} else if (consts.getName().equals("PersistentSessVar")) {
					entryConsts.procXMLPersistentSessVarElement(consts);
				} else if (consts.getName().equals("BoundedPara")) {
					entryConsts.procXMLBoundedParaElement(consts);
				} else if (consts.getName().equals("BoundedSessVar")) {
					entryConsts.procXMLBoundedSessVarElement(consts);
				} else if (consts.getName().equals("EqualityConsts")) {
					entryConsts.procXMLEqualityConstElement(consts);
				}
			}
		}	
	}
	
	public Element genXMLTraceSumElement() {
		Element summary = new Element("Signature");
		summary.setAttribute("Sig", sig);
		for (String entry: entrySet) {
			Element entrySum = new Element("Entry");
			entrySum.setAttribute("Key", entry);
			
			// request messages;			
			Element msgSetElement = new Element("RequestMessages");
			for (RequestMessage request: messages.get(entry)) {
				Element message = new Element("Message");
				message.setAttribute("Content", request.toString());
				msgSetElement.addContent(message);
			}
			Element stateSetElement = new Element("EntryStates");
			for (String entryState: entryStates.get(entry)) {
				Element state = new Element("EntryState");
				state.setAttribute("StateSig", entryState);
				stateSetElement.addContent(state);
			}
			entrySum.addContent(msgSetElement);
			entrySum.addContent(stateSetElement);
			summary.addContent(entrySum);
		}
		return summary;
	}
	
	public void procXMLTraceSumElement(Element e) {
		for (Object o: e.getContent()) {
			if (!(o instanceof Element)) continue;
			Element entrySum = (Element) o;
			if (!entrySum.getName().equals("Entry")) continue;
			String key = entrySum.getAttributeValue("Key");
			messages.put(key, new HashSet<RequestMessage>());
			entryStates.put(key, new HashSet<String>());
			for (Object en: entrySum.getContent()){
				if (!(en instanceof Element)) continue;
				Element sum = (Element) en;
				if (sum.getName().equals("RequestMessages")) {
					for (Object rm: sum.getContent()) {
						if (!(rm instanceof Element)) continue;
						Element message = (Element) rm;
						if (!message.getName().equals("Message")) continue;
						messages.get(key).add(new RequestMessage(message.getAttributeValue("Content")));
					}
				} else if (sum.getName().equals("EntryStates")){
					for (Object s: sum.getContent()) {
						if (!(s instanceof Element)) continue;
						Element state = (Element) s;
						if (!state.getName().equals("EntryState")) continue;
						entryStates.get(key).add(state.getAttributeValue("StateSig"));
					}
				}
			}
		}
	}
}