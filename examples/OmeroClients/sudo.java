import java.util.List;

import omero.api.IAdminPrx;
import omero.api.ISessionPrx;
import omero.api.ServiceFactoryPrx;
import omero.model.Session;
import omero.sys.Principal;

public class sudo {

    public static void main(String args[]) throws Exception {

        omero.client client = new omero.client();
        ServiceFactoryPrx sf = client.createSession("root", "ome");
        ISessionPrx sessionSvc = sf.getSessionService();

        Principal p = new Principal();
        p.name = "user";
        p.group = "user";
        p.eventType = "User";

        Session sudoSession = sessionSvc.createSessionWithTimeout( p, 3*60*1000L ); // 3 minutes to live

        omero.client sudoClient = new omero.client();
        ServiceFactoryPrx sudoSf = sudoClient.joinSession( sudoSession.getUuid().getValue() );
        IAdminPrx sudoAdminSvc = sudoSf.getAdminService();
        System.out.println( sudoAdminSvc.getEventContext().userName );

        sudoClient.closeSession();
        client.closeSession();
    }

}
