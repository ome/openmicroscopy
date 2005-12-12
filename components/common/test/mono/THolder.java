// **********************************************************************
//
// Copyright (c) 2003-2005 ZeroC, Inc. All rights reserved.
//
// This copy of Ice is licensed to you under the terms described in the
// ICE_LICENSE file included in this distribution.
//
// **********************************************************************

// Ice version 3.0.0

package mono;

public final class THolder
{
    public
    THolder()
    {
    }

    public
    THolder(T value)
    {
	this.value = value;
    }

    public class Patcher implements IceInternal.Patcher
    {
	public void
	patch(Ice.Object v)
	{
	    value = (T)v;
	}

	public String
	type()
	{
	    return "::mono::T";
	}
    }

    public Patcher
    getPatcher()
    {
	return new Patcher();
    }

    public T value;
}
