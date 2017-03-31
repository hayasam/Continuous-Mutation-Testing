package mutation.testing;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import operias.Configuration;
import operias.Main;
import operias.OperiasStatus;
import operias.coverage.CoverageReport;
import operias.diff.DiffFile;
import operias.diff.DiffReport;
import operias.report.OperiasFile;
import operias.report.OperiasReport;
import operias.report.change.ChangeSourceChange;
import operias.report.change.DeleteSourceChange;
import operias.report.change.InsertSourceChange;
import operias.report.change.OperiasChange;

public class OperiasMutated {
	
	private ArrayList<MutatedFile> mutatedFiles;
	OperiasReport operiasReport;

	public OperiasMutated(OperiasReport operiasReport, String pathToPitestReport) {
		
		this.operiasReport = operiasReport;
		this.mutatedFiles = new ArrayList<MutatedFile>();
		
		List<OperiasFile> operiasFiles = operiasReport.getChangedClasses();
		//System.out.println("Number of files changed: "+operiasFiles.size());  //1
		for(OperiasFile operiasFile : operiasFiles){
			ArrayList<Integer> diffCoveredLines = parseOperiasFile(operiasFile);
			if(diffCoveredLines.size()>0){
				mutatedFiles.add(new MutatedFile(operiasFile.getSourceDiff().getFileName(operiasReport), diffCoveredLines));
			}
		}
		
		
		//TODO run pitest on last commit
		if(mutatedFiles.size()>0){
			//System.out.println("Current Working Directory = " +System.getProperty("user.dir"));			
			//TODO 3 is an assumption for operias
			String path = operiasReport.getSourceLocations().get(3);
			int tempLocation = path.indexOf("temp");
			String pathToNewVersionPom =  (String) path.subSequence(0, tempLocation+18);
			System.out.println("!!!!!!!!   "+pathToNewVersionPom);
			//executePitestTask(System.getProperty("user.dir"));
			
			//processMutatedCommit();
			
		}else{
			//TODO message for no code to mutate
			Main.printLine("[Mutated Operias Info] There are NO changed code lines that are also covered by the test suite");
		}
		
	}

	private boolean executePitestTask(String directory) throws IOException, InterruptedException {
		boolean executionSucceeded = false;

		File pomXML = new File(directory, "pom.xml");
		String  firstString = null, secondString = null;
		
		try {
			firstString = (new File(directory)).getCanonicalPath();
			secondString = (new File("")).getCanonicalPath();
		} catch (Exception e) {
			Main.printLine("[Error] [" + Thread.currentThread().getName() + "] Error creating pitest task");
			System.exit(OperiasStatus.ERROR_PITEST_TASK_CREATION.ordinal());
		}
		
		if (firstString.equals(secondString)) {
			Main.printLine("[Error] [" + Thread.currentThread().getName() + "] Cannot execute pitest on operias, infinite loop!");
			System.exit(OperiasStatus.ERROR_PITEST_TASK_OPERIAS_EXECUTION.ordinal());
		}
		
		//get temp location of revised version of system
		//mvn cobertura:cobertura -Dcobertura.report.format=xml -f directory on the target directory
		
		ProcessBuilder builder = new ProcessBuilder("mvn","clean", "cobertura:cobertura", "-Dcobertura.aggregate=true", "-Dcobertura.report.format=xml", "-f", pomXML.getAbsolutePath());

		Process process = null;
		process = builder.start();
		
		process.waitFor();

		int exitValue = process.exitValue();
		process.destroy();

		executionSucceeded = exitValue == 0;
		
		if (executionSucceeded) {

			Main.printLine("[Info] [" + Thread.currentThread().getName() + "] Succesfully executed cobertura");
		} else {
			Main.printLine("[Error] [" + Thread.currentThread().getName() + "] Error executing cobertura, exit value: " + exitValue);
		}
		return executionSucceeded;	
		
	}

	private void processMutatedCommit() {
		for(MutatedFile currentMutatedFile : mutatedFiles){
			String fileMutationReportPath = "BankAccount.java.html";
			
			//TODO extract exact location of pitest report for currentMutatedFile
			currentMutatedFile.setMutationReportPath(fileMutationReportPath);	
		}
	}

	
	

