package ome.formats.utests;

import ome.formats.LSID;
import omero.model.Image;
import junit.framework.TestCase;

public class EquivilenceTest extends TestCase
{
    public void testStringVsString()
    {
        LSID a = new LSID("Image:0");
        LSID b = new LSID("Image:0");
        assertTrue(a.equals(b));
    }
    
    public void testLSIDVsString()
    {
        LSID a = new LSID("omero.model.Image:0");
        LSID b = new LSID(Image.class, 0);
        assertTrue(a.equals(b));
    }
    
    public void testStringVsLSID()
    {
        LSID a = new LSID(Image.class, 0);
        LSID b = new LSID("omero.model.Image:0");
        assertTrue(a.equals(b));
    }
}
