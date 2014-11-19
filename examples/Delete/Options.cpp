#include <map>
#include <iostream>
#include <omero/client.h>
#include <omero/callbacks.h>
// Needs rewriting to use omero::cmd::Delete

using namespace std;

namespace OA = omero::api;
namespace OAD = omero::api::_cpp_delete;
namespace OC = omero::callbacks;


/**
 * Passes a non-empty options map to specify
 * that all Annotations should be kept.
 */
int main(int argc, char* argv[]) {

    omero::client_ptr c = new omero::client(); // Close handled by destructor
    OA::ServiceFactoryPrx s = c->createSession();

    {
        OA::IDeletePrx deleteServicePrx = s->getDeleteService();

        // The path here can either be relative or absolute, e.g.
        // /Image/ImageAnnotationLink/Annotation. "KEEP" specifies
        // that no delete should take place. "SOFT" specifies that
        // if a delete is not possible no error should be signaled.
        OAD::DeleteCommand dc;
        dc.type = "/Image";
        dc.id = 1;
        dc.options["/Annotation"] = "KEEP";

        OAD::DeleteCommands dcs;
        dcs.push_back(dc);

        OAD::DeleteHandlePrx deleteHandlePrx = deleteServicePrx->queueDelete(dcs);
        OC::DeleteCallbackIPtr cb = new OC::DeleteCallbackI(c->getObjectAdapter(), deleteHandlePrx); // Closed by destructor

        try {
            cb->loop(10, 500);

            OAD::DeleteReports reports = deleteHandlePrx->report();
            OAD::DeleteReportPtr r = reports[0]; // We only send one command
            cout << "Report:error=" << r->error << ",warning=" << r->warning;
            cout << ",deleted=" << r->actualDeletes << endl;
        } catch (const omero::LockTimeout& lt) {
            cout << "Not finished in 5 seconds. Cancelling..." << endl;
            if (!deleteHandlePrx->cancel()) {
                cout << "ERROR: Failed to cancel" << endl;
            }
        }
    }
}
