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

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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
    private byte[][] newDigests;

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
    
    private MessageDigest newSha1MessageDigest()
    {
        MessageDigest md;
        
        try
        {
            md = MessageDigest.getInstance("SHA-1");
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new RuntimeException(
                    "Required SHA-1 message digest algorithm unavailable.");
        }
        
        md.reset();
        
        return md;
    }
    
    public byte[] calculateMessageDigest(ByteBuffer buffer) throws IOException
    {
        MessageDigest md = newSha1MessageDigest();
        md.update(buffer);
        return md.digest();
    }
    
    public byte[] calculateMessageDigest(byte[] buffer) throws IOException
    {
        MessageDigest md = newSha1MessageDigest();
        md.update(buffer);
        return md.digest();
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
    
    private byte[] createPlane(int planeSize, byte planeNo)
    {
        byte[] plane = new byte[planeSize];
        
        for (int i = 0; i < planeSize; i++)
            plane[i] = planeNo;
        
        return plane;
    }
    
    /**
    * Convenience method to convert a byte to a hex string.
    *
    * @param data the byte to convert
    * @return String the converted byte
    */
    public String byteToHex(byte data)
    {
        StringBuffer buf = new StringBuffer();
        buf.append(toHexChar((data >>> 4) & 0x0F));
        buf.append(toHexChar(data & 0x0F));
        return buf.toString();
    }
    
    /**
    * Convenience method to convert an int to a hex char.
    *
    * @param i the int to convert
    * @return char the converted char
    */
    public char toHexChar(int i)
    {
        if ((0 <= i) && (i <= 9))
            return (char) ('0' + i);
        else
            return (char) ('a' + (i - 10));
    }
    
    /**
    * Convenience method to convert a byte array to a hex string.
    *
    * @param data the byte[] to convert
    * @return String the converted byte[]
    */
    public String bytesToHex(byte[] data) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < data.length; i++)
        {
            buf.append(byteToHex(data[i]));
        }
        return (buf.toString());
    }
    
    private void createPlanes() throws IOException
    {
        Integer planeCount = pixels.getSizeZ() * pixels.getSizeC() *
                             pixels.getSizeT();
        // FIXME: *Hack* right now we assume everything is 16-bits wide
        Integer planeSize  = pixels.getSizeX() * pixels.getSizeY() *
                             2;
        String  path = ome.io.nio.Helper.getPixelsPath(pixels.getId());
        originalDigests = new byte[planeCount][];
        
        FileOutputStream stream = new FileOutputStream(path);
        
        for (int i = 0; i < planeCount; i++)
        {
            byte[] plane = createPlane(planeSize.intValue(), (byte)(i - 128));
            originalDigests[i] = calculateMessageDigest(plane);
            System.out.println("SHA1: " + bytesToHex(originalDigests[i]));
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

        byte[] messageDigest = calculateMessageDigest(plane);
        
        System.out.println("Size: " + plane);
        
        FileOutputStream stream = new FileOutputStream("/tmp/file");
        stream.getChannel().write(plane);
        

        assertEquals(bytesToHex(originalDigests[0]), bytesToHex(messageDigest));
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
