/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#ifndef OMERO_OBJECTFACTORYREGISTRAR_H
#define OMERO_OBJECTFACTORYREGISTRAR_H

#include <string>
#include <Ice/Ice.h>
#include <IceUtil/IceUtil.h>

namespace omero {

    void conditionalAdd(const std::string& name, const Ice::CommunicatorPtr& ic, const Ice::ObjectFactoryPtr& of);

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
    void registerObjectFactory(const Ice::CommunicatorPtr ic);

}

#endif // OMERO_OBJECTFACTORYREGISTRAR_H
