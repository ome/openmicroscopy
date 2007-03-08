/*
 * ome.services.RawFileBean
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Local;
import javax.ejb.PostActivate;
import javax.ejb.PrePassivate;
import javax.ejb.Remote;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;

import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.ejb.RemoteBinding;
import org.jboss.annotation.security.SecurityDomain;
import org.springframework.transaction.annotation.Transactional;

import ome.api.IQuery;
import ome.api.RawFileStore;
import ome.api.ServiceInterface;
import ome.conditions.ResourceError;
import ome.io.nio.FileBuffer;
import ome.io.nio.OriginalFilesService;
import ome.logic.AbstractBean;
import ome.model.core.OriginalFile;
import ome.system.EventContext;
import ome.system.SimpleEventContext;

/**
 * Raw file gateway which provides access to the OMERO file repository.
 * 
 * @author Chris Allan &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:callan@blackcat.ca">callan@blackcat.ca</a>
 * @version 3.0 <small> (<b>Internal version:</b> $Revision$ $Date:
 *          2005/06/08 15:21:59 $) </small>
 * @since OMERO3.0
 */
@TransactionManagement(TransactionManagementType.BEAN)
@Transactional(readOnly = false)
@Stateful
@Remote(RawFileStore.class)
@RemoteBinding(jndiBinding = "omero/remote/ome.api.RawFileStore")
@Local(RawFileStore.class)
@LocalBinding(jndiBinding = "omero/local/ome.api.RawFileStore")
@SecurityDomain("OmeroSecurity")
public class RawFileBean extends AbstractBean implements RawFileStore,
        Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = -450924529925301925L;

    /** The id of the original files instance. */
    private Long id;

    /** The original file this service is currently working on. */
    private transient OriginalFile file;

    /** The file buffer for the service's original file. */
    private transient FileBuffer buffer;

    /** OMERO query service. */
    private transient IQuery iQuery;

    /** ROMIO I/O service for files. */
    private transient OriginalFilesService ioService;

    /*
     * (non-Javadoc)
     * 
     * @see ome.logic.AbstractBean#getServiceInterface()
     */
    @Override
    protected Class<? extends ServiceInterface> getServiceInterface() {
        return RawFileStore.class;
    }

    /**
     * Query service Bean injector.
     * 
     * @param iQuery
     *            an <code>IQuery</code> service.
     */
    public final void setQueryService(IQuery iQuery) {
        throwIfAlreadySet(this.iQuery, iQuery);
        this.iQuery = iQuery;
    }

    /**
     * I/O service (OriginalFilesService) Bean injector.
     * 
     * @param ioService
     *            an <code>OriginalFileService</code>.
     */
    public final void setOriginalFilesService(OriginalFilesService ioService) {
        throwIfAlreadySet(this.ioService, ioService);
        this.ioService = ioService;
    }

    /*
     * (non-Javadoc)
     * 
     * @see ome.logic.AbstractBean#create()
     */
    @Override
    @PostConstruct
    @PostActivate
    public void create() {
        super.create();
        if (id != null) {
            long reset = id.longValue();
            id = null;
            setFileId(reset);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see ome.logic.AbstractBean#destroy()
     */
    @Override
    @PrePassivate
    @PreDestroy
    public void destroy() {
        super.destroy();
        // id is the only thing passivated.
        ioService = null;
        file = null;
        try {
            if (buffer != null) {
                buffer.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new ResourceError(e.getMessage());
        }
        buffer = null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see ome.api.StatefulServiceInterface#close()
     */
    @Remove
    @Transactional(readOnly = true)
    public void close() {
        // don't need to do anything.
    }

    /*
     * (non-Javadoc)
     * 
     * @see ome.api.StatefulServiceInterface#getCurrentEventContext()
     */
    public EventContext getCurrentEventContext() {
        return new SimpleEventContext(getSecuritySystem().getEventContext());
    }

    /*
     * (non-Javadoc)
     * 
     * @see ome.api.RawFileStore#setFileId(long)
     */
    @RolesAllowed("user")
    @Transactional(readOnly = true)
    public void setFileId(long fileId) {
        if (id == null || id.longValue() != fileId) {
            id = new Long(fileId);
            file = null;
            try {
                if (buffer != null) {
                    buffer.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
                throw new ResourceError(e.getMessage());
            }
            buffer = null;

            file = iQuery.get(OriginalFile.class, id);
            buffer = ioService.getFileBuffer(file);
        }
    }
    
    /* (non-Javadoc)
     * @see ome.api.RawFileStore#exists()
     */
    @RolesAllowed("user")
    public boolean exists() {
    	File f = new File(file.getPath());
    	if (f.exists())
    	{
    		if (f.canRead() && f.canWrite())
    			return true;
    		else
    			throw new ResourceError("Cannot read or write to file.");
    	}
    	else
    	{
    		return false;
    	}
    }

    @RolesAllowed("user")
    public byte[] read(long position, int length) {
        byte[] rawBuf = new byte[length];
        ByteBuffer buf = ByteBuffer.wrap(rawBuf);

        try {
            buffer.read(buf, position);
        } catch (IOException e) {
            e.printStackTrace();
            throw new ResourceError(e.getMessage());
        }
        return rawBuf;
    }

    @RolesAllowed("user")
    public void write(byte[] buf, long position, int length) {
        ByteBuffer nioBuffer = ByteBuffer.wrap(buf);
        nioBuffer.limit(length);

        try {
            buffer.write(nioBuffer, position);
        } catch (IOException e) {
            e.printStackTrace();
            throw new ResourceError(e.getMessage());
        }
    }
}
