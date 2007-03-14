/*
 * ome.formats.testclient.ImportLibrary
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2005 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *------------------------------------------------------------------------------
 */

package ome.formats.importer;

// Java imports
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import loci.formats.FormatException;
import loci.formats.FormatTools;
import loci.formats.in.Bits;
import ome.conditions.ApiUsageException;
import ome.formats.OMEROMetadataStore;
import ome.model.containers.Dataset;
import ome.model.core.Pixels;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * support class for the proper usage of {@link OMEROMetadataStore} and
 * {@link FormatReader} instances. This library was factored out of
 * {@link ImportHandler} to support {@link ImportFixture} The general workflow
 * for this class (as seen in {@link ImportFixture} is: <code>
 *   ImportLibrary library = new ImportLibrary(store,reader,files);
 *   for (File file : files) {
 *     String fileName = file.getAbsolutePath();
 *     library.open(fileName);
 *     int count = library.calculateImageCount(fileName);
 *     long pixId = library.importMetadata();
 *     library.importData(pixId, fileName, new ImportLibrary.Step(){
 *       public void step(int i) {}});
 *   }
 * </code>
 * 
 * @author Josh Moore, josh.moore at gmx.de
 * @version $Revision: 1167 $, $Date: 2006-12-15 10:39:34 +0000 (Fri, 15 Dec 2006) $
 * @see FormatReader
 * @see OMEROMetadataStore
 * @see ImportHandler
 * @see ImportFixture
 * @since 3.0-M3
 */
// @RevisionDate("$Date: 2006-12-15 10:39:34 +0000 (Fri, 15 Dec 2006) $")
// @RevisionNumber("$Revision: 1167 $")
public class ImportLibrary
{

    /**
     * simple action class to be used during
     * {@link ImportLibrary#importData(long, String, ome.formats.testclient.ImportLibrary.Step)}
     */
    public abstract static class Step
    {

        public abstract void step(int n);
    }

    private static Log         log = LogFactory.getLog(ImportLibrary.class);

    private Dataset            dataset;

    private OMEROMetadataStore store;

    private OMEROWrapper       reader;

    private ImportContainer[]  fads;

    private int                sizeZ;

    private int                sizeT;

    private int                sizeC;
    
    private int                sizeX;
    
    private int                sizeY;

    private int                zSize;

    private int                tSize;

    private int                wSize;

    /**
     * Note: {@link #setDataset(String)} must be properly invoked before
     * {@link #importMetadata()} can be used.
     * 
     * @param store not null
     * @param reader not null
     * @param fads2 not null, length > 0
     */
    public ImportLibrary(OMEROMetadataStore store, OMEROWrapper reader,
            ImportContainer[] fads)
    {

        if (store == null || reader == null || fads == null
                || fads.length == 0)
        {
            throw new ApiUsageException(
                    "All arguments to ImportLibrary() must be non-null.");
        }
        this.store = store;
        this.reader = reader;
        this.fads = fads;
    }

    /**
     * sets the dataset to which images will be imported. Must be called before
     * {@link #importMetadata()}
     * 
     * @param dataset Not null.
     * @throws ApiUsageException if datasetName is null.
     */
    public void setDataset(Dataset dataset)
    {
        if (dataset == null)
            throw new ApiUsageException("Dataset name cannot be null.");
        this.dataset = dataset;
    }

    
    // ~ Getters
    // =========================================================================

    /** simpler getter. Checks if dataset is still null */
    public Dataset getDataset()
    {
        if (this.dataset == null)
            throw new ApiUsageException(
                    "The dataset has not been set. Please call setDataset(String).");

        return this.dataset;
    }

    /** simpler getter for {@link #files} */
    public ImportContainer[] getFilesAndDatasets()
    {
        return fads;
    }

    /** gets {@link Pixels} instance from {@link OMEROMetadataStore} */
    public Pixels getRoot()
    {
        return (Pixels) store.getRoot();
    }

    // ~ Actions
    // =========================================================================


    /** opens the file using the {@link FormatReader} instance */
    public void open(String fileName) throws IOException, FormatException
    {
        reader.close();
        reader.setMetadataStore(store);
        log.debug("Image Count: " + reader.getImageCount(fileName));
    }

    /**
     * calculates and returns the number of planes in this image. Also sets the
     * offset info.
     * 
     * @param fileName filename for use in {@link #setOffsetInfo(String)}
     * @return the number of planes in this image (z * c * t)
     */
    public int calculateImageCount(String fileName)
    {
        Pixels pixels = getRoot();
        this.sizeZ = pixels.getSizeZ().intValue();
        this.sizeC = pixels.getSizeC().intValue();
        this.sizeT = pixels.getSizeT().intValue();
        this.sizeX = pixels.getSizeX().intValue();
        this.sizeY = pixels.getSizeY().intValue();
        int imageCount = sizeZ * sizeC * sizeT;
        setOffsetInfo(fileName);
        return imageCount;
    }

    /**
     * uses the {@link OMEROMetadataStore} to save the current {@link Pixels} to
     * the database.
     * 
     * @return the newly created {@link Pixels} id.
	 * @throws FormatException if there is an error parsing metadata.
	 * @throws IOException if there is an error reading the file.
     */
    public long importMetadata(String imageName)
    	throws FormatException, IOException
    {
        Pixels p = (Pixels) store.getRoot();
        p.getImage().setName(imageName);
        // Ensure that our metadata is consistent before writing to the DB.
        reader.finalizeMetadataStore(imageName);
        Long pixId = store.saveToDB();
        store.addPixelsToDataset(pixId, dataset);
        return pixId;
    }

    /**
     * Retrieves how many bytes per pixel the current plane or section has.
     * @return the number of bytes per pixel.
     */
    private int getBytesPerPixel(int type) {
      switch(type) {
      case 0:
      case 1:
        return 1;  // INT8 or UINT8
      case 2:
      case 3:
        return 2;  // INT16 or UINT16
      case 4:
      case 5:
      case 6:
        return 4;  // INT32, UINT32 or FLOAT
      case 7:
        return 8;  // DOUBLE
      }
      throw new RuntimeException("Unknown type with id: '" + type + "'");
    }

    
    /**
     * saves the binary data to the server. After each successful save,
     * {@link Step#step(int)} is called with the number of the iteration just
     * completed.
     */
    public void importData(long pixId, String fileName, Step step)
    {
        int i = 1;
        try {
            int bytesPerPixel = getBytesPerPixel(reader.getPixelType(fileName));
            byte[] arrayBuf = new byte[sizeX * sizeY * bytesPerPixel];

            for (int t = 0; t < sizeT; t++)
            {
                for (int c = 0; c < sizeC; c++)
                {
                    for (int z = 0; z < sizeZ; z++)
                    {
                        int planeNumber = getTotalOffset(z, c, t);
                        ByteBuffer buf =
                        	reader.openPlane2D(fileName, planeNumber,
                        			           arrayBuf).getData();
                        arrayBuf = swapIfRequired(buf, fileName);
                        step.step(i);
                        store.setPlane(pixId, arrayBuf, z, c, t);
                        i++;
                    }
                }
            }
        } catch (FormatException e)
        {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            log.info(sw);
            return;
        } catch (IOException e)
        {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            log.info(sw);
            return;
        }
    }
    
    // ~ Helpers
    // =========================================================================

    private void setOffsetInfo(String fileName)
    {
        int order = 0;
        try
        {
            order = getSequenceNumber(reader.getDimensionOrder(fileName));
        } catch (FormatException e)
        {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            log.info(sw);
        } catch (IOException e)
        {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            log.info(sw);
        }

        setOffsetInfo(order, sizeZ, sizeC, sizeT);
    }

    /**
     * This method calculates the size of a w, t, z section depending on which
     * sequence is being used (either ZTW, WZT, or ZWT)
     * 
     * @param imgSequence
     * @param numZSections
     * @param numWaves
     * @param numTimes
     */
    private void setOffsetInfo(int imgSequence, int numZSections, int numWaves,
            int numTimes)
    {
        int smallOffset = 1;
        switch (imgSequence)
        {
            // ZTW sequence
            case 0:
                zSize = smallOffset;
                tSize = zSize * numZSections;
                wSize = tSize * numTimes;
                break;

            // WZT sequence
            case 1:
                wSize = smallOffset;
                zSize = wSize * numWaves;
                tSize = zSize * numZSections;
                break;

            // ZWT sequence
            case 2:
                zSize = smallOffset;
                wSize = zSize * numZSections;
                tSize = wSize * numWaves;
                break;
        }
    }

    /**
     * Given any specific Z, W, and T, determine the totalOffset from the start
     * of the file
     * 
     * @param currentZ
     * @param currentW
     * @param currentT
     * @return
     */
    private int getTotalOffset(int currentZ, int currentW, int currentT)
    {
        return (zSize * currentZ) + (wSize * currentW) + (tSize * currentT);
    }

    private int getSequenceNumber(String dimOrder)
    {
        if (dimOrder.equals("XYZTC")) return 0;
        if (dimOrder.equals("XYCZT")) return 1;
        if (dimOrder.equals("XYZCT")) return 2;
        throw new RuntimeException();
    }
    
    /** Return true if the data is in little-endian format. 
     * @throws IOException 
     * @throws FormatException */
    public boolean isLittleEndian(String fileName) throws FormatException, IOException {
      return reader.isLittleEndian(fileName);
    }
    
    /**
     * Examines a byte array to see if it needs to be byte swapped and modifies
     * the byte array directly.
     * @param byteArray The byte array to check and modify if required.
     * @return the <i>byteArray</i> either swapped or not for convenience.
     * @throws IOException if there is an error read from the file.
     * @throws FormatException if there is an error during metadata parsing.
     */
    private byte[] swapIfRequired(ByteBuffer buffer, String fileName)
      throws FormatException, IOException
    {
      int pixelType = reader.getPixelType(fileName);
      int bytesPerPixel = getBytesPerPixel(pixelType);

      // We've got nothing to do if the samples are only 8-bits wide or if they
      // are floating point.
      if (pixelType == FormatTools.FLOAT || pixelType == FormatTools.DOUBLE
                  || bytesPerPixel == 1) 
          return buffer.array();

      //System.err.println(fileName + " is Little Endian: " + isLittleEndian(fileName));
      if (isLittleEndian(fileName)) {
        if (bytesPerPixel == 2) { // short
          ShortBuffer buf = buffer.asShortBuffer();
          for (int i = 0; i < (buffer.capacity() / 2); i++) {
          //short tmp = buf.get(i);
            buf.put(i, Bits.swap(buf.get(i)));
          //if (tmp == 21253 || buf.get(i) == 21253) 
          //  {
                //System.err.println(tmp + " -> " + buf.get(i));
          //}
          }
        } else if (bytesPerPixel == 4) { // int/uint
            IntBuffer buf = buffer.asIntBuffer();
            for (int i = 0; i < (buffer.capacity() / 4); i++) {
              buf.put(i, Bits.swap(buf.get(i)));
            }
        } else {
          throw new FormatException(
            "Unsupported sample bit width: '" + bytesPerPixel + "'");
        }
      }
      // We've got a big-endian file with a big-endian byte array.
      return buffer.array();
    }
}
