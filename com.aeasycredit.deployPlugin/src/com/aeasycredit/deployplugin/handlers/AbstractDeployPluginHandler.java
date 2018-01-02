package com.aeasycredit.deployplugin.handlers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.internal.resources.Project;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.core.PackageFragment;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.eclipse.ui.handlers.HandlerUtil;

import com.aeasycredit.deployplugin.CmdBuilder;
import com.aeasycredit.deployplugin.DeployPluginHelper;
import com.aeasycredit.deployplugin.exception.DeployPluginException;
import com.aeasycredit.deployplugin.jobs.ClientJob;
import com.aeasycredit.deployplugin.jobs.CompletionAction;
import com.aeasycredit.deployplugin.jobs.Refreshable;
import com.google.common.collect.Lists;

import edu.nyu.cs.javagit.api.DotGit;
import edu.nyu.cs.javagit.api.Ref;

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
    protected final static String CHANGEVERSION_BAT = "./changeVersion.sh";
    protected final static String RELEASE_BAT = "./release.sh";
    protected final static String MERGE_BAT = "./merge.sh";

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
                            if (itObj instanceof Project) {
                                Project prj = (Project) itObj;
                                project = prj.getProject();
                                break;
                            } else if (itObj instanceof JavaProject) {
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
        changeVersion(event, "Change version");
    }

    protected void release(ExecutionEvent event) throws Exception {
        release(event, "Release");
    }

    protected void merge(ExecutionEvent event) throws Exception {
        merge(event, "Merge");
    }

    private String getParentProject(String projectPath, String cmd) throws IOException {
        String gitHome = System.getenv("GIT_HOME");
        if(StringUtils.isBlank(gitHome)) {
            throw new FileNotFoundException("GIT_HOME env must be not empty.");
        }
        if (!new File(projectPath + "\\" + cmd).exists()) {
            String parent = new File(projectPath).getParent();
            if(StringUtils.isBlank(parent)) {
                throw new FileNotFoundException(cmd.replace("./", "") + " not found.");
            }
            return getParentProject(parent, cmd);
        }
        return projectPath;
    }
    
    /**
     * http://www.vogella.com/tutorials/EclipseDialogs/article.html
     */
    private String input(ExecutionEvent event, String name, String defaultValue, String example) throws Exception {
        
        /*ElementListSelectionDialog dlg =
                new ElementListSelectionDialog(shell, new LabelProvider());
            dlg.setElements(new String[] { "Linux", "Mac", "Windows" });
            dlg.setTitle("Which operating system are you using");*/
        
        InputDialog dlg = new InputDialog(
                HandlerUtil.getActiveShellChecked(event), name,
                "Enter parameter, example: " + example, defaultValue, null);
        String input = "";
        if (dlg.open() == Window.OK) {
            // User clicked OK
            input = dlg.getValue();
            if(StringUtils.isBlank(input)) {
                throw new DeployPluginException("Parameter must be not empty.");
            }
        }
        return input;
    }
    
    private String processChangeVersionScript(String tempFolder) throws IOException {
        String changVersionName = CHANGEVERSION_BAT.replace("./", "");
        String projectPath = project.getLocation().toFile().getPath();
        String rootProjectPath = getParentProject(projectPath, CHANGEVERSION_BAT);
        rootProjectPath = rootProjectPath.replace("\\", "\\\\");
//        InputStream input = this.getClass().getResourceAsStream("/merge.sh");
//        String str = IOUtils.toString(input);
        String str = FileUtils.readFileToString(new File(rootProjectPath+"/"+changVersionName), "UTF-8");
        String mergeScript = str.replace("#cd #{project}", "cd "+rootProjectPath);
        File file = new File(tempFolder+"/"+changVersionName);
        FileUtils.writeStringToFile(file, mergeScript);
        return rootProjectPath;
    }
    
    private String processRleaseScript(String tempFolder) throws IOException {
        String releaseName = RELEASE_BAT.replace("./", "");
        String projectPath = project.getLocation().toFile().getPath();
        String rootProjectPath = getParentProject(projectPath, MERGE_BAT);
        rootProjectPath = rootProjectPath.replace("\\", "\\\\");
//        InputStream input = this.getClass().getResourceAsStream("/merge.sh");
//        String str = IOUtils.toString(input);
        String str = FileUtils.readFileToString(new File(rootProjectPath+"/"+releaseName), "UTF-8");
        String mergeScript = str.replace("#cd #{project}", "cd "+rootProjectPath);
        File file = new File(tempFolder+"/"+releaseName);
        FileUtils.writeStringToFile(file, mergeScript);
        return rootProjectPath;
    }
    
    private String processMergeScript(String tempFolder) throws IOException {
        String mergeName = MERGE_BAT.replace("./", "");
        String projectPath = project.getLocation().toFile().getPath();
        String rootProjectPath = getParentProject(projectPath, MERGE_BAT);
        rootProjectPath = rootProjectPath.replace("\\", "\\\\");
//        InputStream input = this.getClass().getResourceAsStream("/merge.sh");
//        String str = IOUtils.toString(input);
        String str = FileUtils.readFileToString(new File(rootProjectPath+"/"+mergeName), "UTF-8");
        String mergeScript = str.replace("#cd #{project}", "cd "+rootProjectPath);
        File file = new File(tempFolder+"/"+mergeName);
        FileUtils.writeStringToFile(file, mergeScript);
        return rootProjectPath;
    }
    
    @SuppressWarnings("unchecked")
    private String getPomVersion(String rootProjectPath) throws IOException {
        String pomFile = rootProjectPath+"/pom.xml";
        List<String> value = FileUtils.readLines(new File(pomFile));
        String version = "";
        for(String v : value) {
            if(v.indexOf("<version>")!=-1) {
                version = StringUtils.substringBetween(v, "<version>", "</version>");
                break;
            }
        }
        return version;
    }

    private void merge(ExecutionEvent event, String name) throws Exception {
        List<CmdBuilder> cmdBuilders = Lists.newLinkedList();
        String tempFolder = System.getenv("TEMP");
        String rootProjectPath = processMergeScript(tempFolder);
        
        String pomVersion = getPomVersion(rootProjectPath);
        String defaultValue = pomVersion+" master";
        
        // Get release version
        String releaseVersion = pomVersion.replace("-SNAPSHOT", "")+".release";
        // Check releaseVersion is exist
        File repositoryDirectory = new File(rootProjectPath);
        // Get the instance of the DotGit Object
        DotGit dotGit = DotGit.getInstance(repositoryDirectory);
        Iterator<Ref> refs = dotGit.getBranches();
        while(refs.hasNext()) {
            Ref ref = refs.next();
//            System.out.println("ref--->"+ref.getName()+"/"+ref.getRepositoryName());
            if(releaseVersion.equals(ref.getName())) {
                defaultValue = releaseVersion+" master";
                break;
            }
        }
        
        String version = input(event, name, defaultValue, "branchFromVersion branchToVersion");
        if(StringUtils.isNotBlank(version)) {
            
            cmdBuilders.add(new CmdBuilder(tempFolder, MERGE_BAT, version));
            if (cmdBuilders != null && !cmdBuilders.isEmpty()) {
                runJob(name, cmdBuilders);
            } else {
//              MessageDialog.openError(shell, name, "No project or pakcage selected.");
                throw new Exception("No project or package selected.");
            }
        }
    }

    private void changeVersion(ExecutionEvent event, String name) throws Exception {
        List<CmdBuilder> cmdBuilders = Lists.newLinkedList();

        String tempFolder = System.getenv("TEMP");
        String rootProjectPath = processChangeVersionScript(tempFolder);
        String pomVersion = getPomVersion(rootProjectPath);
        String bPomVersion = StringUtils.substringBeforeLast(pomVersion, ".");
        String aPomVersion = StringUtils.substringAfterLast(pomVersion, ".").replace("-SNAPSHOT", "");
        aPomVersion = String.valueOf(Integer.parseInt(aPomVersion)+1);
        String defaultValue = bPomVersion+"."+aPomVersion+"-SNAPSHOT";
        String params = input(event, name, defaultValue, "newVersion");
        if(StringUtils.isNotBlank(params)) {
//            String projectPath = project.getLocation().toFile().getPath();
//            String rootProjectPath = getParentProject(projectPath, cmd);
            
            cmdBuilders.add(new CmdBuilder(tempFolder, CHANGEVERSION_BAT, params));
            if (cmdBuilders != null && !cmdBuilders.isEmpty()) {
                runJob(name, cmdBuilders);
            } else {
//                MessageDialog.openError(shell, name, "No project or pakcage selected.");
                throw new Exception("No project or package selected.");
            }
        }
    }

    private void release(ExecutionEvent event, String name) throws Exception {
        List<CmdBuilder> cmdBuilders = Lists.newLinkedList();
        
        String tempFolder = System.getenv("TEMP");
        String rootProjectPath = processRleaseScript(tempFolder);
        
        String pomVersion = getPomVersion(rootProjectPath);
        String defaultValue = pomVersion.replace("-SNAPSHOT", "")+".release test";
        
        String params = input(event, name, defaultValue, "BranchVersion test|release");
        if(StringUtils.isNotBlank(params)) {
//            String projectPath = project.getLocation().toFile().getPath();
//            String rootProjectPath = getParentProject(projectPath, cmd);
            
            cmdBuilders.add(new CmdBuilder(tempFolder, RELEASE_BAT, params));
            if (cmdBuilders != null && !cmdBuilders.isEmpty()) {
                runJob(name, cmdBuilders);
            } else {
//                MessageDialog.openError(shell, name, "No project or pakcage selected.");
                throw new Exception("No project or package selected.");
            }
        }
    }

    protected void runJob(final String name, List<CmdBuilder> cmdBuilders) throws CoreException {
//        boolean isConfirm = MessageDialog.openConfirm(shell, "process confirm?", project.getName() + " process confirm?");

//        if (isConfirm) {

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
//        }
    }
}
