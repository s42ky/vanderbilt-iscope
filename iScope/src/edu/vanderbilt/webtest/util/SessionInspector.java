package edu.vanderbilt.webtest.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;

public class SessionInspector {
	
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
	
	//From webscarab
	public String getSession(String index) {
		String session = "null";
		try {	
			if (!index.equals("")){
				String ssfile = sspath + "sess_" + index;
				//System.out.println(ssfile);
				if (new File(ssfile).exists()){
					BufferedReader br = new BufferedReader(new FileReader(ssfile));
					session = br.readLine();
					if (session == null) session = "null";
					br.close();
				} else {
					System.out.println("ERROR: Session file doesn't exist..");
				}
			}
			/*
			File topdir = new File(sspath);
			File[] dir = topdir.listFiles();
			if (dir.length == 0){
				System.out.println("No session file exist");
				return null;
			}
			for (File file: dir){
				//System.out.println(file.toString());
				BufferedReader br = new BufferedReader(new FileReader(file));
				session = br.readLine();
				br.close();
			}*/
		} catch (Exception e){
			e.printStackTrace();
		}
		return session;
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
					System.out.println("ERROR: Session file doesn't exist..");
				}
			}
		} catch (Exception e){
			e.printStackTrace();
		}
		return session;
	}
}