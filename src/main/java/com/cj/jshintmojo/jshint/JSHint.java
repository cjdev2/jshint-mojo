package com.cj.jshintmojo.jshint;

import org.apache.commons.io.IOUtils;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.NativeObject;

import com.cj.jshintmojo.util.Rhino;

import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class JSHint {
    private final Rhino rhino;

    public JSHint() {
	rhino = new Rhino();
	rhino.eval(resourceAsString("jshint.js"));
    }

    public List<Error> run(InputStream source, String options, String globals) {
	final List<Error> results = new ArrayList<JSHint.Error>();

	String sourceAsText = toString(source);

	NativeObject nativeOptions = toJsObject(options);
	NativeObject nativeGlobals = toJsObject(globals);

	Boolean codePassesMuster = rhino.call("JSHINT", sourceAsText, nativeOptions, nativeGlobals);

	if(!codePassesMuster){
	    NativeArray errors = rhino.eval("JSHINT.errors");

	    for(Object next : errors){
		if(next!=null){ // sometimes it seems that the last error in the list is null
		    Error error = new Error(new JSObject(next));
		    results.add(error);
		}
	    }
	}

	return results;
    }

    private NativeObject toJsObject(String options) {
	NativeObject nativeOptions = new NativeObject();
	for (final String nextOption : options.split(",")) {
	    final String option = nextOption.trim();
	    if(!option.isEmpty()){
		final String name;
		final Object value;
		
		final int valueDelimiter = option.indexOf(':');
		if(valueDelimiter==-1){
		    name = option;
		    value = Boolean.TRUE;
		} else {
		    name = option.substring(0, valueDelimiter);
		    String rest = option.substring(valueDelimiter+1).trim();
		    if (rest.matches("[0-9]+")) {
                value = Integer.parseInt(rest);
            } else if (rest.equals("true")) {
                value = Boolean.TRUE;
            } else if (rest.equals("false")) {
                value = Boolean.FALSE;
            } else {
                value = rest;		        
		    }
		}
		nativeOptions.defineProperty(name, value, NativeObject.READONLY);
	    }
	}
	return nativeOptions;
    }

    private static String toString(InputStream in) {
	try {
	    return IOUtils.toString(in);
	} catch (Exception e) {
	    throw new RuntimeException(e);
	}
    }

    private String resourceAsString(String name) {
	return toString(getClass().getResourceAsStream(name));
    }

    @SuppressWarnings("unchecked") 
    static class JSObject {
	private NativeObject a;

	public JSObject(Object o) {
	    if(o==null) throw new NullPointerException();
	    this.a = (NativeObject)o;
	}

	public <T> T dot(String name){
	    return (T) a.get(name);
	}
    }

    @SuppressWarnings("serial")
    public static class Error implements Serializable {
	public final String id, raw, evidence, reason;
	public final Number line, character;

	public Error(JSObject o) {
	    id = o.dot("id");
	    raw = o.dot("raw");
	    evidence = o.dot("evidence");
	    line = o.dot("line");
	    character = o.dot("character");
	    reason = o.dot("reason");
	}
    }
}
