package operias.mutated.proxy;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.FileUtils;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import operias.Main;
import operias.mutated.EvaluationRunner;
import operias.mutated.exceptions.IncompatibleProjectException;
import operias.mutated.exceptions.PiTestException;
import operias.mutated.exceptions.SystemException;

public class PitestProxy {
	
	public static ThirdPartyProxySeetings settings;
	private static boolean previousCommit;
	
	public static String getMutationReportFor(String commitID, boolean previousFlag) throws PiTestException, SystemException, IncompatibleProjectException {
		/* due to Pitest run on last commit feature limitation we have to update the head 
		 * each time to run the evaluation = running pitest on specific commit not just the last one
		*/
		GitProxy.changeHeadTo(commitID);
		String mutationPath = null;
		previousCommit=previousFlag;
		//change running local path due to Pitest Bug: hcoles/pitest: Issue #336
        Invoker invoker = new DefaultInvoker();	 
        invoker.setMavenHome(new File(settings.MAVEN_PATH));
        Main.printLine("[OPi+][INFO] set maven home to: "+settings.MAVEN_PATH);
		
        
        //make sure pom file works on JUnit 4.10 and we have scm connection setup
        File pomFile =  new File( settings.pomFile.getAbsolutePath()+"/pom.xml" );
        try {
			updatePomFileForMutationProcess(pomFile);
		} catch (ParserConfigurationException|SAXException|IOException|TransformerException e1) {
				e1.printStackTrace();
				throw new PiTestException(commitID,"could not parse and edit pom file ",previousCommit);
		} catch (Exception e){
				e.printStackTrace();
				throw new PiTestException(commitID,"fault in my logic when parsing the pom file ", previousCommit);
			
		}
        
        
        cleanProject(pomFile, invoker, commitID);
        	//setup Pitest on last commit
    		InvocationRequest request = new DefaultInvocationRequest();
            request.setPomFile(pomFile);
            
            request.setGoals( Collections.singletonList( "org.pitest:pitest-maven:scmMutationCoverage -DanalyseLastCommit -Dmutators=ALL -l logfile.txt" ) );
            //-DtargetTests="+GitProxy.getGroupArtifactPath()+" - used to fix the pitest bug but apparently it works for JSoup
            //mvn org.pitest:pitest-maven:scmMutationCoverage -DanalyseLastCommit -DtargetTests=groupID.artifactID.* -Dmutators=ALL
            
                 
            //run Pitest on last commit
            InvocationResult result;
    		try {
    			 
    			result = invoker.execute( request );
    			if ( result.getExitCode() != 0 )
    	        {
    	            throw new IllegalStateException( "Build failed." );
    	        }else{
    	        	/* the build still succeeds if no mutations are found. 
    	        	 * in this case there is no path for the mutation report 
    	        	 * and there is no need to further analyze
    	        	 */
    	        	File logFilePath =  new File( settings.pomFile.getAbsolutePath()+"/logfile.txt");
    	        	if(isPitestAnalysis(logFilePath, commitID)){
    	        		Main.printLine("[OPi+][INFO] successfully run Pitest on last commit");
    		        	mutationPath = settings.pomFile+"/target/pit-reports";
    		        	mutationPath = getLatestFilefromDir(mutationPath).getAbsolutePath();
    		        	
    		        	Main.printLine("[OPi+][INFO] My MUTATION REPORT path is: "+mutationPath+" for commit "+commitID);
    	        	}
    	        }
    		} catch (MavenInvocationException|IllegalStateException e) {
    			Main.printLine("[OPi+][ERROR] could not run Pitest on last commit");
    			//e.printStackTrace();
    			throw new PiTestException(commitID, "Pitest Build Failed on current commit", previousFlag);
    			
    		}
        	
		return mutationPath;
		
	}
	
	
	private static void cleanProject(File pomFile, Invoker invoker, String commitID) throws PiTestException, IncompatibleProjectException {
		//setup Pitest on last commit
		InvocationRequest request = new DefaultInvocationRequest();
        request.setPomFile(pomFile);
        
        request.setGoals( Collections.singletonList( "clean package -l ignoreLogfile.txt" ) );
        InvocationResult result;
		try {
			result = invoker.execute( request );
			if ( result.getExitCode() != 0 )
	        {
	            throw new IncompatibleProjectException(commitID, "unstable version on master branch: build failed for refreshing the bytecode in last commit" );
	        }
		} catch (MavenInvocationException|IllegalStateException e) {
			Main.printLine("[OPi+][ERROR] could not refresh bytecode for pitest on last commit");
			//e.printStackTrace();
			throw new PiTestException(commitID, "Pitest Build Failed on current commit", false);
		}
	}


