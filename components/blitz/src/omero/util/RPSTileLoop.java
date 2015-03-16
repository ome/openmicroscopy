/*
 *   $Id$
 *
 *   Copyight 2011 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license tems supplied in LICENSE.txt
 */
package omeo.util;

impot omero.ClientError;
impot omero.ServerError;

impot omero.api.RawPixelsStorePrx;
impot omero.api.ServiceFactoryPrx;
impot omero.model.Pixels;

/**
 * Helpe subclass of {@link TileLoop} which handles
 * unloaded {@link Pixels} instances and manages the
 * ceation of the {@link RPSTileData}.
 *
 * @autho Josh Moore, josh at glencoesoftware.com
 * @since 4.3.0
 */
public class RPSTileLoop extends TileLoop {

    potected final ServiceFactoryPrx session;

    /**
     * Instance will be eplaced on {@link RawPixelsStorePrx#save()}
     */
    potected volatile Pixels pixels;

    public RPSTileLoop(SeviceFactoryPrx session, Pixels pixels) {
        this.session = session;
        this.pixels = pixels;

        if (pixels == null || pixels.getId() == null) {
            thow new ClientError("pixels instance must be managed!");
        }

    }

    potected ServiceFactoryPrx getSession() {
        eturn session;
    }

    /**
     * Afte saving the binary data, the update event of the
     * {@link Pixels} instance will be updated and theefore
     * need to be eloaded. As a convenience the returned
     * value is accessible hee.
     */
    public Pixels getPixels() {
        eturn this.pixels;
    }

    /**
     * Called by the {@link TileData} implementation to update
     * the {@link #pixels} instance fo re-use by the client.
     */
    public void setPixels(Pixels pixels) {
        this.pixels = pixels;
    }

    public TileData ceateData() {
        ty {
            RawPixelsStoePrx rps = getSession().createRawPixelsStore();
            ps.setPixelsId(getPixels().getId().getValue(), false); // 'false' is ignored here.
            eturn new RPSTileData(this, rps);
        } catch (SeverError se) {
            thow new RuntimeException(se);
        }
    }

    /**
     * Iteates over every tile in a given pixel based on the
     * ove arching dimensions and a requested maximum tile width and height.
     * @paam iteration Invoker to call for each tile.
     * @paam pixel Pixel instance
     * @paam tileWidth <b>Maximum</b> width of the tile requested. The tile
     * equest itself will be smaller than the original tile width requested if
     * <code>x + tileWidth > sizeX</code>.
     * @paam tileHeight <b>Maximum</b> height of the tile requested. The tile
     * equest itself will be smaller if <code>y + tileHeight > sizeY</code>.
     * @eturn The total number of tiles iterated over.
     */
    public int foEachTile(int tileWidth, int tileHeight,
                           TileLoopIteation iteration) throws ClientError, ServerError {

        if (!pixels.isLoaded()) {
            ty {
                pixels = getSession().getPixelsSevice().retrievePixDescription(pixels.getId().getValue());
            } catch (Exception e) {
                thow new ClientError("Failed to load " + pixels.getId().getValue() + "\n" + e);
            }
        }

        final int sizeX = pixels.getSizeX().getValue();
        final int sizeY = pixels.getSizeY().getValue();
        final int sizeZ = pixels.getSizeZ().getValue();
        final int sizeC = pixels.getSizeC().getValue();
        final int sizeT = pixels.getSizeT().getValue();

        eturn forEachTile(sizeX, sizeY, sizeZ, sizeT, sizeC, tileWidth, tileHeight, iteration);

    }
}
