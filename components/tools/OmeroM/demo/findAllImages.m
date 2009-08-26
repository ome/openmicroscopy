function [images] = findAllImages(gateway)
% Get the current user's projects and populate the datasets.
%
% The Gateway object is the service as returned by loadOmero
projects = gateway.getProjects([], true);

datasetList = java.util.ArrayList;

for projectCnt = 0:projects.size()-1,
	projectDatasetList = projects.get(projectCnt).linkedDatasetList;
    for j = 0:projectDatasetList.size()-1,
	datasetList.add(projectDatasetList.get(j));
    end
end
images = java.util.ArrayList;
for i = 0:datasetList.size()-1,
    imageList = datasetList.get(i).linkedImageList;
    for j = 0:imageList.size()-1,
        images.add(imageList.get(j));
    end
end
