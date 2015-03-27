/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#ifndef OMERO_GATEWAY_ICE
#define OMERO_GATEWAY_ICE

#include <omero/Collections.ice>
#include <omero/ServicesF.ice>
#include <omero/ServerErrors.ice>

module omero {

    module api {

        // Data objects
        // =====================================================================

        /**
         * Simple wrapper around an array of packed ints. Individual language
         * mappings may want to add a subclass to the ObjectFactory for working
         * with a visual representation of the ints.
         **/
        class BufferedImage
        {
            IntegerArray packedInts;
        };

        enum ContainerClass
        {
            Category,
            CategoryGroup,
            Project,
            Dataset,
            Image
        };

        // Gateway Service
        // =====================================================================

        /**
         * High-level service which provides a single interface for most client
         * activities. Each stateful Gateway instance internally manages multiple
         * other stateful instances (RenderingEngine, ThumbnailStore, etc.) significantly
         * simplyifing usage.
         **/
        ["deprecated:The Gateway service is deprecated. use the native language gateways instead instead.", "ami"]
        interface Gateway extends StatefulServiceInterface
            {

            /**
             * Get the projects, and datasets in the OMERO.Blitz server in the user
             * account.
             *
             * @param ids The ids of the projects from, if null will retrieve all
             * projects from the users account.
             * @param withLeaves get the projects, images and pixels too.
             * @return see above.
             * @throws omero::ServerError
             **/
            idempotent ProjectList getProjects(LongList ids, bool withLeaves)
                throws omero::ServerError;

            /**
             * Get the datasets in the OMERO.Blitz server with the given ids.
             *
             * @param ids of the datasets to retrieve, if null get all users datasets.
             * @param withLeaves get the images and pixels too.
             * @return see above.
             * @throws omero::ServerError
             **/
            idempotent DatasetList getDatasets(LongList ids, bool withLeaves)
                throws omero::ServerError;

            /**
             * Get the dataset in the OMERO.Blitz server with the given id.
             * @param id of the dataset to retrieve
             * @param withLeaves get the images and pixels too.
             * @return see above.
             * @throws omero::ServerError
             **/
            idempotent omero::model::Dataset getDataset(long datasetId, bool leaves)
                throws omero::ServerError;

            /**
             * Get the pixels associated with the image, this is normally one pixels per
             * image, but can be more.
             * @param imageId
             * @return the list of pixels.
             * @throws omero::ServerError
             **/
            idempotent PixelsList getPixelsFromImage(long imageId)
                throws omero::ServerError;

            /**
             * Get the image with id
             * @param id see above
             * @return see above.
             * @throws omero::ServerError
             **/
            idempotent omero::model::Image getImage(long id)
                throws omero::ServerError;

            /**
             * Get the images in the OMERO.Blitz server from the object parentType with
             * id's in list ids.
             * @param parentType see above.
             * @param ids see above.
             * @return see above.
             * @throws omero::ServerError
             **/
            idempotent ImageList getImages(ContainerClass parentType, LongList ids )
                throws omero::ServerError;

            /**
             * Run the query passed as a string in the iQuery interface. This method will
             * return list of objects.
             * @param myQuery string containing the query.
             * @return the result.
             * @throws omero::ServerError
             **/
            idempotent IObjectList findAllByQuery(string myQuery)
                throws omero::ServerError;

            /**
             * Run the query passed as a string in the iQuery interface.
             * The method expects to return only one result from the query, if more than
             * one result is to be returned the method will throw an exception.
             * @param myQuery string containing the query.
             * @return the result.
             * @throws omero::ServerError
             **/
            idempotent omero::model::IObject findByQuery(string myQuery)
                throws omero::ServerError;

            /**
             * Get the raw plane for the pixels pixelsId, this returns a 2d array
             * representing the plane, it returns doubles but will not lose data.
             * @param pixelsId id of the pixels to retrieve.
             * @param c the channel of the pixels to retrieve.
             * @param t the time point to retrieve.
             * @param z the z section to retrieve.
             * @return The raw plane in as byte stream.
             * @throws omero::ServerError
             **/
            idempotent Ice::ByteSeq getPlane(long pixelsId, int z, int c, int t)
                throws omero::ServerError;

