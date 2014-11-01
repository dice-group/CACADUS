package de.uni_leipzig.cacadus.upload;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;

import org.bio_gene.wookie.connection.Connection;
import org.bio_gene.wookie.utils.GraphHandler;
import org.bio_gene.wookie.utils.LogHandler;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.ibm.icu.util.Calendar;

import de.uni_leipzig.cacadus.utils.Config;
import de.uni_leipzig.cacadus.utils.UpdateComparator;


public class Updater extends Thread{

	private static final String ADDED = "added";
	private static final String REMOVED = "removed";
	private static final String ADDED_REGEX ="[0-9]{6}."+ADDED+".nt";
	private static final String REMOVED_REGEX ="[0-9]{6}."+REMOVED+".nt";
	
	private static int addedNo=0;
	private static int removedNo=0;
	private static TripleComparator tc = new TripleComparator();
	private static boolean endSignal;
	
	private static Model add = ModelFactory.createDefaultModel();
	private static Model del = ModelFactory.createDefaultModel();
	
	private static Connection con;
	private static Logger log = Logger.getLogger(Updater.class.getName());
	private static long maxSize=-1;
	private static Set<String> currentFolder = new HashSet<String>();

	
	static {
		LogHandler.initLogFileHandler(log, Updater.class.getName());
	}
	
	public static void init(){
		con = Config.getConnection();
	}
	
	public static void reset(){
		addedNo=0;
		removedNo=0;
		add.removeAll();
		del.removeAll();
	}
	
	private static String[] getNo(){
		String[] ret = new String[2];
		ret[0] = ("000000"+addedNo).substring(String.valueOf(addedNo).length());
		ret[1] = ("000000"+removedNo).substring(String.valueOf(removedNo).length());
		return ret;
	}
	
	
	public static void saveFromModel(Model m) throws IOException, SQLException{
		Collection<Triple> triples = new HashSet<Triple>();
		StmtIterator sit = m.listStatements();
		while(sit.hasNext()){
			triples.add(sit.next().asTriple());
		}
		saveFromCollection(triples);
	}
	
