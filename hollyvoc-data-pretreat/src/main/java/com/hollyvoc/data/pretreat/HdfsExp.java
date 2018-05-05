package com.hollyvoc.data.pretreat;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;

import java.io.ByteArrayInputStream;

/**
 * Created by alleyz on 2017/5/18.
 */
public class HdfsExp {
    public static void main(String[] args) {
        try {
            Configuration conf = new Configuration();
            conf.setBoolean("dfs.support.append", true);
            System.setProperty("HADOOP_USER_NAME", "jstorm");
            FileSystem fs = FileSystem.get(conf);

            Path path = new Path("/user/alleyz/1");
            ByteArrayInputStream in = new ByteArrayInputStream("\r\n我的名搜啊案件李开复航经历过".getBytes());

            FSDataOutputStream os = fs.append(path);
            IOUtils.copyBytes(in, os, 4096, false);
            os.close();
            fs.close();
            System.out.println("end");
            fs = FileSystem.get(conf);
            FSDataInputStream is = fs.open(path);
            IOUtils.copyBytes(is, System.out, 4096, false);
//            System.out.println(is.readUTF());
            is.close();
        }catch (Exception e) {
            e.printStackTrace();
        }



    }
}
