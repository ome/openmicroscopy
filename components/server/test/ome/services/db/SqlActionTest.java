/*
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ome.model.core.Image;
import ome.model.core.OriginalFile;
import ome.model.core.Pixels;
import ome.model.meta.Experimenter;
import ome.parameters.Parameters;
import ome.server.itests.AbstractManagedContextTest;
import ome.testing.ObjectFactory;
import ome.util.SqlAction;

import org.hibernate.SessionFactory;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.metadata.ClassMetadata;
import org.springframework.dao.DataAccessException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Tests the JDBC-based array methods of {@link PgArrayHelper}
 */
@Test(groups = "integration")
public class SqlActionTest extends AbstractManagedContextTest {

    String key;
    String value;
    OriginalFile f;
    Pixels p;
    SqlAction sql;

    @BeforeMethod
    public void setup() {
        sql = (SqlAction) applicationContext.getBean("simpleSqlAction");
        f = makefile();
        p = makepixels();
        key = uuid();
        value = uuid();
    }

    @Test
    public void testEmptyPasswordSetting() {
        final Experimenter e = loginNewUser();
        final String n = e.getOmeName();
        loginRoot();
        iAdmin.changeUserPassword(n, "");
        assertTrue(iAdmin.checkPassword(n, "anything", false));
        assertTrue(iAdmin.checkPassword(n, "", false));
        assertTrue(iAdmin.checkPassword(n, null, false));

        iAdmin.changeUserPassword(n, "ome");
        assertTrue(iAdmin.checkPassword(n, "ome", false));
        assertFalse(iAdmin.checkPassword(n, "", false));
        assertFalse(iAdmin.checkPassword(n, null, false));

        iAdmin.changeUserPassword(n, null);
        assertFalse(iAdmin.checkPassword(n, "ome", false));
        assertFalse(iAdmin.checkPassword(n, "", false));
        assertFalse(iAdmin.checkPassword(n, null, false));

    }

    @Test
    public void testGetCurrentEventLog() {
        sql.setCurrentEventLog(1, "test.log");
        sql.selectCurrentEventLog("test.log");
    }

    @Test(groups = "ticket:3886", expectedExceptions = DataAccessException.class)
    public void testUnknownSequenceGet() {
        sql.nextValue("This_is_unknown", 50);
    }

    /**
     * Walks through all model object classes, and any that have a generator
     * annotation:
     * <pre>
     * &#64;org.hibernate.annotations.GenericGenerator(name = "seq_lightpathemissionfilterlink",
     *  strategy = "ome.util.TableIdGenerator",
     *  parameters = {
     *      &#64;org.hibernate.annotations.Parameter(name = "table_name", value = "seq_table"),
     *      &#64;org.hibernate.annotations.Parameter(name = "segment_value", value = "seq_lightpathemissionfilterlink"),
     *      &#64;org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled"),
     *      &#64;org.hibernate.annotations.Parameter(name = "increment_size", value = "50")
     *  })
     * </pre>
     * will be tested.
     */
    @SuppressWarnings("unchecked")
    @Test(groups = "ticket:3886")
    public void testGetEachSequenceOnce() throws Exception {

        final SessionFactory factory =
            applicationContext.getBean("sessionFactory", SessionFactory.class);

        final Map<String, ClassMetadata> m = factory.getAllClassMetadata();

        for (String seq : m.keySet()) {
            Class<?> k = Class.forName(seq);
            GenericGenerator gg = k.getAnnotation(GenericGenerator.class);
            if (gg == null) {
                continue;
            }
            String segment_value = null;
            for (Parameter p : gg.parameters()) {
                if (p.name().equals("segment_value")) {
                    segment_value = p.value();
                    break;
                }
            }
            assertNotNull(segment_value);
            sql.nextValue(segment_value, 1);
        }
    }

    @Test(groups = "ticket:3961")
    public void testLargeInClause() throws Exception {

        final String query = "select i from Image i where i.id in (:ids)";
        final List<Long> ids = new ArrayList<Long>();
        for (int i = 0; i < 1001; i++) {
            ids.add(Long.valueOf(i));
        }

        super.setUp();
        Parameters p = new Parameters();
        p.addList("ids", ids);

        iQuery.findAllByQuery(query, p);

    }

    //
    // HELPERS
    //

    private OriginalFile makefile() {
        OriginalFile f = new OriginalFile();
        f.setName("name");
        f.setPath("path");
        f = iUpdate.saveAndReturnObject(f);
        return f;
    }

    private Pixels makepixels() {
        Pixels p = ObjectFactory.createPixelGraph(null);
        Image i = iUpdate.saveAndReturnObject(p.getImage());
        p = i.getPrimaryPixels();
        return p;
    }

}
