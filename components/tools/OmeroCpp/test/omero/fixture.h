/*
 *   Copyright 2007-2012 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#ifndef OMERO_FIXTURE_H
#define OMERO_FIXTURE_H

// domain
#include <omero/all.h>
#include <omero/client.h>
#include <omero/RTypesI.h>
#include <omero/ClientErrors.h>
#include <omero/ServerErrors.h>
#include <omero/model/ImageI.h>
#include <omero/model/PixelsI.h>
#include <omero/model/ExperimenterI.h>

// gtest
#include "gtest/gtest.h"

#ifdef LINUX
// stackframe
#include <execinfo.h>
#endif

// std
#include <exception>
#include <cstdlib>
#include <string>
#include <vector>

omero::model::ImagePtr new_ImageI();

struct Fixture
{
    protected:
        std::vector<omero::client*> clients;
    public:

        /**
         * Creates and fills the "root" client_ptr
         * for later use. This will be closed during
         * destruction or logout.
         */
        Fixture();

        // Fields which should always be present
        omero::client_ptr root;
        omero::client_ptr client;

        // Data graphs
        omero::model::PixelsIPtr pixels();

        // Semi-working debugging tools
        void show_stackframe();
        void printUnexpected();

        /*
         *
         */
        std::string uuid();

        /*
         * Create a new omero::client object by logging
         * in with the username and password. For most
         * users created via Fixture::newUser no password
         * is necessary. The created login will be stored
         * as "client".
         */
        void login(
                const std::string& username = std::string(),
                const std::string& password = std::string());

        /*
         * Like login(username, password) but takes an experimenter
         * object and calls ->getOmeName()->getValue() for you.
         */
        void login(
                const omero::model::ExperimenterPtr& user,
                const std::string& password = std::string());

        /**
         * Close and null the "root" and "client" fields.
         */
        void logout();

        /*
         *
         */
        omero::model::ExperimenterPtr newUser(
                const omero::model::ExperimenterGroupPtr& g = omero::model::ExperimenterGroupPtr());

        /*
         *
         */
        omero::model::ExperimenterGroupPtr newGroup(const std::string& perms = "");

        /*
         *
         */
        void addExperimenter(
                const omero::model::ExperimenterGroupPtr& group,
                const omero::model::ExperimenterPtr& user);
};

#endif // OMERO_FIXTURE_H
