package de.uni_leipzig.cacadus.upload;

import java.io.FileOutputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bio_gene.wookie.connection.Connection;
import org.bio_gene.wookie.utils.LogHandler;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import de.uni_leipzig.cacadus.utils.Config;
import de.uni_leipzig.cacadus.utils.TriplesComparator;
import de.uni_leipzig.cacadus.utils.types.Event;
import de.uni_leipzig.cacadus.utils.types.Player;
import de.uni_leipzig.informatik.swp13_sc.datamodel.rdf.ChessRDFVocabulary;
import de.uni_leipzig.mosquito.data.TripleStoreHandler;

public class TripleComparator implements Comparator<Triple> {

	private Connection con;
	private int limit=2000;
	
	public TripleComparator(){
		super();
		con = Config.getConnection();
	}
	
	private int getAccurateDate(String object1, String object2){
		if(object2.matches("(\\?\\?(.|-|_|/|\\)\\?\\?(.|-|_|/|\\))?[0-9]{4}")){
			//only year
			if(object1.matches("(([0-9]{2}|\\?\\?)(.|-|_|/|\\))?[0-9]{2}(.|-|_|/|\\)[0-9]{4}"))
				return 1;
			else
				return -1;
		}
		else if(object2.matches("(\\?\\?(.|-|_|/|\\))?[0-9]{2}(.|-|_|/|\\)[0-9]{4}")){
			//month and year
			if(object1.matches("[0-9]{2}(.|-|_|/|\\)[0-9]{2}(.|-|_|/|\\)[0-9]{4}"))
				return 1;
			else
				return -1;
		}
		return -1;
	}
	
	public Collection<Triple> getAllTriples(String resource) throws SQLException{
		Collection<Triple> ret = new HashSet<Triple>();
		long offset = 0;
		String query="SELECT ?p ?o ";
		query+= Config.getGraphURI()!=null?" FROM <"+Config.getGraphURI()+"> ":"";
		query+="WHERE {<"+resource+"> ?p ?o}";
		Query q = QueryFactory.create(query);
		q.setLimit(limit);
		
		Node s = NodeFactory.createURI(resource);
		
		boolean hasResults=true;
		try {
			while(hasResults){
				int add=0;
				q.setOffset(offset);
//				System.out.println(q.toString().replace("\n", " "));
				ResultSet res = con.select(q.toString().replace("\n", " "));
				while(res.next()){
					String predicate = res.getString("p");
					String object = res.getString("o");
					Node p = NodeFactory.createURI(predicate);
					Node o = TripleStoreHandler.implToNode(object);
					Triple t = new Triple(s, p, o);
					ret.add(t);
					add++;
				}
				if(add<limit)
					hasResults=false;
				offset+=add;
				
			}
		} catch (SQLException e) {
			//Logging can be done here
			throw e;
		}
		return ret;
	}
	
