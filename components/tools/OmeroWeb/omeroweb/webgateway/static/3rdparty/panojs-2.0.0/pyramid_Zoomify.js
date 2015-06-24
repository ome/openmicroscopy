/*******************************************************************************
  ZoomifyAJAX - creates an image URL pyramid based on Zoomify tiles
  <http://www.staremapy.cz/zoomifyjs/>
  <http://www.zoomify.com/>
  
  GSV 3.0 : PanoJS3
  @author Klokan Petr Pridal
  
  Copyright (c) Klokan Petr Pridal

*******************************************************************************/

function ZoomifyLevel( width, height, tilesize ) {
    this.width = width;
    this.height = height;
    this.xtiles = Math.ceil( width / tilesize );
    this.ytiles = Math.ceil( height / tilesize );
}
ZoomifyLevel.prototype.tiles = function() {
    return this.xtiles * this.ytiles;
}

function ZoomifyPyramid( width, height, tilesize ) {
    this.width = width;
    this.height = height;
    this.tilesize = tilesize;
    this._pyramid = Array();
    var level = new ZoomifyLevel( width, height, tilesize );
    while (level.width > tilesize | level.height > tilesize ) {
        this._pyramid.push( level );
        level = new ZoomifyLevel( Math.floor( level.width / 2 ), Math.floor( level.height / 2 ), tilesize )
    }
    this._pyramid.push( level );
    this._pyramid.reverse();

    this.length = this._pyramid.length;
    this.levels = this._pyramid.length;
    // tiles() is needed
    //this.tiles = this.tiles_upto_level( this.levels );
}

ZoomifyPyramid.prototype.getMaxLevel = function() {
    return this.levels - 1;    
}

ZoomifyPyramid.prototype.tiles_upto_level = function( level ) {
    var tiles = 0;
    for (var i = 0; i < level; i++) {
        tiles = tiles + this._pyramid[i].tiles();
    }
    return tiles;
}
ZoomifyPyramid.prototype.tiles = function() {
    return this.tiles_upto_level( this.levels );
}
ZoomifyPyramid.prototype.tile_index = function( level, x_coordinate, y_coordinate ) {
    return x_coordinate + y_coordinate * this._pyramid[ level ].xtiles + this.tiles_upto_level( level );
}
ZoomifyPyramid.prototype.tile_filename = function( level, x_coordinate, y_coordinate ) {
    return "TileGroup" + Math.floor( this.tile_index( level, x_coordinate, y_coordinate ) / this.tilesize ) + "/" + level + "-" + x_coordinate + "-" + y_coordinate + ".jpg";
}

