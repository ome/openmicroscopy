/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#ifndef OMERO_SYS_PARAMETERSI_H
#define OMERO_SYS_PARAMETERSI_H

#include <omero/IceNoWarnPush.h>
#include <omero/System.h>
#include <omero/IceNoWarnPop.h>
#include <IceUtil/Config.h>
#include <Ice/Handle.h>
#include <Ice/Config.h>
#include <iostream>
#include <string>
#include <vector>

#ifndef OMERO_CLIENT
#   ifdef OMERO_CLIENT_EXPORTS
#       define OMERO_CLIENT ICE_DECLSPEC_EXPORT
#   else
#       define OMERO_CLIENT ICE_DECLSPEC_IMPORT
#   endif
#endif

namespace omero {
    namespace sys {
        class OMERO_CLIENT ParametersI; // Forward
    }
}

namespace IceInternal {
  OMERO_CLIENT ::Ice::Object* upCast(::omero::sys::ParametersI*);
}

namespace omero {

    namespace sys {

	/**
	 * Ice versions 3.4.x and 3.5beta have a bug preventing the
	 * use of IceUtil::Handle (generated classes such as
	 * omero::sys::Parameters are derived from both
	 * IceUtil::Shared and IceInternal::GCShared which both
	 * provide __incRef() and __decRef() and so the Handle class
	 * can't call them without causing a compilation failure.
	 * Until this is fixed, using the internal handle type is a
	 * workaround.
	 */
        typedef IceInternal::Handle<ParametersI> ParametersIPtr;

        /*
         * Helper subclass of omero::sys::Parameters for simplifying method
	 * parameter creation.
         */
        class ParametersI : virtual public Parameters {

	protected:
	    ~ParametersI(); // protected as outlined in Ice docs.
	public:
	    /*
	     * If no argument is provided, creates an empty ParamMap for use
	     * by this instance.
	     *
	     * Uses (and does not copy) the given map as the named parameter
	     * store. Be careful if either null is passed in or if this instance
	     * is being used in a multi-threaded environment. No synchrnization
	     * takes place.
	     */
	    ParametersI(const omero::sys::ParamMap& map = omero::sys::ParamMap());

	    // Parameters.theFilter.limit & offset
	    // ===============================================================

	    /*
	     * Nulls both the Filter.limit and Filter.offset values.
	     */
	    ParametersIPtr noPage();

	    /*
	     * Sets both the Filter.limit and Filter.offset values by
	     * wrapping the arguments in omero::RInts and calling
	     * page(RIntPtr&, RIntPtr&)
	     */
	    ParametersIPtr page(Ice::Int offset, Ice::Int limit);

	    /*
	     * Creates a Filter if necessary and sets both Filter.limit
	     * and Filter.offset
	     */
	    ParametersIPtr page(const omero::RIntPtr& offset,
				const omero::RIntPtr& limit);

	    /*
	     * Returns true if the filter contains a limit OR an offset.
	     * false otherwise.
	     */
	    bool isPagination();

	    omero::RIntPtr getOffset();
	    omero::RIntPtr getLimit();
	    ParametersIPtr unique();
	    ParametersIPtr noUnique();
	    omero::RBoolPtr getUnique();

	    // Parameters.theFilter.ownerId & groupId
	    // ===============================================================

	    ParametersIPtr exp(Ice::Long id);
	    ParametersIPtr allExps();
	    bool isExperimenter();
	    omero::RLongPtr getExperimenter();

	    ParametersIPtr grp(Ice::Long id);
	    ParametersIPtr allGrps();
	    bool isGroup();
	    omero::RLongPtr getGroup();

	    // Parameters.theFilter.starttime, endTime
	    // ===============================================================

	    ParametersIPtr startTime(const omero::RTimePtr& time);
	    ParametersIPtr endTime(const omero::RTimePtr& time);
	    ParametersIPtr allTimes();
	    bool isStartTime();
	    bool isEndTime();
	    omero::RTimePtr getStartTime();
	    omero::RTimePtr getEndTime();

	    // Parameters.theOptions.leaves, orphan, acquisitionData
	    // ===============================================================

	    ParametersIPtr leaves();
	    ParametersIPtr noLeaves();
	    omero::RBoolPtr getLeaves();

	    ParametersIPtr orphan();
	    ParametersIPtr noOrphan();
	    omero::RBoolPtr getOrphan();

	    ParametersIPtr acquisitionData();
	    ParametersIPtr noAcquisitionData();
	    omero::RBoolPtr getAcquisitionData();

	    // Parameters.map
	    // ===============================================================

	    ParametersIPtr add(const std::string& name,
			       const omero::RTypePtr& r);
	    ParametersIPtr addId(Ice::Long id);
	    ParametersIPtr addId(const omero::RLongPtr& id);
	    ParametersIPtr addIds(omero::sys::LongList ids);
	    ParametersIPtr addLong(const std::string& name, Ice::Long l);
	    ParametersIPtr addLong(const std::string& name,
				   const omero::RLongPtr& l);
	    ParametersIPtr addLongs(const std::string& name,
				    omero::sys::LongList longs);

	};

    } // sys

} // omero
#endif // OMERO_SYS_PARAMETERSI_H
