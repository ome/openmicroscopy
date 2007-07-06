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
import java.awt.Dimension;
import java.awt.Rectangle;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


//Third-party libraries
import org.jhotdraw.draw.AttributeKeys;
import org.jhotdraw.draw.AttributeKey;
import org.jhotdraw.draw.DefaultDrawing;
import org.jhotdraw.draw.DefaultDrawingEditor;
import org.jhotdraw.draw.Drawing;
import org.jhotdraw.draw.DrawingEditor;
import org.jhotdraw.draw.Figure;

//Application-internal dependencies

import ome.model.core.Pixels;
import ome.model.core.PixelsDimensions;
import org.openmicroscopy.shoola.agents.measurement.Analyser;
import org.openmicroscopy.shoola.agents.measurement.ChannelMetadataLoader;
import org.openmicroscopy.shoola.agents.measurement.MeasurementAgent;
import org.openmicroscopy.shoola.agents.measurement.MeasurementViewerLoader;
import org.openmicroscopy.shoola.agents.measurement.PixelsLoader;
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.data.model.ChannelMetadata;
import org.openmicroscopy.shoola.util.file.IOUtil;
import org.openmicroscopy.shoola.util.roi.model.annotation.AnnotationKeys;
import org.openmicroscopy.shoola.util.roi.ROIComponent;
import org.openmicroscopy.shoola.util.roi.exception.NoSuchROIException;
import org.openmicroscopy.shoola.util.roi.exception.ParsingException;
import org.openmicroscopy.shoola.util.roi.exception.ROICreationException;
import org.openmicroscopy.shoola.util.roi.figures.ROIFigure;
import org.openmicroscopy.shoola.util.roi.model.ROI;
import org.openmicroscopy.shoola.util.roi.model.ROIShape;
import org.openmicroscopy.shoola.util.roi.model.ShapeList;
import org.openmicroscopy.shoola.util.roi.model.util.Coord3D;
import pojos.ExperimenterData;

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
    
    /** Component managaging the drawing. */
    private	DefaultDrawing			drawing;

    /** Component managaging the drawing. */
	private	DrawingEditor			drawingEditor;
	
	/** Component hosting the drawing. */
	private ROIDrawingView			drawingView;
	
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
    
    /** Boolean to indicating that the tool has been posted to the viewer.*/
    //private boolean 				toolSent;
    
    /**
     * Returns the current user's details.
     * 
     * @return See above.
     */
    private ExperimenterData getUserDetails()
    { 
    	return (ExperimenterData) MeasurementAgent.getRegistry().lookup(
    			LookupNames.CURRENT_USER_DETAILS);
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
		drawingEditor = new DefaultDrawingEditor();
		drawing = new DefaultDrawing();
		drawingView = new ROIDrawingView();
		roiComponent = new ROIComponent();
		drawingView.setDrawing(drawing);
		drawingEditor.add(drawingView);
		roiFileName = imageID+".xml";
		//toolSent = false;
	}
	
	/**
	 * This checks that we've not posted a message to the viewer already to 
	 * add the drawing view of the measurement component. Setting this value to 
	 * true indicates the the MeasurementToolLoaded.ADD has been posted to the 
	 * viewer. 
	 * 
	 * @param state see above.
	 */
	/*
	void setToolSent(boolean state)
	{
		toolSent = state;
	}
	*/
	/**
	 * This checks that we've not posted a message to the viewer already to 
	 * add the drawing view of the measurement component. if this value is 
	 * true it indicates the the MeasurementToolLoaded.ADD has been posted to 
	 * the viewer. 
	 * @return see above.
	 */
	/*
	boolean getToolSent()
	{
		return toolSent;
	}
	*/
	
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
	void setPlane(int z, int t) { currentPlane = new Coord3D(z, t); }
	
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
    DrawingEditor getDrawingEditor() { return drawingEditor; }
    
    /**
     * Returns the drawing.
     * 
     * @return See above.
     */
    Drawing getDrawing() { return drawing; }

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
	 * Fires an asynchronous retrieval of the ROI related to the pixels set. 
	 * 
	 * @param fileName The name of the file to load. If <code>null</code>
	 * 					the {@link #roiFileName} is selected.
	 */
	void fireROILoading(String fileName)
	{
		state = MeasurementViewer.LOADING_ROI;
		if (fileName == null) fileName = roiFileName;
		component.setROI(IOUtil.readFile(fileName));
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
			drawingView.setScaleFactor(magnification, new 
						Dimension(getSizeX(), getSizeY()));
		else drawingView.setScaleFactor(magnification);
	}

	/** 
	 * Sets the ROI for the pixels set.
	 *  
	 * @param input 		The value to set.
	 * @throws Exception	Forward exception thrown by the 
	 * 						{@link ROIComponent}.
	 */
	void setROI(InputStream input)
		throws Exception
	{
		if (input != null) roiComponent.loadROI(input);
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
	 * Returns the {@link ROIDrawingView}.
	 * 
	 * @return See above.
	 */
	ROIDrawingView getDrawingView() { return drawingView; }
	
	/** 
	 * Get the ROI of the currently selected figure in the drawingview. 
	 * 
	 * @return see above.
	 */
	Collection<ROI> getSelectedROI()
	{
		Collection<Figure> selectedFigs = drawingView.getSelectedFigures();
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
	 * Removes the <code>ROI</code> corresponding to the passed id.
	 * 
	 * @param id The id of the <code>ROI</code>.
	 * @throws NoSuchROIException If the ROI does not exist.
	 */
	void removeROIShape(long id)
		throws NoSuchROIException
	{
		roiComponent.deleteShape(id, getCurrentView());
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
		if (attribute.getKey().equals(AttributeKeys.TEXT.getKey())) {
			ROIShape shape = figure.getROIShape();
			AnnotationKeys.BASIC_TEXT.set(shape, 
									AttributeKeys.TEXT.get(figure));
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
	}
	
	/**
	 * Saves the current ROISet in the roi component to file.
	 * @throws ParsingException
	 */
	void saveROI()
		throws ParsingException
	{
		roiComponent.saveROI(IOUtil.writeFile(roiFileName));
	}
	
	/**
	 * Propagates the selected shape in the roi model. 
	 * 
	 * @param shape 	The ROIShape to propagate.
	 * @param timePoint The timepoint to propagate to.
	 * @param zSection 	The z-section to propagate to.
	 * @throws ROICreationException
	 * @throws NoSuchROIException
	 */
	void propagateShape(ROIShape shape, int timePoint, int zSection) 
		throws 	ROICreationException, NoSuchROIException
	{
		roiComponent.propagateShape(shape.getID(), shape.getCoord3D(), 
		shape.getCoord3D(), new Coord3D(zSection, timePoint));
	}
	
	/**
	 * Deletes the selected shape from current coord to timepoint and z-section.
	 * 
	 * @param shape 	The ROIShape to propagate.
	 * @param timePoint The timepoint to propagate to.
	 * @param zSection 	The z-section to propagate to.
	 * @throws ROICreationException
	 * @throws NoSuchROIException
	 */
	void deleteShape(ROIShape shape, int timePoint, int zSection) 
		throws 	ROICreationException, NoSuchROIException
	{
		roiComponent.deleteShape(shape.getID(), shape.getCoord3D(), 
			new Coord3D(zSection, timePoint));
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
	}
	
	/**
	 * Fires an asynchronous call to analyse the passed shape.
	 *  
	 * @param shape The shape to analyse. Mustn't be <code>null</code>.
	 */
	void fireAnalyzeShape(ROIShape shape)
	{
		List l = new ArrayList(1);
		l.add(shape);
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
	 * Get the active channels for the data.
	 * @return active channels.
	 */
	public Map getActiveChannels()
	{
		return activeChannels;
	}
	
}	
