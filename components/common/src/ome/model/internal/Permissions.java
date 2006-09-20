/*
 * ome.model.internal.Permissions
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2005 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */
package ome.model.internal;

//Java imports
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import ome.conditions.ApiUsageException;
import ome.model.IObject;
import ome.parameters.QueryParameter;

//Third-party libraries

//Application-internal dependencies
import static ome.model.internal.Permissions.Role.*;
import static ome.model.internal.Permissions.Right.*;
import static ome.model.internal.Permissions.Flag.*;

/** class responsible for storing all Right/Role-based information for entities
 * as well as various flags for the containing {@link Details} instance.
 * It is strongly encouraged to <em>not</em> base any code on the implementation
 * of the rights, roles, and flag but rather to rely on the public methods.
 * <p>
 * In the future, further roles, rights, and flags may be added to this class. 
 * This will change the representation in the database, but the simple 
 * grant/revoke/isSet logic will remain the same.
 * </p>
 * 
 * @see <a href="https://trac.openmicroscopy.org.uk/omero/ticket/180">ticket:180</a>
 */
public class Permissions implements Serializable
{

	private static final long serialVersionUID = 7089149309186580238L;

	/** enumeration of currently active roles. The {@link #USER} role is active 
	 * when the contents of {@link Details#getOwner()} equals the current user
	 * as determined from the Security system (Server-side only). Similary, the
	 * {@link #GROUP} role is active when the contents of 
	 * {@link Details#getGroup()} matches the current group. {@link #WORLD} is
	 * used for any non-USER, non-GROUP user.
	 * 
	 * For more advanced, ACL, methods taking {@link ome.model.meta.Experimenter}
	 * references can be implemented. 
	 */
	public enum Role {
		USER(8),
		GROUP(4),
		WORLD(0);
		
		private final int shift;
		Role(int shift){ this.shift = shift; }
		int shift(){ return this.shift; }
	}
	
	/** enumeration of granted rights. The {@link #READ} right allows for a user
	 * with the given role to retrieve an entity. This means that all fields of 
	 * that entity can be retrieved. It is not given that all linked entities
	 * can also be retrieved. The {@link #WRITE} right allows for a user with 
	 * the given role to alter the fields of an entity, including changing the
	 * contents of its collection. This does not include changing the fields of
	 * those linked entities, only whether or not they are members of the
	 * given collection. Finally, the {@link #USE} right allows for the linking
	 * of information to a given entity. Care should be taken when granting 
	 * {@link #USE} permissions, because that will hinder the ability to delete
	 * data in the future.
	 */ 
	public enum Right {
		@Deprecated USE(1),
		WRITE(2),
		READ(4),
		/* UNUSED(8) */ ;
		
		private final int mask;
		Right(int mask){ this.mask = mask; }
		int mask(){ return this.mask; }
	}
	
	/** enumeration of flags which can be set on a {@link Permissions} instance.
	 * A {@link Flag#LOCKED} flag implies that the 
	 * {@link Details#getOwner() owner}, {@link Details#getGroup() group}, and
	 * {@link Details#getPermissions() permissions} for an {@link IObject} 
	 * instance may not be changed. {@link Flag#SOFT} implies that the given
	 * {@link Permissions} value is intended as a suggestion, and that other 
	 * sources may override.
	 */
	public enum Flag {
		LOCKED(1<<18),
		SOFT(1<<17);
		
		/* Implementation note:
		 * --------------------
		 * Flags work with reverse logic such that the default permissions can 
		 * remain -1L (all 1s), a flag is "set" when it's bit is set to 0. This
		 * holds for everything over 16.
		 */
		private final int bit;
		Flag(int bit){ this.bit = bit; }
		int bit(){ return this.bit; }
	}
	
	// ~ Constructors
	// =========================================================================
	/** simple contructor. Will turn on all {@link Right rights} for all 
	 * {@link Role roles}
	 */
	public Permissions() {}
	
	/** copy constructor. Will create a new {@link Permissions} with the same
	 * {@link Right rights} as the argument.
	 */
	public Permissions( Permissions p )
	{
		if ( p == null )
		{
			throw new IllegalArgumentException(
					"Permissions argument cannot be null.");
		}
		this.revokeAll(p);
	}
	
