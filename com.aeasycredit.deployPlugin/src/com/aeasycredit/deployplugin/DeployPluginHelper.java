/* 
 * Copyright (C), 2014-2016, 时代际客(深圳)软件有限公司
 * File Name: @(#)PluginUtils.java
 * Encoding UTF-8
 * Author: zhaoxunyong
 * Version: 3.0
 * Date: Feb 22, 2016
 */
package com.aeasycredit.deployplugin;

import java.io.File;
import java.io.IOException;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.LogOutputStream;
import org.apache.commons.exec.PumpStreamHandler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

/**
 * 功能描述
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
    

    public static boolean exec(String workHome, String command, String params, boolean asyc) throws InterruptedException, IOException {
        return exec(null, workHome, command, params, asyc);
    }

    public static boolean exec(final MessageConsoleStream console, String workHome, String command, String params, boolean asyc) throws IOException, InterruptedException {
//        CommandLine cmdLine = CommandLine.parse("cmd.exe /C "+command +" "+ params);
        String shell = "cmd.exe /c \"\"%GIT_HOME%\\bin\\sh.exe\" --login -i -- "+command+" "+params+"\"";
        CommandLine cmdLine = CommandLine.parse(shell);
        Executor executor = new DefaultExecutor();
        executor.setWorkingDirectory(new File(workHome));
        
        if(console!=null){
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
        }
        
        int code = executor.execute(cmdLine);
        if(asyc){
          DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();
          executor.execute(cmdLine, resultHandler);
//          resultHandler.waitFor();
        }
        return code == 0 ? true:false;
    }
    
    
    public static void main(String[] args) throws IOException, InterruptedException {
//        List<String> commands=new ArrayList();  
//        test("d:/eml/", "mkdir xxx", null);
//        test("D:\\Developer\\runtime-EclipseApplication\\test\\tools", "ice_php_all.bat", null);
        
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
        
        executor.setWorkingDirectory(new File("D:\\Developer\\workspace\\new\\datacenter\\datacenter\\datacenter-dataanalysis"));
        executor.execute(cmdLine, resultHandler);
        System.out.println("exitValue===");
        resultHandler.waitFor();
        
        
    }
}
