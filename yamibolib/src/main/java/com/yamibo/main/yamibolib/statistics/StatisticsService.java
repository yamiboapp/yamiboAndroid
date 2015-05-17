package com.yamibo.main.yamibolib.statistics;

import java.util.List;

import org.apache.http.NameValuePair;

/**
 * 统计服务
 * <p>
 * 基于文本行的统计系统，在本地进行累加。当累加个数或时间达到阈值则触发上传并清空本地记录
 * 
 * @author Yimin
 * 
 */
public interface StatisticsService {
	public static final String ACTION_UPLOAD_SUCCESS = "com.dianping.action.STAT_UPLOAD_SUCCESS";

	/**
	 * 新增一条记录，包含完整的记录<br>
	 * 记录中不可包含换行符
	 */
	void push(String rawLine);

	/**
	 * 新增一条记录，包含自定义字段<br>
	 * 其余平台相关字段由实现类补全（如deviceid, cityid等）
	 */
	void record(List<NameValuePair> form);

	/**
	 * PV统计
	 * <p>
	 * PV统计是最常用的统计方式之一，记录了用户访问的内容。
	 * 
	 * @param url
	 *            当前请求API的url。包括GET和POST
	 * @param extras
	 *            额外信息，如network, elapse, tag, statusCode等。可以为null
	 */
	void pageView(String url, List<NameValuePair> extras);

	/**
	 * 事件统计
	 * <p>
	 * 事件统计是移动App的常用统计方式之一，记录了用户的行为。
	 * 
	 * @param category
	 *            分类，按主要页面即业务分，如index代表主页
	 * @param action
	 *            动作，一般命名规则为category_actionName，如index_search
	 * @param label
	 *            可阅读的标签，如名字等。一般是离散的值，用于统计出现频率
	 * @param value
	 *            非离散值，用于统计平均值。如列表点击位置，消费价格等
	 * @param extras
	 *            额外信息，如request_id。可以为null
	 */
	void event(String category, String action, String label, int value,
			List<NameValuePair> extras);

	/**
	 * 强制上传一次
	 */
	void flush();
}
