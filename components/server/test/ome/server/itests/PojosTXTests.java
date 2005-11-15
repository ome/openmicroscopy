/*
 * ome.server.itests.PojosTXTest
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2005 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */
package ome.server.itests;

// Java imports
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

// Third-party libraries
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.test.AbstractTransactionalDataSourceSpringContextTests;

// Application-internal dependencies
import ome.api.Pojos;
import ome.dao.DaoFactory;
import ome.model.Dataset;
import ome.model.Experimenter;
import ome.model.Image;
import ome.model.Project;
import ome.security.Utils;
import ome.util.builders.PojoOptions;

/**
 * TXTests use the Spring AbstractTransactionalDataSourceSpringContextTests
 * superclass to insert data in the database and immediately query for that data
 * using the existing api.
 * 
 * @author Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 <small> (<b>Internal version:</b> $Rev$ $Date$) </small>
 * @since 2.0
 */
public class PojosTXTests
        extends AbstractTransactionalDataSourceSpringContextTests
{

    protected static Log log       = LogFactory.getLog(PojosTXTests.class);

    final static String  TEMPORARY = "TEMPORARY TEST ITEM";

    @Override
    protected void onSetUpInTransaction() throws Exception
    {
        Utils.setUserAuth();
    }

    /**
     * @see org.springframework.test.AbstractDependencyInjectionSpringContextTests#getConfigLocations()
     */
    protected String[] getConfigLocations()
    {

        return ConfigHelper.getConfigLocations();
    }

    DaoFactory daos;

    public void setDaoFactory(DaoFactory factory)
    {
        daos = factory;
    }

    Pojos psrv;

    public void setPojosService(Pojos service)
    {
        psrv = service;
    }

    Experimenter getValidOwner()
    {
        Experimenter e = (Experimenter) daos
                .generic()
                .queryUnique(
                        "select e from Experimenter e left outer join fetch e.moduleExecution where e.attributeId = 1",
                        null);
        return e;
    }

    public void testInitial()
    {
        UUID uuid = UUID.randomUUID();
        Project p = new Project();
        p.setName(uuid.toString());
        p.setDescription(TEMPORARY);
        p.setExperimenter(getValidOwner());
        assertNull(p.getProjectId());
        daos.generic().persist(new Object[] { p });
        assertNotNull(p.getProjectId());

        Project result = (Project) daos.generic().getUniqueByFieldEq(
                Project.class, "name", uuid.toString());
        assertTrue(result.getProjectId().equals(p.getProjectId()));
    }

    public void testLoadContainerReturnsNoLeaves()
    {
        UUID p_uuid = UUID.randomUUID();
        Project p = new Project();
        p.setName(p_uuid.toString());
        p.setDescription(TEMPORARY);
        p.setExperimenter(getValidOwner());

        UUID d_uuid = UUID.randomUUID();
        Dataset d = new Dataset();
        d.setName(d_uuid.toString());
        d.setDescription(TEMPORARY);
        d.setExperimenter(getValidOwner());
        d.setLocked(false);

        UUID i_uuid = UUID.randomUUID();
        Image i = new Image();
        i.setName(i_uuid.toString());
        i.setDescription(TEMPORARY);
        i.setExperimenter(getValidOwner());
        i.setInserted(new Date());
        i.setCreated(new Date());

        p.setDatasets(new HashSet(Arrays.asList(d)));
        d.setImages(new HashSet(Arrays.asList(i)));

        daos.generic().persist(new Object[] { p, d, i });
        
        Map options = new PojoOptions().noLeaves().map();
        Set ids = new HashSet(Arrays.asList(p.getProjectId()));

        Set<Project> s = psrv.loadContainerHierarchy(Project.class, ids,
                options);
        
        for (Project project : s)
        {
            for (Object o : project.getDatasets())
            {
                Dataset dataset = (Dataset) o;
                // FIXME assertNull(dataset.getImages());
            }
        }

    }

}
