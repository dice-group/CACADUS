package de.uni_leipzig.cacadus.upload;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bio_gene.wookie.utils.ConfigParser;
import org.bio_gene.wookie.utils.LogHandler;
import org.w3c.dom.Element;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import de.uni_leipzig.cacadus.utils.Config;
import de.uni_leipzig.cacadus.utils.FileHandler;
import de.uni_leipzig.cacadus.utils.Handler;
import de.uni_leipzig.informatik.swp13_sc.datamodel.rdf.ChessRDFVocabulary;
import de.uni_leipzig.simba.controller.PPJoinController;

public class LimesHandler extends Thread implements Handler {

	private Logger log = Logger.getLogger(LimesHandler.class.getSimpleName());
	
	public static final int GAME=0;
	public static final int EVENT=1;
	public static final int PLAYER=2;
	
	private String convertedFolder;
	private String uploadFolder;
	private String limesConfig;
	private String limesTmpConfig;
	private String limesOutput;
	
	private static String metricGame="AND(AND(trigrams(?y.prop:blackplayer,?x.prop:blackplayer)|0.8, "
			+ "trigrams(?y.prop:whiteplayer,?x.prop:whiteplayer)|0.8 )|1.0, "
			+ "AND(trigram(?y.prop:fen, ?x.prop:fen)|1.0, trigram(?y.prop:date, ?x.prop:date)|0.5 )|1.0)";
	private static String metricEvent="AND(trigrams(?y.prop:name,?x.prop:name)|0.8,trigrams(?y.prop:startdate,?x:prop:startdate)|1.0,trigrams(?y.prop:enddate,?x:prop:enddate)|1.0)";
	private static String metricPlayer="trigrams(?y.prop:fideID,?x.prop:fideID)";
	private String properties;
	private String endpoint;
	private String resy;
	private String resx;
//	private Connection con;
	private boolean endSignal;
	private int mode;
	
	private String outputGame;
	private String outputEvent;
	private String outputPlayer;

	private UploadHandler uploader;

//	private String limesOutputFull;
	
	public LimesHandler(){
		init(Config.getConvertedFolder(), Config.getUploadFolder(), LimesHandler.GAME);
	}
	
	public LimesHandler(String inputFolder, int mode){
		init(inputFolder, Config.getUploadFolder(), mode);
	}
	
	public LimesHandler(String inputFolder, String outputFolder, int mode){
		init(inputFolder, outputFolder, mode);
	}
	
	public void setUploadHandler(UploadHandler uh){
		uploader = uh;
	}
	
	private void init(String folder1, String folder2, int mode){
		LogHandler.initLogFileHandler(log, LimesHandler.class.getSimpleName());	
		convertedFolder = folder1;
		uploadFolder = folder2;
		limesConfig = Config.getLimesConfig();
		limesOutput = getOutputFile();
		endpoint = Config.getUnauthConnection().getEndpoint();
//		con = Config.getConnection();
		limesTmpConfig = limesConfig+limesConfig.hashCode();
		this.mode=mode;
		switch(mode){
		case(LimesHandler.GAME):
			properties="<PROPERTY>prop:blackPlayer</PROPERTY>\n"
					+ "<PROPERTY>prop:locationOfPGN</PROPERTY>\n"
					+ "<PROPERTY>prop:whitePlayer</PROPERTY>\n"
					+ "<PROPERTY>prop:fen</PROPERTY>\n"
					+ "<PROPERTY>prop:date</PROPERTY>";
			resy="?y rdf:type prop:ChessGame";
			resx="?x rdf:type prop:ChessGame";
			if(limesOutput.equals("$output")){
				limesOutput = outputGame;
			}
			break;
		case(LimesHandler.PLAYER):
			properties="<PROPERTY>prop:fideID</PROPERTY>\n"
					+ "<PROPERTY>prop:name</PROPERTY>";
			resy="?y rdf:type prop:Player"; 
			resx="?x rdf:type prop:Player";
			if(limesOutput.equals("$output")){
				limesOutput = outputPlayer;
			}
			break;
		case(LimesHandler.EVENT):
			properties="<PROPERTY>prop:name</PROPERTY>\n"
					+ "<PROPERTY>prop:startdate</PROPERTY>\n"
					+ "<PROPERTY>prop:enddate></PROPERTY>";
			resy="?y rdf:type prop:ChessEvent";
			resx="?x rdf:type prop:ChessEvent";
			if(limesOutput.equals("$output")){
				limesOutput = outputEvent;
			}
		}
	}
	
	private String getOutputFile(){
		try{
			ConfigParser cp = ConfigParser.getParser(limesConfig);
			cp.getElementAt("ACCEPTANCE", 0);
			Element e = cp.getElementAt("FILE", 0);
			return e.getFirstChild().getTextContent();
		}
		catch(Exception e){
			log.severe("Couldn't get output filename of LIMES config file due to: ");
			LogHandler.writeStackTrace(log, e, Level.SEVERE);
		}
		return null;
	}
	
	private void preProcessLimes(String configFile, String outputConfigFile, String source, String output){
		preProcessLimes(new File(configFile), new File(outputConfigFile), source, output);
	}
	
