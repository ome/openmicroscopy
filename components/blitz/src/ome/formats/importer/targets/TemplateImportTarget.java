/*
 *  Copyright (C) 2015 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
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

package ome.formats.importer.targets;

import java.io.File;

import ome.formats.OMEROMetadataStoreClient;
import ome.formats.importer.ImportContainer;
import omero.api.IUpdatePrx;
import omero.constants.namespaces.NSTARGETTEMPLATE;
import omero.model.CommentAnnotation;
import omero.model.CommentAnnotationI;
import omero.model.IObject;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TemplateImportTarget implements ImportTarget {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    private String template;

    @Override
    public void init(String target) {
        this.template = target;
    }

    public String getTemplate() {
        return this.template;
    }

    @Override
    public IObject load(OMEROMetadataStoreClient client, ImportContainer ic) throws Exception {
        // Now we create an annotation for delaying parsing of the template
        // until we can receive server-side pre-processed paths.
        IUpdatePrx update = client.getServiceFactory().getUpdateService();
        CommentAnnotation ca = new CommentAnnotationI();
        ca.setNs(omero.rtypes.rstring(NSTARGETTEMPLATE.value));
        ca.setTextValue(omero.rtypes.rstring(this.template));

        // Here we save the unix-styled path to the directory that the target
        // file was stored in. Server-side, further directories should be
        // stripped from this based on the pattern and *that* will be
        // run against the regex "template".
        File dir = ic.getFile().getParentFile();
        String desc = FilenameUtils.separatorsToUnix(dir.toString());
        ca.setDescription(omero.rtypes.rstring(desc));
        ca = (CommentAnnotation) update.saveAndReturnObject(ca);
        log.debug("Created annotation {}", ca.getId().getValue());
        return ca;
    }

}
