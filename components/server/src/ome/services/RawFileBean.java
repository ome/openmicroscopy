/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.NonWritableChannelException;
import java.sql.SQLException;

import ome.annotations.RolesAllowed;
import ome.api.IAdmin;
import ome.api.IRepositoryInfo;
import ome.api.RawFileStore;
import ome.api.ServiceInterface;
import ome.conditions.ApiUsageException;
import ome.conditions.ResourceError;
import ome.conditions.RootException;
import ome.conditions.SecurityViolation;
import ome.io.nio.FileBuffer;
import ome.io.nio.OriginalFilesService;
import ome.model.core.OriginalFile;
import ome.util.ShallowCopy;
import ome.util.Utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.transaction.annotation.Transactional;

/**
 * Raw file gateway which provides access to the OMERO file repository.
 * 
 * @author Chris Allan &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:callan@blackcat.ca">callan@blackcat.ca</a>
 * @version 3.0 <small> (<b>Internal version:</b> $Revision$ $Date:
 *          2005/06/08 15:21:59 $) </small>
 * @since OMERO3.0
 */
@Transactional(readOnly = true)
public class RawFileBean extends AbstractStatefulBean implements RawFileStore {
    /**
     * 
     */
    private static final long serialVersionUID = -450924529925301925L;
    
    /** The logger for this particular class */
    private static Log log = LogFactory.getLog(RawPixelsBean.class);

    /** The id of the original files instance. */
    private Long id;

    /** Only set after a passivated bean is activated. */
    private transient Long reset = null;
    
    /** The original file this service is currently working on. */
    private transient OriginalFile file;

    /** The file buffer for the service's original file. */
    private transient FileBuffer buffer;
    
    /** ROMIO I/O service for files. */
    private transient OriginalFilesService ioService;

    /** the disk space checking service */
    private transient IRepositoryInfo iRepositoryInfo;

    /** admin for checking permissions of writes */
    private transient IAdmin admin;

    /** is file service checking for disk overflow */
    private transient boolean diskSpaceChecking;
    
    /**
     * default constructor
     */
    public RawFileBean() {}
    
    /**
     * overriden to allow Spring to set boolean
     * @param checking
     */
    public RawFileBean(boolean checking) {
    	this.diskSpaceChecking = checking;
    }
    public Class<? extends ServiceInterface> getServiceInterface() {
        return RawFileStore.class;
    }

    /**
     * I/O service (OriginalFilesService) Bean injector.
     * 
     * @param ioService
     *            an <code>OriginalFileService</code>.
     */
    public final void setOriginalFilesService(OriginalFilesService ioService) {
        getBeanHelper().throwIfAlreadySet(this.ioService, ioService);
        this.ioService = ioService;
    }
    
    /**
     * Disk Space Usage service Bean injector
     * @param iRepositoryInfo
     *   		  	an <code>IRepositoryInfo</code>
     */
    public final void setIRepositoryInfo(IRepositoryInfo iRepositoryInfo) {
        getBeanHelper().throwIfAlreadySet(this.iRepositoryInfo, iRepositoryInfo);
        this.iRepositoryInfo = iRepositoryInfo;
    }

    public final void setAdminService(IAdmin admin) {
        getBeanHelper().throwIfAlreadySet(this.admin, admin);
        this.admin = admin;
    }

    // See documentation on JobBean#passivate
    @RolesAllowed("user")
    @Transactional(readOnly = true)    
    public void passivate() {
	// Nothing necessary
    }

    // See documentation on JobBean#activate
    @RolesAllowed("user")
    @Transactional(readOnly = true)    
    public void activate() {
        if (id != null) {
            reset = id;
            id = null;
        }
    }

