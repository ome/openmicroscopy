/*
 *   Copyright 2010-2014 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services;

import java.awt.Dimension;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.perf4j.StopWatch;
import org.perf4j.slf4j.Slf4JStopWatch;

import ome.api.IPixels;
import ome.api.IQuery;
import ome.api.IRenderingSettings;
import ome.api.IUpdate;
import ome.conditions.ApiUsageException;
import ome.conditions.ResourceError;
import ome.conditions.ValidationException;
import ome.io.nio.ThumbnailService;
import ome.model.IObject;
import ome.model.core.Image;
import ome.model.core.Pixels;
import ome.model.display.RenderingDef;
import ome.model.display.Thumbnail;
import ome.model.internal.Details;
import ome.model.internal.Permissions;
import ome.parameters.Parameters;
import ome.security.SecuritySystem;
import ome.system.EventContext;

/**
 *
 */
public class ThumbnailCtx
{

    /**
     * Marker exception which is thrown by all methods which wish to tell
     * their callers that for whatever reason the desired Thumbnail is not
     * available, in which case the caller can use a *Direct method instead.
     *
     * See <a href="http://trac.openmicroscopy.org/ome/ticket/10618">ticket:10618</a>
     */
    public static class NoThumbnail extends Exception {

        public NoThumbnail(String message) {
            super(message);
        }

    }

    /** Logger for this class. */
    private static final Logger log = LoggerFactory.getLogger(ThumbnailCtx.class);

    /** Default thumbnail MIME type. */
    public static final String DEFAULT_MIME_TYPE = "image/jpeg";

    /** OMERO query service. */
    private IQuery queryService;

    /** OMERO update service. */
    private IUpdate updateService;

    /** OMERO pixels service. */
    private IPixels pixelsService;

    /** ROMIO thumbnail service. */
    private ThumbnailService thumbnailService;

    /** OMERO rendering settings service. */
    private IRenderingSettings settingsService;

    /** OMERO security system for this session. */
    private SecuritySystem securitySystem;

    /** User ID to use in queries. */
    private long userId;

    /** Pixels ID vs. Pixels object map. */
    private Map<Long, Pixels> pixelsIdPixelsMap =
        new HashMap<Long, Pixels>();

    /**
     * Pixels ID vs. owner ID map. We don't access these RenderingDef object
     * properties directly due to load/unload issues with Hibernate
     * (ObjectUnloadedExceptions) when multiple objects were created with or
     * updated by the same event.
     */
    private Map<Long, Long> pixelsIdOwnerIdMap =
        new HashMap<Long, Long>();

    /** Pixels ID vs. RenderingDef object map. */
    private Map<Long, RenderingDef> pixelsIdSettingsMap =
        new HashMap<Long, RenderingDef>();

    /** Pixels ID vs. Thumbnail object map. */
    private Map<Long, Thumbnail> pixelsIdMetadataMap =
        new HashMap<Long, Thumbnail>();

    /**
     * Pixels ID vs. RenderingDef object last modified time map. We don't access
     * these RenderingDef object properties directly due to load/unload issues
     * with Hibernate (ObjectUnloadedExceptions) when multiple objects were
     * created with or updated by the same event.
     */
    private Map<Long, Timestamp> pixelsIdSettingsLastModifiedTimeMap =
        new HashMap<Long, Timestamp>();

    /**
     * Pixels ID vs. Thumbnail object  last modified time map. We don't access
     * these Thumbnail object properties directly due to load/unload issues
     * with Hibernate (ObjectUnloadedExceptions) when multiple objects were
     * created with or updated by the same event.
     */
    private Map<Long, Timestamp> pixelsIdMetadataLastModifiedTimeMap =
        new HashMap<Long, Timestamp>();

    /**
     * Pixels ID vs. RenderingDef object owner. We don't access these
     * RenderingDef object properties directly due to load/unload issues
     * with Hibernate (ObjectUnloadedExceptions) when multiple objects were
     * created with or updated by the same event.
     */
    private Map<Long, Long> pixelsIdSettingsOwnerIdMap =
        new HashMap<Long, Long>();

    /**
     * Default constructor.
     * @param queryService OMERO query service to use.
     * @param updateService OMERO update service to use.
     * @param pixelsService OMERO pixels service to use.
     * @param settingsService OMERO rendering settings service to use.
     * @param thumbnailService OMERO thumbnail service to use.
     * @param securitySystem OMERO security system for this session.
     * @param userId Current user ID.
     */
    public ThumbnailCtx(IQuery queryService, IUpdate updateService,
            IPixels pixelsService, IRenderingSettings settingsService,
            ThumbnailService thumbnailService, SecuritySystem securitySystem,
            long userId)
    {
        this.queryService = queryService;
        this.updateService = updateService;
        this.pixelsService = pixelsService;
        this.settingsService = settingsService;
        this.thumbnailService = thumbnailService;
        this.securitySystem = securitySystem;
        this.userId = userId;
    }

    /**
     * Retrieves the current user ID to use for queries.
     * @return See above.
     */
    public long getUserId()
    {
        return userId;
    }

