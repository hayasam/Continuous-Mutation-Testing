package mutation.testing;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class EvaluationFileWriter {

	private static String SEPARATOR = "|@| ";
	
	private static String commitID;
	private static String filePath;
	private static String fileName;
	private static int lineNumber;
	private static String oldCodeLine;
	private static String newCodeLine;
	private static String blueOutput;
	
	private static BufferedWriter bw;
	private static String evaluationFileName;
	private static FileOutputStream fos;
	
	public EvaluationFileWriter(String fileName, String path) throws IOException{
		
			  evaluationFileName = path+"/"+fileName+".csv";
		      File file = new File(path+"/"+fileName+".csv");
		      System.out.println(path+"/"+fileName+".csv");
		      
		      if (file.createNewFile()){
		        System.out.println("[OPi+][INFO] File "+fileName+" is created!");
		      }else{
		        System.out.println("[OPi+][ERROR] File already exists.");
		      }
		      
		      
		      
		  	fos = new FileOutputStream(file);
		  	bw = new BufferedWriter(new OutputStreamWriter(fos));
		    write("CommitID"+SEPARATOR+"Path in project"+SEPARATOR
		    		+"File Name"+SEPARATOR+"LineNumber"+SEPARATOR
		    		+"Old Line"+SEPARATOR+"New Line"+SEPARATOR
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
	
	
	public static void writeInFile(String extraColumn){
		String text = connect();
		text = text+SEPARATOR+extraColumn;
		write(text);
		
	}
	
	public static void blue1(){
		filePath="";
		fileName="";
		lineNumber=-1;
		oldCodeLine = "";
		newCodeLine="";
		blueOutput="1";
		write(connect());
	}	
	
	public static String connect() {
		
		String text;
		text = commitID+SEPARATOR;
		text = text+filePath+SEPARATOR;
		text = text+fileName+SEPARATOR;
		text = text+lineNumber+SEPARATOR;
		text = text+oldCodeLine+SEPARATOR;
		text = text+newCodeLine+SEPARATOR;
		text = text+blueOutput+SEPARATOR;
		
		return text;
	}
	
	public static void writeInFile(String mutantName, String mutantDescription, String mutantStatus) {
		
		
		String text = connect();
		text = text+mutantName+SEPARATOR;
		text = text+mutantDescription+SEPARATOR;
		text = text+mutantStatus;
		write(text);
	}
	
	public static String getCommitID() {
		return commitID;
	}






	public static void setCommitID(String commitID) {
		EvaluationFileWriter.commitID = commitID;
	}






	public static String getFileName() {
		return fileName;
	}






	public static void setFileName(String fileName) {
		EvaluationFileWriter.fileName = fileName;
	}






	public static int getLineNumber() {
		return lineNumber;
	}






	public static void setLineNumber(int lineNumber) {
		EvaluationFileWriter.lineNumber = lineNumber;
	}






	public static String getOldCodeLine() {
		return oldCodeLine;
	}






	public static void setOldCodeLine(String oldCodeLine) {
		EvaluationFileWriter.oldCodeLine = oldCodeLine;
	}






	public static String getNewCodeLine() {
		return newCodeLine;
	}






	public static void setNewCodeLine(String newCodeLine) {
		EvaluationFileWriter.newCodeLine = newCodeLine;
	}






	public static String getBlueOutput() {
		return blueOutput;
	}






	public static void setBlueOutput(String blueOutput) {
		EvaluationFileWriter.blueOutput = blueOutput;
	}

	public static void setPath(String systemFileName) {
		filePath = systemFileName;
		
	}
	

	
}
