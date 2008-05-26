function [datasets] = getDatasetsOMERO(serviceFactory, list, getLeaves)

datasetlist = java.util.ArrayList;
if(~isempty(list))
	for i=1:length(list)
		datasetlist.add(java.lang.Long(list(i)));
    end
end
datasets = serviceFactory.getDatasets(datasetlist, getLeaves);
%datasets = toMatlabList(list);