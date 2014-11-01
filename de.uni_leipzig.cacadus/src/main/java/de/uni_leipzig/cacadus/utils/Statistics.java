package de.uni_leipzig.cacadus.utils;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Statistics {

	
	public static void main(String[] args){
		Statistics.setLinksFound(100);
		Statistics.setLinksVisit(123);
		Statistics.setPgnFounds(12);
		Statistics.setPGNsPerLinks(10);
		Statistics.setSitesWithPGNFiles(1233);
		Statistics.save("test.csv");
		Statistics.reset();
		Statistics.save("test2.csv");
	}
	
	private static long pgnFounds=0;
	private static long linksVisit=0;
	private static long linksFound=0;
	private static long sitesWithPGNFiles=0;
	private static double pgnsPerLinks=0;
//	private static long avgLinksOnSite;
	
	public static void reset(){
		for(Method method : Statistics.class.getMethods()){
			if(method.getName().startsWith("set") &&!method.getName().equals("setClass")){
				try {
					method.invoke(Statistics.class, 0);
				} catch (IllegalAccessException | IllegalArgumentException
						| InvocationTargetException e) {
				}
			}
		}
	}
	
	public static long getPgnFounds() {
		return pgnFounds;
	}
	public static long getLinksVisit() {
		return linksVisit;
	}
	public static long getLinksFound() {
		return linksFound;
	}
	public static double getPGNsPerLinks(){
		return pgnsPerLinks;
	}
	public static long sitesWithPGNFiles(){
		return sitesWithPGNFiles;
	}
	public static void setPgnFounds(long pgnFounds) {
		Statistics.pgnFounds = pgnFounds;
	}
	public static void setLinksVisit(long linksVisit) {
		Statistics.linksVisit = linksVisit;
	}
	public static void setLinksFound(long linksFound) {
		Statistics.linksFound = linksFound;
	}
	public static void setSitesWithPGNFiles(long sitesWithPGNFiles){
		Statistics.sitesWithPGNFiles = sitesWithPGNFiles;
	}
	
	public static void setPGNsPerLinks(double pgnsPerLinks){
		Statistics.pgnsPerLinks = pgnsPerLinks;
	}
	
	public static void addPgnFounds(long pgnFounds) {
		Statistics.pgnFounds += pgnFounds;
	}
	public static void addLinksVisit(long linksVisit) {
		Statistics.linksVisit += linksVisit;
	}
	public static void addLinksFound(long linksFound) {
		Statistics.linksFound += linksFound;
	}
	public static void addSitesWithPGNFiles(long sitesWithPGNFiles) {
		Statistics.sitesWithPGNFiles+=sitesWithPGNFiles;
	}
	public static void addPGNsPerLinks(double pgnsPerLinks) {
		Statistics.pgnsPerLinks+=pgnsPerLinks;
	}
	
	
	public static boolean save(String file){
		return save(new File(file));
	}
	
	public static boolean save(File file){
		try {
			file.createNewFile();
			PrintWriter pw = new PrintWriter(file);
			String line="", line2="";
			for(Method method : Statistics.class.getMethods()){
				if(method.getName().startsWith("get") &&!method.getName().equals("getClass")){
					String name=method.getName().substring(3);
					String field="";
					for(String n : name.split("(?<=[a-z])(?=[A-Z])")){
						field+=n+" ";
					}
					line+=field.substring(0, field.length()-1)+";";
					line2+=method.invoke(Statistics.class)+";";
				}
			}
			pw.println(line.substring(0, line.length()-1));
			pw.print(line2.substring(0, line2.length()-1));
			pw.close();
			return true;
		} catch (IOException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
			return false;
		}
	}
	
}
