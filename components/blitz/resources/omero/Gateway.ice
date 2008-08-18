/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

#ifndef omero_Gateway
#define omero_Gateway

#include <omero/fwd.ice>
#include <omero/RTypes.ice>

module omero {

    module constants {

        const string GATEWAY = "omero.gateways.PrimaryGateway";

    };

    module gateways {

        // Exceptions
        // =====================================================================

        exception DSOutOfServiceException
        {
	    string message;
	    string cause;
        };

        exception DSAccessException
        {
	    string message;
	    string cause;
        };

        // Collections
        // =====================================================================

        ["java:type:java.util.ArrayList<omero.model.IObject>:java.util.List<omero.model.IObject>"]
            sequence<omero::model::IObject> IObjectList;

        ["java:type:java.util.ArrayList<omero.model.Project>:java.util.List<omero.model.Project>"]
            sequence<omero::model::Project> ProjectList;

        ["java:type:java.util.ArrayList<omero.model.Dataset>:java.util.List<omero.model.Dataset>"]
            sequence<omero::model::Dataset> DatasetList;

        ["java:type:java.util.ArrayList<omero.model.Image>:java.util.List<omero.model.Image>"]
            sequence<omero::model::Image> ImageList;

        ["java:type:java.util.ArrayList<omero.model.Pixels>:java.util.List<omero.model.Pixels>"]
            sequence<omero::model::Pixels> PixelsList;

        ["java:type:java.util.ArrayList<omero.model.PixelsType>:java.util.List<omero.model.PixelsType>"]
            sequence<omero::model::PixelsType> PixelsTypeList;

        ["java:type:java.util.ArrayList<Long>:java.util.List<Long>"]
            sequence<long> LongList;

        ["java:type:java.util.ArrayList<Integer>:java.util.List<Integer>"]
            sequence<int> IntegerList;

        sequence<byte> ByteArray;
        sequence<int> IntegerArray;
        sequence<double> DoubleArray;
        sequence<IntegerArray> IntegerArrayArray;
        sequence<IntegerArrayArray> IntegerArrayArrayArray;
        sequence<DoubleArray> DoubleArrayArray;
        sequence<DoubleArrayArray> DoubleArrayArrayArray;

        dictionary<long, string> LongStringMap;
        dictionary<long, ByteArray> LongByteArrayMap;
        dictionary<long, omero::model::Pixels> LongPixelsMap;
        dictionary<string, omero::RType> StringRTypeMap;

        // Data objects
        // =====================================================================

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

        // Services
        // =====================================================================