	private static boolean isPitestAnalysis(File logFilePath, String commitID) throws SystemException, PiTestException {
		
		try{
			
			if(FileUtils.readFileToString(logFilePath).contains("[INFO] No modified files found - nothing to mutation test")){
				throw new PiTestException(commitID, "Pitest did not detect any change", previousCommit);
			}
			if(FileUtils.readFileToString(logFilePath).contains("No mutations found.")){
				throw new PiTestException(commitID, "Pitest did not generate any mutations", previousCommit);
			}
			return true;
		} catch(IOException e){
			e.printStackTrace();
			throw new SystemException(commitID, "Could not read logFile for Pitest", e);
		}
	}

	public static void updatePomFileForMutationProcess(File pomFile) throws ParserConfigurationException, SAXException, IOException, TransformerException {
		
		//open pom file
	      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	      DocumentBuilder builder;
		  builder = factory.newDocumentBuilder();
	      Document document;
		  document = builder.parse(pomFile);
	      document.getDocumentElement().normalize();
	      
	    //edit Junit version
	      NodeList nList = document.getElementsByTagName("dependencies");
	      for (int temp = 0; temp < nList.getLength(); temp++)
	      {
	         Node node = nList.item(temp);   
	         if (node.getNodeType() == Node.ELEMENT_NODE)
	         {
	            Element eElement = (Element) node;
	            NodeList list = eElement.getElementsByTagName("artifactId");
	            if(list.getLength()>0 && list.item(0).getTextContent().equals("junit")){
	            	 eElement.getElementsByTagName("version").item(0).setTextContent(EvaluationRunner.jUnitVersion);
	            	 Main.printLine("updated Junit dependency");
	            }
	         }
	      }
	      
	      
	    //add scm connection
	      NodeList scmList = document.getElementsByTagName("scm");
	      if(scmList.getLength()>0){
		      for (int temp = 0; temp < scmList.getLength(); temp++)
		      {
		         Node node = scmList.item(temp);   
		         if (node.getNodeType() == Node.ELEMENT_NODE)
		         {
		            Element eElement = (Element) node;
		            processElement(document, eElement);
		            Main.printLine("updated scm dependnecy");
		            EvaluationRunner.updatedSCM++;
		         }
		      }
	      }else{
	    	  Element scmElement = document.createElement("scm"); // Element to be inserted 
	    	  processElement(document, scmElement);
	          document.adoptNode(scmElement);
	    	  
	    	  NodeList depList = document.getElementsByTagName("dependencies");
	    	  Element elem = (Element) depList.item(0);
	    	  elem.getParentNode().insertBefore(scmElement, elem.getNextSibling());
	    	  Main.printLine("added scm connection");
	    	  EvaluationRunner.addedSCM++;
	      }
	      
	     
		// write the content into xml file
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		DOMSource source = new DOMSource(document);
		StreamResult result = new StreamResult(pomFile);
		transformer.transform(source, result);
		
	}

	private static void processElement(Document document, Element eElement) {
		 processTag(document, eElement, "url",EvaluationRunner.scmURL);
		 processTag(document, eElement, "connection",EvaluationRunner.scmConnection);
		 processTag(document, eElement, "tag", EvaluationRunner.scmTag);
		 processTag(document, eElement, "developerConnection", EvaluationRunner.scmDevConnection);
	}


	private static void processTag(Document document, Element eElement, String name, String content) {
		if(eElement.getElementsByTagName(name).getLength()>0){
			if(!eElement.getElementsByTagName(name).item(0).getTextContent().equals(content)){
				eElement.getElementsByTagName(name).item(0).setTextContent(content);
			}
		}else{
        	Element newElement = document.createElement(name); // Element to be inserted 
        	newElement.setTextContent(content);
        	eElement.appendChild(newElement);
        }
	}

	//get latest created folder
	private static File getLatestFilefromDir(String dirPath){
	    File dir = new File(dirPath);
	    File[] files = dir.listFiles();
	    if (files == null || files.length == 0) {
	        return null;
	    }

	    File lastModifiedFile = files[0];
	    for (int i = 1; i < files.length; i++) {
	       if (lastModifiedFile.lastModified() < files[i].lastModified()) {
	           lastModifiedFile = files[i];
	       }
	    }
	    return lastModifiedFile;
	}


		
	
}
