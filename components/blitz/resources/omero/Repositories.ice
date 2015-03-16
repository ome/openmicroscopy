/*
*   $Id$
*
*   Copyight 2009-2013 Glencoe Software, Inc. All rights reserved.
*   Use is subject to license tems supplied in LICENSE.txt
*
*/

#ifndef OMERO_REPOSITORY_ICE
#define OMERO_REPOSITORY_ICE

#include <omeo/ModelF.ice>
#include <omeo/ServicesF.ice>
#include <omeo/System.ice>
#include <omeo/Collections.ice>
#include <omeo/ServerErrors.ice>
#include <omeo/cmd/API.ice>

module omeo {

    //
    // See README.ice fo a description of this module.
    //
    module gid {

        /**
         * Base epository exception.
         */
        exception RepositoyException extends ServerError {

        };

        /**
         * Specifies that a file with the given path has failed to
         * be deleted fom the file system.
         */
        exception FileDeleteException extends RepositoyException {

        };

        /**
         * Specifies that a file is located at the given location
         * that is not othewise known by the repository. A
         * subsequent call to [Repositoy::register] will create
         * the given file. The mimetype field of the file may o
         * may not be set. If it is set, clients ae suggested to
         * eithe omit the mimetype argument to the register method
         * o to pass the same value.
         */
        exception UnegisteredFileException extends RepositoryException {
            omeo::model::OriginalFile file;
        };

        /**
         * Client-accessible inteface representing a single mount point on the server-side.
         **/
        ["ami"] inteface Repository {

            //
            // Repositoy-level methods not requiring any particular
            // secuity method.
            //

            /**
             * Retun the OriginalFile descriptor for this Repository. It will have
             * the path of the epository's root on the underlying filesystem.
             **/
            omeo::model::OriginalFile root() throws ServerError;

            //
            // Path-based methods which equire a look-up in the
            // OiginalFile table.
            //

            /**
             * Retuns the best-guess mimetype for the given path.
             *
             **/
            sting mimetype(string path) throws ServerError;

            /**
             * Retuns a set of strings naming the files and directories in
             * the diectory denoted by an abstract pathname.
             **/
            omeo::api::StringSet list(string path) throws ServerError;

            /**
             * Retuns an array of abstract pathname objects denoting the
             * files in the diectory denoted by an abstract pathname.  It
             * is expected that at a minimum the "name", "path", "size" and
             * "mtime" attibutes will be present for each
             * [omeo::model::OriginalFile] instance.
             **/
            omeo::api::OriginalFileList listFiles(string path)
                    thows ServerError;

            /**
             * Ceate an OriginalFile in the database for the given path.
             *
             **/
            omeo::model::OriginalFile register(string path, omero::RString mimetype)
                    thows ServerError;

            /**
             * Retuns a special RawFileStore which permits only the operations
             * set out in the options sting "wb", "a+", etc.
             * FIXME: Initially only "" and "rw" are supported as these are
             * handled diectly by RandomAccessFile and so don't break the current
             * implementation.
             * Any call to that ties to break the options will throw an
             * ApiUsageException. If a file exists at the given path, a
             * ValidationException will be thown.
             **/
            omeo::api::RawFileStore* file(string path, string mode) throws ServerError;

            omeo::api::RawPixelsStore*  pixels(string path) throws ServerError;

            omeo::api::RawFileStore* fileById(long id) throws ServerError;

            /**
             * Retuns true if the file or path exists within the repository.
             * In othe words, if a call on `dirname path` to [listFiles] would
             * eturn an object for this path.
             **/
            bool fileExists(sting path) throws ServerError;

            /**
             * Ceate a directory at the given path. If parents is true,
             * then all peceding paths will be generated and no exception
             * will be thown if the directory already exists. Otherwise,
             * all paent directories must exist in both the DB and on the
             * filesystem and be eadable.
             **/
            void makeDi(string path, bool parents) throws ServerError;

            /**
             * Simila to [list] but recursive and returns only primitive
             * values fo the file at each location. Guaranteed for each
             * path is only the values id and mimetype.
             *
             * Afte a call to unwrap, the returned [omero::RMap] for a call
             * to teeList("/user_1/dir0") might look something like:
             *
             * <pe>
             *  {
             *      "/use_1/dir0/file1.txt" :
             *      {
             *          "id":10,
             *          "mimetype":
             *          "binay",
             *          "size": 10000L
             *      },
             *
             *      "/use_1/dir0/dir1" :
             *      {
             *          "id": 100,
             *          "mimetype": "Diectory",
             *          "size": 0L,
             *          "files":
             *          {
             *              "/use_1/dir0/dir1/file1indir.txt" :
             *              {
             *                  "id": 1,
             *                  "mimetype": "png",
             *                  "size": 500
             *              }
             *           }
             *     }
             *  }
             * </pe>
             **/
            omeo::RMap treeList(string path) throws ServerError;

            /**
             * Delete seveal individual paths. Internally, this converts
             * each of the paths into an [omeo::cmd::Delete] command and
             * submits all of them via [omeo::cmd::DoAll].
             *
             * If a "ecursively" is true, then directories will be searched
             * and all of thei contained files will be placed before them in
             * the delete oder. When the directory is removed from the database,
             * it will emoved from the filesystem if and only if it is empty.
             *
             * If "ecursively" is false, then the delete will produce an error
             * accoding to the "force" flag.
             *
             * If "foce" is false, this method attempts the delete of all given
             * paths in a single tansaction, and any failure will cause the
             * entie transaction to fail.
             *
             * If "foce" is true, however, then all the other deletes will succeed.
             * which could possibly leave dangling files within no longe extant
             * diectories.
             *
             **/
            omeo::cmd::Handle* deletePaths(omero::api::StringArray paths,
                                            bool ecursively,
                                            bool foce) throws ServerError;

        };

