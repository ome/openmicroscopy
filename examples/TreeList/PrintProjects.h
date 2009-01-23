#include <omero/model/Project.h>
#include <omero/model/ProjectDatasetLink.h>
#include <omero/model/Dataset.h>

struct PrintProjects {

    static void print(std::vector<omero::model::ProjectPtr> projects) {

        std::vector<omero::model::ProjectPtr>::iterator beg = projects.begin();

        while (beg != projects.end()) {
            omero::model::ProjectPtr project = *beg;
            std::cout << project->getName()->getValue() << std::endl;

            omero::model::ProjectDatasetLinksSeq links = project->copyDatasetLinks();
            omero::model::ProjectDatasetLinksSeq::iterator beg2 = links.begin();

            while (beg2 != links.end()) {
                omero::model::ProjectDatasetLinkPtr pdl = *beg2;
                omero::model::DatasetPtr dataset = pdl->getChild();
                std::cout << "  " + dataset->getName()->getValue() << std::endl;
                beg2++;
            }
        }

    }

};
