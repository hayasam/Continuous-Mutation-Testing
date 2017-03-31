package mutation.testing;


import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;

import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.Invoker;
import org.codehaus.plexus.util.cli.Commandline;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;

public class GitRunner {

	private static final String REMOTE_URL = "https://github.com/ileontiuc/testSettings.git";

	public static void main(String[] args) throws Exception {
			
			
		//  https://github.com/ileontiuc/testSettings.git
		
		// prepare a new folder for the cloned repository
	    File localPath = File.createTempFile("TestGitRepository", "");
	    System.out.println(localPath);
	    
	    if(!localPath.delete()) {
	        throw new IOException("Could not delete temporary file " + localPath);
	    }
	
	    // GIT clone
	    System.out.println("Cloning from " + REMOTE_URL + " to " + localPath);
	    Git git = Git.cloneRepository()
	            .setURI(REMOTE_URL)
	            .setDirectory(localPath)
	            .call();
	        // Note: the call() returns an opened repository already which needs to be closed to avoid file handle leaks!
	        System.out.println("Having repository: " + git.getRepository().getDirectory());
	        
	        
	        //GIT print commits
		    
            
	      //TODO filter the commits
            Iterable<RevCommit> history = git.log().call();
            ArrayList<String> commitsID = new ArrayList<String>();
            for (RevCommit commit : history) {
            	commitsID.add(commit.getName());
            }

                        
            Ref head = git.getRepository().findRef("HEAD");
            String headID = head.getObjectId().getName();
            System.out.println("Current real head: "+headID);          
            
            
            Process mvnCleanPackageProcess = Runtime.getRuntime().exec("mvn clean package -f "+localPath);
            //printConsole(mvnCleanPackageProcess);
            
	        
	        if(mvnCleanPackageProcess.waitFor()==0){
	        	
	        	System.out.println("mvn thread finished");
	        	
	        	String commitID = "9d421bac8afe604c5d02bb8d14cc895b51534719";
		        //git.reset().setMode(ResetCommand.ResetType.HARD).setRef(commitID).call();
		         
		        Ref newHead = git.getRepository().findRef("HEAD");
		        String newHeadID = newHead.getObjectId().getName();
	            System.out.println("Switched HEAD to: "+newHeadID);
	            
	            //-------------------------TO FIX -----------------------------------------------------------
	            System.out.println("my project path: "+localPath.getAbsolutePath());
	            
	            InvocationRequest request = new DefaultInvocationRequest();
	            request.setPomFile( new File( localPath.getAbsolutePath()+"/pom.xml" ) );
	            request.setGoals( Collections.singletonList( "org.pitest:pitest-maven:scmMutationCoverage -DanalyseLastCommit" ) );
	            
	            
	            //File myFile = new File("/usr/share/maven");
	            //System.out.println("my path is   "+myFile.getAbsolutePath());
	            
	             
	            Invoker invoker = new DefaultInvoker();	 
	            invoker.setMavenHome(new File("/usr/share/maven"));
	            System.out.println("Maven home: "+"/usr/share/maven");
	            
	            
	            InvocationResult result = invoker.execute( request );
	            
	            if ( result.getExitCode() != 0 )
	            {
	                throw new IllegalStateException( "Build failed." );
	            }else{ }
	            
	            
	            
	            
	            //-------------------------------------------------------------------------------------------
	            
	            
	            
	        }else{
	        	System.out.println("build failed");
	        }
    	
	    
	}
	
	
	
	
	private static void printConsole(Process process) throws IOException{
		//print console output
        String line;
        BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
        while ((line = input.readLine()) != null) {
          System.out.println(line);
        }
        input.close();
	}
	
	
}


/*
 * 
 * 
 * 
 * Collection<Ref> refs = git.lsRemote().call();
            for (Ref ref : refs) {
                System.out.println("Ref: " + ref+"   "+ref.getObjectId().getName());
            }
  // heads only
            refs = git.lsRemote().setHeads(true).call();
            String anotherCommit = null;
            for (Ref ref : refs) {
                System.out.println("Head: " + ref+"   "+ref.getObjectId().getName());
                anotherCommit = ref.getObjectId().getName();
            }

            // tags only
            refs = git.lsRemote().setTags(true).call();
            for (Ref ref : refs) {
            	
                System.out.println("Remote tag: " + ref);
            }
 * */
