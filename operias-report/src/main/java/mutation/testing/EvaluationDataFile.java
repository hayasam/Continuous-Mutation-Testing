package mutation.testing;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class EvaluationDataFile {


		private static BufferedWriter bw;
		private static String evaluationDATAFileName;
		private static FileOutputStream fos;
		
		public EvaluationDataFile(String projectName, String path) throws IOException{
			String fileName = projectName+"DATA";
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
			write("Project name: "+projectName);
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
