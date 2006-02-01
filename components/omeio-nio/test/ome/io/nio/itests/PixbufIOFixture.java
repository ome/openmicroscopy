/*
 * ome.io.nio.itests.PixbufIOFixture
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2005 Open Microscopy Environment
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
package ome.io.nio.itests;

import java.io.IOException;

import org.springframework.beans.factory.access.BeanFactoryLocator;
import org.springframework.beans.factory.access.SingletonBeanFactoryLocator;
import org.springframework.context.ApplicationContext;

import ome.api.IUpdate;
import ome.api.local.LocalUpdate;
import ome.model.core.Pixels;


/**
 * @author callan
 *
 */
public class PixbufIOFixture
{
    private ApplicationContext ctx;

    private IUpdate updater;

    private Pixels pixels;

    private void init()
    {
        BeanFactoryLocator bfl = SingletonBeanFactoryLocator.getInstance();
        ctx = (ApplicationContext) bfl.useBeanFactory("ome");
        updater = (IUpdate) ctx.getBean("updateService");
    }
    
    private void createPixels()
    {
        Pixels p = new Pixels();
        p.setSizeX(new Integer(64));
        p.setSizeY(new Integer(64));
        p.setSizeZ(new Integer(16));
        p.setSizeC(new Integer(2));
        p.setSizeT(new Integer(10));
        // FIXME: Bit of a hack until the model is updated, the follwing
        // is a SHA1 of "pixels"
        p.setSha1("09bc7b2dcc9a510f4ab3a40c47f7a4cb77954356");
        
        pixels = (Pixels) updater.saveAndReturnObject(p);

    }
    
    public Pixels setUp() throws IOException
    {
        init();
        createPixels();
        return pixels;
    }
    
    protected void tearDown()
    {
        ((LocalUpdate) updater).rollback();

    }
}