	private ArrayList<Integer> parseOperiasFile(OperiasFile operiasFile) {
		LinkedList<OperiasChange> changedBlocksinFile =  operiasFile.getChanges();
		int noChangeBlocksInFile =  changedBlocksinFile.size();
		ArrayList<Integer> diffCoveredLines = new ArrayList<Integer>(); //made/file
		
		//System.out.println("Looking at changes in: "+operiasFile.getSourceDiff().getFileName(operiasReport)); 
		//System.out.println("with "+noChangeBlocksInFile+" changed blocks in file");
		for(OperiasChange currentChange : changedBlocksinFile){
			diffCoveredLines = processChange(currentChange,diffCoveredLines,operiasFile);
		}
		return diffCoveredLines;
	}

	private ArrayList<Integer> processChange(OperiasChange currentChange, ArrayList<Integer> diffCoveredLines, OperiasFile operiasFile) {
		if(currentChange instanceof ChangeSourceChange || currentChange instanceof InsertSourceChange){
			//System.out.println("updated code "+currentChange.getSourceDiffDelta().getRevised().getPosition());
			int firstLineAdded = currentChange.getSourceDiffDelta().getRevised().getPosition();
			int lastLineAdded = firstLineAdded + operiasFile.getSourceDiff().getAddedLinesCount()-2; //21+17
			//TODO -1 for previous example. -2 for this example. obs: might be difernt for update and change; maybe use getSourceDiffDelta or size of revisedCoverage
			
			List<Boolean> codeChunkAdded = currentChange.getRevisedCoverage();	
			
			//System.out.println(currentChange.getRevisedCoverage());
			//System.out.println(currentChange.getSourceDiffDelta());
			
			
			for(int i = firstLineAdded; i<lastLineAdded; i++){
				if(codeChunkAdded.get(i-firstLineAdded)!=null && codeChunkAdded.get(i-firstLineAdded))
					diffCoveredLines.add(i+1);//TODO check how pitest looks at the code lines, with +1 we have the new line numbering starting from 1
			}
			System.out.println("We have "+ diffCoveredLines.size()+" lines to mutate");
			System.out.println("The lines that are covered AND changed are: "+ diffCoveredLines.toString());
			
		}else if(currentChange instanceof DeleteSourceChange){
			System.out.println("code was deleted "+currentChange.getSourceDiffDelta().getOriginal().getPosition());
			//TODO this information has to be processed. it does not give correct line number for latter lines
			
		}else {
			//Increased and Decreased Coverage - considered changes by Operias
		}
		return diffCoveredLines;
	}
	
	
	
	
	/*
	System.out.println(currentChange.toString());	//operias.report.change.InsertSourceChange@4dd8dc3
	System.out.println(operiasFile.getChanges().size()+" number of changes in "+operiasFile.getClassName());	//1 number of changes in groupID.artifactID.SecondBank
	System.out.println(operiasFile.getSourceDiff().getOriginalFileName());  ///home/ioana/OperiasMutated/operias-report/temp/1490173115906/src/main/java/groupID/artifactID/SecondBank.java
	System.out.println(operiasFile.getSourceDiff().getOriginalLineCount()); //23
	System.out.println(operiasFile.getSourceDiff().getAddedLinesCount()); //17
	System.out.println(operiasFile.getSourceDiff().getFileName(operiasReport));  // /src/main/java/groupID/artifactID/SecondBank.java
	System.out.println(operiasFile.getSourceDiff().getRemovedLineCount()); //0
	System.out.println(operiasFile.getSourceDiff().getRevisedFileName()); ///home/ioana/OperiasMutated/operias-report/temp/1490173116631/src/main/java/groupID/artifactID/SecondBank.java
	System.out.println(operiasFile.getSourceDiff().getRevisedLineCount()); //40
	System.out.println(operiasFile.getSourceDiff().getClass());  //class operias.diff.DiffFile
	System.out.println(operiasFile.getSourceDiff().getSourceState()); //CHANGED		
	System.out.println(currentChange.getOriginalLineNumber()); //22
	System.out.println(currentChange.getRevisedLineNumber()); //22
	System.out.println(currentChange.getClass().getTypeName()); // operias.report.change.CoverageDecreaseChange
	System.out.println(currentChange.getOriginalCoverage()); //[]
	System.out.println(currentChange.getRevisedCoverage()); //[null, false, false, false, null, null, false, false, false, null, false, false, false, null, false, null]
	System.out.println(currentChange.getSourceDiffDelta()); 
	//[InsertDelta, position: 21, lines: [	code line with , to separate]]
	System.out.println(currentChange.getSourceDiffDelta().getOriginal().getPosition()); //21
	System.out.println(currentChange.countOriginalLinesCovered());  //0
	System.out.println(currentChange.countOriginalRelevantLines()); //0
	System.out.println(currentChange.countRevisedLinesCovered()); //0
	*/
	
	
}
