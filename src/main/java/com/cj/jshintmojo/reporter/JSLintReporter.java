package com.cj.jshintmojo.reporter;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.cj.jshintmojo.cache.Result;
import com.cj.jshintmojo.jshint.JSHint;

/**
 * JSLint style xml reporter class.
 */
public class JSLintReporter implements JSHintReporter {

    /**
     * format type of this reporter.
     */
    public static final String FORMAT = "jslint";

    @Override
    public String report(Map<String, Result> results) {
        if(results == null){
            return "";
        }
        StringBuilder buf = new StringBuilder();
        buf.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
        buf.append("<jslint>\n");
        Set<String> strings = results.keySet();
        String[] files = strings.toArray(new String[strings.size()]);
        Arrays.sort(files);
        for(String file : files){
            Result result = results.get(file);
            buf.append("\t<file name=\"").append(result.path).append("\">\n");
            for(JSHint.Error issue : result.errors){
                buf.append(String.format("\t\t<issue line=\"%d\" char=\"%d\" reason=\"%s\" evidence=\"%s\" ",
                        issue.line.intValue(), issue.character.intValue(), encode(issue.reason), encode(issue.evidence)));
                if(StringUtils.isNotEmpty(issue.code)){
                    buf.append("severity=\"").append(issue.code.charAt(0)).append("\" ");
                }
                buf.append("/>\n");
            }
            buf.append("\t</file>\n");
        }
        buf.append("</jslint>\n");

        return buf.toString();
    }
    
    private String encode(String str) {
        if(str == null){
            return "";
        }
        return str
                .replaceAll("&", "&amp;")
                .replaceAll("\"", "&quot;")
                .replaceAll("'", "&apos;")
                .replaceAll("<", "&lt;")
                .replaceAll(">", "&gt;");
    }

}
