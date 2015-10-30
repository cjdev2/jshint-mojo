package com.cj.jshintmojo.cache;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Cache implements Serializable {
	private static final long serialVersionUID = 1L;
	
	public static class Hash implements Serializable {
        private static final long serialVersionUID = 6729471159921057308L;
        public final String options;
	    public final String globals;
	    public final String jsHintVersion;
	    public final String configFile;
	    public final List<String> directories;
	    public final List<String> excludes;
        public final List<String> includes;
        
	    public Hash(String options, String globals, String jsHintVersion, String configFile, List<String> directories, List<String> excludes, List<String> includes) {
            super();
            this.options = options;
            this.globals = globals;
            this.jsHintVersion = jsHintVersion;
            this.configFile = configFile;
            this.directories = directories;
            this.excludes = excludes;
            this.includes = includes;
        }
	}
	
	public final Hash hash;
	public final Map<String, Result> previousResults;
	
	public Cache(Hash hash) {
      this(hash, new HashMap<String, Result>());
	}

	public Cache(Hash hash, Map<String, Result> previousResults) {
	  this.hash = hash;
	  this.previousResults = previousResults;
	}
}
