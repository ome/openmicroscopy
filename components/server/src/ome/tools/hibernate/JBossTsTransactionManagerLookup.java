//
// No license. Copied from http://docs.codehaus.org/display/BTM/Hibernate
//
package ome.tools.hibernate;

import java.util.Map;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.transaction.TransactionManagerLookup;

/**
 * Simple helper class copied from
 * http://docs.codehaus.org/display/BTM/Hibernate This implementation is
 * available in Hibernate 3.3 at which point this can be removed.
 */
public class JBossTsTransactionManagerLookup implements
        TransactionManagerLookup {

    private final static Log log = LogFactory
            .getLog(JBossTsTransactionManagerLookup.class);

    private final static String utName = "UserTransaction";

    private final static String tmName = "TransactionManager";

    /**
     * Default constructor used by Hibernate.
     */
    public JBossTsTransactionManagerLookup() {

    }

    /**
     * Constructor used to publish a {@link UserTransaction} to JNDI.
     * 
     * @param ds
     */
    public JBossTsTransactionManagerLookup(TransactionManager tm,
            UserTransaction ut, Map<String, Object> map) {
        Context ctx = null;
        try {
            ctx = new InitialContext();
            ctx.bind(tmName, tm);
            ctx.bind(utName, ut);
            if (map != null) {
                for (String key : map.keySet()) {
                    ctx.bind(key, map.get(key));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to create and bind pooled JTA resources", e);
        } finally {
            cleanup(ctx);
        }
    }

    public TransactionManager getTransactionManager(Properties props)
            throws HibernateException {
        Context ctx = null;
        try {
            ctx = new InitialContext();
            return (TransactionManager) ctx.lookup(tmName);
        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to lookup transaction manager from JNDI", e);
        } finally {
            cleanup(ctx);
        }
    }

    private void cleanup(Context ctx) {
        if (ctx != null) {
            try {
                ctx.close();
            } catch (Exception e) {
                log.warn("Error closing JNDI context", e);
            }
        }
    }

    public String getUserTransactionName() {
        return utName;
    }

}