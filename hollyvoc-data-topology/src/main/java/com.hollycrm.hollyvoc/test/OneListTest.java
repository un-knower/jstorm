//package com.hollycrm.hollyvoc.test;
//
//import com.sun.deploy.util.StringUtils;
//
//import java.util.List;
//import java.util.Vector;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.atomic.AtomicInteger;
//import java.util.concurrent.locks.ReentrantReadWriteLock;
//
///**
// * Created by qianxm on 2017/7/14.
// */
//public class OneListTest {
//    private final int batchSize = 10;
//    private AtomicInteger ai = new AtomicInteger(1);
//    private List<String> resources;
//    private List<Integer> backups;
//    private ConcurrentHashMap<String, Long> timeMap = new ConcurrentHashMap<>();
//    private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
//
//
//    public OneListTest(){
//        resources = new Vector<>(batchSize);
//        backups = new Vector<>(batchSize);
//    }
//
//    private Thread[] threads = new Thread[]{
//            new Thread(new Handler(), "Pro-1"),
//            new Thread(new Handler(), "Pro-2"),
//            new Thread(new Handler(), "Pro-3"),
//    };
//    private void start(){
//        for (int i = 0; i < threads.length; i++) {
//            threads[i].start();
//        }
//        timer.start();
//    }
//    private class Handler implements Runnable{
//        @Override
//        public void run() {
//            while (true){
//
//
//                ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();
//
//                while(writeLock.tryLock()) {
//                    int i = ai.getAndIncrement();
//                    String cn = Thread.currentThread().getName();
//                    timeMap.put(cn, System.currentTimeMillis());
//                    resources.add(i + "");
//                    backups.add(i);
//                    System.out.println(Thread.currentThread().getName() + " 数据生成 " + i);
//                    writeLock.unlock();
//                    if (i > 35 && i < 40) {
//                        try {
//                            Thread.sleep(10000);
//                        } catch (Exception e) {
//                        }
//                    }
//                    if (resources.size() >= batchSize) {
//                        System.out.println(Thread.currentThread().getName() + " 提交 " + resources.size()
//                                + "   " + StringUtils.join(resources, "-"));
//                        System.out.println(Thread.currentThread().getName() + " 备份 " + backups.size()
//                                + "   " + backups.toString());
//                        try {
//                            Thread.sleep(1000);
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                        System.out.println(Thread.currentThread().getName() + " 清空 " + resources.size());
//                        resources.clear();
//                        System.out.println(Thread.currentThread().getName() + " 清空备份 " + backups.size());
//                        backups.clear();
//                    }
////                    writeLock.unlock();
//                }
//            }
//        }
//    }
//
//    private Thread timer = new Thread(()->{
//        while (true) {
//            timeMap.forEach((k,v) -> {
//                if ( ((System.currentTimeMillis() - v) / 1000) > 5){
//
//                        ReentrantReadWriteLock.ReadLock readLock = lock.readLock();
//                        while (readLock.tryLock()) {
////                            System.out.println(k + resources.size());
//                            if (resources.size() > 0) { // 有数据需要提交
//                                readLock.unlock();
//                                ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();
//                                while (writeLock.tryLock()) {
//                                    System.out.println(k + "lock-- resources.size:" + resources.size());
//                                    if(resources.size() > 0){
//                                        System.out.println(k + "lock-- resources:" + resources.toString());
//
//                                        System.out.println(k + " 超时提交 resources" + resources.toString());
//                                        System.out.println(k + " emit backups" + backups.toString());
//                                        try {
//                                            Thread.sleep(5000); // 提交的这一分钟内，如果有新的数据写入？
//                                        } catch (Exception e) { }
//
//                                        System.out.println(k + " 超时 清空resources " + resources.size() + " " + resources.toString());
//                                        resources.clear();
//                                        System.out.println(k + "超时 清空备份backups " + backups.size() + " " + backups.toString());
//                                        backups.clear();
//                                        writeLock.unlock();
//                                    } else {
//                                        writeLock.unlock();
//                                    }
//                                }
//                            }else {
//                                readLock.unlock();
//                            }
//                        }
//                }
//            });
//        }
//    });
//    public static void main(String[] args) {
//
//        OneListTest one = new OneListTest();
//        one.start();
//
//
//    }
//}
