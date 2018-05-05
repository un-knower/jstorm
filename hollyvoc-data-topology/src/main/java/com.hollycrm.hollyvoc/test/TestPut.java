package com.hollycrm.hollyvoc.test;

import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;

import java.util.HashMap;
import java.util.Map;

import static com.hollycrm.hollyvoc.constant.Constant.*;

/**
 * Created by qianxm on 2017/7/17.
 */
public class TestPut {
    private static byte[] infoFamily = Bytes.toBytes(CUST_INFO_H_FAMILY), // info 列族
            txtFamily = Bytes.toBytes(CUST_TXT_H_FAMILY), // txt列族
            infoQua = Bytes.toBytes(CUST_INFO_H_QUA), // info 列名
            userTxtQua = Bytes.toBytes(CUST_USER_TXT_H_QUE), // txt 用户通话列名
            agentTxtQua = Bytes.toBytes(CUST_AGENT_TXT_H_QUA), // txt 坐席通话内容
            allTxtQua = Bytes.toBytes(CUST_ALL_TXT_H_QUA); // txt 所有通话内容

    private static Put transfer2Put(String rowKey, String info, String allTxt, String agentTxt, String userTxt) {
        Put put = new Put(Bytes.toBytes(rowKey));
        put.addColumn(infoFamily, infoQua, Bytes.toBytes(info));
        put.addColumn(txtFamily, allTxtQua, Bytes.toBytes(allTxt));
        put.addColumn(txtFamily, agentTxtQua, Bytes.toBytes(agentTxt));
        put.addColumn(txtFamily, userTxtQua, Bytes.toBytes(userTxt));
        return put;
    }
    public static void main(String[] args) {
//        String rowKey = "123456";
//        String basicInfo = "basicinfo";
//        String agentTxt = "agentTxt";
//        String userTxt = "userTxt";
//        String allTxt = "allTxt";
//        // 保存到hbase 数据
//        Put put = transfer2Put(rowKey, basicInfo, allTxt, agentTxt, userTxt);
//        System.out.println(StringUtils.join(put.get(infoFamily, infoQua),"_"));
//        System.out.println(Bytes.toString(put.get(infoFamily, infoQua).get(0).getValue()));
//        System.out.println(Bytes.toString(cloneValue(put.get(infoFamily, infoQua).get(0))));
//        System.out.println(Bytes.toString(put.get(infoFamily, infoQua).get(0).getValueArray()));
//        System.out.println(Bytes.toString(put.get(infoFamily, infoQua).get(0).getQualifierArray()));
//
//        String i = "11,17,18,36";
//        String a = "11";
//        System.out.println(i.contains(a));

//        String s = "003030710218223690881";
//        System.out.println(s.substring(10,12));
//
//        Map<String,List<String>> map = new HashMap<>();
//        List list = map.get(1);
//        System.out.println(list==null);
//        System.out.println(list.isEmpty());
        Map<Integer,String> map  = new HashMap<>(10);
        map.put(0,"0");
        map.put(1,"2");
        map.forEach((k,v) -> {
            map.put(0,"1");
            boolean t = true;
            System.out.println(k);
            while (t) {
                if(k==1){
                    System.out.println("while" + 1);
                } else {
                    System.out.println("while" + 2);

                }

//                break;

            }
        });
        System.out.println(map.get(0));
    }
}
