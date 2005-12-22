package ome.ro.ejb;

import java.util.List;

public interface Generic
{

    Object[] run(String query);
    void persist(Object[] objects);
    
}
