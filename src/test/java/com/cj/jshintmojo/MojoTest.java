package com.cj.jshintmojo;

import static com.cj.jshintmojo.util.Util.*;
import static org.apache.commons.io.FileUtils.writeStringToFile;
import static org.junit.Assert.*;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
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
							Collections.<String>emptyList(),true, null, null, null, null);
			mojo.setLog(log);

			// when
			mojo.execute();

			// then
			assertEquals(1, log.messagesForLevel("warn").size());
			assertEquals("You told me to find tests in src/main/resources/nonexistentDirectory, but there is nothing there (" + directory.getAbsolutePath() + File.separator + "src" + File.separator + "main" + File.separator + "resources" + File.separator + "nonexistentDirectory)",
								log.messagesForLevel("warn").get(0).content.toString());


		}finally{
			deleteDirectory(directory);
		}
	}
	
	@Test
	public void resolvesConfigFileRelativeToMavenBasedirProperty() throws Exception {
		File directory = tempDir();
		try{
			// given
			mkdirs(directory, "src/main/resources");
			mkdirs(directory, "foo/bar");
			FileUtils.writeLines(new File(directory, "foo/bar/my-config-file.js"), Arrays.asList(
					"{",
					"  \"globals\": {", 
					"    \"require\": false",
					"  }",     
					"}"
					));
			
			Mojo mojo = new Mojo(null, "", 
							directory, 
							Collections.singletonList("src/main/resources/"), 
							Collections.<String>emptyList(),true, "foo/bar/my-config-file.js", null, null, null);
			
			LogStub log = new LogStub();
			mojo.setLog(log);

			// when
			mojo.execute();
			
			// then
			final String properPathForConfigFile = new File(directory, "foo/bar/my-config-file.js").getAbsolutePath();
			assertTrue(log.hasMessage("info", "Using configuration file: " + properPathForConfigFile));
			
		}finally{
			deleteDirectory(directory);
		}
	}
}
