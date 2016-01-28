/*
 *   Copyright (C) 2009-2011 University of Dundee & Open Microscopy Environment.
 *   All rights reserved.
 *
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.server.itests;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import ome.api.IMetadata;
import ome.model.IObject;
import ome.model.annotations.Annotation;
import ome.model.annotations.AnnotationAnnotationLink;
import ome.model.annotations.CommentAnnotation;
import ome.model.annotations.FileAnnotation;
import ome.model.annotations.TagAnnotation;
import ome.model.containers.Dataset;
import ome.model.containers.Project;
import ome.model.core.Image;
import ome.model.core.OriginalFile;
import ome.model.meta.Experimenter;
import ome.parameters.Parameters;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** 
 * Collection of test for the {@link IMetadata} service.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @since 3.0-Beta4
 */
public class MetadataServiceTest 
	extends AbstractManagedContextTest
{

	/** Reference to the service test. */
	protected IMetadata iMetadata;

    /** 
     * Sets the service.
     */
    @BeforeClass
    protected void setup()
    	throws Exception
    {
        iMetadata = factory.getMetadataService();
    }
    
    /**
     * Test to retrieve all the annotations linked to a given object.
     */
    @Test
    public void testLoadAnnotationNoTypesSet()
    {
    	//create a project
    	Project p = new Project();
    	p.setName("project 1");
    	//create a comment annotation and a tag annotation
    	CommentAnnotation c = new CommentAnnotation();
        c.setTextValue("comment");
        c.setNs("");
        p.linkAnnotation(c);
        TagAnnotation tag = new TagAnnotation();
        tag.setTextValue("tag");
        tag.setNs("");
        p.linkAnnotation(tag);
        
        p = iUpdate.saveAndReturnObject(p);
        assertEquals(2, p.sizeOfAnnotationLinks());
        
        long self = this.iAdmin.getEventContext().getCurrentUserId();
        Parameters options = new Parameters();
        Set<Long> ids = new HashSet<Long>(1);
        ids.add(p.getId());
        Set<Long> annotators = new HashSet<Long>(1);
        annotators.add(self);
        //user id
       
        Map result = iMetadata.loadAnnotations(Project.class, ids,
                new HashSet(), // Method is "NoTypesSet"!
                //new HashSet(Arrays.asList(CommentAnnotation.class.getName())),
        		annotators, options);
        assertEquals(1, result.size());
        
        Set s = (Set) result.get(p.getId());
        assertEquals(2, s.size()); // Just comments
        Iterator i = s.iterator();
        Annotation annotation;
        int index = 0;
        while (i.hasNext()) {
			annotation = (Annotation) i.next();
			if (annotation instanceof TagAnnotation) {
				index++;
			} else if (annotation instanceof CommentAnnotation) {
				index++;
			}
		}
        assertTrue(index == 2);
    }
    
    /**
     * Test to retrieve annotations of a given type.
     */
    @Test
    public void testLoadAnnotationTypesSet()
    {
        loginNewUser();
        
    	//create a project
    	Project p = new Project();
    	p.setName("project 1");
    	//create a comment annotation and a tag annotation
    	CommentAnnotation c = new CommentAnnotation();
        c.setTextValue("comment");
        c.setNs("");
        p.linkAnnotation(c);
        TagAnnotation tag = new TagAnnotation();
        tag.setTextValue("tag");
        tag.setNs("");
        p.linkAnnotation(tag);
        
        p = iUpdate.saveAndReturnObject(p);
        
        long self = this.iAdmin.getEventContext().getCurrentUserId();
        Parameters options = new Parameters();
        Set<Long> ids = new HashSet<Long>(1);
        ids.add(p.getId());
        Set<Long> annotators = new HashSet<Long>(1);
        annotators.add(self);
        //user id
       
        Set<String> types = new HashSet<String>(1);
        types.add(CommentAnnotation.class.getName());
        Map result = iMetadata.loadAnnotations(Project.class, ids, types, 
        		annotators, options);
        assertTrue(result.size() == 1);
        
        Set s = (Set) result.get(p.getId());
        assertEquals(1, s.size() );
        Iterator i = s.iterator();
        Annotation annotation;
        int index = 0;
        while (i.hasNext()) {
			annotation = (Annotation) i.next();
			if (annotation instanceof CommentAnnotation) {
				index++;
			}
		}
        assertTrue(index == 1);
    }
    
    /** 
     * Retrieve the annotations of a given type. The annotation can be linked
     * to <code>IObject</code> of different type or not linked.
     */
    @Test
    public void testLoadSpecifiedAnnotations()
    {
        loginNewUser();
        
        //create a project
        Project p = new Project();
        p.setName("project 1");
        //create a comment annotation and a tag annotation
        CommentAnnotation c1 = new CommentAnnotation();
        c1.setTextValue("comment 1");
        c1.setNs("");
        p.linkAnnotation(c1);
        CommentAnnotation c2 = new CommentAnnotation();
        c2.setTextValue("comment 2");
        c2.setNs("");
        TagAnnotation t1 = new TagAnnotation();
        t1.setTextValue("tag 1");
        t1.setNs("");
        p = iUpdate.saveAndReturnObject(p);
        c2 = iUpdate.saveAndReturnObject(c2);
        t1 = iUpdate.saveAndReturnObject(t1);
        
        Parameters options = new Parameters();
        Set result = iMetadata.loadSpecifiedAnnotations(
                CommentAnnotation.class, null, null, options);
        assertEquals(2, result.size());
    }
    
    /** 
     * Retrieves the annotations of a given type and with a geiven name space.
     */
    @Test
    public void testLoadSpecifiedAnnotationsNameSpace()
    {
    	//create a comment annotation and a tag annotation
    	TagAnnotation t1 = new TagAnnotation();
        t1.setTextValue("tag 1");
       
        TagAnnotation t2 = new TagAnnotation();
        t2.setTextValue("tag 2");
        t2.setNs(IMetadata.NS_INSIGHT_TAG_SET);
        
        t1 = iUpdate.saveAndReturnObject(t1);
        t2 = iUpdate.saveAndReturnObject(t2);
        
        /*
        Parameters po = new Parameters();
        Set result = iMetadata.loadSpecifiedAnnotations(
        		TagAnnotation.class, IMetadata.NS_INSIGHT_TAG_SET, po.map());
        assertTrue(result.size() == 1);
        Iterator i = result.iterator();
        TagAnnotation tag;
        while (i.hasNext()) {
			tag = (TagAnnotation) i.next();
			assertTrue(tag.getId() == t2.getId());
		}
		*/
    }
    
    /** Counts the elements tagged. */
    @Test
    public void testTagCount()
    {
    	Project p = new Project();
    	p.setName("project 1");
    	//create a comment annotation and a tag annotation
        TagAnnotation t1 = new TagAnnotation();
        t1.setTextValue("tag 1");
        p.linkAnnotation(t1);
        p = iUpdate.saveAndReturnObject(p);
        t1 = iUpdate.saveAndReturnObject(t1);
        Dataset d = new Dataset();
    	d.setName("dataset 1");
    	//create a comment annotation and a tag annotation
        TagAnnotation t2 = new TagAnnotation();
        t2.setTextValue("tag 2");
        d.linkAnnotation(t2);
        d = iUpdate.saveAndReturnObject(d);
        t2 = iUpdate.saveAndReturnObject(t2);
        
        Image i = new Image();
    	i.setName("image 1");
    	//create a comment annotation and a tag annotation
        TagAnnotation t3 = new TagAnnotation();
        t3.setTextValue("tag 3");
        i.linkAnnotation(t3);
        i = iUpdate.saveAndReturnObject(i);
        t3 = iUpdate.saveAndReturnObject(t3);
        
        
        Parameters po = new Parameters();
        Set<Long> ids = new HashSet<Long>(3);
        ids.add(t1.getId());
        ids.add(t2.getId());
        ids.add(t3.getId());
        Map m = iMetadata.getTaggedObjectsCount(ids, po);
        Iterator k = m.keySet().iterator();
        Long id;
        while (k.hasNext()) {
			id = (Long) k.next();
			assertTrue(((Long) m.get(id) == 1));
		}
    }
    
    /** Retrieves the Tag Set, Tag. Not the tagged not linked to a Tag Set. */
    @Test
    public void testLoadTagSetNoOrphan()
    {
        loginNewUser();
        
    	//create a comment annotation and a tag annotation
    	TagAnnotation t1 = new TagAnnotation();
        t1.setTextValue("tag 1");
       
        TagAnnotation t2 = new TagAnnotation();
        t2.setTextValue("tag 2");
        t2.setNs(IMetadata.NS_INSIGHT_TAG_SET);
        
        t1 = iUpdate.saveAndReturnObject(t1);
        t2 = iUpdate.saveAndReturnObject(t2);
        AnnotationAnnotationLink link = new AnnotationAnnotationLink();
        link.setParent(t2);
        link.setChild(t1);
        link = iUpdate.saveAndReturnObject(link);
        Parameters po = new Parameters();
        Set set = iMetadata.loadTagSets(po);
        assertEquals(1, set.size());
        Iterator i = set.iterator();
        IObject object;
        while (i.hasNext()) {
        	object = (IObject) i.next();
        	assertTrue(object.getId().longValue() == t2.getId().longValue());
		}
    }
    
    /** Retrieves the Tag Set, Tag. Not the tagged not linked to a Tag Set. */
    @Test
    public void testLoadTagSetOrphan()
    {
        loginNewUser();
        
    	//create a comment annotation and a tag annotation
    	TagAnnotation t1 = new TagAnnotation();
        t1.setTextValue("tag 1");
       
        TagAnnotation t2 = new TagAnnotation();
        t2.setTextValue("tag 2");
        t2.setNs(IMetadata.NS_INSIGHT_TAG_SET);
        
        TagAnnotation t3 = new TagAnnotation();
        t3.setTextValue("tag 3");
        
        t1 = iUpdate.saveAndReturnObject(t1);
        t2 = iUpdate.saveAndReturnObject(t2);
        AnnotationAnnotationLink link = new AnnotationAnnotationLink();
        link.setParent(t2);
        link.setChild(t1);
        link = iUpdate.saveAndReturnObject(link);
        
        t3 = iUpdate.saveAndReturnObject(t3);
        
        Parameters po = new Parameters();
        po.orphan();
        Set set = iMetadata.loadTagSets(po);
        assertEquals(2, set.size());
        Iterator i = set.iterator();
        TagAnnotation object;
        String ns;
        while (i.hasNext()) {
        	object = (TagAnnotation) i.next();
        	ns = object.getNs();
        	if (ns != null && IMetadata.NS_INSIGHT_TAG_SET.equals(ns)) {
        		assertTrue(object.getId().longValue() == t2.getId().longValue());
        	} else {
        		assertTrue(object.getId().longValue() == t3.getId().longValue());
        	}
		}
    }
    
    /** Retrieves the Values related to a tag. */
    @Test
    public void testloadTagContent()
    {
    	//create a comment annotation and a tag annotation
    	TagAnnotation t1 = new TagAnnotation();
        t1.setTextValue("tag 1");
        
        Project p = new Project();
    	p.setName("project 1");
    	Dataset d = new Dataset();
    	d.setName("dataset 1");
    	Image i = new Image();
      	i.setName("image 1");
      	p.linkAnnotation(t1);
      	d.linkAnnotation(t1);
      	i.linkAnnotation(t1);
      	p = iUpdate.saveAndReturnObject(p);
      	d = iUpdate.saveAndReturnObject(d);
      	i = iUpdate.saveAndReturnObject(i);
      	t1 = iUpdate.saveAndReturnObject(t1);
      	Parameters po = new Parameters();
      	Set<Long> ids = new HashSet<Long>(1);
      	ids.add(t1.getId());
      	Map m = iMetadata.loadTagContent(ids, po);
      	Set set = (Set) m.get(t1.getId());
      	assertTrue(set.size() == 3);
      	Iterator k = set.iterator();
      	IObject object;
      	Long id;
      	while (k.hasNext()) {
      		object = (IObject) k.next();
      		id = object.getId();
			if (object instanceof Project) {
				assertTrue(p.getId().longValue() == id.longValue());
			} else if (object instanceof Dataset) {
				assertTrue(d.getId().longValue() == id.longValue());
			} else if (object instanceof Image) {
				assertTrue(i.getId().longValue() == id.longValue());
			} 
		}
      	
    }
    
    /**
     * Test that the find annotations query still returns all types after
     * the criteria join on file annotation
     */
    @Test(groups = "ticket:1162")
    public void testFileAnnotationLoad() throws Exception
    {
        Project p = new Project();
        p.setName("project 1");
        FileUploader uploader = new FileUploader(this.factory, "test","test","test");
        uploader.run();
        FileAnnotation f1 = new FileAnnotation();
        f1.setFile(new OriginalFile(uploader.getId(), false));
        p.linkAnnotation(f1);
        TagAnnotation t1 = new TagAnnotation();
        p.linkAnnotation(t1);
        p = iUpdate.saveAndReturnObject(p);

        Map<Long, Set<Annotation>> results = iMetadata.loadAnnotations(Project.class,
                Collections.singleton(p.getId()), Collections.<String>emptySet(),
                Collections.<Long>emptySet(), null);
        Set<Annotation> anns = results.get(p.getId());
        assertEquals(2, anns.size());
    }
    
    @Test(groups = "ticket:2232")
    public void testExperimentPhotos() throws Exception {
        loginNewUser();
        iAdmin.uploadMyUserPhoto("foo", "image/png", new byte[]{0,1,2,3});
        iMetadata.loadAnnotations(Experimenter.class,
                Collections.singleton(iAdmin.getEventContext().getCurrentUserId()),
                new HashSet(), null, null);
    }
}
