package com.hollycrm.producer;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

import static com.hollycrm.hollyvoc.constant.Constant.DELIMITER_PIPE;

/**
 * 把通过xml解析的CSV文件和随机生成的文件匹配成一个文件,并生成原始文件。
 * Created by qianxm on 2018/3/7.
 */
public class XMlFIle {
    public static void main(String[] args) throws Exception {
//            String path = "G:\\data\\kafka-reader";
            Map<String, String> map = new HashMap<>();
            String path = "D:\\myWork\\data\\11\\org\\20170530__original.txt_tra"; // 路径
            path = "D:\\myWork\\data\\11\\pre-org\\20170119__original_tra"; // 维度文件
            File file = new File(path);

            try (
                    // 维度数据.
                    BufferedReader br1 = new BufferedReader(new InputStreamReader(new FileInputStream(file), "utf-8"));
                    // 结果文件
                    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File("D:\\myWork\\data\\11\\org\\20180315__origin"))));
                    // xml信息数据
                    BufferedReader br2 = new BufferedReader(new InputStreamReader(new FileInputStream(new File("D:\\myWork\\data\\11\\99\\20180315.csv")), "utf-8"));
                    // 原始文件 只有对话的事件
                    BufferedWriter origin = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File("D:\\myWork\\data\\11\\org\\20180315__original"))));


            ) {
                String line;
                String br1Line;
                int i = 0;
                while((br1Line=br1.readLine()) != null) {
                    map.put(String.valueOf(i), br1Line);
                    i++;
                }

                int a = 0;

                while ((line = br2.readLine()) != null) {
                    // 结构话数据
                    String str = map.get(String.valueOf(a));
                    String [] arr = str.split("#");
                    String hour = arr[1].split("\\|")[2];

                    String[] xml = line.split("\\@");
//                    文件名称0,对话内容
// 1,坐席文本2,用户文本3,通话总时长(毫秒)4,静音时长(毫秒)5
                    //1对话内容,2坐席文本,3用户文本,4通话总时长(毫秒),5静音时长(毫秒)
                    // todo 录音文件名称改成V3
                    StringBuilder builder = new StringBuilder();
                    builder.append(arr[0].replace("recn0", (xml[0].substring(0,xml[0].indexOf(".")))+".V3"))
                            .append(DELIMITER_PIPE).append(xml[5])
                            .append(DELIMITER_PIPE).append(xml[4])
                            .append(DELIMITER_PIPE).append(hour)
                            .append(DELIMITER_PIPE).append(xml[3])
                            .append(DELIMITER_PIPE).append(xml[2])
                            .append(DELIMITER_PIPE).append(xml[1]);
                    // 替换录音时长和静音时长
                    // 在hour后面拼接
//                    builder.append(DELIMITER_PIPE)
//                                .append(userTxt).append(DELIMITER_PIPE)
//                                .append(agentTxt).append(DELIMITER_PIPE)
//                                .append(txt);
                    bw.write(builder.toString() + "\r\n");
                    StringBuffer ori = new StringBuffer();
                    ori.append(arr[0].replace("recn", xml[0].toUpperCase())).append(DELIMITER_PIPE).append(xml[5]).append(DELIMITER_PIPE).append(xml[4])
                            .append(DELIMITER_PIPE).append(hour)
                            .append("#").append(xml[1].replaceAll("n0#","").replaceAll("n1#",""));
                    origin.write(ori.toString()+"\r\n");
                    a++;
                    if((i+1) % 500 == 0) {
                        bw.flush();
                        origin.flush();
                    }
                }


            }


    }
}
