/**
 * Panoramic JavaScript Image Viewer (PanoJS) 1.0.2
 *
 * Generates a draggable and zoomable viewer for images that would
 * be otherwise too large for a browser window.  Examples would include
 * maps or high resolution document scans.
 *
 * Images must be precut into tiles, such as by the accompanying tilemaker.py
 * python library.
 *
 * <div class="viewer">
 *   <div class="well"><!-- --></div>
 *   <div class="surface"><!-- --></div>
 *   <div class="controls">
 *     <a href="#" class="zoomIn">+</a>
 *     <a href="#" class="zoomOut">-</a>
 *   </div>
 * </div>
 * 
 * The "well" node is where generated IMG elements are appended. It
 * should have the CSS rule "overflow: hidden", to occlude image tiles
 * that have scrolled out of view.
 * 
 * The "surface" node is the transparent mouse-responsive layer of the
 * image viewer, and should match the well in size.
 *
 * var viewerBean = new PanoJS(element, 'tiles', 256, 3, 1);
 *
 * To disable the image toolbar in IE, be sure to add the following:
 * <meta http-equiv="imagetoolbar" content="no" />
 *
 * Copyright (c) 2005 Michal Migurski <mike-gsv@teczno.com>
 *                    Dan Allen <dan.allen@mojavelinux.com>
 * 
 * Redistribution and use in source form, with or without modification,
 * are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * @author Michal Migurski <mike-gsv@teczno.com>
 * @author Dan Allen <dan.allen@mojavelinux.com>
 *
 * NOTE: if artifacts are appearing, then positions include half-pixels
 * TODO: additional jsdoc and package jsmin
 * TODO: Tile could be an object
 */
function PanoJS(viewer, options) {

	// listeners that are notified on a move (pan) event
	this.viewerMovedListeners = [];
	// listeners that are notified on a zoom event
	this.viewerZoomedListeners = [];

	if (typeof viewer == 'string') {
		this.viewer = document.getElementById(viewer);
	}
	else {
		this.viewer = viewer;
	}

	if (typeof options == 'undefined') {
		options = {};
	}

	if (typeof options.tileUrlProvider != 'undefined' &&
		PanoJS.isInstance(options.tileUrlProvider, PanoJS.TileUrlProvider)) {
		this.tileUrlProvider = options.tileUrlProvider;
	}
	else {
		this.tileUrlProvider = new PanoJS.TileUrlProvider(
			options.tileBaseUri ? options.tileBaseUri : PanoJS.TILE_BASE_URI,
			options.tilePrefix ? options.tilePrefix : PanoJS.TILE_PREFIX,
			options.tileExtension ? options.tileExtension : PanoJS.TILE_EXTENSION
		);
	}

    this.tileSize = (options.tileSize ? options.tileSize : PanoJS.TILE_SIZE);

	this.tileSizeX = (options.tileSizeX ? options.tileSizeX : PanoJS.TILE_SIZE);
    this.tileSizeY = (options.tileSizeY ? options.tileSizeY : PanoJS.TILE_SIZE);

	// assign and do some validation on the zoom levels to ensure sanity
	this.zoomLevel = (typeof options.initialZoom == 'undefined' ? -1 : parseInt(options.initialZoom));
	this.maxZoomLevel = (typeof options.maxZoom == 'undefined' ? 0 : Math.abs(parseInt(options.maxZoom)));
	if (this.zoomLevel > this.maxZoomLevel) {
		this.zoomLevel = this.maxZoomLevel;
	}

	this.initialPan = (options.initialPan ? options.initialPan : PanoJS.INITIAL_PAN);

	this.initialized = false;
	this.surface = null;
	this.well = null;
	this.width = 0;
	this.height = 0;
	this.top = 0;
	this.left = 0;
	this.x = 0;
	this.y = 0;
	this.border = -1;
	this.mark = { 'x' : 0, 'y' : 0 };
	this.pressed = false;
	this.tiles = [];
	this.cache = {};
	var blankTile = options.blankTile ? options.blankTile : PanoJS.BLANK_TILE_IMAGE;
	var loadingTile = options.loadingTile ? options.loadingTile : PanoJS.LOADING_TILE_IMAGE;
	this.cache['blank'] = new Image();
	this.cache['blank'].src = blankTile;
	if (blankTile != loadingTile) {
		this.cache['loading'] = new Image();
		this.cache['loading'].src = loadingTile;
	}
	else {
		this.cache['loading'] = this.cache['blank'];
	}

	// employed to throttle the number of redraws that
	// happen while the mouse is moving
	this.moveCount = 0;
	this.slideMonitor = 0;
	this.slideAcceleration = 0;

	// add to viewer registry
	PanoJS.VIEWERS[PanoJS.VIEWERS.length] = this;
}

