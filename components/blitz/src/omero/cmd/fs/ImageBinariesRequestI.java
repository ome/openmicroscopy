/*
 * Copyright (C) 2014 Glencoe Software, Inc. All rights reserved.
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

package omero.cmd.fs;

import static omero.rtypes.rlong;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ome.api.IQuery;
import ome.conditions.RootException;
import ome.io.nio.PixelsService;
import ome.model.IObject;
import ome.model.core.Image;
import ome.model.core.Pixels;
import ome.model.display.Thumbnail;
import ome.model.fs.Fileset;
import ome.parameters.Parameters;
import ome.security.ACLVoter;
import omero.cmd.ERR;
import omero.cmd.HandleI.Cancel;
import omero.cmd.Helper;
import omero.cmd.IRequest;
import omero.cmd.ImageBinariesRequest;
import omero.cmd.ImageBinariesResponse;
import omero.cmd.Response;

/**
 * Workflow for converting attached original images into OMERO5+ filesets. Input
 * is an Image ID and which of the various workflow steps ("deletePixels", etc)
 * will be performed. In any case, a the image will be loaded and various metrics
 * of the used space stored in the {@link ImageBinariesResponse} instance.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 5.0.2
 */
public class ImageBinariesRequestI extends ImageBinariesRequest implements
        IRequest {

    private static final long serialVersionUID = -1L;

    private final ImageBinariesResponse rsp = new ImageBinariesResponse();

    private final PixelsService pixelsService;

    private final ACLVoter voter;

    private Helper helper;

    private File pixelsFile;

    private File pyramidFile;

    private List<File> thumbnailFiles = new ArrayList<File>();

    public ImageBinariesRequestI(PixelsService pixelsService,
            ACLVoter voter) {
        this.pixelsService = pixelsService;
        this.voter = voter;
    }

    //
    // CMD API
    //

    public Map<String, String> getCallContext() {
        Map<String, String> all = new HashMap<String, String>();
        all.put("omero.group", "-1");
        return all;
    }

    public void init(Helper helper) {
        this.helper = helper;
        this.helper.setSteps(7);
    }

    public Object step(int step) {
        helper.assertStep(step);
        switch (step) {
        case 0: findImage(); break;
        case 1: findAttached(); break;
        case 2: findBinary(); break;
        case 3: findFileset(); break;
        case 4: deleteAttached(); break;
        case 5: deletePixels(); break;
        case 6: deletePyramid(); break;
        default:
            throw helper.cancel(new ERR(), null, "unknown-step", "step" , ""+step);
        }
        return null;
    }

    @Override
    public void finish() throws Cancel {
        // no-op
    }

    public void buildResponse(int step, Object object) {
        helper.assertResponse(step);
        if (helper.isLast(step)) {
            helper.setResponseIfNull(rsp);
        }
    }

    public Response getResponse() {
        return helper.getResponse();
    }

    //
    // STEP METHODS
    //

    /**
     * Simply load the image for this instance, calling cancel if it cannot
     * loadable.
     */
    private void findImage() {
        try {
            IQuery query = helper.getServiceFactory().getQueryService();
            // Load as a test.
            query.get(Image.class, imageId);
        } catch (RootException re) {
            throw helper.cancel(new ERR(), re, "no-image", "image-id", ""
                    + imageId);
        }
    }

    private void findAttached() {
        IQuery query = helper.getServiceFactory().getQueryService();
        List<IObject> rv = query.findAllByQuery(
            "select o from Image i join i.pixels p " +
            "join p.pixelsFileMaps m join m.parent o " +
            "where i.id = :id", new Parameters().addId(imageId));
        rsp.attachedFiles = new ArrayList<Long>();
        for (IObject obj : rv) {
            rsp.attachedFiles.add(obj.getId());
        }
    }

    /**
     * Use {@link PixelsService} to find pre-FS binary files under
     * "/OMERO/Files", "/OMERO/Pixels", and "/OMERO/Thumbnails",
     *  and store their size in the response.
     */
    private void findBinary() {
        IQuery query = helper.getServiceFactory().getQueryService();
        Pixels pixels = query.get(Image.class, imageId).getPrimaryPixels();
        List<Thumbnail> thumbs = query.findAllByQuery(
                "select tb from Thumbnail tb where " +
                "tb.pixels.id = :id", new Parameters().addId(pixels.getId()));

        String path;
        path = pixelsService.getPixelsPath(pixels.getId());
        pixelsFile = new File(path);
        if (pixelsFile.exists()) {
            rsp.pixelSize = pixelsFile.length();
        }
        pyramidFile = new File(path + "_pyramid");
        if (pyramidFile.exists()) {
            rsp.pyramidSize = pyramidFile.length();
        }
        for (Thumbnail tb: thumbs) {
            path = pixelsService.getThumbnailPath(tb.getId());
            File thumbnailFile = new File(path);
            thumbnailFiles.add(thumbnailFile);
            rsp.thumbnailSize += thumbnailFile.length();
        }
    }

    /**
     * Load the {@link Fileset} for the imageId if it exists.
     */
    private void findFileset() {
        try {
            IQuery query = helper.getServiceFactory().getQueryService();
            Fileset fs = query.findByQuery(
                    "select fs from Image i join i.fileset fs "
                            + "where i.id = :id",
                    new Parameters().addId(imageId));
            if (fs != null) {
                rsp.filesetId = rlong(fs.getId());
            }
        } catch (RootException re) {
            throw helper.cancel(new ERR(), re, "fileset-load-err", "image-id",
                    "" + imageId);
        }

    }

    private void deleteAttached() {
        if (deleteAttached) {
            throw helper.cancel(new ERR(), null, "NYI");
        }
    }

    private void deletePixels() {
        if (deletePixels) {
            requireFileset("pixels");
            deleteFile("pixels", pixelsFile);
            rsp.pixelSize = pixelsFile.length();
        }
    }

    private void deletePyramid() {
        if (deletePyramid) {
            requireFileset("pyramid");
            deleteFile("pyramid", pyramidFile);
            rsp.pyramidSize = pyramidFile.length();
        }
    }

    private void requireFileset(String which) {
        throw helper.cancel(new ERR(), null, which + "-requires-fileset");
    }

    private void deleteFile(String which, File file) {

        if (!file.exists()) {
            return; // Nothing to do
        }

        IQuery query = helper.getServiceFactory().getQueryService();
        Image image = query.get(Image.class, imageId);
        if (!voter.allowDelete(image, image.getDetails())) {
            throw helper.cancel(new ERR(), null, which + "-delete-disallowed");
        }
        if (!file.delete()) {
            // TODO: should we schedule for deleteOnExit here?
            throw helper.cancel(new ERR(), null, which + "-delete-false");
        }
    }
}