    // ~ Fields
	// =========================================================================

	/** represents the lower 64-bits of permissions data. 
	 */
    private long perm1 = -1; // all bits turned on.

    // ~ Getters
	// =========================================================================
    
    /** tests that a given {@link Role} has the given {@link Right}. */
    public boolean isGranted( Role role, Right right )
    {
    	return ( perm1 & right.mask() << role.shift() ) 
    		== ( right.mask() << role.shift() );
    }
    
    /** tests that a given {@link Flag} is set. */
    public boolean isSet( Flag flag )
    {
    	return ( perm1 & flag.bit() ) != flag.bit();
    }
    
    /** returns the order of the bit representing the given {@link Role} and 
     * {@link Right}. This is dependent on the internal representation of
     * {@link Permissions} and should only be used when necessary.
     * @see ome.tools.hibernate.SecurityFilter
     */ 
    public static int bit( Role role, Right right )
    {
    	return right.mask() << role.shift();
    }
    
    public static Permissions parseString( String rwrwrw )
    {
    	
    	Permissions p = new Permissions( EMPTY );
    	String regex = "([Rr_][Ww_]){3}";
    	
    	if ( rwrwrw == null || ! rwrwrw.matches(regex) )
    		throw new ApiUsageException( "Permissions are of the form: "+regex);
    	
    	char c;
    	
    	c = rwrwrw.charAt(0);
    	if ( c == 'r' || c == 'R' ) p.grant( USER, READ );
    	c = rwrwrw.charAt(1);
    	if ( c == 'w' || c == 'W' ) p.grant( USER, WRITE );
    	c = rwrwrw.charAt(2);
    	if ( c == 'r' || c == 'R' ) p.grant( GROUP, READ );
    	c = rwrwrw.charAt(3);
    	if ( c == 'w' || c == 'W' ) p.grant( GROUP, WRITE );
    	c = rwrwrw.charAt(4);
    	if ( c == 'r' || c == 'R' ) p.grant( WORLD, READ );
    	c = rwrwrw.charAt(5);
    	if ( c == 'w' || c == 'W' ) p.grant( WORLD, WRITE );    	
    
    	return p;
    }

    // ~ Setters (return this)
	// =========================================================================
    
    /** turns on the {@link Right rights} for the given {@link Role role}. Null
     * or empty rights are simply ignored. For example,
     * <code>
     *   somePermissions().grant(USER,READ,WRITE,USE);
     * </code>
     * will guarantee that the current user has all rights on this entity.
     */
    public Permissions grant( Role role, Right...rights )
    {
    	if (rights != null && rights.length > 0)
    	{
    		for (Right right : rights) {
    			perm1 = perm1 | singleBitOn(role, right);
    		}
    	}
    	return this;
    }

    /** turns off the {@link Right rights} for the given {@link Role role}. Null
     * or empty rights are simply ignored. For example, 
     * <code>
     *   new Permissions().revoke(WORLD,WRITE,USE);
     * </code>
     * will return a Permissions instance which cannot be altered or linked to
     * by members of WORLD.
     */
    public Permissions revoke( Role role, Right...rights )
    {
    	if (rights != null && rights.length > 0)
    	{
    		for (Right right : rights) {
    			perm1 = perm1 & singleBitOut(role, right);
    		}
    	}
    	return this;
    }
    
    /** takes a permissions instance and ORs it with the current instance. This
     * means that any privileges which have been granted to the argument will
     * also be granted to the current instance. For example,
     * <code>
     *   Permissions mask = new Permissions().grant(WORLD,READ);
     *   someEntity.getDetails().getPermissions().grantAllk(mask);
     * </code>
     * will allow READ access (and possibly more) to <code>someEntity</code> 
     * for members of WORLD.
     */
    public Permissions grantAll( Permissions mask )
    {
    	if ( mask == null ) return this;
    	long maskPerm1 = mask.getPerm1();
    	this.perm1 = this.perm1 | maskPerm1;
    	return this;
    }
    
