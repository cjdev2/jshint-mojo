package com.cj.jshintmojo.util;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;

@SuppressWarnings("unchecked") 
public class Rhino {
	private final Context cx = Context.enter();
	private final Scriptable scope = cx.initStandardObjects();

	public <T> T eval(String code){
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