    /**
     * Sets the user ID to use for queries.
     * @param userId The user ID to use for queries.
     */
    public void setUserId(long userId)
    {
        this.userId = userId;
    }

    /**
     * Bulk loads a set of rendering settings for a  group of pixels sets and
     * prepares our internal data structures.
     * @param pixelsIds Set of Pixels IDs to prepare rendering settings for.
     */
    public void loadAndPrepareRenderingSettings(Set<Long> pixelsIds)
    {
        loadAndPrepareRenderingSettings(Pixels.class, pixelsIds);
    }

    /**
     * Bulk loads a set of rendering settings for a  group of pixels sets and
     * prepares our internal data structures.
     * @param ids Set of IDs to prepare rendering settings for.
     * @param klass Either <code>Image</code> or <code>Pixels</code> qualifying
     * the type that <code>ids</code> are identifiers for.
     */
    private void loadAndPrepareRenderingSettings(Class<? extends IObject> klass,
                                                 Set<Long> ids)
    {
        // Sanity check our ID set
        if (ids == null || ids.size() == 0)
        {
            log.warn("Preparation of null or zero length ID set requested.");
            return;
        }
        // First we need to load our rendering settings either by Image ID or
        // by Pixels ID.
        List<RenderingDef> settingsList = null;
        Set<Long> pixelsIds = null;
        if (klass.equals(Pixels.class))
        {
            // Populate our hash maps asking for our settings by Pixels ID
            settingsList = bulkLoadRenderingSettingsByPixelsId(ids);
            pixelsIds = ids;
        }
        else if (klass.equals((Image.class)))
        {
            // Populate our hash maps asking for our settings by Image ID
            settingsList = bulkLoadRenderingSettingsByImageId(ids);
            pixelsIds = new HashSet<Long>();
            for (RenderingDef def : settingsList)
            {
                pixelsIds.add(def.getPixels().getId());
            }
        }
        else
        {
            throw new ApiUsageException(
                    "Unexpected preparation source type: " + klass.getName());
        }

        // Now prepare the loaded rendering settings
        for (RenderingDef settings : settingsList)
        {
            prepareRenderingSettings(settings, settings.getPixels());
        }

        // Locate the Pixels sets we have no settings for this user for.
        Set<Long> pixelsIdsWithoutSettings =
            getPixelsIdsWithoutSettings(pixelsIds);

        // For dimension pooling and checks of graph criticality to work
        // correctly for the purpose of thumbnail metadata creation we now need
        // to load the Pixels sets that had no rendering settings.
        loadMissingPixels(pixelsIdsWithoutSettings);

        // Now check to see if we're in a state where missing settings requires
        // us to use the owner's settings (we're "graph critical") and load
        // them if possible.
        if (pixelsIdsWithoutSettings.size() > 0
            && isExtendedGraphCritical(pixelsIdsWithoutSettings))
        {
            settingsList =
                bulkLoadOwnerRenderingSettings(pixelsIdsWithoutSettings);
            for (RenderingDef settings : settingsList)
            {
                prepareRenderingSettings(settings, settings.getPixels());
            }
            pixelsIdsWithoutSettings = getPixelsIdsWithoutSettings(pixelsIds);
        }
    }

    /**
     * Loads and prepares a rendering settings for a Pixels ID and RenderingDef
     * ID.
     * @param pixelsId Pixels ID to load.
     * @param settingsId RenderingDef ID to load an prepare settings for.
     */
    public void loadAndPrepareRenderingSettings(long pixelsId, long settingsId)
    {
        Pixels pixels = pixelsService.retrievePixDescription(pixelsId);
        RenderingDef settings = pixelsService.loadRndSettings(settingsId);
        if (settings == null)
        {
            throw new ValidationException(
                    "No rendering definition exists with ID = " + settingsId);
        }
        if (!settingsService.sanityCheckPixels(pixels, settings.getPixels()))
        {
            throw new ValidationException(
                    "The rendering definition " + settingsId +
                    " is incompatible with pixels set " + pixels.getId());
        }
        prepareRenderingSettings(settings, pixels);
    }

    /**
     * Bulk loads and prepares metadata for a group of pixels sets. Calling
     * this method guarantees that metadata are available, creating them if
     * they are not.
     * @param pixelsIds Pixels IDs to prepare metadata for.
     * @param longestSide The longest side of the thumbnails requested.
     */
    public void loadAndPrepareMetadata(Set<Long> pixelsIds, int longestSide)
    {
        // Now we're going to attempt to efficiently retrieve the thumbnail
        // metadata based on our dimension pools above. To save significant
        // time later we're also going to pre-create thumbnail metadata where
        // it is missing.
        Map<Dimension, Set<Long>> dimensionPools =
            createDimensionPools(longestSide);
        loadMetadataByDimensionPool(dimensionPools);
        createMissingThumbnailMetadata(dimensionPools);
    }

