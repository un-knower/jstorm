package com.hollyvoc.data.pretreat.util;


import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.Arrays;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * gzip工具类
 */
public class GzipUtils {
    private static Logger log=Logger.getLogger(GzipUtils.class);

    public static String doCompressFile(String inFileName) throws Exception{
        GZIPOutputStream out = null;
        FileInputStream in = null;
        try {
            log.debug("Creating the GZIP output stream.");
            String outFileName = inFileName + ".gz";
//            GZIPOutputStream out = null;
            try {
                out = new GZIPOutputStream(new FileOutputStream(outFileName));
            } catch(FileNotFoundException e) {
                throw new Exception("File not found. " + outFileName,e);
            }
            log.debug("Opening the input file.");
//            FileInputStream in = null;
            try {
                in = new FileInputStream(inFileName);
            } catch (FileNotFoundException e) {
                throw new Exception("File not found. " + inFileName,e);
            }

            log.debug("Transfering bytes from input file to GZIP Format.");
            byte[] buf = new byte[1024];
            int len;
            while((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();

            log.debug("Completing the GZIP file");
            out.finish();
            out.close();
            return outFileName;
        } catch (IOException e) {
            throw  new Exception(e);
        }finally {
            try{
                log.debug("Closing the file and stream");
                in.close();
                out.close();
            }catch (IOException e){

            }
        }

    }

    /**
     * 解压文件
     * @param inFileName Name of the file to be uncompressed
     */
    public static String  doUncompressFile(String inFileName)throws Exception{
        GZIPInputStream in = null;
        FileOutputStream out = null;
        try {
            if (!getExtension(inFileName).equalsIgnoreCase("gz")) {
                throw new Exception("File name must have extension of \".gz\"");
            }
            log.debug("Opening the compressed file.");
            try {
                in = new GZIPInputStream(new FileInputStream(inFileName));
            } catch(FileNotFoundException e) {
                throw new Exception("File not found. " + inFileName,e);
            }

            log.debug("Open the output file.");
            String outFileName = getFileName(inFileName);

            try {
                out = new FileOutputStream(outFileName);
            } catch (FileNotFoundException e) {
                throw new Exception("Could not write to file. " + outFileName,e);
            }

            log.debug("Transfering bytes from compressed file to the output file.");
            byte[] buf = new byte[1024];
            int len;
            while((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
//            byte[] bytes=doUncompressStream(in);
//            out.write(buf);
            out.flush();
            return outFileName;
        } catch (IOException e) {
            throw new Exception("",e);
        }finally {
            try{
                log.debug("Closing the file and stream");
                // 不判断会出现异常
                if(in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();

                }
            }catch (IOException e){
                log.debug("Closing the file and stream",e);
            }
        }

    }

    /**
     * 读取为数组
     * @param input
     * @return
     * @throws Exception
     */
    public static byte[] doUncompressStream(InputStream input)throws Exception{
        InputStream is=null;
        byte[] result=new byte[1024];
        try {
            byte[] inputArray=new byte[2048];
            GZIPInputStream gzip = new GZIPInputStream(input);
            int len;
            while((len=gzip.read(inputArray))>0){
                result=concat(result,inputArray,len);
                inputArray=new byte[2048];
            }
            return result;
        }catch (IOException e){
            throw new Exception("",e);
        }
    }

    /**
     * 读取为流
     * @param is
     * @return
     * @throws Exception
     */
    public static InputStream doUncompress(InputStream is)throws Exception{
        return new ByteArrayInputStream(doUncompressStream(is));
    }

    /**
     * 数组拷贝
     * @param first
     * @param second
     * @return
     */
    public static byte[] concat(byte[] first,byte[] second,int len) {
        int desPos=0;
        if(first[0]!=0)
            desPos=first.length;
        byte[] result = Arrays.copyOf(first, desPos + len);
        System.arraycopy(second, 0, result, desPos, len);
        return result;
    }
    /**
     * Used to extract and return the extension of a given file.
     * @param f Incoming file to get the extension of
     * @return <code>String</code> representing the extension of the incoming
     *         file.
     */
    public static String getExtension(String f) {
        String ext = "";
        int i = f.lastIndexOf('.');

        if (i > 0 &&  i < f.length() - 1) {
            ext = f.substring(i+1);
        }
        return ext;
    }

    /**
     * Used to extract the filename without its extension.
     * @param f Incoming file to get the filename
     * @return <code>String</code> representing the filename without its
     *         extension.
     */
    public static String getFileName(String f) {
        String fname = "";
        int i = f.lastIndexOf('.');

        if (i > 0 &&  i < f.length() - 1) {
            fname = f.substring(0,i);
        }
        return fname;
    }

    public static void main(String[] args) {
        try {
//            GzipUtils.doUncompressFile("C:\\Users\\alleyz\\Desktop\\ceshi\\CUSTCONTINFO_2016111519_36_00020.txt.gz");
//            GzipUtils.doUncompressFile("G:\\config\\CUSTCONTINFO_2016111519_36_00020.txt.gz");
//            List<String> strs = FileUtils.readLines(new File("C:\\Users\\alleyz\\Desktop\\ceshi\\CUSTCONTINFO_2016111519_36_00020.txt"), "UTF-8", 1);

//            strs.forEach(GzipUtils :: print);

        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void print(String str) {
        String strs[] = str.split("\\|");
        if(strs.length != 22) {
            System.out.println(strs[0]);
        }
//        14792094518926133|6AE2962940AA493DB38EDCB4870EA393|9278|0|17605875882|008657710010||20161115193148|470|05|01|20161115193148|89||9|20161115193148|20161115193148|20161115193148|20161115193148|20161115193148|20161115193317|12

        if(StringUtils.isEmpty(strs[6]) || StringUtils.isEmpty(strs[5]) || StringUtils.isEmpty(strs[4])){
            System.out.println(strs[0]);
        }
    }

}
