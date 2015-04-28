/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2015 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.env.data.views.calls;

import ij.ImagePlus;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.openmicroscopy.shoola.env.data.OmeroImageService;
import org.openmicroscopy.shoola.env.data.OmeroMetadataService;
import org.openmicroscopy.shoola.env.data.model.FileObject;
import org.openmicroscopy.shoola.env.data.model.ResultsObject;
import org.openmicroscopy.shoola.env.data.util.SecurityContext;
import org.openmicroscopy.shoola.env.data.views.BatchCall;
import org.openmicroscopy.shoola.env.data.views.BatchCallTree;
import org.openmicroscopy.shoola.util.roi.io.ROIReader;

import pojos.ExperimenterData;
import pojos.FileAnnotationData;
import pojos.ImageData;
import pojos.ROIData;


/**
 * Saving the ImageJ results back to OMERO.
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since 5.1
 */
public class ResultsSaver
    extends BatchCallTree
{

    /** The security context.*/
    private SecurityContext ctx;

    /** The results to save.*/
    private ResultsObject results;

    /** Reference to the Image Service.*/
    private OmeroImageService svc;

    /** Reference to the Metadata Service.*/
    private OmeroMetadataService msvc;

    /** The partial result.*/
    private Object result;

    /** Create call to save the results.*/
    private void saveROIandTableResults()
    {
        List<Object> objects = results.getRefObjects();
        Iterator<Object> i = objects.iterator();
        Object o;
        FileObject file;
        long id;
        ROIReader reader = new ROIReader();
        List<ROIData> rois;
        ExperimenterData exp = ctx.getExperimenterData();
        final long expID = exp.getId();
        while (i.hasNext()) {
            o = i.next();
            if (o instanceof FileObject) {
                file = (FileObject) o;
                id = file.getOMEROID();
                if (id >= 0) {
                    ctx = new SecurityContext(file.getGroupID());
                    ImagePlus img = (ImagePlus) file.getFile();
                    rois = reader.readImageJROI(id, img);
                    if (CollectionUtils.isEmpty(rois)) {
                        rois = reader.readImageJROI(id);
                    }
                    //create a tmp file.
                    File f = null;
                    String name = FilenameUtils.getBaseName(
                            FilenameUtils.removeExtension(img.getTitle()));
                    try {
                        f = File.createTempFile(name, ".csv");
                        f.deleteOnExit();
                        reader.readROIMeasurement(f);
                    } catch (Exception e) {
                        context.getLogger().error(this,
                                "Cannot Save the ROIs results: "
                                        +e.getMessage());
                    }
                    if (f != null) {
                        final String description = "Save ROIs Results";
                        final long imageID = id;
                        final File fi = f;
                        final List<ROIData> list = rois;
                        add(new BatchCall(description) {
                            public void doCall() { 
                                try {
                                    //first save the roi
                                    svc.saveROI(ctx, imageID, expID, list);
                                    FileAnnotationData fa =
                                            new FileAnnotationData(fi);
                                    //TODO: create ns.
                                    result = msvc.annotate(ctx, ImageData.class,
                                            imageID, fa);
                                } catch (Exception e) {
                                    context.getLogger().error(this,
                                            "Cannot Save the ROIs results: "
                                                    +e.getMessage());
                                }
                            }
                        });
                    }
                }
            }
        }
    }

    /** Create call to save the results.*/
    private void saveTableResults()
    {
        List<Object> objects = results.getRefObjects();
        Iterator<Object> i = objects.iterator();
        Object o;
        FileObject file;
        long id;
        ROIReader reader = new ROIReader();
        while (i.hasNext()) {
            o = i.next();
            if (o instanceof FileObject) {
                file = (FileObject) o;
                id = file.getOMEROID();
                if (id >= 0) {
                    ctx = new SecurityContext(file.getGroupID());
                    ImagePlus img = (ImagePlus) file.getFile();
                    //create a tmp file.
                    File f = null;
                    String name = FilenameUtils.getBaseName(
                            FilenameUtils.removeExtension(img.getTitle()));
                    try {
                        f = File.createTempFile(name, ".csv");
                        f.deleteOnExit();
                        reader.readROIMeasurement(f);
                    } catch (Exception e) {
                        context.getLogger().error(this,
                                "Cannot Save the ROIs results: "
                                        +e.getMessage());
                    }
                    if (f != null) {
                        final String description = "Save ROIs Results";
                        final long imageID = id;
                        final File fi = f;
                        add(new BatchCall(description) {
                            public void doCall() { 
                                try {
                                    FileAnnotationData fa =
                                            new FileAnnotationData(fi);
                                    //TODO: create ns.
                                    result = msvc.annotate(ctx, ImageData.class,
                                            imageID, fa);
                                } catch (Exception e) {
                                    context.getLogger().error(this,
                                            "Cannot Save the ROIs results: "
                                                    +e.getMessage());
                                }
                            }
                        });
                    }
                }
            }
        }
    }

    /** Create call to save the rois.*/
    private void saveROI()
    {
        List<Object> objects = results.getRefObjects();
        Iterator<Object> i = objects.iterator();
        Object o;
        FileObject file;
        long id;
        ROIReader reader = new ROIReader();
        List<ROIData> rois;
        ExperimenterData exp = ctx.getExperimenterData();
        final long expID = exp.getId();
        while (i.hasNext()) {
            o = i.next();
            if (o instanceof FileObject) {
                file = (FileObject) o;
                id = file.getOMEROID();
                if (id >= 0) {
                    ctx = new SecurityContext(file.getGroupID());
                    ImagePlus img = (ImagePlus) file.getFile();
                    rois = reader.readImageJROI(id, img);
                    if (CollectionUtils.isEmpty(rois)) {
                        rois = reader.readImageJROI(id);
                    }
                    if (CollectionUtils.isNotEmpty(rois)) {
                        final String description = "Save ROIs";
                        final long imageID = id;
                        final List<ROIData> list = rois;
                        add(new BatchCall(description) {
                            public void doCall() { 
                                try {
                                    result = svc.saveROI(ctx, imageID, expID,
                                            list);
                                } catch (Exception e) {
                                    context.getLogger().error(this,
                                            "Cannot Save ImageJ rois: "
                                                    +e.getMessage());
                                }
                            }
                        });
                    }
                }
            }
        }
    }

    /**
     * Creates a new instance.
     *
     * @param ctx The security context.
     * @param results The results to save.
     */
    public ResultsSaver(SecurityContext ctx, ResultsObject results)
    {
        this.ctx = ctx;
        if (results == null) {
            throw new IllegalArgumentException("No results to save.");
        }
        this.results = results;
        svc = context.getImageService();
        msvc = context.getMetadataService();
    }

    /** Builds the tree to save the results.*/
    protected void buildTree()
    {
        List<Object> objects = results.getRefObjects();
        if (CollectionUtils.isNotEmpty(objects)) {
            if (results.isROI()) {
                if (results.isTable()) {
                    saveROIandTableResults();
                } else {
                    saveROI();
                }
            } else {
                if (results.isTable()) {
                    saveTableResults();
                }
            }
        }

    }

    /**
     * Returns the result.
     * This will be packed by the framework into a feedback event and
     * sent to the provided call observer, if any.
     * 
     * @return A non-null object.
     */
    protected Object getPartialResult() { return result; }

    /**
     * Returns <code>null</code> as there's no final result.
     * @see BatchCallTree#getResult()
     */
    protected Object getResult() { return null; }

}
