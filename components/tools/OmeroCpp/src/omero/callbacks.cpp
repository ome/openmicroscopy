/*
   Callback implementations.

   Copyright 2010 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

*/

#include <omero/callbacks.h>
#include <omero/util/uuid.h>

#include <omero/IceNoWarnPush.h>
#include <omero/ClientErrors.h>
#include <omero/RTypesI.h>
#include <IceUtil/Time.h>
#include <omero/IceNoWarnPop.h>

using namespace std;
using namespace IceUtil;
using namespace omero;
using namespace omero::grid;
using namespace omero::rtypes;

::Ice::Object* IceInternal::upCast(::omero::callbacks::ProcessCallbackI* p) { return p; }
::Ice::Object* IceInternal::upCast(::omero::callbacks::CmdCallbackI* p) { return p; }

namespace
{
    const std::string FINISHED("FINISHED");
    const std::string CANCELLED("CANCELLED");
    const std::string KILLED("KILLED");
}

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

            this->id.category = "ProcessCallback";
            this->id.name = util::generate_uuid();
            Ice::ObjectPrx prx = adapter->add(this, this->id);
            omero::grid::ProcessCallbackPrx pcb = ProcessCallbackPrx::uncheckedCast(prx);
            process->registerCallback(pcb);

        }

        void ProcessCallbackI::close() {
            if (adapter)
                adapter->remove(id);
        }

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
        }

        void ProcessCallbackI::processCancelled(bool /*success*/, const Ice::Current&) {
            result = CANCELLED;
            event.set();
        }

        void ProcessCallbackI::processFinished(int /*returncode*/, const Ice::Current&) {
            result = FINISHED;
            event.set();
        }

        void ProcessCallbackI::processKilled(bool /*success*/, const Ice::Current&) {
            result = KILLED;
            event.set();
        }

        //
        // CmdCallback
        //

        CmdCallbackI::CmdCallbackI(
            const Ice::ObjectAdapterPtr& adapter, const omero::cmd::HandlePrx handle, std::string category, bool closeHandle) :
            adapter(adapter),
            handle(handle),
            closeHandle(closeHandle){
                doinit(category);
        }

        CmdCallbackI::CmdCallbackI(
            const omero::client_ptr& client, const omero::cmd::HandlePrx handle, bool closeHandle) :
            adapter(client->getObjectAdapter()),
            handle(handle),
            closeHandle(closeHandle){
                doinit(client->getCategory());
        }

        void CmdCallbackI::doinit(std::string category) {
            this->id = Ice::Identity();
            this->id.name = util::generate_uuid();
            this->id.category = category;
            Ice::ObjectPrx prx = adapter->add(this, id);
            omero::cmd::CmdCallbackPrx cb = omero::cmd::CmdCallbackPrx::uncheckedCast(prx);
            handle->addCallback(cb);
            initialPoll();
        }

        void CmdCallbackI::initialPoll() {
            // Now check just in case the process exited VERY quickly
            pollThread = new PollThread(this);
            pollThread->start();
        }

        void CmdCallbackI::close() {
            if (adapter) {
                adapter->remove(id); // OK ADAPTER USAGE
            }
            if (closeHandle && handle) {
                handle->close();
            }
        }

        omero::cmd::ResponsePtr CmdCallbackI::getResponse() {
            IceUtil::RecMutex::Lock lock(mutex);
            return state.first;
        }

        omero::cmd::StatusPtr CmdCallbackI::getStatus() {
            IceUtil::RecMutex::Lock lock(mutex);
            return state.second;
        }

        omero::cmd::StatusPtr CmdCallbackI::getStatusOrThrow() {
            IceUtil::RecMutex::Lock lock(mutex);
            omero::cmd::StatusPtr s = state.second;
            if (!s) {
                throw ClientError(__FILE__, __LINE__, "Status not present!");
            }
            return s;
        }

        int CmdCallbackI::count(omero::cmd::StateList list, omero::cmd::State s) {
            int c = 0;
            omero::cmd::StateList::iterator it;
            for (it=list.begin(); it != list.end(); it++) {
                if (*it == s) {
                    c++;
                }
            }
            return c;
        }

        bool CmdCallbackI::isCancelled() {
            omero::cmd::StatusPtr s = getStatusOrThrow();
            return count(s->flags, omero::cmd::CANCELLED) > 0;

        }

        bool CmdCallbackI::isFailure() {
            omero::cmd::StatusPtr s = getStatusOrThrow();
            return count(s->flags, omero::cmd::FAILURE) > 0;

        }

        omero::cmd::ResponsePtr CmdCallbackI::loop(int loops, long ms) {
            int count = 0;
            bool found = false;
            while (count < loops) {
                count++;
                found = block(ms);
                if (found) {
                    break;
                }
            }

            if (found) {
                return getResponse();
            } else {
                int waited = (ms/1000.0) * loops;
                stringstream ss;
                ss << "Cmd unfinished after " << waited << "seconds.";
                throw LockTimeout("", "", ss.str(), 5000L, waited);
            }
        }

        bool CmdCallbackI::block(long ms) {
            return event.wait(Time::milliSeconds(ms));
        }

        void CmdCallbackI::poll() {
            IceUtil::RecMutex::Lock lock(mutex);

            omero::cmd::ResponsePtr rsp = handle->getResponse();
            if (rsp) {
                omero::cmd::StatusPtr s = handle->getStatus();
                finished(rsp, s, Ice::Current()); // Only time that current should be null.
            }

            pollThread = NULL;
        }

        void CmdCallbackI::step(int /*complete*/, int /*total*/, const Ice::Current&) {
            // no-op
        }

        void CmdCallbackI::finished(const omero::cmd::ResponsePtr& rsp,
                const omero::cmd::StatusPtr& status, const Ice::Current& current) {
            std::pair<omero::cmd::ResponsePtr, omero::cmd::StatusPtr> s(rsp, status);
            this->state = s;
            event.set();
            onFinished(rsp, status, current);
        }

        void CmdCallbackI::onFinished(const omero::cmd::ResponsePtr&,
                const omero::cmd::StatusPtr&, const Ice::Current&) {
            // no-op
        }
    }

}
