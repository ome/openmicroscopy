
#ifndef TEST
#define TEST
#include<ome.ice>
#include<ome/model/core/Roi5D.ice>

module mono {

  interface T {

    ome::model::core::Roi5DRemote getRoi5D();

  };

};

#endif 
