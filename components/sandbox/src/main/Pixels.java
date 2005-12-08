package main;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;

import ome.model.meta.Event;
import ome.model.meta.Experimenter;

public class Pixels
{

    private ApplicationContext ctx;

    private HibernateTemplate  ht;

    public static void main(final String[] args) throws Exception
    {
        new Pixels();
    }

    public Pixels() throws Exception
    {

        String[] paths = new String[] { "config.xml", "data.xml",
                "hibernate.xml" };
        ctx = new ClassPathXmlApplicationContext(paths);

        ht = (HibernateTemplate) ctx.getBean("hibernateTemplate");

        ht.execute(new HibernateCallback()
        {

            public Object doInHibernate(org.hibernate.Session session)
                    throws org.hibernate.HibernateException,
                    java.sql.SQLException
            {

                Experimenter o = (Experimenter) session.get(Experimenter.class, new Integer(1));
                if (o == null)
                {
                    o = new Experimenter();
                    o.setOmeName("test");
                    session.save(o);
                }   
                
                Event e = (Event) session.get(Event.class, new Integer(1));
                if (e == null)
                {
                    e = new Event();
                    e.setName("test");
                    session.save(e);
                }   
                
                ome.model.core.Pixels p = new ome.model.core.Pixels();
                p.setSizeX(new Integer(1));
                p.setSizeY(new Integer(1));
                p.setSizeZ(new Integer(1));
                p.setSizeC(new Integer(1));
                p.setSizeT(new Integer(1));
                p.setBigEndian(Boolean.TRUE);
                
                p.setCreationEvent(e);
                p.setOwner(o);

                
                session.save(p);
                return null;
            };
        });

    }

}
