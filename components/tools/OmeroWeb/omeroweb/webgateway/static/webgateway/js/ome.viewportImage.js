/**
 * jquery-plugin-viewportImage - Viewport Image plugin for JQuery.
 *
 * Depends on jquery
 *
 * Copyright (c) 2007-2014 Glencoe Software, Inc. All rights reserved.
 *
 * This software is distributed under the terms described by the LICENCE file
 * you can find at the root of the distribution bundle, which states you are
 * free to use it only for non commercial purposes.
 * If the file is missing please request a copy by contacting
 * jason@glencoesoftware.com.
 *
 * Author: Carlos Neves <carlos(at)glencoesoftware.com>
 */
 
 /*global InfoControl:true BisqueISPyramid:true PanoJS:true ROIControl:true */

jQuery.fn.viewportImage = function(options) {
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
    var zoomCenter = null;
    
    // big images
    var viewerBean = null;
    
    var panbars = options == null || options.panbars;
    var mediaroot = options == null ? null : options.mediaroot;
    mediaroot = mediaroot || '/appmedia';
    

    wrapdiv.prepend('<span class="wb_zoomIn" style="top: 10px;"><img src="'+mediaroot+'/3rdparty/panojs-2.0.0/images/32px_plus.png" title="Zoom in" style="width: 20px;"></span>');
    wrapdiv.prepend('<span class="wb_zoom11" style="top: 40px;"><img src="'+mediaroot+'/3rdparty/panojs-2.0.0/images/32px_11.png" title="Zoom 1:1" style="width: 20px;"></span>');
    wrapdiv.prepend('<span class="wb_zoomOut" style="top: 70px;"><img src="'+mediaroot+'/3rdparty/panojs-2.0.0/images/32px_minus.png" title="Zoom out" style="width: 20px;"></span>');

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

    var panside_extend = function (e, min, max, mintm, maxtm, s) {
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
    };

    var panside_collapse = function (e, min, max, mintm, maxtm, s) {
      var opt = {};
      var opt2 = {};
      opt[s] = opt2[s] = min;
      e.unbind("mouseout", e.do_collapse)
      .mouseover(e.do_extend)
      .animate(opt);
      opt2[s == 'width' && 'top' || 'left'] = e._img_pos_collapsed;
      e.children("img").animate(opt2);
    };

    var panside_prepare = function (e,x,y) {
      var img = e.children("img");
      var iw = img.width();
      var ih = img.height();
      var max, min, maxtm, mintm, side;
      wrapwidth = wrapdiv.width();
      wrapheight = wrapdiv.height();
      img.unbind('load');

      if (iw === 0 || iw === 0) {
        img.load(function() {panside_prepare(e,x,y);});
        e.center = function () {};
        e.display = function () {};
        return;
      }

      if (x === 0) {
        side = 'height';
        max = ih;
        //img.css('top', '50%');
        e.center = function () {
          e._img_pos_extended = parseInt((wrapwidth / 2) - (iw / 2), 10);
          e._img_pos_collapsed = parseInt((wrapwidth / 2) - (iw / 6), 10);
          img.css('left', e._img_pos_collapsed);
        };
      } else {
        side = 'width';
        max = iw;
        img.css('left', '0%');
        e.center = function () {
          e._img_pos_extended = parseInt((wrapheight / 2) - (ih / 2), 10);
          e._img_pos_collapsed = parseInt((wrapheight / 2) - (ih / 6), 10);
          img.css('top', e._img_pos_collapsed);
        };
      }
      e.center();
      img.css({display: 'block', position: 'absolute'});
      min = parseInt(max / 3, 10);
      maxtm = -parseInt(ih / 2, 10);
      mintm = parseInt(maxtm / 5, 10);
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
    };
    
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

    //var centering  = function (x, y) {
    //  if (x == null && y == null) {
        /* Return current center */
        
   //   } else {
        /* move to center */
   //   }
   // };


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
    };

    this.hideOverlay = function () {
      overlay.hide();
    };

    this.overlayVisible = function () {
      return overlay.is(':visible') || overlay.is('.loading');
    };

    this.setPixelated = function (pixelated) {
      if (pixelated) {
        image.addClass("pixelated");
      } else {
        image.removeClass("pixelated");
      }
    };

    /**
     * Pan the image within the viewport
     */

    this.doMove = function (deltax, deltay, smooth, auto_move_cb) {
      /* Image and wrapping div */
      var pos = dragdiv.offset();
      var rel = wrapdiv.offset();
      pos.left -= rel.left + parseInt($.css(wrapdiv[0], "borderLeftWidth", true), 10);
      pos.top -= rel.top + parseInt($.css(wrapdiv[0], "borderTopWidth", true), 10);
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
    };

    var cur_zoom = 100;
    var orig_width;
    var orig_height;
    var changing = null;

    this.getOrigWidth = function () { return orig_width; };
    this.getOrigHeight = function () { return orig_height; };

    this.getXOffset = function () {
      var offset = parseInt(dragdiv.css('left'), 10);
      return offset < 0 ? (-offset) : 0;
    };
    this.setXOffset = function (xoffset) {dragdiv.css('left', -xoffset); this.doMove(0,0);};
    this.getYOffset = function () {
      var offset = parseInt(dragdiv.css('top'), 10);
      return offset < 0 ? (-offset) : 0;
    };
    this.setYOffset = function (yoffset) {dragdiv.css('top', -yoffset); this.doMove(0,0);};

    this.getZoom = function () {
        return cur_zoom;
    };


    // setZoomCenter sets zoomCenter with a short timeout to reset to null
    var clearCenterTimeout;
    var setZoomCenter = function(center) {
      zoomCenter = zoomCenter || center;
      if (clearCenterTimeout) {
        clearTimeout(clearCenterTimeout);
      }
      clearCenterTimeout = setTimeout(function(){
        zoomCenter = null;
      }, 100);
    };
    var getZoomCenter = function() {
      return zoomCenter;
    };

    this.setZoom = function (val, width, height, center) {
      if (width != null && height != null) {
        orig_width = width;
        orig_height = height;
      }
      width = parseInt(orig_width*val/100, 10);
      height = parseInt(orig_height*val/100, 10);

      var left = parseInt(dragdiv.css('left'), 10),
          top = parseInt(dragdiv.css('top'), 10);

      // center is point in viewport - image should zoom on this spot
      if (!center) {
        center = {'x': wrapdiv.width()/2,   // pick middle by default
            'y': wrapdiv.height()/2};
      }
      // find point on image that must stay at viewport centre (as a fraction of w or h)
      var imgx = (center.x - left) / image.attr('width');
      var imgy = (center.y - top) / image.attr('height');

      // cache the viewport centre and image centre to re-use for a whole series of zoom events
      // only sets zoomCenter if expired (sets at start of new zoom)
      setZoomCenter({'vp':center, 'image':{'x':imgx, 'y':imgy}});

      var zc = getZoomCenter();
      if (zc) {
        imgx = zc.image.x;
        imgy = zc.image.y;
        var cx = zc.vp.x;
        var cy = zc.vp.y;
        // after the image is resized to width & height, need to re-calculate left & top
        var newleft = -parseInt((imgx * width) - cx, 10);
        var newtop = -parseInt((imgy * height) - cy, 10);
        dragdiv.css({'top': newtop+'px', 'left': newleft+'px'});
      }

      cur_zoom = val;
      imagewidth = width;
      imageheight = height;
      this.doMove(0, 0);
      if (!changing) {
          changing = setTimeout(function () {
          image.trigger("zoom", [cur_zoom]);
          changing = null;
        }, 20);
      }
      image.trigger("instant_zoom", [cur_zoom]);
      dragdiv.width(width);
      dragdiv.height(height);
      image.attr({width: width, height: height});
      overlay.attr({width: width, height: height});
     };

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
      this.setZoom(parseInt(ztf, 10));
    };

    this.doZoom = function (increment, justDirection, center) {
      if (justDirection) {
        var t = Math.max(1,((imagewidth+3)*cur_zoom/imagewidth) - cur_zoom);
        increment = cur_zoom + (increment>0?t:-t);
      }
      this.setZoom(parseInt(increment, 10), null, null, center);
    };

    dragdiv.bind('mousewheel', function (e, delta) {
      // calculate zoom point within viewport
       var o = wrapdiv.offset(),
          relX = e.pageX - o.left,
          relY = e.pageY - o.top,
          cxcy = {'x':relX,
              'y':relY};
      _this.doZoom(delta, true, cxcy);
      e.preventDefault();
    });

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
      clickinterval = setTimeout(function () {clearTimeout(clickinterval); clickinterval = null;}, 250);
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

    // If we're on a tablet, use hammer.js for gestures
    if (OME.isMobileDevice()) {
      var myElement = dragdiv.get(0);
      var mc = new Hammer(myElement);
      mc.get('pan').set({ direction: Hammer.DIRECTION_ALL });
      // "panstart" is not fired until after other pan events...
      mc.on("panleft panright panup pandown", function(event) {
          if (drag_px === undefined) {
            self.doMove(event.deltaX, event.deltaY);
          } else {
            self.doMove(event.center.x-drag_px, event.center.y-drag_py);
          }
          drag_px = event.center.x;
          drag_py = event.center.y;
      });
      // ...so we use "panend" instead to distinguish start/end
      mc.on("panend", function(event) {
        drag_px = undefined;
      });
    }


    // Handle zoom buttons
    $(".wb_zoomIn").click(function() {
      var zm = _this.getZoom();
      _this.setZoom(zm + 20);
    });
    $(".wb_zoomOut").click(function() {
      var zm = _this.getZoom();
      if (zm > 21) {
        _this.setZoom(zm - 20);
      } else if (zm > 11) {
        _this.setZoom(zm - 10);
      }
    });
    $(".wb_zoom11").click(function() {
      _this.setZoom(100);
    });

    this.getBigImageContainer = function () {
        return viewerBean;
    };
    
    this.setUpTiles = function (imagewidth, imageheight, xtilesize, ytilesize, init_zoom, levels, hrefProvider, thref, init_cx, init_cy, zoomLevelScaling, nominalMagnification) {
        InfoControl.prototype.viewerZoomed = function(e) {
            if (this.dom_info) {
                if (nominalMagnification && typeof nominalMagnification != "undefined") {
                    var scale = e.scale * nominalMagnification;
                    if (scale % 1 !== 0)
                    //smart float formatting
                        if (scale < 1)
                            scale = Math.round(scale*10)/10;
                        else
                            scale = Math.round(scale*100)/100;
                    this.dom_info.innerHTML = 'Magnification: '+ scale + 'x';
                } else {
                    var scale = e.scale * 100;
                    if (scale % 1 !== 0)
                        scale = scale.toFixed(2);
                    this.dom_info.innerHTML = 'Scale: '+ scale +'%';
                }
            }
        };
        
        var myPyramid = new BisqueISPyramid(
                imagewidth, imageheight, xtilesize, ytilesize, levels);
        var myProvider = new PanoJS.TileUrlProvider('','','');
        myProvider.assembleUrl = function(xIndex, yIndex, zoom) {
            var href = hrefProvider();
            return href+'&'+myPyramid.tile_filename( zoom, xIndex, yIndex );
            //return MY_URL + '/' + MY_PREFIX + myPyramid.tile_filename( zoom, xIndex, yIndex );
        };
        myProvider.thumbnailUrl = function (thref) {
            this.thumbnailUrl = thref;
        };
        myProvider.thumbnailUrl(thref);
        
        if (viewerBean == null) {
            var viewerBeanId = this.id + '-tiles';
            $('<div id="'+viewerBeanId+'" class="viewer" style="width: 100%; height: 100%;" ></div>').appendTo(wrapdiv);
            
            PanoJS.CREATE_CONTROL_MAXIMIZE = true;
            PanoJS.PRE_CACHE_AMOUNT = 2;
            PanoJS.USE_WHEEL_FOR_ZOOM = true;
            viewerBean = new PanoJS(viewerBeanId, {
                tileUrlProvider : myProvider,
                xTileSize       : myPyramid.xtilesize,
                yTileSize       : myPyramid.ytilesize,
                maxZoom         : myPyramid.getMaxLevel(),
                imageWidth      : myPyramid.width,
                imageHeight     : myPyramid.height,
                initialZoom     : init_zoom,
                staticBaseURL   : mediaroot+'3rdparty/panojs-2.0.0/images/',
                blankTile       : mediaroot+'3rdparty/panojs-2.0.0/images/blank.gif',
                loadingTile     : mediaroot+'3rdparty/panojs-2.0.0/images/blank.gif',
                zoomLevelScaling : zoomLevelScaling,
                delay           : 300
            });
            
            viewerBean.mouseReleasedHandler = function(e) {
                e = e ? e : window.event;
                if (!this.pressed) {
                    return false;
                }
                var coords = this.resolveCoordinates(e);
                if (e.type=='mouseout' &&
                    coords.x > 0 && coords.x < this.width &&
                    coords.y > 0 && coords.y < this.height) {
                    // on IE the mouseout event is triggered for every tile boundary,
                    // so make sure we have really crossed the viewport boudary
                    return false;
                }
                var motion = {
                    'x' : (coords.x - this.mark.x),
                    'y' : (coords.y - this.mark.y)
                };
                var moved = this.mouse_have_moved;
                this.release(coords);

                if (!moved) {
                    return false;
                }

                // only if there was little movement
                if (moved || motion.x>5 || motion.y>5) {
                    return false;
                }

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
            };
            
            // thumbnail url overwritten
            // bird-eye view cannot rely on levels in order to load thumbnail,
            // because of the way pyramid is generated.
            viewerBean.thumbnailURL = function() {
                return this.tileUrlProvider.thumbnailUrl;
            };
            
            viewerBean.update_url = function() {
                if (this.thumbnail_control) {
                    this.thumbnail_control.dom_image.src = this.thumbnailURL();
                }
                this.update();
            };
            
            PanoJS.MSG_BEYOND_MIN_ZOOM = null;
            PanoJS.MSG_BEYOND_MAX_ZOOM = null;
            viewerBean.init();
            if ((typeof init_cx != 'undefined') && (typeof init_cy != 'undefined')) {
                var scale = viewerBean.currentScale();
                viewerBean.recenter({
                    'x':parseInt(init_cx, 10)*scale,
                    'y':parseInt(init_cy, 10)*scale}, true, true);
                // Seems that if we're on the edge of image, blank tiles are not cleared...
                setTimeout(function() {
                  viewerBean.positionTiles();
                }, 5000);   // clear AFTER they have loaded (not ideal!)
            }
            if (viewerBean.thumbnail_control) {
                viewerBean.thumbnail_control.update();
            }
            if (!viewerBean.roi_control) {
                viewerBean.roi_control = new ROIControl(viewerBean);
            }
            if (!viewerBean.scalebar_control) {
                viewerBean.scalebar_control = new ScaleBarControl(viewerBean);
            }
            
            // not supported elements
            jQuery('#wblitz-zoom').parent().hide();
            jQuery('#wblitz-lp-enable').parent().hide();
            jQuery('.multiselect').hide();
            jQuery('#wblitz-invaxis').attr('disable', true);
        } else {
            viewerBean.tileUrlProvider = myProvider;
            viewerBean.update_url();
        }
        cur_zoom = viewerBean.currentScale() * 100;
    };

    // Simply causes all tiles to refresh their src
    this.refreshTiles = function () {
        if (viewerBean) {
            viewerBean.positionTiles();
        }
    };
    
    this.destroyTiles = function () {
        jQuery(viewerBeanId).remove();
        viewerBean = null;
    };

    this.refresh = function () {
        
      imagewidth = image.width();
      imageheight = image.height();
      wrapwidth = wrapdiv.width();
      wrapheight = wrapdiv.height();
      //orig_width = image.get(0).clientWidth;
      //orig_height = image.get(0).clientHeight;
      
      if (viewerBean != null) {
          viewerBean.resize();
      }
      
      if (panbars) {
      pantop.center();
      panbottom.center();
      panleft.center();
      panright.center();
      }
      self.doMove(0, 0);
    };

    //jQuery(window).resize(this.refresh);
  });
};

