package com.yamibo.main.yamibolib.app;

import android.content.Context;
import android.text.TextUtils;

import org.apache.http.HttpHost;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.params.ConnRouteParams;

import java.net.URI;
import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by wangxiaoyan on 15/4/20.
 */
public class ServiceManager {

    private final Context context;

    public ServiceManager(Context context) {
        this.context = context;
    }

    public synchronized Object getService(String name) {
        if ("http".equals(name)) {
            if (http == null) {
                Executor executor = new ThreadPoolExecutor(2, 6, 60, TimeUnit.SECONDS,
                        new LinkedBlockingQueue<Runnable>());
                http = new DefaultHttpService(context, executor);
            }
            return http;
        }
        if ("image".equals(name)) {
            if (image == null) {
                MonitorService moni = (MonitorService) getService("monitor");
                image = new DefaultImageService(context, 2, moni);
            }
            return image;
        }
        if ("image_cahce".equals(name)) {
            if (image == null) {
                getService("image");
            }
            return image.cache();
        }
        if ("mapi".equals(name) || "mapi_original".equals(name)) {
            if (mapi_orig == null) {
                if (account == null) {
                    getService("account");
                }
                StatisticsService stat = (StatisticsService) getService("pvprocess");
                MonitorService moni = (MonitorService) getService("monitor");
                mapi_orig = new DefaultMApiService(context, Environment.mapiUserAgent(),
                        Environment.imei(), Environment.uuid(), configProxy, account, stat, moni) {

                    Random rnd = new Random(System.currentTimeMillis());

                    private void switchDomain(HttpUriRequest request, String to,
                                              String defaultDomain, String url) {

                        String head = to.startsWith("http://") ? "" : "http://";
                        head += to;
                        if (!head.endsWith("/")) {
                            head += "/";
                        }
                        url = head + url.substring(defaultDomain.length());
                        Log.i("mapi", "mapi_debug url:" + url);
                        if (request instanceof HttpGet) {
                            ((HttpGet) request).setURI(URI.create(url));
                        } else if (request instanceof HttpPost) {
                            ((HttpPost) request).setURI(URI.create(url));
                        }
                    }

                    @Override
                    protected HttpUriRequest transferUriRequest(HttpRequest original,
                                                                HttpUriRequest request) throws Exception {
                        if (mapi_debug != null) {
                            if (mapi_debug.nextFail > 0) {
                                mapi_debug.nextFail--;
                                throw new Exception("这是一次模拟的网络错误");
                            }
                            if (mapi_debug.failHalf) {
                                if (rnd.nextBoolean()) {
                                    throw new Exception("这是一次模拟的网络错误");
                                }
                            }

                            String url = request.getURI().toString();

                            // 主的domain
                            if (mapi_debug.switchDomain != null
                                    && mapi_debug.switchDomain.length() > 0
                                    && url.startsWith(sDomains[0])) {
                                switchDomain(request, mapi_debug.switchDomain, sDomains[0], url);
                            }

                            /** 预约预定的domain */
                            if (mapi_debug.bookingDebugDomain != null
                                    && mapi_debug.bookingDebugDomain.length() > 0
                                    && url.startsWith(sDomains[1])) {
                                switchDomain(request, mapi_debug.bookingDebugDomain, sDomains[1],
                                        url);
                            }

                            /** 团购的domain */
                            if (mapi_debug.tDebugDomain != null
                                    && mapi_debug.tDebugDomain.length() > 0
                                    && url.startsWith(sDomains[2])) {
                                switchDomain(request, mapi_debug.tDebugDomain, sDomains[2], url);
                            }

                            /** 支付的domain */
                            if (mapi_debug.payDebugDomain != null
                                    && mapi_debug.payDebugDomain.length() > 0
                                    && url.startsWith(sDomains[6])) {
                                switchDomain(request, mapi_debug.payDebugDomain, sDomains[6], url);
                            }

                            /** 会员卡的domain */
                            if (mapi_debug.membercardDebugDomain != null
                                    && mapi_debug.membercardDebugDomain.length() > 0
                                    && url.startsWith(sDomains[3])) {
                                switchDomain(request, mapi_debug.membercardDebugDomain,
                                        sDomains[3], url);
                            }

                            /** 外卖的domain */
                            if (mapi_debug.takeawayDebugDomain != null
                                    && mapi_debug.takeawayDebugDomain.length() > 0
                                    && url.startsWith(sDomains[7])) {
                                switchDomain(request, mapi_debug.takeawayDebugDomain, sDomains[7],
                                        url);
                            }

                            /** 惠惠的domain */
                            if (mapi_debug.huihuiDebugDomain != null
                                    && mapi_debug.huihuiDebugDomain.length() > 0
                                    && url.startsWith(sDomains[9])) {
                                switchDomain(request, mapi_debug.huihuiDebugDomain, sDomains[9],
                                        url);
                            }

                            /** 点菜的domain */
                            if (!TextUtils.isEmpty(mapi_debug.menuDebugDomain)
                                    && url.startsWith(sDomains[8])) {
                                switchDomain(request, mapi_debug.menuDebugDomain, sDomains[8], url);
                            }

                            /** 排队的domain */
                            if (!TextUtils.isEmpty(mapi_debug.queueDebugDomain)
                                    && url.startsWith(sDomains[12])) {
                                switchDomain(request, mapi_debug.queueDebugDomain, sDomains[12], url);
                            }

                            /** 丽人的domain */
                            if (!TextUtils.isEmpty(mapi_debug.beautyDebugDomain)
                                    && url.startsWith(sDomains[10])) {
                                switchDomain(request, mapi_debug.beautyDebugDomain, sDomains[10],
                                        url);
                            }

                            /** 定位的domain */
                            if (mapi_debug.locateDebugDomain != null
                                    && mapi_debug.locateDebugDomain.length() > 0
                                    && url.startsWith(sDomains[4])) {
                                switchDomain(request, mapi_debug.locateDebugDomain, sDomains[4],
                                        url);
                            }

                            /** config的domain */
                            if (mapi_debug.configDebugDomain != null
                                    && mapi_debug.configDebugDomain.length() > 0
                                    && url.startsWith(sDomains[5])) {
                                switchDomain(request, mapi_debug.configDebugDomain, sDomains[5],
                                        url);
                            }

                            /** 电影的domain */
                            if (mapi_debug.movieDebugDomain != null
                                    && mapi_debug.movieDebugDomain.length() > 0
                                    && url.startsWith(sDomains[11])) {
                                switchDomain(request, mapi_debug.movieDebugDomain, sDomains[11], url);
                            }

                            if (mapi_debug.delay > 0) {
                                Thread.sleep(mapi_debug.delay);
                            }

                            String proxy = mapi_debug.proxy;
                            int proxyPort = mapi_debug.proxyPort;
                            if (!TextUtils.isEmpty(proxy) && proxyPort > 0) {
                                HttpHost proxyHost = new HttpHost(proxy, proxyPort);
                                ConnRouteParams.setDefaultProxy(request.getParams(), proxyHost);
                            }

                            return request;
                        } else {
                            return super.transferUriRequest(request);
                        }
                    }
                };
            }
            return mapi_orig;
        }

        if ("config".equals(name)) {
            if (config == null) {
                getService("mapi");
                config = new MyConfigService(context, mapi_orig);
            }
            return config;
        }
        if ("account".equals(name)) {
            if (account == null) {
                account = new DefaultAccountService(context);
            }
            return account;
        }
        if ("location".equals(name)) {
            if (location == null) {
                location = new LocationServiceProxy(context);
            }
            return location;
        }
        if ("statistics".equals(name)) {
            if (statistics == null) {
                statistics = new MyStatisticsService(context,
                        "http://stat.api.dianping.com/utm.js?v=androidv1");
            }
            return statistics;
        }
        return null;
    }

}
