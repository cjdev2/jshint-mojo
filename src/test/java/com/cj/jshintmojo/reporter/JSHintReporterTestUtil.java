package com.cj.jshintmojo.reporter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cj.jshintmojo.cache.Result;
import com.cj.jshintmojo.jshint.JSHint;

/**
 * Utility class to create test data for {@link JSHintReporter}.
 */
class JSHintReporterTestUtil {

    private JSHintReporterTestUtil() {
    }

    private static JSHint.Error createError(String code, int line,
            int character, String evidence, String reason)
    {
        JSHint.Error error = new JSHint.Error();
        error.id = "(error)";
        error.code = code;
        error.line = line;
        error.character = character;
        error.evidence = evidence;
        error.reason = reason;
        error.raw = reason;
        return error;
    }

    static Map<String, Result> createAllPassedResults() {
        return new HashMap<String, Result>();
    }

    static Map<String, Result> createSingleFileFailedResults() {
        Map<String, Result> results = new HashMap<String, Result>();
        {
            List<JSHint.Error> errors = new ArrayList<JSHint.Error>();
            errors.add(createError("W033", 5, 2, "}", "Missing semicolon."));
            errors.add(createError("W014", 12, 5, "    && window.setImmediate;", "Bad line breaking before '&&'."));
            errors.add(createError("E043", 1137, 26, null, "Too many errors."));
            Result result = new Result("/path/to/A.js", 1377309240000L, errors);
            results.put(result.path, result);
        }
        return results;
    }

    static Map<String, Result> createMultipleFilesFailedResults() {
        Map<String, Result> results = new HashMap<String, Result>();
        {
            List<JSHint.Error> errors = new ArrayList<JSHint.Error>();
            errors.add(createError("W033", 5, 2, "}", "Missing semicolon."));
            errors.add(createError("W014", 12, 5, "    && window.setImmediate;", "Bad line breaking before '&&'."));
            errors.add(createError("E043", 1137, 26, null, "Too many errors."));
            Result result = new Result("/path/to/A.js", 1377309240000L, errors);
            results.put(result.path, result);
        }
        {
            List<JSHint.Error> errors = new ArrayList<JSHint.Error>();
            errors.add(createError("I001", 10, 5, "    , [info, \"info\"]", "Comma warnings can be turned off with 'laxcomma'."));
            Result result = new Result("/path/to/B.js", 1377309240000L, errors);
            results.put(result.path, result);
        }
        {
            List<JSHint.Error> errors = new ArrayList<JSHint.Error>();
            errors.add(createError("W004", 3, 14, "    var args = a;", "'args' is already defined."));
            errors.add(createError("W041", 12, 5, "    if (list.length == 0)", "Use '===' to compare with '0'."));
            Result result = new Result("/path/to/C.js", 1377309240000L, errors);
            results.put(result.path, result);
        }
        return results;
    }

}
