/*
 * org.openmicroscopy.shoola.env.data.views.ImViewerViewImpl
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
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

package org.openmicroscopy.shoola.env.data.views;



//Java imports
import java.util.List;

//Third-party libraries

//Application-internal dependencies
import omero.romio.PlaneDef;
import org.openmicroscopy.shoola.env.data.model.ProjectionParam;
import org.openmicroscopy.shoola.env.data.views.calls.AcquisitionDataLoader;
import org.openmicroscopy.shoola.env.data.views.calls.AcquisitionDataSaver;
import org.openmicroscopy.shoola.env.data.views.calls.Analyser;
import org.openmicroscopy.shoola.env.data.views.calls.ChannelMetadataLoader;
import org.openmicroscopy.shoola.env.data.views.calls.EnumerationLoader;
import org.openmicroscopy.shoola.env.data.views.calls.ImageRenderer;
import org.openmicroscopy.shoola.env.data.views.calls.PixelsDataLoader;
import org.openmicroscopy.shoola.env.data.views.calls.PlaneInfoLoader;
import org.openmicroscopy.shoola.env.data.views.calls.ProjectionSaver;
import org.openmicroscopy.shoola.env.data.views.calls.RenderingControlLoader;
import org.openmicroscopy.shoola.env.data.views.calls.RenderingSettingsLoader;
import org.openmicroscopy.shoola.env.data.views.calls.RenderingSettingsSaver;
import org.openmicroscopy.shoola.env.event.AgentEventListener;
import org.openmicroscopy.shoola.env.rnd.RndProxyDef;
import pojos.PixelsData;


/** 
 * Implementation of the {@link ImageDataView} interface.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">a.falconi@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since OME2.2
 */
class ImageDataViewImpl
    implements ImageDataView
{

    /**
     * Implemented as specified by the view interface.
     * @see ImageDataView#loadChannelMetadata(long, AgentEventListener)
     */
    public CallHandle loadChannelMetadata(long imageID,
                                        AgentEventListener observer)
    {
        BatchCallTree cmd = new ChannelMetadataLoader(imageID);
        return cmd.exec(observer);
    }

    /**
     * Implemented as specified by the view interface.
     * @see ImageDataView#loadRenderingControl(long, int, 
     * 											AgentEventListener)
     */
    public CallHandle loadRenderingControl(long pixelsID, int index,
                                        AgentEventListener observer)
    {
    	int i = -1;
    	switch (index) {
    		default:
			case LOAD:
				i = RenderingControlLoader.LOAD;
				break;
			case RELOAD:
				i = RenderingControlLoader.RELOAD;
				break;
			case RESET:
				i = RenderingControlLoader.RESET;
		}
        BatchCallTree cmd = new RenderingControlLoader(pixelsID, i);
        return cmd.exec(observer);
    }

    /**
     * Implemented as specified by the view interface.
     * @see ImageDataView#render(long, PlaneDef, AgentEventListener)
     */
    public CallHandle render(long pixelsID, PlaneDef pd, 
                        AgentEventListener observer)
    {
        BatchCallTree cmd = new ImageRenderer(pixelsID, pd);
        return cmd.exec(observer);
    }

	/**
     * Implemented as specified by the view interface.
     * @see ImageDataView#loadPixels(long, AgentEventListener)
     */
	public CallHandle loadPixels(long pixelsID, AgentEventListener observer) 
	{
		BatchCallTree cmd = new PixelsDataLoader(pixelsID, 
									PixelsDataLoader.SET);
		return cmd.exec(observer);
	}

	/**
     * Implemented as specified by the view interface.
     * @see ImageDataView#analyseShapes(PixelsData, List, List, 
     * 									AgentEventListener)
     */
	public CallHandle analyseShapes(PixelsData pixels, List channels, 
			List shapes, AgentEventListener observer)
	{
		BatchCallTree cmd = new Analyser(pixels, channels, shapes);
		return cmd.exec(observer);
	}

	/**
     * Implemented as specified by the view interface.
     * @see ImageDataView#getRenderingSettings(long, AgentEventListener)
     */
	public CallHandle getRenderingSettings(long pixelsID, 
										AgentEventListener observer)
	{
		BatchCallTree cmd = new RenderingSettingsLoader(pixelsID);
		return cmd.exec(observer);
	}

	/**
     * Implemented as specified by the view interface.
     * @see ImageDataView#renderProjected(long, int, int, int, int, List, 
     *                       AgentEventListener)
     */
	public CallHandle renderProjected(long pixelsID, int startZ, int endZ, 
			int stepping, int algorithm, List<Integer> channels, 
			AgentEventListener observer)
    {
		BatchCallTree cmd = new ProjectionSaver(pixelsID, startZ, endZ, 
				                  stepping, algorithm, channels);
		return cmd.exec(observer);
	}

	/**
     * Implemented as specified by the view interface.
     * @see ImageDataView#projectImage(ProjectionParam, AgentEventListener)
     */
	public CallHandle projectImage(ProjectionParam ref, 
			AgentEventListener observer)
	{
		BatchCallTree cmd = new ProjectionSaver(ref);
        return cmd.exec(observer);
	}

	/**
     * Implemented as specified by the view interface.
     * @see ImageDataView#createRndSetting(long, RndProxyDef, List, 
     * 										AgentEventListener)
     */
	public CallHandle createRndSetting(long pixelsID, RndProxyDef rndToCopy, 
			List<Integer> indexes, AgentEventListener observer)
	{
		BatchCallTree cmd = new RenderingSettingsSaver(pixelsID, rndToCopy, 
									indexes);
        return cmd.exec(observer);
	}

	/**
     * Implemented as specified by the view interface.
     * @see ImageDataView#loadAcquisitionData(Object, AgentEventListener)
     */
	public CallHandle loadAcquisitionData(Object refObject, 
			AgentEventListener observer)
	{
		BatchCallTree cmd = new AcquisitionDataLoader(refObject);
		return cmd.exec(observer);
	}

	/**
     * Implemented as specified by the view interface.
     * @see ImageDataView#saveAcquisitionData(Object, AgentEventListener)
     */
	public CallHandle saveAcquisitionData(Object refObject, 
			AgentEventListener observer)
	{
		BatchCallTree cmd = new AcquisitionDataSaver(refObject);
		return cmd.exec(observer);
	}
	
	/**
     * Implemented as specified by the view interface.
     * @see ImageDataView#loadPlaneInfo(long, AgentEventListener)
     */
	public CallHandle loadPlaneInfo(long pixelsID, AgentEventListener observer)
	{
		BatchCallTree cmd = new PlaneInfoLoader(pixelsID);
		return cmd.exec(observer);
	}

	/**
     * Implemented as specified by the view interface.
     * @see ImageDataView#loadChannelMetadataEnumerations(AgentEventListener)
     */
	public CallHandle loadChannelMetadataEnumerations(AgentEventListener 
					observer) 
	{
		BatchCallTree cmd = new EnumerationLoader(EnumerationLoader.CHANNEL);
		return cmd.exec(observer);
	}

	/**
     * Implemented as specified by the view interface.
     * @see ImageDataView#loadImageMetadataEnumerations(AgentEventListener)
     */
	public CallHandle loadImageMetadataEnumerations(AgentEventListener observer) 
	{
		BatchCallTree cmd = new EnumerationLoader(EnumerationLoader.IMAGE);
		return cmd.exec(observer);
	}
	
}
