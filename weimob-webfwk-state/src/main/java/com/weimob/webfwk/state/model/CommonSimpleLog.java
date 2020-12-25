package com.weimob.webfwk.state.model;

import java.util.Date;

import lombok.Data;

@Data
public class CommonSimpleLog {
    
    private Long id;
    
    /* 操作对象类型 */
    private String objectType;
    
    /* 操作对象主键 */
    private String objectId;
    
    /* 操作人编号 */
    private Long operateUserId;
    
    /* 操作人用户名 */
    private String operateUserName;
    
    /* 操作人显示名 */
    private String operateUserDisplay;
    
    /* 操作类型 */
    private String operateType;
    
    /* 操作描述 */
    private String operateDesc;
    
    /* 操作时间 */
    private Date operateTime;
    
    /* 操作明细 */
    private Long operateDetailId;
}
