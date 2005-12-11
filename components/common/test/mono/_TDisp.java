// **********************************************************************
//
// Copyright (c) 2003-2005 ZeroC, Inc. All rights reserved.
//
// This copy of Ice is licensed to you under the terms described in the
// ICE_LICENSE file included in this distribution.
//
// **********************************************************************

// Ice version 2.1.2

package mono;

public abstract class _TDisp extends Ice.ObjectImpl implements T
{
    protected void
    ice_copyStateFrom(Ice.Object __obj)
	throws java.lang.CloneNotSupportedException
    {
	throw new java.lang.CloneNotSupportedException();
    }

    public static final String[] __ids =
    {
	"::Ice::Object",
	"::mono::T"
    };

    public boolean
    ice_isA(String s)
    {
	return java.util.Arrays.binarySearch(__ids, s) >= 0;
    }

    public boolean
    ice_isA(String s, Ice.Current __current)
    {
	return java.util.Arrays.binarySearch(__ids, s) >= 0;
    }

    public String[]
    ice_ids()
    {
	return __ids;
    }

    public String[]
    ice_ids(Ice.Current __current)
    {
	return __ids;
    }

    public String
    ice_id()
    {
	return __ids[1];
    }

    public String
    ice_id(Ice.Current __current)
    {
	return __ids[1];
    }

    public static String
    ice_staticId()
    {
	return __ids[1];
    }

    public final ome.model.roi.Roi5DRemote
    getRoi5D()
    {
	
	return getRoi5D(null);
    }

    public static IceInternal.DispatchStatus
    ___getRoi5D(T __obj, IceInternal.Incoming __inS, Ice.Current __current)
    {
	IceInternal.BasicStream __os = __inS.os();
	ome.model.roi.Roi5DRemote __ret = __obj.getRoi5D(__current);
	__os.writeObject(__ret);
	__os.writePendingObjects();
	return IceInternal.DispatchStatus.DispatchOK;
    }

    private final static String[] __all =
    {
	"getRoi5D",
	"ice_id",
	"ice_ids",
	"ice_isA",
	"ice_ping"
    };

    public IceInternal.DispatchStatus
    __dispatch(IceInternal.Incoming in, Ice.Current __current)
    {
	int pos = java.util.Arrays.binarySearch(__all, __current.operation);
	if(pos < 0)
	{
	    return IceInternal.DispatchStatus.DispatchOperationNotExist;
	}

	switch(pos)
	{
	    case 0:
	    {
		return ___getRoi5D(this, in, __current);
	    }
	    case 1:
	    {
		return ___ice_id(this, in, __current);
	    }
	    case 2:
	    {
		return ___ice_ids(this, in, __current);
	    }
	    case 3:
	    {
		return ___ice_isA(this, in, __current);
	    }
	    case 4:
	    {
		return ___ice_ping(this, in, __current);
	    }
	}

	assert(false);
	return IceInternal.DispatchStatus.DispatchOperationNotExist;
    }
}
