package utils;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.access.BeanFactoryLocator;
import org.springframework.beans.factory.access.SingletonBeanFactoryLocator;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.transaction.TransactionStatus;

import ome.api.IQuery;
import ome.api.IUpdate;
import ome.model.enums.EventType;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.security.CurrentDetails;

public class OmeroSupport
{

    protected ApplicationContext        ctx;

    protected JdbcTemplate              jt;

    protected IQuery                    _q;

    protected IUpdate                   _u;

    private HibernateTransactionManager _tm;

    private SessionFactory              _sf;

    private Session                     _s;

    protected TransactionStatus         ts;
    
    public OmeroSupport()
    {
        BeanFactoryLocator bfl = SingletonBeanFactoryLocator.getInstance();
        ctx = (ApplicationContext) bfl.useBeanFactory("ome").getFactory();
        jt = (JdbcTemplate) ctx.getBean("jdbcTemplate");
        _tm = (HibernateTransactionManager) ctx.getBean("transactionManager");
        _sf = (SessionFactory) ctx.getBean("sessionFactory");
        _q = (IQuery) ctx.getBean("queryService");
        _u = (IUpdate) ctx.getBean("updateService");

        ts = _tm.getTransaction(null);
        _s = SessionFactoryUtils.getSession(_sf, true);

        Experimenter exp = (Experimenter) _q.getById(Experimenter.class, 0);
        ExperimenterGroup grp = (ExperimenterGroup) _q.getById(
                ExperimenterGroup.class, 0);
        EventType type = (EventType) _q.getUniqueByFieldEq(EventType.class,
                "value", "Test");

        CurrentDetails.setOwner(exp);
        CurrentDetails.setGroup(grp);
        CurrentDetails.newEvent(type);

    }

    protected void commit()
    {
        _tm.commit(ts);
    }
    
}