            /**
             * Get the pixels information for an image, this method will also
             * attach the logical channels, channels, and other metadata in the pixels.
             * @param pixelsId image id relating to the pixels.
             * @return see above.
             * @throws omero::ServerError
             **/
            idempotent omero::model::Pixels getPixels(long pixelsId)
                throws omero::ServerError;

            /**
             * Copy the pixels to a new pixels, this is only the data object
             * and does not create a pixels object in the RawPixelsStore,
             * To load data into the plane the {@link #uploadPlane(long, int, int, int, DoubleArrayArray)}
             * to add data to the pixels.
             * @param pixelsID pixels id to copy.
             * @param x width of plane.
             * @param y height of plane.
             * @param t num timepoints
             * @param z num zsections.
             * @param channelList the list of channels to copy, this is the channel index.
             * @param methodology user supplied text, describing the methods that
             * created the pixels.
             * @return new id.
             * @throws omero::ServerError
             **/
            long copyPixelsXYTZ(long pixelsID, int x, int y, int t, int z,
                                IntegerList channelList, string methodology)
                throws omero::ServerError;

            /**
             * Copy the pixels to a new pixels, this is only the data object
             * and does not create a pixels object in the RawPixelsStore,
             * To load data into the plane the {@link #uploadPlane(long, DoubleArrayArray)}
             * to add data to the pixels.
             * @param pixelsID pixels id to copy.
             * @param channelList the list of channels to copy, this is the channel index.
             * @param methodology user supplied text, describing the methods that
             * created the pixels.
             * @return new id.
             * @throws omero::ServerError
             **/
            long copyPixels(long pixelsID, IntegerList channelList, string methodology)
                throws omero::ServerError;

            /**
             * Copy the image and it's attached pixels and
             * metadata to a new Image and return the id of the new image. The method
             * will not copy annotations or attachments.
             * @param imageId image id to copy.
             * @param x width of plane.
             * @param y height of plane.
             * @param t The number of time-points
             * @param z The number of zSections.
             * @param channelList the list of channels to copy, [0-(sizeC-1)].
             * @param imageName The image name.
             * @return new id.
             * @throws omero::ServerError
             **/
            long copyImage(long imageId, int x, int y, int t, int z,
                           IntegerList channelList, string imageName)
                throws omero::ServerError;

            /**
             * Upload the plane to the server, on pixels id with channel and the
             * time, + z section. the data is the client 2d data values. This will
             * be converted to the raw server bytes.
             * @param pixelsId pixels id to upload to .
             * @param z z section.
             * @param c channel.
             * @param t time point.
             * @param data plane data.
             * @throws omero::ServerError
             **/
            idempotent void uploadPlane(long pixelsId, int z, int c, int t,
                                        Ice::ByteSeq data)
                throws omero::ServerError;

            /**
             * Update the pixels object on the server, updating appropriate tables in the
             * database and returning a new copy of the pixels.
             * @param object see above.
             * @return the new updated pixels.
             * @throws omero::ServerError
             **/
            idempotent omero::model::Pixels updatePixels(omero::model::Pixels pixels)
                throws omero::ServerError;

            /**
             * Get a list of all the possible pixelsTypes in the server.
             * @return see above.
             * @throws omero::ServerError
             **/
            idempotent PixelsTypeList getPixelTypes()
                throws omero::ServerError;

            /**
             * Get the pixelsType for type of name type.
             * @param type see above.
             * @return see above.
             * @throws omero::ServerError
             **/
            idempotent omero::model::PixelsType getPixelType(string type)
                throws omero::ServerError;

            /**
             * Render the pixels for the zSection z and timePoint t.
             * @param pixelsId pixels id of the plane to render
             * @param z z section to render
             * @param t timepoint to render
             * @return The image as a buffered image.
             * @throws omero::ServerError
             **/
            idempotent IntegerArray getRenderedImage(long pixelsId, int z, int t)
                throws omero::ServerError;

            /**
             * Render the pixels for the zSection z and timePoint t.
             * @param pixelsId pixels id of the plane to render
             * @param z z section to render
             * @param t timepoint to render
             * @return The image as a buffered image.
             * @throws omero::ServerError
             **/
            idempotent IntegerArray renderAsPackedIntAsRGBA(long pixelsId, int z, int t)
                throws omero::ServerError;


