package ome.server.itests.query;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.testng.annotations.Configuration;
import org.testng.annotations.Test;

import ome.conditions.ApiUsageException;
import ome.model.IObject;
import ome.model.containers.Category;
import ome.model.containers.CategoryGroup;
import ome.model.containers.Dataset;
import ome.model.containers.Project;
import ome.model.core.Image;
import ome.model.meta.Experimenter;
import ome.parameters.Filter;
import ome.parameters.Parameters;
import ome.server.itests.AbstractManagedContextTest;
import ome.services.query.PojosGetImagesQueryDefinition;
import ome.util.builders.PojoOptions;

@Test( groups = "integration" )
public class GetImagesQueryTest extends AbstractManagedContextTest
{
    PojosGetImagesQueryDefinition q;
    List list;

    protected void creation_fails(Parameters parameters){
        try {
            q= new PojosGetImagesQueryDefinition( 
                    parameters);
            fail("Should have failed!");
        } catch (IllegalArgumentException e) {
        } catch (ApiUsageException aue ) {
        }
        
    }
    
    @Test
    public void test_illegal_arguments() throws Exception
    {

        creation_fails( null );
        
        creation_fails(
                new Parameters()
                    .addIds(null) //Null
                    .addOptions(null)
                    .addClass(null)
                );
        
        creation_fails(
                new Parameters()
                .addIds(Arrays.asList( 1 )) // Not long
                .addOptions(null)
                .addClass(Project.class)
            );

        /* TODO currently handled by IPojos
        creation_fails(
                PojosQP.ids( new ArrayList() ), // Empty
                PojosQP.options( null ),
                PojosQP.Class(QP.CLASS, Project.class )
                );
        
        PojoOptions po = new PojoOptions().allExps();
        creation_fails(
                PojosQP.ids( null ), 
                PojosQP.options( po.map() ), // Has to have experimenter
                PojosQP.Class(QP.CLASS, Project.class )
                );
        */
        
    }

    @org.testng.annotations.Test
    public void test_simple_usage() throws Exception
    {
        Long doesntExist = -1L;
        q= new PojosGetImagesQueryDefinition(
                new Parameters()
                .addIds(Arrays.asList( doesntExist )) 
                .addOptions(null)
                .addClass(Project.class)
            );
           
        list = (List) iQuery.execute(q);

        PojoOptions po = new PojoOptions().exp( doesntExist );
        q= new PojosGetImagesQueryDefinition(
                new Parameters()
                .addIds(Arrays.asList( doesntExist )) 
                .addOptions(po.map())
                .addClass(Project.class)
            );
           
        list = (List) iQuery.execute(q);
        
    }

    @Test( groups = { "ticket:159" })
    public void test_shouldReturnImages() throws Exception
    {
        Project prj = (Project)
        iQuery.findByQuery( 
//                FIXME NullPointerException in Antlr. Report bug.
//                "select p from Project p " +
//                " where p.datasetLinks.child.imageLinks.child is not null ",p);
                "select p from Project p " +
                " left outer join p.datasetLinks as pdl " +
                " left outer join pdl.child as d " +
                " left outer join d.imageLinks as dil " +
                " left outer join dil.child as i " +
                " where i is not null",
                new Parameters( new Filter().unique().page(0,1)));
        
        q= new PojosGetImagesQueryDefinition(
                new Parameters()
                .addIds(Arrays.asList( prj.getId() ))
                .addClass(Project.class)
                );
        list= (List) iQuery.execute(q);
        assertTrue( list.size() > 0  );
        assertTrue( list.iterator().next().getClass().equals( Image.class ) );
        
    }
    
    // ~ Setup
    // =========================================================================

    MAP rootOnlyMap = new MAP();
    Experimenter user;
    MAP rootProjectMap = new MAP();
    MAP userImageRootProjectMap = new MAP();
    MAP userProjectMap = new MAP();

    PojoOptions userPO;
    Parameters filterForUser;
    Parameters noFilter;
    
