package com.cj.jshintmojo.cache;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Cache implements Serializable {
	private static final long serialVersionUID = 1L;
	
	public final String options;
	public final String globals;
	public final Map<String, Result> previousResults;
	
	public Cache(String options, String globals) {
      this(options, globals, new HashMap<String, Result>());
	}

	public Cache(String options, String globals, Map<String, Result> previousResults) {
      this.options = options;
      this.globals = globals;
	  this.previousResults = previousResults;
	}
}
