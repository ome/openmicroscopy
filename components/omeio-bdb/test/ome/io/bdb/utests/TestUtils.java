package ome.io.bdb.utests;

import java.util.Random;

import org.apache.commons.logging.Log;

import ome.api.IO;
import ome.io.bdb.Datastore;


public class TestUtils
{
    public static void list(Datastore db, Log log){
        String[] items = db.list();
        for (int i = 0; i < items.length; i++)
        {
            log.info(items[i]);
        }
    }
    
    public static void fillDB(IO io, Datastore db, Log log, int records, int size){
        long start = System.currentTimeMillis();
        for (int i = 0; i < records; i++)
        {
            io.putPlane(randomByteArray(size),db.last()+1,1,1,1);
        }
        long time = System.currentTimeMillis()-start;
        log.info("Filling \t"+records+" records of \t"+size+" bytes took:\t"+time+" ms.");
    }
    
    public static byte[] randomByteArray(int length)
    {
        byte[] newBytes = new byte[length];
        Random rnd = new Random();
        rnd.nextBytes(newBytes);
        return newBytes;
    }

}
