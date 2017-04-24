package mutation.testing;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.errors.SymlinksNotSupportedException;
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

public class OPi {
	
	private ArrayList<MutatedFile> mutatedFiles;
	OperiasReport operiasReport;

	public OPi(OperiasReport operiasReport) {
		
		this.operiasReport = operiasReport;
		this.mutatedFiles = new ArrayList<MutatedFile>();
		
		List<OperiasFile> filesChanged = operiasReport.getChangedClasses();
		Main.printLine("[OPi+] Pipeline: Number of files changes: "+filesChanged.size());  
		for(OperiasFile currentFileChanged : filesChanged){
			ArrayList<Integer> diffCoveredLines = parseOperiasFile(currentFileChanged);
			String currentFileName = currentFileChanged.getSourceDiff().getFileName(operiasReport);
			if(diffCoveredLines.size()>0){
				mutatedFiles.add(new MutatedFile(currentFileName, diffCoveredLines, Configuration.getRevisedCommitID()));
				Main.printLine("[OPi+] Pipeline: I have changed and covered by tests lines in file "+currentFileName); 
			}
		}
		
		
		Main.printLine("[OPi+] Pipeline: Number of files mutated: "+mutatedFiles.size()); 
		if(mutatedFiles.size()>0){			
			
			Main.printLine("[OPi+][INFO] Start processing the commited changes");
			String pitestReportsPath = PitestProxy.getMutationReportFor(Configuration.getRevisedCommitID());
			processMutatedCommit(pitestReportsPath);
			
		}else{
			//this scenario is not usefull for the Evaluation step => it should just output the old Operias report
			Main.printLine("[OPi+][BLUE-1] There are NO changed code lines that are also covered by the test suite");
			EvaluationFileWriter.blue1();
		}
		
	}

	

	private void processMutatedCommit(String pitestReportsPath) {
		
		for(MutatedFile currentMutatedFile : mutatedFiles){
			System.out.println("[OPi+][INFO] Processing each file in commit "+currentMutatedFile.getCommitID());
			
			System.out.println("my path for pitest reports is "+pitestReportsPath);
			System.out.println("the file that was changed in the same commit is: "+currentMutatedFile.getSystemFileName());
			
			// /tmp/TestGitRepository6589134661357336768/target/pit-reports/201704041512/groupID.artifactID/BankAccount.java.html
			
			String currentPitestReport = pitestReportsPath;
			currentMutatedFile.setMutationReportPath(currentPitestReport);	
			System.out.println("path for current file: "+currentPitestReport);
		}
	}

	
	

	private ArrayList<Integer> parseOperiasFile(OperiasFile operiasFile) {
		LinkedList<OperiasChange> changedBlocksinFile =  operiasFile.getChanges();
		int noChangeBlocksInFile =  changedBlocksinFile.size();
		ArrayList<Integer> diffCoveredLines = new ArrayList<Integer>(); //made/file
		
		//System.out.println("Looking at changes in: "+operiasFile.getSourceDiff().getFileName(operiasReport)); 
		//System.out.println("with "+noChangeBlocksInFile+" changed blocks in file");
		for(OperiasChange currentChange : changedBlocksinFile){
			diffCoveredLines.addAll(processChange(currentChange,operiasFile)) ;
		}
		return diffCoveredLines;
	}

	private ArrayList<Integer> processChange(OperiasChange currentChange, OperiasFile operiasFile) {
		
		ArrayList<Integer> diffCoveredLines = new ArrayList<Integer>();
		if(currentChange instanceof ChangeSourceChange || currentChange instanceof InsertSourceChange){
			int firstLineAdded = currentChange.getSourceDiffDelta().getRevised().getPosition();
			int lastLineAdded = firstLineAdded +currentChange.getSourceDiffDelta().getRevised().getLines().size() -1;
					
					
			//obs: might be different for update and change; maybe use size of revisedCoverage
			
			List<Boolean> codeChunkAdded = currentChange.getRevisedCoverage();
			System.out.println("in the current change there are "+currentChange.getRevisedCoverage().size()+" lines affected");
			for(int i = firstLineAdded; i<=lastLineAdded; i++){
				
				if(codeChunkAdded.get(i-firstLineAdded)!=null && codeChunkAdded.get(i-firstLineAdded)){
					diffCoveredLines.add(i+1);
					System.out.println("[OPi+][INFO] A line that changed and covered is in new version at: "+(i+1));
					System.out.println(diffCoveredLines.toString());
				}
			}
			System.out.println("[OPi+] We have "+ diffCoveredLines.size()+" lines to mutate");
			System.out.println("[OPi+] The lines that are covered AND changed are: "+ diffCoveredLines.toString());
			
		}else if(currentChange instanceof DeleteSourceChange){
			System.out.println("[OPi+] code was deleted starting at line: "+currentChange.getSourceDiffDelta().getOriginal().getPosition()+" for " +currentChange.getSourceDiffDelta().getRevised().size()+" rows");
			//!!! this information has to be processed. it does not give correct line number for latter lines
			
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
