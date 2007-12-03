/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services;

import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;

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
import javax.interceptor.Interceptors;

// Third-party libraries
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.ejb.RemoteBinding;
import org.jboss.annotation.ejb.RemoteBindings;
import org.jboss.annotation.security.SecurityDomain;
import org.springframework.transaction.annotation.Transactional;

// Application-internal dependencies
import ome.api.IPixels;
import ome.api.IRepositoryInfo;
import ome.api.RawPixelsStore;
import ome.api.ServiceInterface;
import ome.conditions.ApiUsageException;
import ome.conditions.ResourceError;
import ome.io.nio.DimensionsOutOfBoundsException;
import ome.io.nio.PixelBuffer;
import ome.io.nio.PixelsService;
import ome.model.core.Pixels;
import ome.services.util.OmeroAroundInvoke;

import omeis.providers.re.RenderingEngine;

/**
 * @author <br>
 *         Josh Moore&nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de"> josh.moore@gmx.de</a>
 * @version 3.0 <small> (<b>Internal version:</b> $Revision$ $Date:
 *          2005/07/05 16:13:52 $) </small>
 * @since OMERO3
 */
@TransactionManagement(TransactionManagementType.BEAN)
@Transactional(readOnly = false)
@Stateful
@Remote(RawPixelsStore.class)
@RemoteBindings({
    @RemoteBinding(jndiBinding = "omero/remote/ome.api.RawPixelsStore"),
    @RemoteBinding(jndiBinding = "omero/secure/ome.api.RawPixelsStore",
		   clientBindUrl="sslsocket://0.0.0.0:3843")
})
@Local(RenderingEngine.class)
@LocalBinding(jndiBinding = "omero/local/ome.api.RawPixelsStore")
@Interceptors( { OmeroAroundInvoke.class })
@SecurityDomain("OmeroSecurity")
public class RawPixelsBean extends AbstractStatefulBean implements RawPixelsStore {
    /** The logger for this particular class */
    private static Log log = LogFactory.getLog(RawPixelsBean.class);

    private static final long serialVersionUID = -6640632220587930165L;

    private Long id;
    
    private transient Long reset = null;

    private transient Pixels pixelsInstance;

    private transient PixelBuffer buffer;

    private transient PixelsService dataService;

    private transient IPixels metadataService;

    /** the disk space checking service */
    private transient IRepositoryInfo iRepositoryInfo;

    /** is file service checking for disk overflow */
    private transient boolean diskSpaceChecking;
    
    /** A copy buffer for the pixel retrieval. */
    private transient byte[] readBuffer;

    /**
     * default constructor
     */
    public RawPixelsBean() {}
    
    /**
     * overriden to allow Spring to set boolean
     * @param checking
     */
    public RawPixelsBean(boolean checking) {
    	this.diskSpaceChecking = checking;
    }
    
    public Class<? extends ServiceInterface> getServiceInterface() {
        return RawPixelsStore.class;
    }

    public final void setPixelsMetadata(IPixels metaService) {
        getBeanHelper().throwIfAlreadySet(this.metadataService, metaService);
        metadataService = metaService;
    }

