package com.aeasycredit.deployplugin.handlers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
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
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Ref;
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

/**
 * AbstractDeployPluginHandler
 * 
 * <p>
 * <a href="AbstractDeployPluginHandler.java"><i>View Source</i></a>
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
    protected final static String MYBATISGEN_BAT = "./mybatisGen.sh";
    protected final static String NEWBRANCH_BAT = "./newBranch.sh";

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
    
    /*private void credentialHelper() throws Exception {
        String projectPath = project.getLocation().toFile().getPath();
//        String tempFile = getTempFolder();
//        File file = new File(tempFile+File.separator+UUID.randomUUID().toString());
//        FileUtils.writeStringToFile(file, data);
        try {
            DeployPluginHelper.exec(projectPath, "git ls-remote", "", true, true);
        } catch(Exception e) {
//            MessageDialog.openError(shell, "Credential Error", "Authentication error, please execute the following command through git bash:\n\ngit config --global credential.helper store\ngit ls-remote");
            throw new Exception("Authentication error, please execute the following command through git bash:\n\ngit config --global credential.helper store\ngit ls-remote");
        }
    }*/

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

    protected void newBranch(ExecutionEvent event) throws Exception {
//        credentialHelper();
        newBranch(event, "New Branch");
    }

    protected void release(ExecutionEvent event) throws Exception {
//        credentialHelper();
        release(event, "Release");
    }

    protected void merge(ExecutionEvent event) throws Exception {
//        credentialHelper();
//        throw new UnsupportedOperationException("Not implemented yet.");
        merge(event, "Merge");
    }

    protected void mybatisGen(ExecutionEvent event) throws Exception {
        mybatisGen(event, "Mybatis Gen");
    }

    private String getParentCmdFile(String projectPath, String cmd) throws IOException {
        if (!new File(projectPath + File.separator + cmd).exists()) {
            /*if(projectPath.indexOf(project.getName()) == -1) {
                // 不在当前项目中
                throw new FileNotFoundException(cmd.replace("./", "") + " not found.");
            }*/
            String parent = new File(projectPath).getParent();
            if(StringUtils.isBlank(parent)) {
                throw new FileNotFoundException(cmd.replace("./", "") + " not found.");
            }
            return getParentCmdFile(parent, cmd);
        }
        return projectPath+File.separator+cmd.replace("./", "");
    }
    
    private String getTempFolder() {
        if(SystemUtils.IS_OS_WINDOWS) {
            return System.getenv("TEMP");   
        } else {
            String tempFolder ="~/.eclipse";
            if(!new File(tempFolder).exists()) {
                new File(tempFolder).mkdirs();
            }
            return tempFolder;
        }
    }

    private String getCmdFile(String projectPath, String cmd) throws IOException {
        if(SystemUtils.IS_OS_WINDOWS) {
            String gitHome = System.getenv("GIT_HOME");
            if(StringUtils.isBlank(gitHome)) {
                throw new FileNotFoundException("GIT_HOME env must be not empty.");
            }
        }
        String allCmd = cmd.replace(".sh", "All.sh");
        String cmdFile = "";
        try {
            cmdFile = getParentCmdFile(projectPath, allCmd);
        } catch(IOException e) {
            cmdFile = getParentCmdFile(projectPath, cmd);
        }
        return cmdFile;
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
    
    private String processScript(String scriptName, String tempFolder) throws IOException {
        String projectPath = project.getLocation().toFile().getPath();
        String cmdFile = getCmdFile(projectPath, scriptName);
        String rootProjectPath = new File(cmdFile).getParent();
        String changVersionName = FilenameUtils.getName(cmdFile);
        if(SystemUtils.IS_OS_WINDOWS) {
            rootProjectPath = rootProjectPath.replace("\\", "\\\\");
        }
//        InputStream input = this.getClass().getResourceAsStream("/merge.sh");
//        String str = IOUtils.toString(input);
        String str = FileUtils.readFileToString(new File(rootProjectPath+File.separator+changVersionName), "UTF-8");
        String script = str.replace("#cd #{project}", "cd "+rootProjectPath);
        File file = new File(tempFolder+File.separator+changVersionName);
        FileUtils.writeStringToFile(file, script);
        return cmdFile;
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
    
    private String haSnapshotVersion(List<String> value, String var) {
        for(String v : value) {
            if(v.indexOf(var) !=-1 && v.indexOf("-SNAPSHOT") != -1) {
                return v.trim();
            }
        }
        return "";
    }
    
    private String checkHasSnapshotVersion(String rootProjectPath) throws IOException {
        File dir = new File(rootProjectPath);  
        Collection<File> files = FileUtils.listFiles(dir,  
                FileFilterUtils.nameFileFilter("pom.xml"),    
                DirectoryFileFilter.DIRECTORY);  
         for (File f : files) {    
             String pomFile = f.getPath();
             List<String> value = FileUtils.readLines(new File(pomFile));
             boolean isNew = false;
             for(String v : value) {
                 if(v.indexOf("<dependency>") !=-1) {
                     isNew = true;
                 } else if(v.indexOf("</dependency>") !=-1) {
                     isNew = false;
                 }
                 
                 if(isNew && v.indexOf("version")!=-1) {
                     // && v.indexOf("-SNAPSHOT") != -1
                     if(v.indexOf("${") != -1) {
                         String var = StringUtils.substringBetween(v, "${", "}");
                         String snapshotVar = haSnapshotVersion(value, var);
                         if(StringUtils.isNotBlank(snapshotVar)) {
                             return pomFile+"("+snapshotVar+")";
                         }
                     } else if(v.indexOf("-SNAPSHOT") != -1) {
                         return pomFile+"("+v.trim()+")";
                     }
                 }
             }    
         }   
         return "";
    }

    private void merge(ExecutionEvent event, String name) throws Exception {
        List<CmdBuilder> cmdBuilders = Lists.newLinkedList();
        String tempFolder = getTempFolder();
        String cmdFile = processScript(MERGE_BAT, tempFolder);
        String rootProjectPath = new File(cmdFile).getParent();
        String cmdName = FilenameUtils.getName(cmdFile);
        
        String pomVersion = getPomVersion(rootProjectPath);
        String defaultValue = "";
        
        // Get release version
        String releaseVersion = pomVersion.replace("-SNAPSHOT", "")+".release";
        
        // Check releaseVersion is exist
        File repositoryDirectory = new File(rootProjectPath);
        Git git = Git.open(repositoryDirectory);
        List<Ref> refs = git.branchList().call();
        for (Ref ref : refs) {
//            System.out.println("ref--->" + ref.getName());
            // ref.getName()--->refs/heads/1.7.x
          if(("refs/heads/"+releaseVersion).equals(ref.getName())) {
              defaultValue = releaseVersion+" master";
              break;
          }
        }
        
        if(StringUtils.isBlank(defaultValue)) {
//            throw new Exception(releaseVersion+" is not exist!");
            defaultValue = pomVersion+" master";
        }
        
        /*// Check releaseVersion is exist
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
        }*/
        
        String version = input(event, name, defaultValue, "branchFromVersion branchToVersion");
        if(StringUtils.isNotBlank(version)) {
            
            cmdBuilders.add(new CmdBuilder(tempFolder, cmdName, version));
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
        String tempFolder = getTempFolder();
        String cmdFile = processScript(CHANGEVERSION_BAT, tempFolder);
        String rootProjectPath = new File(cmdFile).getParent();
        String cmdName = FilenameUtils.getName(cmdFile);
        String pomVersion = getPomVersion(rootProjectPath);
        String bPomVersion = StringUtils.substringBeforeLast(pomVersion, ".");
        String aPomVersion = StringUtils.substringAfterLast(pomVersion, ".").replace("-SNAPSHOT", "");
        aPomVersion = String.valueOf(Integer.parseInt(aPomVersion)+1);
        String defaultValue = bPomVersion+"."+aPomVersion+"-SNAPSHOT";
        String params = input(event, name, defaultValue, "newVersion");
        if(StringUtils.isNotBlank(params)) {
//            String projectPath = project.getLocation().toFile().getPath();
//            String rootProjectPath = getParentProject(projectPath, cmd);
            
            cmdBuilders.add(new CmdBuilder(tempFolder, cmdName, params));
            if (cmdBuilders != null && !cmdBuilders.isEmpty()) {
                runJob(name, cmdBuilders);
            } else {
//                MessageDialog.openError(shell, name, "No project or pakcage selected.");
                throw new Exception("No project or package selected.");
            }
        }
    }

    private void newBranch(ExecutionEvent event, String name) throws Exception {
        List<CmdBuilder> cmdBuilders = Lists.newLinkedList();

        String tempFolder = getTempFolder();
        String cmdFile = processScript(NEWBRANCH_BAT, tempFolder);
        String rootProjectPath = new File(cmdFile).getParent();
        String cmdName = FilenameUtils.getName(cmdFile);
        String pomVersion = getPomVersion(rootProjectPath);
        // 1.5.6->1.6.x
        String bPomVersion = StringUtils.substringBeforeLast(pomVersion, "."); //1.5
        String a1PomVersion = StringUtils.substringBeforeLast(bPomVersion, "."); // 1
        String a2PomVersion = StringUtils.substringAfterLast(bPomVersion, "."); // 5
        a2PomVersion = String.valueOf(Integer.parseInt(a2PomVersion)+1);
        String defaultValue = a1PomVersion+"."+a2PomVersion+".x"; // 1.6.x
        String params = input(event, name, defaultValue, "newBranch");
        if(StringUtils.isNotBlank(params)) {
//            String projectPath = project.getLocation().toFile().getPath();
//            String rootProjectPath = getParentProject(projectPath, cmd);
            
            cmdBuilders.add(new CmdBuilder(tempFolder, cmdName, params));
            if (cmdBuilders != null && !cmdBuilders.isEmpty()) {
                runJob(name, cmdBuilders);
            } else {
//                MessageDialog.openError(shell, name, "No project or pakcage selected.");
                throw new Exception("No project or package selected.");
            }
        }
    }

    private void mybatisGen(ExecutionEvent event, String name) throws Exception {
        List<CmdBuilder> cmdBuilders = Lists.newLinkedList();
        
        String projectPath = project.getLocation().toFile().getPath();

        String cmdFile = getCmdFile(projectPath, MYBATISGEN_BAT);
        String rootProjectPath = new File(cmdFile).getParent();
        String cmdName = FilenameUtils.getName(cmdFile);
        if(SystemUtils.IS_OS_WINDOWS) {
            rootProjectPath = rootProjectPath.replace("\\", "\\\\");
        }
        
        cmdBuilders.add(new CmdBuilder(rootProjectPath, cmdName, ""));
        if (cmdBuilders != null && !cmdBuilders.isEmpty()) {
            boolean isConfirm = MessageDialog.openConfirm(shell, "Mybatis Gen Confirm?", project.getName() + " Mybatis Gen Confirm?");
            if(isConfirm) {
                runJob(name, cmdBuilders);
            }
        } else {
//            MessageDialog.openError(shell, name, "No project or pakcage selected.");
            throw new Exception("No project or package selected.");
        }
    }

    private void release(ExecutionEvent event, String name) throws Exception {
        List<CmdBuilder> cmdBuilders = Lists.newLinkedList();
        
        String tempFolder = getTempFolder();
        String cmdFile = processScript(RELEASE_BAT, tempFolder);
        String cmdName = FilenameUtils.getName(cmdFile);
        String rootProjectPath = new File(cmdFile).getParent();
        
        boolean continute = true;
        String snapshotPath = checkHasSnapshotVersion(rootProjectPath);
        if(StringUtils.isNotBlank(snapshotPath)) {
            continute = MessageDialog.openConfirm(shell, "process confirm?", "There is a SNAPSHOT version in "+snapshotPath+", when the version is released, it's suggested to replace it as release. Do you want to continue?");
        }
        
        if(continute) {
            String pomVersion = getPomVersion(rootProjectPath);
            String defaultValue = pomVersion.replace("-SNAPSHOT", "")+".release";
            
            String params = input(event, name, defaultValue, "BranchVersion TagDate");
            if(StringUtils.isNotBlank(params)) {
//                String projectPath = project.getLocation().toFile().getPath();
//                String rootProjectPath = getParentProject(projectPath, cmd);
                
                cmdBuilders.add(new CmdBuilder(tempFolder, cmdName, params));
                if (cmdBuilders != null && !cmdBuilders.isEmpty()) {
                    runJob(name, cmdBuilders);
                } else {
//                    MessageDialog.openError(shell, name, "No project or pakcage selected.");
                    throw new Exception("No project or package selected.");
                }
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