// project specific variables
PanoJS.PROJECT_NAME = 'PanoJS';
PanoJS.PROJECT_VERSION = '1.0.0';
PanoJS.REVISION_FLAG = '';

// CSS definition settings
PanoJS.SURFACE_STYLE_CLASS = 'surface';
PanoJS.WELL_STYLE_CLASS = 'well';
PanoJS.CONTROLS_STYLE_CLASS = 'controls'
PanoJS.TILE_STYLE_CLASS = 'tile';

// language settings
PanoJS.MSG_BEYOND_MIN_ZOOM = 'Cannot zoom out past the current level.';
PanoJS.MSG_BEYOND_MAX_ZOOM = 'Cannot zoom in beyond the current level.';

// defaults if not provided as constructor options
PanoJS.TILE_BASE_URI = 'tiles';
PanoJS.TILE_PREFIX = 'tile-';
PanoJS.TILE_EXTENSION = 'jpg';
PanoJS.TILE_SIZE = 256;
PanoJS.BLANK_TILE_IMAGE = 'blank.gif';
PanoJS.LOADING_TILE_IMAGE = 'blank.gif';
PanoJS.INITIAL_PAN = { 'x' : .5, 'y' : .5 };
PanoJS.USE_LOADER_IMAGE = true;
PanoJS.USE_SLIDE = true;
PanoJS.USE_KEYBOARD = true;

// performance tuning variables
PanoJS.MOVE_THROTTLE = 3;
PanoJS.SLIDE_DELAY = 40;
PanoJS.SLIDE_ACCELERATION_FACTOR = 5;

// the following are calculated settings
PanoJS.DOM_ONLOAD = (navigator.userAgent.indexOf('KHTML') >= 0 ? false : true);
PanoJS.GRAB_MOUSE_CURSOR = (navigator.userAgent.search(/KHTML|Opera/i) >= 0 ? 'pointer' : (document.attachEvent ? 'url(grab.cur)' : '-moz-grab'));
PanoJS.GRABBING_MOUSE_CURSOR = (navigator.userAgent.search(/KHTML|Opera/i) >= 0 ? 'move' : (document.attachEvent ? 'url(grabbing.cur)' : '-moz-grabbing'));

// registry of all known viewers
PanoJS.VIEWERS = [];

// utility functions
PanoJS.isInstance = function(object, clazz) {
	// FIXME: can this just be replaced with instanceof operator? It has been reported that __proto__ is specific to Netscape
	while (object != null) {
		if (object == clazz.prototype) {
			return true;
		}

		object = object.__proto__;
	}

	return false;
}

