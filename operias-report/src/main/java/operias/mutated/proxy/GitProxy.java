package operias.mutated.proxy;

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
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
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
import operias.mutated.CommitFileLibrary;

public class GitProxy {

	
	private static ArrayList<String> commitsID;
	private static Map<String,String> previousCommit;
	private static Git git;
	private static ThirdPartyProxySeetings settings;
	private static String pathToTmpFolder;
	private static String groupArtifactID;
	private static CommitFileLibrary commitFileLibrary;
	
	
	public GitProxy(int delta,ThirdPartyProxySeetings settings){
		this.settings=settings;
		setFolder();
		cloneGitRepo();
		setUpCommitLibrary(delta);
		if(buildRepoProject()){
			setHead();
		}
	}
	
	
	public static String getGroupArtifactPath(){
		return groupArtifactID;
	}
	
	public static String getPreviousCommitOf(String commitID){
		return previousCommit.get(commitID);
	}
	
	public static ArrayList<String> getFilteredCommits(){
		return commitFileLibrary.getPrefilteredCommitList();
	}
	
	
	public static boolean setFolder(){
		// prepare a new folder for the cloned repository
		try {
			settings.pomPath = File.createTempFile("TestGitRepository", "");
			 if(!settings.pomPath.delete()) {
			        throw new IOException("Could not delete temporary file " + settings.pomPath);
			    }
			 Main.printLine("[OPi+][INFO] Git Repo Processing: created temporary folder at "+ settings.pomPath.getAbsolutePath());
			 
		} catch (IOException e) {
			Main.printLine("[OPi+][Error] Git Repo Processing: could not create temporary folder");
			e.printStackTrace();
		}
		return true;
	}
	
	
	private static boolean cloneGitRepo(){
		try {
			 // GIT clone
			Main.printLine("[OPi+][INFO] Git Repo Processing: cloning from " + settings.REMOTE_URL);
			git = Git.cloneRepository()
			        .setURI(settings.REMOTE_URL)
			        .setDirectory(settings.pomPath)
			        .call();
		} catch (InvalidRemoteException e) {
			Main.printLine("[OPi+][ERROR] Git Repo Processing: could not clone");
			e.printStackTrace();
		} catch (TransportException e) {
			Main.printLine("[OPi+][ERROR] Git Repo Processing: could not clone");
			e.printStackTrace();
		} catch (GitAPIException e) {
			Main.printLine("[OPi+][ERROR] Git Repo Processing: could not clone");
			e.printStackTrace();
		}
		
		pathToTmpFolder =  settings.pomPath.getAbsolutePath();
		Main.printLine("[OPi+][INFO] Git Repo Processing: successfully cloned from " + settings.REMOTE_URL + " to " + settings.pomPath);
		return true;
	}
	
	
	
	
	public static void setUpCommitLibrary(int COMMIT_MUTATION_CHANGE_LOWER_LIMIT){
		commitFileLibrary = new CommitFileLibrary(COMMIT_MUTATION_CHANGE_LOWER_LIMIT);	
		Iterable<RevCommit> history;
        commitsID = new ArrayList<String>();
        previousCommit = new HashMap<String,String>();
		try {
			//get all comits
			history = git.log().call();
			 for (RevCommit commit : history) {
				 //if this is not the first commit
				 if(commit.getParentCount()>0){
						 processCommitToLibrary(git.getRepository(),commit,true);
						 commitsID.add(commit.getName());
						 previousCommit.put(commit.getName(),commit.getParent(0).getName());
				}else{
					Main.printLine("[OPi+][INFO] Git Repo Processing: this is the first commit in history. Therefore the entire text is a new change. This particular commit we ignore");
				 } 
		    }
			 Main.printLine("[OPi+][INFO] Git Repo Processing: set up commit library containing all file changes type and created lists with previous commitID info");
		} catch (GitAPIException e) {
			Main.printLine("[OPi+][Error] Git Repo Processing: could not retrieve repository history");
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
	
	
	
	
	public static boolean buildRepoProject() {
		   
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
				//we cant run pitest on this version since it does not build. we only build the system one time at the beginning
				System.exit(1);
			}
		} catch (IOException|InterruptedException e) {
			Main.printLine("[OPi+][Error] Git Repo Processing: could not build repository project");
			e.printStackTrace();
			return false;
		}
		return false;
	}


