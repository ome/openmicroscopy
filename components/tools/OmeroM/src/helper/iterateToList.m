function [list] = iterateToList(iterator)

list = java.util.ArrayList;
while(iterator.hasNext())
    list.add(iterator.next());
end