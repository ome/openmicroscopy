/*
 * org.openmicroscopy.shoola.env.data.DataServicesTestCase
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

package org.openmicroscopy.shoola.env.data;


//Java imports

//Third-party libraries
import junit.framework.TestCase;

//Application-internal dependencies
import org.openmicroscopy.shoola.env.Container;
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.login.LoginService;
import org.openmicroscopy.shoola.env.data.login.UserCredentials;
import org.openmicroscopy.shoola.env.init.AgentsInit;
import org.openmicroscopy.shoola.env.init.BatchLoginServiceInit;
import org.openmicroscopy.shoola.env.init.CmdProcessorInit;
import org.openmicroscopy.shoola.env.init.ContainerConfigInit;
import org.openmicroscopy.shoola.env.init.DataServicesInit;
import org.openmicroscopy.shoola.env.init.DataServicesTestsInit;
import org.openmicroscopy.shoola.env.init.FakeAgentsInit;
import org.openmicroscopy.shoola.env.init.FakeCmdProcessorInit;
import org.openmicroscopy.shoola.env.init.FakeInitializer;
import org.openmicroscopy.shoola.env.init.FakeLoggerInit;
import org.openmicroscopy.shoola.env.init.FakeTaskBarInit;
import org.openmicroscopy.shoola.env.init.FakeUserNotifierInit;
import org.openmicroscopy.shoola.env.init.LoggerInit;
import org.openmicroscopy.shoola.env.init.LoginServiceInit;
import org.openmicroscopy.shoola.env.init.NullContainerConfigInit;
import org.openmicroscopy.shoola.env.init.NullSplashScreenInit;
import org.openmicroscopy.shoola.env.init.SplashScreenInit;
import org.openmicroscopy.shoola.env.init.TaskBarInit;
import org.openmicroscopy.shoola.env.init.UserNotifierInit;

/** 
 * Ancestor of all Data Services test cases.
 * A Data Service test case verifies the operation of any of the Data Services
 * made available by the Container: Data Management Service, Semantic Types
 * Service, Pixels Service, and Data Services Views. 
 * <p>This class conveniently inherits from <code>TestCase</code> so that a 
 * Data Services test case can be run by JUnit.  The JUnit's hook method 
 * {@link #setUp() setUp} is overridden to factor out the functionality common
 * to all test cases &#151; so sublcasses should first call <code>super.setUp()
 * </code> if they override this method too.</p>
 * <p>Altough you write a Data Services test case as a regular JUnit test case,
 * there are several things to keep in mind, regarding the test environment:</p>
 * <ul>
 *  <li><b>OMEDS</b>.  A live instance of <i>OMEDS</i> is needed by the 
 *   underlying Data Services.  So a Data Services test case depends on
 *   <i>external</i> resources.</li>
 *  <li><b>OMEDS Connnection</b>.  The connection to the server is managed
 *   transparently by the Login Service, which operates in test mode &#151;
 *   see {@link org.openmicroscopy.shoola.env.data.login.BatchLoginService}.
 *   (So no UI is ever brought on screen to ask for user's credentials, which
 *   are specified through system properties &#151; along with the server's
 *   address, see {@link Env}.)  The <i>connection</i> is used for all tests  
 *   in a session.  Note that if a test fails because of an invalid link to 
 *   <i>OMEDS</i>, the Login Service will try reestablishing a valid link under
 *   the hood and so the next test may run just fine.  However, if a vaild link
 *   can't be reestablished (for example, a permanent network failure) then all
 *   subsequent tests will obviously fail.</li>
 *  <li><b>Container</b>.  It's started in test mode and the only available
 *   services will be the Data Services.  (All other services are replaced
 *   with no-op services.)  In particular, no UI is linked to the Container and
 *   thus no Agents either.  The Container instance is static and spans an 
 *   entire test session &#151; which, in most cases, is the same as the JVM
 *   process life-time.  You can access the Container's registry directly by
 *   using the protected field made available by this class.</li>
 *  <li><b>Threading</b>.  The Container and all its services are run in a 
 *   single thread, which is the same as the one in which JUnit is run.
 *   (Even the Data Services Views use this thread.)  So a Data Services test
 *   case is single-threaded &#151; unless you explicitly spawn other threads,
 *   which we reccommend you <i>don't</i> do.</li>
 * </ul>
 * <p>Finally some guidelines to run Data Services test cases.  First off, you
 * should set up a test <i>OMEDS</i> instance, which in turn runs on a test DB.
 * This is the server that you're then going to use for the Data Services tests.
 * Second, run all the Data Services tests together in a separate JVM process.
 * This is a good idea because of the dependency on external resources and
 * the singleton instance of the Container (and its services) which is shared
 * among all tests in a session.</p>
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
public abstract class DataServicesTestCase
    extends TestCase
{

    /** The singleton Container shrared among all test cases. */
    private static Container    container;
    
    
    /**
     * Reconfigures the Container's initialization procedure so to replace
     * the services we don't need with no-op services and those that we need
     * with test services.
     */
    private static void reconfigureInitProc()
    {   
        //Don't load container.xml; we'll configure the registry entries that
        //we need manually instead in the DataServicesTestsInit.
        FakeInitializer.replaceInitTask(ContainerConfigInit.class,
                                        NullContainerConfigInit.class);
        
        //Iinitialize Data Services so that they connect to the server address
        //Env and use a SyncBatchCallMonitor in asynchronous calls.
        FakeInitializer.replaceInitTask(DataServicesInit.class,
                                        DataServicesTestsInit.class);
                
        //Get rid of the default Login Service and replace it with one 
        //suitable for testing.  (No user interaction, no UI.)
        FakeInitializer.replaceInitTask(LoginServiceInit.class, 
                                        BatchLoginServiceInit.class);
        
        //Disable multi-threading.
        FakeInitializer.replaceInitTask(CmdProcessorInit.class, 
                                        FakeCmdProcessorInit.class);
        
        //Disable logging.
        FakeInitializer.replaceInitTask(LoggerInit.class, 
                                        FakeLoggerInit.class);
        
        //Get rid of all UI-related services and components.
        FakeInitializer.replaceInitTask(SplashScreenInit.class,
                                        NullSplashScreenInit.class);
        FakeInitializer.replaceInitTask(UserNotifierInit.class,
                                        FakeUserNotifierInit.class);
        FakeInitializer.replaceInitTask(TaskBarInit.class, 
                                        FakeTaskBarInit.class);
        FakeInitializer.replaceInitTask(AgentsInit.class, FakeAgentsInit.class);
        
        //In conclusion, we're only going to have the following services:
        // * Data Services: Initialized to connect to the address found in
        //                  Env and to run in the JUnit thread.
        // * Login Service: Initialized to operate in batch mode.
        // * Event Bus: It has no external dependencies, so we keep it as is.
    }
    
    /**
     * Initializes the test environment the first time it is called; does
     * nothing thereafter.
     */
    private static void ensureInit() 
    {
        if (container != null) return;  //Already intialized.

        //Create the Container.
        reconfigureInitProc();
        container = Container.startupInTestMode("");  //home is irrelevant here.
        
        //Now we're ready to log onto OMEDS for the first time.
        UserCredentials uc = new UserCredentials(Env.getOmeroUser(), 
                                                 Env.getOmeroPass(),
                                                 Env.getOmeroHost());
        Registry reg = container.getRegistry();
        LoginService loginSvc = (LoginService) reg.lookup(LookupNames.LOGIN);
        loginSvc.login(uc);
    }
    
    
    /**
     * Reference to the Container's registry.
     * Subclasses mainly use this to get references to the various
     * Data Services. 
     */
    protected Registry      registry;
    
    
    /**
     * Makes sure the Container and its services are correctly initialized
     * for testing.
     */
    protected void setUp() 
    { 
        ensureInit();
        registry = container.getRegistry();
    }
    
}
