package com.hollycrm.hollyvoc.test;

import java.util.List;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
* Created by qianxm on 2017/7/14.
*/
public class AppTests {
private final int batchSize = 10;
private AtomicInteger ai = new AtomicInteger(1);
//    private ConcurrentHashMap<String, List<String>> resource;
private ConcurrentHashMap<String,ConcurrentHashMap<String,Object>> res;
private ConcurrentHashMap<String, Long> timeMap = new ConcurrentHashMap<>();
private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();


public AppTests(){
//        resources = new Vector<>(batchSize);
//        resource = new ConcurrentHashMap<>();
    res = new ConcurrentHashMap<>();
}

private Thread[] threads = new Thread[]{
        new Thread(new Handler(), "Pro-1"),
        new Thread(new Handler(), "Pro-2"),
        new Thread(new Handler(), "Pro-3"),
};
public void start(){
    for (int i = 0; i < threads.length; i++) {
        threads[i].start();
    }
    timer.start();
}
private class Handler implements Runnable{
    @Override
public void run() {
    while (true){

            int i = ai.getAndIncrement();
            String cn = Thread.currentThread().getName();
            timeMap.put(cn, System.currentTimeMillis());
            ConcurrentHashMap map = res.get(cn);
            if (map == null) {
                map = new ConcurrentHashMap();
                res.put(cn, map);
            }
            List<String> ls1 = (List<String>) map.get("put");
            List<Integer> ls2 = (List<Integer>) map.get("data");
            if (ls1 == null) {
                ls1 = new Vector<>(batchSize);
                map.put("put", ls1);
            }
            if (ls2 == null) {
                ls2 = new Vector<>(batchSize);
                map.put("data", ls2);
            }

            res.put(cn, map);
            ls1.add(i + "");
            ls2.add(i);

//                 list.add(i + "");
            System.out.println(Thread.currentThread().getName() + " 数据生成 " + i );
            if(i > 35 && i < 40 ){
                try {Thread.sleep(10000);}catch (Exception e){};
            }

//            if(i > 100 && i < 105){
//                try {Thread.sleep(20000);}catch (Exception e){};
//            }
            if(ls1.size()> batchSize) {
                System.out.println(Thread.currentThread().getName() + " 提交 " + ls1.size()
                        + "   " + ls1.toString());
                System.out.println(Thread.currentThread().getName() + " 备份 " + ls2.size()
                        + "   " + ls2.toString());
                try {
                    Thread.sleep(1000);
                }catch (Exception e){
                    e.printStackTrace();
                }
                System.out.println(Thread.currentThread().getName() + " 清空ls1 " + ls1.size());
                ls1.clear();
                System.out.println(Thread.currentThread().getName() + " 清空ls2 " + ls2.size());
                ls2.clear();
            }
        }
    }
//        }
}

private Thread timer = new Thread(()->{
    while (true) {
        timeMap.forEach((k,v) -> {
            if ( ((System.currentTimeMillis() - v) / 1000) > 5){
                // todo 所不起作用。 因为是同一个线程的数据。
                ReentrantReadWriteLock.ReadLock readLock = lock.readLock();
                ConcurrentHashMap map = res.get(k); // 应该对res上锁
                List<String> ls1 =(List<String>) map.get("put");
                List<Integer> ls2 = (List<Integer>) map.get("data");
                while (readLock.tryLock()) {
//                readLock.lock();
                    System.out.println("read");
                    if (ls1.size() > 0) { // 有数据需要提交
                        readLock.unlock();
                        ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();
                        while (writeLock.tryLock()) {
                            System.out.println("writeLock");
//                            res.put("data",new ArrayList<String>());
                            if(ls1.size()>0){
                                System.out.println("lock-- ls1:" + ls1.toString());
                                ls1.add("write");
                                System.out.println("lock-- ls1:" + ls1.toString());
                                System.out.println(k + " 超时提交 ls1" + ls1.toString());
                                System.out.println(k + " emit ls2" + ls2.toString());
                                try {
                                    Thread.sleep(10000); // 提交的这一分钟内，如果有新的数据写入？
                                } catch (Exception e) {
                                }
                                System.out.println(k + " 清空提交ls1 " + ls1.size() + " " + ls1.toString());
                                ls1.clear();
                                System.out.println(k + " 清空备份ls2 " + ls2.size() + " " + ls2.toString());
                                ls2.clear();
                            } else {
                                writeLock.unlock();
                            }
                        }
                    } else {
                        readLock.unlock();
                    }

                }
            }
        });

    }
});
public static void main(String[] args) {

    AppTests app = new AppTests();
    app.start();


}
}