    @Configuration( beforeTestClass = true )
    public void test_createObjects() throws Exception
    {
        // Forcing setup once now.
        adaptSetUp();

        // ~ ALL ROOT OBJECTS
        // =====================================================================
        
        loginRoot();
        Project p1 = createProject();
        Dataset d1 = createDataset();
        Dataset d2 = createDataset();
        Image i_d1 = createImage();
        Image i_d2 = createImage();
        
        p1.linkDataset( d1 );
        p1.linkDataset( d2 );
        d1.linkImage( i_d1 );
        d2.linkImage( i_d2 );
        
        rootOnlyMap.put(p1,i_d1);
        rootOnlyMap.put(d1,i_d1);
        rootOnlyMap.put(p1,i_d2);
        rootOnlyMap.put(d2,i_d2);
        
        CategoryGroup cg1 = createCategoryGroup();
        Category c1 = createCategory();
        Category c2 = createCategory();
        Image i_c1 = createImage();
        Image i_c2 = createImage();
        
        cg1.linkCategory(c1);
        cg1.linkCategory(c2);
        c1.linkImage(i_c1);
        c2.linkImage(i_c2);

        rootOnlyMap.put(cg1,i_c1);
        rootOnlyMap.put(c1,i_c1);
        rootOnlyMap.put(cg1,i_c2);
        rootOnlyMap.put(c2,i_c2);
        
        // Saving the newly created links.
        iUpdate.saveArray(new IObject[]{p1,d1,d2,i_d1,i_d2,cg1,c1,c2,i_c1,i_c2});

        // ~ USER FILTER
        // =====================================================================
        
        // TODO submit bug. leaving out the e of "Experimenter e" throws a 
        // null pointer exception in org.hibernate.hql.ast.*
        user = (Experimenter) iQuery.findByQuery(
                "select e from Experimenter e " +
                "where e.id != 0",
                new Parameters( new Filter().unique().page(0,1)));

        userPO = new PojoOptions().exp( user.getId() );
        filterForUser = new Parameters().addOptions( userPO.map() );
        noFilter = new Parameters().addOptions( null );

        // ~ MIXED ROOT/USER ITEMS
        // =====================================================================

        loginRoot();
        Project p1_root = createProject();
        Dataset d1_root = createDataset();
        Dataset d2_root = createDataset();
        Image i_d1_root = createImage();
        Image i_d2_root = createImage();
        
        p1_root.linkDataset( d1_root );
        p1_root.linkDataset( d2_root );
        d1_root.linkImage( i_d1_root );
        d2_root.linkImage( i_d2_root );
        
        rootProjectMap.put(p1_root,i_d1_root);
        rootProjectMap.put(d1_root,i_d1_root);
        rootProjectMap.put(p1_root,i_d2_root);
        rootProjectMap.put(d2_root,i_d2_root);
        
        loginUser(user.getOmeName());
        Image i_d1_user = createImage();
        Image i_d2_user = createImage();
        
        p1_root.linkDataset( d1_root );
        p1_root.linkDataset( d2_root );
        d1_root.linkImage( i_d1_user );
        d2_root.linkImage( i_d2_user );
        
        userImageRootProjectMap.put(p1_root,i_d1_user);
        userImageRootProjectMap.put(d1_root,i_d1_user);
        userImageRootProjectMap.put(p1_root,i_d2_user);
        userImageRootProjectMap.put(d2_root,i_d2_user);

        Project p11_user = createProject();
        Dataset d11_user = createDataset();
        Dataset d22_user = createDataset();
        Image i_d11_user = createImage();
        Image i_d22_user = createImage();
        
        p11_user.linkDataset( d11_user );
        p11_user.linkDataset( d22_user );
        d11_user.linkImage( i_d11_user );
        d22_user.linkImage( i_d22_user );
        
        userProjectMap.put(p11_user,i_d11_user);
        userProjectMap.put(d11_user,i_d11_user);
        userProjectMap.put(p11_user,i_d22_user);
        userProjectMap.put(d22_user,i_d22_user);

        // TODO not working.
//        setUser( user, 
//        i_d1_user,i_d2_user,
//        p11_user,d11_user,d22_user,i_d11_user,i_d22_user);
        
        // omitting CGC for now since this is only testing "enableFilters"
        
        loginRoot();
        
        // Saving the newly created links.
        iUpdate.saveArray(
                new IObject[]{
                        p1_root,d1_root,d2_root,i_d1_root,i_d2_root,
                        i_d1_user,i_d2_user,
                        p11_user,d11_user,d22_user,i_d11_user,i_d22_user});
        
        adaptTearDown();
    }

