package operias.mutated;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.jcraft.jsch.ConfigRepository.Config;

import difflib.Chunk;
import difflib.Delta;
import operias.Configuration;
import operias.Main;
import operias.mutated.exceptions.FileException;
import operias.mutated.exceptions.IncompatibleProjectException;
import operias.mutated.exceptions.PiTestException;
import operias.mutated.exceptions.SystemException;
import operias.mutated.proxy.PitestProxy;
import operias.mutated.record.files.EvaluationCrashStatus;
import operias.mutated.record.files.EvaluationDataFile;
import operias.mutated.record.files.EvaluationFileWriter;
import operias.report.OperiasFile;
import operias.report.OperiasReport;
import operias.report.change.ChangeSourceChange;
import operias.report.change.DeleteSourceChange;
import operias.report.change.InsertSourceChange;
import operias.report.change.OperiasChange;

public class OPi {
	
	
	private ArrayList<MutatedFile> mutatedFiles;
	public static ArrayList<Line> updatedLines;
	OperiasReport operiasReport;
	private ArrayList<String> filesAnalyzed;

	public OPi(OperiasReport operiasReport) throws PiTestException, SystemException, IncompatibleProjectException {
		
		this.operiasReport = operiasReport;
		this.mutatedFiles = new ArrayList<MutatedFile>();
		this.updatedLines = new ArrayList<Line>();
		this.filesAnalyzed = new ArrayList<String>();
		List<OperiasFile> filesChanged = operiasReport.getChangedClasses();
		Main.printLine("[OPi+] Pipeline: Number of files changes: "+filesChanged.size()); 
		Main.printLine("[OPi+] Pipeline: Collecting all changed lines for each file");
		//Cheat Sheet: the list of files changed by Operias is larger than reality.i.e jsoup commit 3519d82fb3b11bc3a96b3598bfa9ccde9fae4fc9 has 38 file changes, and operias reports it as 99
		for(OperiasFile currentFileChanged : filesChanged){
			//create mutated file
			
			String[] tokens = currentFileChanged.getClassName().split("\\$");
			if(!filesAnalyzed.contains(tokens[0])){
				ArrayList<Line> changedLines = getLinesFromOperiasFile(currentFileChanged);
				String currentFileName = currentFileChanged.getSourceDiff().getFileName(operiasReport);
				if(changedLines.size()>0){
					mutatedFiles.add(new MutatedFile(currentFileName, changedLines, Configuration.getRevisedCommitID()));
					filesAnalyzed.add(tokens[0]);
				}
			}
		}
		
		Main.printLine("[OPi+] Pipeline: Number of files with changes in this commit: "+mutatedFiles.size()); 
		if(mutatedFiles.size()>0){	
			Main.printLine("[OPi+][INFO] Start processing the changes commited in "+Configuration.getRevisedCommitID());
			//run pitest on last commit to get all report for files changed in this commmit
			String pitestReportsPath = PitestProxy.getMutationReportFor(Configuration.getRevisedCommitID(), false);
			processMutatedCommit(pitestReportsPath);
			
			//print to file all line for this file
			//boolean runPrevious=updatedLines.size()>0;
			//String previousCommit = Configuration.getOriginalCommitID();
			EvaluationFileWriter.print(mutatedFiles);
			computeCommitImpactToLibrary();
			
			//if(runPrevious){
			//	runPreviousMutationAnalysis(previousCommit);
			//}
		}else{
			//this scenario means the prefiltering is not good enough
			Main.printLine("[OPi+][ERROR] There are NO changed code lines");
			throw new SystemException(Configuration.getRevisedCommitID(), "this commit should have been prefiltered. There are no lines changed or added in this commit", new Exception());
		}
		
	}

	





	private void computeCommitImpactToLibrary() {
		int currentCommitImpact = 0;
		for(MutatedFile mf : mutatedFiles){
			currentCommitImpact += mf.getFileCommitImpact();
		}
		CommitFileLibrary.addImpact(Configuration.getRevisedCommitID(), currentCommitImpact);
		
	}




	



	public static void rememberUpdatedLine(Line line){
		updatedLines.add(line);
	}
	
	//go through all files changed in current commit
	private void processMutatedCommit(String pitestReportsPath) throws SystemException {
		
		for(MutatedFile currentMutatedFile : mutatedFiles){
			Main.printLine("[OPi+][INFO] Processing each file in commit "+currentMutatedFile.getCommitID());
			
			Main.printLine("my path for pitest reports is "+pitestReportsPath);
			Main.printLine("the file that was changed in the same commit is: "+currentMutatedFile.getSystemFileName());
			
			// /tmp/TestGitRepository6589134661357336768/target/pit-reports/201704041512/groupID.artifactID/BankAccount.java.html
			
			try {
				if(currentMutatedFile.setMutationReportPath(pitestReportsPath) != null){
					currentMutatedFile.extractMutationReport();
					Main.printLine("path for mutation report for current file: "+currentMutatedFile.getFileName()+" is at "+currentMutatedFile.getMutationReportPath());
					EvaluationRunner.withMutationReport++;
				}else{
					Main.printLine("Pitest did not create mutation report for current file: "+currentMutatedFile.getFileName());
					EvaluationRunner.noMutationReport++;
				}
				
			} catch (FileException e) {
				EvaluationCrashStatus.recordFileMutationPathCrash(e.getInfo());
				Main.printLine("Ignored current file dues to no mutation path");
			}	
			
		}
	}

	
	private ArrayList<Line> getLinesFromOperiasFile(OperiasFile currentFileChanged) {
		ArrayList<Line> changedLines = new ArrayList<Line>();
		
		LinkedList<OperiasChange> changedBlocksinFile =  currentFileChanged.getChanges();
		int noChangeBlocksInFile =  changedBlocksinFile.size();
		
		//for each block
		for(OperiasChange currentChange : changedBlocksinFile){
			changedLines.addAll(processBlockChange(currentChange,currentFileChanged)) ;
		}
		return changedLines;
	}

