package com.cj.jshintmojo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

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
}
