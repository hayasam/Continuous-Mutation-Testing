package operias.git;

import java.io.File;
import java.util.Calendar;

import operias.Configuration;
import operias.Main;


public class Git {

	
	/**
	 * Clone the repository url to a temporary directory
	 * @param URL Repository url
	 * @return The location of the temporary directory
	 * @throws Exception 
	 */
	public static String clone(String URL) throws Exception {
		File tempDirectory = new File(Configuration.getTemporaryDirectory() + "/" + Calendar.getInstance().getTime().getTime() + "");
		
		Main.printLine("[Info] Cloning from \"" +URL+ "\"");
		ProcessBuilder builder = new ProcessBuilder("git","clone", URL, tempDirectory.getAbsolutePath());

		Process process = null;
		
		process = builder.start();
		process.waitFor();
		
		int exitValue = process.exitValue();
		
		process.destroy();
		
		if (exitValue == 0) {
			Main.printLine("[Info] Cloning succesfull!");
			return tempDirectory.getAbsolutePath();
		} else {
			throw new Exception(exitValue + "");
		}
	}
	
	/**
	 * Check out to a given commit
	 * 
	 * EG 9d3821f6411ad85a683b8d38e4d42411229f2eec
	 * @return True if succeeded, false otherwise
	 * @throws Exception 
	 */
	public static boolean checkout(String repositoryDirectory, String commitOrBranch) throws Exception {
		
		Main.printLine("[Info] Checking out: \"" +commitOrBranch+ "\"");
		ProcessBuilder builder = new ProcessBuilder("git","--git-dir", repositoryDirectory + "/.git/", "--work-tree", repositoryDirectory , "checkout", "-f", commitOrBranch);

		Process process = null;
		
		process = builder.start();
		process.waitFor();
		
		int exitValue = process.exitValue();
		
		process.destroy();
		
		if (exitValue == 0) {
			Main.printLine("[Info] Checkout succesfull! ");
			return true;
		} else {
			throw new Exception(exitValue + "");
		}
	}
	
	
}


