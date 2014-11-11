/*
 *   Copyright 2014 Unversity of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#ifndef OMERO_OBJECTFACTORY_H
#define OMERO_OBJECTFACTORY_H

#include <Ice/Ice.h>
#include <IceUtil/IceUtil.h>
#include <omero/clientF.h>

namespace omero {

    /*
     * Responsible for creating model instances based
     * on string representations of their type. An
     * instance of this class can take an Ice::Communicator
     * and add itself as the ObjectFactory to be used
     * for all known types. If another type has already
     * been registered, this instance will not register
     * itself. (Normal Ice logic is to throw an exception
     * if a type has already been registered.)
     */
  void registerObjectFactory(const Ice::CommunicatorPtr& ic,
                             const omero::client* client);

}

#endif // OMERO_OBJECTFACTORY_H
