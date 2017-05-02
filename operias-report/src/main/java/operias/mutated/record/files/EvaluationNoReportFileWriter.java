package operias.mutated.record.files;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import operias.Main;

public class EvaluationNoReportFileWriter {
	

		private static String SEPARATOR = "|@| ";
		
		
		
		private static BufferedWriter bw;
		private static String evaluationFileName;
		private static FileOutputStream fos;
		
		
		public EvaluationNoReportFileWriter(String fileName, String path) throws IOException{
			
				  evaluationFileName = path+"/"+fileName+"NOMutationReportFiles.csv";
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
			    		+"File Name");
			    
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
