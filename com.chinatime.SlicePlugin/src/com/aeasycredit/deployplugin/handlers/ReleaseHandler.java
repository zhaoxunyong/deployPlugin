/* 
 * Copyright (C), 2014-2016, 时代际客(深圳)软件有限公司
 * File Name: @(#)DeployAllHandler.java
 * Encoding UTF-8
 * Author: zhaoxunyong
 * Version: 3.0
 * Date: Feb 24, 2016
 */
package com.aeasycredit.deployplugin.handlers;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

/** 
 * 功能描述
 * 
 * <p>
 * <a href="DeployAllHandler.java"><i>View Source</i></a>
 * </p>
 * @author zhaoxunyong
 * @version 3.0
 * @since 1.0 
*/
public class ReleaseHandler extends AbstractDeployPluginHandler {
    /**
     * The constructor.
     */
    public ReleaseHandler() {
    }

    /**
     * the command has been executed, so extract extract the needed information
     * from the application context.
     */
    public Object execute(ExecutionEvent event) throws ExecutionException {
        IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
        this.shell = window.getShell();
        this.selection = window.getSelectionService().getSelection();
        clearConsole();
        try {
            this.setProject(project());
            release(event);
		} catch (Exception e) {
			MessageDialog.openError(shell, "release error", e.getMessage());
		}
        return null;
    }
}
