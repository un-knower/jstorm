//package com.hollycrm;
//
//import com.sun.deploy.util.StringUtils;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.atomic.AtomicInteger;
//
///**
// * Created by qianxm on 2017/7/13.
// */
//public class App{
//
////    private List<String> resources;
//    private final int batchSize = 10;
//    private AtomicInteger ai = new AtomicInteger(1);
//    private ConcurrentHashMap<String, List<String>> resource;
//    private ConcurrentHashMap<String, Long> timeMap = new ConcurrentHashMap<>();
//
//    public App(){
////        resources = new Vector<>(batchSize);
//        resource = new ConcurrentHashMap<>();
//    }
//
//    private Thread[] threads = new Thread[]{
//        new Thread(new Handler(), "Pro-1"),
//        new Thread(new Handler(), "Pro-2"),
//        new Thread(new Handler(), "Pro-3"),
//    };
//    public void start(){
//        for (int i = 0; i < threads.length; i++) {
//            threads[i].start();
//        }
//        timer.start();
//    }
//    private class Handler implements Runnable{
//        @Override
//        public void run() {
//            while (true){
//                int i = ai.getAndIncrement();
//                String cn = Thread.currentThread().getName();
//                timeMap.put(cn, System.currentTimeMillis());
//                List<String> list = resource.get(cn);
//                if(list == null) {
//                    list = new ArrayList<>(batchSize);
//                    resource.put(cn, list);
//                }
//                list.add(i + "");
//                System.out.println(Thread.currentThread().getName() + " 数据生成 " + i );
//                if(i > 35 && i < 40){
//                    try {Thread.sleep(10000);}catch (Exception e){};
//                }
//                if(list.size() >= batchSize ) {
//                    System.out.println(Thread.currentThread().getName() + " 提交 " + list.size()
//                    + "   " + StringUtils.join(list, "-"));
//                    try {
//                        Thread.sleep(2000);
//                    }catch (Exception e){
//                        e.printStackTrace();
//                    }
//                    System.out.println(Thread.currentThread().getName() + " 清空 " + list.size());
//                    list.clear();
//                }
//            }
//        }
//    }
//
//    private Thread timer = new Thread(()->{
//        while (true) {
//            timeMap.forEach((k,v) -> {
//                if ( ((System.currentTimeMillis() - v) / 1000) > 5){
//                    List<String> list = resource.get(k);
//                    synchronized (list){
//                        // commit;
//                        System.out.println(k + " timer " + list.size() + "   " + StringUtils.join(list, "-"));
//                        try {
//                            Thread.sleep(10000);
//                        }catch (Exception e) {}
//                        System.out.println(k + " clear :" + list.size() + "   " + StringUtils.join(list, "-"));
//                        list.clear();
//
//                    }
//
//                }
//            });
////            try {
////                Thread.sleep(3000);
////            }catch (Exception e) {}
//        }
//    });
//    public static void main(String[] args) {
//
//        App app = new App();
//        app.start();
//
//
//    }
//}
