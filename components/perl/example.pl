#!/usr/bin/perl
#
# example.pl - Redland eaxmple Perl program
#
# $Id: example.pl,v 1.21 2004/07/23 10:02:53 cmdjb Exp $
#
# Copyright (C) 2000-2001 David Beckett - http://purl.org/net/dajobe/
# Institute for Learning and Research Technology - http://www.ilrt.org/
# University of Bristol - http://www.bristol.ac.uk/
# 
# This package is Free Software or Open Source available under the
# following licenses (these are alternatives):
#   1. GNU Lesser General Public License (LGPL)
#   2. GNU General Public License (GPL)
#   3. Mozilla Public License (MPL)
# 
# See LICENSE.html or LICENSE.txt at the top of this package for the
# full license terms.
# 
# 
#

use RDF::Redland;

$test_file="ome.owl";

warn "Creating storage\n";
my $storage=new RDF::Redland::Storage("hashes", "test", 
				      "new='yes',hash-type='bdb',dir='.'");
die "Failed to create RDF::Redland::Storage\n" unless $storage;
  
warn "\nCreating model\n";
my $model=new RDF::Redland::Model($storage, "");
die "Failed to create RDF::Redland::Model for storage\n" unless $model;


my $creator_uri=new RDF::Redland::URI("http://purl.org/dc/elements/1.1/creator");

warn "\nCreating statement\n";
my $statement=new RDF::Redland::Statement(RDF::Redland::Node->new_from_uri("http://purl.org/net/dajobe/"),
					  $creator_uri,
					  new RDF::Redland::Node("Dave Beckett"));
die "Failed to create RDF::Redland::Statement\n" unless $statement;

warn "\nAdding statement to model\n";
$model->add_statement($statement);
$statement=undef;

my $n=new RDF::Redland::URI('http://example.org/foo');
my $statementn=new RDF::Redland::Statement($n, $n, $n);

warn "\nAdding statement (new Statement(n, n, n)) to model\n";
$model->add_statement($statementn);
$statement=undef;

warn "\nAdding statement (n,n,n) to model\n";
$model->add_statement($n, $n, $n);
$statement=undef;

warn "\nParsing URI (file) $test_file\n";
my $uri=new RDF::Redland::URI("file:$test_file");

# Use any rdf/xml parser that is available
my $parser=new RDF::Redland::Parser("rdfxml", "application/rdf+xml");
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
