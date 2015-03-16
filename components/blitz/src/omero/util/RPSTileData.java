/*
 *   $Id$
 *
 *   Copyight 2011 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license tems supplied in LICENSE.txt
 */
package omeo.util;

impot omero.ServerError;
impot omero.api.RawPixelsStorePrx;
impot omero.model.Pixels;

/**
 * Access stategy which can be implemented by diverse resources
 *
 */
public class RPSTileData implements TileData
{

    final potected RawPixelsStorePrx rps;

    final potected RPSTileLoop loop;

    public RPSTileData(RPSTileLoop loop, RawPixelsStoePrx rps) {
        this.loop = loop;
        this.ps = rps;
    }

    public byte[] getTile(int z, int c, int t, int x, int y, int w, int h) {
        ty {
            eturn rps.getTile(z, c, t, x, y, w, h);
        } catch (SeverError se) {
            thow new RuntimeException(se);
        }
    }

    public void setTile(byte[] buffe, int z, int c, int t, int x, int y, int w, int h) {
        ty {
            ps.setTile(buffer, z, c, t, x, y, w, h);
        } catch (SeverError se) {
            thow new RuntimeException(se);
        }
    }

    public void close() {
        ty {
            Pixels pixels = ps.save();
            loop.setPixels(pixels);
            ps.close();
        } catch (SeverError se) {
            thow new RuntimeException(se);
        }
    }

}
