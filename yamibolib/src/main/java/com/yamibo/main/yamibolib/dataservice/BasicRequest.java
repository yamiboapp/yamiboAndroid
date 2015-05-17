package com.dianping.dataservice;

/**
 * 最基本的包含url的Request<br>
 * 不包含比较函数，只有当引用相等时才相等
 * 
 * @author Yimin
 * 
 */
public class BasicRequest implements Request {
	private String url;

	public BasicRequest(String url) {
		this.url = url;
	}

	@Override
	public String url() {
		return url;
	}

	@Override
	public String toString() {
		return url;
	}
}
