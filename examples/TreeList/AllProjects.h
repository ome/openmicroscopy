#include <string>
#include <omero/API.h>
#include <omero/System.h>
#include <omero/Collections.h>
#include <omero/templates.h>
#include <omero/model/Project.h>

struct AllProjects {

    static std::vector<omero::model::ProjectPtr> getProjects(omero::api::IQueryPrx query, std::string username) {
        omero::api::IObjectList rv = query->findAllByQuery(
            "select p from Project p where p.details.owner.name = :name", (omero::sys::ParametersPtr) 0);
            // FIXME new ParametersI().add("name", new omero::RString(username)));
        return omero::cast<omero::model::ProjectPtr>(rv);

    }

};
