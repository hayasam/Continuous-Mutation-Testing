package operias.mutated.record.files;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import operias.Configuration;
import operias.Main;
import operias.mutated.Line;
import operias.mutated.MutatedFile;
import operias.mutated.Mutation;

public class EvaluationFileWriter {

	private static String SEPARATOR = "~";
	
	private static String commitID;
	private static String filePath;
	private static String fileName;
	private static int lineNumber;
	private static String oldCodeLine;
	private static String newCodeLine;
	private static String blueOutput;
	
	private static String changeType;
	private static boolean coverage;
	private static int survived;
	private static int killed;
	private static boolean noCoverage;
	
	private static String mutantName;
	private static String mutantDescription;
	private static String mutantStatus;
	
	
	
	private static BufferedWriter bw;
	private static String evaluationFileName;
	private static FileOutputStream fos;
	
	
	public EvaluationFileWriter(String fileName, String path) throws IOException{
		
			  evaluationFileName = path+"/"+fileName+".csv";
		      File file = new File(evaluationFileName);
		      Main.printLine(evaluationFileName);
		      
		      if (file.createNewFile()){
		    	  Main.printLine("[OPi+][INFO] File "+evaluationFileName+" is created!");
		      }else{
		    	  Main.printLine("[OPi+][ERROR] File already exists.");
		      }
		      
		      
		      
		  	fos = new FileOutputStream(file);
		  	bw = new BufferedWriter(new OutputStreamWriter(fos));
		    write("CommitID"+SEPARATOR+"Path in project"+SEPARATOR
		    		+"File Name"+SEPARATOR+"LineNumber"+SEPARATOR
		    		+"New Line"+SEPARATOR
		    		+"Change type"+SEPARATOR
		    		+"Test Coverage"+SEPARATOR+"#Survived"+SEPARATOR
		    								  +"#Killed"+SEPARATOR
		    								  +"#NoCoverage"+SEPARATOR
		    		+"BLUE output"+SEPARATOR+"Mutant Name"
		    		+SEPARATOR+"Mutation description"+SEPARATOR+"Mutant STATUS");
		    
	}
	
	
	
	
	
	public static void write(String text){
		try {
			bw.write(text);
			bw.newLine();
		    
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void close(){
		try {
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	
	public static void recordNewLine(String extraColumn){
		String text = connect();
		text = text+SEPARATOR+extraColumn;
		write(text);
		System.out.println(text);
		
	}
	
	
	public static void recordNewLine(){
		String text = connect();
		write(text);
	}
	
	
	
	public static String connect() {
		
		String text;
		text = commitID+SEPARATOR;
		text = text+filePath+SEPARATOR;
		text = text+fileName+SEPARATOR;
		text = text+lineNumber+SEPARATOR;
		text = text+oldCodeLine+SEPARATOR;
		text = text+newCodeLine+SEPARATOR;
		text = text+changeType+SEPARATOR;
		text = text+coverage+SEPARATOR;
		text = text+survived+SEPARATOR;
		text = text+killed+SEPARATOR;
		text = text+noCoverage+SEPARATOR;
		text = text+blueOutput+SEPARATOR;
		text = text+mutantName+SEPARATOR;
		text = text+mutantDescription+SEPARATOR;
		text = text+mutantStatus+SEPARATOR;
		
		return text;
	}
	
	
	public static void print(ArrayList<MutatedFile> mutatedFiles) {
		
		for(MutatedFile mutatedFile: mutatedFiles){
			
			commitID= mutatedFile.getCommitID();
			if(commitID.isEmpty()){
				commitID=" ";
			}
			
			filePath= mutatedFile.getSystemFileName();
			if(filePath.isEmpty()){
				filePath=" ";
			}
			
			fileName= mutatedFile.getFileName();
			if(fileName.isEmpty()){
				fileName=" ";
			}
			
			
			
			String mutationReportPath = mutatedFile.getMutationReportPath();
			if(mutationReportPath==null){
				EvaluationNoReportFileWriter.write(commitID+SEPARATOR+filePath+SEPARATOR+fileName);
			}else{
				ArrayList<Line> diffLines = mutatedFile.getDiffLines();
				for(Line line: diffLines){
					lineNumber = line.getNumber();
					oldCodeLine = line.getOldLine();
					newCodeLine = line.getNewLine();
					changeType = line.getType();
					
					
					
					coverage = line.hasTestCoverage();
					
					survived= line.getSuvived();
					killed = line.getKilled();
					noCoverage = line.getNoCoverage();
					
					blueOutput = line.getBlueOutput();
					ArrayList<Mutation> survivedMutantList= line.getSurvivedMutantList();
					for(Mutation m : survivedMutantList){
						mutantName = m.getName();
						mutantDescription= m.getDescription();
						mutantStatus = m.getStatus();
						recordNewLine();
						
						mutantName = "";
						mutantDescription= "";
						mutantStatus ="";
					}
					if(survivedMutantList.isEmpty()){
						recordNewLine();
					}
					lineNumber = -1;
					oldCodeLine = "";
					newCodeLine = "";
					changeType = "";
					coverage = false;
					blueOutput = "";
				}
				if(diffLines.isEmpty()){
					recordNewLine(); //"this should not happen"
				}
				commitID= " ";
				filePath = " ";
				fileName =" ";
			}
			
		}
			
			
	}

	
	
	/*
	public static void print(ArrayList<MutatedFile> mutatedFiles) {
			
		for(MutatedFile mutatedFile: mutatedFiles){
			commitID= mutatedFile.getCommitID();
			filePath = mutatedFile.getSystemFileName();
			fileName = mutatedFile.getFileName();
			ArrayList<Line> diffLines = mutatedFile.getDiffLines();
			for(Line line: diffLines){
				lineNumber = line.getNumber();
				oldCodeLine = line.getOldLine();
				newCodeLine = line.getNewLine();
				changeType = line.getType();
				previousCommitID = line.getPreviousCommitID();
				previousCommitMutationReportPath = line.getPreviousCommitMutationReportPath();
				coverage = line.hasTestCoverage();
				blueOutput = line.getBlueOutput();
				ArrayList<Mutation> survivedMutantList= line.getSurvivedMutantList();
				for(Mutation m : survivedMutantList){
					mutantName = m.getName();
					mutantDescription= m.getDescription();
					mutantStatus = m.getStatus();
					recordNewLine();
					mutantName = "";
					mutantDescription= "";
					mutantStatus ="";
				}
				if(survivedMutantList.isEmpty()){
					recordNewLine();
					lineNumber = -1;
					oldCodeLine = "";
					newCodeLine = "";
					changeType = "";
					previousCommitID = "";
					previousCommitMutationReportPath = "";
					coverage = false;
					blueOutput = "";
				}
				
			}
			if(diffLines.isEmpty()){
				recordNewLine("this should not happen");
			}
			commitID= "";
			filePath = "";
			fileName ="";
		}
		
	}
	
*/
	
}