    // ~ UNFILTERED
    // =========================================================================
    
    @Test
    public void test_retrieve_levels() throws Exception
    {
       
        runWholeLevel( Project.class ); 
        runWholeLevel( CategoryGroup.class );
        runWholeLevel( Dataset.class );
        runWholeLevel( Category.class );

        runAllOnLevel( Project.class ); 
        runAllOnLevel( CategoryGroup.class );
        runAllOnLevel( Dataset.class );
        runAllOnLevel( Category.class );
    }

    private void runWholeLevel( Class klass )
    {
        runLevel( klass,rootOnlyMap.listKeyIds(klass), rootOnlyMap.listAllObjects(klass) );
    }
    
    private void runAllOnLevel( Class klass )
    {
        Set<Long> ids = rootOnlyMap.listKeyIds(klass);
        for (Long l : ids)
        {
            runLevel( klass,Collections.singleton(l), rootOnlyMap.get(klass,l) );    
        }
    }
    
    private void runLevel( 
            Class klass, 
            Collection<Long> klassIds, 
            Collection<IObject> results )
    {
        q= new PojosGetImagesQueryDefinition(
                new Parameters()
                    .addIds(klassIds)
                    .addOptions(null)
                    .addClass(klass));
           
        list = (List) iQuery.execute(q);
        
        assertTrue( "Didn't find expected number of results, but "+list.size(), 
                list.size() == results.size() );
    }
    
    // ~ FILTERING
    // =========================================================================

    @Test
    public void test_owner_filter_user_obj() throws Exception 
    {
        Parameters ids;
        
        Set set = new HashSet();
        set.addAll( rootProjectMap.listKeyIds(Project.class));
        set.addAll( userImageRootProjectMap.listKeyIds(Project.class));
        set.addAll( userProjectMap.listKeyIds(Project.class));
        ids = new Parameters().addIds( set );
        
        q= new PojosGetImagesQueryDefinition(
                new Parameters( ids ).addAll(noFilter).addClass(Project.class));
           
        list = (List) iQuery.execute(q);
        assertTrue( list.size() == 
            userImageRootProjectMap.listAllObjects(Project.class).size() + 
            rootProjectMap.listAllObjects(Project.class).size() + 
            userProjectMap.listAllObjects(Project.class).size());
        
        q= new PojosGetImagesQueryDefinition(
                new Parameters( ids ).addAll(filterForUser).addClass(Project.class));
           
        list = (List) iQuery.execute(q);
        int expected = 
            userImageRootProjectMap.listAllObjects(Project.class).size()
            + userProjectMap.listAllObjects(Project.class).size();
        assertTrue( list.size() + " not " +expected, list.size() == expected);

    }

    @Test
    public void test_owner_filter_root_obj() throws Exception 
    {
        Parameters ids;
        
        // Doesn't belong to user.
        // Using rootOnlyMap and not root*Map because root*Map has links to user, 
        // while rootOnlyMap doesn't.        
        ids = new Parameters().addIds( rootOnlyMap.listKeyIds(Project.class));
        q= new PojosGetImagesQueryDefinition(
                new Parameters( ids ).addAll(noFilter).addClass(Project.class));
   
        list = (List) iQuery.execute(q);
        assertTrue( list.size() == 
            rootOnlyMap.listAllObjects(Project.class).size() );
        
        q= new PojosGetImagesQueryDefinition(
                new Parameters( ids ).addAll(filterForUser).addClass(Project.class));
        
        list = (List) iQuery.execute(q);
        assertTrue( list.size() +" not 0", list.size() == 0 );

    }

