package operias.coverage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Ignore;
import org.junit.Test;

import mutation.testing.ExitRequiredException;
import operias.OperiasStatus;
import operias.test.general.ExitException;
import operias.test.general.NoExitSecurityManager;
@Ignore
public class CoberturaTest {

    private Cobertura cobertura;
    
	/**
	 * Simple test which executes a mvn project, retrieves the report and cleans up afterwards
	 */
	@Test
	public void testCoberturaExecution(){
		cobertura = new Cobertura("src/test/resources/simpleMavenProject");
		cobertura.setOutputDirectory("target/simpleMavenProject");
		try {
			assertNotNull("Executing cobertura failed", cobertura.executeCobertura());
		} catch (ExitRequiredException e) {
			e.printStackTrace();
		}	
	}
	
	/**
	 * Simple failure test when no correct maven project exists in the given folder
	 */
	@Test
	public void testFailedCoberturaExecution() {
		cobertura = new Cobertura("src/test/resources/noMavenProject");
		try {
			assertNull("Executing cobertura failed", cobertura.executeCobertura());
		} catch (ExitRequiredException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Proper execution of the maven project, but the coverage file was not found, can be test by giving a random non existing output directory
	 */
	@Test
	public void testCoverageXMLNotFound() {
        System.setSecurityManager(new NoExitSecurityManager());
		boolean exceptionThrown = false;
		
        cobertura = new Cobertura("src/test/resources/simpleMavenProject");
		cobertura.setOutputDirectory("target/randomFolder");
		try {
			cobertura.executeCobertura();
		}
	    catch (ExitException e) {
	    	exceptionThrown = true;
            assertEquals("Exit status invalid", OperiasStatus.COVERAGE_XML_NOT_FOUND.ordinal(), e.status);
	    } catch (ExitRequiredException e) {
			e.printStackTrace();
		}
		System.setSecurityManager(null);
		assertTrue("No exception was thrown", exceptionThrown);
	}
	
	/**
	 * Test an invalid directory name, see if an error occurs
	 */
	@Test
	public void testInvalidDirectory() {
		System.setSecurityManager(new NoExitSecurityManager());
		boolean exceptionThrown = false;
		
        cobertura = new Cobertura(null);
		try {
			cobertura.executeCobertura();
		}
	    catch (ExitException e) {
	    	exceptionThrown = true;
            assertEquals("Exit status invalid", OperiasStatus.ERROR_COBERTURA_TASK_CREATION.ordinal(), e.status);
	    } catch (ExitRequiredException e) {
			e.printStackTrace();
		}
		
		exceptionThrown = false;
		
		cobertura = new Cobertura("src/../");
		try {
			cobertura.executeCobertura();
		}
	    catch (ExitException e) {
	    	exceptionThrown = true;
            assertEquals("Exit status invalid", OperiasStatus.ERROR_COBERTURA_TASK_OPERIAS_EXECUTION.ordinal(), e.status);
	    } catch (ExitRequiredException e) {
			e.printStackTrace();
		}

		assertTrue("No exception was thrown", exceptionThrown);
		
		System.setSecurityManager(null);
	}
	
}
