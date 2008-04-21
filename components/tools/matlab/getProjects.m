function [projects] = getProjects(gateway)
projects = gateway.getProjects([],false);