package com.cj.jshintmojo.cache;

import java.io.Serializable;
import java.util.List;

import com.cj.jshintmojo.jshint.JSHint.Hint;

@SuppressWarnings("serial")
public class Result implements Serializable {
    public final String path;
    public final Long lastModified;
    public final List<Hint> hints;

	public Result(final String path, final Long lastModified, final List<Hint> hints) {
		super();
		this.path = path;
		this.lastModified = lastModified;
		this.hints = hints;
	}
}