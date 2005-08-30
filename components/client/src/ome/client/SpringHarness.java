/*
 * ome.client.SpringHarness
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

package ome.client;

//Java imports

//Third-party libraries

import java.net.URL;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.core.io.Resource;

//Application-internal dependencies



/** provides context for all objects configured by the Spring Framework.
 * 
 * @author  Josh Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 
 * @DEV.TODO Possibly deprecate SpringHarness, make internal to ServiceFactory.
 * <small>
 * (<b>Internal version:</b> $Rev$ $Date$)
 * </small>
 * @since 1.0
 */
public class SpringHarness {

    private final static String springConfFile = "ome/client/spring.xml";
    public static URL path;
    public static ApplicationContext ctx;
    
    static {
        path = SpringHarness.class.getClassLoader().getResource(springConfFile);
        if (path==null){
            throw new RuntimeException("Client jar corrupted. Can't find internal configuration file:\n"+springConfFile);
        }
        try {
			ctx = new FileSystemXmlApplicationContext(path.toString());
		} catch (Exception e) {
			throw new RuntimeException("Can't load file: "+path,e);
		}
    }
	
    
}
