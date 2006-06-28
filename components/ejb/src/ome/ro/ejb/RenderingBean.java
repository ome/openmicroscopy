/*
 * ome.ro.ejb.RenderingBean
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

package ome.ro.ejb;

// Java imports
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Local;
import javax.ejb.PostActivate;
import javax.ejb.PrePassivate;
import javax.ejb.Remote;
import javax.ejb.Stateful;
import javax.ejb.TransactionAttribute;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;

import static javax.ejb.TransactionAttributeType.*;

// Third-party libraries
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.ejb.RemoteBinding;
import org.jboss.annotation.security.SecurityDomain;
import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;

// Application-internal dependencies
import ome.conditions.InternalException;
import ome.conditions.RootException;
import ome.model.IObject;
import ome.model.core.Pixels;
import ome.model.display.QuantumDef;
import ome.model.enums.Family;
import ome.model.enums.RenderingModel;
import ome.model.internal.Details;
import ome.model.meta.Event;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.util.Filter;
import ome.util.Filterable;

import omeis.providers.re.RGBBuffer;
import omeis.providers.re.RenderingEngine;
import omeis.providers.re.Renderer;
import omeis.providers.re.codomain.CodomainMapContext;
import omeis.providers.re.data.PlaneDef;



/**
 * Provides the {@link RenderingEngine} service. This class is an Adapter to
 * wrap the {@link Renderer} so to make it thread-safe.
 * <p>
 * The multi-threaded design of this component is based on dynamic locking and
 * confinement techiniques. All access to the component's internal parts happens
 * through a <code>RenderingEngineImpl</code> object, which is fully
 * synchronized. Internal parts are either never leaked out or given away only
 * if read-only objects. (The only exception are the {@link CodomainMapContext}
 * objects which are not read-only but are copied upon every method invocation
 * so to maintain safety.)
 * </p>
 * <p>
 * Finally the {@link RenderingEngine} component doesn't make use of constructs
 * that could compromise liveness.
 * </p>
 * 
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author <br>
 *         Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:a.falconi@dundee.ac.uk"> a.falconi@dundee.ac.uk</a>
 * @version 2.2 <small> (<b>Internal version:</b> $Revision: 1.4 $ $Date:
 *          2005/07/05 16:13:52 $) </small>
 * @since OME2.2
 */
@Stateful
@Remote(RenderingEngine.class)
@RemoteBinding(jndiBinding="omero/remote/omeis.providers.re.RenderingEngine")
@Local(RenderingEngine.class)
@LocalBinding (jndiBinding="omero/local/omeis.providers.re.RenderingEngine")
@SecurityDomain("OmeroSecurity")
public class RenderingBean extends AbstractBean 
    implements RenderingEngine, Serializable
{

    private static final long serialVersionUID = -4383698215540637032L;

    private RenderingEngine delegate;

    @PostActivate
    @PostConstruct
    public void create()
    {
        super.create();
        delegate = (RenderingEngine) applicationContext.getBean(
                RenderingEngine.class.getName());
    }
    
    @AroundInvoke
    public Object invoke( InvocationContext context ) throws Exception
    {
        return wrap( context, "&renderService" );
    }
    
    @PrePassivate
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
    public void addCodomainMap(CodomainMapContext arg0)
    {
        delegate.addCodomainMap(arg0);
    }

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
        Pixels pix = delegate.getPixels();
        return (Pixels) new ShallowCopy().copy(pix);
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

    @RolesAllowed("user") 
    public void removeCodomainMap(CodomainMapContext arg0)
    {
        delegate.removeCodomainMap(arg0);
    }

    @RolesAllowed("user") 
    public RGBBuffer render(PlaneDef arg0)
    {
        return delegate.render(arg0);
    }

    @RolesAllowed("user") 
    public void resetDefaults()
    {
        delegate.resetDefaults();
    }

    @RolesAllowed("user") 
    public void saveCurrentSettings()
    {
        delegate.saveCurrentSettings();
    }

    @RolesAllowed("user") 
    public void selfConfigure()
    {
        delegate.selfConfigure();
    }

    @RolesAllowed("user") 
    public void setActive(int arg0, boolean arg1)
    {
        delegate.setActive(arg0, arg1);
    }

    @RolesAllowed("user") 
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
    {
        delegate.setApplicationContext(applicationContext);
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
        delegate.setModel(arg0);
    }

    @RolesAllowed("user") 
    public void setQuantizationMap(int arg0, Family arg1, double arg2, boolean arg3)
    {
        delegate.setQuantizationMap(arg0, arg1, arg2, arg3);
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

    @RolesAllowed("user") 
    public void updateCodomainMap(CodomainMapContext arg0)
    {
        delegate.updateCodomainMap(arg0);
    }

    // ~ Helpers
    // =========================================================================

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

    private static class ShallowCopy {

        public Filterable copy(Filterable original)
        {
            Filterable f = original.newInstance();
            StoreValues store = new StoreValues();
            SetValues set = new SetValues(store);
            f.acceptFilter(store);
            f.acceptFilter(set);
            return f;
        }
        
    }    

    private static class SetValues implements Filter {

        private StoreValues store;
        
        public SetValues(StoreValues store)
        {
            this.store = store;
        }
        
        public Filterable filter(String fieldId, Filterable f)
        {
            return (Filterable) store.values.get(fieldId);  
        }

        public Collection filter(String fieldId, Collection c)
        {
            return (Collection) store.values.get(fieldId);        
        }

        public Map filter(String fieldId, Map m)
        {
            return (Map) store.values.get(fieldId);
        }

        public Object filter(String fieldId, Object o)
        {
            return store.values.get(fieldId);
        }
    
    }

    private static class StoreValues implements Filter {

        public Map values = new HashMap();
        
        public Filterable filter(String fieldId, Filterable f)
        {
            if (f==null) return null;
            if ( Details.class.isAssignableFrom( f.getClass() ))
            {
                values.put(fieldId, ((Details)f).shallowCopy());
            } 
            
            else if (IObject.class.isAssignableFrom( f.getClass() ))
            {
                IObject old = (IObject) f;
                IObject iobj = (IObject) f.newInstance();
                iobj.setId( old.getId() );
                iobj.unload();
                values.put(fieldId,iobj);
            } 
            
            else 
            {
                throw new InternalException(
                        "Unknown filterable type:"+f.getClass());
            }
            
            return f;
            
        }

        public Collection filter(String fieldId, Collection c)
        {
            values.put(fieldId,null);
            return c;
        }

        public Map filter(String fieldId, Map m)
        {
            values.put(fieldId,null);
            return m;
        }

        public Object filter(String fieldId, Object o)
        {
            if (o == null) 
            {
                values.put(fieldId,null);
            }
            values.put(fieldId,o);
            return o;
        }
        
    }
    
}
