import java.util.List;
import omero.model.Project;
import omero.api.IQueryPrx;
import omero.sys.ParametersI;
import static omero.rtypes.*;

public class AllProjects {

    public static List<Project> getProjects(IQueryPrx query, String username) throws Exception {

        List rv = query.findAllByQuery(
            "select p from Project p where p.details.owner.name = :name",
            new ParametersI().add("name", rstring(username)));
        return (List<Project>) rv;

    }

}
