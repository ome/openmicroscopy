/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2015 University of Dundee. All rights reserved.
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
 */
package org.openmicroscopy.shoola.agents.measurement.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.apache.commons.collections.CollectionUtils;
import org.jhotdraw.draw.AttributeKey;
import org.jhotdraw.draw.Drawing;
import org.jhotdraw.draw.DrawingEditor;
import org.jhotdraw.draw.Figure;
import org.openmicroscopy.shoola.agents.events.SaveData;
import org.openmicroscopy.shoola.agents.events.iviewer.SaveRelatedData;
import org.openmicroscopy.shoola.agents.measurement.Analyser;
import org.openmicroscopy.shoola.agents.measurement.IconManager;
import org.openmicroscopy.shoola.agents.measurement.MeasurementAgent;
import org.openmicroscopy.shoola.agents.measurement.MeasurementViewerLoader;
import org.openmicroscopy.shoola.agents.measurement.ROIAnnotationLoader;
import org.openmicroscopy.shoola.agents.measurement.ROIAnnotationSaver;
import org.openmicroscopy.shoola.agents.measurement.ROILoader;
import org.openmicroscopy.shoola.agents.measurement.ROISaver;
import org.openmicroscopy.shoola.agents.measurement.ServerSideROILoader;
import org.openmicroscopy.shoola.agents.measurement.TagsLoader;
import org.openmicroscopy.shoola.agents.measurement.WorkflowLoader;
import org.openmicroscopy.shoola.agents.measurement.WorkflowSaver;
import org.openmicroscopy.shoola.agents.measurement.util.FileMap;

import org.openmicroscopy.shoola.agents.util.EditorUtil;
import org.openmicroscopy.shoola.agents.util.ViewerSorter;
import org.openmicroscopy.shoola.env.data.OmeroImageService;
import org.openmicroscopy.shoola.env.data.model.DeletableObject;
import org.openmicroscopy.shoola.env.data.model.DeleteActivityParam;

import omero.gateway.SecurityContext;
import omero.gateway.exception.DSAccessException;
import omero.gateway.exception.DSOutOfServiceException;
import omero.gateway.model.ROIResult;

import org.openmicroscopy.shoola.env.event.EventBus;

import omero.log.Logger;

import org.openmicroscopy.shoola.env.ui.UserNotifier;
import org.openmicroscopy.shoola.util.file.IOUtil;
import org.openmicroscopy.shoola.util.roi.model.annotation.AnnotationKeys;
import org.openmicroscopy.shoola.util.roi.model.annotation.MeasurementAttributes;
import org.openmicroscopy.shoola.util.roi.ROIComponent;
import org.openmicroscopy.shoola.util.roi.exception.NoSuchROIException;
import org.openmicroscopy.shoola.util.roi.exception.ParsingException;
import org.openmicroscopy.shoola.util.roi.exception.ROICreationException;
import org.openmicroscopy.shoola.util.roi.figures.ROIFigure;
import org.openmicroscopy.shoola.util.roi.model.ROI;
import org.openmicroscopy.shoola.util.roi.model.ROIShape;
import org.openmicroscopy.shoola.util.roi.model.ShapeList;
import org.openmicroscopy.shoola.util.roi.model.util.Coord3D;
import org.openmicroscopy.shoola.util.roi.model.util.MeasurementUnits;
import org.openmicroscopy.shoola.util.ui.drawingtools.DrawingComponent;
import org.openmicroscopy.shoola.util.ui.drawingtools.canvas.DrawingCanvasView;

import omero.gateway.model.AnnotationData;
import omero.gateway.model.ChannelData;
import omero.gateway.model.DataObject;
import omero.gateway.model.ExperimenterData;
import omero.gateway.model.FileAnnotationData;
import omero.gateway.model.GroupData;
import omero.gateway.model.ImageData;
import omero.gateway.model.PixelsData;
import omero.gateway.model.ROIData;
import ome.model.units.BigResult;
import omero.model.Length;
import omero.model.LengthI;
import omero.model.enums.UnitsLength;

/**
 * The Model component in the <code>MeasurementViewer</code> MVC triad.
 * This class tracks the <code>MeasurementViewer</code>'s state and knows how to
 * initiate data retrievals. It also knows how to store and manipulate
 * the results. This class provides a suitable data loader.
 * The {@link MeasurementViewerComponent} intercepts the results of data
 * loadings, feeds them back to this class and fires state transitions as
 * appropriate.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since OME3.0
 */
class MeasurementViewerModel
{

	/** The id of the image this {@link MeasurementViewer} is for. */
	private long					imageID;

	/** The name of the image this {@link MeasurementViewer} is for. */
	private String					name;

    /** The bounds of the component requesting the viewer. */
    private Rectangle				requesterBounds;

    /** Holds one of the state flags defined by {@link MeasurementViewer}. */
    private int 					state;

	/**
	 * The drawing component to create drawing, view and editor and link them.
	 */
	private DrawingComponent 		drawingComponent;

	/** The component managing the ROI. */
	private ROIComponent			roiComponent;

	/** The currently selected plane. */
	private Coord3D					currentPlane;

	/** The pixels set. */
	private PixelsData 				pixels;

    /** The image's magnification factor. */
    private double					magnification;

    /** Collection of pairs (channel's index, channel's color). */
    private Map						activeChannels;

    /** Collection of pairs (ROIShape, Map of ROIShapeStats). */
    private Map						analysisResults;

    /** Metadata for the pixels set. */
    private List<ChannelData>		metadata;

    /**
     * Will either be a data loader or
     * <code>null</code> depending on the current state.
     */
    private MeasurementViewerLoader	currentLoader;

    /**
     * The ROISaver.
     */
    private MeasurementViewerLoader	currentSaver;

    /** Reference to the component that embeds this model. */
    private MeasurementViewer		component;

    /**
     * Reference to the event posted to save the data when closing the
     * viewer.
     */
    private SaveRelatedData 		event;

    /** The rendered image either a buffered image or a texture data. */
    private Object 					rndImage;

    /** The roi file previously saved if any. */
    private String					fileSaved;

    /** The measurements associated to the image. */
    private List<FileAnnotationData> measurements;

    /** The collection of ROIs and tables related to the measurements. */
    private Collection 				 measurementResults;

    /** The current workflow namespace being used. */
	private String 					workflowNamespace;

	/** The map of workflow namespace, workflow. */
	private Map<String, WorkflowData> 	workflows;

	/** The keyword of the current workflow. */
	private List<String>			keyword;

	/** Flag indicating if the tool is for HCS data. */
	private boolean					HCSData;

	/** Collection of ROIs to delete. */
	private List<ROI>				roiToDelete;

	/** Flag indicating that the current user can deleted the ROI. */
	private boolean					dataToDelete;

	 /** Flag indicating if it is a big image or not.*/
    private boolean 				bigImage;

    /** The security context.*/
    private SecurityContext ctx;

    /** The sorter to order shapes.*/
    private ViewerSorter sorter;

    /** Collection of existing tags if any. */
    private Collection existingTags;

	/**
	 * Map figure attributes to ROI and ROIShape annotations where necessary.
	 * @param attribute see above.
	 * @param figure see above.
	 */
	private void mapFigureAttributeToROIAnnotation(AttributeKey attribute,
													ROIFigure figure)
	{

		if (MeasurementAttributes.TEXT.getKey().equals(attribute.getKey()))
		{
			ROIShape shape = figure.getROIShape();
			AnnotationKeys.TEXT.set(shape,
				MeasurementAttributes.TEXT.get(figure));
		}
	}

