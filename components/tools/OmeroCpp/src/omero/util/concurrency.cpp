/*
 * OMERO Concurrency Utilities
 *
 * Copyright 2010 Glencoe Software, Inc.  All Rights Reserved.
 * Use is subject to license terms supplied in LICENSE.txt
 */

#include <omero/util/concurrency.h>

namespace omero {
    namespace util {
        namespace concurrency {

            Event::Event() : flag(false) {
            }

            Event::~Event() {
                // cond and mutex will be reaped.
            }

            bool Event::isSet() {
                return flag;
            }

            void Event::set() {
                IceUtil::RecMutex::Lock sync(mutex);
                flag = true;
                cond.broadcast();
            }

            void Event::clear() {
                IceUtil::RecMutex::Lock sync(mutex);
                flag = false;
            }

            bool Event::wait(const IceUtil::Time& timeout) {
                IceUtil::RecMutex::Lock sync(mutex);
                if (!flag) {
                    return cond.timedWait(sync, timeout);
                }
                return true;
            }
        }
    }
}

