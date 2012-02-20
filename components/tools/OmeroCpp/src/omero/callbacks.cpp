/*
   Callback implementations.

   Copyright 2010 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

*/

#include <omero/ClientErrors.h>
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
            adapter(adapter),
            id(Ice::Identity()),
            poll(poll),
            result(std::string()),
            process(process) {

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
            poll(true),
            handle(handle) {
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
                throw LockTimeout("", "", ss.str(), 5000L, waited);
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
                } catch (const Ice::ObjectNotExistException& onee) {
                    throw omero::ClientError(__FILE__, __LINE__, "Handle is gone!");
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

        //
        // CmdCallback
        //

        CmdCallbackI::CmdCallbackI(
            const Ice::ObjectAdapterPtr& adapter, const omero::cmd::HandlePrx handle) :
            adapter(adapter),
            poll(true),
            handle(handle) {
        };

	CmdCallbackI::~CmdCallbackI() {
            handle->close();
        };

        omero::cmd::ResponsePtr CmdCallbackI::loop(int loops, long ms) {
            int count = 0;
            omero::cmd::ResponsePtr rsp;
            while (!rsp && count < loops) {
                rsp = block(ms);
                count++;
            }

            if (!rsp) {
                int waited = (ms/1000) * loops;
                stringstream ss;
                ss << "Cmd unfinished after " << waited << "seconds.";
                throw LockTimeout("", "", ss.str(), 5000L, waited);
            } else {
                return rsp;
            }
        };

        omero::cmd::ResponsePtr CmdCallbackI::block(long ms) {
            omero::cmd::ResponsePtr rsp;
            if (poll) {
                try {
                    rsp = handle->getResponse();
                    if (rsp) {
                            try {
                                finished(handle->getStatus(), rsp);
                            } catch (const Ice::Exception& ex) {
                                cerr << "Error calling CmdCallbackI.finished: " << ex << endl;
                            }
                    }
                } catch (const Ice::ObjectNotExistException& onee) {
                    throw omero::ClientError(__FILE__, __LINE__, "Handle is gone!");
                } catch (const Ice::Exception& ex) {
                    cerr << "Error polling CmdHandle:" << ex << endl;
                }
            }

            event.wait(Time::milliSeconds(ms));
            return rsp; // Possibly empty

        };

        void CmdCallbackI::finished(const omero::cmd::StatusPtr& status, const omero::cmd::ResponsePtr& rsp) {
            event.set();
        };
    };

};
