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
import de.uni_leipzig.cacadus.utils.iterator.PlayerIterator;
import de.uni_leipzig.cacadus.utils.types.Player;

public class PlayerService extends Thread implements Service<Player> {

	PlayerIterator pvi;
	private int maxPlayers=40000;
	private Logger log = Logger.getLogger("Service");

	@Override
	public void startService() {
		LogHandler.initLogFileHandler(log, "service");
		try {
			pvi = new PlayerIterator();
		} catch (IOException e1) {
			LogHandler.writeStackTrace(log, e1, Level.SEVERE);
			return;
		}
		Model m = ModelFactory.createDefaultModel();
		LimesHandler lh = new LimesHandler(Config.getPlayerFolder(), LimesHandler.PLAYER);
		lh.start();
		int Players = 0;
		while(pvi.hasNext()){
			Player current = pvi.next();
			m.add(current.getModel());
			Players++;
			if(Players>=maxPlayers || !pvi.hasNext()){
				Players=0;
				try {
					String fileName = Config.getPlayerFolder()+File.separator+"Player_"+UUID.randomUUID().toString()+".rdf";
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