    /** takes a permissions instance and ANDs it with the current instance. This
     * means that any privileges which have been revoked from the argument will
     * also be revoked from the current instance. For example,
     * <code>
     *   Permissions mask = new Permissions().revoke(WORLD,READ,WRITE,USE);
     *   someEntity.getDetails().getPermissions().applyMask(mask);
     * </code>
     * will disallow all access to <code>someEntity</code> for members of WORLD.
     * 
     * This also implies that applyMask can be used to make copies of Permissions.
     * For example,
     * <code>
     *   new Permissions().applyMask( somePermissions );
     * </code>
     * will produce a copy of <code>somePermissions</code>.
     * 
     * Note: the logic here is different from Unix UMASKS. 
     */
    public Permissions revokeAll( Permissions mask )
    {
    	if ( mask == null ) return this;
    	long maskPerm1 = mask.getPerm1();
    	this.perm1 = this.perm1 & maskPerm1;
    	return this;
    }

    /** turn a given {@link Flag} on. A null {@link Flag} will be ignored. */
    public Permissions set( Flag flag )
    {
    	if ( flag == null ) return this;
    	this.perm1 &= (-1L ^ flag.bit() );
    	return this;
    }

    /** turn a given {@link Flag} off. A null {@link Flag} will be ignored. */
    public Permissions unSet( Flag flag )
    {
    	if ( flag == null ) return this;
    	this.perm1 |= (0L ^ flag.bit() );
    	return this;
    }
    
    // ~ Overrides
	// =========================================================================
    
    /** produces a String representation of the {@link Permissions} similar to
     * those on a Unix filesystem. Unset bits are represented by a 
     * dash, while other bits are represented by a symbolic value in the correct
     * bit position. For example, a Permissions with all {@link Right rights} 
     * granted to all but WORLD {@link Role roles} would look like:
     *   rwurwu---
     */
    @Override
    public String toString()
    {
    	StringBuilder sb = new StringBuilder(16);
    	sb.append(     isSet(LOCKED)      ? "L" : ""  );
    	sb.append( isGranted(USER,READ)   ? "r" : "-" ); 
    	sb.append( isGranted(USER,WRITE)  ? "w" : "-" ); 
    	sb.append( isGranted(GROUP,READ)  ? "r" : "-" ); 
    	sb.append( isGranted(GROUP,WRITE) ? "w" : "-" ); 
    	sb.append( isGranted(WORLD,READ)  ? "r" : "-" ); 
    	sb.append( isGranted(WORLD,WRITE) ? "w" : "-" ); 
    	return sb.toString();
    }
    
    /** returns true if two {@link Permissions} instances have all the same
     * {@link Right} / {@link Role} pairs granted.
	 */
    public boolean sameRights(Permissions p)
    {
    	if ( p == this ) return true;
    	
		for (Role ro : Role.values()) {
			for (Right rt : Right.values()) {
				if (isGranted(ro, rt) != p.isGranted(ro, rt))
					return false;
			}
		}
		
		return true;
    }
    
    /** two {@link Permissions} instances are <code>identical</code> if they have the
     * same bit representation.
     * @see <a href="https://trac.openmicroscopy.org.uk/omero/ticket/291">ticket:291</a>
     */
//    @Override
    public boolean identical(Permissions p) 
    {
//    	if (!(obj instanceof Permissions)) return false;
//    	
//    	Permissions p = (Permissions) obj;
    	
    	if ( p == this ) return true;
    
    	if ( p.perm1 == this.perm1 ) return true;
    	
    	return false;
    
    }
    
//    /** hashCode based on the bit representation of this {@link Permissions} 
//     * instance.
//     */
//    @Override
//    public int hashCode() {
//        int result = 11;
//        result = 17 * result + (int)(perm1^(perm1>>>32));
//        return result;
//    }
    
    // ~ Property accessors : used primarily by Hibernate
    // =========================================================================

    protected long getPerm1()
    {
        return this.perm1;
    }

    protected void setPerm1(long value)
    {
        this.perm1 = value;
    }

    // ~ Helpers
	// =========================================================================
    
