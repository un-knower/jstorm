package com.hollycrm.hollyvoc.test;

/**
 * Created by qianxm on 2018/3/7.
 */
public class DayTest {

    private static int count=0;

    /**
     * 计数器自增.
     */
    private static synchronized void countIncrement(){
        count++;
        if(count > 100) count = 0;
    }

    public static void main(String[] args) {
        countIncrement();
        for (int i=0 ;i<10; i++) {
//            System.out.println(count);
//            System.out.println(count%28);
            System.out.println( 100 * Math.random());
            System.out.println( (int)(100 * Math.random()%28));
        }
    }
}
