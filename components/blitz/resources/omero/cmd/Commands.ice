/*
 *   $Id$
 *
 *   Copyright 2011 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#ifndef OMERO_CMD_COMMANDS_ICE
#define OMERO_CMD_COMMANDS_ICE

#include <omero/RTypes.ice>
#include <Glacier2/Session.ice>
#include <Ice/BuiltinSequences.ice>
#include <Ice/Identity.ice>

module omero {

    /**
     * Simplified API that is intended for passing
     **/
    module cmd {

        dictionary<string, string> OptionMap;

        class Command {
            string session;
            OptionMap options;
        };

        sequence<Command> CommandList;

        class Status {

        };

        sequence<Status> StatusList;

        interface Handler {
            CommandList commands();
            StatusList status();
            bool cancel();
            void close();
        };

	/**
	 * Starting point for all command-based OMERO.blitz interaction.
	 **/
	interface Session extends Glacier2::Session
	{

            /**
             * Returns all commands that this session is
             * configured to handle.
             **/
            CommandList availableCommands();

            /**
             *
             **/
             Handler* submit(CommandList commands);

	};

    };
};

#endif
