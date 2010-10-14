#include <map>
#include <iostream>
#include <omero/client.h>
#include <omero/callbacks.h>

using namespace std;

namespace OA = omero::api;
namespace OAD = omero::api::_cpp_delete;
namespace OC = omero::callbacks;


/*
 * Subclasses DeleteCallbackI
 */
class Subclass : virtual public OC::DeleteCallbackI {

private:
    Subclass& operator=(const Subclass& rv);
    Subclass(Subclass&);

public:
    Subclass(
        const Ice::ObjectAdapterPtr& oa,
        const OAD::DeleteHandlePrx& handle) :
        OC::DeleteCallbackI(oa, handle) {
    };

    void finished(int errors) {
        OC::DeleteCallbackI::finished(errors);
        cout << "Finished. Error count=" << errors << endl;
        try {
            OAD::DeleteReports reports = handle->report();
            OAD::DeleteReports::iterator beg = reports.begin();
            OAD::DeleteReports::iterator end = reports.end();
            while (beg != end) {
                OAD::DeleteReportPtr r = *beg;
                cout << "Report:error=" << r->error << ",warning=";
                cout << r->warning << ",deleted=" << r->actualDeletes << endl;
                beg++;
            }
        } catch (const omero::ServerError& se) {
            cout << "Something happened to the handle?!?" << endl;
        }

    };

};

/**
 * Uses the default {@link DeleteCallbackI} instance.
 */
int main(int argc, char* argv[]) {

    omero::client_ptr c = new omero::client(); // Close handled by destructor
    OA::ServiceFactoryPrx s = c->createSession();

    {
        OA::IDeletePrx deleteServicePrx = s->getDeleteService();
        OAD::DeleteCommand dc;
        dc.type = "/Image";
        dc.id = 1;
        OAD::DeleteCommands dcs;
        dcs.push_back(dc);

        OAD::DeleteHandlePrx deleteHandlePrx = deleteServicePrx->queueDelete(dcs);
        OC::DeleteCallbackIPtr cb = new Subclass(c->getObjectAdapter(), deleteHandlePrx); // Closed by destructor

        try {

            cb->loop(10, 500);
            // If we reach here, finished() was called.

        } catch (const omero::LockTimeout& lt) {
            cout << "Not finished in 5 seconds. Cancelling..." << endl;
            if (!deleteHandlePrx->cancel()) {
                cout << "ERROR: Failed to cancel" << endl;
            }
        }
    }
}
