package com.hollyvoc.data.pretreat.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Created by alleyz on 2017/5/17.
 */
public class Config {
    private static String configFile = "/config.properties";
    private static long sleepMills = 5000L;
    private final Map<String, String> maps = new HashMap<String, String>();
    private Config(){
        final String filePath = Config.class.getResource(configFile).getPath();
        File file = new File(filePath);
        if(!file.exists()) {
            System.err.println("配置文件[" + configFile + "]不存在");
        }else {
            try {
                init(file);
                System.out.println(file.lastModified());
                lastModified = file.lastModified();
            } catch (Exception e) {
                System.err.println("配置文件[" + configFile + "]不存在");
            }

        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        File temp = new File(filePath);
                        if (!temp.exists()) {
                            System.err.println("配置文件[" + configFile + "]不存在");
                            continue;
                        }
                        if(temp.lastModified() != lastModified) {
                            synchronized (maps) {
                                maps.clear();
                                init(temp);
                                lastModified = temp.lastModified();
                            }
                        }
                        Thread.sleep(sleepMills);
                    }catch (IOException e) {
                        e.printStackTrace();
                        System.err.println("文件读取错误，继续检测配置更新!");
                    }catch (InterruptedException e) {
                        e.printStackTrace();
                        System.err.println("线程睡眠异常，继续检测配置更新!");
                    }
                }
            }
        }, "check-file-change")
                .start();

    }
    private final static Config config = new Config();
    private volatile long lastModified = -1L;
    private void init(File file) throws IOException{
        System.out.println("更新配置..." + lastModified + " >> " + file.lastModified());
        InputStream is = null;
        try {
            Properties props = new Properties();
            is = new FileInputStream(file);
            props.load(is);
            Enumeration enums = props.propertyNames();
            while (enums.hasMoreElements()) {
                String key = (String) enums.nextElement();
                String val = props.getProperty(key);
                if (val != null)
                    maps.put(key, val);
            }
        }finally {
            try {
                if (is != null)
                    is.close();
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 获取val
     * @param key key
     * @return val
     */
    public static String getVal(String key) {
        return config.maps.get(key);
    }

    /**
     * 获取val，如果val为null则返回def
     * @param key key
     * @param def def
     * @return val
     */
    public static String getVal(String key, String def) {
        String val = config.maps.get(key);
        return val == null ? def : val;
    }
}