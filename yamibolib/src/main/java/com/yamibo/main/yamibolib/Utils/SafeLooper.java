package com.yamibo.main.yamibolib.Utils;

import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.MessageQueue;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 防止应用崩溃
 * <p/>
 * 调用SafeLooper.install()，主消息循环会被接管，所有的消息会运行在一个嵌套的子消息循环中<br>
 * 一旦崩溃，getUncaughtExceptionHandler()将会接收到崩溃的消息，但是主消息循环会继续执行。
 * <p/>
 * 注意SafeLooper不会处理背景线程的异常
 *
 * @author yimin.tu
 */
public class SafeLooper implements Runnable {
    private static final Object EXIT = new Object();
    private static boolean installed;
    private static Handler handler = new Handler(Looper.getMainLooper());
    private static Thread.UncaughtExceptionHandler uncaughtExceptionHandler;

    /**
     * 在下一个消息循环生效
     */
    public static void install() {
        handler.removeMessages(0, EXIT);
        handler.post(new SafeLooper());
    }

    public static void setUncaughtExceptionHandler(Thread.UncaughtExceptionHandler h) {
        uncaughtExceptionHandler = h;
    }

    @Override
    public void run() {
        if (installed)
            return;
        if (Looper.myLooper() != Looper.getMainLooper())
            return;

        Method next;
        Field target;
        Method recycleUnchecked = null;
        try {
            Method m = MessageQueue.class.getDeclaredMethod("next");
            m.setAccessible(true);
            next = m;
            Field f = Message.class.getDeclaredField("target");
            f.setAccessible(true);
            target = f;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Method method = Message.class.getDeclaredMethod("recycleUnchecked");
                method.setAccessible(true);
                recycleUnchecked = method;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        installed = true;
        MessageQueue queue = Looper.myQueue();
        Binder.clearCallingIdentity();
        final long ident = Binder.clearCallingIdentity();

        while (true) {
            try {
                Message msg = (Message) next.invoke(queue);
                if (msg == null || msg.obj == EXIT)
                    break;
                // Log.i("loop", String.valueOf(msg));

                Handler h = (Handler) target.get(msg);
                h.dispatchMessage(msg);
                final long newIdent = Binder.clearCallingIdentity();
                /*if (newIdent != ident) {
                }*/
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    if (recycleUnchecked != null) {
                        recycleUnchecked.invoke(msg);
                    }
                } else {
                    msg.recycle();
                }
            } catch (Exception e) {
                if (Environment.isDebug()) {
                    Thread.UncaughtExceptionHandler h =
                            Thread.getDefaultUncaughtExceptionHandler();
                    if (h != null) {
                        h.uncaughtException(Thread.currentThread(), e);
                    }
                    break;
                } else {
                    Thread.UncaughtExceptionHandler h = uncaughtExceptionHandler;
                    Throwable ex = e;
                    if (e instanceof InvocationTargetException) {
                        ex = ((InvocationTargetException) e).getCause();
                        if (ex == null) {
                            ex = e;
                        }
                    }
                    e.printStackTrace(System.err);
                    if (h != null) {
                        h.uncaughtException(Thread.currentThread(), ex);
                    }
                    new Handler().post(this);
                    break;
                }
            }
        }

        installed = false;
    }
}