        ["ami"] interface PrimaryGateway {

            /*
             * Get the projects, and datasets in the OMERO.Blitz server in the user
             * account.
             * @param ids user ids to get the projects from, if null will retrieve all
             * projects from the users account.
             * @param withLeaves get the projects, images and pixels too.
             * @return see above.
             * @throws DSOutOfServiceException
             * @throws DSAccessException
             */
            idempotent ProjectList getProjects(LongList ids, bool withLeaves)
                throws DSOutOfServiceException, DSAccessException;

            /*
             * Get the datasets in the OMERO.Blitz server in the projects ids.
             * @param ids of the datasets to retrieve, if null get all users datasets.
             * @param withLeaves get the images and pixels too.
             * @return see above.
             * @throws DSOutOfServiceException
             * @throws DSAccessException
             */
            idempotent DatasetList getDatasets(LongList ids, bool withLeaves)
                throws DSOutOfServiceException, DSAccessException;


            /*
             * Get the dataset in the OMERO.Blitz server with the given id.
             * @param id of the dataset to retrieve
             * @param withLeaves get the images and pixels too.
             * @return see above.
             * @throws DSOutOfServiceException
             * @throws DSAccessException
             */
            idempotent omero::model::Dataset getDataset(long datasetId, bool leaves)
                throws DSOutOfServiceException, DSAccessException;

            /*
             * Get the pixels associated with the image, this is normally one pixels per
             * image, but can be more.
             * @param imageId
             * @return the list of pixels.
             * @throws DSOutOfServiceException
             * @throws DSAccessException
             */
            idempotent PixelsList getPixelsFromImage(long imageId)
                throws DSOutOfServiceException, DSAccessException;

            /*
             * Get the image with id
             * @param id see above
             * @return see above.
             * @throws DSOutOfServiceException
             * @throws DSAccessException
             */
            idempotent omero::model::Image getImage(long id)
                throws DSOutOfServiceException, DSAccessException;

            /*
             * Get the images in the OMERO.Blitz server from the object parentType with
             * id's in list ids.
             * @param parentType see above.
             * @param ids see above.
             * @return see above.
             * @throws DSOutOfServiceException
             * @throws DSAccessException
             */
            idempotent ImageList getImages(ContainerClass parentType, LongList ids )
                throws DSOutOfServiceException, DSAccessException;

            /*
             * Run the query passed as a string in the iQuery interface. This method will
             * return list of objects.
             * @param myQuery string containing the query.
             * @return the result.
             * @throws DSOutOfServiceException
             * @throws DSAccessException
             */
            idempotent IObjectList findAllByQuery(string myQuery)
                throws DSOutOfServiceException, DSAccessException;

            /*
             * Run the query passed as a string in the iQuery interface.
             * The method expects to return only one result from the query, if more than
             * one result is to be returned the method will throw an exception.
             * @param myQuery string containing the query.
             * @return the result.
             * @throws DSOutOfServiceException
             * @throws DSAccessException
             */
            idempotent omero::model::IObject findByQuery(string myQuery)
                throws DSOutOfServiceException, DSAccessException;

            /*
             * Get the raw plane for the pixels pixelsId, this returns a 2d array
             * representing the plane, it returns doubles but will not lose data.
             * @param pixelsId id of the pixels to retrieve.
             * @param c the channel of the pixels to retrieve.
             * @param t the time point to retrieve.
             * @param z the z section to retrieve.
             * @return The raw plane in 2-d array of doubles.
             * @throws DSAccessException
             * @throws DSOutOfServiceException
             */
            idempotent DoubleArrayArray getPlane(long pixelsId, int z, int c, int t)
                throws DSOutOfServiceException, DSAccessException;

            /*
             * Get the pixels information for an image, this method will also
             * attach the logical channels, channels, and other metadata in the pixels.
             * @param pixelsId image id relating to the pixels.
             * @return see above.
             * @throws DSAccessException
             * @throws DSOutOfServiceException
             * @throws DSOutOfServiceException
             * @throws DSAccessException
             */
            idempotent omero::model::Pixels getPixels(long pixelsId)
                throws DSOutOfServiceException, DSAccessException;

            /*
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
             * @throws DSOutOfServiceException
             * @throws DSAccessException
             */
            long copyPixelsXYTZ(long pixelsID, int x, int y, int t, int z, IntegerList channelList, string methodology)
                throws DSOutOfServiceException, DSAccessException;

            /*
             * Copy the pixels to a new pixels, this is only the data object
             * and does not create a pixels object in the RawPixelsStore,
             * To load data into the plane the {@link #uploadPlane(long, DoubleArrayArray)}
             * to add data to the pixels.
             * @param pixelsID pixels id to copy.
             * @param channelList the list of channels to copy, this is the channel index.
             * @param methodology user supplied text, describing the methods that
             * created the pixels.
             * @return new id.
             * @throws DSOutOfServiceException
             * @throws DSAccessException
             */
            long copyPixels(long pixelsID, IntegerList channelList, string methodology)
                throws DSOutOfServiceException, DSAccessException;

            /*
             * Copy the image and it's attached pixels and
             * metadata to a new Image and return the id of the new image. The method
             * will not copy annotations or attachments.
             * @param imageId image id to copy.
             * @param x width of plane.
             * @param y height of plane.
             * @param t The number of time-points
             * @param z The number of zSections.
             * @param channelList the list of channels to copy, [0-(sizeC-1)].
             * @param imageName The new imageName.
             * @return new id.
             * @throws DSOutOfServiceException
             * @throws DSAccessException
             */
            long copyImage(long imageId, int x, int y, int t, int z, IntegerList channelList, string imageName)
                throws DSOutOfServiceException, DSAccessException;

            /*
             * Upload the plane to the server, on pixels id with channel and the
             * time, + z section. the data is the client 2d data values. This will
             * be converted to the raw server bytes.
             * @param pixelsId pixels id to upload to .
             * @param z z section.
             * @param c channel.
             * @param t time point.
             * @param data plane data.
             * @throws DSOutOfServiceException
             * @throws DSAccessException
             */
            idempotent void uploadPlane(long pixelsId, int z, int c, int t, DoubleArrayArray data)
                throws DSOutOfServiceException, DSAccessException;

            /*
             * Update the pixels object on the server, updating appropriate tables in the
             * database and returning a new copy of the pixels.
             * @param object see above.
             * @return the new updated pixels.
             * @throws DSOutOfServiceException
             * @throws DSAccessException
             */
            idempotent omero::model::Pixels updatePixels(omero::model::Pixels pixels)
                throws DSOutOfServiceException, DSAccessException;

            /*
             * Get a list of all the possible pixelsTypes in the server.
             * @return see above.
             * @throws DSOutOfServiceException
             * @throws DSAccessException
             */
            idempotent PixelsTypeList getPixelTypes()
                throws DSOutOfServiceException, DSAccessException;

            /*
             * Get the pixelsType for type of name type.
             * @param type see above.
             * @return see above.
             * @throws DSOutOfServiceException
             * @throws DSAccessException
             */
            idempotent omero::model::PixelsType getPixelType(string type)
                throws DSOutOfServiceException, DSAccessException;

            /*
             * Get the scripts from the iScript Service.
             * @return All the available scripts in a map by id and name.
             * @throws DSOutOfServiceException
             * @throws DSAccessException
             */
            idempotent LongStringMap getScripts()
                throws DSOutOfServiceException, DSAccessException;

            /*
             * Get the id of the script with name
             * @param name name of the script.
             * @return the id of the script.
             * @throws DSOutOfServiceException
             * @throws DSAccessException
             */
            idempotent long getScriptID(string name)
                throws DSOutOfServiceException, DSAccessException;

            /*
             * Upload the script to the server.
             * @param script script to upload
             * @return id of the new script.
             * @throws DSOutOfServiceException
             * @throws DSAccessException
             */
            long uploadScript(string script)
                throws DSOutOfServiceException, DSAccessException;

            /*
             * Get the script with id, this returns the actual script as a string.
             * @param id id of the script to retrieve.
             * @return see above.
             * @throws DSOutOfServiceException
             * @throws DSAccessException
             */
            idempotent string getScript(long id)
                throws DSOutOfServiceException, DSAccessException;

            /*
             * Get the parameters the script takes, this is a map of the parameter name and type.
             * @param id id of the script.
             * @return see above.
             * @throws DSOutOfServiceException
             * @throws DSAccessException
             */
            idempotent StringRTypeMap getParams(long id)
                throws DSOutOfServiceException, DSAccessException;

            /*
             * Run the script and get the results returned as a name , value map.
             * @param id id of the script to run.
             * @param map the map of parameters, values for inputs.
             * @return see above.
             * @throws DSOutOfServiceException
             * @throws DSAccessException
             */
            StringRTypeMap runScript(long id, StringRTypeMap map)
                throws DSOutOfServiceException, DSAccessException;

            /*
             * Delete the script with id from the server.
             * @param id id of the script to delete.
             * @throws DSOutOfServiceException
             * @throws DSAccessException
             */
            void deleteScript(long id)
                throws DSOutOfServiceException, DSAccessException;

            /*
             * Get the zSection stack from the pixels at timepoint t
             * @param pixelId The pixelsId from the imageStack.
             * @param c The channel.
             * @param t The time-point.
             * @return see above.
             * @throws DSOutOfServiceException
             * @throws DSAccessException
             */
            idempotent DoubleArrayArrayArray getPlaneStack(long pixelId, int c, int t)
                throws DSOutOfServiceException, DSAccessException;

            /*
             * Render the pixels for the zSection z and timePoint t.
             * @param pixelsId pixels id of the plane to render
             * @param z z section to render
             * @param t timepoint to render
             * @return The image as a buffered image.
             * @throws DSOutOfServiceException
             * @throws DSAccessException
             */
            idempotent BufferedImage getRenderedImage(long pixelsId, int z, int t)
		throws DSOutOfServiceException, DSAccessException;

            /*
             * Render the pixels for the zSection z and timePoint t.
             * @param pixelsId pixels id of the plane to render
             * @param z z section to render
             * @param t timepoint to render
             * @return The image as a 3d array where it represents the image as
             * [x][y][channel]
             * @throws DSOutOfServiceException
             * @throws DSAccessException
             */
            idempotent IntegerArrayArrayArray getRenderedImageMatrix(long pixelsId, int z, int t)
		throws DSOutOfServiceException, DSAccessException;

            /*
             * Render the pixels for the zSection z and timePoint t.
             * @param pixelsId pixels id of the plane to render
             * @param z z section to render
             * @param t timepoint to render
             * @return The pixels are returned as 4 bytes representing the r,g,b,a of
             * image.
             * @throws DSOutOfServiceException
             * @throws DSAccessException
             */
            idempotent IntegerArray renderAsPackedInt(long pixelsId, int z, int t)
		throws DSOutOfServiceException, DSAccessException;

            /*
             * Set the active channels to be on or off in the rendering engine for
             * the pixels.
             * @param pixelsId the pixels id.
             * @param w the channel
             * @param active set active?
             * @throws DSOutOfServiceException
             * @throws DSAccessException
             */
            void setActive(long pixelsId, int w, bool active)
		throws DSOutOfServiceException, DSAccessException;

            /*
             * Is the channel active, turned on in the rendering engine.
             * @param pixelsId the pixels id.
             * @param w channel
             * @return true if the channel active.
             * @throws DSOutOfServiceException
             * @throws DSAccessException
             */
            idempotent bool isActive(long pixelsId, int w)
		throws DSOutOfServiceException, DSAccessException;

            /*
             * Get the default zSection of the image, this is the zSection the image
             * should open on when an image viewer is loaded.
             * @param pixelsId the pixelsId of the image.
             * @return see above.
             * @throws DSOutOfServiceException
             * @throws DSAccessException
             */
            idempotent int getDefaultZ(long pixelsId)
		throws DSOutOfServiceException, DSAccessException;

            /*
             * Get the default time-point of the image, this is the time-point the image
             * should open on when an image viewer is loaded.
             * @param pixelsId the pixelsId of the image.
             * @return see above.
             * @throws DSOutOfServiceException
             * @throws DSAccessException
             */
            idempotent int getDefaultT(long pixelsId)
		throws DSOutOfServiceException, DSAccessException;

            /*
             * Set the default zSection of the image, this is the zSection the image
             * should open on when an image viewer is loaded.
             * @param pixelsId the pixelsId of the image.
             * @param z see above.
             * @throws DSOutOfServiceException
             * @throws DSAccessException
             */
            void setDefaultZ(long pixelsId, int z)
		throws DSOutOfServiceException, DSAccessException;

            /*
             * Set the default timepoint of the image, this is the timepoint the image
             * should open on when an image viewer is loaded.
             * @param pixelsId the pixelsId of the image.
             * @param t see above.
             * @throws DSOutOfServiceException
             * @throws DSAccessException
             */
            void setDefaultT(long pixelsId, int t)
		throws DSOutOfServiceException, DSAccessException;

            /*
             * Set the channel Minimum, Maximum values, that map from image space to
             * rendered space (3 channel, 8 bit, screen).
             * @param pixelsId the pixelsId of the image the mapping applied to.
             * @param w channel of the pixels.
             * @param start The minimum value to map from.
             * @param end The maximum value to map to.
             * @throws DSOutOfServiceException
             * @throws DSAccessException
             */
            void setChannelWindow(long pixelsId, int w, double start, double end)
		throws DSOutOfServiceException, DSAccessException;

            /*
             * Get the channel Minimum value, that maps from image space to
             * rendered space.
             * @param pixelsId the pixelsId of the image the mapping applied to.
             * @param w channel of the pixels.
             * @return see above.
             * @throws DSOutOfServiceException
             * @throws DSAccessException
             */
            idempotent double getChannelWindowStart(long pixelsId, int w)
		throws DSOutOfServiceException, DSAccessException;

            /*
             * Get the channel Maximum value, that maps from image space to
             * rendered space.
             * @param pixelsId the pixelsId of the image the mapping applied to.
             * @param w channel of the pixels.
             * @return see above.
             * @throws DSOutOfServiceException
             * @throws DSAccessException
             */
            idempotent double getChannelWindowEnd(long pixelsId, int w)
		throws DSOutOfServiceException, DSAccessException;

            /*
             * Set the rendering definition of the rendering engine from the default
             * to the one supplied. This allows for more than one rendering definition-
             * mapping per pixels.
             * @param pixelsId for pixelsId
             * @param renderingDefId see above.
             * @throws DSOutOfServiceException
             * @throws DSAccessException
             */
            void setRenderingDefId(long pixelsId, long renderingDefId)
		throws DSOutOfServiceException, DSAccessException;

            /*
             * Get the thumbnail of the image.
             * @param pixelsId for pixelsId
             * @param sizeX size of thumbnail.
             * @param sizeY size of thumbnail.
             * @return see above.
             * @throws DSOutOfServiceException
             * @throws DSAccessException
             */
            idempotent ByteArray getThumbnail(long pixelsId, omero::RInt sizeX, omero::RInt sizeY)
                throws DSOutOfServiceException, DSAccessException;

            /*
             * Get a set of thumbnails, of size X, Y from the list of pixelId's supplied
             * in the list.
             * @param sizeX size of thumbnail.
             * @param sizeY size of thumbnail.
             * @param pixelsIds list of ids.
             * @return see above.
             * @throws DSOutOfServiceException
             * @throws DSAccessException
             */
            idempotent LongByteArrayMap getThumbnailSet(omero::RInt sizeX, omero::RInt sizeY, LongList pixelsIds)
		throws DSOutOfServiceException, DSAccessException;

            /*
             * Get a set of thumbnails from the pixelsId's in the list,
             * maintaining aspect ratio.
             * @param size size of thumbnail.
             * @param pixelsIds list of ids.
             * @return see above.
             * @throws DSOutOfServiceException
             * @throws DSAccessException
             */
            idempotent LongByteArrayMap getThumbnailBylongestSideSet(omero::RInt size, LongList pixelsIds)
                throws DSOutOfServiceException, DSAccessException;

            /*
             * Get the thumbnail of the image, maintain aspect ratio.
             * @param pixelsId for pixelsId
             * @param size size of thumbnail.
             * @return see above.
             * @throws DSOutOfServiceException
             * @throws DSAccessException
             */
            idempotent ByteArray getThumbnailBylongestSide(long pixelsId, omero::RInt size)
                throws DSOutOfServiceException, DSAccessException;

            /*
             * Attach an image to a dataset.
             * @param dataset see above.
             * @param image see above.
             * @throws DSOutOfServiceException
             * @throws DSAccessException
             *
             */
            void attachImageToDataset(omero::model::Dataset dataset, omero::model::Image image)
                throws DSOutOfServiceException, DSAccessException;

            /*
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
             * @throws DSOutOfServiceException
             * @throws DSAccessException
             */
            long createImage(int sizeX, int sizeY, int sizeZ, int sizeT,
                             IntegerList channelList, omero::model::PixelsType pixelsType, string name,
                             string description)
		throws DSOutOfServiceException, DSAccessException;
            /*
             * Get the images from as dataset.
             * @param dataset see above.
             * @return see above.
             * @throws DSOutOfServiceException
             * @throws DSAccessException
             */
            idempotent ImageList getImagesFromDataset(omero::model::Dataset dataset)
                throws DSOutOfServiceException, DSAccessException;

            /*
             * Get the plane from the image with imageId.
             * @param imageId see above.
             * @param z zSection of the plane.
             * @param c channel of the plane.
             * @param t timepoint of the plane.
             * @return see above.
             * @throws DSOutOfServiceException
             * @throws DSAccessException
             */
            idempotent DoubleArrayArray getPlaneFromImage(long imageId, int z, int c, int t)
		throws DSOutOfServiceException, DSAccessException;

            /*
             * This is a helper method and makes no calls to the server. It
             * gets a list of all the dataset in a project if the project has already
             * had the datasets attached, via getLeaves in {@link #getProjects(List, bool)}
             * or fetched via HQL in {@link #findAllByQuery(string)}, {@link #findByQuery(string)}
             * @param project see above.
             * @return see above.
             * @throws DSOutOfServiceException
             * @throws DSAccessException
             */
            idempotent DatasetList getDatasetsFromProject(omero::model::Project project)
                throws DSOutOfServiceException, DSAccessException;

            /*
             * This is a helper method and makes no calls to the server. It
             * gets a list of all the pixels in a dataset if the dataset has already
             * had the pixels attached, via getLeaves in {@link #getProjects(List, bool)}
             * {@link #getDatasets(List, bool)} or fetched via HQL in
             * {@link #findAllByQuery(string)}, {@link #findByQuery(string)}
             * @param dataset see above.
             * @return see above.
             * @throws DSOutOfServiceException
             * @throws DSAccessException
             */
            idempotent PixelsList getPixelsFromDataset(omero::model::Dataset dataset)
                throws DSOutOfServiceException, DSAccessException;

            /*
             * This is a helper method and makes no calls to the server. It
             * gets a list of all the pixels in a project if the project has already
             * had the pixels attached, via getLeaves in {@link #getProjects(List, bool)}
             * or fetched via HQL in {@link #findAllByQuery(string)},
             * {@link #findByQuery(string)}
             * @param project see above.
             * @return see above.
             * @throws DSOutOfServiceException
             * @throws DSAccessException
             */
            idempotent PixelsList getPixelsFromProject(omero::model::Project project)
                throws DSOutOfServiceException, DSAccessException;

            /*
             * This is a helper methods, which makes no calls to the server. It get all
             * the pixels attached to a list of images. It requires that the pixels are
             * already attached via  {@link #getProjects(List, bool)}
             * {@link #getDatasets(List, bool)} or fetched via HQL in
             * {@link #findAllByQuery(string)}, {@link #findByQuery(string)}
             * Get the pixels from the images in the list.
             * @param images see above.
             * @return map of the pixels-->imageId.
             */
            idempotent LongPixelsMap getPixelsImageMap(ImageList images)
                throws DSOutOfServiceException, DSAccessException;

            /*
             * This is a helper methods, which makes no calls to the server. It get all
             * the pixels attached to a list of images. It requires that the pixels are
             * already attached via  {@link #getProjects(List, bool)}
             * {@link #getDatasets(List, bool)} or fetched via HQL in
             * {@link #findAllByQuery(string)}, {@link #findByQuery(string)}
             * Get the pixels from the images in the list.
             * @param images see above.
             * @return list of the pixels.
             */
            idempotent PixelsList getPixelsFromImageList(ImageList images)
                throws DSOutOfServiceException, DSAccessException;

            /*
             * Get the images from the dataset with name, this can use wild cards.
             * @param datasetId see above.
             * @param imageName see above.
             * @return see above.
             * @throws DSOutOfServiceException
             * @throws DSAccessException
             */
            idempotent ImageList getImageFromDatasetByName(long datasetId, string imageName)
                throws DSOutOfServiceException, DSAccessException;

            /*
             * Get the list of images with name containing imageName.
             * @param imageName see above.
             * @return see above.
             * @throws DSOutOfServiceException
             * @throws DSAccessException
             */
            idempotent ImageList getImageByName(string imageName)
                throws DSOutOfServiceException, DSAccessException;

            /*
             * Save the object to the db .
             * @param obj see above.
             * @throws DSOutOfServiceException
             * @throws DSAccessException
             */
            void saveObject(omero::model::IObject obj)
                throws DSOutOfServiceException, DSAccessException;

            /*
             * Save and return the Object.
             * @param obj see above.
             * @return see above.
             * @throws DSOutOfServiceException
             * @throws DSAccessException
             */
            omero::model::IObject saveAndReturnObject(omero::model::IObject obj)
                throws DSOutOfServiceException, DSAccessException;

            /*
             * Save the array.
             * @param graph see above.
             * @throws DSOutOfServiceException
             * @throws DSAccessException
             */
            void saveArray(IObjectList graph)
                throws DSOutOfServiceException, DSAccessException;

            /*
             * Save and return the array.
             * @param <T> The Type to return.
             * @param graph the object
             * @return see above.
             * @throws DSOutOfServiceException
             * @throws DSAccessException
             */
            IObjectList saveAndReturnArray(IObjectList graph)
                 throws DSOutOfServiceException, DSAccessException;
            /*
             * Delete the object.
             * @param row the object.(commonly a row in db)
             * @throws DSOutOfServiceException
             * @throws DSAccessException
             */
            void deleteObject(omero::model::IObject row)
                throws DSOutOfServiceException, DSAccessException;

            /*
             * Get the username.
             * @return see above.
             */
            idempotent string getUsername()
                throws DSOutOfServiceException, DSAccessException;

            /*
             * Keep service alive.
             * @throws DSOutOfServiceException
             * @throws DSAccessException
             */
            idempotent void keepAlive()
                throws DSOutOfServiceException, DSAccessException;

        };

    };
};

#endif
