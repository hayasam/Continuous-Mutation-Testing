package operias.mutated;

import java.util.ArrayList;

public class Line {

	private int number;
	private String type; //add or update
	private boolean testCoverage;
	private String newLine;
	private String oldLine;
	private int survivedMutants;
	private int killedMutants;
	private int noCoverageMutants;
	private String blueOutput;
	private String previousCommitID;
	private String previousCommitMutationReportPath;
	
	private ArrayList<Mutation> survivedMutantList;
	
	public Line(int number, String type, boolean testCoverage, String newLine, String oldLine){
		
		 this.number=number;
		 this.type= type; //add or update
		 this.testCoverage=testCoverage;
		 this.newLine=newLine;
		 this.oldLine=oldLine;
		 survivedMutants=0;
		 killedMutants=0;
		 noCoverageMutants=0;
		 survivedMutantList = new ArrayList<Mutation>();
		 
		 if(type.equals("UPDATE")){
			 OPi.rememberUpdatedLine(this);
		 }
	}
	
	
	public void setSurvivedMutantList(ArrayList<Mutation> survivedMutantList){
		this.survivedMutantList = survivedMutantList;
	}
	
	public ArrayList<Mutation> getSurvivedMutantList(){
		return survivedMutantList;
	}
	
	public void incrementSuvived(){
		survivedMutants++;
	}
	
	public void incrementKilled(){
		killedMutants++;
	}
	
	public void incrementNoCoverage(){
		noCoverageMutants++;
	}
	
	
	public int getSuvived(){
		return survivedMutants;
	}
	
	public int getKilled(){
		return killedMutants;
	}
	
	public int getNoCoverage(){
		return noCoverageMutants;
	}
		
	
	public int getNumber() {
		return number;
	}
	
	public String getType() {
		return type;
	}
	
	public boolean hasTestCoverage() {
		return testCoverage;
	}
	
	public String getNewLine() {
		return newLine;
	}
	
	public String getOldLine() {
		return oldLine;
	}
	

	public String getBlueOutput() {
		return blueOutput;
	}


	public void setBlueOutput(String blueOutput) {
		this.blueOutput = blueOutput;
	}


	public String getPreviousCommitID() {
		return previousCommitID;
	}


	public void setPreviousCommitID(String previousCommitID) {
		this.previousCommitID = previousCommitID;
	}


	public String getPreviousCommitMutationReportPath() {
		return previousCommitMutationReportPath;
	}


	public void setPreviousCommitMutationReportPath(String previousCommitMutationReportPath) {
		this.previousCommitMutationReportPath = previousCommitMutationReportPath;
	}
	
}
