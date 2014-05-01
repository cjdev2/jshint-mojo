package com.cj.jshintmojo;

import static com.cj.jshintmojo.util.Util.mkdirs;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
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
import com.cj.jshintmojo.jshint.JSHint.Hint;
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
	 * @parameter property="ignoreFile"
	 */
	private String ignoreFile = "";

    /**
     * @parameter expression="${jshint.version}"
     */
    private final String version = "2.4.3";

	/**
	 * @parameter
	 */
	private Boolean failOnError = true;
	/**
     * @parameter
     */
    private Boolean failOnWarning = true;

	/**
	 * @parameter default-value="${basedir}
	 * @readonly
	 * @required
	 */
	File basedir;

	public Mojo() {}

	public Mojo(final String options, final String globals, final File basedir, final List<String> directories, 
	        final List<String> excludes, final boolean failOnError, final boolean failOnWarning, 
	        final String configFile, final String reporter, final String reportFile, final String ignoreFile) {
		super();
		this.options = options;
		this.globals = globals;
		this.basedir = basedir;
		this.directories.addAll(directories);
		this.excludes.addAll(excludes);
        this.failOnError = failOnError;
        this.failOnWarning = failOnWarning;
		this.configFile = configFile;
		this.reporter = reporter;
		this.reportFile = reportFile;
		this.ignoreFile = ignoreFile;
	}

	@Override
    public void execute() throws MojoExecutionException, MojoFailureException {
	    getLog().info("using jshint version " + version);

	    final String jshintCode = getEmbeddedJshintCode(version);

        final JSHint jshint = new JSHint(jshintCode);

        final Config config = readConfig(this.options, this.globals, this.configFile, this.basedir, getLog());
        if (this.excludes.isEmpty() || (this.ignoreFile != null && !this.ignoreFile.isEmpty())) {
            this.excludes.addAll(readIgnore(this.ignoreFile, this.basedir, getLog()).lines);
        }
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

        public Config(final String options, final String globals) {
            super();
            this.options = options;
            this.globals = globals;
        }

	}

    private static Config readConfig(final String options, final String globals, final String configFileParam, final File basedir, final Log log) throws MojoExecutionException {
        final Config config;
        if (options != null) {
            config = new Config(options, globals);
        } else {
            File configFile = null;
            if (StringUtils.isNotBlank(configFileParam)) {
                configFile = new File(configFileParam);
                if (!configFile.isAbsolute()) {
                    configFile = new File(basedir, configFileParam);
                }
            }
            if (configFile != null && configFile.exists()) {
                log.info("Using configured 'configFile' from: " + configFile.getAbsolutePath());
                config = processConfigFile(configFile);
            } else {
                final File jshintRc = findJshintrc(basedir);
                if (jshintRc != null) {
                    log.info("No configFile configured or found, but found a '.jshintrc' file: " + jshintRc.getAbsolutePath());
                    config = processConfigFile(jshintRc);
                } else {
                    log.info("No options, configFile or '.jshintrc' file found. Only using configured globals.");
                    config = new Config("", globals);
                }
            }
        }
        return config;
    }

    static class Ignore {

        final List<String> lines;

        public Ignore(final List<String> lines) {
            this.lines = lines;
        }

    }

    private static Ignore readIgnore(final String ignoreFileParam, final File basedir, final Log log) throws MojoExecutionException {
        final File jshintignore = findJshintignore(basedir);
        final File ignoreFile = StringUtils.isNotBlank(ignoreFileParam) ? new File(basedir, ignoreFileParam) : null;

        final Ignore ignore;
        if (ignoreFile != null) {
            log.info("Using ignore file: " + ignoreFile.getAbsolutePath());
            ignore = processIgnoreFile(ignoreFile);
        } else if (jshintignore != null) {
            log.info("Using ignore file: " + jshintignore.getAbsolutePath());
            ignore = processIgnoreFile(jshintignore);
        } else {
            ignore = new Ignore(Collections.<String>emptyList());
        }

        return ignore;
    }

    private List<File> findFilesToCheck() {
        List<File> javascriptFiles = new ArrayList<File>();

        for(String next: directories){
        	File path = new File(basedir, next);
        	if(!path.exists() && !path.isDirectory()){
        		getLog().info("You told me to find tests in " + next + ", but there is nothing there (" + path.getAbsolutePath() + ")");
        	}else{
        		collect(path, javascriptFiles);
        	}
        }

        List<File> matches = FunctionalJava.filter(javascriptFiles, new Fn<File, Boolean>(){
        	@Override
            public Boolean apply(final File i) {
        		for(String exclude : excludes){
        			File e = new File(basedir, exclude);
        			if(i.getAbsolutePath().startsWith(e.getAbsolutePath())){
        				getLog().info("Excluding " + i);
        				return Boolean.FALSE;
        			}
        		}

        		return Boolean.TRUE;
        	}
        });
        return matches;
    }

    private static Map<String, Result> lintTheFiles(final JSHint jshint, final Cache cache, final List<File> filesToCheck, final Config config, final Log log) throws FileNotFoundException {
        final Map<String, Result> currentResults = new HashMap<String, Result>();
        for (File file : filesToCheck) {
            Result previousResult = cache.previousResults.get(file.getAbsolutePath());
            Result theResult;
            if (previousResult == null || (previousResult.lastModified.longValue() != file.lastModified())) {
                log.info("  " + file);
                List<Hint> hints = jshint.run(new FileInputStream(file), config.options, config.globals);
                theResult = new Result(file.getAbsolutePath(), file.lastModified(), hints);
            } else {
                log.info("  " + file + " [no change]");
                theResult = previousResult;
            }

            if (theResult != null) {
                currentResults.put(theResult.path, theResult);
                Result r = theResult;
                currentResults.put(r.path, r);
                for (Hint hint : r.hints) {
                    String consoleLogMessage = hint.printLogMessage();
                    if (hint instanceof JSHint.Info) {
                        log.info(consoleLogMessage);
                    } else if (hint instanceof JSHint.Warning) {
                        log.warn(consoleLogMessage);
                    } else if (hint instanceof JSHint.Error) {
                        log.error(consoleLogMessage);
                    }
                }
            }
        }
        return currentResults;
    }

    private void handleResults(final Map<String, Result> currentResults,
            final String reporter, final String reportFile) throws MojoExecutionException {
        char NEWLINE = '\n';
        StringBuilder errorRecap = new StringBuilder(NEWLINE);

        int numProblematicFiles = 0;
        boolean hasErrors = false;
        boolean hasWarnings = false;
        for(Result r : currentResults.values()){
            if (!r.hints.isEmpty()) {
                numProblematicFiles ++;

                errorRecap
                    .append(NEWLINE)
                    .append(r.path)
                    .append(NEWLINE);

                for (Hint hint : r.hints) {
                    if (hint == null) {
                        errorRecap.append("!!!hint was null!");
                        continue;
                    }
                    hasWarnings |= (hint instanceof JSHint.Warning);
                    hasErrors |= (hint instanceof JSHint.Error);

                    errorRecap
                        .append(hint.printLogMessage())
                        .append(NEWLINE);
        		}
        	}
        }

        if(numProblematicFiles > 0) {
            saveReportFile(currentResults, reporter, reportFile);

            String errorMessage = "\nJSHint found ";
            
            if (hasErrors)
                errorMessage += "errors ";
            if (hasErrors && hasWarnings)
                errorMessage += "and ";
            if (hasWarnings)
                errorMessage += "warnings ";
            
            errorMessage += "in " + numProblematicFiles + " file";
            // pluralise
            if (numProblematicFiles > 1) {
                errorMessage += "s";
            }
            errorMessage += "! Please see errors/warning above!";
            

            errorMessage += "\nJSHint is ";
            if (!failOnError && !failOnWarning) {
                errorMessage += "not configured to fail on error or warning.";
            } else {
                errorMessage += "configured to fail on ";
                if (failOnError)
                    errorMessage += "error ";
                if (failOnError && failOnWarning)
                    errorMessage += "or ";
                if (failOnWarning)
                    errorMessage += "warning ";
            }
            
            if (hasErrors || hasWarnings)
            
            if ((failOnError && hasErrors) || (failOnWarning && hasWarnings)) {
                throw new MojoExecutionException(errorMessage);
            } else {
                getLog().info(errorMessage);
            }
        }
    }

    private void saveReportFile(final Map<String, Result> results, final String reportType, final String reportFile) {
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
    private static String getEmbeddedJshintCode(final String version) throws MojoFailureException {

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

    private static File findJshintrc(final File cwd) {
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

    private static File findJshintignore(final File cwd) {
        File placeToLook = cwd;
        while (placeToLook.getParentFile() != null) {
            File ignoreFile = new File(placeToLook, ".jshintignore");
            if (ignoreFile.exists()) {
                return ignoreFile;
            } else {
                placeToLook = placeToLook.getParentFile();
            }
        }

        return null;
    }

	private static boolean nullSafeEquals(final String a, final String b) {
		if(a==null && b==null) {
            return true;
        } else if(a==null || b==null) {
            return false;
        } else {
            return a.equals(b);
        }
	}

	private Cache readCache(final File path, final Cache.Hash hash){
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
			super.getLog().warn("I was unable to read the cache. This may be because of an upgrade to the plugin.");
		}

		return new Cache(hash);
	}

	private void collect(final File directory, final List<File> files) {
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
	private static Config processConfigFile(final File configFile) throws MojoExecutionException {
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

    /**
     * Read contents of the specified ignore file and use the values defined
     * there instead of the ones defined directly in pom.xml config.
     *
     * @throws MojoExecutionException if the specified file cannot be processed
     */
    private static Ignore processIgnoreFile(final File ignoreFile) throws MojoExecutionException {
        try {
            return new Ignore(FileUtils.readLines(ignoreFile, "UTF-8"));
        } catch (IOException e) {
            throw new MojoExecutionException("Unable to read ignore file located in " + ignoreFile, e);
        }
    }
}
