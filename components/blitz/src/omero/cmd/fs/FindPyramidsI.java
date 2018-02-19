/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2018 University of Dundee & Open Microscopy Environment.
 *  All rights reserved.
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
package omero.cmd.fs;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ome.api.IQuery;
import ome.io.bioformats.BfPixelBuffer;
import ome.io.bioformats.BfPixelsWrapper;
import ome.io.bioformats.BfPyramidPixelBuffer;
import ome.io.nio.PixelBuffer;
import ome.io.nio.PixelsService;
import ome.model.core.Image;
import ome.model.core.Pixels;
import ome.parameters.Parameters;
import omero.cmd.HandleI.Cancel;
import omero.cmd.ERR;
import omero.cmd.Helper;
import omero.cmd.IRequest;
import omero.cmd.FindPyramids;
import omero.cmd.FindPyramidsResponse;
import omero.cmd.Response;

/**
 * Retrieves pyramid files
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since 5.4
 */
public class FindPyramidsI extends FindPyramids implements IRequest{

    private static final long serialVersionUID = -1L;

    private final FindPyramidsResponse rsp = new FindPyramidsResponse();

    /** The collection of pyramid image ID.*/
    private List<Long> imageIds = new ArrayList<Long>();

    private final PixelsService pixelsService;

    private Helper helper;

    private IQuery service;

    private String query;

    private File pixeldsDir;

    public FindPyramidsI(PixelsService pixelsService)
    {
        this.pixelsService = pixelsService;
        File tmp = new File(pixelsService.getPixelsPath(1L));
        pixeldsDir = tmp.getParentFile();
    }

    @Override
    public Map<String, String> getCallContext() {
        Map<String, String> all = new HashMap<String, String>();
        all.put("omero.group", "-1");
        return all;
    }

    @Override
    public void init(Helper helper) throws Cancel {
        this.helper = helper;
        service = helper.getServiceFactory().getQueryService();
        helper.setSteps(1);
    }

    @Override
    public Object step(int step) throws Cancel {
        helper.assertStep(step);
        switch (step) {
            case 0:
                findPyramids(); break;
            default:
                throw helper.cancel(new ERR(), null, "unknown-step", "step" , ""+step);
        }
        return null;
    }

    @Override
    public void finish() throws Cancel {
     // no-op
    }

    @Override
    public void buildResponse(int step, Object object) {
        helper.assertResponse(step);
        if (step == 0) {
            helper.setResponseIfNull(rsp);
        }
    }

    @Override
    public Response getResponse() {
        return helper.getResponse();
    }

    /**
     * Finds the pyramids and prepares the response
     */
    private void findPyramids() {
        StringBuilder sb = new StringBuilder();
        sb.append("select p from Pixels as p ");
        sb.append("left outer join fetch p.image as i ");
        sb.append("left outer join fetch i.details.creationEvent ");
        sb.append("where p.id = :id");
        query = sb.toString();
        walkDirectory(pixeldsDir);
        rsp.pyramidFiles = imageIds;
    }

    /**
     * Walks the specified directory.
     *
     * @param dir The directory to walk.
     */
    private void walkDirectory(File dir) {
        File[] files = dir.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                walkDirectory(file);
            } else {
                String name = file.getName();
                if (name.endsWith("_pyramid")) {
                    boolean toDelete = true;
                    if (checkEmptyFile) {
                        if (file.length() == 0) {
                            File[] list = pixeldsDir.listFiles();
                            for (File lockfile : list) {
                                String n = lockfile.getName();
                                if (n.startsWith("." + name) &&
                                (n.endsWith(".tmp") || n.endsWith(".pyr_lock"))) {
                                    toDelete = false;
                                    break;
                                }
                                
                            }
                        }
                    }
                    if (toDelete) {
                        String[] values = name.split("_");
                        long id = getImage(Long.parseLong(values[0]));
                        if (id > 0) {
                            imageIds.add(id);
                        }
                    }
                }
            }
        }
    }

    /**
     * Finds the image matching the criteria.
     * @param pixelsId The pixels set ID.
     * @return The image Id or -1.
     */
    private long getImage(long pixelsId) {
        Pixels pixels = service.findByQuery(query, new Parameters().addId(pixelsId));
        Image image = pixels.getImage();
        if (checkEmptyFile) {
            return image.getId().longValue();
        }
        long creation = image.getDetails().getCreationEvent().getTime().getTime();
        if (importeAfter < creation/1000) {
            if (isLittleEndian(pixels) == littleEndian) {
                return image.getId().longValue();
            }
        }
        return -1;
    }

    /**
     * Returns whether or not the pixels set is little endian.
     * @param pixels The pixels set to handle
     * @return See above.
     */
    private boolean isLittleEndian(Pixels pixels) {
        PixelBuffer pf = pixelsService._getPixelBuffer(pixels, false);
        if (pf instanceof BfPixelsWrapper) {
            return ((BfPixelsWrapper) pf).isLittleEndian();
        }
        if (pf instanceof BfPyramidPixelBuffer) {
            return ((BfPyramidPixelBuffer) pf).isLittleEndian();
        }
        if (pf instanceof BfPixelBuffer) {
            return ((BfPixelBuffer) pf).isLittleEndian();
        }
        return false;
    }
}
