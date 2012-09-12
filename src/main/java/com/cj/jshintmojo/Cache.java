package com.cj.jshintmojo;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Cache implements Serializable {
	private static final long serialVersionUID = 1L;
	
	String options;
	Map<String, Result> previousResults = new HashMap<String, Result>();
	
	public Cache(String options) {
		super();
		this.options = options;
	}

	public Cache(String options, Map<String, Result> previousResults) {
		super();
		this.options = options;
		this.previousResults = previousResults;
	}
}
