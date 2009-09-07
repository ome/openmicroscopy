/*
 *   $Id$
 *
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.services.blitz.repo;

import java.util.List;

import omero.ServerError;
import omero.api.RawFileStorePrx;
import omero.api.RawPixelsStorePrx;
import omero.api.RenderingEnginePrx;
import omero.api.ThumbnailStorePrx;
import omero.grid.RepositoryPrx;
import omero.grid._RepositoryDisp;
import omero.model.Format;
import omero.model.OriginalFile;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import Ice.Current;

/**
 * 
 * @since Beta4.1
 */
public class PublicRepositoryI extends _RepositoryDisp {

    private final static Log log = LogFactory.getLog(PublicRepositoryI.class);

    public PublicRepositoryI() throws Exception {
    }

    public void delete(String path, Current __current) throws ServerError {
        // TODO Auto-generated method stub
        
    }

    public List<String> list(String path, Current __current) throws ServerError {
        // TODO Auto-generated method stub
        return null;
    }

    public List<String> listDirs(String path, Current __current)
            throws ServerError {
        // TODO Auto-generated method stub
        return null;
    }

    public List<String> listFiles(String path, Current __current)
            throws ServerError {
        // TODO Auto-generated method stub
        return null;
    }

    public OriginalFile load(String path, Current __current) throws ServerError {
        // TODO Auto-generated method stub
        return null;
    }

    public RawPixelsStorePrx pixels(String path, Current __current)
            throws ServerError {
        // TODO Auto-generated method stub
        return null;
    }

    public RawFileStorePrx read(String path, Current __current)
            throws ServerError {
        // TODO Auto-generated method stub
        return null;
    }

    public OriginalFile register(String path, Format fmt, Current __current)
            throws ServerError {
        // TODO Auto-generated method stub
        return null;
    }

    public void rename(String path, Current __current) throws ServerError {
        // TODO Auto-generated method stub
        
    }

    public RenderingEnginePrx render(String path, Current __current)
            throws ServerError {
        // TODO Auto-generated method stub
        return null;
    }

    public ThumbnailStorePrx thumbs(String path, Current __current)
            throws ServerError {
        // TODO Auto-generated method stub
        return null;
    }

    public void transfer(String srcPath, RepositoryPrx target,
            String targetPath, Current __current) throws ServerError {
        // TODO Auto-generated method stub
        
    }

    public RawFileStorePrx write(String path, Current __current)
            throws ServerError {
        // TODO Auto-generated method stub
        return null;
    }
}