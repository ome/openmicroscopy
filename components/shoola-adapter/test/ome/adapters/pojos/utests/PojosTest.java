package ome.adapters.pojos.utests;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import junit.framework.TestCase;

import ome.model.IObject;
import ome.model.annotations.DatasetAnnotation;
import ome.model.annotations.ImageAnnotation;
import ome.model.containers.Category;
import ome.model.containers.CategoryGroup;
import ome.model.containers.Dataset;
import ome.model.containers.Project;
import ome.model.core.Image;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;

import pojos.CategoryData;
import pojos.CategoryGroupData;
import pojos.DatasetData;
import pojos.ImageData;
import pojos.ProjectData;


public class PojosTest extends TestCase
{
    IObject[] all;
    Project p;
    Dataset d1, d2, d3;
    Image i1, i2, i3;
    CategoryGroup cg;
    Category c;
    ImageAnnotation iann;
    DatasetAnnotation dann;
    Experimenter e;
    ExperimenterGroup g;
    
    protected void setUp() throws Exception {
        p = new Project(new Long(1));
        d1 = new Dataset(new Long(2));
        d2 = new Dataset(new Long(3));
        d3 = new Dataset(new Long(4));
        i1 = new Image(new Long(5));
        i2 = new Image(new Long(6));
        i3 = new Image(new Long(7));
        cg = new CategoryGroup(new Long(8));
        c = new Category(new Long(9));
        iann = new ImageAnnotation(new Long(10));
        dann = new DatasetAnnotation(new Long(11));
        e = new Experimenter(new Long(12));
        g = new ExperimenterGroup(new Long(13));
        
        all = new IObject[] { p,d1,d2,d3,i1,i2,i3,cg,c,iann,dann,e,g};
        
        p.linkDataset( d1 );
        p.linkDataset( d2 );
        p.linkDataset( d3 );
        d1.linkImage( i1 );
        d1.linkImage( i2 );
        d1.linkImage( i3 );
        d2.linkImage( i2 );
        d3.linkImage( i3 );
        i2.linkCategory( c );
        c.linkCategoryGroup( cg );
        i3.addToAnnotations( iann );
        d3.addToAnnotations( dann );
        
        e.linkExperimenterGroup( g );

        setOwnerAndGroup( e,g,all );
        
    }

    void setOwnerAndGroup( Experimenter e, ExperimenterGroup g, IObject[] objs) 
    {
        for (int i = 0; i < objs.length; i++)
        {
            objs[i].getDetails().setOwner( e );
            objs[i].getDetails().setGroup( g );
        }
    }
    
    public void test(){
        ProjectData pd = new ProjectData( p );
        assertNotNull( pd.getDatasets() );
        assertFalse( pd.getDatasets().size()==0 );
        assertFalse( pd.getDatasets().iterator().next().getClass()==Dataset.class );
        System.out.println( pd );
    }
    
    public void test_modying_got_set_does_nothing() throws Exception
    {
        ProjectData pd = new ProjectData( p );
        Set got = pd.getDatasets();
        got.add( "X" );
        
        assertTrue( got.size() > pd.getDatasets().size() );
        assertTrue( got != pd.getDatasets() );
        
    }
    
    public void testReverseMapping() throws Exception
    {
        ProjectData pd = new ProjectData();
        DatasetData dd = new DatasetData();
        
        Set dds = new HashSet();
        dds.add( dd );
        pd.setDatasets( dds );

        Set pds = new HashSet();
        pds.add( pd );
        dd.setProjects( pds );
        
        Project prj = (Project) pd.asIObject();
        assertTrue( prj.sizeOfDatasetLinks() > 0 );
        assertTrue( ( (Dataset) prj.linkedDatasetList().get(0))
                .sizeOfProjectLinks() > 0);
    }
    
    public void testNoDuplicateLinks() throws Exception
    {
        Project p_2 = new Project();
        Dataset d_2 = new Dataset();
        Image i_2 = new Image();
        p_2.linkDataset( d_2 );
        d_2.linkImage( i_2 );
        
        ProjectData pd = new ProjectData( p_2 );
        DatasetData dd = (DatasetData) pd.getDatasets().iterator().next();
        Dataset test = (Dataset) dd.asIObject();
        
        Set p_links = new HashSet( p_2.collectDatasetLinks( null ));
        Set d_links = new HashSet( test.collectProjectLinks( null ));
        
        System.out.println( p_links );
        System.out.println( d_links );
        
        assertTrue( p_links.containsAll( d_links ));
        
        DatasetData dd2 = new DatasetData( test );
        ImageData id = (ImageData) dd2.getImages().iterator().next();
        Image test2 = (Image) id.asIObject();
        
        Set d2_links = new HashSet( d_2.collectImageLinks( null ));
        Set i2_links = new HashSet( test2.collectDatasetLinks( null ));
        
        System.out.println( d2_links );
        System.out.println( i2_links );
        
        assertTrue( d2_links.containsAll( i2_links ) );
    }
    
    public void test_p_and_d() throws Exception
    {
        Project p = new Project();
        Dataset d = new Dataset();
        
        p.linkDataset( d );
        
        ProjectData pd = new ProjectData( p );
        
        Set datasets = pd.getDatasets();
        
        DatasetData dd = new DatasetData();
        datasets.add( dd );
        
        pd.setDatasets( datasets );
        
        Project test = (Project) pd.asIObject(); 
        
        assertTrue( test.sizeOfDatasetLinks() > 1 );
    }
    
    public void test_walk_a_graph() throws Exception
    {
        ProjectData pd = new ProjectData( p );
        Iterator it = pd.getDatasets().iterator();
        DatasetData dd = null;
        while ( it.hasNext() )
        {
            dd = (DatasetData) it.next();
            if ( dd.asIObject() == d3 )
                break;
        }
        assertTrue( dd.getAnnotations().size() == 1 );
        assertTrue( dd.getImages().size() == 1 );
        
        it = dd.getImages().iterator();
        ImageData id = null;
        while ( it.hasNext() )
        {
            id = (ImageData) it.next();
            assertTrue( id.asIObject() == i3 );
        }
        
        dd = null;
        it = pd.getDatasets().iterator();
        while ( it.hasNext() )
        {
            dd = (DatasetData) it.next();
            if ( dd.asIObject() == d2 )
                break;
        }
        id = (ImageData) dd.getImages().iterator().next();
        assertTrue( id.getCategories().size() == 1 );
        
        CategoryData cd = (CategoryData) id.getCategories().iterator().next();
        CategoryGroupData cgd = cd.getGroup();
        CategoryGroup haha = (CategoryGroup) cgd.asIObject();
        
    }
    
    public void test_bidirectional() throws Exception
    {
        CategoryData cd = new CategoryData( c );
        ImageData id = (ImageData) cd.getImages().iterator().next();

        Set ref_imgs = cd.getImages();
        Set ref_cats = id.getCategories();
        
        ImageData add = new ImageData( );
        Set imgs = cd.getImages();
        imgs.add( add );
        cd.setImages( imgs );
        
        assertTrue( cd.getImages().size() > ref_imgs.size() );
        assertTrue( id.getCategories().size() > ref_cats.size() );
        
    }
}
