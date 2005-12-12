#include <ome.ice>
#include <ome/model/core/Image.ice>

module main
{
module eis
{
module generic
{
	sequence<ome::model::core::ImageRemote> Graph;

	interface Server
	{
		Graph 	query(string path);
		void  	putGraph(Graph myGraph);
	};
};
};
};
