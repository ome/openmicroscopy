import java.util.List;

import omero.api.ServiceFactoryPrx;
import omero.api.IQueryPrx;
import omero.model.IObject;
import omero.model.ImageI;
import omero.model.PixelsI;
import omero.util.ParametersI;

public class queries {

    public static void main(String args[]) throws Exception {

        omero.client client = new omero.client();
        ServiceFactoryPrx sf = client.createSession();
        IQueryPrx q = sf.getQueryService();

        String query_string = "select i from Image i where i.id = :id and name like :namedParameter";

        ParametersI p = new ParametersI();
        p.add("id", new omero.RLong(1L));
        p.add("namedParameter", new omero.RString("cell%mit%"));

        List<IObject> results = q.findAllByQuery(query_string, p);

    }

}
