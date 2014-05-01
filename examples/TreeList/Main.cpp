#include <omero/client.h>
#include "Usage.h"
#include "AllProjects.h"
#include "PrintProjects.h"

int main(int argc, char* argv[]) {

    std::string host, port, user, pass;
    try {
        host = argv[0];
        port = argv[1];
        user = argv[2];
        pass = argv[3];
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
