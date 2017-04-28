package operias;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import operias.coverage.Cobertura;
import operias.coverage.CoverageReport;
import operias.diff.DiffReport;
import operias.mutated.OPi;
import operias.mutated.exceptions.ExitRequiredException;
import operias.mutated.exceptions.PiTestException;
import operias.mutated.exceptions.SystemException;
import operias.output.html.HTMLReport;
import operias.output.xml.XMLReport;
import operias.report.OperiasReport;

/**
 * Base class of the tool, where the information is combined and the reports and html sites are generated
 * @author soosterwaal
 *
 */
public class Operias {

	OperiasReport report;
	
	CoverageReport reportRevised;
	
	CoverageReport reportOriginal;
	
	DiffReport reportFileDiff;
	
	static boolean clock = true;
	
	Thread reportRevisedThread, reportOriginalThread, reportFileDiffThread;
	/**
	 * Construct a report based on the difference in source files and coverage between the two folders in the configuration
	 * @return Operias instance
	 * @throws PiTestException 
	 * @throws SystemException 
	 */
	public Operias constructReport() throws ExitRequiredException, PiTestException, SystemException{

		if (Configuration.getOriginalDirectory() == null || Configuration.getRevisedDirectory() == null) {
			Main.printLine("[Error] Missing either the original or the revised directory");
			//System.exit(OperiasStatus.MISSING_ARGUMENTS.ordinal());
			throw new ExitRequiredException(OperiasStatus.MISSING_ARGUMENTS);
		}
		
		Main.printLine("[Info] Setting up threads");
		// Construct the cobertura reports
		reportRevisedThread = new Thread("RevisedCoverage") { public void run() {try {
			reportRevised = constructCoberturaReport(Configuration.getRevisedDirectory());
		} catch (ExitRequiredException e) {
			clock=false;
		}
																			}};
																			
		reportOriginalThread = new Thread("OriginalCoverage") { public void run() { 
																				try {
																					reportOriginal = constructCoberturaReport(Configuration.getOriginalDirectory());
																				} catch (ExitRequiredException e) {
																					clock=false;
																				}
																			}};
		reportFileDiffThread = new Thread("DiffReport") { public void run() {
				try {
					reportFileDiff = new DiffReport(Configuration.getOriginalDirectory(), Configuration.getRevisedDirectory());
				} catch (IOException e) {
					clock=false;
				}
			
				//Main.printLine("[Info] [" + Thread.currentThread().getName() + "] Error while comparing directory \"" +Configuration.getRevisedDirectory() + "\" to \"" + Configuration.getOriginalDirectory()+ "\"");
				//System.exit(OperiasStatus.ERROR_FILE_DIFF_REPORT_GENERATION.ordinal());
				
		}};
		

		Main.printLine("[Info] Starting threads");
		clock = true;
		
		reportRevisedThread.start();
		reportOriginalThread.start();
		reportFileDiffThread.start();
		
		try {
			
			reportRevisedThread.join();
			reportOriginalThread.join();
			reportFileDiffThread.join();
			
			
			Main.printLine("[Info] Start to combine reports");
			
			Main.printLine("-----------------------------output from threads after join");
			if(clock){
				
				report = new OperiasReport(reportOriginal, reportRevised, reportFileDiff);
				
				Main.printLine("[[OPi+]]----START---------------------------------------------");
				new OPi(report);
				Main.printLine("[[OPi+]]----END---------------------------------------------");
			}
						
		} catch (InterruptedException e1) {
			//System.exit(OperiasStatus.ERROR_THREAD_JOINING.ordinal());
			Main.printLine("[Operias] error when joining threads");
			throw new ExitRequiredException(OperiasStatus.ERROR_THREAD_JOINING);
		}
		
		
		return this;
	}
	
	
	/**
	 * Construct a cobertura coverage report for the given directory
	 * @param baseDirectory Directory containing the source code which needs to be checked for coverage
	 * @param destinationDirectory Destination folder for the result
	 * @param dataFile Data file used
	 * @return A cobertura report containing coverage metrics
	 */
	private CoverageReport constructCoberturaReport(String baseDirectory) throws ExitRequiredException{
		
		Cobertura cobertura = new Cobertura(baseDirectory);
		
		CoverageReport report = cobertura.executeCobertura();
		
		if (report == null) {
			//System.exit(OperiasStatus.ERROR_COBERTURA_TASK_EXECUTION.ordinal());
			throw new ExitRequiredException(OperiasStatus.ERROR_COBERTURA_TASK_EXECUTION);
		}
		
		return report;
	}
	
	
	/**
	 * Write a site based on the report
	 * @return Operias instance
	 */
	public Operias writeHTMLReport() {
		
		Main.printLine("[Info] Start writing data to html report");
		try {
			(new HTMLReport(report)).generateReport();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return this;
	}
	
	/**
	 * Write a xml report based on the operias report
	 * @return This operias instance
	 */
	public Operias writeXMLReport() {
		
		Main.printLine("[Info] Start writing data to xml report");
		
		try {
			(new XMLReport(report)).generateReport();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return this;
	}
}
