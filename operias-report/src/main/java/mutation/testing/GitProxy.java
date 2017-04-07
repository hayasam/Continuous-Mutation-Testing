package mutation.testing;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;

import operias.Main;

public class GitProxy {

	
	public static ArrayList<String> commitsID;
	public static Map<String,String> previousCommit;
	public static Git git;
	public static ThirdPartyProxySeetings settings;
	
	
	
	
	public static ArrayList<String> getFilteredCommits(){
		runRepoProcessing();
		return commitsID;
		
	}
	
	private static void runRepoProcessing(){
		
		// prepare a new folder for the cloned repository
	    
		try {
					
			settings.pomPath = File.createTempFile("TestGitRepository", "");
			 if(!settings.pomPath.delete()) {
			        throw new IOException("Could not delete temporary file " + settings.pomPath);
			    }
			System.out.println("[OPi+][INFO] Git Repo Processing: created temporary folder at "+ settings.pomPath.getAbsolutePath());
			
			
		    // GIT clone
			System.out.println("[OPi+][INFO] Git Repo Processing: cloning from " + settings.REMOTE_URL);
		   
			try {
				git = Git.cloneRepository()
				        .setURI(settings.REMOTE_URL)
				        .setDirectory(settings.pomPath)
				        .call();
				
				System.out.println("[OPi+][INFO] Git Repo Processing: successfully cloned from " + settings.REMOTE_URL + " to " + settings.pomPath);
				
				if(buildRepoProject()){
					setCommitsToProcess();
					setHead();
				}
				
			} catch (GitAPIException e) {
				System.out.println("[OPi+][ERROR] Git Repo Processing: could not clone");
				e.printStackTrace();
			}
		} catch (IOException e) {
			System.out.println("[OPi+][Error] Git Repo Processing: could not create temporary folder");
			e.printStackTrace();
		}
	}
	
	public static void changeHeadTo(String commitID) {
		try {
			git.reset().setMode(ResetCommand.ResetType.HARD).setRef(commitID).call();
			Ref newHead = git.getRepository().findRef("HEAD");
			String newHeadID = newHead.getObjectId().getName();
			System.out.println("[OPi+][INFO] Git Repo Processing: succesfully changed repo HEAD to "+newHeadID);	
		} catch (GitAPIException|IOException e) {
			System.out.println("[OPi+][Error] Git Repo Processing: could not set as HEAD the commit with ID: "+commitID);
			e.printStackTrace();
		}
		
	}

	public static String getMAVEN_PATH(){
		return settings.MAVEN_PATH;
	}
	
	private static boolean buildRepoProject() {
		   //TODO
        /*
         It is expected behaviour for no mutants to be found if you clone a project and directly run a mutation coverage goal. 
			The project has to be built first. Normally you would bind the goal to a phase within the pom so that this happens 
			automatically, but if you're running the goal manually you'll need to do an `mvn test` (or package as you are doing) first. ~Henry
         */
        Process mvnCleanPackageProcess;
		try {
			mvnCleanPackageProcess = Runtime.getRuntime().exec("mvn clean package -f "+settings.pomPath);
			if(mvnCleanPackageProcess.waitFor()==0){
				    	
				System.out.println("[OPi+][INFO] Git Repo Processing: successfully build the project");
				return true;
			}
		} catch (IOException|InterruptedException e) {
			System.out.println("[OPi+][Error] Git Repo Processing: could not build repository project");
			e.printStackTrace();
		}
		return false;
	}

	private static void setCommitsToProcess() {
		//TODO filter the commits
        Iterable<RevCommit> history;
        //TODO make classes not static with proper constructor
        commitsID = new ArrayList<String>();
        previousCommit = new HashMap<String,String>();
		try {
			
			history = git.log().call();
			
			 for (RevCommit commit : history) {
				 if(commit.getParentCount()>0){
					 if(commit.getName().equals("ee61cea19f2c1b7b2d9ff4fac9c36ebbc7a7061c")){
						 commitsID.add(commit.getName());
						 previousCommit.put(commit.getName(),commit.getParent(0).getName());
					 }
				 }else{
					 System.out.println("[OPi+][INFO] Git Repo Processing: this is the first commit in history. Therefore the entire text is a new change. This particular commit we ignore");
				 }
				 
		        
		    }
		    System.out.println("[OPi+][INFO] commits that are selected from this project are: "+commitsID.size());
		    
		} catch (GitAPIException e) {
			System.out.println("[OPi+][Error] Git Repo Processing: could not retrieve repository history");
			e.printStackTrace();
		}
	}
	
	
	private static void setHead(){
		 Ref head;
			try {
				head = git.getRepository().findRef("HEAD");
				settings.headID = head.getObjectId().getName();  
				System.out.println("[OPi+][INFO] Git Repo Processing: archive real HEAD of project");
			} catch (IOException e) {
				System.out.println("[OPi+][WARNING] Git Repo Processing: could not retrieve repository HEAD");
				e.printStackTrace();
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
