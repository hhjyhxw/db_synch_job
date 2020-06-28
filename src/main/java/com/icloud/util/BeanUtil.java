package com.icloud.util;

import java.lang.reflect.Method;
import java.util.Map;

import org.apache.log4j.Logger;

public class BeanUtil {
    private static Logger logger = Logger.getLogger(BeanUtil.class);

    public static Object getValue(Object obj, String fieldName) {
        try {
            String firstLetter = fieldName.substring(0, 1).toUpperCase();
            String getter = "get" + firstLetter + fieldName.substring(1);
            Method method = obj.getClass().getMethod(getter, new Class[] {});
            Object value = method.invoke(obj, new Object[] {});
            return value;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    /**
     * 将 NICK_NAME 方式的名称转换成nickName方式
     * 
     * @param name
     * @return
     */
    public static String convertFieldName(String name) {
        if (name == null || "".equals(name.trim())) {
            return null;
        }
        String[] arr = name.toLowerCase().split("_");
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < arr.length; i++) {
            String str = arr[i];
            if (str != null && !"".equals(str.trim())) {
                buf.append(str.substring(0, 1).toUpperCase());
                buf.append(str.substring(1));
            }
        }
        return buf.length() <= 0 ? "" : buf.substring(0, 1).toLowerCase() + buf.substring(1);
    }
    
    /**
     * 根据key从map中获取值，并转换成String类型
     *
     * @param map
     * @param key
     * @return
     */
    public static String getStringValue(Map<String, Object> map, String key) {
        Object obj = map.get(key);
        return obj == null ? null : obj.toString();
    }
    
    /**
     * 根据key从map中获取值，并转换成Integer类型
     *
     * @param map
     * @param key
     * @return
     */
    public static Integer getIntegerValue(Map<String, Object> map, String key) {
        Object obj = map.get(key);
        if (obj != null) {
            return Integer.valueOf(obj.toString());
        } else {
            return null;
        }
    }

    public static void main(String[] args) {
        System.out.println(convertFieldName("L_nialeiCK_NAME"));
        System.out.println(convertFieldName("NICK_NAME"));
        System.out.println(convertFieldName("NICK_NAME"));
    }
}
