/*
 *   Copyright 2007-2012 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

package omero.model;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import ome.util.Utils;
import Ice.Object;

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
public class PermissionsI extends Permissions implements ome.model.ModelBased {

    public final static Ice.ObjectFactory Factory = new Ice.ObjectFactory() {

        public Object create(String arg0) {
            return new PermissionsI();
        }

        public void destroy() {
            // no-op
        }

    };

    public PermissionsI() {
        Long l = (Long) Utils
                .internalForm(ome.model.internal.Permissions.EMPTY);
        if (l == null) {
            throw new IllegalStateException("Permissions.DEFAULT is null");
        }
        this.perm1 = l.longValue();
    }

    public PermissionsI(String representation) {
        Long l = (Long) Utils.internalForm(ome.model.internal.Permissions
                .parseString(representation));
        if (l == null) {
            throw new IllegalStateException(representation + " produced null");
        }
        this.perm1 = l.longValue();
    }

    public PermissionsI(PermissionsI perms) {
        this.perm1 = perms.getPerm1();
    }

    public long getPerm1(Ice.Current current) {
        return this.perm1;
    }

    public void setPerm1(long perm1, Ice.Current current) {
        this.perm1 = perm1;
    }

    public void setPerm1(Long perm1) {
        this.perm1 = perm1 == null ? 0 : perm1.longValue();

    }

    public void copyObject(ome.util.Filterable model,
            ome.util.ModelMapper _mapper) {
        throw new UnsupportedOperationException();
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

    // ~ Overrides
    // =========================================================================

    /**
     * produces a String representation of the {@link PermissionsI} similar to
     * those on a Unix filesystem. Unset bits are represented by a dash, while
     * other bits are represented by a symbolic value in the correct bit
     * position. For example, a Permissions with all rights
     * granted to all but WORLD roles would look like: rwrw--
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(16);
        sb.append(isUserRead(null) ? "r" : "-");
        sb.append(isUserWrite(null) ? "w" : "-");
        sb.append(isGroupRead(null) ? "r" : "-");
        sb.append(isGroupWrite(null) ? "w" : "-");
        sb.append(isWorldRead(null) ? "r" : "-");
        sb.append(isWorldWrite(null) ? "w" : "-");
        return sb.toString();
    }

    /**
     * Based on the implementation of ome.model.internal.Permissions#ImmutablePermissions
     */
    private final static class ImmutablePermissionsI extends PermissionsI {

        /**
         * Factory method to create an immutable Permissions object.
         */
        public static PermissionsI immutable(PermissionsI p) {
            return new ImmutablePermissionsI(p);
        }

        /**
         * the delegate {@link PermissionsI} which this immutable wrapper bases
         * all of its logic on. Not final for reasons of serialization.
         */
        private PermissionsI delegate;

        /**
         * the sole constructor for an {@link ImmutablePermissionsI}. Note: this
         * does not behave like {@link PermissionsI#PermissionsI(PermissionsI)} --
         * the copy constructor. Rather stores the {@link PermissionsI} instance
         * for delegation
         *
         * @param p
         *            Non-null {@link PermissionsI} instance.
         */
        ImmutablePermissionsI(PermissionsI p) {
            if (p == null) {
                throw new IllegalArgumentException(
                        "Permissions may not be null");
            }

            this.delegate = new PermissionsI(p);
        }

        // ~ Disallows
        // =====================================================================

        @Override
        public void setPerm1(long perm1, Ice.Current current) {
            throw new omero.ClientError("ImmutablePermissionsI: " + this);
        }

        @Override
        public void setPerm1(Long perm1) {
            throw new omero.ClientError("ImmutablePermissionsI: " + this);
        }

        @Override
        protected void set(int mask, int shift, boolean on) {
            throw new omero.ClientError("ImmutablePermissionsI: " + this);
        }

        /**
         * delegates to {@link #toString()}
         */
        @Override
        public String toString() {
            return delegate.toString();
        }

        // ~ Serialization
        // =====================================================================

        private void readObject(ObjectInputStream s) throws IOException,
                ClassNotFoundException {
            PermissionsI p = (PermissionsI) s.readObject();
            if (p == null) {
                throw new IllegalArgumentException(
                        "Permissions may not be null");
            }

            this.delegate = new PermissionsI(p);
        }

        private void writeObject(ObjectOutputStream s) throws IOException {
            s.writeObject(delegate);
        }


    }

}
