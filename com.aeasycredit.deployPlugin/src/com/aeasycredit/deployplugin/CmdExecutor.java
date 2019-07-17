package com.aeasycredit.deployplugin;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.LogOutputStream;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.lang.SystemUtils;

import com.aeasycredit.deployplugin.utils.FileHandlerUtils;

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
public class CmdExecutor {

    /** 
     * <功能简述><br>
     * <功能详细描述>
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
    public static String exec(String workHome, String command, String params, boolean isBatchCommand) throws Exception {
//        CommandLine cmdLine = CommandLine.parse("cmd.exe /C "+command +" "+ params);
//        cmd.exe /c ""D:\Developer\Git\bin\sh.exe" --login -i -c "wget http://gitlab.aeasycredit.net/dave.zhao/codecheck/raw/master/scripts/merge.sh""
//        String shell = "cmd.exe /c \"\"%GIT_HOME%\\bin\\sh.exe\" --login -i -- "+command+" "+params+"\"";
        String shell = "";
        if(SystemUtils.IS_OS_WINDOWS) {
            shell = "\""+FileHandlerUtils.getGitHome()+"\\bin\\bash.exe\" --login -i -c \""+(isBatchCommand?"":"bash")+" "+command+" "+params+"\"";
        } else {
            shell = ""+(isBatchCommand?"":"bash")+" "+command+" "+params;
        }
        CommandLine cmdLine = CommandLine.parse(shell);
        Executor executor = new DefaultExecutor();
        executor.setWorkingDirectory(new File(workHome));
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ByteArrayOutputStream errorStream = new ByteArrayOutputStream();
        PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream,errorStream);
        executor.setStreamHandler(streamHandler);
        executor.execute(cmdLine);
        String out = outputStream.toString("utf-8");
//        String error = errorStream.toString("utf-8");
        return out;
    }
    
    
    public static void main(String[] args) throws IOException, InterruptedException {
        String line = "git ls-remote ; git branch";
        
        CommandLine cmdLine = CommandLine.parse(line);
        Executor executor = new DefaultExecutor();
        
        executor.setStreamHandler(new PumpStreamHandler(new LogOutputStream() {

            @Override
            protected void processLine(String line, int level) {
                System.out.println(line);
            }
        }));
        
        DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();
        
        executor.setWorkingDirectory(new File("/Developer/workspace/gittest"));
        executor.execute(cmdLine, resultHandler);
        System.out.println("exitValue===");
        resultHandler.waitFor();
        
        
    }
}
