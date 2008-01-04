package ome.model.itests;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;
import org.springframework.util.ResourceUtils;

public class HibernateUtil {
    private static final Log log = LogFactory.getLog(HibernateUtil.class);
    private static final SessionFactory sessionFactory;
    static {
        try {
            File local = ResourceUtils.getFile("classpath:local.properties");
            File testCfg = ResourceUtils.getFile("classpath:hibernate.cfg.xml");
            Properties props = new Properties();
            props.load(new FileInputStream(local));
            AnnotationConfiguration cfg = new AnnotationConfiguration();
            cfg.setProperties(props);
            cfg.configure(testCfg);
            sessionFactory = cfg.buildSessionFactory();
        } catch (Throwable ex) {
            log.error(ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static Session getSession() throws HibernateException {
        return sessionFactory.openSession();
    }
}