/*
 * org.openmicroscopy.shoola.env.data.ClientSearchTest 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.env.data;




//Java imports
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openmicroscopy.shoola.env.data.util.PojoMapper;

import junit.framework.TestCase;

//Third-party libraries

//Application-internal dependencies
import ome.api.Search;
import ome.model.IObject;
import ome.model.annotations.FileAnnotation;
import ome.model.annotations.TagAnnotation;
import ome.model.annotations.TextAnnotation;
import ome.model.containers.Dataset;
import ome.model.containers.Project;
import ome.model.core.Image;
import ome.model.meta.Experimenter;
import ome.system.Login;
import ome.system.Server;
import ome.system.ServiceFactory;
import ome.util.builders.PojoOptions;
import pojos.ProjectData;

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class ClientSearchTest 
	extends TestCase
{

	private final ServiceFactory entry = 
		new ServiceFactory(new Server("localhost", 1099), 
				new Login("root", "ome")); 
	
	/** Id of project one. */
	private Long p_one = 50L;
	
	/** Id of project two. */
	private Long p_two = 100L;
	
	/** Id of dataset one. */
	private Long d_one = 50L;
	
	
	/**
	 * Returns the {@link Search} service.
	 * 
	 * @return See above.
	 */
	private Search getSearchService()
	{
		return entry.createSearchService();
	}
	
	/** 
	 * Searches on the name/description of a given type.
	 * Context:
	 * one project: name is "project one" id= 50
	 * A second project: description is "one" id = 100
	 * Result: working
	 */
	public void testBasicFieldsSearch()
	{
		Search service = getSearchService();
		service.onlyType(Project.class);
		service.byFullText("one");
		assertEquals(service.hasNext(), true);
		
		List r = service.results();
		assertEquals(r.size() == 2, true);
		Iterator i = r.iterator();
		IObject object;
		List ids = new ArrayList();
		while (i.hasNext()) {
			object = (IObject) i.next();
			System.err.println(object);
			ids.add(object.getId());
		}
		assertEquals(ids.contains(p_one), true);
		assertEquals(ids.contains(p_two), true);
		service.close();
	}
	
	/** 
	 * Searches on the name/description of a given type.
	 * Context:
	 * one project: name is "project one" id= 50
	 * A second project: description is "one" id = 100
	 * Result: working
	 */
	public void testBasicFieldsSearchWildCard()
	{
		Search service = getSearchService();
		service.onlyType(Project.class);
		service.byFullText("on?");
		assertEquals(service.hasNext(), true);
		List r = service.results();
		assertEquals(r.size() == 2, true);
		Iterator i = r.iterator();
		IObject object;
		List ids = new ArrayList();
		while (i.hasNext()) {
			object = (IObject) i.next();
			ids.add(object.getId());
		}
		assertEquals(ids.contains(p_one), true);
		assertEquals(ids.contains(p_two), true);
		service.close();
	}
	
	/** 
	 * Searches on the name/description of a given type.
	 * Context:
	 * one project: name is "project one" id= 50
	 * A second project: name is "project two" id = 100
	 * Result: working
	 */
	public void testBasicFieldsSearchWildCard2()
	{
		Search service = getSearchService();
		service.onlyType(Project.class);
		service.byFullText("proj*");
		assertEquals(service.hasNext(), true);
		List r = service.results();
		assertEquals(r.size() == 2, true);
		Iterator i = r.iterator();
		IObject object;
		List ids = new ArrayList();
		while (i.hasNext()) {
			object = (IObject) i.next();
			ids.add(object.getId());
		}
		assertEquals(ids.contains(p_one), true);
		assertEquals(ids.contains(p_two), true);
		service.close();
	}
	
	/** 
	 * Searches on the name/description of a given type.
	 * Context:
	 * one project: name is "project one" id= 50
	 * A second project: description is "one" id = 100
	 * A dataset: name is "dataset one" id = 50;
	 * Result: working but..
	 * If I set if the merge batch flag to false, i expect to have the only one
	 * dataset in the results map.
	 */
	public void testBasicFieldsSearch2()
	{
		Search service = getSearchService();
		Class[] nodes = new Class[2];
		nodes[0] = Project.class;
		nodes[1] = Dataset.class;
		//If I set if to false, i expect to have the results 
		// with one dataset. but still have everything
		//service.setMergedBatches(false);
		for (int i = 0; i < nodes.length; i++) {
			service.onlyType(nodes[i]);
			service.byFullText("one");
		}
		assertEquals(service.hasNext(), true);
		List r = service.results();
		assertEquals(r.size() == 3, true);
		Iterator i = r.iterator();
		IObject object;
		List projects = new ArrayList();
		List datasets = new ArrayList();
		while (i.hasNext()) {
			object = (IObject) i.next();
			if (object instanceof Dataset) {
				datasets.add(object);
			} else if (object instanceof Project)
				projects.add(object);
		}
		assertEquals(datasets.size() == 1, true);
		assertEquals(projects.size() == 2, true);
		service.close();
	}
	
	/** 
	 * Searches on the name/description of a given type.
	 * Context:
	 * A dataset: name is "dataset one" id = 50;
	 * Search on images, not that I don't have images.
	 */
	public void testBasicFieldsSearch2bis()
	{
		Search service = getSearchService();
		Class[] nodes = new Class[2];
		nodes[0] = Dataset.class;
		nodes[1] = Image.class;
		//If I set if to false, i expect to have the results 
		// with one dataset. but still have everything
		//service.setMergedBatches(false);
		for (int i = 0; i < nodes.length; i++) {
			service.onlyType(nodes[i]);
			service.byFullText("one");
		}
		assertEquals(service.hasNext(), true);
		List r = service.results();
		assertEquals(r.size() == 1, true);
		Iterator i = r.iterator();
		IObject object;
		List images = new ArrayList();
		List datasets = new ArrayList();
		while (i.hasNext()) {
			object = (IObject) i.next();
			if (object instanceof Dataset) {
				datasets.add(object);
			} else if (object instanceof Image)
				images.add(object);
		}
		assertEquals(datasets.size() == 1, true);
		assertEquals(images.size() == 0, true);
		service.close();
	}
	
	/** 
	 * Similar to basicFieldsSearch2 but in that case, I 
	 * limit the number of results to 2.
	 * Results: working
	 * It might be useful to be able to ask for the project with highest id.
	 * e.g. if I limit the size to 1 or modified the latest.
	 */
	public void testBasicFieldsSearch3()
	{
		Search service = getSearchService();
		int num = 1;
		service.setBatchSize(num);
		Class[] nodes = new Class[2];
		nodes[0] = Project.class;
		nodes[1] = Dataset.class;
		for (int i = 0; i < nodes.length; i++) {
			service.onlyType(nodes[i]);
			service.byFullText("one");
		}
		assertEquals(service.hasNext(), true);
		List r = service.results();
		assertEquals(r.size() == num, true);
		service.close();
	}
	
	/**
	 * Retrieve project with value two and with text annotation.
	 */
	public void testAnnotationOnSelectedType()
	{
		Search service = getSearchService();
		
		service.onlyAnnotatedWith(TextAnnotation.class);
		service.onlyType(Project.class);
		service.byFullText("two");
		assertEquals(service.hasNext(), false);
		service.close();
	}
	
	/**
	 * Find objects of a given type with no annotation.
	 * Context:
	 * Two projects (id = 50, id = 100) with name containing "project" in name
	 * only one is annotated (id = 50)
	 * 
	 * Result: works.
	 *
	 */
	public void testNotAnnotatedType()
	{
		Search service = getSearchService();
		
		service.onlyAnnotatedWith(new Class[] {});
		service.onlyType(Project.class);
		service.byFullText("project");
		assertEquals(service.hasNext(), true);
		List r = service.results();
		Iterator i = r.iterator();
		IObject object;
		List ids = new ArrayList();
		while (i.hasNext()) {
			object = (IObject) i.next();
			ids.add(object.getId());
		}
		assertEquals(ids.contains(p_one), false);
		assertEquals(ids.contains(p_two), true);
		service.close();
	}
	
	/**
	 * Find objects of a given type with no annotation.
	 * Context:
	 * Two projects (id = 50, id = 100) with name containing "one" in name 
	 * for the first one, and one in the description for the second one.
	 * only one is annotated (id = 50)
	 * 
	 * Result: works. Returned the correct project.
	 *
	 */
	public void testNotAnnotatedType2()
	{
		Search service = getSearchService();
		
		service.onlyAnnotatedWith(new Class[] {});
		service.onlyType(Project.class);
		service.byFullText("one");
		assertEquals(service.hasNext(), true);
		List r = service.results();
		Iterator i = r.iterator();
		IObject object;
		List ids = new ArrayList();
		while (i.hasNext()) {
			object = (IObject) i.next();
			ids.add(object.getId());
		}
		assertEquals(ids.contains(p_one), false);
		assertEquals(ids.contains(p_two), true);
		service.close();
	}
	
	/**
	 * Retrieve the project annotated with Text annotation.
	 * Not yet implemented
	 *
	 */
	public void testByAnnotatedWith()
	{
		Search service = getSearchService();
		service.close();
	}
	
	/**
	 * Retrieve the projects containing the term "one" 
	 */
	public void testSomeMustNone()
	{
		Search service = getSearchService();
		service.onlyType(Project.class);
		String[] some = new String[1];
		some[0] = "one";
		service.bySomeMustNone(some, null, null);
		service.onlyType(Project.class);
		assertEquals(service.hasNext(), true);
		List r = service.results();
		Iterator i = r.iterator();
		IObject object;
		List ids = new ArrayList();
		while (i.hasNext()) {
			object = (IObject) i.next();
			ids.add(object.getId());
		}
		assertEquals(r.size() == 2, true);
		assertEquals(ids.contains(p_one), true);
		assertEquals(ids.contains(p_two), true);
	
	}
	
	/**
	 * Retrieve the projects that must contain "two"
	 */
	public void testSomeMustNone2()
	{
		Search service = getSearchService();
		service.onlyType(Project.class);
		String[] must = new String[1];
		must[0] = "two";
		service.bySomeMustNone(null, must, null);
		service.onlyType(Project.class);
		assertEquals(service.hasNext(), true);
		List r = service.results();
		Iterator i = r.iterator();
		IObject object;
		List ids = new ArrayList();
		while (i.hasNext()) {
			object = (IObject) i.next();
			ids.add(object.getId());
		}
		assertEquals(r.size() == 1, true);
		assertEquals(ids.contains(p_one), false);
		assertEquals(ids.contains(p_two), true);
	
	}
	
	/**
	 * Retrieve the projects that must contain "two" and some "project"
	 */
	public void testSomeMustNone3()
	{
		Search service = getSearchService();
		service.onlyType(Project.class);
		String[] must = new String[1];
		must[0] = "two";
		String[] some = new String[1];
		some[0] = "project";
		service.bySomeMustNone(some, must, null);
		service.onlyType(Project.class);
		assertEquals(service.hasNext(), true);
		List r = service.results();
		Iterator i = r.iterator();
		IObject object;
		List ids = new ArrayList();
		while (i.hasNext()) {
			object = (IObject) i.next();
			ids.add(object.getId());
		}
		assertEquals(r.size() == 1, true);
		assertEquals(ids.contains(p_one), false);
		assertEquals(ids.contains(p_two), true);
	}
	
	/**
	 * Retrieve the projects that must contain "two" and some "project"
	 */
	public void testSomeMustNone4()
	{
		Search service = getSearchService();
		service.onlyType(Project.class);
		String[] none = new String[1];
		none[0] = "two";
		String[] some = new String[1];
		some[0] = "project";
		service.bySomeMustNone(some, null, none);
		service.onlyType(Project.class);
		assertEquals(service.hasNext(), true);
		List r = service.results();
		Iterator i = r.iterator();
		IObject object;
		List ids = new ArrayList();
		while (i.hasNext()) {
			object = (IObject) i.next();
			ids.add(object.getId());
		}
		assertEquals(r.size() == 1, true);
		assertEquals(ids.contains(p_one), true);
		assertEquals(ids.contains(p_two), false);
	}
	/**
	 * Retrieve the projects that must contain "two" and some "project"
	 */
	public void testSomeMustNone5()
	{
		Search service = getSearchService();
		service.onlyType(Project.class);
		String[] none = new String[1];
		none[0] = "\"project one\"";
		String[] some = new String[1];
		some[0] = "project";
		service.bySomeMustNone(some, null, none);
		service.onlyType(Project.class);
		assertEquals(service.hasNext(), true);
		List r = service.results();
		Iterator i = r.iterator();
		IObject object;
		List ids = new ArrayList();
		while (i.hasNext()) {
			object = (IObject) i.next();
			ids.add(object.getId());
		}
		assertEquals(r.size() == 1, true);
		assertEquals(ids.contains(p_one), false);
		assertEquals(ids.contains(p_two), true);
	}
	
	/**
	 * Retrieve the projects that must contain "two" and some "project"
	 */
	public void testSomeMustNone6()
	{
		Search service = getSearchService();
		service.onlyType(Project.class);
		String[] none = new String[1];
		none[0] = "\"project one\"";
		String[] some = new String[1];
		some[0] = "\"project\"";
		service.bySomeMustNone(some, null, null);
		service.onlyType(Project.class);
		assertEquals(service.hasNext(), true);
		List r = service.results();
		Iterator i = r.iterator();
		IObject object;
		List ids = new ArrayList();
		while (i.hasNext()) {
			object = (IObject) i.next();
			ids.add(object.getId());
		}
		assertEquals(r.size() == 2, true);
		assertEquals(ids.contains(p_one), true);
		assertEquals(ids.contains(p_two), true);
	}
	
	public void testSearchAnnotation()
	{
		Search service = getSearchService();
		service.onlyType(TextAnnotation.class);
		service.byFullText("annotation");
		assertEquals(service.hasNext(), true);
	}

	public void testAnnotationRetrieval()
	{
		Search service = getSearchService();
		Class[] klass = new Class[2];
		klass[0] = TagAnnotation.class;
		klass[1] = FileAnnotation.class;
		//klass[]
		service.onlyAnnotatedWith(klass);
		service.onlyType(Project.class);
		
		assertEquals(service.hasNext(), false);
	}
	
	public void testUser()
	{
		Search service = getSearchService();
		service.onlyType(Experimenter.class);
		service.byFullText("root");
		assertEquals(service.hasNext(), true);
	}
	
	public void testPower()
	{
		Search service = getSearchService();
		service.onlyType(Project.class);
		String[] some = new String[2];
		some[1] = "two";
		some[0] = "\"project\"";
		
		service.bySomeMustNone(some, null, null);
		assertEquals(service.hasNext(), true);
		List r = service.results();
		Iterator i = r.iterator();
		IObject object;
		int index = 0;
		assertEquals(r.size() == 2, true);
		while (i.hasNext()) {
			object = (IObject) i.next();
			if (index == 0) {
				assertEquals(object.getId() == p_two, false);
			}
			
			index++;
		}
	}
}
