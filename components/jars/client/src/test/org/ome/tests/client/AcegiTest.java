/* Copyright 2004, 2005 Acegi Technology Pty Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ome.tests.client;

import net.sf.acegisecurity.Authentication;
import net.sf.acegisecurity.context.ContextHolder;
import net.sf.acegisecurity.context.security.SecureContext;
import net.sf.acegisecurity.context.security.SecureContextImpl;
import net.sf.acegisecurity.providers.UsernamePasswordAuthenticationToken;

import org.ome.interfaces.ImageService;
import org.ome.model.IImage;
import org.ome.model.LSID;
import org.ome.model.Vocabulary;
import org.springframework.beans.factory.ListableBeanFactory;

import org.springframework.util.StopWatch;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;


/**
 * Demonstrates accessing the {@link ContactManager} via remoting protocols.
 * 
 * <P>
 * Based on Spring's JPetStore sample, written by Juergen Hoeller.
 * </p>
 *
 * @author Ben Alex
 */
public class AcegiTest extends TestCase{
    //~ Instance fields ========================================================

    
    
    //~ Methods ================================================================

    public void invokeImageService(Authentication authentication,
        int nrOfCalls) throws URISyntaxException {
        StopWatch stopWatch = new StopWatch(nrOfCalls
                + " ContactManager call(s)");
        Map contactServices = SpringTestHarness.ctx.getBeansOfType(ImageService.class,
                true, true);

        SecureContext secureContext = new SecureContextImpl();
        secureContext.setAuthentication(authentication);
        ContextHolder.setContext(secureContext);

        for (Iterator it = contactServices.keySet().iterator(); it.hasNext();) {
            String beanName = (String) it.next();

            Object object = SpringTestHarness.ctx.getBean("&" + beanName);

            try {
                System.out.println(
                    "Trying to find setUsername(String) method on: "
                    + object.getClass().getName());

                Method method = object.getClass().getMethod("setUsername",
                        new Class[] {String.class});
                System.out.println("Found; Trying to setUsername(String) to "
                    + authentication.getPrincipal());
                method.invoke(object,
                    new Object[] {authentication.getPrincipal()});
            } catch (NoSuchMethodException ignored) {
                System.out.println(
                    "This client proxy factory does not have a setUsername(String) method");
            } catch (IllegalAccessException ignored) {
                ignored.printStackTrace();
            } catch (InvocationTargetException ignored) {
                ignored.printStackTrace();
            }

            try {
                System.out.println(
                    "Trying to find setPassword(String) method on: "
                    + object.getClass().getName());

                Method method = object.getClass().getMethod("setPassword",
                        new Class[] {String.class});
                method.invoke(object,
                    new Object[] {authentication.getCredentials()});
                System.out.println("Found; Trying to setPassword(String) to "
                    + authentication.getCredentials());
            } catch (NoSuchMethodException ignored) {
                System.out.println(
                    "This client proxy factory does not have a setPassword(String) method");
            } catch (IllegalAccessException ignored) {}
            catch (InvocationTargetException ignored) {}

            ImageService remoteContactManager = (ImageService) contactServices
                .get(beanName);
            System.out.println("Calling ContactManager '" + beanName + "'");

            stopWatch.start(beanName);

            List contacts = new ArrayList();

            for (int i = 0; i < nrOfCalls; i++) {
                contacts.add(remoteContactManager.retrieveImage(new LSID(Vocabulary.NS+"img"+i)));
            }

            stopWatch.stop();

            if (contacts.size() != 0) {
                Iterator listIterator = contacts.iterator();

                while (listIterator.hasNext()) {
                    IImage contact = (IImage) listIterator.next();
                    System.out.println("Contact: " + contact.toString());
                }
            } else {
                System.out.println(
                    "No contacts found which this user has permission to");
            }

            System.out.println();
            System.out.println(stopWatch.prettyPrint());
        }

        ContextHolder.setContext(null);
    }

    public void testAcegi() throws URISyntaxException {
        String username = "Josh";
        String password = "Moore";
        int nrOfCalls = 4;
        invokeImageService(new UsernamePasswordAuthenticationToken(
                    username, password), nrOfCalls);
            
        }
}
