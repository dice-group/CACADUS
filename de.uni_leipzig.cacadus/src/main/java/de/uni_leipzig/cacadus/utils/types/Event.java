package de.uni_leipzig.cacadus.utils.types;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;

public class Event{

	private Model m = ModelFactory.createDefaultModel();

	private static Property name;
	private static Property city;
	private static Property country;
	private static Property numberOfPlayers;
	private static Property system;
	private static Property category;
	private static Property startDate;
	private static Property endDate;
	private static Property dateReceived;
	private static Property dateRegistered;
	private static Property type;
	private static Property timeControl;
	private static Property zone;
	private static Property chiefArbiter;
	private static Property deputyArbiter;
	private static Property chiefOrganizer;
	private static Property organizer;
	
	static{
		Model m = ModelFactory.createDefaultModel();
		
		name = m.createProperty("name");
		city = m.createProperty("city");
		country = m.createProperty("country");
		numberOfPlayers = m.createProperty("numberOfPlayers");
		system = m.createProperty("system");
		category = m.createProperty("category");
		startDate = m.createProperty("startDate");
		endDate = m.createProperty("endDate");
		dateReceived = m.createProperty("dateReceived");
		dateRegistered = m.createProperty("dateRegistered");
		type = m.createProperty("type");
		timeControl = m.createProperty("timeControl");
		zone = m.createProperty("zone");
		chiefArbiter = m.createProperty("chiefArbiter");
		deputyArbiter = m.createProperty("deputyArbiter");
		chiefOrganizer = m.createProperty("chiefOrganizer");
		organizer = m.createProperty("organizer");
	}
	
	public static Property getName() {
		return name;
	}

	public static Property getCity() {
		return city;
	}

	public static Property getCountry() {
		return country;
	}

	public static Property getNumberOfPlayers() {
		return numberOfPlayers;
	}

	public static Property getSystem() {
		return system;
	}

	public static Property getCategory() {
		return category;
	}

	public static Property getStartDate() {
		return startDate;
	}

	public static Property getEndDate() {
		return endDate;
	}

	public static Property getDateReceived() {
		return dateReceived;
	}

	public static Property getDateRegistered() {
		return dateRegistered;
	}

	public static Property getType() {
		return type;
	}

	public static Property getTimeControl() {
		return timeControl;
	}

	public static Property getZone() {
		return zone;
	}

	public static Property getChiefArbiter() {
		return chiefArbiter;
	}

	public static Property getDeputyArbiter() {
		return deputyArbiter;
	}

	public static Property getChiefOrganizer() {
		return chiefOrganizer;
	}

	public static Property getOrganizer() {
		return organizer;
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
}
