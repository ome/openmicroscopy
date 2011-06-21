/*******************************************************************************
  BisqueISPyramid - creates an image URL pyramid based on Bisque Image Service
  <http://www.bioimage.ucsb.edu/downloads/Bisque%20Database>
  
  GSV 3.0 : PanoJS3
  @author Dmitry Fedorov  <fedorov@ece.ucsb.edu>   
  
  Copyright (c) 2010 Dmitry Fedorov, Center for Bio-Image Informatics

*******************************************************************************/

function formatInt(n, pad) {
    var s = n.toString();
    while (s.length<pad)
        s = '0'+s;
    return s;
};   

// -----------------------------------------------------
// BisqueISLevel
// -----------------------------------------------------

function BisqueISLevel( width, height, tilesize, level ) {
    this.width = width;
    this.height = height;
    this.xtiles = Math.ceil( width / tilesize );
    this.ytiles = Math.ceil( height / tilesize );
    this.level = level;
}

BisqueISLevel.prototype.tiles = function() {
    return this.xtiles * this.ytiles;
}

// -----------------------------------------------------
// BisqueISPyramid
// -----------------------------------------------------

function BisqueISPyramid( width, height, tilesize ) {
    this.width = width;
    this.height = height;
    this.tilesize = tilesize;
    this._pyramid = Array();
    
    var level_id = 0;
    var level_width = width;    
    var level_height = height;   
    var min_size = (tilesize / 2) + 1;
    while (level_id==0 || level_width > min_size || level_height > min_size ) {      
        var level = new BisqueISLevel( level_width, level_height, tilesize, level_id );
        this._pyramid.push( level );
        level_width  = Math.floor( level_width / 2 );
        level_height = Math.floor( level_height / 2 );
        level_id++;
    }
    this._pyramid.reverse();
    
    this.length = this._pyramid.length;
    this.levels = this._pyramid.length;
}

BisqueISPyramid.prototype.getMaxLevel = function() {
    return this.levels - 1;    
}

BisqueISPyramid.prototype.getLevel = function( level ) {
    if (level<this._pyramid.length)
        return this._pyramid[ level ];    
    else
        return this._pyramid[ this._pyramid.length-1 ];          
}

BisqueISPyramid.prototype.tiles_upto_level = function( level ) {
    var tiles = 0;
    for (var i = 0; i < level; i++) {
        tiles = tiles + this._pyramid[i].tiles();
    }
    return tiles;
}

BisqueISPyramid.prototype.tiles = function() {
    return this.tiles_upto_level( this.levels );
}

BisqueISPyramid.prototype.tile_index = function( level, x_coordinate, y_coordinate ) {
    return x_coordinate + y_coordinate * this._pyramid[ level ].xtiles + this.tiles_upto_level( level );
}

BisqueISPyramid.prototype.tile_filename = function( level, x_coordinate, y_coordinate ) {
    var l = this.getLevel(level).level;
    var x = x_coordinate;
    var y = y_coordinate;
    return 'tile=' + l + ',' + x + ',' + y + ',' + this.tilesize;
}

