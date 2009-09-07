/*
 *   $Id$
 *
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.services.blitz.repo;

import java.io.File;
import java.util.UUID;

import ome.services.blitz.fire.Registry;
import omero.api.RawFileStorePrx;
import omero.api.RawPixelsStorePrx;
import omero.api.RenderingEnginePrx;
import omero.api.ThumbnailStorePrx;
import omero.grid.RepositoryPrx;
import omero.grid.RepositoryPrxHelper;
import omero.grid._InternalRepositoryDisp;
import omero.model.OriginalFile;
import omero.model.Repository;
import omero.model.RepositoryI;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import Ice.Current;
import Ice.ObjectAdapter;

/**
 * 
 * @since Beta4.1
 */
public class InternalRepositoryI extends _InternalRepositoryDisp {

    private final static Log log = LogFactory.getLog(InternalRepositoryI.class);

    private final Ice.InitializationData id;

    private final Ice.Communicator ic;

    private final Ice.ObjectAdapter oa;

    private final Repository description;

    private final RepositoryPrx proxy;

    private final String repoUuid;
    
    private final String ownUuid = Ice.Util.generateUUID();

    public InternalRepositoryI() throws Exception {

        String repo = System.getProperty("omero.repo");
        log.info("Directory: " + repo);

        File repoDir = new File(repo);
        File repoFile = new File(repoDir.getAbsolutePath() + File.separator
                + ".omero.repository");

        // WILL HAVE TO WAIT ON BLITZ TO COME ONLINE

        if (!repoFile.exists()) {
            repoUuid = UUID.randomUUID().toString();
            FileUtils.writeStringToFile(repoFile, repoUuid);
            log.info("Initialized new repository: " + repoUuid);
            // UUID SHOULDN'T EXIST IN DB
            description = new RepositoryI();
        } else {
            repoUuid = FileUtils.readFileToString(repoFile);
            log.info("Opened repository: " + repoUuid);
            // UUID SHOULD EXIST IN DB
            description = new RepositoryI();
        }

        id = new Ice.InitializationData();
        id.properties = Ice.Util.createProperties();
        String ICE_CONFIG = System.getProperty("ICE_CONFIG");
        if (ICE_CONFIG != null) {
            id.properties.load(ICE_CONFIG);
        }
        ic = Ice.Util.initialize(id);
        oa = ic.createObjectAdapter("RepositoryAdapter");
        
        String serverId = ic.getProperties().getProperty("Ice.Admin.ServerId");
        Ice.Identity id = Ice.Util.stringToIdentity(serverId);
        Ice.ObjectPrx obj = oa.add(this, id);

        PublicRepositoryI pr = new PublicRepositoryI();
        proxy = RepositoryPrxHelper.uncheckedCast(oa.addWithUUID(pr));

        oa.activate();

        // Now that we're active, register us with the grid if we haven't
        // already been.
        Registry reg = new Registry.Impl(ic);
        if (reg.getGridQuery().findObjectById(id) == null) {
            reg.addObject(obj);
        }

    }

    public Ice.Communicator getCommunicator() {
        return ic;
    }

    public ObjectAdapter getObjectAdapter() {
        return oa;
    }

    public Repository getDescription(Current __current) {
        return description;
    }

    public RepositoryPrx getProxy(Current __current) {
        return proxy;
    }

    public RawFileStorePrx createRawFileStore(OriginalFile file,
            Current __current) {
        return null;
    }

    public RawPixelsStorePrx createRawPixelsStore(OriginalFile file,
            Current __current) {
        // TODO Auto-generated method stub
        return null;
    }

    public RenderingEnginePrx createRenderingEngine(OriginalFile file,
            Current __current) {
        // TODO Auto-generated method stub
        return null;
    }

    public ThumbnailStorePrx createThumbnailStore(OriginalFile file,
            Current __current) {
        // TODO Auto-generated method stub
        return null;
    }

}