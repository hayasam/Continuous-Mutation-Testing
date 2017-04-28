package operias.mutated.exceptions;

import operias.Main;

public class SystemException extends Exception{
	private String commit; 
	private String reason;
	private Exception e;


  public SystemException(String commit, String reason, Exception e) {
	  super(commit+reason);
	  this.commit=commit;
      this.reason = reason;
      this.e=e;
    }

  @Override
  public void printStackTrace(){
	  Main.printLine("Error for commit "+commit+" because "+reason+" due to "+e.getMessage());
  }
	
  public String[] getInfo(){
	  String[] info = {commit, reason, e.getMessage()};
	  return info;
  }
  
}
