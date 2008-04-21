function [datasets] = getDatasets(gateway, list)

datasetlist = java.util.ArrayList
if(~isempty(list))
	for i=1:length(list)
		datasetlist.add(list(i))
	end
end
datasets = gateway.getDatasets(datasetlist,false)