/*
   Callbacks to be used with asynchronous services.

   Copyright 2010 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

*/


#ifndef OMERO_CALLBACKS_H
#define OMERO_CALLBACKS_H

#include <string>

#include <omero/IceNoWarnPush.h>
#include <Ice/Ice.h>
#include <Ice/ObjectAdapter.h>
#include <IceUtil/Monitor.h>
#include <IceUtil/Config.h>
#include <IceUtil/Thread.h>
#include <Ice/Handle.h>
#include <omero/Scripts.h>
#include <omero/cmd/API.h>
#include <omero/Scripts.h>
#include <omero/IceNoWarnPop.h>

#include <omero/client.h>
#include <omero/RTypesI.h>
#include <omero/util/concurrency.h>

#ifndef OMERO_CLIENT
#   ifdef OMERO_CLIENT_EXPORTS
#       define OMERO_CLIENT ICE_DECLSPEC_EXPORT
#   else
#       define OMERO_CLIENT ICE_DECLSPEC_IMPORT
#   endif
#endif

namespace omero {
    namespace callbacks {
        class ProcessCallbackI;
        class CmdCallbackI;
    }
}

namespace IceInternal {
  OMERO_CLIENT ::Ice::Object* upCast(::omero::callbacks::ProcessCallbackI*);
  OMERO_CLIENT ::Ice::Object* upCast(::omero::callbacks::CmdCallbackI*);
}

namespace omero {

    namespace callbacks {

        // TODO: use shared_ptr instead of Ice handle

        // This wrapper allows for having two different destructors for callbacks
        // one that is called when Ice frees the callback and one when other code does
        // In the case where Ice does the freeing, we don't need to do anything in the destructor,
        // and in the case where all non Ice references go out of scope, we need to close
        // the callback to prevent keeping it open until session close
        template <typename T> // OMERO_CLIENT ?
        class CallbackWrapper : public IceUtil::Handle<T> {
        public:
            CallbackWrapper(T* p) : IceUtil::Handle<T>(p) {}
            CallbackWrapper() {}

            ~CallbackWrapper() {
                // When our last non Ice reference goes out of scope, we need to close the callback
                // this would be a ref count of 2 or less, as Ice always holds a refernce to this object while it's active
                // this effectively makes the Ice reference a weak one, without having to change the Ice code
                if (this->_ptr && this->_ptr->__getRef() <= 2) {
                    try {
                        this->_ptr->close();
                    }
                    catch (Ice::ObjectAdapterDeactivatedException e) {
                        // Okay to ignore as this should only happen when the session is closed before this is called
                    }
                }
            }
        };

        typedef CallbackWrapper<CmdCallbackI> CmdCallbackIPtr;
        typedef CallbackWrapper<ProcessCallbackI> ProcessCallbackIPtr;

        /*
         * Simple callback which registers itself with the given process.
         */
        class OMERO_CLIENT ProcessCallbackI : virtual public omero::grid::ProcessCallback {

        // Preventing copy-construction and assigning by value.
        private:
            ProcessCallbackI& operator=(const ProcessCallbackI& rv);
            ProcessCallbackI(ProcessCallbackI&);
            omero::util::concurrency::Event event;
            Ice::ObjectAdapterPtr adapter;
            Ice::Identity id;
            bool poll;
            std::string result;
        protected:
            /**
             * Proxy passed to this instance on creation. Can be used by subclasses
             * freely. The object will not be nulled, but may be closed server-side.
             */
            omero::grid::ProcessPrx process;
        public:
            ProcessCallbackI(const Ice::ObjectAdapterPtr& adapter, const omero::grid::ProcessPrx& process, bool poll = true);

            /**
             * First removes self from the adapter so as to no longer receive notifications
             *
             * WARNING:
             * This cannot be called from the destructor, because during session destruction,
             * the Ice ServantManager will delete this class, and that would cause a
             * double delete, as we'd be calling back into the servant manager to remove us,
             * when it's already in the process of doing the remove/deletion.
             */
            void close();

            /**
             * Should only be used if the default logic of the process methods is kept
             * in place. If "event.set" does not get called, this method will always
             * block for the given milliseconds.
             */
            virtual std::string block(long ms);

            // NYI: virtual int loop(int loops, long ms);

            virtual void processCancelled(bool success, const Ice::Current& current = Ice::Current());

            virtual void processFinished(int returncode, const Ice::Current& current = Ice::Current());

            virtual void processKilled(bool success, const Ice::Current& current = Ice::Current());

        };

        /*
         * Callback used for waiting until omero::cmd::HandlePrx will return
         * a non-empty Response. The block(long) method will wait the given number of
         * milliseconds and then return the number of errors if any or None
         * if the delete is not yet complete.
         *
         * Example usage:
         *
         *     CmdCallbackI cb(client, handle);
         *     omero::cmd::ResponsePtr rsp;
         *     while (!rsp) {
         *         rsp = cb.block(500);
         *     }
         *  or
         *
         *     ResponsePtr rsp = cb.loop(5, 500);
         *
         */

        class OMERO_CLIENT CmdCallbackI : virtual public omero::cmd::CmdCallback {

        // Preventing copy-construction and assigning by value.
        private:
            CmdCallbackI& operator=(const CmdCallbackI& rv);
            CmdCallbackI(CmdCallbackI&);

            // TODO: use std thread instead of Ice thread

            // Thread to allow async poll, to ensure onFinished is called after the ctor finishes,
            // as virtual function calls do not work from constructors
            class PollThread : public IceUtil::Thread {
            public:
                PollThread(CmdCallbackIPtr callback) :
                    callback(callback)
                {}

