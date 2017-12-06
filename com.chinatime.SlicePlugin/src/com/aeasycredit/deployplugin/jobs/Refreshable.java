/* 
 * Copyright (C), 2014-2016, 时代际客(深圳)软件有限公司
 * File Name: @(#)Refreshable.java
 * Encoding UTF-8
 * Author: zhaoxunyong
 * Version: 3.0
 * Date: Feb 24, 2016
 */
package com.aeasycredit.deployplugin.jobs;

import com.aeasycredit.deployplugin.DeployPluginException;

/**
 * 功能描述
 * 
 * <p>
 * <a href="Refreshable.java"><i>View Source</i></a>
 * </p>
 * 
 * @author zhaoxunyong
 * @version 3.0
 * @since 1.0
 */
public interface Refreshable {
    public void refresh() throws DeployPluginException;
}
