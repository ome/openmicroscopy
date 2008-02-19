/**
 * Viewport Image plugin for JQuery.
 *
 * Called on an image, this plugin wraps the image in a couple of divs to make it draggable.
 * Client side zooming, panning by mouse drag, panning controls on the sides and a few other things
 * are implemented.
 *
 * Todo: better documentation and example usage.
 *
 * Depends on jquery
 *
 * Author: C. Neves <carlos@glencoesoftware.com>
 *
 * Copyright (c) 2007, 2008 Glencoe Software, Inc. All rights reserved.
 * 
 * This software is distributed under the terms described by the LICENCE file
 * you can find at the root of the distribution bundle, which states you are
 * free to use it only for non commercial purposes.
 * If the file is missing please request a copy by contacting
 * jason@glencoesoftware.com.
 *
 */

$.fn.viewportImage = function() {
  return this.each(function(){
    if (!this.id) {
      this.id = '' + (new Date()).getTime();
    }

    var self = this;

	var insideId = this.id + '-vpi';
	var panleftId = this.id + '-panl';
	var panrightId = this.id + '-panr';
	var pantopId = this.id + '-pant';
	var panbottomId = this.id + '-panb';

    jQuery(this).wrap('<div id="'+insideId+'" style="display: inline; position: absolute;"></div>');
    var dragdiv = jQuery('#'+insideId);
    var dragdiv_dom = dragdiv.get(0);
    var wrapdiv = jQuery(dragdiv_dom.parentNode);
    var image = jQuery(this);

    /* Panning sides */
    var side_styles = 'background-color: #ABC; filter:alpha(opacity=50); opacity: 0.5; z-index: 5; position: absolute; display: block;';
    var side_styles_h = side_styles+'height: 100%; width: 3em;';
    var side_styles_v = side_styles+'height: 3em; width: 100%;';
    wrapdiv.prepend('<div id="'+panleftId+'" style="'+ side_styles_h +'left: 0%;"><img src="/media/img/blitzcon/arrow_left.gif">');
    wrapdiv.prepend('<div id="'+panrightId+'" style="'+ side_styles_h +'right: 0px; _right: 0%;"><img src="/media/img/blitzcon/arrow_right.gif">');
    wrapdiv.prepend('<div id="'+pantopId+'" style="'+ side_styles_v +'top: 0%;"><img src="/media/img/blitzcon/arrow_up.gif">');
    wrapdiv.prepend('<div id="'+panbottomId+'" style="'+ side_styles_v +'bottom: 0%;"><img src="/media/img/blitzcon/arrow_down.gif">');
    var panleft = jQuery('#'+panleftId);
    var panright = jQuery('#'+panrightId);
    var pantop = jQuery('#'+pantopId);
    var panbottom = jQuery('#'+panbottomId);
    
    function panside_extend (e, min, max, mintm, maxtm, s) {
      var opt = {};
      opt[s] = max;
      e.queue("fx", []);
      e.children().queue("fx", []);
      e.unbind("mouseover", e.do_extend)
      .mouseout(e.do_collapse)
      .animate(opt);
      opt['marginTop'] = maxtm;
      e.children("img").animate(opt);
    }

    function panside_collapse (e, min, max, mintm, maxtm, s) {
      var opt = {};
      opt[s] = min;
      e.unbind("mouseout", e.do_collapse)
      .mouseover(e.do_extend)
      .animate(opt);
      opt['marginTop'] = mintm;
      e.children("img").animate(opt);
    }

    function panside_prepare (e,x,y) {
      var img = e.children("img");
      var iw = img.width();
      var ih = img.height();
      var max, min, maxtm, mintm, side;

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
        img.css('top', '50%');
        e.center = function () {
          var t = parseInt((wrapwidth / 2) - (iw / 2));
          e.css({paddingLeft: t+'px', paddingRight:t+'px'});
        }
      } else {
        side = 'width';
        max = iw;
        //img.css('left', '50%');
        e.center = function () {
          var t = parseInt((wrapheight / 2) - (ih / 2));
          e.css({paddingTop: t+'px', paddingBottom:t+'px'});
        }
      }
      img.css({display: 'block', position: 'relative'});
      min = max / 3;
      maxtm = -(ih / 2);
      mintm = maxtm / 5;
      e.do_extend = function () {panside_extend(e,min,max,mintm,maxtm,side);};
      e.do_collapse = function () {panside_collapse(e,min,max,mintm,maxtm,side);};
      e.do_collapse();
      //panside_collapse(e, min, max, mintm + 'px', maxtm+ 'px', side);
      e.mouseout(function() { e.removeClass('auto-val'); });
      e.mousedown(function () { e.addClass('auto-val'); self.doMove(x,y,1, function() {return e.is('.auto-val');}) });
      e.mouseup(function() { e.removeClass('auto-val'); });
      e.css({'cursor': 'pointer'});

      e.display = function (show) {
        if (show && this.css('display') == 'none') {
          panside_collapse(this, min, max, mintm + 'px', maxtm+ 'px', side);
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
     * Pan the image within the viewport
     */

   this.doMove = function (deltax, deltay, smooth, auto_move_cb) {
      /* Image and wrapping div */
      var pos = dragdiv.offset({relativeTo: wrapdiv});
      var left = pos.left + deltax;
      var top = pos.top + deltay;
      var self = this;

      /* Is the viewport bigger than the image ? */
      if (imagewidth <= wrapwidth) {
        /* Viewport wider than image, center horizontally */
        left = (wrapwidth - imagewidth) / 2;
        panleft.display(false);
        panright.display(false);
      } else {
        /* Image wider than viewport... */
        if (left >= 0) {
          left = 0;
          panleft.display(false);
        } else {
          panleft.display(true);
        }
        if ((wrapwidth - imagewidth) >= left ) {
          left = wrapwidth - imagewidth;
          panright.display(false);
        } else {
          panright.display(true);
        }
      }
      if (imageheight <= wrapheight) {
        /* Viewport higher than image, center vertically */
        top = (wrapheight - imageheight) / 2;
        pantop.display(false);
        panbottom.display(false);
      } else {
        /* Image higher than viewport... */
        if (top >= 0) {
          top = 0;
          pantop.display(false);
        } else {
          pantop.display(true);
        }
        if ((wrapheight - imageheight) >= top ) {
          top = wrapheight - imageheight;
          panbottom.display(false);
        } else {
          panbottom.display(true);
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

    this.getOrigWidth = function () { return orig_width; };
    this.getOrigHeight = function () { return orig_height; };

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
      image.trigger("zoom", [cur_zoom]);
      image.attr({width: width, height: height});
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

    this.doZoom = function (increment) {
      if (Math.abs(increment) > 1) {
        increment = increment>1?10:-10;
      }
      this.setZoom(cur_zoom + increment);
    }

    /**
     * Handle Zoom by mousewheel (IE)
     */

		jQuery(this).bind("mousewheel", function(e){
			// Respond to mouse wheel in IE. (It returns up/dn motion in multiples of 120)
			if (e.wheelDelta >= 120)
				this.doZoom(1);
			else if (e.wheelDelta <= -120)
        this.doZoom(-1);
			
			e.preventDefault();
		})
	
	
    /**
     * Handle Zoom by mousewheel (FF)
     */

		if (this.addEventListener) {
			// Respond to mouse wheel in Firefox
			this.addEventListener('DOMMouseScroll', function(e) {
				if (e.detail > 0)
          this.doZoom(-1);
				else if (e.detail < 0)
          this.doZoom(1);
				
				e.preventDefault();
			}, false);
		}

    /**
     * Handle panning by mouse drag
     */

    var ondrag = false;
    var drag_px;
    var drag_py;
    jQuery('#'+insideId)
    .mousedown(function (e) {
      drag_px = e.screenX;
      drag_py = e.screenY;
      //jQuery(this).css('cursor', 'move');
      jQuery(this).addClass('ondrag');
      ondrag = true;
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

    /**
     * Make sure the image is correctly located inside the div and assert context variables are sync'd
     */

    this.refresh = function () {
      imagewidth = image.width();
      imageheight = image.height();
      wrapwidth = wrapdiv.width();
      wrapheight = wrapdiv.height();
      //orig_width = image.get(0).clientWidth;
      //orig_height = image.get(0).clientHeight;
      pantop.center();
      panbottom.center();
      panleft.center();
      panright.center();
      self.doMove(0, 0);
    }

    //jQuery(window).resize(this.refresh);
  });
}
