package operias.mutated.exceptions;

import operias.Main;
import operias.OperiasStatus;

public class ExitRequiredException extends Exception {

	String text;
	
	    public ExitRequiredException(String message) {
	        super(message);
	        text = message;
	    }

		

		public ExitRequiredException(OperiasStatus errorFileDiffReportGeneration) {
			Main.printLine(errorFileDiffReportGeneration.toString());
		}

		public String getText(){
			return text;
		}
		
	
}
