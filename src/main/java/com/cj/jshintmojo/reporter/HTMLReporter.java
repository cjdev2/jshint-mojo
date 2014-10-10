package com.cj.jshintmojo.reporter;

import java.util.Arrays;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.cj.jshintmojo.cache.Result;
import com.cj.jshintmojo.jshint.JSHint;

/**
 * HTML style reporter class.
 */
public class HTMLReporter implements JSHintReporter {

    /**
     * format type of this reporter.
     */
    public static final String FORMAT = "html";

    @Override
    public String report(Map<String, Result> results) {
        if(results == null){
            return "";
        }
        StringBuilder buf = new StringBuilder();
        buf.append("<!DOCTYPE HTML>\n");
        buf.append("<html>\n");
        String[] files = results.keySet().toArray(new String[0]);
        Arrays.sort(files);
        for(String file : files){
            Result result = results.get(file);
            buf.append("<h2>" + result.path + "</h2>\n");
            for(JSHint.Error issue : result.errors){
                buf.append(String.format("\t\t<div style=\"background-color:#2956B2;color:white;padding:4px\"><span style=\"padding-right:40px;padding-left:4px;\">line:%d char:%d</span><span style=\"font-weight:bold;padding-right:50px;\">%s</span></div><div style=\"margin-left:20px;margin-bottom:1em;font-size:11pt;font-family:consolas;\"><p>%s</p></div>",
                		issue.line.intValue(), issue.character.intValue(), encode(issue.reason), encode(issue.evidence)));
            }
        }
        buf.append("</html>\n");

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
