/*
 * blitzgateway.service.gateway.RenderingEngineGateway 
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
package ome.services.blitz.gateway;


//Java imports

//Third-party libraries

//Application-internal dependencies
import omero.gateways.DSAccessException;
import omero.gateways.DSOutOfServiceException;
import omero.model.Pixels;


/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public interface RenderingEngineGateway
{	
	void keepAlive() throws  DSOutOfServiceException, DSAccessException;
	/**
	 * Render as a packedInt 
	 * @param z z section to render
	 * @param t timepoint to render
	 * @return packed int
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public int[] renderAsPackedInt(int z, int t) throws DSOutOfServiceException, DSAccessException;
	
	/**
	 * Render as a comrpessed image. 
	 * @param z z section to render
	 * @param t timepoint to render
	 * @return bytes.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public byte[] renderCompressed(int z, int t) throws  DSOutOfServiceException, DSAccessException;

	/**
	 * Set the active channels in the pixels.
	 * @param w the channel
	 * @param active set active?
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public void setActive(int w, boolean active) throws  DSOutOfServiceException, DSAccessException;

	/**
	 * Is the channel active.
	 * @param w channel
	 * @return true if the channel active.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public boolean isActive(int w) throws  DSOutOfServiceException, DSAccessException;

	/**
	 * Get the default Z section of the image
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public int getDefaultZ() throws  DSOutOfServiceException, DSAccessException;
	
	/**
	 * Get the default T point of the image
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public int getDefaultT() throws  DSOutOfServiceException, DSAccessException;
	
	/**
	 * Set the default Z section of the image.
	 * @param z see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public void setDefaultZ(int z) throws  DSOutOfServiceException, DSAccessException;
	
	/**
	 * Set the default timepoint of the image.
	 * @param t see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public void setDefaultT(int t) throws  DSOutOfServiceException, DSAccessException;
	
	/**
	 * Get the pixels of the Rendering engine.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public Pixels getPixels() throws  DSOutOfServiceException, DSAccessException;
	
	/**
	 * Set the channel min, max.
	 * @param w channel.
	 * @param start min.
	 * @param end max.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public void setChannelWindow(int w, double start, double end) throws  DSOutOfServiceException, DSAccessException;
	
	/**
	 * Get the channel min.
	 * @param w channel.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public double getChannelWindowStart(int w) throws  DSOutOfServiceException, DSAccessException;
	
	/**
	 * Get the channel max.
	 * @param w channel.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	public double getChannelWindowEnd(int w) throws  DSOutOfServiceException, DSAccessException;
	
	/**
	 * Set the compression level for the compressed images.
	 * @param percentage see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	void setCompressionLevel(float percentage) throws  DSOutOfServiceException, DSAccessException;

	/**
	 * Get the compression level for the compressed images.
	 * @return see above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	float getCompressionLevel() throws  DSOutOfServiceException, DSAccessException;


	/* 
	 * RenderingEngine java to ICE Mappings from the API.ice slice definition.
	 * Below are all the calls in the RenderingEngine service. 
	 * As the are created in the RenderingEngineGateway they will be marked as done.
	 *
	 *
	 *
	 *
	 *	
		omero::romio::RGBBuffer render(omero::romio::PlaneDef def) throws  DSOutOfServiceException, DSAccessException;
DONE	Ice::IntSeq renderAsPackedInt(omero::romio::PlaneDef def) throws  DSOutOfServiceException, DSAccessException;
DONE	Ice::ByteSeq renderCompressed(omero::romio::PlaneDef def) throws  DSOutOfServiceException, DSAccessException;
		void lookupPixels(long pixelsId) throws  DSOutOfServiceException, DSAccessException;
		bool lookupRenderingDef(long pixelsId) throws  DSOutOfServiceException, DSAccessException;
		void loadRenderingDef(long renderingDefId) throws  DSOutOfServiceException, DSAccessException;
		void load() throws  DSOutOfServiceException, DSAccessException;
		void setModel(omero::model::RenderingModel model) throws  DSOutOfServiceException, DSAccessException;
		omero::model::RenderingModel getModel() throws  DSOutOfServiceException, DSAccessException;
DONE	int getDefaultZ() throws  DSOutOfServiceException, DSAccessException;
DONE	int getDefaultT() throws  DSOutOfServiceException, DSAccessException;
DONE	void setDefaultZ(int z) throws  DSOutOfServiceException, DSAccessException;
DONE	void setDefaultT(int t) throws  DSOutOfServiceException, DSAccessException;
DONE	omero::model::Pixels getPixels() throws  DSOutOfServiceException, DSAccessException;
		IObjectList getAvailableModels() throws  DSOutOfServiceException, DSAccessException;
		IObjectList getAvailableFamilies() throws  DSOutOfServiceException, DSAccessException;
		void setQuantumStrategy(int bitResolution) throws  DSOutOfServiceException, DSAccessException;
		void setCodomainInterval(int start, int end) throws  DSOutOfServiceException, DSAccessException;
		omero::model::QuantumDef getQuantumDef() throws  DSOutOfServiceException, DSAccessException;
		void setQuantizationMap(int w, omero::model::Family fam, double coefficient, bool noiseReduction) throws  DSOutOfServiceException, DSAccessException;
		omero::model::Family getChannelFamily(int w) throws  DSOutOfServiceException, DSAccessException;
		bool getChannelNoiseReduction(int w) throws  DSOutOfServiceException, DSAccessException;
		Ice::DoubleSeq getChannelStats(int w) throws  DSOutOfServiceException, DSAccessException;
		double getChannelCurveCoefficient(int w) throws  DSOutOfServiceException, DSAccessException;
DONE	void setChannelWindow(int w, double start, double end) throws  DSOutOfServiceException, DSAccessException;
DONE	double getChannelWindowStart(int w) throws  DSOutOfServiceException, DSAccessException;
DONE	double getChannelWindowEnd(int w) throws  DSOutOfServiceException, DSAccessException;
		void setRGBA(int w, int red, int green, int blue, int alpha) throws  DSOutOfServiceException, DSAccessException;
		Ice::IntSeq getRGBA(int w) throws  DSOutOfServiceException, DSAccessException;
DONE	void setActive(int w, bool active) throws  DSOutOfServiceException, DSAccessException;
DONE	bool isActive(int w) throws  DSOutOfServiceException, DSAccessException;
		void addCodomainMap(omero::romio::CodomainMapContext mapCtx) throws  DSOutOfServiceException, DSAccessException;
		void updateCodomainMap(omero::romio::CodomainMapContext mapCtx) throws  DSOutOfServiceException, DSAccessException;
		void removeCodomainMap(omero::romio::CodomainMapContext mapCtx) throws  DSOutOfServiceException, DSAccessException;
		void saveCurrentSettings() throws  DSOutOfServiceException, DSAccessException;
		void resetDefaults() throws  DSOutOfServiceException, DSAccessException;
		void resetDefaultsNoSave() throws  DSOutOfServiceException, DSAccessException;
DONE	void setCompressionLevel(float percentage) throws  DSOutOfServiceException, DSAccessException;
DONE	float getCompressionLevel() throws  DSOutOfServiceException, DSAccessException;
		bool isPixelsTypeSigned() throws  DSOutOfServiceException, DSAccessException;
		double getPixelsTypeUpperBound(int w) throws  DSOutOfServiceException, DSAccessException;
		double getPixelsTypeLowerBound(int w) throws  DSOutOfServiceException, DSAccessException;
	*/
}


