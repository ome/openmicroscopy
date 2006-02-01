package utils;

import org.springframework.beans.factory.access.BeanFactoryLocator;
import org.springframework.beans.factory.access.SingletonBeanFactoryLocator;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;

import ome.api.IQuery;
import ome.api.IUpdate;
import ome.model.meta.Event;
import ome.model.meta.Experimenter;

public class OmeroSupport
{

    protected ApplicationContext ctx;

    protected JdbcTemplate       jt;

    protected IQuery             _q;

    protected IUpdate            _u;

    public OmeroSupport()
    {
        BeanFactoryLocator bfl = SingletonBeanFactoryLocator.getInstance();
        ctx = (ApplicationContext) bfl.useBeanFactory("ome").getFactory();
        jt = (JdbcTemplate) ctx.getBean("jdbcTemplate");
        _q = (IQuery) ctx.getBean("queryService");
        _u = (IUpdate) ctx.getBean("updateService");
    }

}
