package com.hollycrm.hollyvoc.qc.kryo;

//import org.apache.kafka.common.serialization.Serializer;

import com.alibaba.jstorm.esotericsoftware.kryo.Kryo;
import com.alibaba.jstorm.esotericsoftware.kryo.Serializer;
import com.alibaba.jstorm.esotericsoftware.kryo.io.Input;
import com.alibaba.jstorm.esotericsoftware.kryo.io.Output;
import com.hollycrm.hollyvoc.qc.bean.CustcontentInfo;

/**
 * Created by qianxm on 2017/12/5.
 */
public class CustInfoSerializer extends Serializer<CustcontentInfo> {
    @Override
    public void write(Kryo kryo, Output output, CustcontentInfo inner) {
        output.writeString(inner.getCustinfoId());
        output.writeString(inner.getAreaCode());
        output.writeString(inner.getUserCode());
        output.writeString(inner.getCallee());
        output.writeString(inner.getCallee());
        output.writeString(inner.getMobileNo());
        output.writeString(inner.getAcceptTime());
        output.writeString(inner.getYear());
        output.writeString(inner.getMonth());
        output.writeString(inner.getDay());
        output.writeString(inner.getWeek());
        output.writeString(inner.getCustArea());
        output.writeString(inner.getCustBrand());
        output.writeString(inner.getSatisfication());
        output.writeString(inner.getQueue());
        output.writeString(inner.getServiceType());
        output.writeString(inner.getSheetNo());
        output.writeString(inner.getSheetType());
        output.writeString(inner.getCustLevel());
        output.writeString(inner.getBusinessType());
        output.writeString(inner.getRecordLength());
        output.writeString(inner.getSilenceLength());
        output.writeString(inner.getRecordLengthRange());
        output.writeString(inner.getSilenceLengthRange());
        output.writeString(inner.getUserContent());
        output.writeString(inner.getAgentContent());
        output.writeString(inner.getAllContent());
        output.writeString(inner.getDirection());
        output.writeString(inner.getRecordFormat());
        output.writeString(inner.getRecordSampRate());
        output.writeString(inner.getRecordEncodeRate());
        output.writeString(inner.getHasSheet());
        output.writeString(inner.getNetType());
        output.writeString(inner.getHour());
//        kryo.writeObject(output, inner.getCustinfoId());
//        kryo.writeObject(output, inner.getAreaCode());
    }

    @Override
    public CustcontentInfo read(Kryo kryo, Input input, Class<CustcontentInfo> aClass) {
        CustcontentInfo inner = new CustcontentInfo(input.readString(),input.readString(),input.readString(),
                input.readString(),input.readString(),input.readString(),input.readString(), input.readString(),
                input.readString(), input.readString(), input.readString(), input.readString(),input.readString(),
                input.readString(),input.readString(), input.readString(), input.readString(), input.readString(),
                input.readString(), input.readString(), input.readString(), input.readString(), input.readString(),
                input.readString(), input.readString(), input.readString(), input.readString(), input.readString(),
                input.readString(), input.readString(), input.readString(), input.readString(), input.readString(),
                input.readString());
        return inner;
    }
}
