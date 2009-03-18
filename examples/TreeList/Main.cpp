#include <omero/client.h>
#include <TreeList/Usage.h>
#include <TreeList/AllProjects.h>
#include <TreeList/PrintProjects.h>

int main(int argc, char* argv[]) {

    std::string host, user, pass;
    try {
        host = argv[0];
        user = argv[1];
        pass = argv[2];
    } catch (...) {
        Usage::usage();
    }

    omero::client client(argc, argv);
    int rc = 0;
    try {
        omero::api::ServiceFactoryPrx factory = client.createSession(user, pass);
        std::vector<omero::model::ProjectPtr> projects = AllProjects::getProjects(factory->getQueryService(), user);
        PrintProjects::print(projects);
    } catch (...) {
        client.closeSession();
    }
    return rc;

}
