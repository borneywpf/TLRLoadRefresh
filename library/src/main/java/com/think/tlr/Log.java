package com.think.tlr;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Created by borney on 4/28/17.
 */
public class Log {
    private static final String DEF_TAG = "TLRLoadRefresh";

    public static void d(String msg) {
        d(DEF_TAG, msg);
    }

    public static void d(String tag, String msg) {
        android.util.Log.d(tag, msg);
    }

    public static void i(String msg) {
        i(DEF_TAG, msg);
    }

    public static void i(String tag, String msg) {
        android.util.Log.i(tag, msg);
    }

    public static void w(String msg) {
        w(DEF_TAG, msg);
    }

    public static void w(String tag, String msg) {
        android.util.Log.w(tag, msg);
    }

    public static void e(String msg) {
        e(DEF_TAG, msg);
    }

    public static void e(String tag, String msg) {
        android.util.Log.e(tag, msg);
    }

    public static void e(Throwable thx) {
        e(DEF_TAG, thx);
    }

    public static void e(String tag, Throwable thx) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        thx.printStackTrace(pw);
        e(tag, sw.toString());
    }
}
