/* 
 * Copyright (C), 2014-2016, 时代际客(深圳)软件有限公司
 * File Name: @(#)AbstractHandler.java
 * Encoding UTF-8
 * Author: zhaoxunyong
 * Version: 3.0
 * Date: Feb 24, 2016
 */
package com.aeasycredit.deployplugin.handlers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.core.PackageFragment;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.eclipse.ui.handlers.HandlerUtil;

import com.aeasycredit.deployplugin.CmdBuilder;
import com.aeasycredit.deployplugin.DeployPluginException;
import com.aeasycredit.deployplugin.DeployPluginHelper;
import com.aeasycredit.deployplugin.jobs.ClientJob;
import com.aeasycredit.deployplugin.jobs.CompletionAction;
import com.aeasycredit.deployplugin.jobs.Refreshable;
import com.google.common.collect.Lists;

/**
 * 功能描述
 * 
 * <p>
 * <a href="AbstractHandler.java"><i>View Source</i></a>
 * </p>
 * 
 * @author zhaoxunyong
 * @version 3.0
 * @since 1.0
 */
@SuppressWarnings("restriction")
public abstract class AbstractDeployPluginHandler extends AbstractHandler implements Refreshable {
    protected Shell shell;
    protected ISelection selection;

    private IProject project;
    protected MessageConsoleStream console;
    protected final static String DEPLOY_BAT = "deploy.bat";
    protected final static String DEPLOY_ALL_BAT = "deployAll.bat";

    /**
     * 
     */
    public AbstractDeployPluginHandler() {
        this.console = DeployPluginHelper.console(true);
    }
    
    public IProject getProject() {
        return project;
    }

    public void setProject(IProject project) {
        this.project = project;
    }



