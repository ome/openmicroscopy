package ome.server.itests.update;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import ome.model.IObject;
import ome.server.itests.AbstractInternalContextTest;


public class AbstractUpdateTest
        extends AbstractInternalContextTest
{
    
    protected boolean equalCollections(Collection<IObject> before, Collection<IObject> after)
    {
        Set<Long> beforeIds = new HashSet<Long>();
        for (IObject object : before)
        {
            beforeIds.add(object.getId());
        }
        
        Set<Long> afterIds = new HashSet<Long>();
        for (IObject object : after)
        {
            afterIds.add(object.getId());
        }
        
        return beforeIds.containsAll(afterIds);
    }
    
}
