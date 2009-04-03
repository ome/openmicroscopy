/*
 * org.openmicroscopy.shoola.env.ui.TestTaskBarStartup
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

package org.openmicroscopy.shoola.env.ui;


//Java imports

//Third-party libraries
import junit.framework.TestCase;

//Application-internal dependencies
import org.openmicroscopy.shoola.env.Container;
import org.openmicroscopy.shoola.env.init.AgentsInit;
import org.openmicroscopy.shoola.env.init.ContainerConfigInit;
import org.openmicroscopy.shoola.env.init.DataServicesInit;
import org.openmicroscopy.shoola.env.init.FakeAgentsInit;
import org.openmicroscopy.shoola.env.init.FakeDataServicesInit;
import org.openmicroscopy.shoola.env.init.FakeInitializer;
import org.openmicroscopy.shoola.env.init.FakeLoggerInit;
import org.openmicroscopy.shoola.env.init.FakeLoginServiceInit;
import org.openmicroscopy.shoola.env.init.FakeTaskBarInit;
import org.openmicroscopy.shoola.env.init.LoggerInit;
import org.openmicroscopy.shoola.env.init.LoginServiceInit;
import org.openmicroscopy.shoola.env.init.NullContainerConfigInit;
import org.openmicroscopy.shoola.env.init.NullSplashScreenInit;
import org.openmicroscopy.shoola.env.init.SplashScreenInit;
import org.openmicroscopy.shoola.env.init.TaskBarInit;

/** 
 * Verifies that the {@link TaskBar} is displayed on screen at the end of the
 * {@link org.openmicroscopy.shoola.env.Container}'s initialization procedure.
 * The {@link org.openmicroscopy.shoola.env.Container} is set to operate with
 * Null services and a {@link org.openmicroscopy.shoola.env.ui.MockTaskBar}.
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
public class TestTaskBarStartup
    extends TestCase
{
    
    //Mock object used to verify that the TaskBar::open method is invoked
    //at the end of the startup procedure.
    private MockTaskBar     taskBar;
    
    
    protected void setUp()
    {
        //Create the mock.
        taskBar = new MockTaskBar();
        
        //Replace the initialization tasks that create services which depend
        //on external resources with tasks that create no-op services instead.
        FakeInitializer.replaceInitTask(AgentsInit.class, FakeAgentsInit.class);
        FakeInitializer.replaceInitTask(LoginServiceInit.class, 
                                        FakeLoginServiceInit.class);
        FakeInitializer.replaceInitTask(DataServicesInit.class, 
                                        FakeDataServicesInit.class);
        FakeInitializer.replaceInitTask(LoggerInit.class, 
                                        FakeLoggerInit.class);
        FakeInitializer.replaceInitTask(TaskBarInit.class, 
                                        FakeTaskBarInit.class);
        FakeInitializer.replaceInitTask(ContainerConfigInit.class,
                                        NullContainerConfigInit.class);
        FakeInitializer.replaceInitTask(SplashScreenInit.class,
                                        NullSplashScreenInit.class);
        
        //All FakeXXXInit classes will use, by default, Null objects to
        //replace services.  It is possible to modify this default behavior
        //by specifying a different service implementation to the static
        //fields in those classes.  In our case, we need a MockTaskBar
        //insead of a NullTaskBar.
        FakeTaskBarInit.taskBar = taskBar;
    }
    
    public void test()
    {
        //Set up expexted calls on the mock.
        taskBar.open();
        
        //Transition the mock to verification mode.
        taskBar.activate();
        
        //Test.  Note that we don't start the container with the same
        //method invoked in main (startup method).  This avoids threads
        //headaches.
        Container.startupInTestMode("");  //Root directory taken as home.
        
        //Make sure all expected calls were performed.
        taskBar.verify();
    }
    
}
