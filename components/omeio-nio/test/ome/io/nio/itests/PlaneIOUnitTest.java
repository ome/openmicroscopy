/*
 * ome.io.nio.itests.PlaneIOUnitTest
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2005 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */
package ome.io.nio.itests;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import ome.io.nio.DimensionsOutOfBoundsException;
import ome.io.nio.PixelBuffer;
import ome.io.nio.PixelsService;
import ome.model.core.Pixels;
import ome.model.meta.Event;
import ome.model.meta.Experimenter;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;

import junit.framework.TestCase;


/**
 * @author callan
 *
 */
public class PlaneIOUnitTest extends TestCase
{

    private ApplicationContext ctx;

    private HibernateTemplate  ht;
    
    private Pixels pixels;
    private Experimenter experimenter;
    private Event event;
    private byte[][] originalDigests;
    
    private Integer planeCount;
    private Integer planeSize;

    private void initHibernate()
    {
        String[] paths = new String[] { "config.xml", "data.xml",
                "hibernate.xml" };
        ctx = new ClassPathXmlApplicationContext(paths);

        ht = (HibernateTemplate) ctx.getBean("hibernateTemplate");
    }
    
    private void createExperimenter()
    {
        experimenter = (Experimenter) ht.execute(new HibernateCallback()
        {
            public Object doInHibernate(org.hibernate.Session session)
                throws org.hibernate.HibernateException, java.sql.SQLException
            {
                Experimenter e = new Experimenter();
                e.setOmeName("test");
                session.save(e);
                
                return e;
            }
        });
    }
    
    private void createEvent()
    {
        event = (Event) ht.execute(new HibernateCallback()
        {
            public Object doInHibernate(org.hibernate.Session session)
                throws org.hibernate.HibernateException, java.sql.SQLException
            {
                Event e = new Event();
                e.setName("test");
                session.save(e);
                
                return e;
            }
        });
    }
    
    private void createPixels()
    {
        pixels = (Pixels) ht.execute(new HibernateCallback()
        {
            public Object doInHibernate(org.hibernate.Session session)
                throws org.hibernate.HibernateException, java.sql.SQLException
            {
                Pixels p = new Pixels();
                p.setSizeX(new Integer(64));
                p.setSizeY(new Integer(64));
                p.setSizeZ(new Integer(16));
                p.setSizeC(new Integer(2));
                p.setSizeT(new Integer(10));
                // FIXME: Bit of a hack until the model is updated, the follwing
                // is a SHA1 of "pixels"
                p.setSha1("09bc7b2dcc9a510f4ab3a40c47f7a4cb77954356");
                p.setBigEndian(Boolean.TRUE);

                p.setCreationEvent(event);
                p.setOwner(experimenter);

                session.save(p);
                return p;
            }
        });
    }
    
    private int getDigestOffset(int z, int c, int t)
    {
        int planeCountT = pixels.getSizeZ().intValue() *
                          pixels.getSizeC().intValue();
        
        return (planeCountT * t) + (pixels.getSizeZ() * c) + z;
    }
    
    private String getPlaneCheckErrStr(int z, int c, int t)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("Error with plane: ");
        sb.append("Z[");
        sb.append(z);
        sb.append("] ");
        sb.append("C[");
        sb.append(c);
        sb.append("] ");
        sb.append("T[");
        sb.append(t);
        sb.append("].");
        return sb.toString();
    }
    
    private byte[] createPlane(int planeSize, byte planeNo)
    {
        byte[] plane = new byte[planeSize];
        
        for (int i = 0; i < planeSize; i++)
            plane[i] = planeNo;
        
        return plane;
    }
    
    private void createPlanes() throws IOException
    {
        planeCount = pixels.getSizeZ() * pixels.getSizeC() *
                     pixels.getSizeT();
        // FIXME: *Hack* right now we assume everything is 16-bits wide
        planeSize  = pixels.getSizeX() * pixels.getSizeY() *
                     2;
        String  path = ome.io.nio.Helper.getPixelsPath(pixels.getId());
        originalDigests = new byte[planeCount][];
        
        FileOutputStream stream = new FileOutputStream(path);
        
        for (int i = 0; i < planeCount; i++)
        {
            byte[] plane = createPlane(planeSize.intValue(), (byte)(i - 128));
            originalDigests[i] = Helper.calculateMessageDigest(plane);
            stream.write(plane);
        }
    }
 
    protected void setUp() throws IOException
    {
        initHibernate();
        createExperimenter();
        createEvent();
        createPixels();
        createPlanes();
    }
    
    public void testInitialPlane()
        throws IOException, DimensionsOutOfBoundsException
    {
        PixelsService service = PixelsService.getInstance();
        PixelBuffer pixbuf = service.getPixelBuffer(pixels);
        MappedByteBuffer plane = pixbuf.getPlane(0, 0, 0);

        byte[] messageDigest = Helper.calculateMessageDigest(plane);
        
        assertEquals(Helper.bytesToHex(originalDigests[0]),
                     Helper.bytesToHex(messageDigest));
    }
    
    public void testLastPlane()
        throws IOException, DimensionsOutOfBoundsException
    {
        PixelsService service = PixelsService.getInstance();
        PixelBuffer pixbuf = service.getPixelBuffer(pixels);
        MappedByteBuffer plane = pixbuf.getPlane(pixels.getSizeZ() - 1,
                                                 pixels.getSizeC() - 1,
                                                 pixels.getSizeT() - 1);
        int digestOffset = getDigestOffset(pixels.getSizeZ() - 1,
                                           pixels.getSizeC() - 1,
                                           pixels.getSizeT() - 1);

        byte[] messageDigest = Helper.calculateMessageDigest(plane);
        
        assertEquals(Helper.bytesToHex(originalDigests[digestOffset]),
                     Helper.bytesToHex(messageDigest));
    }

    public void testAllPlanes()
    throws IOException, DimensionsOutOfBoundsException
    {
        PixelsService service = PixelsService.getInstance();
        PixelBuffer pixbuf = service.getPixelBuffer(pixels);
        
        String newMessageDigest;
        String oldMessageDigest;
        int digestOffset;
        for (int t = 0; t < pixels.getSizeT(); t++)
        {
            for (int c = 0; c < pixels.getSizeC(); c++)
            {
                for (int z = 0; z < pixels.getSizeZ(); z++)
                {
                    digestOffset = getDigestOffset(z, c, t);
                    MappedByteBuffer plane = pixbuf.getPlane(z, c, t);
                    newMessageDigest = 
                        Helper.bytesToHex(Helper.calculateMessageDigest(plane));
                    oldMessageDigest =
                        Helper.bytesToHex(originalDigests[digestOffset]);
                    
                    assertEquals(getPlaneCheckErrStr(z, c, t),
                                 oldMessageDigest, newMessageDigest);
                }
            }
        }
    }
    
    protected void tearDown()
    {
        ht.execute(new HibernateCallback()
        {
            public Object doInHibernate(org.hibernate.Session session)
                throws org.hibernate.HibernateException, java.sql.SQLException
            {
                if (pixels != null)
                {
                    Pixels p = (Pixels)
                        session.get(Pixels.class, pixels.getId());
                    session.delete(p);
                }
                
                if (experimenter != null)
                {
                    Experimenter ex = (Experimenter)
                        session.get(Experimenter.class, experimenter.getId());
                    session.delete(ex);
                }
                
                if (event != null)
                {
                    Event ev = (Event) session.get(Event.class, event.getId());
                    session.delete(ev);
                }
                
                return null;
            }
        });
    }
}
