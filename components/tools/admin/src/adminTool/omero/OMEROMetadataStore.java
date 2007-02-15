/*
 * ome.formats.OMEROMetadataStore
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package src.adminTool.omero;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.ejb.EJBAccessException;
import javax.swing.Timer;

import ome.api.IAdmin;
import ome.api.IQuery;
import ome.api.IUpdate;
import ome.api.RawFileStore;
import ome.api.RawPixelsStore;
import ome.model.core.Pixels;
import ome.model.meta.Experimenter;
import ome.system.Login;
import ome.system.Server;
import ome.system.ServiceFactory;

/**
 * An OMERO metadata store. This particular metadata store requires the user to
 * be logged into OMERO prior to use with the {@link #login()} method. NOTE: All
 * indexes are ignored by this metadata store as they don't make much real
 * sense.
 * 
 * @author Brian W. Loranger brain at lifesci.dundee.ac.uk
 * @author Chris Allan callan at blackcat.ca
 */
public class OMEROMetadataStore {

    /** OMERO service factory; all other services are retrieved from here. */
    private ServiceFactory sf;

    /** OMERO raw pixels service */
    private RawPixelsStore pservice;

    /** OMERO query service */
    private IQuery iQuery;

    /** OMERO update service */
    private IUpdate iUpdate;

    /** OMERO admin service. */
    private IAdmin iAdmin;

    /** The "root" pixels object */
    private Pixels pixels = new Pixels();

    private Experimenter exp;

    private RawFileStore rawFileStore;

    private Timer timeOutTimer;
    
    /**
     * Creates a new instance.
     * 
     * @param username
     *            the username to use to login to the OMERO server.
     * @param password
     *            the password to use to login to the OMERO server.
     * @param host
     *            the hostname of the OMERO server.
     * @param port
     *            the port the OMERO server is listening on.
     */
    public OMEROMetadataStore(String username, String password, String host,
            String port) {
        // Mask the password information for display in the debug window
    		
        String maskedPswd = "";
        if (password == null) {
            password = new String("");
        }
        if (password.length() > 0) {
            maskedPswd = "<" + password.length() + "chars>";
        } else {
            maskedPswd = "<empty>";
        }

        // Attempt to log in
        Server server = new Server(host, Integer.parseInt(port));
       
        Login login = new Login(username, password);
        // Instantiate our service factory
        sf = new ServiceFactory(server, login);

        // Now initialize all our services
        iAdmin = sf.getAdminService();

        iQuery = sf.getQueryService();
        iUpdate = sf.getUpdateService();
        pservice = sf.createRawPixelsStore();
        rawFileStore = sf.createRawFileStore();

        exp = iQuery.findByString(Experimenter.class, "omeName", username);
    }

    public IAdmin getAdminService() {
        return iAdmin;
    }

    public IQuery getQueryService() {
        return iQuery;
    }

    public IUpdate getUpdateService() {
        return iUpdate;
    }
}
