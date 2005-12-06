package ome.io.bdb.utests;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class SetupTest extends AbstractBdbTest
{

    private static Log log = LogFactory.getLog(SetupTest.class);

    public void testIOSimply()
    {
        byte[] arr = TestUtils.randomByteArray(10);
        long id = db.last() + 1;
        log.info("Creating plane with id = " + id);

        io.putPlane(arr, id, 1, 1, 1);
        assertTrue(io.planeExists(id, 1, 1, 1));

        byte[] got = io.getPlane(id, 1, 1, 1);
        assertTrue(arr.length == got.length);
        for (int i = 0; i < got.length; i++)
        {
            assertEquals(arr[i], got[i]);
        }

        String[] items = db.list();
        assert (items.length > 0);
        TestUtils.list(db, log);
        io.deletePlane(id, 1, 1, 1);
    }

    public void testIOPixels()
    {
        byte[][][][] pixs = new byte[2][2][2][10];
        for (int i = 0; i < pixs.length; i++)
        {
            byte[][][] pixs_i = pixs[i];
            for (int j = 0; j < pixs_i.length; j++)
            {
                byte[][] pixs_ij = pixs_i[j];
                for (int k = 0; k < pixs_ij.length; k++)
                {
                    pixs_ij[k] = TestUtils.randomByteArray(10);
                }
            }
        }
        long id = db.last() + 1;
        log.info("Putting pixel with id = " + id);
        io.putPixels(pixs, id);
        TestUtils.list(db, log);
        assertTrue(io.pixelsExist(id));
    }

    public void testIOSpeed()
    {
        TestUtils.fillDB(io, db, log, 100, 10000);
        TestUtils.list(db, log);
        log.info("Finished.");
    }

}
