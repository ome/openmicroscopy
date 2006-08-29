/*
 * ome.logic.PixelsImpl
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

package ome.logic;

//Java imports
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;

//Third-party libraries
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.ejb.RemoteBinding;
import org.jboss.annotation.security.SecurityDomain;
import org.springframework.transaction.annotation.Transactional;

//Application-internal dependencies
import ome.api.IPixels;
import ome.api.ServiceInterface;
import ome.io.nio.PixelBuffer;
import ome.io.nio.PixelsService;
import ome.model.IObject;
import ome.model.core.Pixels;
import ome.model.display.RenderingDef;
import ome.model.enums.PixelsType;
import ome.parameters.Parameters;

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision: 1.3 $ $Date: 2005/06/12 23:27:31 $)
 * </small>
 * @since OME2.2
 */
@Transactional(readOnly=true)
@Stateless
@Remote(IPixels.class)
@RemoteBinding (jndiBinding="omero/remote/ome.api.IPixels")
@Local(IPixels.class)
@LocalBinding (jndiBinding="omero/local/ome.api.IPixels")
@SecurityDomain("OmeroSecurity")
@Interceptors({SimpleLifecycle.class})
class PixelsImpl extends AbstractLevel2Service
    implements IPixels
{

    protected transient PixelsService pixelsData;

    public final void setPixelsData( PixelsService pixelsData )
    {
    	throwIfAlreadySet(this.pixelsData, pixelsData);
        this.pixelsData = pixelsData;
    }
    
    @Override
    protected Class<? extends ServiceInterface> getServiceInterface()
    {
        return IPixels.class;
    }
    
    // ~ Service methods
	// =========================================================================
    
    @RolesAllowed("user") 
	public Pixels retrievePixDescription(long pixId) {
		Pixels p = (Pixels) iQuery.get(Pixels.class, pixId);
		return p;
	}

    //TODO we need to validate and make sure only one RndDef per user. 
    @RolesAllowed("user") 
    public RenderingDef retrieveRndSettings(final long pixId) {
        
        final Long userId = getSecuritySystem().currentUserId();
        return (RenderingDef) iQuery.findByQuery(
                " select rdef from RenderingDef rdef where " +
                " rdef.pixels.id = :pixid and rdef.details.owner.id = :ownerid",
                new Parameters().addLong("pixid",pixId).addLong("ownerid",userId));
	}

    @RolesAllowed("user") 
    @Transactional(readOnly=false)
	public void saveRndSettings(RenderingDef rndSettings) {
	    iUpdate.saveObject(rndSettings);
	}

    @RolesAllowed("user") 
    public int getBitDepth(PixelsType pixelsType)
    {
        return PixelBuffer.getBitDepth( pixelsType );
    }

    @RolesAllowed("user") 
    public <T extends IObject> T getEnumeration(Class<T> klass, String value)
    {
    	return iQuery.findByString(klass, "value", value);
    }

    @RolesAllowed("user") 
    public <T extends IObject> List<T> getAllEnumerations(Class<T> klass)
    {
    	return iQuery.findAll(klass, null);
    }
}
