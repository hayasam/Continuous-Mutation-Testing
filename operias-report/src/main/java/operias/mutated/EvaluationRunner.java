package operias.mutated;

import java.io.IOException;
import java.util.ArrayList;

import operias.Configuration;
import operias.Main;
import operias.mutated.exceptions.ExitRequiredException;
import operias.mutated.exceptions.IncompatibleProjectException;
import operias.mutated.exceptions.PiTestException;
import operias.mutated.exceptions.SystemException;
import operias.mutated.proxy.GitProxy;
import operias.mutated.proxy.PitestProxy;
import operias.mutated.proxy.ThirdPartyProxySeetings;
import operias.mutated.record.files.EvaluationCrashStatus;
import operias.mutated.record.files.EvaluationDataFile;
import operias.mutated.record.files.EvaluationFileWriter;
import operias.mutated.record.files.EvaluationNoReportFileWriter;
import operias.mutated.record.files.EvaluationOPiLogFile;

public class EvaluationRunner {
	
	public static String scmConnection;
	public static String scmURL;
	public static String scmTag;
	public static String scmDevConnection;
	public static String jUnitVersion;
	public static String localPathForOPiReport;
	
	public static int commentLinesSkiped;

	public static int noMutationReport;
	public static int withMutationReport;
	
	public static int updatedSCM;
	public static int addedSCM;
	
