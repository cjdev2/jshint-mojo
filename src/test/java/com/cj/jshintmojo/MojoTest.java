package com.cj.jshintmojo;

import static com.cj.jshintmojo.util.Util.deleteDirectory;
import static com.cj.jshintmojo.util.Util.mkdirs;
import static com.cj.jshintmojo.util.Util.tempDir;
import static junit.framework.Assert.assertEquals;

import java.io.File;
import java.util.Collections;

import org.junit.Test;

public class MojoTest {
	
	@Test
	public void warnsUsersWhenConfiguredToWorkWithNonexistentDirectories() throws Exception {
		File directory = tempDir();
		try{
			// given
			mkdirs(directory, "src/main/resources");
			LogStub log = new LogStub();
			Mojo mojo = new Mojo("", "", 
							directory, 
							Collections.singletonList("src/main/resources/nonexistentDirectory"), 
							Collections.<String>emptyList());
			mojo.setLog(log);
			
			// when
			mojo.execute();
			
			// then
			assertEquals(1, log.messagesForLevel("warn").size());
			assertEquals("You told me to find tests in src/main/resources/nonexistentDirectory, but there is nothing there (" + directory.getAbsolutePath() + "/src/main/resources/nonexistentDirectory)", 
								log.messagesForLevel("warn").get(0).content.toString());
			
			
		}finally{
			deleteDirectory(directory);
		}
	}
	
}
