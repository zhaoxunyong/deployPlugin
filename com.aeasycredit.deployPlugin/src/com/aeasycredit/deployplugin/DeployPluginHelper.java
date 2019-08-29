package com.aeasycredit.deployplugin;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.LogOutputStream;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

import com.aeasycredit.deployplugin.utils.FileHandlerUtils;
import com.google.common.base.Joiner;

/**
 * DeployPluginHelper
 * 
 * <p>
 * <a href="PluginUtils.java"><i>View Source</i></a>
 * </p>
 * 
 * @author zhaoxunyong
 * @version 3.0
 * @since 1.0
 */
public class DeployPluginHelper {
    public final static String PLUGIN_ID = "com.aeasycredit.DeployPlugin";
    private final static String DEPLOY_PLUGIN = "DeployPlugin";
    
//    public static void success(Shell shell, String context){
//        MessageDialog.openInformation(shell, SLICE_PLUGIN, context);
//    }
//    
//    public static void error(Shell shell, String name, String context){
//        MessageDialog.openError(shell, name, context);
//    }

    private static MessageConsoleStream createConsole(String consoleName, boolean clean) {
        MessageConsole console = findConsole(consoleName);
        MessageConsoleStream cs = console.newMessageStream();
        cs.setEncoding("utf-8");
        cs.setColor(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
        if(clean){
            console.clearConsole();
        }
        console.activate();
        return cs;
    }

    private static MessageConsole findConsole(String name) {
        ConsolePlugin plugin = ConsolePlugin.getDefault();
        IConsoleManager conMan = plugin.getConsoleManager();
        IConsole[] existing = conMan.getConsoles();
        for (int i = 0; i < existing.length; i++) {
            if (name.equals(existing[i].getName())) {
                return (MessageConsole) existing[i];
            }
        }
        // no console found -> create new one
        MessageConsole newConsole = new MessageConsole(name, null);
        conMan.addConsoles(new IConsole[]{ newConsole });
        return newConsole;
    }
    


    public static MessageConsole findConsole() {
        return findConsole(DEPLOY_PLUGIN);
    }
    
    public static MessageConsoleStream console(boolean clean){
        MessageConsoleStream console = createConsole(DEPLOY_PLUGIN, clean);
        return console;
    }
    

    public static String exec(String workHome, String command, List<String> params, boolean isBatchScript) throws InterruptedException, IOException {
        return exec(null, workHome, command, params, isBatchScript);
    }

    /** 
     * 只支持sh脚本执行，如果是具体的命令的话，请使用CmdExecutor类执行
     *
     * @param console
     * @param workHome
     * @param command
     * @param params
     * @return
     * @throws IOException
     * @throws InterruptedException
     * 
     * @return boolean [返回类型说明]
     * @throws [异常类型] [异常说明]
     * @see [类、类#方法、类#成员]
     * @version [版本号, 2018年5月3日]
     * @author Dave.zhao
     */
    public static String exec(final MessageConsoleStream console, String workHome, String command, List<String> parameters, boolean isBatchScript) throws IOException, InterruptedException {
//        CommandLine cmdLine = CommandLine.parse("cmd.exe /C "+command +" "+ params);
//        cmd.exe /c ""D:\Developer\Git\bin\sh.exe" --login -i -c "wget http://gitlab.aeasycredit.net/dave.zhao/codecheck/raw/master/scripts/merge.sh""
//        String shell = "cmd.exe /c \"\"%GIT_HOME%\\bin\\sh.exe\" --login -i -- "+command+" "+params+"\"";
    	boolean debug = DeployPluginLauncherPlugin.getGitBashDebug();
    	String debugStr = debug?"-x":"";
    	// console.setEncoding("utf-8");
    	/*String shell = "";
    	String params = Joiner.on(" ").join(parameters);
        if(SystemUtils.IS_OS_WINDOWS) {
            shell = "\""+FileHandlerUtils.getGitHome()+"\\bin\\bash.exe\" --login -i -c \""+(isBatchCommand?"":"bash "+debugStr)+" "+command+" "+params+"\"";
        } else {
            shell = ""+(isBatchCommand?"":"bash "+debugStr)+" "+command+" "+params;
        }*/
        
        CommandLine cmdLine = null;
        if(SystemUtils.IS_OS_WINDOWS) {
        	cmdLine = new CommandLine(FileHandlerUtils.getGitHome()+"\\bin\\bash.exe");
            if(StringUtils.isNotBlank(debugStr)) {
                cmdLine.addArgument(debugStr);
            }
            if(isBatchScript) {
        		cmdLine.addArgument(command);
                if(parameters!=null && !parameters.isEmpty()) {
                	for(String p : parameters) {
                		cmdLine.addArgument(p);
                	}
                }
            } else {
            	String params = Joiner.on(" ").join(parameters);
                cmdLine.addArgument("-c");
                cmdLine.addArgument("\""+command+" "+params+"\"");
            }
        } else {
        	if(isBatchScript) {
        		cmdLine = new CommandLine("bash");
                if(StringUtils.isNotBlank(debugStr)) {
                    cmdLine.addArgument(debugStr);
                }
                cmdLine.addArgument(command);
        	} else {
        		cmdLine = new CommandLine(command);
        	}
            if(parameters!=null && !parameters.isEmpty()) {
            	for(String p : parameters) {
            		cmdLine.addArgument(p);
            	}
            }
        }
        
        // CommandLine cmdLine = CommandLine.parse(shell);
        Executor executor = new DefaultExecutor();
        executor.setWorkingDirectory(new File(workHome));
        String out = "";
        if(console!=null) {
            executor.setStreamHandler(new PumpStreamHandler(new LogOutputStream() {

                @Override
                protected void processLine(String line, int level) {
                    console.println(line);
                }
            }, new LogOutputStream() {

                @Override
                protected void processLine(String line, int level) {
                    console.println(line);
                }
            }));
            
            int code = executor.execute(cmdLine);
            /*if(asyc){
                  DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();
                  executor.execute(cmdLine, resultHandler);
//                  resultHandler.waitFor();
                }*/
            out = String.valueOf(code);
            // return code == 0 ? true:false;
        } else {
        	// return the output
        	ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ByteArrayOutputStream errorStream = new ByteArrayOutputStream();
            PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream,errorStream);
            executor.setStreamHandler(streamHandler);
            executor.execute(cmdLine);
            out = outputStream.toString("utf-8");
        	
        }
        return out;
    }
    
    
    public static void main(String[] args) throws IOException, InterruptedException {
        String line = "cmd.exe /C deploy.bat";
        
        CommandLine cmdLine = CommandLine.parse(line);
        Executor executor = new DefaultExecutor();
        
        executor.setStreamHandler(new PumpStreamHandler(new LogOutputStream() {

            @Override
            protected void processLine(String line, int level) {
                System.out.println(line);
            }
        }));
        
        DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();
        
        executor.setWorkingDirectory(new File("D:\\works"));
        executor.execute(cmdLine, resultHandler);
        System.out.println("exitValue===");
        resultHandler.waitFor();
        
        
    }
}
