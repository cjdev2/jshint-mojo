package com.cj.jshintmojo.jshint;

import java.util.ArrayList;
import java.util.List;

public class FunctionalJava {
	public interface Fn<Input, Return>{
		Return apply(Input i);
	}
	public static <T> List<T> filter(List<T> items, Fn<T, Boolean> fn){
		List<T> matches = new ArrayList<T>(items.size());

		for(T next : items){
			if(fn.apply(next)){
				matches.add(next);
			}
		}

		return matches;
	}

}
