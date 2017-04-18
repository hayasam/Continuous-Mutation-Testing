package mutation.testing;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;

import operias.Main;

public class PitestProxy {
	
	public static ThirdPartyProxySeetings settings;
	
	

		
	public static String getMutationReportFor(String commitID) {
		GitProxy.changeHeadTo(commitID);
		String mutationPath = null;
		
		//change running local path due to Pitest Bug: hcoles/pitest: Issue #336
        Invoker invoker = new DefaultInvoker();	 
        invoker.setMavenHome(new File(settings.MAVEN_PATH));
        Main.printLine("[OPi+][INFO] set maven home to: "+settings.MAVEN_PATH);
		
		//setup Pitest on last commit
		InvocationRequest request = new DefaultInvocationRequest();
        request.setPomFile( new File( settings.pomPath.getAbsolutePath()+"/pom.xml" ) );
        request.setGoals( Collections.singletonList( "org.pitest:pitest-maven:scmMutationCoverage -DanalyseLastCommit -DtargetTests="+GitProxy.groupArtifactID+" -Dmutators=ALL" ) );
        //mvn org.pitest:pitest-maven:scmMutationCoverage -DanalyseLastCommit -DtargetTests=groupID.artifactID.* -Dmutators=ALL
        
       
        //run Pitest on last commit
        InvocationResult result;
		try {
			result = invoker.execute( request );
			if ( result.getExitCode() != 0 )
	        {
	            throw new IllegalStateException( "Build failed." );
	        }else{
	        	Main.printLine("[OPi+][INFO] successfully run Pitest on last commit");
	        	mutationPath = settings.pomPath+"/target/pit-reports";
	        	mutationPath = getLatestFilefromDir(mutationPath).getAbsolutePath();
	        	
	        	System.out.println("[OPi+][INFO] My MUTATION REPORT path is: "+mutationPath+" for commit "+commitID);
	        }
		} catch (MavenInvocationException|IllegalStateException e) {
			Main.printLine("[OPi+][ERROR] could not run Pitest on last commit");
			e.printStackTrace();
		}
		return mutationPath;
		
	}
	
	
	//get latest created folder
	private static File getLatestFilefromDir(String dirPath){
	    File dir = new File(dirPath);
	    File[] files = dir.listFiles();
	    if (files == null || files.length == 0) {
	        return null;
	    }

	    File lastModifiedFile = files[0];
	    for (int i = 1; i < files.length; i++) {
	       if (lastModifiedFile.lastModified() < files[i].lastModified()) {
	           lastModifiedFile = files[i];
	       }
	    }
	    return lastModifiedFile;
	}


		
	
}
