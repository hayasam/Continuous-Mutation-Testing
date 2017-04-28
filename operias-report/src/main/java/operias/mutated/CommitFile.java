package operias.mutated;

public class CommitFile {

	private String filePath;
	private String mainChangeType;
	
	
	/** Add a new file to the project */
	private int noADD;
	
	/** Modify an existing file in the project (content and/or mode) */
	private int noMODIFY;
	
	/** Delete an existing file from the project */
	private int noDELETE;
	
	/** Rename an existing file to a new location */
	private int noRENAME;
	
	/** Copy an existing file to a new location, keeping the original */
	private int noCOPY;
	
	public CommitFile(String path, String changeType){
		this.filePath = path;
		this.mainChangeType=changeType;
		noADD=0;
		noMODIFY=0;
		noDELETE=0;
		noRENAME=0;
		noCOPY=0;
	}
	
	public String getMainChangeType(){
		return mainChangeType;
	}
	
	public int getTotalMutationRelevantFilesChanged(){
		return noADD+noMODIFY+noRENAME;
	}
	
	public String getFilePath(){
		return filePath;
	}
	
	
	public void incrementADD(){
		noADD++;
	}
	
	public void incrementMODIFY(){
		noMODIFY++;
	}
	
	public void incrementDELETE(){
		noDELETE++;
	}
	
	public void incrementRENAME(){
		noRENAME++;
	}
	
	public void incrementCOPY(){
		noCOPY++;
	}
	
	
	public int getTotalNumberOfFilesChanged(){
		return noADD+noMODIFY+noDELETE+noRENAME+noCOPY;
	}
	
	
}

