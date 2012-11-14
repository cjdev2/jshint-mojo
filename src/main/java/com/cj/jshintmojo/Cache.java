package com.cj.jshintmojo;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Cache implements Serializable {
	private static final long serialVersionUID = 1L;
	
	String options;
	String globals;
	Map<String, Result> previousResults = new HashMap<String, Result>();
	
	public Cache(String options, String globals) {
		super();
		this.options = options;
		this.globals = globals;
	}

	public Cache(String options, String globals, Map<String, Result> previousResults) {
		this(options, globals);
		this.previousResults = previousResults;
	}
}
