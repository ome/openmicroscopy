package ome.formats.utests;

import ome.formats.OMEROMetadataStoreClient;
import ome.formats.model.BlitzInstanceProvider;
import ome.units.UNITS;
import ome.units.quantity.Time;
import ome.util.LSID;
import ome.xml.model.primitives.NonNegativeInteger;
import omero.api.ServiceFactoryPrx;
import omero.model.PlaneInfo;
import junit.framework.TestCase;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;


public class PlaneInfoProcessorTest extends TestCase
{
    private OMEROMetadataStoreClient store;

    private static final int IMAGE_INDEX = 1;

    private static final int PLANE_INFO_INDEX = 1;

    @BeforeMethod
    protected void setUp() throws Exception
    {
        Time onesec = new Time(1, UNITS.S);
        ServiceFactoryPrx sf = new TestServiceFactory().proxy();
        store = new OMEROMetadataStoreClient();
        store.initialize(sf);
        store.setEnumerationProvider(new TestEnumerationProvider());
        store.setInstanceProvider(
                new BlitzInstanceProvider(store.getEnumerationProvider()));
        store.setPlaneTheC(new NonNegativeInteger(0), IMAGE_INDEX, PLANE_INFO_INDEX);
        store.setPlaneTheZ(new NonNegativeInteger(0), IMAGE_INDEX, PLANE_INFO_INDEX);
        store.setPlaneTheT(new NonNegativeInteger(0), IMAGE_INDEX, PLANE_INFO_INDEX);
        store.setPlaneTheC(new NonNegativeInteger(1), IMAGE_INDEX, PLANE_INFO_INDEX + 1);
        store.setPlaneTheZ(new NonNegativeInteger(1), IMAGE_INDEX, PLANE_INFO_INDEX + 1);
        store.setPlaneTheT(new NonNegativeInteger(1), IMAGE_INDEX, PLANE_INFO_INDEX + 1);
        store.setPlaneTheC(new NonNegativeInteger(2), IMAGE_INDEX, PLANE_INFO_INDEX + 2);
        store.setPlaneTheZ(new NonNegativeInteger(2), IMAGE_INDEX, PLANE_INFO_INDEX + 2);
        store.setPlaneTheT(new NonNegativeInteger(2), IMAGE_INDEX, PLANE_INFO_INDEX + 2);
        store.setPlaneDeltaT(onesec, IMAGE_INDEX, PLANE_INFO_INDEX +2);
    }

    @Test
    public void testPlaneInfoExists()
    {
        assertEquals(3, store.countCachedContainers(PlaneInfo.class, null));
        LSID planeInfoLSID1 = new LSID(PlaneInfo.class, IMAGE_INDEX, PLANE_INFO_INDEX);
        LSID planeInfoLSID2 = new LSID(PlaneInfo.class, IMAGE_INDEX, PLANE_INFO_INDEX + 1);
        LSID planeInfoLSID3 = new LSID(PlaneInfo.class, IMAGE_INDEX, PLANE_INFO_INDEX + 2);
        PlaneInfo pi1 = (PlaneInfo) store.getSourceObject(planeInfoLSID1);
        PlaneInfo pi2 = (PlaneInfo) store.getSourceObject(planeInfoLSID2);
        PlaneInfo pi3 = (PlaneInfo) store.getSourceObject(planeInfoLSID3);
        assertNotNull(pi1);
        assertNotNull(pi2);
        assertNotNull(pi3);
        assertEquals(0, pi1.getTheC().getValue());
        assertEquals(0, pi1.getTheZ().getValue());
        assertEquals(0, pi1.getTheT().getValue());
        assertEquals(1, pi2.getTheC().getValue());
        assertEquals(1, pi2.getTheZ().getValue());
        assertEquals(1, pi2.getTheT().getValue());
        assertEquals(2, pi3.getTheC().getValue());
        assertEquals(2, pi3.getTheZ().getValue());
        assertEquals(2, pi3.getTheT().getValue());
        assertEquals(1.0, pi3.getDeltaT().getValue());
    }

    @Test
    public void testPlaneInfoCleanup()
    {
        store.postProcess();
        assertEquals(1, store.countCachedContainers(PlaneInfo.class, null));
        LSID planeInfoLSID = new LSID(PlaneInfo.class, IMAGE_INDEX, PLANE_INFO_INDEX + 2);
        PlaneInfo pi = (PlaneInfo) store.getSourceObject(planeInfoLSID);
        assertNotNull(pi);
        assertEquals(2, pi.getTheC().getValue());
        assertEquals(2, pi.getTheZ().getValue());
        assertEquals(2, pi.getTheT().getValue());
        assertEquals(1.0, pi.getDeltaT().getValue());
    }
}
