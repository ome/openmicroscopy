/*
 *   $Id$
 *
 *   Copyright 2010 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services;

import java.awt.Dimension;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.perf4j.StopWatch;
import org.perf4j.commonslog.CommonsLogStopWatch;

import ome.api.IPixels;
import ome.api.IQuery;
import ome.api.IRenderingSettings;
import ome.api.IUpdate;
import ome.conditions.ValidationException;
import ome.model.core.Pixels;
import ome.model.display.RenderingDef;
import ome.model.display.Thumbnail;
import ome.model.internal.Details;
import ome.parameters.Parameters;
import ome.security.SecuritySystem;

/**
 *
 */
public class ThumbnailCtx
{
    /** The default MIME type. */
    public static final String DEFAULT_MIME_TYPE = "image/jpeg";

    private IQuery queryService;

    private IUpdate updateService;

    private IPixels pixelsService;

    private IRenderingSettings settingsService;

    private SecuritySystem securitySystem;

    private long userId;

    private Map<Long, Pixels> pixelsIdPixelsMap =
        new HashMap<Long, Pixels>();

    private Map<Long, RenderingDef> pixelsIdSettingsMap =
        new HashMap<Long, RenderingDef>();

    private Map<Long, Thumbnail> pixelsIdMetadataMap =
        new HashMap<Long, Thumbnail>();
    
    private Map<Long, Timestamp> pixelsIdSettingsLastModifiedTimeMap =
        new HashMap<Long, Timestamp>();

    private Map<Long, Timestamp> pixelsIdMetadataLastModifiedTimeMap =
        new HashMap<Long, Timestamp>();

    private Map<Long, Long> pixelsIdSettingsOwnerIdMap =
        new HashMap<Long, Long>();

    public ThumbnailCtx(IQuery queryService, IUpdate updateService,
            IPixels pixelsService, IRenderingSettings settingsService,
            SecuritySystem securitySystem, long userId)
    {
        this.queryService = queryService;
        this.updateService = updateService;
        this.pixelsService = pixelsService;
        this.settingsService = settingsService;
        this.securitySystem = securitySystem;
        this.userId = userId;
    }

    /**
     * Bulk loads a set of rendering settings for the current group of pixels
     * sets and prepares our internal data structures.
     */
    public void loadAndPrepare(Set<Long> pixelsIds, int longestSide)
    {
        // First populate our hash maps asking for our settings.
        List<RenderingDef> settingsList = bulkLoadRenderingSettings(pixelsIds);
        for (RenderingDef settings : settingsList)
        {
            prepareRenderingSettings(settings, settings.getPixels());
        }

        // Now check to see if we're in a state where missing settings requires
        // us to use the owner's settings (we're "graph critical") and load
        // them if possible.
        Set<Long> pixelsIdsWithoutSettings = 
            getPixelsIdsWithoutSettings(pixelsIds);
        if (securitySystem.isGraphCritical())
        {
            settingsList = 
                bulkLoadOwnerRenderingSettings(pixelsIdsWithoutSettings);
            for (RenderingDef settings : settingsList)
            {
                prepareRenderingSettings(settings, settings.getPixels());
            }
            pixelsIdsWithoutSettings = getPixelsIdsWithoutSettings(pixelsIds);
        }

        // For dimension pooling to work correctly for the purpose of thumbnail
        // metadata creation we now need to load the Pixels sets that had no
        // rendering settings.
        loadMissingPixels(pixelsIdsWithoutSettings);

        // Now we're going to attempt to efficiently retrieve the thumbnail
        // metadata based on our dimension pools above. To save significant
        // time later we're also going to pre-create thumbnail metadata where
        // it is missing.
        Map<Dimension, Set<Long>> dimensionPools = 
            createDimensionPools(longestSide);
        loadMetadataByDimensionPool(dimensionPools);
        createMissingThumbnailMetadata(dimensionPools, pixelsIds, longestSide);
    }

    public void loadAndPrepare(long pixelsId, long settingsId)
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

    public Map<Dimension, Set<Long>> createDimensionPools(int size)
    {
        Map<Dimension, Set<Long>> dimensionPools =
            new HashMap<Dimension, Set<Long>>();
        for (Pixels pixels : pixelsIdPixelsMap.values())
        {
            addToDimensionPool(dimensionPools, pixels, size);
        }
        return dimensionPools;
    }

