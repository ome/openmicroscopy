import omero.api.ServiceFactoryPrx;
import omero.model.Project;
import java.util.List;

public class Main {

    public static void main(String args[]) throws Exception{

        String host = null, user = null, pass = null;
        try {
            host = args[0];
            user = args[1];
            pass = args[2];
        } catch (Exception e) {
            Usage.usage();
        }

        omero.client client = new omero.client(host);
        ServiceFactoryPrx factory = client.createSession(user, pass);
        List<Project> projects = AllProjects.getProjects(factory.getQueryService(), user);
        PrintProjects.print(projects);

    }

}
