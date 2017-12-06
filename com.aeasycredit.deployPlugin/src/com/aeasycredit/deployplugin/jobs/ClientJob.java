/* 
 * Copyright (C), 2014-2016, 时代际客(深圳)软件有限公司
 * File Name: @(#)ClientJob.java
 * Encoding UTF-8
 * Author: zhaoxunyong
 * Version: 3.0
 * Date: Feb 22, 2016
 */
package com.aeasycredit.deployplugin.jobs;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.progress.IProgressConstants;

import com.aeasycredit.deployplugin.Activator;
import com.aeasycredit.deployplugin.CmdBuilder;
import com.aeasycredit.deployplugin.DeployPluginHelper;

/**
 * 功能描述 http://blog.eitchnet.ch/archives/46
 * 
 * <p>
 * <a href="ClientJob.java"><i>View Source</i></a>
 * </p>
 * 
 * @author zhaoxunyong
 * @version 3.0
 * @since 1.0
 */
public class ClientJob extends Job {

    private List<CmdBuilder> cmdBuilders;
    // private MessageConsoleStream console;
    // private Shell shell;
    // private IProject project;
    private CompletionAction completionAction;

    /**
     * @param name
     * @param completedAction
     */
    public ClientJob(String name, List<CmdBuilder> cmdBuilders, CompletionAction completionAction) {
        super(name);
        // this.console = console;
        // this.shell = shell;
        // this.project = project;
        this.cmdBuilders = cmdBuilders;
        this.completionAction = completionAction;
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {
        IStatus status = null;
        completionAction.getConsole().print("loading...");
        // activate the progress bar with an unknown amount of task work
        monitor.beginTask("Loading " + getName(), IProgressMonitor.UNKNOWN);
        // perform the job
        try {
            // execute task work...
            boolean ok = true;
            if (cmdBuilders != null && !cmdBuilders.isEmpty()) {
                for (CmdBuilder cmdBuilder : cmdBuilders) {
                    boolean runOk = DeployPluginHelper.exec(completionAction.getConsole(), cmdBuilder.getWorkHome(), cmdBuilder.getCommand(), cmdBuilder.getParams(), false);
                    if (!runOk) {
                        ok = false;
                        break;
                    }
                }
            }
            completionAction.setOk(ok);
            completionAction.getConsole().print(this.getName()+" OK.");
            // refresh workspace
            completionAction.getRefreshable().refresh();
            status = Status.OK_STATUS;
            // at the end of the successfully ended work, set the completion
            // task to be ok
        } catch (Exception e) {
            e.printStackTrace();
            // logger.error(e1, e1);
            // if the work failed then set the completion
            // task to be NOT ok and set the exception so it
            // can be shown to the user
            completionAction.setThrowable(e);
            completionAction.getConsole().print("ERROR.");
            completionAction.setOk(false);
            status = new Status(IStatus.ERROR, DeployPluginHelper.PLUGIN_ID, e.getLocalizedMessage(), e);
        }
        // stop the monitor
        monitor.done();
        // execute the completion task
        complete();
        return status;
    }

    /**
     * completes the task by showing the user a dialog about the execution state
     * of the job
     */
    protected void complete() {
        setProperty(IProgressConstants.ICON_PROPERTY, Activator.getImageDescriptor("/icons/sample.gif"));
        Boolean isModal = (Boolean) this.getProperty(IProgressConstants.PROPERTY_IN_DIALOG);
        if (isModal != null && isModal.booleanValue()) {
            // The progress dialog is still open so
            // just open the message
            showResults(completionAction);
        } else {
            setProperty(IProgressConstants.KEEP_PROPERTY, Boolean.TRUE);
            setProperty(IProgressConstants.ACTION_PROPERTY, completionAction);
        }
    }

    /**
     * Asynchronous execution of an {@link Action}
     * 
     * @param action
     */
    protected static void showResults(final Action action) {
        Display.getDefault().asyncExec(new Runnable() {
            public void run() {
                action.run();
            }
        });
    }
}