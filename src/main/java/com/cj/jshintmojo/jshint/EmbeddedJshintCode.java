package com.cj.jshintmojo.jshint;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class EmbeddedJshintCode {
    public static final Map<String, String> EMBEDDED_VERSIONS = Collections.unmodifiableMap(new HashMap<String, String>(){{
        put("2.4.3", "jshint-rhino-2.4.3.js");
        put("2.4.1", "jshint-rhino-2.4.1.js");
        put("2.1.9", "jshint-rhino-2.1.9.js");
        put("r12", "jshint-r12.js");
    }});
}