	/**
	 * 
	 * @return 0 if they are equal or both should be used, 1 if t1 is more accurate than t2, -1 otherwise
	 */
	public int compare(Triple t1, Triple t2){
		if(t1.objectMatches(t2.getObject())){
			return 0;
		}
		String prop = t2.getPredicate().toString();
		
		//Player
		if(Player.getBDay().toString().equals(prop)){
			//t2 exist, we do not need t1
			String object1 = t1.getObject().toString();
			String object2 = t2.getObject().toString();
			return getAccurateDate(object1, object2);
			
		}
		else if(prop.equals(Player.getLiveBRtng().toString())){
			return 1;
		}
		else if(prop.equals(Player.getLiveSRtng().toString())){
			return 1;
		}
		else if(prop.equals(Player.getLiveRRtng().toString())){
			return 1;
		}
		else if(prop.equals(Player.getBRtng().toString())){
			return 1;
		}
		else if(prop.equals(Player.getFed().toString())){
			String object = t2.getObject().toString();
			if(t1.getObject().toString().length()>object.length()){
				return 1;
			}
			return -1;
		}
		else if(prop.equals(Player.getFide().toString())){
			return -1;
		}
		else if(prop.equals(Player.getRRtng().toString())){
			return 1;
		}
		else if(prop.equals(Player.getName().toString())){
			String object = t2.getObject().toString();
			if(t1.getObject().toString().length()>object.length()){
				return 1;
			}
			return -1;
		}
		else if(prop.equals(Player.getSex().toString())){
			return -1;
		}
		else if(prop.equals(Player.getSRtng().toString())){
			return 1;
		}
		else if(prop.equals(Player.getTit().toString())){
			return 1;
		}
		//Event
		else if(prop.equals(Event.getCategory().toString())){
			return 1;
		}
		else if(prop.equals(Event.getChiefArbiter().toString())){
			String object = t2.getObject().toString();
			if(t1.getObject().toString().length()>object.length()){
				return 1;
			}
			return -1;
		}
		else if(prop.equals(Event.getChiefOrganizer().toString())){
			String object = t2.getObject().toString();
			if(t1.getObject().toString().length()>object.length()){
				return 1;
			}
			return -1;
		}
		else if(prop.equals(Event.getCity().toString())){
			return 1;
		}
		else if(prop.equals(Event.getCountry().toString())){
			return 1;
		}
		else if(prop.equals(Event.getDateReceived().toString())){
			String object1 = t1.getObject().toString();
			String object2 = t2.getObject().toString();
			return getAccurateDate(object1, object2);
		}
		else if(prop.equals(Event.getDateRegistered().toString())){
			String object1 = t1.getObject().toString();
			String object2 = t2.getObject().toString();
			return getAccurateDate(object1, object2);
		}
		else if(prop.equals(Event.getDeputyArbiter().toString())){
			return 1;
		}
		else if(prop.equals(Event.getEndDate().toString())){
			String object1 = t1.getObject().toString();
			String object2 = t2.getObject().toString();
			return getAccurateDate(object1, object2);
		}
		else if(prop.equals(Event.getName().toString())){
			String object = t2.getObject().toString();
			if(t1.getObject().toString().length()>object.length()){
				return 1;
			}
			return -1;
		}
		else if(prop.equals(Event.getNumberOfPlayers().toString())){
			return 1;
		}
		else if(prop.equals(Event.getOrganizer().toString())){
			return 1;
		}
		else if(prop.equals(Event.getStartDate().toString())){
			String object1 = t1.getObject().toString();
			String object2 = t2.getObject().toString();
			return getAccurateDate(object1, object2);
		}
		else if(prop.equals(Event.getSystem().toString())){
			return 1;
		}
		else if(prop.equals(Event.getTimeControl().toString())){
			return 1;
		}
		else if(prop.equals(Event.getType().toString())){
			return 1;
		}
		else if(prop.equals(Event.getZone().toString())){
			return 1;
		}
		//Game
		else if(prop.equals(ChessRDFVocabulary.birthDate.toString())){
			String object1 = t1.getObject().toString();
			String object2 = t2.getObject().toString();
			return getAccurateDate(object1, object2);
		}
		else if(prop.equals(ChessRDFVocabulary.birthPlace.toString())){
			String object = t2.getObject().toString();
			if(t1.getObject().toString().length()>object.length()){
				return 1;
			}
			return -1;
		}
		else if(prop.equals(ChessRDFVocabulary.blackPlayer.toString())){
			String object = t2.getObject().toString();
			if(t1.getObject().toString().length()>object.length()){
				return 1;
			}
			return -1;
		}
		else if(prop.equals(ChessRDFVocabulary.comment.toString())){
			String object = t2.getObject().toString();
			if(t1.getObject().toString().length()>object.length()){
				return 1;
			}
			return -1;
		}
		else if(prop.equals(ChessRDFVocabulary.date.toString())){
			String object1 = t1.getObject().toString();
			String object2 = t2.getObject().toString();
			return getAccurateDate(object1, object2);
		}
		else if(prop.equals(ChessRDFVocabulary.elo.toString())){
			return 1;
		}
		else if(prop.equals(ChessRDFVocabulary.event.toString())){
			return 1;
		}
		else if(prop.equals(ChessRDFVocabulary.fen.toString())){
			return -1;
		}
		else if(prop.equals(ChessRDFVocabulary.whitePlayer.toString())){
			String object = t2.getObject().toString();
			if(t1.getObject().toString().length()>object.length()){
				return 1;
			}
			return -1;
		}
		else if(prop.equals(ChessRDFVocabulary.title.toString())){
			return 1;
		}
		else if(prop.equals(ChessRDFVocabulary.name.toString())){
			String object = t2.getObject().toString();
			if(t1.getObject().toString().length()>object.length()){
				return 1;
			}
			return -1;
		}
		else if(prop.equals(ChessRDFVocabulary.nation.toString())){
			String object = t2.getObject().toString();
			if(t1.getObject().toString().length()>object.length()){
				return 1;
			}
			return -1;
		}
		else if(prop.equals(ChessRDFVocabulary.result.toString())){
			return 1;
		}		
		else if(prop.equals(ChessRDFVocabulary.site.toString())){
			return 0;
		}
		else{
			//String compare
			String object = t2.getObject().toString();
			if(t1.getObject().toString().length()>object.length()){
				return 1;
			}
			return -1;
		}		
	}
	
