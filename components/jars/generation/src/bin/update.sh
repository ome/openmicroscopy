jena n3 --rdf-xml ome.n3.owl > tmp.xml
jena dbremove --model ome
jena dbcreate --model ome
jena dbload --model ome tmp.xml
