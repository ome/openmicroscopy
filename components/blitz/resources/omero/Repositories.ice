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
        
        class RepositoryListConfig 
        {
            int depth;
            bool files;
            bool dirs;
            bool hidden;
            bool registered;
            bool showOriginalFiles;
        };

        class FileSet 
        {
            bool importableImage;
            string fileName;
            omero::model::OriginalFile parentFile;
            bool hidden;
            bool dir;
            string reader;
	        int imageCount;
            omero::api::IObjectList usedFiles;
            omero::api::ImageList imageList;
        };

        ["java:type:java.util.ArrayList<FileSet>:java.util.List<FileSet>"]
            sequence<FileSet> FileSetList;
        
        /**
         * Client-accessible interface representing a single mount point on the server-side.
         **/
        ["ami"] interface Repository {

            /**
             * Return the OriginalFile descriptor for this Repository. It will have
             * the path "/"
             **/
            omero::model::OriginalFile root() throws ServerError;

            /*
             * Directory listing methods. 
             */

            // A list of all files and/or directories, registered or not depending on cnfig.
            omero::api::OriginalFileList listFiles(string path, RepositoryListConfig config) 
                    throws ServerError;
            
            // A list of importable and non-importable file sets in a directory depending on config.
            FileSetList listFileSets(string path, RepositoryListConfig config) 
                    throws ServerError;
            
            /**
             * Returns the best-guess mimetype for the given path.
             *
             **/
            string mimetype(string path) throws ServerError;

            /**
             * Create an OriginalFile in the database for the given path.
             *
             **/
            omero::model::OriginalFile register(string path, omero::RString mimetype)
                    throws ServerError;

           /**
             * Create an entry in the database for the given OriginalFile.
             *
             * If the given OriginalFile is null a ValidationException is thrown. 
             * Otherwise, an entry is added and an unloaded IObject returned with id set.
             *
             **/
            omero::model::OriginalFile registerOriginalFile(omero::model::OriginalFile omeroFile) 
                    throws ServerError;
            
           /**
             * Create entries in the database for the OriginalFile and Images in the imageList.
             *
             * If the given ImageList is null or empty the OriginalFile is registered only. 
             * If the OriginalFile is null a ValidationException is thrown. 
             * Otherwise, objects are added and list containing a loaded OriginalFile followed 
             * by the loaded Images is returned with ids set.
             *
             **/
            omero::api::IObjectList registerFileSet(omero::model::OriginalFile keyFile, omero::api::ImageList imageList) 
                    throws ServerError;
            
           /**
             * Import image metadata using the parent orginal file.
             *
             * If the id does not exist a ValidationException is thrown. 
             * Otherwise, the image set linked to that original file will have its metadata imported.
             * The imported pixels list is returned.
             *
             **/
            omero::api::ImageList importFileSet(omero::model::OriginalFile keyFile) throws ServerError;
            
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

            void importMetadata(string fileId) throws ServerError;
            void writeBlock(string fileId, Ice::ByteSeq data) throws ServerError;

            omero::api::StringSet getCurrentRepoDir(omero::api::StringSet paths) throws ServerError;

            /* TODO for both methods: These methods should both be removed
              in favour of a full implementation of thumbs()*/
            /**
             * Return the full path of a jpg thumbnail of the image file
             * given in the path argument.
             **/
            ["deprecated:currently for testing only"] string getThumbnail(string path) throws ServerError;
            
            /**
             * Return the full path of a jpg thumbnail of the image 
             * at the imageIndex in the file set represented by
             * the file given in the path argument.
             **/
            ["deprecated:currently for testing only"] string getThumbnailByIndex(string path, int imageIndex) throws ServerError;
            
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