    @SuppressWarnings("rawtypes")
    protected IProject project() throws Exception {
//      String name = "windows start";
        IProject project = null;
//      try {
            if (selection.isEmpty() || selection instanceof TextSelection) {
//              MessageDialog.openError(shell, name, "No project or package selected.");
                throw new Exception("No project or package selected.");
            } else {
                if (selection instanceof TreeSelection) {
                    TreeSelection ts = (TreeSelection) selection;
                    if (!ts.isEmpty()) {
                        Iterator iterator = ts.iterator();
                        while (iterator.hasNext()) {
                            Object itObj = iterator.next();
                            if (itObj instanceof JavaProject) {
                                JavaProject jproject = (JavaProject) itObj;
                                project = jproject.getProject();
                                break;
                            } else if (itObj instanceof PackageFragment) {
                                PackageFragment packageFragment = (PackageFragment) itObj;
                                IJavaProject jproject = packageFragment.getJavaProject();
                                project = jproject.getProject();
                                break;
                            }
                        }

                    }
                }
            }
            if (project == null) {
                throw new Exception("No project or package selected.");
            }
            return project;
//      } catch (Exception e) {
//          e.printStackTrace();
//          MessageDialog.openError(shell, name, e.getMessage());
//      }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.chinatime.sliceplugin.popup.Refreshable#refresh()
     */
    @Override
    public void refresh() throws DeployPluginException {
        if (project == null) {
            throw new DeployPluginException("project is null.");
        }
        try {
            project.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
        } catch (CoreException e) {
            throw new DeployPluginException(e.getMessage(), e);
        }
    }

    public void clearConsole() {
        MessageConsole cs = DeployPluginHelper.findConsole();
        cs.clearConsole();
        cs.activate();
    }

    protected void changeVersion(ExecutionEvent event) throws Exception {
        runCmd(event, "Change version", "changeVersion.sh");
    }

    protected void release(ExecutionEvent event) throws Exception {
        runCmd(event, "Release", "release.sh");
    }

    private void checkParam(String projectPath, String cmd) throws IOException {
        String gitHome = System.getenv("GIT_HOME");
        if(StringUtils.isBlank(gitHome)) {
            throw new FileNotFoundException("GIT_HOME env must be not empty.");
        }
        if (!new File(projectPath + "\\" + cmd).exists()) {
            throw new FileNotFoundException(cmd + " file not fonund.");
        }
    }
    
    private String input(ExecutionEvent event, String name) throws Exception {
        InputDialog dlg = new InputDialog(
                HandlerUtil.getActiveShellChecked(event), name,
                "Enter new version", "", null);
        String input = "";
        if (dlg.open() == Window.OK) {
            // User clicked OK; run perl
            input = dlg.getValue();
        }
        if(StringUtils.isBlank(input)) {
            throw new DeployPluginException("New version must be not empty.");
        }
        return input;
    }

    @SuppressWarnings({ "rawtypes" })
    private void runCmd(ExecutionEvent event, String name, String cmd) throws Exception {
        List<CmdBuilder> cmdBuilders = Lists.newLinkedList();
        if (selection.isEmpty() || selection instanceof TextSelection) {
            project = null;
//              MessageDialog.openError(shell, name, "No project or package selected.");
            throw new Exception("No project or package selected.");
        } else {
            if (selection instanceof TreeSelection) {
                TreeSelection ts = (TreeSelection) selection;
                if (!ts.isEmpty()) {
                    Iterator iterator = ts.iterator();
                    while (iterator.hasNext()) {
                        Object itObj = iterator.next();
                        if (itObj instanceof JavaProject) {
                            JavaProject jproject = (JavaProject) itObj;
                            project = jproject.getProject();
                            String projectPath = project.getLocation().toFile().getPath();
                            checkParam(projectPath, cmd);
                            cmdBuilders.add(new CmdBuilder(projectPath, cmd, input(event, name)));
                        } else if (itObj instanceof PackageFragment) {
                            PackageFragment packageFragment = (PackageFragment) itObj;
                            IJavaProject jproject = packageFragment.getJavaProject();
                            project = jproject.getProject();
                            String projectPath = project.getLocation().toFile().getPath();
                            checkParam(projectPath, cmd);
                            cmdBuilders.add(new CmdBuilder(projectPath, cmd, input(event, name)));
                        }
                    }

                }
            }
            if (project == null) {
                throw new Exception("No project or package selected.");
            }
            if (cmdBuilders != null && !cmdBuilders.isEmpty()) {
                runJob(name, cmd, cmdBuilders);
            } else {
//                  MessageDialog.openError(shell, name, "No project or pakcage selected.");
                throw new Exception("No project or package selected.");
            }
        }
    }

    protected void runJob(final String name, final String cmd, List<CmdBuilder> cmdBuilders) throws CoreException {
        boolean isConfirm = MessageDialog.openConfirm(shell, "process confirm?", project.getName() + " process confirm?");

        if (isConfirm) {

            // create action to be called after the action is completed
            // this action shows a success dialog if the job executed without
            // an exception, otherwise it shows an error dialog
            final CompletionAction completionAction = new CompletionAction(DeployPluginHelper.PLUGIN_ID, shell, this,
                    console);
            completionAction.setOkTitle(name + " sucess");
            completionAction.setOkMsg("Sucessfully executed " + name);
            completionAction.setFailTitle(name + " failed");
            completionAction.setFailMsg("There was an exception while executing " + name);

            final ClientJob job = new ClientJob("DeployPlugin:" + name, cmdBuilders, completionAction);

            // if short action, otherwise is long Job.LONG
            job.setPriority(Job.SHORT);
            // show a dialog immediately
            job.setUser(false);
            // job.setSystem(true);
            // start as soon as possible
            job.schedule();
            
            /*String mode = ILaunchManager.RUN_MODE;
            if (debug)
                mode = ILaunchManager.DEBUG_MODE;
            else
                mode = ILaunchManager.RUN_MODE;
            ILaunchConfigurationWorkingCopy config = createConfig(name);
            
            final ILaunch ILAUNCH = config.launch(mode, null);
            
//          final ILaunch pendingLaunch = new DebugUIPlugin.PendingLaunch(createConfig(name), mode, job);
            DebugPlugin.getDefault().getLaunchManager().addLaunch(ILAUNCH);
            
            IJobChangeListener listener = new IJobChangeListener() {
                @Override
                public void sleeping(IJobChangeEvent event) {
                }

                @Override
                public void scheduled(IJobChangeEvent event) {
                }

                @Override
                public void running(IJobChangeEvent event) {
                }

                @Override
                public void awake(IJobChangeEvent event) {
                }

                @Override
                public void aboutToRun(IJobChangeEvent event) {
                    
                }

                @Override
                public void done(IJobChangeEvent event) {
                    DebugPlugin dp = DebugPlugin.getDefault();
                    if (dp != null) {
                        dp.getLaunchManager().removeLaunch(ILAUNCH);
                    }
                    job.removeJobChangeListener(this);
                }
            };
            job.addJobChangeListener(listener);*/
            
            // job.addJobChangeListener(new JobChangeAdapter() {
            // @Override
            // public void done(IJobChangeEvent event) {
            // if (event.getResult().isOK()) {
            // SlicePluginTools.success(shell, name+" completed successfully");
            // } else {
            // MessageDialog.openError(shell, name+" did not complete
            // successfully");
            // }
            // }
            // });
        }
    }
}
