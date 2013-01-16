/*
   Callbacks to be used with asynchronous services.

   Copyright 2010 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

*/


#ifndef OMERO_CALLBACKS_H
#define OMERO_CALLBACKS_H

#include <string>
#include <Ice/Ice.h>
#include <Ice/ObjectAdapter.h>
#include <IceUtil/Monitor.h>
#include <IceUtil/Config.h>
#if ICE_INT_VERSION / 100 >= 304
#   include <Ice/Handle.h>
#else
#   include <IceUtil/Handle.h>
#endif
#include <omero/client.h>
#include <omero/RTypesI.h>
#include <omero/Scripts.h>
#include <omero/api/IDelete.h>
#include <omero/cmd/API.h>
#include <omero/util/concurrency.h>

#ifndef OMERO_API
#   ifdef OMERO_API_EXPORTS
#       define OMERO_API ICE_DECLSPEC_EXPORT
#   else
#       define OMERO_API ICE_DECLSPEC_IMPORT
#   endif
#endif

namespace omero {

    namespace callbacks {

        const std::string FINISHED = "FINISHED";
        const std::string CANCELLED = "CANCELLED";
        const std::string KILLED = "KILLED";

        /*
         * Simple callback which registers itself with the given process.
         */
        class OMERO_API ProcessCallbackI : virtual public omero::grid::ProcessCallback {

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
	    virtual ~ProcessCallbackI();
        public:
            ProcessCallbackI(const Ice::ObjectAdapterPtr& adapter, const omero::grid::ProcessPrx& process, bool poll = true);

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


#if ICE_INT_VERSION / 100 >= 304
        typedef IceInternal::Handle<ProcessCallbackI> ProcessCallbackIPtr;
#else
        typedef IceUtil::Handle<ProcessCallbackI> ProcessCallbackIPtr;
#endif


        namespace OME_API_DEL = omero::api::_cpp_delete;

        /*
         * Callback used for waiting until DeleteHandlePrx will return true on
         * finished(). The block(long) method will wait the given number of
         * milliseconds and then return the number of errors if any or None
         * if the delete is not yet complete.
         *
         * Example usage:
         *
         *     DeleteCallbackI cb(client, handle);
         *     omero::RTypePtr errors;
         *     while (!errors) {
         *         errors = cb.block(500);
         *     }
         */
        class OMERO_API DeleteCallbackI : virtual public IceUtil::Shared {

        // Preventing copy-construction and assigning by value.
        private:
            DeleteCallbackI& operator=(const DeleteCallbackI& rv);
            DeleteCallbackI(DeleteCallbackI&);
            // State
            omero::util::concurrency::Event event;
            Ice::ObjectAdapterPtr adapter;
            bool poll;
            omero::RIntPtr result;
	protected:
            /**
             * Proxy passed to this instance on creation. Can be used by subclasses
             * freely. The object will not be nulled, but may be closed server-side.
             */
            OME_API_DEL::DeleteHandlePrx handle;
	    virtual ~DeleteCallbackI();
        public:
            DeleteCallbackI(const Ice::ObjectAdapterPtr& adapter, const OME_API_DEL::DeleteHandlePrx handle);
            virtual omero::RIntPtr block(long ms);
            virtual omero::api::_cpp_delete::DeleteReports loop(int loops, long ms);
            virtual void finished(int errors);
        };


#if ICE_INT_VERSION / 100 >= 304
        typedef IceInternal::Handle<DeleteCallbackI> DeleteCallbackIPtr;
#else
        typedef IceUtil::Handle<DeleteCallbackI> DeleteCallbackIPtr;
#endif


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
        class OMERO_API CmdCallbackI : virtual public omero::cmd::CmdCallback {

        // Preventing copy-construction and assigning by value.
        private:
            CmdCallbackI& operator=(const CmdCallbackI& rv);
            CmdCallbackI(CmdCallbackI&);

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

            /**
             * First removes self from the adapter so as to no longer receive
             * notifications, and the calls close on the remote handle if requested.
             */
	    virtual ~CmdCallbackI();

            omero::cmd::StatusPtr getStatusOrThrow();

            void doinit(std::string category);

        public:

            CmdCallbackI(const Ice::ObjectAdapterPtr& adapter, const omero::cmd::HandlePrx handle, std::string category, bool closeHandle = true);

            /**
             * Uses the category from client::getCategory as the id.
             */
            CmdCallbackI(const omero::client_ptr& client, const omero::cmd::HandlePrx handle, bool closeHandle = true);

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

#if ICE_INT_VERSION / 100 >= 304
        typedef IceInternal::Handle<CmdCallbackI> CmdCallbackIPtr;
#else
        typedef IceUtil::Handle<CmdCallbackI> CmdCallbackIPtr;
#endif

    };
};


#endif // OMERO_CALLBACKS_H