	public Collection<Node> compareObject(Triple t1, Triple t2){
		Collection<Node> ret = new HashSet<Node>();
		if(t2==null){
			ret.add(t1.getObject());
			return ret;
		}
		if(t1.subjectMatches(t2.getSubject())){
			if(t1.predicateMatches(t2.getPredicate())){
				int cmp = compare(t1, t2);
				if(cmp==0){
					//if they are equal, then they will only be added once, otherwise 
					//both will be added.
					ret.add(t1.getObject());
					ret.add(t2.getObject());
					return ret;
				}
				else if(cmp>0){
					ret.add(t2.getObject());
					return ret;
				}
				else{
					ret.add(t1.getObject());
					return ret;
				}
			}
		}
		return null;
	}
	
	public void compareTriples(Collection<Triple> tc, String addedFile, String removedFile) throws SQLException{
		Comparator<Triple> tcmp = new TriplesComparator();
		List<Triple> t = new ArrayList<Triple>(tc);
		Collections.sort(t, tcmp);

		Model[] m = new Model[2];
		
		for(String subject : getSubjects(tc)){
			List<Triple> t1 = getListForSubject(t, subject);
			List<Triple> t2 = null;
			try{
				t2 = new ArrayList<Triple>(getAllTriples(subject)); 
			}catch(Exception e){
				LogHandler.writeStackTrace(Logger.getGlobal(), e, Level.WARNING);
				continue;
			}
			Collections.sort(t1, tcmp);
			Collections.sort(t2, tcmp);
			Model[] m2 = compareTriples(t1, t2);
			m[0].add(m2[0]);
			m[1].add(m2[1]);
		}
		FileOutputStream fos=null;
		try{
			fos= new FileOutputStream(addedFile);
			m[0].write(fos, "N-TRIPLE");
			fos.close();
			fos = new FileOutputStream(removedFile);
			m[1].write(fos, "N-TRIPLE");
		}catch(Exception e){
		}
		finally{
			try{
				fos.close();
			}catch(Exception e){}
		}
	}
	
	public Model[] compareTriplesToModels(Collection<Triple> tc) throws SQLException{
		Comparator<Triple> tcmp = new TriplesComparator();
		List<Triple> t = new ArrayList<Triple>(tc);
		Collections.sort(t, tcmp);

		Model[] m = new Model[2];
		
		for(String subject : getSubjects(tc)){
			List<Triple> t1 = getListForSubject(t, subject);
			List<Triple> t2 = new ArrayList<Triple>(getAllTriples(subject)); 
			Collections.sort(t1, tcmp);
			Collections.sort(t2, tcmp);
			Model[] m2 = compareTriples(t1, t2);
			m[0].add(m2[0]);
			m[1].add(m2[1]);
		}
		return m;
	}
	
	private List<Triple> getListForSubject(List<Triple> t, String subject) {
		List<Triple> ret = new ArrayList<Triple>();
		Iterator<Triple> it = t.iterator();
		boolean over = false;
		String sOld="";
		while(it.hasNext()){
			Triple triple = it.next();
			String s =triple.getSubject().toString();
			if(over&&!s.equals(sOld)){
				break;
			}
			if(s.equals(subject)){
				ret.add(triple);
				over =true;
			}
			sOld =s;
			
		}
		return ret;
	}

	private Collection<String> getSubjects(Collection<Triple> t1) {
		Collection<String> ret = new HashSet<String>();
		for(Triple t : t1)
			ret.add(t.getSubject().toString());
		return ret;
	}

	public Model[] compareTriples(Collection<Triple> t1, Collection<Triple> t2){
		Model add = ModelFactory.createDefaultModel();
		Model delete = ModelFactory.createDefaultModel();
	
		//match them with the properties. If properties don't match -> added
		//else compareObject(t1, t2) -> added and old one to removed
		Triple currentT1, currentT2 = null;
		Iterator<Triple> it1 = t1.iterator();
		Iterator<Triple> it2 = t2.iterator();
		boolean next = false;
		while(it1.hasNext()){
			currentT1 = it1.next();
			while(it2.hasNext()){
				if(!next)
					currentT2 = it2.next();
				next=false;
				if(currentT1.predicateMatches(currentT2.getPredicate())){
					Collection<Node> nodes = compareObject(currentT1, currentT2);
					boolean doDelete=true;
					for(Node n : nodes){
						if(n.equals(currentT1.asTriple().getObject())){
							add.add(add.asStatement(currentT1));
						}
						else{
							add.add(add.asStatement(currentT2));
							doDelete=false;
						}
					}
					if(doDelete){
						delete.add(delete.asStatement(currentT2));
					}
					doDelete=true;
				}
				else if(currentT1.getPredicate().toString()
						.compareTo(currentT2.getPredicate().toString())>0){
					next=true;
					add.add(add.asStatement(currentT1));
					break;
				}
				
			}
			if(!it2.hasNext()){
				while(it1.hasNext()){
					add.add(add.asStatement(it1.next()));
				}
				break;
			}
			
		}
		Model[] ret = new Model[2];
		ret[0] = add;
		ret[1] = delete;
		return ret;
		
	}
	
	
}
