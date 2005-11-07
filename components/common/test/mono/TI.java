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

import ome.model.adapters.Model2IceMapper;
import ome.model.core.Roi3D;
import ome.model.core.Roi3DRemote;
import ome.model.core.Roi4D;
import ome.model.core.Roi4DRemote;
import ome.model.core.Roi5D;
import ome.model.core.Roi5DRemote;
import ome.model.meta.Event;
import ome.model.meta.Experimenter;

public final class TI extends _TDisp
{
	Roi5DRemote r5 = new Roi5DRemote();
	Roi4DRemote r4 = new Roi4DRemote();
	Roi4DRemote r4_ = new Roi4DRemote();
	Roi3DRemote r3 = new Roi3DRemote();
	Set s1 = new HashSet();
	Set s2 = new HashSet();
	Set s3 = new HashSet();
	
	public TI(){
		Roi5D h5 = new Roi5D();
		Roi4D h4 = new Roi4D();
		Roi4D h4_ = new Roi4D();
		Roi3D h3 = new Roi3D();
		Roi3D h3_ = new Roi3D();
		Event ev = new Event();
		s1.add(h4);
		s1.add(h4_);
		h5.setRoi4ds(s1);
		h5.setCreationEvent(ev);
		s2.add(h3);
		s3.add(r3);
		h4.setRoi3ds(s2);
		h4_.setRoi3ds(s3);
		r5 = (Roi5DRemote) new Model2IceMapper().mapWithReflection(h5);
	}
	
	public
    TI(boolean useRemote)
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
