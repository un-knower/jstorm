package com.hollyvoc.data.pretreat.pares.match.file;

import com.hollyvoc.data.pretreat.config.Const;
import lombok.extern.log4j.Log4j;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by zhaihw on 2017/2/9.
 * 文件处理，输出
 */
@Log4j
public abstract class ListStructuredFileProcess<T> {

    public final List<T> parse(String fileName, Map<Const.CodeType, Map<String, String>> maps) throws IOException{
        List<T> list = new ArrayList<>();
        boolean skip = true; // 是否需要跳过
        // try with resource AutoCloseable
        try(BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "UTF-8"))){
            String line;
            while ((line = br.readLine()) != null) {
                if(skip) {
                    skip = false;
                    continue;
                }
                list.add(assign(line, maps));
            }
        }
        return list;
    }
    abstract T assign(String line, Map<Const.CodeType, Map<String, String>> maps);

}
