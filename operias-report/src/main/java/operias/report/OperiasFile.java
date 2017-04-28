package operias.report;

import java.util.LinkedList;

import difflib.ChangeDelta;
import difflib.DeleteDelta;
import difflib.Delta;
import difflib.InsertDelta;
import operias.Main;
import operias.OperiasStatus;
import operias.coverage.CoberturaClass;
import operias.coverage.CoberturaLine;
import operias.diff.DiffFile;
import operias.diff.SourceDiffState;
import operias.mutated.exceptions.ExitRequiredException;
import operias.report.change.ChangeSourceChange;
import operias.report.change.CoverageDecreaseChange;
import operias.report.change.CoverageIncreaseChange;
import operias.report.change.DeleteSourceChange;
import operias.report.change.InsertSourceChange;
import operias.report.change.OperiasChange;
import operias.report.change.SourceChange;

/**
 * Operias File class.
 * 
 * An operias file contains the information for a class in the project.
 * The changes consists of 5 types, CoverageDecreaseChange, CoverageIncreaseChange,
 * DeleteSourceChange, InsertSourceChange and ChangeSourceChange. 
 * These changes are a result of combining the information from cobertura and the source diff report.
 * 
 * @author soosterwaal
 *
 */
public class OperiasFile {
	
	/**
	 * Package name
	 */
	private String packageName;
	
	/**
	 * Class name
	 */
	private String className;
	
	/**
	 * Changes of the file
	 */
	private LinkedList<OperiasChange> changes;
	
	/**
	 * Original class coverage information
	 */
	private CoberturaClass originalClass;
	
	/**
	 * New class coverage information
	 */
	private CoberturaClass revisedClass;
	
	/**
	 * Source diff information
	 */
	private DiffFile sourceDiff;
	
	/**
	 * Construct a new operias file diff for the given class, which should be a new or deleted class
	 * @param cClass
	 */
	public OperiasFile(CoberturaClass cClass, DiffFile sourceDiff) {
		
		this.className = cClass.getName();
		this.packageName = cClass.getPackageName();
		this.changes = new LinkedList<OperiasChange>();
		this.originalClass = null;
		this.revisedClass = null;
		this.sourceDiff = sourceDiff;
		if (sourceDiff != null) {
			
			if (sourceDiff.getSourceState() == SourceDiffState.NEW) {
				this.revisedClass = cClass;
				InsertSourceChange insertChange = new InsertSourceChange(1, 1, (InsertDelta) sourceDiff.getChanges().get(0));
				
				for(int i = 0; i < sourceDiff.getChanges().get(0).getRevised().getLines().size(); i++) {
					// Add offset
					CoberturaLine line = revisedClass.tryGetLine(i + 1);
					
					if (line != null) {
						insertChange.addRevisedCoverageLine(line.isCovered());
					} else {
						insertChange.addRevisedCoverageLine(null);
					}
				}
				changes.add(insertChange);
			} else {
				this.originalClass = cClass;
				DeleteSourceChange deleteChange = new DeleteSourceChange(1, 1, (DeleteDelta) sourceDiff.getChanges().get(0));
				
				for(int i = 0; i < sourceDiff.getChanges().get(0).getRevised().getLines().size(); i++) {
					// Add offset
					CoberturaLine line = originalClass.tryGetLine(i + 1);
					
					if (line != null) {
						deleteChange.addOriginalCoverageLine(line.isCovered());
					} else {
						deleteChange.addOriginalCoverageLine(null);
					}
				}
				changes.add(deleteChange);
				
			}
		} else {
			Main.printLine("[Warning] No source differences were found for file: " + cClass.getFileName());
		}
		
	}
	
	/**
	 * Construct a new operias file diff for the changes
	 * @param originalClass
	 * @param revisedClass
	 * @param sourceDiff
	 * @throws ExitRequiredException 
	 */
	public OperiasFile(CoberturaClass originalClass, CoberturaClass revisedClass, DiffFile sourceDiff) throws ExitRequiredException {
		this.className = originalClass.getName();
		this.packageName = originalClass.getPackageName();
		this.changes = new LinkedList<OperiasChange>();
		this.originalClass = originalClass;
		this.revisedClass = revisedClass;
		this.sourceDiff = sourceDiff;
		
		if (!originalClass.getName().equals(revisedClass.getName())) {
			// Invalid class comparison, may not happen!
			throw new ExitRequiredException(OperiasStatus.ERROR_OPERIAS_DIFF_INVALID_CLASS_COMPARISON);
			//System.exit(OperiasStatus.ERROR_OPERIAS_DIFF_INVALID_CLASS_COMPARISON.ordinal());
		}
		
		CompareLines(1, 1);
	}
	