    private Set<Long> getPixelsIdsWithoutSettings(Set<Long> pixelsIds)
    {
        Set<Long> pixelsIdsWithoutSettings = new HashSet<Long>();
        Set<Long> pixelsIdsWithSettings = pixelsIdPixelsMap.keySet(); 
        for (Long pixelsId : pixelsIds)
        {
            if (!pixelsIdsWithSettings.contains(pixelsId))
            {
                pixelsIdsWithoutSettings.add(pixelsId);
            }
        }
        return pixelsIdsWithoutSettings;
    }

    private Set<Long> getPixelsIdsWithoutMetadata(Set<Long> pixelsIds)
    {
        Set<Long> pixelsIdsWithoutMetadata = new HashSet<Long>();
        Set<Long> pixelsIdsWithMetadata = pixelsIdMetadataMap.keySet(); 
        for (Long pixelsId : pixelsIds)
        {
            if (!pixelsIdsWithMetadata.contains(pixelsId))
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
    private List<RenderingDef> bulkLoadRenderingSettings(Set<Long> pixelsIds)
    {
        StopWatch s1 = new CommonsLogStopWatch(
                "omero.bulkLoadRenderingSettings");
        List<RenderingDef> toReturn = queryService.findAllByQuery(
                "select r from RenderingDef as r join fetch r.pixels " +
                "join fetch r.details.updateEvent " +
                "join fetch r.pixels.details.updateEvent " +
                "where r.details.owner.id = :id and r.pixels.id in (:ids)",
                new Parameters().addId(userId).addIds(pixelsIds));
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
        StopWatch s1 = new CommonsLogStopWatch(
                "omero.bulkLoadOwnerRenderingSettings");
        List<RenderingDef> toReturn = queryService.findAllByQuery(
                "select r from RenderingDef as r join fetch r.pixels " +
                "join fetch r.details.updateEvent " +
                "join fetch r.pixels.details.updateEvent " +
                "where r.details.owner.id = p.details.owner.id " +
                "and r.pixels.id in (:ids)",
                new Parameters().addIds(pixelsIds));
        s1.stop();
        return toReturn;
    }

    private List<Thumbnail> bulkLoadMetadata(Dimension dimensions,
                                             Set<Long> pixelsIds)
    {
        Parameters params = new Parameters();
        params.addInteger("x", (int) dimensions.getWidth());
        params.addInteger("y", (int) dimensions.getHeight());
        params.addLong("o_id", userId);
        params.addIds(pixelsIds);
        StopWatch s1 = new CommonsLogStopWatch("omero.bulkLoadMetadata");
        List<Thumbnail> toReturn = queryService.findAllByQuery(
                "select t from Thumbnail as t " +
                "join t.pixels " +
                "join fetch t.details.updateEvent " +
                "where t.sizeX = :x and t.sizeY = :y " +
                "and t.details.owner.id = :o_id " +
                "and t.pixels.id in (:ids)", params);
        s1.stop();
        return toReturn;
    }

    private List<Thumbnail> bulkLoadOwnerMetadata(Dimension dimensions,
                                                  Set<Long> pixelsIds)
    {
        Parameters params = new Parameters();
        params.addInteger("x", (int) dimensions.getWidth());
        params.addInteger("y", (int) dimensions.getHeight());
        params.addIds(pixelsIds);
        StopWatch s1 = new CommonsLogStopWatch("omero.bulkLoadOwnerMetadata");
        List<Thumbnail> toReturn = queryService.findAllByQuery(
                "select t from Thumbnail as t" +
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
        StopWatch s1 = new CommonsLogStopWatch(
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

            // Now check to see if we're in a state where missing settings
            // requires us to use the owner's settings (we're "graph critical")
            // and load them if possible.
            Set<Long> pixelsIdsWithoutMetadata =
                getPixelsIdsWithoutMetadata(pool);
            thumbnailList = bulkLoadOwnerMetadata(
                    dimensions, pixelsIdsWithoutMetadata); 
            for (Thumbnail metadata : thumbnailList)
            {
                prepareMetadata(metadata, metadata.getPixels().getId());
            }
        }
        s1.stop();
    }

    private void loadMissingPixels(Set<Long> pixelsIdsWithoutSettings)
    {
        if (pixelsIdsWithoutSettings.size() > 0)
        {
            Parameters parameters = new Parameters();
            parameters.addIds(pixelsIdsWithoutSettings);
            List<Pixels> pixelsWithoutSettings = queryService.findAllByQuery(
                    "select p from Pixels as p where id in (:ids)", parameters);
            for (Pixels pixels : pixelsWithoutSettings)
            {
                pixelsIdPixelsMap.put(pixels.getId(), pixels);
            }
        }
    }

    private void prepareRenderingSettings(RenderingDef settings, Pixels pixels)
    {
        Long pixelsId = pixels.getId();
        pixelsIdPixelsMap.put(pixelsId, pixels);
        Details details = settings.getDetails();
        Timestamp timestemp = details.getUpdateEvent().getTime();
        pixelsIdSettingsMap.put(pixelsId, settings);
        pixelsIdSettingsLastModifiedTimeMap.put(pixelsId, timestemp);
        pixelsIdSettingsOwnerIdMap.put(pixelsId, details.getOwner().getId());
    }
    
    private void prepareMetadata(Thumbnail metadata, long pixelsId)
    {
        Timestamp t = metadata.getDetails().getUpdateEvent().getTime();
        pixelsIdMetadataMap.put(pixelsId, metadata);
        pixelsIdMetadataLastModifiedTimeMap.put(pixelsId, t);
    }

    /**
     * Creates metadata for a thumbnail of a given set of pixels set and X-Y
     * dimensions.
     * 
     * @param pixels The Pixels set to create thumbnail metadata for.
     * @param dimensions The dimensions of the thumbnail.
     * 
     * @return the thumbnail metadata as created.
     * @see getThumbnailMetadata()
     */
    private Thumbnail createThumbnailMetadata(Pixels pixels,
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

    private void createMissingThumbnailMetadata(
            Map<Dimension, Set<Long>> dimensionPools, Set<Long> pixelsIds,
            int longestSide)
    {
        StopWatch s1 = new CommonsLogStopWatch(
                "omero.createMissingThumbnailMetadata");
        List<Thumbnail> toSave = new ArrayList<Thumbnail>();
        Map<Dimension, Set<Long>> temporaryDimensionPools = 
            new HashMap<Dimension, Set<Long>>();
        Set<Long> pixelsIdsWithoutMetadata = 
            getPixelsIdsWithoutMetadata(pixelsIds);
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
                            temporaryDimensionPools, pixels, longestSide);
                }
            }
        }
        updateService.saveAndReturnIds(
                toSave.toArray(new Thumbnail[toSave.size()]));
        loadMetadataByDimensionPool(temporaryDimensionPools);
        s1.stop();
    }

