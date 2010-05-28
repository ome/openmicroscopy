/*
 * ome.formats.importer.gui.GuiCommonElements
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 *
 */
package ome.formats;

import static omero.rtypes.rstring;

import java.util.ArrayList;
import java.util.List;

import loci.formats.meta.MetadataStore;
import ome.xml.r201004.enums.AcquisitionMode;
import ome.xml.r201004.enums.ArcType;
import ome.xml.r201004.enums.Binning;
import ome.xml.r201004.enums.ContrastMethod;
import ome.xml.r201004.enums.Correction;
import ome.xml.r201004.enums.DetectorType;
import ome.xml.r201004.enums.DimensionOrder;
import ome.xml.r201004.enums.ExperimentType;
import ome.xml.r201004.enums.FilamentType;
import ome.xml.r201004.enums.FilterType;
import ome.xml.r201004.enums.IlluminationType;
import ome.xml.r201004.enums.Immersion;
import ome.xml.r201004.enums.LaserMedium;
import ome.xml.r201004.enums.LaserType;
import ome.xml.r201004.enums.Medium;
import ome.xml.r201004.enums.MicrobeamManipulationType;
import ome.xml.r201004.enums.MicroscopeType;
import ome.xml.r201004.enums.NamingConvention;
import ome.xml.r201004.enums.PixelType;
import ome.xml.r201004.enums.Pulse;
import ome.xml.r201004.primitives.NonNegativeInteger;
import ome.xml.r201004.primitives.PercentFraction;
import ome.xml.r201004.primitives.PositiveInteger;
import omero.ServerError;
import omero.api.IUpdatePrx;
import omero.api.ServiceFactoryPrx;
import omero.grid.Column;
import omero.grid.ImageColumn;
import omero.grid.LongColumn;
import omero.grid.MaskColumn;
import omero.grid.RoiColumn;
import omero.grid.TablePrx;
import omero.model.FileAnnotation;
import omero.model.FileAnnotationI;
import omero.model.IObject;
import omero.model.Image;
import omero.model.ImageI;
import omero.model.OriginalFileI;
import omero.model.Pixels;
import omero.model.PlateAnnotationLink;
import omero.model.PlateAnnotationLinkI;
import omero.model.PlateI;
import omero.model.Roi;
import omero.model.RoiAnnotationLink;
import omero.model.RoiAnnotationLinkI;
import omero.model.RoiI;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Client side metadata store implementation that only deals with overlays. At
 * the moment this is restricted to <b>mask</b> based ROI inserted in OMERO
 * tables.
 * @author Chris Allan <callan at blackcat dot ca>
 *
 */
public class OverlayMetadataStore implements MetadataStore {
	
	/** Logger for this class. */
    private static Log log = LogFactory.getLog(OverlayMetadataStore.class);
	
	private List<Pixels> pixelsList;
	
	private ServiceFactoryPrx sf;
	
	private static final int DEFAULT_BUFFER_SIZE = 10;
	
	private static final int IMAGE_COLUMN = 0;
	
	private static final int ROI_COLUMN = 1;
	
	private static final int COLOR_COLUMN = 2;
	
	private static final int MASK_COLUMN = 3;
	
	private Integer currentIndex;
	
	private Integer currentImageIndex;
	
	private Integer currentRoiIndex;
	
	private Integer currentShapeIndex;
	
	private Column[] columns;
	
	private TablePrx table;
	
	private long tableFileId;
	
	private long plateId;
	
	private FileAnnotation fileAnnotation;
	
	private long fileAnnotationId;
	
	private IUpdatePrx updateService;
	
	/**
	 * Initializes the metadata store implementation, creating an empty mask
	 * column and a new table to store results.
	 * @param sf Client side service factory.
	 * @param pixelsList List of pixels already saved in the database.
	 * @param plateIds List of plate Ids already saved in the database. (This
	 * should have <code>plateIds.size() == 1</code>).
	 * @throws ServerError Thrown if there was an error communicating with the
	 * server during table creation.
	 */
	public void initialize(ServiceFactoryPrx sf, List<Pixels> pixelsList,
			                  List<Long> plateIds)
		throws ServerError {
		this.pixelsList = pixelsList;
		this.sf = sf;
		updateService = sf.getUpdateService();
		columns = createColumns(DEFAULT_BUFFER_SIZE);
		plateId = plateIds.get(0);
	}
	
	/**
	 * Completes overlay population, performing the correct original file
	 * linkages, annotation creation and flushing buffered overlays to the
	 * backing OMERO table.
	 * @throws ServerError Thrown if there was an error linking the table to
	 * the plate.
	 */
	public void complete() throws ServerError {
		long saved = saveIfNecessary(true);
		if (saved > 0)
		{
			PlateAnnotationLink link = new PlateAnnotationLinkI();
			link.setParent(new PlateI(plateId, false));
			link.setChild(new FileAnnotationI(fileAnnotationId, false));
			updateService.saveObject(link);
		}
	}
	
	/**
	 * Creates new, empty columns.
	 * @param length Number of rows the columns should have.
	 * @return See above.
	 */
	private Column[] createColumns(int length) {
		Column[] newColumns = new Column[4];
		newColumns[IMAGE_COLUMN] = new ImageColumn("Image", "", new long[length]);
		newColumns[ROI_COLUMN] = new RoiColumn("ROI", "", new long[length]);
		newColumns[COLOR_COLUMN] = new LongColumn("Color", "", new long[length]);
		newColumns[MASK_COLUMN] = new MaskColumn("Overlays", "", 
				new long[length],    // imageId
				new int[length],     // theZ
				new int[length],     // theT
				new double[length],  // x
				new double[length],  // y
				new double[length],  // w
				new double[length],  // h
				new byte[length][]); // bytes
		return newColumns;
	}
	
	/**
	 * Creates a new table, initializing with the current set of rows and
	 * a measurement file annotation to identify the table.
	 * @throws ServerError Thrown if there was an error initializing the table
	 * or creating the annotation.
	 */
	private void createTable() throws ServerError {
		table = sf.sharedResources().newTable(1, "Overlays");
		table.initialize(columns);
		tableFileId = table.getOriginalFile().getId().getValue();
		// Create our measurement file annotation
		fileAnnotation = new FileAnnotationI();
		fileAnnotation.setDescription(rstring("Overlays"));
		fileAnnotation.setNs(rstring(omero.constants.namespaces.NSMEASUREMENT.value));
		fileAnnotation.setFile(new OriginalFileI(tableFileId, false));
		fileAnnotation = (FileAnnotation) updateService.saveAndReturnObject(fileAnnotation);
		fileAnnotationId = fileAnnotation.getId().getValue();
		log.info(String.format("New table %d annotation %d", 
				tableFileId, fileAnnotationId));
	}
	
	/**
	 * Returns the index of the current row we're populating.
	 * @param imageIndex Image index as received from Bio-Formats.
	 * @param roiIndex ROI index as received from Bio-Formats.
	 * @param shapeIndex Shape index as received from Bio-Formats.
	 * @return See above.
	 */
	private int getTableIndex(int imageIndex, int roiIndex, int shapeIndex) {
        if (table == null)
        {
            try
            {
                createTable();
            }
            catch (ServerError e)
            {
                throw new RuntimeException(e);
            }
        }
        if (currentImageIndex == null
            && currentRoiIndex == null
            && currentShapeIndex == null)
        {
            currentIndex = 0;
            currentImageIndex = imageIndex;
            currentRoiIndex = roiIndex;
            currentShapeIndex = shapeIndex;
        }
        else if (currentImageIndex != imageIndex)
		{
			currentIndex++;
			saveIfNecessary(false);
			currentImageIndex = imageIndex;
			currentRoiIndex = roiIndex;
			currentShapeIndex = shapeIndex;
		}
		else if (currentRoiIndex != roiIndex)
		{
			currentIndex++;
			saveIfNecessary(false);
			currentRoiIndex = roiIndex;
			currentShapeIndex = shapeIndex;
		}
		else if (currentShapeIndex != shapeIndex)
		{
			currentIndex++;
			saveIfNecessary(false);
			currentShapeIndex = shapeIndex;
		}
		return currentIndex;
	}
	
	/**
	 * Writes ROI objects to the server and updates the ROI column with their
	 * IDs. 
	 */
	private void saveAndUpdateROI() throws ServerError {
		ImageColumn imageColumn = (ImageColumn) columns[IMAGE_COLUMN];
		RoiColumn roiColumn = (RoiColumn) columns[ROI_COLUMN];
		List<IObject> toSave = new ArrayList<IObject>();
		for (int i = 0; i < imageColumn.values.length; i++)
		{
			Image unloadedImage = new ImageI(imageColumn.values[i], false);
			RoiAnnotationLink link = new RoiAnnotationLinkI();
			Roi roi = new RoiI();
			roi.setDescription(rstring("Overlay"));
			roi.setImage(unloadedImage);
			link.setParent(roi);
			link.setChild(new FileAnnotationI(fileAnnotationId, false));
			roi.addRoiAnnotationLink(link);
			toSave.add(roi);
		}
		toSave = updateService.saveAndReturnArray(toSave);
		log.info(String.format("Saved %d ROI objects.", toSave.size()));
		for (int i = 0; i < toSave.size(); i++)
		{
			roiColumn.values[i] = toSave.get(i).getId().getValue();
		}
	}
	