	private ArrayList<Line> processBlockChange(OperiasChange currentChange, OperiasFile currentFileChanged) {
		ArrayList<Line> changedLinesInBlock = new ArrayList<Line>();
		
		if(currentChange instanceof ChangeSourceChange){
			changedLinesInBlock=processBlock(currentChange, currentFileChanged,"UPDATE");
		}else if(currentChange instanceof InsertSourceChange){
			changedLinesInBlock=processBlock(currentChange, currentFileChanged,"ADD");
		}else if(currentChange instanceof DeleteSourceChange){
			Main.printLine("[OPi+] code was deleted starting at line: "+currentChange.getSourceDiffDelta().getOriginal().getPosition());
		}else{
			//Increased and Decreased Coverage - considered changes by Operias not relevant for OPi+
		}
		return changedLinesInBlock;
		
	}


	private ArrayList<Line> processBlock(OperiasChange currentChange, OperiasFile currentFileChanged,  String changeType) {
		
		ArrayList<Line> changedLinesInBlock = new ArrayList<Line>();
		boolean coverageFlag=false;
		//int firstLineAdded = currentChange.getSourceDiffDelta().getRevised().getPosition()+1;
		//int lastLineAdded = firstLineAdded +currentChange.getSourceDiffDelta().getRevised().getLines().size()-1;
				
				
		//obs: might be different for update and change; maybe use size of revisedCoverage
		
		List<Boolean> blockCoverage = currentChange.getRevisedCoverage();
		Main.printLine("in the current block there are "+currentChange.getRevisedCoverage().size()+" lines affected");
		
		Delta delta = currentChange.getSourceDiffDelta();
		Chunk newChunk = delta.getRevised();
		for(int i = 0; i<newChunk.getLines().size(); i++){
			
			if(blockCoverage.get(i)!=null && blockCoverage.get(i)){
				coverageFlag=true;
			}
			
			String newLine = newChunk.getLines().get(i).toString();
			String codeLine = newLine.replaceAll("\\s+","");
			if(codeLine.startsWith("//")||codeLine.startsWith("*")||codeLine.startsWith("/*")||codeLine.endsWith("*/")||
					codeLine.startsWith("@Override")||codeLine.startsWith("import")||codeLine.startsWith("package") ||
					codeLine.startsWith("{")|| codeLine.endsWith("}")|| codeLine.endsWith("};")||
					codeLine.startsWith("<p>")|| codeLine.startsWith("</p>")||
					codeLine.contains("a href")|| codeLine.isEmpty() || codeLine.startsWith("@")){
				EvaluationRunner.commentLinesSkiped++;
			}else{
				Line currentLine = new Line(currentChange.getRevisedLineNumber()+i, changeType, coverageFlag, newLine);
				changedLinesInBlock.add(currentLine);
			}
			coverageFlag=false;	

			
		}
		Main.printLine("[OPi+] We have "+ changedLinesInBlock.size()+" lines to mutate");
		//print the line number that should be analyzed
		ArrayList<Integer> list = new ArrayList<Integer>();
		for(Line l : changedLinesInBlock){
			list.add(l.getNumber());
		}
		Main.printLine(list.toString());
		
		return changedLinesInBlock;
	}



	private void runPreviousMutationAnalysis(String previousCommit) throws PiTestException, SystemException, IncompatibleProjectException {
		//run previous Pitest analysis
		
			//run pitest on previous commit and record data in different file?
			Main.printLine("[OPi+][INFO] running Pitest for previous commit"+previousCommit+" because current commit "+Configuration.getRevisedCommitID()+" has at least one updated line");
			
			String mutationReportPath = PitestProxy.getMutationReportFor(previousCommit, true);
			
	        File srcDir = new File(mutationReportPath);
	        String destination = EvaluationRunner.localPathForOPiReport+"/previousCommitReports/"+previousCommit;
	        File destDir = new File(destination);
	        try {
	            FileUtils.copyDirectory(srcDir, destDir);
	            EvaluationDataFile.write("for commit "+Configuration.getRevisedCommitID()+"  the previous commit "+previousCommit+" mutation reports are at: "+destDir);
	            
	        } catch (IOException e) {
	            e.printStackTrace();
	            throw new SystemException(Configuration.getRevisedCommitID(), "could not copy mutation reports for previous commit", e);
	        }
	}



/*

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
			Main.printLine("in the current change there are "+currentChange.getRevisedCoverage().size()+" lines affected");
			for(int i = firstLineAdded; i<=lastLineAdded; i++){
				
				if(codeChunkAdded.get(i-firstLineAdded)!=null && codeChunkAdded.get(i-firstLineAdded)){
					diffCoveredLines.add(i+1);
					Main.printLine("[OPi+][INFO] A line that changed and covered is in new version at: "+(i+1));
					Main.printLine(diffCoveredLines.toString());
				}
			}
			Main.printLine("[OPi+] We have "+ diffCoveredLines.size()+" lines to mutate");
			Main.printLine("[OPi+] The lines that are covered AND changed are: "+ diffCoveredLines.toString());
			
		}else if(currentChange instanceof DeleteSourceChange){
			Main.printLine("[OPi+] code was deleted starting at line: "+currentChange.getSourceDiffDelta().getOriginal().getPosition()+" for " +currentChange.getSourceDiffDelta().getRevised().size()+" rows");
			//!!! this information has to be processed. it does not give correct line number for latter lines
			
		}else {
			//Increased and Decreased Coverage - considered changes by Operias
		}
		return diffCoveredLines;
	}
	
	*/
	
	
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