    /**
     * Adds the Id of a particular set of Pixels to the correct dimension pool 
     * based on the requested longest side.
     * 
     * @param pools Map of the current dimension pools.
     * @param pixels Pixels set to add to the correct dimension pool.
     * @param size Requested longest side.
     */
    private void addToDimensionPool(Map<Dimension, Set<Long>> pools,
            Pixels pixels, int size)
    {
        // Calculate the XY widths we would use for a thumbnail of Pixels
        Dimension dimensions = calculateXYWidths(pixels, size);

        // If the XY widths already have a pool (an instance that only differs
        // by object reference) find it and use that as our hash key.
        for (Dimension poolDimensions : pools.keySet())
        {
            if (poolDimensions.equals(dimensions))
            {
                dimensions = poolDimensions;
                break;
            }
        }

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
     * Calculates the ratio of the two sides of a Pixel set and returns the
     * X and Y widths based on the longest side maintaining aspect ratio.
     * 
     * @param pixels The Pixels set to calculate against.
     * @param size The size of the longest side of the thumbnail requested.
     * @return The calculated width (X) and height (Y).
     */
    private Dimension calculateXYWidths(Pixels pixels, int size)
    {
        int sizeX = pixels.getSizeX();
        int sizeY = pixels.getSizeY();
        if (sizeX > sizeY)
        {
            float ratio = (float) size / sizeX;
            return new Dimension(size, (int) (sizeY * ratio));
        }
        else
        {
            float ratio = (float) size / sizeY;
            return new Dimension((int) (sizeX * ratio), size);
        }
    }
}
