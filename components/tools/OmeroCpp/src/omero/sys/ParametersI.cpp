/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#include <omero/sys/ParametersI.h>
#include <omero/RTypesI.h>

using omero::rtypes::rlist;
using omero::rtypes::rlong;
using omero::rtypes::rint;

namespace omero {

    namespace sys {

        ParametersI::~ParametersI() {}

        ParametersI::ParametersI(const omero::sys::ParamMap& map) : Parameters() {
	    this->map = map;
        }

	//
	// Parameters.theFilter
	//
	ParametersIPtr ParametersI::noPage(const Ice::Current& c) {
	    this->theFilter = omero::sys::FilterPtr();
	    return this;
	}

	ParametersIPtr ParametersI::page(Ice::Int offset,
					 Ice::Int limit,
					 const Ice::Current& c) {
	    return this->page(rint(offset), rint(limit));
	}

	ParametersIPtr ParametersI::page(const omero::RIntPtr& offset,
					 const omero::RIntPtr& limit,
					 const Ice::Current& c) {
	    if (! this->theFilter) {
		this->theFilter = new omero::sys::Filter();
	    }
	    this->theFilter->offset = offset;
	    this->theFilter->limit = limit;
	    return this;
	}

	//
	// Parameters.map
	//

	ParametersIPtr ParametersI::add(const std::string& name,
					const omero::RTypePtr& r,
					const Ice::Current& c) {
	    this->map[name] = r;
	    return this;
	}


        ParametersIPtr ParametersI::addId(Ice::Long id,
					  const Ice::Current& current) {
	    return add("id", rlong(id));
        }

	ParametersIPtr ParametersI::addId(const omero::RLongPtr& id,
					  const Ice::Current& c) {
	    return add("id", id);
	}

	ParametersIPtr ParametersI::addIds(omero::sys::LongList ids,
					   const Ice::Current& c) {
	    return addLongs("ids", ids);
	}

	ParametersIPtr ParametersI::addLong(const std::string& name,
					Ice::Long l,
					const Ice::Current& c) {
	    return add(name, rlong(l));
	}

	ParametersIPtr ParametersI::addLong(const std::string& name,
					const omero::RLongPtr& l,
					const Ice::Current& c) {
	    return add(name, l);
	}

	ParametersIPtr ParametersI::addLongs(const std::string& name,
					     omero::sys::LongList longs,
					     const Ice::Current& c) {
	    omero::RListPtr list = rlist();
	    omero::sys::LongList::const_iterator beg = longs.begin();
	    omero::sys::LongList::const_iterator end = longs.end();
	    while (beg != end) {
		list->add(rlong(*beg));
		beg++;
	    }
	    this->map[name] = list;
	    return this;
	}


    } // sys

} // omero
