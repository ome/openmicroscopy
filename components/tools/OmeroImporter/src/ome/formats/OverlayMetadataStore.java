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

import loci.formats.meta.DummyMetadata;
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
public class OverlayMetadataStore extends DummyMetadata {
	
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

	@Override
	public void setMaskStroke(Integer stroke, int roiIndex, int shapeIndex) {
	    // FIXME: Everything below is now broken
	    //long imageId = pixelsList.get(imageIndex).getImage().getId().getValue();
	    //int index = getTableIndex(imageIndex, roiIndex, shapeIndex);
	    MaskColumn maskColumn = (MaskColumn) columns[MASK_COLUMN];
	    LongColumn colorColumn = (LongColumn) columns[COLOR_COLUMN];
	    ImageColumn imageColumn = (ImageColumn) columns[IMAGE_COLUMN];
	    //imageColumn.values[index] = imageId;
	    //maskColumn.imageId[index] = imageId;
	    //colorColumn.values[index] = Integer.parseInt(strokeColor);
	}

	@Override
	public void setMaskHeight(Double height, int roiIndex, int shapeIndex) {
	    // FIXME: Everything below is now broken
	    //long imageId = pixelsList.get(imageIndex).getImage().getId().getValue();
	    //int index = getTableIndex(imageIndex, roiIndex, shapeIndex);
	    MaskColumn maskColumn = (MaskColumn) columns[MASK_COLUMN];
	    ImageColumn imageColumn = (ImageColumn) columns[IMAGE_COLUMN];
	    //imageColumn.values[index] = imageId;
	    //maskColumn.imageId[index] = imageId;
	    //maskColumn.h[index] = height;
	}

	@Override
	public void setMaskWidth(Double width, int roiIndex, int shapeIndex) {
	    // FIXME: Everything below is now broken
	    //long imageId = pixelsList.get(imageIndex).getImage().getId().getValue();
	    //int index = getTableIndex(imageIndex, roiIndex, shapeIndex);
	    MaskColumn maskColumn = (MaskColumn) columns[MASK_COLUMN];
	    ImageColumn imageColumn = (ImageColumn) columns[IMAGE_COLUMN];
	    //imageColumn.values[index] = imageId;
	    //maskColumn.imageId[index] = imageId;
	    //maskColumn.w[index] = Double.parseDouble(width);
	}

	@Override
	public void setMaskX(Double x, int roiIndex, int shapeIndex) {
	    // FIXME: Everything below is now broken
	    //long imageId = pixelsList.get(imageIndex).getImage().getId().getValue();
	    //int index = getTableIndex(imageIndex, roiIndex, shapeIndex);
	    MaskColumn maskColumn = (MaskColumn) columns[MASK_COLUMN];
	    ImageColumn imageColumn = (ImageColumn) columns[IMAGE_COLUMN];
	    //imageColumn.values[index] = imageId;
	    //maskColumn.imageId[index] = imageId;
	    //maskColumn.x[index] = Double.parseDouble(x);
	}

	@Override
	public void setMaskY(Double y, int roiIndex, int shapeIndex) {
	    // FIXME: Everything below is now broken
	    //long imageId = pixelsList.get(imageIndex).getImage().getId().getValue();
	    //int index = getTableIndex(imageIndex, roiIndex, shapeIndex);
	    MaskColumn maskColumn = (MaskColumn) columns[MASK_COLUMN];
	    ImageColumn imageColumn = (ImageColumn) columns[IMAGE_COLUMN];
	    //imageColumn.values[index] = imageId;
	    //maskColumn.imageId[index] = imageId;
	    //maskColumn.y[index] = Double.parseDouble(y);
	}

	/*
	FIXME: Needs to be re-added to the stack
	@Override
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
	*/
}
