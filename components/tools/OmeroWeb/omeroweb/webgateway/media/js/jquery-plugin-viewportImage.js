/**
 * jquery-plugin-viewportImage - Viewport Image plugin for JQuery.
 *
 * Depends on jquery
 *
 * Copyright (c) 2007, 2008, 2009 Glencoe Software, Inc. All rights reserved.
 * 
 * This software is distributed under the terms described by the LICENCE file
 * you can find at the root of the distribution bundle, which states you are
 * free to use it only for non commercial purposes.
 * If the file is missing please request a copy by contacting
 * jason@glencoesoftware.com.
 *
 * Author: Carlos Neves <carlos(at)glencoesoftware.com>
 */

$.fn.viewportImage = function(options) {
  return this.each(function(){
    if (!this.id) {
      this.id = '' + (new Date()).getTime();
    }

    var self = this;
    var _this = this;
    var insideId = this.id + '-vpi';

    var image = jQuery(this);
    image.addClass('weblitz-viewport-img');
    image.wrap('<div id="'+insideId+'" style="display: inline; position: absolute;" class="draggable"></div>');
    var dragdiv = jQuery('#'+insideId);
    var dragdiv_dom = dragdiv.get(0);
    var wrapdiv = jQuery(dragdiv_dom.parentNode);
    var overlay = $('<img id="'+insideId+'-ovl">').appendTo(dragdiv);
    overlay.addClass('weblitz-viewport-img').hide();
    
    // big images
    var viewerBean = null;
    
    var panbars = options == null || options.panbars;
    var mediaroot = options == null ? null : options.mediaroot;
    mediaroot = mediaroot || '/appmedia';
    
    if (panbars) { 
    /* Panning sides */
    var panleftId = this.id + '-panl';
    var panrightId = this.id + '-panr';
    var pantopId = this.id + '-pant';
    var panbottomId = this.id + '-panb';
    var side_styles = 'background-color: #ABC; filter:alpha(opacity=50); opacity: 0.5; z-index: 5; position: absolute; display: block;';
    var side_styles_h = side_styles+'height: 100%;';
    var side_styles_v = side_styles+'width: 100%; left: 0%;';
    wrapdiv.prepend('<div id="'+panleftId+'" style="'+ side_styles_h +'left: 0%;"><img src="'+mediaroot+'/webgateway/img/arrow_left.gif">');
    wrapdiv.prepend('<div id="'+panrightId+'" style="'+ side_styles_h +'right: 0px; _right: 0%;"><img src="'+mediaroot+'/webgateway/img/arrow_right.gif">');
    wrapdiv.prepend('<div id="'+pantopId+'" style="'+ side_styles_v +'top: 0%;"><img src="'+mediaroot+'/webgateway/img/arrow_up.gif">');
    wrapdiv.prepend('<div id="'+panbottomId+'" style="'+ side_styles_v +'bottom: 0%;"><img src="'+mediaroot+'/webgateway/img/arrow_down.gif">');
    var panleft = jQuery('#'+panleftId);
    var panright = jQuery('#'+panrightId);
    var pantop = jQuery('#'+pantopId);
    var panbottom = jQuery('#'+panbottomId);

    function panside_extend (e, min, max, mintm, maxtm, s) {
      var opt = {};
      var opt2 = {};
      opt[s] = opt2[s] = max;
      e.queue("fx", []);
      e.children().queue("fx", []);
      e.unbind("mouseover", e.do_extend)
      .mouseout(e.do_collapse)
      .animate(opt);
      opt2[s == 'width' && 'top' || 'left'] = e._img_pos_extended;
      e.children("img").animate(opt2);
    }

    function panside_collapse (e, min, max, mintm, maxtm, s) {
      var opt = {};
      var opt2 = {};
      opt[s] = opt2[s] = min;
      e.unbind("mouseout", e.do_collapse)
      .mouseover(e.do_extend)
      .animate(opt);
      opt2[s == 'width' && 'top' || 'left'] = e._img_pos_collapsed;
      e.children("img").animate(opt2);
    }

    function panside_prepare (e,x,y) {
      var img = e.children("img");
      var iw = img.width();
      var ih = img.height();
      var max, min, maxtm, mintm, side;
      wrapwidth = wrapdiv.width();
      wrapheight = wrapdiv.height();
      img.unbind('load');

      if (iw == 0 || iw == 0) {
        img.load(function() {panside_prepare(e,x,y);});
        e.center = function () {};
        e.display = function () {};
        return;
      }

      if (x == 0) {
        side = 'height';
        max = ih;
        //img.css('top', '50%');
        e.center = function () {
          e._img_pos_extended = parseInt((wrapwidth / 2) - (iw / 2));
          e._img_pos_collapsed = parseInt((wrapwidth / 2) - (iw / 6));
          img.css('left', e._img_pos_collapsed);
        }
      } else {
        side = 'width';
        max = iw;
        img.css('left', '0%');
        e.center = function () {
          e._img_pos_extended = parseInt((wrapheight / 2) - (ih / 2));
          e._img_pos_collapsed = parseInt((wrapheight / 2) - (ih / 6));
          img.css('top', e._img_pos_collapsed);
        }
      }
      e.center();
      img.css({display: 'block', position: 'absolute'});
      min = parseInt(max / 3);
      maxtm = -parseInt(ih / 2);
      mintm = parseInt(maxtm / 5);
      e.do_extend = function () {panside_extend(e,min,max,mintm,maxtm,side);};
      e.do_collapse = function () {panside_collapse(e,min,max,mintm,maxtm,side);};
      e.do_collapse();
      //panside_collapse(e, min, max, mintm + 'px', maxtm+ 'px', side);
      e.mouseout(function() { e.removeClass('auto-val'); });
      e.mousedown(function () { e.addClass('auto-val'); self.doMove(x,y,1, function() {return e.is('.auto-val');}); return false;});
      e.mouseup(function() { e.removeClass('auto-val'); return false;});
      e.css({'cursor': 'pointer'});

      e.display = function (show) {
        if (show && this.css('display') == 'none') {
          panside_collapse(this, min, max, mintm, maxtm, side);
          this.show();
        }
        if (!show && this.css('display') != 'none') {
          this.removeClass('auto-val')
          .unbind("mouseout")
          .hide();
        }
      };
    }
    
    panside_prepare(panleft,50,0);
    panside_prepare(panright,-50,0);
    panside_prepare(pantop,0,50);
    panside_prepare(panbottom,0,-50);
    }

    /* These get recalculated on zoom and refresh() */
    var imagewidth = 0;
    var imageheight = 0;
    var wrapwidth = 0;
    var wrapheight = 0;

    var centering  = function (x, y) {
      if (x == null && y == null) {
        /* Return current center */
        
      } else {
        /* move to center */
      }
    };


    /**
     * Overlay control
     */

    this.showOverlay = function (url, cb, error_cb) {
      if (url) {
        overlay.addClass('loading').hide();
        var load = function () {overlay.unbind('error',error); overlay.removeClass('loading').show(); cb && cb();};
        var error = function () {overlay.unbind('load',load); overlay.removeClass('loading'); error_cb && error_cb();};
        overlay.one('load', load);
        overlay.one('error', error);
        overlay.attr('src', url);
      } else {
        overlay.show();
        cb && cb();
      }
    }

    this.hideOverlay = function () {
      overlay.hide();
    }

    this.overlayVisible = function () {
      return overlay.is(':visible') || overlay.is('.loading');
    }

    /**
     * Pan the image within the viewport
     */

    this.doMove = function (deltax, deltay, smooth, auto_move_cb) {
      /* Image and wrapping div */
      var pos = dragdiv.offset();
      var rel = wrapdiv.offset();
      pos.left -= rel.left + parseInt(jQuery.curCSS(wrapdiv[0], "borderLeftWidth", true));
      pos.top -= rel.top + parseInt(jQuery.curCSS(wrapdiv[0], "borderTopWidth", true));
      var left = pos.left + deltax;
      var top = pos.top + deltay;
      var self = this;

      /* Is the viewport bigger than the image ? */
      if (imagewidth <= wrapwidth) {
        /* Viewport wider than image, center horizontally */
        left = (wrapwidth - imagewidth) / 2;
        panbars && panleft.display(false);
        panbars && panright.display(false);
      } else {
        /* Image wider than viewport... */
        if (left >= 0) {
          left = 0;
          panbars && panleft.display(false);
        } else {
          panbars && panleft.display(true);
        }
        if ((wrapwidth - imagewidth) >= left ) {
          left = wrapwidth - imagewidth;
          panbars && panright.display(false);
        } else {
          panbars && panright.display(true);
        }
      }
      if (imageheight <= wrapheight) {
        /* Viewport higher than image, center vertically */
        top = (wrapheight - imageheight) / 2;
        panbars && pantop.display(false);
        panbars && panbottom.display(false);
      } else {
        /* Image higher than viewport... */
        if (top >= 0) {
          top = 0;
          panbars && pantop.display(false);
        } else {
          panbars && pantop.display(true);
        }
        if ((wrapheight - imageheight) >= top ) {
          top = wrapheight - imageheight;
          panbars && panbottom.display(false);
        } else {
          panbars && panbottom.display(true);
        }
      }
      if (left == dragdiv_dom.offsetLeft && top == dragdiv_dom.offsetTop) {
        return;
      }

      if (smooth != null) {
        dragdiv.animate({left: left, top: top}, 'fast', 'linear', function () {
          if (auto_move_cb != null && auto_move_cb()) {
            self.doMove(deltax, deltay, smooth, auto_move_cb);
          }
        });
      } else {
        dragdiv.css({left: left, top: top});
      }
    }

    var cur_zoom = 100;
    var orig_width;
    var orig_height;
    var changing = null;

    this.getOrigWidth = function () { return orig_width; };
    this.getOrigHeight = function () { return orig_height; };

    this.getXOffset = function () {
      offset = parseInt(dragdiv.css('left'));
      return offset < 0 ? (-offset) : 0;
    };
    this.setXOffset = function (xoffset) {dragdiv.css('left', -xoffset); this.doMove(0,0);};
    this.getYOffset = function () {
      offset = parseInt(dragdiv.css('top'));
      return offset < 0 ? (-offset) : 0;
    };
    this.setYOffset = function (yoffset) {dragdiv.css('top', -yoffset); this.doMove(0,0);};

    this.getZoom = function () {
        return cur_zoom;
    };
    this.setZoom = function (val, width, height) {
      if (width != null && height != null) {
        orig_width = width;
        orig_height = height;
      }
      width = parseInt(orig_width*val/100);
      height = parseInt(orig_height*val/100);
      cur_zoom = val;
      imagewidth = width;
      imageheight = height;
      this.doMove(0, 0);
      if (!changing) {
	changing = setTimeout(function () {;
          image.trigger("zoom", [cur_zoom]);
          changing = null;
			      }, 20);
      }
      image.trigger("instant_zoom", [cur_zoom])
      image.attr({width: width, height: height});
      overlay.attr({width: width, height: height});
            
     }     

    this.setZoomToFit = function (only_shrink, width, height) {
      if (width != null && height != null) {
        orig_width = width;
        orig_height = height;
        cur_zoom = 100;
      }
      var ztf = Math.min(wrapwidth * 100.0 / orig_width, wrapheight * 100.0 / orig_height);
      if (only_shrink && ztf >= 100.0) {
        ztf = 100.0;
      }
      this.setZoom(parseInt(ztf));
    }

    this.doZoom = function (increment, justDirection) {
      if (justDirection) {
        var t = Math.max(1,((imagewidth+3)*cur_zoom/imagewidth) - cur_zoom);
        increment = cur_zoom + (increment>0?t:-t);
      }
      this.setZoom(parseInt(increment));
    }

    /**
     * Handle Zoom by mousewheel (IE)
     */

    dragdiv.bind("mousewheel", function(e){
      // Respond to mouse wheel in IE. (It returns up/dn motion in multiples of 120)
      if (e.wheelDelta >= 120)
        _this.doZoom(1, true);
      else if (e.wheelDelta <= -120)
        _this.doZoom(-1, true);
      
      e.preventDefault();
    })
  
    /**
     * Handle Zoom by mousewheel (FF)
     */

    if (dragdiv.get(0).addEventListener) {
      // Respond to mouse wheel in Firefox
      dragdiv.get(0).addEventListener('DOMMouseScroll', function(e) {
        if (e.detail > 0)
          _this.doZoom(-1, true);
        else if (e.detail < 0)
          _this.doZoom(1, true);
        
        e.preventDefault();
      }, false);
    }

    /**
     * Handle panning by mouse drag
     */

    var ondrag = false;
    var clickinterval = null;
    var drag_px;
    var drag_py;
    dragdiv
      .click(function (e) {
          if (clickinterval != null) {
            clickinterval = null;
            image.trigger(e);
          }
        })
    .mousedown(function (e) {
      drag_px = e.screenX;
      drag_py = e.screenY;
      //jQuery(this).css('cursor', 'move');
      jQuery(this).addClass('ondrag');
      ondrag = true;
      if (clickinterval != null) {
        clearInterval(clickinterval);
      }
      clickinterval = setTimeout(function () {clearTimeout(clickinterval); clickinterval = null;}, 250)
      return false;
    })
    .mouseup(function (e) {
      if (ondrag) {
        ondrag = false;
        jQuery(this).removeClass('ondrag');
        return false;
        //jQuery(this).css('cursor', 'default');
      }
    })
    .mouseout(function (e) {
      if (ondrag) {
        ondrag = false;
        jQuery(this).removeClass('ondrag');
        return false;
        //jQuery(this).css('cursor', 'default');
      }
    })
    .mousemove(function (e) {
      if (ondrag) {
        self.doMove(e.screenX-drag_px, e.screenY-drag_py);
        drag_px = e.screenX;
        drag_py = e.screenY;
        return false;
      }
    });

    this.getBigImageContainer = function () {
        return viewerBean;
    }
    
    this.setUpTiles = function (imagewidth, imageheight, xtilesize, ytilesize, init_zoom, levels, href, thref) {
        var myPyramid = new BisqueISPyramid( imagewidth, imageheight, xtilesize, ytilesize);
        var myProvider = new PanoJS.TileUrlProvider('','','');
        myProvider.assembleUrl = function(xIndex, yIndex, zoom) {
            return href+'&'+myPyramid.tile_filename( zoom, xIndex, yIndex )
            //return MY_URL + '/' + MY_PREFIX + myPyramid.tile_filename( zoom, xIndex, yIndex );
        }
        myProvider.thumbnailUrl = function (thref) {
            var d = new Date();
            this.thumbnailUrl = thref+'?_='+Math.ceil(parseInt(Math.floor(d.getTime()))); /* Pick random number */ 
        }
        myProvider.thumbnailUrl(thref);
        
        if (viewerBean == null) {
            $('<div id="weblitz-viewport-tiles" class="viewer" style="width: 100%; height: 100%;" ></div>').appendTo(wrapdiv);
            jQuery('#weblitz-viewport-tiles').css({width: wrapwidth, height: wrapheight});
            
            viewerBean = new PanoJS('weblitz-viewport-tiles', {
                tileUrlProvider : myProvider,
                xTileSize       : myPyramid.xtilesize,
                yTileSize       : myPyramid.ytilesize,
                maxZoom         : myPyramid.getMaxLevel(),
                imageWidth      : myPyramid.width,
                imageHeight     : myPyramid.height,
                initialZoom     : init_zoom,
                blankTile       : '/appmedia/webgateway/img/3rdparty/panojs/blank.gif',
                loadingTile     : '/appmedia/webgateway/img/3rdparty/panojs/blank.gif'//'progress.gif'
            });
            
            viewerBean.mouseReleasedHandler = function(e) {
                e = e ? e : window.event;
                if (!this.pressed) return false;
                var coords = this.resolveCoordinates(e);
                var motion = {
                    'x' : (coords.x - this.mark.x),
                    'y' : (coords.y - this.mark.y)
                };
                var moved = this.mouse_have_moved;
                this.release(coords);

                if (!moved) return false;


                // only if there was little movement
                if (moved || motion.x>5 || motion.y>5) return false;

                if (e.button == 2) {
                    this.blockPropagation(e);      
                    this.zoom(-1);    
                } else
                // move on one click
                if (e.button < 2) {
                    //if (!this.pointExceedsBoundaries(coords)) {
                        this.resetSlideMotion();
                        this.recenter(coords);
                    //} 
                }

                return false;
            }
            
            // thumbnail url overwritten
            // bird-eye view cannot relay on levels in order to load thumbail,
            // becuase of the way pyramid is generated.
            viewerBean.thumbnailURL = function() {
                return this.tileUrlProvider.thumbnailUrl;
            }
            
            viewerBean.update_url = function() {
                if (this.thumbnail_control) {
                    this.thumbnail_control.dom_image.src = this.thumbnailURL();
                }
                this.update();
            }
            
            PanoJS.MSG_BEYOND_MIN_ZOOM = null;
            PanoJS.MSG_BEYOND_MAX_ZOOM = null;
            viewerBean.init();
            if (viewerBean.thumbnail_control) viewerBean.thumbnail_control.update();
            if (!viewerBean.roi_control) viewerBean.roi_control = new ROIControl(viewerBean);
            
            // not supported elements
            jQuery('#wblitz-zoom').parent().hide();
            jQuery('#wblitz-lp-enable').parent().hide();
            jQuery('.multiselect').hide();
            jQuery('#wblitz-invaxis').attr('disable', true);
        } else {
            viewerBean.tileUrlProvider = myProvider;
            viewerBean.update_url();
        }
        cur_zoom = viewerBean.zoomLevel;
    }
    
    
    this.refresh = function () {
        
      imagewidth = image.width();
      imageheight = image.height();
      wrapwidth = wrapdiv.width();
      wrapheight = wrapdiv.height();
      //orig_width = image.get(0).clientWidth;
      //orig_height = image.get(0).clientHeight;
      
      if (viewerBean != null) {
          jQuery('#weblitz-viewport-tiles').css({width: wrapwidth, height: wrapheight});
          viewerBean.resize();
      }
      
      if (panbars) {
      pantop.center();
      panbottom.center();
      panleft.center();
      panright.center();
      }
      self.doMove(0, 0);
    }

    //jQuery(window).resize(this.refresh);
  });
}

