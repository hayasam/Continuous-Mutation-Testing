package mutation.testing;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.DepthWalk.RevWalk;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.TreeWalk;

import operias.Main;

public class GitProxy {

	
	public static ArrayList<String> commitsID;
	public static Map<String,String> previousCommit;
	public static Git git;
	public static ThirdPartyProxySeetings settings;
	public static String pathToTmpFolder;
	
	
	
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
			 Main.printLine("[OPi+][INFO] Git Repo Processing: created temporary folder at "+ settings.pomPath.getAbsolutePath());
			pathToTmpFolder =  settings.pomPath.getAbsolutePath();
			
		    // GIT clone
			Main.printLine("[OPi+][INFO] Git Repo Processing: cloning from " + settings.REMOTE_URL);
		   
			try {
				git = Git.cloneRepository()
				        .setURI(settings.REMOTE_URL)
				        .setDirectory(settings.pomPath)
				        .call();
				
				Main.printLine("[OPi+][INFO] Git Repo Processing: successfully cloned from " + settings.REMOTE_URL + " to " + settings.pomPath);
				
				if(buildRepoProject()){
					setCommitsToProcess();
					setHead();
				}
				
			} catch (GitAPIException e) {
				Main.printLine("[OPi+][ERROR] Git Repo Processing: could not clone");
				e.printStackTrace();
			}
		} catch (IOException e) {
			Main.printLine("[OPi+][Error] Git Repo Processing: could not create temporary folder");
			e.printStackTrace();
		}
	}
	
	public static void changeHeadTo(String commitID) {
		try {
			git.reset().setMode(ResetCommand.ResetType.HARD).setRef(commitID).call();
			Ref newHead = git.getRepository().findRef("HEAD");
			String newHeadID = newHead.getObjectId().getName();
			Main.printLine("[OPi+][INFO] Git Repo Processing: succesfully changed repo HEAD to "+newHeadID);	
		} catch (GitAPIException|IOException e) {
			Main.printLine("[OPi+][Error] Git Repo Processing: could not set as HEAD the commit with ID: "+commitID);
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
				    	
				Main.printLine("[OPi+][INFO] Git Repo Processing: successfully build the project");
				return true;
			}else{
				Main.printLine("[OPi+][Error] Git Repo Processing: could not build repository project");
				//TODO should the entire process stop??? we cant run pitest on this version anyway
			}
		} catch (IOException|InterruptedException e) {
			Main.printLine("[OPi+][Error] Git Repo Processing: could not build repository project");
			e.printStackTrace();
			return false;
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
					 if(commit.getName().equals("0d237147620f1484831ab12cc87a7f242cd22b85")|| commit.getName().equals("ee61cea19f2c1b7b2d9ff4fac9c36ebbc7a7061c")){ //
							
					 //if(commitPassOPiPreFilter(commit)){
						 commitsID.add(commit.getName());
						 previousCommit.put(commit.getName(),commit.getParent(0).getName());
					 } 
				}else{
					Main.printLine("[OPi+][INFO] Git Repo Processing: this is the first commit in history. Therefore the entire text is a new change. This particular commit we ignore");
				 }
				 
		        
		    }
			 Main.printLine("[OPi+][INFO] commits that are selected from this project are: "+commitsID.size());
		    
		} catch (GitAPIException e) {
			Main.printLine("[OPi+][Error] Git Repo Processing: could not retrieve repository history");
			e.printStackTrace();
		}
	}
	
	
	/*preOPi+ filter 
	 * 
	 * current options: change must be made in the code = change made in src/main/java folder
	 * */
	private static boolean commitPassOPiPreFilter(RevCommit commit) {
		
		List<String> filesChangedInCurrentCommit = getFilesInCommit(git.getRepository(),commit,true);
		for(String currentFilePath : filesChangedInCurrentCommit)
			if(currentFilePath.contains("src/main/java"))
				return true;
		return false;
	}


	public static boolean hasCommits(Repository repository) {
		if (repository != null && repository.getDirectory().exists()) {
			return (new File(repository.getDirectory(), "objects").list().length > 2)
					|| (new File(repository.getDirectory(), "objects/pack").list().length > 0);
		}
		return false;
	}
	
	public static List<String> getFilesInCommit(Repository repository, RevCommit commit, boolean calculateDiffStat) {
		List<String> list = new ArrayList<String>();
		if (!hasCommits(repository)) {
			return list;
		}
		RevWalk rw = new RevWalk(repository, 0);
		try {
			if (commit == null) {
				ObjectId object = repository.resolve(Constants.HEAD);
				commit = rw.parseCommit(object);
			}

			if (commit.getParentCount() == 0) {
				TreeWalk tw = new TreeWalk(repository);
				tw.reset();
				tw.setRecursive(true);
				tw.addTree(commit.getTree());
				while (tw.next()) {
					long size = 0;
					ObjectId objectId = tw.getObjectId(0);
					
					try {
						if (!tw.isSubtree() && (tw.getFileMode(0) != FileMode.GITLINK)) {
							size = tw.getObjectReader().getObjectSize(objectId, Constants.OBJ_BLOB);
						}
					} catch (Throwable t) {
						Main.printLine("[OPi+][ERROR] failed to retrieve blob size for " + tw.getPathString());
					}			
					list.add(tw.getPathString());
				}
				tw.close();
			} else {
				RevCommit parent = rw.parseCommit(commit.getParent(0).getId());
				DiffFormatter df = new DiffFormatter(null); //commit.getName(), repository
				df.setRepository(repository);
				df.setDiffComparator(RawTextComparator.DEFAULT);
				df.setDetectRenames(true);
				List<DiffEntry> diffs = df.scan(parent.getTree(), commit.getTree());
				for (DiffEntry diff : diffs) {
					// create the path change model
					list.add(diff.getNewPath());
					}
				}
			
		} catch (Throwable t) {
			Main.printLine("[OPi+][ERROR] preprocesing commint: failed to determine files in commit!");
		} 
		return list;
	}
	
	
	
	private static void setHead(){
		 Ref head;
			try {
				head = git.getRepository().findRef("HEAD");
				settings.headID = head.getObjectId().getName();  
				Main.printLine("[OPi+][INFO] Git Repo Processing: archive real HEAD of project");
			} catch (IOException e) {
				Main.printLine("[OPi+][WARNING] Git Repo Processing: could not retrieve repository HEAD");
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

	public static void deleteTempFolder() {
		try {
			FileUtils.deleteDirectory(new File(pathToTmpFolder));
		} catch (IOException e) {
			Main.printLine("[OPi+][WARNING] Could not delete temporary folder at: "+GitProxy.pathToTmpFolder);
			e.printStackTrace();
		}
		
		
	}

	
	
}