            /**
             * Render the pixels for the zSection z and timePoint t.
             * @param pixelsId pixels id of the plane to render
             * @param z z section to render
             * @param t timepoint to render
             * @return The image as a 3d array where it represents the image as
             * [x][y][channel]
             * @throws omero::ServerError
             **/
            idempotent IntegerArrayArrayArray getRenderedImageMatrix(long pixelsId,
                                                                     int z, int t) throws omero::ServerError;

            /**
             * Set the active channels to be on or off in the rendering engine for
             * the pixels.
             * @param pixelsId the pixels id.
             * @param w the channel
             * @param active set active?
             * @throws omero::ServerError
             **/
            void setActive(long pixelsId, int w, bool active)
                throws omero::ServerError;

            /**
             * Get the thumbnail of the image.
             * @param pixelsId for pixelsId
             * @param sizeX size of thumbnail.
             * @param sizeY size of thumbnail.
             * @return see above.
             * @throws omero::ServerError
             **/
            idempotent ByteArray getThumbnail(long pixelsId, omero::RInt sizeX, omero::RInt sizeY)
                throws omero::ServerError;

            /**
             * Get a set of thumbnails, of size X, Y from the list of pixelId's supplied
             * in the list.
             * @param sizeX size of thumbnail.
             * @param sizeY size of thumbnail.
             * @param pixelsIds list of ids.
             * @return see above.
             * @throws omero::ServerError
             **/
            idempotent LongByteArrayMap getThumbnailSet(omero::RInt sizeX,
                                                        omero::RInt sizeY, LongList pixelsIds)
                throws omero::ServerError;

            /**
             * Attach an image to a dataset.
             * @param dataset see above.
             * @param image see above.
             * @throws omero::ServerError
             *
             **/
            void attachImageToDataset(omero::model::Dataset dataset,
                                      omero::model::Image image)
                throws omero::ServerError;

            /**
             * Create a new Image of X,Y, and zSections+time-points. The channelList is
             * the emission wavelength of the channel and the pixelsType.
             * @param sizeX width of plane.
             * @param sizeY height of plane.
             * @param sizeZ num zSections.
             * @param sizeT num time-points
             * @param channelList the list of channels to copy.
             * @param pixelsType the type of pixels in the image.
             * @param name the image name.
             * @param description the description of the image.
             * @return new id.
             * @throws omero::ServerError
             **/
            long createImage(int sizeX, int sizeY, int sizeZ, int sizeT,
                             IntegerList channelList,
                             omero::model::PixelsType pixelsType, string name,
                             string description)
                throws omero::ServerError;

            /**
             * Get the images from the dataset with name, this can use wild cards.
             * @param datasetId see above.
             * @param imageName see above.
             * @return see above.
             * @throws omero::ServerError
             **/
            idempotent ImageList getImageFromDatasetByName(long datasetId, string imageName)
                throws omero::ServerError;

            /**
             * Get the list of images with name containing imageName.
             * @param imageName see above.
             * @return see above.
             * @throws omero::ServerError
             **/
            idempotent ImageList getImageByName(string imageName)
                throws omero::ServerError;

            /**
             * Save the object to the db .
             * @param obj see above.
             * @throws omero::ServerError
             **/
            void saveObject(omero::model::IObject obj)
                throws omero::ServerError;

            /**
             * Save and return the Object.
             * @param obj see above.
             * @return see above.
             * @throws omero::ServerError
             **/
            omero::model::IObject saveAndReturnObject(omero::model::IObject obj)
                throws omero::ServerError;

            /**
             * Save the array.
             * @param graph see above.
             * @throws omero::ServerError
             **/
            void saveArray(IObjectList graph)
                throws omero::ServerError;

            /**
             * Save and return the array.
             * @param <T> The Type to return.
             * @param graph the object
             * @return see above.
             * @throws omero::ServerError
             **/
            IObjectList saveAndReturnArray(IObjectList graph)
                 throws omero::ServerError;

            /**
             * Delete the object.
             * @param row the object.(commonly a row in db)
             * @throws omero::ServerError
             **/
            void deleteObject(omero::model::IObject row)
                throws omero::ServerError;

            /**
             * Keep service alive.
             * @throws omero::ServerError
             **/
            idempotent void keepAlive()
                throws omero::ServerError;

        };

    };
};

#endif
