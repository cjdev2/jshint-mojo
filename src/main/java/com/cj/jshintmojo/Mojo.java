package com.cj.jshintmojo;

import static com.cj.jshintmojo.Util.mkdirs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import com.cj.jshintmojo.FunctionalJava.Fn;
import com.cj.jshintmojo.JSHint.Error;

/**
 * @goal lint
 * @phase compile
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
	private String options = "";

	/**
	 * @parameter property="globals"
	 */
	private String globals = "";

	/**
	 * @parameter default-value="${basedir}
	 * @readonly
	 * @required
	 */
	private File basedir;
	
	public Mojo() {}
	
	public Mojo(String options, String globals, File basedir, List<String> directories, List<String> excludes) {
		super();
		this.options = options;
		this.globals = globals;
		this.basedir = basedir;
		this.directories.addAll(directories);
		this.excludes.addAll(excludes);
	}

	/*
	 * TODO: 
	 *   1) Add a way to skip (i.e. 'mvn install -Dlint.skip=true')
	 *   2) Make it parallelizable: i.e. 'mvn install -Dlint.threads=4'
	 */

	public void execute() throws MojoExecutionException, MojoFailureException {
		if(directories.isEmpty()){
			directories.add("src");
		}
		try {
			final File targetPath = new File(basedir, "target");
			mkdirs(targetPath);
			final File cachePath = new File(targetPath, "lint.cache");
			
			final Cache cache = readCache(cachePath, new Cache(this.options));
			
			if(!options.equals(cache.options)){
				getLog().warn("Options changed ... clearing cache");
				cache.previousResults.clear();
			}
			
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

			JSHint jshint = new JSHint();

			final Map<String, Result> currentResults = new HashMap<String, Result>();
			for(File file : matches){
				Result previousResult = cache.previousResults.get(file.getAbsolutePath());
				Result theResult;
				if(previousResult==null || (previousResult.lastModified.longValue()!=file.lastModified())){
					getLog().info("  " + file );
					List<Error> errors = jshint.run(new FileInputStream(file), options, globals);
					theResult = new Result(file.getAbsolutePath(), file.lastModified(), errors); 
				}else{
					getLog().info("  " + file + " [no change]");
					theResult = previousResult;
				}
				
				if(theResult!=null){
					currentResults.put(theResult.path, theResult);
					Result r = theResult;
					currentResults.put(r.path, r);
					for(Error error: r.errors){
						getLog().error("   " + error.line.intValue() + "," + error.character.intValue() + ": " + error.reason);
					}
				}
			}
			
			Util.writeObject(new Cache(options, currentResults), cachePath);
			
			int numProblems = 0;
			
			for(Result r : currentResults.values()){
				if(!r.errors.isEmpty()){
					numProblems ++;
				}
			}
			
			if(numProblems>0){
				throw new MojoFailureException("JSHint found problems with " + numProblems + " files");
			}
		} catch (FileNotFoundException e) {
			throw new MojoExecutionException("Something bad happened", e);
		}
	}
	
	private Cache readCache(File path, Cache defaultCache){
		try {
			if(path.exists()){
				return Util.readObject(path);
			}
		} catch (Throwable e) {
			super.getLog().warn("I was unable to read the cache.  This may be because of an upgrade to the plugin.");
		}
		
		return defaultCache;
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

}
