package de.uni_leipzig.cacadus.utils.iterator;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import de.uni_leipzig.cacadus.utils.CSVReader;
import de.uni_leipzig.cacadus.utils.Config;
import de.uni_leipzig.cacadus.utils.ZipUtils;
import de.uni_leipzig.cacadus.utils.types.Player;

public class PlayerIterator implements Iterator<Player>{

	CSVReader reader;
	
	private static String fideURL = "http://ratings.fide.com/download/players_list.zip";
//	private static String fidePicture = "http://ratings.fide.com/card.php?code=";
	
	
	public PlayerIterator() throws IOException{
		super();
		new File(Config.getPlayerFolder()).mkdirs();
		ZipUtils.extractFromURL(fideURL, Config.getPlayerFolder()+File.separator+"player_list.txt");
		reader = new CSVReader(Config.getPlayerFolder()+File.separator+"player_list.txt");
		
	}
	
	public void close(){
		reader.close();
	}
	
	@Override
	public boolean hasNext() {
		return reader.hasNext();
	}

	@Override
	public Player next() {
		return reader.next();
	}

	@Override
	public void remove() {
	}

}
