package operias.mutated;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import operias.Configuration;
import operias.Main;
import operias.mutated.exceptions.FileException;
import operias.mutated.exceptions.SystemException;
import operias.mutated.record.files.EvaluationFileWriter;

public class MutatedFile {

	private String systemFileName;
	private String fileName;
	private String mutationReportPath;
	private String commitID;
	private ArrayList<Line> diffLines;
	private ArrayList<Integer> diffCoveredLines;
	
	
	public MutatedFile(String fileName, ArrayList<Line> diffLines, String commitID){
		this.diffLines = diffLines;
		this.systemFileName = fileName;
		this.commitID = commitID;
		this.fileName = "";
		this.diffCoveredLines = new ArrayList<Integer>();
		for(Line currentLine: diffLines){
			if(currentLine.hasTestCoverage()){
				this.diffCoveredLines.add(currentLine.getNumber());
			}
		}
	}
	
	public ArrayList<Line> getDiffLines(){
		return diffLines;
	}
	
	public int getFileCommitImpact(){
		return diffLines.size();
	}
	
	public void setMutationReportPath(String path) throws SystemException, FileException{
		String[] tokens = systemFileName.split("/");
		
		//TODO jsoup hack
		String mutationReportPath = null;
		if(tokens.length==8){
			mutationReportPath = path+"/"+ tokens[4]+"."+tokens[5]+"."+tokens[6]+"/"+tokens[7]+".html";
			fileName=tokens[7];
		}else if(tokens.length==7){
			mutationReportPath = path+"/"+ tokens[4]+"."+tokens[5]+"/"+tokens[6]+".html";
			fileName=tokens[6];
		}else{
			throw new FileException(commitID, "Can`t parse path to mutation report");
		}
		
		this.mutationReportPath = mutationReportPath;
		Main.printLine("[OPi+][INFO] computing path to the pitest report for "+tokens[6]+"  "+mutationReportPath);
	}
	
	

	public String getSystemFileName() {
		return systemFileName;
	}
	
	public String getCommitID() {
		return commitID;
	}
	
	
	public ArrayList<Integer> getDiffCoveredLines() {
		return diffCoveredLines;
	}
	
	public String getMutationReportPath() {
		return mutationReportPath;
	}
	
