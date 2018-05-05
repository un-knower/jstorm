//package com.hollycrm;
//
//import com.sun.deploy.util.StringUtils;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.concurrent.atomic.AtomicInteger;
//import java.util.concurrent.locks.ReentrantReadWriteLock;
//
///**
// * Created by qianxm on 2017/7/13.
// */
//public class App1 {
//
//    private final List<String> list = new ArrayList<>();
//    private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
//    private AtomicInteger ai = new AtomicInteger(1);
//    private class Handler implements Runnable {
//        @Override
//        public void run() {
//            while (true) {
//                ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();
//                if(writeLock.tryLock()) {
//                    int i = ai.getAndIncrement();
//                    System.out.println("bbb " + i + "  " + list.size());
//                    list.add(i + "");
//                    writeLock.unlock();
//                    try {
////                        Thread.sleep(500);
//                    } catch (Exception e) {
//                    }
//                }
//            }
//        }
//    }
//
//    private Thread b = new Thread(new Handler(), "b");
//    private Thread c = new Thread(()->{
//        while (true) {
//            String k = Thread.currentThread().getName();
//            ReentrantReadWriteLock.ReadLock readLock = lock.readLock();
//            while (readLock.tryLock()){
////                readLock.lock();
////                System.out.println("read lock ... ");
//                if (list.size() > 5) {
//                    System.out.println(k+" > 5 ");
//                    readLock.unlock();
//                    ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();
//                    while (writeLock.tryLock()) {
//                        System.out.println(k +"--- lock --");
//                        System.out.println(StringUtils.join(list, "--"));
//                        try {
//                            Thread.sleep(2000);
//                        } catch (Exception e) {
//                        }
//                        list.clear();
//                        writeLock.unlock();
//                        break;
//                    }
//
//                }else {
//                    readLock.unlock();
//                }
//
//            }
//
//        }
//    }, "c");
//
//    public void start(){
//        b.start();
//        c.start();
//    }
//
//    public static void main(String[] args) {
//        App1 app1 = new App1();
//        app1.start();
//    }
//}
