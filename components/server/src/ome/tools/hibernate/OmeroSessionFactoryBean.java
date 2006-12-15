/*
 * ome.tools.hibernate.OmeroSessionFactoryBean
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.tools.hibernate;

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
