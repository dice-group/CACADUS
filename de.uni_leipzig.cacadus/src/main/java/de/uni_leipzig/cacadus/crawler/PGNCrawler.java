package de.uni_leipzig.cacadus.crawler;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.apache.commons.codec.digest.DigestUtils;
import org.bio_gene.wookie.utils.LogHandler;

import de.uni_leipzig.cacadus.utils.Config;
import de.uni_leipzig.cacadus.utils.FileHandler;
import de.uni_leipzig.cacadus.utils.Statistics;
import de.uni_leipzig.cacadus.utils.ZipUtils;
import de.uni_leipzig.mosquito.utils.StringHandler;
import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.BinaryParseData;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;

public class PGNCrawler extends WebCrawler{
	
	private static final Pattern FILTERS = Pattern.compile(".*(\\.(css|js|jpe?g|gif|png|mid|mp2|mp3|mp4|wav|avi|mov|mpeg|ram|m4v|pdf"
		      + "|rm|smil|wmv|swf|wma|rar|gz|exe))$");

	private static final Pattern PATTERNS = Pattern.compile(".*\\.(pgn|zip)$");
	
	private Set<String> blackList;
	private String outputFolder; 
	
	private Logger log = Logger.getLogger(PGNCrawler.class.getSimpleName());
	

	public PGNCrawler(){
		super();
		LogHandler.initLogFileHandler(log, PGNCrawler.class.getSimpleName());
		this.blackList = Config.getBlackList();
		this.outputFolder = Config.getConverterFolder();
	}
	
	@Override
	public boolean shouldVisit(WebURL url){
		String href = url.getURL().toLowerCase();
		for(String s : blackList){
			if(href.matches(s))
				return false;
		}
		if(FILTERS.matcher(href).find())
			return false;
		if(PATTERNS.matcher(href).find()){
//			System.out.println(href);
			return true;
		}
		return true;
	}
	
	private String encode(String url){
		try {
			return URLEncoder.encode(url, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			return StringHandler.stringToAlphanumeric(url);
		}
	}
	
	@Override
	public void visit(Page page){
		Statistics.addLinksVisit(1);
		String url = page.getWebURL().getURL();
		log.info("Visting url: "+url);
		if (page.getParseData() instanceof HtmlParseData) {
			HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
			//FIDE is a fucking issue
			//TODO! 
			String html = htmlParseData.getHtml().trim();
//			System.out.println(html.charAt(1));
			if(html.charAt(1)=='[' || html.charAt(0)=='['){
				//probably PGN
				String hashedName = outputFolder+File.separator+encode(url)+".pgn";
				try {
					FileHandler.writeBytesToFile(hashedName, html.getBytes());
				} catch (IOException e) {
					LogHandler.writeStackTrace(log, e, Level.SEVERE);
				}
				Statistics.addPgnFounds(1);
			}
			List<WebURL> links = htmlParseData.getOutgoingUrls();
			int pgnsOnSite=0;
			for(WebURL wu : links){
				String wurl = wu.getURL();
//				log.info("link: "+wurl);
//				System.out.println(wurl);
				if(wurl.endsWith(".pgn")){
					pgnsOnSite++;
				}
			}
			if(pgnsOnSite>0)
				Statistics.addSitesWithPGNFiles(1);
			double pgnsPerLinks = pgnsOnSite/(1.0*Math.max(links.size(), 1));
			
			Statistics.addPGNsPerLinks(pgnsPerLinks);
			if(pgnsPerLinks>=Config.getPGNLinkThreshold())
				Config.addSeedLink(url);
			if(pgnsOnSite==0&&links.size()<=Config.getLinkThreshold())
				Config.addBlackList(url);
			
			log.info("Getting "+links.size()+" links");
			Statistics.addLinksFound(links.size());
		}
		if(page.getParseData() instanceof BinaryParseData || PATTERNS.matcher(url).find() ){
			if(!PATTERNS.matcher(url).find()){
//				log.info("url: "+url);
				return;
			}
			String extension = url.substring(url.lastIndexOf("."));
			if(extension.endsWith(".zip")){
//				log.info(url);
				String hashedName = outputFolder+File.separator+DigestUtils.md5Hex(page.getContentData()) + extension;
				try {
					FileHandler.writeBytesToFile(hashedName, page.getContentData());
				} catch (IOException e) {
					LogHandler.writeStackTrace(log, e, Level.SEVERE);
				}
				ByteArrayInputStream bais =new ByteArrayInputStream(page.getContentData());
				try {
					int pgns = ZipUtils.extractFilesWithSuffix(bais, outputFolder, ".pgn", encode(url+File.separator));
					Statistics.addPgnFounds(pgns);
				} catch (IOException e) {
					log.warning("Couldn't process Zip file due to:");
					LogHandler.writeStackTrace(log, e, Level.WARNING);
				}
				new File(hashedName).delete();
			}
			else{
				//Unique but no duplicates DigestUtils.md5Hex(page.getContentData())
				String hashedName = outputFolder+File.separator+encode(url);
				try {
					FileHandler.writeBytesToFile(hashedName, page.getContentData());
				} catch (IOException e) {
					LogHandler.writeStackTrace(log, e, Level.SEVERE);
				}
				Statistics.addPgnFounds(1);
			}
		}
	}
}