    public final void setPixelsData(PixelsService dataService) {
        getBeanHelper().throwIfAlreadySet(this.dataService, dataService);
        this.dataService = dataService;
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
    
    // ~ Lifecycle methods
    // =========================================================================

    @PostConstruct
    @PostActivate
    public void create() {
        selfConfigure();
        // no longer trying to recreate here because of transactional 
        // difficulties. instead we'll set reset, and let errorIfNotLoaded()
        // do the work.
        if (id != null) {
            reset = id;
            id = null;
        }
    }

    @PrePassivate
    @PreDestroy
    public void destroy() {
        // id is the only thing passivated.
        dataService = null;
        pixelsInstance = null;
        closePixelBuffer();
        buffer = null;
        readBuffer = null;
    }

    @RolesAllowed("user")
    @Remove
    @Transactional(readOnly = true)
    public void close() {
    	closePixelBuffer();
    }
    
    /**
     * Close the active pixel buffer, cleaning up any potential messes left by
     * the pixel buffer itself.
     */
    private void closePixelBuffer()
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

    @RolesAllowed("user")
    public void setPixelsId(long pixelsId) {
        if (id == null || id.longValue() != pixelsId) {
            id = new Long(pixelsId);
            pixelsInstance = null;
            closePixelBuffer();
            buffer = null;
            reset = null;

            pixelsInstance = metadataService.retrievePixDescription(id);
            buffer = dataService.getPixelBuffer(pixelsInstance);
        }
    }

    private synchronized void errorIfNotLoaded() {
        // If we're not loaded because of passivation, then load.
        if (reset != null) {
            id = null;
            setPixelsId(reset.longValue());
            reset = null;
        }
        if (buffer == null) {
            throw new ApiUsageException(
                    "This RawPixelsStore has not been properly initialized.\n"
                            + "Please set the pixels id before executing any other methods.\n");
        }
    }

    // ~ Delegation
    // =========================================================================

    @RolesAllowed("user")
    public byte[] calculateMessageDigest() {
        errorIfNotLoaded();

        try {
            return buffer.calculateMessageDigest();
        } catch (Exception e) {
            handleException(e);
        }
        return null;
    }

    @RolesAllowed("user")
    public byte[] getPlaneRegion(Integer z, Integer c, Integer t,
    		Integer count, Integer offset) {
        errorIfNotLoaded();

        int size = buffer.getByteWidth() * count;
        if (readBuffer == null || readBuffer.length != size)
        	readBuffer = new byte[size];
        try {
            readBuffer = buffer.getPlaneRegionDirect(z, c, t, count,
                                                     offset, readBuffer);
        } catch (Exception e) {
            handleException(e);
        }
        return readBuffer;
    }
    
    @RolesAllowed("user")
    public byte[] getPlane(Integer arg0, Integer arg1, Integer arg2) {
        errorIfNotLoaded();

        int size = buffer.getPlaneSize();
        if (readBuffer == null || readBuffer.length != size)
        	readBuffer = new byte[size];
        try {
            readBuffer = buffer.getPlaneDirect(arg0, arg1, arg2, readBuffer);
        } catch (Exception e) {
            handleException(e);
        }
        return readBuffer;
    }

    @RolesAllowed("user")
    public Long getPlaneOffset(Integer arg0, Integer arg1, Integer arg2) {
        errorIfNotLoaded();

        try {
            return buffer.getPlaneOffset(arg0, arg1, arg2);
        } catch (Exception e) {
            handleException(e);
        }
        return null;
    }

    @RolesAllowed("user")
    public Integer getPlaneSize() {
        errorIfNotLoaded();

        return buffer.getPlaneSize();
    }

    @RolesAllowed("user")
    public byte[] getRegion(Integer arg0, Long arg1) {
        errorIfNotLoaded();

        MappedByteBuffer region = null;
        try {
            region = buffer.getRegion(arg0, arg1).getData();
        } catch (Exception e) {
            handleException(e);
        }
        return bufferAsByteArrayWithExceptionIfNull(region);
    }

    @RolesAllowed("user")
    public byte[] getRow(Integer arg0, Integer arg1, Integer arg2, Integer arg3) {
        errorIfNotLoaded();

        int size = buffer.getRowSize();
        if (readBuffer == null || readBuffer.length != size)
        	readBuffer = new byte[size];
        try {
            readBuffer = buffer.getRowDirect(arg0, arg1, arg2, arg3, readBuffer);
        } catch (Exception e) {
            handleException(e);
        }
        return readBuffer;
    }

    @RolesAllowed("user")
    public Long getRowOffset(Integer arg0, Integer arg1, Integer arg2,
            Integer arg3) {
        errorIfNotLoaded();

        try {
            return buffer.getRowOffset(arg0, arg1, arg2, arg3);
        } catch (Exception e) {
            handleException(e);
        }
        return null;
    }

    @RolesAllowed("user")
    public Integer getRowSize() {
        errorIfNotLoaded();

        return buffer.getRowSize();
    }

    @RolesAllowed("user")
    public byte[] getStack(Integer arg0, Integer arg1) {
        errorIfNotLoaded();

        int size = buffer.getStackSize();
        if (readBuffer == null || readBuffer.length != size)
        	readBuffer = new byte[size];
        try {
            readBuffer = buffer.getStackDirect(arg0, arg1, readBuffer);
        } catch (Exception e) {
            handleException(e);
        }
        return readBuffer;
    }

    @RolesAllowed("user")
    public Long getStackOffset(Integer arg0, Integer arg1) {
        errorIfNotLoaded();

        try {
            return buffer.getStackOffset(arg0, arg1);
        } catch (Exception e) {
            handleException(e);
        }
        return null;
    }

    @RolesAllowed("user")
    public Integer getStackSize() {
        errorIfNotLoaded();

        return buffer.getStackSize();
    }

    @RolesAllowed("user")
    public byte[] getTimepoint(Integer arg0) {
        errorIfNotLoaded();

        int size = buffer.getTimepointSize();
        if (readBuffer == null || readBuffer.length != size)
        	readBuffer = new byte[size];
        try {
            readBuffer = buffer.getTimepointDirect(arg0, readBuffer);
        } catch (Exception e) {
            handleException(e);
        }
        return readBuffer;
    }

    @RolesAllowed("user")
    public Long getTimepointOffset(Integer arg0) {
        errorIfNotLoaded();

        try {
            return buffer.getTimepointOffset(arg0);
        } catch (Exception e) {
            handleException(e);
        }
        return null;
    }

    @RolesAllowed("user")
    public Integer getTimepointSize() {
        errorIfNotLoaded();

        return buffer.getTimepointSize();
    }

    @RolesAllowed("user")
    public Integer getTotalSize() {
        errorIfNotLoaded();

        return buffer.getTotalSize();
    }
    
    @RolesAllowed("user")
    public int getByteWidth() {
    	errorIfNotLoaded();
    	
    	return buffer.getByteWidth();
    }
    
    @RolesAllowed("user")
    public boolean isSigned() {
    	errorIfNotLoaded();
    	
    	return buffer.isSigned();
    }
    
    @RolesAllowed("user")
    public boolean isFloat() {
    	errorIfNotLoaded();
    	
    	return buffer.isFloat();
    }

    @RolesAllowed("user")
    public void setPlane(byte[] arg0, Integer arg1, Integer arg2, Integer arg3) {
        errorIfNotLoaded();

    	if (diskSpaceChecking) {
        	iRepositoryInfo.sanityCheckRepository();
        }
        
        try {
            buffer.setPlane(arg0, arg1, arg2, arg3);
        } catch (Exception e) {
            handleException(e);
        }
    }

    @RolesAllowed("user")
    public void setRegion(Integer arg0, Long arg1, byte[] arg2) {
        errorIfNotLoaded();

    	if (diskSpaceChecking) {
        	iRepositoryInfo.sanityCheckRepository();
        }
        
        try {
            buffer.setRegion(arg0, arg1, arg2);
        } catch (Exception e) {
            handleException(e);
        }
    }

    @RolesAllowed("user")
    public void setRow(byte[] arg0, Integer arg1, Integer arg2, Integer arg3,
            Integer arg4) {
        errorIfNotLoaded();

    	if (diskSpaceChecking) {
        	iRepositoryInfo.sanityCheckRepository();
        }
        
        try {
            ByteBuffer buf = ByteBuffer.wrap(arg0);
            buffer.setRow(buf, arg1, arg2, arg3, arg4);
        } catch (Exception e) {
            handleException(e);
        }
    }

    @RolesAllowed("user")
    public void setStack(byte[] arg0, Integer arg1, Integer arg2, Integer arg3) {
        errorIfNotLoaded();

    	if (diskSpaceChecking) {
        	iRepositoryInfo.sanityCheckRepository();
        }
        
        try {
            buffer.setStack(arg0, arg1, arg2, arg3);
        } catch (Exception e) {
            handleException(e);
        }
    }

    @RolesAllowed("user")
    public void setTimepoint(byte[] arg0, Integer arg1) {
        errorIfNotLoaded();

    	if (diskSpaceChecking) {
        	iRepositoryInfo.sanityCheckRepository();
        }
        
        try {
            buffer.setTimepoint(arg0, arg1);
        } catch (Exception e) {
            handleException(e);
        }
    }

    // ~ Helpers
    // =========================================================================

    private byte[] bufferAsByteArrayWithExceptionIfNull(MappedByteBuffer buffer) {
        byte[] b = new byte[buffer.capacity()];
        buffer.get(b, 0, buffer.capacity());
        return b;
    }

    private void handleException(Exception e) {

        if (log.isDebugEnabled()) {
            log.debug("Error handling pixels.", e);
        }

        if (e instanceof IOException) {
            throw new ResourceError(e.getMessage());
        }
        if (e instanceof DimensionsOutOfBoundsException) {
            throw new ApiUsageException(e.getMessage());
        }
        if (e instanceof BufferOverflowException) {
            throw new ResourceError(e.getMessage());
        }

        // Fallthrough
        throw new RuntimeException(e);
    }

	public boolean isDiskSpaceChecking() {
		return diskSpaceChecking;
	}

	public void setDiskSpaceChecking(boolean diskSpaceChecking) {
		this.diskSpaceChecking = diskSpaceChecking;
	}
}
