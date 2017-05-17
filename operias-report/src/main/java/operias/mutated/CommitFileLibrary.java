package operias.mutated;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import operias.Main;
import operias.mutated.record.files.EvaluationDataFile;

public class CommitFileLibrary{

	public static Map<String, List<CommitFile>> commitFileLibrary;  //commitID, commited files
	public static Map<String, Integer> commitImpact; 
	private int COMMIT_MUTATION_CHANGE_LOWER_LIMIT;
	public static ArrayList<String> prefilteredCommitID ;
	
	public CommitFileLibrary(int delta){
		commitImpact = new HashMap<String, Integer>();
		commitFileLibrary = new HashMap<String, List<CommitFile>>();
		prefilteredCommitID = new ArrayList<String>();
		COMMIT_MUTATION_CHANGE_LOWER_LIMIT=delta;
	}
	
	
	public static void addImpact(String comitID, int impact){
		commitImpact.put(comitID, impact);
	}
	
	public ArrayList<String> getPrefilteredCommitList(){
		boolean flag = true;
		int counter =0;
		
		if(prefilteredCommitID.isEmpty()){
			for(String commitID: commitFileLibrary.keySet()){
				if(OPiPrefilter(commitFileLibrary.get(commitID),commitID)){
					
					//if(counter>10)
					//	flag = false;
					
					//TODO this commit seems to block the process
					//if(!commitID.equals("5c43ce2d83e44cd46049a7f0bfaeb5d9a322c032")&& !commitID.equals("472c7e55ee9d5cc7e7771696092c6eeca5a91b4d") && 
						//	!commitID.equals("561d12a9894210d9f5a8a41d655361732a39b6c8") && !commitID.equals("1e85878fe008f6c651c348dd1d5b2edf713f63bb") && 
							//!commitID.equals("a97385eb2870d113427b0d9430e108236ae363b4") && !commitID.equals("002a4940935a7545ed94f1b776e4d7f6bf6c2525") 
							//&& flag){
						prefilteredCommitID.add(commitID);
						//counter++;
					//}
				}
			}
			
					
			//record project and process data
			int totalNumberCommits = commitFileLibrary.size();
			int prefilterdCommits = prefilteredCommitID.size();
			float percentajePrefiltered = prefilterdCommits*100/totalNumberCommits;
			EvaluationDataFile.write("Total commit number in system: "+totalNumberCommits);
			EvaluationDataFile.write("Total commits analyzed by OPi+: "+prefilterdCommits+" which is "+percentajePrefiltered+"%");
			EvaluationDataFile.write("Total commits filtered out by OPi+: "+(totalNumberCommits-prefilterdCommits)+" which is "+(100-percentajePrefiltered)+"%");
			
			Main.printLine("[OPi+][INFO] total number of commits for this system: "+commitFileLibrary.size());
			Main.printLine("[OPi+][INFO] Total commits analyzed by OPi+: "+prefilterdCommits+" which is "+percentajePrefiltered+"%");
			Main.printLine("Total commits filtered out by OPi+: "+(totalNumberCommits-prefilterdCommits)+" which is "+(100-percentajePrefiltered)+"%");
			
		}
		return prefilteredCommitID;
	}
	
	
	private boolean OPiPrefilter(List<CommitFile> list,String commitID) {
		int counter =0;
		for(CommitFile currentCommitFile: list){
			//at least one file in the commit should have correct path
			if(currentCommitFile.getFilePath().contains(".java") && 
					currentCommitFile.getFilePath().contains("src/main/java") &&
					currentCommitFile.getMainChangeType().matches("ADD|MODIFY|RENAME")){
			   counter++;
			}
		}
		//TODO changed lower limit logic
		if(counter==COMMIT_MUTATION_CHANGE_LOWER_LIMIT){
			return true;
		}
		return false;
	}


	public Map<String, List<CommitFile>> getLibrary(){
		return commitFileLibrary;
	}
	
	public int getTotalChangesFor(String commitID, String filePath){
		CommitFile currentCommitFile = getCommitFile(commitID, filePath);
		return currentCommitFile.getTotalNumberOfFilesChanged();
	}
	
	public int getTotalMutationRelevantChangesFor(String commitID, String filePath){
		CommitFile currentCommitFile = getCommitFile(commitID, filePath);
		return currentCommitFile.getTotalMutationRelevantFilesChanged();
	}
	
	
	private CommitFile getCommitFile(String commitID, String filePath){
		List<CommitFile> commitListForCurrentCommit = commitFileLibrary.get(commitID);
		int index = commitListForCurrentCommit.indexOf(filePath);
		return commitListForCurrentCommit.get(index);	
	}
	
	
	private int findIndexOfIn(String path, List<CommitFile> fileListForCurrentCommit){
		for(CommitFile file : fileListForCurrentCommit){
			if(file.getFilePath().equals(path)){
				return fileListForCurrentCommit.indexOf(file);
			}
		}
		return -1;
	}
	
	
	
	public void addCommitedFileToLibrary(String commitID, String path, String changeType){
		//register new commit if not already in the library
		if(!commitFileLibrary.containsKey(commitID)){
			commitFileLibrary.put(commitID, new ArrayList<CommitFile>());
		}
		List<CommitFile> fileListForCurrentCommit = commitFileLibrary.get(commitID);
		
		//register new file changed in commit if not already in the library
		
		int index = findIndexOfIn(path, fileListForCurrentCommit);
		if(index==-1){  
			CommitFile newCommitFile = new CommitFile(path,changeType);
			fileListForCurrentCommit.add(newCommitFile);
			index = fileListForCurrentCommit.indexOf(newCommitFile);
		}else{
			Main.printLine("[OPi+][ERROR] i have a file recorded as changed 2 in the same commit. This should not happen");		
			System.out.println("break!");
		}		
	}
	
	
	public void addChangeWithinCommitedFile(String commitID, String path, String changeType){
		//regiser new commit if not already in the library
		if(!commitFileLibrary.containsKey(commitID)){
			commitFileLibrary.put(commitID, new ArrayList<CommitFile>());
		}
		List<CommitFile> fileListForCurrentCommit = commitFileLibrary.get(commitID);
		
		//register new file changed in commit if not already in the library
		
		int index = findIndexOfIn(path, fileListForCurrentCommit);
		if(index==-1){  
			Main.printLine("[OPi+][ERROR] This file should have been recorded already "+path+" in commit "+commitID);
		}else{
			//register the change in the library
			CommitFile currentCommitFile = fileListForCurrentCommit.get(index);	
			registerChange(currentCommitFile, changeType);		
		}	
	}
	
	public void registerChange(CommitFile commitFile, String changeType){
		switch(changeType){
		case "ADD": commitFile.incrementADD();
					break;
		case "MODIFY": commitFile.incrementMODIFY();
					break;
		case "DELETE": commitFile.incrementDELETE();
					break;
		case "RENAME": commitFile.incrementRENAME();
					break;
		case "COPY": commitFile.incrementCOPY();
					break;
		}
	}
	
	

}
