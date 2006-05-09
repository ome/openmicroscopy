package ome.io.nio.utests;


import org.testng.annotations.*;
import junit.framework.TestCase;
import ome.io.nio.PixelsService;

public class HelperUnitTest extends TestCase
{

    //
    // SINGLE DIRECTORY TESTS
    //
  @Test
    public void testPixelsSingleDirectoryLowerBoundsPath()
    {
        String path = new PixelsService("/OME/OMEIS/").getPixelsPath(new Long(1));
        assertEquals("/OME/OMEIS/Pixels/1", path);
    }
    
  @Test
    public void testFilesSingleDirectoryLowerBoundsPath()
    {
        String path = new PixelsService("/OME/OMEIS/").getFilesPath(new Long(1));
        assertEquals("/OME/OMEIS/Files/1", path);
    }

  @Test
    public void testPixelsSingleDirectoryUpperBoundsPath()
    {
        String path = new PixelsService("/OME/OMEIS/").getPixelsPath(new Long(999));
        assertEquals("/OME/OMEIS/Pixels/999", path);
    }
    
  @Test
    public void testFilesSingleDirectoryUpperBoundsPath()
    {
        String path = new PixelsService("/OME/OMEIS/").getFilesPath(new Long(999));
        assertEquals("/OME/OMEIS/Files/999", path);
    }

    //
    // TWO DIRECTORY TESTS
    //
  @Test
    public void testPixelsTwoDirectoryLowerBoundsPath()
    {
        String path = new PixelsService("/OME/OMEIS/").getPixelsPath(new Long(1001));
        assertEquals("/OME/OMEIS/Pixels/Dir-001/1001", path);
    }
    
  @Test
    public void testFilesTwoDirectoryLowerBoundsPath()
    {
        String path = new PixelsService("/OME/OMEIS/").getFilesPath(new Long(1001));
        assertEquals("/OME/OMEIS/Files/Dir-001/1001", path);
    }

  @Test
    public void testPixelsTwoDirectoryUpperBoundsPath()
    {
        String path = new PixelsService("/OME/OMEIS/").getPixelsPath(new Long(999999));
        assertEquals("/OME/OMEIS/Pixels/Dir-999/999999", path);
    }
    
  @Test
    public void testFilesTwoDirectoryUpperBoundsPath()
    {
        String path = new PixelsService("/OME/OMEIS/").getFilesPath(new Long(999999));
        assertEquals("/OME/OMEIS/Files/Dir-999/999999", path);
    }
    
    //
    // THREE DIRECTORY TESTS
    //
  @Test
    public void testPixelsThreeDirectoryLowerBoundsPath()
    {
        String path = new PixelsService("/OME/OMEIS/").getPixelsPath(new Long(1000001));
        assertEquals("/OME/OMEIS/Pixels/Dir-001/Dir-000/1000001", path);
    }
    
  @Test
    public void testFilesThreeDirectoryLowerBoundsPath()
    {
        String path = new PixelsService("/OME/OMEIS/").getFilesPath(new Long(1000001));
        assertEquals("/OME/OMEIS/Files/Dir-001/Dir-000/1000001", path);
    }

  @Test
    public void testPixelsThreeDirectoryUpperBoundsPath()
    {
        String path = new PixelsService("/OME/OMEIS/").getPixelsPath(new Long(999999999));
        assertEquals("/OME/OMEIS/Pixels/Dir-999/Dir-999/999999999", path);
    }
    
  @Test
    public void testFilesThreeDirectoryUpperBoundsPath()
    {
        String path = new PixelsService("/OME/OMEIS/").getFilesPath(new Long(999999999));
        assertEquals("/OME/OMEIS/Files/Dir-999/Dir-999/999999999", path);
    }
    
    //
    // FOUR DIRECTORY TESTS
    //
  @Test
    public void testPixelsFourDirectoryLowerBoundsPath()
    {
        String path = new PixelsService("/OME/OMEIS/").getPixelsPath(new Long(1000000001));
        assertEquals("/OME/OMEIS/Pixels/Dir-001/Dir-000/Dir-000/1000000001", path);
    }
    
  @Test
    public void testFilesFourDirectoryLowerBoundsPath()
    {
        String path = new PixelsService("/OME/OMEIS/").getFilesPath(new Long(1000000001));
        assertEquals("/OME/OMEIS/Files/Dir-001/Dir-000/Dir-000/1000000001", path);
    }

  @Test
    public void testPixelsFourDirectoryUpperBoundsPath()
    {
        String path = new PixelsService("/OME/OMEIS/").getPixelsPath((long)Integer.MAX_VALUE);
        assertEquals("/OME/OMEIS/Pixels/Dir-002/Dir-147/Dir-483/2147483647", path);
    }
    
  @Test
    public void testFilesFourDirectoryUpperBoundsPath()
    {
        String path = new PixelsService("/OME/OMEIS/").getFilesPath((long)Integer.MAX_VALUE);
        assertEquals("/OME/OMEIS/Files/Dir-002/Dir-147/Dir-483/2147483647", path);
    }
}