    @RolesAllowed("user")
    @Transactional(readOnly = false)
    public synchronized OriginalFile save() {
        if (isModified()) {
            Long id = (file == null) ? null : file.getId();
            if (id == null) {
                return null;
            }

            String path = buffer.getPath();

            try {
                buffer.force(true); // Flush to disk.
            } catch (IOException ie) {
                log.error("Failed to flush to disk.", ie);
                throw new ResourceError("Failed to flush to disk:"
                        + ie.getMessage());
            }

            try {
                byte[] hash = Utils.pathToSha1(path);
                file.setSha1(Utils.bytesToHex(hash));

                long size = new File(path).length();
                file.setSize(size);

            } catch (RuntimeException re) {
                // ticket:3140
                if (re.getCause() instanceof FileNotFoundException) {
                    String msg = "Cannot find path. Deleted? " + path;
                    log.warn(msg);
                    clean(); // Prevent a second exception on close.
                    throw new ResourceError(msg);
                }
                throw re;
            }

            iUpdate.flush();
            modified = false;

            return new ShallowCopy().copy(file);
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see ome.api.StatefulServiceInterface#close()
     */
    @RolesAllowed("user")
    @Transactional(readOnly = false)
    public synchronized void close() {
        try {
            save();
        } catch (RootException root) {
            // ticket:3140
            // if one of our exceptions, then just rethrow
            throw root;
        } catch (RuntimeException re) {
            Long id = (file == null ? null : file.getId());
            log.error("Failed to update file: " + id, re);
        } finally {
            clean();
        }
    }

    public void clean() {
        ioService = null;
        file = null;
        closeFileBuffer();
        buffer = null;
    }

    /**
     * Close the active file buffer, cleaning up any potential messes left by
     * the file buffer itself.
     */
    private void closeFileBuffer()
    {
		try
		{
			if (buffer != null)
				buffer.close();
		}
		catch (IOException e)
		{
            if (log.isDebugEnabled()) {
                log.debug("Buffer could not be closed successfully.", e);
            }
			throw new ResourceError(
					e.getMessage() + " Please check server log.");
		}
    }

    /*
     * (non-Javadoc)
     * 
     * @see ome.api.RawFileStore#setFileId(long)
     */
    @RolesAllowed("user")
    @Transactional(readOnly = true)
    public synchronized void setFileId(final long fileId) {
        setFileIdWithBuffer(fileId, null);
    }

    public synchronized void setFileIdWithBuffer(final long fileId,
            final FileBuffer buffer) {

        if (id == null || id.longValue() != fileId) {
            id = new Long(fileId);
            file = null;
            closeFileBuffer();
            this.buffer = null;

            modified = false;
            file = iQuery.get(OriginalFile.class, fileId);

            String mode = admin.canUpdate(file) ? "rw" : "r";

            if (buffer == null) {
                // If no buffer has been provided, then we check that this is
                // omero.data.dir (i.e. no repository) since otherwise our
                // use of ioService will not function.
                String repo = (String) iQuery.execute(new HibernateCallback<String>(){
                    public String doInHibernate(Session arg0)
                            throws HibernateException, SQLException {
                        return (String) arg0.createSQLQuery(
                                "select repo from originalfile where id = ?")
                                .setParameter(0, fileId)
                                .uniqueResult();
                    }});
                if (repo != null) {
                    throw new RuntimeException(repo);
                }

                this.buffer = ioService.getFileBuffer(file, mode);
            } else {
                this.buffer = buffer;
            }
        }
    }


    private synchronized void errorIfNotLoaded() {
        // If we're not loaded because of passivation, then load.
        if (reset != null) {
            id = null;
            setFileId(reset.longValue());
            reset = null;
        }
        if (buffer == null) {
            throw new ApiUsageException(
                    "This RawFileStore has not been properly initialized.\n"
                            + "Please set the file id before executing any other methods.\n");
        }
    }
    
    /* (non-Javadoc)
     * @see ome.api.RawFileStore#exists()
     */
    @RolesAllowed("user")
    public boolean exists() {
        errorIfNotLoaded();
        return new File(buffer.getPath()).exists();
    }

    @RolesAllowed("user")
    public byte[] read(long position, int length) {
        errorIfNotLoaded();
        byte[] rawBuf = new byte[length];
        ByteBuffer buf = ByteBuffer.wrap(rawBuf);

        try {
            buffer.read(buf, position);
        } catch (IOException e) {
            if (log.isDebugEnabled()) {
                log.debug("Buffer could not be read.", e);
            }
            throw new ResourceError(e.getMessage());
        }
        return rawBuf;
    }

    @RolesAllowed("user")
    public boolean truncate(long length) {
        errorIfNotLoaded();

        try {
            if (length < buffer.size()) {
                buffer.truncate(length);
                modified();
                return true;
            }
            return false;
        } catch (NonWritableChannelException nwce) {
            throw new SecurityViolation("File not writeable!");
        } catch (IOException e) {
            if (log.isDebugEnabled()) {
                log.debug("Buffer write did not occur.", e);
            }
            throw new ResourceError(e.getMessage());
        }
    }


    @RolesAllowed("user")
    public long size() {
        errorIfNotLoaded();

        try {
            return buffer.size();
        } catch (IOException e) {
            if (log.isDebugEnabled()) {
                log.debug("Buffer write did not occur.", e);
            }
            throw new ResourceError(e.getMessage());
        }
    }

    @RolesAllowed("user")
    public void write(byte[] buf, long position, int length) {
        errorIfNotLoaded();
        ByteBuffer nioBuffer = MappedByteBuffer.wrap(buf);
        nioBuffer.limit(length);

        if (diskSpaceChecking) {
            iRepositoryInfo.sanityCheckRepository();
        }
        
        try {
            buffer.write(nioBuffer, position);
            modified();
        } catch (NonWritableChannelException nwce) {
            throw new SecurityViolation("File not writeable!");
        } catch (IOException e) {
            if (log.isDebugEnabled()) {
                log.debug("Buffer write did not occur.", e);
            }
            throw new ResourceError(e.getMessage());
        }
    }

    /**
     * getter disk overflow checking
     * @return
     */
	public boolean isDiskSpaceChecking() {
		return diskSpaceChecking;
	}

	/**
	 * setter disk overflow checking
	 * @param diskSpaceChecking
	 *   a <code>boolean</code>
	 */
	public void setDiskSpaceChecking(boolean diskSpaceChecking) {
		this.diskSpaceChecking = diskSpaceChecking;
	}
}