	/**
	 * Updates the mask column in the table if the buffer size is reached.
	 * @param force Whether or not to force an update.
	 * @return The number of rows saved.
	 */
	private long saveIfNecessary(boolean force) {
		if (currentIndex == null || currentIndex == 0
			|| (currentIndex != DEFAULT_BUFFER_SIZE && force == false))
		{
			return 0;
		}
		long saved = 0;
		try
		{
			MaskColumn maskColumn = (MaskColumn) columns[MASK_COLUMN];
			ImageColumn imageColumn = (ImageColumn) columns[IMAGE_COLUMN];
			LongColumn colorColumn = (LongColumn) columns[COLOR_COLUMN];
			if (currentIndex != DEFAULT_BUFFER_SIZE)
			{
				int size = currentIndex + 1;
				Column[] newColumns = createColumns(currentIndex + 1);

				// Copy values for the Mask column
				MaskColumn c = (MaskColumn) newColumns[MASK_COLUMN];
				System.arraycopy(maskColumn.imageId, 0, c.imageId, 0, size);
				System.arraycopy(maskColumn.theZ, 0, c.theZ, 0, size);
				System.arraycopy(maskColumn.theT, 0, c.theT, 0, size);
				System.arraycopy(maskColumn.x, 0, c.x, 0, size);
				System.arraycopy(maskColumn.y, 0, c.y, 0, size);
				System.arraycopy(maskColumn.w, 0, c.w, 0, size);
				System.arraycopy(maskColumn.h, 0, c.h, 0, size);
				for (int i = 0; i < size; i++)
				{
					c.bytes[i] = maskColumn.bytes[i];
				}
				// Copy values for the Image column
				ImageColumn c2 = (ImageColumn) newColumns[IMAGE_COLUMN];
				System.arraycopy(imageColumn.values, 0, c2.values, 0, size);
				// Copy values for the Color column
				LongColumn c3 = (LongColumn) newColumns[COLOR_COLUMN];
				System.arraycopy(colorColumn.values, 0, c3.values, 0, size);
				// Update our references
				columns = newColumns;
				maskColumn = (MaskColumn) columns[MASK_COLUMN];
				imageColumn = (ImageColumn) columns[IMAGE_COLUMN];
				colorColumn = (LongColumn) columns[COLOR_COLUMN];
			}
			saveAndUpdateROI();
			table.addData(columns);
			saved = maskColumn.imageId.length;
			columns = createColumns(DEFAULT_BUFFER_SIZE);
			currentIndex = 0;
		}
		catch (Throwable t)
		{
			throw new RuntimeException(t);
		}
		return saved;
	}
	
	//
	// FIXME: All of the below need to be implemented
	//
	
	public void setShapeStrokeColor(String strokeColor, int imageIndex,
			int roiIndex, int shapeIndex) {
		long imageId = pixelsList.get(imageIndex).getImage().getId().getValue();
		int index = getTableIndex(imageIndex, roiIndex, shapeIndex);
		MaskColumn maskColumn = (MaskColumn) columns[MASK_COLUMN];
		LongColumn colorColumn = (LongColumn) columns[COLOR_COLUMN];
		ImageColumn imageColumn = (ImageColumn) columns[IMAGE_COLUMN];
		imageColumn.values[index] = imageId;
		maskColumn.imageId[index] = imageId;
		colorColumn.values[index] = Integer.parseInt(strokeColor);
	}
	
	public void setMaskHeight(String height, int imageIndex, int roiIndex,
			int shapeIndex) {
		long imageId = pixelsList.get(imageIndex).getImage().getId().getValue();
		int index = getTableIndex(imageIndex, roiIndex, shapeIndex);
		MaskColumn maskColumn = (MaskColumn) columns[MASK_COLUMN];
		ImageColumn imageColumn = (ImageColumn) columns[IMAGE_COLUMN];
		imageColumn.values[index] = imageId;
		maskColumn.imageId[index] = imageId;
		maskColumn.h[index] = Double.parseDouble(height);
	}

	public void setMaskID(String id, int imageIndex, int roiIndex,
			int shapeIndex) {
		// Unused
	}

	public void setMaskTransform(String transform, int imageIndex,
			int roiIndex, int shapeIndex) {
		// Unused
	}

	public void setMaskWidth(String width, int imageIndex, int roiIndex,
			int shapeIndex) {
		long imageId = pixelsList.get(imageIndex).getImage().getId().getValue();
		int index = getTableIndex(imageIndex, roiIndex, shapeIndex);
		MaskColumn maskColumn = (MaskColumn) columns[MASK_COLUMN];
		ImageColumn imageColumn = (ImageColumn) columns[IMAGE_COLUMN];
		imageColumn.values[index] = imageId;
		maskColumn.imageId[index] = imageId;
		maskColumn.w[index] = Double.parseDouble(width);
	}

	public void setMaskX(String x, int imageIndex, int roiIndex,
			int shapeIndex) {
		long imageId = pixelsList.get(imageIndex).getImage().getId().getValue();
		int index = getTableIndex(imageIndex, roiIndex, shapeIndex);
		MaskColumn maskColumn = (MaskColumn) columns[MASK_COLUMN];
		ImageColumn imageColumn = (ImageColumn) columns[IMAGE_COLUMN];
		imageColumn.values[index] = imageId;
		maskColumn.imageId[index] = imageId;
		maskColumn.x[index] = Double.parseDouble(x);
	}

	public void setMaskY(String y, int imageIndex, int roiIndex,
			int shapeIndex) {
		long imageId = pixelsList.get(imageIndex).getImage().getId().getValue();
		int index = getTableIndex(imageIndex, roiIndex, shapeIndex);
		MaskColumn maskColumn = (MaskColumn) columns[MASK_COLUMN];
		ImageColumn imageColumn = (ImageColumn) columns[IMAGE_COLUMN];
		imageColumn.values[index] = imageId;
		maskColumn.imageId[index] = imageId;
		maskColumn.y[index] = Double.parseDouble(y);
	}

	public void setMaskPixelsBigEndian(Boolean bigEndian, int imageIndex,
			int roiIndex, int shapeIndex) {
		// Unused
	}

	public void setMaskPixelsBinData(byte[] binData, int imageIndex,
			int roiIndex, int shapeIndex) {
		long imageId = pixelsList.get(imageIndex).getImage().getId().getValue();
		int index = getTableIndex(imageIndex, roiIndex, shapeIndex);
		MaskColumn maskColumn = (MaskColumn) columns[MASK_COLUMN];
		ImageColumn imageColumn = (ImageColumn) columns[IMAGE_COLUMN];
		imageColumn.values[index] = imageId;
		maskColumn.imageId[index] = imageId;
		maskColumn.bytes[index] = binData;
	}

	public void setMaskPixelsExtendedPixelType(String extendedPixelType,
			int imageIndex, int roiIndex, int shapeIndex) {
		// Unused
	}

	public void setMaskPixelsID(String id, int imageIndex, int roiIndex,
			int shapeIndex) {
		// Unused
	}

	public void setMaskPixelsSizeX(Integer sizeX, int imageIndex, int roiIndex,
			int shapeIndex) {
		// Unused
	}

