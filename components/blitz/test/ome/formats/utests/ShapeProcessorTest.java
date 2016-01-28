package ome.formats.utests;

import ome.formats.OMEROMetadataStoreClient;
import ome.formats.model.BlitzInstanceProvider;
import ome.util.LSID;
import omero.api.ServiceFactoryPrx;
import omero.model.Rectangle;
import omero.model.Roi;
import junit.framework.TestCase;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ShapeProcessorTest extends TestCase
{
	private OMEROMetadataStoreClient store;

	private static final int IMAGE_INDEX = 1;

	private static final int ROI_INDEX = 1;

	private static final int SHAPE_INDEX = 1;

	@BeforeMethod
	protected void setUp() throws Exception
	{
		ServiceFactoryPrx sf = new TestServiceFactory().proxy();
        store = new OMEROMetadataStoreClient();
        store.initialize(sf);
        store.setEnumerationProvider(new TestEnumerationProvider());
        store.setInstanceProvider(
			new BlitzInstanceProvider(store.getEnumerationProvider()));
        store.setROIDescription("Foobar", ROI_INDEX);
        store.setRectangleX(25.0, ROI_INDEX + 1, SHAPE_INDEX);
	}

	@Test
	public void testShapeExists()
	{
		assertEquals(1, store.countCachedContainers(Roi.class, null));
		assertEquals(1, store.countCachedContainers(Rectangle.class, null));
		LSID roiLSID1 = new LSID(Roi.class, ROI_INDEX);
		LSID shapeLSID1 = new LSID(Rectangle.class, ROI_INDEX + 1, SHAPE_INDEX);
		Roi roi = (Roi) store.getSourceObject(roiLSID1);
		Rectangle shape = (Rectangle) store.getSourceObject(shapeLSID1);
		assertNotNull(roi);
		assertNotNull(shape);
		assertEquals("Foobar", roi.getDescription().getValue());
		assertEquals(25.0, shape.getX().getValue());
	}

	@Test
	public void testShapePostProcess()
	{
		store.postProcess();
		assertEquals(2, store.countCachedContainers(Roi.class, null));
		assertEquals(1, store.countCachedContainers(Rectangle.class, null));
		LSID roiLSID1 = new LSID(Roi.class, ROI_INDEX);
		LSID roiLSID2 = new LSID(Roi.class, ROI_INDEX + 1);
		Roi roi1 = (Roi) store.getSourceObject(roiLSID1);
		Roi roi2 = (Roi) store.getSourceObject(roiLSID2);
		assertNotNull(roi1);
		assertNotNull(roi2);
		assertEquals("Foobar", roi1.getDescription().getValue());
		assertNull(roi2.getDescription());
	}
}
