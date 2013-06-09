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
    public void booleanOptionsCanBeFalse(){
        // given
        final String globals = "";
        final String options = "evil:false";
        final InputStream code = toStream("eval('var x = 1 + 1;');");
        final JSHint jsHint = new JSHint();

        // when
        List<JSHint.Error> errors = jsHint.run(code, options, globals);

        // then
        Assert.assertNotNull(errors);
        Assert.assertEquals(1, errors.size());
        Assert.assertEquals("eval is evil.", errors.get(0).reason);
    }
    
    @Test
    public void booleanOptionsCanBeTrue(){
        // given
        final String globals = "";
        final String options = "evil:true";
        final InputStream code = toStream("eval('var x = 1 + 1;');");
        final JSHint jsHint = new JSHint();

        // when
        List<JSHint.Error> errors = jsHint.run(code, options, globals);

        // then
        Assert.assertNotNull(errors);
        Assert.assertEquals(0, errors.size());
    }
    
    @Test
    public void supportsOptionsThatTakeANumericValue(){
        // given
        final String globals = "alert";
        final String options = "indent:4";
        final InputStream code = toStream(" alert('Bad Indentation');");
        final JSHint jsHint = new JSHint();

        // when
        List<JSHint.Error> errors = jsHint.run(code, options, globals);

        // then
        Assert.assertNotNull(errors);
        Assert.assertEquals(1, errors.size());
        Assert.assertEquals("Expected 'alert' to have an indentation at 1 instead at 2.", errors.get(0).reason);
    }
    
    @Test
    public void supportsParametersWithValues(){
        // given
        final String globals = "";
        final String options = "maxparams:1";
        final InputStream code = toStream("function cowboyFunction(param1, param2){return 'yee-haw!';}");
        final JSHint jsHint = new JSHint();

        // when
        List<JSHint.Error> errors = jsHint.run(code, options, globals);

        // then
        Assert.assertNotNull(errors);
        Assert.assertEquals(1, errors.size());
        Assert.assertEquals("Too many parameters per function (2).", errors.get(0).raw);
    }
    
    @Test
    public void supportsParametersWithoutValues(){
        // given
        final String globals = "Foo";
        final String options = "nonew";
        final InputStream code = toStream("new Foo();");
        final JSHint jsHint = new JSHint();

        // when
        List<JSHint.Error> errors = jsHint.run(code, options, globals);

        // then
        Assert.assertNotNull(errors);
        Assert.assertEquals(1, errors.size());
        Assert.assertEquals("Do not use 'new' for side effects.", errors.get(0).raw);
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
