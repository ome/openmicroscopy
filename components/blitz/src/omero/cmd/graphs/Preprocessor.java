/*
 * Copyright (C) 2013 Glencoe Software, Inc. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package omero.cmd.graphs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.HashMultimap;

import omero.cmd.Chgrp;
import omero.cmd.Delete;
import omero.cmd.DoAll;
import omero.cmd.GraphModify;
import omero.cmd.Helper;
import omero.cmd.Request;

/**
 * Preprocessors have a chance to modify the list of
 * {@link Request} instances which are passed to a
 * {@link DoAll}. If this strategy is continued, this
 * should be refactored behind a discoverable interface.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 5.0
 */
public class Preprocessor {

    private final List<Request> requests;

    private final Helper helper;

    /**
     * Cache of filesets which have been looked up.
     */
    protected final HashMultimap<Long, Long> filesetIdToImageIds;

    /**
     * Cache of Images which have been found during the lookups.
     */
    protected final Map<Long, Long> imageIdToFilesetId;

    public Preprocessor(List<Request> requests, Helper helper) {

        this.requests = requests;
        this.helper = helper;

        if (this.requests.size() == 0) {
            filesetIdToImageIds = null;
            imageIdToFilesetId = null;
            return; // EARLY EXIT
        }

        filesetIdToImageIds = HashMultimap.create();
        imageIdToFilesetId = new HashMap<Long, Long>();

        process();
    }

    /**
     * Returns the size of the Image id cache or -1 if null.
     * @return
     */
    public long getImageCount() {
        if (imageIdToFilesetId == null) {
            return -1;
        }
        return imageIdToFilesetId.size();
    }

    /**
     * Returns the size of the Fileset id cache or -1 if null.
     */
    public long getFilesetCount() {
        if (filesetIdToImageIds == null) {
            return -1;
        }
        return filesetIdToImageIds.keySet().size();
    }

    /**
     * Returns a copy of the requests field or an empty list if null.
     */
    public List<Request> getRequests() {
        if (requests == null) {
            return new ArrayList<Request>();
        }
        return new ArrayList<Request>(requests);
    }

    @SuppressWarnings("unchecked")
    protected void process() {
        for (Class<? extends GraphModify> op : new Class[]{Chgrp.class, Delete.class}) {

            // Targets for possible optimization.
            final Set<Request> targets = new HashSet<Request>();

            // Image IDs which are present in a request as opposed to queried.
            final Set<Long> requestedImageIds = new HashSet<Long>();

            // 1. Lookup all filesets for the given images.
            for (Request request : requests) {
                if (op.isAssignableFrom(request.getClass())) {
                    GraphModify gm = (GraphModify) request;
                    String type = gm.type;
                    long id = gm.id;
                    if ("/Image".equals(type)) {
                        targets.add(request); // Properly filtered.
                        requestedImageIds.add(id);
                        lookupFilesetForImage(id, imageIdToFilesetId, filesetIdToImageIds);
                    }
                }
            }

            // 2. For those filesets which have all their images selected,
            // reduce the entries found by inserting the fileset operation
            // at the location of the last contained image.
            Map<Long, Collection<Long>> asMap = filesetIdToImageIds.asMap();
            for (Map.Entry<Long, Collection<Long>> entry : asMap.entrySet()) {
                final Long filesetId = entry.getKey();
                final Collection<Long> imageIds = entry.getValue();

                if (imageIds.size() < 2) {
                    helper.debug("Skipping on Image count " + imageIds.size());
                    continue; // SKIP
                }

                Request lastRequest = null;
                int lastIndex = -1;
                int removed = 0;
                for (int i = requests.size() - 1; i >= 0; i--) {

                    // Check that this is one of our candidate requests
                    Request request = requests.get(i);
                    if (!(targets.contains(request))) {
                        continue; // SKIP. Must match previous filter.
                    }

                    // If so, it's a GraphModify in which case we find its id.
                    long imageId = ((GraphModify) request).id;
                    if (imageIds.contains(imageId) &&
                            requestedImageIds.containsAll(imageIds)) {
                        Request popped = requests.remove(i);
                        removed++;
                        if (lastRequest == null) {
                            lastRequest = popped;
                            lastIndex = i;
                        }
                    }
                }

                if (lastIndex >= 0) {
                    // FIXME: this does not look into modifying the options
                    // set by the user and in general this is likely handled
                    // better by clients.
                    GraphModify gm = ((GraphModify) lastRequest);
                    gm.type = "/Fileset";
                    gm.id = filesetId;
                    requests.add(lastIndex-removed+1, lastRequest);
                }
            }
        }
    }

    private void lookupFilesetForImage(long imageId1,
            Map<Long, Long> imageIdToFilesetId,
            HashMultimap<Long, Long> filesetIdToImageIds) {

        if (imageIdToFilesetId.containsKey(imageId1)) {
            return; // EARLY EXIT
        }

        helper.debug("Loading fileset for Image: " + imageId1);
        List<Object[]> rv =
        helper.getServiceFactory().getQueryService().projection(
                "select i.fileset.id, i2.id from Image i, Image i2 " +
                "where i.fileset.id = i2.fileset.id and i.id = " + imageId1, null);

        if (rv.size() <= 1) {
           // Only perform optimization for multi-image filesets (MIF)
            return; // EARLY EXIT
        }

        for (Object[] ids : rv) {
            Long filesetId = (Long) ids[0];
            Long imageId2 = (Long) ids[1];
            filesetIdToImageIds.put(filesetId, imageId2);
            imageIdToFilesetId.put(imageId2, filesetId);
            helper.debug("Registered Image:%s=>Fileset:%s", imageId2, filesetId);
        }

    }

}
