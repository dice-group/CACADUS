package de.uni_leipzig.cacadus.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bio_gene.wookie.utils.LogHandler;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import de.uni_leipzig.cacadus.upload.LimesHandler;
import de.uni_leipzig.cacadus.utils.Config;
import de.uni_leipzig.cacadus.utils.iterator.EventIterator;
import de.uni_leipzig.cacadus.utils.types.Event;

public class EventService extends Thread implements Service<Event> {

	EventIterator evi;
	private int maxEvents=40000;
	
	private Logger log = Logger.getLogger("Service");

	
	@Override
	public void startService() {
		LogHandler.initLogFileHandler(log, "service");
		evi = new EventIterator();
		Model m = ModelFactory.createDefaultModel();
		LimesHandler lh = new LimesHandler(Config.getEventFolder(), LimesHandler.EVENT);
		lh.start();
		int events = 0;
		while(evi.hasNext()){
			Event current = evi.next();
			m.add(current.getModel());
			events++;
			if(events>=maxEvents){
				events=0;
				try {
					String fileName = Config.getEventFolder()+File.separator+"Event_"+UUID.randomUUID().toString();
					File f = new File(fileName);
					f.createNewFile();
					FileOutputStream fos = new FileOutputStream(f);
					m.write(fos);
					try {
						fos.close();
					} catch (IOException e) {
					}
					m = ModelFactory.createDefaultModel();
				} catch (IOException e) {
					LogHandler.writeStackTrace(log, e, Level.SEVERE);
				}
			}
		}
		lh.sendEndSignal();
	}
	
	@Override
	public void run(){
		startService();
	}

}
