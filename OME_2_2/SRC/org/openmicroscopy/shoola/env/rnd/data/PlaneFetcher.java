/*
 * org.openmicroscopy.shoola.env.rnd.data.PlaneFetcher
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
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

package org.openmicroscopy.shoola.env.rnd.data;


//Java imports
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.ds.st.Pixels;
import org.openmicroscopy.is.ImageServerException;
import org.openmicroscopy.shoola.env.data.PixelsService;
import org.openmicroscopy.shoola.util.concur.tasks.Invocation;
import org.openmicroscopy.shoola.util.mem.ReadOnlyByteArray;

/** 
 * Retrieves a XY plane within a stack at a given wavelength and timepoint.
 * Implements the {@link Invocation} interface so that it can be run as a
 * service to retrieve data asynchronously.
 *
 * @see org.openmicroscopy.shoola.util.concur.tasks.Invocation
 * @see org.openmicroscopy.shoola.util.concur.tasks.CmdProcessor
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
class PlaneFetcher
    implements Invocation
{
   
    /** Proxy to <i>OMEIS</i>. */
    private final PixelsService    omeis;
    
    /** Identifies the pixels set. */
    private final Pixels  pixelsID;
    
    /** The section index within the wavelength stack. */
    private final int     z;
    
    /** The wavelength index. */
    private final int     w;
    
    /** The timepoint of the stack we want to retrieve. */
    private final int     t;
    
    /** Size of a wavelength plane within the pixels set. */
    private final int     planeSize;
    
    /** Endianness of the data we're going to retrieve. */
    private final boolean bigEndian;
    
    
    /**
     * Craetes a new instance to an retrieve XY plane within a given wavelength
     * stack.
     * 
     * @param source Proxy to <i>OMEIS</i>.  Mustn't be <code>null</code>.
     * @param pixelsID Identifies the pixels set.  Mustn't be <code>null</code>. 
     * @param z The index of the plane within the stack.
     * @param w The wavelength index.
     * @param t The timepoint of the stack we want to retrieve.
     * @param planeSize The size of a wavelength plane within the pixels set.
     * @param bigEndian Endianness of the data we're going to retrieve.
     */
    PlaneFetcher(PixelsService source, Pixels pixelsID, 
            int z, int w, int t, int planeSize, boolean bigEndian)
    {
        if (source == null) throw new NullPointerException("No source.");
        if (pixelsID == null) throw new NullPointerException("No pixelsID.");
        omeis = source;
        this.pixelsID = pixelsID;
        this.z = z;
        this.w = w;
        this.t = t;
        this.planeSize = planeSize;
        this.bigEndian = bigEndian;
    }
    
    /**
     * Fetches the plane data.
     * 
     * @return A byte array containing the plane data.
     * @throws DataSourceException If an error occurs during the retrieval
     *                              of the data. 
     */
    byte[] retrieveXYPlane()
        throws DataSourceException
    {
        byte[] buf = null;
        try {
//long start = System.currentTimeMillis();
            buf = omeis.getPlane(pixelsID, z, w, t, bigEndian);
//buf = retrieveXYPlane2();
//System.out.println("Plane"+z+"data retrived in: "+(System.currentTimeMillis()-start));
            if (buf.length != planeSize)  //Should never happen.
                throw new DataSourceException("The nominal size of the plane ("+
                    planeSize+" bytes) at (z="+z+", w="+w+", t="+t+
                    ") differs from the actual size ("+buf.length+").");
        } catch (ImageServerException ise) {
            throw new DataSourceException("Can't retrieve pixels data.", ise);
        }
        return buf;
    }
    
    /**
     * Fetches the plane data and wraps it into a {@link ReadOnlyByteArray}.
     * 
     * @return A {@link ReadOnlyByteArray} object that wraps the actual plane
     *          data.
     * @throws DataSourceException If an error occurs during the retrieval
     *                              of the data. 
     * @see Invocation#call() 
     */
    public Object call() 
        throws Exception
    {
        byte[] buf = retrieveXYPlane();
        return new ReadOnlyByteArray(buf, 0, planeSize);
    }
    
    /*
    private void writeRequest(OutputStream out) 
    {
        PrintWriter request = new PrintWriter(out);
        //request.println("SessionKey=NA");
        request.println("Method=GetPlane");
        request.println("&PixelsID="+pixelsID.getImageServerID());
        request.println("&theZ="+z);
        request.println("&theC="+w);
        request.println("&theT="+t);
        request.println("&BigEndian="+(bigEndian ? 1 : 0));
        request.close();  //Will close out as well.
    }
    
    private URL buildURL(String omeisURL) 
        throws DataSourceException 
    {
        StringBuffer buf = new StringBuffer(omeisURL);
        buf.append("?Method=GetPlane");
        //TODO: &SessionKey=..
        buf.append("&PixelsID=");
        buf.append(pixelsID.getImageServerID());
        buf.append("&theZ=");
        buf.append(z);
        buf.append("&theC=");
        buf.append(w);
        buf.append("&theT=");
        buf.append(t);
        buf.append("&BigEndian=");
        buf.append(bigEndian ? 1 : 0);
        try {
            return new URL(buf.toString());
        } catch (MalformedURLException mue) {
            throw new DataSourceException(
                    "Can't retrieve pixels "+pixelsID.getID()+" data. ("+
                    "Malformed OMEIS url: "+omeisURL+".)",
                                            mue);
        }   
    }
    
    private void checkResponse(URLConnection connection)
        throws DataSourceException
    {
        String contentType = connection.getContentType();
        int contentLength = connection.getContentLength();
        System.out.println("Content-Type: "+contentType);
        System.out.println("Content-Length: "+contentLength);
        
        if (contentType != "application/octet-stream")
            throw new DataSourceException(
                    "Can't retrieve pixels "+pixelsID.getID()+" data. "+
                    "(OMEIS internal error.)");  //Should check status code.
        if (contentLength != planeSize)
            throw new DataSourceException("The nominal size of the plane ("+
                    planeSize+" bytes) at (z="+z+", w="+w+", t="+t+
                    ") differs from the actual size ("+contentLength+").");
        
    }
    
    private byte[] readResponse(InputStream in)
        throws DataSourceException
    {
        byte[] buf = new byte[planeSize];
System.out.println("PLANE: "+planeSize);
        int bytesWritten = 0, writeLength;
        try {
            while ((writeLength = in.read(buf, bytesWritten, 4096)) > 0) {
            System.out.println("WL: "+writeLength+"    BW: "+bytesWritten);
                bytesWritten += writeLength;
            } 
        } catch (IOException ioe) {
            throw new DataSourceException(
                    "Can't retrieve pixels "+pixelsID.getID()+" data. ("+
                    "Failed to read response.)", ioe);
        } finally {
            try {
                in.close();
            } catch (IOException e) {}
        }
        return buf;
    }
    
    private byte[] retrieveXYPlane2()
        throws DataSourceException
    {
        String omeisURL = pixelsID.getRepository().getImageServerURL();
        URL omeis = buildURL(omeisURL);
        try {
            InputStream responseBody = omeis.openStream();
            //NOTE: openStream will throw IO if HTTP error code, so 
            //we don't need to check that.  However, check the rest.
            //checkResponse(connection);
            return readResponse(responseBody);
        } catch (IOException ioe) {
            throw new DataSourceException(
                    "Can't retrieve pixels "+pixelsID.getID()+" data. ("+
                    "Failed to connect to OMEIS at "+omeisURL+".)", ioe);
        }
    }
    
    private byte[] retrieveXYPlane1()
        throws DataSourceException
    {
        String omeisURL = pixelsID.getRepository().getImageServerURL();
        try {
            URL omeis = new URL(omeisURL);
            URLConnection connection = omeis.openConnection();
            connection.setDoOutput(true);
            writeRequest(connection.getOutputStream());
            //checkResponse(connection);
            return readResponse(connection.getInputStream());
            //NOTE: getInputStream will throw IO if HTTP error code, so 
            //we don't need to check that.
        } catch (MalformedURLException mue) {
            throw new DataSourceException(
                    "Can't retrieve pixels "+pixelsID.getID()+" data. ("+
                    "Malformed OMEIS url: "+omeisURL+".)",
                                            mue);
        } catch (IOException ioe) {
            throw new DataSourceException(
                    "Can't retrieve pixels "+pixelsID.getID()+" data. ("+
                    "Failed to connect to OMEIS at "+omeisURL+".)", ioe);
        }
    }
    */
    
}
