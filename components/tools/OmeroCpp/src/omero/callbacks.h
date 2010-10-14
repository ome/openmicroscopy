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
#include <omero/RTypesI.h>
#include <omero/Scripts.h>
#include <omero/api/IDelete.h>
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
	protected:
            omero::util::concurrency::Event event;
            omero::grid::ProcessPrx process;
            Ice::ObjectAdapterPtr adapter;
            Ice::Identity id;
            bool poll;
            std::string result;
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


        typedef IceUtil::Handle<ProcessCallbackI> ProcessCallbackIPtr;


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
            OME_API_DEL::DeleteHandlePrx handle;
            bool poll;
            omero::RIntPtr result;
	protected:
	    virtual ~DeleteCallbackI();
        public:
            DeleteCallbackI(const Ice::ObjectAdapterPtr& adapter, const OME_API_DEL::DeleteHandlePrx handle);
            virtual omero::RIntPtr block(long ms);
            virtual omero::api::_cpp_delete::DeleteReports loop(int loops, long ms);
            virtual void finished(int errors);
        };


        typedef IceUtil::Handle<DeleteCallbackI> DeleteCallbackIPtr;


    };
};


#endif // OMERO_CALLBACKS_H
