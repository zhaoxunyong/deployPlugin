package com.aeasycredit.deployplugin;

import java.io.File;
import java.util.Collection;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

// http://www.codeaffine.com/2015/11/30/jgit-clone-repository/
// http://www.codeaffine.com/2015/12/15/getting-started-with-jgit/
public class JGitTest {
    public static void main(String[] args) throws Exception {
        String username = "dave.zhao@zerofinance.cn";
        String password = "Aa654321";
        
//        FileRepositoryBuilder builder = new FileRepositoryBuilder();
//        Repository repository = builder.setGitDir(new File("/Developer/workspace/gittest"))
//          .readEnvironment() // scan environment GIT_* variables
//          .findGitDir() // scan up the file system tree
//          .build();
//        
//        if (!repository.getObjectDatabase().exists()) {
//            Git.cloneRepository()
//                    .setURI("http://gitlab.aeasycredit.net/dave.zhao/my-test")
//                    .setDirectory(new File("/Developer/workspace/gittest"))
//                    .setCloneAllBranches(true)
//                    .setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, password))
//                    .call();
//        }
        

        Git git = Git.open(new File("/Developer/workspace/config-server"));
        
//        git.pull()
//                .setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, password))
//                .call();
        
        Collection<Ref> refs = git.lsRemote()
        		.setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, password))
        		.call();
        for (Ref ref : refs) {
            System.out.println("ref--->" + ref.getName());
            
        }
        git.close();
    }
}
