/*
 * ome.server.dbtests.AbstractDbUnitTest
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
package ome.server.dbtests;

//Java imports
import javax.sql.DataSource;


//Third-party libraries
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.operation.DatabaseOperation;
import org.springframework.context.ApplicationContext;

//Application-internal dependencies
import ome.server.itests.*;

/**
 * @author josh
 * @DEV.TODO test "valid=false" sections of queries
 */
public abstract class AbstractDbUnitTest extends AbstractInternalContextTest {

    protected static IDatabaseConnection c = null;
    protected ApplicationContext ctx;
    protected DataSource ds = null;
    
    public static void main(String[] args) {
        junit.textui.TestRunner.run(AbstractDbUnitTest.class);
    }

        @Override
        protected void onSetUpInTransaction() throws Exception
        {

            if (null==c) {
            try {
                c = new DatabaseConnection(ds.getConnection());
                DatabaseOperation.CLEAN_INSERT.execute(c,getData());
            } catch (Exception e){
                c = null;
                throw e;
            }
        }

    }
    
    public abstract IDataSet getData() throws Exception;
    
}
