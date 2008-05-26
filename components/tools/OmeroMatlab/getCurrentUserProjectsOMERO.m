function [projects] = getCurrentUserProjectsOMERO(serviceFactory, getLeaves)
projects = serviceFactory.getProjects([], getLeaves);
%projects = toMatlabList(list);