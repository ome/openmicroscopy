/*
 * ome.logic.AbstractLevel2Service
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

/*------------------------------------------------------------------------------
 *
 * Written by:    Josh Moore <josh.moore@gmx.de>
 *
 *------------------------------------------------------------------------------
 */

package ome.logic;

//Java imports

//Third-party libraries
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;

//Application-internal dependencies
import ome.api.ServiceInterface;
import ome.api.local.LocalQuery;
import ome.api.local.LocalUpdate;
import ome.security.SecuritySystem;
import ome.services.query.QueryFactory;
import ome.system.OmeroContext;
import ome.system.SelfConfigurableService;

/**
 * service level 2
 * 
 * @author Josh Moore, <a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0
 * <small>
 * (<b>Internal version:</b> $Rev$ $Date$)
 * </small>
 * @since OMERO 3.0
 */
public abstract class AbstractLevel2Service implements SelfConfigurableService{

    protected OmeroContext ctx;
    
    protected LocalUpdate iUpdate;
    
    protected LocalQuery iQuery;

    protected QueryFactory queryFactory;
    
    protected SecuritySystem securitySystem;
    
    protected abstract Class<? extends ServiceInterface> getServiceInterface();
    
    public void setUpdateService(LocalUpdate update)
    {
        this.iUpdate = update;
    }
    
    public void setQueryService(LocalQuery query)
    {
        this.iQuery = query;
    }

    public void setQueryFactory(QueryFactory factory)
    {
        this.queryFactory = factory;
    }
    
    public void setSecuritySystem(SecuritySystem securitySystem)
    {
    	this.securitySystem = securitySystem;
    }
    
    public void setApplicationContext(ApplicationContext applicationContext) 
        throws BeansException
    {
        this.ctx = (OmeroContext) applicationContext;
    }
    
    public void selfConfigure()
    {
        this.ctx = OmeroContext.getInternalServerContext();
        this.ctx.applyBeanPropertyValues(this,getServiceInterface());
    }

}

