/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.tools.spring;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * Simple subclass of
 * {@link org.springframework.transaction.jta.JotmFactoryBean} which publishes
 * the {@link Jotm} to "UserTransaction" in JNDI
 */
public class JotmFactoryBean extends
        org.springframework.transaction.jta.JotmFactoryBean {

    public JotmFactoryBean() throws NamingException {
        super();
        Context context = new InitialContext();
        context.bind("java:comp/UserTransaction", getObject());
    }

}
