package com.hollycrm.hollyvoc.qc.test;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by qianxm on 2017/9/25.
 */
public class CodeTest {
    public static void main(String[] args) {
//        for(int i=0;i<10;i++){
//            for(int j=0;j<5;j++){
//                System.out.println("j: " + j);
//                if(i==4 && j==2){
//                    break;
//                }
//            }
//            System.out.println("i:" + i);
//        }

        Map<String,String> map = new HashMap<>();
        map.put("1","1");
        map.put("2","1");
        map.put("3","1");
        map.put("4","1");
        map.forEach((k,v)->{
//            System.out.println( " k :"+ k );
            String a = "";
            for(int j=0;j<5;j++){
//                System.out.println("j: " + j);
                if(k.equals("3") && j==2){
                    a= "2";
                    break;
                }
            }
            if(a.equals("2")){
                return;
            }
            System.out.println("k " +k);
        });

    }
}
