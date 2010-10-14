/*
   Callback implementations.

   Copyright 2010 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

*/

#include <omero/callbacks.h>
#include <omero/RTypesI.h>
#include <IceUtil/Time.h>
#include <IceUtil/UUID.h>

using namespace std;
using namespace IceUtil;
using namespace omero;
using namespace omero::grid;
using namespace omero::rtypes;
using namespace omero::api::_cpp_delete;

namespace omero {

    namespace callbacks {


        //
        // ProcessCallback
        //

        ProcessCallbackI::ProcessCallbackI(
                const Ice::ObjectAdapterPtr& adapter,
                const ProcessPrx& process,
                bool poll) :
            ProcessCallback(),
            process(process),
            adapter(adapter),
            id(Ice::Identity()),
            poll(poll),
            result(std::string()) {

            std::string uuid = generateUUID();
            this->id.category = "ProcessCallback";
            this->id.name = uuid;
            Ice::ObjectPrx prx = adapter->add(this, this->id);
            omero::grid::ProcessCallbackPrx pcb = ProcessCallbackPrx::uncheckedCast(prx);
            process->registerCallback(pcb);

        };

	ProcessCallbackI::~ProcessCallbackI() {
            this->adapter->remove(this->id);
        };

        std::string ProcessCallbackI::block(long ms) {
            if (poll) {
                try {
                    RIntPtr rc = process->poll();
                    if (rc) {
                        processFinished(rc->getValue());
                    }
                } catch (const Ice::Exception& ex) {
                    cerr << "Error calling poll: " << ex << endl;
                }
            }
            event.wait(Time::milliSeconds(ms));
            return result; // Possibly empty
        };

        void ProcessCallbackI::processCancelled(bool success, const Ice::Current& current) {
            result = CANCELLED;
            event.set();
        };

        void ProcessCallbackI::processFinished(int returncode, const Ice::Current& current) {
            result = FINISHED;
            event.set();
        };

        void ProcessCallbackI::processKilled(bool success, const Ice::Current& current) {
            result = KILLED;
            event.set();
        };

        //
        // DeleteCallback
        //

        DeleteCallbackI::DeleteCallbackI(
            const Ice::ObjectAdapterPtr& adapter, const OME_API_DEL::DeleteHandlePrx handle) :
            adapter(adapter),
            handle(handle),
            poll(true) {
        };

	DeleteCallbackI::~DeleteCallbackI() {
            handle->close();
        };

        DeleteReports DeleteCallbackI::loop(int loops, long ms) {
            int count = 0;
            RIntPtr errors;
            while (!errors && count < loops) {
                errors = block(ms);
                count++;
            }

            if (!errors) {
                int waited = (ms/1000) * loops;
                stringstream ss;
                ss << "Delete unfinished after " << waited << "seconds.";
                throw new LockTimeout("", "", ss.str(), 5000L, waited);
            } else {
                return handle->report();
            }
        };

        RIntPtr DeleteCallbackI::block(long ms) {
            if (poll) {
                try {
                    if (handle->finished()) {
                            try {
                                finished(handle->errors());
                            } catch (const Ice::Exception& ex) {
                                cerr << "Error calling DeleteCallbackI.finished: " << ex << endl;
                            }
                    }
                } catch (const Ice::Exception& ex) {
                    cerr << "Error polling DeleteHandle:" << ex << endl;
                }
            }

            event.wait(Time::milliSeconds(ms));
            return result; // Possibly empty

        };

        void DeleteCallbackI::finished(int errors) {
            result = rint(errors);
            event.set();
        };
    };

};
