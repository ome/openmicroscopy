package ome.formats.importer;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

import loci.formats.ChannelFiller;
import loci.formats.ChannelSeparator;
import loci.formats.ClassList;
import loci.formats.FormatException;
import loci.formats.IFormatReader;
import loci.formats.ImageReader;
import loci.formats.MinMaxCalculator;
import loci.formats.in.FlexReader;
import loci.formats.in.InCellReader;
import loci.formats.in.LeicaReader;
import loci.formats.in.MIASReader;
import loci.formats.meta.MetadataStore;
import ome.formats.OMEROMetadataStoreClient;
import omero.model.Channel;
import omero.model.Pixels;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class OMEROWrapper extends MinMaxCalculator
{
    
    private final static Log log = LogFactory.getLog(OMEROWrapper.class);
    
    public static String READERS_KEY = "omero.import.readers";
    
    private ChannelSeparator separator;
    private ChannelFiller filler;
    public Boolean minMaxSet = null; 

    /**
	 * Reference copy of <i>reader</i> so that we can be compatible with the
	 * IFormatReader/ReaderWrapper interface but still maintain functionality
	 * that we require.
	 */
    private ImageReader iReader;

    
    public OMEROWrapper()
    {
        try
        {
                        
            // Set up static config file
            String readersDirectory = System.getProperty("user.dir") + File.separator + "config";
            String readersFile = readersDirectory + File.separator + "importer_readers.txt";
            
            File rFile = new File(readersFile);
            if (rFile.exists())
            {
                iReader = new ImageReader(
                        new ClassList(readersFile, IFormatReader.class, null));
            } else {
                iReader = new ImageReader(
                        new ClassList("importer_readers.txt", IFormatReader.class, OMEROWrapper.class));                
            }
            
            // OR if we find it in the system properties use that.
            String readers = System.getProperty(READERS_KEY);
            System.out.println(readers);
            if (readers != null) {
                log.info("Using user configured readers: "+readers);
                iReader = new ImageReader(new ClassList(readers, IFormatReader.class, null));
            }            
            
            filler = new ChannelFiller(iReader);
        }
        catch (IOException e)
        {
            throw new RuntimeException("Unable to load readers.txt.");
        }
        reader = separator  = new ChannelSeparator(filler);
        //reader = separator = new ChannelSeparator(iReader);
        
        // Force unreadable characters to be removed from metadata key/value pairs 
        iReader.setMetadataFiltered(true);
        filler.setMetadataFiltered(true);
        separator.setMetadataFiltered(true);
    };
    
	/**
	 * Obtains an object which represents a given plane within the file.
	 * @param id The path to the file.
	 * @param planeNumber The plane or section within the file to obtain.
	 * @param buf Pre-allocated buffer which has a <i>length</i> that can fit
	 * the byte count of an entire plane.
	 * @return an object which represents the plane.
	 * @throws FormatException if there is an error parsing the file.
	 * @throws IOException if there is an error reading from the file or
	 *   acquiring permissions to read the file.
	 */
	public Plane2D openPlane2D(String id, int planeNumber, byte[] buf)
		throws FormatException, IOException
	{
		// FIXME: HACK! The ChannelSeparator isn't exactly what one would call
		// "complete" so we have to work around the fact that it still copies
		// all of the plane data (all three channels) from the file if the file
		// is RGB.
		ByteBuffer plane;
		if (iReader.isRGB() || isLeicaReader())
        {
            //System.err.println("RGB, not using cached buffer.");
            byte[] bytePlane = openBytes(planeNumber);
			plane = ByteBuffer.wrap(bytePlane);
        }
		else
        {
            //System.err.println("Not RGB, using cached buffer.");
			plane = ByteBuffer.wrap(openBytes(planeNumber, buf));
        }
		return new Plane2D(plane, getPixelType(), isLittleEndian(),
				           getSizeX(), getSizeY());
	}

    public boolean isLeicaReader()
    {
        if (iReader.getReader() instanceof LeicaReader)
        {
            return true;
        }
        else
        {
            return false;
        }
    } 
    
    /**
     * Returns whether or not the reader for a given file is a screening format
     * or not.
     * @param string Absolute path to the image file to check.
     * @return <code>true</code> if the reader is an <i>SPW</i> reader and
     * <code>false</code> otherwise.
     */
    public boolean isSPWReader(String string)
    {
        try
        {
            if (iReader.getReader(string) instanceof InCellReader
                || iReader.getReader(string) instanceof FlexReader
                || iReader.getReader(string) instanceof MIASReader)
            {
                return true;
            }
            else
            {
                return false;
            }
        }
        catch (Exception e)
        {
            return false;
        }
    }
    
    @SuppressWarnings("unchecked")
	public boolean isMinMaxSet() throws FormatException, IOException
    {
        if (minMaxSet == null)
        {
            MetadataStore store = reader.getMetadataStore();
            int series = reader.getSeries();
            List<Pixels> pixels = (List<Pixels>) store.getRoot();
            Pixels p = pixels.get(series);
            Channel c = p.getChannel(p.getSizeC().getValue() - 1);
            if (c.getStatsInfo() == null)
            {
                minMaxSet = false;
            } else {
                minMaxSet = true;
            }
        }
        return minMaxSet;
    }

    @Override
    protected void updateMinMax(byte[] b, int ndx)
    throws FormatException, IOException
    {
    	if (isMinMaxSet() == false)
    		super.updateMinMax(b, ndx);
    }

    public void close() throws IOException
    {
    	minMaxSet = null;
    	super.close(false);
    }

    @Override
    public OMEROMetadataStoreClient getMetadataStore()
    {
    	return (OMEROMetadataStoreClient) super.getMetadataStore();
    }

    /**
     * Return the base image reader
     * @return See above.
     */
    public ImageReader getImageReader()
    {
    	return iReader;
    }
}
