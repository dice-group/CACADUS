package de.uni_leipzig.cacadus.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
//import java.util.Map;
//import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bio_gene.wookie.utils.LogHandler;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;

import de.uni_leipzig.cacadus.upload.Updater;
import de.uni_leipzig.cacadus.utils.Config;
import de.uni_leipzig.cacadus.utils.types.Player;
//import de.uni_leipzig.dog.upload.LimesHandler;

public class LiveRatingService implements Service<Player>{

	
	public static void main(String[] argc){
		LiveRatingService lrs = new LiveRatingService();
		lrs.startService();
	}
	
	private static String link = "http://www.2700chess.com/ratings.php";
	private static String linkU = "http://www.2700chess.com";
	private static String fideString = "http://ratings.fide.com/id.phtml?event=";
	private static String updates="liveUpdates.upd";
	
	private Logger log = Logger.getLogger("Service");
	private List<String> fideIds = new ArrayList<String>();
	private HashMap<String, String> map;
	
	private File downloadFile(){
		BufferedReader br = null;
		File f = new File(link.replaceAll("[^A-Za-z0-9]", "_"));
		
		PrintWriter pw = null;
		try {
			URL url = new URL(link);
			URLConnection urlConn = url.openConnection();
			br = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
			String line;
			f.createNewFile();
			pw = new PrintWriter(f);
			boolean begin=false;
			while((line=br.readLine())!=null){
				if(line.contains("<tbody>")){
					begin=true;
				}
				else if(line.contains("</tbody>")){
					pw.println(line);
					break;
				}
				if(begin){
					if(line.contains(fideString)){
						Pattern  p = Pattern.compile(fideString+"[0-9]+\"");
						Matcher m = p.matcher(line);
						if(m.find()){
							fideIds.add(m.group().replace(fideString, "").replace("\"", ""));
						}
					}
					pw.println(line);
				}
			}
			
		} catch (IOException e) {
			return null;
		}finally{
			try{
				pw.close();
			}catch(Exception e){}
			try{
				br.close();
			}catch(Exception e){}
		}
		return f;
	}
	
	private void getResources(){
		map = new HashMap<String, String>();
		String query="SELECT ?fide ?s WHERE {?s <"+Player.getFide().getURI()+"> ?fide. ";
		String r = "?s <"+Player.getFide().getURI()+"> ";
		for(String fide : fideIds){
			query+=r+fide+" . ";
		}
		query.substring(0, query.length()-2);
		query+="}";
		try{
			ResultSet res = Config.getConnection().select(query);
			while(res.next()){
				map.put(res.getString("fide"), res.getString("s"));
			}
		}
		catch(Exception e){
			LogHandler.writeStackTrace(log, e, Level.SEVERE);
		}
	}
	
	private String getUpdateFromURL(){
		BufferedReader br = null;
		try {
			URL url = new URL(linkU);
			URLConnection urlConn = url.openConnection();
			br = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
			String line;
			while((line=br.readLine())!=null){
//				System.out.println(line);
				if(line.toLowerCase().contains(" last update: ")){
					line=line.substring(line.toLowerCase().indexOf("last update: "));
					line=line.replaceAll("(l|L)ast (U|u)pdate: ", "");
					line=line.substring(0,line.indexOf("<"));
					return line.replace("\"", "");
				}
			}
			
		} catch (IOException e) {
			return null;
		}finally{
			try{
				br.close();
			}catch(Exception e){}
		}
		return "";
	}
	
	private int monthToInt(String month){
		Calendar cal = Calendar.getInstance();
		try {
			cal.setTime(new SimpleDateFormat("MMMM", Locale.ENGLISH).parse(month));
		} catch (ParseException e) {
			return -1;
		}
		return cal.get(Calendar.MONTH);
	}
	
	private Calendar getLastUpdate(){
		Calendar cal = Calendar.getInstance();

		String calString = getUpdateFromURL();
		DateFormat sdf = new SimpleDateFormat("dd MMMM yyyy, hh:mm z");
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
		String[] cals = calString.replace(",", "").split(" ");
		cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(cals[0]));
		
		cal.set(Calendar.MONTH, monthToInt(cals[1]));
		cal.set(Calendar.YEAR, Integer.parseInt(cals[2]));
		cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(cals[3].substring(0, cals[3].indexOf(":"))));
		cal.set(Calendar.MINUTE, Integer.parseInt(cals[3].substring(cals[3].indexOf(":")+1)));
		cal.setTimeZone(TimeZone.getTimeZone("GMT"));