PanoJS.prototype = {

	/**
	 * Resize the viewer to fit snug inside the browser window (or frame),
	 * spacing it from the edges by the specified border.
	 *
	 * This method should be called prior to init()
	 * FIXME: option to hide viewer to prevent scrollbar interference
	 */
	fitToWindow : function(border) {
		if (typeof border != 'number' || border < 0) {
			border = 0;
		}

		this.border = border;
		var calcWidth = 0;
		var calcHeight = 0;
		if (window.innerWidth) {
			calcWidth = window.innerWidth;
			calcHeight = window.innerHeight;
		}
		else {
			calcWidth = (document.compatMode == 'CSS1Compat' ? document.documentElement.clientWidth : document.body.clientWidth);
			calcHeight = (document.compatMode == 'CSS1Compat' ? document.documentElement.clientHeight : document.body.clientHeight);
		}
		calcWidth = Math.max(calcWidth - 2 * border, 0);
		calcHeight = Math.max(calcHeight - 2 * border, 0);
		if (calcWidth % 2) {
			calcWidth--;
		}

		if (calcHeight % 2) {
			calcHeight--;
		}

		this.width = calcWidth;
		this.height = calcHeight;
		this.viewer.style.width = this.width + 'px';
		this.viewer.style.height = this.height + 'px';
		this.viewer.style.top = border + 'px';
		this.viewer.style.left = border + 'px';
	},

	init : function() {
		if (document.attachEvent) {
			document.body.ondragstart = function() { return false; }
		}
		
		if (this.width == 0 && this.height == 0) {
			this.width = this.viewer.offsetWidth;
			this.height = this.viewer.offsetHeight;
		}

		//var fullSize = this.tileSize;
		
		var fullSizeX = this.tileSizeX;
		var fullSizeY = this.tileSizeY;
		
		// explicit set of zoom level
		if (this.zoomLevel >= 0 && this.zoomLevel <= this.maxZoomLevel) {
			//fullSize = this.tileSize * Math.pow(2, this.zoomLevel);
			fullSizeX = this.tileSizeX * Math.pow(2, this.zoomLevel);
			fullSizeY = this.tileSizeY * Math.pow(2, this.zoomLevel);
		}
		// calculate the zoom level based on what fits best in window
		else {
			this.zoomLevel = -1;
			fullSizeX = this.tileSizeX / 2;
			fullSizeY = this.tileSizeY / 2;
			do {
				this.zoomLevel += 1;
				fullSizeX *= 2;
				fullSizeY *= 2;
			} while (fullSizeY < Math.max(this.height) || fullSizeX < Math.max(this.width));
			// take into account picture smaller than window size
			if (this.zoomLevel > this.maxZoomLevel) {
				var diff = this.zoomLevel - this.maxZoomLevel;
				this.zoomLevel = this.maxZoomLevel;
				//fullSize /= Math.pow(2, diff);
				fullSizeX /= Math.pow(2, diff);
				fullSizeY /= Math.pow(2, diff);
			}
		}

		// move top level up and to the left so that the image is centered
		this.x = Math.floor((fullSizeX - this.width) * -this.initialPan.x);
		this.y = Math.floor((fullSizeY - this.height) * -this.initialPan.y);

		// offset of viewer in the window
		for (var node = this.viewer; node; node = node.offsetParent) {
			this.top += node.offsetTop;
			this.left += node.offsetLeft;
		}

		for (var child = this.viewer.firstChild; child; child = child.nextSibling) {
			if (child.className == PanoJS.SURFACE_STYLE_CLASS) {
				this.surface = child;
				child.backingBean = this;
			}
			else if (child.className == PanoJS.WELL_STYLE_CLASS) {
				this.well = child;
				child.backingBean = this;
			}
			else if (child.className == PanoJS.CONTROLS_STYLE_CLASS) {
				for (var control = child.firstChild; control; control = control.nextSibling) {
					if (control.className) {
						control.onclick = PanoJS[control.className + 'Handler'];
					}
				}
			}
		}

		this.viewer.backingBean = this;
		this.surface.style.cursor = PanoJS.GRAB_MOUSE_CURSOR;
		this.prepareTiles();
		this.initialized = true;
	},

	prepareTiles : function() {
		var rows = Math.ceil(this.height / this.tileSizeY) + 1;
		var cols = Math.ceil(this.width / this.tileSizeX) + 1;

        for (var c = 0; c < cols; c++) {
			var tileCol = [];

			for (var r = 0; r < rows; r++) {
				/**
				 * element is the DOM element associated with this tile
				 * posx/posy are the pixel offsets of the tile
				 * xIndex/yIndex are the index numbers of the tile segment
				 * qx/qy represents the quadrant location of the tile
				 */
				var tile = {
					'element' : null,
					'posx' : 0,
					'posy' : 0,
					'xIndex' : c,
					'yIndex' : r,
					'qx' : c,
					'qy' : r
				};

				tileCol.push(tile);
			}
		
			this.tiles.push(tileCol);
		}

		this.surface.onmousedown = PanoJS.mousePressedHandler;
		this.surface.onmouseup = this.surface.onmouseout = PanoJS.mouseReleasedHandler;
		this.surface.ondblclick = PanoJS.doubleClickHandler;
		if (PanoJS.USE_KEYBOARD) {
			window.onkeypress = PanoJS.keyboardMoveHandler;
			window.onkeydown = PanoJS.keyboardZoomHandler;
		}

		this.positionTiles();
	},

	/**
	 * Position the tiles based on the x, y coordinates of the
	 * viewer, taking into account the motion offsets, which
	 * are calculated by a motion event handler.
	 */
	positionTiles : function(motion, reset) {
		// default to no motion, just setup tiles
		if (typeof motion == 'undefined') {
			motion = { 'x' : 0, 'y' : 0 };
		}

		for (var c = 0; c < this.tiles.length; c++) {
			for (var r = 0; r < this.tiles[c].length; r++) {
				var tile = this.tiles[c][r];

				tile.posx = (tile.xIndex * this.tileSizeX) + this.x + motion.x;
				tile.posy = (tile.yIndex * this.tileSizeY) + this.y + motion.y;

				var visible = true;

				if (tile.posx > this.width) {
					// tile moved out of view to the right
					// consider the tile coming into view from the left
					do {
						tile.xIndex -= this.tiles.length;
						tile.posx = (tile.xIndex * this.tileSizeX) + this.x + motion.x;
					} while (tile.posx > this.width);

					if (tile.posx + this.tileSizeX < 0) {
						visible = false;
					}

				} else {
					// tile may have moved out of view from the left
					// if so, consider the tile coming into view from the right
					while (tile.posx < -this.tileSizeX) {
						tile.xIndex += this.tiles.length;
						tile.posx = (tile.xIndex * this.tileSizeX) + this.x + motion.x;
					}

					if (tile.posx > this.width) {
						visible = false;
					}
				}

				if (tile.posy > this.height) {
					// tile moved out of view to the bottom
					// consider the tile coming into view from the top
					do {
						tile.yIndex -= this.tiles[c].length;
						tile.posy = (tile.yIndex * this.tileSizeY) + this.y + motion.y;
					} while (tile.posy > this.height);

					if (tile.posy + this.tileSizeY < 0) {
						visible = false;
					}

				} else {
					// tile may have moved out of view to the top
					// if so, consider the tile coming into view from the bottom
					while (tile.posy < -this.tileSizeY) {
						tile.yIndex += this.tiles[c].length;
						tile.posy = (tile.yIndex * this.tileSizeY) + this.y + motion.y;
					}

					if (tile.posy > this.height) {
						visible = false;
					}
				}

				// initialize the image object for this quadrant
				if (!this.initialized) {
					this.assignTileImage(tile, true);
					tile.element.style.top = tile.posy + 'px';
					tile.element.style.left = tile.posx + 'px';
				}

				// display the image if visible
				if (visible) {
					this.assignTileImage(tile);
				}

				// seems to need this no matter what
				tile.element.style.top = tile.posy + 'px';
				tile.element.style.left = tile.posx + 'px';
			}
		}

		// reset the x, y coordinates of the viewer according to motion
		if (reset) {
			this.x += motion.x;
			this.y += motion.y;
		}
	},

	/**
	 * Determine the source image of the specified tile based
	 * on the zoom level and position of the tile.  If forceBlankImage
	 * is specified, the source should be automatically set to the
	 * null tile image.  This method will also setup an onload
	 * routine, delaying the appearance of the tile until it is fully
	 * loaded, if configured to do so.
	 */
	assignTileImage : function(tile, forceBlankImage) {
		var tileImgId, src;
		var useBlankImage = (forceBlankImage ? true : false);

		// check if image has been scrolled too far in any particular direction
		// and if so, use the null tile image
		if (!useBlankImage) {
			var left = tile.xIndex < 0;
			var high = tile.yIndex < 0;
			var right = tile.xIndex >= Math.pow(2, this.zoomLevel);
			var low = tile.yIndex >= Math.pow(2, this.zoomLevel);
			if (high || left || low || right) {
				useBlankImage = true;
			}
		}

		if (useBlankImage) {
			tileImgId = 'blank:' + tile.qx + ':' + tile.qy;
			src = this.cache['blank'].src;
		}
		else {
			tileImgId = src = this.tileUrlProvider.assembleUrl(tile.xIndex, tile.yIndex, this.zoomLevel);
		}

		// only remove tile if identity is changing
		if (tile.element != null &&
			tile.element.parentNode != null &&
			tile.element.relativeSrc != src) {
			this.well.removeChild(tile.element);
		}

		var tileImg = this.cache[tileImgId];
		// create cache if not exist
		if (tileImg == null) {
			tileImg = this.cache[tileImgId] = this.createPrototype(src);
		}

		if (useBlankImage || !PanoJS.USE_LOADER_IMAGE || tileImg.complete || (tileImg.image && tileImg.image.complete)) {
			tileImg.onload = function() {};
			if (tileImg.image) {
				tileImg.image.onload = function() {};
			}

			if (tileImg.parentNode == null) {
				tile.element = this.well.appendChild(tileImg);
			}
		}
		else {
			var loadingImgId = 'loading:' + tile.qx + ':' + tile.qy;
			var loadingImg = this.cache[loadingImgId];
			if (loadingImg == null) {
				loadingImg = this.cache[loadingImgId] = this.createPrototype(this.cache['loading'].src);
			}

			loadingImg.targetSrc = tileImgId;

			var well = this.well;
			tile.element = well.appendChild(loadingImg);
			tileImg.onload = function() {
				// make sure our destination is still present
				if (loadingImg.parentNode && loadingImg.targetSrc == tileImgId) {
					tileImg.style.top = loadingImg.style.top;
					tileImg.style.left = loadingImg.style.left;
					well.replaceChild(tileImg, loadingImg);
					tile.element = tileImg;
				}

				tileImg.onload = function() {};
				return false;
			}

			// konqueror only recognizes the onload event on an Image
			// javascript object, so we must handle that case here
			if (!PanoJS.DOM_ONLOAD) {
				tileImg.image = new Image();
				tileImg.image.onload = tileImg.onload;
				tileImg.image.src = tileImg.src;
			}
		}
	},

	createPrototype : function(src) {
		var img = document.createElement('img');
		img.src = src;
		img.relativeSrc = src;
		img.className = PanoJS.TILE_STYLE_CLASS;
		img.style.width = this.tileSizeX + 'px';
		img.style.height = this.tileSizeY + 'px';
		return img;
	},

	addViewerMovedListener : function(listener) {
		this.viewerMovedListeners.push(listener);
	},

	addViewerZoomedListener : function(listener) {
		this.viewerZoomedListeners.push(listener);
	},

	/**
	 * Notify listeners of a zoom event on the viewer.
	 */
	notifyViewerZoomed : function() {
		var percentage = (100/(this.maxZoomLevel + 1)) * (this.zoomLevel + 1);
		for (var i = 0; i < this.viewerZoomedListeners.length; i++) {
			this.viewerZoomedListeners[i].viewerZoomed(
				new PanoJS.ZoomEvent(this.x, this.y, this.zoomLevel, percentage)
			);
		}
	},

	/**
	 * Notify listeners of a move event on the viewer.
	 */
	notifyViewerMoved : function(coords) {
		if (typeof coords == 'undefined') {
			coords = { 'x' : 0, 'y' : 0 };
		}

		for (var i = 0; i < this.viewerMovedListeners.length; i++) {
			this.viewerMovedListeners[i].viewerMoved(
				new PanoJS.MoveEvent(
					this.x + (coords.x - this.mark.x),
					this.y + (coords.y - this.mark.y)
				)
			);
		}
	},

	zoom : function(direction) {
		// ensure we are not zooming out of range
		if (this.zoomLevel + direction < 0) {
			if (PanoJS.MSG_BEYOND_MIN_ZOOM) {
				alert(PanoJS.MSG_BEYOND_MIN_ZOOM);
			}
			return;
		}
		else if (this.zoomLevel + direction > this.maxZoomLevel) {
			if (PanoJS.MSG_BEYOND_MAX_ZOOM) {
				alert(PanoJS.MSG_BEYOND_MAX_ZOOM);
			}
			return;
		}

		this.blank();

		var coords = { 'x' : Math.floor(this.width / 2), 'y' : Math.floor(this.height / 2) };

		var before = {
			'x' : (coords.x - this.x),
			'y' : (coords.y - this.y)
		};

		var after = {
			'x' : Math.floor(before.x * Math.pow(2, direction)),
			'y' : Math.floor(before.y * Math.pow(2, direction))
		};

		this.x = coords.x - after.x;
		this.y = coords.y - after.y;
		this.zoomLevel += direction;
		this.positionTiles();

		this.notifyViewerZoomed();
	},

	/** 
	 * Clear all the tiles from the well for a complete reinitialization of the
	 * viewer. At this point the viewer is not considered to be initialized.
	 */
	clear : function() {
		this.blank();
		this.initialized = false;
		this.tiles = [];
	},

	/**
	 * Remove all tiles from the well, which effectively "hides"
	 * them for a repaint.
	 */
	blank : function() {
		for (imgId in this.cache) {
			var img = this.cache[imgId];
			img.onload = function() {};
			if (img.image) {
				img.image.onload = function() {};
			}

			if (img.parentNode != null) {
				this.well.removeChild(img);
			}
		}
	},

	/**
	 * Method specifically for handling a mouse move event.  A direct
	 * movement of the viewer can be achieved by calling positionTiles() directly.
	 */
	moveViewer : function(coords) {
		this.positionTiles({ 'x' : (coords.x - this.mark.x), 'y' : (coords.y - this.mark.y) });
		this.notifyViewerMoved(coords);
	},

	/**
	 * Make the specified coords the new center of the image placement.
	 * This method is typically triggered as the result of a double-click
	 * event.  The calculation considers the distance between the center
	 * of the viewable area and the specified (viewer-relative) coordinates.
	 * If absolute is specified, treat the point as relative to the entire
	 * image, rather than only the viewable portion.
	 */
	recenter : function(coords, absolute) {
		if (absolute) {
			coords.x += this.x;
			coords.y += this.y;
		}

		var motion = {
			'x' : Math.floor((this.width / 2) - coords.x),
			'y' : Math.floor((this.height / 2) - coords.y)
		};

		if (motion.x == 0 && motion.y == 0) {
			return;
		}

		if (PanoJS.USE_SLIDE) {
			var target = motion;
			var x, y;
			// handle special case of vertical movement
			if (target.x == 0) {
				x = 0;
				y = this.slideAcceleration;
			}
			else {
				var slope = Math.abs(target.y / target.x);
				x = Math.round(Math.pow(Math.pow(this.slideAcceleration, 2) / (1 + Math.pow(slope, 2)), .5));
				y = Math.round(slope * x);
			}
			
			motion = {
				'x' : Math.min(x, Math.abs(target.x)) * (target.x < 0 ? -1 : 1),
				'y' : Math.min(y, Math.abs(target.y)) * (target.y < 0 ? -1 : 1)
			}
		}

		this.positionTiles(motion, true);
		this.notifyViewerMoved();

		if (!PanoJS.USE_SLIDE) {
			return;
		}

		var newcoords = {
			'x' : coords.x + motion.x,
			'y' : coords.y + motion.y
		};

		var self = this;
		// TODO: use an exponential growth rather than linear (should also depend on how far we are going)
		// FIXME: this could be optimized by calling positionTiles directly perhaps
		this.slideAcceleration += PanoJS.SLIDE_ACCELERATION_FACTOR;
		this.slideMonitor = setTimeout(function() { self.recenter(newcoords); }, PanoJS.SLIDE_DELAY );
	},

	resize : function() {
	    // IE fires a premature resize event
		if (!this.initialized) {
			return;
		}

        var newWidth = this.viewer.offsetWidth;
        var newHeight = this.viewer.offsetHeight;
        this.viewer.style.display = 'none';
		this.clear();

		var before = {
			'x' : Math.floor(this.width / 2),
			'y' : Math.floor(this.height / 2)
		};

		if (this.border >= 0) {
			this.fitToWindow(this.border);
		}
		else {
            this.width = newWidth;
            this.height = newHeight;
        }

		this.prepareTiles();

		var after = {
			'x' : Math.floor(this.width / 2),
			'y' : Math.floor(this.height / 2)
		};

		if (this.border >= 0) {
			this.x += (after.x - before.x);
			this.y += (after.y - before.y);
		}
		this.positionTiles();
		this.viewer.style.display = '';
		this.initialized = true;
		this.notifyViewerMoved();
	},

	/**
	 * Resolve the coordinates from this mouse event by subtracting the
	 * offset of the viewer in the browser window (or frame).  This does
	 * take into account the scroll offset of the page.
	 */
	resolveCoordinates : function(e) {
		return {
			'x' : (e.pageX || (e.clientX + (document.documentElement.scrollLeft || document.body.scrollLeft))) - this.left,
			'y' : (e.pageY || (e.clientY + (document.documentElement.scrollTop || document.body.scrollTop))) - this.top
		}
	},

	press : function(coords) {
		this.activate(true);
		this.mark = coords;
	},

	release : function(coords) {
		this.activate(false);
		var motion = {
			'x' : (coords.x - this.mark.x),
			'y' : (coords.y - this.mark.y)
		};

		this.x += motion.x;
		this.y += motion.y;
		this.mark = { 'x' : 0, 'y' : 0 };
	},

	/**
	 * Activate the viewer into motion depending on whether the mouse is pressed or
	 * not pressed.  This method localizes the changes that must be made to the
	 * layers.
	 */
	activate : function(pressed) {
		this.pressed = pressed;
		this.surface.style.cursor = (pressed ? PanoJS.GRABBING_MOUSE_CURSOR : PanoJS.GRAB_MOUSE_CURSOR);
		this.surface.onmousemove = (pressed ? PanoJS.mouseMovedHandler : function() {});
	},

	/**
	 * Check whether the specified point exceeds the boundaries of
	 * the viewer's primary image.
	 */
	pointExceedsBoundaries : function(coords) {
		return (coords.x < this.x ||
			coords.y < this.y ||
			coords.x > (this.tileSizeX * Math.pow(2, this.zoomLevel) + this.x) ||
			coords.y > (this.tileSizeY * Math.pow(2, this.zoomLevel) + this.y));
	},

	// QUESTION: where is the best place for this method to be invoked?
	resetSlideMotion : function() {
		// QUESTION: should this be > 0 ?	
		if (this.slideMonitor != 0) {
			clearTimeout(this.slideMonitor);
			this.slideMonitor = 0;
		}

		this.slideAcceleration = 0;
	}
};

