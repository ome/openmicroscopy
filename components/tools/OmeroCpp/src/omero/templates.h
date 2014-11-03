/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#ifndef OMERO_TEMPLATES_H
#define OMERO_TEMPLATES_H

#include <vector>
#include <algorithm>

/*
 * General use templates
 */

namespace omero {

    /**
     * Functoid which takes a IObjectPtr in its constructor and tests
     * via "==" comparision when operator() is called.
     */
    template<class T> struct ContainsPointer {
        const T test;
        ContainsPointer(const T lookfor) : test(lookfor) {}
        bool operator()(T const& o) {
            return o == test;
        }
    };

    /**
     * Functoid which takes a sequence in its constructor and tests
     * via std::find when operator() is called.
     */
    template<class T> struct VectorContainsPointer {
        const std::vector<T> test;
        VectorContainsPointer(const std::vector<T> lookfor) : test(lookfor) {}
        bool operator()(T const& o) {
            return std::find(test.begin(), test.end(), o) != test.end();
        }
    };

    /**
     * Return the index in the vector of the given element.
     */
    template<class T> int indexOf(const std::vector<T>& v, const T& t) {
        return static_cast<int>(std::find(v.begin(), v.end(), t) - v.begin());
    }

    /**
     * Cast an IObjectList vector to a vector of the template type.
     */
    template<class T> std::vector<T> cast(omero::api::IObjectList& v) {
        std::vector<T> rv;
        omero::api::IObjectList::iterator beg = v.begin();
        while (beg != v.end()) {
            rv.push_back(T::dynamicCast(*beg));
            beg++;
        }
        return rv;
    }

}
#endif // OMERO_TEMPLATES_H
