package operias.mutated.exceptions;

import operias.Main;
import operias.OperiasStatus;

public class ExitRequiredException extends Exception {

	    public ExitRequiredException(String message) {
	        super(message);
	    }

		

		public ExitRequiredException(OperiasStatus errorFileDiffReportGeneration) {
			Main.printLine(errorFileDiffReportGeneration.toString());
		}

		
	
}
