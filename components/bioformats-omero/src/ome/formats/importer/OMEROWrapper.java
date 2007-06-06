package ome.formats.importer;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import loci.formats.ChannelSeparator;
import loci.formats.ClassList;
import loci.formats.FormatException;
import loci.formats.IFormatReader;
import loci.formats.ImageReader;
import loci.formats.MinMaxCalculator;
import loci.formats.in.LeicaReader;
import ome.formats.OMEROMetadataStore;
import ome.model.core.Channel;
import ome.model.core.Pixels;

public class OMEROWrapper extends MinMaxCalculator
{
    private ChannelSeparator separator;
    private Boolean minMaxSet = null; 
    private ImageReader iReader;
    /**
	 * Reference copy of <i>reader</i> so that we can be compatible with the
	 * IFormatReader/ReaderWrapper interface but still maintain functionality
	 * that we require.
	 * @param separator 
	 */
    
    public OMEROWrapper() 
    {
        try
        {
            iReader = new ImageReader(
                    new ClassList("readers.txt", 
                            IFormatReader.class));
        } catch (IOException e)
        {
            throw new RuntimeException("Unable to load readers.txt.");
        }
        reader = separator = new ChannelSeparator(iReader);
    };
	/**
	 * Obtains an object which represents a given plane within the file.
	 * @param id The path to the file.
	 * @param no The plane or section within the file to obtain.
	 * @param buf Pre-allocated buffer which has a <i>length</i> that can fit
	 * the byte count of an entire plane.
	 * @return an object which represents the plane.
	 * @throws FormatException if there is an error parsing the file.
	 * @throws IOException if there is an error reading from the file or
	 *   acquiring permissions to read the file.
	 */
	public Plane2D openPlane2D(String id, int no, byte[] buf)
		throws FormatException, IOException
	{
		// FIXME: HACK! The ChannelSeparator isn't exactly what one would call
		// "complete" so we have to work around the fact that it still copies
		// all of the plane data (all three channels) from the file if the file
		// is RGB.
		ByteBuffer plane;
		if (separator.getReader().isRGB() || iReader.getReader() instanceof LeicaReader)
			plane = ByteBuffer.wrap(openBytes(no));
		else
			plane = ByteBuffer.wrap(openBytes(no, buf));

		return new Plane2D(plane, getPixelType(), isLittleEndian(),
				           getSizeX(), getSizeY());
	}

	/**
	 * Retrieves the global min and max for each channel and sets those values
	 * in the MetadataStore.
	 * @param id The path to the file.
	 * @throws FormatException if there is an error parsing metadata.
	 * @throws IOException if there is an error reading the file.
	 */
	public void setChannelGlobalMinMax(String id)
		throws FormatException, IOException
	{
	    for(int c = 0; c < getSizeC(); c++)
        {
            double gMin = Double.MIN_VALUE;
            double gMax = Double.MAX_VALUE;
            
            double cMin = getChannelGlobalMinimum(c);
            double cMax = getChannelGlobalMaximum(c);
            
            gMin = cMin;
            gMax = cMax;
                       
            getMetadataStore().setChannelGlobalMinMax(c, gMin, gMax, null);
        }    
    }

	/**
	 * Makes sure that the reader's <code>MetadataStore</code> has all the
	 * relevant metadata that the OMERO server requires.
	 * @param id The path to the file.
	 * @throws FormatException if there is an error parsing metadata.
	 * @throws IOException if there is an error reading the file.
	 */
	public void finalizeMetadataStore(String id)
		throws FormatException, IOException
	{
		// Make sure we have StatsInfo objects.
		if (getChannelGlobalMinimum(0) == null
			|| getChannelGlobalMaximum(0) == null)
			setChannelGlobalMinMax(id);
	}

    public boolean isMinMaxSet() throws FormatException, IOException
    {
        if (minMaxSet == null)
        {
            OMEROMetadataStore store = 
                (OMEROMetadataStore) separator.getMetadataStore();
            List<Pixels> p = (ArrayList<Pixels>) store.getRoot();
            int series = reader.getSeries();
            List<Channel> channels = p.get(series).getChannels();
            if (channels.get(0).getStatsInfo() == null)
            {
                minMaxSet = false;
            } else {
                minMaxSet = true;
            }
        }
        return minMaxSet;
    }
    
     protected void updateMinMax(BufferedImage b, int ndx)
        throws FormatException, IOException
      {
         if (isMinMaxSet() == false)
              super.updateMinMax(b, ndx);
      }
     
     protected void updateMinMax(byte[] b, int ndx)
     throws FormatException, IOException
   {
         if (isMinMaxSet() == false)
             super.updateMinMax(b, ndx);
   }
     
     public void populateMinMax(Long id, Integer i) throws FormatException, IOException
     {
         if (isMinMaxSet() == false)
         {
             OMEROMetadataStore store = 
                 (OMEROMetadataStore) separator.getMetadataStore();
             store.populateMinMax(id, i);
         }
     }
     
     public void close() throws IOException
     {
         minMaxSet = null;
         super.close();
     }
}
