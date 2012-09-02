package edu.vanderbilt.iscope.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class SessionController {
	// sessionExporter
	String sspath = "/var/lib/php5/";

	public void write(BufferedWriter os, String index){
		try {
			String session = inspect(index);
			//System.out.println(session);
			os.write("[SESSION][" + session +"]");
			os.flush();
		} catch (Exception e){
			e.printStackTrace();
		}
	}

	public String inspect(String index) {
		String session = "null";
		try {
			if (!index.equals("")){
				String ssfile = sspath + "sess_" + index;
				if (new File(ssfile).exists()){
					BufferedReader br = new BufferedReader(new FileReader(ssfile));
					session = br.readLine();
					if(session==null || session.trim().equals("")) {
						session = "null";
					}
					br.close();
				} else {
					//System.out.println("ERROR: Session file doesn't exist..");
				}
			}
		} catch (Exception e){
			e.printStackTrace();
		}
		return session;
	}
	
	// write session file.
	public void setSessionVars(String session_id, String session) {
		String ssfile = sspath + "sess_" + session_id;
		try {
			//if (new File(ssfile).exists()) {
				BufferedWriter bw = new BufferedWriter(new FileWriter(ssfile));
				if (session.equals("null")) {
					bw.write("");
				} else {
					bw.write(session);
				}
				bw.close();
			//} else {
			//	System.out.println("Error: fail to write the session file: " + ssfile);
			//}
		} catch (Exception e){
			e.printStackTrace();
		}
	}
}