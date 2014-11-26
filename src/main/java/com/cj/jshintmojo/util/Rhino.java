package com.cj.jshintmojo.util;

import org.mozilla.javascript.BaseFunction;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.ScriptableObject;

@SuppressWarnings("unchecked") 
public class Rhino {
	private final Context cx = Context.enter();
	private final ScriptableObject scope = cx.initStandardObjects();

	public Rhino() {
		// dummy readFile function to avoid undefined error
		scope.defineProperty("readFile", new BaseFunction() {},
				ScriptableObject.DONTENUM);
	}

	public <T> T eval(String code){
		// workaround for the 64k limit of rhino with enabled optimizations: set the Optimization Level to -1. See https://github.com/jshint/jshint/issues/1333
		cx.setOptimizationLevel(-1);
		return (T) 	cx.evaluateString(scope, code, "<cmd>", 1, null);
	}
	
	public <T> T call(String functionName, Object ... args){
	    Function f = getFunction(functionName);
	    return (T) f.call(cx, scope, scope, args);
	}
	
	private Function getFunction(String name){
		return (Function) scope.get(name, scope);
	}
}