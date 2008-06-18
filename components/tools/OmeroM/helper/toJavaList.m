function [javaList] = toJavaList(matlabList)

javaList = java.util.ArrayList;
for i=1:length(matlabList)
    javaList.add(matlabList(i));
end