PanoJS.TileUrlProvider = function(baseUri, prefix, extension) {
	this.baseUri = baseUri;
	this.prefix = prefix;
	this.extension = extension;
}

PanoJS.TileUrlProvider.prototype = {
	assembleUrl: function(xIndex, yIndex, zoom) {
		/*return this.baseUri + '/' +
			this.prefix + zoom + '-' + xIndex + '-' + yIndex + '.' + this.extension +
			(PanoJS.REVISION_FLAG ? '?r=' + PanoJS.REVISION_FLAG : '');*/
		return this.baseUri+'&region='+zoom+','+xIndex+','+yIndex
	}
}

PanoJS.mousePressedHandler = function(e) {
	e = e ? e : window.event;
	// only grab on left-click
	if (e.button < 2) {
		var self = this.backingBean;
		var coords = self.resolveCoordinates(e);
		if (self.pointExceedsBoundaries(coords)) {
			e.cancelBubble = true;
		}
		else {
			self.press(coords);
		}
	}

	// NOTE: MANDATORY! must return false so event does not propagate to well!
	return false;
};

PanoJS.mouseReleasedHandler = function(e) {
	e = e ? e : window.event;
	var self = this.backingBean;
	if (self.pressed) {
		// OPTION: could decide to move viewer only on release, right here
		self.release(self.resolveCoordinates(e));
	}
};

