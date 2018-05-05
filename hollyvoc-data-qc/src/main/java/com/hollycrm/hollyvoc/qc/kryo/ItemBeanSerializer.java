package com.hollycrm.hollyvoc.qc.kryo;

import com.alibaba.jstorm.esotericsoftware.kryo.Kryo;
import com.alibaba.jstorm.esotericsoftware.kryo.Serializer;
import com.alibaba.jstorm.esotericsoftware.kryo.io.Input;
import com.alibaba.jstorm.esotericsoftware.kryo.io.Output;
import com.hollycrm.hollyvoc.qc.bean.ItemBean;

/**
 * Created by qianxm on 2017/12/6.
 */
public class ItemBeanSerializer extends Serializer<ItemBean> {
    @Override
    public void write(Kryo kryo, Output output, ItemBean itemBean) {
        output.writeLong(itemBean.getStartTime());
        output.writeLong(itemBean.getEndTime());
        output.writeDouble(itemBean.getEnergy());
        output.writeDouble(itemBean.getSpeed());
        output.writeString(itemBean.getText());
    }

    @Override
    public ItemBean read(Kryo kryo, Input input, Class<ItemBean> aClass) {
        ItemBean item = new ItemBean(input.readLong(),input.readLong(),input.readDouble(), input.readDouble(),input.readString());
        return item;
    }
}
