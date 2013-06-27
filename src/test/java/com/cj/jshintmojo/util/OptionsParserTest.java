package com.cj.jshintmojo.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

public class OptionsParserTest {

	@Test
	public void testGetGlobals() throws IOException {
		// given
		byte[] configFileContents = FileUtils.readFileToByteArray(new File("src/test/resources/jshint.conf.js"));

		//when
		Set<String> globals = OptionsParser.extractGlobals(configFileContents);

		// then
		assertEquals(2, globals.size());
		assertTrue(globals.contains("define:false"));
		assertTrue(globals.contains("require:false"));
	}

	@Test
	public void testGetOptions() throws IOException {
		// given
		byte[] configFileContents = FileUtils.readFileToByteArray(new File("src/test/resources/jshint.conf.js"));

		//when
		Set<String> options = OptionsParser.extractOptions(configFileContents);

		// then
		assertEquals(15, options.size());
		assertTrue(options.contains("bitwise:true"));
		assertTrue(options.contains("camelcase:true"));
		assertTrue(options.contains("curly:true"));
		assertTrue(options.contains("forin:true"));
		assertTrue(options.contains("immed:true"));
		assertTrue(options.contains("latedef:true"));
		assertTrue(options.contains("newcap:true"));
		assertTrue(options.contains("noarg:true"));
		assertTrue(options.contains("noempty:true"));
		assertTrue(options.contains("nonew:true"));
		assertTrue(options.contains("quotmark:single"));
		assertTrue(options.contains("undef:true"));
		assertTrue(options.contains("unused:true"));
		assertTrue(options.contains("strict:true"));
		assertTrue(options.contains("browser:true"));
	}
}
