package mutation.testing;

import java.io.IOException;
import java.util.ArrayList;

import operias.Configuration;
import operias.Main;

public class EvaluationRunner {
	
	public static String scmConnection;
	public static String scmURL;
	public static String scmTag;
	public static String scmDevConnection;
	public static String jUnitVersion;

	public static void main(String[] args){
		
		//RUN EVALUATION
		
		//set up the project
		String REMOTE_URL = "https://github.com/ileontiuc/jsoup";
				//"https://github.com/jhy/jsoup";
				//"https://github.com/ileontiuc/commons-text";
				//"https://github.com/ileontiuc/testSettings"; 
				
				//"https://github.com/junit-team/junit5.git";
		String MAVEN_PATH = "/usr/share/maven";
		int COMMIT_MUTATION_CHANGE_LOWER_LIMIT = 2;
		String localPathForOPiReport = "/home/ioana/a_Thesis_Evaluation";
		
		//JSoup
		scmConnection="scm:git:https://github.com/jhy/jsoup.git";
		scmURL="https://github.com/jhy/jsoup";
		scmTag="HEAD";
		scmDevConnection="scm:git:git@github.com:jhy/jsoup.git";
		jUnitVersion="4.10";
		
		
		ThirdPartyProxySeetings settings = new ThirdPartyProxySeetings(REMOTE_URL, MAVEN_PATH);
		GitProxy localGitProxy = new GitProxy(COMMIT_MUTATION_CHANGE_LOWER_LIMIT, settings);
		PitestProxy.settings = settings;
		
		String[] tokens = REMOTE_URL.split("/");
		String projectName = tokens[tokens.length-1].substring(0, tokens[tokens.length-1].length() );
		try {
			EvaluationFileWriter projectFile = new EvaluationFileWriter(projectName, localPathForOPiReport);
			EvaluationDataFile dataFile = new EvaluationDataFile(projectName, localPathForOPiReport);
			EvaluationCrashStatus crashFile = new EvaluationCrashStatus(projectName, localPathForOPiReport);
			EvaluationDataFile.write("Project repository link: "+REMOTE_URL);
			
		//get commits to analyze
		ArrayList<String>  commits = GitProxy.getFilteredCommits();
		
		/*
		 * 
		 //one time only compute the group and artifact part of the path - to use later for identifying the pitest path
				 if(sw){
						String[] tokens = .split("/");
						GitProxy.groupArtifactID = tokens[3]+"."+tokens[4]+".*";
						sw=false;
					}
		 * */
		
		//for each commit start the process
		
		
		for(String commitID: commits){
			try {
				Configuration.reset();
				projectFile.setCommitID(commitID);
				runOperiasMutated(REMOTE_URL, GitProxy.getPreviousCommitOf(commitID), commitID);
			} catch (ExitRequiredException e) {
				System.out.println("[OPi+][ERROR]------------------------------------------------Operias crashed for commit: "+commitID);
				EvaluationCrashStatus.recordOperiasCrash(commitID, e.getCause().toString());
				e.printStackTrace();
				
			} catch (PiTestException e) {
				System.out.println("[OPi+][ERROR]------------------------------------------------Pitest crashed for commit: "+commitID);
				EvaluationCrashStatus.recordPitestCrash(e.getInfo());
				e.printStackTrace();
			}
		}
		
		
		
		
		//DELETE files & CLOSE reports
		
		//GitProxy.deleteTempFolder();
		EvaluationFileWriter.close();
		EvaluationDataFile.close();
		EvaluationCrashStatus.close();
		Main.printLine("[OPi+][Info] done parsing all usefull commits from project "+ REMOTE_URL);
		
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void runOperiasMutated(String repoLink, String originalCommitID, String revisedCommitID) throws ExitRequiredException, PiTestException{
				
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
