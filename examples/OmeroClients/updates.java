import static omero.rtypes.*;

import omero.api.ServiceFactoryPrx;
import omero.api.IUpdatePrx;
import omero.model.Image;
import omero.model.ImageI;

public class updates {

    public static void main(String args[]) throws Exception {

        omero.client client = new omero.client(args);
	try {

            Image i = new ImageI();
            i.setName( rstring("name") );

            ServiceFactoryPrx sf = client.createSession();
            IUpdatePrx u = sf.getUpdateService();
            i = (Image) u.saveAndReturnObject( i );

        } finally {
            client.closeSession();
	}
    }

}
