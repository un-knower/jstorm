package com.hollyvoc.helper.jdbc.bean;

import java.io.Serializable;

/**
 * Created by qianxm on 2017/7/27.
 * 质检项对象
 */
//@Getter @Setter
public class TblVocQcItem implements Serializable{
    private String qcItemId;
    private String qcItemType;
    private String qcItemName;
    private String content;
    private String status;
    private String roleType;
    private String domainId;
    private String isValid;
    private String lastCreator;
    private String lastCreateTime;
    private String creator;
    private String createTime;
    private String province;
    private String startTime;
    private String endTime;
    private String custBrand;
    private String custLevel;
    private String satisfication;
    private String businessType;
    private String direction;
    private String recoinfoLength;
    private String silenceLength;
}