	/** Checks the user currently logged in has ROI to delete. */
	private void checkIfHasROIToDelete()
	{
		if (dataToDelete) return;
		Collection<ROI> rois = roiComponent.getROIMap().values();
		Iterator<ROI> i = rois.iterator();
		List<ROI> ownedRois = new ArrayList<ROI>();
		ROI roi;
		List<ROIFigure> figures = new ArrayList<ROIFigure>();
		while (i.hasNext()) {
			roi = i.next();
			//if (roi.getOwnerID() == ownerID || roi.getOwnerID() == -1) {
			if (roi.canDelete()) {
				figures.addAll(roi.getAllFigures());
				ownedRois.add(roi);
			}
		}
		dataToDelete = ownedRois.size() > 0;
	}

	/**
	 * Creates a new instance.
	 *
	 * @param ctx The security context.
	 * @param imageID The image's id.
	 * @param pixels The pixels set the measurement tool is for.
	 * @param name The image's name.
	 * @param bounds The bounds of the component requesting the model.
	 * @param channelsData The channel metadata.
	 */
	MeasurementViewerModel(SecurityContext ctx, long imageID, PixelsData pixels,
			String name, Rectangle bounds, List<ChannelData> channelsData)
	{
		metadata = channelsData;
		this.ctx = ctx;
		this.imageID = imageID;
		this.pixels = pixels;
		this.name = name;
		requesterBounds = bounds;
		state = MeasurementViewer.NEW;
		sorter = new ViewerSorter();
		drawingComponent = new DrawingComponent();
		roiComponent = new ROIComponent();
		fileSaved = null;
		roiComponent.setPixelSizes(getPixelSizeX(), getPixelSizeY(), getPixelSizeZ());
		workflows = new HashMap<String, WorkflowData>();
		this.workflowNamespace = WorkflowData.DEFAULTWORKFLOW;
		this.keyword = new ArrayList<String>();
		setPlane(0, 0);
	}

	/**
	 * Called by the <code>ROIViewer</code> after creation to allow this
	 * object to store a back reference to the embedding component.
	 *
	 * @param component The embedding component.
	 */
	void initialize(MeasurementViewer component)
	{
		this.component = component;
	}

	/**
	 * Get a link to the ROIComponent.
	 * @return see above.
	 */
	ROIComponent getROIComponent()
	{
		return roiComponent;
	}

	/**
	 * Returns all the figures hosted by the <code>ROIComponent</code>.
	 *
	 * @return See above.
	 */
	Collection<ROIFigure> getAllFigures()
	{
		TreeMap<Long, ROI> rois = roiComponent.getROIMap();
		List<ROIFigure> all = new ArrayList<ROIFigure>();
		if (rois == null) return all;
		Iterator i = rois.entrySet().iterator();
		Entry entry;
		ROI roi;
		List<ROIFigure> l;
		while (i.hasNext()) {
			entry = (Entry) i.next();
			roi = (ROI) entry.getValue();
			l = roi.getAllFigures();
			if (l != null && l.size() > 0)
				all.addAll(l);
		}
		return all;
	}

	/**
     * Returns the name used to log in.
     *
     * @return See above.
     */
    String getUserName()
    {
    	return MeasurementAgent.getRegistry().getAdminService().getLoggingName();
    }

	/**
     * Returns the user currently logged in.
     *
     * @return See above.
     */
	ExperimenterData getCurrentUser()
    {
    	return MeasurementAgent.getUserDetails();
    }


    /**
     * Returns the name of the server the user is connected to.
     *
     * @return See above.
     */
    String getServerName()
    {
    	return MeasurementAgent.getRegistry().getAdminService().getServerName();
    }

	/**
	 * Sets the selected z-section and timepoint.
	 *
	 * @param z	The selected z-section.
	 * @param t	The selected timepoint.
	 */
	void setPlane(int z, int t)
	{
	    if (z != -1 && t !=-1) {
	        currentPlane = new Coord3D(z, t);
	    } else if (z == -1 && t !=-1) {
            currentPlane = new Coord3D(currentPlane.getZSection(), t);
        } else if (z != -1 && t ==-1) {
            currentPlane = new Coord3D(z, currentPlane.getTimePoint());
        }
	}

	/**
     * Compares another model to this one to tell if they would result in
     * having the same display.
     *
     * @param other The other model to compare.
     * @return <code>true</code> if <code>other</code> would lead to a viewer
     *          with the same display as the one in which this model belongs;
     *          <code>false</code> otherwise.
     */
    boolean isSameDisplay(MeasurementViewerModel other)
    {
        if (other == null) return false;
        return ((other.pixels.getId() == getPixelsID())
        		&& (other.imageID == imageID));
    }

    /**
     * Returns the ID of the image.
     *
     * @return See above.
     */
    long getImageID() { return imageID; }

    /**
     * Returns the ID of the pixels set this model is for.
     *
     * @return See above.
     */
    long getPixelsID() { return pixels.getId(); }

	/**
	 * Returns the name of the image.
	 *
	 * @return See above.
	 */
	String getImageName() { return name; }

	/**
	 * Returns the name of image and id.
	 *
	 * @return See above.
	 */
	String getImageTitle()
	{
		return "[ID: "+getImageID()+"] "+
				EditorUtil.getPartialName(getImageName());
	}

	/**
     * Returns the bounds of the component invoking the
     * {@link MeasurementViewer} or <code>null</code> if not available.
     *
     * @return See above.
     */
    Rectangle getRequesterBounds() { return requesterBounds; }

	 /**
     * Returns the current state.
     *
     * @return 	One of the flags defined by the {@link MeasurementViewer}
     * 			interface.
     */
    int getState() { return state; }

    /**
     * Returns the drawing editor.
     *
     * @return See above.
     */
    DrawingEditor getDrawingEditor() { return drawingComponent.getEditor(); }

    /**
     * Returns the drawing.
     *
     * @return See above.
     */
    Drawing getDrawing() { return drawingComponent.getDrawing(); }

    /**
     * Sets the object in the {@link MeasurementViewer#DISCARDED} state.
     * Any ongoing data loading will be cancelled.
     */
    void discard()
    {
    	cancel();
    	state = MeasurementViewer.DISCARDED;
    }

    /**
     * Sets the object in the {@link MeasurementViewer#READY} state.
     * Any ongoing data loading will be cancelled.
     */
    void cancel()
    {
    	if (currentLoader != null) currentLoader.cancel();
    	state = MeasurementViewer.READY;
    }

	/**
	 * Returns the currently selected z-section.
	 *
	 * @return See above.
	 */
	int getDefaultZ() { return currentPlane.getZSection(); }

	/**
	 * Returns the currently selected timepoint.
	 *
	 * @return See above.
	 */
	int getDefaultT() { return currentPlane.getTimePoint(); }

	/**
     * Returns the image's magnification factor.
     *
     * @return See above.
     */
	double getMagnification() { return magnification; }

	/**
     * Returns the image's magnification factor.
     *
     * @param magnification The value to set.
     */
	void setMagnification(double magnification)
	{
		int sizeX = getSizeX();
		int sizeY = getSizeY();
		this.magnification = magnification;
		getDrawingView().setScaleFactor(magnification,
                new Dimension(sizeX, sizeY));
	}