//		System.out.println(sdf.format(cal.getTime()));
		return cal;
	}
	
	private void appendUpdate(Calendar cal){
		File f = new File(updates);
		PrintWriter pw=null;
		SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy, hh:mm z");
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
		try{
//			cal.setTime(sdf.parse(calString));
			f.createNewFile();
			pw = new PrintWriter(new FileOutputStream(f, true));		    
			pw.append(sdf.format(cal.getTime())+"\n");		
		}catch(IOException e){
			LogHandler.writeStackTrace(log, e, Level.SEVERE);
		}
		finally{
			try{
				pw.close();
			}catch(Exception e){}
		}
	}
	
	private long getUpdateDiff(Calendar cal2){
		Calendar cal = Calendar.getInstance();
		FileInputStream fis = null;
		
		BufferedReader br = null;
		String line="";
		try{
			new File(updates).createNewFile();
			fis = new FileInputStream(updates);
			br = new BufferedReader(new InputStreamReader(fis, Charset.forName("UTF-8")));
			String line2="";
			int upd = 0;
			while((line = br.readLine())!= null){
				line2=line;
				upd++;
			}
			if(upd==0){
				appendUpdate(cal2);
				return 0;
			}
		    SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy, HH:mm z");
		    sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
		    cal.setTime(sdf.parse(line2));
		}
		catch(IOException | ParseException e){
//			LogHandler.writeStackTrace(log, e, Level.SEVERE);
		}
		finally{
			try {
				if(fis!=null)
					fis.close();
				if(br!=null)
					br.close();
			} catch (IOException e) {
//				LogHandler.writeStackTrace(log, e, Level.SEVERE);
			}
		}
		return cal2.getTimeInMillis()-cal.getTimeInMillis()==0?-1:0;
	}
	
	private Model getModel(File f){
		Model m = ModelFactory.createDefaultModel();
		FileInputStream fis = null;
		BufferedReader br = null;
		String line="";
		try{
			fis = new FileInputStream(f);
			br = new BufferedReader(new InputStreamReader(fis, Charset.forName("UTF-8")));
			String html="";
			boolean begin=false;
			while((line = br.readLine())!= null){
				if(line.contains("<tr")){
					begin=true;
				}
				else if(line.contains("<tr/>")){
					m.add(getFromXML(html));
					begin=false;
					html="";
				}
				if(begin){
					html+=line+"\n";
				}
			}
		}
		catch(IOException e){
//			LogHandler.writeStackTrace(log, e, Level.SEVERE);
		}
		finally{
			try {
				fis.close();
				br.close();
			} catch (IOException e) {
//				LogHandler.writeStackTrace(log, e, Level.SEVERE);
			}
		}
		return m;
	}
	
	
	private String innerValue(String tags){
		String ret="";
		ret = tags.replaceAll("<[^>]*>", "");
		return ret.trim();
	}
	
	private Model getFromXML(String html){
		Model m = ModelFactory.createDefaultModel();
		String[] lines = html.split("\n");
		lines[4] = lines[4].substring(lines[4].indexOf("title=\""));
		lines[4] = lines[4].substring(lines[4].indexOf("\"")+1, lines[4].lastIndexOf("\""));
		lines[14] = lines[14].substring(lines[14].indexOf("value=\""));
		lines[14] = lines[14].substring(lines[14].indexOf("\"")+1);
		lines[14] = lines[14].substring(0, lines[14].indexOf("\""));
		Pattern  p = Pattern.compile(fideString+"[0-9]+\"");
		Matcher match = p.matcher(lines[13]);
		String fide=null;
		if(match.find()){
			fide = match.group().replace(fideString, "").replace("\"", "");
		}
		String name = innerValue(lines[3]);
		String resource = map.get(fide);
		if(resource==null){
			resource = name+"_"+fide;
		}
		Resource res = m.createResource(resource);
		 //replace everything in <>
		String country = lines[4]; //title=""
		res.addProperty(Player.getFed(), country);
		String std = innerValue(lines[5]); //replace everything in <>
		res.addLiteral(Player.getLiveSRtng(), m.createTypedLiteral(Double.valueOf(std)));
		String rpd = innerValue(lines[9]); //replace everything in <>
		res.addLiteral(Player.getLiveRRtng(), m.createTypedLiteral(Double.valueOf(rpd)));
		String btz = innerValue(lines[11]); //replace everything in <>
		res.addLiteral(Player.getLiveBRtng(), m.createTypedLiteral(Double.valueOf(btz)));
		String bday = lines[14]; //input ... value=""
		res.addProperty(Player.getBDay(), bday);
		return m;
	}
	
	
	@Override
	public void startService() {
		LogHandler.initLogFileHandler(log, "service");
		Calendar cal = getLastUpdate();
		if(getUpdateDiff(cal)<0){
			return;
		}
		appendUpdate(cal);
		File f  = downloadFile();
		getResources();
		Model m = getModel(f);
		try {
			Updater.saveFromModel(m);
			Updater.uploadStorage();
		} catch (IOException | SQLException e) {
			LogHandler.writeStackTrace(log, e, Level.SEVERE);
		}
	}

}