	private void preProcessLimes(File configFile, File outputConfigFile, String source, String output){
		FileInputStream fis = null;
		BufferedReader br = null;
		String line="";
		PrintWriter pw = null;
		try{
			outputConfigFile.createNewFile();
			pw = new PrintWriter(outputConfigFile);
			fis = new FileInputStream(configFile);
			br = new BufferedReader(new InputStreamReader(fis, Charset.forName("UTF-8")));
			while((line = br.readLine())!= null){
				line =line.replace("$file", source).replace("$endpoint", endpoint)
						.replace("$resy", resy).replace("$resx", resx)
						.replace("$properties", properties)
						.replace("$res", ChessRDFVocabulary.ResourceURI)
						.replace("$prop", ChessRDFVocabulary.Prefix)
						.replace("$outFormat", Config.getOutputFormat());
				outputGame = output;
				outputPlayer = output;
				outputEvent =  output;
				switch(mode){
				case LimesHandler.GAME: 
					
					line = line.replace("$metric", metricGame).replace("$output", outputGame);
					break;
				case LimesHandler.PLAYER:
					line = line.replace("$metric", metricPlayer).replace("$output", outputPlayer);
					break;
				case LimesHandler.EVENT:
					line = line.replace("$metric", metricEvent).replace("$output", outputEvent);
					break;
				}
					pw.println(line);
			}
		}
		catch(IOException e){
			LogHandler.writeStackTrace(log, e, Level.SEVERE);
		}
		finally{
			pw.close();
			try {
				fis.close();
				br.close();
			} catch (IOException e) {
				
				LogHandler.writeStackTrace(log, e, Level.SEVERE);
			}
		}
	}
	
	private void folderLookup(){
		if(!FileHandler.isFolderEmpty(convertedFolder)){
			for(File f : new File(convertedFolder).listFiles()){
				directLookup(f);
			}
		}
	}
	
	public void directLookup(File f){
		String output =f.getAbsolutePath().substring(0, f.getAbsolutePath().lastIndexOf(File.separator))+File.separator+"ACCEPT"+f.getName();
		executeLimes(f, output);
		deDuplication(f, output);
		f.delete();
		new File(output).delete();
	}
	
	private void executeLimes(File f, String output){
		preProcessLimes(limesConfig, limesTmpConfig, f.getAbsolutePath(), output);
		while(!UploadHandler.isEmpty(Config.getUploadFolder(), mode)){}
		PPJoinController.run(limesTmpConfig);
		new File(limesTmpConfig).delete();
	}
	
	private Collection<String> getAllDuplicates(String limes) throws IOException{
		Collection<String> set = new HashSet<String>();
		for(String line : FileHandler.getLinesOfFile(limes)){
			line = line.trim();
			line = line.split(" ")[0];
			set.add(line);
		}
		return set;
	} 
	
	private void deDuplication(File f, String o){
		File output = new File(uploadFolder+File.separator+mode+File.separator+f.getName()+".tmp");
		FileInputStream fis = null;
		FileOutputStream fos = null;
		try{
			//output soll alle 
			output.createNewFile();
			fos = new FileOutputStream(output);
			Model m = ModelFactory.createDefaultModel();
			fis = new FileInputStream(f);
			m.read(fis, null);
			Collection<String> set = getAllDuplicates(o);
			Collection<Triple> triples = new LinkedList<Triple>();
			for(String s : set){
				StmtIterator sit = m.listStatements();
				while(sit.hasNext()){
					Statement stmt = sit.next();
					if(stmt.asTriple().subjectMatches(ResourceFactory.createResource(s).asNode())){
						triples.add(stmt.asTriple());
						m.remove(stmt);
					}
				}
				
				Updater.saveFromCollection(triples);
//				m.removeAll(ResourceFactory.createResource(s), null, null);
			}
//			ResIterator rit = m.listSubjects();
//			set.removeAll(getAllDuplicates(limesOutputFull));
//			while(rit.hasNext()){
//				Resource resource =rit.next();
//				String res = resource.toString();
//				//Model from complete model, these can be simply uploaded
//				
////				getIncompleteTriples(res);
//			}
			uploader.uploadFile(f);
			f.delete();
//			Updater.uploadStorage();
//			m.write(fos);
//			output.renameTo(new File(uploadFolder+File.separator+mode+File.separator+f.getName()));
		}
		catch(IOException | SQLException e){
			log.severe("Couldn't write deduplicated file "+output.getAbsolutePath()+" due to: ");
			LogHandler.writeStackTrace(log, e, Level.SEVERE);
		}
		finally{
			if(fis!=null){
				try {
					fis.close();
				} catch (IOException e) {
					LogHandler.writeStackTrace(log, e, Level.SEVERE);
				}
			}
			if(fos!=null){
				try {
					fos.close();
				} catch (IOException e) {
					LogHandler.writeStackTrace(log, e, Level.SEVERE);
				}
			}
		}
		
	}
	
//	private void getIncompleteTriples(String resource){
//		while(!Updater.isEmpty()){}
//		Updater.saveFromResource(resource);
////		Updater.uploadStorage();
//	}


	public void sendEndSignal() {
		endSignal = true;
	}

	
	public void startLookup(){
		while(!endSignal){
			folderLookup();
		}
		folderLookup();
	}

	@Override
	public void run(){
		startLookup();
	}

	
}
