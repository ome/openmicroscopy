/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package omero;

import java.io.File;
import java.util.Properties;

import omero.api.ServiceFactoryPrx;
import omero.api.ServiceFactoryPrxHelper;
import omero.util.ObjectFactoryRegistrar;
import Glacier2.CannotCreateSessionException;
import Glacier2.PermissionDeniedException;

/**
 * Central client-side blitz entry point. This class uses solely Ice
 * functionality to provide access to blitz (as opposed to also using Spring)
 * and should be in sync with the OmeroPy omero.client class as well as the
 * OmeroCpp omero::client class.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta3
 */
public class client {

    final Ice.Communicator ic;

    ServiceFactoryPrx sf;

    public client(String[] args) {
        ic = Ice.Util.initialize(args);
        if (null == ic) {
            throw new ClientError("Improper initialization");
        }
        init();
    }

    public client(String iceConfig) {
        ic = Ice.Util.initialize(new String[] { "--Ice.Config=" + iceConfig });
        init();
    }

    public client(File... files) {
        this(filesToString(files));
    }

    public client(Properties p) {
        Ice.InitializationData id = new Ice.InitializationData();
        for (Object key : p.keySet()) {
            id.properties.setProperty(key.toString(), p.get(key).toString());
        }
        ic = Ice.Util.initialize(id);
        init();
    }

    protected static String filesToString(File... files) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < files.length; i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(files[i].getAbsolutePath());
        }
        return sb.toString();
    }

    private void init() {
        if (ic == null) {
            throw new ClientError("No communicator.");
        }
        ObjectFactoryRegistrar.registerObjectFactory(ic,
                ObjectFactoryRegistrar.INSTANCE);
    }

    public client(String name, String password) {
        final Ice.InitializationData id = new Ice.InitializationData();
        id.properties.setProperty(omero.constants.USERNAME.value, name);
        id.properties.setProperty(omero.constants.PASSWORD.value, password);
        this.ic = Ice.Util.initialize(id);
    }

    public Ice.Communicator getCommunicator() {
        return ic;
    }

    public ServiceFactoryPrx getServiceFactory() {
        return sf;
    }

    public Ice.Properties getProperties() {
        return this.ic.getProperties();
    }

    public String getProperty(String key) {
        return getProperties().getProperty(key);
    }

    public ServiceFactoryPrx createSession()
            throws CannotCreateSessionException, PermissionDeniedException {
        return createSession(null, null);
    }

    public ServiceFactoryPrx createSession(String username, String password)
            throws CannotCreateSessionException, PermissionDeniedException {
        if (username == null) {
            username = getProperty("omero.user");
            if (username == null) {
                throw new ClientError("No username specified");
            }
        }
        if (password == null) {
            password = getProperty("omero.pass");
            if (password == null) {
                throw new ClientError("No password specified");
            }
        }

        Glacier2.SessionPrx prx = getRouter().createSession(username, password);
        if (null == prx) {
            throw new ClientError("No session obtained");
        }
        sf = ServiceFactoryPrxHelper.checkedCast(prx);
        if (sf == null) {
            throw new ClientError("No session obtained");
        }
        return this.sf;
    }

    public Glacier2.RouterPrx getRouter() {
        Ice.RouterPrx prx = ic.getDefaultRouter();
        if (prx == null) {
            throw new ClientError("No default router found.");
        }

        Glacier2.RouterPrx router = Glacier2.RouterPrxHelper.checkedCast(prx);
        if (router == null) {
            throw new ClientError("Error obtaining Glacier2 router");
        }
        return router;
    }

    public void closeSession() {
        if (this.sf == null) {
            return; // EARLY EXIT
        }

        try {
            sf.close();
        } catch (Exception e) {
            // what can we do
        }

        try {
            getRouter().destroySession();
        } catch (Glacier2.SessionNotExistException snee) {
            // ok. We don't want it to exist
        } catch (Ice.ConnectionLostException cle) {
            // ok. Exception will always be thrown
        }
    }

    public Object getInput(String key) {
        throw new UnsupportedOperationException();
    }

    public Object getOutput(String key) {
        throw new UnsupportedOperationException();
    }

    public void setInput(String key, Object value) {
        throw new UnsupportedOperationException();
    }

    public void setOutput(String key, Object value) {
        throw new UnsupportedOperationException();
    }
}
