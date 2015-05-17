package com.yamibo.main.yamibolib.configservice;

/**
 * 配置项更新Listener
 * <p>
 * 如需关注某个配置项的变化，需要创建Listner，并调用subscribe，不再关注时调用unsubscribe
 */
public interface ConfigChangeListener {

	/**
	 * 名字为 key 的配置项变化时被调用
	 *
	 * @param from 变化前的值，需要调用者强制转换为Integer或者String，变化前无该配置项时from为null
	 * @param to   变化后的值，需要调用者强制转换为Integer或者String，变化后删除该配置项时to为null
	 */
	void onConfigChange(String key, Object from, Object to);

}