    /** returns a long with only a single 0 defined by role/right */ 
	final protected static long singleBitOut(Role role, Right right) {
		return ( -1L ^ ( right.mask() << role.shift() ) );
	}
	
	/** returns a long with only a single 1 defined by role/right */
	final protected static long singleBitOn(Role role, Right right) {
		return ( 0L | ( right.mask() << role.shift() ) );
	}
	
	/** an immutable wrapper around {@link Permission} instances so that 
	 * 	commonly used permissions can be made available as public final static
	 *  constants.
	 */
	private static class ImmutablePermissions extends Permissions
	implements Serializable
	{

		private static final long serialVersionUID = -4407900270934589522L;

		/** the delegate {@link Permissions} which this immutable wrapper
		 * bases all of its logic on. Not final for reasons of serialization.
		 */
		private Permissions delegate;
		
		/** the sole constructor for an {@link ImmutablePermissions}. Note: this
		 * does not behave like {@link Permissions#Permissions(Permissions)} --
		 * the copy constructor. Rather stores the {@link Permissions} instance
		 * for delegation
		 * 
		 * @param p Non-null {@link Permissions} instance.
		 */
		ImmutablePermissions( Permissions p )
		{
			if ( p == null )
				throw new IllegalArgumentException("Permissions may not be null");
			
			this.delegate = new Permissions( p );
		}
	
		// ~ SETTERS
		// =====================================================================		
		/** throws {@link UnsupportedOperationException}  
		 */
		@Override
		public Permissions grant(Role role, Right... rights) {
			throw new UnsupportedOperationException();
		}
		
		/** throws {@link UnsupportedOperationException}  
		 */
		@Override
		public Permissions revoke(Role role, Right... rights) {
			throw new UnsupportedOperationException();
		}
		
		/** throws {@link UnsupportedOperationException}  
		 */
		@Override
		public Permissions grantAll(Permissions mask) {
			throw new UnsupportedOperationException();
		}
		
		/** throws {@link UnsupportedOperationException}  
		 */
		@Override
		public Permissions revokeAll(Permissions mask) {
			throw new UnsupportedOperationException();
		}
	
		/** delegates to {@link #set(ome.model.internal.Permissions.Flag)}
		 */
		@Override
		public Permissions set(Flag flag) 
		{
			return delegate.set(flag);
		}
		
		/** delegates to {@link #unSet(ome.model.internal.Permissions.Flag)}
		 */
		@Override
		public Permissions unSet(Flag flag) 
		{
			return delegate.unSet(flag);
		}
		
		// ~ GETTERS
		// =========================================================================
		
		/** delegates to {@link #delegate}
		 */
		@Override
		public boolean isGranted(Role role, Right right) {
			return delegate.isGranted(role, right);
		}

		/** delegates to {@link #delegate}
		 */
		@Override
	    protected long getPerm1()
	    {
	        return delegate.getPerm1();
	    }

		/** delegates to {@link #delegate}
		 */
		@Override
	    protected void setPerm1(long value)
	    {
	        delegate.setPerm1(value);
	    }
		
		/** delegates to {@link #isSet(ome.model.internal.Permissions.Flag)} 
		 */
		@Override
		public boolean isSet(Flag flag)
		{
			return delegate.isSet(flag);
		}
		
		// ~ Other
		// =====================================================================
		
		/** delegates to {@link #identical(Permissions)}
		 */
		@Override
		public boolean identical(Permissions p) 
		{
			return delegate.identical(p);
		}
		
		/** delegates to {@link #sameRights(Permissions)}
		 */
		@Override
		public boolean sameRights(Permissions p) 
		{
			return delegate.sameRights(p);
		}
		
		/** delegates to {@link #toString()}
		 */
		@Override
		public String toString() 
		{
			return delegate.toString();
		}
		
		// ~ Serialization
		// =====================================================================
		
	    private void readObject(ObjectInputStream s)
	    throws IOException, ClassNotFoundException
	    {
	    	Permissions p = (Permissions) s.readObject();
	    	if ( p == null )
	    		throw new IllegalArgumentException("Permissions may not be null");
	    	
	    	this.delegate = new Permissions( p );
	    }
	    
		private void writeObject(ObjectOutputStream s)
	    throws IOException 
	    {
			s.writeObject( delegate );
	    }
		
	}
	
	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// NOTE: when rights or roles change, the definition of EMPTY needs to
	// be kept in sync.
	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	
	/** an immutable {@link Permissions} instance with all {@link Right rights} 
	 * turned off.
	 */
	public final static Permissions EMPTY 
		= new ImmutablePermissions( new Permissions()
			.revoke( USER, READ, WRITE )
			.revoke( GROUP, READ, WRITE )
			.revoke( WORLD, READ, WRITE ));
	