    /**
     * Bulk loads and prepares metadata for a group of pixels sets. Calling
     * this method guarantees that metadata are available, creating them if
     * they are not.
     * @param pixelsIds Pixels IDs to prepare metadata for.
     * @param dimensions X-Y dimensions of the thumbnails requested.
     */
    public void loadAndPrepareMetadata(Set<Long> pixelsIds,
                                       Dimension dimensions)
    {
        loadAndPrepareMetadata(pixelsIds, dimensions, true);
    }

    /**
     * Bulk loads and prepares metadata for a group of pixels sets. Calling
     * this method guarantees that metadata are available, creating them if
     * they are not.
     * @param pixelsIds Pixels IDs to prepare metadata for.
     * @param dimensions X-Y dimensions of the thumbnails requested.
     */
    public void loadAndPrepareMetadata(Set<Long> pixelsIds,
                                       Dimension dimensions,
                                       boolean createMissing)
    {
        // Now we're going to attempt to efficiently retrieve the thumbnail
        // metadata based on our dimension pools above. To save significant
        // time later we're also going to pre-create thumbnail metadata where
        // it is missing.
        Map<Dimension, Set<Long>> dimensionPools =
            new HashMap<Dimension, Set<Long>>();
        dimensionPools.put(dimensions, pixelsIds);
        loadMetadataByDimensionPool(dimensionPools);
        if (createMissing)
        {
                createMissingThumbnailMetadata(dimensionPools);
        }
    }

    /**
     * Retrieves all thumbnail metadata available in the database for a given
     * Pixels ID.
     * @param pixelsId Pixels ID to retrieve thumbnail metadata for.
     * @return See above.
     */
    public List<Thumbnail> loadAllMetadata(long pixelsId)
    {
        Parameters params = new Parameters();
        params.addId(pixelsId);
        params.addLong("o_id", userId);
        StopWatch s1 = new Slf4JStopWatch("omero.loadAllMetadata");
        List<Thumbnail> toReturn = queryService.findAllByQuery(
                "select t from Thumbnail as t " +
                "join t.pixels p " +
                "join fetch t.details.updateEvent " +
                "where t.details.owner.id = :o_id " +
                "and p.id = :id", params);
        if (toReturn.isEmpty()) {
            toReturn = queryService.findAllByQuery(
                    "select t from Thumbnail as t " +
                    "join t.pixels p " +
                    "join fetch t.details.updateEvent " +
                    "where t.details.owner.id = p.details.owner.id " +
                    "and p.id = :id", params);
        }
        s1.stop();
        return toReturn;
    }

    /**
     * Resets a given set of Pixels rendering settings to the default
     * effectively creating any which do not exist.
     * @param pixelsIds Pixels IDs
     */
    public void createAndPrepareMissingRenderingSettings(Set<Long> pixelsIds)
    {
        // Now check to see if we're in a state where missing rendering
        // settings and our state requires us to not save.
        if (isExtendedGraphCritical(pixelsIds))
        {
            // TODO: Could possibly "su" to the user and create a thumbnail
            return;
        }
        StopWatch s1 = new Slf4JStopWatch(
                "omero.createAndPrepareMissingRenderingSettings");
        Set<Long> pixelsIdsWithoutSettings =
            getPixelsIdsWithoutSettings(pixelsIds);
        int count = pixelsIdsWithoutSettings.size();
        if (count > 0)
        {
            log.info(count + " pixels without settings");
            Set<Long> imageIds = settingsService.resetDefaultsInSet(
                    Pixels.class, pixelsIdsWithoutSettings);
            if (count != imageIds.size())
            {
                log.warn(String.format(
                        "Return value ID count %d does not match pixels " +
                        "without settings count %d", imageIds.size(), count));
            }
            loadAndPrepareRenderingSettings(Image.class, imageIds);
        }
        s1.stop();
    }

    /**
     * Whether or not settings are available for a given Pixels ID.
     * @param pixelsId Pixels ID to check for availability.
     * @return <code>true</code> if settings are available and
     * <code>false</code> otherwise.
     */
    public boolean hasSettings(long pixelsId)
    {
        return pixelsIdSettingsMap.containsKey(pixelsId);
    }

    /**
     * Whether or not thumbnail metadata is available for a given Pixels ID.
     * @param pixelsId Pixels ID to check for availability.
     * @return <code>true</code> if metadata is available and
     * <code>false</code> otherwise.
     */
    public boolean hasMetadata(long pixelsId)
    {
        return pixelsIdMetadataMap.containsKey(pixelsId);
    }

    /**
     * Retrieves the Pixels object for a given Pixels ID.
     * @param pixelsId Pixels ID to retrieve the Pixels object for.
     * @return See above.
     */
    public Pixels getPixels(long pixelsId)
    {
        Pixels pixels = pixelsIdPixelsMap.get(pixelsId);
        if (pixels == null)
        {
            throw new ResourceError(String.format(
                    "Error retrieving Pixels id:%d. Pixels set does not " +
                    "exist or the user id:%d has insufficient permissions " +
                    "to retrieve it.", pixelsId, userId));
        }
        return pixelsIdPixelsMap.get(pixelsId);
    }

