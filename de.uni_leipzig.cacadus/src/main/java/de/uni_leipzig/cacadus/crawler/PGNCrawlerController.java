package de.uni_leipzig.cacadus.crawler;

import java.io.IOException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bio_gene.wookie.utils.LogHandler;

import de.uni_leipzig.cacadus.utils.Config;
import de.uni_leipzig.cacadus.utils.FileHandler;
import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;

public class PGNCrawlerController extends Thread {

	private static Logger log = Logger.getLogger(PGNCrawlerController.class.getSimpleName());
	
	public static void main(String[] argc){
		PGNCrawlerController pgn = new PGNCrawlerController();
        pgn.start();
	}
	
	static {
		LogHandler.initLogFileHandler(log, PGNCrawlerController.class.getSimpleName());
	}
	
	public void run(){
		startCrawler();
	}
	
	public void startCrawler(){
		
		
		CrawlConfig config = new CrawlConfig();
		config.setMaxDownloadSize(Config.getMaxDownloadSize());
		config.setCrawlStorageFolder(Config.getCrawlStorageFolder());
		config.setPolitenessDelay(Config.getPolitenessDelay());
		config.setMaxDepthOfCrawling(Config.getMaxDepthOfCrawling());
		config.setIncludeBinaryContentInCrawling(true);	
		config.setIncludeHttpsPages(true);
		config.setFollowRedirects(true);
		PageFetcher pageFetcher = new PageFetcher(config);
        RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
        RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
        CrawlController controller = null;
        try {
			controller = new CrawlController(config, pageFetcher, robotstxtServer);
		} catch (Exception e) {
			log.severe("Couldn't instantiate Crawler Controller due to following exception: ");
			LogHandler.writeStackTrace(log, e, Level.SEVERE);
			log.info("Will abort now...");
			return;
		}
		try {
			Collection<String> set = FileHandler.getLinesOfFile(Config.getSeedFile());
			for(String page : set)
				controller.addSeed(page);
		} catch (IOException e) {
			log.warning("Couldn't get seedFile due to following exception: ");
			log.warning(e.getMessage());
			log.info("Will use PGNMentor and Fide as Seed");
//			controller.addSeed(Config.PGNMENTOR_URL);
//			controller.addSeed(Config.FIDE_URL);
			controller.addSeed(Config.test);
		}
		log.info("Configurations haven been set.");
		log.info("Starting the crawler");
		controller.start(PGNCrawler.class, Config.getNumberOfCrawlers());
		log.info("stop crawling");
	}
}
