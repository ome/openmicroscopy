/*
 * org.openmicroscopy.omero.logic.OmeroSessionFactoryBean
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

package org.openmicroscopy.omero.logic;

//Java imports

//Third-party libraries
import org.hibernate.HibernateException;
import org.hibernate.cfg.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.orm.hibernate3.LocalSessionFactoryBean;

//Application-internal dependencies



/** 
 * provides a cacheable Hibernate configuration to speed up development.
 * 
 * @author  Josh Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 
 * <small>
 * (<b>Internal version:</b> $Rev$ $Date$)
 * </small>
 * @since 1.0
 */
public class OmeroSessionFactoryBean extends LocalSessionFactoryBean{
 
    private Resource[] cachedMappingResources; 
    
    /**
     * Set Hibernate mapping resources to be found in the class path,
     * like "example.hbm.xml" or "mypackage/example.hbm.xml". Resources stored
     * as <b>cached</b>MappingResouces will use the Configuration.addCacheableFile
     * call.
     * @see org.hibernate.cfg.Configuration#addCacheableFile
     * @see org.springframework.orm.hibernate3.LocalSessionFactoryBean#setMappingResources
     */
    public void setCachedMappingResources(String[] mappingResources) {
            this.cachedMappingResources = new Resource[mappingResources.length];
            for (int i = 0; i < mappingResources.length; i++) {
                    this.cachedMappingResources[i] = new ClassPathResource(mappingResources[i].trim());
            }
    }
           
    /**
     * @see org.springframework.orm.hibernate3.LocalSessionFactoryBean#postProcessConfiguration(org.hibernate.cfg.Configuration)
     */
    protected void postProcessConfiguration(Configuration config)
            throws HibernateException {
        
        if (this.cachedMappingResources != null) {
            for (int i = 0; i < this.cachedMappingResources.length; i++) {
                    try {
                        config.addCacheableFile(this.cachedMappingResources[i].getFile());
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to load Hibernate mapping resource "+this.cachedMappingResources[i],e);
                    }
            }
        }
            
    }
     
}
