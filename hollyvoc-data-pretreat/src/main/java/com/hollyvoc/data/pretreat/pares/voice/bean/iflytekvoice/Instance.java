//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.05.13 at 09:53:11 PM CST 
//


package com.hollyvoc.data.pretreat.pares.voice.bean.iflytekvoice;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{}subject" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *       &lt;attribute name="bit-rate" use="required" type="{http://www.w3.org/2001/XMLSchema}integer" />
 *       &lt;attribute name="channel" use="required" type="{http://www.w3.org/2001/XMLSchema}NCName" />
 *       &lt;attribute name="duration" use="required" type="{http://www.w3.org/2001/XMLSchema}integer" />
 *       &lt;attribute name="file_comment" use="required" type="{http://www.w3.org/2001/XMLSchema}integer" />
 *       &lt;attribute name="fmt" use="required" type="{http://www.w3.org/2001/XMLSchema}NCName" />
 *       &lt;attribute name="sample-rate" use="required" type="{http://www.w3.org/2001/XMLSchema}integer" />
 *       &lt;attribute name="waveuri" use="required" type="{http://www.w3.org/2001/XMLSchema}anyURI" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "subject"
})
@XmlRootElement(name = "instance")
public class Instance {

    @XmlElement(required = true)
    protected List<Subject> subject;
    @XmlAttribute(name = "bit-rate", required = true)
    protected BigInteger bitRate;
    @XmlAttribute(name = "channel", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "NCName")
    protected String channel;
    @XmlAttribute(name = "duration", required = true)
    protected BigInteger duration;
    @XmlAttribute(name = "file_comment", required = true)
    protected BigInteger fileComment;
    @XmlAttribute(name = "fmt", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "NCName")
    protected String fmt;
    @XmlAttribute(name = "sample-rate", required = true)
    protected BigInteger sampleRate;
    @XmlAttribute(name = "waveuri", required = true)
    @XmlSchemaType(name = "anyURI")
    protected String waveuri;

    /**
     * Gets the value of the subject property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the subject property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSubject().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Subject }
     * 
     * 
     */
    public List<Subject> getSubject() {
        if (subject == null) {
            subject = new ArrayList<Subject>();
        }
        return this.subject;
    }

    /**
     * Gets the value of the bitRate property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getBitRate() {
        return bitRate;
    }

    /**
     * Sets the value of the bitRate property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setBitRate(BigInteger value) {
        this.bitRate = value;
    }

    /**
     * Gets the value of the channel property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getChannel() {
        return channel;
    }

    /**
     * Sets the value of the channel property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setChannel(String value) {
        this.channel = value;
    }

    /**
     * Gets the value of the duration property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getDuration() {
        return duration;
    }

    /**
     * Sets the value of the duration property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setDuration(BigInteger value) {
        this.duration = value;
    }

    /**
     * Gets the value of the fileComment property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getFileComment() {
        return fileComment;
    }

    /**
     * Sets the value of the fileComment property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setFileComment(BigInteger value) {
        this.fileComment = value;
    }

    /**
     * Gets the value of the fmt property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFmt() {
        return fmt;
    }

    /**
     * Sets the value of the fmt property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFmt(String value) {
        this.fmt = value;
    }

    /**
     * Gets the value of the sampleRate property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getSampleRate() {
        return sampleRate;
    }

    /**
     * Sets the value of the sampleRate property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setSampleRate(BigInteger value) {
        this.sampleRate = value;
    }

    /**
     * Gets the value of the waveuri property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getWaveuri() {
        return waveuri;
    }

    /**
     * Sets the value of the waveuri property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setWaveuri(String value) {
        this.waveuri = value;
    }

}