	public void setMaskPixelsSizeY(Integer sizeY, int imageIndex, int roiIndex,
			int shapeIndex) {
		// Unused
	}

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#createRoot()
     */
    public void createRoot()
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#getRoot()
     */
    public Object getRoot()
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setArcID(java.lang.String, int, int)
     */
    public void setArcID(String id, int instrumentIndex, int lightSourceIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setArcLotNumber(java.lang.String, int, int)
     */
    public void setArcLotNumber(String lotNumber, int instrumentIndex,
            int lightSourceIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setArcManufacturer(java.lang.String, int, int)
     */
    public void setArcManufacturer(String manufacturer, int instrumentIndex,
            int lightSourceIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setArcModel(java.lang.String, int, int)
     */
    public void setArcModel(String model, int instrumentIndex,
            int lightSourceIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setArcPower(java.lang.Double, int, int)
     */
    public void setArcPower(Double power, int instrumentIndex,
            int lightSourceIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setArcSerialNumber(java.lang.String, int, int)
     */
    public void setArcSerialNumber(String serialNumber, int instrumentIndex,
            int lightSourceIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setArcType(ome.xml.r201004.enums.ArcType, int, int)
     */
    public void setArcType(ArcType type, int instrumentIndex,
            int lightSourceIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setBooleanAnnotationID(java.lang.String, int)
     */
    public void setBooleanAnnotationID(String id, int booleanAnnotationIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setBooleanAnnotationNamespace(java.lang.String, int)
     */
    public void setBooleanAnnotationNamespace(String namespace,
            int booleanAnnotationIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setBooleanAnnotationValue(java.lang.Boolean, int)
     */
    public void setBooleanAnnotationValue(Boolean value,
            int booleanAnnotationIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setChannelAcquisitionMode(ome.xml.r201004.enums.AcquisitionMode, int, int)
     */
    public void setChannelAcquisitionMode(AcquisitionMode acquisitionMode,
            int imageIndex, int channelIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setChannelAnnotationRef(java.lang.String, int, int, int)
     */
    public void setChannelAnnotationRef(String annotation, int imageIndex,
            int channelIndex, int annotationRefIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setChannelColor(java.lang.Integer, int, int)
     */
    public void setChannelColor(Integer color, int imageIndex, int channelIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setChannelContrastMethod(ome.xml.r201004.enums.ContrastMethod, int, int)
     */
    public void setChannelContrastMethod(ContrastMethod contrastMethod,
            int imageIndex, int channelIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setChannelEmissionWavelength(ome.xml.r201004.primitives.PositiveInteger, int, int)
     */
    public void setChannelEmissionWavelength(
            PositiveInteger emissionWavelength, int imageIndex, int channelIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setChannelExcitationWavelength(ome.xml.r201004.primitives.PositiveInteger, int, int)
     */
    public void setChannelExcitationWavelength(
            PositiveInteger excitationWavelength, int imageIndex,
            int channelIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setChannelFilterSetRef(java.lang.String, int, int)
     */
    public void setChannelFilterSetRef(String filterSet, int imageIndex,
            int channelIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setChannelFluor(java.lang.String, int, int)
     */
    public void setChannelFluor(String fluor, int imageIndex, int channelIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setChannelID(java.lang.String, int, int)
     */
    public void setChannelID(String id, int imageIndex, int channelIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setChannelIlluminationType(ome.xml.r201004.enums.IlluminationType, int, int)
     */
    public void setChannelIlluminationType(IlluminationType illuminationType,
            int imageIndex, int channelIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setChannelLightSourceSettingsAttenuation(ome.xml.r201004.primitives.PercentFraction, int, int)
     */
    public void setChannelLightSourceSettingsAttenuation(
            PercentFraction attenuation, int imageIndex, int channelIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setChannelLightSourceSettingsID(java.lang.String, int, int)
     */
    public void setChannelLightSourceSettingsID(String id, int imageIndex,
            int channelIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setChannelLightSourceSettingsWavelength(ome.xml.r201004.primitives.PositiveInteger, int, int)
     */
    public void setChannelLightSourceSettingsWavelength(
            PositiveInteger wavelength, int imageIndex, int channelIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setChannelNDFilter(java.lang.Double, int, int)
     */
    public void setChannelNDFilter(Double ndfilter, int imageIndex,
            int channelIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setChannelName(java.lang.String, int, int)
     */
    public void setChannelName(String name, int imageIndex, int channelIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setChannelOTFRef(java.lang.String, int, int)
     */
    public void setChannelOTFRef(String otf, int imageIndex, int channelIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setChannelPinholeSize(java.lang.Double, int, int)
     */
    public void setChannelPinholeSize(Double pinholeSize, int imageIndex,
            int channelIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setChannelPockelCellSetting(java.lang.Integer, int, int)
     */
    public void setChannelPockelCellSetting(Integer pockelCellSetting,
            int imageIndex, int channelIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setChannelSamplesPerPixel(java.lang.Integer, int, int)
     */
    public void setChannelSamplesPerPixel(Integer samplesPerPixel,
            int imageIndex, int channelIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setDatasetAnnotationRef(java.lang.String, int, int)
     */
    public void setDatasetAnnotationRef(String annotation, int datasetIndex,
            int annotationRefIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setDatasetDescription(java.lang.String, int)
     */
    public void setDatasetDescription(String description, int datasetIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setDatasetExperimenterRef(java.lang.String, int)
     */
    public void setDatasetExperimenterRef(String experimenter, int datasetIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setDatasetGroupRef(java.lang.String, int)
     */
    public void setDatasetGroupRef(String group, int datasetIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setDatasetID(java.lang.String, int)
     */
    public void setDatasetID(String id, int datasetIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setDatasetName(java.lang.String, int)
     */
    public void setDatasetName(String name, int datasetIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setDatasetProjectRef(java.lang.String, int, int)
     */
    public void setDatasetProjectRef(String project, int datasetIndex,
            int projectRefIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setDetectorAmplificationGain(java.lang.Double, int, int)
     */
    public void setDetectorAmplificationGain(Double amplificationGain,
            int instrumentIndex, int detectorIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setDetectorGain(java.lang.Double, int, int)
     */
    public void setDetectorGain(Double gain, int instrumentIndex,
            int detectorIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setDetectorID(java.lang.String, int, int)
     */
    public void setDetectorID(String id, int instrumentIndex, int detectorIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setDetectorLotNumber(java.lang.String, int, int)
     */
    public void setDetectorLotNumber(String lotNumber, int instrumentIndex,
            int detectorIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setDetectorManufacturer(java.lang.String, int, int)
     */
    public void setDetectorManufacturer(String manufacturer,
            int instrumentIndex, int detectorIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setDetectorModel(java.lang.String, int, int)
     */
    public void setDetectorModel(String model, int instrumentIndex,
            int detectorIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setDetectorOffset(java.lang.Double, int, int)
     */
    public void setDetectorOffset(Double offset, int instrumentIndex,
            int detectorIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setDetectorSerialNumber(java.lang.String, int, int)
     */
    public void setDetectorSerialNumber(String serialNumber,
            int instrumentIndex, int detectorIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setDetectorSettingsBinning(ome.xml.r201004.enums.Binning, int, int)
     */
    public void setDetectorSettingsBinning(Binning binning, int imageIndex,
            int channelIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setDetectorSettingsGain(java.lang.Double, int, int)
     */
    public void setDetectorSettingsGain(Double gain, int imageIndex,
            int channelIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setDetectorSettingsID(java.lang.String, int, int)
     */
    public void setDetectorSettingsID(String id, int imageIndex,
            int channelIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setDetectorSettingsOffset(java.lang.Double, int, int)
     */
    public void setDetectorSettingsOffset(Double offset, int imageIndex,
            int channelIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setDetectorSettingsReadOutRate(java.lang.Double, int, int)
     */
    public void setDetectorSettingsReadOutRate(Double readOutRate,
            int imageIndex, int channelIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setDetectorSettingsVoltage(java.lang.Double, int, int)
     */
    public void setDetectorSettingsVoltage(Double voltage, int imageIndex,
            int channelIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setDetectorType(ome.xml.r201004.enums.DetectorType, int, int)
     */
    public void setDetectorType(DetectorType type, int instrumentIndex,
            int detectorIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setDetectorVoltage(java.lang.Double, int, int)
     */
    public void setDetectorVoltage(Double voltage, int instrumentIndex,
            int detectorIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setDetectorZoom(java.lang.Double, int, int)
     */
    public void setDetectorZoom(Double zoom, int instrumentIndex,
            int detectorIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setDichroicID(java.lang.String, int, int)
     */
    public void setDichroicID(String id, int instrumentIndex, int dichroicIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setDichroicLotNumber(java.lang.String, int, int)
     */
    public void setDichroicLotNumber(String lotNumber, int instrumentIndex,
            int dichroicIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setDichroicManufacturer(java.lang.String, int, int)
     */
    public void setDichroicManufacturer(String manufacturer,
            int instrumentIndex, int dichroicIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setDichroicModel(java.lang.String, int, int)
     */
    public void setDichroicModel(String model, int instrumentIndex,
            int dichroicIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setDichroicSerialNumber(java.lang.String, int, int)
     */
    public void setDichroicSerialNumber(String serialNumber,
            int instrumentIndex, int dichroicIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setDoubleAnnotationID(java.lang.String, int)
     */
    public void setDoubleAnnotationID(String id, int doubleAnnotationIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setDoubleAnnotationNamespace(java.lang.String, int)
     */
    public void setDoubleAnnotationNamespace(String namespace,
            int doubleAnnotationIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setDoubleAnnotationValue(java.lang.Double, int)
     */
    public void setDoubleAnnotationValue(Double value, int doubleAnnotationIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setEllipseDescription(java.lang.String, int, int)
     */
    public void setEllipseDescription(String description, int ROIIndex,
            int shapeIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setEllipseFill(java.lang.Integer, int, int)
     */
    public void setEllipseFill(Integer fill, int ROIIndex, int shapeIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setEllipseFontSize(java.lang.Integer, int, int)
     */
    public void setEllipseFontSize(Integer fontSize, int ROIIndex,
            int shapeIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setEllipseID(java.lang.String, int, int)
     */
    public void setEllipseID(String id, int ROIIndex, int shapeIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setEllipseLabel(java.lang.String, int, int)
     */
    public void setEllipseLabel(String label, int ROIIndex, int shapeIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setEllipseName(java.lang.String, int, int)
     */
    public void setEllipseName(String name, int ROIIndex, int shapeIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setEllipseRadiusX(java.lang.Double, int, int)
     */
    public void setEllipseRadiusX(Double radiusX, int ROIIndex, int shapeIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setEllipseRadiusY(java.lang.Double, int, int)
     */
    public void setEllipseRadiusY(Double radiusY, int ROIIndex, int shapeIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setEllipseStroke(java.lang.Integer, int, int)
     */
    public void setEllipseStroke(Integer stroke, int ROIIndex, int shapeIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setEllipseStrokeDashArray(java.lang.String, int, int)
     */
    public void setEllipseStrokeDashArray(String strokeDashArray, int ROIIndex,
            int shapeIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setEllipseStrokeWidth(java.lang.Double, int, int)
     */
    public void setEllipseStrokeWidth(Double strokeWidth, int ROIIndex,
            int shapeIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setEllipseTheC(java.lang.Integer, int, int)
     */
    public void setEllipseTheC(Integer theC, int ROIIndex, int shapeIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setEllipseTheT(java.lang.Integer, int, int)
     */
    public void setEllipseTheT(Integer theT, int ROIIndex, int shapeIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setEllipseTheZ(java.lang.Integer, int, int)
     */
    public void setEllipseTheZ(Integer theZ, int ROIIndex, int shapeIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setEllipseTransform(java.lang.String, int, int)
     */
    public void setEllipseTransform(String transform, int ROIIndex,
            int shapeIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setEllipseX(java.lang.Double, int, int)
     */
    public void setEllipseX(Double x, int ROIIndex, int shapeIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setEllipseY(java.lang.Double, int, int)
     */
    public void setEllipseY(Double y, int ROIIndex, int shapeIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setExperimentDescription(java.lang.String, int)
     */
    public void setExperimentDescription(String description, int experimentIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setExperimentExperimenterRef(java.lang.String, int)
     */
    public void setExperimentExperimenterRef(String experimenter,
            int experimentIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setExperimentID(java.lang.String, int)
     */
    public void setExperimentID(String id, int experimentIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setExperimentType(ome.xml.r201004.enums.ExperimentType, int)
     */
    public void setExperimentType(ExperimentType type, int experimentIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setExperimenterAnnotationRef(java.lang.String, int, int)
     */
    public void setExperimenterAnnotationRef(String annotation,
            int experimenterIndex, int annotationRefIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setExperimenterDisplayName(java.lang.String, int)
     */
    public void setExperimenterDisplayName(String displayName,
            int experimenterIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setExperimenterEmail(java.lang.String, int)
     */
    public void setExperimenterEmail(String email, int experimenterIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setExperimenterFirstName(java.lang.String, int)
     */
    public void setExperimenterFirstName(String firstName, int experimenterIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setExperimenterGroupRef(java.lang.String, int, int)
     */
    public void setExperimenterGroupRef(String group, int experimenterIndex,
            int groupRefIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setExperimenterID(java.lang.String, int)
     */
    public void setExperimenterID(String id, int experimenterIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setExperimenterInstitution(java.lang.String, int)
     */
    public void setExperimenterInstitution(String institution,
            int experimenterIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setExperimenterLastName(java.lang.String, int)
     */
    public void setExperimenterLastName(String lastName, int experimenterIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setExperimenterMiddleName(java.lang.String, int)
     */
    public void setExperimenterMiddleName(String middleName,
            int experimenterIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setExperimenterUserName(java.lang.String, int)
     */
    public void setExperimenterUserName(String userName, int experimenterIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setFilamentID(java.lang.String, int, int)
     */
    public void setFilamentID(String id, int instrumentIndex,
            int lightSourceIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setFilamentLotNumber(java.lang.String, int, int)
     */
    public void setFilamentLotNumber(String lotNumber, int instrumentIndex,
            int lightSourceIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setFilamentManufacturer(java.lang.String, int, int)
     */
    public void setFilamentManufacturer(String manufacturer,
            int instrumentIndex, int lightSourceIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setFilamentModel(java.lang.String, int, int)
     */
    public void setFilamentModel(String model, int instrumentIndex,
            int lightSourceIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setFilamentPower(java.lang.Double, int, int)
     */
    public void setFilamentPower(Double power, int instrumentIndex,
            int lightSourceIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setFilamentSerialNumber(java.lang.String, int, int)
     */
    public void setFilamentSerialNumber(String serialNumber,
            int instrumentIndex, int lightSourceIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setFilamentType(ome.xml.r201004.enums.FilamentType, int, int)
     */
    public void setFilamentType(FilamentType type, int instrumentIndex,
            int lightSourceIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setFileAnnotationBinaryFileFileName(java.lang.String, int)
     */
    public void setFileAnnotationBinaryFileFileName(String fileName,
            int fileAnnotationIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setFileAnnotationBinaryFileMIMEType(java.lang.String, int)
     */
    public void setFileAnnotationBinaryFileMIMEType(String mimetype,
            int fileAnnotationIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setFileAnnotationBinaryFileSize(java.lang.Integer, int)
     */
    public void setFileAnnotationBinaryFileSize(Integer size,
            int fileAnnotationIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setFileAnnotationID(java.lang.String, int)
     */
    public void setFileAnnotationID(String id, int fileAnnotationIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setFileAnnotationNamespace(java.lang.String, int)
     */
    public void setFileAnnotationNamespace(String namespace,
            int fileAnnotationIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setFilterFilterWheel(java.lang.String, int, int)
     */
    public void setFilterFilterWheel(String filterWheel, int instrumentIndex,
            int filterIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setFilterID(java.lang.String, int, int)
     */
    public void setFilterID(String id, int instrumentIndex, int filterIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setFilterLotNumber(java.lang.String, int, int)
     */
    public void setFilterLotNumber(String lotNumber, int instrumentIndex,
            int filterIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setFilterManufacturer(java.lang.String, int, int)
     */
    public void setFilterManufacturer(String manufacturer, int instrumentIndex,
            int filterIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setFilterModel(java.lang.String, int, int)
     */
    public void setFilterModel(String model, int instrumentIndex,
            int filterIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setFilterSerialNumber(java.lang.String, int, int)
     */
    public void setFilterSerialNumber(String serialNumber, int instrumentIndex,
            int filterIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setFilterSetDichroicRef(java.lang.String, int, int)
     */
    public void setFilterSetDichroicRef(String dichroic, int instrumentIndex,
            int filterSetIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setFilterSetEmissionFilterRef(java.lang.String, int, int, int)
     */
    public void setFilterSetEmissionFilterRef(String emissionFilter,
            int instrumentIndex, int filterSetIndex, int emissionFilterRefIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setFilterSetExcitationFilterRef(java.lang.String, int, int, int)
     */
    public void setFilterSetExcitationFilterRef(String excitationFilter,
            int instrumentIndex, int filterSetIndex,
            int excitationFilterRefIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setFilterSetID(java.lang.String, int, int)
     */
    public void setFilterSetID(String id, int instrumentIndex,
            int filterSetIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setFilterSetLotNumber(java.lang.String, int, int)
     */
    public void setFilterSetLotNumber(String lotNumber, int instrumentIndex,
            int filterSetIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setFilterSetManufacturer(java.lang.String, int, int)
     */
    public void setFilterSetManufacturer(String manufacturer,
            int instrumentIndex, int filterSetIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setFilterSetModel(java.lang.String, int, int)
     */
    public void setFilterSetModel(String model, int instrumentIndex,
            int filterSetIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setFilterSetSerialNumber(java.lang.String, int, int)
     */
    public void setFilterSetSerialNumber(String serialNumber,
            int instrumentIndex, int filterSetIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setFilterType(ome.xml.r201004.enums.FilterType, int, int)
     */
    public void setFilterType(FilterType type, int instrumentIndex,
            int filterIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setGroupContact(java.lang.String, int)
     */
    public void setGroupContact(String contact, int groupIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setGroupDescription(java.lang.String, int)
     */
    public void setGroupDescription(String description, int groupIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setGroupID(java.lang.String, int)
     */
    public void setGroupID(String id, int groupIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setGroupLeader(java.lang.String, int)
     */
    public void setGroupLeader(String leader, int groupIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setGroupName(java.lang.String, int)
     */
    public void setGroupName(String name, int groupIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setImageAcquiredDate(java.lang.String, int)
     */
    public void setImageAcquiredDate(String acquiredDate, int imageIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setImageAnnotationRef(java.lang.String, int, int)
     */
    public void setImageAnnotationRef(String annotation, int imageIndex,
            int annotationRefIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setImageDatasetRef(java.lang.String, int, int)
     */
    public void setImageDatasetRef(String dataset, int imageIndex,
            int datasetRefIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setImageDescription(java.lang.String, int)
     */
    public void setImageDescription(String description, int imageIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setImageExperimentRef(java.lang.String, int)
     */
    public void setImageExperimentRef(String experiment, int imageIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setImageExperimenterRef(java.lang.String, int)
     */
    public void setImageExperimenterRef(String experimenter, int imageIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setImageGroupRef(java.lang.String, int)
     */
    public void setImageGroupRef(String group, int imageIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setImageID(java.lang.String, int)
     */
    public void setImageID(String id, int imageIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setImageInstrumentRef(java.lang.String, int)
     */
    public void setImageInstrumentRef(String instrument, int imageIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setImageMicrobeamManipulationRef(java.lang.String, int, int)
     */
    public void setImageMicrobeamManipulationRef(String microbeamManipulation,
            int imageIndex, int microbeamManipulationRefIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setImageName(java.lang.String, int)
     */
    public void setImageName(String name, int imageIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setImageObjectiveSettingsCorrectionCollar(java.lang.Double, int)
     */
    public void setImageObjectiveSettingsCorrectionCollar(
            Double correctionCollar, int imageIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setImageObjectiveSettingsID(java.lang.String, int)
     */
    public void setImageObjectiveSettingsID(String id, int imageIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setImageObjectiveSettingsMedium(ome.xml.r201004.enums.Medium, int)
     */
    public void setImageObjectiveSettingsMedium(Medium medium, int imageIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setImageObjectiveSettingsRefractiveIndex(java.lang.Double, int)
     */
    public void setImageObjectiveSettingsRefractiveIndex(
            Double refractiveIndex, int imageIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setImageROIRef(java.lang.String, int, int)
     */
    public void setImageROIRef(String roi, int imageIndex, int ROIRefIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setImagingEnvironmentAirPressure(java.lang.Double, int)
     */
    public void setImagingEnvironmentAirPressure(Double airPressure,
            int imageIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setImagingEnvironmentCO2Percent(ome.xml.r201004.primitives.PercentFraction, int)
     */
    public void setImagingEnvironmentCO2Percent(PercentFraction co2percent,
            int imageIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setImagingEnvironmentHumidity(ome.xml.r201004.primitives.PercentFraction, int)
     */
    public void setImagingEnvironmentHumidity(PercentFraction humidity,
            int imageIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setImagingEnvironmentTemperature(java.lang.Double, int)
     */
    public void setImagingEnvironmentTemperature(Double temperature,
            int imageIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setInstrumentID(java.lang.String, int)
     */
    public void setInstrumentID(String id, int instrumentIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setLaserFrequencyMultiplication(ome.xml.r201004.primitives.PositiveInteger, int, int)
     */
    public void setLaserFrequencyMultiplication(
            PositiveInteger frequencyMultiplication, int instrumentIndex,
            int lightSourceIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setLaserID(java.lang.String, int, int)
     */
    public void setLaserID(String id, int instrumentIndex, int lightSourceIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setLaserLaserMedium(ome.xml.r201004.enums.LaserMedium, int, int)
     */
    public void setLaserLaserMedium(LaserMedium laserMedium,
            int instrumentIndex, int lightSourceIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setLaserLotNumber(java.lang.String, int, int)
     */
    public void setLaserLotNumber(String lotNumber, int instrumentIndex,
            int lightSourceIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setLaserManufacturer(java.lang.String, int, int)
     */
    public void setLaserManufacturer(String manufacturer, int instrumentIndex,
            int lightSourceIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setLaserModel(java.lang.String, int, int)
     */
    public void setLaserModel(String model, int instrumentIndex,
            int lightSourceIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setLaserPockelCell(java.lang.Boolean, int, int)
     */
    public void setLaserPockelCell(Boolean pockelCell, int instrumentIndex,
            int lightSourceIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setLaserPower(java.lang.Double, int, int)
     */
    public void setLaserPower(Double power, int instrumentIndex,
            int lightSourceIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setLaserPulse(ome.xml.r201004.enums.Pulse, int, int)
     */
    public void setLaserPulse(Pulse pulse, int instrumentIndex,
            int lightSourceIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setLaserPump(java.lang.String, int, int)
     */
    public void setLaserPump(String pump, int instrumentIndex,
            int lightSourceIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setLaserRepetitionRate(java.lang.Double, int, int)
     */
    public void setLaserRepetitionRate(Double repetitionRate,
            int instrumentIndex, int lightSourceIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setLaserSerialNumber(java.lang.String, int, int)
     */
    public void setLaserSerialNumber(String serialNumber, int instrumentIndex,
            int lightSourceIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setLaserTuneable(java.lang.Boolean, int, int)
     */
    public void setLaserTuneable(Boolean tuneable, int instrumentIndex,
            int lightSourceIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setLaserType(ome.xml.r201004.enums.LaserType, int, int)
     */
    public void setLaserType(LaserType type, int instrumentIndex,
            int lightSourceIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setLaserWavelength(ome.xml.r201004.primitives.PositiveInteger, int, int)
     */
    public void setLaserWavelength(PositiveInteger wavelength,
            int instrumentIndex, int lightSourceIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setLightEmittingDiodeID(java.lang.String, int, int)
     */
    public void setLightEmittingDiodeID(String id, int instrumentIndex,
            int lightSourceIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setLightEmittingDiodeLotNumber(java.lang.String, int, int)
     */
    public void setLightEmittingDiodeLotNumber(String lotNumber,
            int instrumentIndex, int lightSourceIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setLightEmittingDiodeManufacturer(java.lang.String, int, int)
     */
    public void setLightEmittingDiodeManufacturer(String manufacturer,
            int instrumentIndex, int lightSourceIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setLightEmittingDiodeModel(java.lang.String, int, int)
     */
    public void setLightEmittingDiodeModel(String model, int instrumentIndex,
            int lightSourceIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setLightEmittingDiodePower(java.lang.Double, int, int)
     */
    public void setLightEmittingDiodePower(Double power, int instrumentIndex,
            int lightSourceIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setLightEmittingDiodeSerialNumber(java.lang.String, int, int)
     */
    public void setLightEmittingDiodeSerialNumber(String serialNumber,
            int instrumentIndex, int lightSourceIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setLightPathDichroicRef(java.lang.String, int, int)
     */
    public void setLightPathDichroicRef(String dichroic, int imageIndex,
            int channelIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setLightPathEmissionFilterRef(java.lang.String, int, int, int)
     */
    public void setLightPathEmissionFilterRef(String emissionFilter,
            int imageIndex, int channelIndex, int emissionFilterRefIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setLightPathExcitationFilterRef(java.lang.String, int, int, int)
     */
    public void setLightPathExcitationFilterRef(String excitationFilter,
            int imageIndex, int channelIndex, int excitationFilterRefIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setLineDescription(java.lang.String, int, int)
     */
    public void setLineDescription(String description, int ROIIndex,
            int shapeIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setLineFill(java.lang.Integer, int, int)
     */
    public void setLineFill(Integer fill, int ROIIndex, int shapeIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setLineFontSize(java.lang.Integer, int, int)
     */
    public void setLineFontSize(Integer fontSize, int ROIIndex, int shapeIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setLineID(java.lang.String, int, int)
     */
    public void setLineID(String id, int ROIIndex, int shapeIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setLineLabel(java.lang.String, int, int)
     */
    public void setLineLabel(String label, int ROIIndex, int shapeIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setLineName(java.lang.String, int, int)
     */
    public void setLineName(String name, int ROIIndex, int shapeIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setLineStroke(java.lang.Integer, int, int)
     */
    public void setLineStroke(Integer stroke, int ROIIndex, int shapeIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setLineStrokeDashArray(java.lang.String, int, int)
     */
    public void setLineStrokeDashArray(String strokeDashArray, int ROIIndex,
            int shapeIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setLineStrokeWidth(java.lang.Double, int, int)
     */
    public void setLineStrokeWidth(Double strokeWidth, int ROIIndex,
            int shapeIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setLineTheC(java.lang.Integer, int, int)
     */
    public void setLineTheC(Integer theC, int ROIIndex, int shapeIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setLineTheT(java.lang.Integer, int, int)
     */
    public void setLineTheT(Integer theT, int ROIIndex, int shapeIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setLineTheZ(java.lang.Integer, int, int)
     */
    public void setLineTheZ(Integer theZ, int ROIIndex, int shapeIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setLineTransform(java.lang.String, int, int)
     */
    public void setLineTransform(String transform, int ROIIndex, int shapeIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setLineX1(java.lang.Double, int, int)
     */
    public void setLineX1(Double x1, int ROIIndex, int shapeIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setLineX2(java.lang.Double, int, int)
     */
    public void setLineX2(Double x2, int ROIIndex, int shapeIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setLineY1(java.lang.Double, int, int)
     */
    public void setLineY1(Double y1, int ROIIndex, int shapeIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setLineY2(java.lang.Double, int, int)
     */
    public void setLineY2(Double y2, int ROIIndex, int shapeIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setListAnnotationAnnotationRef(java.lang.String, int, int)
     */
    public void setListAnnotationAnnotationRef(String annotation,
            int listAnnotationIndex, int annotationRefIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setListAnnotationID(java.lang.String, int)
     */
    public void setListAnnotationID(String id, int listAnnotationIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setListAnnotationNamespace(java.lang.String, int)
     */
    public void setListAnnotationNamespace(String namespace,
            int listAnnotationIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setLongAnnotationID(java.lang.String, int)
     */
    public void setLongAnnotationID(String id, int longAnnotationIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setLongAnnotationNamespace(java.lang.String, int)
     */
    public void setLongAnnotationNamespace(String namespace,
            int longAnnotationIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setLongAnnotationValue(java.lang.Long, int)
     */
    public void setLongAnnotationValue(Long value, int longAnnotationIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setMaskDescription(java.lang.String, int, int)
     */
    public void setMaskDescription(String description, int ROIIndex,
            int shapeIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setMaskFill(java.lang.Integer, int, int)
     */
    public void setMaskFill(Integer fill, int ROIIndex, int shapeIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setMaskFontSize(java.lang.Integer, int, int)
     */
    public void setMaskFontSize(Integer fontSize, int ROIIndex, int shapeIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setMaskID(java.lang.String, int, int)
     */
    public void setMaskID(String id, int ROIIndex, int shapeIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setMaskLabel(java.lang.String, int, int)
     */
    public void setMaskLabel(String label, int ROIIndex, int shapeIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setMaskName(java.lang.String, int, int)
     */
    public void setMaskName(String name, int ROIIndex, int shapeIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setMaskStroke(java.lang.Integer, int, int)
     */
    public void setMaskStroke(Integer stroke, int ROIIndex, int shapeIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setMaskStrokeDashArray(java.lang.String, int, int)
     */
    public void setMaskStrokeDashArray(String strokeDashArray, int ROIIndex,
            int shapeIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setMaskStrokeWidth(java.lang.Double, int, int)
     */
    public void setMaskStrokeWidth(Double strokeWidth, int ROIIndex,
            int shapeIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setMaskTheC(java.lang.Integer, int, int)
     */
    public void setMaskTheC(Integer theC, int ROIIndex, int shapeIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setMaskTheT(java.lang.Integer, int, int)
     */
    public void setMaskTheT(Integer theT, int ROIIndex, int shapeIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setMaskTheZ(java.lang.Integer, int, int)
     */
    public void setMaskTheZ(Integer theZ, int ROIIndex, int shapeIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setMaskTransform(java.lang.String, int, int)
     */
    public void setMaskTransform(String transform, int ROIIndex, int shapeIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setMaskX(java.lang.Double, int, int)
     */
    public void setMaskX(Double x, int ROIIndex, int shapeIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setMaskY(java.lang.Double, int, int)
     */
    public void setMaskY(Double y, int ROIIndex, int shapeIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setMicrobeamManipulationExperimenterRef(java.lang.String, int, int)
     */
    public void setMicrobeamManipulationExperimenterRef(String experimenter,
            int experimentIndex, int microbeamManipulationIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setMicrobeamManipulationID(java.lang.String, int, int)
     */
    public void setMicrobeamManipulationID(String id, int experimentIndex,
            int microbeamManipulationIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setMicrobeamManipulationLightSourceSettingsAttenuation(ome.xml.r201004.primitives.PercentFraction, int, int, int)
     */
    public void setMicrobeamManipulationLightSourceSettingsAttenuation(
            PercentFraction attenuation, int experimentIndex,
            int microbeamManipulationIndex, int lightSourceSettingsIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setMicrobeamManipulationLightSourceSettingsID(java.lang.String, int, int, int)
     */
    public void setMicrobeamManipulationLightSourceSettingsID(String id,
            int experimentIndex, int microbeamManipulationIndex,
            int lightSourceSettingsIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setMicrobeamManipulationLightSourceSettingsWavelength(ome.xml.r201004.primitives.PositiveInteger, int, int, int)
     */
    public void setMicrobeamManipulationLightSourceSettingsWavelength(
            PositiveInteger wavelength, int experimentIndex,
            int microbeamManipulationIndex, int lightSourceSettingsIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setMicrobeamManipulationROIRef(java.lang.String, int, int, int)
     */
    public void setMicrobeamManipulationROIRef(String roi, int experimentIndex,
            int microbeamManipulationIndex, int ROIRefIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setMicrobeamManipulationType(ome.xml.r201004.enums.MicrobeamManipulationType, int, int)
     */
    public void setMicrobeamManipulationType(MicrobeamManipulationType type,
            int experimentIndex, int microbeamManipulationIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setMicroscopeLotNumber(java.lang.String, int)
     */
    public void setMicroscopeLotNumber(String lotNumber, int instrumentIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setMicroscopeManufacturer(java.lang.String, int)
     */
    public void setMicroscopeManufacturer(String manufacturer,
            int instrumentIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setMicroscopeModel(java.lang.String, int)
     */
    public void setMicroscopeModel(String model, int instrumentIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setMicroscopeSerialNumber(java.lang.String, int)
     */
    public void setMicroscopeSerialNumber(String serialNumber,
            int instrumentIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setMicroscopeType(ome.xml.r201004.enums.MicroscopeType, int)
     */
    public void setMicroscopeType(MicroscopeType type, int instrumentIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setOTFBinaryFileFileName(java.lang.String, int, int)
     */
    public void setOTFBinaryFileFileName(String fileName, int instrumentIndex,
            int OTFIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setOTFBinaryFileMIMEType(java.lang.String, int, int)
     */
    public void setOTFBinaryFileMIMEType(String mimetype, int instrumentIndex,
            int OTFIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setOTFBinaryFileSize(java.lang.Integer, int, int)
     */
    public void setOTFBinaryFileSize(Integer size, int instrumentIndex,
            int OTFIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setOTFFilterSetRef(java.lang.String, int, int)
     */
    public void setOTFFilterSetRef(String filterSet, int instrumentIndex,
            int OTFIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setOTFID(java.lang.String, int, int)
     */
    public void setOTFID(String id, int instrumentIndex, int OTFIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setOTFObjectiveSettingsCorrectionCollar(java.lang.Double, int, int)
     */
    public void setOTFObjectiveSettingsCorrectionCollar(
            Double correctionCollar, int instrumentIndex, int OTFIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setOTFObjectiveSettingsID(java.lang.String, int, int)
     */
    public void setOTFObjectiveSettingsID(String id, int instrumentIndex,
            int OTFIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setOTFObjectiveSettingsMedium(ome.xml.r201004.enums.Medium, int, int)
     */
    public void setOTFObjectiveSettingsMedium(Medium medium,
            int instrumentIndex, int OTFIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setOTFObjectiveSettingsRefractiveIndex(java.lang.Double, int, int)
     */
    public void setOTFObjectiveSettingsRefractiveIndex(Double refractiveIndex,
            int instrumentIndex, int OTFIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setOTFOpticalAxisAveraged(java.lang.Boolean, int, int)
     */
    public void setOTFOpticalAxisAveraged(Boolean opticalAxisAveraged,
            int instrumentIndex, int OTFIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setOTFSizeX(ome.xml.r201004.primitives.PositiveInteger, int, int)
     */
    public void setOTFSizeX(PositiveInteger sizeX, int instrumentIndex,
            int OTFIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setOTFSizeY(ome.xml.r201004.primitives.PositiveInteger, int, int)
     */
    public void setOTFSizeY(PositiveInteger sizeY, int instrumentIndex,
            int OTFIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setOTFType(ome.xml.r201004.enums.PixelType, int, int)
     */
    public void setOTFType(PixelType type, int instrumentIndex, int OTFIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setObjectiveCalibratedMagnification(java.lang.Double, int, int)
     */
    public void setObjectiveCalibratedMagnification(
            Double calibratedMagnification, int instrumentIndex,
            int objectiveIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setObjectiveCorrection(ome.xml.r201004.enums.Correction, int, int)
     */
    public void setObjectiveCorrection(Correction correction,
            int instrumentIndex, int objectiveIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setObjectiveID(java.lang.String, int, int)
     */
    public void setObjectiveID(String id, int instrumentIndex,
            int objectiveIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setObjectiveImmersion(ome.xml.r201004.enums.Immersion, int, int)
     */
    public void setObjectiveImmersion(Immersion immersion, int instrumentIndex,
            int objectiveIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setObjectiveIris(java.lang.Boolean, int, int)
     */
    public void setObjectiveIris(Boolean iris, int instrumentIndex,
            int objectiveIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setObjectiveLensNA(java.lang.Double, int, int)
     */
    public void setObjectiveLensNA(Double lensNA, int instrumentIndex,
            int objectiveIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setObjectiveLotNumber(java.lang.String, int, int)
     */
    public void setObjectiveLotNumber(String lotNumber, int instrumentIndex,
            int objectiveIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setObjectiveManufacturer(java.lang.String, int, int)
     */
    public void setObjectiveManufacturer(String manufacturer,
            int instrumentIndex, int objectiveIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setObjectiveModel(java.lang.String, int, int)
     */
    public void setObjectiveModel(String model, int instrumentIndex,
            int objectiveIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setObjectiveNominalMagnification(java.lang.Integer, int, int)
     */
    public void setObjectiveNominalMagnification(Integer nominalMagnification,
            int instrumentIndex, int objectiveIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setObjectiveSerialNumber(java.lang.String, int, int)
     */
    public void setObjectiveSerialNumber(String serialNumber,
            int instrumentIndex, int objectiveIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setObjectiveWorkingDistance(java.lang.Double, int, int)
     */
    public void setObjectiveWorkingDistance(Double workingDistance,
            int instrumentIndex, int objectiveIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPathDefinition(java.lang.String, int, int)
     */
    public void setPathDefinition(String definition, int ROIIndex,
            int shapeIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPathDescription(java.lang.String, int, int)
     */
    public void setPathDescription(String description, int ROIIndex,
            int shapeIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPathFill(java.lang.Integer, int, int)
     */
    public void setPathFill(Integer fill, int ROIIndex, int shapeIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPathFontSize(java.lang.Integer, int, int)
     */
    public void setPathFontSize(Integer fontSize, int ROIIndex, int shapeIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPathID(java.lang.String, int, int)
     */
    public void setPathID(String id, int ROIIndex, int shapeIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPathLabel(java.lang.String, int, int)
     */
    public void setPathLabel(String label, int ROIIndex, int shapeIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPathName(java.lang.String, int, int)
     */
    public void setPathName(String name, int ROIIndex, int shapeIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPathStroke(java.lang.Integer, int, int)
     */
    public void setPathStroke(Integer stroke, int ROIIndex, int shapeIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPathStrokeDashArray(java.lang.String, int, int)
     */
    public void setPathStrokeDashArray(String strokeDashArray, int ROIIndex,
            int shapeIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPathStrokeWidth(java.lang.Double, int, int)
     */
    public void setPathStrokeWidth(Double strokeWidth, int ROIIndex,
            int shapeIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPathTheC(java.lang.Integer, int, int)
     */
    public void setPathTheC(Integer theC, int ROIIndex, int shapeIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPathTheT(java.lang.Integer, int, int)
     */
    public void setPathTheT(Integer theT, int ROIIndex, int shapeIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPathTheZ(java.lang.Integer, int, int)
     */
    public void setPathTheZ(Integer theZ, int ROIIndex, int shapeIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPathTransform(java.lang.String, int, int)
     */
    public void setPathTransform(String transform, int ROIIndex, int shapeIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPixelsAnnotationRef(java.lang.String, int, int)
     */
    public void setPixelsAnnotationRef(String annotation, int imageIndex,
            int annotationRefIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPixelsBinDataBigEndian(java.lang.Boolean, int, int)
     */
    public void setPixelsBinDataBigEndian(Boolean bigEndian, int imageIndex,
            int binDataIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPixelsDimensionOrder(ome.xml.r201004.enums.DimensionOrder, int)
     */
    public void setPixelsDimensionOrder(DimensionOrder dimensionOrder,
            int imageIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPixelsID(java.lang.String, int)
     */
    public void setPixelsID(String id, int imageIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPixelsPhysicalSizeX(java.lang.Double, int)
     */
    public void setPixelsPhysicalSizeX(Double physicalSizeX, int imageIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPixelsPhysicalSizeY(java.lang.Double, int)
     */
    public void setPixelsPhysicalSizeY(Double physicalSizeY, int imageIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPixelsPhysicalSizeZ(java.lang.Double, int)
     */
    public void setPixelsPhysicalSizeZ(Double physicalSizeZ, int imageIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPixelsSizeC(ome.xml.r201004.primitives.PositiveInteger, int)
     */
    public void setPixelsSizeC(PositiveInteger sizeC, int imageIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPixelsSizeT(ome.xml.r201004.primitives.PositiveInteger, int)
     */
    public void setPixelsSizeT(PositiveInteger sizeT, int imageIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPixelsSizeX(ome.xml.r201004.primitives.PositiveInteger, int)
     */
    public void setPixelsSizeX(PositiveInteger sizeX, int imageIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPixelsSizeY(ome.xml.r201004.primitives.PositiveInteger, int)
     */
    public void setPixelsSizeY(PositiveInteger sizeY, int imageIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPixelsSizeZ(ome.xml.r201004.primitives.PositiveInteger, int)
     */
    public void setPixelsSizeZ(PositiveInteger sizeZ, int imageIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPixelsTimeIncrement(java.lang.Double, int)
     */
    public void setPixelsTimeIncrement(Double timeIncrement, int imageIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPixelsType(ome.xml.r201004.enums.PixelType, int)
     */
    public void setPixelsType(PixelType type, int imageIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPlaneAnnotationRef(java.lang.String, int, int, int)
     */
    public void setPlaneAnnotationRef(String annotation, int imageIndex,
            int planeIndex, int annotationRefIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPlaneDeltaT(java.lang.Double, int, int)
     */
    public void setPlaneDeltaT(Double deltaT, int imageIndex, int planeIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPlaneExposureTime(java.lang.Double, int, int)
     */
    public void setPlaneExposureTime(Double exposureTime, int imageIndex,
            int planeIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPlaneHashSHA1(java.lang.String, int, int)
     */
    public void setPlaneHashSHA1(String hashSHA1, int imageIndex, int planeIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPlanePositionX(java.lang.Double, int, int)
     */
    public void setPlanePositionX(Double positionX, int imageIndex,
            int planeIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPlanePositionY(java.lang.Double, int, int)
     */
    public void setPlanePositionY(Double positionY, int imageIndex,
            int planeIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPlanePositionZ(java.lang.Double, int, int)
     */
    public void setPlanePositionZ(Double positionZ, int imageIndex,
            int planeIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPlaneTheC(java.lang.Integer, int, int)
     */
    public void setPlaneTheC(Integer theC, int imageIndex, int planeIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPlaneTheT(java.lang.Integer, int, int)
     */
    public void setPlaneTheT(Integer theT, int imageIndex, int planeIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPlaneTheZ(java.lang.Integer, int, int)
     */
    public void setPlaneTheZ(Integer theZ, int imageIndex, int planeIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPlateAcquisitionAnnotationRef(java.lang.String, int, int, int)
     */
    public void setPlateAcquisitionAnnotationRef(String annotation,
            int plateIndex, int plateAcquisitionIndex, int annotationRefIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPlateAcquisitionDescription(java.lang.String, int, int)
     */
    public void setPlateAcquisitionDescription(String description,
            int plateIndex, int plateAcquisitionIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPlateAcquisitionEndTime(java.lang.String, int, int)
     */
    public void setPlateAcquisitionEndTime(String endTime, int plateIndex,
            int plateAcquisitionIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPlateAcquisitionID(java.lang.String, int, int)
     */
    public void setPlateAcquisitionID(String id, int plateIndex,
            int plateAcquisitionIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPlateAcquisitionMaximumFieldCount(java.lang.Integer, int, int)
     */
    public void setPlateAcquisitionMaximumFieldCount(Integer maximumFieldCount,
            int plateIndex, int plateAcquisitionIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPlateAcquisitionName(java.lang.String, int, int)
     */
    public void setPlateAcquisitionName(String name, int plateIndex,
            int plateAcquisitionIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPlateAcquisitionStartTime(java.lang.String, int, int)
     */
    public void setPlateAcquisitionStartTime(String startTime, int plateIndex,
            int plateAcquisitionIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPlateAcquisitionWellSampleRef(java.lang.String, int, int, int)
     */
    public void setPlateAcquisitionWellSampleRef(String wellSample,
            int plateIndex, int plateAcquisitionIndex, int wellSampleRefIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPlateAnnotationRef(java.lang.String, int, int)
     */
    public void setPlateAnnotationRef(String annotation, int plateIndex,
            int annotationRefIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPlateColumnNamingConvention(ome.xml.r201004.enums.NamingConvention, int)
     */
    public void setPlateColumnNamingConvention(
            NamingConvention columnNamingConvention, int plateIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPlateColumns(java.lang.Integer, int)
     */
    public void setPlateColumns(Integer columns, int plateIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPlateDescription(java.lang.String, int)
     */
    public void setPlateDescription(String description, int plateIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPlateExternalIdentifier(java.lang.String, int)
     */
    public void setPlateExternalIdentifier(String externalIdentifier,
            int plateIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPlateID(java.lang.String, int)
     */
    public void setPlateID(String id, int plateIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPlateName(java.lang.String, int)
     */
    public void setPlateName(String name, int plateIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPlateRowNamingConvention(ome.xml.r201004.enums.NamingConvention, int)
     */
    public void setPlateRowNamingConvention(
            NamingConvention rowNamingConvention, int plateIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPlateRows(java.lang.Integer, int)
     */
    public void setPlateRows(Integer rows, int plateIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPlateScreenRef(java.lang.String, int, int)
     */
    public void setPlateScreenRef(String screen, int plateIndex,
            int screenRefIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPlateStatus(java.lang.String, int)
     */
    public void setPlateStatus(String status, int plateIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPlateWellOriginX(java.lang.Double, int)
     */
    public void setPlateWellOriginX(Double wellOriginX, int plateIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPlateWellOriginY(java.lang.Double, int)
     */
    public void setPlateWellOriginY(Double wellOriginY, int plateIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPointDescription(java.lang.String, int, int)
     */
    public void setPointDescription(String description, int ROIIndex,
            int shapeIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPointFill(java.lang.Integer, int, int)
     */
    public void setPointFill(Integer fill, int ROIIndex, int shapeIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPointFontSize(java.lang.Integer, int, int)
     */
    public void setPointFontSize(Integer fontSize, int ROIIndex, int shapeIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPointID(java.lang.String, int, int)
     */
    public void setPointID(String id, int ROIIndex, int shapeIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPointLabel(java.lang.String, int, int)
     */
    public void setPointLabel(String label, int ROIIndex, int shapeIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPointName(java.lang.String, int, int)
     */
    public void setPointName(String name, int ROIIndex, int shapeIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPointStroke(java.lang.Integer, int, int)
     */
    public void setPointStroke(Integer stroke, int ROIIndex, int shapeIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPointStrokeDashArray(java.lang.String, int, int)
     */
    public void setPointStrokeDashArray(String strokeDashArray, int ROIIndex,
            int shapeIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPointStrokeWidth(java.lang.Double, int, int)
     */
    public void setPointStrokeWidth(Double strokeWidth, int ROIIndex,
            int shapeIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPointTheC(java.lang.Integer, int, int)
     */
    public void setPointTheC(Integer theC, int ROIIndex, int shapeIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPointTheT(java.lang.Integer, int, int)
     */
    public void setPointTheT(Integer theT, int ROIIndex, int shapeIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPointTheZ(java.lang.Integer, int, int)
     */
    public void setPointTheZ(Integer theZ, int ROIIndex, int shapeIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPointTransform(java.lang.String, int, int)
     */
    public void setPointTransform(String transform, int ROIIndex, int shapeIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPointX(java.lang.Double, int, int)
     */
    public void setPointX(Double x, int ROIIndex, int shapeIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPointY(java.lang.Double, int, int)
     */
    public void setPointY(Double y, int ROIIndex, int shapeIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPolylineClosed(java.lang.Boolean, int, int)
     */
    public void setPolylineClosed(Boolean closed, int ROIIndex, int shapeIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPolylineDescription(java.lang.String, int, int)
     */
    public void setPolylineDescription(String description, int ROIIndex,
            int shapeIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPolylineFill(java.lang.Integer, int, int)
     */
    public void setPolylineFill(Integer fill, int ROIIndex, int shapeIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPolylineFontSize(java.lang.Integer, int, int)
     */
    public void setPolylineFontSize(Integer fontSize, int ROIIndex,
            int shapeIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPolylineID(java.lang.String, int, int)
     */
    public void setPolylineID(String id, int ROIIndex, int shapeIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPolylineLabel(java.lang.String, int, int)
     */
    public void setPolylineLabel(String label, int ROIIndex, int shapeIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPolylineName(java.lang.String, int, int)
     */
    public void setPolylineName(String name, int ROIIndex, int shapeIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPolylinePoints(java.lang.String, int, int)
     */
    public void setPolylinePoints(String points, int ROIIndex, int shapeIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPolylineStroke(java.lang.Integer, int, int)
     */
    public void setPolylineStroke(Integer stroke, int ROIIndex, int shapeIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPolylineStrokeDashArray(java.lang.String, int, int)
     */
    public void setPolylineStrokeDashArray(String strokeDashArray,
            int ROIIndex, int shapeIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPolylineStrokeWidth(java.lang.Double, int, int)
     */
    public void setPolylineStrokeWidth(Double strokeWidth, int ROIIndex,
            int shapeIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPolylineTheC(java.lang.Integer, int, int)
     */
    public void setPolylineTheC(Integer theC, int ROIIndex, int shapeIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPolylineTheT(java.lang.Integer, int, int)
     */
    public void setPolylineTheT(Integer theT, int ROIIndex, int shapeIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPolylineTheZ(java.lang.Integer, int, int)
     */
    public void setPolylineTheZ(Integer theZ, int ROIIndex, int shapeIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPolylineTransform(java.lang.String, int, int)
     */
    public void setPolylineTransform(String transform, int ROIIndex,
            int shapeIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setProjectAnnotationRef(java.lang.String, int, int)
     */
    public void setProjectAnnotationRef(String annotation, int projectIndex,
            int annotationRefIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setProjectDescription(java.lang.String, int)
     */
    public void setProjectDescription(String description, int projectIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setProjectExperimenterRef(java.lang.String, int)
     */
    public void setProjectExperimenterRef(String experimenter, int projectIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setProjectGroupRef(java.lang.String, int)
     */
    public void setProjectGroupRef(String group, int projectIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setProjectID(java.lang.String, int)
     */
    public void setProjectID(String id, int projectIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setProjectName(java.lang.String, int)
     */
    public void setProjectName(String name, int projectIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setROIAnnotationRef(java.lang.String, int, int)
     */
    public void setROIAnnotationRef(String annotation, int ROIIndex,
            int annotationRefIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setROIDescription(java.lang.String, int)
     */
    public void setROIDescription(String description, int ROIIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setROIID(java.lang.String, int)
     */
    public void setROIID(String id, int ROIIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setROIName(java.lang.String, int)
     */
    public void setROIName(String name, int ROIIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setROINamespace(java.lang.String, int)
     */
    public void setROINamespace(String namespace, int ROIIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setReagentAnnotationRef(java.lang.String, int, int, int)
     */
    public void setReagentAnnotationRef(String annotation, int screenIndex,
            int reagentIndex, int annotationRefIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setReagentDescription(java.lang.String, int, int)
     */
    public void setReagentDescription(String description, int screenIndex,
            int reagentIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setReagentID(java.lang.String, int, int)
     */
    public void setReagentID(String id, int screenIndex, int reagentIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setReagentName(java.lang.String, int, int)
     */
    public void setReagentName(String name, int screenIndex, int reagentIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setReagentReagentIdentifier(java.lang.String, int, int)
     */
    public void setReagentReagentIdentifier(String reagentIdentifier,
            int screenIndex, int reagentIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setRectangleDescription(java.lang.String, int, int)
     */
    public void setRectangleDescription(String description, int ROIIndex,
            int shapeIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setRectangleFill(java.lang.Integer, int, int)
     */
    public void setRectangleFill(Integer fill, int ROIIndex, int shapeIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setRectangleFontSize(java.lang.Integer, int, int)
     */
    public void setRectangleFontSize(Integer fontSize, int ROIIndex,
            int shapeIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setRectangleHeight(java.lang.Double, int, int)
     */
    public void setRectangleHeight(Double height, int ROIIndex, int shapeIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setRectangleID(java.lang.String, int, int)
     */
    public void setRectangleID(String id, int ROIIndex, int shapeIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setRectangleLabel(java.lang.String, int, int)
     */
    public void setRectangleLabel(String label, int ROIIndex, int shapeIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setRectangleName(java.lang.String, int, int)
     */
    public void setRectangleName(String name, int ROIIndex, int shapeIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setRectangleStroke(java.lang.Integer, int, int)
     */
    public void setRectangleStroke(Integer stroke, int ROIIndex, int shapeIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setRectangleStrokeDashArray(java.lang.String, int, int)
     */
    public void setRectangleStrokeDashArray(String strokeDashArray,
            int ROIIndex, int shapeIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setRectangleStrokeWidth(java.lang.Double, int, int)
     */
    public void setRectangleStrokeWidth(Double strokeWidth, int ROIIndex,
            int shapeIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setRectangleTheC(java.lang.Integer, int, int)
     */
    public void setRectangleTheC(Integer theC, int ROIIndex, int shapeIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setRectangleTheT(java.lang.Integer, int, int)
     */
    public void setRectangleTheT(Integer theT, int ROIIndex, int shapeIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setRectangleTheZ(java.lang.Integer, int, int)
     */
    public void setRectangleTheZ(Integer theZ, int ROIIndex, int shapeIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setRectangleTransform(java.lang.String, int, int)
     */
    public void setRectangleTransform(String transform, int ROIIndex,
            int shapeIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setRectangleWidth(java.lang.Double, int, int)
     */
    public void setRectangleWidth(Double width, int ROIIndex, int shapeIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setRectangleX(java.lang.Double, int, int)
     */
    public void setRectangleX(Double x, int ROIIndex, int shapeIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setRectangleY(java.lang.Double, int, int)
     */
    public void setRectangleY(Double y, int ROIIndex, int shapeIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setRoot(java.lang.Object)
     */
    public void setRoot(Object root)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setScreenAnnotationRef(java.lang.String, int, int)
     */
    public void setScreenAnnotationRef(String annotation, int screenIndex,
            int annotationRefIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setScreenDescription(java.lang.String, int)
     */
    public void setScreenDescription(String description, int screenIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setScreenID(java.lang.String, int)
     */
    public void setScreenID(String id, int screenIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setScreenName(java.lang.String, int)
     */
    public void setScreenName(String name, int screenIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setScreenPlateRef(java.lang.String, int, int)
     */
    public void setScreenPlateRef(String plate, int screenIndex,
            int plateRefIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setScreenProtocolDescription(java.lang.String, int)
     */
    public void setScreenProtocolDescription(String protocolDescription,
            int screenIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setScreenProtocolIdentifier(java.lang.String, int)
     */
    public void setScreenProtocolIdentifier(String protocolIdentifier,
            int screenIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setScreenReagentSetDescription(java.lang.String, int)
     */
    public void setScreenReagentSetDescription(String reagentSetDescription,
            int screenIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setScreenReagentSetIdentifier(java.lang.String, int)
     */
    public void setScreenReagentSetIdentifier(String reagentSetIdentifier,
            int screenIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setScreenType(java.lang.String, int)
     */
    public void setScreenType(String type, int screenIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setStageLabelName(java.lang.String, int)
     */
    public void setStageLabelName(String name, int imageIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setStageLabelX(java.lang.Double, int)
     */
    public void setStageLabelX(Double x, int imageIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setStageLabelY(java.lang.Double, int)
     */
    public void setStageLabelY(Double y, int imageIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setStageLabelZ(java.lang.Double, int)
     */
    public void setStageLabelZ(Double z, int imageIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setStringAnnotationID(java.lang.String, int)
     */
    public void setStringAnnotationID(String id, int stringAnnotationIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setStringAnnotationNamespace(java.lang.String, int)
     */
    public void setStringAnnotationNamespace(String namespace,
            int stringAnnotationIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setStringAnnotationValue(java.lang.String, int)
     */
    public void setStringAnnotationValue(String value, int stringAnnotationIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setTextDescription(java.lang.String, int, int)
     */
    public void setTextDescription(String description, int ROIIndex,
            int shapeIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setTextFill(java.lang.Integer, int, int)
     */
    public void setTextFill(Integer fill, int ROIIndex, int shapeIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setTextFontSize(java.lang.Integer, int, int)
     */
    public void setTextFontSize(Integer fontSize, int ROIIndex, int shapeIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setTextID(java.lang.String, int, int)
     */
    public void setTextID(String id, int ROIIndex, int shapeIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setTextLabel(java.lang.String, int, int)
     */
    public void setTextLabel(String label, int ROIIndex, int shapeIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setTextName(java.lang.String, int, int)
     */
    public void setTextName(String name, int ROIIndex, int shapeIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setTextStroke(java.lang.Integer, int, int)
     */
    public void setTextStroke(Integer stroke, int ROIIndex, int shapeIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setTextStrokeDashArray(java.lang.String, int, int)
     */
    public void setTextStrokeDashArray(String strokeDashArray, int ROIIndex,
            int shapeIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setTextStrokeWidth(java.lang.Double, int, int)
     */
    public void setTextStrokeWidth(Double strokeWidth, int ROIIndex,
            int shapeIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setTextTheC(java.lang.Integer, int, int)
     */
    public void setTextTheC(Integer theC, int ROIIndex, int shapeIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setTextTheT(java.lang.Integer, int, int)
     */
    public void setTextTheT(Integer theT, int ROIIndex, int shapeIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setTextTheZ(java.lang.Integer, int, int)
     */
    public void setTextTheZ(Integer theZ, int ROIIndex, int shapeIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setTextTransform(java.lang.String, int, int)
     */
    public void setTextTransform(String transform, int ROIIndex, int shapeIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setTextValue(java.lang.String, int, int)
     */
    public void setTextValue(String value, int ROIIndex, int shapeIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setTextX(java.lang.Double, int, int)
     */
    public void setTextX(Double x, int ROIIndex, int shapeIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setTextY(java.lang.Double, int, int)
     */
    public void setTextY(Double y, int ROIIndex, int shapeIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setTiffDataFirstC(java.lang.Integer, int, int)
     */
    public void setTiffDataFirstC(Integer firstC, int imageIndex,
            int tiffDataIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setTiffDataFirstT(java.lang.Integer, int, int)
     */
    public void setTiffDataFirstT(Integer firstT, int imageIndex,
            int tiffDataIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setTiffDataFirstZ(java.lang.Integer, int, int)
     */
    public void setTiffDataFirstZ(Integer firstZ, int imageIndex,
            int tiffDataIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setTiffDataIFD(java.lang.Integer, int, int)
     */
    public void setTiffDataIFD(Integer ifd, int imageIndex, int tiffDataIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setTiffDataPlaneCount(java.lang.Integer, int, int)
     */
    public void setTiffDataPlaneCount(Integer planeCount, int imageIndex,
            int tiffDataIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setTimestampAnnotationID(java.lang.String, int)
     */
    public void setTimestampAnnotationID(String id, int timestampAnnotationIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setTimestampAnnotationNamespace(java.lang.String, int)
     */
    public void setTimestampAnnotationNamespace(String namespace,
            int timestampAnnotationIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setTimestampAnnotationValue(java.lang.String, int)
     */
    public void setTimestampAnnotationValue(String value,
            int timestampAnnotationIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setTransmittanceRangeCutIn(java.lang.Integer, int, int)
     */
    public void setTransmittanceRangeCutIn(Integer cutIn, int instrumentIndex,
            int filterIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setTransmittanceRangeCutInTolerance(java.lang.Integer, int, int)
     */
    public void setTransmittanceRangeCutInTolerance(Integer cutInTolerance,
            int instrumentIndex, int filterIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setTransmittanceRangeCutOut(java.lang.Integer, int, int)
     */
    public void setTransmittanceRangeCutOut(Integer cutOut,
            int instrumentIndex, int filterIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setTransmittanceRangeCutOutTolerance(java.lang.Integer, int, int)
     */
    public void setTransmittanceRangeCutOutTolerance(Integer cutOutTolerance,
            int instrumentIndex, int filterIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setTransmittanceRangeTransmittance(ome.xml.r201004.primitives.PercentFraction, int, int)
     */
    public void setTransmittanceRangeTransmittance(
            PercentFraction transmittance, int instrumentIndex, int filterIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setUUID(java.lang.String)
     */
    public void setUUID(String uuid)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setUUIDFileName(java.lang.String, int, int)
     */
    public void setUUIDFileName(String fileName, int imageIndex,
            int tiffDataIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setUUIDValue(java.lang.String, int, int)
     */
    public void setUUIDValue(String fileName, int imageIndex,
            int tiffDataIndex)
    {
        // TODO not in OMERO Model
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setWellAnnotationRef(java.lang.String, int, int, int)
     */
    public void setWellAnnotationRef(String annotation, int plateIndex,
            int wellIndex, int annotationRefIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setWellColor(java.lang.Integer, int, int)
     */
    public void setWellColor(Integer color, int plateIndex, int wellIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setWellColumn(ome.xml.r201004.primitives.NonNegativeInteger, int, int)
     */
    public void setWellColumn(NonNegativeInteger column, int plateIndex,
            int wellIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setWellExternalDescription(java.lang.String, int, int)
     */
    public void setWellExternalDescription(String externalDescription,
            int plateIndex, int wellIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setWellExternalIdentifier(java.lang.String, int, int)
     */
    public void setWellExternalIdentifier(String externalIdentifier,
            int plateIndex, int wellIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setWellID(java.lang.String, int, int)
     */
    public void setWellID(String id, int plateIndex, int wellIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setWellReagentRef(java.lang.String, int, int)
     */
    public void setWellReagentRef(String reagent, int plateIndex, int wellIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setWellRow(ome.xml.r201004.primitives.NonNegativeInteger, int, int)
     */
    public void setWellRow(NonNegativeInteger row, int plateIndex, int wellIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setWellSampleAnnotationRef(java.lang.String, int, int, int, int)
     */
    public void setWellSampleAnnotationRef(String annotation, int plateIndex,
            int wellIndex, int wellSampleIndex, int annotationRefIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setWellSampleID(java.lang.String, int, int, int)
     */
    public void setWellSampleID(String id, int plateIndex, int wellIndex,
            int wellSampleIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setWellSampleImageRef(java.lang.String, int, int, int)
     */
    public void setWellSampleImageRef(String image, int plateIndex,
            int wellIndex, int wellSampleIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setWellSampleIndex(ome.xml.r201004.primitives.NonNegativeInteger, int, int, int)
     */
    public void setWellSampleIndex(NonNegativeInteger index, int plateIndex,
            int wellIndex, int wellSampleIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setWellSamplePositionX(java.lang.Double, int, int, int)
     */
    public void setWellSamplePositionX(Double positionX, int plateIndex,
            int wellIndex, int wellSampleIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setWellSamplePositionY(java.lang.Double, int, int, int)
     */
    public void setWellSamplePositionY(Double positionY, int plateIndex,
            int wellIndex, int wellSampleIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setWellSampleTimepoint(java.lang.Integer, int, int, int)
     */
    public void setWellSampleTimepoint(Integer timepoint, int plateIndex,
            int wellIndex, int wellSampleIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setWellStatus(java.lang.String, int, int)
     */
    public void setWellStatus(String status, int plateIndex, int wellIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setXMLAnnotationID(java.lang.String, int)
     */
    public void setXMLAnnotationID(String id, int XMLAnnotationIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setXMLAnnotationNamespace(java.lang.String, int)
     */
    public void setXMLAnnotationNamespace(String namespace,
            int XMLAnnotationIndex)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setXMLAnnotationValue(java.lang.String, int)
     */
    public void setXMLAnnotationValue(String value, int XMLAnnotationIndex)
    {
        // TODO Auto-generated method stub
        
    }
	
	///
	/// METHODS BELOW THIS POINT ARE STUBS
	///

}
