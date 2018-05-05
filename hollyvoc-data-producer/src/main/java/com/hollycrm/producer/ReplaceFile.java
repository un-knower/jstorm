package com.hollycrm.producer;

import java.io.*;

/**
 * Created by qianxm on 2018/3/7.
 */
public class ReplaceFile {
    public static void main(String[] args) throws Exception{
        int j = 0;
        try (
//                BufferedReader br1 = new BufferedReader(new InputStreamReader(new FileInputStream(new File("D:\\myWork\\data\\11\\org\\20170530_tra_result")), "utf-8"));
//                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File("D:\\myWork\\data\\11\\org\\20170530_tra_results"))));
                BufferedReader br1 = new BufferedReader(new InputStreamReader(new FileInputStream(new File("D:\\myWork\\data\\11\\pre-org\\20170119__original_tra")), "utf-8"));
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File("D:\\myWork\\data\\11\\pre-org\\20170119__original_tra_result"))));
        ) {
            String line;
            String br1Line;
            while ((br1Line = br1.readLine()) != null) {
                String[] str = br1Line.split("\\|");
                // str[7] 受理时间 str[29] 小时 str[9] 月
                StringBuilder builder = new StringBuilder();

                for (int i=0; i< str.length; i++) {
                    if (i==7) {
                       builder.append(str[29]).append(str[7].substring(10,str[7].length())).append("|");
                    } else if(i==9){
                        builder.append(str[29].substring(0,6)).append("|");
                    }else {
                        builder.append(str[i]).append("|");
                    }
                }

                String sbd = builder.toString().substring(0, (builder.toString().length()-1));
                bw.write(sbd + "\r\n");
                if((j+1) % 500 == 0)
                    bw.flush();
            }
        }
    }
}
