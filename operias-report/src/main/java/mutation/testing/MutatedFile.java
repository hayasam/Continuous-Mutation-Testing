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
			
			//TODO talk about lines that are changed but not covered => add warning	
			
			//actual covered content = size/2;
			parseSetToInflexionPoints(noTestCoverageContent,diffCoveredLines);
			
		
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
				if(!mutationInfo.text().isEmpty()){  //line has mutants
					survivingMutants = processMutantsInfo(mutationInfo.text());
					counter++;
					String codeLine = content.get(counter).text();
					inflexionPoints.add(new InflexionPoint(codeLineNumber, codeLine, survivingMutants));
					Main.printLine("[OPi+][BLUE-5] created new inflexion point "+codeLineNumber);
					
				}else{
					counter++;
					//TODO expand this to blue 2 and blue 3
					Main.printLine("[OPi+][BLUE-2/3] there are no mutants for "+codeLineNumber+"   "+content.get(counter).ownText());
				}
			}else{
				counter++;
			}
			counter++;
		}
	}
	
	
	private ArrayList<Mutation> processMutantsInfo(String info){
		
		String[] tokens = info.split(" ");
		int numberOfMutantsForLine = Integer.parseInt(info.split(" ")[0]);
		ArrayList<Mutation> mutations = new ArrayList<Mutation>();
		
		String[] descriptions = info.split("\\.");
		for(int i=1; i<descriptions.length;i++){
			String description = descriptions[i];
			String status;
			if(description.contains("SURVIVED")){
				status = "SURVIVED";
				int startOfDescription = description.indexOf(":")+1;
				int endOfDescription = description.indexOf(status)-3;
				String mutantDescription = description.substring(startOfDescription, endOfDescription);
				String mutantName = parseDescriptionToMutantName(description);
				mutations.add(new Mutation(mutantName, mutantDescription, status));
			}else{
				status = "KILLED";
			}
			//OBS: i do not record killed mutants. not in the scope of this exercise
		}
		return mutations;
	}
	
	

	//TODO not all operators have valid condition nor unique
	private String parseDescriptionToMutantName(String description){
		
		String mutantName = null;
		String mutationDescription;
		
		if(description.contains("changed conditional boundary")){
			mutantName = "CONDITIONALS_BOUNDARY_MUTATOR";
			mutationDescription = "changed conditional boundary";
		}else if(description.contains("replaced call to ")){
			mutantName = "ARGUMENT_PROPAGATION_MUTATOR";
			mutationDescription = "replaces the result of the method call with a parameter";
		}else if(description.contains("removed call")){
			mutantName = "CONSTRUCTOR_CALL_MUTATOR";
			mutationDescription = "removed call";
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
		}else if(description.contains("removed conditional - replaced")){
			mutantName = "NON_VOID_METHOD_CALL_MUTATOR";
			mutationDescription = "";
		}else if(description.contains("removed conditional - replaced")){
			mutantName = "REMOVE_CONDITIONALS_MUTATOR";
			mutationDescription = "removed conditional - replaced";
		}else if(description.contains("mutated return of Object value for")){
			mutantName = "RETURN_VALS_MUTATOR";
			mutationDescription = "mutated return of Object value for ?  to ( if (x != null) null else throw new RuntimeException )";
		}else if(description.contains("")){
			mutantName = "VOID_METHOD_CALL_MUTATOR";
			mutationDescription = "";
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
		}else if(description.contains("")){
			mutantName = "INVERT_NEGS_MUTATOR";
			mutationDescription = "";
		}
		return mutantName;
	}
	
	public void print(){
		System.out.println("[OPi+] Mutated file:.......................");
		System.out.println(systemFileName);
		System.out.println(diffCoveredLines);
		
	}
}
