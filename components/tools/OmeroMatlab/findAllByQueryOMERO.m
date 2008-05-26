function [results] = findAllByQueryOMERO(serviceFactory, query)
queryStr = java.lang.String(query);
results = serviceFactory.findAllByQuery(queryStr);