	public static String getFolder(){
		String year = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));
		String month = String.valueOf(Calendar.getInstance().get(Calendar.MONTH));
		String day = String.valueOf(Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
		String hour = String.valueOf(Calendar.getInstance().get(Calendar.HOUR_OF_DAY));
		if(month.length()<2)
			month = "0"+month;
		if(day.length()<2)
			day = "0"+day;
		if(hour.length()<2)
			hour = "0"+hour;
		String folder = Config.getUpdateFolder()+File.separator+year+File.separator+month
				+File.separator+day+File.separator+hour+File.separator;
		new File(folder).mkdirs();
		currentFolder.add(folder);
		return folder;
	}
	
	public static void saveFromCollection(Collection<Triple> triples) throws SQLException{
		String[] numbers = getNo();
		String folder = getFolder();
		tc.compareTriples(triples, folder+numbers[0]+"."+ADDED+".nt", folder+numbers[1]+"."+REMOVED+".nt");
	}
	
	public static void saveFromResource(String res){
		Collection<Triple> triples=null;
		try {
			triples  = tc.getAllTriples(res);
		} catch (Exception e) {
			LogHandler.writeStackTrace(log, e, Level.SEVERE);
		}
		if(triples !=null){
			try {
				saveFromCollection(triples);
			} catch (SQLException e) {
				LogHandler.writeStackTrace(log, e, Level.SEVERE);
			}
		}
	}
	
	public static void storeFromResource(String res){
		try {
			storeFromCollection(tc.getAllTriples(res));
		} catch (SQLException e) {
			LogHandler.writeStackTrace(log, e, Level.SEVERE);
		}
	}
	
	public static void storeFromModel(Model m){
		Collection<Triple> triples = new HashSet<Triple>();
		StmtIterator sit = m.listStatements();
		while(sit.hasNext()){
			triples.add(sit.next().asTriple());
		}
		storeFromCollection(triples);
	}
	
	public static void storeFromCollection(Collection<Triple> triples){
		String[] numbers = getNo();
		String folder = getFolder();
		FileOutputStream fos = null;
		try {
			Model[] m = tc.compareTriplesToModels(triples);
			add.add(m[0]);
			del.add(m[1]);
			if(add.size()>=maxSize||del.size()>=maxSize){
				fos= new FileOutputStream(folder+numbers[0]+"."+ADDED+".nt");
				add.write(fos, "N-TRIPLE");
				add.removeAll();
				fos.close();
				fos = new FileOutputStream(folder+numbers[1]+"."+REMOVED+".nt");
				del.write(fos, "N-TRIPLE");
				del.removeAll();
			}
		} catch (SQLException | IOException e) {
			LogHandler.writeStackTrace(log, e, Level.SEVERE);
		}
		finally{
			try{
				fos.close();
			}
			catch(Exception e){}
		}
	}
	
	public static void uploadStorage(){
		for(String folder : currentFolder){
			File dir = new File(folder);
			List<File> files = new ArrayList<File>();
			for(File f : dir.listFiles()){
				files.add(f);
			}
			Comparator<File> cmp = new UpdateComparator(UpdateComparator.DELETE_INSERT);
			Collections.sort(files, cmp);
			for(int i=0;i<files.size();i++){
				String name = files.get(i).getName();
				if(name.matches(ADDED_REGEX)){
					// upload or update?
					con.uploadFile(files.get(i), Config.getGraphURI());
				}
				else if(name.matches(REMOVED_REGEX)){
					con.update(ntToQuery(files.get(i), false, Config.getGraphURI()));
				}
				if(Config.getBackUp()){
					int chunk = 4092;
					byte[] buffer = new byte[chunk];
					String newDir = files.get(i).getAbsolutePath();
					String[] dirs = newDir.split(File.separator);
					newDir = Config.getBackUpFolder()
								+File.separator+dirs[dirs.length-5]
								+File.separator+dirs[dirs.length-4]
								+File.separator+dirs[dirs.length-3]
								+File.separator+dirs[dirs.length-2]+File.separator;
					File f = new File(newDir+files.get(i).getName()+".gz");
					GZIPOutputStream zipStream = null;
					try{
						f.createNewFile();
						zipStream = new GZIPOutputStream(new FileOutputStream(f));
						FileInputStream in = new FileInputStream(files.get(i));
						int length;
						while ((length = in.read(buffer, 0, chunk)) != -1){
							zipStream.write(buffer, 0, length);
						}
						in.close();
					}catch(IOException e){
						LogHandler.writeStackTrace(log, e, Level.SEVERE);
					}
					finally{
						try{
							zipStream.close();
						}
						catch(Exception e){}
					}
				}
				files.get(i).delete();
			}
		}
	}
	
	public static void startTimer(int minutes){
		int ms = 60000;
		long lastUpdate=Calendar.getInstance().getTimeInMillis();
		while(!endSignal){
			try {
				Updater.sleep((minutes*ms)+1);
			} catch (InterruptedException e) {
				LogHandler.writeStackTrace(log, e, Level.SEVERE);
			}
			long currentTime = Calendar.getInstance().getTimeInMillis();
			if(currentTime-lastUpdate>=minutes*ms){
				uploadStorage();
				lastUpdate=currentTime;
				reset();
			}
		}
	}
	
	@Override
	public void run(){
		Updater.startTimer(Config.getUpdateTimer());
	}
	
	/**
	 * NTRIPLE File to an insert or delete query.
	 *
	 * @param file the filename
	 * @param insert if query should be insert (true) or delete (false)
	 * @param graphUri the graph to use (can be null)
	 * @return the query
	 */
	public static String ntToQuery(String file, Boolean insert, String graphUri){
		return ntToQuery(new File(file), insert, graphUri);
	}
	
	/**
	 * NTRIPLE File to an insert or delete query.
	 *
	 * @param file the file
	 * @param insert if query should be insert (true) or delete (false)
	 * @param graphUri the graph to use (can be null)
	 * @return the query
	 */
	public static String ntToQuery(File file, Boolean insert, String graphUri){
//		try{
			String query = "";
			query= "INSERT DATA {";
			if(!insert){
				query="DELETE DATA {";
			}
			if(graphUri!=null){
				query+=" GRAPH <"+graphUri+"> { ";
			}
			Model m = ModelFactory.createDefaultModel();
			m.read(file.toURI().toString());
			String lines = GraphHandler.GraphToSPARQLString(m.getGraph());
			lines = lines.substring(1, lines.length()-1);
			query+=lines;
			if(graphUri!=null){
				query+=" }";
			}
			query+=" }";
//			br.close();
			return query;
	}

	public static boolean isEmpty() {
		for(String folder : currentFolder){
			if(new File(folder).listFiles().length>0)
				return false;
		}
		return true;
	}
}
