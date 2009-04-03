/*
 * org.openmicroscopy.shoola.env.init.FakeLoginServiceInit
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

package org.openmicroscopy.shoola.env.init;


//Java imports

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.login.LoginService;
import org.openmicroscopy.shoola.env.data.login.NullLoginService;

/** 
 * Fake initialization task.
 * Binds the Container's registry to the {@link LoginService} specified by its
 * static field.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class FakeLoginServiceInit
    extends InitializationTask
{

    /**
     * Default Null service, does nothing.
     * Change it to whatever implementation is required by your tests.
     */
    public static LoginService loginService = new NullLoginService();
 
    
    /**
     * Constructor required by superclass.
     */
    FakeLoginServiceInit() {}
    
    /**
     * Returns the name of this task.
     * @see InitializationTask#getName()
     */
    String getName() { return "Initializing Login Service"; }

    /** 
     * Does nothing, as this task requires no set up.
     * @see InitializationTask#configure()
     */
    void configure() {}

    /**
     * Carries out this task.
     * @see InitializationTask#execute()
     */
    void execute()
    {
        Registry reg = container.getRegistry();
        reg.bind(LookupNames.LOGIN, loginService);
    }

    /** 
     * Does nothing.
     * @see InitializationTask#rollback()
     */
    void rollback() {}
}
