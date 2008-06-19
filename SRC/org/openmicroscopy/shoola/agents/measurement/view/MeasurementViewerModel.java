/*
 * org.openmicroscopy.shoola.agents.measurement.view.MeasurementViewerModel 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
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


//Java imports
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


//Third-party libraries
import org.jhotdraw.draw.AttributeKey;
import org.jhotdraw.draw.Drawing;
import org.jhotdraw.draw.DrawingEditor;
import org.jhotdraw.draw.Figure;

//Application-internal dependencies
import ome.model.core.Pixels;
import ome.model.core.PixelsDimensions;

import org.openmicroscopy.shoola.agents.events.SaveData;
import org.openmicroscopy.shoola.agents.events.iviewer.SaveRelatedData;
import org.openmicroscopy.shoola.agents.measurement.Analyser;
import org.openmicroscopy.shoola.agents.measurement.ChannelMetadataLoader;
import org.openmicroscopy.shoola.agents.measurement.MeasurementAgent;
import org.openmicroscopy.shoola.agents.measurement.MeasurementViewerLoader;
import org.openmicroscopy.shoola.agents.measurement.PixelsLoader;
import org.openmicroscopy.shoola.agents.measurement.util.FileMap;
import org.openmicroscopy.shoola.env.data.model.ChannelMetadata;
import org.openmicroscopy.shoola.env.event.EventBus;
import org.openmicroscopy.shoola.env.log.Logger;
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
import org.openmicroscopy.shoola.util.ui.drawingtools.DrawingComponent;
import org.openmicroscopy.shoola.util.ui.drawingtools.canvas.DrawingCanvasView;

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
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
class MeasurementViewerModel 
{
	
	/** The id of the image this {@link MeasurementViewer} is for. */
	private long					imageID;
	
	/** The id of the pixels set this {@link MeasurementViewer} is for. */
	private long					pixelsID;
	
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
	
	/** The dimensions of the pixels set. */
	private PixelsDimensions 		pixelsDims;
	
	/** The pixels set. */
	private Pixels 					pixels;
	
    /** The image's magnification factor. */
    private double					magnification;
    
    /** The name of the file name where the ROIs are saved. */
    private String					roiFileName;
    
    /** Collection of pairs (channel's index, channel's color). */
    private Map						activeChannels;
    
    /** Collection of pairs (ROIShape, Map of ROIShapeStats). */
    private Map						analysisResults;
    
    /** Metadata for the pixels set. */
    private ChannelMetadata[]		metadata;
    
    /** 
     * Will either be a data loader or
     * <code>null</code> depending on the current state. 
     */
    private MeasurementViewerLoader	currentLoader;
    
    /** Reference to the component that embeds this model. */
    private MeasurementViewer		component;
    
    /** Has the data been saved since last update. 			*/
    private Boolean					hasBeenSaved;
    
    private Boolean 				pixelDataAvailable;
    
    /** 
     * Reference to the event posted to save the data when closing the
     * viewer.
     */
    private SaveRelatedData 		event;
    
    /**
     * Returns the name used to log in.
     * 
     * @return See above.
     */
    String getUserName()
    {
    	return MeasurementAgent.getRegistry().getDataService().getLoggingName();
    }
    
    /**
     * Returns the name of the server the user is connected to.
     * 
     * @return See above.
     */
    String getServerName()
    {
    	return MeasurementAgent.getRegistry().getDataService().getServerName();
    }

	/**
	 * Creates a new instance.
	 * 
	 * @param imageID	The image's id.
	 * @param pixelsID	The id of the pixels set.
	 * @param name		The image's name.
	 * @param bounds	The bounds of the component requesting the component.
	 */
	MeasurementViewerModel(long imageID, long pixelsID, String name, 
						Rectangle bounds)
	{
		this.imageID = imageID;
		this.pixelsID = pixelsID;
		this.name = name;
		requesterBounds = bounds;
		state = MeasurementViewer.NEW;
		drawingComponent = new DrawingComponent();
		roiComponent = new ROIComponent();
		roiFileName = imageID+".xml";
		hasBeenSaved = true;
		pixelDataAvailable = false;
	}
	
	/**
	 * Get a link to the ROIComponent. 
	 * @return see above.
	 */
	public ROIComponent getROIComponent()
	{
		return roiComponent;
	}
	
	public Boolean isPixelDataAvailable()
	{
		return pixelDataAvailable;
	}
	
	private void setPixelDataAvailable(Boolean value)
	{
		pixelDataAvailable = value;
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
	 * Sets the selected z-section and timepoint.
	 * 
	 * @param z	The selected z-section.
	 * @param t	The selected timepoint.
	 */
	void setPlane(int z, int t) 
	{ 
		currentPlane = new Coord3D(z, t);
		setPixelDataAvailable(false);
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
        return ((other.pixelsID == pixelsID) && (other.imageID == imageID));
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
    long getPixelsID() { return pixelsID; }
    
	/**
	 * Returns the name of the image.
	 * 
	 * @return See above.
	 */
	String getImageName() { return name; }
	
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

	/** Fires an asynchronous retrieval of the pixels set. */
	void firePixelsLoading()
	{
		state = MeasurementViewer.LOADING_DATA;
		currentLoader = new PixelsLoader(component, pixelsID);
		currentLoader.load();
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
		this.magnification = magnification;
		if (state != MeasurementViewer.NEW)
			getDrawingView().setScaleFactor(magnification, new 
						Dimension(getSizeX(), getSizeY()));
		else 
			getDrawingView().setScaleFactor(magnification);
	}

	/** 
	 * Sets the ROI for the pixels set.
	 *  
	 * @param input 		The value to set.
	 * @throws ROICreationException If the ROI cannot be created.
	 * @throws NoSuchROIException 	If the ROI does not exist.
	 * @throws ParsingException		Thrown when an error occured
	 * 								while parsing the stream.
	 */
	void setROI(InputStream input)
		throws ROICreationException, NoSuchROIException, ParsingException
	{
		if (input != null) {
			List<ROI> roiList = roiComponent.loadROI(input);
			component.attachListeners(roiList);
		}
		state = MeasurementViewer.READY;
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
	 * Returns the size in microns of a pixel along the X-axis.
	 * 
	 * @return See above.
	 */
	float getPixelSizeX() { return pixelsDims.getSizeX().floatValue(); }
	
	/**
	 * Returns the size in microns of a pixel along the Y-axis.
	 * 
	 * @return See above.
	 */
	float getPixelSizeY() { return pixelsDims.getSizeY().floatValue(); }
	
	/**
	 * Returns the size in microns of a pixel along the Z-axis.
	 * 
	 * @return See above.
	 */
	float getPixelSizeZ() { return pixelsDims.getSizeZ().floatValue(); }
	
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
	int getSizeX() { return pixels.getSizeX().intValue(); }
	
	/**
	 * Returns the number of pixels along the Y-axis.
	 * 
	 * @return See above.
	 */
	int getSizeY() { return pixels.getSizeY().intValue(); }
	
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
	 * Get the ROI of the currently selected figure in the drawingview. 
	 * 
	 * @return see above.
	 */
	Collection<ROI> getSelectedROI()
	{
		Collection<Figure> selectedFigs = getDrawingView().getSelectedFigures();
		ArrayList<ROI> roiList = new ArrayList<ROI>();
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
		if(shape!=null)
		{
			if(drawingComponent.contains(shape.getFigure()))
				drawingComponent.removeFigure(shape.getFigure());
			else
			roiComponent.deleteShape(id, getCurrentView());
		}
	}
		
	/**
	 * Removes the <code>ROIShape</code> corresponding to the passed id on the plane
	 * coord.
	 * 
	 * @param id The id of the <code>ROI</code>.
	 * @param coord the coord of the shape to delete.
	 * @throws NoSuchROIException If the ROI does not exist.
	 */
	void removeROIShape(long id, Coord3D coord)
		throws NoSuchROIException
	{
		ROIShape shape = roiComponent.getShape(id, coord);
		if(shape!=null)
		{
			if(drawingComponent.contains(shape.getFigure()))
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
		if(shape!=null)
		{
			if(drawingComponent.contains(shape.getFigure()))
				drawingComponent.removeFigure(shape.getFigure());
		}
		if(roiComponent.containsROI(id))
			roiComponent.deleteROI(id);
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
	 * @return Returns the created <code>ROI</code>.
	 * @throws ROICreationException If the ROI cannot be created.
	 * @throws NoSuchROIException If the ROI does not exist.
	 */
	ROI createROI(ROIFigure figure)
		throws ROICreationException, NoSuchROIException
	{
		return roiComponent.addROI(figure, getCurrentView());
	}
	

	/**
	 * Returns the ROIComponent to create a <code>ROI</code> from 
	 * the passed figure.
	 * 
	 * @param figure The figure to create the <code>ROI</code> from.
	 * @param addAttribs add atrributs to figure
	 * @return Returns the created <code>ROI</code>.
	 * @throws ROICreationException If the ROI cannot be created.
	 * @throws NoSuchROIException If the ROI does not exist.
	 */
	ROI createROI(ROIFigure figure, boolean addAttribs)
		throws ROICreationException, NoSuchROIException
	{
		return roiComponent.addROI(figure, getCurrentView(),addAttribs);
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
	 * Map figure attributes to ROIa nd ROIShape annotations where necessary. 
	 * @param attribute see above.
	 * @param figure see above.
	 */
	private void mapFigureAttributeToROIAnnotation(AttributeKey attribute, 
													ROIFigure figure)
	{

		if (attribute.getKey().equals(MeasurementAttributes.TEXT.getKey())) 
		{
			ROIShape shape = figure.getROIShape();
			AnnotationKeys.TEXT.set(shape, 
				MeasurementAttributes.TEXT.get(figure));
		}
	}
	
	/**
	 * Sets the pixels set this model is for.
	 * 
	 * @param pixels The value to set.
	 */
	void setPixels(Pixels pixels)
	{ 
		this.pixels = pixels;
		pixelsDims = pixels.getPixelsDimensions(); 
		roiComponent.setMicronsPixelX(getPixelSizeX());
		roiComponent.setMicronsPixelY(getPixelSizeY());
		roiComponent.setMicronsPixelZ(getPixelSizeZ());
		setPixelDataAvailable(false);
	}
	
	
	/** 
	 * Fires an asynchronous retrieval of the ROI related to the pixels set. 
	 * 
	 * @param fileName The name of the file to load. If <code>null</code>
	 * 					the {@link #roiFileName} is selected.
	 */
	void fireROILoading(String fileName)
	{
		state = MeasurementViewer.LOADING_ROI;
		InputStream stream = null;
		try {
			if (fileName == null)
			{
				fileName = FileMap.getSavedFile(getServerName(), getUserName(), 
												getPixelsID());
			}
			stream = IOUtil.readFile(fileName);
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
	 * Saves the current ROISet in the roi component to file.
	 * @throws ParsingException
	 */
	void saveROI()
		throws ParsingException
	{
		saveROI(roiFileName, true);
	}
	
	/**
	 * Saves the current ROISet in the roi component to file.
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
			setDataDiscarded();
		} catch (Exception e) {
			Logger log = MeasurementAgent.getRegistry().getLogger();
			log.warn(this, "Cannot close the stream "+e.getMessage());
		}
	}
	
	/**
	 * Propagates the selected shape in the roi model. 
	 * 
	 * @param shape 	The ROIShape to propagate.
	 * @param timePoint The timepoint to propagate to.
	 * @param zSection 	The z-section to propagate to.
	 * @return arraylist A list of the newly added shapes.
	 * @throws NoSuchROIException	Thrown if ROI with id does not exist.
	 * @throws ROICreationException	Thrown if the ROI cannot be created.
	 */
	ArrayList<ROIShape> propagateShape(ROIShape shape, int timePoint, int zSection) 
		throws ROICreationException, NoSuchROIException
	{
		setDataChanged();
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
		if(drawingComponent.contains(shape.getFigure()))
			drawingComponent.getDrawing().remove(shape.getFigure());
		else
		{
			setDataChanged();
			roiComponent.deleteShape(shape.getID(), shape.getCoord3D(), 
			new Coord3D(zSection, timePoint));
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
		roiComponent.showMeasurementsInMicrons(inMicrons);
	}
	
	/**
	 * Sets the active channels.
	 * 
	 * @param activeChannels The value to set.
	 */
	void setActiveChannels(Map activeChannels)
	{
		this.activeChannels = activeChannels;
		setPixelDataAvailable(false);
	}
	
	/**
	 * Fires an asynchronous call to analyse the passed shape.
	 *  
	 * @param shape The shape to analyse. Mustn't be <code>null</code>.
	 */
	void fireAnalyzeShape(ROIShape shape)
	{
		if(getState() == MeasurementViewer.ANALYSE_SHAPE)
			return;
		List l = new ArrayList(1);
		l.add(shape);
		state = MeasurementViewer.ANALYSE_SHAPE;
		List channels = new ArrayList(activeChannels.size());
		channels.addAll(activeChannels.keySet());
		
		currentLoader = new Analyser(component, pixels, channels, l);
		currentLoader.load();
	}
		
	/**
	 * Fires an asynchronous call to analyse the passed shapes.
	 *  
	 * @param shapeList The shapelist to analyse. Mustn't be <code>null</code>.
	 */
	void fireAnalyzeShape(ArrayList<ROIShape> shapeList)
	{
		if(getState() == MeasurementViewer.ANALYSE_SHAPE)
			return;
		state = MeasurementViewer.ANALYSE_SHAPE;
		List channels = new ArrayList(activeChannels.size());
		channels.addAll(activeChannels.keySet());
		
		currentLoader = new Analyser(component, pixels, channels, shapeList);
		currentLoader.load();
	}
	
	
	/**
	 * Fires an asynchronous call to analyse the passed roi.
	 *  
	 * @param roi The roi to analyse. Mustn't be <code>null</code>.
	 */
	void fireAnalyzeROI(ROI roi)
	{
		if(getState() == MeasurementViewer.ANALYSE_SHAPE)
			return;
		List<ROIShape> l = new ArrayList<ROIShape>(roi.getShapes().size());
		Iterator<ROIShape> shapeIterator = roi.getShapes().values().iterator();
		while(shapeIterator.hasNext())
			l.add(shapeIterator.next());
		
		state = MeasurementViewer.ANALYSE_SHAPE;
		List channels = new ArrayList(activeChannels.size());
		channels.addAll(activeChannels.keySet());
		
		currentLoader = new Analyser(component, pixels, channels, l);
		currentLoader.load();
	}
	
	/** Fires an asynchronous call to retrieve the channel metadata. */
	void fireChannelMetadataLoading()
	{
		state = MeasurementViewer.LOADING_DATA;
		currentLoader = new ChannelMetadataLoader(component, pixelsID);
		currentLoader.load();
	}
	
	/**
	 * Sets the channel metadata.
	 * 
	 * @param m The value to set.
	 */
	void setChannelMetadata(List m)
	{
		metadata = new ChannelMetadata[m.size()];
        Iterator i = m.iterator();
        ChannelMetadata cm;
        while (i.hasNext()) {
        	cm = (ChannelMetadata) i.next();
            metadata[cm.getIndex()] = cm;
        }
	}
	
	/**
	 * Returns the channel metadata.
	 * 
	 * @return See above.
	 */
	ChannelMetadata[] getMetadata() { return metadata; }
	
	/**
	 * Returns the metadata corresponding to the specified index or 
	 * <code>null</code> if the index is not valid.
	 * 
	 * @param index The channel index.
	 * @return See above.
	 */
	ChannelMetadata getMetadata(int index) 
	{
		if (index < 0 || index >= metadata.length) return null;
		return metadata[index];
	}
	
	/**
	 * Sets the results of an analysis.
	 * 
	 * @param analysisResults The value to set.
	 */
	void setAnalysisResults(Map analysisResults)
	{
		this.analysisResults = analysisResults;
		this.setPixelDataAvailable(true);
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
	 * Has the data been saved since the last update in the model.
	 * 
	 * @return See above.
	 */
	boolean isDataSaved() { return hasBeenSaved; }
	
	/**
	 * Don't care about data changes.
	 * Notifies listeners that the measurement tool does not have data to save.
	 */
	void setDataDiscarded()
	{
		hasBeenSaved = true;
		if (event == null) return;
		EventBus bus = MeasurementAgent.getRegistry().getEventBus();
		event = new SaveRelatedData(pixelsID, 
					new SaveData(pixelsID, SaveData.MEASUREMENT_TYPE), 
									"The ROI", false);
		bus.post(event);
		event = null;
	}
	
	/**
	 * The model has changed the data has not all been saved
	 * Notifies listeners that the measurement tool has data to save.
	 */
	void setDataChanged()
	{
		//Post an event.
		hasBeenSaved = false;
		if (event != null) return;
		EventBus bus = MeasurementAgent.getRegistry().getEventBus();
		event = new SaveRelatedData(pixelsID, 
						new SaveData(pixelsID, SaveData.MEASUREMENT_TYPE),
									"The ROI", true);
		bus.post(event);
	}
	
	/**
	 * Calculate the stats for the roi in the shapelist with id.
	 * @param id see above.
	 * @param shapeList see above.
	 */
	public void calculateStats(long id, ArrayList<ROIShape> shapeList)
	{
		component.analyseShapeList(shapeList);
	}
	
	/**
	 * Calculate the stats for the roi in the shapelist with id.
	 * @param id see above.
	 * @param shapeList see above.
	 */
	public void calculateStats(ArrayList<ROIShape> shapeList)
	{
		component.analyseShapeList(shapeList);
	}

	public ROI cloneROI(long id) throws ROICreationException, NoSuchROIException
	{
		return roiComponent.cloneROI(id);
	}
	
	public void deleteShape(long id, Coord3D coord) throws NoSuchROIException
	{
		roiComponent.deleteShape(id, coord);
	}
	
	public void addShape(long id, Coord3D coord, ROIShape shape) throws ROICreationException, NoSuchROIException
	{
		roiComponent.addShape(id, coord, shape);
	}
	
	public Double getPixelValue(int channel, int x, int y)
	{
		return null;
	}
	
}	
