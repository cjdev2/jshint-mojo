package com.cj.jshintmojo.reporter;

import java.util.Arrays;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.cj.jshintmojo.cache.Result;
import com.cj.jshintmojo.jshint.JSHint;

/**
 * CheckStyle style xml reporter class.
 */
public class CheckStyleReporter implements JSHintReporter {

    /**
     * format type of this reporter.
     */
    public static final String FORMAT = "checkstyle";

    @Override
    public String report(Map<String, Result> results) {
        if(results == null){
            return "";
        }
        StringBuilder buf = new StringBuilder();
        buf.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
        buf.append("<checkstyle version=\"4.3\">\n");
        String[] files = results.keySet().toArray(new String[0]);
        Arrays.sort(files);
        for(String file : files){
            Result result = results.get(file);
            buf.append("\t<file name=\"" + result.path + "\">\n");
            for(JSHint.Error error : result.errors){
                buf.append(String.format("\t\t<error line=\"%d\" column=\"%d\" message=\"%s\" source=\"jshint.%s\" severity=\"%s\" />\n",
                        error.line.intValue(), error.character.intValue(), encode(error.reason), encode(error.code), severity(error.code)));
            }
            buf.append("\t</file>\n");
        }
        buf.append("</checkstyle>\n");

        return buf.toString();
    }
    
    private String severity(String errorCode) {
        if(StringUtils.isNotEmpty(errorCode)){
            switch(errorCode.charAt(0)){
            case 'E':
                return "error";
            case 'I':
                return "info";
            case 'W':
            default:
                break;
            }
        }
        return "warning";
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
