package ome.io.nio.utests;


import junit.framework.TestCase;
import ome.io.nio.PixelsService;

public class HelperUnitTest extends TestCase
{

    //
    // SINGLE DIRECTORY TESTS
    //
    public void testPixelsSingleDirectoryLowerBoundsPath()
    {
        String path = new PixelsService("/OME/OMEIS/").getPixelsPath(new Long(1));
        assertEquals("/OME/OMEIS/Pixels/1", path);
    }
    
    public void testFilesSingleDirectoryLowerBoundsPath()
    {
        String path = new PixelsService("/OME/OMEIS/").getFilesPath(new Long(1));
        assertEquals("/OME/OMEIS/Files/1", path);
    }

    public void testPixelsSingleDirectoryUpperBoundsPath()
    {
        String path = new PixelsService("/OME/OMEIS/").getPixelsPath(new Long(999));
        assertEquals("/OME/OMEIS/Pixels/999", path);
    }
    
    public void testFilesSingleDirectoryUpperBoundsPath()
    {
        String path = new PixelsService("/OME/OMEIS/").getFilesPath(new Long(999));
        assertEquals("/OME/OMEIS/Files/999", path);
    }

    //
    // TWO DIRECTORY TESTS
    //
    public void testPixelsTwoDirectoryLowerBoundsPath()
    {
        String path = new PixelsService("/OME/OMEIS/").getPixelsPath(new Long(1001));
        assertEquals("/OME/OMEIS/Pixels/Dir-001/1001", path);
    }
    
    public void testFilesTwoDirectoryLowerBoundsPath()
    {
        String path = new PixelsService("/OME/OMEIS/").getFilesPath(new Long(1001));
        assertEquals("/OME/OMEIS/Files/Dir-001/1001", path);
    }

    public void testPixelsTwoDirectoryUpperBoundsPath()
    {
        String path = new PixelsService("/OME/OMEIS/").getPixelsPath(new Long(999999));
        assertEquals("/OME/OMEIS/Pixels/Dir-999/999999", path);
    }
    
    public void testFilesTwoDirectoryUpperBoundsPath()
    {
        String path = new PixelsService("/OME/OMEIS/").getFilesPath(new Long(999999));
        assertEquals("/OME/OMEIS/Files/Dir-999/999999", path);
    }
    
    //
    // THREE DIRECTORY TESTS
    //
    public void testPixelsThreeDirectoryLowerBoundsPath()
    {
        String path = new PixelsService("/OME/OMEIS/").getPixelsPath(new Long(1000001));
        assertEquals("/OME/OMEIS/Pixels/Dir-001/Dir-000/1000001", path);
    }
    
    public void testFilesThreeDirectoryLowerBoundsPath()
    {
        String path = new PixelsService("/OME/OMEIS/").getFilesPath(new Long(1000001));
        assertEquals("/OME/OMEIS/Files/Dir-001/Dir-000/1000001", path);
    }

    public void testPixelsThreeDirectoryUpperBoundsPath()
    {
        String path = new PixelsService("/OME/OMEIS/").getPixelsPath(new Long(999999999));
        assertEquals("/OME/OMEIS/Pixels/Dir-999/Dir-999/999999999", path);
    }
    
    public void testFilesThreeDirectoryUpperBoundsPath()
    {
        String path = new PixelsService("/OME/OMEIS/").getFilesPath(new Long(999999999));
        assertEquals("/OME/OMEIS/Files/Dir-999/Dir-999/999999999", path);
    }
    
    //
    // FOUR DIRECTORY TESTS
    //
    public void testPixelsFourDirectoryLowerBoundsPath()
    {
        String path = new PixelsService("/OME/OMEIS/").getPixelsPath(new Long(1000000001));
        assertEquals("/OME/OMEIS/Pixels/Dir-001/Dir-000/Dir-000/1000000001", path);
    }
    
    public void testFilesFourDirectoryLowerBoundsPath()
    {
        String path = new PixelsService("/OME/OMEIS/").getFilesPath(new Long(1000000001));
        assertEquals("/OME/OMEIS/Files/Dir-001/Dir-000/Dir-000/1000000001", path);
    }

    public void testPixelsFourDirectoryUpperBoundsPath()
    {
        String path = new PixelsService("/OME/OMEIS/").getPixelsPath((long)Integer.MAX_VALUE);
        assertEquals("/OME/OMEIS/Pixels/Dir-002/Dir-147/Dir-483/2147483647", path);
    }
    
    public void testFilesFourDirectoryUpperBoundsPath()
    {
        String path = new PixelsService("/OME/OMEIS/").getFilesPath((long)Integer.MAX_VALUE);
        assertEquals("/OME/OMEIS/Files/Dir-002/Dir-147/Dir-483/2147483647", path);
    }
}
