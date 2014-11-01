package de.uni_leipzig.cacadus.crawler;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.CodeSource;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;

import org.bio_gene.wookie.connection.ConnectionFactory;
import org.bio_gene.wookie.utils.ConfigParser;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import de.uni_leipzig.cacadus.converter.ConverterController;
import de.uni_leipzig.cacadus.service.EventService;
import de.uni_leipzig.cacadus.service.LiveRatingService;
import de.uni_leipzig.cacadus.service.PlayerService;
import de.uni_leipzig.cacadus.upload.LimesHandler;
import de.uni_leipzig.cacadus.upload.UploadHandler;
import de.uni_leipzig.cacadus.utils.Config;
import de.uni_leipzig.cacadus.utils.FileHandler;

public class Main
{
    public static void main( String[] args )
    {
    	ConnectionFactory.setDriver("org.apache.jena.jdbc.remote.RemoteEndpointDriver");
		ConnectionFactory.setJDBCPrefix("jdbc:jena:remote:query=http://");
//    	if(args[0].equals("--debug")){
//    		try {
//				ConfigParser cp = ConfigParser.getParser(args[1]);
//				Config.initXML((Node)cp.getElementAt("dog", 0)); 
//        	} catch (SAXException | IOException | ParserConfigurationException e) {
//        		System.err.println("An exception occured and the crawl process will be aborted. \nPlease run screaming in a circle now.\n\n");
//				e.printStackTrace();
//				System.exit(0);
//			}
//    		return;
//    	}
        switch(args.length){
        case 1: 
        	if(args[0].equals("--help")){
        		printHelp();
        		System.exit(0);
        	}
        	try {
				ConfigParser cp = ConfigParser.getParser(args[0]);
				Config.initXML((Node)cp.getElementAt("dog", 0)); 
        	} catch (SAXException | IOException | ParserConfigurationException e) {
        		System.err.println("An exception occured and the crawl process will be aborted. \nPlease run screaming in a circle now.\n\n");
				e.printStackTrace();
				System.exit(0);
			}
        	break;
        case Config.REQUIRED_PARAMETER_COUNT: configSet(args); break;
        default:
        	if(args.length>Config.REQUIRED_PARAMETER_COUNT){
        		configSet(args);
        	}
        	else{
        		printHelp();
        		System.exit(0);
        	}
        }
        printConfig();
        ConverterController cc = new ConverterController();
        cc.start();
    	LimesHandler lh = null;
        if(Config.getLimesLookup()){
        	lh = new LimesHandler();
        	lh.start();
        }
        UploadHandler uh = new UploadHandler();
        uh.start();
        PGNCrawlerController pgn = new PGNCrawlerController();
        pgn.start();
        while(cc.isAlive()|| pgn.isAlive()){
        	if(!pgn.isAlive())
        		cc.sendEndSignal();
//        	if(!cc.isAlive())
//        		lh.sendEndSignal();
//        	if(!lh.isAlive()){
        		 
//        	}
        }
        if(lh!=null){
        	lh.sendEndSignal();
        	while(lh.isAlive()){
        	}
        }
        System.out.println("Crawler finished");
        System.out.println("Starting services...");
	    System.out.println("...player service...");
	    PlayerService ps = new PlayerService();
	    ps.startService();
	    System.out.println("...stopping player service");
	    System.out.println("...event service...");
        EventService es = new EventService();
	    es.startService();
	    System.out.println("...stopping event service");
	    System.out.println("...live rating service...");
		LiveRatingService lrs = new LiveRatingService();
		lrs.startService();
		System.out.println("...stopping live rating service");
		uh.sendEndSignal();
        
    }
    
    private static void configSet(String[] args){
    	Properties p = new Properties();
    	for(int i=0;i<args.length;i++){
    		String key = args[i].trim().substring(2, args[i].indexOf("="));
    		String value = args[i].trim().substring(args[i].indexOf("=")+1);
    		p.put(key, value);
    	}
    	Config.init(String.valueOf(p.get("seedFile")), 
    			String.valueOf(p.get("numberOfCrawlers")), 
    			String.valueOf(p.get("graphURI")), 
    			String.valueOf(p.get("uploadFolder")), 
    			String.valueOf(p.get("converterFolder")), 
    			String.valueOf(p.get("convertedFolder")), 
    			String.valueOf(p.get("limesConfig")), 
    			String.valueOf(p.get("outputFormat")), 
    			String.valueOf(p.get("blackList")),
    			String.valueOf(p.get("maxDownloadSize")), 
    			String.valueOf(p.get("crawlStorageFolder")), 
    			String.valueOf(p.get("politenessDelay")), 
    			String.valueOf(p.get("maxDepth")),
    			String.valueOf(p.get("linkThreshold")),
    			String.valueOf(p.get("pgnsPerLinksThreshold")),
    			String.valueOf(p.get("endpoint")),
    			String.valueOf(p.get("user")),
    			String.valueOf(p.get("pwd")),
    			String.valueOf(p.get("unauthEndpoint")),
    			String.valueOf(p.get("backUp")),
    			String.valueOf(p.get("updateTimer")));
    }
    
