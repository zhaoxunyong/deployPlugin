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
