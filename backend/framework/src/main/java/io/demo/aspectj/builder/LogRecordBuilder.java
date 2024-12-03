package io.demo.aspectj.builder;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LogRecordBuilder {
    private String successLogTemplate;
    private String failLogTemplate;
    private String operatorId;
    private String type;
    private String bizNo;
    private String subType;
    private String extra;
    private String condition;
    private String isSuccess;
}
