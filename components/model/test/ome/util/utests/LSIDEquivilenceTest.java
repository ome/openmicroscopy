package ome.util.utests;

import ome.model.core.Image;
import ome.model.screen.Plate;
import ome.util.LSID;
import junit.framework.TestCase;

public class LSIDEquivilenceTest extends TestCase
{
    public void testStringVsStringNumeric()
    {
        LSID a = new LSID("Image:0");
        LSID b = new LSID("Image:0");
        assertEquals(a, b);
    }
    
    public void testStringVsStringAlpha()
    {
        LSID a = new LSID("Image:ABC");
        LSID b = new LSID("Image:ABC");
        assertEquals(a, b);
    }
    
    public void testStringVsStringMixed()
    {
        LSID a = new LSID("Image:ABC-1");
        LSID b = new LSID("Image:ABC-1");
        assertEquals(a, b);
    }
    
    public void testLSIDVsString()
    {
        LSID a = new LSID("ome.model.core.Image:0");
        LSID b = new LSID(Image.class, 0);
        assertEquals(a, b);
    }
    
    public void testStringVsLSID()
    {
        LSID a = new LSID(Image.class, 0);
        LSID b = new LSID("ome.model.core.Image:0");
        assertEquals(a, b);
    }
    
    public void testPlateStringVsLSID()
    {
        LSID a = new LSID(Plate.class, 0);
        LSID b = new LSID("ome.model.screen.Plate:0");
        assertEquals(a, b);
    }
    
    public void testLSIDVsStringWithConstructor()
    {
        LSID a = new LSID("ome.model.core.Image:0", true);
        LSID b = new LSID(Image.class, 0);
        assertEquals(a, b);
        assertEquals(a.getJavaClass(), b.getJavaClass());
        assertEquals(a.getIndexes()[0], b.getIndexes()[0]);
    }
    
    public void testStringVsLSIDWithConstructor()
    {
        LSID a = new LSID(Image.class, 0);
        LSID b = new LSID("ome.model.core.Image:0", true);
        assertEquals(a, b);
        assertEquals(a.getJavaClass(), b.getJavaClass());
        assertEquals(a.getIndexes()[0], b.getIndexes()[0]);
    }
    
    public void testPlateStringVsLSIDWithConstructor()
    {
        LSID a = new LSID(Plate.class, 0);
        LSID b = new LSID("ome.model.screen.Plate:0", true);
        assertEquals(a, b);
        assertEquals(a.getJavaClass(), b.getJavaClass());
        assertEquals(a.getIndexes()[0], b.getIndexes()[0]);
    }
}
