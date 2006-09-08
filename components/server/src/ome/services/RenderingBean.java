/*
 * ome.services.RenderingBean
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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Local;
import javax.ejb.PostActivate;
import javax.ejb.PrePassivate;
import javax.ejb.Remote;
import javax.ejb.Stateful;
import javax.ejb.TransactionAttribute;

import static javax.ejb.TransactionAttributeType.*;

// Third-party libraries
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.ejb.RemoteBinding;
import org.jboss.annotation.ejb.cache.Cache;
import org.jboss.annotation.security.SecurityDomain;
import org.jboss.ejb3.cache.NoPassivationCache;

// Application-internal dependencies
import ome.annotations.RevisionDate;
import ome.annotations.RevisionNumber;
import ome.api.ServiceInterface;
import ome.logic.AbstractLevel2Service;
import ome.model.IObject;
import ome.model.core.Channel;
import ome.model.core.Pixels;
import ome.model.display.QuantumDef;
import ome.model.enums.Family;
import ome.model.enums.RenderingModel;
import ome.util.ShallowCopy;

import omeis.providers.re.RGBBuffer;
import omeis.providers.re.RenderingEngine;
import omeis.providers.re.RenderingEngineImpl;
import omeis.providers.re.codomain.CodomainMapContext;
import omeis.providers.re.data.PlaneDef;

interface RenderingWrapper extends RenderingEngine {}

/**
 * Provides the {@link RenderingEngine} service. This class is a serializable 
 * wrapper around {@link RenderingEngineImpl}.
 * 
 * @author  Josh Moore, josh.moore at gmx.de
 * @version $Revision$, $Date$
 * @since   3.0-M3
 */
