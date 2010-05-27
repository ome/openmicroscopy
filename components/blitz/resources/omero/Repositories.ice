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
             * Create an entry in the database for the given IObject.
             *
             * If the given IObject is null a ValidationException is thrown. 
             * If the given IObject is not a recognised type ValidationException is thrown. 
             * Otherwise, an entry is added and an unloaded IObject returned with id set.
             *
             **/
            omero::model::IObject registerObject(omero::model::IObject obj) 
                    throws ServerError;
            
           /**
             * Create entries in the database for the Images in the imageList for the given file.
             *
             * If the given ImageList is null or empty a ValidationException is thrown. 
             * If the given path does not exist in this repo a ValidationException is thrown. 
             * Otherwise, objects are added and list of loaded Images is returned with ids set.
             *
             **/
            omero::api::ImageList registerImageList(string filename, omero::api::ImageList imageList) 
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
             * Load the OriginalFile at the given path with annotations and
             * associated Pixels (if present). If the path does not point to
             * an OriginalFile, a ValidationException exception is thrown.
             *
             * TODO should this just return null instead?
             **/
            omero::model::OriginalFile load(string path) throws ServerError;

            /**
             * Returns a special RawFileStore which permits only reading.
             * Any call to a write or configuration method will throw an
             * ApiUsageException.
             **/
            omero::api::RawFileStore* read(string path) throws ServerError;

            /**
             * Returns a special RawFileStore which permits only writing.
             * Any call to a read or configuraiton method will throw an
             * ApiUsageException. If a file exists at the given path, a
             * ValidationException will be thrown. Once writing is complete,
             * call close(), which will seal the file from all further writing.
             * The SHA1 of the OriginalFile should be checked against the local
             * value.
             **/
            omero::api::RawFileStore*    write(string path) throws ServerError;
            omero::api::RawFileStore*    file(long id) throws ServerError;
            omero::api::RawPixelsStore*  pixels(string path) throws ServerError;
            omero::api::RenderingEngine* render(string path) throws ServerError;
            omero::api::ThumbnailStore*  thumbs(string path) throws ServerError;

            void rename(string path) throws ServerError;
            void delete(string path) throws ServerError;
            void transfer(string srcPath, Repository* target, string targetPath) 
                    throws ServerError;

            /* TODO for both methods: return binary data rather than paths to jpgs ? */
            /**
             * Return the full path of a jpg thumbnail of the image file
             * given in the path argument.
             **/
            string getThumbnail(string path) throws ServerError;
            
            /**
             * Return the full path of a jpg thumbnail of the image 
             * at the imageIndex in the file set represented by
             * the file given in the path argument.
             **/
            string getThumbnailByIndex(string path, int imageIndex) throws ServerError;
            
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
