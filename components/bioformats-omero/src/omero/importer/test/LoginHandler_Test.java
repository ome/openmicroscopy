package omero.importer.test;

import java.io.IOException;

import javax.swing.UIManager;

import omero.importer.engine.IObservable;
import omero.importer.engine.IObserver;
import omero.importer.engine.LoginHandler;
import omero.importer.engine.OMEROMetadataStore;

public class LoginHandler_Test
implements IObserver
{
    LoginHandler loginHandler = new LoginHandler();
    
    LoginHandler_Test() throws IOException
    {
        String username = "brian";
        String password = "ome";
        String port = "1099";
        String server = "valewalker.openmicroscopy.org.uk";
        
        loginHandler.addObserver(this);
        loginHandler.login(username, password, port, server);
    }

    void printRepositorySpace()
    {
        OMEROMetadataStore store = loginHandler.getMetadataStore();
        System.err.println("Repository Space: " + store.getRepositorySpace());
    }
    /**
     * @param args
     */
    public static void main(String[] args)
    {
        String laf = UIManager.getSystemLookAndFeelClassName() ;
        //laf = "com.sun.java.swing.plaf.windows.WindowsLookAndFeel";
        //laf = "com.sun.java.swing.plaf.gtk.GTKLookAndFeel";
        //laf = "com.sun.java.swing.plaf.motif.MotifLookAndFeel";
        //laf = "javax.swing.plaf.metal.MetalLookAndFeel";
        
        try {
            UIManager.setLookAndFeel(laf);
        } catch (Exception e) 
        { System.err.println(laf + " not supported."); }
        
        try {
            new LoginHandler_Test();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void update(IObservable observable, Object message)
    {        
        System.err.println(message);
        if (observable instanceof LoginHandler)
        {
            switch ((LoginHandler.Message) message)
            {
                case CONNECTED:
                    printRepositorySpace();
                    break;
                case AUTH_ERROR:
                    break;
                case URL_ERROR:
                    break;                
                case UNKNOWN_ERROR:
                    break;
            }
            if (message != LoginHandler.Message.CONNECTING)
                java.lang.System.exit(0);
        }
    }
}
