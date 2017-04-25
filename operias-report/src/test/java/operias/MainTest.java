package operias;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import operias.test.general.NoExitSecurityManager;
@Ignore
public class MainTest {
	
    Main main;
	
	/**
	 * Set up the test case
	 */
	@Before
	public void setUp() {		
		main = new Main();
        System.setSecurityManager(new NoExitSecurityManager());
        Configuration.resetConfiguration();
	}
	
	/**
	 * Reset the security manager
	 */
	@After
	public void tearDown() {
        System.setSecurityManager(null);		
	}
	
	@Test
	/**
	 * Test operis for a multi module maven project
	 */
	public void testFunctionalMultiModule() {
		
		try {
			new File(new File("").getAbsolutePath() + "/target/testsProjects/multiProject1").mkdirs();
			FileUtils.copyDirectory(
				new File(new File("").getAbsolutePath() +"/src/test/resources/multiProject1"), 
				new File(new File("").getAbsolutePath() +"/target/testsProjects/multiProject1"));
			
			new File(new File("").getAbsolutePath() +"/target/testsProjects/multiProject2").mkdirs();
			FileUtils.copyDirectory(
				new File(new File("").getAbsolutePath() +"/src/test/resources/multiProject2"), 
				new File(new File("").getAbsolutePath() +"/target/testsProjects/multiProject2"));
		} catch (IOException e) {
			fail(e.getMessage());
		}
		
		String tempDirectory = new File("").getAbsolutePath() + "/target/temp" + Calendar.getInstance().getTime().getTime();
		String destDirectory = new File("").getAbsolutePath() + "/target/mainTestMulti" + Calendar.getInstance().getTime().getTime();
		assertFalse(new File(tempDirectory).exists());
		String[] arguments = new String[] {
				"--original-directory", new File("").getAbsolutePath() + "/target/testsProjects/multiProject1",
				"--revised-directory", new File("").getAbsolutePath() + "/target/testsProjects/multiProject2",
				"--temp-directory", tempDirectory,
				"--destination-directory", destDirectory
		};
		
		Main.main(arguments);
		
		// Check if th destination directory exists and the temp is deleted correctly
		assertFalse("Temporary directort still exists" , new File(tempDirectory).exists());
		assertTrue("Destination directort does not exists", new File(destDirectory).exists());
		
		// Finnaly, check the content
		assertTrue("Image folder does not exists", new File(destDirectory + "/img").exists());
		assertTrue("1", new File(destDirectory + "/img/arc.png").exists());
		assertTrue("2", new File(destDirectory + "/css").exists());
		assertTrue("3", new File(destDirectory + "/css/style.css").exists());
		assertTrue("4", new File(destDirectory + "/simpleMavenProject.Simple.SAME.html").exists());
		assertTrue("5", new File(destDirectory + "/simpleMavenProject2.Simple2.CHANGED.html").exists());
		assertTrue("6", new File(destDirectory + "/samples.src.test.java.simpleMavenProject.SimpleTest.java.html").exists());
		assertTrue("7", new File(destDirectory + "/samples2.src.test.java.simpleMavenProject2.Simple2Test.java.html").exists());
		assertTrue("8", new File(destDirectory + "/index.html").exists());
		assertTrue("9", new File(destDirectory + "/operias.xml").exists());
	}
	
