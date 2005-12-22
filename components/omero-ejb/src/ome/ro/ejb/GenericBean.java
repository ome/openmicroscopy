package ome.ro.ejb;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import javax.ejb.PreDestroy;
import javax.ejb.Remote;
import javax.ejb.Stateless;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.beans.factory.access.BeanFactoryLocator;
import org.springframework.beans.factory.access.BeanFactoryReference;
import org.springframework.beans.factory.access.SingletonBeanFactoryLocator;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;

import ome.model.core.Pixels;

@Stateless
@Remote(Generic.class)
public class GenericBean implements Generic
{

    BeanFactoryLocator   bfl = SingletonBeanFactoryLocator.getInstance();

    BeanFactoryReference bf  = bfl.useBeanFactory("ome");

    protected HibernateTemplate getTemplate()
    {
        return (HibernateTemplate) bf.getFactory().getBean("hibernateTemplate");
    }

    public Object[] run(String query)
    {
        List l = getTemplate().find(query);
        //new ProxyC
        return Collections.singletonList(new Pixels()).toArray();
    }

    public void persist(final Object[] objects)
    {

        getTemplate().execute(new HibernateCallback()
        {

            public Object doInHibernate(Session session)
                    throws HibernateException, SQLException
            {
                for (int i = 0; i < objects.length; i++)
                {
                    session.save(objects[i]);
                }
                session.flush();
                return null;
            }
        });

    }

    @PreDestroy
    public void destroy()
    {
        bf.release();
    }

}
