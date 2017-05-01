package operias.mutated.exceptions;

import operias.Main;
import operias.OperiasStatus;

public class PiTestException extends Exception{

		private String commit; 
		private String reason;
		private String flag;
	
	
	  public PiTestException(String commit, String reason) {
		  super(commit+reason);
		  this.commit=commit;
	      this.reason = reason;
	    }
	  
	  public PiTestException(String commit, String reason, String previousFlag) {
		  super(commit+reason+previousFlag);
		  this.commit=commit;
	      this.reason = reason;
	      this.flag = previousFlag;
	    }

	  @Override
	  public void printStackTrace(){
		  Main.printLine("Error for commit "+commit+" because "+reason+" "+flag);
	  }
		
	  public String[] getInfo(){
		  String[] info = {commit, reason, flag};
		  return info;
	  }
	  
}
