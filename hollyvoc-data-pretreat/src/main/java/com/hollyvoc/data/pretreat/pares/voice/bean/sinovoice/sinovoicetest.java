package com.hollyvoc.data.pretreat.pares.voice.bean.sinovoice;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class sinovoicetest {
	public static void main(String[] args) {
		try {
			/**
			 * 读取xml至对象
			 */
			JAXBContext jc = JAXBContext
					.newInstance("com.hollycrm.textminer.util.sinovoice");
			Unmarshaller unmarshaller = jc.createUnmarshaller();
			Result result = (Result) unmarshaller.unmarshal(new File("src/voice.xml"));
			System.out.println("xml-->对象: "
					+ result.getSentenceList().getSentence().get(0).getText()
							.toString());
			/**
			 * 将对象输出至xml
			 */
			ObjectFactory objFactory = new ObjectFactory();
			Result resultOut = objFactory.createResult();
			SentenceList sl = objFactory.createSentenceList();
			Sentence st = objFactory.createSentence();
			sl.getSentence().add(st);
			st.setText("测试输出为xml");
			resultOut.setSentenceList(sl);
			System.out.println("对象-->xml: "
					+ resultOut.getSentenceList().getSentence().get(0)
							.getText().toString());
			Marshaller marshaller = jc.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,
					new Boolean(true));
			marshaller.marshal(resultOut, new FileOutputStream(
					"src/voiceOut.xml"));
		} catch (JAXBException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
}
