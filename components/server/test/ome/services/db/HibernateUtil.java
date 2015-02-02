/*
 *   Copyright (C) 2010-2011 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.services.db;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.engine.spi.FilterDefinition;
import org.springframework.util.ResourceUtils;

public class HibernateUtil {
    private static final Logger log = LoggerFactory.getLogger(HibernateUtil.class);
    private static final SessionFactory sessionFactory;
    static {
        try {
            File local = ResourceUtils.getFile("classpath:local.properties");
            File testCfg = ResourceUtils.getFile("classpath:hibernate.cfg.xml");
            File mockFilter = ResourceUtils
                    .getFile("classpath:mock_filters.hbm.xml");
            Properties props = new Properties();
            props.load(new FileInputStream(local));
            AnnotationConfiguration cfg = new AnnotationConfiguration();
            cfg.addFilterDefinition(new FilterDefinition("securityFilter",
                    "1=1", new HashMap()));
            cfg.configure(testCfg);
            cfg.setProperties(props);
            sessionFactory = cfg.buildSessionFactory();
        } catch (Throwable ex) {
            log.error(ex.toString());
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static Session getSession() throws HibernateException {
        return sessionFactory.openSession();
    }
}
