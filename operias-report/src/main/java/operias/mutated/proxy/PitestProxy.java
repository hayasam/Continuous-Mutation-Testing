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
import operias.mutated.exceptions.PiTestException;
import operias.mutated.exceptions.SystemException;

public class PitestProxy {
	
	public static ThirdPartyProxySeetings settings;
	
	

	
		
	public static String getMutationReportFor(String commitID) throws PiTestException, SystemException {
		/* due to Pitest run on last commit feature limitation we have to update the head 
		 * each time to run the evaluation = running pitest on specific commit not just the last one
		*/
		GitProxy.changeHeadTo(commitID);
		String mutationPath = null;
		
		//change running local path due to Pitest Bug: hcoles/pitest: Issue #336
        Invoker invoker = new DefaultInvoker();	 
        invoker.setMavenHome(new File(settings.MAVEN_PATH));
        Main.printLine("[OPi+][INFO] set maven home to: "+settings.MAVEN_PATH);
		
        
        //make sure pom file works on JUnit 4.10 and we have scm connection setup
        File pomFile =  new File( settings.pomPath.getAbsolutePath()+"/pom.xml" );
        try {
			updatePomFileForMutationProcess(pomFile);
		} catch (ParserConfigurationException|SAXException|IOException|TransformerException e1) {
			throw new PiTestException(commitID,"could not parse and edit pom file ");
		} catch (Exception e){
			e.printStackTrace();
			throw new PiTestException(commitID,"fault in my logic when parsing the pom file ");
			
		}
        
		//setup Pitest on last commit
		InvocationRequest request = new DefaultInvocationRequest();
        request.setPomFile(pomFile);
        request.setGoals( Collections.singletonList( "org.pitest:pitest-maven:scmMutationCoverage -DanalyseLastCommit -Dmutators=ALL -l logfile.txt" ) );
        //-DtargetTests="+GitProxy.getGroupArtifactPath()+" - used to fix the pitest bug but apparently it works for JSoup
        //mvn org.pitest:pitest-maven:scmMutationCoverage -DanalyseLastCommit -DtargetTests=groupID.artifactID.* -Dmutators=ALL
        
        //-l logfile.txt
        //mvn console output is stored in logfile.txt
		
        
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
	        	File logFilePath =  new File( settings.pomPath.getAbsolutePath()+"/logfile.txt");
	        	if(isPitestAnalysis(logFilePath, commitID)){
	        		Main.printLine("[OPi+][INFO] successfully run Pitest on last commit");
		        	mutationPath = settings.pomPath+"/target/pit-reports";
		        	mutationPath = getLatestFilefromDir(mutationPath).getAbsolutePath();
		        	
		        	Main.printLine("[OPi+][INFO] My MUTATION REPORT path is: "+mutationPath+" for commit "+commitID);
	        	}
	        }
		} catch (MavenInvocationException|IllegalStateException e) {
			Main.printLine("[OPi+][ERROR] could not run Pitest on last commit");
			e.printStackTrace();
			throw new PiTestException(commitID, "Pitest Build Failed on current commit");
			
		}
		return mutationPath;
		
	}
	
	
	private static boolean isPitestAnalysis(File logFilePath, String commitID) throws SystemException, PiTestException {
		
		try{
			
			if(FileUtils.readFileToString(logFilePath).contains("[INFO] No modified files found - nothing to mutation test")){
				throw new PiTestException(commitID, "Pitest did not detect any change");
			}
			if(FileUtils.readFileToString(logFilePath).contains("No mutations found.")){
				throw new PiTestException(commitID, "Pitest did not generate any mutations");
			}
			return true;
		} catch(IOException e){
			e.printStackTrace();
			throw new SystemException(commitID, "Could not read logFile for Pitest", e);
		}
	}

	private static void updatePomFileForMutationProcess(File pomFile) throws ParserConfigurationException, SAXException, IOException, TransformerException {
		
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
	            }
	         }
	      }
	      
	      
	    //add scm connection
	      NodeList dList = document.getElementsByTagName("scm");
	      if(dList.getLength()>0){
	    	  Node node = dList.item(0);  
	    	  if (node.getNodeType() == Node.ELEMENT_NODE)
		         {
		            Element eElement = (Element) node;
		            
		            if(eElement.getElementsByTagName("developerConnection").getLength()==0){
		            	 Element newTag = document.createElement("developerConnection");
		            	 newTag.appendChild(document.createTextNode(EvaluationRunner.scmDevConnection));
		            	 eElement.appendChild(newTag);
		            }
		            if(eElement.getElementsByTagName("url").getLength()==0){
		            	 Element newTag = document.createElement("url");
		            	 newTag.appendChild(document.createTextNode(EvaluationRunner.scmURL));
		            	 eElement.appendChild(newTag);
		            }
		            if(eElement.getElementsByTagName("connection").getLength()==0){
		            	 Element newTag = document.createElement("connection");
		            	 newTag.appendChild(document.createTextNode(EvaluationRunner.scmConnection));
		            	 eElement.appendChild(newTag);
		            }
		            if(eElement.getElementsByTagName("tag").getLength()==0){
		            	 Element newTag = document.createElement("tag");
		            	 newTag.appendChild(document.createTextNode(EvaluationRunner.scmTag));
		            	 eElement.appendChild(newTag);
		            }
		         }
	      }else{
		      Element scmTag = document.createElement("scm"); // Element to be inserted 
		      scmTag.setAttribute("url", EvaluationRunner.scmURL);
		      scmTag.setAttribute("connection", EvaluationRunner.scmConnection);
		      scmTag.setAttribute("developerConnection", EvaluationRunner.scmDevConnection);
		      scmTag.setAttribute("tag", EvaluationRunner.scmTag);
		      document.adoptNode(scmTag);
	      }
	      
	     
	      
	      
		// write the content into xml file
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		DOMSource source = new DOMSource(document);
		StreamResult result = new StreamResult(pomFile);
		transformer.transform(source, result);
		
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
