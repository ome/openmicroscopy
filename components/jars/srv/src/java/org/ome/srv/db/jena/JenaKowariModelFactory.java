/*
 * Created on Feb 28, 2005
 */
package org.ome.srv.db.jena;

import java.io.File;
import java.net.URI;
import java.net.URL;

import org.kowari.server.Session;
import org.kowari.server.SessionFactory;
import org.kowari.server.driver.SessionFactoryFinder;
import org.kowari.store.Database;
import org.kowari.store.DatabaseSession;
import org.kowari.store.jena.GraphKowariMaker;
import org.kowari.store.jena.ModelKowariMaker;
import org.kowari.store.xa.XADatabaseImpl;

import com.hp.hpl.jena.rdf.model.ModelMaker;
import com.hp.hpl.jena.shared.ReificationStyle;

/**
 * @author josh
 */
public class JenaKowariModelFactory extends JenaModelFactory {

    protected Database database;

    protected DatabaseSession session;

    protected URI serverUri;

    protected File serverDir;

    /**
     * @return
     */
    public ModelMaker getMaker() {
        boolean exceptionOccurred = true;
        if (null == maker) {
            try {
                //serverDir.mkdirs();
                //removeContents(serverDir);
                //database = new XADatabaseImpl(serverUri, serverDir);
                // session = (DatabaseSession) database.newSession();
                 SessionFactory sessionFactory = SessionFactoryFinder.newSessionFactory(serverUri, true);
                session = (DatabaseSession) sessionFactory.newSession();
                exceptionOccurred = false;

                maker = new ModelKowariMaker(
                        new GraphKowariMaker(session, database.getURI(), ReificationStyle.Standard));
            } catch (Exception e) {
                throw new RuntimeException("Can't create JenaKowariMaker", e);
            } finally {
//                if (exceptionOccurred && (database != null)) {
//                    database.close();
//                }
//                if ((exceptionOccurred) && (session != null)) {
//                    session.close();
//                }
            }
        }
        return maker;
    }

    private static void removeContents(File dir) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; ++i) {
                if (files[i].isFile()) {
                    files[i].delete();
                }
            }
        }
    }
    /**
     * @return Returns the serverURI.
     */
    public URI getServerUri() {
        return serverUri;
    }
    /**
     * @param serverURI The serverURI to set.
     */
    public void setServerUri(URI serverURI) {
        this.serverUri = serverURI;
    }
    /**
     * @return Returns the session.
     */
    public DatabaseSession getSession() {
        return session;
    }
    /**
     * @param session The session to set.
     */
    public void setSession(DatabaseSession session) {
        this.session = session;
    }
    /**
     * @return Returns the serverDir.
     */
    public File getServerDir() {
        return serverDir;
    }
    /**
     * @param serverDir The serverDir to set.
     */
    public void setServerDir(File serverDir) {
        this.serverDir = serverDir;
    }
}