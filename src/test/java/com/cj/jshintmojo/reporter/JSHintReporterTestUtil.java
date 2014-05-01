package com.cj.jshintmojo.reporter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cj.jshintmojo.cache.Result;
import com.cj.jshintmojo.jshint.JSHint;
import com.cj.jshintmojo.jshint.JSHint.Hint;

/**
 * Utility class to create test data for {@link JSHintReporter}.
 */
class JSHintReporterTestUtil {

    private JSHintReporterTestUtil() {
    }

    private static JSHint.Error createError(final String code, final int line,
            final int character, final String evidence, final String reason)
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
            List<Hint> hints = new ArrayList<Hint>();
            hints.add(createError("W033", 5, 2, "}", "Missing semicolon."));
            hints.add(createError("W014", 12, 5, "    && window.setImmediate;", "Bad line breaking before '&&'."));
            hints.add(createError("E043", 1137, 26, null, "Too many hints."));
            Result result = new Result("/path/to/A.js", 1377309240000L, hints);
            results.put(result.path, result);
        }
        return results;
    }

    static Map<String, Result> createMultipleFilesFailedResults() {
        Map<String, Result> results = new HashMap<String, Result>();
        {
            List<Hint> hints = new ArrayList<Hint>();
            hints.add(createError("W033", 5, 2, "}", "Missing semicolon."));
            hints.add(createError("W014", 12, 5, "    && window.setImmediate;", "Bad line breaking before '&&'."));
            hints.add(createError("E043", 1137, 26, null, "Too many hints."));
            Result result = new Result("/path/to/A.js", 1377309240000L, hints);
            results.put(result.path, result);
        }
        {
            List<Hint> hints = new ArrayList<Hint>();
            hints.add(createError("I001", 10, 5, "    , [info, \"info\"]", "Comma warnings can be turned off with 'laxcomma'."));
            Result result = new Result("/path/to/B.js", 1377309240000L, hints);
            results.put(result.path, result);
        }
        {
            List<Hint> hints = new ArrayList<Hint>();
            hints.add(createError("W004", 3, 14, "    var args = a;", "'args' is already defined."));
            hints.add(createError("W041", 12, 5, "    if (list.length == 0)", "Use '===' to compare with '0'."));
            Result result = new Result("/path/to/C.js", 1377309240000L, hints);
            results.put(result.path, result);
        }
        return results;
    }

}
