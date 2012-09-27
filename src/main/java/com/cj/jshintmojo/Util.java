package com.cj.jshintmojo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.apache.commons.io.FileUtils;

public class Util {

	@SuppressWarnings("unchecked")
	public static <T> T readObject(File path){
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

	public static void writeObject(Object o, File path){
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
	

	public static void deleteDirectory(File directory) {
		try {
			FileUtils.deleteDirectory(directory);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static File tempDir() {
		try {
			File path = File.createTempFile("tempdirectory", ".dir");
			delete(path);
			mkdirs(path);
			return path;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static void delete(File path) {
		if(!path.delete()) throw new RuntimeException("Could not delete " + path.getAbsolutePath());
	}

	public static void mkdirs(File directory, String string) {
		File path = new File(directory, string);
		mkdirs(path);
	}

	public static void mkdirs(File path) {
		if(!path.mkdirs()){
			throw new RuntimeException("Could not create directory:" + path.getAbsolutePath());
		}
	}
	
}
