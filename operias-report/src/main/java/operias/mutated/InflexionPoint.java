package operias.mutated;

import java.util.ArrayList;

import operias.mutated.record.files.EvaluationFileWriter;

public class InflexionPoint {

	private int codeLineNumber;
	private String codeLine;
	private ArrayList<Mutation> mutations;
	
	public InflexionPoint( int codeLineNumber, String codeLine, ArrayList<Mutation> mutations){
		this.codeLineNumber = codeLineNumber;
		this.codeLine = codeLine;
		this.mutations = mutations;
		
		for(Mutation mutant : mutations){
			EvaluationFileWriter.writeInFile(mutant.getName(), mutant.getDescription(), mutant.getStatus());
		}
		
	}
	
	
	
}