	/**
	 * Sets the attribute of all the ROI in the current plane to the key with
	 * value.
	 *
	 * @param key see above.
	 * @param value see above.
	 */
	void setAttributes(AttributeKey key, Object value)
	{
		List<Figure> figures =  getDrawing().getFigures();
		for (Figure f : figures)
			f.setAttribute(key, value);
		getDrawingView().repaint();
	}

	/**
	 * Sets the state.
	 *
	 * @param state The value to set.
	 */
	void setState(int state)
	{
		this.state = state;
	}

	/**
	 * Sets the ROI for the pixels set. Returns <code>true</code>
	 * if the ROI are compatible with the image, <code>false</code> otherwise.
	 *
	 * @param input	The value to set.
	 * @return See above.
	 * @throws ROICreationException If the ROI cannot be created.
	 * @throws NoSuchROIException 	If the ROI does not exist.
	 * @throws ParsingException		Thrown when an error occurred
	 * 								while parsing the stream.
	 */
	boolean setROI(InputStream input)
		throws ROICreationException, NoSuchROIException, ParsingException
	{
		state = MeasurementViewer.READY;
		if (input == null) return false;
		List<ROI> roiList = roiComponent.loadROI(input);
		if (roiList == null) return false;
		Iterator<ROI> i = roiList.iterator();
		ROI roi;
		TreeMap<Coord3D, ROIShape> shapeList;
		Iterator<ROIShape> shapeIterator;
		ROIShape shape;
		Coord3D c;
		int sizeZ = pixels.getSizeZ();
		int sizeT = pixels.getSizeT();

		boolean b = true;
		while (i.hasNext()) {
			roi = i.next();
			shapeList = roi.getShapes();
			shapeIterator = shapeList.values().iterator();
			while (shapeIterator.hasNext()) {
				shape = shapeIterator.next();
				c = shape.getCoord3D();
				if (c.getTimePoint() > sizeT) {
					b = false;
					break;
				}
				if (c.getZSection() > sizeZ) {
					b = false;
					break;
				}
			}
		}
		if (!b) {
			i = roiList.iterator();
			while (i.hasNext()) {
				roi = i.next();
				roiComponent.deleteROI(roi.getID());
			}
			return false;
		}
		notifyDataChanged(true);
		return true;
	}

	/**
	 * Returns the file corresponding to the passed id.
	 *
	 * @param fileID The id of the file.
	 * @return See above.
	 */
	FileAnnotationData getMeasurement(long fileID)
	{
		if (measurements == null) return null;
		Iterator<FileAnnotationData> i = measurements.iterator();
		FileAnnotationData fa;
		while (i.hasNext()) {
			fa = i.next();
			if (fa.getId() == fileID) return fa;
		}
		return null;
	}

	/**
	 * Returns the measurements.
	 *
	 * @return See above.
	 */
	List<FileAnnotationData> getMeasurements() { return measurements; }

	/**
	 * Returns the collection of measurements results.
	 *
	 * @return See above.
	 */
	Collection getMeasurementResults() { return measurementResults; }

	/**
	 * Sets the server ROIS.
	 *
	 * @param rois The collection of Rois.
	 * @return See above.
	 * @throws ROICreationException
	 * @throws NoSuchROIException
	 */
	List<DataObject> setServerROI(Collection rois)
		throws ROICreationException, NoSuchROIException
	{
	    List<DataObject> nodes = new ArrayList<DataObject>();
		measurementResults = rois;
		state = MeasurementViewer.READY;
		List<ROI> roiList = new ArrayList<ROI>();
		Iterator r = rois.iterator();
		ROIResult result;
		long userID = MeasurementAgent.getUserDetails().getId();
		while (r.hasNext()) {
			result = (ROIResult) r.next();
			roiList.addAll(roiComponent.loadROI(result.getFileID(),
					result.getROIs(), userID));
		}
		if (roiList == null) return nodes;
		Iterator<ROI> i = roiList.iterator();
		ROI roi;
		TreeMap<Coord3D, ROIShape> shapeList;
		Iterator j;
		ROIShape shape;
		Coord3D coord;
		int sizeZ = pixels.getSizeZ();
		int sizeT = pixels.getSizeT();
		Entry entry;
		int c;
		ROIFigure f;
		while (i.hasNext()) {
			roi = i.next();
			shapeList = roi.getShapes();
			j = shapeList.entrySet().iterator();
			while (j.hasNext()) {
				entry = (Entry) j.next();
				shape = (ROIShape) entry.getValue();
				coord = shape.getCoord3D();
				if (coord.getTimePoint() < sizeT &&
				        coord.getZSection() < sizeZ) {
				    c = coord.getChannel();
	                f = shape.getFigure();
	                if (shape.getData() != null) {
	                    nodes.add(shape.getData());
	                }
	                if (c >= 0 && f.isVisible())
	                    f.setVisible(isChannelActive(c));
				}
			}
		}
		checkIfHasROIToDelete();
		return nodes;
	}

	/**
	 * Returns the ROI.
	 *
	 * @return See above.
	 */
	TreeMap getROI() { return roiComponent.getROIMap(); }

	/**
	 * Returns the currently selected plane.
	 *
	 * @return See above.
	 */
	Coord3D getCurrentView() { return currentPlane; }

	/**
	 * Returns <code>true</code> if the size in microns can be displayed, this
	 * only if a valid value is stored, <code>false</code> otherwise.
	 *
	 * @return
	 */
	boolean sizeInMicrons()
	{
		return !getPixelSizeX().getUnit().equals(UnitsLength.PIXEL);
	}

	/**
	 * Returns the size of a pixel along the X-axis.
	 *
	 * @return See above.
	 */
	Length getPixelSizeX() {
        Length l = null;
        try {
            l = pixels.getPixelSizeX(UnitsLength.MICROMETER);
        } catch (BigResult e) {
            MeasurementAgent.getRegistry().getLogger()
                    .warn(this, "Could not get pixel size X in micrometer");
        }
		return l != null ? l : new LengthI(1, UnitsLength.PIXEL);
	}

	/**
	 * Returns the size of a pixel along the Y-axis.
	 *
	 * @return See above.
	 */
	Length getPixelSizeY() {
	    Length l = null;
        try {
            l = pixels.getPixelSizeY(UnitsLength.MICROMETER);
        } catch (BigResult e) {
            MeasurementAgent.getRegistry().getLogger()
                    .warn(this, "Could not get pixel size Y in micrometer");
        }
		return l != null ? l : new LengthI(1, UnitsLength.PIXEL);
	}

	/**
	 * Returns the size of a pixel along the Z-axis.
	 *
	 * @return See above.
	 */
    Length getPixelSizeZ() {
        try {
            return pixels.getPixelSizeZ(UnitsLength.MICROMETER);
        } catch (BigResult e) {
            MeasurementAgent.getRegistry().getLogger()
                    .warn(this, "Could not get pixel size Z in micrometer");
        }
        return null;
    }
	
	/**
	 * Returns the number of z sections in an image.
	 *
	 * @return See above.
	 */
	int getNumZSections() { return pixels.getSizeZ(); }

	/**
	 * Returns the number of timepoints in an image.
	 *
	 * @return See above.
	 */
	int getNumTimePoints() { return pixels.getSizeT(); }