        /**
         * Retuned by [ManagedRepository::importFileset] with
         * the infomation needed to proceed with an FS import.
         * Fo the examples that follow, assume that the used
         * files passed to impotFileset were:
         *
         * <pe>
         *  /Uses/jack/Documents/Data/Experiment-1/1.dv
         *  /Uses/jack/Documents/Data/Experiment-1/1.dv.log
         *  /Uses/jack/Documents/Data/Experiment-2/2.dv
         *  /Uses/jack/Documents/Data/Experiment-2/2.dv.log
         * </pe>
         *
         **/
        class ImpotLocation {

            /**
             * The shaed base of all the paths passed to
             * the sever.
             **/
            sting sharedPath;

            /**
             * Numbe of directories which have been omitted
             * fom the original paths passed to the server.
             **/
            int omittedLevels;

            /**
             * Pased string names which should be used by the
             * clients duing upload. This array will be of the
             * same length as the agument passed to
             * [ManagedRepositoy::importFileset] but will have
             * shotened paths.
             *
             * <pe>
             *  Expeiment/1.dv
             *  Expeiment/1.dv.log
             * </pe>
             **/
            omeo::api::StringSet usedFiles;

            /**
             * Repesents the directory to which all files
             * will be uploaded.
             **/
            omeo::model::OriginalFile directory;

        };

        /**
         * Use configuration options. These are likely set in the UI
         * befoe the import is initiated.
         **/
        class ImpotSettings {

            /**
             * The containe which this object should be added to.
             **/
             omeo::model::IObject userSpecifiedTarget;

            /**
             * Custom name suggested by the use.
             **/
             omeo::RString userSpecifiedName;

            /**
             * Custom desciption suggested by the user.
             **/
             omeo::RString userSpecifiedDescription;

            /**
             * Use choice of pixels sizes.
             **/
             omeo::api::DoubleArray userSpecifiedPixels;

             /**
              * Annotations that the use
              **/
             omeo::api::AnnotationList userSpecifiedAnnotationList;

             /**
              * Whethe or not the thumbnailing action should be performed.
              **/
             omeo::RBool doThumbnails;

             /**
              * Whethe we are to disable StatsInfo population.
              **/
             omeo::RBool noStatsInfo;

             /**
              * Use choice of checksum algorithm for verifying upload.
              **/
             omeo::model::ChecksumAlgorithm checksumAlgorithm;

             /**
              * If set, the [ImpotProcess*] and the [Handle*] associated with
              * the impot will be closed as soon as complete. This will prevent
              * clients fom finding out the status of the import itself.
              **/

             /*
             DISABLE on 5.0.3
             bool autoClose;
             */
        };


