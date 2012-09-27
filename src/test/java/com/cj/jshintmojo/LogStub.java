package com.cj.jshintmojo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.plugin.logging.Log;

public class LogStub implements Log {
	public static class Message {
		final CharSequence content;
		final Throwable error;
		
		public Message(CharSequence content, Throwable error) {
			super();
			this.content = content;
			this.error = error;
		}
		
	}
	private final Map<String, List<Message>> messages = new HashMap<String, List<Message>>();
	

	private void log(String level, CharSequence content){
		log(level, content, null);
	}

	private void log(String level, Throwable error){
		log(level, null, error);
	}
	private void log(String level, CharSequence content, Throwable error){
		List<Message> levelMessages = messagesForLevel(level);
		levelMessages.add(new Message(content, error));
	}

	public List<Message> messagesForLevel(String level) {
		List<Message> levelMessages = messages.get(level);
		if(levelMessages==null){
			levelMessages = new ArrayList<LogStub.Message>();
			messages.put(level, levelMessages);
		}
		return levelMessages;
	}
	
	@Override
	public boolean isDebugEnabled() {
		return false;
	}

	@Override
	public void debug(CharSequence content) {
		log("debug", content);
	}

	@Override
	public void debug(CharSequence content, Throwable error) {
		log("debug", content, error);
	}

	@Override
	public void debug(Throwable error) {
		log("debug", error);
	}

	@Override
	public boolean isInfoEnabled() {
		return false;
	}

	@Override
	public void info(CharSequence content) {
		log("info", content);
	}

	@Override
	public void info(CharSequence content, Throwable error) {
		log("info", content, error);
	}

	@Override
	public void info(Throwable error) {
		log("info", error);
	}

	@Override
	public boolean isWarnEnabled() {
		return false;
	}


	@Override
	public void warn(CharSequence content) {
		log("warn", content);
	}

	@Override
	public void warn(CharSequence content, Throwable error) {
		log("warn", content, error);
	}

	@Override
	public void warn(Throwable error) {
		log("warn", error);
	}
	
	@Override
	public boolean isErrorEnabled() {
		return false;
	}

	@Override
	public void error(CharSequence content) {
		log("error", content);
	}

	@Override
	public void error(CharSequence content, Throwable error) {
		log("error", content, error);
	}

	@Override
	public void error(Throwable error) {
		log("error", error);
	}
	
}
