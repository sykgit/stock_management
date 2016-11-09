/**
 * 
 */
package com.syk.sm.utility;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author skuppuraju
 *
 */
public class SM_CACHE {
	private static ConcurrentHashMap<String, Object> cacheMap = new ConcurrentHashMap<String, Object>();

	public static ConcurrentHashMap<String, Object> getCacheMap() {
		return cacheMap;
	}
}
