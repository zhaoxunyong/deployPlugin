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
