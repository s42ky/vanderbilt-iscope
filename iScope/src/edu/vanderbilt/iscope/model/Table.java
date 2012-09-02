package edu.vanderbilt.iscope.model;

import java.util.HashMap;

public class Table {
	private String name;								// table name.
	private HashMap<String, Variable> fields;              // key: field name
	
	public Table(String t){
		name = t;
		fields = new HashMap<String, Variable>();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public HashMap<String, Variable> getFields() {
		return fields;
	}

	public void setFields(HashMap<String, Variable> fields) {
		this.fields = fields;
	}	
}