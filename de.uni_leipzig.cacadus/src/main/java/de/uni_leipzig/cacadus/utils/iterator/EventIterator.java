package de.uni_leipzig.cacadus.utils.iterator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

import de.uni_leipzig.cacadus.utils.Config;
import de.uni_leipzig.cacadus.utils.types.Event;

public class EventIterator implements Iterator<Event> {

	private String fideHTML="http://ratings.fide.com/tournament_details.phtml?event=";
	private int startID=3226;
	private String mainDiv = "<table cellpadding=\"3\" cellspacing=\"1\" border=\"0\" width=\"700\" align=\"center\">";
	private String end = "No tournament details found";
	private int currentID = startID;
	private int count=0;
	
	private Model getEvent(int ID){
		Model m = ModelFactory.createDefaultModel();
		BufferedReader br = null;
		try {
			boolean main=false;
			URL url = new URL(fideHTML+ID);
			URLConnection urlConn = url.openConnection();
			br = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
			String line;
			String res="";
			while((line=br.readLine())!=null){
				if(line.contains(mainDiv)){
					main=true;
					continue;
				}
				if(main){
					if(line.contains("Crosstable")){
						break;
					}
					//tr->td(Res name)|td->(b)
					//replace all<\w+\s+was auch immer> mit leerzeichen, trim, leerzeichen verbinden -> split -> gerade sind res namen ungerade sind values 
					boolean close=false;
					int index=0;
					String tmp="";
					for(int i=0;i<line.length();i++){
						if(line.charAt(i)=='>'){
							index=i;	
							close=true;
						}
						else if(line.charAt(i)=='<'&&close){
							if(index+1==i){
								continue;
							}
							tmp += line.substring(index+1, i)+" ";
						}
					}
					tmp = tmp.trim().replaceAll("\\s+", " ");
					String[] split = tmp.split(" ");
					for(int i=0;i<split.length/2;i+=2){
						String key = split[i];
						String value=split[i+1].replace("&nbsp;", "");
						if(value.isEmpty())
							continue;
						if(key.equals("Tournament Name")){
							Triple[] t = getEventTriple(value);
							res = t[0].getSubject().toString();
							m.add(m.asStatement(t[0]));
							m.add(m.asStatement(t[1]));
						}
						else{
							m.add(getTriple(res, key, value));
						}
					}
				}
			}
			
		} catch (IOException e) {
		}finally{
			try{
				br.close();
			}catch(Exception e){}
		}
		return m;
	}
	
	private boolean testID(int ID){
		BufferedReader br = null;
		System.out.println(ID);
		try {
			URL url = new URL(fideHTML+ID);
			URLConnection urlConn = url.openConnection();
			br = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
			String line;
			while((line=br.readLine())!=null){
				if(line.contains(mainDiv) || line.contains(mainDiv.replace("\"", ""))){
					return true;
				}
				else if(line.contains(end) || line.contains(end.replace("\"", ""))){
					return false;
				}
				
			}
			
		} catch (IOException e) {
		}finally{
			try{
				br.close();
			}catch(Exception e){}
		}
		return false;
	}
	
	private Triple[] getEventTriple(String value){
		Triple[] ret = new Triple[2];
		Node s = NodeFactory.createURI(Config.getResourceURI()+value);
		Node p = NodeFactory.createURI(Config.getPrefix()+"name");
		Node o = NodeFactory.createLiteral(value);
		ret[1] = new Triple(s, p, o);
		s = NodeFactory.createURI(Config.getResourceURI()+value);
		p = NodeFactory.createURI(Config.W3CTYPE_URI);
		o = NodeFactory.createURI(Config.getResourceURI()+"ChessEvent");
		ret[0] = new Triple(s, p, o);
		return ret;
	}
	
	private Model getTriple(String res, String key, String value){
		Model m = ModelFactory.createDefaultModel();
		Resource r = m.createResource(res);
		String[] camel = key.split(" ");
		key = camel[0].toLowerCase();
		for(int i=1; i<camel.length;i++){
			key+=String.valueOf(camel[i].charAt(0)).toUpperCase()+camel[i].substring(1);
		}
		Property p = m.createProperty(Config.getPrefix()+key);
		if(key.equals("numberOfPlayers")){
			r.addLiteral(p, Long.valueOf(value));
		}
		else if(key.contains("Date")){
			Calendar cal =  Calendar.getInstance();
			try {
				cal.setTime(new SimpleDateFormat("yyyy-MM-dd").parse(value));
				r.addLiteral(p, m.createTypedLiteral(cal));
			} catch (ParseException e) {
				r.addLiteral(p, m.createLiteral(value));
			}
			
		}
		else{
			r.addLiteral(p, m.createLiteral(value));
		}
		return m;
	}
	
	@Override
	public boolean hasNext() {
		if(count<10){
			if(testID(currentID)){
				return true;	
			}
			currentID++;
			count++;
		}
		return false;
	}

	@Override
	public Event next() {
		Event e = new Event();
		e.addModel(getEvent(currentID++));
		return e;
	}

	@Override
	public void remove() {
	}
	
	public void reset(){
		currentID=startID;
	}

}
