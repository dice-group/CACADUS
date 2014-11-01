package de.uni_leipzig.cacadus.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.xml.parsers.ParserConfigurationException;

import org.bio_gene.wookie.connection.Connection;
import org.bio_gene.wookie.connection.ConnectionFactory;
import org.bio_gene.wookie.utils.ConfigParser;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.ibm.icu.text.SimpleDateFormat;


public class Config {
	
	private static String seedFile="suggestedSeeds.txt";
	private static int numberOfCrawlers=2;
	private static String graphURI;
	private static String uploadFolder;
	private static String converterFolder;
	private static String convertedFolder;
	private static String limesConfig="LIMES.xml";
	private static String outputFormat = "RDF/XML";
	private static String blackList="suggestedBlackList.txt";
	private static int maxDownloadSize = 5000000;
	private static String crawlStorageFolder=UUID.randomUUID().toString();
	private static int politenessDelay=1000;
	private static int maxDepth=20000;
	private static Connection con;
	private static Connection unauthCon;
	private static double pgnPerLinkThreshold=1.1;
	private static int linkThreshold=-1;
	
	private static boolean dataDescription=false;
	private static String namespace="http://example.com/";
	private static String anchor="#";
	private static String prefix="http://example.com/prop/";
	private static String resourceURI="http://example.com/res/";
	private static String propertyPrefixName="prop";
	private static String resourcePrefixName="res";
	private static boolean metaData=true;
	private static int updateTimer=60;
	
	private static boolean backUp=false;
	
	private static final String date = new SimpleDateFormat("yyyy_MM_dd").format(new Date());
	private static String updateFolder="update_"+date;
	private static String backUpFolder="backup_"+date;
	private static String playerFolder="player_"+date;
	private static String eventFolder="event_"+date;
	private static boolean limesLookup = false;

	public static final String PGNMENTOR_URL = "http://www.pgnmentor.com";
	public static final int REQUIRED_PARAMETER_COUNT = 5;
	public static final String W3CTYPE_URI ="http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
	public static final String FIDE_URL = "http://www.fide.com";
	public static final String test = "http://ratings.fide.com/view_pgn.phtml?code=99392&download=1";
	
	
	public static String getSeedFile() {
		return seedFile;
	}

	public static int getNumberOfCrawlers() {
		return numberOfCrawlers;
	}

	public static String getGraphURI() {
		return graphURI;
	}

	public static String getUploadFolder() {
		return uploadFolder;
	}

	public static String getConvertedFolder() {
		return convertedFolder;
	}

	public static String getConverterFolder() {
		return converterFolder;
	}
	
	public static Connection getConnection() {
		return con;
	}

	public static String getLimesConfig() {
		return limesConfig;
	}


	public static String getOutputFormat() {
		return outputFormat;
	}

	public static void init(String seedFile, String numberOfCrawlers,
			String graphURI, String uploadFolder, 
			String converterFolder, String convertedFolder,
			String limesConfig,	String outputFormat,
			String blackList, String maxDownloadSize,
			String crawlStorageFolder, String politenessDelay,
			String maxDepth, 
			String linkThreshold, String pgnPerLinkThreshold,
			String endpoint, String user, String pwd, String endpointUnauth, 
			String backUp, String updateTimer){
		
		Integer numCraw=null;
		if(!numberOfCrawlers.equals("null"))
			numCraw = Integer.valueOf(numberOfCrawlers);
		Integer mDSize=null;
		if(!maxDownloadSize.equals("null"))
			 mDSize = Integer.valueOf(maxDownloadSize);
		Integer mDepth=null, lTh=null, uTim = null, pDelay=null;
		Double pgnPLTh =null;
		if(!politenessDelay.equals("null"))
			 pDelay=Integer.valueOf(politenessDelay);
		if(!maxDepth.equals("null"))
			 mDepth=Integer.valueOf(maxDepth);
		if(!linkThreshold.equals("null"))
			 lTh=Integer.valueOf(linkThreshold);
		if(!pgnPerLinkThreshold.equals("null"))
			 pgnPLTh=Double.valueOf(pgnPerLinkThreshold);
		if(!updateTimer.equals("null"))
			 uTim=Integer.valueOf(updateTimer);
		init(seedFile, numCraw,
				graphURI, uploadFolder, 
				converterFolder, convertedFolder,
				limesConfig,	outputFormat,
				blackList, mDSize,
				crawlStorageFolder, pDelay,
				mDepth, 
				lTh, pgnPLTh,
				endpoint, user, pwd, endpointUnauth, 
				Boolean.valueOf(backUp), uTim);
	}
	
