/*
 * ome.tools.spring.PostProcessInjector
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.tools.spring;

//Java imports

//Third-party imports
import org.springframework.beans.factory.config.BeanPostProcessor;

//Application-internal dependencies
import ome.security.basic.BasicSecuritySystem;
import ome.tools.hibernate.ExtendedMetadata;

/** catch all {@link BeanPostProcessor} which handles cyclical references.
 */
public class PostProcessInjector
{
    public PostProcessInjector(BasicSecuritySystem sys, ExtendedMetadata meta) 
    {
    	sys.setExtendedMetadata( meta );
	}
    
}

