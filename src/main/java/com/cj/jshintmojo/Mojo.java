package com.cj.jshintmojo;

import static com.cj.jshintmojo.util.Util.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.util.StringUtils;

import com.cj.jshintmojo.cache.Cache;
import com.cj.jshintmojo.cache.Result;
import com.cj.jshintmojo.jshint.EmbeddedJshintCode;
import com.cj.jshintmojo.jshint.FunctionalJava;
import com.cj.jshintmojo.jshint.FunctionalJava.Fn;
import com.cj.jshintmojo.jshint.JSHint;
import com.cj.jshintmojo.jshint.JSHint.Error;
import com.cj.jshintmojo.reporter.CheckStyleReporter;
import com.cj.jshintmojo.reporter.JSHintReporter;
import com.cj.jshintmojo.reporter.JSLintReporter;
import com.cj.jshintmojo.util.OptionsParser;
import com.cj.jshintmojo.util.Util;

/**
 * @goal lint
 * @phase compile
 * @threadSafe
 */
public class Mojo extends AbstractMojo {

	/**
	 * @parameter property="directories"
	 */
	private final List<String> directories = new ArrayList<String>();

	/**
	 * @parameter property="excludes"
	 */
	private final List<String> excludes = new ArrayList<String>();

	/**
	 * @parameter property="options"
	 */
	private String options = null;

	/**
	 * @parameter 
	 */
	private String globals = "";

	/**
	 * @parameter property="configFile"
	 */
	private String configFile = "";

	/**
	 * @parameter property="reporter"
	 */
	private String reporter = "";

	/**
	 * @parameter property="reportFile"
	 */
	private String reportFile = "";

    /**
     * @parameter expression="${jshint.version}"
     */
    private String version = "2.4.3";
	
	/**
	 * @parameter 
	 */
	private Boolean failOnError = true;

	/**
	 * @parameter default-value="${basedir}
	 * @readonly
	 * @required
	 */
	File basedir;
	
	public Mojo() {}
	
	public Mojo(String options, String globals, File basedir, List<String> directories, List<String> excludes, boolean failOnError, String configFile, String reporter, String reportFile) {
		super();
		this.options = options;
		this.globals = globals;
		this.basedir = basedir;
		this.directories.addAll(directories);
		this.excludes.addAll(excludes);
		this.failOnError = failOnError;
		this.configFile = configFile;
		this.reporter = reporter;
		this.reportFile = reportFile;
	}
	
	public void execute() throws MojoExecutionException, MojoFailureException {
	    getLog().info("using jshint version " + version);

	    final String jshintCode = getEmbeddedJshintCode(version);
	    
        final JSHint jshint = new JSHint(jshintCode);

        final Config config = readConfig(this.options, this.globals, this.configFile, this.basedir, getLog());
        final Cache.Hash cacheHash = new Cache.Hash(config.options, config.globals, this.version, this.configFile, this.directories, this.excludes);
		
		if(directories.isEmpty()){
			directories.add("src");
		}
		
		try {
			final File targetPath = new File(basedir, "target");
			mkdirs(targetPath);
			final File cachePath = new File(targetPath, "lint.cache");
			final Cache cache = readCache(cachePath, cacheHash);
			
			final List<File> files = findFilesToCheck();

			final Map<String, Result> currentResults = lintTheFiles(jshint, cache, files, config, getLog());
			
			Util.writeObject(new Cache(cacheHash, currentResults), cachePath);
			
            handleResults(currentResults, this.reporter, this.reportFile);
            
		} catch (FileNotFoundException e) {
			throw new MojoExecutionException("Something bad happened", e);
		}
	}
	
	static class Config {
	    final String options, globals;

        public Config(String options, String globals) {
            super();
            this.options = options;
            this.globals = globals;
        }
	    
	}
	