	public static void init(String seedFile, Integer numberOfCrawlers,
							String graphURI, String uploadFolder, 
							String converterFolder, String convertedFolder,
							String limesConfig,	String outputFormat,
							String blackList, Integer maxDownloadSize,
							String crawlStorageFolder, Integer politenessDelay,
							Integer maxDepth, 
							Integer linkThreshold, Double pgnPerLinkThreshold,
							String endpoint, String user, String pwd, String endpointUnauth,
							Boolean backUp, Integer updateTimer){
		//required
		Config.uploadFolder=uploadFolder;
		Config.converterFolder=converterFolder;
		Config.convertedFolder=convertedFolder;
		if(limesConfig!=null)
			Config.limesConfig=limesConfig;
		//optional
		if(!seedFile.equals("null"))
			Config.seedFile=seedFile;
		if(numberOfCrawlers!=null)	
			Config.numberOfCrawlers=numberOfCrawlers;
		if(!graphURI.equals("null"))
			Config.graphURI=graphURI;
		if(!outputFormat.equals("null"))
			Config.outputFormat=outputFormat;
		if(!blackList.equals("null"))
			Config.blackList=blackList;
		if(maxDownloadSize!=null)
			Config.maxDownloadSize=maxDownloadSize;
		if(!crawlStorageFolder.equals("null"))	
			Config.crawlStorageFolder=crawlStorageFolder;
		if(politenessDelay!=null)
			Config.politenessDelay=politenessDelay;
		if(maxDepth!=null)
			Config.maxDepth=maxDepth;
		if(linkThreshold!=null)
			Config.linkThreshold = linkThreshold;
		if(pgnPerLinkThreshold!=null)
			Config.pgnPerLinkThreshold = pgnPerLinkThreshold;
		if(!user.equals("null") && !pwd.equals("null")){
			Config.con = ConnectionFactory.createImplConnection(endpoint, user, pwd, endpoint);
		}
		else{
			Config.con = ConnectionFactory.createImplConnection(endpoint, endpoint);
		}
		if(endpointUnauth.equals("null"))
			Config.unauthCon = con;
		else
			Config.unauthCon = ConnectionFactory.createImplConnection(endpointUnauth, endpointUnauth);
		Config.backUp=backUp;
		if(updateTimer!=null)
			Config.updateTimer = updateTimer;
		new File(Config.eventFolder).mkdir();
		new File(Config.playerFolder).mkdir();
		if(Config.backUp)
			new File(Config.backUpFolder).mkdir();
		new File(Config.updateFolder).mkdir();
	}
	
