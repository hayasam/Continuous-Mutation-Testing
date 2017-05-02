package operias.mutated;

import java.io.File;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import operias.mutated.proxy.GitProxy;
import operias.mutated.proxy.PitestProxy;
import operias.mutated.proxy.ThirdPartyProxySeetings;

public class TestRunner {

	public static void main(String[] args) throws SAXException, IOException, ParserConfigurationException, TransformerException {
		
		String s = null;
		System.out.println(s==null);
		/*
		
		//set up the project
				String REMOTE_URL = "https://github.com/ileontiuc/testSettings.git";
				String MAVEN_PATH = "/usr/share/maven";
				
				ThirdPartyProxySeetings settings = new ThirdPartyProxySeetings(REMOTE_URL, MAVEN_PATH);
				GitProxy dummyGitProxy = new GitProxy(1, settings);
				PitestProxy.settings = settings;
				
				
				
				//open pom file
				//add scm connection
				//edit Junit version
				
				String pomFile = "/home/ioana/original (copy).xml";
				 
				
				
			      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			      DocumentBuilder builder = factory.newDocumentBuilder();
			      Document document = builder.parse(new File(pomFile));
			      document.getDocumentElement().normalize();
			      NodeList nList = document.getElementsByTagName("dependencies");
			      
			      
			      for (int temp = 0; temp < nList.getLength(); temp++)
			      {
			         Node node = nList.item(temp);   
			         if (node.getNodeType() == Node.ELEMENT_NODE)
			         {
			            Element eElement = (Element) node;
			            if(eElement.getElementsByTagName("artifactId").item(0).getTextContent().equals("junit")){
			            	 eElement.getElementsByTagName("version").item(0).setTextContent("4.10");
			            }
			         }
			      }
			      
			      
			      NodeList dList = document.getElementsByTagName("scm");
			      if(dList.getLength()>0){
			    	  Node node = nList.item(0);  
			    	  if (node.getNodeType() == Node.ELEMENT_NODE)
				         {
				            Element eElement = (Element) node;
				            if(eElement.getElementsByTagName("developerConnection").getLength()==0){
				            	 eElement.setAttribute("developerConnection", "scm:git:git@github.com:jhy/jsoup.git");
				            }
				            if(eElement.getElementsByTagName("url").getLength()==0){
				            	 eElement.setAttribute("url", "https://github.com/jhy/jsoup");
				            }
				            if(eElement.getElementsByTagName("connection").getLength()==0){
				            	 eElement.setAttribute("connection", "scm:git:https://github.com/jhy/jsoup.git");
				            }
				            if(eElement.getElementsByTagName("tag").getLength()==0){
				            	 eElement.setAttribute("tag", "HEAD");
				            }
				         }
			      }else{
			    	  Element scmTag = document.createElement("scm"); // Element to be inserted 
				      scmTag.setAttribute("url", "https://github.com/jhy/jsoup");
				      scmTag.setAttribute("connection", "scm:git:https://github.com/jhy/jsoup.git");
				      scmTag.setAttribute("developerConnection", "scm:git:git@github.com:jhy/jsoup.git");
				      scmTag.setAttribute("tag", "HEAD");
				      document.adoptNode(scmTag);
			      }
			      
			      
			     
			      
			      
				// write the content into xml file
				TransformerFactory transformerFactory = TransformerFactory.newInstance();
				Transformer transformer = transformerFactory.newTransformer();
				DOMSource source = new DOMSource(document);
				StreamResult result = new StreamResult(new File(pomFile));
				transformer.transform(source, result);
				
				*/
	}

}
