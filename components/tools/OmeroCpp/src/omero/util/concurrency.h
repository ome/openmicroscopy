/*
 * OMERO Concurrency Utilities
 *
 * Copyright 2010 Glencoe Software, Inc.  All Rights Reserved.
 * Use is subject to license terms supplied in LICENSE.txt
 */

#ifndef OMERO_UTIL_CONCURRENCY_H
#define OMERO_UTIL_CONCURRENCY_H

#include <IceUtil/Cond.h>
#include <IceUtil/Monitor.h>
#include <IceUtil/RecMutex.h>
#include <IceUtil/Time.h>

#ifndef OMERO_API
#   ifdef OMERO_API_EXPORTS
#       define OMERO_API ICE_DECLSPEC_EXPORT
#   else
#       define OMERO_API ICE_DECLSPEC_IMPORT
#   endif
#endif

namespace omero {
    namespace util {
        namespace concurrency {

            /**
            * Port of Python's threading.Event to C++
            */
            class OMERO_API Event : private IceUtil::Monitor<IceUtil::RecMutex> {
            private:
                bool flag;
                IceUtil::RecMutex mutex;
                IceUtil::Cond cond;
            public:
                Event();
                ~Event();
                bool isSet();
                void set();
                void clear();
                bool wait(const IceUtil::Time& timeout);
            };

        }
    }
}

#endif // OMERO_UTIL_CONCURRENCY_H