	public static void initXML(Node rootNode) throws SAXException, IOException, ParserConfigurationException{
		ConfigParser cp = ConfigParser.getParser(rootNode);
		Element connection = cp.getElementAt("connection", 0);
		
//		String graphURI =  connection.getAttribute("graphURI");
//		Config.graphURI = (graphURI.isEmpty()?null:graphURI);
		
		String type =  connection.getAttribute("type");
		String endpoint=cp.getElementAt("endpoint", 0).getAttribute("value");
		cp.setNode(connection);
		String endpointUpdate="";
		try{
			cp.getElementAt("endpointUpdate", 0).getAttribute("value");
		}
		catch(Exception e){
			endpointUpdate = endpoint;
		}
		cp.setNode(connection);
		String endpointUnauth="";
		try{
			endpointUnauth=cp.getElementAt("unauthEndpoint", 0).getAttribute("value");
		}
		catch(Exception e){
			endpointUnauth =endpoint;
		}
		Config.unauthCon = ConnectionFactory.createImplConnection(endpointUnauth, endpointUnauth);
		cp.setNode(connection);
		String user=null, pwd=null;
		try{
			user=cp.getElementAt("user", 0).getAttribute("name");
			cp.setNode(connection);
			pwd=cp.getElementAt("pwd", 0).getAttribute("value");
		}
		catch(Exception e){}
		switch(type){
		case "curl": 
			cp.setNode(connection);
			String curlCommand=cp.getElementAt("curlCommand", 0).getAttribute("command");
			cp.setNode(connection);
			String curlDrop=cp.getElementAt("curlDrop", 0).getAttribute("command");
			cp.setNode(connection);
			String curlURL=cp.getElementAt("curlURL", 0).getAttribute("url");
			cp.setNode(connection);
			String curlUpdate=cp.getElementAt("curlUpdate", 0).getAttribute("command");
			ConnectionFactory.createCurlConnection(endpoint, user, pwd, curlCommand, curlDrop, curlURL, curlUpdate, endpointUpdate); 
			break;
		case "impl": 	
			if((user==null || pwd==null) || (user.isEmpty() || pwd.isEmpty())){
				Config.con=ConnectionFactory.createImplConnection(endpoint, endpoint);
			}
			else{
				Config.con=ConnectionFactory.createImplConnection(endpoint, user, pwd, endpoint);
			}
			break;
		}
		
		try{
			cp.setNode((Element) rootNode);
			Element crawler = cp.getElementAt("crawler", 0);
			if(crawler==null)
				throw new Exception("No crawler tag");
			try{
				Config.seedFile=cp.getElementAt("seedFile", 0).getAttribute("file");
				cp.setNode(crawler);
			}
			catch(Exception e){}
			try{
				Config.numberOfCrawlers=Integer.valueOf(cp.getElementAt("numberOfCrawlers", 0).getAttribute("value"));
				cp.setNode(crawler);
			}
			catch(Exception e){}
			try{
				Config.graphURI=cp.getElementAt("graphURI", 0).getAttribute("uri");
				cp.setNode(crawler);
			}
			catch(Exception e){}
			try{
				Config.outputFormat=cp.getElementAt("outputFormat", 0).getAttribute("value");
				cp.setNode(crawler);
			}
			catch(Exception e){}
			try{
				Config.blackList=cp.getElementAt("blackList", 0).getAttribute("file");
				cp.setNode(crawler);
			}
			catch(Exception e){}
			try{
				Config.maxDownloadSize=Integer.valueOf(cp.getElementAt("maxDownloadSize", 0).getAttribute("value"));
				cp.setNode(crawler);
			}
			catch(Exception e){}
			try{
				Config.crawlStorageFolder=cp.getElementAt("crawlStorageFolder", 0).getAttribute("path");
				cp.setNode(crawler);
			}
			catch(Exception e){}
			try{
				Config.politenessDelay=Integer.valueOf(cp.getElementAt("politenessDelay", 0).getAttribute("value"));
				cp.setNode(crawler);
			}
			catch(Exception e){}
			try{
				Config.maxDepth=Integer.valueOf(cp.getElementAt("maxDepth", 0).getAttribute("value"));
			}
			catch(Exception e){}
		}
		catch(Exception e){
		}
		cp.setNode((Element) rootNode);
		Element general = cp.getElementAt("general", 0);
		Config.converterFolder=cp.getElementAt("downloadFolder", 0).getAttribute("path");
		new File(Config.converterFolder).mkdirs();
		cp.setNode(general);
		Config.convertedFolder=cp.getElementAt("converterFolder", 0).getAttribute("path");
		new File(Config.convertedFolder).mkdirs();
		cp.setNode(general);
		Config.uploadFolder=cp.getElementAt("uploadFolder", 0).getAttribute("path");
		new File(Config.uploadFolder).mkdirs();
		cp.setNode(general);
		try{
			Config.limesConfig=cp.getElementAt("limesConfig", 0).getAttribute("file");
		}catch(Exception e){}
		cp.setNode(general);
		try{
			Config.limesLookup=Boolean.valueOf(cp.getElementAt("limesLookUp", 0).getAttribute("value"));
		}catch(Exception e){}
		cp.setNode(general);
		try{
			Config.backUp=Boolean.valueOf(cp.getElementAt("backUp", 0).getAttribute("value"));
		}catch(Exception e){}
		cp.setNode(general);
		try{
			Config.updateTimer=Integer.valueOf(cp.getElementAt("updateTimer", 0).getAttribute("value"));
		}catch(Exception e){}
		try{
			cp.setNode(general);
			Config.linkThreshold=Integer.valueOf(cp.getElementAt("linkThreshold", 0).getAttribute("value"));
		}catch(Exception e){
		}
		try{
			cp.setNode(general);
			Config.pgnPerLinkThreshold=Double.valueOf(cp.getElementAt("pgnsPerLinksThreshold", 0).getAttribute("value"));
		}catch(Exception e){
		}
		cp.setNode((Element) rootNode);
		try{
			Element description = cp.getElementAt("description", 0);
			if(!description.getAttribute("metaData").isEmpty())
				Config.metaData = Boolean.valueOf(description.getAttribute("metaData"));
			Config.namespace = cp.getElementAt("namespace", 0).getAttribute("value");
			cp.setNode(description);
			Config.resourceURI = cp.getElementAt("resourceURI", 0).getAttribute("value");
			cp.setNode(description);
			Config.prefix = cp.getElementAt("propertyURI", 0).getAttribute("value");
			Config.dataDescription=true;
		}catch(Exception e){
			Config.dataDescription=false;
		}
		new File(Config.eventFolder).mkdir();
		new File(Config.playerFolder).mkdir();
		if(backUp)
			new File(Config.backUpFolder).mkdir();
		new File(Config.updateFolder).mkdir();
	}

