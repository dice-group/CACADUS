package de.uni_leipzig.cacadus.utils.types;

import java.util.Map;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import de.uni_leipzig.cacadus.utils.Config;

public class Player extends Type{

	private Model m = ModelFactory.createDefaultModel();

	private boolean hasRatings;
	
	private static Property name;
	private static Property fide;
	private static Property fed;
	private static Property sex;
	private static Property Tit;
//	private static Property WTit;
//	private static Property OTit;
	private static Property SRtng;
	private static Property liveSRtng;
//	private static Property SGm;
//	private static Property SK;
	private static Property RRtng;
	private static Property liveRRtng;
//	private static Property RGm;
//	private static Property Rk;
	private static Property BRtng;
	private static Property liveBRtng;
//	private static Property BGm;
//	private static Property Bk;
	private static Property BDay;
//	private static Property flag;
	
	static {
		Model m = ModelFactory.createDefaultModel();
		String prop = Config.getPrefix();
		name =  m.createProperty(prop+"name");
		fide =  m.createProperty(prop+"fideID");
		fed =  m.createProperty(prop+"federation");
		sex =  m.createProperty(prop+"sex");
		Tit =  m.createProperty(prop+"fideTitle");
//		WTit =  m.createProperty(prop+"");
//		OTit =  m.createProperty(prop+"");
		SRtng =  m.createProperty(prop+"rating#standard");
		liveSRtng =  m.createProperty(prop+"liveRating#standard");
//		SGm =  m.createProperty(prop+"");
//		SK =  m.createProperty(prop+"");
		RRtng =  m.createProperty(prop+"rating#rapid");
		liveRRtng =  m.createProperty(prop+"liveRating#rapid");
//		RGm =  m.createProperty(prop+"");
//		Rk =  m.createProperty(prop+"");
		BRtng =  m.createProperty(prop+"rating#blitz");
		liveBRtng =  m.createProperty(prop+"liveRating#blitz");
//		BGm =  m.createProperty(prop+"");
//		Bk =  m.createProperty(prop+"");
		BDay =  m.createProperty(prop+"birthDay");
//		flag =  m.createProperty(prop+"");
	}
	
	public boolean hasRating(){
		return hasRatings;
	}
	
	public static Property getName() {
		return name;
	}

	public static Property getFide() {
		return fide;
	}

	public static Property getFed() {
		return fed;
	}

	public static Property getSex() {
		return sex;
	}

	public static Property getTit() {
		return Tit;
	}

	public static Property getSRtng() {
		return SRtng;
	}

	public static Property getRRtng() {
		return RRtng;
	}

	public static Property getBRtng() {
		return BRtng;
	}

	public static Property getBDay() {
		return BDay;
	}

	@Override
	public void setMap(Map<String, String> map) {
		hasRatings=false;
		m = ModelFactory.createDefaultModel();
		String playerName = map.get("Name").trim().replaceAll("[^a-zA-Z0-9]", "_");
		String fideID = map.get("ID Number").trim();
		Resource res = m.createResource(Config.getResourceURI()+playerName+"_"+fideID, m.createResource(Config.getResourceURI()+"Player"));
		res.addLiteral(name, m.createLiteral(map.get("Name")));
		res.addLiteral(fide, m.createTypedLiteral(Integer.valueOf(map.get("ID Number"))));
		res.addLiteral(fed, m.createLiteral(map.get("Fed")));
		String sex2 = map.get("Sex");
		if(sex2 ==null){}
		else if(sex2.equals("M"))
			sex2 = "Male";
		else
			sex2 = "Female";
		if(sex2 !=null)
			res.addLiteral(sex, m.createLiteral(sex2));
//		res.addLiteral(flag, m.createLiteral(map.get("Flag")));
		if(map.get("Tit") !=null && !map.get("Tit").isEmpty())
			res.addLiteral(Tit, m.createLiteral(map.get("Tit")));
//		res.addLiteral(WTit, m.createLiteral(map.get("WTit")));
//		res.addLiteral(OTit, m.createLiteral(map.get("OTit")));
		if(map.get("SRtng") !=null && !map.get("SRtng").isEmpty()){
			res.addLiteral(SRtng, m.createTypedLiteral(Double.valueOf(map.get("SRtng"))));
			hasRatings =true;
		}
//		res.addLiteral(SGm, m.createTypedLiteral(Integer.valueOf(map.get("SGm"))));
//		res.addLiteral(SK, m.createTypedLiteral(Integer.valueOf(map.get("SK"))));
		if(map.get("RRtng") !=null && !map.get("RRtng").isEmpty()){
			res.addLiteral(RRtng, m.createTypedLiteral(Double.valueOf(map.get("RRtng"))));
			hasRatings =true;
		}
//		res.addLiteral(RGm, m.createTypEDLITERAL(INTEGER.VALUEOF(MAP.GET("RGM"))));
//		RES.ADDLITERAL(RK, M.CREATETYPEDLiteral(Integer.valueOf(map.get("Rk"))));
		if(map.get("BRtng") !=null && !map.get("BRtng").isEmpty()){
			res.addLiteral(BRtng, m.createTypedLiteral(Double.valueOf(map.get("BRtng"))));
			hasRatings=true;
		}
//		res.addLiteral(BGm, m.createTypedLiteral(Integer.valueOf(map.get("BGm"))));
//		res.addLiteral(Bk, m.createTypedLiteral(Integer.valueOf(map.get("BK"))));
		if(map.get("B-day") !=null && !map.get("B-day").isEmpty()){
			String byear = map.get("B-day");
			if(byear!="0000")
				res.addLiteral(BDay, byear);
		}
	}
	
	public static Property getLiveSRtng() {
		return liveSRtng;
	}

	public static Property getLiveRRtng() {
		return liveRRtng;
	}

	public static Property getLiveBRtng() {
		return liveBRtng;
	}

	public void addModel(Model union){
		m.union(union);
	}
	
	public void setModel(Model model){
		m = model;
	}
	
	public Model getModel(){
		return m;
	}
	
	public String toString(){
		String ret="{";
		StmtIterator stit = m.listStatements();
		while(stit.hasNext()){
			Statement s = stit.next();
			Triple t = s.asTriple();
			ret +=t.getSubject()+" ";
			ret +=t.getPredicate()+" ";
			ret +=t.getObject()+".";
		}
		return ret+"}";
	}
	
}
