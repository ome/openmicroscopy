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
using omero::rtypes::rbool;

::Ice::Object* IceInternal::upCast(::omero::sys::ParametersI* p) { return p; }

namespace omero {

    namespace sys {

        ParametersI::~ParametersI() {}

        ParametersI::ParametersI(const omero::sys::ParamMap& map) : Parameters() {
	    this->map = map;
        }

	// Parameters.theFilter.limit & offset
	// ===============================================================

	ParametersIPtr ParametersI::noPage() {
	    if (0 != this->theFilter) {
		this->theFilter->limit = omero::RIntPtr();
		this->theFilter->offset = omero::RIntPtr();
	    }
	    return this;
	}

	ParametersIPtr ParametersI::page(Ice::Int offset,
					 Ice::Int limit) {
	    return this->page(rint(offset), rint(limit));
	}

	ParametersIPtr ParametersI::page(const omero::RIntPtr& offset,
					 const omero::RIntPtr& limit) {
	    if (0 == this->theFilter) {
		this->theFilter = new omero::sys::Filter();
	    }
	    this->theFilter->offset = offset;
	    this->theFilter->limit = limit;
	    return this;
	}

	bool ParametersI::isPagination() {
	    if (0 != this->theFilter) {
		return this->theFilter->offset && this->theFilter->limit;
	    }
	    return false;
	}

	omero::RIntPtr ParametersI::getOffset() {
	    if (0 != this->theFilter) {
		return this->theFilter->offset;
	    }
	    return omero::RIntPtr();
	}

	omero::RIntPtr ParametersI::getLimit() {
	    if (0 != this->theFilter) {
		return this->theFilter->limit;
	    }
	    return omero::RIntPtr();
	}

	ParametersIPtr ParametersI::unique() {
	    if (0 == this->theFilter) {
		this->theFilter = new omero::sys::Filter();
	    }
	    this->theFilter->unique = rbool(true);
	    return this;
	}

	ParametersIPtr ParametersI::noUnique() {
	    if (0 == this->theFilter) {
		this->theFilter = new omero::sys::Filter();
	    }
	    this->theFilter->unique = rbool(false);
	    return this;
	}

	omero::RBoolPtr ParametersI::getUnique() {
	    if (0 != this->theFilter) {
		return this->theFilter->unique;
	    }
	    return omero::RBoolPtr();
	}

	// Parameters.theFilter.ownerId & groupId
	// ===============================================================

	ParametersIPtr ParametersI::exp(Ice::Long id) {
	    if (0 == this->theFilter) {
		this->theFilter = new omero::sys::Filter();
	    }
	    this->theFilter->ownerId = rlong(id);
	    return this;
	}

	ParametersIPtr ParametersI::allExps() {
	    if (0 != this->theFilter) {
		this->theFilter->ownerId = omero::RLongPtr();
	    }
	    return this;
	}

	bool ParametersI::isExperimenter() {
	    if (0 != this->theFilter) {
		return this->theFilter->ownerId != 0;
	    }
	    return false;
	}

	omero::RLongPtr ParametersI::getExperimenter() {
	    if (0 != this->theFilter) {
		return this->theFilter->ownerId;
	    }
	    return omero::RLongPtr();
	}

	ParametersIPtr ParametersI::grp(Ice::Long id) {
	    if (0 == this->theFilter) {
		this->theFilter = new omero::sys::Filter();
	    }
	    this->theFilter->groupId = rlong(id);
	    return this;
	}

	ParametersIPtr ParametersI::allGrps() {
	    if (0 != this->theFilter) {
		this->theFilter->groupId = omero::RLongPtr();
	    }
	    return this;
	}

	bool ParametersI::isGroup() {
	    if (0 != this->theFilter) {
		return this->theFilter->groupId != 0;
	    }
	    return false;
	}

	omero::RLongPtr ParametersI::getGroup() {
	    if (0 != this->theFilter) {
		return this->theFilter->groupId;
	    }
	    return omero::RLongPtr();
	}

	// Parameters.theFilter.starttime, endTime
	// ===============================================================

	ParametersIPtr ParametersI::startTime(const omero::RTimePtr& time) {
	    if (0 == this->theFilter) {
		this->theFilter = new omero::sys::Filter();
	    }
	    this->theFilter->startTime = time;
	    return this;
	}

