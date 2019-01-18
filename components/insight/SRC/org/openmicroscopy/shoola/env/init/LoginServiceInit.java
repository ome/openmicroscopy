/*
 * org.openmicroscopy.shoola.env.init.LoginServiceInit
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2019 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.env.init;

import java.security.Security;
import java.util.ArrayList;
import java.util.List;

import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.login.LoginManager;
import org.openmicroscopy.shoola.env.data.login.LoginServiceImpl;
import org.openmicroscopy.shoola.util.CommonsLangUtils;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;

/** 
 * This task initializes the 
 * {@link org.openmicroscopy.shoola.env.data.login.LoginService}.
 * The Container will use to manage the link to <i>OMEDS</i>.
 * This task has to be run after reading in the Container's configuration and
 * after initializing the Event Bus.
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
public final class LoginServiceInit
    extends InitializationTask
{

    /** Constructor required by superclass. */
    LoginServiceInit() {}
    
    /**
     * Returns the name of this task.
     * @see InitializationTask#getName()
     */
    String getName() { return "Initializing Login Service"; }

    /** 
     * Ensure that <q>anon</q> is not included among {@code jdk.tls.disabledAlgorithms}.
     * @see InitializationTask#configure()
     */
    void configure() {
        final String property = "jdk.tls.disabledAlgorithms";
        final String value = Security.getProperty(property);
        if (CommonsLangUtils.isNotBlank(value)) {
            final List<String> algorithms = new ArrayList<>();
            boolean isChanged = false;
            for (final String algorithm : Splitter.on(',').trimResults().split(value)) {
                if ("anon".equals(algorithm.toLowerCase())) {
                    isChanged = true;
                } else {
                    algorithms.add(algorithm);
                }
            }
            if (isChanged) {
                Security.setProperty(property, Joiner.on(", ").join(algorithms));
            }
        }
    }

    /**
     * Carries out this task.
     * @see InitializationTask#execute()
     */
    void execute()
    {
        LoginServiceImpl service = new LoginServiceImpl(container);
        LoginManager decorator = new LoginManager(service);
        Registry reg = container.getRegistry();
        reg.bind(LookupNames.LOGIN, decorator);
    }

    /** 
     * Does nothing.
     * @see InitializationTask#rollback()
     */
    void rollback() {}

}
