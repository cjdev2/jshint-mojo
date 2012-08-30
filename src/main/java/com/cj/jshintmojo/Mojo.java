package com.cj.jshintmojo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import com.cj.jshintmojo.FunctionalJava.Fn;
import com.cj.jshintmojo.JSHint.Error;

/**
 * @goal lint
 * @phase verify
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
    
    public void execute() throws MojoExecutionException, MojoFailureException {
    	getLog().info("Found " + directories.size() + " directories");
    	try {
			List<File> javascriptFiles = new ArrayList<File>();
			
			for(String path: directories){
				collect(new File(basedir, path), javascriptFiles);
			}
			
			
			List<File> matches = FunctionalJava.filter(javascriptFiles, new Fn<File, Boolean>(){
				public Boolean apply(File i) {
					for(String exclude : excludes){
						File file = new File(basedir, exclude);
						if(file.getAbsolutePath().equals(i.getAbsolutePath())){
							getLog().warn("Excluding " + file);
							return Boolean.FALSE;
						}
					}

					return Boolean.TRUE;
				}
			});
			
			JSHint jshint = new JSHint();
			
			List<FileErrors> problemFiles = new ArrayList<Mojo.FileErrors>();
			for(File file : matches){
				getLog().info("  " + file );
				List<Error> errors = jshint.run(new FileInputStream(file), options, globals);
				if(errors.size()>0){
					problemFiles.add(new FileErrors(file, errors));
					for(Error error: errors){
						getLog().error("   " + error.line.intValue() + "," + error.character.intValue() + ": " + error.reason);
					}
				}
			}
			
			if(problemFiles.size()>0){
				throw new MojoFailureException("JSHint found problems with " + problemFiles.size() + " files");
			}
		} catch (FileNotFoundException e) {
			throw new MojoExecutionException("Something bad happened", e);
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
