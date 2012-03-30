/*
 *   Copyright 2007-2012 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

package omero.model;

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

    private static final long serialVersionUID = 89928049580980928L;

    public final static Ice.ObjectFactory Factory = new Ice.ObjectFactory() {

        public Object create(String arg0) {
            return new PermissionsI();
        }

        public void destroy() {
            // no-op
        }

    };

    /**
     * Whether or not this object is immutable. Currently this should only
     * be set to true after marshalling and unmarshalling via Ice.
     */
    private boolean __immutable = false;

    /**
     * Called as Ice converts from a binary stream to a PermissionsI object.
     * Here we set {@link #__immutable} to true so that clients consuming this
     * object cannot alter them.
     */
    @Override
    public void ice_postUnmarshal() {
        super.ice_postUnmarshal();
        __immutable = true;
    }

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

    public boolean canAnnotate(Ice.Current c) {
        return ! disallowAnnotate;
    }

    public boolean canEdit(Ice.Current c) {
        return ! disallowEdit;
    }

    public PermissionsI(PermissionsI perms) {
        this.perm1 = perms.getPerm1();
    }

    public PermissionsI(ome.model.internal.Permissions sourceP) {
        setPerm1((Long) ome.util.Utils.internalForm(sourceP));
        this.disallowAnnotate = sourceP.isDisallowAnnotate();
        this.disallowEdit = sourceP.isDisallowEdit();
    }

    public long getPerm1(Ice.Current current) {
        return this.perm1;
    }

    public void setPerm1(long perm1, Ice.Current current) {
        throwIfImmutable();
        this.perm1 = perm1;
    }

    public void setPerm1(Long perm1) {
        throwIfImmutable();
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
        throwIfImmutable();
        if (on) {
            perm1 = perm1 | (0L | (mask << shift));
        } else {
            perm1 = perm1 & (-1L ^ (mask << shift));
        }
    }

    private void throwIfImmutable() {
        if (__immutable) {
            throw new omero.ClientError("ImmutablePermissions:"+toString());
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

}
