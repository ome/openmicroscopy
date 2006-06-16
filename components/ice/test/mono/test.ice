
#ifndef TEST
#define TEST
#include<ome.ice>
#include<ome/model/roi/Roi5D.ice>

module mono {

  interface T {

    ome::model::roi::Roi5DRemote getRoi5D();

  };

};

#endif 
