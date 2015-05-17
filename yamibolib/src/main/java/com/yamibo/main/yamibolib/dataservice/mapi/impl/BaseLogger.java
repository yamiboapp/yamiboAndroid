package com.dianping.dataservice.mapi.impl;

import java.net.HttpURLConnection;
import java.net.URL;

import com.dianping.util.Log;

public abstract class BaseLogger implements Runnable {

	protected String dpid;
	protected String netInfo;
	protected String command;
	protected byte[] logInfo;

	public BaseLogger(String dpid, String command, String netInfo) {
		this.dpid = dpid;
		this.netInfo = netInfo;
		this.command = command;
	}

	public abstract byte[] buildLogInfo();

	@Override
	public void run() {
		try {
			URL u = new URL("http://114.80.165.63/broker-service/log");
			// URL u = new URL("http://182.92.229.130/dianping/log.php");
			HttpURLConnection c = (HttpURLConnection) u.openConnection();
			c.addRequestProperty("Content-Type", "text/plain");
			c.setDoOutput(true);
			c.setRequestMethod("POST");
			c.setReadTimeout(15000);
			c.getOutputStream().write(buildLogInfo());
			int code = c.getResponseCode();
			c.disconnect();
			Log.i("cat " + this.getClass() + " log " + code);
		} catch (Exception e) {
		}
	}

	public static String getCommand(String url) {
		if (url == null || url.length() == 0)
			return "";
		int b = url.indexOf('?');
		if (b < 0)
			b = url.length();
		int a = url.lastIndexOf('/', b);
		if (a < 0)
			a = -1;
		return url.substring(a + 1, b);
	}
}
