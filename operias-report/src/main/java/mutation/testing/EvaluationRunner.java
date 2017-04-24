package mutation.testing;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import operias.Configuration;
import operias.Main;

public class EvaluationRunner {
	
	

	public static void main(String[] args){
		
		//RUN EVALUATION
		
		//set up the project
		String REMOTE_URL = "https://github.com/jhy/jsoup";
				//"https://github.com/ileontiuc/commons-text";
				//"https://github.com/ileontiuc/testSettings"; 
				
				//"https://github.com/junit-team/junit5.git";
		String MAVEN_PATH = "/usr/share/maven";
		
		ThirdPartyProxySeetings settings = new ThirdPartyProxySeetings(REMOTE_URL, MAVEN_PATH);
		GitProxy.settings = settings;
		PitestProxy.settings = settings;
		
		String[] tokens = REMOTE_URL.split("/");
		String projectName = tokens[tokens.length-1].substring(0, tokens[tokens.length-1].length() );
		try {
			EvaluationFileWriter projectFile = new EvaluationFileWriter(projectName, "/home/ioana/a_Thesis_Evaluation");
			
		//get commits to analyze
		ArrayList<String>  commits = GitProxy.getFilteredCommits();
		
		//for each commit start the process
		
		for(String commitID: commits){
			try {
				Configuration.reset();
				projectFile.setCommitID(commitID);
				runOperiasMutated(REMOTE_URL, GitProxy.previousCommit.get(commitID), commitID);
			} catch (ExitRequiredException e) {
				System.out.println("------------------------------------------------Operias crashed for commit: "+commitID);
				e.printStackTrace();
			}
		}
		
		//GitProxy.deleteTempFolder();
		EvaluationFileWriter.close();
		Main.printLine("[OPi+][Info] done parsing all usefull commits from project "+ REMOTE_URL);
		
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void runOperiasMutated(String repoLink, String originalCommitID, String revisedCommitID) throws ExitRequiredException{
				
		String[] arguments = { "-oru", repoLink,
							   "-rru", repoLink,
							   "-oc",  originalCommitID,
							   "-rc",  revisedCommitID,
							   "-d",   "IoanaOperias", // directory where the generated site will be placed
							   "-v"};
		
		//String pathToPitestReport = "/home/ioana/TestLastCommitProject/target/pit-reports/";
		Main.mutatedOperias(arguments);
		
	}
}
