package operias.mutated.record.files;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import operias.Main;

public class EvaluationOPiLogFile {

			private static BufferedWriter bw;
			private static String evaluationLOGFileName;
			private static FileOutputStream fos;
			
			public EvaluationOPiLogFile(String projectName, String path) throws IOException{
				String fileName = projectName+"OPiLogs";
				evaluationLOGFileName = path+"/"+fileName+".csv";
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
