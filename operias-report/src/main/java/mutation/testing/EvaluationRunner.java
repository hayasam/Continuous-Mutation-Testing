package mutation.testing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import operias.Main;

public class EvaluationRunner {

	public static void main(String[] args){
		
		//RUN EVALUATION
		
		//set up the project
		String REMOTE_URL = "https://github.com/ileontiuc/testSettings.git";
		String MAVEN_PATH = "/usr/share/maven";
		
		ThirdPartyProxySeetings settings = new ThirdPartyProxySeetings(REMOTE_URL, MAVEN_PATH);
		GitProxy.settings = settings;
		PitestProxy.settings = settings;
		
		
		
		//get commits to analyze
		ArrayList<String>  commits = GitProxy.getFilteredCommits();
		
		//for each commit start the process
		for(String commitID: commits){
			try {
				runOperiasMutated(REMOTE_URL, GitProxy.previousCommit.get(commitID), commitID);
			} catch (ExitRequiredException e) {
				System.out.println("------------------------------------------------Operias crashed for commit: "+commitID);
				e.printStackTrace();
			}
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
