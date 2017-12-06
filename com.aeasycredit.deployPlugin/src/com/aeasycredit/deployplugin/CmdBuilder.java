/* 
 * Copyright (C), 2014-2016, 时代际客(深圳)软件有限公司
 * File Name: @(#)CmdParam.java
 * Encoding UTF-8
 * Author: zhaoxunyong
 * Version: 3.0
 * Date: Feb 22, 2016
 */
package com.aeasycredit.deployplugin;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * 功能描述
 * 
 * <p>
 * <a href="CmdParam.java"><i>View Source</i></a>
 * </p>
 * 
 * @author zhaoxunyong
 * @version 3.0
 * @since 1.0
 */
public class CmdBuilder {
    private String workHome;
    private String command;
    private String params;
    
    public CmdBuilder(String workHome, String command, String params) {
        this.workHome = workHome;
        this.command = command;
        this.params = params;
    }

    public String getWorkHome() {
        return workHome;
    }

    public void setWorkHome(String workHome) {
        this.workHome = workHome;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }
    
    public String getParams() {
        return params;
    }

    public void setParams(String params) {
        this.params = params;
    }

    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
