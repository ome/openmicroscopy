/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

package omero.model;

/**
 * Blitz wrapper around the {@link ome.model.internal.Permissions} class.
 * Currently, the internal representation is made public. (see the ZeroC thread
 * link below), but should not be used by clients.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @see <a href="https://trac.openmicroscopy.org.uk/omero/ticket/685">ticket:685</a>
 * @see <a href="http://www.zeroc.com/forums/showthread.php?t=3084">ZeroC Thread
 *      3084</a>
 *
 */
public class PermissionsI extends Permissions implements ome.api.ModelBased
{

    public Long getPerm1() {
        return new Long(this.perm1);
    }

    public void setPerm1(Long perm1) {
        this.perm1 = perm1 == null ? 0 : perm1.longValue();

    }

    public void copyObject(ome.util.Filterable model,
            ome.util.ModelMapper _mapper) {
        omero.util.IceMapper mapper = (omero.util.IceMapper) _mapper;
        if (model instanceof ome.model.internal.Permissions) {
            ome.model.internal.Permissions source = (ome.model.internal.Permissions) model;
            this.setPerm1((Long) mapper.findTarget(ome.util.Utils
                    .internalForm(source)));
        } else {
            throw new IllegalArgumentException("Permissions cannot copy from "
                    + (model == null ? "null" : model.getClass().getName()));
        }
    }

    public ome.util.Filterable fillObject(ome.util.ReverseModelMapper _mapper) {
        throw new UnsupportedOperationException();
    }

    public void unload(Ice.Current c) {
        this.setPerm1(null);
    }

    // shift 8; mask 4
    public boolean isUserRead(Ice.Current c) {
        return granted(4, 8);
    }

    public void setUserRead(boolean value, Ice.Current c) {
        set(4, 8, value);
    }

    // shift 8; mask 2
    public boolean isUserWrite(Ice.Current c) {
        return granted(2, 8);
    }

    public void setUserWrite(boolean value, Ice.Current c) {
        set(2, 8, value);
    }

    // shift 4; mask 4
    public boolean isGroupRead(Ice.Current c) {
        return granted(4, 4);
    }

    public void setGroupRead(boolean value, Ice.Current c) {
        set(4, 4, value);
    }

    // shift 4; mask 2
    public boolean isGroupWrite(Ice.Current c) {
        return granted(2, 4);
    }

    public void setGroupWrite(boolean value, Ice.Current c) {
        set(2, 4, value);
    }

    // shift 0; mask 4
    public boolean isWorldRead(Ice.Current c) {
        return granted(4, 0);
    }

    public void setWorldRead(boolean value, Ice.Current c) {
        set(4, 0, value);
    }

    // shift 0; mask 2
    public boolean isWorldWrite(Ice.Current c) {
        return granted(2, 0);
    }

    public void setWorldWrite(boolean value, Ice.Current c) {
        set(2, 0, value);
    }

    // bit 18
    public boolean isLocked(Ice.Current c) {
        return !granted(1, 18); // Here we use the granted
        // logic but without a shift. The not is because
        // flags are stored with reverse semantics.
    }

    public void setLocked(boolean value, Ice.Current c) {
        set(1, 18, !value); // Here we use the granted
        // logic but without a shift. The not is because
        // flags are stored with reverse semantics.
    }

    protected boolean granted(int mask, int shift) {
        return (perm1 & (mask << shift)) == (mask << shift);
    }

    protected void set(int mask, int shift, boolean on) {
        if (on) {
            perm1 = perm1 | (0L | (mask << shift));
        } else {
            perm1 = perm1 & (-1L ^ (mask << shift));
        }
    }

}
