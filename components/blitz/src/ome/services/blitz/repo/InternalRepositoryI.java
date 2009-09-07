/*
 *   $Id$
 *
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.services.blitz.repo;

import static omero.rtypes.rstring;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.UUID;

import ome.services.blitz.fire.Registry;
import omero.api.RawFileStorePrx;
import omero.api.RawPixelsStorePrx;
import omero.api.RenderingEnginePrx;
import omero.api.ServiceFactoryPrx;
import omero.api.ThumbnailStorePrx;
import omero.grid.RepositoryPrx;
import omero.grid.RepositoryPrxHelper;
import omero.grid._InternalRepositoryDisp;
import omero.model.OriginalFile;
import omero.model.Repository;
import omero.model.RepositoryI;

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

    private final Registry reg;

    private final Repository description;

    private final RepositoryPrx proxy;

    private final RandomAccessFile raf;

    private final FileChannel channel;

    private final FileLock lock;

    private final String dbUuid;

    private final String repoUuid;

    public InternalRepositoryI() throws Exception {

        String mount = System.getProperty("omero.repo");
        log.info("Initializing repository in" + mount);

        //
        // Ice Initialization
        //
        id = new Ice.InitializationData();
        id.properties = Ice.Util.createProperties();
        String ICE_CONFIG = System.getProperty("ICE_CONFIG");
        if (ICE_CONFIG != null) {
            id.properties.load(ICE_CONFIG);
        }
        ic = Ice.Util.initialize(id);
        reg = new Registry.Impl(ic);
        oa = ic.createObjectAdapter("RepositoryAdapter");
        String serverId = ic.getProperties().getProperty("Ice.Admin.ServerId");
        Ice.Identity id = Ice.Util.stringToIdentity(serverId);
        Ice.ObjectPrx obj = oa.add(this, id);

        //
        // Database access
        //
        ServiceFactoryPrx sf = reg.getInternalServiceFactory("root", null, 5,
                12, UUID.randomUUID().toString());
        try {
            dbUuid = sf.getConfigService().getDatabaseUuid().getValue();
            File mountDir = new File(mount);
            File omeroDir = new File(mountDir, ".omero");
            File uuidDir = new File(omeroDir, dbUuid);
            if (!uuidDir.exists()) {
                uuidDir.mkdirs();
                log.info("Creating " + uuidDir);
            }

            File repoLock = new File(uuidDir, "omero.grid.repository");
            raf = new RandomAccessFile(repoLock, "rw");
            channel = raf.getChannel();
            lock = channel.lock();
            String line = raf.readLine();
            if (line == null || line.trim().equals("")) {
                repoUuid = UUID.randomUUID().toString();
                raf.writeChars(repoUuid);
                Repository r = new RepositoryI();
                r.setUuid(rstring(repoUuid));
                r.setName(rstring(mount));
                r.setType(rstring("unused"));
                description = (Repository) sf.getUpdateService()
                        .saveAndReturnObject(r);
                log.info("Registered new repository: " + repoUuid);
            } else {
                repoUuid = line.trim();
                description = (Repository) sf.getQueryService().findByString(
                        "Repository", "uuid", repoUuid);
                log.info("Opened repository: " + repoUuid);
            }
        } finally {
            sf.destroy();
        }

        //
        // Servants
        //
        PublicRepositoryI pr = new PublicRepositoryI();
        proxy = RepositoryPrxHelper.uncheckedCast(oa.addWithUUID(pr));

        //
        // Activation & Registration
        //
        oa.activate();
        Registry reg = new Registry.Impl(ic);
        if (reg.getGridQuery().findObjectById(id) == null) {
            reg.addObject(obj);
        }

    }

    public void close() {

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