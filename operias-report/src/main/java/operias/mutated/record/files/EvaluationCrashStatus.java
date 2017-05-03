package operias.mutated.record.files;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import operias.Main;

public class EvaluationCrashStatus {

	
	public static int pitestCrash;
	//public static int previousPitestCrash;
	public static int operiasCrash;
	public static int systemCrash;
	public static int fileMutationPathCrash;
	public static int incompatibleSystem;
	private static String SEPARATOR = "~";
	
	private static BufferedWriter bw;
	private static String evaluationDATAFileName;
	private static FileOutputStream fos;
	
	
	public EvaluationCrashStatus(String projectName, String path) throws IOException{
		
		pitestCrash=0;
	//	previousPitestCrash=0;
		operiasCrash=0;
		systemCrash=0;
		fileMutationPathCrash=0;
		incompatibleSystem=0;
		
		String fileName = projectName+"CRASH";
		evaluationDATAFileName = path+"/"+fileName+".csv";
		File file = new File(path+"/"+fileName+".csv");
		Main.printLine(path+"/"+fileName+".csv");
		      
		if (file.createNewFile()){
			Main.printLine("[OPi+][INFO] File "+fileName+" is created!");
		}else{
			Main.printLine("[OPi+][ERROR] File already exists.");
		}
		      
		fos = new FileOutputStream(file);
		bw = new BufferedWriter(new OutputStreamWriter(fos));
		write("Crash Report for: "+projectName);
	}
	
	
	
	public static void recordPitestCrash(String[] strings){
		
		pitestCrash++;
		write("Pitest Crash"+SEPARATOR+strings[0]+" "+SEPARATOR+" "+strings[1]);
	}
	
	public static void recordOperiasCrash(String commitID, String cause){
		operiasCrash++;
		write("Operias Crash "+SEPARATOR+" "+commitID+" "+SEPARATOR+" "+cause);
	}
	
	public static void recordSystemCrash(String[] info){
		systemCrash++;
		write("System Crash "+SEPARATOR+" "+info[0]+" "+SEPARATOR+" "+info[1]+" "+SEPARATOR+" "+info[2]);
	}
	
	public static void recordFileMutationPathCrash(String[] info){
		fileMutationPathCrash++;
		write("Mutation File Path Crash "+SEPARATOR+" "+info[0]+" "+SEPARATOR+" "+info[1]);
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



	public static void recordIncompatibleSystem(String[] info) {
		incompatibleSystem++;
		write("Incompatible System "+SEPARATOR+" "+info[0]+" "+SEPARATOR+" "+info[1]);
		
	}
	
	
	
	
	
	
}
