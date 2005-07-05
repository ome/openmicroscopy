/*
 * Created on Jun 29, 2005
*/
package org.openmicroscopy.omero.logic.dynamic;

import org.hibernate.cfg.Configuration;
import org.springframework.context.ApplicationContext;
import org.springframework.orm.hibernate3.LocalSessionFactoryBean;

/** at this point, either BuildRunner or CodeGeneration have 
 * placed well-formed *.class and *.hbm.xml files in our WEB-INF
 * now we need to get them and replace sessionFactory.
 * @author josh
 */
public class SessionUpdater {

    /* Stefan Frank in private email:

Ok, this is doable without shutting down anything, if you either get the
SessionFactory via JNDI, or acquire it using spring: 

- dynamically create a new class
- compile the class using javac.compile(args);
- create a <class>.hbm.xml (or just add annotations to the class)
- get a handle to the new class with Class clazz = Class.forName(classname);
- make up a new SessionFactory, reading in all old and... 
- ... add the new class to the configuration via
cfg.addAnnotatedClass(clazz);
- call cfg.buildSessionFactory(); again?
- the call to buildSessionFactory makes all necessary updates to the db
- replace the old SessionFactory with the new one in jndi/spring
- old sessions can still run without the new class until they are committed
- new clients get their Sessions from the new SessionFActory, that already
supports the new class. 

You see, it's not much of a deal:)

- jnlp or at least some url-classloader would be useful for the clients, to
make sure they get the proper classes shipped. 

The hibernate-folks say it's doable this way, although noone has done this
yet.

Cheers
stf
     */
    
    /**
     * possibly best to move from all config in spring to using
     * a hibernate.cfg.xml references from Hibernate. (DataSource there)
     * ...time for JNDI??? 
     * this lets tools recreate the hibernate.cfg.xml 
     * --waiting a bit on hibernate tools
     * --middlegen (or just hibernate)
     * --??
     */
    public void run(){
        ApplicationContext ctx = null;
        LocalSessionFactoryBean lsfb = new LocalSessionFactoryBean();
//        ctx.
//			http://opensource.atlassian.com/projects/hibernate/browse/HBX-36
//        LocalSessionFactoryBean lsfbr = LocalSessionFactoryBean) ctx.getBean("&sessionFactory");
//        SessionFactoryImpl sfbr = (SessionFactoryImpl) ctx.getBean("sessionFactory");
        Configuration cfg = lsfb.getConfiguration();
    }
    
}
