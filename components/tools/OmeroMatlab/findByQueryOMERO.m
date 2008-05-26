function [results] = findByQueryOMERO(serviceFactory, query)
queryStr = java.lang.String(query);
results = serviceFactory.findByQuery(queryStr);