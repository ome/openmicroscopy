package main.hibernate;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;

import ome.model.meta.Event;
import ome.model.meta.Experimenter;

import utils.OmeroSupport;

public class Pixels extends OmeroSupport
{

    public static void main(final String[] args) throws Exception
    {
        new Pixels();
    }

    public Pixels() throws Exception
    {

        ome.model.core.Pixels p = new ome.model.core.Pixels();
        p.setSizeX(new Integer(1));
        p.setSizeY(new Integer(1));
        p.setSizeZ(new Integer(1));
        p.setSizeC(new Integer(1));
        p.setSizeT(new Integer(1));
        p.setSha1("09bc7b2dcc9a510f4ab3a40c47f7a4cb77954356"); // "pixels"

        _u.saveObject(p);

    }

}