PanoJS.mouseMovedHandler = function(e) {
	e = e ? e : window.event;
	var self = this.backingBean;
	self.moveCount++;
	if (self.moveCount % PanoJS.MOVE_THROTTLE == 0) {
		self.moveViewer(self.resolveCoordinates(e));
	}
};

PanoJS.zoomInHandler = function(e) {
	e = e ? e : window.event;
	var self = this.parentNode.parentNode.backingBean;
	self.zoom(1);
	return false;
};

PanoJS.zoomOutHandler = function(e) {
	e = e ? e : window.event;
	var self = this.parentNode.parentNode.backingBean;
	self.zoom(-1);
	return false;
};

PanoJS.doubleClickHandler = function(e) {
	e = e ? e : window.event;
	var self = this.backingBean;
	coords = self.resolveCoordinates(e);
	if (!self.pointExceedsBoundaries(coords)) {
		self.resetSlideMotion();
		self.recenter(coords);
	}
};

PanoJS.keyboardMoveHandler = function(e) {
	e = e ? e : window.event;
	for (var i = 0; i < PanoJS.VIEWERS.length; i++) {
		var viewer = PanoJS.VIEWERS[i];
		if (e.keyCode == 38)
				viewer.positionTiles({'x': 0,'y': -PanoJS.MOVE_THROTTLE}, true);
		if (e.keyCode == 39)
				viewer.positionTiles({'x': -PanoJS.MOVE_THROTTLE,'y': 0}, true);
		if (e.keyCode == 40)
				viewer.positionTiles({'x': 0,'y': PanoJS.MOVE_THROTTLE}, true);
		if (e.keyCode == 37)
				viewer.positionTiles({'x': PanoJS.MOVE_THROTTLE,'y': 0}, true);
	}
}

PanoJS.keyboardZoomHandler = function(e) {
	e = e ? e : window.event;
	for (var i = 0; i < PanoJS.VIEWERS.length; i++) {
		var viewer = PanoJS.VIEWERS[i];
		if (e.keyCode == 109)
				viewer.zoom(-1);
		if (e.keyCode == 107)
				viewer.zoom(1);
	}
}

PanoJS.MoveEvent = function(x, y) {
	this.x = x;
	this.y = y;
};

PanoJS.ZoomEvent = function(x, y, level, percentage) {
	this.x = x;
	this.y = y;
	this.percentage = percentage;
	this.level = level;
};
