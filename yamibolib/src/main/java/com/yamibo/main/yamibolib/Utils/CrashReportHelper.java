package com.yamibo.main.yamibolib.Utils;

import android.content.Context;
import android.os.Build;

import com.yamibo.main.yamibolib.app.YMBApplication;
import com.yamibo.main.yamibolib.dataservice.http.HttpService;
import com.yamibo.main.yamibolib.dataservice.http.impl.BasicHttpRequest;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class CrashReportHelper {
    private static final java.util.LinkedList<String> listSchemaAll = new java.util.LinkedList<>();
    private static final String TAG = CrashReportHelper.class.getSimpleName();
    private static final CrashHandler unknownCrashHandler;
    private static final SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    static {
        defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        unknownCrashHandler = new CrashHandler();
        Thread.setDefaultUncaughtExceptionHandler(unknownCrashHandler);
    }

    public static int versionCode = 0;
    public static String versionName = null;
    public static File reportFile;

    // 记录URL Schema 跳转历史，打印到Crash Log中，方便定位错误位置
    public static long lastOutOfMemoryMills;
    public static boolean debug = true;
    private static Thread.UncaughtExceptionHandler defaultHandler;

    public static void initialize(Context context) {
        if (reportFile != null)
            return;
        versionCode = Environment.versionCode();
        versionName = Environment.versionName();
        debug = Environment.isDebug();

        reportFile = new File(context.getFilesDir(), "crash_report");
        File oomFile = new File(reportFile.getParent(), "out_of_memory");
        if (oomFile.exists()) {
            lastOutOfMemoryMills = oomFile.lastModified();
            oomFile.delete();
        }
    }

    public static void installSafeLooper() {
        SafeLooper.install();
        if (!Environment.isDebug()) {
            // 不抛出去
            defaultHandler = null;
        }
        SafeLooper.setUncaughtExceptionHandler(unknownCrashHandler);
    }

    /**
     * 向栈中添加一条新的URL Schema。在onResume的时候
     *
     * @param urlSchema
     */
    public static void putUrlSchemaOnShow(String urlSchema) {
        if (android.text.TextUtils.isEmpty(urlSchema)) {
            return;
        }
        putUrlSchemeInternal("show: " + urlSchema);
    }

    private static void putUrlSchemeInternal(String urlSchema) {
        if (android.text.TextUtils.isEmpty(urlSchema)) {
            return;
        }

        if (listSchemaAll.size() > 20) {
            listSchemaAll.removeLast();
        }

        listSchemaAll.addFirst(urlSchema + " " + fmt.format(new Date(System.currentTimeMillis())));

        if (Environment.isDebug()) {
            Log.d(TAG, "putUrlSchemeInternal: " + urlSchema + " " + fmt.format(new Date(System.currentTimeMillis())));
        }
    }

    private static boolean hasOutOfMemoryError(Throwable ex) {
        if (ex == null)
            return false;
        Throwable next = ex.getCause();
        for (int i = 0; i < 0xF; i++) {
            if (ex instanceof OutOfMemoryError)
                return true;
            if (next == null || next == ex)
                return false;
            ex = next;
            next = ex.getCause();
        }
        return false;
    }

    public static boolean isAvailable() {
        return reportFile != null && reportFile.exists();
    }

    public static String getReport() {
        if (reportFile == null)
            return null;

        FileInputStream fis = null;
        try {
            fis = new FileInputStream(reportFile);
            if (fis.available() > 64 * 1000)
                return null;
            byte[] buf = new byte[fis.available()];
            fis.read(buf);
            return new String(buf);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static String getReportBak() {
        if (reportFile == null)
            return null;
        File bak = new File(reportFile.getParent(), reportFile.getName() + ".bak");
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(bak);
            if (fis.available() > 64 * 1000)
                return null;
            byte[] buf = new byte[fis.available()];
            fis.read(buf);
            return new String(buf);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static boolean deleteReport() {
        if (reportFile == null)
            return false;
        File bak = new File(reportFile.getParent(), reportFile.getName() + ".bak");
        bak.delete();
        return reportFile.renameTo(bak);
    }

    public static void sendAndDelete() {
        if (!isAvailable())
            return;
        final String report = getReport();
        deleteReport();
        Log.i(TAG, report);
        if (report == null || !report.startsWith("====") || report.contains("debug=true"))
            return;
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("module", "crash"));
        params.add(new BasicNameValuePair("crash", report));

        httpService().exec(BasicHttpRequest.httpPost(Environment.HTTP_ADDRESS, params), null);
    }

    static HttpService httpService() {
        return YMBApplication.instance().httpService();
    }

    public static class CrashHandler implements Thread.UncaughtExceptionHandler {
        @Override
        public final void uncaughtException(Thread thread, Throwable ex) {
            boolean oom = false;
            try {
                if (reportFile == null)
                    return;
                reportFile.delete();
                if (oom = hasOutOfMemoryError(ex)) {
                    File oomFile = new File(reportFile.getParent(), "out_of_memory");
                    oomFile.delete();
                    oomFile.createNewFile();
                }
                PrintWriter w = new PrintWriter(reportFile, "utf-8");

                w.print("===============================");
                w.print(UUID.randomUUID().toString());
                w.println("============================");

                if (debug)
                    w.println("debug=true");

                w.print("addtime=");
                w.println(fmt.format(new Date(System.currentTimeMillis())));

                w.print("deviceid=");
                //noinspection deprecation
                w.println(Environment.deviceId());

                w.print("os-version=");
                w.println(Build.VERSION.RELEASE);

                w.print("os-build=");
                w.println(Build.ID);

                w.print("device-brand=");
                w.println(Build.BRAND);

                w.print("device-model=");
                w.println(Build.MODEL);

                w.print("device-fingerprint=");
                w.println(Build.FINGERPRINT);

                w.print("thread=");
                w.println(thread.getName());

                w.println();
                ex.printStackTrace(w);
                w.println();
                w.println();

                w.println("Url Schema history full:");
                for (String url : listSchemaAll) {
                    w.println(url);
                }

                w.flush();
                w.close();
            } catch (Throwable t) {
                t.printStackTrace();
            } finally {
                if (defaultHandler != null) {
                    defaultHandler.uncaughtException(thread, ex);
                }
            }
        }
    }
}
