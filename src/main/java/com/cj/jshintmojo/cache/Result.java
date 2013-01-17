package com.cj.jshintmojo.cache;

import java.io.Serializable;
import java.util.List;

import com.cj.jshintmojo.jshint.JSHint.Error;

@SuppressWarnings("serial")
public class Result implements Serializable {
    public final String path;
    public final Long lastModified;
    public final List<Error> errors;
	
	public Result(String path, Long lastModified, List<Error> errors) {
		super();
		this.path = path;
		this.lastModified = lastModified;
		this.errors = errors;
	}
}