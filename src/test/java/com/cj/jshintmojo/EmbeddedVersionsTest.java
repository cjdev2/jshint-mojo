package com.cj.jshintmojo;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.cj.jshintmojo.jshint.EmbeddedJshintCode;
import com.cj.jshintmojo.jshint.JSHint;
import com.cj.jshintmojo.jshint.JSHint.Error;

@RunWith(Parameterized.class)
public class EmbeddedVersionsTest {
    public static class OutputMessagesVariant {
        protected String expectedEvalIsEvilMessage(){
            return "eval can be harmful.";
        }

        protected String expectedErrorMessageForTwoTooManyParameters(){
            return "This function has too many parameters. (2)";
        }

        protected String expectedLineTooLongMessage(){
            return "Line is too long.";
        }
    }

    @Parameters(name = "Test for compatibility with jshint version {0}")
    public static Collection<Object[]> data() {
        List<Object[]> params = new ArrayList<Object[]>();
        for(String version : EmbeddedJshintCode.EMBEDDED_VERSIONS.keySet()){
            params.add(new Object[]{version});
        }
        return params;
    }

    private final String jshintVersion;
    private final OutputMessagesVariant variants;

    public EmbeddedVersionsTest(String jshintVersion) {
        super();
        this.jshintVersion = EmbeddedJshintCode.EMBEDDED_VERSIONS.get(jshintVersion);
        variants = new OutputMessagesVariant();
    }

    @Test
    public void booleanOptionsCanBeFalse(){
        // given
        final String globals = "";
        final String options = "evil:false";
        final InputStream code = toStream("eval('var x = 1 + 1;');");
        final JSHint jsHint = new JSHint(jshintVersion);

        // when
        List<JSHint.Error> errors = jsHint.run(code, options, globals);

        // then
        Assert.assertNotNull(errors);
        Assert.assertEquals(1, errors.size());
        Assert.assertEquals(variants.expectedEvalIsEvilMessage(), errors.get(0).reason);
    }

    @Test
    public void booleanOptionsCanBeTrue(){
        // given
        final String globals = "";
        final String options = "evil:true";
        final InputStream code = toStream("eval('var x = 1 + 1;');");
        final JSHint jsHint = new JSHint(jshintVersion);

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
        final String options = "maxlen:10";
        final InputStream code = toStream(" alert('Over Max Length');");
        final JSHint jsHint = new JSHint(jshintVersion);

        // when
        List<JSHint.Error> errors = jsHint.run(code, options, globals);

        // then
        Assert.assertNotNull(errors);
        Assert.assertEquals(1, errors.size());
        Assert.assertEquals(variants.expectedLineTooLongMessage(), errors.get(0).reason);
    }

    @Test
    public void supportsParametersWithValues(){
        // given
        final String globals = "";
        final String options = "maxparams:1";
        final InputStream code = toStream("function cowboyFunction(param1, param2){return 'yee-haw!';}");
        final JSHint jsHint = new JSHint(jshintVersion);

        // when
        List<JSHint.Error> errors = jsHint.run(code, options, globals);

        // then
        Assert.assertNotNull(errors);
        Assert.assertEquals(1, errors.size());
        Assert.assertEquals(variants.expectedErrorMessageForTwoTooManyParameters(), errors.get(0).reason);
    }

    @Test
    public void supportsParametersWithoutValues(){
        // given
        final String globals = "Foo";
        final String options = "nonew";
        final InputStream code = toStream("new Foo();");
        final JSHint jsHint = new JSHint(jshintVersion);

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
        final JSHint jsHint = new JSHint(jshintVersion);

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
        StringBuilder text = new StringBuilder ();
        for (Error error: errors){
            text.append (error.reason).append ("\n");
        }
        return text.toString();
    }
}
