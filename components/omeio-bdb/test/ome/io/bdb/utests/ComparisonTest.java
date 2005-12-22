package ome.io.bdb.utests;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ome.io.nio.PixelBuffer;
import ome.io.nio.PixelsService;
import ome.model.core.Pixels;

import omeis.io.Gateway;

public class ComparisonTest extends AbstractBdbTest
{

    private static Log log    = LogFactory.getLog(ComparisonTest.class);

    static Map         images = new HashMap();
    static
    {
        images.put(new Integer(1), new Image(1, 64, 64, 1, 1, 13,
                "17ee01a5953b0a045c1e62af21116f96f3f02964"));
        images.put(new Integer(2), new Image(2, 512, 512, 35, 2, 1,
                "45193aa18b2ab6655bc9a0bda0fea25d1a521a9d"));
        images.put(new Integer(3), new Image(3, 1024, 1024, 24, 2, 1,
                "d6b52f52986a0f24a802df3b2a7cd03b1d57c8ff"));
        images.put(new Integer(4), new Image(4, 512, 512, 15, 1, 1,
                "8aee7600cb26d49fa5282465c96e4667fb9502b0"));
        images.put(new Integer(5), new Image(5, 512, 512, 15, 1, 1,
                "8f96202d71a75b0ea50851d558230f0153aa1641"));
        images.put(new Integer(6), new Image(6, 512, 512, 15, 1, 1,
                "647349cce5e84f07513a4a4a7d2560fcb2568c34"));
        images.put(new Integer(7), new Image(7, 512, 512, 1, 1, 1,
                "435281c08327877898f3600a44758a997ab5ba39"));
    }

    public void txestGetFromGateway() throws Exception
    {
        for (Iterator it = images.values().iterator(); it.hasNext();)
        {
            Image i = (Image) it.next();
            byte[] plane = i.getPlane(0, 0, 0);
        }

    }

    public void getAndCopy() throws Exception
    {
        
        final PixelsService ps = new PixelsService("/tmp"+PixelsService.ROOT_DEFAULT);
        final PixelBuffer bh[] = new PixelBuffer[1]; // BufferHolder
        
        for (Iterator it = images.values().iterator(); it.hasNext();)
        {
            // TestUtils.list(db,log); // FIXME MEMORY!
            Image i = (Image) it.next();
            bh[0] = ps.createPixelBuffer(image2pixels(i));
            i.eachPlane(new PlaneCallback()
            {

                void each(Image i, int z, int c, int t, int counter) throws Exception
                {
                    if (!io.planeExists(i.id, z, c, t))
                    {
                        byte[] plane = i.getPlane(z, c, t);
                        io.putPlane(plane, i.id, z, c, t);
                        bh[0].setPlane(plane,Integer.valueOf(z),Integer.valueOf(c),Integer.valueOf(t));  
                    }
                };
            });
        }
    }

    public void xtestGetAndCopy() throws Exception 
    {
        getAndCopy();
    }
    
    public void testSpeendCompareRandom() throws Exception 
    {
        int n = 200; // FIXME 100000 -> memory error
        Random r = new Random();
        Stats stats = new Stats();
        List imgs = new ArrayList(images.values());
   
        for (int i = 0; i < n; i++)
        {
            Image img = (Image) imgs.get(r.nextInt(imgs.size()));
            int z = r.nextInt(img.sz);
            int c = r.nextInt(img.sc);
            int t = r.nextInt(img.st);
     
            compare(stats,img,z,c,t);
            
        }

        System.out.flush();
        log.info(stats);
        db.stats(System.out);
    }
    
    public void xtestSpeedCompareOnceThrough() throws Exception
    {
        final Stats stats = new Stats();

        for (Iterator it = images.values().iterator(); it.hasNext();)
        {
            Image i = (Image) it.next();
            i.eachPlane(new PlaneCallback()
            {

                void each(Image i, int z, int c, int t, int counter)
                        throws Exception
                {
                    compare(stats,i,z,c,t);
                }
            });
        }
        log.info(stats);
    }

    public void xestSpeedCompareNThrough() throws Exception
    {
        final int n = 5;
        for (Iterator it = images.values().iterator(); it.hasNext();)
        {
            final Stats stats = new Stats();
            Image i = (Image) it.next();

            for (int j = 0; j < n; j++)
            {

                i.eachPlane(new PlaneCallback()
                {

                    void each(Image i, int z, int c, int t, int counter)
                            throws Exception
                    {
                        compare(stats,i,z,c,t);
                    }
                });
            }
            log.info(stats);
        }
    }

    void compare(Stats stats, Image i, int z, int c, int t) throws Exception{
        stats.g_start();
        byte[] g_arr = Gateway.getPlane(i.id, z, c, t);
        stats.g_stop();
        assertFalse(g_arr.length == 0);

        stats.b_start();
        byte[] b_arr = io.getPlane(i.id, z, c, t);
        stats.b_stop();
        assertFalse(b_arr.length == 0);

        PixelsService ps = new PixelsService("/tmp"+PixelsService.ROOT_DEFAULT);
        PixelBuffer pb = ps.getPixelBuffer(image2pixels(i));
        Integer Z = Integer.valueOf(z), C = Integer.valueOf(c), T=Integer.valueOf(t);
        
        byte[] result = new byte[b_arr.length];
        stats.n_start();
        // byte[] n_arr = buffer2byte(pb.getPlane(Z,C,T)); SLOWER than BDB
        // pb.getPlane(Z,C,T); 3x Faster
        // pb.getPlane(Z,C,T).get(result); // 2.6 slower
        // empty is .006 of C speed.
        stats.n_stop();
//        assertFalse(n_arr.length == 0);
        
//        assertTrue(g_arr.length == b_arr.length);
 //       assertTrue(g_arr.length == n_arr.length);
        
        stats.update();
        System.err.print("B/G:"+stats.s_times.get(stats.last()-1));
        System.err.println("\tN/G:"+stats.s2_times.get(stats.last()-1));

    }
    
