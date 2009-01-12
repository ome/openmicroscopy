/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.tools.spring;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NamingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Simple subclass of
 * {@link org.springframework.transaction.jta.JotmFactoryBean} which publishes
 * the {@link Jotm} to "UserTransaction" in JNDI
 */
public class JotmFactoryBean extends
        org.springframework.transaction.jta.JotmFactoryBean {

    private final static Log log = LogFactory.getLog(JotmFactoryBean.class);
    
    public JotmFactoryBean() throws NamingException {
        super();
        try {
            Context context = new InitialContext();
            context.bind("java:comp/UserTransaction", getObject());
            log.info("Bound UserTransaction in JNDI");
        } catch (NameAlreadyBoundException nabe) {
            log.info("UserTransaction already bound in JNDI");
        }
    }

}
