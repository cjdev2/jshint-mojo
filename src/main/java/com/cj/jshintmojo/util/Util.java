package com.cj.jshintmojo.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.maven.model.FileSet;

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
		if(!path.exists() && !path.mkdirs()){
			throw new RuntimeException("Could not create directory: " + path.getAbsolutePath());
		}
	}

    public static List<File> toFileList(final FileSet fileSet) {
        File directory = new File(fileSet.getDirectory());
        String includes = toCommaSeparated(fileSet.getIncludes());
        String excludes = toCommaSeparated(fileSet.getExcludes());
        try {
            return org.codehaus.plexus.util.FileUtils.getFiles(
                    directory, includes, excludes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String toCommaSeparated(final List<String> strings) {
        StringBuilder sb = new StringBuilder();
        for (String s : strings) {
            sb.append(s).append(',');
        }
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        }
        return sb.toString();
    }

}