	/**
	 * Returns the number of pixels along the X-axis.
	 *
	 * @return See above.
	 */
	int getSizeX() { return pixels.getSizeX(); }

	/**
	 * Returns the number of pixels along the Y-axis.
	 *
	 * @return See above.
	 */
	int getSizeY() { return pixels.getSizeY(); }

	/**
	 * Returns the {@link DrawingCanvasView}.
	 *
	 * @return See above.
	 */
	DrawingCanvasView getDrawingView()
	{
		return drawingComponent.getDrawingView();
	}

	/**
	 * Returns the ROI of the currently selected figure in the drawing view.
	 *
	 * @return see above.
	 */
	Collection<ROI> getSelectedROI()
	{
		Collection<Figure> selectedFigs = getDrawingView().getSelectedFigures();
		List<ROI> roiList = new ArrayList<ROI>();
		Iterator<Figure> figIterator = selectedFigs.iterator();
		ROIFigure fig;
		while (figIterator.hasNext())
		{
			fig = (ROIFigure) figIterator.next();
			roiList.add(fig.getROI());
		}
		return roiList;
	}

	/**
	 * Returns a collection of the currently selected shapes in the drawing view.
	 *
	 * @return see above.
	 */
    Collection<ROIShape> getSelectedShapes()
    {
        Collection<Figure> selectedFigs = getDrawingView().getSelectedFigures();
        List<ROIShape> l = new ArrayList<ROIShape>();
        Iterator<Figure> figIterator = selectedFigs.iterator();
        ROIFigure fig;
        while (figIterator.hasNext())
        {
            fig = (ROIFigure) figIterator.next();
            l.add(fig.getROIShape());
        }
        return l;
    }

	/**
	 * Removes the <code>ROIShape</code> on the current View corresponding
	 * to the passed id.
	 *
	 * @param id The id of the <code>ROI</code>.
	 * @throws NoSuchROIException If the ROI does not exist.
	 */
	void removeROIShape(long id)
		throws NoSuchROIException
	{
		ROIShape shape = roiComponent.getShape(id, getCurrentView());
		if (shape != null)
		{
			if (drawingComponent.contains(shape.getFigure()))
				drawingComponent.removeFigure(shape.getFigure());
			else roiComponent.deleteShape(id, getCurrentView());
		}
	}

	/**
	 * Removes the <code>ROIShape</code> corresponding to the passed id on
	 * the plane coordinates.
	 *
	 * @param id The id of the <code>ROI</code>.
	 * @param coord the coordinates of the shape to delete.
	 * @throws NoSuchROIException If the ROI does not exist.
	 */
	void removeROIShape(long id, Coord3D coord)
		throws NoSuchROIException
	{
		ROIShape shape = roiComponent.getShape(id, coord);
		if (shape != null) {
			if (drawingComponent.contains(shape.getFigure()))
				drawingComponent.removeFigure(shape.getFigure());
			else
				roiComponent.deleteShape(id, coord);
		}
	}

	/**
	 * Removes the <code>ROI</code> corresponding to the passed id.
	 *
	 * @param id The id of the <code>ROI</code>.
	 * @throws NoSuchROIException If the ROI does not exist.
	 */
	void removeROI(long id)
		throws NoSuchROIException
	{
		ROIShape shape = roiComponent.getShape(id, getCurrentView());
		if (shape != null) {
			if (drawingComponent.contains(shape.getFigure()))
				drawingComponent.removeFigure(shape.getFigure());
		}
		if (roiComponent.containsROI(id))
			roiComponent.deleteROI(id);
	}

	/**
	 * Removes all the <code>ROI</code> in the system.
	 *
	 * @throws NoSuchROIException If the ROI does not exist.
	 */
	void removeAllROI()
		throws NoSuchROIException
	{
		state = MeasurementViewer.READY;
		drawingComponent.removeAllFigures();
		int size = roiComponent.getROIMap().values().size();
		ROI[] valueList = new ROI[size];
		roiComponent.getROIMap().values().toArray(valueList);
		if (valueList != null)
			for (ROI roi: valueList)
				roiComponent.deleteROI(roi.getID());
	}

	/**
	 * Removes all the <code>ROI</code> in the system.
	 * Returns the collection of figures.
	 *
	 * @return See above.
	 * @throws NoSuchROIException If the ROI does not exist.
	 */
	List<ROIFigure> removeAllROI(long ownerID, int level)
		throws NoSuchROIException
	{
		Collection<ROI> rois = roiComponent.getROIMap().values();
		Iterator<ROI> i = rois.iterator();
		List<ROI> ownedRois = new ArrayList<ROI>();
		ROI roi;
		List<ROIFigure> figures = new ArrayList<ROIFigure>();
		switch (level) {
			case MeasurementViewer.ALL:
				while (i.hasNext()) {
					roi = i.next();
					figures.addAll(roi.getAllFigures());
					ownedRois.add(roi);
				}
				break;
			case MeasurementViewer.ME:
				while (i.hasNext()) {
					roi = i.next();
					if (roi.getOwnerID() == ownerID || roi.getOwnerID() == -1) {
						figures.addAll(roi.getAllFigures());
						ownedRois.add(roi);
					}
				}
				break;
			case MeasurementViewer.OTHER:
				while (i.hasNext()) {
					roi = i.next();
					if (roi.getOwnerID() != ownerID) {
						figures.addAll(roi.getAllFigures());
						ownedRois.add(roi);
					}
				}
		}
		i = ownedRois.iterator();
		while (i.hasNext()) {
			roi = i.next();
			roiComponent.deleteROI(roi.getID());
		}
		Iterator<ROIFigure> j = figures.iterator();
		while (j.hasNext()) {
			drawingComponent.removeFigure(j.next());
		}
		event = null;
		notifyDataChanged(false);
		dataToDelete = false;
		return figures;
	}

	/**
	 * Returns the <code>ROI</code> corresponding to the passed id.
	 *
	 * @param id The id of the <code>ROI</code>.
	 * @return See above.
	 * @throws NoSuchROIException If the ROI does not exist.
	 */
	ROI getROI(long id)
		throws NoSuchROIException
	{
		return roiComponent.getROI(id);
	}

	/**
	 * Returns the ROIComponent to create a <code>ROI</code> from
	 * the passed figure.
	 *
	 * @param figure The figure to create the <code>ROI</code> from.
	 * @param addAttribs add attributes to figure
	 * @return Returns the created <code>ROI</code>.
	 * @throws ROICreationException If the ROI cannot be created.
	 * @throws NoSuchROIException If the ROI does not exist.
	 */
	ROI createROI(ROIFigure figure, boolean addAttribs)
		throws ROICreationException, NoSuchROIException
	{
		ROI roi = roiComponent.addROI(figure, getCurrentView(), addAttribs);
		roi.setAnnotation(AnnotationKeys.NAMESPACE, this.workflowNamespace);
		return roi;
	}

	/**
	 * Returns the {@link ShapeList} for the current plane.
	 *
	 * @return See above.
	 * @throws NoSuchROIException Thrown if the ROI doesn't exist.
	 */
	ShapeList getShapeList()
		throws NoSuchROIException
	{
		return roiComponent.getShapeList(currentPlane);
	}

