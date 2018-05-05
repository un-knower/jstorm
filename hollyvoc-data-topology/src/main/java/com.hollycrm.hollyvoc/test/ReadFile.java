package com.hollycrm.hollyvoc.test;

import java.io.*;

/**
 * Created by qianxm on 2017/8/1.
 */
public class ReadFile {
    private static void read(String fileName) throws IOException{
//        File file = new File(fileName);
//        String path = "D:\\myWork\\data\\"; // 路径

            try(
                    BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
            ) {
                String line;
                while ((line = br.readLine()) != null) {
                        String[] values = line.split("_");
                        String rowkey = values[0];
                        System.out.println(" rowkey:" + rowkey );
                        System.out.println(" value: " + rowkey.substring(10,12));


                }
            } catch (Exception e) {
                System.out.println(" err: " +e);
            }


    }

    public static void main(String[] args) {
        String filename = "D:\\myWork\\data\\txt_tra";
//        try {
//            read(filename);
//        }catch (Exception e) {
//            e.printStackTrace();
//        }
        Long s = System.currentTimeMillis();
        System.out.println(" s : " + (System.currentTimeMillis()-s)/1000 + " s ");

    }
}
