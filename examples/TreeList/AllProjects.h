#include <string>
#include <omero/API.h>
#include <omero/api/IQuery.h>
#include <omero/System.h>
#include <omero/RTypesI.h>
#include <omero/Collections.h>
#include <omero/templates.h>
#include <omero/model/Project.h>
#include <omero/sys/ParametersI.h>
using namespace omero::rtypes;
struct AllProjects {

    static std::vector<omero::model::ProjectPtr> getProjects(omero::api::IQueryPrx query, std::string username) {
        omero::sys::ParametersIPtr p = new omero::sys::ParametersI();
        p->add("name", rstring(username));
        omero::api::IObjectList rv = query->findAllByQuery(
            "select p from Project p join fetch p.datasetLinks dil join fetch dil.child where p.details.owner.omeName = :name", p);
        return omero::cast<omero::model::ProjectPtr>(rv);
    }

};
