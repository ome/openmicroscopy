/*
 * ome.services.RawPixelsBean
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

package ome.services;

// Java imports
import java.io.IOException;
import java.io.Serializable;
import java.nio.BufferOverflowException;
import java.nio.MappedByteBuffer;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Local;
import javax.ejb.PostActivate;
import javax.ejb.PrePassivate;
import javax.ejb.Remote;
import javax.ejb.Stateful;

// Third-party libraries
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.ejb.RemoteBinding;
import org.jboss.annotation.security.SecurityDomain;

// Application-internal dependencies
import ome.api.IPixels;
import ome.api.RawPixelsStore;
import ome.api.ServiceInterface;
import ome.conditions.ApiUsageException;
import ome.io.nio.DimensionsOutOfBoundsException;
import ome.io.nio.PixelBuffer;
import ome.io.nio.PixelsService;
import ome.logic.AbstractBean;
import ome.model.core.Pixels;

import omeis.providers.re.RenderingEngine;



/**
 * @author <br>
 *         Josh Moore&nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de"> josh.moore@gmx.de</a>
 * @version 3.0 <small> (<b>Internal version:</b> $Revision: 1.4 $ $Date:
 *          2005/07/05 16:13:52 $) </small>
 * @since OMERO3
 */
@Stateful
@Remote(RawPixelsStore.class)
@RemoteBinding(jndiBinding="omero/remote/ome.api.RawPixelsStore")
@Local(RenderingEngine.class)
@LocalBinding (jndiBinding="omero/local/ome.api.RawPixelsStore")
@SecurityDomain("OmeroSecurity")
public class RawPixelsBean extends AbstractBean 
    implements RawPixelsStore, Serializable
{

    private static final long serialVersionUID = -6640632220587930165L;

    private Long id; 
    
    private transient Pixels pixelsInstance;
    
    private transient PixelBuffer buffer;
    
    private transient PixelsService dataService;
    
    private transient IPixels metadataService;
    
    @Override
    protected Class<? extends ServiceInterface> getServiceInterface() {
    	return RawPixelsStore.class;
    }
    
    public final void setPixelsMetadata(IPixels metaService)
    {
    	throwIfAlreadySet(this.metadataService, metaService);
        metadataService = metaService;
    }
    
    public final void setPixelsData(PixelsService dataService)
    {
    	throwIfAlreadySet(this.dataService, dataService);
        this.dataService = dataService;
    }
    
    // ~ Lifecycle methods
	// =========================================================================
    
    @PostConstruct
    @PostActivate
    public void create()
    {  
        super.create();
        if ( id != null )
        {
            long reset = id.longValue();
            id = null;
            setPixelsId( reset );
        }
    }

    @PrePassivate
    @PreDestroy
    public void destroy()
    {
        super.destroy();
        // id is the only thing passivated.
        dataService = null;
        pixelsInstance = null;
        buffer = null;
    }
    
    // ~ Lifecycle
    // =========================================================================
    
    @RolesAllowed("user") 
    public void setPixelsId( long pixelsId )
    {
        if ( id == null || id.longValue() != pixelsId )
        {
            id = new Long( pixelsId );
            pixelsInstance = null;
            buffer = null;

            pixelsInstance = metadataService.retrievePixDescription( id );
        	buffer = dataService.getPixelBuffer( pixelsInstance );
		}
    }
    
    private void errorIfNotLoaded()
    {
        if ( buffer == null )
        throw new ApiUsageException(
                "This RawPixelsStore has not been properly initialized.\n" +
                "Please set the pixels id before executing any other methods.\n");
    }
    
    // ~ Delegation
    // =========================================================================

    @RolesAllowed("user")
    public byte[] calculateMessageDigest()
    {
        errorIfNotLoaded();
        
        try
        {
            return buffer.calculateMessageDigest();
        } catch (IOException e)
        {
            e.printStackTrace();
            throw new RuntimeException("Unimplemented exception.");
        }
    }

    @RolesAllowed("user")
    public byte[] getPlane(Integer arg0, Integer arg1, Integer arg2)
    {
        errorIfNotLoaded();
        
        MappedByteBuffer plane;
		try
		{
			plane = buffer.getPlane(arg0, arg1, arg2);
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}

        return bufferAsByteArrayWithExceptionIfNull( plane );
    }

    @RolesAllowed("user")
    public Long getPlaneOffset(Integer arg0, Integer arg1, Integer arg2)
    {
        errorIfNotLoaded();
        
        try
        {
            return buffer.getPlaneOffset(arg0, arg1, arg2);
        } catch (DimensionsOutOfBoundsException e)
        {
            e.printStackTrace();
            throw new RuntimeException("Unimplemented exception.");
        }
    }

    @RolesAllowed("user")
    public Integer getPlaneSize()
    {
        errorIfNotLoaded();
        
        return buffer.getPlaneSize();
    }

    @RolesAllowed("user")
    public byte[] getRegion(Integer arg0, Long arg1)
    {
        errorIfNotLoaded();
        
        MappedByteBuffer region;
        try
        {
            region = buffer.getRegion(arg0, arg1);
        } catch (IOException e)
        {
            e.printStackTrace();
            throw new RuntimeException("Unimplemented exception.");
        }
        return bufferAsByteArrayWithExceptionIfNull( region ); 
    }

    @RolesAllowed("user")
    public byte[] getRow(Integer arg0, Integer arg1, Integer arg2, Integer arg3)
    {
        errorIfNotLoaded();
        
        MappedByteBuffer row;
        try
        {
            row = buffer.getRow(arg0, arg1, arg2, arg3);
        } catch (IOException e)
        {
            e.printStackTrace();
            throw new RuntimeException("Unimplemented exception.");
        } catch (DimensionsOutOfBoundsException e)
        {
            e.printStackTrace();
            throw new RuntimeException("Unimplemented exception.");
        }
        return bufferAsByteArrayWithExceptionIfNull( row ); 
        
    }

    @RolesAllowed("user")
    public Long getRowOffset(Integer arg0, Integer arg1, Integer arg2, Integer arg3)
    {
        errorIfNotLoaded();
        
        try
        {
            return buffer.getRowOffset(arg0, arg1, arg2, arg3);
        } catch (DimensionsOutOfBoundsException e)
        {
            e.printStackTrace();
            throw new RuntimeException("Unimplemented exception.");
        }
    }

    @RolesAllowed("user")
    public Integer getRowSize()
    {
        errorIfNotLoaded();
        
        return buffer.getRowSize();
    }

    @RolesAllowed("user")
    public byte[] getStack(Integer arg0, Integer arg1)
    {
        errorIfNotLoaded();
        
        MappedByteBuffer stack;
        try
        {
            stack = buffer.getStack(arg0, arg1);
        } catch (IOException e)
        {
            e.printStackTrace();
            throw new RuntimeException("Unimplemented exception.");
        } catch (DimensionsOutOfBoundsException e)
        {
            e.printStackTrace();
            throw new RuntimeException("Unimplemented exception.");
        }
        return bufferAsByteArrayWithExceptionIfNull( stack );
    }

    @RolesAllowed("user")
    public Long getStackOffset(Integer arg0, Integer arg1)
    {
        errorIfNotLoaded();
        
        try
        {
            return buffer.getStackOffset(arg0, arg1);
        } catch (DimensionsOutOfBoundsException e)
        {
            e.printStackTrace();
            throw new RuntimeException("Unimplemented exception.");
        }
    }

    @RolesAllowed("user")
    public Integer getStackSize()
    {
        errorIfNotLoaded();
        
        return buffer.getStackSize();
    }

    @RolesAllowed("user")
    public byte[] getTimepoint(Integer arg0)
    {
        errorIfNotLoaded();
        
        MappedByteBuffer timepoint;
        try
        {
            timepoint = buffer.getTimepoint(arg0);
        } catch (IOException e)
        {
            e.printStackTrace();
            throw new RuntimeException("Unimplemented exception.");
        } catch (DimensionsOutOfBoundsException e)
        {
            e.printStackTrace();
            throw new RuntimeException("Unimplemented exception.");
        }
        return bufferAsByteArrayWithExceptionIfNull( timepoint );
    }

    @RolesAllowed("user")
    public Long getTimepointOffset(Integer arg0)
    {
        errorIfNotLoaded();
        
        try
        {
            return buffer.getTimepointOffset(arg0);
        } catch (DimensionsOutOfBoundsException e)
        {
            e.printStackTrace();
            throw new RuntimeException("Unimplemented exception.");
        }
    }

    @RolesAllowed("user")
    public Integer getTimepointSize()
    {
        errorIfNotLoaded();
        
        return buffer.getTimepointSize();
    }

    @RolesAllowed("user")
    public Integer getTotalSize()
    {
        errorIfNotLoaded();
        
        return buffer.getTotalSize();
    }

    @RolesAllowed("user")
    public void setPlane(byte[] arg0, Integer arg1, Integer arg2, Integer arg3)
    {
        errorIfNotLoaded();
        
        try
        {
            buffer.setPlane(arg0, arg1, arg2, arg3);
        } catch (BufferOverflowException e)
        {
            e.printStackTrace();
            throw new RuntimeException("Unimplemented exception.");
        } catch (IOException e)
        {
            e.printStackTrace();
            throw new RuntimeException("Unimplemented exception.");
        } catch (DimensionsOutOfBoundsException e)
        {
            e.printStackTrace();
            throw new RuntimeException("Unimplemented exception.");
        }
    }

    @RolesAllowed("user")
    public void setRegion(Integer arg0, Long arg1, byte[] arg2)
    {
        errorIfNotLoaded();
        
        try
        {
            buffer.setRegion(arg0, arg1, arg2);
        } catch (BufferOverflowException e)
        {
            e.printStackTrace();
            throw new RuntimeException("Unimplemented exception.");
        } catch (IOException e)
        {
            e.printStackTrace();
            throw new RuntimeException("Unimplemented exception.");
        }
    }

    @RolesAllowed("user")
    public void setRow(byte[] arg0, Integer arg1, Integer arg2, Integer arg3, Integer arg4)
    {
        errorIfNotLoaded();
        
        // FIXME buffer.setRow(arg0, arg1, arg2, arg3, arg4);
        throw new RuntimeException("Not implemented yet.");
    }

    @RolesAllowed("user")
    public void setStack(byte[] arg0, Integer arg1, Integer arg2, Integer arg3)
    {
        errorIfNotLoaded();
        
        try
        {
            buffer.setStack(arg0, arg1, arg2, arg3);
        } catch (BufferOverflowException e)
        {
            e.printStackTrace();
            throw new RuntimeException("Unimplemented exception.");
        } catch (IOException e)
        {
            e.printStackTrace();
            throw new RuntimeException("Unimplemented exception.");
        } catch (DimensionsOutOfBoundsException e)
        {
            e.printStackTrace();
            throw new RuntimeException("Unimplemented exception.");
        }
    }

    @RolesAllowed("user")
    public void setTimepoint(byte[] arg0, Integer arg1)
    {
        errorIfNotLoaded();
        
        try
        {
            buffer.setTimepoint(arg0, arg1);
        } catch (BufferOverflowException e)
        {
            e.printStackTrace();
            throw new RuntimeException("Unimplemented exception.");
        } catch (IOException e)
        {
            e.printStackTrace();
            throw new RuntimeException("Unimplemented exception.");
        } catch (DimensionsOutOfBoundsException e)
        {
            e.printStackTrace();
            throw new RuntimeException("Unimplemented exception.");
        }
    }
    
    // ~ Helpers
    // =========================================================================
    
    private byte[] bufferAsByteArrayWithExceptionIfNull( MappedByteBuffer buffer )
    {
		byte[] b = new byte[buffer.capacity()];
		buffer.get(b, 0, buffer.capacity());
		return b;
    }
}
