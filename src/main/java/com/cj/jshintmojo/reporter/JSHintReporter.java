package com.cj.jshintmojo.reporter;

import java.util.Map;

import com.cj.jshintmojo.cache.Result;

/**
 * A interface for JSHint reporting class.
 */
public interface JSHintReporter {

    /**
     * Creates a lint reporting string. 
     * 
     * @param results lint results to report.
     * @return reporting string.
     */
    public String report(Map<String, Result> results);
}
