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
	 *   2) Make it incremental: only files that have changed since the last run are re-checked
	 *   3) Make it parallelizable: i.e. 'mvn install -Dlint.threads=4'
	 */

	public static class Record implements Serializable {
		final String path;
		final Long lastModified;
		final List<Error> errors;
		
		public Record(String path, Long lastModified, List<Error> errors) {
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
			
			final File cachePath = new File(basedir, "target/lint.dat");
			
			final Map<String, Record> previousProblems;
			if(cachePath.exists()){
				previousProblems = readObject(cachePath);
			}else{
				previousProblems = new HashMap<String, Mojo.Record>();
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


			final Map<String, Record> currentProblems = new HashMap<String, Mojo.Record>();
			for(File file : matches){
				getLog().info("  " + file );
				Record previousProblem = previousProblems.get(file.getAbsolutePath());
				Record theProblem;
				if(previousProblem==null || (previousProblem.lastModified.longValue()!=file.lastModified())){
					getLog().info("  " + file );
					List<Error> errors = jshint.run(new FileInputStream(file), options, globals);
					if(errors.size()>0){
						theProblem = new Record(file.getAbsolutePath(), file.lastModified(), errors); 
						
					}else{
						theProblem = null;
					}
				}else{
					getLog().info("  " + file + " [displaying cached results because this hasn't changed since " + previousProblem.lastModified + "]");
					theProblem = previousProblem;
				}
				
				if(theProblem!=null){
					currentProblems.put(theProblem.path, theProblem);
					Record r = theProblem;
					currentProblems.put(r.path, r);
					for(Error error: r.errors){
						getLog().error("   " + error.line.intValue() + "," + error.character.intValue() + ": " + error.reason);
					}
				}
			}
			
			writeObject(currentProblems, cachePath);
			
			if(currentProblems.size()>0){
				throw new MojoFailureException("JSHint found problems with " + currentProblems.size() + " files");
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
