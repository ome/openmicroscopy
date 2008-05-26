function [projects] = getProjectsOMERO(serviceFactory, idList, getLeaves)
list = java.util.ArrayList;
if(~isempty(idList))
    for(i=1:length(idList))
        list.add(java.lang.Long(idList(i)));
    end
end

projects = serviceFactory.getProjects(list, getLeaves);
%projects = toMatlabList(list);