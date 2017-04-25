package mutation.testing;

import operias.OperiasStatus;

public class ExitRequiredException extends Exception {

	    public ExitRequiredException(String message) {
	        super(message);
	    }

		

		public ExitRequiredException(OperiasStatus errorFileDiffReportGeneration) {
			System.out.println(errorFileDiffReportGeneration.toString());
		}

		
	
}
