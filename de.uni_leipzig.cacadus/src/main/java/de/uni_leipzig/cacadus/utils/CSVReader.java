package de.uni_leipzig.cacadus.utils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import de.uni_leipzig.cacadus.utils.types.Player;

public class CSVReader implements Iterator<Player>{

	private FileInputStream fis;
	private BufferedReader br;
	
	private String[] header = new String[18];
	private List<Integer> spaces = new ArrayList<Integer>();
	private Boolean next=false;
	private String line;
	private Player nextPlayer;
	
//	private String delimiter=";";
	
	public static void main(String[] args) throws IOException{
		CSVReader reader = new CSVReader("player_2014_10_10/player_list.txt");
		while(reader.hasNext())
			System.out.println("next: "+reader.next());
		
	}
	
	public void close(){
		try{
			fis.close();
		}
		catch(Exception e){}
		try{
			br.close();
		}
		catch(Exception e){}
	}
	
	public CSVReader(String fileName) throws IOException{
		fis = new FileInputStream(fileName);
		br = new BufferedReader(new InputStreamReader(fis, Charset.forName("UTF-8")));
		String head = br.readLine();
		spaces.add(0);
		header[0] = "ID Number";
		head = head.replace("ID Number", "         ");
		String current="";
		int h =1;
		for(int i=0; i<head.length(); i++){
			if(head.charAt(i)!=' '){
				if(head.charAt(i-1)==' ')
					spaces.add(i);
				current+=head.charAt(i);
			}
			else{
				if(!current.isEmpty()){
					header[h++] = current;
					current="";
				}
			}
		}
		if(!current.isEmpty()){
			header[h++] = current;
		}
//		header = br.readLine().split(delimiter);
	}
	
	public String[] getHeader(){
		return header;
	}

	@Override
	public boolean hasNext() {
		if(next)
			return true;
		do{
			nextPlayer = getNext();
			if(nextPlayer==null)
				return false;
		}while(!nextPlayer.hasRating());
		next=true;
		return true;
//		try {
//			if(!next){
//				line = br.readLine();
//				next=true;
//				if(line!=null){					
//					return true;
//				}
//				return false;
//			}
//			else{
//				if(line!=null)
//					return true;
//				return false;
//			}
//		} catch (IOException e) {
//			return false;
//		}
	}
	
	public Player getNext() {
		next =false;
		if(line==null){
			try {
				line = br.readLine();
				if(line==null)
					return null;
			} catch (IOException e) {
				return null;
			}
		}
		HashMap<String, String> map = new HashMap<String, String>();
		String[] split = new String[spaces.size()];
		for(int k=0;k<spaces.size()-1;k++){
			try{
				split[k] = line.substring(spaces.get(k), spaces.get(k+1)).trim();
			}catch(Exception e){
				System.out.println("");
			}
		}
		split[spaces.size()-1] = line.substring(spaces.get(spaces.size()-1)).trim();
		for(int i=0; i<header.length;i++){
			map.put(header[i], split[i]);
		}
		line=null;
		Player ret = new Player();
		ret.setMap(map);
		return ret;
	}

	@Override
	public Player next() {
//		
//		if(line==null){
//			try {
//				line = br.readLine();
//			} catch (IOException e) {
//				return null;
//			}
//		}
//		HashMap<String, String> map = new HashMap<String, String>();
//		String[] split = new String[spaces.size()];
//		for(int k=0;k<spaces.size()-1;k++){
//			split[k] = line.substring(spaces.get(k), spaces.get(k+1)).trim();
//		}
//		split[spaces.size()-1] = line.substring(spaces.get(spaces.size()-1)).trim();
//		for(int i=0; i<header.length;i++){
//			map.put(header[i], split[i]);
//		}
//		line=null;
//		Player ret = new Player();
//		ret.setMap(map);
//		return ret;
		if(!next)
			hasNext();
		next =false;
		return nextPlayer;
	}

	@Override
	public void remove() {
	}
	
	
	
}
