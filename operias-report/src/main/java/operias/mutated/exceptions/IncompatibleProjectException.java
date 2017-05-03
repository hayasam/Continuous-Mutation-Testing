package operias.mutated.exceptions;

import operias.Main;

public class IncompatibleProjectException extends Exception{

	
	private String commit; 
	private String reason;


  public IncompatibleProjectException(String commit) {
	  super(commit+"This version of the project does not contain a pom file => no maven settings; fault caught by operias");
	  this.commit=commit;
      this.reason = "This version of the project does not contain a pom file => no maven settings; fault caught by operias";
    }
  
 

  @Override
  public void printStackTrace(){
	  Main.printLine("Error for commit "+commit+" because "+reason);
  }
	
  public String[] getInfo(){
	  String[] info = {commit, reason};
	  return info;
  }
  
}
