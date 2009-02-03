//
// No license. Copied from http://docs.codehaus.org/display/BTM/Hibernate
//
package ome.tools.hibernate;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

import org.hibernate.HibernateException;
import org.hibernate.transaction.TransactionManagerLookup;

import bitronix.tm.TransactionManagerServices;
import bitronix.tm.resource.jdbc.PoolingDataSource;

/**
 * Simple helper class copied from
 * http://docs.codehaus.org/display/BTM/Hibernate This implementation is
 * available in Hibernate 3.3 at which point this can be removed.
 */
public class BitronixTransactionManagerLookup implements
        TransactionManagerLookup {

    private final static String utName = "UserTransaction";

    /**
     * Default constructor used by Hibernate.
     */
    public BitronixTransactionManagerLookup() {

    }

    /**
     * Constructor used to publish a {@link PoolingDataSource} to JNDI.
     * 
     * @param ds
     */
    public BitronixTransactionManagerLookup(PoolingDataSource ds,
            UserTransaction ut) {
        try {
            Context ctx = new InitialContext();
            ctx.bind(utName, ut);
            ctx.bind(ds.getUniqueName(), ds);
        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to create and bind pooled JTA resources", e);
        }
    }

    public TransactionManager getTransactionManager(Properties props)
            throws HibernateException {
        return TransactionManagerServices.getTransactionManager();
    }

    public String getUserTransactionName() {
        return utName;
    }

}