	ParametersIPtr ParametersI::endTime(const omero::RTimePtr& time) {
	    if (0 == this->theFilter) {
		this->theFilter = new omero::sys::Filter();
	    }
	    this->theFilter->endTime = time;
	    return this;
	}

	ParametersIPtr ParametersI::allTimes() {
	    if (0 != this->theFilter) {
		this->theFilter->startTime = omero::RTimePtr();
		this->theFilter->endTime = omero::RTimePtr();
	    }
	    return this;
	}

	bool ParametersI::isStartTime() {
	    if (0 != this->theFilter) {
		return this->theFilter->startTime != 0;
	    }
	    return false;
	}

	bool ParametersI::isEndTime() {
	    if (0 != this->theFilter) {
		return this->theFilter->endTime != 0;
	    }
	    return false;
	}

	omero::RTimePtr ParametersI::getStartTime() {
	    if (0 != this->theFilter) {
		return this->theFilter->startTime;
	    }
	    return omero::RTimePtr();
	}

	omero::RTimePtr ParametersI::getEndTime() {
	    if (0 != this->theFilter) {
		return this->theFilter->endTime;
	    }
	    return omero::RTimePtr();
	}


	// Parameters.theOptions.leaves, orphan, acquisitionData
	// ===============================================================

	ParametersIPtr ParametersI::leaves() {
	    if (0 == this->theOptions) {
		this->theOptions = new omero::sys::Options();
	    }
	    this->theOptions->leaves = rbool(true);
	    return this;
	}

	ParametersIPtr ParametersI::noLeaves() {
	    if (0 == this->theOptions) {
		this->theOptions = new omero::sys::Options();
	    }
	    this->theOptions->leaves = rbool(false);
	    return this;
	}

	omero::RBoolPtr ParametersI::getLeaves() {
	    if (0 != this->theOptions) {
		return this->theOptions->leaves;
	    }
	    return omero::RBoolPtr();
	}

	ParametersIPtr ParametersI::orphan() {
	    if (0 == this->theOptions) {
		this->theOptions = new omero::sys::Options();
	    }
	    this->theOptions->orphan = rbool(true);
	    return this;
	}

	ParametersIPtr ParametersI::noOrphan() {
	    if (0 == this->theOptions) {
		this->theOptions = new omero::sys::Options();
	    }
	    this->theOptions->orphan = rbool(false);
	    return this;
	}

	omero::RBoolPtr ParametersI::getOrphan() {
	    if (0 != this->theOptions) {
		return this->theOptions->orphan;
	    }
	    return omero::RBoolPtr();
	}

	ParametersIPtr ParametersI::acquisitionData() {
	    if (0 == this->theOptions) {
		this->theOptions = new omero::sys::Options();
	    }
	    this->theOptions->acquisitionData = rbool(true);
	    return this;
	}

	ParametersIPtr ParametersI::noAcquisitionData() {
	    if (0 == this->theOptions) {
		this->theOptions = new omero::sys::Options();
	    }
	    this->theOptions->acquisitionData = rbool(false);
	    return this;
	}

	omero::RBoolPtr ParametersI::getAcquisitionData() {
	    if (0 != this->theOptions) {
		return this->theOptions->acquisitionData;
	    }
	    return omero::RBoolPtr();
	}


	// Parameters.map
	// ===============================================================

	ParametersIPtr ParametersI::add(const std::string& name,
					const omero::RTypePtr& r) {
	    this->map[name] = r;
	    return this;
	}


        ParametersIPtr ParametersI::addId(Ice::Long id) {
	    return add("id", rlong(id));
        }

	ParametersIPtr ParametersI::addId(const omero::RLongPtr& id) {
	    return add("id", id);
	}

	ParametersIPtr ParametersI::addIds(omero::sys::LongList ids) {
	    return addLongs("ids", ids);
	}

	ParametersIPtr ParametersI::addLong(const std::string& name,
					    Ice::Long l) {
	    return add(name, rlong(l));
	}

	ParametersIPtr ParametersI::addLong(const std::string& name,
					    const omero::RLongPtr& l) {
	    return add(name, l);
	}

	ParametersIPtr ParametersI::addLongs(const std::string& name,
					     omero::sys::LongList longs) {
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
