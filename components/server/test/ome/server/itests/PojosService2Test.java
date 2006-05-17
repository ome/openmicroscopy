/*
 * ome.server.itests.PojosServiceTest
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2005 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */
package ome.server.itests;

//Java imports
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.beans.factory.config.PropertyResourceConfigurer;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

import ome.api.Pojos;
import ome.model.Category;
import ome.model.CategoryGroup;
import ome.model.Classification;
import ome.model.Dataset;
import ome.model.Image;
import ome.model.Project;
import ome.security.Utils;
import ome.testing.OMEData;
import ome.util.ContextFilter;
import ome.util.Filterable;
import ome.util.builders.PojoOptions;

/** 
 * currently tests are crashing Eclipse left and right. This is separated
 * out to simplify finding that bug (and others). Can be merged back at some point.
 * @author  Josh Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 
 * <small>
 * (<b>Internal version:</b> $Rev$ $Date$)
 * </small>
 * @since 2.0
 */
public class PojosService2Test
        extends
            AbstractDependencyInjectionSpringContextTests {

    protected static Log log = LogFactory.getLog(PojosService2Test.class); // TODO modify to getLog() abstract
    
    protected Pojos psrv;
    
    protected OMEData data;
    
    /**
     * @see org.springframework.test.AbstractDependencyInjectionSpringContextTests#getConfigLocations()
     */
    protected String[] getConfigLocations() {

        return ConfigHelper.getConfigLocations(); 
    }

    @Override
    protected void onSetUp() throws Exception {
    	super.onSetUp();
    	Utils.setUserAuth();
        psrv = (Pojos) applicationContext.getBean("pojosService");
        data = (OMEData) applicationContext.getBean("data");
    }
    

    // ====================================
    // Groups
    // ====================================
    
    public void testGroupVersusExperimenterInMap() throws Exception{
        Set<Integer> ids;
        Set e_result,g_result; 
        Map firstOwnerMap = (Map) data.getFirst("Owner.MostImages");
        Integer experimenterWithMost = (Integer) firstOwnerMap.get("id");
        Integer groupOfExperimenterWithMost = (Integer) firstOwnerMap.get("group");
        
        PojoOptions epo = new PojoOptions().exp(experimenterWithMost);
        PojoOptions gpo = new PojoOptions().grp(groupOfExperimenterWithMost);
        
        ids = new HashSet(data.getMax("Project.ofOwner.MostImages",2));
        e_result = psrv.loadContainerHierarchy(Project.class, ids, epo.map());
        g_result = psrv.loadContainerHierarchy(Project.class, ids, gpo.map());
        assertSubGraph(e_result,g_result);
        
        ids = new HashSet(data.getMax("Dataset.ofOwner.MostImages",2));
        e_result = psrv.loadContainerHierarchy(Dataset.class, ids, epo.map());
        g_result = psrv.loadContainerHierarchy(Dataset.class, ids, gpo.map());
        assertSubGraph(e_result,g_result);
        
        ids = new HashSet(data.getMax("CategoryGroup.ofOwner.MostImages",2));
        e_result = psrv.loadContainerHierarchy(CategoryGroup.class, ids, epo.map());
        g_result = psrv.loadContainerHierarchy(CategoryGroup.class, ids, gpo.map());
        assertSubGraph(e_result,g_result);
        
        ids = new HashSet(data.getMax("Category.ofOwner.MostImages",2));
        e_result = psrv.loadContainerHierarchy(Category.class, ids, epo.map());
        g_result = psrv.loadContainerHierarchy(Category.class, ids, gpo.map());
        assertSubGraph(e_result,g_result);        
        
        // ---using Image ids--------------------------------------------------
        ids = new HashSet(data.getMax("Image.ofOwner.MostImages",2));
       
        e_result = psrv.findContainerHierarchies(Project.class, ids, epo.map());
        g_result = psrv.findContainerHierarchies(Project.class, ids, gpo.map());
        assertSubGraph(e_result,g_result);
        
        e_result = psrv.findContainerHierarchies(CategoryGroup.class, ids, epo.map());
        g_result = psrv.findContainerHierarchies(CategoryGroup.class, ids, gpo.map());
        assertSubGraph(e_result,g_result);        
        
        e_result = psrv.findCGCPaths(ids, Pojos.CLASSIFICATION_ME, epo.map());
        g_result = psrv.findCGCPaths(ids, Pojos.CLASSIFICATION_ME, gpo.map());
        assertSubGraph(e_result,g_result);        

        e_result = psrv.findCGCPaths(ids, Pojos.CLASSIFICATION_NME, epo.map());
        g_result = psrv.findCGCPaths(ids, Pojos.CLASSIFICATION_NME, gpo.map());
        assertSubGraph(e_result,g_result);        
        
        e_result = psrv.findCGCPaths(ids, Pojos.DECLASSIFICATION, epo.map());
        g_result = psrv.findCGCPaths(ids, Pojos.DECLASSIFICATION, gpo.map());
        assertSubGraph(e_result,g_result);        
        
        ids = new HashSet(data.getMax("Project.ofOwner.MostImages",2));
        e_result = psrv.getImages(Project.class, ids, epo.map());
        g_result = psrv.getImages(Project.class, ids, gpo.map());
        assertSubGraph(e_result,g_result);        
        
        ids = new HashSet(data.getMax("Dataset.ofOwner.MostImages",2));
        e_result = psrv.getImages(Dataset.class, ids, epo.map());
        g_result = psrv.getImages(Dataset.class, ids, gpo.map());
        assertSubGraph(e_result,g_result);        
     
        ids = new HashSet(data.getMax("CategoryGroup.ofOwner.MostImages",2));
        e_result = psrv.getImages(CategoryGroup.class, ids, epo.map());
        g_result = psrv.getImages(CategoryGroup.class, ids, gpo.map());
        assertSubGraph(e_result,g_result);      

        ids = new HashSet(data.getMax("Category.ofOwner.MostImages",2));
        e_result = psrv.getImages(Category.class, ids, epo.map());
        g_result = psrv.getImages(Category.class, ids, gpo.map());
        assertSubGraph(e_result,g_result);      

        e_result = psrv.getUserImages(epo.map());
        g_result = psrv.getUserImages(gpo.map());
        assertSubGraph(e_result,g_result);      
        
    }
    
    
    void assertSubGraph(Set subGraph, Set superGraph ) {
        
        assertTrue(subGraph.size() <= superGraph.size());
        assertTrue(new DoubleFilter().isSetASubGraph(subGraph,superGraph));
        
    }

    static class DoubleFilter extends ContextFilter {
        
        /* possibilities:
         *  multi-filter where top-level filter gets a SWITCH "a" "b" "c"
         */
        
        Map<Object,Set<Integer>> idTable = new IdentityHashMap<Object,Set<Integer>>();
        
        // FIXME: this is only getting called at the top-level.
        // needs coroutine-like method to make this work.
        boolean isSetASubGraph(Set subGraph, Set supGraph){
            
            filter("Subgraph",subGraph);
            filter("Supergraph",supGraph);
            Set subGraphIds = idTable.get(subGraph);
            Set supGraphIds = idTable.get(supGraph);
            Set notSubGraph = new HashSet(supGraphIds);
            notSubGraph.removeAll(subGraphIds); // TODO only if logging on.
            
            log.info("Sub:"+subGraphIds);
            log.info("Sup:"+supGraphIds);
            log.info("!Sb:"+notSubGraph);
            
            return supGraphIds.containsAll(subGraphIds);
        }

        @Override
        public Filterable filter(String fieldId, Filterable f)
        {
            Integer id = getId(f);
            Object o = this.currentContext();
            if (o instanceof Collection)
                idTable.get(o).add(id);
            
            return super.filter(fieldId,f);
        }
        
        @Override
        public Collection filter(String fieldId, Collection c)
        {
            if (idTable.get(c) == null)
                idTable.put(c,new HashSet());
            
            return super.filter(fieldId,c);
            
        }
        
        Integer getId(Filterable f){
            if (f instanceof Project)
                { return ((Project) f).getProjectId();}
            else if (f instanceof Dataset)
                { return ((Dataset) f).getDatasetId();}
            else if (f instanceof CategoryGroup)
                { return ((CategoryGroup) f).getAttributeId();}
            else if (f instanceof Category)
                { return ((Category) f).getAttributeId();}
            else if (f instanceof Image)
                { return ((Image) f).getImageId();}
            else 
                { return null; }
        }
        
    }
    
    
    // ====================================
    // Paths
    // ====================================
    
    static class Paths {
        
        final static int EXISTS = -3;
        final static int WILDCARD = -2;
        final static int NULL_IMAGE = -1;
        final static int CG = 0;
        final static int C = 1;
        final static int I = 2;
        
        List<Integer> 
            cg = new ArrayList<Integer>(),
            c = new ArrayList<Integer>(),
            i = new ArrayList<Integer>(),
            removed = new ArrayList<Integer>();
        
        public String toString(){
            StringBuilder sb = new StringBuilder();
            for (int t = 0; t < cg.size(); t++)
            {
                if (!removed.contains(t))
                    sb.append(cg.get(t)+"/"+c.get(t)+"/"+ ( i.get(t) == NULL_IMAGE ? "EMPTY" : i.get(t) ) +"\n");   
            }
            return sb.toString();
        }
        
        int size(){
            return cg.size()-removed.size();
        }
        
        void add(int newCg, int newC, int newI){
            this.cg.add(newCg);
            this.c.add(newC);
            this.i.add(newI);
        }
        
        boolean remove(int t){
            removed.add(t);
            return true;
        }
        
        int[] get(int n){
            int[] values = new int[3];
            values[CG] = cg.get(n);
            values[C] = c.get(n);
            values[I] = i.get(n);
            return values;
        }
        
        boolean remove(int removeCg, int removeC, int removeI){
            Set n = find(removeCg,removeC,removeI);
            return n.size() < 1 ? false : removed.addAll(n);
        }
        
        Set<Integer> uniqueGroups(){
            return new HashSet<Integer>(cg);
        }
        
        Set<Integer> uniqueCats(){
            return new HashSet<Integer>(c);
        }
        
        Set<Integer> uniqueImages(){
            return new HashSet<Integer>(i); // TODO remove negatives?
        }
        
        Set<Integer> find(int testCg, int testC, int testI){
            Set<Integer> result = new HashSet<Integer>();
            for (int n = 0; n < cg.size(); n++)
            {
                if  (
                        ( cg.get(n).equals(testCg) || testCg == WILDCARD || testCg == EXISTS)
                        && (c.get(n).equals(testC) || testC == WILDCARD || testC == EXISTS)
                        && (i.get(n).equals(testI) || testI == WILDCARD || ( testI == EXISTS && i.get(n) != NULL_IMAGE ))
                        && (!removed.contains(n))
                      )
                    result.add(n);
            }
            return result;
        }
        
    }
    
    public void testCGCPaths()
    {
        
        //
        // SETUP
        //
        
        List cgciPaths = data.get("CGCPaths.all");
        int[] singlePath = null;
        Paths paths = new Paths();
        Set<Integer> imgs = new HashSet<Integer>();
        for (Iterator it = cgciPaths.iterator(); it.hasNext();)
        {  
            Map m = (Map) it.next();
            int cg = (Integer)m.get("cg"), c = (Integer) m.get("c");
            int i = m.get("i") == null ? Paths.NULL_IMAGE : (Integer) m.get("i");
            paths.add(cg,c,i);
            imgs.add(i);

            if (singlePath == null && i != Paths.NULL_IMAGE)
                singlePath = paths.get(paths.size()-1);
            
        }
        log.info(paths);
        
        //
        // DECLASSIFICATION
        //
        
        Set<CategoryGroup> de = psrv.findCGCPaths(imgs,Pojos.DECLASSIFICATION,null);
        assertTrue(de.size() == paths.uniqueGroups().size());
        for (CategoryGroup cg : de)
        {
            for (Object o_c : cg.getCategories())
            {
                Category c = (Category) o_c;
                for (Object o_cla : c.getClassifications())
                {
                    Classification cla = (Classification) o_cla;
                    Image i = cla.getImage();
                    
                    Set found = paths.find(cg.getAttributeId(),c.getAttributeId(),i.getImageId()); 
                    assertTrue( found.size() == 1);
                    
                }
            }
        }
        
        
        int single_i = singlePath[Paths.I];
        Set<CategoryGroup> one_de = psrv.findCGCPaths(Collections.singleton(single_i),Pojos.DECLASSIFICATION,null);
        assertTrue(one_de.size() == paths.find(Paths.WILDCARD,Paths.WILDCARD,single_i).size());
        
        
        //
        // CLASSIFICATION
        //
        
        // Finding a good test
        int[] targetPath = null;
        Set<Integer> withNoImages = paths.find(Paths.WILDCARD,Paths.WILDCARD,Paths.NULL_IMAGE);
        
        for (Integer n : withNoImages)
        {
            // Must be at least two Categories in one CG.
            int[] values = paths.get(n);
            if (paths.find(values[Paths.CG],Paths.WILDCARD,Paths.WILDCARD).size() > 1) {
                Set<Integer> target = paths.find(values[Paths.CG],Paths.WILDCARD,Paths.EXISTS);
                if (target.size() > 0) {
                    targetPath = paths.get(target.iterator().next());
                    break;
                }
            }
        }
        
        if (targetPath == null) fail("No valid category group found for classification test.");        
        
        Set single = Collections.singleton(targetPath[Paths.I]);
        Set<CategoryGroup> me = psrv.findCGCPaths(single,Pojos.CLASSIFICATION_ME,null);
        Set<CategoryGroup> nme = psrv.findCGCPaths(single,Pojos.CLASSIFICATION_NME,null);

        for (CategoryGroup group : nme)
        {
            if (group.getAttributeId().equals(targetPath[Paths.CG])){
                for (Iterator it = group.getCategories().iterator(); it.hasNext();)
                {
                    Category c = (Category) it.next();
                    if (c.getAttributeId().equals(targetPath[Paths.C]))
                        fail("Own category should not be included.");
                }
            }
                
        }
        
        for (CategoryGroup group : me)
        {
            if (group.getAttributeId().equals(targetPath[Paths.CG]))
                fail("Should not be in mutually-exclusive set.");
        }
        
        
    }
    
    // ====================================
    // Counting
    // ====================================
    
    public void testCountingApiPassingCalls(){
        List projectDatasetCounts = data.get("Counting.ProjectDatasetCount");
        List datasetImageCounts = data.get("Counting.DatasetImageCount");
        List groupExpCounts = data.get("Counting.GroupExperimenterCount");
        
        Set ids;
        Map results;
        
        // Project.datasets
        ids = gatherIds(projectDatasetCounts);
        results = psrv.getCollectionCount("ome.model.Project","datasets",ids,null);
        checkIdCount(projectDatasetCounts, results);
        
        // Datasets.images
        ids = gatherIds(datasetImageCounts);
        results = psrv.getCollectionCount("ome.model.Dataset","images",ids,null);
        checkIdCount(datasetImageCounts, results);
    
        // Group.experimenters
        ids = gatherIds(groupExpCounts);
        results = psrv.getCollectionCount("ome.model.Group","experimenters",ids,null);
        checkIdCount(groupExpCounts, results);
    
    }

    public void testCountingApiExceptions(){
        
        Set ids = Collections.singleton(1);
        
        // Does not exist
        try {
            psrv.getCollectionCount("DoesNotExist","meNeither",ids,null);
            fail("An exception should have been thrown");
        } catch (IllegalArgumentException e) {
            // Good.
        }
        
        // Missing plural on dataset
        try { 
            psrv.getCollectionCount("ome.model.Project","dataset",ids,null);
            fail("An exception should have been thrown");
        } catch (IllegalArgumentException e) {
            // Good.
        }
        
        // Null ids
        try {
            psrv.getCollectionCount("ome.model.Project","datasets",null,null);
            fail("An exception should have been thrown");
        } catch (IllegalArgumentException e) {
            // Good.
        }
        
        // Poorly formed
        try {
            psrv.getCollectionCount("hackers.rock!!!","",ids,null);
            fail("An exception should have been thrown");
        } catch (IllegalArgumentException e) {
            // Good.
        }
        
        // Empty Class string
        try {
            psrv.getCollectionCount("","datasets",ids,null);
            fail("An exception should have been thrown");
        } catch (IllegalArgumentException e) {
            // Good.
        }
        
        // Empty Class string
        try {
            psrv.getCollectionCount(null,"datasets",ids,null);
            fail("An exception should have been thrown");
        } catch (IllegalArgumentException e) {
            // Good.
        }
        
        // Empty property string
        try {
            psrv.getCollectionCount("ome.model.Image","",ids,null);
            fail("An exception should have been thrown");
        } catch (IllegalArgumentException e) {
            // Good.
        }
       
        // Null property string
        try {
            psrv.getCollectionCount("ome.model.Image",null,ids,null);
            fail("An exception should have been thrown");
        } catch (IllegalArgumentException e) {
            // Good.
        }
       
    }
    
    private Set gatherIds(List projectDatasetCounts)
    {
        Set ids = new HashSet();
        for (Iterator it = projectDatasetCounts.iterator(); it.hasNext();)
        {
            Map m = (Map) it.next();
            ids.add(m.get("id"));
        }
        return ids;
    }

    private void checkIdCount(List projectDatasetCounts, Map results)
    {
        for (Iterator it = projectDatasetCounts.iterator(); it.hasNext();)
        {
            Map m = (Map) it.next();
            Integer id = (Integer) m.get("id");
            Long count = (Long) m.get("count");
            Integer test = (Integer) results.get(id);
            assertEquals(count.intValue(),test.intValue());
        
        }
    }
    

}
