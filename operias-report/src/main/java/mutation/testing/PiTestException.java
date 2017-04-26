package mutation.testing;

import operias.OperiasStatus;

public class PiTestException extends Exception{

		private String commit; 
		private String reason;
	
	
	  public PiTestException(String commit, String reason) {
		  super(commit+reason);
		  this.commit=commit;
	      this.reason = reason;
	    }

	  @Override
	  public void printStackTrace(){
		  System.out.println("Error for commit "+commit+" because "+reason);
	  }
		
	  public String[] getInfo(){
		  String[] info = {commit, reason};
		  return info;
	  }
	  
}