	/**
	 * Figure attribute has changed, need to add any special processing to see
	 * if it should affect ROIShape, ROI or other object.
	 *
	 * @param attribute
	 * @param figure
	 */
	void figureAttributeChanged(AttributeKey attribute, ROIFigure figure)
	{
		mapFigureAttributeToROIAnnotation(attribute, figure);
	}

	/**
	 * Loads the ROI associated to the image.
	 *
	 * @param measurements The measurements if any.
	 */
	void fireLoadROIFromServer(List<FileAnnotationData> measurements)
	{
		this.measurements = measurements;
		List<Long> files = null;
		if (measurements != null) {
			files = new ArrayList<Long>();
			Iterator<FileAnnotationData> i = measurements.iterator();
			while (i.hasNext())
				files.add(i.next().getId());
		}

		state = MeasurementViewer.LOADING_ROI;
		ExperimenterData exp =
			(ExperimenterData) MeasurementAgent.getUserDetails();
		currentLoader = new ROILoader(component, getSecurityContext(),
				getImageID(), files, exp.getId());
		currentLoader.load();
	}

	/**
	 * Fires an asynchronous retrieval of the ROI related to the pixels set.
	 *
	 * @param dataChanged 	Pass <code>true</code> if the ROI has been changed.
	 * 						<code>false</code> otherwise.
	 */
	void fireLoadROIServerOrClient(boolean dataChanged)
	{
		state = MeasurementViewer.LOADING_ROI;
		ExperimenterData exp =
			(ExperimenterData) MeasurementAgent.getUserDetails();
		currentLoader = new ServerSideROILoader(component, getSecurityContext(),
				getImageID(),  exp.getId());
		currentLoader.load();
		notifyDataChanged(dataChanged);
	}

	/**
	 * Fires an asynchronous retrieval of the Workflow related user.
	 */
	void fireLoadWorkflow()
	{
		ExperimenterData exp =
			(ExperimenterData) MeasurementAgent.getUserDetails();
		currentLoader = new WorkflowLoader(component, getSecurityContext(),
				exp.getId());
		currentLoader.load();
	}

	/**
	 * Retrieves the workflows saved.
	 */
	void retrieveWorkflowsFromServer()
	{
		ExperimenterData exp =
			(ExperimenterData) MeasurementAgent.getUserDetails();
		OmeroImageService svc =
			MeasurementAgent.getRegistry().getImageService();
		try
		{
			List<WorkflowData> result = svc.retrieveWorkflows(
					getSecurityContext(), exp.getId());
			workflows.clear();
			component.setWorkflowList(result);
		} catch (DSAccessException e)
		{
			Logger log = MeasurementAgent.getRegistry().getLogger();
			log.error(this, "Cannot load workflows");
		} catch (DSOutOfServiceException e)
		{
			Logger log = MeasurementAgent.getRegistry().getLogger();
			log.error(this, "Cannot load workflows");
		}
	}

	/**
	 * Fires an asynchronous retrieval of the ROI related to the pixels set.
	 *
	 * @param fileName The name of the file to load. If <code>null</code>
	 * 					is selected.
	 */
	void fireROILoading(String fileName)
	{
		InputStream stream = null;
		state = MeasurementViewer.LOADING_ROI;
		try {
			if (fileName == null)
				fileName = FileMap.getSavedFile(getServerName(), getUserName(),
												getPixelsID());
			fileSaved = fileName;
			if (fileSaved != null)
				stream = IOUtil.readFileAsInputStream(fileName);
		} catch (Exception e) {
			Logger log = MeasurementAgent.getRegistry().getLogger();
			log.warn(this, "Cannot load the ROI "+e.getMessage());
		}
		component.setROI(stream);
		try {
			if (stream != null) stream.close();
		} catch (Exception e) {
			Logger log = MeasurementAgent.getRegistry().getLogger();
			log.warn(this, "Cannot close the stream "+e.getMessage());
		}
	}

	/**
	 * Loads the ROI associated to the image.
	 *
	 * @param measurements The measurements if any.
	 */
	/*
	void fireLoadROIFromServer(List<FileAnnotationData> measurements)
	{
		this.measurements = measurements;
		List<Long> files = null;
		if (measurements != null) {
			files = new ArrayList<Long>();
			Iterator<FileAnnotationData> i = measurements.iterator();
			while (i.hasNext())
				files.add(i.next().getId());
		}

		state = MeasurementViewer.LOADING_ROI;
		ExperimenterData exp =
			(ExperimenterData) MeasurementAgent.getUserDetails();
		currentLoader = new ROILoader(component, getImageID(), files,
				exp.getId());
		currentLoader.load();
	}
	*/

	/**
	 * Returns the path to the file where the ROIs have been saved
	 * or <code>null</code> if not previously saved.
	 *
	 * @return See above.
	 */
	String getFileSaved() { return fileSaved; }

	/**
	 * Saves the current ROISet in the ROI component to file.
	 *
	 * @param fileName 	name of the file to be saved.
	 * @param post		Pass <code>true</code> to post an event,
	 * 					<code>false</code> otherwise.
	 * @throws ParsingException
	 */
	void saveROI(String fileName, boolean post)
		throws ParsingException
	{
		OutputStream stream = null;
		try {
			stream = IOUtil.writeFile(fileName);
		} catch (Exception e) {
			Logger log = MeasurementAgent.getRegistry().getLogger();
			log.warn(this, "Cannot save the ROI "+e.getMessage());
		}
		roiComponent.saveROI(stream);
		try {
			if (stream != null) stream.close();
			FileMap.setSavedFile(getServerName(), getUserName(), getPixelsID(),
								fileName);
			if (!post) event = null;
			notifyDataChanged(false);
		} catch (Exception e) {
			Logger log = MeasurementAgent.getRegistry().getLogger();
			log.warn(this, "Cannot close the stream "+e.getMessage());
		}
	}

	/**
	 * Saves the current ROISet in the ROI component to server.
	 *
	 * @param async Pass <code>true</code> to save the ROI asynchronously,
	 * 				 <code>false</code> otherwise.
	 * @param close Indicates to close the component if <code>true</code>.
	 */
	void saveROIToServer(boolean async, boolean close)
	{
		try {
			List<ROIData> roiList = getROIData();
			ExperimenterData exp =
				(ExperimenterData) MeasurementAgent.getUserDetails();
			if (roiList.size() == 0) return;
			roiComponent.reset();
			if (async) {
				currentSaver = new ROISaver(component, getSecurityContext(),
						getImageID(), exp.getId(), roiList, close);
				currentSaver.load();
				state = MeasurementViewer.SAVING_ROI;
				notifyDataChanged(false);
			} else {
				OmeroImageService svc =
					MeasurementAgent.getRegistry().getImageService();
				svc.saveROI(getSecurityContext(), getImageID(), exp.getId(),
						roiList);
				state = MeasurementViewer.READY;
				event = null;
			}
			checkIfHasROIToDelete();
		} catch (Exception e) {
			Logger log = MeasurementAgent.getRegistry().getLogger();
			log.warn(this, "Cannot save to server "+e.getMessage());
			UserNotifier un = MeasurementAgent.getRegistry().getUserNotifier();
			un.notifyInfo("Save ROI", "Unable to save the ROIs");
		}
	}