    /**
     * Retrieves the RenderingDef object for a given Pixels ID.
     * @param pixelsId Pixels ID to retrieve the RenderingDef object for.
     * @return See above.
     */
    public RenderingDef getSettings(long pixelsId)
    {
        return pixelsIdSettingsMap.get(pixelsId);
    }

    /**
     * Retrieves the Thumbnail object for a given Pixels ID.
     * @param pixelsId Pixels ID to retrieve the Thumbnail object for.
     * @return See above.
     */
    public Thumbnail getMetadata(long pixelsId) throws NoThumbnail
    {
        Thumbnail thumbnail = pixelsIdMetadataMap.get(pixelsId);
        if (thumbnail == null && securitySystem.isGraphCritical(null)) // maythrow
        {
            Pixels pixels = pixelsIdPixelsMap.get(pixelsId);
            long ownerId = pixels.getDetails().getOwner().getId();
            throw new ResourceError(String.format(
                    "The user id:%s may not be the owner id:%d. The owner " +
                    "has not viewed the Pixels set id:%d and thumbnail " +
                    "metadata is missing.", userId, ownerId, pixelsId));
        }
        else if (thumbnail == null)
        {
            throw new NoThumbnail(
                    "Fatal error retrieving thumbnail metadata for Pixels " +
                    "set id:" + pixelsId);
        }
        return thumbnail;
    }

    /**
     * Whether or not the thumbnail metadata for a given Pixels ID is dirty
     * (the RenderingDef has been updated since the Thumbnail was).
     * @param pixelsId Pixels ID to check for dirty metadata.
     * @return <code>true</code> if the metadata is dirty <code>false</code>
     * otherwise.
     */
    public boolean dirtyMetadata(long pixelsId)
    {
        Timestamp metadataLastUpdated =
            pixelsIdMetadataLastModifiedTimeMap.get(pixelsId);
        Timestamp settingsLastUpdated =
            pixelsIdSettingsLastModifiedTimeMap.get(pixelsId);
        if (log.isDebugEnabled())
        {
            log.debug("Thumb time: " + metadataLastUpdated);
            log.debug("Settings time: " + settingsLastUpdated);
        }
        if (metadataLastUpdated == null) {
           return true;
        }
        return settingsLastUpdated.after(metadataLastUpdated);
    }

    /**
     * Checks to see if a thumbnail is in the on disk cache or not.
     *
     * @param pixelsId The Pixels set the thumbnail is for.
     * @return Whether or not the thumbnail is in the on disk cache.
     */
    public boolean isThumbnailCached(long pixelsId)
    {
        Thumbnail metadata = pixelsIdMetadataMap.get(pixelsId);
        if (metadata == null) {
            return false;
        }
        try
        {
            boolean dirtyMetadata = dirtyMetadata(pixelsId);
            boolean thumbnailExists =
                thumbnailService.getThumbnailExists(metadata);
            boolean isExtendedGraphCritical =
                isExtendedGraphCritical(Collections.singleton(pixelsId));
            Long metadataOwnerId = metadata.getDetails().getOwner().getId();
            Long sessionUserId = securitySystem.getEffectiveUID();
            boolean isMyMetadata = sessionUserId.equals(metadataOwnerId);
            if (!dirtyMetadata)
            {
                if (thumbnailExists)
                {
                    return true;
                }
                else if (!thumbnailExists && isExtendedGraphCritical)
                {
                    throw new ResourceError(String.format(
                            "Error retrieving Pixels id:%d. Thumbnail " +
                            "metadata exists but a thumbnail is not " +
                            "available in the cache. User id:%d has " +
                            "insufficient permissions to create it.",
                            pixelsId, userId));
                }
            }
            else if (thumbnailExists && !isMyMetadata)
            {
                //we need thumbnail for new settings. User creating his own
                if (sessionUserId == userId && userId != metadataOwnerId) {
                    return false;
                }
                //session user updating someone else thumbnail if allowed
                if (userId == metadataOwnerId && sessionUserId != userId) {
                    return false;
                }
                log.warn(String.format(
                        "Thumbnail metadata is dirty for Pixels Id:%d and " +
                        "the metadata is owned User id:%d which is not " +
                        "User id:%d. Ignoring this and returning the cached " +
                        "thumbnail.", pixelsId, metadataOwnerId, userId));
                return true;
            }
            else if (thumbnailExists && isExtendedGraphCritical)
            {
                if (dirtyMetadata && userId == metadataOwnerId) {
                    return false;
                 }
                log.warn(String.format(
                        "Thumbnail metadata is dirty for Pixels Id:%d and " +
                        "graph is critical for User id:%d. Ignoring this " +
                        "and returning the cached thumbnail.owner %d dirty %d",
                        pixelsId, userId, metadataOwnerId, dirtyMetadata));
                return true;
            }
        }
        catch (IOException e)
        {
            String s = "Could not check if thumbnail is cached: ";
            log.error(s, e);
            throw new ResourceError(s + e.getMessage());
        }
        return false;
    }

