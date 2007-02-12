package ome.formats.importer;

import java.io.IOException;
import java.nio.ByteBuffer;


import loci.formats.ChannelSeparator;
import loci.formats.FormatException;
import loci.formats.FormatReader;
import loci.formats.ImageReader;
import loci.formats.ReaderWrapper;

public class OMEROWrapper extends ReaderWrapper
{
	/**
	 * Reference copy of <i>reader</i> so that we can be compatible with the
	 * IFormatReader/ReaderWrapper interface but still maintain functionality
	 * that we require.
	 */
	public ChannelSeparator separator;
	
	public OMEROWrapper()
	{
		reader = separator = new ChannelSeparator(new ImageReader());
	}
	
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
		if (separator.getReader().isRGB(id))
			plane = ByteBuffer.wrap(openBytes(id, no));
		else
			plane = ByteBuffer.wrap(openBytes(id, no, buf));
		
		return new Plane2D(plane, getPixelType(id), isLittleEndian(id),
				           getSizeX(id), getSizeY(id)); 
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
		int planeSize = getSizeX(id) * getSizeY(id) *
		FormatReader.getBytesPerPixel(getPixelType(id));
		
		byte[] buf = new byte[planeSize];
		for (int c = 0; c < getSizeC(id); c++) {
			double min = Double.MAX_VALUE;
			double max = Double.MIN_VALUE;
			for (int t = 0; t < getSizeT(id); t++) {
				for (int z = 0; z < getSizeZ(id); z++) {
					int index = getIndex(id, z, c, t);
					Plane2D plane = openPlane2D(id, index, buf);
					for (int x = 0; x < getSizeX(id); x++) {
						for (int y = 0; y < getSizeY(id); y++) {
							double pixelValue = plane.getPixelValue(x, y);
							if (pixelValue < min) min = pixelValue;
							if (pixelValue > max) max = pixelValue;
						}
					}
				}
			}
			getMetadataStore(id).setChannelGlobalMinMax(c, min, max, null);
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
		if (getChannelGlobalMinimum(id, 0) == null
			|| getChannelGlobalMaximum(id, 0) == null)
			setChannelGlobalMinMax(id);
	}
}