	public static boolean hasCommits(Repository repository) {
		if (repository != null && repository.getDirectory().exists()) {
			return (new File(repository.getDirectory(), "objects").list().length > 2)
					|| (new File(repository.getDirectory(), "objects/pack").list().length > 0);
		}
		return false;
	}
	
	
	
	
	public static void processCommitToLibrary(Repository repository, RevCommit commit, boolean calculateDiffStat) {
		if (hasCommits(repository)) {
			
		RevWalk rw = new RevWalk(repository, 0);
		try {
			if (commit == null) {
				ObjectId object = repository.resolve(Constants.HEAD);
				commit = rw.parseCommit(object);
			}
			/*
			//this is the first commit in history. this case should be ignored by previous logic
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
					list.add(new CommitFile(tw.getPathString()));
				}
				tw.close();
			} else {
			*/
				RevCommit parent = rw.parseCommit(commit.getParent(0).getId());
				DiffFormatter df = new DiffFormatter(null); //commit.getName(), repository
				df.setRepository(repository);
				df.setDiffComparator(RawTextComparator.DEFAULT);
				df.setDetectRenames(true);
				List<DiffEntry> diffs = df.scan(parent.getTree(), commit.getTree());
				for (DiffEntry diff : diffs) {
					// a diff represents a file that was changed within the current commit
					String filePath;
					ChangeType type  = diff.getChangeType();
					if( type.compareTo(ChangeType.DELETE) != 0){
						filePath = diff.getNewPath();
						commitFileLibrary.addCommitedFileToLibrary(commit.getName(), filePath, diff.getChangeType().toString());
					}
					
				}
		} catch (Throwable t) {
			Main.printLine("[OPi+][ERROR] preprocesing commit: failed to determine files in commit!");
			t.printStackTrace();
		} 
	}}
	
	
	
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

	
	/*
	 * need to add gitective project to buildpath
	 * BUT add the local version since it had outdated method calls
	private static void gitectiveOnCommit(RevCommit commit, RevCommit previous) {
			
		CommitListFilter list = new CommitListFilter();
		
		
		DiffFileCountFilter diffFileCountFilter = new DiffFileCountFilter();
		
		CommitFileImpactFilter commitFileIMpactFilter = new CommitFileImpactFilter(123);
		
		//includes commits that introduced a minimum number of line differences
		DiffLineSizeFilter difLineSizeFilter = new DiffLineSizeFilter(10);
	
		AndCommitFilter filters = new AndCommitFilter(difLineSizeFilter,list);
		
		
		CommitFinder finder = new CommitFinder(git.getRepository());
		finder.setFilter(new AllCommitFilter(filters));
		finder.setFilter(PathFilterUtils.andSuffix(".java"));
		finder.setFilter(PathFilterUtils.and("src/main/java"));
		//finder.findBetween(commit, previous);
		finder.find();
		
		

		System.out.println(list.getCommits().size());
		System.out.println(list.getCommits().toString());
		
		
		
		System.out.println("added "+diffFileCountFilter.getAdded());
		System.out.println("copied "+diffFileCountFilter.getCopied());
		System.out.println("deleted "+diffFileCountFilter.getDeleted());
		System.out.println("edited "+diffFileCountFilter.getEdited());
		System.out.println("renamed "+diffFileCountFilter.getRenamed());
		System.out.println("total "+diffFileCountFilter.getTotal());
		System.out.println(diffFileCountFilter.toString());
		
		
	}
*/
	
	
}
