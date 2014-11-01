package de.uni_leipzig.cacadus.converter;

import java.io.File;

import de.uni_leipzig.cacadus.upload.LimesHandler;
import de.uni_leipzig.cacadus.utils.Config;
import de.uni_leipzig.cacadus.utils.FileHandler;
import de.uni_leipzig.cacadus.utils.Handler;
import de.uni_leipzig.informatik.swp13_sc.converter.PGNToRDFConverterRanged;
import de.uni_leipzig.informatik.swp13_sc.datamodel.rdf.ChessRDFVocabulary;

public class ConverterController extends Thread implements Handler{
	
	public static void main(String[] argc){
		ChessRDFVocabulary rdfv = new ChessRDFVocabulary();
		rdfv.init("http://example.com", "#", 
				"http://example.com/prop/", "http://example.com/res/", 
				"prop", "res",
				true);
		
		PGNToRDFConverterRanged pg = new PGNToRDFConverterRanged();
		File f = new File("src/test/resources/http-__www.pgnmentor.com_compilation.pgn");
		
		pg.setOutputFormat("TURTLE");
		pg.processToStream(f.getAbsolutePath(), new File("src/test/resources/complete.ttl").getAbsolutePath(), true);
		pg.setOutputFormat("RDF/XML");
		pg.processToStream(f.getAbsolutePath(), new File("src/test/resources/complete.rdf").getAbsolutePath(), true);
		
		rdfv.init("http://example.com", "#", 
				"http://example.com/prop/", "http://example.com/res/", 
				"prop", "res",
				false);
		
		pg.setOutputFormat("TURTLE");
		pg.processToStream(f.getAbsolutePath(), new File("src/test/resources/only_meta.ttl").getAbsolutePath(), true);
		pg.setOutputFormat("RDF/XML");
		pg.processToStream(f.getAbsolutePath(), new File("src/test/resources/only_meta.rdf").getAbsolutePath(), true);

	}
	
	private boolean endSignal=false;
	
	private String convertFolder;
	private String convertedFolder;
	private String outputFormat;
	private LimesHandler lh;
	
	public ConverterController(){
		convertFolder = Config.getConverterFolder();
		convertedFolder = Config.getConvertedFolder();
		outputFormat = Config.getOutputFormat();
		lh = new LimesHandler();
		if(Config.isDataDescription()){
			ChessRDFVocabulary rdfv = new ChessRDFVocabulary();
			rdfv.init(Config.getNamespace(), Config.getAnchor(), 
					Config.getPrefix(), Config.getResourceURI(), 
					Config.getPropertyPrefixName(), Config.getResourcePrefixName(),
					Config.getMetaData());
		}
	}
	
	private void folderLookup(){
		if(!FileHandler.isFolderEmpty(convertFolder)){
			for(File f : new File(convertFolder).listFiles()){
				if(!f.getName().toLowerCase().endsWith("pgn"))
					continue;
				String newFile = convertedFolder+File.separator+f.getName();
				
				PGNToRDFConverterRanged pg = new PGNToRDFConverterRanged();
				pg.setOutputFormat(outputFormat);
				
				pg.processToStream(f.getAbsolutePath(), new File(newFile).getAbsolutePath(), true);
				f.delete();
				//TODO better
				newFile = new File(newFile).getAbsolutePath();
				newFile = newFile.substring(0, newFile.lastIndexOf("."));
				for(File f2 : new File(convertedFolder).listFiles()){
					if(f2.getAbsolutePath().startsWith(newFile)){
						newFile = f2.getAbsolutePath();
					}
				}
				lh.directLookup(new File(newFile));
			}
		}
	}
	
	public void startLookup() {
		while(!endSignal){
			folderLookup();
		}
		folderLookup();
	}

	public void sendEndSignal() {
		this.endSignal = true;
	}
	
	public void run(){
		startLookup();
	}
}
