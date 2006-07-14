package ome.util.utests;

import ome.model.core.Image;
import ome.model.core.Pixels;
import ome.model.internal.Details;
import ome.util.ShallowCopy;

import org.testng.annotations.Test;

import junit.framework.TestCase;

public class ShallowCopyTest extends TestCase {

	@Test public void testNoNulls() throws Exception
	{
		Pixels pix = new Pixels();
		pix.setId( 1L );
		pix.setDetails( new Details() );
		pix.setSizeC( 1 );
		pix.setImage( new Image() );
		
		Pixels test = (Pixels) new ShallowCopy().copy(pix);
		assertNotNull(test.getId());
		assertNotNull(test.getDetails());
		assertNotNull(test.getSizeC());
		assertNotNull(test.getImage());
	}
	
}
