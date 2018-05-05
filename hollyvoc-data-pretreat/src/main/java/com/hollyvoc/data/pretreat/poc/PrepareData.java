package com.hollyvoc.data.pretreat.poc;

import java.io.*;

/**
 * desc: 三种渠道数据准备.
 * date: 2018-05-04
 *
 * @author qianxm
 */
public class PrepareData {

    public static void main(String[] args) {
        String content = "您好，请问什么帮您？您好，不好意思这么晚还打扰你了。您如果今天白天。就是投诉那里联通公司，那个里还是？。他说帮我改一下套餐。就是他把它八五加入1857343。0735的卡里面。。我已经。这个我已经记录了他已经记录了。。但是我刚才。因为你我女儿的另外一张卡。你呃，就是说3个多月前就是2月28号。他说帮我。。嗯，搞个家庭宽带。。把我们加入人家的好嘛，里面1857340。5741。这个人我们认都不认识。他把我们加入人家的套餐。一个月扣，我们几百块钱话费。太多是我女儿。";
        try(
                // 微博
                BufferedWriter weibo = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File("D:\\myWork\\data\\poc\\weibo_origin"))));
                // 微信
                BufferedWriter weixin = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File("D:\\myWork\\data\\poc\\weixin_origin"))));
                // cbss
                BufferedWriter cbss = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File("D:\\myWork\\data\\poc\\cbss_origin"))));
        ) {
            StringBuilder wb = new StringBuilder();
            StringBuilder wx = new StringBuilder();
            StringBuilder cb = new StringBuilder();

//            int count =0;
//            for(int i=0;i<3000000; i++) {
//                count ++;
//                wb.append(i).append("#").append(content).append("\r\n");
//                weibo.write(wb.toString());
//                if((count+1) % 500 == 0) {
//                    weibo.flush();
//                }
//            }

            int wc=0;
            for(int i=4000000;i<7000000; i++) {
                wx.append(i).append("#").append(content).append("\r\n");
                wc++;
                weixin.write(wx.toString());
                if((wc+1) % 500 == 0) {
                    weixin.flush();
                }
            }

//            int cc =0;
//            for(int i=7000000;i<10000000; i++) {
//                cb.append(i).append("#").append(content).append("\r\n");
//                cbss.write(cb.toString());
//                if((cc+1) % 500 == 0) {
//                    cbss.flush();
//                }
//            }
            System.out.println("over~");
        } catch (Exception e) {
            System.out.println("error: " + e);
        }

    }
}
