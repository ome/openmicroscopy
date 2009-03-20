import omero.api.ServiceFactoryPrx;
import omero.model.Project;
import java.util.List;

public class Main {

    public static void main(String args[]) throws Exception{

        String host = null, port = null, user = null, pass = null;
        try {
            host = args[0];
            port = args[1];
            user = args[2];
            pass = args[3];
        } catch (Exception e) {
            Usage.usage();
        }

        omero.client client = new omero.client(args);
	try {
            ServiceFactoryPrx factory = client.createSession(user, pass);
            List<Project> projects = AllProjects.getProjects(factory.getQueryService(), user);
            PrintProjects.print(projects);
	} finally {
	    client.closeSession();
	}

    }

}
