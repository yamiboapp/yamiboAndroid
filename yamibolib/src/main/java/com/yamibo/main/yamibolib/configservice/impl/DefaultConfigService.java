package com.yamibo.main.yamibolib.configservice.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Looper;

import com.dianping.configservice.ConfigChangeListener;
import com.dianping.configservice.ConfigService;
import com.dianping.dataservice.RequestHandler;
import com.dianping.dataservice.mapi.MApiRequest;
import com.dianping.dataservice.mapi.MApiResponse;
import com.dianping.dataservice.mapi.MApiService;
import com.dianping.util.Log;

public abstract class DefaultConfigService implements ConfigService,
		RequestHandler<MApiRequest, MApiResponse> {
	private Context context;
	private MApiService mapiService;
	private JSONObject dump;
	private HashMap<String, ArrayList<ConfigChangeListener>> listeners;
	private MApiRequest request;

	public DefaultConfigService(Context context, MApiService mapiService) {
		this.context = context;
		this.mapiService = mapiService;
		listeners = new HashMap<String, ArrayList<ConfigChangeListener>>();
	}

	/**
	 * 创建mapi请求，一般包含cityid, token等参数
	 */
	protected abstract MApiRequest createRequest();

	private File getConfigDir() {
		File dir = new File(context.getFilesDir(), "config");
		if (!dir.isDirectory()) {
			dir.delete();
			dir.mkdir();
		}

		return dir;
	}

	private File getConfigFile() {
		return new File(getConfigDir(), "1"); // 1 is a base version code
	}

	// read from file, return null if fail
	private JSONObject read() {
		File file = getConfigFile();
		if (!file.exists()) {
			File old = new File(context.getFilesDir(), "KFSDF09D0234GDSDSYERRA");
			if (old.exists()) {
				old.renameTo(file);
			}
		}
		if (file.exists()) {
			try {
				FileInputStream fis = new FileInputStream(file);
				if (fis.available() > 1000000)
					throw new IOException();
				byte[] buf = new byte[fis.available()];
				fis.read(buf);
				fis.close();
				String str = new String(buf, "UTF-8");
				JSONObject json = new JSONObject(str);
				return json;
			} catch (Exception e) {
			}
		} else {
		}
		return null;
	}

	private boolean write(JSONObject json, File file) {
		try {
			FileOutputStream fos = new FileOutputStream(file);
			fos.write(json.toString().getBytes("UTF-8"));
			fos.close();
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public JSONObject dump() {
		if (dump == null) {
			JSONObject dump = read();
			if (dump == null)
				dump = new JSONObject();
			this.dump = dump;
		}
		return dump;

	}

	@Override
	public void addListener(String key, ConfigChangeListener l) {
		synchronized (listeners) {
			ArrayList<ConfigChangeListener> list = listeners.get(key);
			if (list == null) {
				list = new ArrayList<ConfigChangeListener>();
				listeners.put(key, list);
			}
			list.add(l);
		}
	}

	@Override
	public void removeListener(String key, ConfigChangeListener l) {
		synchronized (listeners) {
			ArrayList<ConfigChangeListener> list = listeners.get(key);
			if (list != null) {
				list.remove(l);
				if (list.isEmpty()) {
					listeners.remove(key);
				}
			}
		}
	}

	@Override
	public void refresh() {
		if (request != null) {
			mapiService.abort(request, this, true);
		}
		request = createRequest();
		mapiService.exec(request, this);
	}

	@Override
	public void onRequestFinish(MApiRequest req, MApiResponse resp) {
		if (resp.result() instanceof String) {
			String str = (String) resp.result();
			try {
				JSONObject json = new JSONObject(str);
				setConfig(json);
			} catch (Exception e) {
				Log.w("config", "result from " + req + " is not a json object",
						e);
			}
		} else {
			Log.w("config", "result from " + req + " is not a string");
		}
	}

	@Override
	public void onRequestFailed(MApiRequest req, MApiResponse resp) {
		Log.i("config", "fail to refresh config from " + req);
	}

	public void setConfig(JSONObject dump) {
		if (dump == null)
			return;

		if (Thread.currentThread().getId() != Looper.getMainLooper()
				.getThread().getId()) {
			Log.w("config", "setConfig must be run under main thread");
			if (Log.LEVEL < Integer.MAX_VALUE) {
				throw new RuntimeException(
						"setConfig must be run under main thread");
			} else {
				return;
			}
		}
		File file = new File(getConfigDir(), new Random(
				System.currentTimeMillis()).nextInt()
				+ ".tmp");
		if (!write(dump, file)) {
			Log.w("config", "fail to write config to " + file);
			return;
		}
		if (!file.renameTo(getConfigFile())) {
			Log.w("config", "fail to move config file " + file);
			return;
		}
		JSONObject old = this.dump;
		this.dump = dump;

		ArrayList<ConfigChangeListener> list = listeners.get(ANY);
		if (list != null) {
			for (ConfigChangeListener l : list) {
				l.onConfigChange(ANY, old, dump);
			}
		}
		for (Map.Entry<String, ArrayList<ConfigChangeListener>> e : listeners
				.entrySet()) {
			String key = e.getKey();
			if (ANY.equals(key))
				continue;
			Object v1 = old.opt(key);
			Object v2 = dump.opt(key);
			boolean eq = (v1 == null) ? (v2 == null) : (v1.equals(v2));
			if (eq)
				continue;
			list = e.getValue();
			Log.i("config", "config changed, " + key + " has " + list.size()
					+ " listeners");
			for (ConfigChangeListener l : list) {
				l.onConfigChange(key, v1, v2);
			}
		}
	}
}
