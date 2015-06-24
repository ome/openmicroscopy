#include <map>
#include <iostream>
#include <omero/client.h>
#include <omero/callbacks.h>

using namespace std;

namespace OA = omero::api;
namespace OC = omero::callbacks;
namespace OCMD = omero::cmd;


/**
 * Uses the default {@link DeleteCallbackI} instance.
 */
int main(int argc, char* argv[]) {

    omero::client_ptr c = new omero::client(); // Close handled by destructor
    OA::ServiceFactoryPrx s = c->createSession();

    {
        OCMD::Delete dc;
        dc.type = "/Image";
        dc.id = 1;

        OCMD::HandlePrx handlePrx = s->submit(dc);
        OC::CmdCallbackIPtr cb = new OC::CmdCallbackI(c->getObjectAdapter(), handlePrx); // Closed by destructor

        try {
            cb->loop(10, 500);

            OAD::DeleteReports reports = deleteHandlePrx->report();
            OAD::DeleteReportPtr r = reports[0]; // We only send one command
            cout << "Report:error=" << r->error << ",warning=" << r->warning;
            cout << ",deleted=" << r->actualDeletes << endl;
        } catch (const omero::LockTimeout& lt) {
            cout << "Not finished in 5 seconds. Cancelling..." << endl;
            if (!handlePrx->cancel()) {
                cout << "ERROR: Failed to cancel" << endl;
            }
        }
    }
}
