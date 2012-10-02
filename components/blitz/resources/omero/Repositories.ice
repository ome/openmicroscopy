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
#include <omero/ServicesF.ice>
#include <omero/System.ice>
#include <omero/Collections.ice>
#include <omero/ServerErrors.ice>

module omero {

    //
    // See README.ice for a description of this module.
    //
    module grid {
        
        class RepositoryImportContainer
        {
            string file;
            long projectId;
            omero::model::IObject target;
            string reader;
            omero::api::StringArray usedFiles;
            bool isSPW;
            int bfImageCount;
            omero::api::PixelsList bfPixels;
            omero::api::StringSet bfImageNames;
            omero::api::DoubleArray userPixels;
            string customImageName;
            string customImageDescription;
            string customPlateName;
            string customPlateDescription;
            bool doThumbnails;
            omero::api::AnnotationList customAnnotationList;
        };

        /**
         * Client-accessible interface representing a single mount point on the server-side.
         **/
        ["ami"] interface Repository {

            /**
             * Return the OriginalFile descriptor for this Repository. It will have
             * the path "/"
             **/
            omero::model::OriginalFile root() throws ServerError;

            /**
             * Returns the best-guess mimetype for the given path.
             *
             **/
            string mimetype(string path) throws ServerError;

            /**
             * Returns a set of strings naming the files and directories in
             * the directory denoted by an abstract pathname.
             **/
            omero::api::StringSet list(string path) throws ServerError;

            /**
             * Returns an array of abstract pathanam objects denoting the
             * files in the directory denoted by an abstract pathname.  It
             * is expected that at a minimum the "name", "path", "size" and
             * "mtime" attributes will be present for each
             * [omero::model::OriginalFile] instance.
             **/
            omero::api::OriginalFileList listFiles(string path)
                    throws ServerError;

            /**
             * Create an OriginalFile in the database for the given path.
             *
             **/
            omero::model::OriginalFile register(string path, omero::RString mimetype)
                    throws ServerError;

            /**
             * Load the OriginalFile at the given path with annotations and
             * associated Pixels (if present). If the path does not point to
             * an OriginalFile, a ValidationException exception is thrown.
             *
             * TODO should this just return null instead?
             **/
            omero::model::OriginalFile load(string path) throws ServerError;

            /**
             * Returns a special RawFileStore which permits only the operations
             * set out in the options string "wb", "a+", etc.
             * FIXME: Initially only "r" and "rw" are supported as these are
             * handled directly by RandomAccessFile and so don't break the current
             * implementation.
             * Any call to that tries to break the options will throw an
             * ApiUsageException. If a file exists at the given path, a
             * ValidationException will be thrown.
             **/
            omero::api::RawFileStore* file(string path, string mode) throws ServerError;

            omero::api::RawPixelsStore*  pixels(string path) throws ServerError;
            omero::api::RenderingEngine* render(string path) throws ServerError;
            omero::api::ThumbnailStore*  thumbs(string path) throws ServerError;
            
            omero::api::RawFileStore* fileById(long id) throws ServerError;
            
            /**
             * Returns true if the file or path exists within the repository
             **/
            bool fileExists(string path) throws ServerError;
            
            ["deprecated:currently for testing only"] bool create(string path) throws ServerError;
            void makeDir(string path) throws ServerError;
            void rename(string path) throws ServerError;
            void delete(string path) throws ServerError;
            void transfer(string srcPath, Repository* target, string targetPath) 
                    throws ServerError;

            omero::api::PixelsList importMetadata(RepositoryImportContainer ic) throws ServerError;

            omero::api::StringSet getCurrentRepoDir(omero::api::StringSet paths) throws ServerError;

            omero::api::StringSet deleteFiles(omero::api::StringArray paths) throws ServerError;

        };

        /**
         * Internal portion of the API used for management. Not available to clients.
         **/
        ["ami"] interface InternalRepository {

            //
            // Provides all the stateful services dealing with binary data
            //
            omero::api::RawFileStore*    createRawFileStore(omero::model::OriginalFile file) 
                    throws ServerError;
            omero::api::RawPixelsStore*  createRawPixelsStore(omero::model::OriginalFile file) 
                    throws ServerError;
            omero::api::RenderingEngine* createRenderingEngine(omero::model::OriginalFile file) 
                    throws ServerError;
            omero::api::ThumbnailStore*  createThumbnailStore(omero::model::OriginalFile file) 
                    throws ServerError;

            // Other repository methods
            omero::model::OriginalFile getDescription() throws ServerError;
            // If this returns null, user will have to wait
            Repository* getProxy() throws ServerError;  

            string getFilePath(omero::model::OriginalFile file) 
                    throws ServerError;

        };

        ["java:type:java.util.ArrayList<omero.grid.RepositoryPrx>:java.util.List<omero.grid.RepositoryPrx>"]
            sequence<Repository*> RepositoryProxyList;

        /**
         * Return value for [omero::grid::SharedResources].acquireRepositories()
         */
        struct RepositoryMap {
            omero::api::OriginalFileList descriptions;
            RepositoryProxyList proxies;
        };


};


};

#endif