	// ~ Systematic
	// =========================================================================	
	/*
	 *  All possible (sensible) permission combinations are:
	 *   
	 *    R_____  user immutable
	 *    RW____  user private
	 *    RWR___  group readable
	 *    RWRW__  group private
	 *    RWRWR_  group writeable
	 *    RWRWRW  world writeable
	 *    RWR_R_  user writeable
	 *    R_R_R_  world immutable
	 *    R_R___  group immutable
	 */
	
	/**
	 * R______ : user and only the user can only read
	 */
	public final static Permissions USER_IMMUTABLE
		= new ImmutablePermissions( new Permissions(EMPTY)
				.grant( USER, READ ));
	
	/** RW____ : user and only user can read and write
	 */
	public final static Permissions USER_PRIVATE 
		= new ImmutablePermissions( new Permissions(EMPTY)
				.grant( USER, READ, WRITE ));
	
	/**
	 * RWR___ : user can read and write, group can read
	 */
	public final static Permissions	GROUP_READABLE
		= new ImmutablePermissions( new Permissions(USER_PRIVATE)
			.grant( GROUP, READ));

	
	/** RWRW__ : user and group can read and write
	 */
	public final static Permissions GROUP_PRIVATE 
		= new ImmutablePermissions( new Permissions(GROUP_READABLE)
				.grant( GROUP, WRITE ));
	
	/**
	 * RWRWR_ : user and group can read and write, world can read
	 */

	public final static Permissions	GROUP_WRITEABLE
		= new ImmutablePermissions( new Permissions(GROUP_PRIVATE)
			.grant( WORLD, READ ));

	/**
	 * RWRWRW : everyone can read and write
	 */
	public final static Permissions	WORLD_WRITEABLE
		= new ImmutablePermissions( new Permissions(GROUP_WRITEABLE)
			.grant( WORLD, WRITE));

	/**
	 * RWR_R_ : all can read, user can write
	 */
	public final static Permissions	USER_WRITEABLE
		= new ImmutablePermissions( new Permissions(GROUP_READABLE)
			.grant( WORLD, READ ));
	
	/**
	 * R_R_R_ : all can only read
	 */
	public final static Permissions	WORLD_IMMUTABLE
		= new ImmutablePermissions( new Permissions(USER_WRITEABLE)
				.revoke( USER, WRITE ));
	
	/**
	 * R_R___ : user and group can only read
	 */
	public final static Permissions GROUP_IMMUTABLE
		= new ImmutablePermissions( new Permissions(WORLD_IMMUTABLE)
				.revoke( WORLD, READ ));
	

	// ~ Non-systematic (easy to remember)
	// =========================================================================
	
	/** an immutable {@link Permissions} instance which is used as the default
	 * value in all persistent classes. It revokes {@link Right#WRITE} to both
	 * {@link Role#GROUP} and {@link Role#WORLD} 
	 */
	public final static Permissions DEFAULT = USER_WRITEABLE;
	
	/** an immutable {@link Permissions} instance with all {@link Right#WRITE}
	 * rights turned off. Identical to {@link #WORLD_IMMUTABLE} 
	 */
	public final static Permissions READ_ONLY = WORLD_IMMUTABLE;

	/** an immutable {@link Permissions} instance with all {@link Right Rights}
	 * granted. Identical to {@link #WORLD_WRITEABLE}
	 */
	public final static Permissions PUBLIC = WORLD_WRITEABLE;

}
