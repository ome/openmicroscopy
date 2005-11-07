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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import ome.model.core.Roi3DRemote;
import ome.model.core.Roi4DRemote;
import ome.model.core.Roi5DRemote;

public final class TI extends _TDisp
{
	Roi5DRemote r5 = new Roi5DRemote();
	Roi4DRemote r4 = new Roi4DRemote();
	Roi4DRemote r4_ = new Roi4DRemote();
	Roi3DRemote r3 = new Roi3DRemote();
	Set s1 = new HashSet();
	Set s2 = new HashSet();
	Set s3 = new HashSet();
	public
    TI()
    {
    	s1.add(r4); 
    	s1.add(r4_);
    	r5.setRoi4ds(s1);
    	s2.add(r3);
    	s3.add(r3);
    	r4.setRoi3ds(s2);
    	r4_.setRoi3ds(s3);
    }

    public ome.model.core.Roi5DRemote
    getRoi5D(Ice.Current __current)
    {
	return r5; 
    }
}
