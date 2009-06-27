package ome.formats.utests;

import ome.formats.OMEROMetadataStoreClient;
import ome.formats.model.BlitzInstanceProvider;
import ome.util.LSID;
import omero.api.ServiceFactoryPrx;
import omero.model.CommentAnnotation;
import omero.model.Image;
import omero.model.OriginalFile;
import omero.model.Pixels;
import junit.framework.TestCase;

public class AnnotationTest extends TestCase
{
	private OMEROMetadataStoreClient store;
	
	private static final int IMAGE_INDEX = 0;
	
	private static final int PIXELS_INDEX = 0;
	
	@Override
	protected void setUp() throws Exception
	{
		ServiceFactoryPrx sf = new TestServiceFactory();
        store = new OMEROMetadataStoreClient();
        store.initialize(sf);
        store.setEnumerationProvider(new TestEnumerationProvider());
        store.setInstanceProvider(
        		new BlitzInstanceProvider(store.getEnumerationProvider()));
        store.setImageName("An Image", IMAGE_INDEX);
        store.setPixelsSizeZ(1, IMAGE_INDEX, PIXELS_INDEX);
	}
	
	public void testLinkSingleAnnotation()
	{
        LSID imageKey = new LSID(Image.class, IMAGE_INDEX);
        LSID annotationKey = new LSID(CommentAnnotation.class, IMAGE_INDEX);
        store.addReference(imageKey, annotationKey);
        int c = store.countCachedReferences(Image.class,
        		                            CommentAnnotation.class);
        assertEquals(1, c);
        assertEquals(1, store.getReferenceCache().size());
        assertEquals(1, store.getReferenceCache().get(imageKey).size());
        assertTrue(store.hasReference(imageKey, annotationKey));
	}
	
	public void testLinkTwoAnnotations()
	{
        LSID imageKey = new LSID(Image.class, IMAGE_INDEX);
        LSID annotationKey1 = new LSID(CommentAnnotation.class, IMAGE_INDEX);
        LSID annotationKey2 = new LSID(CommentAnnotation.class, IMAGE_INDEX + 1);
        
        store.addReference(imageKey, annotationKey1);
        store.addReference(imageKey, annotationKey2);
        int c = store.countCachedReferences(Image.class,
        		                            CommentAnnotation.class);
        assertEquals(2, c);
        assertEquals(1, store.getReferenceCache().size());
        assertEquals(2, store.getReferenceCache().get(imageKey).size());
        assertTrue(store.hasReference(imageKey, annotationKey1));
        assertTrue(store.hasReference(imageKey, annotationKey2));
	}
	
	public void testLinkSingleAnnotationAndOriginalFile()
	{
        LSID imageKey = new LSID(Image.class, IMAGE_INDEX);
        LSID pixelsKey = new LSID(Pixels.class, IMAGE_INDEX, PIXELS_INDEX);
        LSID annotationKey = new LSID(CommentAnnotation.class, IMAGE_INDEX);
        LSID originalFileKey = new LSID(OriginalFile.class, 0);
        store.addReference(imageKey, annotationKey);
        store.addReference(pixelsKey, originalFileKey);
        
        int c = store.countCachedReferences(Image.class,
        		                            CommentAnnotation.class);
        assertEquals(1, c);
        c = store.countCachedReferences(Pixels.class, OriginalFile.class);
        assertEquals(1, c);
        assertEquals(2, store.getReferenceCache().size());
        assertEquals(1, store.getReferenceCache().get(imageKey).size());
        assertEquals(1, store.getReferenceCache().get(pixelsKey).size());
        assertTrue(store.hasReference(imageKey, annotationKey));
        assertTrue(store.hasReference(pixelsKey, originalFileKey));
	}
	
	public void testLinkTwoAnnotationsAndOriginalFiles()
	{
        LSID imageKey = new LSID(Image.class, IMAGE_INDEX);
        LSID pixelsKey = new LSID(Pixels.class, IMAGE_INDEX, PIXELS_INDEX);
        LSID annotationKey1 = new LSID(CommentAnnotation.class, IMAGE_INDEX);
        LSID annotationKey2 = new LSID(CommentAnnotation.class, IMAGE_INDEX + 1);
        LSID originalFileKey1 = new LSID(OriginalFile.class, 0);
        LSID originalFileKey2 = new LSID(OriginalFile.class, 0);
        
        store.addReference(imageKey, annotationKey1);
        store.addReference(imageKey, annotationKey2);
        store.addReference(pixelsKey, originalFileKey1);
        store.addReference(pixelsKey, originalFileKey2);
        int c = store.countCachedReferences(Image.class,
        		                            CommentAnnotation.class);
        assertEquals(2, c);
        c = store.countCachedReferences(Pixels.class, OriginalFile.class);
        assertEquals(2, c);
        assertEquals(2, store.getReferenceCache().size());
        assertEquals(2, store.getReferenceCache().get(imageKey).size());
        assertEquals(2, store.getReferenceCache().get(pixelsKey).size());
        assertTrue(store.hasReference(imageKey, annotationKey1));
        assertTrue(store.hasReference(imageKey, annotationKey2));
        assertTrue(store.hasReference(pixelsKey, originalFileKey1));
        assertTrue(store.hasReference(pixelsKey, originalFileKey2));
        
	}
}
