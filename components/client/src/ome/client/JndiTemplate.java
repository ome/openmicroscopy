/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc.. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.client;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * Simple subclass of {@link org.springframework.jndi.JndiTemplate} to override
 * minimal needed methods.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @version 3.0 <small> (<b>Internal version:</b> $Revision$ $Date$) </small>
 * @since 3.0-Beta3.1
 */
public class JndiTemplate extends org.springframework.jndi.JndiTemplate {

    /**
     * Sole constructor. Requires a {@link Properties} which should contain the
     * {@link ome.system.Principal} instance. This is something of a WORKAROUND
     * via the very broken {@link Properties} class.
     * 
     * @param p
     */
    public JndiTemplate(Properties p) {
        super(p);
    }

    /**
     * Starting with Spring 2.5.5, the
     * {@link org.springframework.jndi.JndiTemplate#createInitialContext()}
     * method reads only the String-valued properties out of the environment. We
     * need to pass a Principal instance.
     */
    @Override
    protected Context createInitialContext() throws NamingException {
        return new InitialContext(getEnvironment());
    }

}
