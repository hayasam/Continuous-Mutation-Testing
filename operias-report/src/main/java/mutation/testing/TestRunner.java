package mutation.testing;

import java.util.ArrayList;

public class TestRunner {

	public static void main(String[] args) {
		
		
		GitProxy dummyGitProxy = new GitProxy();
		
		//set up the project
				String REMOTE_URL = "https://github.com/ileontiuc/testSettings.git";
				String MAVEN_PATH = "/usr/share/maven";
				
				ThirdPartyProxySeetings settings = new ThirdPartyProxySeetings(REMOTE_URL, MAVEN_PATH);
				GitProxy.settings = settings;
				PitestProxy.settings = settings;
				
				GitProxy.getChangesFromACommit();
				
				//get commits to analyze
				ArrayList<String>  commits = GitProxy.getFilteredCommits();
				
				
				System.out.println("Commits selected");
				for(String commitID: commits){
					System.out.println(commitID);
				}
				
				

	}

}
