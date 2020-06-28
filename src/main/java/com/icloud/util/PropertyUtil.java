package com.icloud.util;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

public class PropertyUtil {
    private static Logger logger = Logger.getLogger(PropertyUtil.class);
    private static Properties properties = null;

    public static String getValue(String key) {
        return getProperties().getProperty(key);
    }

    public static Properties getProperties() {
        if (properties == null) {
            try {
                properties = new Properties();
                String filePath = System.getProperty("user.dir") + "/config/jdbc.properties";
                if (new File(filePath).exists()) {
                    logger.info("加载 " + filePath + " 文件！");
                    properties.load(new FileInputStream(filePath));
                } else {
                    logger.info("加载 jar 包 jdbc.properties 文件！");
                    properties.load(PropertyUtil.class.getClassLoader().getResourceAsStream("jdbc.properties"));
                }
                logger.debug("属性文件加载完成！");
            } catch (Exception e) {
                logger.error("加载 jdbc.properties 属性文件出错！", e);
                System.exit(-1);
            }
        }
        return properties;
    }
}
