package com.yamibo.main.yamibolib.Utils;

/**
 * Created by wangxiaoyan on 15/4/20.
 */
public class Log {

    public final static String TAG = "YMB";

    public static int LEVEL = android.util.Log.VERBOSE;

    public static boolean isDebug() {
        return Environment.isDebug();
    }

    public static void v(Object msg) {
        v(TAG, msg);
    }

    public static void v(String tag, Object msg) {
        if (isDebug()) {
            if (LEVEL <= android.util.Log.VERBOSE) {
                android.util.Log.v(tag, getFinalMsg(msg));
            }
        }
    }

    public static void d(Object msg) {
        d(TAG, msg);
    }

    public static void d(String tag, Object msg) {
        if (isDebug()) {
            if (LEVEL <= android.util.Log.DEBUG) {
                android.util.Log.d(tag, getFinalMsg(msg));
            }
        }
    }

    public static void i(Object msg) {
        i(TAG, msg);
    }

    public static void i(String tag, Object msg) {
        if (isDebug()) {
            if (LEVEL <= android.util.Log.INFO) {
                android.util.Log.i(tag, getFinalMsg(msg));
            }
        }
    }

    public static void w(Object msg) {
        w(TAG, msg);
    }

    public static void w(String tag, Object msg) {
        if (isDebug()) {
            if (LEVEL <= android.util.Log.WARN) {
                android.util.Log.w(tag, getFinalMsg(msg));
            }

        }
    }

    public static void e(Object msg) {
        e(TAG, msg);
    }

    public static void e(String tag, Object msg) {
        if (isDebug()) {
            if (LEVEL <= android.util.Log.ERROR) {
                android.util.Log.e(tag, getFinalMsg(msg));
            }
        }
    }

    public static void e(Object msg, Throwable e) {
        e(TAG, msg, e);
    }

    public static void e(String tag, Object msg, Throwable e) {
        if (isDebug()) {
            if (LEVEL <= android.util.Log.ERROR) {
                android.util.Log.e(tag, getFinalMsg(msg), e);
            }
        }
    }

    private static String getFinalMsg(Object msg) {
        String realMsg = (msg != null) ? msg.toString() : "null";
        String finalMsg = realMsg;

        String extraMsg = getExtraMsg();
        if (extraMsg != null) {
            finalMsg = extraMsg + " - " + realMsg;
        }

        return finalMsg;
    }

    private static String getExtraMsg() {
        StackTraceElement[] sts = Thread.currentThread().getStackTrace();
        if (sts == null) {
            return null;
        }

        for (StackTraceElement st : sts) {
            if (st.isNativeMethod()) {
                continue;
            }

            if (st.getClassName().equals(Thread.class.getName())) {
                continue;
            }

            if (st.getClassName().equals(Log.class.getName())) {
                continue;
            }

            StringBuilder strBuilder = new StringBuilder();
            strBuilder.append("t:");
            strBuilder.append(Thread.currentThread().getName());
            strBuilder.append(" f:");
            strBuilder.append(st.getFileName());
            strBuilder.append(" l:");
            strBuilder.append(st.getLineNumber());
            strBuilder.append(" m:");
            strBuilder.append(st.getMethodName());

            return strBuilder.toString();
        }

        return null;
    }

}
