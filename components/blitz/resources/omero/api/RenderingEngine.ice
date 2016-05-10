/*
 *   $Id$
 *
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#ifndef OMERO_API_RENDERINGENGINE_ICE
#define OMERO_API_RENDERINGENGINE_ICE

#include <omero/ModelF.ice>
#include <omero/Collections.ice>
#include <omero/ROMIO.ice>
#include <omero/Constants.ice>
#include <omero/api/PyramidService.ice>

module omero {

    module api {

        /**
         * Defines a service to render a given pixels set.
         *
         * A pixels set is a <i>5D</i> array that stores the pixels data of an
         * image, that is the pixels intensity values. Every instance of this
         * service is paired up to a pixels set. Use this service to transform
         * planes within the pixels set onto an <i>RGB</i> image.
         *
         * The RenderingEngine allows to fine-tune the settings that
         * define the transformation context &#151; that is, a specification
         * of how raw pixels data is to be transformed into an image that can
         * be displayed on screen. Those settings are referred to as rendering
         * settings or display options. After tuning those settings it is
         * possible to save them to the metadata repository so that they can
         * be used the next time the pixels set is accessed for rendering; for
         * example by another RenderingEngine instance. Note that the display
         * options are specific to the given pixels set and are experimenter
         * scoped &#151; that is, two different users can specify different
         * display options for the <i>same</i> pixels set. (A RenderingEngine
         * instance takes this into account automatically as it is always
         * bound to a given experimenter.)
         *
         * This service is <b>thread-safe</b>.
         **/
        ["ami", "amd"] interface RenderingEngine extends PyramidService
            {
                /**
                 * Renders the data selected by <code>def</code> according to
                 * the current rendering settings.
                 * The passed argument selects a plane orthogonal to one
                 * of the <i>X</i>, <i>Y</i>, or <i>Z</i> axes. How many
                 * wavelengths are rendered and what color model is used
                 * depends on the current rendering settings.
                 *
                 * @param def Selects a plane orthogonal to one of the
                 *            <i>X</i>, <i>Y</i>, or <i>Z</i> axes.
                 * @return An <i>RGB</i> image ready to be displayed on screen.
                 * @throws ValidationException
                 *             If <code>def</code> is <code>null</code>.
                 */
                idempotent omero::romio::RGBBuffer render(omero::romio::PlaneDef def) throws ServerError;

                /**
                 * Renders the data selected by <code>def</code> according to
                 * the current rendering settings.
                 * The passed argument selects a plane orthogonal to one
                 * of the <i>X</i>, <i>Y</i>, or <i>Z</i> axes. How many
                 * wavelengths are rendered and what color model is used
                 * depends on the current rendering settings.
                 *
                 * @param def Selects a plane orthogonal to one of the
                 *            <i>X</i>, <i>Y</i>, or <i>Z</i> axes.
                 * @return An <i>RGB</i> image ready to be displayed on screen.
                 * @throws ValidationException
                 *             If <code>def</code> is <code>null</code>.
                 * @see {@link #render}
                 */
                idempotent Ice::IntSeq renderAsPackedInt(omero::romio::PlaneDef def) throws ServerError;

                /**
                 * Performs a projection through selected optical sections of
                 * a particular timepoint with the currently active channels
                 * and renders the data for display.
                 *
                 * @param algorithm {@link ome.api.IProjection#MAXIMUM_INTENSITY},
                 * {@link ome.api.IProjection#MEAN_INTENSITY} or
                 * {@link ome.api.IProjection#SUM_INTENSITY}.
                 * @param stepping Stepping value to use while calculating the
                 *                 projection.
                 *                 For example, <code>stepping=1</code> will
                 *                 use every optical section from
                 *                 <code>start</code> to <code>end</code>
                 *                 where <code>stepping=2</code> will use every
                 *                 other section from <code>start</code> to
                 *                 <code>end</code> to perform the projection.
                 * @param start Optical section to start projecting from.
                 * @param end Optical section to finish projecting.
                 * @return A packed-integer <i>RGBA</i> rendered image of the
                 *         projected pixels.
                 * @throws ValidationException Where:
                 * <ul>
                 *   <li><code>algorithm</code> is unknown</li>
                 *   <li><code>timepoint</code> is out of range</li>
                 *   <li><code>start</code> is out of range</li>
                 *   <li><code>end</code> is out of range</li>
                 *   <li><code>start > end</code></li>
                 * </ul>
                 * @see omero.api.IProjection#projectPixels
                 */
                idempotent Ice::IntSeq renderProjectedAsPackedInt(omero::constants::projection::ProjectionType algorithm, int timepoint, int stepping, int start, int end) throws ServerError;

                /**
                 * Renders the data selected by <code>def</code> according to
                 * the current rendering settings and compresses the resulting
                 * RGBA composite image.
                 *
                 * @param def Selects a plane orthogonal to one of the
                 *            <i>X</i>, <i>Y</i> or <i>Z</i> axes.
                 * @return A compressed RGBA JPEG for display.
                 * @throws ValidationException
                 *             If <code>def</code> is <code>null</code>.
                 * @see #render
                 * @see #renderAsPackedInt
                 */
                idempotent Ice::ByteSeq renderCompressed(omero::romio::PlaneDef def) throws ServerError;

                /**
                 * Performs a projection through selected optical sections of
                 * a particular timepoint with the currently active channels,
                 * renders the data for display and compresses the resulting
                 * RGBA composite image.
                 *
                 * @param algorithm {@link ome.api.IProjection#MAXIMUM_INTENSITY},
                 * {@link ome.api.IProjection#MEAN_INTENSITY} or
                 * {@link ome.api.IProjection#SUM_INTENSITY}.
                 * @param stepping Stepping value to use while calculating the
                 *                 projection.
                 *                 For example, <code>stepping=1</code> will
                 *                 use every optical section from
                 *                 <code>start</code> to <code>end</code>
                 *                 where <code>stepping=2</code> will use every
                 *                 other section from <code>start</code> to
                 *                 <code>end</code> to perform the projection.
                 * @param start Optical section to start projecting from.
                 * @param end Optical section to finish projecting.
                 * @return A compressed <i>RGBA</i> rendered JPEG image of the
                 *         projected pixels.
                 * @throws ValidationException Where:
                 * <ul>
                 *   <li><code>algorithm</code> is unknown</li>
                 *   <li><code>timepoint</code> is out of range</li>
                 *   <li><code>start</code> is out of range</li>
                 *   <li><code>end</code> is out of range</li>
                 *   <li><code>start > end</code></li>
                 * </ul>
                 * @see omero.api.IProjection#projectPixels
                 */
                idempotent Ice::ByteSeq renderProjectedCompressed(omero::constants::projection::ProjectionType algorithm, int timepoint, int stepping, int start, int end) throws ServerError;

                /**
                 * Returns the id of the {@link omero.model.RenderingDef}
                 * loaded by either {@link #lookupRenderingDef} or
                 * {@link #loadRenderingDef}.
                 */
                idempotent long getRenderingDefId() throws ServerError;

                /**
                 * Loads the Pixels set this Rendering Engine is for.
                 *
                 * @param pixelsId The pixels set ID.
                 */
                idempotent void lookupPixels(long pixelsId) throws ServerError;

                /**
                 * Loads the rendering settings associated to the specified
                 * pixels set.
                 *
                 * @param pixelsId The pixels set ID.
                 * @return <code>true</code> if a RenderingDef exists for the
                 *         Pixels set, otherwise <code>false</code>.
                 */
                idempotent bool lookupRenderingDef(long pixelsId) throws ServerError;

                /**
                 * Loads a specific set of rendering settings that does not
                 * necessarily have to be linked to the given Pixels set.
                 * However, the rendering settings <b>must</b> be linked to a
                 * compatible Pixels set as defined by
                 * {@link omero.api.IRenderingSettings#sanityCheckPixels}.
                 *
                 * @param renderingDefId The rendering definition ID.
                 * @throws ValidationException If a RenderingDef does not
                 *         exist with the ID <code>renderingDefId</code> or if
                 *         the RenderingDef is incompatible due to differing
                 *         pixels sets.
                 */
                idempotent void loadRenderingDef(long renderingDefId) throws ServerError;

                /**
                 * Informs the rendering engine that it should render a set of
                 * overlays on each rendered frame. These are expected to be
                 * binary masks.
                 * @param overlays Binary mask to color map.
                 */
                ["deprecated: use omero::romio::PlaneDefWithMasks instead"] idempotent void setOverlays(omero::RLong tablesId, omero::RLong imageId, LongIntMap rowColorMap) throws ServerError;

                /** Creates a instance of the rendering engine. */
                idempotent void load() throws ServerError;

                /**
                 * Specifies the model that dictates how transformed raw data
                 * has to be mapped onto a color space.
                 *
                 * @param model Identifies the color space model.
                 */
                idempotent void setModel(omero::model::RenderingModel model) throws ServerError;

                /**
                 * Returns the model that dictates how transformed raw data
                 * has to be mapped onto a color space.
                 */
                idempotent omero::model::RenderingModel getModel() throws ServerError;

                /**
                 * Returns the index of the default focal section.
                 */
                idempotent int getDefaultZ() throws ServerError;

                /**
                 * Returns the default timepoint index.
                 */
                idempotent int getDefaultT() throws ServerError;

                /**
                 * Sets the index of the default focal section. This index is
                 * used to define a default plane.
                 *
                 * @param z The value to set.
                 */
                idempotent void setDefaultZ(int z) throws ServerError;

                /**
                 * Sets the default timepoint index. This index is used to
                 * define a default plane.
                 *
                 * @param t The value to set.
                 */
                idempotent void setDefaultT(int t) throws ServerError;

                /**
                 * Returns the {@link omero.model.Pixels} set the Rendering
                 * engine is for.
                 */
                idempotent omero::model::Pixels getPixels() throws ServerError;

                /**
                 * Returns the list of color models supported by the Rendering
                 * engine.
                 */
                idempotent IObjectList getAvailableModels() throws ServerError;

                /**
                 * Returns the list of mapping families supported by the
                 * Rendering engine.
                 */
                idempotent IObjectList getAvailableFamilies() throws ServerError;

                /**
                 * Sets the quantization strategy. The strategy is common to
                 * all channels.
                 *
                 * @param bitResolution The bit resolution defining associated
                 *                      to the strategy.
                 */
                idempotent void setQuantumStrategy(int bitResolution) throws ServerError;

                /**
                 * Sets the sub-interval of the device space i.e. a discrete
                 * sub-interval of \[0, 255].
                 *
                 * @param start The lower bound of the interval.
                 * @param end The upper bound of the interval.
                 */
                idempotent void setCodomainInterval(int start, int end) throws ServerError;

                /**
                 * Returns the quantization object.
                 */
                idempotent omero::model::QuantumDef getQuantumDef() throws ServerError;

                /**
                 * Sets the quantization map, one per channel.
                 *
                 * @param w  The channel index.
                 * @param family The mapping family.
                 * @param coefficient The coefficient identifying a curve in
                 *                    the family.
                 * @param noiseReduction Pass <code>true</code> to turn the
                 *                       noise reduction algorithm on,
                 *                       <code>false</code> otherwise.
                 * @see #getAvailableFamilies
                 * @see #getChannelCurveCoefficient
                 * @see #getChannelFamily
                 * @see #getChannelNoiseReduction
                 */
                idempotent void setQuantizationMap(int w, omero::model::Family fam, double coefficient, bool noiseReduction) throws ServerError;

                /**
                 * Returns the family associated to the specified channel.
                 *
                 * @param w The channel index.
                 * @return See above.
                 * @see #getAvailableFamilies
                 */
                idempotent omero::model::Family getChannelFamily(int w) throws ServerError;

                /**
                 * Returns <code>true</code> if the noise reduction algorithm
                 * used to map the pixels intensity values is turned on,
                 * <code>false</code> if the algorithm is turned off. Each
                 * channel has an algorithm associated to it.
                 *
                 * @param w The channel index.
                 * @return See above.
                 */
                idempotent bool getChannelNoiseReduction(int w) throws ServerError;
                idempotent Ice::DoubleSeq getChannelStats(int w) throws ServerError;

                /**
                 * Returns the coefficient identifying a map in the family.
                 * Each channel has a map associated to it.
                 *
                 * @param w The channel index.
                 * @return See above.
                 * @see #getChannelFamily
                 */
                idempotent double getChannelCurveCoefficient(int w) throws ServerError;

                /**
                 * Returns the pixels intensity interval. Each channel has a
                 * pixels intensity interval associated to it.
                 *
                 * @param w The channel index.
                 * @param start The lower bound of the interval.
                 * @param end The upper bound of the interval.
                 */
                idempotent void setChannelWindow(int w, double start, double end) throws ServerError;

                /**
                 * Returns the lower bound of the pixels intensity interval.
                 * Each channel has a pixels intensity interval associated to
                 * it.
                 *
                 * @param w The channel index.
                 */
                idempotent double getChannelWindowStart(int w) throws ServerError;

                /**
                 * Returns the upper bound of the pixels intensity interval.
                 * Each channel has a pixels intensity interval associated to
                 * it.
                 *
                 * @param w The channel index.
                 */
                idempotent double getChannelWindowEnd(int w) throws ServerError;

                /**
                 * Sets the four components composing the color associated to
                 * the specified channel.
                 *
                 * @param w The channel index.
                 * @param red The red component. A value between 0 and 255.
                 * @param green The green component. A value between 0 and 255.
                 * @param blue The blue component. A value between 0 and 255.
                 * @param alpha The alpha component. A value between 0 and 255.
                 */
                idempotent void setRGBA(int w, int red, int green, int blue, int alpha) throws ServerError;

                /**
                 * Returns a 4D-array representing the color associated to the
                 * specified channel. The first element corresponds to the red
                 * component (value between 0 and 255). The second corresponds
                 * to the green component (value between 0 and 255). The third
                 * corresponds to the blue component (value between 0 and
                 * 255). The fourth corresponds to the alpha component (value
                 * between 0 and 255).
                 *
                 * @param w The channel index.
                 */
                idempotent Ice::IntSeq getRGBA(int w) throws ServerError;

                /**
                 * Maps the specified channel if <code>true</code>, unmaps the
                 * channel otherwise.
                 *
                 * @param w The channel index.
                 * @param active Pass <code>true</code> to map the channel,
                 *               <code>false</code> otherwise.
                 */
                idempotent void setActive(int w, bool active) throws ServerError;

                /**
                 * Returns <code>true</code> if the channel is mapped,
                 * <code>false</code> otherwise.
                 *
                 * @param w The channel index.
                 */
                idempotent bool isActive(int w) throws ServerError;
                idempotent void setChannelLookupTable(int w, string lookup) throws ServerError;
                idempotent string getChannelLookupTable(int w) throws ServerError;

                /**
                 * Adds the context to the mapping chain. Only one context of
                 * the same type can be added to the chain. The codomain
                 * transformations are functions from the device space to
                 * device space. Each time a new context is added, the second
                 * LUT is rebuilt.
                 *
                 * @param mapCtx The context to add.
                 * @see #updateCodomainMap
                 * @see #removeCodomainMap
                 */
                void addCodomainMap(omero::romio::CodomainMapContext mapCtx) throws ServerError;

                /**
                 * Updates the specified context. The codomain chain already
                 * contains the specified context. Each time a new context is
                 * updated, the second LUT is rebuilt.
                 *
                 * @param mapCtx The context to update.
                 * @see #addCodomainMap
                 * @see #removeCodomainMap
                 */
                void updateCodomainMap(omero::romio::CodomainMapContext mapCtx) throws ServerError;

                /**
                 * Removes the specified context from the chain. Each time a
                 * new context is removed, the second LUT is rebuilt.
                 *
                 * @param mapCtx The context to remove.
                 * @see #addCodomainMap
                 * @see #updateCodomainMap
                 */
                void removeCodomainMap(omero::romio::CodomainMapContext mapCtx) throws ServerError;

                /** Saves the current rendering settings in the database. */
                void saveCurrentSettings() throws ServerError;

                /**
                 * Saves the current rendering settings in the database
                 * as a new {@link omero.model.RenderingDef} and loads the
                 * object into the current {@link omero.api.RenderingEngine}.
                 */
                long saveAsNewSettings() throws ServerError;

                /**
                 * Resets the default settings i.e. the default values
                 * internal to the Rendering engine. The settings will be
                 * saved.
                 *
                 * @param save Pass <code>true</code> to save the settings,
                 *             <code>false</code> otherwise.
                 */
                long resetDefaultSettings(bool save) throws ServerError;

		/**
		 * Sets the current compression level for the service. (The
                 * default is 85%)
		 *
		 * @param percentage A percentage compression level from 1.00
                 *                  (100%) to 0.01 (1%).
		 * @throws ValidationException if the <code>percentage</code
                 *         is out of range.
		 */
                idempotent void setCompressionLevel(float percentage) throws ServerError;

		/**
		 * Returns the current compression level for the service.
		 */
                idempotent float getCompressionLevel() throws ServerError;

		/**
                 * Returns <code>true</code> if the pixels type is signed,
                 * <code>false</code> otherwise.
                 */
                idempotent bool isPixelsTypeSigned() throws ServerError;

		/**
                 * Returns the minimum value for that channels depending on
                 * the pixels type and the original range (globalmax,
                 * globalmin)
                 *
                 * @param w The channel index.
                 */
                idempotent double getPixelsTypeUpperBound(int w) throws ServerError;

		/**
                 * Returns the maximum value for that channels depending on
                 * the pixels type and the original range (globalmax,
                 * globalmin)
                 *
                 * @param w The channel index.
                 */
                idempotent double getPixelsTypeLowerBound(int w) throws ServerError;

            };
    };
};

#endif
