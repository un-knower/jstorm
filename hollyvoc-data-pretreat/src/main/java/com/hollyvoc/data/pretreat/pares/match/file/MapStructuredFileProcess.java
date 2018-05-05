package com.hollyvoc.data.pretreat.pares.match.file;


import com.hollyvoc.data.pretreat.config.Const;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhaihw on 2017/2/10.
 *
 */
public abstract class MapStructuredFileProcess<T> {
    public final static String ID = "id", OBJ="obj";

    public final Map<String, T> parse(String fileName, String prov, Map<Const.CodeType, Map<String, String>> maps)
            throws IOException {
        Map<String, T> map = new HashMap<>();
        boolean skip = true; // 是否需要跳过
        // try with resource AutoCloseable
        Map<String, Object> cols = new HashMap<>(1);
        try(BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "UTF-8"))){
            String line;
            while ((line = br.readLine()) != null) {
                if(skip) {
                    skip = false;
                    continue;
                }
                assign(line, prov, maps, cols);
                if(cols.size() == 0) {
                    continue;
                }
                map.put((String)cols.get(ID),  (T)cols.get(OBJ));
                cols.clear();
            }
        }
        return map;
    }

    protected abstract void assign(String line, String prov, Map<Const.CodeType, Map<String, String>> maps, Map<String,
            Object> result);
}
