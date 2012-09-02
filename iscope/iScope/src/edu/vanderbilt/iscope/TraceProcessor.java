package edu.vanderbilt.iscope;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;


public class TraceProcessor {
	
	private static String workingDir = Portal.workingDir;
	private static String project = Portal.project;
	private static String traceDir = workingDir + project + "/";
	
	public static void main(String[] args) {
		try {
			//process1();
			process2();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}	
	
	// For Scarf, bloggit.
	public static void process1() throws Exception {
		BufferedReader br = new BufferedReader(new FileReader(traceDir + project + ".log"));
		BufferedWriter bw = new BufferedWriter(new FileWriter(traceDir + project + ".new.log"));
		String line;
		while ((line=br.readLine())!=null){
			if (line.contains("[PARA][------")) {    // processing here.
				line = line.substring(0,line.indexOf("[--"));
				System.out.println(line);
				String query = "";
				String line1;
				do {
					line1 = br.readLine();
					if (line1.equals("||]")) break;
					System.out.println(line1);
					if (line1.contains("filename=\"\"")){
						String line2 = br.readLine();
						if (line2.equals("Content-Type: application/octet-stream")){
							br.readLine();
							br.readLine();
							br.readLine();
						}
					} else {
						String name = line1.substring(line1.indexOf("\"")+1, line1.lastIndexOf("\""));
						name = name.replace("[]", "");
						br.readLine();
						String value = br.readLine();
						value = value.replace(":", "");
						br.readLine();
						query += name + ":" + value + "||";
					}
				} while (true);
				line = line + "[" + query + "]";
				System.out.println(line);
				bw.write(line+"\n");
			} else {
				bw.write(line+"\n");
			}		
		}	
		br.close();
		bw.close();	
	}	
	
	
	// For Events, OpenIT.
	public static void process2() throws Exception {
		BufferedReader br = new BufferedReader(new FileReader(traceDir + project + ".log"));
		BufferedWriter bw = new BufferedWriter(new FileWriter(traceDir + project + ".new2.log"));
		String line;
		while ((line=br.readLine())!=null){
			if (!line.contains("[SESSION]")) {    // processing here.
				break;
				/*
				String line1 =""; 
				do {
					line1 = br.readLine();
					line += line1;
				} while (!line1.contains("[SESSION]"));
				bw.write(line+"\n");
				*/
			} else {
				bw.write(line + "\n");
			}
		}
		br.close();
		bw.close();	
	}
	
	
	
	
	
}