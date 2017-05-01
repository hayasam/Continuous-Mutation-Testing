package operias.mutated.exceptions;

import operias.Main;

public class FileException extends Exception{

	private String commit; 
	private String reason;


  public FileException(String commit, String reason) {
	  super(commit+reason);
	  this.commit=commit;
      this.reason = reason;
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