	/**
	 * Returns the collection of ROI on the image owned by the user currently
	 * logged in
	 *
	 * @return See above.
	 */
	List<ROIData> getROIData()
	{
		try {
			long userID = getCurrentUser().getId();
			return roiComponent.saveROI(getImage(), ROIComponent.EDIT,
					userID);
		} catch (Exception e) {
			Logger log = MeasurementAgent.getRegistry().getLogger();
			log.warn(this, "Cannot transform the ROI: "+e.getMessage());
		}
		return new ArrayList<ROIData>();
	}

	/**
	 * Returns the collection of ROI on the image owned by the user currently
	 * logged in
	 *
	 * @param level One of the constants defined by the
	 * <code>MeasurementViewer</code>.
	 * @return See above.
	 */
	List<ROIData> getROIData(int level)
	{
		try {
			long userID = getCurrentUser().getId();
			switch (level) {
			case MeasurementViewer.ALL:
				return roiComponent.saveROI(getImage(), ROIComponent.DELETE,
						userID);
			case MeasurementViewer.ME:
				return roiComponent.saveROI(getImage(), ROIComponent.DELETE_MINE,
						userID);
			case MeasurementViewer.OTHER:
				return roiComponent.saveROI(getImage(),
						ROIComponent.DELETE_OTHERS, userID);
			}
		} catch (Exception e) {
			Logger log = MeasurementAgent.getRegistry().getLogger();
			log.warn(this, "Cannot transform the ROI: "+e.getMessage());
		}
		return new ArrayList<ROIData>();
	}

	/**
	 * Returns the image the pixels set is linked to.
	 *
	 * @return See above.
	 */
	ImageData getImage() { return pixels.getImage(); }

	/**
	 * Saves the current ROISet in the ROI component to server.
	 *
	 * @param async Pass <code>true</code> to save the ROI asynchronously,
	 * 				 <code>false</code> otherwise.
	 */
	void saveWorkflowToServer(boolean async)
	{
		List<WorkflowData> workflowList = new ArrayList<WorkflowData>();
		Iterator<WorkflowData> workflowIterator = workflows.values().iterator();
		while (workflowIterator.hasNext())
			workflowList.add(workflowIterator.next());
		try {
			ExperimenterData exp =
				(ExperimenterData) MeasurementAgent.getUserDetails();
			if (async) {
				currentSaver = new WorkflowSaver(component,
					getSecurityContext(), workflowList, exp.getId());
				currentSaver.load();
				notifyDataChanged(false);
			} else {
				OmeroImageService svc =
					MeasurementAgent.getRegistry().getImageService();
				svc.storeWorkflows(getSecurityContext(), workflowList,
						exp.getId());
				event = null;
			}
		} catch (Exception e) {
			Logger log = MeasurementAgent.getRegistry().getLogger();
			log.warn(this, "Cannot save workflows to server "+e.getMessage());
		}
	}

	/**
	 * Propagates the selected shape in the roi model.
	 *
	 * @param shape 	The ROIShape to propagate.
	 * @param timePoint The timepoint to propagate to.
	 * @param zSection 	The z-section to propagate to.
	 * @return A list of the newly added shapes.
	 * @throws NoSuchROIException	Thrown if ROI with id does not exist.
	 * @throws ROICreationException	Thrown if the ROI cannot be created.
	 */
	List<ROIShape> propagateShape(ROIShape shape, int timePoint, int zSection)
		throws ROICreationException, NoSuchROIException
	{
		notifyDataChanged(true);
		Coord3D coord = new Coord3D(zSection, timePoint);
		return roiComponent.propagateShape(shape.getID(), shape.getCoord3D(),
			shape.getCoord3D(),coord);
	}

	/**
	 * Deletes the selected shape from current coord to timepoint and z-section.
	 *
	 * @param shape 	The ROIShape to propagate.
	 * @param timePoint The timepoint to propagate to.
	 * @param zSection 	The z-section to propagate to.
	 * @throws NoSuchROIException Thrown if no such ROI exists.
	 */
	void deleteShape(ROIShape shape, int timePoint, int zSection)
		throws NoSuchROIException
	{
		if (drawingComponent.contains(shape.getFigure()))
			drawingComponent.getDrawing().remove(shape.getFigure());
		else
		{
			notifyDataChanged(true);
			roiComponent.deleteShape(
					shape.getID(), shape.getCoord3D(), new Coord3D(zSection,
							timePoint));
		}
	}

	/**
	 * Show the measurements in the ROIFigures in microns.
	 *
	 * @param inMicrons show the measurement in microns if true.
	 *
	 */
	void showMeasurementsInMicrons(boolean inMicrons)
	{
		if (inMicrons)
			roiComponent.setPixelSizes(getPixelSizeX(), getPixelSizeY(), getPixelSizeZ());
		else
			roiComponent.setPixelSizes(new LengthI(1, UnitsLength.PIXEL), new LengthI(1, UnitsLength.PIXEL), new LengthI(1, UnitsLength.PIXEL));
	}

	/**
	 * Returns the type of units.
	 *
	 * @return See above.
	 */
	MeasurementUnits getMeasurementUnits()
	{
		return roiComponent.getMeasurementUnits();
	}

	/**
	 * Sets the active channels.
	 *
	 * @param activeChannels The value to set.
	 */
	void setActiveChannels(Map activeChannels)
	{
		this.activeChannels = activeChannels;
	}

	/**
	 * Fires an asynchronous call to analyze the passed shapes.
	 *
	 * @param shapeList The shapelist to analyze. Mustn't be <code>null</code>.
	 */
	void fireAnalyzeShape(List<ROIShape> shapeList)
	{
	    if (CollectionUtils.isEmpty(shapeList)) return;
		state = MeasurementViewer.ANALYSE_SHAPE;
		if (currentLoader != null) currentLoader.cancel();
		List<ROIShape> l = new ArrayList<ROIShape>(shapeList.size());
		Iterator<ROIShape> i = shapeList.iterator();
		ROIShape shape;
		int z, t;
		while (i.hasNext()) {
            shape = i.next();
            z = shape.getZ();
            t = shape.getT();
            if (z >= 0 && t >= 0) {
                l.add(shape);
            } else if (z == -1 && t >= 0) {
                l.add(shape.copy(new Coord3D(currentPlane.getZSection(), t)));
            } else if (z >=0 && t == -1) {
                l.add(shape.copy(new Coord3D(z, currentPlane.getTimePoint())));
            } else if (z == -1 && t == -1) {
                l.add(shape.copy(new Coord3D(currentPlane.getZSection(),
                        currentPlane.getTimePoint())));
            }
        }
		currentLoader = new Analyser(component, getSecurityContext(), pixels,
				activeChannels.keySet(), l);
		currentLoader.load();
	}

	/**
	 * Returns the channels metadata.
	 *
	 * @return See above
	 */
	List<ChannelData> getMetadata() { return metadata; }

	/**
	 * Returns the metadata corresponding to the specified index or
	 * <code>null</code> if the index is not valid.
	 *
	 * @param index The channel index.
	 * @return See above.
	 */
	ChannelData getMetadata(int index)
	{
		if (index < 0 || index >= metadata.size()) return null;
		Iterator<ChannelData> i = metadata.iterator();
		ChannelData d;
		while (i.hasNext()) {
			d = i.next();
			if (d.getIndex() == index) return d;
		}
		return null;
	}

