/*
 *   Copyright (C) 2006-2011 University of Dundee & Open Microscopy Environment.
 *   All rights reserved.
 *
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.formats.importer.util;

import java.io.File;
import java.util.UUID;

import ome.model.containers.Dataset;
import ome.model.core.Pixels;
import ome.system.ServiceFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ResourceUtils;

/**
 * test fixture which uses a hard-coded file ("tinyTest.d3d.dv") from the
 * classpath, and adds them to a new UUID-named dataset.
 *
 * @author Josh Moore, josh.moore at gmx.de
 * @version $Revision: 1167 $, $Date: 2006-12-15 10:39:34 +0000 (Fri, 15 Dec 2006) $
 * @see OMEROMetadataStore
 * @see ExampleUnitTest
 * @since 3.0-M3
 */
public class TinyImportFixture
{

    /** Hard-coded filename of the image to be imported */
    public final static String FILENAME = "tinyTest.d3d.dv";

    Logger log = LoggerFactory.getLogger(TinyImportFixture.class);

    private Dataset d;

    private ServiceFactory sf;

    public TinyImportFixture(ServiceFactory services) throws Exception
    {
        this.sf = services;
    }

    /**
     * checks for the necessary fields and initializes the {@link ImportLibrary}
     *
     * @throws Exception
     */
    public void setUp() throws Exception
    {
        d = new Dataset();
        d.setName(UUID.randomUUID().toString());
        d = sf.getUpdateService().saveAndReturnObject(d);

        File tinyTest = ResourceUtils.getFile("classpath:"+FILENAME);
    }

    public void doImport() {}
    public void tearDown() {}

    /** provides access to the created {@link Dataset} instance.
     */
    public Dataset getDataset()
    {
        return d;
    }

    public Pixels getPixels()
    {
        return sf.getQueryService().findByQuery("select p from Dataset d " +
            "join d.imageLinks dil " +
            "join dil.child img " +
            "join img.pixels p where d.id = "+d.getId(), null);
    }
}
