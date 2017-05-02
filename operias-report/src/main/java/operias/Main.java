package operias;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import operias.mutated.*;
import operias.mutated.exceptions.ExitRequiredException;
import operias.mutated.exceptions.PiTestException;
import operias.mutated.exceptions.SystemException;
import operias.mutated.record.files.EvaluationOPiLogFile;

/**
 * Main class of operias
 * @author soosterwaal
 *
 */
public class Main {
	
	/**
	 * Start up the operias tool
	 * @param args Contains serveral arguments, for example the source directories which
	 * needs to be compared and to which branch it must be compared.
	 * @throws PiTestException 
	 * @throws SystemException 
	 * 
	 
	 * 
	 */
	
	
	public static void mutatedOperias(String[] args) throws ExitRequiredException, PiTestException, SystemException{
		
		Configuration.parseArguments(args);
		// Check if the directories were set
		if (Configuration.getOriginalDirectory() == null || Configuration.getRevisedDirectory() == null) {
			// if not, try to set up directories through git
			Configuration.setUpDirectoriesThroughGit();
		}
		
		Operias myOperias = new Operias().constructReport();
		if(Operias.clock){
			//myOperias.writeHTMLReport().writeXMLReport();
		}
		
		Main.printLine("[Info] Cleaning up!");
		// Remove temporary directory
		
		try {
			FileUtils.deleteDirectory(new File(Configuration.getTemporaryDirectory()));
		} catch (IOException e) {
			e.printStackTrace();
			//not being able to delete the temporrary folders from operias does not influence the evaluation process
		}
		
		
		Main.printLine("[OPi+][Info] Execution of mutated operias was a great success!");
		
	}
	
	/**
	 * Print a line to the console, it will only be printed if the verbose argument was given
	 * @param line
	 */
	public static void printLine(String line) {
		if(Configuration.isOutputEnabled() && !line.startsWith("[Info]")) {
			EvaluationOPiLogFile.write(line);
			//System.out.println(line);
		}
	}

	public static void main(String[] arguments) {
		
	}

	
}