	/**
	 * Sets the results of an analysis.
	 *
	 * @param analysisResults The value to set.
	 */
	void setAnalysisResults(Map analysisResults)
	{
		if (this.analysisResults != null) {
			this.analysisResults.clear();
		} else {
			this.analysisResults = new LinkedHashMap();
		}
		//sort the map.
		if (analysisResults != null) {
			List newList = sorter.sort(analysisResults.keySet());
			Iterator i = newList.iterator();
			ROIShape shape;
			while (i.hasNext()) {
				shape = (ROIShape) i.next();
				this.analysisResults.put(shape, analysisResults.get(shape));
			}
			analysisResults.clear();
		}
		state = MeasurementViewer.READY;
	}

	/**
	 * Returns the collection of stats or <code>null</code>
	 * if no analysis run on the selected ROI shapes.
	 *
	 * @return See above.
	 */
	Map getAnalysisResults() { return analysisResults; }

	/**
	 * Returns the active channels for the data.
	 *
	 * @return See above.
	 */
	Map getActiveChannels() { return activeChannels; }

	/**
	 * Returns the color associated to the specified channel or
	 * <code>null</code> if the channel is not active.
	 *
	 * @param index The index of the channel.
	 * @return See above.
	 */
	Color getActiveChannelColor(int index)
	{
		return (Color) activeChannels.get(index);
	}

	/**
	 * Returns the figures selected in the current view.
	 *
	 * @return See above.
	 */
	Collection<Figure> getSelectedFigures()
	{
		return getDrawingView().getSelectedFigures();
	}

	/**
	 * Returns <code>true</code> if the channel is active,
	 * <code>false</code> otherwise.
	 *
	 * @param index The index of the channel.
	 * @return See above.
	 */
	boolean isChannelActive(int index)
	{
		return (activeChannels.get(index) != null);
	}

	/**
	 * Notifies listeners that the measurement tool does not have data to save
	 * if <code>false</code>.
	 *
	 * @param toSave Pass <code>true</code> to save the data, <code>false</code>
	 * 				 otherwise.
	 */
	void notifyDataChanged(boolean toSave)
	{
		if (isHCSData()) return;
		if (event != null && toSave) return;
		EventBus bus = MeasurementAgent.getRegistry().getEventBus();
		event = new SaveRelatedData(getPixelsID(),
					new SaveData(getPixelsID(), SaveData.MEASUREMENT_TYPE),
									"The ROI", toSave);
		checkIfHasROIToDelete();
		bus.post(event);
		if (!toSave) event = null;
	}

	/**
	 * Calculate the stats for the roi in the shapelist.
	 *
	 * @param shapeList see above.
	 */
	void calculateStats(List<ROIShape> shapeList)
	{
		component.analyseShapeList(shapeList);
	}

	/**
	 * Returns the list of ROIs associated to that file.
	 *
	 * @param fileID The id of the file.
	 * @return See above.
	 */
	List<ROI> getROIList(long fileID)
	{
		return roiComponent.getROIList(fileID);
	}

	/**
	 * Clones the specified ROI.
	 *
	 * @param id The id of the ROI to clone.
	 * @return See above.
	 * @throws ROICreationException
	 * @throws NoSuchROIException
	 */
	ROI cloneROI(long id)
		throws ROICreationException, NoSuchROIException
	{
		return roiComponent.cloneROI(id);
	}

	/**
	 * Deletes the shapes.
	 *
	 * @param id
	 * @param coord
	 * @throws NoSuchROIException
	 */
	void deleteShape(long id, Coord3D coord)
		throws NoSuchROIException
	{
		roiComponent.deleteShape(id, coord);
	}

	/**
	 * Adds a new shape to the ROI component.
	 *
	 * @param id
	 * @param coord
	 * @param shape
	 * @throws ROICreationException
	 * @throws NoSuchROIException
	 */
	void addShape(long id, Coord3D coord, ROIShape shape)
		throws ROICreationException, NoSuchROIException
	{
		roiComponent.addShape(id, coord, shape);
	}

	/**
	 * Sets the rendered image.
	 *
	 * @param rndImage The value to set.
	 */
	void setRenderedImage(Object rndImage)
	{
		this.rndImage = rndImage;
	}

	/**
	 * Returns the rendered image.
	 *
	 * @return See above.
	 */
	BufferedImage getRenderedImage()
	{
		if (rndImage instanceof BufferedImage)
			return (BufferedImage) rndImage;
		return null;
	}

	/**
	 * Returns the object of reference.
	 *
	 * @return See above.
	 */
	Object getRefObject() { return pixels; }

	/**
	 * Returns <code>true</code> if data to save, <code>false</code>
	 * otherwise.
	 *
	 * @return See above.
	 */
	boolean hasROIToSave()
	{
		if (isHCSData()) return false;
		return event != null;
	}

	/**
	 * Returns <code>true</code> if data to delete, <code>false</code>
	 * otherwise.
	 *
	 * @return See above.
	 */
	boolean hasROIToDelete()
	{
		if (hasROIToSave()) return true;
		return dataToDelete;
	}

	/**
	 * Sets the workflow for the next ROI.
	 *
	 * @param workflowNamespace  See above.
	 */
	void setWorkflow(String workflowNamespace)
	{
		if (workflowNamespace == null) return;
		if (WorkflowData.DEFAULTWORKFLOW.equals(workflowNamespace))
		{
			this.workflowNamespace = workflowNamespace;
			keyword = new ArrayList<String>();
		} else {
			if (!workflows.containsKey(workflowNamespace))
				throw new IllegalArgumentException("Workflow " +
						workflowNamespace + " does not exist");
			this.workflowNamespace = workflowNamespace;
			keyword = getWorkflow().getKeywordsAsList();
		}
	}

	/**
	 * Returns the current workflow; or null if default workflow selected.
	 *
	 * @return See above.
	 */
	WorkflowData getWorkflow()
	{
		if (!WorkflowData.DEFAULTWORKFLOW.equals(workflowNamespace))
			return workflows.get(workflowNamespace);
		return null;
	}

	/**
	 * Adds a new workflow to the workflow list;
	 * @param workflow See above.
	 */
	void addWorkflow(WorkflowData workflow)
	{
		if (workflow != null)
			workflows.put(workflow.getNameSpace(), workflow);
	}

	/**
	 * Set the Workflows of the system.
	 * @param workflowList See above.
	 */
	void resetWorkflows(List<WorkflowData> workflowList)
	{
		workflows.clear();
		for(WorkflowData workflow : workflowList)
			workflows.put(workflow.getNameSpace(), workflow);
	}

	/**
	 * Returns all the workflow namespaces in the model, as an array list
	 * @return See above.
	 */
	List<String> getWorkflows()
	{
		List<String> workflowList = new ArrayList<String>();
		Iterator<String> i = workflows.keySet().iterator();
		workflowList.add(WorkflowData.DEFAULTWORKFLOW);
		while (i.hasNext())
			workflowList.add(i.next());
		return workflowList;
	}

	/**
	 * Returns all the workflow namespaces in the model, as an array list.
	 *
	 * @return See above.
	 */
	List<WorkflowData> getWorkflowDataList()
	{
		List<WorkflowData> workflowList = new ArrayList<WorkflowData>();
		Iterator<WorkflowData> i = workflows.values().iterator();
		while (i.hasNext())
			workflowList.add(i.next());
		return workflowList;
	}

