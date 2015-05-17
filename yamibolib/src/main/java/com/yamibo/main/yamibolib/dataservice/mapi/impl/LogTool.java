package com.dianping.dataservice.mapi.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.http.NameValuePair;

import android.os.Environment;

import com.dianping.archive.DPObject;
import com.dianping.dataservice.http.HttpRequest;
import com.dianping.dataservice.http.HttpResponse;
import com.dianping.dataservice.mapi.MApiResponse;
import com.dianping.model.SimpleMsg;
import com.dianping.util.URLBase64;

public class LogTool {

	final static DateFormat FMT_TIME = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");
	final static DateFormat FMT_DATE = new SimpleDateFormat("yyyyMMdd.HHmm");

	static File logFile = null;

	public static void log(HttpRequest req, HttpResponse resp,
			MApiResponse respM) {
		try {
			if (logFile == null) {
				logFile = new File(Environment.getExternalStorageDirectory(),
						FMT_DATE.format(new Date()) + ".log");
			}
			FileOutputStream fos = new FileOutputStream(logFile, true);
			OutputStreamWriter w = new OutputStreamWriter(fos, "UTF-8");
			log(w, req, resp, respM);
			w.close();
			fos.close();
		} catch (Exception e) {
		}
	}

	public static void log(Writer w, HttpRequest req, HttpResponse resp,
			MApiResponse respM) throws IOException {
		w.append("========== ").append(FMT_TIME.format(new Date()))
				.append(" ==========\n");
		w.append(req.method()).append(" ").append(req.url()).append('\n');
		if (req.headers() != null) {
			for (NameValuePair p : req.headers()) {
				w.append(p.getName()).append(": ").append(p.getValue())
						.append('\n');
			}
		}
		w.append('\n');
		if (req.input() != null) {
			w.append(String.valueOf(req.input()));
		}
		w.append('\n').append('\n');

		w.append("" + resp.statusCode()).append('\n');
		if (resp.headers() != null) {
			for (NameValuePair p : resp.headers()) {
				w.append(p.getName()).append(": ").append(p.getValue())
						.append('\n');
			}
		}
		w.append('\n');

		if (resp.error() != null) {
			if (resp.error() instanceof Exception) {
				PrintWriter pw = new PrintWriter(w);
				((Exception) resp.error()).printStackTrace(pw);
				pw.close();
			} else {
				w.append(String.valueOf(resp.error()));
			}
			w.append('\n');
		} else {
			byte[] bytes = (byte[]) resp.result();
			int n = Math.min(bytes.length, 512);
			int c = 0;
			for (int i = 0; i < n; i++) {
				byte b = bytes[i];
				if (b >= ' ' && b <= '~' || b == '\n' || b == '\r' || b == '\t') {
					c++;
				}
			}
			if (c * 100 / n > 95) { // 95%
				w.append(new String(bytes, "UTF-8"));
			} else {
				w.append(URLBase64.encode(bytes));
			}
			w.append('\n');
		}
		w.append('\n').append('\n');

		if (respM == null)
			return;

		w.append("" + respM.statusCode()).append('\n');
		if (respM.error() != null) {
			if (respM.error() instanceof SimpleMsg) {
				w.append(String.valueOf(respM.error()));
			}
		} else {
			if (respM.result() instanceof DPObject) {
				w.append("DPObject, type="
						+ Integer.toHexString(((DPObject) respM.result())
								.getClassHash16()));
			} else if (respM.result() instanceof DPObject[]) {
				DPObject[] arr = (DPObject[]) respM.result();
				if (arr.length == 0) {
					w.append("DPObject[0]\n");
				} else {
					w.append("DPObject["
							+ arr.length
							+ "], type="
							+ Integer.toHexString(((DPObject) arr[0])
									.getClassHash16()));
				}
			} else if (respM.result() instanceof String) {
				w.append((String) respM.result());
			}
			w.append('\n');
		}
		w.append('\n').append('\n');
	}
}