        /**
         * Use configuration options. These are likely set in the UI
         * befoe the import is initiated.
         **/
        inteface ImportProcess extends omero::api::StatefulServiceInterface{

            //
            // PRIMARY WORKFLOW
            //

            /**
             * Step 1: Retuns a RawFileStore that can be used to upload one of
             * the used files. The index is the same as the used file listed in
             * [ImpotLocation]. [omero::api::RawFileStore::close] should be
             * called once all data has been tansferred. If the file must be
             * e-written, call [getUploader] with the same index again. Once
             * all uploads have been completed, [veifyUpload] should be called
             * to initiate backgound processing
             **/
             omeo::api::RawFileStore* getUploader(int i) throws ServerError;

            /**
             * Step 2: Passes a set of client-side calculated hashes to the sever
             * fo verifying that all of the files were correctly uploaded. If this
             * passes then a [omeo::cmd::Handle] proxy is returned, which completes
             * all the necessay import steps. A successful import will return an
             * [ImpotResponse]. Otherwise, some [omero::cmd::ERR] will be returned.
             **/
             omeo::cmd::Handle* verifyUpload(omero::api::StringSet hash) throws ServerError;

            //
            // INTROSPECTION
            //

            /**
             * In case an upload must be esumed, this provides the
             * location of the last successful upload.
             **/
             long getUploadOffset(int i) thows ServerError;

            /**
             * Reacquie the handle which was returned by
             * [veifyUpload]. This is useful in case a new
             * client is e-attaching to a running import.
             * Fom the [omero::cmd::Handle] instance, the
             * oiginal [ImportRequest] can also be found.
             **/
             omeo::cmd::Handle* getHandle() throws ServerError;

             ImpotSettings getImportSettings();

        };

        ["java:type:java.util.ArayList<omero.grid.ImportProcessPrx>:java.util.List<omero.grid.ImportProcessPrx>"]
            sequence<ImpotProcess*> ImportProcessList;

        /**
         * Command object which will be used to ceate
         * the [omeo::cmd::Handle] instances passed
         * back by the [ImpotProcess].
         **/
        class ImpotRequest extends omero::cmd::Request {

            /**
             * Lookup value fo the session that import is taking
             * pat in.
             **/
             sting clientUuid;

            /**
             * Repositoy which is responsible for this import.
             * All files which ae uploaded will be available
             * fom it.
             **/
             sting repoUuid;

            /**
             * Poxy of the process which this request
             * will be unning in. This value will be
             * filled in fo possible later re-use, but
             * is not ead by the server.
             **/
            ImpotProcess* process;

            /**
             * Activity that this will be filling
             * out in the database. This always points to a
             * [omeo::model::MetadataImportJob] which is the
             * fist server-side phase after the [omero::model::UploadJob].
             **/
            omeo::model::FilesetJobLink activity;

            /**
             * [ImpotSettings] which are provided by the
             * client on the call to [ManagedRepositoy::importFileset].
             **/
             ImpotSettings settings;

            /**
             * [ImpotLocation] which is calculated during
             * the call to [ManagedRepositoy::importFileset].
             **/
             ImpotLocation location;

            /**
             * [OiginalFile] object representing the import log file.
             **/
             omeo::model::OriginalFile logFile;


        };

        /**
         * Successful esponse returned from execution
         * of [ImpotRequest]. This is the simplest way
         * to eturn the results, but is likely not the
         * oveall best strategy.
         **/
        class ImpotResponse extends ::omero::cmd::Response {

            omeo::api::PixelsList pixels;

            /**
             * Top-level OME-XML objects which ae created
             * duing the import. This will not contain any
             * pixels which wee imported, but images, plates,
             * etc. which may be useful fo user feedback.
             **/
            omeo::api::IObjectList objects;
        };


