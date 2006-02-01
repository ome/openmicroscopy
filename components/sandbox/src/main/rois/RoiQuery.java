package main.rois;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.sql.RowSet;

import org.hibernate.Query;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;

import ome.model.core.Pixels;
import ome.model.meta.Event;
import ome.model.meta.Experimenter;
import ome.model.roi.Roi5D;
import ome.model.roi.RoiExtent;
import ome.model.roi.RoiMap;
import ome.model.roi.RoiSet;

import sun.security.krb5.internal.crypto.h;
import utils.OmeroSupport;

public class RoiQuery extends OmeroSupport
{

    public static void main(final String[] args) throws Exception
    {
        new RoiQuery().run();
    }

    public void run()
    {

        String q;
        Map params = new HashMap();
        List l;

        /*
         * For the biggest roiset, get a list of all pixels that are attached.
         */
        q = " select p from RoiSet s " + " left outer join s.roiMaps m "
                + " left outer join m.roi5d r "
                + " left outer join r.pixels p " + " where s.id = :id ";

        params.put("id", new Integer(getSetWithMostMaps()));

        l = _q.queryListMap(q, params);
        System.out.println("Pixels count:" + l.size());

        /*
         * For some pixel in that list, find all roisets that contain it.
         */
        int which = new Random().nextInt(l.size());
        int id = ((Pixels) l.get(which)).getId().intValue();
        q = " select s from RoiSet s " + " left outer join s.roiMaps m "
                + " left outer join m.roi5d r "
                + " left outer join r.pixels p " + " where p.id = :id ";
        params.put("id", new Integer(id));

        l = _q.queryListMap(q, params);
        System.out.println("Set count:" + l.size());

    }

    private int getSetWithMostMaps()
    {
        SqlRowSet rs = jt.queryForRowSet("select m.roiset, count(m.roiset) "
                + "from roiset as s, roimap as m " + "where s.id = m.roiset "
                + "group by m.roiset " + "order by count(m.roiset) desc "
                + "limit 1", null);

        rs.first();
        final int s_id = rs.getInt("roiset");
        final int s_count = rs.getInt("count");
        return s_id;
    }
}
