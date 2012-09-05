package edu.vanderbilt.iscope;

public class Configuration {
	public static final String project = "scarf";
	public static final String dbname = project;
	private static final int mode = 0;    // 0: Training mode; 1: Testing mode
	
	
	public static final String loggingBaseDir = "/srv/logger/";
	public static final String host = "localhost:80";
	public static final String wwwroot = "/srv/htdocs/";

	public static String projectDir = loggingBaseDir + project + "/";
	public static int getMode() { return mode; }
}
