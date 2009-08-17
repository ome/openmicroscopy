function [images] = findAllImages(gateway)

% Get the current user projects and populate the datasets.
projects = gateway.getProjects([], true);

datasetList = java.util.ArrayList;

for projectCnt = 0:projects.size()-1,
	projectDatasetList = getDatasetsFromProject(omerojService, projects.get(projectCnt));
    for j = 0:projectDatasetList.size()-1,
    	datasetList.add(projectDatasetList.get(j));
    end
end
images = java.util.ArrayList;
for i = 0:datasetList.size()-1,
    imageList = getImagesFromDataset(omerojService, datasetList.get(i));
    for j = 0:imageList.size()-1,
        images.add(imageList.get(j));
    end
end
    