	/**
	 * Compare lines with each other
	 * @param originalClassLine Line in the original class coverage information
	 * @param revisedClassLine Line in the new class coverage information
	 * @throws ExitRequiredException 
	 */
	public void CompareLines(int originalClassLine, int revisedClassLine) throws ExitRequiredException {
		if (originalClassLine > originalClass.getMaxLineNumber() && 
				revisedClassLine > revisedClass.getMaxLineNumber()) {
			return;
		}
		
		// Source diff starts at lines 0
		Delta change = sourceDiff.tryGetChange(originalClassLine - 1, revisedClassLine - 1);
		
		if (change == null) {
			// No source diff, check the coverage difference between the lines
			CoberturaLine originalLine = originalClass.tryGetLine(originalClassLine);
			CoberturaLine revisedLine = revisedClass.tryGetLine(revisedClassLine);
			
			if (originalLine == null ^ revisedLine == null) {
				// This can be the case, if a constructor has been removed!
				// The class line will be marked as either covered or uncovered, but no information was known about the other class
				
				if (originalLine == null && revisedLine.isCovered() || revisedLine == null && originalLine.isCovered()) {
					changes.add(new CoverageIncreaseChange(originalClassLine, revisedClassLine));
				} else {
					changes.add(new CoverageDecreaseChange(originalClassLine, revisedClassLine));
				}
			} else if (originalLine != null) {
				// Lines found, compare!
				if (originalLine.isCondition() ^ revisedLine.isCondition()) {
					// Again something went wrong i suppose... no change in the line, so is either should both be conditions or not
					throw new ExitRequiredException(OperiasStatus.ERROR_OPERIAS_INVALID_LINE_COMPARISON);
					//System.exit(OperiasStatus.ERROR_OPERIAS_INVALID_LINE_COMPARISON.ordinal());				
				}
				
				if (!originalLine.isCovered() && revisedLine.isCovered()) {
					// Increase delta
					changes.add(new CoverageIncreaseChange(originalClassLine, revisedClassLine));
				} else if (originalLine.isCovered() && !revisedLine.isCovered()) {
					// Decrease delta
					changes.add(new CoverageDecreaseChange(originalClassLine, revisedClassLine));
				}
			}
			CompareLines(originalClassLine + 1, revisedClassLine + 1);	
		} else {
			SourceChange sourceChange = null;
			if (change instanceof InsertDelta) {
				sourceChange = new InsertSourceChange(originalClassLine, revisedClassLine, (InsertDelta) change);
			} else if (change instanceof ChangeDelta) {
				sourceChange = new ChangeSourceChange(originalClassLine, revisedClassLine, (ChangeDelta) change);
			} else {
				sourceChange = new DeleteSourceChange(originalClassLine, revisedClassLine, (DeleteDelta) change);
			}
			
			if (!(change instanceof DeleteDelta)) {
				for(int i = revisedClassLine; i < revisedClassLine + change.getRevised().getLines().size(); i++) {
					CoberturaLine line = revisedClass.tryGetLine(i);
					if (line == null) {
						sourceChange.addRevisedCoverageLine(null);
					} else {
						sourceChange.addRevisedCoverageLine(line.isCovered());
					}
				}
			}
			
			if (!(change instanceof InsertDelta)) {
				for(int i = originalClassLine; i < originalClassLine + change.getOriginal().getLines().size(); i++) {
					CoberturaLine line = originalClass.tryGetLine(i);
					if (line == null) {
						sourceChange.addOriginalCoverageLine(null);
					} else {
						sourceChange.addOriginalCoverageLine(line.isCovered());
					}
				}
			}
			changes.add(sourceChange);
			
			CompareLines(originalClassLine + change.getOriginal().getLines().size(), revisedClassLine + change.getRevised().getLines().size());
		}
		
	}

	/**
	 * @return the changes
	 */
	public LinkedList<OperiasChange> getChanges() {
		return changes;
	}


	/**
	 * @return the packageName
	 */
	public String getPackageName() {
		return packageName;
	}

	/**
	 * @return the className
	 */
	public String getClassName() {
		return className;
	}

	/**
	 * @return the originalClass
	 */
	public CoberturaClass getOriginalClass() {
		return originalClass;
	}

	/**
	 * @return the revisedClass
	 */
	public CoberturaClass getRevisedClass() {
		return revisedClass;
	}

	/**
	 * @return the sourceDiff
	 */
	public DiffFile getSourceDiff() {
		return sourceDiff;
	}
	
	public String toString() {
		return className;
	}
}