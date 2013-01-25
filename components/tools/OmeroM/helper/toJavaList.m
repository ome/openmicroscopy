function [javaList] = toJavaList(matlabList)
% Convert a MATLAB vector into a Java ArrayList

% Check input
assert(isvector(matlabList) || isempty(matlabList),...
    'OMERO:toJavaList:wrongInputType',...
    'Input must be a vector');

% Create Java list
javaList = java.util.ArrayList;
for i=1:length(matlabList)
    javaList.add(matlabList(i));
end
