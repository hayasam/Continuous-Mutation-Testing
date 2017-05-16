package operias.mutated;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

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
import operias.mutated.exceptions.PiTestException;
import operias.mutated.proxy.GitProxy;
import operias.mutated.proxy.PitestProxy;
import operias.mutated.proxy.ThirdPartyProxySeetings;
import operias.mutated.record.files.EvaluationCrashStatus;
import operias.mutated.record.files.EvaluationDataFile;
import operias.mutated.record.files.EvaluationFileWriter;
import operias.mutated.record.files.EvaluationNoReportFileWriter;
import operias.mutated.record.files.EvaluationOPiLogFile;

public class TestRunner {

	
	
	
	
	public static void main(String[] args) throws SAXException, IOException, ParserConfigurationException, TransformerException, MavenInvocationException {
		int CONDITIONALS_BOUNDARY_MUTATOR = 3;
		String overview = "first line";
		overview+= "\nCONDITIONALS_BOUNDARY_MUTATOR"+CONDITIONALS_BOUNDARY_MUTATOR;
		System.out.println(overview);
	}
	
}