	public void extractMutationReport() throws SystemException {
		File mutationReport = new File(this.mutationReportPath);
		Document doc;
		try {
			doc = Jsoup.parse(mutationReport, "UTF-8", "http://example.com/");
			//these are lines that have mutants created for them and are covered my tests
			Elements coveredContent = doc.getElementsByAttributeValue("class", "covered");
				
			//these are lines that have mutants created for them BUT there is no test coverage
			Elements noTestCoverageContent = doc.getElementsByAttributeValue("class", "uncovered");
				
			//these lines dont have any mutant to create for them. only code line available.
			Elements noAvailableOperator = doc.getElementsByAttributeValue("class", "na");
			
			
			//process all coveredContent
			int counter =0;
			while(counter<coveredContent.size()){
				int codeLineNumber = Integer.parseInt(coveredContent.get(counter).ownText());
				Line line = lineArrayContains(diffLines, codeLineNumber);
				if(line != null){
					processChangedCoveredMutants(counter, coveredContent, line);
					counter++;
				}else{
					counter++;
				}
				counter++;
			}
			
			
			//process noTestCoverageContent
			counter =0;
			while(counter<noTestCoverageContent.size()){
				int codeLineNumber = Integer.parseInt(noTestCoverageContent.get(counter).ownText());
				Line line = lineArrayContains(diffLines, codeLineNumber);
				if(line != null){
					//TODO test
					
					//>0 no coverage mutants => blue 1
					line.setBlueOutput("1");
					//otherwise blue 2/3
					
					counter++;
				}else{
					counter++;
				}
				counter++;
			}
			
			
			//process noAvailableOperator
			counter =0;
			while(counter<noAvailableOperator.size()){
				int codeLineNumber = Integer.parseInt(noAvailableOperator.get(counter).ownText());
				Line line = lineArrayContains(diffLines, codeLineNumber);
				if(line != null){
					//TODO test
					line.setBlueOutput("2/3");					
					counter++;
				}else{
					counter++;
				}
				counter++;
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw new SystemException(Configuration.getRevisedCommitID(), "could not parse OPi+ mutation report", e);
		}	
	}
	
	
	
	

	private void processChangedCoveredMutants(int counter, Elements coveredContent, Line line ) throws SystemException {
		//process mutants for this line of code (changed and also covered)
		Element mutationInfo = coveredContent.get(counter).nextElementSibling();
		ArrayList<Mutation> survivingMutants = new ArrayList<Mutation>();
		
		if(!mutationInfo.text().isEmpty()){  //line has mutants
			processMutantsInfo(mutationInfo.text(), line);
			counter++;
		}else{
			counter++;
			//TODO expand this to blue 2 and blue 3
			line.setBlueOutput("2/3");
			Main.printLine("[OPi+][BLUE-2/3] there are no mutants for "+line.getNumber()+"   "+coveredContent.get(counter).ownText());
		}
		
	}

	private Line lineArrayContains(ArrayList<Line> lines, int lineNumber){
		for(Line line: lines){
			if(line.getNumber()==lineNumber){
				return line;
			}
		}
		return null;
	}
	
	
	private void processMutantsInfo(String info, Line line) throws SystemException{
		
		
		int numberOfMutantsForLine = Integer.parseInt(info.split(" ")[0]);
		ArrayList<Mutation> mutations = new ArrayList<Mutation>();
		String[] descriptions = info.split(":");
		for(int i=1; i<descriptions.length;i++){
			String description = descriptions[i];
			String status;
			if(description.contains("SURVIVED")){
				status = "SURVIVED";
				int endOfDescription = description.indexOf(status)-3;
				String mutantDescription = description.substring(0, endOfDescription);
				String mutantName = parseDescriptionToMutantName(description);
				mutations.add(new Mutation(mutantName, mutantDescription, status));
				line.incrementSuvived();
			}else if (description.contains("KILLED")){
				line.incrementKilled();
			//TODO test this
			}else if(description.contains("NO COVERAGE")){
				line.incrementNoCoverage();
			}else{
				throw new SystemException(Configuration.getRevisedCommitID(), "this shouldnot happend. these mutants are covered??", new Exception("mutant should be covered here"));
			}
		}
		if(mutations.isEmpty()){
			line.setBlueOutput("4");
			Main.printLine("[OPi+][BLUE-4] all mutants are killed by tests for "+line.getNumber());
		}else{
			line.setSurvivedMutantList(mutations);
			line.setBlueOutput("5");
			Main.printLine("[OPi+][BLUE-5] we have surviving mutants for line "+line.getNumber());
		}
		
	}
	
	

	private String parseDescriptionToMutantName(String description){
		
		String mutantName = null;
		String mutationDescription;
		
		if(description.contains("changed conditional boundary")){
			mutantName = "CONDITIONALS_BOUNDARY_MUTATOR";
			mutationDescription = "changed conditional boundary";
		}else if(description.contains("replaced call to ")){
			mutantName = "ARGUMENT_PROPAGATION_MUTATOR";
			mutationDescription = "replaces the result of the method call with a parameter";
		}else if(description.contains("Changed increment from")){
			mutantName = "INCREMENTS_MUTATOR";
			mutationDescription = "Changed increment";
		}else if(description.contains("Substituted")){
			mutantName = "INLINE_CONSTANT_MUTATOR";
			mutationDescription = "Substituted constant with replacement";
		}else if(description.contains("Replaced")){
			mutantName = "MATH_MUTATOR";
			mutationDescription = "Replaced math operations";
		}else if(description.contains("negated conditional")){
			mutantName = "NEGATE_CONDITIONALS_MUTATOR";
			mutationDescription = "negated conditional";
		}else if(description.contains("removed conditional - replaced") && description.contains(" check with")){
			mutantName = "REMOVE_CONDITIONALS_MUTATOR";
			mutationDescription = "removed conditional - replaced";
		}else if(description.contains("mutated return of Object value for")){
			mutantName = "RETURN_VALS_MUTATOR";
			mutationDescription = "mutated return of Object value for ?  to ( if (x != null) null else throw new RuntimeException )";
		}else if(description.contains("Removed increment")){
			mutantName = "REMOVE_INCREMENTS_MUTATOR";
			mutationDescription = "Removed increment";
		}else if(description.contains("RemoveSwitch")){
			mutantName = "REMOVE_SWITCH_MUTATOR_";
			mutationDescription = "RemoveSwitch mutation";
		}else if(description.contains("Switch mutation")){
			mutantName = "SWITCH_MUTATOR";
			mutationDescription = "Switch mutation";
		}else if(description.contains("Removed assignment to member variable")){
			mutantName = "MEMBER_VARIABLE_MUTATOR";
			mutationDescription = "Removed assignment to member variable";
		}else if(description.contains("removed negation")){
			mutantName = "INVERT_NEGS_MUTATOR";
			mutationDescription = "removed negation";
		}else if(description.contains("removed call to")){
			mutantName = "METHOD_CALL_VISITOR";
			mutationDescription = "Removed a method call. This is one of 3 posible mutants that have the same description: NON_VOID_METHOD_CALL_MUTATOR, VOID_METHOD_CALL_MUTATOR or CONSTRUCTOR_MUTATOR";
		}
		return mutantName;
	}
	
	public void print(){
		System.out.println("[OPi+] Mutated file:.......................");
		System.out.println(systemFileName);
		System.out.println(diffCoveredLines);
		
	}

	public String getFileName() {
		return fileName;
	}

	
	
	/*
	 private void parseSetToInflexionPoints(Elements content, ArrayList<Integer> diffCoveredLines){
		int counter =0;
		while(counter<content.size()){
			int codeLineNumber = Integer.parseInt(content.get(counter).ownText());
			
			if(diffCoveredLines.contains(codeLineNumber)){
				//process mutants for this line of code (changed and also covered)
				Element mutationInfo = content.get(counter).nextElementSibling();
				ArrayList<Mutation> survivingMutants = new ArrayList<Mutation>();
				
				EvaluationFileWriter.setLineNumber(codeLineNumber);
				
				if(!mutationInfo.text().isEmpty()){  //line has mutants
					survivingMutants = processMutantsInfo(mutationInfo.text());
					counter++;
					String codeLine = content.get(counter).text();
					
					EvaluationFileWriter.setNewCodeLine(codeLine);
					EvaluationFileWriter.setBlueOutput("5");
					inflexionPoints.add(new InflexionPoint(codeLineNumber, codeLine, survivingMutants));
					Main.printLine("[OPi+][BLUE-5] created new inflexion point "+codeLineNumber);
					
				}else{
					counter++;
					//TODO expand this to blue 2 and blue 3
					EvaluationFileWriter.setNewCodeLine(content.get(counter).ownText());
					EvaluationFileWriter.setBlueOutput("2/3");
					EvaluationFileWriter.writeInFile("");
					Main.printLine("[OPi+][BLUE-2/3] there are no mutants for "+codeLineNumber+"   "+content.get(counter).ownText());
				}
			}else{
				counter++;
			}
			counter++;
		}
	}
	
	
	 * 
	 */
	
}
