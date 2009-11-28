package ome.formats;

import static omero.rtypes.rstring;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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

import loci.formats.meta.MetadataStore;

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
		newColumns[IMAGE_COLUMN] = 
			new ImageColumn("Image", "", new long[length]);
		newColumns[ROI_COLUMN] = new RoiColumn("ROI", "", new long[length]);
		newColumns[COLOR_COLUMN] = 
			new LongColumn("Color", "", new long[length]);
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
		fileAnnotation.setNs(rstring(
				omero.constants.namespaces.NSMEASUREMENT.value));
		fileAnnotation.setFile(new OriginalFileI(tableFileId, false));
		fileAnnotation = (FileAnnotation) 
			updateService.saveAndReturnObject(fileAnnotation);
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
	
	///
	/// METHODS BELOW THIS POINT ARE STUBS
	///

	public void createRoot() {
		// Unused

	}

	public Object getRoot() {
		// Unused
		return null;
	}

	public void setArcType(String arg0, int arg1, int arg2) {
		// Unused

	}

	public void setChannelComponentColorDomain(String arg0, int arg1, int arg2,
			int arg3) {
		// Unused

	}

	public void setChannelComponentIndex(Integer arg0, int arg1, int arg2,
			int arg3) {
		// Unused

	}

	public void setChannelComponentPixels(String arg0, int arg1, int arg2,
			int arg3) {
		// Unused

	}

	public void setCircleCx(String arg0, int arg1, int arg2, int arg3) {
		// Unused

	}

	public void setCircleCy(String arg0, int arg1, int arg2, int arg3) {
		// Unused

	}

	public void setCircleID(String arg0, int arg1, int arg2, int arg3) {
		// Unused

	}

	public void setCircleR(String arg0, int arg1, int arg2, int arg3) {
		// Unused

	}

	public void setCircleTransform(String arg0, int arg1, int arg2, int arg3) {
		// Unused

	}

	public void setContactExperimenter(String arg0, int arg1) {
		// Unused

	}

	public void setDatasetDescription(String arg0, int arg1) {
		// Unused

	}

	public void setDatasetExperimenterRef(String arg0, int arg1) {
		// Unused

	}

	public void setDatasetGroupRef(String arg0, int arg1) {
		// Unused

	}

	public void setDatasetID(String arg0, int arg1) {
		// Unused

	}

	public void setDatasetLocked(Boolean arg0, int arg1) {
		// Unused

	}

	public void setDatasetName(String arg0, int arg1) {
		// Unused

	}

	public void setDatasetRefID(String arg0, int arg1, int arg2) {
		// Unused

	}

	public void setDetectorAmplificationGain(Float arg0, int arg1, int arg2) {
		// Unused

	}

	public void setDetectorGain(Float arg0, int arg1, int arg2) {
		// Unused

	}

	public void setDetectorID(String arg0, int arg1, int arg2) {
		// Unused

	}

	public void setDetectorManufacturer(String arg0, int arg1, int arg2) {
		// Unused

	}

	public void setDetectorModel(String arg0, int arg1, int arg2) {
		// Unused

	}

	public void setDetectorOffset(Float arg0, int arg1, int arg2) {
		// Unused

	}

	public void setDetectorSerialNumber(String arg0, int arg1, int arg2) {
		// Unused

	}

	public void setDetectorSettingsBinning(String arg0, int arg1, int arg2) {
		// Unused

	}

	public void setDetectorSettingsDetector(String arg0, int arg1, int arg2) {
		// Unused

	}

	public void setDetectorSettingsGain(Float arg0, int arg1, int arg2) {
		// Unused

	}

	public void setDetectorSettingsOffset(Float arg0, int arg1, int arg2) {
		// Unused

	}

	public void setDetectorSettingsReadOutRate(Float arg0, int arg1, int arg2) {
		// Unused

	}

	public void setDetectorSettingsVoltage(Float arg0, int arg1, int arg2) {
		// Unused

	}

	public void setDetectorType(String arg0, int arg1, int arg2) {
		// Unused

	}

	public void setDetectorVoltage(Float arg0, int arg1, int arg2) {
		// Unused

	}

	public void setDetectorZoom(Float arg0, int arg1, int arg2) {
		// Unused

	}

	public void setDichroicID(String arg0, int arg1, int arg2) {
		// Unused

	}

	public void setDichroicLotNumber(String arg0, int arg1, int arg2) {
		// Unused

	}

	public void setDichroicManufacturer(String arg0, int arg1, int arg2) {
		// Unused

	}

	public void setDichroicModel(String arg0, int arg1, int arg2) {
		// Unused

	}

	public void setDimensionsPhysicalSizeX(Float arg0, int arg1, int arg2) {
		// Unused

	}

	public void setDimensionsPhysicalSizeY(Float arg0, int arg1, int arg2) {
		// Unused

	}

	public void setDimensionsPhysicalSizeZ(Float arg0, int arg1, int arg2) {
		// Unused

	}

	public void setDimensionsTimeIncrement(Float arg0, int arg1, int arg2) {
		// Unused

	}

	public void setDimensionsWaveIncrement(Integer arg0, int arg1, int arg2) {
		// Unused

	}

	public void setDimensionsWaveStart(Integer arg0, int arg1, int arg2) {
		// Unused

	}

	public void setDisplayOptionsDisplay(String arg0, int arg1) {
		// Unused

	}

	public void setDisplayOptionsID(String arg0, int arg1) {
		// Unused

	}

	public void setDisplayOptionsZoom(Float arg0, int arg1) {
		// Unused

	}

	public void setEllipseCx(String arg0, int arg1, int arg2, int arg3) {
		// Unused

	}

	public void setEllipseCy(String arg0, int arg1, int arg2, int arg3) {
		// Unused

	}

	public void setEllipseID(String arg0, int arg1, int arg2, int arg3) {
		// Unused

	}

	public void setEllipseRx(String arg0, int arg1, int arg2, int arg3) {
		// Unused

	}

	public void setEllipseRy(String arg0, int arg1, int arg2, int arg3) {
		// Unused

	}

	public void setEllipseTransform(String arg0, int arg1, int arg2, int arg3) {
		// Unused

	}

	public void setEmFilterLotNumber(String arg0, int arg1, int arg2) {
		// Unused

	}

	public void setEmFilterManufacturer(String arg0, int arg1, int arg2) {
		// Unused

	}

	public void setEmFilterModel(String arg0, int arg1, int arg2) {
		// Unused

	}

	public void setEmFilterType(String arg0, int arg1, int arg2) {
		// Unused

	}

	public void setExFilterLotNumber(String arg0, int arg1, int arg2) {
		// Unused

	}

	public void setExFilterManufacturer(String arg0, int arg1, int arg2) {
		// Unused

	}

	public void setExFilterModel(String arg0, int arg1, int arg2) {
		// Unused

	}

	public void setExFilterType(String arg0, int arg1, int arg2) {
		// Unused

	}

	public void setExperimentDescription(String arg0, int arg1) {
		// Unused

	}

	public void setExperimentExperimenterRef(String arg0, int arg1) {
		// Unused

	}

	public void setExperimentID(String arg0, int arg1) {
		// Unused

	}

	public void setExperimentType(String arg0, int arg1) {
		// Unused

	}

	public void setExperimenterEmail(String arg0, int arg1) {
		// Unused

	}

	public void setExperimenterFirstName(String arg0, int arg1) {
		// Unused

	}

	public void setExperimenterID(String arg0, int arg1) {
		// Unused

	}

	public void setExperimenterInstitution(String arg0, int arg1) {
		// Unused

	}

	public void setExperimenterLastName(String arg0, int arg1) {
		// Unused

	}

	public void setExperimenterMembershipGroup(String arg0, int arg1, int arg2) {
		// Unused

	}

	public void setExperimenterOMEName(String arg0, int arg1) {
		// Unused

	}

	public void setFilamentType(String arg0, int arg1, int arg2) {
		// Unused

	}

	public void setFilterFilterWheel(String arg0, int arg1, int arg2) {
		// Unused

	}

	public void setFilterID(String arg0, int arg1, int arg2) {
		// Unused

	}

	public void setFilterLotNumber(String arg0, int arg1, int arg2) {
		// Unused

	}

	public void setFilterManufacturer(String arg0, int arg1, int arg2) {
		// Unused

	}

	public void setFilterModel(String arg0, int arg1, int arg2) {
		// Unused

	}

	public void setFilterSetDichroic(String arg0, int arg1, int arg2) {
		// Unused

	}

	public void setFilterSetEmFilter(String arg0, int arg1, int arg2) {
		// Unused

	}

	public void setFilterSetExFilter(String arg0, int arg1, int arg2) {
		// Unused

	}

	public void setFilterSetID(String arg0, int arg1, int arg2) {
		// Unused

	}

	public void setFilterSetLotNumber(String arg0, int arg1, int arg2) {
		// Unused

	}

	public void setFilterSetManufacturer(String arg0, int arg1, int arg2) {
		// Unused

	}

	public void setFilterSetModel(String arg0, int arg1, int arg2) {
		// Unused

	}

	public void setFilterType(String arg0, int arg1, int arg2) {
		// Unused

	}

	public void setGroupID(String arg0, int arg1) {
		// Unused

	}

	public void setGroupName(String arg0, int arg1) {
		// Unused

	}

	public void setImageAcquiredPixels(String arg0, int arg1) {
		// Unused

	}

	public void setImageCreationDate(String arg0, int arg1) {
		// Unused

	}

	public void setImageDefaultPixels(String arg0, int arg1) {
		// Unused

	}

	public void setImageDescription(String arg0, int arg1) {
		// Unused

	}

	public void setImageExperimentRef(String arg0, int arg1) {
		// Unused

	}

	public void setImageExperimenterRef(String arg0, int arg1) {
		// Unused

	}

	public void setImageGroupRef(String arg0, int arg1) {
		// Unused

	}

	public void setImageID(String arg0, int arg1) {
		// Unused

	}

	public void setImageInstrumentRef(String arg0, int arg1) {
		// Unused

	}

	public void setImageName(String arg0, int arg1) {
		// Unused

	}

	public void setImagingEnvironmentAirPressure(Float arg0, int arg1) {
		// Unused

	}

	public void setImagingEnvironmentCO2Percent(Float arg0, int arg1) {
		// Unused

	}

	public void setImagingEnvironmentHumidity(Float arg0, int arg1) {
		// Unused

	}

	public void setImagingEnvironmentTemperature(Float arg0, int arg1) {
		// Unused

	}

	public void setInstrumentID(String arg0, int arg1) {
		// Unused

	}

	public void setLaserFrequencyMultiplication(Integer arg0, int arg1, int arg2) {
		// Unused

	}

	public void setLaserLaserMedium(String arg0, int arg1, int arg2) {
		// Unused

	}

	public void setLaserPockelCell(Boolean arg0, int arg1, int arg2) {
		// Unused

	}

	public void setLaserPulse(String arg0, int arg1, int arg2) {
		// Unused

	}

	public void setLaserRepetitionRate(Boolean arg0, int arg1, int arg2) {
		// Unused

	}

	public void setLaserTuneable(Boolean arg0, int arg1, int arg2) {
		// Unused

	}

	public void setLaserType(String arg0, int arg1, int arg2) {
		// Unused

	}

	public void setLaserWavelength(Integer arg0, int arg1, int arg2) {
		// Unused

	}

	public void setLightSourceID(String arg0, int arg1, int arg2) {
		// Unused

	}

	public void setLightSourceManufacturer(String arg0, int arg1, int arg2) {
		// Unused

	}

	public void setLightSourceModel(String arg0, int arg1, int arg2) {
		// Unused

	}

	public void setLightSourcePower(Float arg0, int arg1, int arg2) {
		// Unused

	}

	public void setLightSourceRefAttenuation(Float arg0, int arg1, int arg2,
			int arg3) {
		// Unused

	}

	public void setLightSourceRefLightSource(String arg0, int arg1, int arg2,
			int arg3) {
		// Unused

	}

	public void setLightSourceRefWavelength(Integer arg0, int arg1, int arg2,
			int arg3) {
		// Unused

	}

	public void setLightSourceSerialNumber(String arg0, int arg1, int arg2) {
		// Unused

	}

	public void setLightSourceSettingsAttenuation(Float arg0, int arg1,
			int arg2) {
		// Unused

	}

	public void setLightSourceSettingsLightSource(String arg0, int arg1,
			int arg2) {
		// Unused

	}

	public void setLightSourceSettingsWavelength(Integer arg0, int arg1,
			int arg2) {
		// Unused

	}

	public void setLineID(String arg0, int arg1, int arg2, int arg3) {
		// Unused

	}

	public void setLineTransform(String arg0, int arg1, int arg2, int arg3) {
		// Unused

	}

	public void setLineX1(String arg0, int arg1, int arg2, int arg3) {
		// Unused

	}

	public void setLineX2(String arg0, int arg1, int arg2, int arg3) {
		// Unused

	}

	public void setLineY1(String arg0, int arg1, int arg2, int arg3) {
		// Unused

	}

	public void setLineY2(String arg0, int arg1, int arg2, int arg3) {
		// Unused

	}

	public void setLogicalChannelContrastMethod(String arg0, int arg1, int arg2) {
		// Unused

	}

	public void setLogicalChannelDetector(String arg0, int arg1, int arg2) {
		// Unused

	}

	public void setLogicalChannelEmWave(Integer arg0, int arg1, int arg2) {
		// Unused

	}

	public void setLogicalChannelExWave(Integer arg0, int arg1, int arg2) {
		// Unused

	}

	public void setLogicalChannelFilterSet(String arg0, int arg1, int arg2) {
		// Unused

	}

	public void setLogicalChannelFluor(String arg0, int arg1, int arg2) {
		// Unused

	}

	public void setLogicalChannelID(String arg0, int arg1, int arg2) {
		// Unused

	}

	public void setLogicalChannelIlluminationType(String arg0, int arg1,
			int arg2) {
		// Unused

	}

	public void setLogicalChannelLightSource(String arg0, int arg1, int arg2) {
		// Unused

	}

	public void setLogicalChannelMode(String arg0, int arg1, int arg2) {
		// Unused

	}

	public void setLogicalChannelName(String arg0, int arg1, int arg2) {
		// Unused

	}

	public void setLogicalChannelNdFilter(Float arg0, int arg1, int arg2) {
		// Unused

	}

	public void setLogicalChannelOTF(String arg0, int arg1, int arg2) {
		// Unused

	}

	public void setLogicalChannelPhotometricInterpretation(String arg0,
			int arg1, int arg2) {
		// Unused

	}

	public void setLogicalChannelPinholeSize(Float arg0, int arg1, int arg2) {
		// Unused

	}

	public void setLogicalChannelPockelCellSetting(Integer arg0, int arg1,
			int arg2) {
		// Unused

	}

	public void setLogicalChannelSamplesPerPixel(Integer arg0, int arg1,
			int arg2) {
		// Unused

	}

	public void setLogicalChannelSecondaryEmissionFilter(String arg0, int arg1,
			int arg2) {
		// Unused

	}

	public void setLogicalChannelSecondaryExcitationFilter(String arg0,
			int arg1, int arg2) {
		// Unused

	}

	public void setMicrobeamManipulationExperimenterRef(String arg0, int arg1,
			int arg2) {
		// Unused

	}

	public void setMicrobeamManipulationID(String arg0, int arg1, int arg2) {
		// Unused

	}

	public void setMicrobeamManipulationRefID(String arg0, int arg1, int arg2) {
		// Unused

	}

	public void setMicrobeamManipulationType(String arg0, int arg1, int arg2) {
		// Unused

	}

	public void setMicroscopeID(String arg0, int arg1) {
		// Unused

	}

	public void setMicroscopeManufacturer(String arg0, int arg1) {
		// Unused

	}

	public void setMicroscopeModel(String arg0, int arg1) {
		// Unused

	}

	public void setMicroscopeSerialNumber(String arg0, int arg1) {
		// Unused

	}

	public void setMicroscopeType(String arg0, int arg1) {
		// Unused

	}

	public void setOTFBinaryFile(String arg0, int arg1, int arg2) {
		// Unused

	}

	public void setOTFID(String arg0, int arg1, int arg2) {
		// Unused

	}

	public void setOTFObjective(String arg0, int arg1, int arg2) {
		// Unused

	}

	public void setOTFOpticalAxisAveraged(Boolean arg0, int arg1, int arg2) {
		// Unused

	}

	public void setOTFPixelType(String arg0, int arg1, int arg2) {
		// Unused

	}

	public void setOTFSizeX(Integer arg0, int arg1, int arg2) {
		// Unused

	}

	public void setOTFSizeY(Integer arg0, int arg1, int arg2) {
		// Unused

	}

	public void setObjectiveCalibratedMagnification(Float arg0, int arg1,
			int arg2) {
		// Unused

	}

	public void setObjectiveCorrection(String arg0, int arg1, int arg2) {
		// Unused

	}

	public void setObjectiveID(String arg0, int arg1, int arg2) {
		// Unused

	}

	public void setObjectiveImmersion(String arg0, int arg1, int arg2) {
		// Unused

	}

	public void setObjectiveIris(Boolean arg0, int arg1, int arg2) {
		// Unused

	}

	public void setObjectiveLensNA(Float arg0, int arg1, int arg2) {
		// Unused

	}

	public void setObjectiveManufacturer(String arg0, int arg1, int arg2) {
		// Unused

	}

	public void setObjectiveModel(String arg0, int arg1, int arg2) {
		// Unused

	}

	public void setObjectiveNominalMagnification(Integer arg0, int arg1,
			int arg2) {
		// Unused

	}

	public void setObjectiveSerialNumber(String arg0, int arg1, int arg2) {
		// Unused

	}

	public void setObjectiveSettingsCorrectionCollar(Float arg0, int arg1) {
		// Unused

	}

	public void setObjectiveSettingsMedium(String arg0, int arg1) {
		// Unused

	}

	public void setObjectiveSettingsObjective(String arg0, int arg1) {
		// Unused

	}

	public void setObjectiveSettingsRefractiveIndex(Float arg0, int arg1) {
		// Unused

	}

	public void setObjectiveWorkingDistance(Float arg0, int arg1, int arg2) {
		// Unused

	}

	public void setPathD(String arg0, int arg1, int arg2, int arg3) {
		// Unused

	}

	public void setPathID(String arg0, int arg1, int arg2, int arg3) {
		// Unused

	}

	public void setPixelsBigEndian(Boolean arg0, int arg1, int arg2) {
		// Unused

	}

	public void setPixelsDimensionOrder(String arg0, int arg1, int arg2) {
		// Unused

	}

	public void setPixelsID(String arg0, int arg1, int arg2) {
		// Unused

	}

	public void setPixelsPixelType(String arg0, int arg1, int arg2) {
		// Unused

	}

	public void setPixelsSizeC(Integer arg0, int arg1, int arg2) {
		// Unused

	}

	public void setPixelsSizeT(Integer arg0, int arg1, int arg2) {
		// Unused

	}

	public void setPixelsSizeX(Integer arg0, int arg1, int arg2) {
		// Unused

	}

	public void setPixelsSizeY(Integer arg0, int arg1, int arg2) {
		// Unused

	}

	public void setPixelsSizeZ(Integer arg0, int arg1, int arg2) {
		// Unused

	}

	public void setPlaneHashSHA1(String arg0, int arg1, int arg2, int arg3) {
		// Unused

	}

	public void setPlaneID(String arg0, int arg1, int arg2, int arg3) {
		// Unused

	}

	public void setPlaneTheC(Integer arg0, int arg1, int arg2, int arg3) {
		// Unused

	}

	public void setPlaneTheT(Integer arg0, int arg1, int arg2, int arg3) {
		// Unused

	}

	public void setPlaneTheZ(Integer arg0, int arg1, int arg2, int arg3) {
		// Unused

	}

	public void setPlaneTimingDeltaT(Float arg0, int arg1, int arg2, int arg3) {
		// Unused

	}

	public void setPlaneTimingExposureTime(Float arg0, int arg1, int arg2,
			int arg3) {
		// Unused

	}

	public void setPlateColumnNamingConvention(String arg0, int arg1) {
		// Unused

	}

	public void setPlateDescription(String arg0, int arg1) {
		// Unused

	}

	public void setPlateExternalIdentifier(String arg0, int arg1) {
		// Unused

	}

	public void setPlateID(String arg0, int arg1) {
		// Unused

	}

	public void setPlateName(String arg0, int arg1) {
		// Unused

	}

	public void setPlateRefID(String arg0, int arg1, int arg2) {
		// Unused

	}

	public void setPlateRefSample(Integer arg0, int arg1, int arg2) {
		// Unused

	}

	public void setPlateRefWell(String arg0, int arg1, int arg2) {
		// Unused

	}

	public void setPlateRowNamingConvention(String arg0, int arg1) {
		// Unused

	}

	public void setPlateStatus(String arg0, int arg1) {
		// Unused

	}

	public void setPlateWellOriginX(Double arg0, int arg1) {
		// Unused

	}

	public void setPlateWellOriginY(Double arg0, int arg1) {
		// Unused

	}

	public void setPointCx(String arg0, int arg1, int arg2, int arg3) {
		// Unused

	}

	public void setPointCy(String arg0, int arg1, int arg2, int arg3) {
		// Unused

	}

	public void setPointID(String arg0, int arg1, int arg2, int arg3) {
		// Unused

	}

	public void setPointR(String arg0, int arg1, int arg2, int arg3) {
		// Unused

	}

	public void setPointTransform(String arg0, int arg1, int arg2, int arg3) {
		// Unused

	}

	public void setPolygonID(String arg0, int arg1, int arg2, int arg3) {
		// Unused

	}

	public void setPolygonPoints(String arg0, int arg1, int arg2, int arg3) {
		// Unused

	}

	public void setPolygonTransform(String arg0, int arg1, int arg2, int arg3) {
		// Unused

	}

	public void setPolylineID(String arg0, int arg1, int arg2, int arg3) {
		// Unused

	}

	public void setPolylinePoints(String arg0, int arg1, int arg2, int arg3) {
		// Unused

	}

	public void setPolylineTransform(String arg0, int arg1, int arg2, int arg3) {
		// Unused

	}

	public void setProjectDescription(String arg0, int arg1) {
		// Unused

	}

	public void setProjectExperimenterRef(String arg0, int arg1) {
		// Unused

	}

	public void setProjectGroupRef(String arg0, int arg1) {
		// Unused

	}

	public void setProjectID(String arg0, int arg1) {
		// Unused

	}

	public void setProjectName(String arg0, int arg1) {
		// Unused

	}

	public void setProjectRefID(String arg0, int arg1, int arg2) {
		// Unused

	}

	public void setPumpLightSource(String arg0, int arg1, int arg2) {
		// Unused

	}

	public void setROIID(String arg0, int arg1, int arg2) {
		// Unused

	}

	public void setROIRefID(String arg0, int arg1, int arg2, int arg3) {
		// Unused

	}

	public void setROIT0(Integer arg0, int arg1, int arg2) {
		// Unused

	}

	public void setROIT1(Integer arg0, int arg1, int arg2) {
		// Unused

	}

	public void setROIX0(Integer arg0, int arg1, int arg2) {
		// Unused

	}

	public void setROIX1(Integer arg0, int arg1, int arg2) {
		// Unused

	}

	public void setROIY0(Integer arg0, int arg1, int arg2) {
		// Unused

	}

	public void setROIY1(Integer arg0, int arg1, int arg2) {
		// Unused

	}

	public void setROIZ0(Integer arg0, int arg1, int arg2) {
		// Unused

	}

	public void setROIZ1(Integer arg0, int arg1, int arg2) {
		// Unused

	}

	public void setReagentDescription(String arg0, int arg1, int arg2) {
		// Unused

	}

	public void setReagentID(String arg0, int arg1, int arg2) {
		// Unused

	}

	public void setReagentName(String arg0, int arg1, int arg2) {
		// Unused

	}

	public void setReagentReagentIdentifier(String arg0, int arg1, int arg2) {
		// Unused

	}

	public void setRectHeight(String arg0, int arg1, int arg2, int arg3) {
		// Unused

	}

	public void setRectID(String arg0, int arg1, int arg2, int arg3) {
		// Unused

	}

	public void setRectTransform(String arg0, int arg1, int arg2, int arg3) {
		// Unused

	}

	public void setRectWidth(String arg0, int arg1, int arg2, int arg3) {
		// Unused

	}

	public void setRectX(String arg0, int arg1, int arg2, int arg3) {
		// Unused

	}

	public void setRectY(String arg0, int arg1, int arg2, int arg3) {
		// Unused

	}

	public void setRegionID(String arg0, int arg1, int arg2) {
		// Unused

	}

	public void setRegionName(String arg0, int arg1, int arg2) {
		// Unused

	}

	public void setRegionTag(String arg0, int arg1, int arg2) {
		// Unused

	}

	public void setRoiLinkDirection(String arg0, int arg1, int arg2, int arg3) {
		// Unused

	}

	public void setRoiLinkName(String arg0, int arg1, int arg2, int arg3) {
		// Unused

	}

	public void setRoiLinkRef(String arg0, int arg1, int arg2, int arg3) {
		// Unused

	}

	public void setRoot(Object arg0) {
		// Unused

	}

	public void setScreenAcquisitionEndTime(String arg0, int arg1, int arg2) {
		// Unused

	}

	public void setScreenAcquisitionID(String arg0, int arg1, int arg2) {
		// Unused

	}

	public void setScreenAcquisitionStartTime(String arg0, int arg1, int arg2) {
		// Unused

	}

	public void setScreenDescription(String arg0, int arg1) {
		// Unused

	}

	public void setScreenExtern(String arg0, int arg1) {
		// Unused

	}

	public void setScreenID(String arg0, int arg1) {
		// Unused

	}

	public void setScreenName(String arg0, int arg1) {
		// Unused

	}

	public void setScreenProtocolDescription(String arg0, int arg1) {
		// Unused

	}

	public void setScreenProtocolIdentifier(String arg0, int arg1) {
		// Unused

	}

	public void setScreenReagentSetDescription(String arg0, int arg1) {
		// Unused

	}

	public void setScreenReagentSetIdentifier(String arg0, int arg1) {
		// Unused

	}

	public void setScreenRefID(String arg0, int arg1, int arg2) {
		// Unused

	}

	public void setScreenType(String arg0, int arg1) {
		// Unused

	}

	public void setShapeBaselineShift(String arg0, int arg1, int arg2, int arg3) {
		// Unused

	}

	public void setShapeDirection(String arg0, int arg1, int arg2, int arg3) {
		// Unused

	}

	public void setShapeFillColor(String arg0, int arg1, int arg2, int arg3) {
		// Unused

	}

	public void setShapeFillOpacity(String arg0, int arg1, int arg2, int arg3) {
		// Unused

	}

	public void setShapeFillRule(String arg0, int arg1, int arg2, int arg3) {
		// Unused

	}

	public void setShapeFontFamily(String arg0, int arg1, int arg2, int arg3) {
		// Unused

	}

	public void setShapeFontSize(Integer arg0, int arg1, int arg2, int arg3) {
		// Unused

	}

	public void setShapeFontStretch(String arg0, int arg1, int arg2, int arg3) {
		// Unused

	}

	public void setShapeFontStyle(String arg0, int arg1, int arg2, int arg3) {
		// Unused

	}

	public void setShapeFontVariant(String arg0, int arg1, int arg2, int arg3) {
		// Unused

	}

	public void setShapeFontWeight(String arg0, int arg1, int arg2, int arg3) {
		// Unused

	}

	public void setShapeG(String arg0, int arg1, int arg2, int arg3) {
		// Unused

	}

	public void setShapeGlyphOrientationVertical(Integer arg0, int arg1,
			int arg2, int arg3) {
		// Unused

	}

	public void setShapeID(String arg0, int arg1, int arg2, int arg3) {
		// Unused

	}

	public void setShapeLocked(Boolean arg0, int arg1, int arg2, int arg3) {
		// Unused

	}

	public void setShapeStrokeAttribute(String arg0, int arg1, int arg2,
			int arg3) {
		// Unused

	}

	public void setShapeStrokeDashArray(String arg0, int arg1, int arg2,
			int arg3) {
		// Unused

	}

	public void setShapeStrokeLineCap(String arg0, int arg1, int arg2, int arg3) {
		// Unused

	}

	public void setShapeStrokeLineJoin(String arg0, int arg1, int arg2, int arg3) {
		// Unused

	}

	public void setShapeStrokeMiterLimit(Integer arg0, int arg1, int arg2,
			int arg3) {
		// Unused

	}

	public void setShapeStrokeOpacity(Float arg0, int arg1, int arg2, int arg3) {
		// Unused

	}

	public void setShapeStrokeWidth(Integer arg0, int arg1, int arg2, int arg3) {
		// Unused

	}

	public void setShapeText(String arg0, int arg1, int arg2, int arg3) {
		// Unused

	}

	public void setShapeTextAnchor(String arg0, int arg1, int arg2, int arg3) {
		// Unused

	}

	public void setShapeTextDecoration(String arg0, int arg1, int arg2, int arg3) {
		// Unused

	}

	public void setShapeTextFill(String arg0, int arg1, int arg2, int arg3) {
		// Unused

	}

	public void setShapeTextStroke(String arg0, int arg1, int arg2, int arg3) {
		// Unused

	}

	public void setShapeTheT(Integer arg0, int arg1, int arg2, int arg3) {
		// Unused

	}

	public void setShapeTheZ(Integer arg0, int arg1, int arg2, int arg3) {
		// Unused

	}

	public void setShapeVectorEffect(String arg0, int arg1, int arg2, int arg3) {
		// Unused

	}

	public void setShapeVisibility(Boolean arg0, int arg1, int arg2, int arg3) {
		// Unused

	}

	public void setShapeWritingMode(String arg0, int arg1, int arg2, int arg3) {
		// Unused

	}

	public void setStageLabelName(String arg0, int arg1) {
		// Unused

	}

	public void setStageLabelX(Float arg0, int arg1) {
		// Unused

	}

	public void setStageLabelY(Float arg0, int arg1) {
		// Unused

	}

	public void setStageLabelZ(Float arg0, int arg1) {
		// Unused

	}

	public void setStagePositionPositionX(Float arg0, int arg1, int arg2,
			int arg3) {
		// Unused

	}

	public void setStagePositionPositionY(Float arg0, int arg1, int arg2,
			int arg3) {
		// Unused

	}

	public void setStagePositionPositionZ(Float arg0, int arg1, int arg2,
			int arg3) {
		// Unused

	}

	public void setThumbnailHref(String arg0, int arg1) {
		// Unused

	}

	public void setThumbnailID(String arg0, int arg1) {
		// Unused

	}

	public void setThumbnailMIMEtype(String arg0, int arg1) {
		// Unused

	}

	public void setTiffDataFileName(String arg0, int arg1, int arg2, int arg3) {
		// Unused

	}

	public void setTiffDataFirstC(Integer arg0, int arg1, int arg2, int arg3) {
		// Unused

	}

	public void setTiffDataFirstT(Integer arg0, int arg1, int arg2, int arg3) {
		// Unused

	}

	public void setTiffDataFirstZ(Integer arg0, int arg1, int arg2, int arg3) {
		// Unused

	}

	public void setTiffDataIFD(Integer arg0, int arg1, int arg2, int arg3) {
		// Unused

	}

	public void setTiffDataNumPlanes(Integer arg0, int arg1, int arg2, int arg3) {
		// Unused

	}

	public void setTiffDataUUID(String arg0, int arg1, int arg2, int arg3) {
		// Unused

	}

	public void setTransmittanceRangeCutIn(Integer arg0, int arg1, int arg2) {
		// Unused

	}

	public void setTransmittanceRangeCutInTolerance(Integer arg0, int arg1,
			int arg2) {
		// Unused

	}

	public void setTransmittanceRangeCutOut(Integer arg0, int arg1, int arg2) {
		// Unused

	}

	public void setTransmittanceRangeCutOutTolerance(Integer arg0, int arg1,
			int arg2) {
		// Unused

	}

	public void setTransmittanceRangeTransmittance(Integer arg0, int arg1,
			int arg2) {
		// Unused

	}

	public void setUUID(String arg0) {
		// Unused

	}

	public void setWellColumn(Integer arg0, int arg1, int arg2) {
		// Unused

	}

	public void setWellExternalDescription(String arg0, int arg1, int arg2) {
		// Unused

	}

	public void setWellExternalIdentifier(String arg0, int arg1, int arg2) {
		// Unused

	}

	public void setWellID(String arg0, int arg1, int arg2) {
		// Unused

	}

	public void setWellReagent(String arg0, int arg1, int arg2) {
		// Unused

	}

	public void setWellRow(Integer arg0, int arg1, int arg2) {
		// Unused

	}

	public void setWellSampleID(String arg0, int arg1, int arg2, int arg3) {
		// Unused

	}

	public void setWellSampleImageRef(String arg0, int arg1, int arg2, int arg3) {
		// Unused

	}

	public void setWellSampleIndex(Integer arg0, int arg1, int arg2, int arg3) {
		// Unused

	}

	public void setWellSamplePosX(Float arg0, int arg1, int arg2, int arg3) {
		// Unused

	}

	public void setWellSamplePosY(Float arg0, int arg1, int arg2, int arg3) {
		// Unused

	}

	public void setWellSampleRefID(String arg0, int arg1, int arg2, int arg3) {
		// Unused

	}

	public void setWellSampleTimepoint(Integer arg0, int arg1, int arg2,
			int arg3) {
		// Unused

	}

	public void setWellType(String arg0, int arg1, int arg2) {
		// Unused

	}

}