@RevisionDate("$Date$")
@RevisionNumber("$Revision$")
@Stateful
@Remote(RenderingEngine.class)
@RemoteBinding(jndiBinding="omero/remote/omeis.providers.re.RenderingEngine")
@Local(RenderingEngine.class)
@LocalBinding (jndiBinding="omero/local/omeis.providers.re.RenderingEngine")
@SecurityDomain("OmeroSecurity")
@Cache(NoPassivationCache.class)
public class RenderingBean extends AbstractLevel2Service 
    implements RenderingWrapper, Serializable
{

    private static final long serialVersionUID = -4383698215540637038L;

    private transient RenderingEngine delegate;

    public final void setDelegate(RenderingEngine delegate) {
    	throwIfAlreadySet(this.delegate, delegate);
		this.delegate = delegate;
	}
    
    @Override
    protected Class<? extends ServiceInterface> getServiceInterface() {
    	return RenderingWrapper.class;
    }
    
    // ~ Lifecycle methods
	// =========================================================================
    
    @PostActivate
    @PostConstruct
    public void create()
    {
    	super.create();
    }

    @PrePassivate
    public void passivate()
    {
    	super.passivationNotAllowed();
    }
    
    @PreDestroy
    public void destroy()
    {
        super.destroy();
        delegate = null;
    }
    
    // ~ DELEGATION
    // =========================================================================

    @RolesAllowed("user")
    @TransactionAttribute(REQUIRED)
    public void lookupPixels(long arg0)
    {
        delegate.lookupPixels(arg0);
    }

    @RolesAllowed("user")
    @TransactionAttribute(REQUIRED)
    public void lookupRenderingDef(long arg0)
    {
        delegate.lookupRenderingDef(arg0);
    }
    
    @RolesAllowed("user")
    @TransactionAttribute(REQUIRED)
    public void load()
    {
        delegate.load();
    }
    
    // -------------------------------------------------------------------------
    
    @RolesAllowed("user") 
    public RGBBuffer render(PlaneDef arg0)
    {
        return delegate.render(arg0);
    }
    
    // -------------------------------------------------------------------------
    
    @RolesAllowed("user") 
    public void resetDefaults()
    {
        delegate.resetDefaults();
    }

    @RolesAllowed("user") 
    public void saveCurrentSettings()
    {
        delegate.saveCurrentSettings();
        iUpdate.flush();
        iUpdate.commit();
    }

    // -------------------------------------------------------------------------
    
    @RolesAllowed("user") 
    public double getChannelCurveCoefficient(int arg0)
    {
        return delegate.getChannelCurveCoefficient(arg0);
    }

    @RolesAllowed("user") 
    public Family getChannelFamily(int arg0)
    {
        Family family = delegate.getChannelFamily(arg0);
        return copyFamily(family);
    }

    @RolesAllowed("user") 
    public boolean getChannelNoiseReduction(int arg0)
    {
        return delegate.getChannelNoiseReduction(arg0);
    }

    @RolesAllowed("user") 
    public double[] getChannelStats(int arg0)
    {
        return delegate.getChannelStats(arg0);
    }

    @RolesAllowed("user") 
    public double getChannelWindowEnd(int arg0)
    {
        return delegate.getChannelWindowEnd(arg0);
    }

    @RolesAllowed("user") 
    public double getChannelWindowStart(int arg0)
    {
        return delegate.getChannelWindowStart(arg0);
    }

    @RolesAllowed("user") 
    public int getDefaultT()
    {
        return delegate.getDefaultT();
    }

    @RolesAllowed("user") 
    public int getDefaultZ()
    {
        return delegate.getDefaultZ();
    }

    @RolesAllowed("user") 
    public RenderingModel getModel()
    {
        return copyRenderingModel(delegate.getModel());
    }

    @RolesAllowed("user") 
    public Pixels getPixels()
    {
    	return copyPixels(delegate.getPixels());
    }
    
    @RolesAllowed("user") 
    public List getAvailableModels()
    {
        List<RenderingModel> models = delegate.getAvailableModels();
        List result = new ArrayList();
        for (RenderingModel model : models)
        {
            result.add( copyRenderingModel(model));
        }
        return result; 
    }

    @RolesAllowed("user") 
    public List getAvailableFamilies()
    {
        List<Family> families = delegate.getAvailableFamilies();
        List result = new ArrayList();
        for (Family family : families)
        {
            result.add( copyFamily(family) );
        }
        return result;
    }
    
    @RolesAllowed("user") 
    public QuantumDef getQuantumDef()
    {
        QuantumDef def = delegate.getQuantumDef();
        return (QuantumDef) new ShallowCopy().copy(def);
    }

    @RolesAllowed("user") 
    public int[] getRGBA(int arg0)
    {
        return delegate.getRGBA(arg0);
    }

    @RolesAllowed("user") 
    public boolean isActive(int arg0)
    {
        return delegate.isActive(arg0);
    }

    // -------------------------------------------------------------------------
    
    @RolesAllowed("user") 
    public void addCodomainMap(CodomainMapContext arg0)
    {
        delegate.addCodomainMap(arg0);
    }
    
    @RolesAllowed("user") 
    public void removeCodomainMap(CodomainMapContext arg0)
    {
        delegate.removeCodomainMap(arg0);
    }
    
    @RolesAllowed("user") 
    public void updateCodomainMap(CodomainMapContext arg0)
    {
        delegate.updateCodomainMap(arg0);
    }

    @RolesAllowed("user") 
    public void setActive(int arg0, boolean arg1)
    {
        delegate.setActive(arg0, arg1);
    }

    @RolesAllowed("user") 
    public void setChannelWindow(int arg0, double arg1, double arg2)
    {
        delegate.setChannelWindow(arg0, arg1, arg2);
    }

    @RolesAllowed("user") 
    public void setCodomainInterval(int arg0, int arg1)
    {
        delegate.setCodomainInterval(arg0, arg1);
    }

    @RolesAllowed("user") 
    public void setDefaultT(int arg0)
    {
        delegate.setDefaultT(arg0);
    }

    @RolesAllowed("user") 
    public void setDefaultZ(int arg0)
    {
        delegate.setDefaultZ(arg0);
    }

    @RolesAllowed("user") 
    public void setModel(RenderingModel arg0)
    {
        RenderingModel m = lookup(arg0);
        delegate.setModel(m);
    }

    @RolesAllowed("user") 
    public void setQuantizationMap(int arg0, Family arg1, double arg2, boolean arg3)
    {
        Family f = lookup(arg1);
        delegate.setQuantizationMap(arg0, f, arg2, arg3);
    }

    @RolesAllowed("user") 
    public void setQuantumStrategy(int arg0)
    {
        delegate.setQuantumStrategy(arg0);
    }

    @RolesAllowed("user") 
    public void setRGBA(int arg0, int arg1, int arg2, int arg3, int arg4)
    {
        delegate.setRGBA(arg0, arg1, arg2, arg3, arg4);
    }


    // ~ Helpers
    // =========================================================================

    private <T extends IObject> T lookup( T argument )
    {
    		if ( argument == null ) return null;
    		if ( argument.getId() == null ) return argument;
    		return (T) iQuery.get(
    				argument.getClass(),
    				argument.getId());
    }
    
	@SuppressWarnings("unchecked")
	private Pixels copyPixels(Pixels pixels)
    {
    	if (pixels == null) return null;
    	Pixels newPixels = new ShallowCopy().copy(pixels);
    	newPixels.setChannels(copyChannels(pixels.getChannels()));
    	newPixels.setPixelsDimensions(
    			new ShallowCopy().copy(pixels.getPixelsDimensions()));
    	newPixels.setPixelsType(
    			new ShallowCopy().copy(pixels.getPixelsType()));
    	return newPixels;
    }
    
    private List<Channel> copyChannels(List<Channel> channels)
    {
    	List<Channel> newChannels = new ArrayList<Channel>();
    	for (Channel c : channels)
    		newChannels.add(copyChannel(c));
    	return newChannels;
    }
    
    private Channel copyChannel(Channel channel)
    {
    	Channel newChannel = new ShallowCopy().copy(channel);
    	newChannel.setLogicalChannel(
    			new ShallowCopy().copy(channel.getLogicalChannel()));
    	newChannel.setStatsInfo(
    			new ShallowCopy().copy(channel.getStatsInfo()));
    	return newChannel;
    }
    
    private RenderingModel copyRenderingModel(RenderingModel model)
    {
        if (model == null) return null;
        RenderingModel newModel = new RenderingModel();
        newModel.setId( model.getId() );
        newModel.setValue( model.getValue() );
        newModel.setDetails( model.getDetails() == null ? null :
            model.getDetails().shallowCopy() );
        return newModel;
    }

    private Family copyFamily(Family family)
    {
        if (family == null) return null;
        Family newFamily = new Family( );
        newFamily.setId( family.getId() );
        newFamily.setValue( family.getValue() );
        newFamily.setDetails( 
                family.getDetails() == null ? null : 
                    family.getDetails().shallowCopy() );
        return newFamily;
    }

}

