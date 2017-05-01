package operias.mutated.record.files;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.plaf.synth.SynthEditorPaneUI;

import org.eclipse.jgit.transport.GitProtocolConstants;
import org.omg.PortableServer.SERVANT_RETENTION_POLICY_ID;

import operias.Main;
import operias.mutated.CommitFile;
import operias.mutated.CommitFileLibrary;
import operias.mutated.MutatedFile;

public class EvaluationDataFile {

		private static final String SEPARATOR = "|@|";
		private static BufferedWriter bw;
		private static String evaluationDATAFileName;
		private static FileOutputStream fos;
		
		public EvaluationDataFile(String projectName, String path) throws IOException{
			String fileName = projectName+"DATA";
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

		public static void printLibrary() {
			write("Commit ID"+SEPARATOR+
				  "#Changed Files"+SEPARATOR+
				  "OPi+ Impact"+SEPARATOR+
				  "Passed Prefiltering");
			
			Map<String, List<CommitFile>> commitFileLibrary = CommitFileLibrary.commitFileLibrary;
			Map<String, Integer> commitImpact = CommitFileLibrary.commitImpact;
			for(String commitID : commitFileLibrary.keySet()){
				write(commitID+SEPARATOR+
						commitFileLibrary.get(commitID).size()+SEPARATOR+
						commitImpact.get(commitID)+SEPARATOR+
						CommitFileLibrary.prefilteredCommitID.contains(commitID));
			}
		}

		
		
		
}
