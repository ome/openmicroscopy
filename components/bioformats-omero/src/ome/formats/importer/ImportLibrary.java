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
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import loci.formats.FormatException;
import loci.formats.FormatTools;
import loci.formats.DataTools;
import ome.conditions.ApiUsageException;
import ome.formats.OMEROMetadataStore;
import ome.model.annotations.BooleanAnnotation;
import ome.model.containers.Dataset;
import ome.model.core.Image;
import ome.model.core.Pixels;
import ome.model.core.PixelsDimensions;
import ome.model.display.Color;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ome.formats.importer.util.Actions;

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
public class ImportLibrary implements IObservable
{

    
    ArrayList<IObserver> observers = new ArrayList<IObserver>();
    
    /**
     * simple action class to be used during
     * {@link ImportLibrary#importData(long, String, ome.formats.testclient.ImportLibrary.Step)}
     */
    public abstract static class Step
    {

        public abstract void step(int series, int step);
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
    public ImportLibrary(OMEROMetadataStore store, OMEROWrapper reader)
    {
        this.store = store;
        this.reader = reader;

        if (store == null || reader == null)
        {
            throw new ApiUsageException(
                    "All arguments to ImportLibrary() must be non-null.");
        }
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

    /** gets {@link Image} instance from {@link OMEROMetadataStore} */
    @SuppressWarnings("unchecked")
	public List<Image> getRoot()
    {
        return (List<Image>) store.getRoot();
    }

    // ~ Actions
    // =========================================================================


    /** opens the file using the {@link FormatReader} instance */
    public void open(String fileName) throws IOException, FormatException
    {
        reader.close();
        reader.setMetadataStore(store);
        reader.setId(fileName);
        //reset series count
        log.debug("Image Count: " + reader.getImageCount());
    }

    /**
     * calculates and returns the number of planes in this image. Also sets the
     * offset info.
     * 
     * @param fileName filename for use in {@link #setOffsetInfo(String)}
     * @return the number of planes in this image (z * c * t)
     */
    public int calculateImageCount(String fileName, Integer series)
    {
        List<Image> imageList = getRoot();
        // FIXME: This assumes only *one* Pixels.
        Pixels pixels = (Pixels) imageList.get(series).iteratePixels().next();
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
    @SuppressWarnings("unchecked")
	public List<Pixels> importMetadata(String imageName)
    	throws FormatException, IOException
    {
        List<Image> imageList = (List<Image>) store.getRoot();
        // Ensure that our metadata is consistent before writing to the DB.
        int series = 0;
        for (Image image : imageList)
        {
        	// FIXME: This assumes only *one* set of pixels.
        	Pixels pix = (Pixels) image.iteratePixels().next();
            String name = imageName;
            String seriesName = reader.getImageName(series);
            
            if (reader.getImageReader().isRGB())
            {
                log.debug("Setting color channels to RGB format.");
                if (pix.sizeOfChannels() == 3)
                {
                    Color red = new Color();
                    red.setRed(255);
                    red.setGreen(0);
                    red.setBlue(0);
                    red.setAlpha(255);
                    
                    Color green = new Color();
                    green.setGreen(255);
                    green.setRed(0);
                    green.setBlue(0);
                    green.setAlpha(255); 
                    
                    Color blue = new Color();
                    blue.setBlue(255);
                    blue.setGreen(0);
                    blue.setRed(0);
                    blue.setAlpha(255);            
                    
                    pix.getChannel(0).setColorComponent(red);
                    pix.getChannel(1).setColorComponent(green);
                    pix.getChannel(2).setColorComponent(blue);
                }
                
            }

            if (seriesName != null && seriesName.length() != 0)
                name += " [" + seriesName + "]";

            pix.getImage().setName(name);
            if (pix.getPixelsDimensions() == null)
            {   
                PixelsDimensions pixDims = new PixelsDimensions();
                pixDims.setSizeX(1.0f);
                pixDims.setSizeY(1.0f);
                pixDims.setSizeZ(1.0f);
                pix.setPixelsDimensions(pixDims);
            }
            series++;
            
        }
        
        log.debug("Saving pixels to DB.");
        
        List<Pixels> pixelsList = store.saveToDB();
        
        for (Image image : imageList)
        {
            store.addImageToDataset(image, dataset);
        }
        
        return pixelsList;
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
     * @param file
     * @param index
     * @param total Import the actual image planes
     * @param b 
     * @throws FormatException if there is an error parsing metadata.
     * @throws IOException if there is an error reading the file.
     */
    // TODO: Add observer messaging for any agnostic viewer class to use
    @SuppressWarnings("unused")
    public List<Pixels> importImage(File file, int index, int numDone, int total, String imageName, boolean archive)
    throws FormatException, IOException
    {        
        String fileName = file.getAbsolutePath();
        String shortName = file.getName();
        Object[] args;
        
        args = new Object[9];
        args[0] = shortName;
        args[1] = index;
        args[2] = numDone;
        args[3] = total;

        notifyObservers(Actions.LOADING_IMAGE, args);

        open(file.getAbsolutePath());
        
        notifyObservers(Actions.LOADED_IMAGE, args);
        
        String[] fileNameList = reader.getUsedFiles();
        File[] files = new File[fileNameList.length];
        for (int i = 0; i < fileNameList.length; i++) 
        {
            files[i] = new File(fileNameList[i]); 
        }
        String formatString = reader.getImageReader().getReader().getClass().toString();
        formatString = formatString.replace("class loci.formats.in.", "");
        formatString = formatString.replace("Reader", "");
        System.err.println(formatString);
        store.setOriginalFiles(files, formatString);
        
        reader.getUsedFiles();
        
        List<Pixels> pixList = importMetadata(imageName);

        int seriesCount = reader.getSeriesCount();
        
        for (int series = 0; series < seriesCount; series++)
        {
            int count = calculateImageCount(fileName, series);
            Long pixId = pixList.get(series).getId(); 
            
            args[4] = getDataset();
            args[5] = pixId;
            args[6] = count;
            args[7] = series;
            
            notifyObservers(Actions.DATASET_STORED, args);
            
            BooleanAnnotation annotation = new BooleanAnnotation();
            annotation.setBoolValue(archive);
            annotation.setNs("openmicroscopy.org/omero/importer/archived"); // openmicroscopy.org/omero/importer/archived
            
            store.addBooleanAnnotationToPixels(annotation, pixList.get(series));
            
            importData(pixId, fileName, series, new ImportLibrary.Step()
            {
                @Override
                public void step(int series, int step)
                {
                    Object args2[] = {series, step, reader.getSeriesCount()};
                    notifyObservers(Actions.IMPORT_STEP, args2);
                }
            });
            
            notifyObservers(Actions.DATA_STORED, args);  
           
            if (archive == true)
            {
                //qTable.setProgressArchiving(index);
                for (int i = 0; i < fileNameList.length; i++) 
                {
                    files[i] = new File(fileNameList[i]);
                    store.writeFilesToFileStore(files, pixId);   
                }
            }
        }
        
        notifyObservers(Actions.IMPORT_DONE, args);
        
        return pixList;
        
    }
    
    /**
     * saves the binary data to the server. After each successful save,
     * {@link Step#step(int)} is called with the number of the iteration just
     * completed.
     * @param series 
     */
    public void importData(Long pixId, String fileName, int series, Step step)
    throws FormatException, IOException
    {
        int i = 1;
        int bytesPerPixel = getBytesPerPixel(reader.getPixelType());
        byte[] arrayBuf = new byte[sizeX * sizeY * bytesPerPixel];

        reader.setSeries(series);
        MessageDigest md;
        
        try {
            md = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(
                    "Required SHA-1 message digest algorithm unavailable.");
        }
        
        store.setPixelsId(pixId);
        
        for (int t = 0; t < sizeT; t++)
        {
            for (int c = 0; c < sizeC; c++)
            {
                for (int z = 0; z < sizeZ; z++)
                {
                    int planeNumber = reader.getIndex(z, c, t);
                    //int planeNumber = getTotalOffset(z, c, t);
                    ByteBuffer buf =
                        reader.openPlane2D(fileName, planeNumber, arrayBuf).getData();
                    arrayBuf = swapIfRequired(buf, fileName);
                    if (Main.DO_SHA1)
                    {
                        try {
                            md.update(arrayBuf);
                        } catch (Exception e) {
                            // This better not happen. :)
                            throw new RuntimeException(e);
                        }
                    }
                    step.step(series, i);
                    store.setPlane(pixId, arrayBuf, z, c, t);
                    i++;
                }
            }
        }
        reader.populateSHA1(pixId, md);
        reader.populateMinMax(pixId, series);
    }
    
    // ~ Helpers
    // =========================================================================

    private void setOffsetInfo(String fileName)
    {
        int order = 0;
        order = getSequenceNumber(reader.getDimensionOrder());
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
            // TWZ sequence
            case 3:
                tSize = smallOffset;
                wSize = tSize * numTimes;
                zSize = wSize * numWaves;
                break;
            // WTZ sequence
            case 4:
                wSize = smallOffset;
                tSize = wSize * numWaves;
                zSize = tSize * numTimes;
                break;
            //TZW
            case 5:
                tSize = smallOffset;
                zSize = wSize * numTimes;
                wSize = tSize * numZSections;
                
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
        if (dimOrder.equals("XYTCZ")) return 3;
        if (dimOrder.equals("XYCTZ")) return 4;
        if (dimOrder.equals("XYTZC")) return 5;
        throw new RuntimeException(dimOrder + " not represented in " +
                "getSequenceNumber");
    }
    
    /** Return true if the data is in little-endian format. 
     * @throws IOException 
     * @throws FormatException */
    public boolean isLittleEndian(String fileName) throws FormatException, IOException {
      return reader.isLittleEndian();
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
      int pixelType = reader.getPixelType();
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
            buf.put(i, DataTools.swap(buf.get(i)));
          }
        } else if (bytesPerPixel == 4) { // int/uint
            IntBuffer buf = buffer.asIntBuffer();
            for (int i = 0; i < (buffer.capacity() / 4); i++) {
              buf.put(i, DataTools.swap(buf.get(i)));
            }
        } else {
          throw new FormatException(
            "Unsupported sample bit width: '" + bytesPerPixel + "'");
        }
      }
      // We've got a big-endian file with a big-endian byte array.
      return buffer.array();
    }
    
    // Observable methods
    
    public boolean addObserver(IObserver object)
    {
        return observers.add(object);
    }
    
    public boolean deleteObserver(IObserver object)
    {
        return observers.remove(object);
        
    }

    public void notifyObservers(Object message, Object[] args)
    {
        for (IObserver observer:observers)
        {
            observer.update(this, message, args);
        }
    }
}