	/**
	 * Sets the keyword of the workflow to keyword, the keyword must exist in
	 * the workflow to be set.
	 *
	 * @param keyword See above.
	 */
	void setKeyword(List<String> keywords)
	{
		if (keywords == null) return;
		if (keywords.size() == 0)
			this.keyword = keywords;
		else {
			WorkflowData workflow = getWorkflow();
			if (workflow == null) return;
			boolean b = true;
			for (String word : keywords) {
				if (!workflow.contains(word) && word.trim().length() != 0)
					b = false;
			}
			/*
			for (String word : keywords)
				if (!workflow.contains(word) && word.trim().length() != 0)
					throw new IllegalArgumentException(
							"Workflow does not contain keyword '" +
							keyword +"'");
							*/
			if (b)
				this.keyword = keywords;
			else keyword = new ArrayList<String>();
		}
	}

	/**
	 * Returns the keywords associated with the namespace that have been
	 * selected.
	 * @return See above.
	 */
	List<String> getKeywords() { return keyword; }

	/**
	 * Returns <code>true</code> if the tool is for HCS data, <code>false</code>
	 * otherwise.
	 *
	 * @return See above.
	 */
	boolean isHCSData() { return HCSData; }

	/**
     * Sets the flag indicating if the tool is for HCS data.
     *
     * @param value The value to set.
     */
    void setHCSData(boolean value) { HCSData = value; }

    /**
     * Adds the passed ROI to the collection of ROIs to delete.
     *
     * @param id The id of the shape.
     * @param roi The ROI to add.
     * @param force Pass <code>true</code> when the roi is removed via key.
     */
    void markROIForDelete(long id, ROI roi, boolean force)
    {
    	if (roi == null) return;
    	if (roiToDelete == null) roiToDelete = new ArrayList<ROI>();
    	if (id < 0) return;
    	if (force) {
    		if (!roi.isClientSide())
        		roiToDelete.add(roi);
    	} else {
    		if (!roiComponent.containsROI(id) && !roi.isClientSide())
        		roiToDelete.add(roi);
    	}
    }

    /**
     * Returns the collection to ROI to delete.
     *
     * @return See above.
     */
    List<ROI> getROIToDelete() { return roiToDelete; }

    /**
     * Invokes when the ROI has been deleted.
     *
     * @param imageID The image's identifier.
     */
    void onROIDeleted(long imageID)
    {
    	if (this.imageID != imageID) return;
    	state = MeasurementViewer.READY;
    	if (roiToDelete != null) roiToDelete.clear();
    	if (getROIData().size() == 0)
    		notifyDataChanged(false);
    }


	/**
	 * Post an event indicating to delete all the rois.
	 *
	 * @param list The list of objects to delete.
	 */
	void deleteAllROIs(List<DeletableObject> list)
	{
		if (list.size() == 0) return;
		IconManager icons = IconManager.getInstance();
		DeleteActivityParam p = new DeleteActivityParam(
				icons.getIcon(IconManager.APPLY_22), list);
		p.setImageID(imageID);
		p.setFailureIcon(icons.getIcon(IconManager.DELETE_22));
		UserNotifier un = MeasurementAgent.getRegistry().getUserNotifier();
		un.notifyActivity(getSecurityContext(), p);
	}

    /**
     * Sets the flag indicating if the tool is for big image data.
     *
     * @param value The value to set.
     */
    void setBigImage(boolean value) { bigImage = value; }

    /**
     * Returns <code>true</code> if big image data, <code>false</code>
     * otherwise.
     *
     * @return See above.
     */
   boolean isBigImage() { return bigImage; }

   /**
    * Returns the security context.
    *
    * @return See above.
    */
   SecurityContext getSecurityContext() { return ctx; }

   /**
    * Returns <code>true</code> if the user is not an owner nor an admin,
    * <code>false</code> otherwise.
    *
    * @return See above.
    */
	boolean isMember()
	{
		if (MeasurementAgent.isAdministrator()) return false;
		Collection groups = MeasurementAgent.getAvailableUserGroups();
		Iterator i = groups.iterator();
		GroupData g, gRef = null;
		long gId = getImage().getGroupId();
		while (i.hasNext()) {
			g = (GroupData) i.next();
			if (g.getId() == gId) {
				gRef = g;
				break;
			}
		}
		if (gRef != null)
			return EditorUtil.isUserGroupOwner(gRef,
				MeasurementAgent.getUserDetails().getId());
		return false;
	}

	/**
	 * Sets the channels that have been modified.
	 *
	 * @param channels The value to set.
	 */
	void setChannelData(List<ChannelData> channels) { metadata = channels; }

	/**
	 * Loads the annotations linked to rois.
	 *
	 * @param shapes The shapes
	 */
	void fireLoadROIAnnotations(List<DataObject> shapes)
	{
	    ROIAnnotationLoader loader = new ROIAnnotationLoader(component,
	            getSecurityContext(), shapes);
	    loader.load();
	}

	/**
	 * Saves the annotations.
	 *
	 * @param toAnnotate The objects to annotate.
	 * @param toAdd The annotation to add.
	 * @param toRemove The annotation to remove.
	 */
	void fireAnnotationSaving(Collection<DataObject> toAnnotate,
	        List<AnnotationData> toAdd, List<Object> toRemove)
	{
	    ROIAnnotationSaver saver = new ROIAnnotationSaver(component,
	            getSecurityContext(), toAnnotate, toAdd, toRemove);
	    saver.load();
	}

	/**
     * Sets the collection of existing tags.
     * 
     * @param tags The value to set.
     */
    void setExistingTags(Collection tags)
    {
        if (tags != null) existingTags = sorter.sort(tags);
    }

    /**
     * Returns the collection of existing tags.
     * 
     * @return See above.
     */
    Collection getExistingTags() { return existingTags; }

    /** Fires an asynchronous retrieval of existing tags. */
    void fireExistingTagsLoading()
    {
        TagsLoader loader = new TagsLoader(component,
                getSecurityContext(), canRetrieveAll());
        loader.load();
    }

    /**
     * Indicates to load all annotations available if the user can annotate
     * and is an administrator/group owner or to only load the user's
     * annotation.
     * 
     * @return See above
     */
    private boolean canRetrieveAll()
    {
        if (!pixels.canAnnotate()) return false;
        //check the group level
        GroupData group = getGroup(pixels.getGroupId());
        if (group == null) return false;
        switch (group.getPermissions().getPermissionsLevel()) {
            case GroupData.PERMISSIONS_GROUP_READ:
                if (MeasurementAgent.isAdministrator()) return true;
                Set leaders = group.getLeaders();
                Iterator i = leaders.iterator();
                long userID = getCurrentUser().getId();
                ExperimenterData exp;
                while (i.hasNext()) {
                    exp = (ExperimenterData) i.next();
                    if (exp.getId() == userID)
                        return true;
                }
                return false;
            case GroupData.PERMISSIONS_PRIVATE:
                return false;
        }
        return true;
    }

    /**
     * Returns the group corresponding to the specified id or <code>null</code>.
     * 
     * @param groupId The identifier of the group.
     * @return See above.
     */
    private GroupData getGroup(long groupId)
    {
        Collection groups = MeasurementAgent.getAvailableUserGroups();
        if (groups == null) return null;
        Iterator i = groups.iterator();
        GroupData group;
        while (i.hasNext()) {
            group = (GroupData) i.next();
            if (group.getId() == groupId) return group;
        }
        return null;
    }
}
