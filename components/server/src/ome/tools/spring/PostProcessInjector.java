/* ome.tools.spring.PostProcessInjector
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

package ome.tools.spring;

//Java imports

//Third-party imports
import org.springframework.beans.factory.config.BeanPostProcessor;

//Application-internal dependencies
import ome.security.BasicSecuritySystem;
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

