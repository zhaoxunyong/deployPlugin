/* 
 * Copyright (C), 2014-2016, 时代际客(深圳)软件有限公司
 * File Name: @(#)SlicePluginException.java
 * Encoding UTF-8
 * Author: zhaoxunyong
 * Version: 3.0
 * Date: Feb 24, 2016
 */
package com.aeasycredit.deployplugin;

/** 
 * 功能描述
 * 
 * <p>
 * <a href="SlicePluginException.java"><i>View Source</i></a>
 * </p>
 * @author zhaoxunyong
 * @version 3.0
 * @since 1.0 
*/
public class DeployPluginException extends Exception {

    private static final long serialVersionUID = 7718770093867602818L;

    /**
     * @param string
     */
    public DeployPluginException(String message) {
        super(message);
    }
    
    public DeployPluginException(String message, Throwable cause) {
        super(message, cause);
    }

}
