package com.cj.jshintmojo;

import java.io.Serializable;
import java.util.List;

import com.cj.jshintmojo.JSHint.Error;

@SuppressWarnings("serial")
public class Result implements Serializable {
	final String path;
	final Long lastModified;
	final List<Error> errors;
	
	public Result(String path, Long lastModified, List<Error> errors) {
		super();
		this.path = path;
		this.lastModified = lastModified;
		this.errors = errors;
	}
}