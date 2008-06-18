function [javaMap] = idListToMap(idList)

javaMap = java.util.TreeMap;
for i = 0:idList.size()-1
    object = idList.get(i);
    javaMap.put(object.id.val, object);
end
