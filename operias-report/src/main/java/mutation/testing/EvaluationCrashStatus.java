package mutation.testing;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class EvaluationCrashStatus {

	
	public static int PitestCrash;
	public static int OperiasCrash;
	private static String SEPARATOR = "|@| ";
	
	private static BufferedWriter bw;
	private static String evaluationDATAFileName;
	private static FileOutputStream fos;
	
	public EvaluationCrashStatus(String projectName, String path) throws IOException{
		
		PitestCrash=0;
		OperiasCrash=0;
		
		String fileName = projectName+"CRASH";
		evaluationDATAFileName = path+"/"+fileName+".csv";
		File file = new File(path+"/"+fileName+".csv");
		System.out.println(path+"/"+fileName+".csv");
		      
		if (file.createNewFile()){
		    System.out.println("[OPi+][INFO] File "+fileName+" is created!");
		}else{
		     System.out.println("[OPi+][ERROR] File already exists.");
		}
		      
		fos = new FileOutputStream(file);
		bw = new BufferedWriter(new OutputStreamWriter(fos));
		write("Crash Report for: "+projectName);
	}
	
	
	
	public static void recordPitestCrash(String[] strings){
		PitestCrash++;
		write("Pitest Crash"+SEPARATOR+strings[0]+" "+SEPARATOR+" "+strings[1]);
	}
	
	public static void recordOperiasCrash(String commitID, String cause){
		OperiasCrash++;
		write("Operias Crash "+SEPARATOR+" "+commitID+" "+SEPARATOR+" "+cause);
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
	
	
	
	
	
	
}
