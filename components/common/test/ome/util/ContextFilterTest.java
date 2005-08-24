package ome.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.openmicroscopy.omero.model.Dataset;
import org.openmicroscopy.omero.model.Image;
import org.openmicroscopy.omero.model.Project;

import junit.framework.TestCase;

public class ContextFilterTest extends TestCase {

	/*
	 * Test method for 'ome.util.ContextFilter.filter(String, Filterable)'
	 */
	public void testFilterStringFilterable() {
		Filter filter = new ContextFilter();
		
		Project p = new Project();
		Dataset d = new Dataset();
		Image i = new Image();
		
		Set p_d = new HashSet();
		Set d_i = new HashSet();
		Set i_d = new HashSet();
		Set d_p = new HashSet();
		
		p_d.add(d);
		d_i.add(i);
		i_d.add(d);
		d_p.add(p);
		p.setDatasets(p_d);
		d.setProjects(d_p);
		d.setImages(d_i);
		i.setDatasets(i_d);
		
		Map m = new HashMap();
		
		m.put(d,i);
		m.put(i,p);
		
		System.err.println("Starting with "+m);
		m= filter.filter("Top-Level Map",m);
	}

	/*
	 * Test method for 'ome.util.ContextFilter.filter(String, Collection)'
	 */
	public void testFilterStringCollection() {

	}

	/*
	 * Test method for 'ome.util.ContextFilter.filter(String, Map)'
	 */
	public void testFilterStringMap() {

	}

	/*
	 * Test method for 'ome.util.ContextFilter.filter(String, Object)'
	 */
	public void testFilterStringObject() {

	}

}
