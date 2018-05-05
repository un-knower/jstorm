package com.hollycrm.hollyvoc.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by qianxm on 2017/9/8.
 */
public class CreateOrDelCollection {
    private static void createOrDelCollection(List<Integer> prov, String url) throws IOException {


        for(Integer p:prov) {
            String colUrl = url + p;
            URL getUrl = new URL(colUrl);
            HttpURLConnection connection = (HttpURLConnection) getUrl.openConnection();
            // 连接
            connection.connect();  // todo 考虑失败情况，重新访问链接
            // 取得输入流，并使用Reader读取请求结果
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
            String lines;
            while ((lines = reader.readLine()) != null) {
                System.out.println(" flush url:" + colUrl + " \n result: " + lines);  //{"status":true,msg:"success"}
            }
            reader.close();
            // 断开连接
            connection.disconnect();
        }
    }

    private static void delCollection(List<Integer> prov){

    }
    public static void main(String[] args) {
        List<Integer> prov = new ArrayList<>(30);
        for (int i = 11; i<40; i++){
            prov.add(i);
        }
        prov.add(74);
        System.out.println(prov.toString());

        String createUrl= "http://10.8.177.23:8983/solr/admin/collections?action=CREATE&collection.configName=nostored-ansj-hour&maxShardsPerNode=6&numShards=3&replicationFactor=3&router.name=compositeId&routerName=compositeId&wt=json&name=holly-";
        String deleteurl = "http://hd-23:8983/solr/admin/collections?action=DELETE&name=holly-";

        try {
//            createOrDelCollection(prov, deleteurl);
            createOrDelCollection(prov, createUrl);

        } catch (Exception e) {
            System.out.println(" error" + e);
        }
    }
}