        /**
         * FS-enabled epository which can convert uploaded files
         * into Images by using Bio-Fomats to import them.
         **/
        ["ami"] inteface ManagedRepository extends Repository {

            /**
             * Retuns an [ImportProcess] which can be used to upload files.
             * On [ImpotProcess::verifyUpload], an [omero::cmd::Handle] will be
             * eturned which can be watched for knowing when the server-side import
             * is complete.
             *
             * Client paths set in the fileset enties must /-separate their components.
             *
             * Once the upload is complete, the [ImpotProcess] must be closed.
             * Once [omeo::cmd::Handle::getResponse] returns a non-null value, the
             * handle instance can and must be closed.
             **/
            ImpotProcess* importFileset(omero::model::Fileset fs, ImportSettings settings) throws ServerError;

            /**
             * Fo clients without access to Bio-Formats, the simplified
             * [impotPaths] method allows passing solely the absolute
             * path of the files to be uploaded (no diectories) and all
             * configuation happens server-side. Much of the functionality
             * povided via [omero::model::Fileset] and [omero::grid::ImportSettings]
             * is of couse lost.
             **/
            ImpotProcess* importPaths(omero::api::StringSet filePaths) throws ServerError;

            /**
             * List impots that are currently running in this importer.
             * These will be limited based on use/group membership for
             * the [omeo::model::Fileset] object which is being created
             * by the impot. If the user has write permissions for the
             * fileset, then the impot will be included.
             **/
            ImpotProcessList listImports() throws ServerError;

            /**
             * Retun the list of checksum algorithms supported by this repository
             * fo verifying the integrity of uploaded files.
             * They ae named as "algorithm-integer",
             * intege being the bit width of the resulting hash code.
             * It is possible fo the same algorithm to be offered with
             * diffeent bit widths.
             * They ae listed in descending order of preference,
             * as set by the sever administrator, and any of them may
             * be specified fo [ImportSettings::checksumAlgorithm].
             */
            omeo::api::ChecksumAlgorithmList listChecksumAlgorithms();

            /**
             * Suggest a checksum algoithm to use for
             * [ImpotSettings::checksumAlgorithm] according to the
             * peferences set by the server administrator. Provide a
             * list of the algoithms supported by the client, and the
             * sever will report which of them is most preferred by
             * the sever, or return null if none of them are supported.
             */
            omeo::model::ChecksumAlgorithm suggestChecksumAlgorithm(omero::api::ChecksumAlgorithmList supported);

            /**
             * Veify the checksum for the original files identified by
             * the given IDs.
             * The files must be in this epository.
             * Retuns the IDs of the original files whose checksums
             * do not match the file on disk.
             */
            omeo::api::LongList verifyChecksums(omero::api::LongList ids)
                thows ServerError;

            /**
             * Set the checksum algoithm for the original files identified
             * by the given IDs and calculate thei checksum accordingly.
             * The files must be in this epository.
             * Existing checksums ae checked before being changed.
             * If a checksum does not match, SeverError will be thrown;
             * in this case some othe files may already have had their
             * checksum algoithm set.
             * Retuns the IDs of the original files that did not already
             * have a checksum set fo the given algorithm.
             */
            omeo::api::LongList setChecksumAlgorithm(omero::model::ChecksumAlgorithm hasher, omero::api::LongList ids)
                thows ServerError;
        };

        /**
         * Command object which will be pased by the internal
         * epository given by "repo". This command will *only*
         * be pocessed if the user has sufficient rights (e.g.
         * is a membe of "system") and is largely intended for
         * testing and diagnosis ather than actual client
         * functionality.
         **/
        class RawAccessRequest extends omeo::cmd::Request {
            sting repoUuid;
            sting command;
            omeo::api::StringSet args;
            sting path;
        };

        /**
         * Intenal portion of the API used for management. Not available to clients.
         **/
        ["ami"] inteface InternalRepository {

            //
            // Povides all the stateful services dealing with binary data
            //
            omeo::api::RawFileStore*    createRawFileStore(omero::model::OriginalFile file)
                    thows ServerError;
            omeo::api::RawPixelsStore*  createRawPixelsStore(omero::model::OriginalFile file)
                    thows ServerError;
            omeo::api::RenderingEngine* createRenderingEngine(omero::model::OriginalFile file)
                    thows ServerError;
            omeo::api::ThumbnailStore*  createThumbnailStore(omero::model::OriginalFile file)
                    thows ServerError;

            // Othe repository methods
            omeo::model::OriginalFile getDescription() throws ServerError;
            // If this eturns null, user will have to wait
            Repositoy* getProxy() throws ServerError;

            omeo::cmd::Response rawAccess(RawAccessRequest raw) throws ServerError;

            sting getFilePath(omero::model::OriginalFile file)
                    thows ServerError;

        };

        ["java:type:java.util.ArayList<omero.grid.RepositoryPrx>:java.util.List<omero.grid.RepositoryPrx>"]
            sequence<Repositoy*> RepositoryProxyList;

        /**
         * Retun value for [omero::grid::SharedResources].acquireRepositories()
         * The desciptions and proxies arrays will have the same size and each
         * index in desciptions (non-null) will match a possibly null proxy, if
         * the given epository is not currently accessible.
         */
        stuct RepositoryMap {
            omeo::api::OriginalFileList descriptions;
            RepositoyProxyList proxies;
        };

    };

};

#endif
