package operias.git;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import operias.Configuration;

public class GitTest {

	@Before
	public void setUp() {
		Configuration.setTemporaryDirectory(new File("").getAbsolutePath() + "/target/gittests");
	}
	
	@After
	public void tearDown() {
		try {
			FileUtils.deleteDirectory(new File(Configuration.getTemporaryDirectory()));
		} catch (IOException e) {
			fail(e.getMessage());
		}
		Configuration.setTemporaryDirectory(new File("").getAbsolutePath() + "/temp");
	}
	
	/**
	 * Test succesfully cloning a git directory
	 */
	@Test
	public void testCloningAndCheckingOut() {
		String gitDirectory = null;
		try {
			gitDirectory = Git.clone("https://github.com/soosterwaal/operias.git");
			
			assertTrue(new File(gitDirectory, ".git").exists());
		} catch (Exception e) {
			fail(e.getMessage());
		}
		
		try {
			// checkout to inital commit of the project
			Git.checkout(gitDirectory, "8e201ca8e50761cbefe880ceae527fa2393ae2e0");
			
			// Check if the head is set correctly
			File HEADfile = new File(gitDirectory, "/.git/HEAD");
			FileInputStream fis = new FileInputStream(HEADfile);
			byte[] data = new byte[(int)HEADfile.length()];
			fis.read(data);
			fis.close();
			
			assertEquals("8e201ca8e50761cbefe880ceae527fa2393ae2e0\n",new String(data, "UTF-8"));
		} catch(Exception e) {
			fail(e.getMessage());
		}
	}
	
	/**
	 * Test the failure of cloning a non-existing repo
	 */
	@Test 
	public void testCloningFail() {
		boolean exceptionThrown = false;
		try {
			Git.clone("https://github.com/non-existing-git-repo");
		} catch (Exception e) {
			exceptionThrown = true;
		}
		
		assertTrue(exceptionThrown);
	}
	
	/**
	 * Test checking out a non-existing branch
	 */
	@Test
	public void testCheckoutFail() {
		boolean exceptionThrown = false;
		String gitDirectory  = "";
		try {
			gitDirectory = Git.clone("https://github.com/soosterwaal/operias.git");
			
			assertTrue(new File(gitDirectory, ".git").exists());
		} catch(Exception e) {
			fail(e.getMessage());
		}
		
		try {
			Git.checkout(gitDirectory, "ARANDOMBRANCHWHICHCANNOTEXIST");
		} catch (Exception e) {
			exceptionThrown = true;
		}
		assertTrue(exceptionThrown);
	}
	
}
