function [images] = getImagesFromDatasetOMERO(serviceFactory, list)

datasetlist = java.util.ArrayList;
if(~isempty(list))
	for i=1:length(list)
		datasetlist.add(java.lang.Long(list(i)));
	end
end
images = serviceFactory.getImages(blitzgateway.util.OMEROClass.Dataset, datasetlist);
%images = toMatlabList(list);