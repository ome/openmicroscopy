#!/usr/bin/perl

use RDF::Redland;

warn "Creating storage\n";
my $storage=new RDF::Redland::Storage("hashes", "test", 
				      "new='yes',hash-type='bdb',dir='.'");
die "Failed to create RDF::Redland::Storage\n" unless $storage;
  
warn "\nCreating model\n";
my $model=new RDF::Redland::Model($storage, "");
die "Failed to create RDF::Redland::Model for storage\n" unless $model;

my $test_uri="http://localhost:8080/ome3-srv/Image?id=4";
warn "\nParsing URI (file) $test_uri\n";
my $uri=new RDF::Redland::URI("$test_uri");

# Use any rdf/xml parser that is available
#my $parser=new RDF::Redland::Parser("rdfxml", "application/rdf+xml");
my $parser=new RDF::Redland::Parser("ntriples");
die "Failed to find parser\n" if !$parser;

$stream=$parser->parse_as_stream($uri,$uri);
my $count=0;
while(!$stream->end) {
  $model->add_statement($stream->current);
  $count++;
  $stream->next;
}
$stream=undef;
warn "Parsing added $count statements\n";

warn "\nPrinting all statements\n";
$stream=$model->as_stream;
while(!$stream->end) {
  print "Statement: ",$stream->current->as_string,"\n";
  $stream->next;
}
$stream=undef;

warn "\nSearching model for statements matching predicate http://purl.org/dc/elements/1.1/creator\n";
$statement=new RDF::Redland::Statement(undef, $creator_uri, undef);
my $stream=$model->find_statements($statement);
while(!$stream->end) {
  my $statement2=$stream->current;
  print "Matching Statement: ",$statement2->as_string,"\n";
  my $subject=$statement2->subject;
  print "  Subject: ",$subject->as_string,"\n";
  print "  Predicate: ",$statement2->predicate->as_string,"\n";
  print "  Object: ",$statement2->object->as_string,"\n";
  $stream->next;
}
$stream=undef;

$statement=undef;


my $home=RDF::Redland::Node->new_from_uri("http://purl.org/net/dajobe/");
warn "\nSearching model for targets of subject ",$home->uri->as_string," predicate ", $creator_uri->as_string, "\n";
my(@nodes)=$model->targets($home, new RDF::Redland::Node($creator_uri));
die "Failed to find any targets matching\n"
  unless @nodes;
for my $node (@nodes) {
  print "Matching Node: ",$node->as_string,"\n";
}
$iterator=undef;


my $q = new RDF::Redland::Query("SELECT ?a ?c WHERE (?a dc:title ?c) USING dc FOR <http://purl.org/dc/elements/1.1/>");
print "Querying for dc:titles:\n";
my $results=$model->query_execute($q);
my $count=1;
while(!$results->finished) {
  print "result $count: {\n";
  for(my $i=0; $i < $results->bindings_count; $i++) {
    print "  ",$results->binding_name($i),"=",$results->binding_value($i)->as_string,"\n";
  }
  print "}\n";
  $results->next_result;
  $count++;
}
$results=undef;

warn "\nWriting model to test-out.rdf as rdf/xml\n";

# Use any rdf/xml parser that is available
my $serializer=new RDF::Redland::Serializer("rdfxml");
die "Failed to find serializer\n" if !$serializer;

$serializer->serialize_model_to_file("test-out.rdf", $uri, $model);
$serializer=undef;


warn "\nDone\n";

# Required in order to ensure storage is correctly flushed to disk
$storage=undef;
$model=undef;
l
