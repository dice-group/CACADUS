package de.uni_leipzig.cacadus.utils;

import java.util.Comparator;

import com.hp.hpl.jena.graph.Triple;

public class TriplesComparator implements Comparator<Triple>{

	@Override
	public int compare(Triple t0, Triple t1) {
		int cmp = t0.getSubject().toString().compareTo(t1.getSubject().toString());
		if(cmp!=0)
			return cmp;
		cmp = t0.getPredicate().toString().compareTo(t1.getPredicate().toString());
		if(cmp!=0)
			return cmp;
		return t0.getObject().toString().compareTo(t1.getObject().toString());
	}

	
	
	
}
