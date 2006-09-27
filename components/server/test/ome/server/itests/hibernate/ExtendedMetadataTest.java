package ome.server.itests.hibernate;

import java.util.Arrays;

import ome.model.ILink;
import ome.model.IObject;
import ome.model.containers.Dataset;
import ome.model.containers.Project;
import ome.model.containers.ProjectDatasetLink;
import ome.model.core.Image;
import ome.model.core.Pixels;
import ome.model.display.RenderingDef;
import ome.model.meta.Experimenter;
import ome.server.itests.AbstractManagedContextTest;
import ome.testing.ObjectFactory;
import ome.tools.hibernate.ExtendedMetadata;

import org.testng.annotations.Configuration;
import org.testng.annotations.Test;


public class ExtendedMetadataTest extends AbstractManagedContextTest
{

	ExtendedMetadata metadata;
	
	@Configuration( beforeTestClass = true)
	public void init() throws Exception
	{
		setUp();
		metadata = new ExtendedMetadata( hibernateTemplate.getSessionFactory() );
		tearDown();
	}

	// ~ Locking
	// =========================================================================
	
	@Test
	public void testProjectLocksDataset() throws Exception 
	{
		Project p = new Project();
		Dataset d = new Dataset();
		p.linkDataset( d );
		ILink l = (ILink) p.collectDatasetLinks(null).iterator().next();
		
		assertDoesntContain( metadata.getLockCandidates( p ), d );
		assertContains( metadata.getLockCandidates( l ), d );
		
	}
	
	@Test
	// Because Pixels does not have a reference to RenderingDef
	public void testRenderingDefLocksPixels() throws Exception
	{
		Pixels p = ObjectFactory.createPixelGraph(null);
		RenderingDef r = ObjectFactory.createRenderingDef();
	
		r.setPixels(p);
		
		assertContains( metadata.getLockCandidates(r), p );
	
	}
	
	@Test( groups = "ticket:357" )
	// quirky because of defaultTag
	// see https://trac.openmicroscopy.org.uk/omero/ticket/357
	public void testPixelsLocksImage() throws Exception
	{
		Pixels p = ObjectFactory.createPixelGraph(null);
		p.setDefaultPixels(Boolean.TRUE);
		Image i = new Image(); i.setName("locking");
		i.addPixels(p);
		
		assertContains( metadata.getLockCandidates(p), i );
	
	}
	
	@Test
	// omit locks for system types (TODO they shouldn't have permissions anyway)
	public void testExperimenterDoesntGetLocked() throws Exception
	{
		Experimenter e = new Experimenter();
		Project p = new Project();
		p.getDetails().setOwner( e );
		
		assertDoesntContain( metadata.getLockCandidates(p), e );
	}
	
	@Test
	public void testNoNulls() throws Exception
	{
		Project p = new Project();
		ProjectDatasetLink pdl = new ProjectDatasetLink();
		pdl.link( p, null );
		
		assertDoesntContain( metadata.getLockCandidates(pdl), null );
		
	}
	
	// ~ Unlocking
	// =========================================================================

	@Test
	public void testProjectCanBeUnlockedFromDataset() throws Exception 
	{
		assertContains( metadata.getLockChecks(Project.class), 
				ProjectDatasetLink.class.getName(),
				"parent" ); 
	}
	
	@Test
	// Because Pixels does not have a reference to RenderingDef
	public void testPixelsCanBeUnlockedFromRenderingDef() throws Exception
	{
		assertContains( metadata.getLockChecks( Pixels.class ), 
				RenderingDef.class.getName(),
				"pixels");
	}

	@Test( groups = "ticket:357" )
	// quirky because of defaultTag
	// see https://trac.openmicroscopy.org.uk/omero/ticket/357
	public void testImageCanBeUnlockedFromPixels() throws Exception
	{
		assertContains( metadata.getLockChecks( Image.class ), 
				Pixels.class.getName(),
				"defaultPixelsTag.image");
	}

	// ~ Updating
	// =========================================================================
	
	@Test( groups = {"ticket:346","broken"} )
	public void testCreateEventImmutable() throws Exception {
		assertContains( metadata.getImmutableFields( Image.class ),
				"details.creationEvent" );
	}
	
	// ~ Helpers
	// =========================================================================
	
	private void assertContains( Object[] array, Object i)
	{
		if (!contained(array,i)) fail(i+" not contained in "+Arrays.toString(array));
	}

	private void assertDoesntContain( IObject[] array, IObject i)
	{
		if (contained(array,i)) fail(i+" contained in "+Arrays.toString(array));
	}
	
	private void assertContains( String[][] array, String t1, String t2 )
	{
		boolean contained = false;
		
		for (int i = 0; i < array.length; i++) {
			String[] test = array[i];
			if (test[0].equals( t1 ) && 
					test[1].equals( t2 ))
				contained |= true;
		}
		assertTrue(contained);
		
	}
	
	private boolean contained( Object[] array, Object i) {
		boolean contained = false;
		for (Object object : array) {
			if (i == null ) { 
				if ( object == null ) contained = true;
			} else {
				if (i.equals( object )) contained = true;
			}
		}
		return contained;
	}

}