    private static Config readConfig(String options, String globals, String configFileParam, File basedir, Log log) throws MojoExecutionException {
        final File jshintRc = findJshintrc(basedir);
        final File configFile = StringUtils.isNotBlank(configFileParam)?new File(basedir, configFileParam):null;
        
        final Config config;
        if(options==null){
            if(configFile!=null){
                log.info("Using configuration file: " + configFile.getAbsolutePath());
                config = processConfigFile(configFile);
            }else if(jshintRc!=null){
                log.info("Using configuration file: " + jshintRc.getAbsolutePath());
                config = processConfigFile(jshintRc);
            }else{
                config = new Config("", globals);
            }
        }else{
            config = new Config(options, globals);
        }
        
        return config;
    }

    private List<File> findFilesToCheck() {
        List<File> javascriptFiles = new ArrayList<File>();

        for(String next: directories){
        	File path = new File(basedir, next);
        	if(!path.exists() && !path.isDirectory()){
        		getLog().warn("You told me to find tests in " + next + ", but there is nothing there (" + path.getAbsolutePath() + ")");
        	}else{
        		collect(path, javascriptFiles);
        	}
        }

        List<File> matches = FunctionalJava.filter(javascriptFiles, new Fn<File, Boolean>(){
        	public Boolean apply(File i) {
        		for(String exclude : excludes){
        			File e = new File(basedir, exclude);
        			if(i.getAbsolutePath().startsWith(e.getAbsolutePath())){
        				getLog().warn("Excluding " + i);
        				
        				return Boolean.FALSE;
        			}
        		}

        		return Boolean.TRUE;
        	}
        });
        return matches;
    }

    private static Map<String, Result> lintTheFiles(final JSHint jshint, final Cache cache, List<File> filesToCheck, final Config config, final Log log) throws FileNotFoundException {
        final Map<String, Result> currentResults = new HashMap<String, Result>();
        for(File file : filesToCheck){
        	Result previousResult = cache.previousResults.get(file.getAbsolutePath());
        	Result theResult;
        	if(previousResult==null || (previousResult.lastModified.longValue()!=file.lastModified())){
        		log.info("  " + file );
        		List<Error> errors = jshint.run(new FileInputStream(file), config.options, config.globals);
        		theResult = new Result(file.getAbsolutePath(), file.lastModified(), errors); 
        	}else{
        		log.info("  " + file + " [no change]");
        		theResult = previousResult;
        	}
        	
        	if(theResult!=null){
        		currentResults.put(theResult.path, theResult);
        		Result r = theResult;
        		currentResults.put(r.path, r);
        		for(Error error: r.errors){
        			log.error("   " + error.line.intValue() + "," + error.character.intValue() + ": " + error.reason);
        		}
        	}
        }
        return currentResults;
    }

    private void handleResults(final Map<String, Result> currentResults,
            final String reporter, final String reportFile) throws MojoExecutionException
    {
        char NEWLINE = '\n';
        StringBuilder errorRecap = new StringBuilder(NEWLINE);
        
        int numProblematicFiles = 0;
        for(Result r : currentResults.values()){
        	if(!r.errors.isEmpty()){
        		numProblematicFiles ++;

                errorRecap
                    .append(NEWLINE)
                    .append(r.path)
                    .append(NEWLINE);

        		for(Error error: r.errors){
        			errorRecap
                        .append("   ")
                        .append(error.line.intValue())
                        .append(",")
                        .append(error.character.intValue())
                        .append(": ")
                        .append(error.reason)
                        .append(NEWLINE);
        		}
        	}
        }
        
        if(numProblematicFiles > 0) {
            saveReportFile(currentResults, reporter, reportFile);

        	String errorMessage = "\nJSHint found problems with " + numProblematicFiles + " file";

        	// pluralise
        	if (numProblematicFiles > 1) {
        		errorMessage += "s";
        	}

            errorMessage += errorRecap.toString();

        	if (failOnError) {
        		throw new MojoExecutionException(errorMessage);
        	} else {
        		getLog().info(errorMessage);
        	}
        }
    }

