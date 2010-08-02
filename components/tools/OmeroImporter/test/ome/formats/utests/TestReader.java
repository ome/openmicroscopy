package ome.formats.utests;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Set;

import loci.common.RandomAccessInputStream;
import loci.formats.CoreMetadata;
import loci.formats.FileInfo;
import loci.formats.FormatException;
import loci.formats.FormatTools;
import loci.formats.IFormatReader;
import loci.formats.in.MetadataLevel;
import loci.formats.in.MetadataOptions;
import loci.formats.meta.MetadataStore;

public class TestReader implements IFormatReader {

	
	/** The reader's domains. */
	private String[] domains;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param graphicsDomain Pass <code>true</code> to add the graphics domain,
	 * 						 <code>false</code> otherwise.
	 */
	public TestReader(boolean graphicsDomain)
	{
		if (graphicsDomain) 
			domains = new String[] { FormatTools.LM_DOMAIN, 
				FormatTools.GRAPHICS_DOMAIN };
		else domains = new String[] { FormatTools.LM_DOMAIN };
	}
	
	/** Creates a default instance. */
	public TestReader()
	{
		this(false);
	}
	
	public IFormatReader getReader()
	{
		return new loci.formats.in.MinimalTiffReader();
	}
	
	public void close(boolean arg0) throws IOException {
		// TODO Auto-generated method stub

	}

	public int fileGroupOption(String arg0) throws FormatException, IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	public short[][] get16BitLookupTable() throws FormatException, IOException {
		// TODO Auto-generated method stub
		return null;
	}

	public byte[][] get8BitLookupTable() throws FormatException, IOException {
		// TODO Auto-generated method stub
		return null;
	}

	public FileInfo[] getAdvancedSeriesUsedFiles(boolean arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public FileInfo[] getAdvancedUsedFiles(boolean arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public int[] getChannelDimLengths() {
		// TODO Auto-generated method stub
		return null;
	}

	public String[] getChannelDimTypes() {
		// TODO Auto-generated method stub
		return null;
	}

	public CoreMetadata[] getCoreMetadata() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getCurrentFile() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getDimensionOrder() {
		// TODO Auto-generated method stub
		return null;
	}

	public String[] getDomains() {
		return domains;
	}

	public int getEffectiveSizeC() {
		// TODO Auto-generated method stub
		return 0;
	}

	public Hashtable getGlobalMetadata() {
		// TODO Auto-generated method stub
		return null;
	}

	public int getImageCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getIndex(int arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		return 0;
	}

	public Hashtable getMetadata() {
		// TODO Auto-generated method stub
		return null;
	}

	public MetadataStore getMetadataStore() {
		// TODO Auto-generated method stub
		return null;
	}

	public Object getMetadataStoreRoot() {
		// TODO Auto-generated method stub
		return null;
	}

	public Object getMetadataValue(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public int getPixelType() {
		// TODO Auto-generated method stub
		return 0;
	}

	public String[] getPossibleDomains(String arg0) throws FormatException,
			IOException {
		// TODO Auto-generated method stub
		return null;
	}

	public int getRGBChannelCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getSeries() {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getSeriesCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	public Hashtable getSeriesMetadata() {
		// TODO Auto-generated method stub
		return null;
	}

	public String[] getSeriesUsedFiles() {
		// TODO Auto-generated method stub
		return null;
	}

	public String[] getSeriesUsedFiles(boolean arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public int getSizeC() {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getSizeT() {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getSizeX() {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getSizeY() {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getSizeZ() {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getThumbSizeX() {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getThumbSizeY() {
		// TODO Auto-generated method stub
		return 0;
	}

	public IFormatReader[] getUnderlyingReaders() {
		// TODO Auto-generated method stub
		return null;
	}

	public String[] getUsedFiles() {
		// TODO Auto-generated method stub
		return null;
	}

	public String[] getUsedFiles(boolean arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public int[] getZCTCoords(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isFalseColor() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isGroupFiles() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isIndexed() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isInterleaved() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isInterleaved(int arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isLittleEndian() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isMetadataCollected() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isMetadataComplete() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isMetadataFiltered() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isNormalized() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isOrderCertain() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isOriginalMetadataPopulated() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isRGB() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isSingleFile(String arg0) throws FormatException,
			IOException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isThisType(byte[] arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isThisType(RandomAccessInputStream arg0) throws IOException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isThisType(String arg0, boolean arg1) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isThumbnailSeries() {
		// TODO Auto-generated method stub
		return false;
	}

	public byte[] openBytes(int arg0) throws FormatException, IOException {
		// TODO Auto-generated method stub
		return null;
	}

	public byte[] openBytes(int arg0, byte[] arg1) throws FormatException,
			IOException {
		// TODO Auto-generated method stub
		return null;
	}

	public byte[] openBytes(int arg0, int arg1, int arg2, int arg3, int arg4)
			throws FormatException, IOException {
		// TODO Auto-generated method stub
		return null;
	}

	public byte[] openBytes(int arg0, byte[] arg1, int arg2, int arg3,
			int arg4, int arg5) throws FormatException, IOException {
		// TODO Auto-generated method stub
		return null;
	}

	public Object openPlane(int arg0, int arg1, int arg2, int arg3, int arg4)
			throws FormatException, IOException {
		// TODO Auto-generated method stub
		return null;
	}

	public byte[] openThumbBytes(int arg0) throws FormatException, IOException {
		// TODO Auto-generated method stub
		return null;
	}

	public void setGroupFiles(boolean arg0) {
		// TODO Auto-generated method stub

	}

	public void setMetadataCollected(boolean arg0) {
		// TODO Auto-generated method stub

	}

	public void setMetadataFiltered(boolean arg0) {
		// TODO Auto-generated method stub

	}

	public void setMetadataStore(MetadataStore arg0) {
		// TODO Auto-generated method stub

	}

	public void setNormalized(boolean arg0) {
		// TODO Auto-generated method stub

	}

	public void setOriginalMetadataPopulated(boolean arg0) {
		// TODO Auto-generated method stub

	}

	public void setSeries(int arg0) {
		// TODO Auto-generated method stub

	}

	public void close() throws IOException {
		// TODO Auto-generated method stub

	}

	public String getFormat() {
		// TODO Auto-generated method stub
		return null;
	}

	public Class getNativeDataType() {
		// TODO Auto-generated method stub
		return null;
	}

	public String[] getSuffixes() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isThisType(String arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public void setId(String arg0) throws FormatException, IOException {
		// TODO Auto-generated method stub

	}

	public int getBitsPerPixel() {
		// TODO Auto-generated method stub
        return 0;
	}

    /* (non-Javadoc)
     * @see loci.formats.IMetadataConfigurable#getMetadataOptions()
     */
    public MetadataOptions getMetadataOptions()
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see loci.formats.IMetadataConfigurable#getSupportedMetadataLevels()
     */
    public Set<MetadataLevel> getSupportedMetadataLevels()
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see loci.formats.IMetadataConfigurable#setMetadataOptions(loci.formats.in.MetadataOptions)
     */
    public void setMetadataOptions(MetadataOptions arg0)
    {
        // TODO Auto-generated method stub
        
    }

	public boolean hasCompanionFiles() {
		// TODO Auto-generated method stub
		return false;
	}

}