    @Test( groups = "broken" )
    public void test_defaultPixelsIsFilled() throws Exception
    {
        Parameters param = new Parameters( new Filter().unique().page(0,1) );
        Image i = (Image) iQuery.findByQuery( 
                "select i from Image i " +
                "left join fetch i.datasetLinks idl " +
                "left join fetch idl.parent d " +
                "left join fetch d.projectLinks dpl " +
                "left join fetch dpl.parent p " +
                "where i.defaultPixels is not null and p is not null",param);
        assertNotNull( i );
        assertTrue( i.sizeOfDatasetLinks() > 0 );
        Dataset d = (Dataset) i.linkedDatasetList().get(0);
        assertTrue( d.sizeOfProjectLinks() > 0 );
        Project p = (Project) d.linkedProjectList().get(0);
        
        q= new PojosGetImagesQueryDefinition(
                new Parameters( )
                .addClass(Project.class)
                .addIds(Collections.singleton(p.getId())));
        List<Image> testImages = (List) iQuery.execute(q);
        boolean found = false;
        for (Image test : testImages)
        {
            if ( test.getId().equals( i.getId() ))
            {
                assertNotNull( test );
                assertNotNull( test.getDefaultPixels() );
                found = true;
            }
        }
        assertTrue( found );
        
    }
    // ~ Helpers
    // =========================================================================

    private void setUser( Experimenter e, IObject...objs) {
        for (IObject object : objs)
        {
            object.getDetails().setOwner(e);
        }
    }
    
    // TODO refactor to ObjectFactory
    private Project createProject(  )
    {
        Project p1 = new Project(  );
        p1.setName( this.getClass().getName() );
        return (Project)iUpdate.saveAndReturnObject(p1);
    }

    private Dataset createDataset(  )
    {
        Dataset d1 = new Dataset(  );
        d1.setName( this.getClass().getName() );
        return (Dataset)iUpdate.saveAndReturnObject(d1);
    }

    private Image createImage(  )
    {
        Image i1 = new Image(  );
        i1.setName( this.getClass().getName() );
        return (Image)iUpdate.saveAndReturnObject(i1);
    }

    private Category createCategory()
    {
        Category c1 = new Category();
        c1.setName( this.getClass().getName());
        return (Category)iUpdate.saveAndReturnObject(c1);
    }

    private CategoryGroup createCategoryGroup()
    {
        CategoryGroup cg1 = new CategoryGroup();
        cg1.setName( this.getClass().getName());
        return (CategoryGroup)iUpdate.saveAndReturnObject(cg1);
    }
    
    private class MAP {
        // TODO possibly simplfy in that this contains only one class 
        // a la generics, since that seems to be the use case.
        private Map<Class,Map<Long,Set<IObject>>> m 
            = new HashMap<Class,Map<Long,Set<IObject>>>();
        
        public void put(IObject source, IObject target)
        {
            assert source != null;
            assert target != null;
            assert source.getId() != null;
        
            Map<Long,Set<IObject>> i = m.get(source.getClass());
            if (i==null)
            {
                i = new HashMap<Long,Set<IObject>>();
                m.put(source.getClass(),i);
            }
            
            Set<IObject> s = i.get(source.getId()); 
            if ( s == null )
            {
                s = new HashSet<IObject>();
                i.put(source.getId(),s);
            }
            s.add(target);
            
        }
        
        public Set<IObject> get(IObject source)
        {
            assert source != null;
            assert source.getId() != null;
            
            Map<Long,Set<IObject>> i = m.get(source.getClass());
            if (i!=null)
            {
                Set<IObject> s = i.get(source.getId());
                if ( s == null )
                {
                    return new HashSet<IObject>();
                }
                return s;
            }
            return null;
        }

        public Set<IObject> get(Class sourceClass, Long id)
        {
            assert sourceClass != null;
            assert id != null;
            
            Map<Long,Set<IObject>> i = m.get(sourceClass);
            if (i!=null)
            {
                Set<IObject> s = i.get(id);
                if ( s == null )
                {
                    return new HashSet<IObject>();
                }
                return s;
            }
            return null;
        }
        
        public Set<Long> listKeyIds(Class sourceClass)
        {
            assert sourceClass != null;
            
            Map<Long,Set<IObject>> i = m.get(sourceClass);
            if ( i != null )
            {
                return i.keySet();
            }
            return new HashSet<Long>();
        }
        
        public Set<IObject> listAllObjects( Class sourceClass )
        {
            assert sourceClass != null;
            
            Set<Long> s = listKeyIds( sourceClass );
            Set<IObject> result = new HashSet<IObject>();
            
            for (Long l : s)
            {
                result.addAll(this.get( sourceClass, l ));
            }
            return result;
        }
        
    }
    
}
