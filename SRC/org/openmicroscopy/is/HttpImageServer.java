/*
 * org.openmicroscopy.is.HttpImageServer
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 Open Microscopy Environment
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




/*------------------------------------------------------------------------------
 *
 * Written by:    Douglas Creager <dcreager@alum.mit.edu>
 *
 *------------------------------------------------------------------------------
 */




package org.openmicroscopy.is;

import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.StringTokenizer;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.MultipartPostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.ByteArrayPartSource;

public class HttpImageServer
    extends ImageServer
{
    protected String               url;
    protected MultipartPostMethod  post;
    protected HttpClient           client;

    HttpImageServer(String url)
    {
        super();
        setURL(url);
    }

    protected void setURL(String url)
    {
        this.url = url;
        this.client = createHttpClient();
        this.post = null;
    }

    protected HttpClient createHttpClient()
    {
        HttpClient client = new HttpClient();
        client.setConnectionTimeout(10000);
        return client;
    }

    protected MultipartPostMethod createPostMethod()
    {
        MultipartPostMethod post = new MultipartPostMethod(url);
        post.setUseExpectHeader(false);
        return post;
    }

    protected void startCall()
    {
        post = createPostMethod();
    }

    protected void executeCall()
        throws ImageServerException
    {
        int status = 0;

        try
        {
            status = client.executeMethod(post);
        } catch (IOException e) {
            e.printStackTrace();
            throw new ImageServerException(e.getMessage());
        }

        if (status != HttpStatus.SC_OK)
        {
            throw new ImageServerException(HttpStatus.getStatusText(status));
        }
    }

    protected void finishCall()
    {
        post.releaseConnection();
        post = null;
    }

    protected void checkToken(StringTokenizer token, String correctValue)
        throws ImageServerException
    {
        String realValue = token.nextToken();
        if (realValue == null || !realValue.equals(correctValue))
            throw new ImageServerException("Invalid response: expected "+
                                           correctValue+
                                           ", got "+
                                           realValue);
    }

    protected void checkToken(StringTokenizer token, String correctValue,
                              String newDelim)
        throws ImageServerException
    {
        String realValue = token.nextToken(newDelim);
        if (realValue == null || !realValue.equals(correctValue))
            throw new ImageServerException("Invalid response: expected "+
                                           correctValue+
                                           ", got "+
                                           realValue);
    }

    protected int getIntToken(StringTokenizer token)
        throws ImageServerException
    {
        String value = token.nextToken();
        try
        {
            int intValue = Integer.parseInt(value);
            return intValue;
        } catch (NumberFormatException e) {
            throw new ImageServerException("Invalid response: expected number, got "+
                                           value);
        }
    }

    protected long getLongToken(StringTokenizer token)
        throws ImageServerException
    {
        String value = token.nextToken();
        try
        {
            long longValue = Long.parseLong(value);
            return longValue;
        } catch (NumberFormatException e) {
            throw new ImageServerException("Invalid response: expected number, got "+
                                           value);
        }
    }

    protected double getDoubleToken(StringTokenizer token)
        throws ImageServerException
    {
        String value = token.nextToken();
        if (value.equalsIgnoreCase("nan")) return Double.NaN;
        try
        {
            double doubleValue = Double.parseDouble(value);
            return doubleValue;
        } catch (NumberFormatException e) {
            throw new ImageServerException("Invalid response: expected number, got "+
                                           value);
        }
    }

    public long newPixels(int sizeX,
                          int sizeY,
                          int sizeZ,
                          int sizeC,
                          int sizeT,
                          int bytesPerPixel,
                          boolean isSigned,
                          boolean isFloat)
        throws ImageServerException
    {
        String dims = 
            sizeX+","+
            sizeY+","+
            sizeZ+","+
            sizeC+","+
            sizeT+","+
            bytesPerPixel;

        startCall();
        try
        {
            post.addParameter("Method","NewPixels");
            post.addParameter("Dims",dims);
            post.addParameter("IsSigned",isSigned? "1": "0");
            post.addParameter("IsFloat",isFloat? "1": "0");
            executeCall();

            return Long.parseLong(post.getResponseBodyAsString().trim());
        } catch (NumberFormatException e) {
            throw new ImageServerException("Illegal response: Invalid pixels ID");
        } finally {
            finishCall();
        }
    }

    public PixelsFileFormat getPixelsInfo(long pixelsID)
        throws ImageServerException
    {
        startCall();
        try
        {
            post.addParameter("Method","PixelsInfo");
            post.addParameter("PixelsID",Long.toString(pixelsID));
            executeCall();

            String result = post.getResponseBodyAsString();
            StringTokenizer token = new StringTokenizer(result,
                                                        "\r\n=,");

            checkToken(token,"Dims");
            int sizeX = getIntToken(token);
            int sizeY = getIntToken(token);
            int sizeZ = getIntToken(token);
            int sizeC = getIntToken(token);
            int sizeT = getIntToken(token);
            int bytesPerPixel = getIntToken(token);
            checkToken(token,"Finished");
            getIntToken(token);
            checkToken(token,"Signed");
            int isSigned = getIntToken(token);
            checkToken(token,"Float");
            int isFloat = getIntToken(token);

            return new PixelsFileFormat(sizeX,sizeY,sizeZ,sizeC,sizeT,
                                        bytesPerPixel,
                                        isSigned == 1,
                                        isFloat == 1);
        } finally {
            finishCall();
        }
    }

    public String getPixelsSHA1(long pixelsID)
        throws ImageServerException
    {
        startCall();
        try
        {
            post.addParameter("Method","PixelsSHA1");
            post.addParameter("PixelsID",Long.toString(pixelsID));
            executeCall();

            return post.getResponseBodyAsString().trim();
        } finally {
            finishCall();
        }
    }

    public String getPixelsServerPath(long pixelsID)
        throws ImageServerException
    {
        startCall();
        try
        {
            post.addParameter("Method","GetLocalPath");
            post.addParameter("PixelsID",Long.toString(pixelsID));
            executeCall();

            return post.getResponseBodyAsString().trim();
        } finally {
            finishCall();
        }
    }

    public boolean isPixelsFinished(long pixelsID)
        throws ImageServerException
    {
        startCall();
        try
        {
            post.addParameter("Method","PixelsInfo");
            post.addParameter("PixelsID",Long.toString(pixelsID));
            executeCall();

            String result = post.getResponseBodyAsString();
            StringTokenizer token = new StringTokenizer(result,
                                                        "\r\n=,");

            checkToken(token,"Dims");
            getIntToken(token);
            getIntToken(token);
            getIntToken(token);
            getIntToken(token);
            getIntToken(token);
            getIntToken(token);
            checkToken(token,"Finished");
            int isFinished = getIntToken(token);
            checkToken(token,"Signed");
            getIntToken(token);
            checkToken(token,"Float");
            getIntToken(token);

            return (isFinished == 1);
        } finally {
            finishCall();
        }
    }

    public byte[] getPixels(long pixelsID,
                            boolean bigEndian)
        throws ImageServerException
    {
        startCall();
        try
        {
            post.addParameter("Method","GetPixels");
            post.addParameter("PixelsID",Long.toString(pixelsID));
            post.addParameter("BigEndian",bigEndian? "1": "0");
            executeCall();

            return post.getResponseBody();
        } finally {
            finishCall();
        }        
    }

    public byte[] getStack(long pixelsID,
                           int theC, int theT,
                           boolean bigEndian)
        throws ImageServerException
    {
        startCall();
        try
        {
            post.addParameter("Method","GetStack");
            post.addParameter("PixelsID",Long.toString(pixelsID));
            post.addParameter("theC",Integer.toString(theC));
            post.addParameter("theT",Integer.toString(theT));
            post.addParameter("BigEndian",bigEndian? "1": "0");
            executeCall();

            return post.getResponseBody();
        } finally {
            finishCall();
        }        
    }

    public byte[] getPlane(long pixelsID,
                           int theZ, int theC, int theT,
                           boolean bigEndian)
        throws ImageServerException
    {
        startCall();
        try
        {
            post.addParameter("Method","GetPlane");
            post.addParameter("PixelsID",Long.toString(pixelsID));
            post.addParameter("theZ",Integer.toString(theZ));
            post.addParameter("theC",Integer.toString(theC));
            post.addParameter("theT",Integer.toString(theT));
            post.addParameter("BigEndian",bigEndian? "1": "0");
            executeCall();

            return post.getResponseBody();
        } finally {
            finishCall();
        }        
    }

    public byte[] getROI(long pixelsID,
                         int x0,int y0,int z0,int c0,int t0,
                         int x1,int y1,int z1,int c1,int t1,
                         boolean bigEndian)
        throws ImageServerException
    {
        String roi =
            x0+","+y0+","+z0+","+c0+","+t0+","+
            x1+","+y1+","+z1+","+c1+","+t1;

        startCall();
        try
        {
            post.addParameter("Method","GetROI");
            post.addParameter("PixelsID",Long.toString(pixelsID));
            post.addParameter("ROI",roi);
            post.addParameter("BigEndian",bigEndian? "1": "0");
            executeCall();

            return post.getResponseBody();
        } finally {
            finishCall();
        }        
    }

    public void setPixels(long pixelsID, byte[] buf, boolean bigEndian)
        throws ImageServerException
    {
        startCall();
        try
        {
            post.addParameter("Method","SetPixels");
            post.addParameter("PixelsID",Long.toString(pixelsID));
            post.addParameter("UploadSize",Long.toString(buf.length));
            post.addParameter("BigEndian",bigEndian? "1": "0");
            post.addPart(new FilePart("Pixels",
                                      new ByteArrayPartSource("pixels",buf)));
            executeCall();

            return;
        } finally {
            finishCall();
        }        
    }

    public void setPixels(long pixelsID, File file, boolean bigEndian)
        throws ImageServerException, FileNotFoundException
    {
        startCall();
        try
        {
            post.addParameter("Method","SetPixels");
            post.addParameter("PixelsID",Long.toString(pixelsID));
            post.addParameter("BigEndian",bigEndian? "1": "0");
            post.addParameter("Pixels",file);
            executeCall();

            return;
        } finally {
            finishCall();
        }        
    }

    public void setStack(long pixelsID,
                         int theC, int theT,
                         byte[] buf, boolean bigEndian)
        throws ImageServerException
    {
        startCall();
        try
        {
            post.addParameter("Method","SetStack");
            post.addParameter("PixelsID",Long.toString(pixelsID));
            post.addParameter("theC",Integer.toString(theC));
            post.addParameter("theT",Integer.toString(theT));
            post.addParameter("UploadSize",Long.toString(buf.length));
            post.addParameter("BigEndian",bigEndian? "1": "0");
            post.addPart(new FilePart("Pixels",
                                      new ByteArrayPartSource("pixels",buf)));
            executeCall();

            return;
        } finally {
            finishCall();
        }        
    }

    public void setStack(long pixelsID,
                         int theC, int theT,
                         File file, boolean bigEndian)
        throws ImageServerException, FileNotFoundException
    {
        startCall();
        try
        {
            post.addParameter("Method","SetStack");
            post.addParameter("PixelsID",Long.toString(pixelsID));
            post.addParameter("theC",Integer.toString(theC));
            post.addParameter("theT",Integer.toString(theT));
            post.addParameter("BigEndian",bigEndian? "1": "0");
            post.addParameter("Pixels",file);
            executeCall();

            return;
        } finally {
            finishCall();
        }        
    }

    public void setPlane(long pixelsID,
                         int theZ, int theC, int theT,
                         byte[] buf, boolean bigEndian)
        throws ImageServerException
    {
        startCall();
        try
        {
            post.addParameter("Method","SetPlane");
            post.addParameter("PixelsID",Long.toString(pixelsID));
            post.addParameter("theZ",Integer.toString(theZ));
            post.addParameter("theC",Integer.toString(theC));
            post.addParameter("theT",Integer.toString(theT));
            post.addParameter("UploadSize",Long.toString(buf.length));
            post.addParameter("BigEndian",bigEndian? "1": "0");
            post.addPart(new FilePart("Pixels",
                                      new ByteArrayPartSource("pixels",buf)));
            executeCall();

            return;
        } finally {
            finishCall();
        }        
    }

    public void setPlane(long pixelsID,
                         int theZ, int theC, int theT,
                         File file, boolean bigEndian)
        throws ImageServerException, FileNotFoundException
    {
        startCall();
        try
        {
            post.addParameter("Method","SetPlane");
            post.addParameter("PixelsID",Long.toString(pixelsID));
            post.addParameter("theZ",Integer.toString(theZ));
            post.addParameter("theC",Integer.toString(theC));
            post.addParameter("theT",Integer.toString(theT));
            post.addParameter("BigEndian",bigEndian? "1": "0");
            post.addParameter("Pixels",file);
            executeCall();

            return;
        } finally {
            finishCall();
        }        
    }

    public void setROI(long pixelsID,
                       int x0,int y0,int z0,int c0,int t0,
                       int x1,int y1,int z1,int c1,int t1,
                       byte[] buf, boolean bigEndian)
        throws ImageServerException
    {
        String roi =
            x0+","+y0+","+z0+","+c0+","+t0+","+
            x1+","+y1+","+z1+","+c1+","+t1;

        startCall();
        try
        {
            post.addParameter("Method","SetROI");
            post.addParameter("PixelsID",Long.toString(pixelsID));
            post.addParameter("ROI",roi);
            post.addParameter("UploadSize",Long.toString(buf.length));
            post.addParameter("BigEndian",bigEndian? "1": "0");
            post.addPart(new FilePart("Pixels",
                                      new ByteArrayPartSource("pixels",buf)));
            executeCall();

            return;
        } finally {
            finishCall();
        }        
    }

    public void setROI(long pixelsID,
                       int x0,int y0,int z0,int c0,int t0,
                       int x1,int y1,int z1,int c1,int t1,
                       File file, boolean bigEndian)
        throws ImageServerException, FileNotFoundException
    {
        String roi =
            x0+","+y0+","+z0+","+c0+","+t0+","+
            x1+","+y1+","+z1+","+c1+","+t1;

        startCall();
        try
        {
            post.addParameter("Method","SetROI");
            post.addParameter("PixelsID",Long.toString(pixelsID));
            post.addParameter("ROI",roi);
            post.addParameter("BigEndian",bigEndian? "1": "0");
            post.addParameter("Pixels",file);
            executeCall();

            return;
        } finally {
            finishCall();
        }        
    }

    public void finishPixels(long pixelsID)
        throws ImageServerException
    {
        startCall();
        try
        {
            post.addParameter("Method","FinishPixels");
            post.addParameter("PixelsID",Long.toString(pixelsID));
            executeCall();

            return;
        } finally {
            finishCall();
        }        
    }

    public long uploadFile(File file)
        throws ImageServerException, FileNotFoundException
    {
        startCall();
        try
        {
            post.addParameter("Method","UploadFile");
            post.addParameter("File",file);
            executeCall();

            return Long.parseLong(post.getResponseBodyAsString().trim());
        } catch (NumberFormatException e) {
            throw new ImageServerException("Illegal response: Invalid file ID");
        } finally {
            finishCall();
        }        
    }

    public FileInfo getFileInfo(long fileID)
        throws ImageServerException
    {
        startCall();
        try
        {
            post.addParameter("Method","FileInfo");
            post.addParameter("FileID",Long.toString(fileID));
            executeCall();
 
            String result = post.getResponseBodyAsString();
            StringTokenizer token = new StringTokenizer(result,
                                                        "\r\n=,");

            checkToken(token,"Name");

            // Just for the filename token, ignore "=" and "," as
            // delimeters.  Also, there will be a leading "=" that we
            // need to remove.
            String name = token.nextToken("\r\n").substring(1);

            // Set the delimeters back
            checkToken(token,"Length","\r\n=,");
            long length = getLongToken(token);

            return new FileInfo(name,length);
       } finally {
            finishCall();
        }
    }

    public String getFileSHA1(long fileID)
        throws ImageServerException
    {
        startCall();
        try
        {
            post.addParameter("Method","FileSHA1");
            post.addParameter("FileID",Long.toString(fileID));
            executeCall();

            return post.getResponseBodyAsString().trim();
        } finally {
            finishCall();
        }
    }

    public String getFileServerPath(long fileID)
        throws ImageServerException
    {
        startCall();
        try
        {
            post.addParameter("Method","GetLocalPath");
            post.addParameter("FileID",Long.toString(fileID));
            executeCall();

            return post.getResponseBodyAsString().trim();
        } finally {
            finishCall();
        }
    }

    public byte[] readFileWithoutCaching(final long fileID,
                                         final long offset,
                                         final int  length)
        throws ImageServerException
    {
        startCall();
        try
        {
            post.addParameter("Method","ReadFile");
            post.addParameter("FileID",Long.toString(fileID));
            post.addParameter("Offset",Long.toString(offset));
            post.addParameter("Length",Integer.toString(length));
            executeCall();

            return post.getResponseBody();
        } finally {
            finishCall();
        }
    }

    public long convertStack(final long pixelsID,
                             final int theC, final int theT,
                             final long fileID, final long offset,
                             final boolean bigEndian)
        throws ImageServerException
    {
        startCall();
        try
        {
            post.addParameter("Method","ConvertStack");
            post.addParameter("PixelsID",Long.toString(pixelsID));
            post.addParameter("theC",Integer.toString(theC));
            post.addParameter("theT",Integer.toString(theT));
            post.addParameter("FileID",Long.toString(fileID));
            post.addParameter("Offset",Long.toString(offset));
            post.addParameter("BigEndian",bigEndian? "1": "0");
            executeCall();

            return Long.parseLong(post.getResponseBodyAsString().trim());
        } finally {
            finishCall();
        }
    }

    public long convertPlane(final long pixelsID,
                             final int theZ, final int theC, final int theT,
                             final long fileID, final long offset,
                             final boolean bigEndian)
        throws ImageServerException
    {
        startCall();
        try
        {
            post.addParameter("Method","ConvertPlane");
            post.addParameter("PixelsID",Long.toString(pixelsID));
            post.addParameter("theZ",Integer.toString(theZ));
            post.addParameter("theC",Integer.toString(theC));
            post.addParameter("theT",Integer.toString(theT));
            post.addParameter("FileID",Long.toString(fileID));
            post.addParameter("Offset",Long.toString(offset));
            post.addParameter("BigEndian",bigEndian? "1": "0");
            executeCall();

            return Long.parseLong(post.getResponseBodyAsString().trim());
        } finally {
            finishCall();
        }
    }

    public long convertRows(final long pixelsID,
                            final int theY, final int numRows,
                            final int theZ, final int theC, final int theT,
                            final long fileID, final long offset,
                            final boolean bigEndian)
        throws ImageServerException
    {
        startCall();
        try
        {
            post.addParameter("Method","ConvertRows");
            post.addParameter("PixelsID",Long.toString(pixelsID));
            post.addParameter("theY",Integer.toString(theY));
            post.addParameter("nRows",Integer.toString(numRows));
            post.addParameter("theZ",Integer.toString(theZ));
            post.addParameter("theC",Integer.toString(theC));
            post.addParameter("theT",Integer.toString(theT));
            post.addParameter("FileID",Long.toString(fileID));
            post.addParameter("Offset",Long.toString(offset));
            post.addParameter("BigEndian",bigEndian? "1": "0");
            executeCall();

            return Long.parseLong(post.getResponseBodyAsString().trim());
        } finally {
            finishCall();
        }
    }

    public PlaneStatistics getPlaneStatistics(long pixelsID)
        throws ImageServerException
    {
        PixelsFileFormat pff = getPixelsInfo(pixelsID);
        int sizeZ = pff.getSizeZ();
        int sizeC = pff.getSizeC();
        int sizeT = pff.getSizeT();

        startCall();
        try
        {
            post.addParameter("Method","GetPlaneStats");
            post.addParameter("PixelsID",Long.toString(pixelsID));
            executeCall();

            double[][][]
                minimum = new double[sizeZ][sizeC][sizeT],
                maximum = new double[sizeZ][sizeC][sizeT],
                mean = new double[sizeZ][sizeC][sizeT],
                sigma = new double[sizeZ][sizeC][sizeT],
                geomean = new double[sizeZ][sizeC][sizeT],
                geosigma = new double[sizeZ][sizeC][sizeT],
                centroidX = new double[sizeZ][sizeC][sizeT],
                centroidY = new double[sizeZ][sizeC][sizeT],
                sumI = new double[sizeZ][sizeC][sizeT],
                sumI2 = new double[sizeZ][sizeC][sizeT],
                sumLogI = new double[sizeZ][sizeC][sizeT],
                sumXI = new double[sizeZ][sizeC][sizeT],
                sumYI = new double[sizeZ][sizeC][sizeT],
                sumZI = new double[sizeZ][sizeC][sizeT];

            String result = post.getResponseBodyAsString();
            StringTokenizer rtoken = new StringTokenizer(result,"\r\n");

            while (rtoken.hasMoreTokens())
            {
                String line = rtoken.nextToken();
                StringTokenizer ltoken = new StringTokenizer(line,"\t");

                int theC = getIntToken(ltoken);
                int theT = getIntToken(ltoken);
                int theZ = getIntToken(ltoken);
                minimum[theZ][theC][theT] = getDoubleToken(ltoken);
                maximum[theZ][theC][theT] = getDoubleToken(ltoken);
                mean[theZ][theC][theT] = getDoubleToken(ltoken);
                geomean[theZ][theC][theT] = getDoubleToken(ltoken);
                geosigma[theZ][theC][theT] = getDoubleToken(ltoken);
                centroidX[theZ][theC][theT] = getDoubleToken(ltoken);
                centroidY[theZ][theC][theT] = getDoubleToken(ltoken);
                sumI[theZ][theC][theT] = getDoubleToken(ltoken);
                sumI2[theZ][theC][theT] = getDoubleToken(ltoken);
                sumLogI[theZ][theC][theT] = getDoubleToken(ltoken);
                sumXI[theZ][theC][theT] = getDoubleToken(ltoken);
                sumYI[theZ][theC][theT] = getDoubleToken(ltoken);
                sumZI[theZ][theC][theT] = getDoubleToken(ltoken);
            }

            return new PlaneStatistics(minimum,
                                       maximum,
                                       mean,
                                       sigma,
                                       geomean,
                                       geosigma,
                                       centroidX,
                                       centroidY,
                                       sumI,
                                       sumI2,
                                       sumLogI,
                                       sumXI,
                                       sumYI,
                                       sumZI);
        } finally {
            finishCall();
        }
    }

    public StackStatistics getStackStatistics(long pixelsID)
        throws ImageServerException
    {
        PixelsFileFormat pff = getPixelsInfo(pixelsID);
        int sizeC = pff.getSizeC();
        int sizeT = pff.getSizeT();

        startCall();
        try
        {
            post.addParameter("Method","GetStackStats");
            post.addParameter("PixelsID",Long.toString(pixelsID));
            executeCall();

            double[][]
                minimum = new double[sizeC][sizeT],
                maximum = new double[sizeC][sizeT],
                mean = new double[sizeC][sizeT],
                sigma = new double[sizeC][sizeT],
                geomean = new double[sizeC][sizeT],
                geosigma = new double[sizeC][sizeT],
                centroidX = new double[sizeC][sizeT],
                centroidY = new double[sizeC][sizeT],
                centroidZ = new double[sizeC][sizeT],
                sumI = new double[sizeC][sizeT],
                sumI2 = new double[sizeC][sizeT],
                sumLogI = new double[sizeC][sizeT],
                sumXI = new double[sizeC][sizeT],
                sumYI = new double[sizeC][sizeT],
                sumZI = new double[sizeC][sizeT];

            String result = post.getResponseBodyAsString();
            StringTokenizer rtoken = new StringTokenizer(result,"\r\n");

            while (rtoken.hasMoreTokens())
            {
                String line = rtoken.nextToken();
                StringTokenizer ltoken = new StringTokenizer(line,"\t");

                int theC = getIntToken(ltoken);
                int theT = getIntToken(ltoken);
                int theZ = getIntToken(ltoken);
                minimum[theC][theT] = getDoubleToken(ltoken);
                maximum[theC][theT] = getDoubleToken(ltoken);
                mean[theC][theT] = getDoubleToken(ltoken);
                geomean[theC][theT] = getDoubleToken(ltoken);
                geosigma[theC][theT] = getDoubleToken(ltoken);
                centroidX[theC][theT] = getDoubleToken(ltoken);
                centroidY[theC][theT] = getDoubleToken(ltoken);
                centroidZ[theC][theT] = getDoubleToken(ltoken);
                sumI[theC][theT] = getDoubleToken(ltoken);
                sumI2[theC][theT] = getDoubleToken(ltoken);
                sumLogI[theC][theT] = getDoubleToken(ltoken);
                sumXI[theC][theT] = getDoubleToken(ltoken);
                sumYI[theC][theT] = getDoubleToken(ltoken);
                sumZI[theC][theT] = getDoubleToken(ltoken);
            }

            return new StackStatistics(minimum,
                                       maximum,
                                       mean,
                                       sigma,
                                       geomean,
                                       geosigma,
                                       centroidX,
                                       centroidY,
                                       centroidZ,
                                       sumI,
                                       sumI2,
                                       sumLogI,
                                       sumXI,
                                       sumYI,
                                       sumZI);
        } finally {
            finishCall();
        }
    }

}