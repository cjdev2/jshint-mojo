package com.cj.jshintmojo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
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

	/*
	 * TODO: 
	 *   1) Add a way to skip (i.e. 'mvn install -Dlint.skip=true')
	 *   2) Make it parallelizable: i.e. 'mvn install -Dlint.threads=4'
	 */

	public static class Result implements Serializable {
		final String path;
		final Long lastModified;
		final List<Error> errors;
		
		public Result(String path, Long lastModified, List<Error> errors) {
			super();
			this.path = path;
			this.lastModified = lastModified;
			this.errors = errors;
		}
	}
	
	public void execute() throws MojoExecutionException, MojoFailureException {
		if(directories.isEmpty()){
			directories.add("src");
		}
		try {
			
			final File cachePath = new File(basedir, "target/lint.cache");
			
			final Map<String, Result> previousResults;
			if(cachePath.exists()){
				previousResults = readObject(cachePath);
			}else{
				previousResults = new HashMap<String, Mojo.Result>();
			}
			
			List<File> javascriptFiles = new ArrayList<File>();

			for(String path: directories){
				collect(new File(basedir, path), javascriptFiles);
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


			final Map<String, Result> currentResults = new HashMap<String, Mojo.Result>();
			for(File file : matches){
				getLog().info("  " + file );
				Result previousResult = previousResults.get(file.getAbsolutePath());
				Result theResult;
				if(previousResult==null || (previousResult.lastModified.longValue()!=file.lastModified())){
					getLog().info("  " + file );
					List<Error> errors = jshint.run(new FileInputStream(file), options, globals);
					theResult = new Result(file.getAbsolutePath(), file.lastModified(), errors); 
				}else{
					getLog().info("  " + file + " [displaying cached results because this hasn't changed since " + previousResult.lastModified + "]");
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
			
			writeObject(currentResults, cachePath);
			
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


	private static <T> T readObject(File path){
		try {
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(path));
			try{
				return (T) in.readObject();
			}finally{
				in.close();
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static void writeObject(Object o, File path){
		try{
			ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(path));
			try{
				out.writeObject(o);
			}finally{
				out.close();
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static class FileErrors {
		final File path;
		final List<Error> errors;
		public FileErrors(File path, List<Error> errors) {
			super();
			this.path = path;
			this.errors = errors;
		}
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
