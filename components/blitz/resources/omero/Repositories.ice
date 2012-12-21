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
#include <omero/cmd/API.ice>

module omero {

    //
    // See README.ice for a description of this module.
    //
    module grid {

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
        class ImportLocation {

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

        };

        /**
         * User configuration options. These are likely set in the UI
         * before the import is initiated.
         **/
        class ImportSettings {

            /**
             * The container which this object should be added to.
             **/
             omero::model::IObject userSpecifiedTarget;

            /**
             * Custom name suggested by the user.
             **/
             omero::RString userSpecifiedName;

            /**
             * Custom description suggested by the user.
             **/
             omero::RString userSpecifiedDescription;

            /**
             * User choice of pixels sizes.
             **/
             omero::api::DoubleArray userSpecifiedPixels;

             /**
              * Annotations that the user
              **/
             omero::api::AnnotationList userSpecifiedAnnotationList;

             /**
              * Whether or not the thumbnailing action should be performed.
              **/
             omero::RBool doThumbnails;

        };


        /**
         * User configuration options. These are likely set in the UI
         * before the import is initiated.
         **/
        interface ImportProcess extends omero::api::StatefulServiceInterface{

            //
            // PRIMARY WORKFLOW
            //

            /**
             * Step 1: Returns a RawFileStore that can be used to upload one of
             * the used files. The index is the same as the used file listed in
             * [ImportLocation]. [omero::api::RawFileStore::close] should be
             * called once all data has been transferred. If the file must be
             * re-written, call [getUploader] with the same index again. Once
             * all uploads have been completed, [verifyUpload] should be called
             * to initiate background processing
             **/
             omero::api::RawFileStore* getUploader(int i) throws ServerError;

            /**
             * Step 2: Passes a set of client-side calculated hashes to the server
             * for verifying that all of the files were correctly uploaded. If this
             * passes then [omero::cmd::Handle*] proxies are returned, each of which
             * represents a step of the import. Each will be run in turn. Calling
             * [pauseImport] will prevent the next step from completing, but it's
             * not possible to pause the running activity.
             **/
             omero::cmd::HandleList verifyUpload(omero::api::StringSet hash) throws ServerError;

            // what happens if close is called on this instance?
            // permit skipVerification()?
            // Get pixels here or on the cmd/status objects
            // What happens if there's not a thread for the session heartbeat?
            //    single heartbeat for all of the managedrepo, or endless sessions?

            /**
             * Pauses future activities from starting. If one is running,
             * the argument will determine whether to wait for the current
             * activity to finish. If the activity is taking too long, an
             * [omero::LockTimeout] will be thrown. If pausing is otherwise
             * unsuccessful, then a false will be returned.
             **/
            bool pauseImport(bool wait) throws ServerError;

            /**
             * Further import actions should be resumed.
             **/
            void resumeImport() throws ServerError;

            /**
             * If the user wishes to cancel the import, then this method should be
             * called in order to delete any dangling files and free up
             * resources.
             **/
            void cancelImport() throws ServerError;

            //
            // INTROSPECTION
            //

            /**
             * In case an upload must be resumed, this provides the
             * location of the last successful upload.
             **/
             long getUploadOffset(int i) throws ServerError;

            /**
             * Returns the session that the import is taking part in.
             **/
             string getSession() throws ServerError;

            /**
             * Defines what the server knows about the files to be
             * uploaded, where they are going, etc.
             **/
             ImportLocation getLocation() throws ServerError;

            /**
             * Returns the original settings that were set
             * on creating the process
             **/
             ImportSettings getSettings() throws ServerError;

            /**
             * Once all the uploads have been taken care of
             * the server will begin the process of importing
             * the data and performing other activities like
             * thumbnailing, etc. This returns the current
             * step of the import.
             *
             * Might be null if no activity is currently running.
             * See [pauseImport].
             **/
             omero::cmd::Handle* getCurrentActivity() throws ServerError;

        };

        /**
         * Command object which will be used to create
         * the [omero::cmd::Handle*] instances passed
         * back by the [ImportProcess].
         **/
        class ImportRequest extends omero::cmd::Request {

            /**
             * Proxy of the process which this request
             * will be running in.
             **/
            ImportProcess* process;

            /**
             * Activity that this will be filling
             * out in the database.
             **/
            omero::model::FilesetActivity activity;

        };

        /**
         * Successful response returned from execution
         * of [ImportRequest]. This is the simplest way
         * to return the results, but is likely not the
         * overall best strategy.
         **/
        class ImportResponse extends ::omero::cmd::Response {
            omero::api::PixelsList pixels;
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
            ImportProcess* prepareImport(omero::model::Fileset fs, ImportSettings settings) throws ServerError;

           /**
             * Create an OriginalFile object to represent an uploaded file.
             **/
            omero::model::OriginalFile createOriginalFile(string path) throws ServerError;

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
