import java.util.List;


import omero.api.ServiceFactoryPrx;
import omero.api.IQueryPrx;
import omero.model.IObject;
import omero.sys.ParametersI;

public class queries {

    public static void main(String args[]) throws Exception {

        omero.client client = new omero.client(args);
	try {
            ServiceFactoryPrx sf = client.createSession();
            IQueryPrx q = sf.getQueryService();

            String query_string = "select i from Image i where i.id = :id and name like :namedParameter";

            ParametersI p = new ParametersI();
            p.add("id", omero.rtypes.rlong(1L));
            p.add("namedParameter", omero.rtypes.rstring("cell%mit%"));

            List<IObject> results = q.findAllByQuery(query_string, p);
        } finally {
            client.closeSession();
	}
    }

}