    private void saveReportFile(Map<String, Result> results, String reportType, String reportFile) {
        JSHintReporter reporter = null;
        if(JSLintReporter.FORMAT.equalsIgnoreCase(reportType)){
            reporter = new JSLintReporter();
        }else if(CheckStyleReporter.FORMAT.equalsIgnoreCase(reportType)){
            reporter = new CheckStyleReporter();
        }else if(StringUtils.isNotBlank(reportType)){
            getLog().warn("Unknown reporter \"" + reportType + "\". Skip reporting.");
            return;
        }else{
            return;
        }
        File file = StringUtils.isNotBlank(reportFile) ?
                new File(reportFile) : new File("target/jshint.xml");
        getLog().info(String.format("Generating \"JSHint\" report. reporter=%s, reportFile=%s.",
                reportType, file.getAbsolutePath()));

        String report = reporter.report(results);
        Writer writer = null;
        try{
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "utf-8"));
            writer.write(report);
        }catch (IOException e){
            getLog().error(e);
        }finally{
            if(writer != null){
                 try {
                    writer.close();
                } catch (IOException e) {
                    getLog().error(e);
                }
            }
        }
    }

    @SuppressWarnings("serial")
    private static String getEmbeddedJshintCode(String version) throws MojoFailureException {
        
        final String resource = EmbeddedJshintCode.EMBEDDED_VERSIONS.get(version);
        if(resource==null){
            StringBuffer knownVersions = new StringBuffer();
            for(String v : EmbeddedJshintCode.EMBEDDED_VERSIONS.keySet()){
                knownVersions.append("\n    " + v);
            }
            throw new MojoFailureException("I don't know about the \"" + version + "\" version of jshint.  Here are the versions I /do/ know about: " + knownVersions);
        }
        return resource;
    }
    
    private static File findJshintrc(File cwd) {
        File placeToLook = cwd;
        while(placeToLook.getParentFile()!=null){
            File rcFile = new File(placeToLook, ".jshintrc");
            if(rcFile.exists()){
                return rcFile;
            }else{
                placeToLook = placeToLook.getParentFile();
            }
        }
        
        return null;
    }
	
	private static boolean nullSafeEquals(String a, String b) {
		if(a==null && b==null) return true;
		else if(a==null || b==null) return false;
		else return a.equals(b);
	}

	private Cache readCache(File path, Cache.Hash hash){
		try {
			if(path.exists()){
				Cache cache = Util.readObject(path);
		        if(EqualsBuilder.reflectionEquals(cache.hash, hash)){
		            return cache;
		        }else{
		        	getLog().warn("Something changed ... clearing cache");
		            return new Cache(hash);
		        }
		        
			}
		} catch (Throwable e) {
			super.getLog().warn("I was unable to read the cache.  This may be because of an upgrade to the plugin.");
		}
		
		return new Cache(hash);
	}
	
	private void collect(File directory, List<File> files) {
		for(File next : directory.listFiles()){
			if(next.isDirectory()){
				collect(next, files);
			}else if(next.getName().endsWith(".js")){
				files.add(next);
			}
		}
	}

	/**
	 * Read contents of the specified config file and use the values defined there instead of the ones defined directly in pom.xml config.
	 *
	 * @throws MojoExecutionException if the specified file cannot be processed
	 */
	private static Config processConfigFile(File configFile) throws MojoExecutionException {
		byte[] configFileContents;
		try {
			configFileContents = FileUtils.readFileToByteArray(configFile);
		} catch (IOException e) {
			throw new MojoExecutionException("Unable to read config file located in " + configFile);
		}

		Set<String> globalsSet = OptionsParser.extractGlobals(configFileContents);
		Set<String> optionsSet = OptionsParser.extractOptions(configFileContents);

		final String globals, options;
		
		if (globalsSet.size() > 0) {
			globals = StringUtils.join(globalsSet.iterator(), ",");
		}else{
		    globals = "";
		}

		if (optionsSet.size() > 0) {
			options = StringUtils.join(optionsSet.iterator(), ",");
		}else{
		    options = "";
		}
		
		return new Config(options, globals);
	}
}
