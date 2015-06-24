/*
 *   Copyright 2014 Unversity of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#ifndef OMERO_MODEL_OBJECTFACTORY_H
#define OMERO_MODEL_OBJECTFACTORY_H

#include <Ice/Ice.h>
#include <Ice/Communicator.h>
#include <IceUtil/IceUtil.h>

namespace omero {
    namespace model {

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
        void registerObjectFactory(const Ice::CommunicatorPtr& ic);

        void conditionalAdd(
                const std::string&,
                const Ice::CommunicatorPtr&,
                const Ice::ObjectFactoryPtr&);

    }
}

#endif // OMERO_MODEL_OBJECTFACTORY_H
