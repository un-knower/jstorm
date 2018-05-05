package com.hollycrm.util.config;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Created by alleyz on 2017/5/5.
 *
 */
public class ConfigUtils {
    private static Logger logger = LoggerFactory.getLogger(ConfigUtils.class);
    private ConfigUtils(){}
    private static String fileName = "/application.properties";
    private static Properties prop = null;

    /**
     * 获取配置信息
     * @param configFiles 配置文件名称
     * @return 返回properties
     */
    public static Properties getProp(String configFiles){
        Properties prop = new Properties();
        logger.info("configFiles"+configFiles);
        System.out.println("configFiles"+configFiles);
        try {
            prop.load(ConfigUtils.class.getResourceAsStream(configFiles));
        }catch (IOException e) {
            logger.error("Can`t find config file [" +  configFiles + "]", e);
        }
        return prop;
    }

    public static Map<String, Object> prop2Map(Properties prop) {
        Map<String, Object> maps = new HashMap<>();
        prop.forEach((k, v) -> {
            maps.put((String) k, v);
        });
        return maps;
    }
    public static Map<String, Object> prop2Map(String configFiles) {
        Properties prop = getProp(configFiles);
        Map<String, Object> maps = new HashMap<>();
        prop.forEach((k, v) -> {
            maps.put((String) k, v);
        });
        return maps;
    }

    /**
     * 获取配置字符串值
     * @param key key
     * @param defaults 默认值
     * @return 字符串
     */
    public static String getStrVal(String key, String defaults) {
        if(prop == null) prop = getProp(fileName);
        if(prop == null) return defaults;
        return prop.getProperty(key, defaults);
    }

    /**
     * 获取配置字符串值
     * @param key key
     * @return 值 如果没有则为null
     */
    public static String getStrVal(String key) {
        return getStrVal(key, null);
    }
    /**
     * 获取配置int值
     * @param key key
     * @param defaults 默认值
     * @return int
     */
    public static Integer getIntVal(String key, Integer defaults) {
        String val = getStrVal(key);
        if(val == null || "".equals(val)) return defaults;
        return Integer.parseInt(val);
    }

    public static Integer getIntVal(Map<String, Object> conf, String key, Integer defaults) {
        String val = (String)conf.get(key);
        if(val == null || "".equals(val)) return defaults;
        return Integer.parseInt(val);
    }

    public static Long getLongVal(String key, Long defaults) {
        String val = getStrVal(key);
        if(val == null || "".equals(val)) return defaults;
        return Long.parseLong(val);
    }

    public static String getExpress(String file) throws IOException{
        StringBuilder builder = new StringBuilder();
        try(BufferedReader br = new BufferedReader(new InputStreamReader(ConfigUtils.class.getResourceAsStream(file)))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.indexOf("#") == 0) continue;
                if (line.length() > 0)
                    builder.append(line);
            }
        }
        return builder.toString();
    }
}
