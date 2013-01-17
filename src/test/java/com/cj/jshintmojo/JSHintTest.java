package com.cj.jshintmojo;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.cj.jshintmojo.jshint.JSHint;
import com.cj.jshintmojo.jshint.JSHint.Error;

public class JSHintTest {

    @Test
    public void supportsParametersWithValues(){
	// given
	final String globals = "";
	final String options = "";//"maxparams:3";
	final InputStream code = toStream("/*jshint maxparams:1  */ \n function cowboyFunction(param1, param2){return 'yee-haw!';}");
	final JSHint jsHint = new JSHint();

	// when
	List<JSHint.Error> errors = jsHint.run(code, options, globals);

	// then
	Assert.assertNotNull(errors);
	Assert.assertEquals(1, errors.size());
	Assert.assertEquals("Too many parameters per function (2).", errors.get(0).raw);
    }

    @Test
    public void supportsTheGlobalsParameter(){
	// given
	final String globals = "someGlobal";
	final String options = "undef";
	final InputStream code = toStream("(function(){var value = someGlobal();}());");
	final JSHint jsHint = new JSHint();

	// when
	List<JSHint.Error> errors = jsHint.run(code, options, globals);

	// then
	Assert.assertNotNull(errors);
	Assert.assertEquals("Expected no errors, but received:\n " + toString(errors), 0, errors.size());
    }
    
    private static InputStream toStream(String text){
	return new ByteArrayInputStream(text.getBytes());
    }
    
    private static String toString(List<Error> errors) {
	StringBuffer text = new StringBuffer();
	for(Error error: errors){
	    text.append(error.reason + "\n");
	}
	return text.toString();
    }
}
