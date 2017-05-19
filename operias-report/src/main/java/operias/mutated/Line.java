package operias.mutated;

import java.util.ArrayList;

public class Line {

	private int number;
	private String type; //add or update
	private boolean branchCoverage;
	private String newLine;
	private int survivedMutants;
	private int killedMutants;
	private boolean noCoverageMutants;
	private String blueOutput;
	
	
	private ArrayList<Mutation> survivedMutantList;
	
	public Line(int number, String type, boolean testCoverage, String newLine){
		
		 this.number=number;
		 this.type= type; //add or update
		 this.branchCoverage=testCoverage;
		 this.newLine=newLine;
		 survivedMutants=0;
		 killedMutants=0;
		 noCoverageMutants=false;
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
	
	public void setNoCoverage(boolean value){
		noCoverageMutants=value;
	}
	
	
	public int getSuvived(){
		return survivedMutants;
	}
	
	public int getKilled(){
		return killedMutants;
	}
	
	public boolean getNoCoverage(){
		return noCoverageMutants;
	}
		
	
	public int getNumber() {
		return number;
	}
	
	public String getType() {
		return type;
	}
	
	public boolean hasBranchCoverage() {
		return branchCoverage;
	}
	
	public String getNewLine() {
		return newLine;
	}
	
	

	public String getBlueOutput() {
		return blueOutput;
	}


	public void setBlueOutput(String blueOutput) {
		this.blueOutput = blueOutput;
	}


	
	
}
