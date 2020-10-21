package io.strimzi.http.server.logging;

import java.io.PrintWriter;
import java.io.StringWriter;

public class Utils {

    public static String formatStacktrace(final Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        return sw.toString();
    }

}
