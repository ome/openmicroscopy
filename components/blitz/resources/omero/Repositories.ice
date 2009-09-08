/*
*   $Id$
*
*   Copyright 2009 Glencoe Software, Inc. All rights reserved.
*   Use is subject to license terms supplied in LICENSE.txt
*
*/

#ifndef OMERO_REPOSITORY_ICE
#define OMERO_REPOSITORY_ICE

#include <omero/ModelF.ice>
#include <omero/Collections.ice>
#include <omero/ServerErrors.ice>

module omero {

// Forward declarations
module api {
    interface RawFileStore;
    interface RawPixelsStore;
    interface RenderingEngine;
    interface ThumbnailStore;
};

/*
    * Interfaces and types running the backend.
    * Used by OMERO.blitz to manage the public
    * omero.api types.
    */
module grid {

    interface Repository {

        // Return the OriginalFile descriptor for this Repository. It will have
        // the path "/"
        omero::model::OriginalFile root() throws ServerError;

        //
        // Basic directory listing methods. This needs to be flushed out.
        // Possibly OriginalFile map or some special structure, since we
        // need to know what is an original file and what is not yet.
        //

        // TODO should we return OriginalFiles here for acmTime, etc.
        omero::api::StringSet list(string path) throws ServerError;
        omero::api::StringSet listDirs(string path) throws ServerError;
        omero::api::StringSet listFiles(string path) throws ServerError;

        // These list methods provide only registered files
        omero::api::OriginalFileList listKnown(string path) throws ServerError;
        omero::api::OriginalFileList listKnownDirs(string path) throws ServerError;
        omero::api::OriginalFileList listKnownFiles(string path) throws ServerError;

        // Or do we use an options object here?


        /**
            * Create an OriginalFile in the database for the given path.
            * If the given path is already registered as an OriginalFile,
            * a ValidationException is thrown. Otherwise, one is added and
            * returned.
            *
            * TODO should this just return and not throw?
            *
            */
        omero::model::OriginalFile register(string path, omero::model::Format fmt) throws ServerError;

        /**
            * Load the OriginalFile at the given path with annotations and
            * associated Pixels (if present). If the path does not point to
            * an OriginalFile, a ValidationException exception is thrown.
            *
            * TODO should this just return null instead?
            */
        omero::model::OriginalFile load(string path) throws ServerError;

        /**
            * Returns a special RawFileStore which permits only reading.
            * Any call to a write or configuration method will throw an
            * ApiUsageException.
            */
        omero::api::RawFileStore* read(string path) throws ServerError;

        /**
            * Returns a special RawFileStore which permits only writing.
            * Any call to a read or configuraiton method will throw an
            * ApiUsageException. If a file exists at the given path, a
            * ValidationException will be thrown. Once writing is complete,
            * call close(), which will seal the file from all further writing.
            * The SHA1 of the OriginalFile should be checked against the local
            * value.
            */
        omero::api::RawFileStore*    write(string path) throws ServerError;
        omero::api::RawPixelsStore*  pixels(string path) throws ServerError;
        omero::api::RenderingEngine* render(string path) throws ServerError;
        omero::api::ThumbnailStore*  thumbs(string path) throws ServerError;

        void rename(string path) throws ServerError;
        void delete(string path) throws ServerError;
        void transfer(string srcPath, Repository* target, string targetPath) throws ServerError;

    };

    /**
        * Internal portion of the API used for management. Not available to clients.
        */
    interface InternalRepository {

        //
        // Provides all the stateful services dealing with binary data
        //
        omero::api::RawFileStore*    createRawFileStore(omero::model::OriginalFile file) throws ServerError;
        omero::api::RawPixelsStore*  createRawPixelsStore(omero::model::OriginalFile file) throws ServerError;
        omero::api::RenderingEngine* createRenderingEngine(omero::model::OriginalFile file) throws ServerError;
        omero::api::ThumbnailStore*  createThumbnailStore(omero::model::OriginalFile file) throws ServerError;

        // Other repository methods
        omero::model::OriginalFile   getDescription() throws ServerError;
        Repository*                  getProxy() throws ServerError;  // If this returns null, user will have to wait

        string                       getFilePath(omero::model::OriginalFile file) throws ServerError;

    };

    // Return value for ServiceFactory.acquireRepositories()
    ["java:type:java.util.ArrayList<omero.grid.RepositoryPrx>:java.util.List<omero.grid.RepositoryPrx>"]
    sequence<Repository*> RepositoryProxyList;
    struct RepositoryMap {
        omero::api::OriginalFileList descriptions;
        RepositoryProxyList proxies;
    };


};


};

#endif
