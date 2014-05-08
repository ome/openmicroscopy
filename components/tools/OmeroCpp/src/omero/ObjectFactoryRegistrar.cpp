/*
 *   Copyright 2006-2014 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#include <omero/ObjectFactoryRegistrar.h>

namespace omero {

  void conditionalAdd(const std::string& name, const Ice::CommunicatorPtr& ic, const Ice::ObjectFactoryPtr& of) {

    if (0==ic->findObjectFactory(name)) {
      ic->addObjectFactory(of, name);
    }

  }

} // End omero