    /**
     * Calculates the ratio of the two sides of a Pixel set and returns the
     * X and Y widths based on the longest side maintaining aspect ratio.
     *
     * @param pixels The Pixels set to calculate against.
     * @param longestSide The size of the longest side of the thumbnail
     * requested.
     * @return The calculated width (X) and height (Y).
     */
    public Dimension calculateXYWidths(Pixels pixels, int longestSide)
    {
        int sizeX = pixels.getSizeX();
        int sizeY = pixels.getSizeY();
        if (sizeX > sizeY)
        {
            float ratio = (float) longestSide / sizeX;
            return new Dimension(longestSide, (int) (sizeY * ratio));
        }
        float ratio = (float) longestSide / sizeY;
        return new Dimension((int) (sizeX * ratio), longestSide);
    }

    /**
     * Whether or not we're extended graph critical for a given set of
     * dimension pools. We're extended graph critical if:
     * <ul>
     *   <li>
     *      <code>isGraphGritical() == true</code> and the Pixels set does not
     *      belong to us.
     *   </li>
     *   <li>
     *      <code>isGraphCritical() == false</code>, the Pixels set does not
     *      belong to us and the group is READ-ONLY.
     *   </li>
     * </ul>
     * @param dimensionPools Dimension pools to check if we're graph critical
     * for.
     * @return <code>true</code> if we're graph critical, and
     * <code>false</code> otherwise.
     * @see #isExtendedGraphCritical(Set)
     */
    private boolean isExtendedGraphCritical(
            Map<Dimension, Set<Long>> dimensionPools)
    {
        for (Set<Long> pool : dimensionPools.values())
        {
            if (isExtendedGraphCritical(pool))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Whether or not we're extended graph critical for a given set of
     * dimension pools. We're extended graph critical if:
     * <ul>
     *   <li>
     *      <code>isGraphGritical() == true</code> and the Pixels set does not
     *      belong to us.
     *   </li>
     *   <li>
     *      <code>isGraphCritical() == false</code>, the Pixels set does not
     *      belong to us and the group is READ-ONLY.
     *   </li>
     * </ul>
     * @param pixelsIds Set of Pixels to check if we're graph critical for.
     * @return <code>true</code> if we're graph critical, and
     * <code>false</code> otherwise.
     */
    public boolean isExtendedGraphCritical(Set<Long> pixelsIds)
    {
        EventContext ec = securitySystem.getEventContext();
        Permissions currentGroupPermissions = ec.getCurrentGroupPermissions();
        Permissions readOnly = Permissions.parseString("rwr---");

        if (ec.getCurrentShareId() != null)
        {
            return true;
        }
        if (securitySystem.isGraphCritical(null) // May throw
            || currentGroupPermissions.identical(readOnly))
        {
            for (Long pixelsId : pixelsIds)
            {
                // Check if the Pixels ID vs. Owner ID map is missing, which
                // signifies that we were completely unable to load any of the
                // Pixels sets identified in pixelsIds.
                if (pixelsIdOwnerIdMap == null) {
                    throw new ResourceError(String.format(
                            "Error retrieving Pixels id:%d. Pixels set does " +
                            "not exist or the user id:%d has insufficient " +
                            "permissions to retrieve it.", pixelsId, userId));
                }
                Long pixelsOwner = pixelsIdOwnerIdMap.get(pixelsId);
                // Check if the Owner ID is missing from the map, which as
                // above signifies that we unable to load this particular
                // Pixels set as identified by Pixels ID. This will be a hard
                // failure due to the crazy state that this potentially
                // suggests.
                if (pixelsOwner == null) {
                    throw new ResourceError(String.format(
                            "Error retrieving Pixels id:%d. Pixels set does " +
                            "not exist or the user id:%d has insufficient " +
                            "permissions to retrieve it.", pixelsId, userId));
                }
                if (pixelsOwner != userId &&
                        !(ec.getLeaderOfGroupsList().contains(userId) ||
                                ec.isCurrentUserAdmin()))
                {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Creates X-Y dimension pools based on a requested longest side.
     * @param longestSide Requested longest side of the thumbnail.
     * @return Map of X-Y dimension vs. Pixels ID (a set of dimension pools).
     */
    private Map<Dimension, Set<Long>> createDimensionPools(int longestSide)
    {
        Map<Dimension, Set<Long>> dimensionPools =
            new HashMap<Dimension, Set<Long>>();
        for (Pixels pixels : pixelsIdPixelsMap.values())
        {
            // Calculate the XY widths we would use for a thumbnail of Pixels
            Dimension dimensions = calculateXYWidths(pixels, longestSide);
            addToDimensionPool(dimensionPools, pixels, dimensions);
        }
        return dimensionPools;
    }

    /**
     * Adds the Id of a particular set of Pixels to the correct dimension pool
     * based on the requested longest side.
     *
     * @param pools Map of the current dimension pools.
     * @param pixels Pixels set to add to the correct dimension pool.
     * @param dimensions Dimensions pool to add to.
     */
    private void addToDimensionPool(Map<Dimension, Set<Long>> pools,
            Pixels pixels, Dimension dimensions)
    {
        // Insert the Pixels set into the dimension pool
        Set<Long> pool = pools.get(dimensions);
        if (pool == null)
        {
            pool = new HashSet<Long>();
        }
        pool.add(pixels.getId());
        pools.put(dimensions, pool);
    }

    /**
     * Examines the currently prepared data structures for Pixels IDs without
     * settings.
     * @param pixelsIds Pixels IDs to check.
     * @return Set of Pixels IDs which do not have settings prepared.
     */
    private Set<Long> getPixelsIdsWithoutSettings(Set<Long> pixelsIds)
    {
        Set<Long> pixelsIdsWithoutSettings = new HashSet<Long>();
        for (Long pixelsId : pixelsIds)
        {
            if (!hasSettings(pixelsId))
            {
                pixelsIdsWithoutSettings.add(pixelsId);
            }
        }
        return pixelsIdsWithoutSettings;
    }

    /**
     * Examines the currently prepared data structures for Pixels IDs without
     * thumbnail metadata.
     * @param pixelsIds Pixels IDs to check.
     * @return Set of Pixels IDs which do not have thumbnail metadata prepared.
     */
    private Set<Long> getPixelsIdsWithoutMetadata(Set<Long> pixelsIds)
    {
        Set<Long> pixelsIdsWithoutMetadata = new HashSet<Long>();
        for (Long pixelsId : pixelsIds)
        {
            if (!hasMetadata(pixelsId))
            {
                pixelsIdsWithoutMetadata.add(pixelsId);
            }
        }
        return pixelsIdsWithoutMetadata;
    }

    /**
     * Bulk loads a set of rendering sets for a group of pixels sets.
     * @param pixelsIds the Pixels sets to retrieve thumbnails for.
     * @return Loaded rendering settings for <code>pixelsIds</code>.
     */
    private List<RenderingDef> bulkLoadRenderingSettingsByPixelsId(
            Set<Long> pixelsIds)
    {
        StopWatch s1 = new Slf4JStopWatch(
                "omero.bulkLoadRenderingSettings");
        List<RenderingDef> toReturn = queryService.findAllByQuery(
                "select r from RenderingDef as r " +
                "join fetch r.pixels as p " +
                "join fetch r.details.updateEvent " +
                "join p.details.updateEvent " +
                "where r.details.owner.id = :id and r.pixels.id in (:ids) " +
                "order by r.details.updateEvent.time asc",
                new Parameters().addId(userId).addIds(pixelsIds));
        if (toReturn.isEmpty()) {
            toReturn = queryService.findAllByQuery(
                    "select r from RenderingDef as r " +
                    "join fetch r.pixels as p " +
                    "join fetch r.details.updateEvent " +
                    "join p.details.updateEvent " +
                    "where r.details.owner.id = p.details.owner.id " +
                    "and r.pixels.id in (:ids) " +
                    "order by r.details.updateEvent.time asc",
                    new Parameters().addId(userId).addIds(pixelsIds));
        }
        s1.stop();
        return toReturn;
    }

    /**
     * Bulk loads a set of rendering sets for a group of Images.
     * @param imageIds the Images retrieve thumbnails for.
     * @return Loaded rendering settings for <code>imageIds</code>.
     */
    private List<RenderingDef> bulkLoadRenderingSettingsByImageId(
            Set<Long> imageIds)
    {
        StopWatch s1 = new Slf4JStopWatch(
                "omero.bulkLoadRenderingSettings");
        List<RenderingDef> toReturn = queryService.findAllByQuery(
                "select r from RenderingDef as r " +
                "join fetch r.pixels as p " +
                "join fetch r.details.updateEvent " +
                "join fetch p.details.updateEvent " +
                "where r.details.owner.id = :id " +
                "and r.pixels.image.id in (:ids) " +
                "order by r.details.updateEvent.time asc",
                new Parameters().addId(userId).addIds(imageIds));
        if (toReturn.isEmpty()) {
            toReturn = queryService.findAllByQuery(
                    "select r from RenderingDef as r " +
                    "join fetch r.pixels as p " +
                    "join fetch r.details.updateEvent " +
                    "join fetch p.details.updateEvent " +
                    "where r.details.owner.id = p.details.owner.id " +
                    "and r.pixels.image.id in (:ids) " +
                    "order by r.details.updateEvent.time asc",
                    new Parameters().addId(userId).addIds(imageIds));
        }
        s1.stop();
        return toReturn;
    }

    /**
     * Bulk loads a set of rendering sets for a group of pixels sets.
     * @param pixelsIds the Pixels sets to retrieve thumbnails for.
     * @return Loaded rendering settings for <code>pixelsIds</code>.
     */
    private List<RenderingDef> bulkLoadOwnerRenderingSettings(
            Set<Long> pixelsIds)
    {
        StopWatch s1 = new Slf4JStopWatch(
                "omero.bulkLoadOwnerRenderingSettings");
        // Why doesn't this first try by userId?
        List<RenderingDef> toReturn = queryService.findAllByQuery(
                "select r from RenderingDef as r " +
                "join fetch r.pixels as p " +
                "join fetch r.details.updateEvent " +
                "join fetch p.details.updateEvent " +
                "where r.details.owner.id = p.details.owner.id " +
                "and r.pixels.id in (:ids) " +
                "order by r.details.updateEvent.time asc",
                new Parameters().addIds(pixelsIds));
        s1.stop();
        return toReturn;
    }

    /**
     * Bulk loads thumbnail metadata.
     * @param dimensions X-Y dimensions to bulk load metdata for.
     * @param pixelsIds Pixels IDs to bulk load metadata for.
     * @return List of thumbnail objects with <code>thumbnail.pixels</code> and
     * <code>thumbnail.details.updateEvent</code> loaded.
     */
    private List<Thumbnail> bulkLoadMetadata(Dimension dimensions,
                                             Set<Long> pixelsIds)
    {
        Parameters params = new Parameters();
        params.addInteger("x", (int) dimensions.getWidth());
        params.addInteger("y", (int) dimensions.getHeight());
        params.addLong("o_id", userId);
        params.addIds(pixelsIds);
        StopWatch s1 = new Slf4JStopWatch("omero.bulkLoadMetadata");
        List<Thumbnail> toReturn = queryService.findAllByQuery(
                "select t from Thumbnail as t " +
                "join t.pixels p " +
                "join fetch t.details.updateEvent " +
                "where t.sizeX = :x and t.sizeY = :y " +
                "and t.details.owner.id = :o_id " +
                "and p.id in (:ids)", params);
        if (toReturn.isEmpty()) {
            toReturn = queryService.findAllByQuery(
                    "select t from Thumbnail as t " +
                    "join t.pixels p " +
                    "join fetch t.details.updateEvent " +
                    "where t.sizeX = :x and t.sizeY = :y " +
                    "and t.details.owner.id = p.details.owner.id " +
                    "and p.id in (:ids)", params);

        }
        s1.stop();
        return toReturn;
    }

    /**
     * Bulk loads thumbnail metadata that is owned by the owner of the Pixels
     * set..
     * @param dimensions X-Y dimensions to bulk load metadata for.
     * @param pixelsIds Pixels IDs to bulk load metadata for.
     * @return List of thumbnail objects with <code>thumbnail.pixels</code> and
     * <code>thumbnail.details.updateEvent</code> loaded.
     */
    private List<Thumbnail> bulkLoadOwnerMetadata(Dimension dimensions,
                                                  Set<Long> pixelsIds)
    {
        Parameters params = new Parameters();
        params.addInteger("x", (int) dimensions.getWidth());
        params.addInteger("y", (int) dimensions.getHeight());
        params.addIds(pixelsIds);
        StopWatch s1 = new Slf4JStopWatch("omero.bulkLoadOwnerMetadata");
        // Why is does this not try userId first?
        List<Thumbnail> toReturn = queryService.findAllByQuery(
                "select t from Thumbnail as t " +
                "join t.pixels as p " +
                "join fetch t.details.updateEvent " +
                "where t.sizeX = :x and t.sizeY = :y " +
                "and t.details.owner.id = p.details.owner.id " +
                "and t.pixels.id in (:ids)", params);
        s1.stop();
        return toReturn;
    }

    /**
     * Attempts to efficiently retrieve the thumbnail metadata based on a set
     * of dimension pools. At worst, the result of maintaining the aspect ratio
     * (calculating the new XY widths) is that we have to retrieve each
     * thumbnail object separately.
     * @param dimensionPools Dimension pools to query based upon.
     * @param metadataMap Dictionary of Pixels ID vs. thumbnail metadata. Will
     * be updated by this method.
     * @param metadataTimeMap Dictionary of Pixels ID vs. thumbnail metadata
     * last modification time. Will be updated by this method.
     */
    private void loadMetadataByDimensionPool(
            Map<Dimension, Set<Long>> dimensionPools)
    {
        StopWatch s1 = new Slf4JStopWatch(
                "omero.loadMetadataByDimensionPool");
        for (Dimension dimensions : dimensionPools.keySet())
        {
            Set<Long> pool = dimensionPools.get(dimensions);
            // First populate our hash maps asking for our metadata.
            List<Thumbnail> thumbnailList = bulkLoadMetadata(dimensions, pool);
            for (Thumbnail metadata : thumbnailList)
            {
                prepareMetadata(metadata, metadata.getPixels().getId());
            }

            // Now check to see if we're in a state where missing metadata
            // requires us to use the owner's metadata (we're "graph critical")
            // and load them if possible.
            Set<Long> pixelsIdsWithoutMetadata =
                getPixelsIdsWithoutMetadata(pool);
            if (pixelsIdsWithoutMetadata.size() > 0
                && isExtendedGraphCritical(pixelsIdsWithoutMetadata))
            {
                thumbnailList = bulkLoadOwnerMetadata(
                        dimensions, pixelsIdsWithoutMetadata);
                for (Thumbnail metadata : thumbnailList)
                {
                    prepareMetadata(metadata, metadata.getPixels().getId());
                }
            }
        }
        s1.stop();
    }

    /**
     * Loads and prepares missing Pixels sets.
     * @param pixelsIds Pixels IDs to load missing Pixels objects for.
     */
    private void loadMissingPixels(Set<Long> pixelsIds)
    {
        if (pixelsIds.size() > 0)
        {
            Parameters parameters = new Parameters();
            parameters.addIds(pixelsIds);
            if (log.isDebugEnabled())
            {
                log.debug("Loading " + pixelsIds.size() + " missing Pixels.");
            }
            StopWatch s1 = new Slf4JStopWatch(
                    "omero.loadMissingPixels");
            List<Pixels> pixelsWithoutSettings = queryService.findAllByQuery(
                    "select p from Pixels as p where id in (:ids)", parameters);
            s1.stop();
            for (Pixels pixels : pixelsWithoutSettings)
            {
                Long pixelsId = pixels.getId();
                pixelsIdPixelsMap.put(pixelsId, pixels);
                pixelsIdOwnerIdMap.put(
                        pixelsId, pixels.getDetails().getOwner().getId());
            }
        }
    }

    /**
     * Prepares a set of rendering settings, extracting relevant metadata and
     * preparing the internal maps.
     * @param settings RenderingDef object to prepare.
     * @param pixels Pixels object to prepare.
     */
    private void prepareRenderingSettings(RenderingDef settings, Pixels pixels)
    {
        Long pixelsId = pixels.getId();
        pixelsIdPixelsMap.put(pixelsId, pixels);
        pixelsIdOwnerIdMap.put(pixelsId, pixels.getDetails().getOwner().getId());
        Details details = settings.getDetails();
        Timestamp timestemp = details.getUpdateEvent().getTime();
        pixelsIdSettingsMap.put(pixelsId, settings);
        pixelsIdSettingsLastModifiedTimeMap.put(pixelsId, timestemp);
        pixelsIdSettingsOwnerIdMap.put(pixelsId, details.getOwner().getId());
    }

    /**
     * Prepares thumbnail metadata extracting relevant metadata and prepares
     * the internal maps.
     * @param metadata Thumbnail object to prepare.
     * @param pixelsId Pixels ID to prepare.
     */
    private void prepareMetadata(Thumbnail metadata, long pixelsId)
    {
        Timestamp t = metadata.getDetails().getUpdateEvent().getTime();
        pixelsIdMetadataMap.put(pixelsId, metadata);
        pixelsIdMetadataLastModifiedTimeMap.put(pixelsId, t);
    }

    /**
     * Creates missing thumbnail metadata for a set of Pixels IDs that have
     * been prepared.
     * @param dimensionPools Dimension pools to retrieve pre-calculated,
     * requested dimensions from.
     */
    private void createMissingThumbnailMetadata(
            Map<Dimension, Set<Long>> dimensionPools)
    {
        // Now check to see if we're in a state where missing metadata
        // and our state requires us to not save.
        if (isExtendedGraphCritical(dimensionPools))
        {
            // TODO: Could possibly "su" to the user and create a thumbnail
            return;
        }
        StopWatch s1 = new Slf4JStopWatch(
                "omero.createMissingThumbnailMetadata");
        List<Thumbnail> toSave = new ArrayList<Thumbnail>();
        Map<Dimension, Set<Long>> temporaryDimensionPools =
            new HashMap<Dimension, Set<Long>>();
        Set<Long> pixelsIdsWithoutMetadata =
            getPixelsIdsWithoutMetadata(pixelsIdPixelsMap.keySet());
        for (Long pixelsId : pixelsIdsWithoutMetadata)
        {
            Pixels pixels = pixelsIdPixelsMap.get(pixelsId);
            for (Dimension dimension : dimensionPools.keySet())
            {
                Set<Long> pool = dimensionPools.get(dimension);
                if (pool.contains(pixelsId))
                {
                    toSave.add(createThumbnailMetadata(pixels, dimension));
                    addToDimensionPool(
                            temporaryDimensionPools, pixels, dimension);
                    break;
                }
            }
        }
        log.info("New thumbnail object set size: " + toSave.size());
        log.info("Dimension pool size: " + temporaryDimensionPools.size());
        if (toSave.size() > 0)
        {
            updateService.saveAndReturnIds(
                    toSave.toArray(new Thumbnail[toSave.size()]));
            loadMetadataByDimensionPool(temporaryDimensionPools);
        }
        s1.stop();
    }

    /**
     * Creates metadata for a thumbnail of a given set of pixels set and X-Y
     * dimensions.
     *
     * @param pixels The Pixels set to create thumbnail metadata for.
     * @param dimensions The dimensions of the thumbnail.
     *
     * @return the thumbnail metadata as created.
     * @see #getMetadata(long)
     */
    public Thumbnail createThumbnailMetadata(Pixels pixels,
            Dimension dimensions)
    {
        // Unload the pixels object to avoid transactional headaches
        Pixels unloadedPixels = new Pixels(pixels.getId(), false);
        Thumbnail thumb = new Thumbnail();
        thumb.setPixels(unloadedPixels);
        thumb.setMimeType(DEFAULT_MIME_TYPE);
        thumb.setSizeX((int) dimensions.getWidth());
        thumb.setSizeY((int) dimensions.getHeight());
        return thumb;
    }
}
