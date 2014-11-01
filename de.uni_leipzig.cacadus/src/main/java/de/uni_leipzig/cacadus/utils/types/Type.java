package de.uni_leipzig.cacadus.utils.types;

import java.util.Map;

public class Type {

	public static Type setFromMap(Class<? extends Type> cl, Map<String, String> map){
		Type obj;
		try {
			obj = cl.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			return null;
		}
		obj.setMap(map);
		return obj;
	}

	private Map<String, String> map;
	
	public Map<String, String> getMap() {
		return map;
	}

	public void setMap(Map<String, String> map) {
		this.map = map;
	}
	
}
