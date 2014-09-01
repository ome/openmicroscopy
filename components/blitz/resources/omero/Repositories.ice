/*
*   $Id$
*
*   Copyright 2009-2013 Glencoe Software, Inc. All rights reserved.
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
         * Base repository exception.
         */
        exception RepositoryException extends ServerError {

        };

        /**
         * Specifies that a file with the given path has failed to
         * be deleted from the file system.
         */
        exception FileDeleteException extends RepositoryException {

        };

        /**
         * Specifies that a file is located at the given location
         * that is not otherwise known by the repository. A
         * subsequent call to [Repository::register] will create
         * the given file. The mimetype field of the file may or
         * may not be set. If it is set, clients are suggested to
         * either omit the mimetype argument to the register method
         * or to pass the same value.
         */
        exception UnregisteredFileException extends RepositoryException {
            omero::model::OriginalFile file;
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
             * Returns true if the file or path exists within the repository.
             * In other words, if a call on `dirname path` to [listFiles] would
             * return an object for this path.
             **/
            bool fileExists(string path) throws ServerError;

            /**
             * Create a directory at the given path. If parents is true,
             * then all preceding paths will be generated and no exception
             * will be thrown if the directory already exists. Otherwise,
             * all parent directories must exist in both the DB and on the
             * filesystem and be readable.
             **/
            void makeDir(string path, bool parents) throws ServerError;

            /**
             * Similar to [list] but recursive and returns only primitive
             * values for the file at each location. Guaranteed for each
             * path is only the values id and mimetype.
             *
             * After a call to unwrap, the returned [omero::RMap] for a call
             * to treeList("/user_1/dir0") might look something like:
             *
             * <pre>
             *  {
             *      "/user_1/dir0/file1.txt" :
             *      {
             *          "id":10,
             *          "mimetype":
             *          "binary",
             *          "size": 10000L
             *      },
             *
             *      "/user_1/dir0/dir1" :
             *      {
             *          "id": 100,
             *          "mimetype": "Directory",
             *          "size": 0L,
             *          "files":
             *          {
             *              "/user_1/dir0/dir1/file1indir.txt" :
             *              {
             *                  "id": 1,
             *                  "mimetype": "png",
             *                  "size": 500
             *              }
             *           }
             *     }
             *  }
             * </pre>
             **/
            omero::RMap treeList(string path) throws ServerError;

            /**
             * Delete several individual paths. Internally, this converts
             * each of the paths into an [omero::cmd::Delete] command and
             * submits all of them via [omero::cmd::DoAll].
             *
             * If a "recursively" is true, then directories will be searched
             * and all of their contained files will be placed before them in
             * the delete order. When the directory is removed from the database,
             * it will removed from the filesystem if and only if it is empty.
             *
             * If "recursively" is false, then the delete will produce an error
             * according to the "force" flag.
             *
             * If "force" is false, this method attempts the delete of all given
             * paths in a single transaction, and any failure will cause the
             * entire transaction to fail.
             *
             * If "force" is true, however, then all the other deletes will succeed.
             * which could possibly leave dangling files within no longer extant
             * directories.
             *
             **/
            omero::cmd::Handle* deletePaths(omero::api::StringArray paths,
                                            bool recursively,
                                            bool force) throws ServerError;

        };

        /**
         * Returned by [ManagedRepository::importFileset] with
         * the information needed to proceed with an FS import.
         * For the examples that follow, assume that the used
         * files passed to importFileset were:
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
             * [ManagedRepository::importFileset] but will have
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

             /**
              * User choice of checksum algorithm for verifying upload.
              **/
             omero::model::ChecksumAlgorithm checksumAlgorithm;

             /**
              * If set, the [ImportProcess*] and the [Handle*] associated with
              * the import will be closed as soon as complete. This will prevent
              * clients from finding out the status of the import itself.
              **/

             /*
             DISABLE on 5.0.3
             bool autoClose;
             */
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
             * passes then a [omero::cmd::Handle] proxy is returned, which completes
             * all the necessary import steps. A successful import will return an
             * [ImportResponse]. Otherwise, some [omero::cmd::ERR] will be returned.
             **/
             omero::cmd::Handle* verifyUpload(omero::api::StringSet hash) throws ServerError;

            //
            // INTROSPECTION
            //

            /**
             * In case an upload must be resumed, this provides the
             * location of the last successful upload.
             **/
             long getUploadOffset(int i) throws ServerError;

            /**
             * Reacquire the handle which was returned by
             * [verifyUpload]. This is useful in case a new
             * client is re-attaching to a running import.
             * From the [omero::cmd::Handle] instance, the
             * original [ImportRequest] can also be found.
             **/
             omero::cmd::Handle* getHandle() throws ServerError;

             ImportSettings getImportSettings();

        };

        ["java:type:java.util.ArrayList<omero.grid.ImportProcessPrx>:java.util.List<omero.grid.ImportProcessPrx>"]
            sequence<ImportProcess*> ImportProcessList;

        /**
         * Command object which will be used to create
         * the [omero::cmd::Handle] instances passed
         * back by the [ImportProcess].
         **/
        class ImportRequest extends omero::cmd::Request {

            /**
             * Lookup value for the session that import is taking
             * part in.
             **/
             string clientUuid;

            /**
             * Repository which is responsible for this import.
             * All files which are uploaded will be available
             * from it.
             **/
             string repoUuid;

            /**
             * Proxy of the process which this request
             * will be running in. This value will be
             * filled in for possible later re-use, but
             * is not read by the server.
             **/
            ImportProcess* process;

            /**
             * Activity that this will be filling
             * out in the database. This always points to a
             * [omero::model::MetadataImportJob] which is the
             * first server-side phase after the [omero::model::UploadJob].
             **/
            omero::model::FilesetJobLink activity;

            /**
             * [ImportSettings] which are provided by the
             * client on the call to [ManagedRepository::importFileset].
             **/
             ImportSettings settings;

            /**
             * [ImportLocation] which is calculated during
             * the call to [ManagedRepository::importFileset].
             **/
             ImportLocation location;

            /**
             * [OriginalFile] object representing the import log file.
             **/
             omero::model::OriginalFile logFile;


        };

        /**
         * Successful response returned from execution
         * of [ImportRequest]. This is the simplest way
         * to return the results, but is likely not the
         * overall best strategy.
         **/
        class ImportResponse extends ::omero::cmd::Response {

            omero::api::PixelsList pixels;

            /**
             * Top-level OME-XML objects which are created
             * during the import. This will not contain any
             * pixels which were imported, but images, plates,
             * etc. which may be useful for user feedback.
             **/
            omero::api::IObjectList objects;
        };


        /**
         * FS-enabled repository which can convert uploaded files
         * into Images by using Bio-Formats to import them.
         **/
        ["ami"] interface ManagedRepository extends Repository {

            /**
             * Returns an [ImportProcess] which can be used to upload files.
             * On [ImportProcess::verifyUpload], an [omero::cmd::Handle] will be
             * returned which can be watched for knowing when the server-side import
             * is complete.
             *
             * Client paths set in the fileset entries must /-separate their components.
             *
             * Once the upload is complete, the [ImportProcess] must be closed.
             * Once [omero::cmd::Handle::getResponse] returns a non-null value, the
             * handle instance can and must be closed.
             **/
            ImportProcess* importFileset(omero::model::Fileset fs, ImportSettings settings) throws ServerError;

            /**
             * For clients without access to Bio-Formats, the simplified
             * [importPaths] method allows passing solely the absolute
             * path of the files to be uploaded (no directories) and all
             * configuration happens server-side. Much of the functionality
             * provided via [omero::model::Fileset] and [omero::grid::ImportSettings]
             * is of course lost.
             **/
            ImportProcess* importPaths(omero::api::StringSet filePaths) throws ServerError;

            /**
             * List imports that are currently running in this importer.
             * These will be limited based on user/group membership for
             * the [omero::model::Fileset] object which is being created
             * by the import. If the user has write permissions for the
             * fileset, then the import will be included.
             **/
            ImportProcessList listImports() throws ServerError;

            /**
             * Return the list of checksum algorithms supported by this repository
             * for verifying the integrity of uploaded files.
             * They are named as "algorithm-integer",
             * integer being the bit width of the resulting hash code.
             * It is possible for the same algorithm to be offered with
             * different bit widths.
             * They are listed in descending order of preference,
             * as set by the server administrator, and any of them may
             * be specified for [ImportSettings::checksumAlgorithm].
             */
            omero::api::ChecksumAlgorithmList listChecksumAlgorithms();

            /**
             * Suggest a checksum algorithm to use for
             * [ImportSettings::checksumAlgorithm] according to the
             * preferences set by the server administrator. Provide a
             * list of the algorithms supported by the client, and the
             * server will report which of them is most preferred by
             * the server, or return null if none of them are supported.
             */
            omero::model::ChecksumAlgorithm suggestChecksumAlgorithm(omero::api::ChecksumAlgorithmList supported);

            /**
             * Verify the checksum for the original files identified by
             * the given IDs.
             * The files must be in this repository.
             * Returns the IDs of the original files whose checksums
             * do not match the file on disk.
             */
            omero::api::LongList verifyChecksums(omero::api::LongList ids)
                throws ServerError;

            /**
             * Set the checksum algorithm for the original files identified
             * by the given IDs and calculate their checksum accordingly.
             * The files must be in this repository.
             * Existing checksums are checked before being changed.
             * If a checksum does not match, ServerError will be thrown;
             * in this case some other files may already have had their
             * checksum algorithm set.
             * Returns the IDs of the original files that did not already
             * have a checksum set for the given algorithm.
             */
            omero::api::LongList setChecksumAlgorithm(omero::model::ChecksumAlgorithm hasher, omero::api::LongList ids)
                throws ServerError;
        };

        /**
         * Command object which will be parsed by the internal
         * repository given by "repo". This command will *only*
         * be processed if the user has sufficient rights (e.g.
         * is a member of "system") and is largely intended for
         * testing and diagnosis rather than actual client
         * functionality.
         **/
        class RawAccessRequest extends omero::cmd::Request {
            string repoUuid;
            string command;
            omero::api::StringSet args;
            string path;
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

            omero::cmd::Response rawAccess(RawAccessRequest raw) throws ServerError;

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