    byte[] buffer2byte(ByteBuffer buffer)
    {
        byte[] bytes = new byte[buffer.limit()];
        for (int i = 0; i < bytes.length; i++)
        {
            bytes[i] = buffer.get(i);
        }
        return bytes;
    }
    
    Pixels image2pixels(Image i)
    {
        Pixels p = new Pixels();
        p.setId(new Long(i.id));
        p.setSizeX(Integer.valueOf(i.sx));
        p.setSizeY(Integer.valueOf(i.sy));
        p.setSizeZ(Integer.valueOf(i.sz));
        p.setSizeC(Integer.valueOf(i.sc));
        p.setSizeT(Integer.valueOf(i.st));
        return p;
    }
    
}

class Stats
{

    List   b_times = new ArrayList();

    List   g_times = new ArrayList();

    List   n_times = new ArrayList();
    
    List   s_times = new ArrayList();
    
    List   s_avg   = new ArrayList();

    List   s2_times= new ArrayList();
    
    List   s2_avg  = new ArrayList();
    
    long   b_total = 0, g_total = 0, n_total = 0;

    double s_total = 0.0, s2_total = 0.0;

    long b_running, g_running, n_running;
    
    void b_start(){
        b_running = System.nanoTime();
    }
    
    void b_stop(){
        b_running = System.nanoTime() - b_running;
    }

    void g_start(){
        g_running = System.nanoTime();
    }
    
    void g_stop(){
        g_running = System.nanoTime() - g_running;
    }

    void n_start(){
        n_running = System.nanoTime();
    }
    
    void n_stop(){
        n_running = System.nanoTime() - n_running;
    }
        
    void update(){
        add(b_running,g_running,n_running);
    }
    
    void add(long b_time, long g_time, long n_time)
    {
        Long b = new Long(b_time);
        Long g = new Long(g_time);
        Long n = new Long(n_time);
        Double s = new Double(b.doubleValue() / g.doubleValue());
        Double s2 = new Double(n.doubleValue() / g.doubleValue());
        b_times.add(b);
        g_times.add(g);
        n_times.add(n);
        s_times.add(s);
        s2_times.add(s2);
        b_total += b.longValue();
        g_total += g.longValue();
        s_total += s.doubleValue();
        s2_total += s2.doubleValue();
        s_avg.add(new Double(s_total/(double)s_times.size()));
        s2_avg.add(new Double(s2_total/(double)s2_times.size()));
    }

    int last()
    {
        return b_times.size();
    }

    public String toString()
    {
        double total=0.0;
        StringBuilder sb = new StringBuilder();
        sb.append("\n---------------------------------\n");
        for (int i = 0; i < last(); i++)
        {
            total+=((Double)s_times.get(i)).doubleValue();
            sb.append(i);
            sb.append("\tB/G:  " + s_times.get(i));
            sb.append(" (" + s_avg.get(i) + ")   ");
            sb.append("\tN/G:  " + s2_times.get(i));
            sb.append(" (" + s2_avg.get(i) + ")   ");
            sb.append("\tGtw:\t" + g_times.get(i));
            sb.append("\tBdb:\t" + b_times.get(i));
            sb.append("\tNio:\t" + n_times.get(i));
            sb.append("\n");
        }
        sb.append("\n----------------------------------");
        sb.append("\nBMin: "+Collections.min(b_times));
        sb.append("\nBMax: "+Collections.max(b_times));
        sb.append("\nGMin: "+Collections.min(g_times));
        sb.append("\nGMax: "+Collections.max(g_times));
        sb.append("\nNMin: "+Collections.min(n_times));
        sb.append("\nNMax: "+Collections.max(n_times));
        sb.append("\nSMin: "+Collections.min(s_times));
        sb.append("\nSMax: "+Collections.max(s_times));
        sb.append("\nS2Min: "+Collections.min(s2_times));
        sb.append("\nS2Max: "+Collections.max(s2_times));
        sb.append("\n----------------------------------");
         
        return sb.toString();
    }

}

abstract class PlaneCallback
{

    abstract void each(Image i, int z, int c, int t, int counter)
            throws Exception;

}

class Image
{

    int    id, sx, sy, sz, sc, st;

    String sha1;

    Image(int id, int size_x, int size_y, int size_z, int size_c, int size_t,
            String sha1)
    {
        this.id = id;
        sx = size_x;
        sy = size_y;
        sz = size_z;
        sc = size_c;
        st = size_t;
        this.sha1 = sha1;
    }

    byte[] getPlane(int z, int c, int t) throws Exception
    {
        if (z < 0 || z >= sz || c < 0 || c >= sc || t < 0 || t >= st)
            throw new IllegalArgumentException("Nope.");

        byte[] out = Gateway.getPlane(id, z, c, t);

        return out;
    }

    void eachPlane(PlaneCallback action) throws Exception
    {
        int counter = 0;
        for (int z = 0; z < sz; z++)
        {
            for (int c = 0; c < sc; c++)
            {
                for (int t = 0; t < st; t++)
                {
                    if (action != null) action.each(this, z, c, t, ++counter);
                }
            }
        }
    }

}