    private static void printConfig(){
    	System.out.println("Configuration which will be used:");
    	System.out.println();
    	System.out.println("##########################\n");
    	System.out.println("seeds: ");
    	try {
			Object[] seeds = FileHandler.getLinesOfFile(Config.getSeedFile()).toArray();
			System.out.print("[");
			for(Object s : seeds)
				System.out.println("\t"+s);
			System.out.println("]");
		} catch (IOException e) {
			System.out.println("\t["+Config.PGNMENTOR_URL);
			System.out.println("\t "+Config.FIDE_URL+"]");
		}
    	System.out.println();
       	if(Config.getBlackList().size()>0){
       		System.out.println("blacklisted: ");
    		System.out.println("\t"+Config.getBlackList().toArray());
    		System.out.println();
       	}
    	System.out.println("Download folder: "+Config.getConverterFolder());
    	System.out.println("Converter folder: "+Config.getConvertedFolder());
    	System.out.println("Upload folder: "+Config.getUploadFolder());
    	System.out.println("Limes configuration file: "+Config.getLimesConfig());
    	if(Config.getGraphURI()!=null)
    		System.out.println("GraphURI: "+Config.getGraphURI());
    	System.out.println("Maximum Depth of Crawling: "+Config.getMaxDepthOfCrawling());
    	System.out.println("Maximum download size: "+Config.getMaxDownloadSize());
    	System.out.println("Number of crawlers: "+Config.getNumberOfCrawlers());
    	System.out.println("Converter output format: "+Config.getOutputFormat());
    	System.out.println("Politeness delay: "+Config.getPolitenessDelay());
    	if(Config.getLinkThreshold()>=0)
    		System.out.println(Config.getLinkThreshold());
    	else
    		System.out.println("No links will be added to blacklist");
    	if(Config.getPGNLinkThreshold()<=1.0)
    		System.out.println(Config.getPGNLinkThreshold());
    	else
    		System.out.println("No links will be added to the seeds");
    	String con = "Connection to use: "+Config.getConnection().getEndpoint();
    	if(Config.getConnection().getUser()!=null)
    		con+=" with user "+Config.getConnection().getUser();
    	
    	System.out.println(con);
    	con = "Unauthorized Connection: "+Config.getUnauthConnection().getEndpoint();
    	System.out.println(con);
    	if(Config.getBackUp())
    		System.out.println("Backup of updates in folder: "+Config.getBackUpFolder());
    	else
    		System.out.println("No backup of updates will be taken");
    	System.out.println("The timespan (in min.) updates will be uploaded: "+Config.getUpdateTimer());
    	
    }
    
    private static void printHelp(){
    	CodeSource codeSource = Main.class.getProtectionDomain().getCodeSource();
		String jarName= "dog.jar";
		try {
			File jarFile = new File(codeSource.getLocation().toURI().getPath());
			jarName = jarFile.getName();
		} catch (URISyntaxException e) {
		}
		
		System.out.println("Usage: "+jarName+" (args+|config.xml)");
		System.out.println();
		System.out.println("If you're using the arguments args instead of the XML file\n\t-The only connection type you can use is IMPL\n\t"
				+ "-Data description such as resourceURI can't be set");
		System.out.println();
		System.out.println("##################");
		System.out.println("##  Arguments:  ##");
		System.out.println("##################");
		System.out.println();
		System.out.println("required: ");
		System.out.println();
		System.out.println("\t--uploadFolder=[The folder in which the files, ready to upload, should be saved in]");
    	System.out.println("\t--converterFolder=[The folder in which the downloaded files should be saved in]");
    	System.out.println("\t--convertedFolder=[The folder in which the converted files should be saved in]");
		System.out.println("\t--endpoint=[The SPARQL endpoint to upload the files in]");
		System.out.println("\n");
		System.out.println("optional: ");
    	System.out.println("\t--limesConfig=[The location of the limes configuration file]");
		System.out.println("\t--seed=[file with the urls to start with]");
		System.out.println("\t--numberOfCrawlers=[How many crawlers (threads) should be used]");
		System.out.println("\t--graphURI=[The graph in which the files should be uploaded]");
		System.out.println("\t--outputFormat=[The format (RDF/XML|TURTLE|NTRIPLE) in which the files should be converted in]");
		System.out.println("\t--blackList=[The name of the file with the urls not to visit]");
		System.out.println("\t--maxDownloadSize=[The maximal download size]");
		System.out.println("\t--crawlStorageFolder=[The folde in which the crawler should store its files]");
		System.out.println("\t--politenessDelay=[The delay to wait for the next request]");
    	System.out.println("\t--maxDepth=[The maximum depth to search in the world wide web]");
    	System.out.println("\t--user=[The user for the SPARQL Connection]");
    	System.out.println("\t--pwd=[The password for the user for the SPARQL Connection]");
    	System.out.println("\t--linkThreshold=[A link will be added to the blacklist if the links in the link will be under the threshold]");
    	System.out.println("\t--pgnsPerLinksThreshold=[A link will be added to the seedlist of the pgns/links is over the threshold]");
    	System.out.println("\t--unauthEndpoint=[The SPARQL endpoint with no authorization (default=endpoint)]");
    	System.out.println("\t--updateTimer=[The timespan (in min.) when updates will be uploaded (for 2700chess Service etc.)]");
    	System.out.println("\t--backUp=[true|false should be taken backups of the Updates (default=false)]");

    }
}