	public static void main(String[] args){
		
		//RUN EVALUATION
		
		//set up the project
		String REMOTE_URL = "https://github.com/jhy/jsoup";
				//"https://github.com/ileontiuc/jsoup";
				//"https://github.com/jhy/jsoup";
				//"https://github.com/ileontiuc/commons-text";
				//"https://github.com/ileontiuc/testSettings"; 
				
				//"https://github.com/junit-team/junit5.git";
		String MAVEN_PATH = "/usr/share/maven";
		//TODO set limit
		int COMMIT_MUTATION_CHANGE_LOWER_LIMIT = 3;
		localPathForOPiReport = "/home/ioana/a_Thesis_Evaluation";
		
		//JSoup
		scmConnection="scm:git:https://github.com/jhy/jsoup.git";
		scmURL="https://github.com/jhy/jsoup";
		scmTag="HEAD";
		scmDevConnection="scm:git:git@github.com:jhy/jsoup.git";
		jUnitVersion="4.10";
		
		
		
		String[] tokens = REMOTE_URL.split("/");
		String projectName = tokens[tokens.length-1].substring(0, tokens[tokens.length-1].length() );
		try {
			//setup tracking files
			EvaluationOPiLogFile logFile = new EvaluationOPiLogFile(projectName, localPathForOPiReport);
			EvaluationFileWriter projectFile = new EvaluationFileWriter(projectName, localPathForOPiReport);
			EvaluationDataFile dataFile = new EvaluationDataFile(projectName, localPathForOPiReport);
			EvaluationCrashStatus crashFile = new EvaluationCrashStatus(projectName, localPathForOPiReport);
			EvaluationNoReportFileWriter noReportCommits = new EvaluationNoReportFileWriter(projectName, localPathForOPiReport);
			
			EvaluationDataFile.write("Project repository link: "+REMOTE_URL);
			
			ThirdPartyProxySeetings settings = new ThirdPartyProxySeetings(REMOTE_URL, MAVEN_PATH);
			GitProxy localGitProxy = new GitProxy(COMMIT_MUTATION_CHANGE_LOWER_LIMIT, settings);
			PitestProxy.settings = settings;
			
			
		//get commits to analyze
		ArrayList<String>  commits = GitProxy.getFilteredCommits();
		
		//for each commit start the process
		int counter =1;
		int total = commits.size();
		EvaluationDataFile.write("");
		EvaluationDataFile.write("commits that are analyzed (filtered in - crashes)");
		EvaluationDataFile.write("Commit ID ~ #Mutation report files missing ~ File with mutation report");
		for(String commitID: commits){
			System.out.println("Analyzing  "+counter+" out of "+total+" ---> "+(counter*100/total)+"% DONE");
			System.out.println("Looking at commit "+commitID);
			noMutationReport = 0;
			withMutationReport = 0;
			commentLinesSkiped=0;
			try {
				Configuration.reset();
				runOperiasMutated(REMOTE_URL, GitProxy.getPreviousCommitOf(commitID), commitID);
				EvaluationDataFile.write(commitID, noMutationReport, withMutationReport);
			} catch (ExitRequiredException e) {
				Main.printLine("[OPi+][ERROR]------------------------------------------------Operias crashed for commit: "+commitID);
				EvaluationCrashStatus.recordOperiasCrash(commitID, e.getCause().toString());
			} catch (PiTestException e) {
				Main.printLine("[OPi+][ERROR]------------------------------------------------Pitest crashed for commit: "+commitID);
				EvaluationCrashStatus.recordPitestCrash(e.getInfo());
			} catch (SystemException e) {
				Main.printLine("[OPi+][ERROR]------------------------------------------------System crashed for commit: "+commitID);
				EvaluationCrashStatus.recordSystemCrash(e.getInfo());
				e.printStackTrace();
			}catch(IncompatibleProjectException e){
				Main.printLine("[OPi+][ERROR]------------------------------------------------Incompatible system for commit: "+commitID);
				EvaluationCrashStatus.recordIncompatibleSystem(e.getInfo());
				e.printStackTrace();
			}catch(Exception e){
				Main.printLine("[OPi+][ERROR]------------------------------------------------System crashed for unknown reason in commit: "+commitID);
				String[] info = {commitID,"Fault in OPi+ logic. Exception was thrown during OPI+ excecution.",e.getMessage()};
				EvaluationCrashStatus.recordSystemCrash(info);
				e.printStackTrace();
			}
			
			System.out.println("Pitest crashes: "+EvaluationCrashStatus.pitestCrash);
			System.out.println("Operias crashes: "+EvaluationCrashStatus.operiasCrash);
			System.out.println("System crashes: "+EvaluationCrashStatus.systemCrash);
			System.out.println("Incompatible system: "+EvaluationCrashStatus.incompatibleSystem);
			float totalFailures = EvaluationCrashStatus.pitestCrash+EvaluationCrashStatus.operiasCrash+EvaluationCrashStatus.systemCrash;
			System.out.println("So far "+(totalFailures*100/total)+"% failed");
			counter++;
		}
		
		
		//add final statistic to DATA file
		EvaluationDataFile.write("Pitest crashes: "+EvaluationCrashStatus.pitestCrash);
		EvaluationDataFile.write("Operias crashes: "+EvaluationCrashStatus.operiasCrash);
		EvaluationDataFile.write("System crashes: "+EvaluationCrashStatus.systemCrash);
		EvaluationDataFile.write("Incompatible system: "+EvaluationCrashStatus.incompatibleSystem);
		float totalFailures = EvaluationCrashStatus.pitestCrash+EvaluationCrashStatus.operiasCrash+EvaluationCrashStatus.systemCrash;
		EvaluationDataFile.write("Out of the"+ total +"commits, "+(totalFailures*100/total)+"% failed");
		EvaluationDataFile.write("Skipped "+commentLinesSkiped+" lines that where changed and also a comment");
		EvaluationDataFile.write("Had to update scm connection in pom file for "+updatedSCM+" commits");
		EvaluationDataFile.write("Had to add scm connection in pom file for "+addedSCM+" commits");
		
		EvaluationDataFile.write("");
		EvaluationDataFile.write("These are ALL commits from repository");
		EvaluationDataFile.printLibrary();
		
		
		//DELETE files & CLOSE reports
		GitProxy.deleteTempFolder();
		EvaluationFileWriter.close();
		EvaluationDataFile.close();
		EvaluationCrashStatus.close();
		EvaluationNoReportFileWriter.close();
		
		System.out.println("[OPi+][Info] done parsing all usefull commits from project "+ REMOTE_URL);
		EvaluationOPiLogFile.write("[OPi+][Info] done parsing all usefull commits from project "+ REMOTE_URL);
		EvaluationOPiLogFile.close();
		
		} catch (IOException e) {
			e.printStackTrace();
			//if setup of initial files crashes it does not affect the evaluation process
		}
	}

	private static void runOperiasMutated(String repoLink, String originalCommitID, String revisedCommitID) throws ExitRequiredException, PiTestException, SystemException, IncompatibleProjectException{
				
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
