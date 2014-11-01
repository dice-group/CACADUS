package de.uni_leipzig.cacadus.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.HashSet;

public class FileHandler {

	
	public static Collection<String> getLinesOfFile(String fileName) throws IOException{
		return getLinesOfFile(new File(fileName));
	}
	
	public static Collection<String> getLinesOfFile(File file) throws IOException{
		Collection<String> set = new HashSet<String>();
		FileInputStream fis = null;
		BufferedReader br = null;
		try{
			fis = new FileInputStream(file);
			br = new BufferedReader(new InputStreamReader(fis, Charset.forName("UTF-8")));
			String line="";
			while((line = br.readLine())!=null){
				set.add(line);
			}
		}
		catch(IOException e){
			throw new IOException(e);
		}
		finally{
			if(fis!=null)
				fis.close();
			if(br!=null)
				br.close();
		}
		return set;
	}
 
	
	
	public static boolean writeBytesToFile(String name, byte[] content) throws IOException{
		File f = new File(name);
		if(f.exists())
			return false;
		f.createNewFile();
		FileOutputStream fos = null;
		try{
			fos = new FileOutputStream(f);
			fos.write(content);
		}
		catch(IOException e){
			throw new IOException(e);
		}
		finally{
			if(fos!=null)
				fos.close();
		}
		return true;
	}
	
	public static boolean isFolderEmpty(String folderName){
		return isFolderEmpty( new File(folderName));
	}
	
	public static boolean isFolderEmpty(File dir){
		if(dir.list().length>0)
			return false;
		return true;
	}
	
}
