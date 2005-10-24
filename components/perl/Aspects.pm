# Add to OME::Factory

#-------------------------------------------------------------------------------
#
# Copyright (C) 2003 Open Microscopy Environment
#       Massachusetts Institute of Technology,
#       National Institutes of Health,
#       University of Dundee
#
#
#
#    This library is free software; you can redistribute it and/or
#    modify it under the terms of the GNU Lesser General Public
#    License as published by the Free Software Foundation; either
#    version 2.1 of the License, or (at your option) any later version.
#
#    This library is distributed in the hope that it will be useful,
#    but WITHOUT ANY WARRANTY; without even the implied warranty of
#    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
#    Lesser General Public License for more details.
#
#    You should have received a copy of the GNU Lesser General Public
#    License along with this library; if not, write to the Free Software
#    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
#
#-------------------------------------------------------------------------------

package main;
use Aspect;
use Carp qw(cluck croak confess carp);
use Data::Dumper::Simple;

$Data::Dumper::Maxdepth=4;
$Data::Dumper::Indent=1;

$pointcut = 
call qr/^OME::.*?::storeObject/ |
call qr/^OME::.*?::deleteObject/ |
call qr/^OME::.*?::populate/ | 
call qr/^OME::.*?::getDataHash/ |
call qr/^OME::.*?::populate_list/ |
call qr/^OME::.*?::getDataHashes/ |
call qr/^OME::.*?::refresh/ |
call qr/^OME::.*?::loadObject/ |
call qr/^OME::.*?::loadAttribute/ |
call qr/^OME::.*?::objectExists/ |
call qr/^OME::.*?::findObject/ |
call qr/^OME::.*?::findObjects/ |
call qr/^OME::.*?::countObjects/ |
call qr/^OME::.*?::objectExistsLike/ |
call qr/^OME::.*?::findObjectLike/ |
call qr/^OME::.*?::findObjectsLike/ |
call qr/^OME::.*?::countObjectsLike/ |
call qr/^OME::.*?::newObject/ |
call qr/^OME::.*?::maybeNewObject/ |
call qr/^OME::.*?::findAttributes/ |
call qr/^OME::.*?::findAttributesLike/ |
call qr/^OME::.*?::countAttributesLike/ |
call qr/^OME::.*?::countAttributes/ |
call qr/^OME::.*?::findAttribute/ |
call qr/^OME::.*?::newParentAttribute/ | 
call qr/^OME::.*?::newAttribute/ |
call qr/^OME::.*?::getAttributeTypePackage/ |
call qr/^OME::.*?::requireAttributeTypePackage/ |
call qr/^OME::.*?::dataTables/ |
call qr/^OME::.*?::newAttributesWithGuessing/ |
call qr/^OME::.*?::newAttributesInOneRow/ |
call qr/^OME::.*?::newAttributes/ ;
#            call qr/^OME::Factory::[^_A]\w+$/
#          | call qr/^OME::DBObject::[^_AC]\w+$/
#          | call qr/^OME::SemanticType::[^_A]\w+$/;
after{ 
     my $context = shift;
     my $method  = $context->sub_name;
     my @params  = @{$context->params}; shift @params;
     my $return  = $context->return_value;
     carp 
         
          "======================================================================\n".
          "======================================================================\n".
          "[METHOD:".$method."]\n".
          "[IN:]\n".Dumper(@params)."\n".
          "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n".
          "[OUT:]\n".Dumper($return)."\n".
          "======================================================================\n".
          "======================================================================\n";

} $pointcut;


#-------------------------------------------------------------------------------
#
# Written by:    Josh Moore
#
#-------------------------------------------------------------------------------

