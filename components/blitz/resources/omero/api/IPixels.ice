/*
 *   $Id$
 *
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#ifndef OMERO_API_ICONFIG_ICE
#define OMERO_API_ICONFIG_ICE

#include <omero/Collections.ice>
#include <omero/ServicesF.ice>

module omero {

    module api {
        /**
         * Metadata gateway for the {@link omero.api.RenderingEngine} and
         * clients. This service provides all DB access that the rendering
         * engine needs as well as Pixels services to a client. It also allows
         * the rendering  engine to also be run external to the server (e.g.
         * client-side).
         *
         **/
        ["ami", "amd"] interface IPixels extends ServiceInterface
            {
                /**
                 * Retrieves the pixels metadata. The following objects are
                 * pre-linked:
                 * <ul>
                 * <li>pixels.pixelsType</li>
                 * <li>pixels.pixelsDimensions</li>
                 * <li>pixels.channels</li>
                 * <li>pixels.channnels.statsInfo</li>
                 * <li>pixels.channnels.colorComponent</li>
                 * <li>pixels.channnels.logicalChannel</li>
                 * <li>pixels.channnels.logicalChannel.photometricInterpretation</li>
                 * </ul>
                 *
                 * @param pixId Pixels id.
                 * @return Pixels object which matches <code>id</code>.
                 **/
                idempotent omero::model::Pixels retrievePixDescription(long pixId) throws ServerError;

                /**
                 * Retrieves the rendering settings for a given pixels set and
                 * the currently logged in user. If the current user has no
                 * {@link omero.model.RenderingDef}, and the user is an
                 * administrator, then a {@link omero.model.RenderingDef} may
                 * be returned for the owner of the
                 *{@link omero.model.Pixels}. This matches the behavior of the
                 * Rendering service.
                 *
                 * The following objects will be pre-linked:
                 * <ul>
                 * <li>renderingDef.quantization</li>
                 * <li>renderingDef.model</li>
                 * <li>renderingDef.waveRendering</li>
                 * <li>renderingDef.waveRendering.color</li>
                 * <li>renderingDef.waveRendering.family</li>
                 * <li>renderingDef.spatialDomainEnhancement</li>
                 * </ul>
                 *
                 * @param pixId Pixels id.
                 * @return Rendering definition.
                 */
                idempotent omero::model::RenderingDef retrieveRndSettings(long pixId) throws ServerError;

                /**
                 * Retrieves the rendering settings for a given pixels set and
                 * the passed user. The following objects are pre-linked:
                 * <ul>
                 * <li>renderingDef.quantization</li>
                 * <li>renderingDef.model</li>
                 * <li>renderingDef.waveRendering</li>
                 * <li>renderingDef.waveRendering.color</li>
                 * <li>renderingDef.waveRendering.family</li>
                 * <li>renderingDef.spatialDomainEnhancement</li>
                 * </ul>
                 *
                 * @param pixId Pixels id.
                 * @param userID  The id of the user.
                 * @return Rendering definition.
                 **/
                idempotent omero::model::RenderingDef retrieveRndSettingsFor(long pixId, long userId) throws ServerError;

                /**
                 * Retrieves all the rendering settings for a given pixels set
                 * and the passed user. The following objects are pre-linked:
                 * <ul>
                 * <li>renderingDef.quantization</li>
                 * <li>renderingDef.model</li>
                 * <li>renderingDef.waveRendering</li>
                 * <li>renderingDef.waveRendering.color</li>
                 * <li>renderingDef.waveRendering.family</li>
                 * <li>renderingDef.spatialDomainEnhancement</li>
                 * </ul>
                 *
                 * @param pixId    Pixels id.
                 * @param userId   The id of the user.
                 * @return Rendering definition.
                 **/
                idempotent IObjectList retrieveAllRndSettings(long pixId, long userId) throws ServerError;

                /**
                 * Loads a specific set of rendering settings. The
                 * following objects are pre-linked:
                 * <ul>
                 * <li>renderingDef.quantization</li>
                 * <li>renderingDef.model</li>
                 * <li>renderingDef.waveRendering</li>
                 * <li>renderingDef.waveRendering.color</li>
                 * <li>renderingDef.waveRendering.family</li>
                 * <li>renderingDef.spatialDomainEnhancement</li>
                 * </ul>
                 *
                 * @param renderingSettingsId Rendering definition id.
                 * @throws ValidationException If no <code>RenderingDef</code>
                 * matches the ID <code>renderingDefId</code>.
                 * @return Rendering definition.
                 **/
                idempotent omero::model::RenderingDef loadRndSettings(long renderingSettingsId) throws ServerError;

                /**
                 * Saves the specified rendering settings.
                 *
                 * @param rndSettings Rendering settings.
                 **/
                void saveRndSettings(omero::model::RenderingDef rndSettings) throws ServerError;

                /**
                 * Bit depth for a given pixel type.
                 *
                 * @param type Pixels type.
                 * @return Bit depth in bits.
                 **/
                idempotent int getBitDepth(omero::model::PixelsType type) throws ServerError;

                /**
                 * Retrieves a particular enumeration for a given enumeration
                 * class.
                 *
                 * @param enumClass Enumeration class.
                 * @param value Enumeration string value.
                 * @return Enumeration object.
                 **/
                 ["deprecated:Use ITypes#getEnumeration(string, string) instead."]
                idempotent omero::model::IObject getEnumeration(string enumClass, string value) throws ServerError;

                /**
                 * Retrieves the exhaustive list of enumerations for a given
                 * enumeration class.
                 *
                 * @param enumClass Enumeration class.
                 * @return List of all enumeration objects for the
                 *         <i>enumClass</i>.
                 **/
                 ["deprecated:Use ITypes#allEnumerations(string) instead."]
                idempotent IObjectList getAllEnumerations(string enumClass) throws ServerError;

                /**
                 * Copies the metadata, and <b>only</b> the metadata linked to
                 * a Pixels object into a new Pixels object of equal or
                 * differing size across one or many of its three physical
                 * dimensions or temporal dimension.
                 * It is beyond the scope of this method to handle updates or
                 * changes to the raw pixel data available through
                 * {@link omero.api.RawPixelsStore} or to add
                 * and link {@link omero.model.PlaneInfo} and/or other Pixels
                 * set specific metadata.
                 * It is also assumed that the caller wishes the pixels
                 * dimensions and {@link omero.model.PixelsType} to remain the
                 * same; changing these is outside the scope of this method.
                 * <b>NOTE:</b> As {@link omero.model.Channel} objects are
                 * only able to apply to a single set of Pixels any
                 * annotations or linkage to these objects will be lost.
                 *
                 * @param pixelsId The source Pixels set id.
                 * @param sizeX The new size across the X-axis.
                 *              <code>null</code> if the copy should maintain
                 *              the same size.
                 * @param sizeY The new size across the Y-axis.
                 *              <code>null</code> if the copy should maintain
                 *              the same size.
                 * @param sizeZ The new size across the Z-axis.
                 *              <code>null</code> if the copy should maintain
                 *              the same size.
                 * @param sizeT The new number of timepoints.
                 *              <code>null</code> if the copy should maintain
                 *              the same number.
                 * @param channelList The channels that should be copied into
                 *                    the new Pixels set.
                 * @param methodology An optional string signifying the
                 *                    methodology that will be used to produce
                 *                    this new Pixels set.
                 * @param copyStats Whether or not to copy the
                 *                  {@link omero.model.StatsInfo} for each
                 *                  channel.
                 * @return Id of the new Pixels object on success or
                 *         <code>null</code> on failure.
                 * @throws ValidationException If the X, Y, Z, T or
                 *         channelList dimensions are out of bounds or the
                 *         Pixels object corresponding to
                 *         <code>pixelsId</code> is unlocatable.
                 **/
                omero::RLong copyAndResizePixels(long pixelsId,
                                                 omero::RInt sizeX,
                                                 omero::RInt sizeY,
                                                 omero::RInt sizeZ,
                                                 omero::RInt sizeT,
                                                 omero::sys::IntList channelList,
                                                 string methodology,
                                                 bool copyStats) throws ServerError;

                /**
                 * Copies the metadata, and <b>only</b> the metadata linked to
                 * a Image object into a new Image object of equal or
                 * differing size across one or many of its three physical
                 * dimensions or temporal dimension.
                 * It is beyond the scope of this method to handle updates or
                 * changes to  the raw pixel data available through
                 * {@link omero.api.RawPixelsStore} or to add
                 * and link {@link omero.model.PlaneInfo} and/or other Pixels
                 * set specific metadata.
                 * It is also assumed that the caller wishes the pixels
                 * dimensions and {@link omero.model.PixelsType} to remain the
                 * same; changing these is outside the scope of this method.
                 * <b>NOTE:</b> As {@link omero.model.Channel} objects are
                 * only able to apply to a single set of Pixels any
                 * annotations or linkage to these objects will be lost.
                 *
                 * @param imageId The source Image id.
                 * @param sizeX The new size across the X-axis.
                 *              <code>null</code> if the copy should maintain
                 *              the same size.
                 * @param sizeY The new size across the Y-axis.
                 *              <code>null</code> if the copy should maintain
                 *              the same size.
                 * @param sizeZ The new size across the Z-axis.
                 *              <code>null</code> if the copy should maintain
                 *              the same size.
                 * @param sizeT The new number of timepoints.
                 *              <code>null</code> if the copy should maintain
                 *              the same number.
                 * @param channelList The channels that should be copied into
                 *                    the new Pixels set.
                 * @param methodology The name of the new Image.
                 * @param copyStats Whether or not to copy the
                 *                  {@link omero.model.StatsInfo} for each
                 *                  channel.
                 * @return Id of the new Pixels object on success or
                 *         <code>null</code> on failure.
                 * @throws ValidationException If the X, Y, Z, T or
                 *         channelList dimensions are out of bounds or the
                 *         Pixels object corresponding to
                 *         <code>pixelsId</code> is unlocatable.
                 */
                omero::RLong copyAndResizeImage(long imageId,
                                                omero::RInt sizeX,
                                                omero::RInt sizeY,
                                                omero::RInt sizeZ,
                                                omero::RInt sizeT,
                                                omero::sys::IntList channelList,
                                                string methodology,
                                                bool copyStats) throws ServerError;

                /**
                 * Creates the metadata, and <b>only</b> the metadata linked
                 * to an Image object. It is beyond the scope of this method
                 * to handle updates or changes to the raw pixel data
                 * available through {@link omero.api.RawPixelsStore} or to
                 * add and link {@link omero.model.PlaneInfo} or
                 * {@link omero.model.StatsInfo} objects and/or other Pixels
                 * set specific metadata. It is also up to the caller to
                 * update the pixels dimensions.
                 *
                 * @param sizeX The new size across the X-axis.
                 * @param sizeY The new size across the Y-axis.
                 * @param sizeZ The new size across the Z-axis.
                 * @param sizeT The new number of timepoints.
                 * @param pixelsType The pixelsType
                 * @param name The name of the new Image.
                 * @param description The description of the new Image.
                 * @return Id of the new Image object on success or
                 *         <code>null</code> on failure.
                 * @throws ValidationException If the channel list is
                 *         <code>null</code> or of size == 0.
                 **/
                omero::RLong createImage(int sizeX, int sizeY, int sizeZ, int sizeT,
                                         omero::sys::IntList channelList,
                                         omero::model::PixelsType pixelsType,
                                         string name, string description) throws ServerError;

                /**
                 * Sets the channel global (all 2D optical sections
                 * corresponding to a particular channel) minimum and maximum
                 * for a Pixels set.
                 *
                 * @param pixelsId The source Pixels set id.
                 * @param channelIndex The channel index within the Pixels set.
                 * @param min The channel global minimum.
                 * @param max The channel global maximum.
                 **/
                void setChannelGlobalMinMax(long pixelsId, int channelIndex, double min, double max) throws ServerError;
            };
    };
};

#endif