	public static Set<String> getBlackList() {
		try {
			return (Set<String>) FileHandler.getLinesOfFile(blackList);
		} catch (IOException e) {
			return new HashSet<String>();
		}
	}

	public static int getMaxDownloadSize() {
		return maxDownloadSize ;
	}

	public static String getCrawlStorageFolder() {
		return crawlStorageFolder;
	}

	public static int getPolitenessDelay() {
		return politenessDelay;
	}

	public static int getMaxDepthOfCrawling() {
		return maxDepth;
	}

	public static double getPGNLinkThreshold() {
		return pgnPerLinkThreshold;
	}

	public static int getLinkThreshold() {
		return linkThreshold;
	}

	private static void append(File f, String content){
		if(!f.exists())
			try {
				f.createNewFile();
			} catch (IOException e1) {
				return;
			}
		PrintWriter pw = null;
		try {
			if(FileHandler.getLinesOfFile(f).contains(content))
				return;
			pw = new PrintWriter(new FileOutputStream(f, true));
			pw.append(content);
		} catch (IOException e) {
			return;
		}
		finally{
			if(pw!=null)
				pw.close();
		}

	}
	
	public static void addBlackList(String url) {
		File f = new File(Config.blackList);
		append(f, url);
	}

	public static void addSeedLink(String url) {
		File f = new File(Config.seedFile);
		append(f, url);
	}

	public static boolean isDataDescription() {
		return dataDescription;
	}

	public static String getNamespace() {
		return namespace;
	}

	public static String getAnchor() {
		return anchor;
	}

	public static String getPrefix() {
		return prefix;
	}

	public static String getResourceURI() {
		return resourceURI;
	}

	public static String getPropertyPrefixName() {
		return propertyPrefixName;
	}

	public static String getResourcePrefixName() {
		return resourcePrefixName;
	}

	public static boolean getMetaData() {
		return metaData;
	}

	public static int getUpdateTimer() {
		return updateTimer;
	}

	public static String getUpdateFolder() {
		return updateFolder;
	}

	public static boolean getBackUp() {
		return backUp;
	}

	public static Connection getUnauthConnection() {
		return unauthCon;
	}

	public static String getEventFolder() {
		return eventFolder;
	}

	public static String getPlayerFolder() {
		return playerFolder;
	}

	public static String getBackUpFolder() {
		return backUpFolder;
	}

	public static boolean getLimesLookup() {
		return limesLookup ;
	}


}
