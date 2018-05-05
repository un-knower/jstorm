package com.hollycrm.hollyvoc.test;

import com.hollyvoc.helper.jdbc.JdbcHelper;

import java.util.ArrayList;
import java.util.List;

import static com.hollycrm.hollyvoc.constant.ConstUtils.javaId;

/**
 * Created by qianxm on 2017/8/12.
 */
public class SaveData {

    public static void main(String[] args) {

        String qcItemId = "test1234";
        String itemName = "测试";
        String rowkey = "001";
        String type = "1";
        String content = "";
        String matchword = "";
        String prov = "";
        String qcCustBrand = "";
        String qcCustLevel = "";
        String satisfication = "";
        String businessType = "";
        String direction = "";
        String recordLength = "";
        String silenceLength = "";
        String userCode = "";
        String acceptTime = "";
        String calle = "";
        String callee = "";

        String id =javaId(); // 自动生成id
        System.out.println("id"+id);

        List<String[]> queue = new ArrayList<>();
        // CALLER 主叫 CALLEE 被叫
        queue.add(new String[]{javaId(),qcItemId,itemName,rowkey,type,content,matchword,prov,qcCustBrand
        ,satisfication,businessType,silenceLength,userCode,acceptTime,calle,callee});

        queue.add(new String[]{javaId(),qcItemId,itemName,rowkey,type,content,matchword,prov,qcCustBrand
                ,satisfication,businessType,silenceLength,userCode,acceptTime,calle,callee});
        queue.add(new String[]{javaId(),qcItemId,itemName,rowkey,type,content,matchword,prov,qcCustBrand
                ,satisfication,businessType,silenceLength,userCode,acceptTime,calle,callee});
        queue.add(new String[]{javaId(),qcItemId,itemName,rowkey,type,content,matchword,prov,qcCustBrand
                ,satisfication,businessType,silenceLength,userCode,acceptTime,calle,callee});
        queue.add(new String[]{javaId(),qcItemId,itemName,rowkey,type,content,matchword,prov,qcCustBrand
                ,satisfication,businessType,silenceLength,userCode,acceptTime,calle,callee});
        queue.add(new String[]{javaId(),qcItemId,itemName,rowkey,type,content,matchword,prov,qcCustBrand
                ,satisfication,businessType,silenceLength,userCode,acceptTime,calle,callee});
        System.out.println(queue.size());
        System.out.println(javaId());
        System.out.println(javaId());
        try {
            String sql = "insert into tbl_voc_qc_list(row_id,qc_item_id," +
                    "qc_item_name,custcontinfo_id,qc_item_type,qc_item_content," +
                    "txt_content,area_code,cust_band,satisfication,business_type," +
                    "silence_length,user_code,start_time,caller,callee)" +
                    " values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
            System.out.println(" ");
            if (queue.size() > 5) {
                // 每500条提交一次
                List<String[]> items = new ArrayList<>();
                items.addAll(queue);
                queue.clear();
                boolean reslut = JdbcHelper.getInstance().batchInsert(sql, items);
                System.out.println(reslut);
                if(!reslut) {
                    System.out.println(" save item err! ");
                    // todo 保存失败需要把数据发送给kafka
                } else {
                    System.out.println("保存成功！");
                }
            }
        } catch (Exception e) {
            System.out.println(" save item err! "+ e);
            // todo 保存失败需要把数据发送给kafka
        }
    }
}
