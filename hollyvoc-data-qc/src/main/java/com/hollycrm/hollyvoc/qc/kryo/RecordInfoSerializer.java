package com.hollycrm.hollyvoc.qc.kryo;

import com.alibaba.jstorm.esotericsoftware.kryo.Kryo;
import com.alibaba.jstorm.esotericsoftware.kryo.Serializer;
import com.alibaba.jstorm.esotericsoftware.kryo.io.Input;
import com.alibaba.jstorm.esotericsoftware.kryo.io.Output;
import com.hollycrm.hollyvoc.qc.bean.ItemBean;
import com.hollycrm.hollyvoc.qc.bean.RecordInfo;

import java.util.List;

/**
 * Created by qianxm on 2017/12/6.
 */
public class RecordInfoSerializer extends Serializer<RecordInfo> {

    @Override
    public void write(Kryo kryo, Output output, RecordInfo recordInfo) {
        output.writeString(recordInfo.getRole());
        output.writeString(recordInfo.getCount());
        output.writeLong(recordInfo.getDuration());
        kryo.writeObject(output, recordInfo.getContents());
    }

    @Override
    public RecordInfo read(Kryo kryo, Input input, Class<RecordInfo> aClass) {
//        List<ItemBean> item = new ArrayList<>();
        List<ItemBean> items = kryo.readObject(input, List.class);
        RecordInfo recordInfo = new RecordInfo(input.readString(),input.readString(),input.readLong(), items);
        return recordInfo;
    }
}
