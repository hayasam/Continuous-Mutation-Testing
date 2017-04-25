package mutation.testing;

public class TestRunner {

	public static void main(String[] args) {
		
		
		
		
		//set up the project
				String REMOTE_URL = "https://github.com/ileontiuc/testSettings.git";
				String MAVEN_PATH = "/usr/share/maven";
				
				ThirdPartyProxySeetings settings = new ThirdPartyProxySeetings(REMOTE_URL, MAVEN_PATH);
				GitProxy dummyGitProxy = new GitProxy(1, settings);
				PitestProxy.settings = settings;
				
	}

}