	/**
	 * Test operis for a multi module maven project
	 */
	@Test
	public void testFunctionalMultiModuleNameChange() {
		
		try {
			new File(new File("").getAbsolutePath() + "/target/testsProjects/multiProjectModuleNameChange1").mkdirs();
			FileUtils.copyDirectory(
				new File(new File("").getAbsolutePath() +"/src/test/resources/multiProjectModuleNameChange1"), 
				new File(new File("").getAbsolutePath() +"/target/testsProjects/multiProjectModuleNameChange1"));
			
			new File(new File("").getAbsolutePath() +"/target/testsProjects/multiProjectModuleNameChange2").mkdirs();
			FileUtils.copyDirectory(
				new File(new File("").getAbsolutePath() +"/src/test/resources/multiProjectModuleNameChange2"), 
				new File(new File("").getAbsolutePath() +"/target/testsProjects/multiProjectModuleNameChange2"));
		} catch (IOException e) {
			fail(e.getMessage());
		}
		
		String tempDirectory = new File("").getAbsolutePath() + "/target/temp" + Calendar.getInstance().getTime().getTime();
		String destDirectory = new File("").getAbsolutePath() + "/target/mainTestMultiNameChange" + Calendar.getInstance().getTime().getTime();
		assertFalse(new File(tempDirectory).exists());
		String[] arguments = new String[] {
				"--original-directory", new File("").getAbsolutePath() + "/target/testsProjects/multiProjectModuleNameChange1",
				"--revised-directory", new File("").getAbsolutePath() + "/target/testsProjects/multiProjectModuleNameChange2",
				"--temp-directory", tempDirectory,
				"--destination-directory", destDirectory
		};
		
		Main.main(arguments);
		
		// Check if th destination directory exists and the temp is deleted correctly
		assertFalse(new File(tempDirectory).exists());
		assertTrue(new File(destDirectory).exists());
	}
	@Test
	/**
	 * Function test for a single module proejct
	 */
	public void testFunctionalSingleModule() {
		try {
			new File(new File("").getAbsolutePath() + "/target/testsProjects/mavenProject1").mkdirs();
			FileUtils.copyDirectory(
				new File(new File("").getAbsolutePath() +"/src/test/resources/mavenProject1"), 
				new File(new File("").getAbsolutePath() +"/target/testsProjects/mavenProject1"));
			
			new File(new File("").getAbsolutePath() +"/target/testsProjects/mavenProject2").mkdirs();
			FileUtils.copyDirectory(
				new File(new File("").getAbsolutePath() +"/src/test/resources/mavenProject2"), 
				new File(new File("").getAbsolutePath() +"/target/testsProjects/mavenProject2"));
		} catch (IOException e) {
			fail(e.getMessage());
		}
		
		String tempDirectory = new File("").getAbsolutePath() + "/target/temp" + Calendar.getInstance().getTime().getTime();
		String destDirectory = new File("").getAbsolutePath() + "/target/mainTestSingle" + Calendar.getInstance().getTime().getTime();
		assertFalse(new File(tempDirectory).exists());
		String[] arguments = new String[] {
				"--original-directory", new File("").getAbsolutePath() + "/target/testsProjects/mavenProject1",
				"--revised-directory", new File("").getAbsolutePath() + "/target/testsProjects/mavenProject2",
				"--temp-directory", tempDirectory,
				"--destination-directory", destDirectory
		};
		
		Main.main(arguments);
		
		// Check if th destination directory exists and the temp is deleted correctly
		assertFalse(new File(tempDirectory).exists());
		assertTrue(new File(destDirectory).exists());
		
		// Finnaly, check the content

		assertTrue(new File(destDirectory + "/img").exists());
		assertTrue(new File(destDirectory + "/img/arc.png").exists());
		assertTrue(new File(destDirectory + "/css").exists());
		assertTrue(new File(destDirectory + "/css/style.css").exists());
		assertTrue(new File(destDirectory + "/example.Calculations.CHANGED.html").exists());
		assertTrue(new File(destDirectory + "/example.deletablePackage.DeletableClass.DELETED.html").exists());
		assertTrue(new File(destDirectory + "/example.Loops.SAME.html").exists());
		assertTrue(new File(destDirectory + "/example.NewClass.NEW.html").exists());
		assertTrue(new File(destDirectory + "/index.html").exists());
		assertTrue(new File(destDirectory + "/moreExamples.Switch.NEW.html").exists());
		assertTrue(new File(destDirectory + "/operias.xml").exists());
		assertTrue(new File(destDirectory + "/src.test.java.example.CalculationsTest.java.html").exists());
		assertTrue(new File(destDirectory + "/src.test.java.example.LoopsTest.java.html").exists());
		assertTrue(new File(destDirectory + "/src.test.java.example.MusicTest.java.html").exists());
		assertTrue(new File(destDirectory + "/src.test.java.moreExamples.SwitchTest.java.html").exists());
	}
	
	
	
}
