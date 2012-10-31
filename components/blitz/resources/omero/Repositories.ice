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

        /**
         * Information passed back and forth during import.
         * Needs to be reviewed.
         **/
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

            //
            // Repository-level methods not requiring any particular
            // security method.
            //

            /**
             * Return the OriginalFile descriptor for this Repository. It will have
             * the path "/"
             **/
            omero::model::OriginalFile root() throws ServerError;

            //
            // Path-based methods which require a look-up in the
            // OriginalFile table.
            //

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

            omero::api::RawFileStore* fileById(long id) throws ServerError;

            /**
             * Returns true if the file or path exists within the repository
             **/
            bool fileExists(string path) throws ServerError;

            ["deprecated:currently for testing only"] bool create(string path) throws ServerError;
            void makeDir(string path) throws ServerError;

            /**
             * Delete the path at the given location. If the file cannot be deleted
             * for operating system reasons, a false will be returned, otherwise true.
             * If a deletion is not permitted, then an exception will be thrown.
             **/
            bool delete(string path) throws ServerError;

            /**
             * Delete several individual paths as with [delete] but rather than
             * a single boolean return all the paths for which a delete is not
             * possible. If [delete] would throw, so would this method.
             **/
            omero::api::StringSet deleteFiles(omero::api::StringArray paths) throws ServerError;

        };

        /**
         * Returned by [ManagedRepository::prepareUpload] with
         * the information needed to proceed with an FS import.
         * For the examples that follow, assume that the used
         * files passed to prepareUpload were:
         *
         * <pre>
         *  /Users/jack/Documents/Data/Experiment-1/1.dv
         *  /Users/jack/Documents/Data/Experiment-1/1.dv.log
         *  /Users/jack/Documents/Data/Experiment-2/2.dv
         *  /Users/jack/Documents/Data/Experiment-2/2.dv.log
         * </pre>
         *
         **/
        class Import {

            /**
             * The shared base of all the paths passed to
             * the server.
             **/
            string sharedPath;

            /**
             * Number of directories which have been omitted
             * from the original paths passed to the server.
             **/
            int omittedLevels;

            /**
             * Parsed string names which should be used by the
             * clients during upload. This array will be of the
             * same length as the argument passed to
             * [ManagedRepository::prepareUpload] but will have
             * shortened paths.
             *
             * <pre>
             *  Experiment/1.dv
             *  Experiment/1.dv.log
             * </pre>
             **/
            omero::api::StringSet usedFiles;

            /**
             * Represents the directory to which all files
             * will be uploaded.
             **/
            omero::model::OriginalFile directory;

            /**
             * A map of absolute path to uploaded original files.
             **/
            omero::api::OriginalFileMap originalFileMap;

        };

        /**
         * FS-enabled repository which can convert uploaded files
         * into Images by using Bio-Formats to import them.
         **/
        ["ami"] interface ManagedRepository extends Repository {

            /**
             * Returns the directory which should be the import location for
             * the set of paths passed in. Each set of paths consitutes a
             * single import session. In order to prevent files from being
             * overwritten or interfering with one another, a new directory
             * may be created for the current session.
             **/
            Import prepareImport(omero::api::StringSet paths) throws ServerError;

            /**
             *
             **/
            omero::api::RawFileStore* uploadUsedFile(Import importData, string usedFile) throws ServerError;

           /**
             * Return the absolute path of a file.
             * If this is needed outside of import it might be moved to PublicRepository
             **/
            string getAbsolutePath(string path) throws ServerError;

           /**
             * Create an OriginalFile object to represent an uploaded file.
             **/
            omero::model::OriginalFile createOriginalFile(string path) throws ServerError;

            /**
             * This will free any locks, etc in the Up
             **/
            omero::api::PixelsList importMetadata(Import importData, RepositoryImportContainer ic) throws ServerError;

            /**
             * If the user cancels the import, then this method should
             * be called in order to delete any dangling files and free up resources.
             **/
            void cancelImport(Import importData) throws ServerError;


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
         * The descriptions and proxies arrays will have the same size and each
         * index in descriptions (non-null) will match a possibly null proxy, if
         * the given repository is not currently accessible.
         */
        struct RepositoryMap {
            omero::api::OriginalFileList descriptions;
            RepositoryProxyList proxies;
        };

    };

};

#endif
