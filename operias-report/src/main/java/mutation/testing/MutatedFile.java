package mutation.testing;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import operias.Main;
import operias.report.OperiasFile;

public class MutatedFile {

	private String systemFileName;
	private String mutationReportPath;
	private String commitID;
	private ArrayList<Integer> diffCoveredLines;
	private ArrayList<InflexionPoint> inflexionPoints;
	
	public MutatedFile(String fileName, ArrayList<Integer> diffCoveredLines, String commitID){
		this.diffCoveredLines = diffCoveredLines;
		this.systemFileName = fileName;
		this.commitID = commitID;
		inflexionPoints = new ArrayList<InflexionPoint>();
	}
	
	
	
	public void setMutationReportPath(String path){
		String[] tokens = systemFileName.split("/");
		String mutationReportPath = path+"/"+ tokens[4]+"."+tokens[5]+"/"+tokens[6]+".html";
		this.mutationReportPath = mutationReportPath;
		Main.printLine("[OPi+][INFO] computing path to the pitest report for "+tokens[6]+"  "+mutationReportPath);
		
		FileWriter.setFileName(tokens[6]);
		FileWriter.setPath(systemFileName);
		
		extractMutationReport();
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
	
	private void extractMutationReport() {
		File mutationReport = new File(this.mutationReportPath);
		
		Document doc;
		try {
			doc = Jsoup.parse(mutationReport, "UTF-8", "http://example.com/");
			//these are lines that have mutants created fot them and are covered my tests
			Elements coveredContent = doc.getElementsByAttributeValue("class", "covered");
				
			//these are lines that have mutants created for them BUT there is no test coverage
			Elements noTestCoverageContent = doc.getElementsByAttributeValue("class", "uncovered");
				
			//these lines dont have any mutant to create for them. only code line available.
			Elements noAvailableOperator = doc.getElementsByAttributeValue("class", "na");
			
			
			
			
			if(noAvailableOperator.size()>0){
				//TODO if line has no mutants BUT was changed then it has BLUE-2 output!!
				//ALSO talk to Arie: if line has mutants but no test coverage do we considet that critical area that should be tested? do we take it into consideration for the evaluation??
				
				Main.printLine("[OPi+][BLUE-2??]");
				
			}
			
			
			//actual covered content = size/2;
			parseSetToInflexionPoints(coveredContent,diffCoveredLines);
			
		
			Main.printLine("[OPi+][INFO] Total number of inflexion points: "+inflexionPoints.size());	
			
			
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}
	
	private void parseSetToInflexionPoints(Elements content, ArrayList<Integer> diffCoveredLines){
		int counter =0;
		while(counter<content.size()){
			int codeLineNumber = Integer.parseInt(content.get(counter).ownText());
			if(diffCoveredLines.contains(codeLineNumber)){
				//process mutants for this line of code (changed and also covered)
				Element mutationInfo = content.get(counter).nextElementSibling();
				ArrayList<Mutation> survivingMutants = new ArrayList<Mutation>();
				
				FileWriter.setLineNumber(codeLineNumber);
				
				if(!mutationInfo.text().isEmpty()){  //line has mutants
					survivingMutants = processMutantsInfo(mutationInfo.text());
					counter++;
					String codeLine = content.get(counter).text();
					
					FileWriter.setNewCodeLine(codeLine);
					FileWriter.setBlueOutput("5");
					inflexionPoints.add(new InflexionPoint(codeLineNumber, codeLine, survivingMutants));
					Main.printLine("[OPi+][BLUE-5] created new inflexion point "+codeLineNumber);
					
				}else{
					counter++;
					//TODO expand this to blue 2 and blue 3
					FileWriter.setNewCodeLine(content.get(counter).ownText());
					FileWriter.setBlueOutput("2/3");
					FileWriter.writeInFile("");
					Main.printLine("[OPi+][BLUE-2/3] there are no mutants for "+codeLineNumber+"   "+content.get(counter).ownText());
				}
			}else{
				counter++;
			}
			counter++;
		}
	}
	
	
	private ArrayList<Mutation> processMutantsInfo(String info){
		
		
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
			}else{
				status = "KILLED";
			}
			//OBS: i do not record killed mutants. not in the scope of this exercise
		}
		return mutations;
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
}
