package com.cj.jshintmojo.reporter;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

/**
 * Test cases of {@link CheckStyleReporter}.
 */
public class CheckStyleReporterTest {

    private final CheckStyleReporter reporter = new CheckStyleReporter();

    @Test
    public void formatType() {
        assertThat(CheckStyleReporter.FORMAT, is("checkstyle"));
    }

    @Test
    public void reportNullResults() {
        String report = this.reporter.report(null);
        assertThat(report, is(""));
    }

    @Test
    public void reportAllPassedResults() {
        String report = this.reporter.report(
                JSHintReporterTestUtil.createAllPassedResults());
        assertThat(report, is(
                "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<checkstyle version=\"4.3\">\n" +
                "</checkstyle>\n"));
    }

    @Test
    public void reportSingleFileFailedResults() {
        String report = this.reporter.report(
                JSHintReporterTestUtil.createSingleFileFailedResults());
        assertThat(report, is(
                "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<checkstyle version=\"4.3\">\n" +
                "\t<file name=\"/path/to/A.js\">\n" +
                "\t\t<warning line=\"5\" column=\"2\" message=\"Missing semicolon.\" source=\"jshint.W033\" severity=\"warning\" />\n" +
                "\t\t<warning line=\"12\" column=\"5\" message=\"Bad line breaking before &apos;&amp;&amp;&apos;.\" source=\"jshint.W014\" severity=\"warning\" />\n" +
                "\t\t<error line=\"1137\" column=\"26\" message=\"Too many hints.\" source=\"jshint.E043\" severity=\"error\" />\n" +
                "\t</file>\n" +
                "</checkstyle>\n"));
    }

    @Test
    public void reportMultipleFilesFailedResults() {
        String report = this.reporter.report(
                JSHintReporterTestUtil.createMultipleFilesFailedResults());
        assertThat(report, is(
                "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<checkstyle version=\"4.3\">\n" +
                "\t<file name=\"/path/to/A.js\">\n" +
                "\t\t<warning line=\"5\" column=\"2\" message=\"Missing semicolon.\" source=\"jshint.W033\" severity=\"warning\" />\n" +
                "\t\t<warning line=\"12\" column=\"5\" message=\"Bad line breaking before &apos;&amp;&amp;&apos;.\" source=\"jshint.W014\" severity=\"warning\" />\n" +
                "\t\t<error line=\"1137\" column=\"26\" message=\"Too many hints.\" source=\"jshint.E043\" severity=\"error\" />\n" +
                "\t</file>\n" +
                "\t<file name=\"/path/to/B.js\">\n" +
                "\t\t<info line=\"10\" column=\"5\" message=\"Comma warnings can be turned off with &apos;laxcomma&apos;.\" source=\"jshint.I001\" severity=\"info\" />\n" +
                "\t</file>\n" +
                "\t<file name=\"/path/to/C.js\">\n" +
                "\t\t<warning line=\"3\" column=\"14\" message=\"&apos;args&apos; is already defined.\" source=\"jshint.W004\" severity=\"warning\" />\n" +
                "\t\t<warning line=\"12\" column=\"5\" message=\"Use &apos;===&apos; to compare with &apos;0&apos;.\" source=\"jshint.W041\" severity=\"warning\" />\n" +
                "\t</file>\n" +
                "</checkstyle>\n"));
    }

}
