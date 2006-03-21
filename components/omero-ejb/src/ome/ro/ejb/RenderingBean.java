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
import java.io.IOException;

import javax.ejb.Local;
import javax.ejb.PreDestroy;
import javax.ejb.Remote;
import javax.ejb.Stateful;

// Third-party libraries
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;

// Application-internal dependencies
import ome.model.display.QuantumDef;

import omeis.providers.re.RGBBuffer;
import omeis.providers.re.RenderingEngine;
import omeis.providers.re.Renderer;
import omeis.providers.re.codomain.CodomainMapContext;
import omeis.providers.re.data.PlaneDef;
import omeis.providers.re.quantum.QuantizationException;


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
@Local(RenderingEngine.class)
public class RenderingBean extends AbstractBean implements RenderingEngine
{

    private RenderingEngine delegate;
    
    /*
     * CREATION:
     */
    public  RenderingBean()
    {
        super();
        delegate = (RenderingEngine) applicationContext.getBean("renderService");
    }

    @PreDestroy
    public void destroy()
    {
        delegate = null;
        super.destroy();
    }
    
    // ~ DELEGATION
    // =========================================================================
    
    public void addCodomainMap(CodomainMapContext arg0)
    {
        delegate.addCodomainMap(arg0);
    }

    public double getChannelCurveCoefficient(int arg0)
    {
        return delegate.getChannelCurveCoefficient(arg0);
    }

    public int getChannelFamily(int arg0)
    {
        return delegate.getChannelFamily(arg0);
    }

    public boolean getChannelNoiseReduction(int arg0)
    {
        return delegate.getChannelNoiseReduction(arg0);
    }

    public double[] getChannelStats(int arg0)
    {
        return delegate.getChannelStats(arg0);
    }

    public double getChannelWindowEnd(int arg0)
    {
        return delegate.getChannelWindowEnd(arg0);
    }

    public double getChannelWindowStart(int arg0)
    {
        return delegate.getChannelWindowStart(arg0);
    }

    public int getDefaultT()
    {
        return delegate.getDefaultT();
    }

    public int getDefaultZ()
    {
        return delegate.getDefaultZ();
    }

    public int getModel()
    {
        return delegate.getModel();
    }

    public QuantumDef getQuantumDef()
    {
        return delegate.getQuantumDef();
    }

    public int[] getRGBA(int arg0)
    {
        return delegate.getRGBA(arg0);
    }

    public boolean isActive(int arg0)
    {
        return delegate.isActive(arg0);
    }

    public void load()
    {
        delegate.load();
    }

    public void lookupPixels(long arg0)
    {
        delegate.lookupPixels(arg0);
    }

    public void lookupRenderingDef(long arg0)
    {
        delegate.lookupRenderingDef(arg0);
    }

    public void removeCodomainMap(CodomainMapContext arg0)
    {
        delegate.removeCodomainMap(arg0);
    }

    public RGBBuffer render(PlaneDef arg0) throws IOException, QuantizationException
    {
        return delegate.render(arg0);
    }

    public void resetDefaults()
    {
        delegate.resetDefaults();
    }

    public void saveCurrentSettings()
    {
        delegate.saveCurrentSettings();
    }

    public void selfConfigure()
    {
        delegate.selfConfigure();
    }

    public void setActive(int arg0, boolean arg1)
    {
        delegate.setActive(arg0, arg1);
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
    {
        delegate.setApplicationContext(applicationContext);
    }

    public void setChannelWindow(int arg0, double arg1, double arg2)
    {
        delegate.setChannelWindow(arg0, arg1, arg2);
    }

    public void setCodomainInterval(int arg0, int arg1)
    {
        delegate.setCodomainInterval(arg0, arg1);
    }

    public void setDefaultT(int arg0)
    {
        delegate.setDefaultT(arg0);
    }

    public void setDefaultZ(int arg0)
    {
        delegate.setDefaultZ(arg0);
    }

    public void setModel(int arg0)
    {
        delegate.setModel(arg0);
    }

    public void setQuantizationMap(int arg0, int arg1, double arg2, boolean arg3)
    {
        delegate.setQuantizationMap(arg0, arg1, arg2, arg3);
    }

    public void setQuantumStrategy(int arg0)
    {
        delegate.setQuantumStrategy(arg0);
    }

    public void setRGBA(int arg0, int arg1, int arg2, int arg3, int arg4)
    {
        delegate.setRGBA(arg0, arg1, arg2, arg3, arg4);
    }

    public void updateCodomainMap(CodomainMapContext arg0)
    {
        delegate.updateCodomainMap(arg0);
    }

    public int getSizeX()
    {
        return delegate.getSizeX();
    }

    public int getSizeY()
    {
        return delegate.getSizeY();
    }
    
    
}
