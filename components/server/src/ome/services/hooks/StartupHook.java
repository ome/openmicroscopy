/*
 * ome.services.hooks.StartupHook
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

package ome.services.hooks;

//Java imports

//Third-party libraries
import org.hibernate.SessionFactory;
import org.jboss.annotation.ejb.Service;
import org.jboss.annotation.ejb.Management;

//Application-internal dependencies
import ome.annotations.RevisionDate;
import ome.annotations.RevisionNumber;
import ome.system.OmeroContext;

/** 
 * Hook run after all the application has been deployed to the server. At that
 * point, it can be guaranteed that the Omero classes are available and so 
 * attempting to connect to the database "internally" should work.
 * 
 * @author  Josh Moore, josh.moore at gmx.de
 * @version $Revision$, $Date$
 * @since   3.0-Beta1
 * @see <a href="https://trac.openmicroscopy.org.uk/omero/ticket/444">ticket:444</a>
 */
@RevisionDate("$Date$")
@RevisionNumber("$Revision$")
@Service (objectName="omero:service=StartupHook")
@Management(Startup.class)
public class StartupHook implements Startup {

    OmeroContext ctx = OmeroContext.getManagedServerContext();
    SessionFactory sf = (SessionFactory) ctx.getBean("sessionFactory");
    
    /**
     * Attempts twice to connect to the server to overcome any initial
     * difficulties.
     * 
     * @see <a href="https://trac.openmicroscopy.org.uk/omero/ticket/444">ticket:444</a>
     */
    public void start() throws Exception
    {
		System.out.println("Starting Omero...");

		try {
			connect();
		} catch (Exception e) {
		    // ok
		}

		connect();
		System.out.println("Ready.");
    }

    /**
     * Attempts a simple database query.
     */
    protected void connect()
    {
		sf.openSession().createQuery("select count(*) from Experimenter");    	
    }
    
}
