function [matlabList] = toMatlabList(arraylist)
for i=0:arraylist.size()-1,
 matlabList(i+1)=arraylist.get(i);
end
