/*
 * 描述： <描述>
 * 修改人： Dave.zhao
 * 修改时间： 2018年1月2日
 * 项目： com.aeasycredit.deployPlugin
 */
package com.aeasycredit.deployplugin.exception;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;

/**
 * Exceptionhelper
 * 
 * @author Dave.zhao
 * @version [版本号, 2018年1月2日]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public class Exceptionhelper {
    
    public static MultiStatus createMultiStatus(String msg, Throwable t) {

        List<Status> childStatuses = new ArrayList<>();
        StackTraceElement[] stackTraces = Thread.currentThread().getStackTrace();

        for (StackTraceElement stackTrace: stackTraces) {
            Status status = new Status(IStatus.ERROR,
                    "com.example.e4.rcp.todo", stackTrace.toString());
            childStatuses.add(status);
        }

        MultiStatus ms = new MultiStatus("com.example.e4.rcp.todo",
                IStatus.ERROR, childStatuses.toArray(new Status[] {}),
                t.toString(), t);
        return ms;
    }
    
}
