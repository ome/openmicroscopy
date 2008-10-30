/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#ifndef OMERO_SYS_PARAMETERSI_H
#define OMERO_SYS_PARAMETERSI_H

#include <omero/System.h>
#include <IceUtil/Handle.h>
#include <Ice/Config.h>
#include <iostream>
#include <string>
#include <vector>

namespace omero {

    namespace sys {

        class ParametersI; // Forward
        typedef IceUtil::Handle<ParametersI> ParametersIPtr;

        /*
         *
         */
        class ParametersI : virtual public Parameters {

	protected:
	    ~ParametersI(); // protected as outlined in Ice docs.
	public:
	    ParametersI(const omero::sys::ParamMap& map = omero::sys::ParamMap());

	    //
	    // Parameters.theFilter
	    //

	    ParametersIPtr noPage(const Ice::Current& c = Ice::Current());
	    ParametersIPtr page(Ice::Int offset, Ice::Int limit,
				const Ice::Current& c = Ice::Current());
	    ParametersIPtr page(const omero::RIntPtr& offset,
				const omero::RIntPtr& limit,
				const Ice::Current& c = Ice::Current());

	    //
	    // Parameters.map
	    //

	    ParametersIPtr add(const std::string& name,
			       const omero::RTypePtr& r,
			       const Ice::Current& c = Ice::Current());
	    ParametersIPtr addId(Ice::Long id,
				 const Ice::Current& c = Ice::Current());
	    ParametersIPtr addId(const omero::RLongPtr& id,
				 const Ice::Current& c = Ice::Current());
	    ParametersIPtr addIds(omero::sys::LongList ids,
				  const Ice::Current& c = Ice::Current());
	    ParametersIPtr addLong(const std::string& name,
			       Ice::Long l,
			       const Ice::Current& c = Ice::Current());
	    ParametersIPtr addLong(const std::string& name,
			       const omero::RLongPtr& l,
			       const Ice::Current& c = Ice::Current());
	    ParametersIPtr addLongs(const std::string& name,
				    omero::sys::LongList longs,
				    const Ice::Current& c = Ice::Current());

	};

    } // sys

} // omero
#endif // OMERO_SYS_PARAMETERSI_H
