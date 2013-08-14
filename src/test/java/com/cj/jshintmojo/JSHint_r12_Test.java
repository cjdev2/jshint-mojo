package com.cj.jshintmojo;

public class JSHint_r12_Test extends AbstractJSHintTest {
    public JSHint_r12_Test() {
        super("r12");
    }
    
    @Override
    protected String expectedErrorMessageForTwoTooManyParameters() {
        return "Too many parameters per function (2).";
    }
    
    @Override
    protected String expectedEvalIsEvilMessage() {
        return "eval is evil.";
    }
}