                virtual void run() {
                    try {
                        callback->poll();
                    } catch (...) {
                        // don't throw any exceptions, e.g. if the
                        // handle has already been closed.
                        try {
                            callback->onFinished(
                                omero::cmd::ResponsePtr(),
                                omero::cmd::StatusPtr(),
                                Ice::Current());
                        } catch (...) {
                            // ditto
                        }
                    }
                }
            private:
                CmdCallbackIPtr callback;
            };
            typedef IceUtil::Handle<PollThread> PollThreadPtr;
            PollThreadPtr pollThread;

        protected:

            Ice::ObjectAdapterPtr adapter;

            Ice::Identity id;

            /**
             * Latch which is released once {@link #finished(Response, Current)} is
             * called. Other methods will block on this value.
             */
            omero::util::concurrency::Event event;

            /**
             * Protects all mutable state.
             */
            IceUtil::RecMutex mutex;

            /**
             * Primary mutable state which is the return
             * value set upon finish
             */
            std::pair<omero::cmd::ResponsePtr, omero::cmd::StatusPtr> state;

            /**
             * Proxy passed to this instance on creation. Can be used by subclasses
             * freely. The object will not be nulled, but may be closed server-side.
             */
            omero::cmd::HandlePrx handle;

            /**
             * Whether or not the destructor should call close
             * on the handle object.
             */
            bool closeHandle;

            omero::cmd::StatusPtr getStatusOrThrow();

            int count(
                    omero::cmd::StateList list,
                    omero::cmd::State s);

            void doinit(std::string category);

            /**
             * Called at the end of construction to check a race condition.
             *
             * If HandlePrx finishes its execution before the
             * CmdCallbackPrx has been sent set via addCallback,
             * then there's a chance that this implementation will never
             * receive a call to finished, leading to perceived hangs.
             *
             * By default, this method starts a background thread and
             * calls poll(). An Ice::ObjectNotExistException
             * implies that another caller has already closed the
             * HandlePrx.
             */
            void initialPoll();

        public:

            CmdCallbackI(const Ice::ObjectAdapterPtr& adapter, const omero::cmd::HandlePrx handle, std::string category, bool closeHandle = true);

            /**
             * Uses the category from client::getCategory as the id.
             */
            CmdCallbackI(const omero::client_ptr& client, const omero::cmd::HandlePrx handle, bool closeHandle = true);

            /**
             * First removes self from the adapter so as to no longer receive
             * notifications, and then calls close on the remote handle if requested.
             *
             * WARNING:
             * This cannot be called from the destructor, because during session destruction,
             * the Ice ServantManager will delete this class, and that would cause a
             * double delete, as we'd be calling back into the servant manager to remove us,
             * when it's already in the process of doing the remove/deletion.
             */
            void close();

            //
            // Local invcations
            //

            /**
             * Returns possibly null Response value. If null, then neither has
             * the remote server nor the local poll method called finish with
             * non-null values.
             */
            virtual omero::cmd::ResponsePtr getResponse();

            /**
             * Returns possibly null Status value. If null, then neither has
             * the remote server nor the local poll method called finish with
             * non-null values.
             */
            virtual omero::cmd::StatusPtr getStatus();

            /**
             * Returns whether Status::CANCELLED is contained in
             * the flags variable of the Status instance. If no
             * Status is available, a ClientError will be thrown.
             */
            virtual bool isCancelled();

            /**
             * Returns whether Status::FAILURE is contained in
             * the flags variable of the Status instance. If no
             * Status is available, a ClientError will be thrown.
             */
            virtual bool isFailure();

            /**
             * Calls block(long) "loops" number of times with the "ms"
             * argument. This means the total wait time for the delete to occur
             * is: loops X ms. Sensible values might be 10 loops for 500 ms, or
             * 5 seconds.
             *
             * @param loops Number of times to call block(long)
             * @param ms Number of milliseconds to pass to block(long
             * @throws omero.LockTimeout if block(long) does not return
             * a non-null value after loops calls.
             */
            virtual omero::cmd::ResponsePtr loop(int loops, long ms);

            /**
             * Blocks for the given number of milliseconds unless
             * {@link #finished(Response, Status, Current)} has been called in which case
             * it returns immediately with true. If false is returned, then the timeout
             * was reached.
             *
             * @param ms Milliseconds which this method should block for.
             * @return
             */
            virtual bool block(long ms);

            //
            // Remote invcations
            //

            /**
             * Calls {@link HandlePrx#getResponse} in order to check for a non-null
             * value. If so, {@link Handle#getStatus} is also called, and the two
             * non-null values are passed to
             * {@link #finished(Response, Status, Current)}. This should typically
             * not be used. Instead, favor the use of block and loop.
             *
             */
            virtual void poll();

            /**
             * Called periodically by the server to signal that processing is
             * moving forward. Default implementation does nothing.
             */
            virtual void step(int complete, int total, const Ice::Current& current = Ice::Current());

            /**
             * Called when the command has completed.
             */
            virtual void finished(const omero::cmd::ResponsePtr& response,
                   const omero::cmd::StatusPtr& status, const Ice::Current& current = Ice::Current());

            /**
             * Method intended to be overridden by subclasses. Default logic does
             * nothing.
             */
            virtual void onFinished(const omero::cmd::ResponsePtr& rsp,
                const omero::cmd::StatusPtr& status, const Ice::Current& current = Ice::Current());

        };

    }
}


#endif // OMERO_CALLBACKS_H
