/**
 * jquery-plugin-slider - Slider plugin for jQuery
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

/*
 * TODO: Make css classes configurable to avoid clashes.
 */

$.fn.gs_slider = function(cfg) {
  return this.each(function(){
      this.sliderCfg = {
        orientation: cfg && cfg.orientation == 'v' ? 'height' : 'width',
        anchor: cfg && cfg.orientation == 'v' ? 'top' : 'left',
        direction: cfg && cfg.orientation == 'v' ? -1 : 1,
        handleSize: cfg && cfg.handleSize ? cfg.handleSize : '10px',
        min: cfg && !isNaN(parseInt(cfg.min)) ? parseInt(cfg.min) : 0,
        max: cfg && !isNaN(parseInt(cfg.max)) ? parseInt(cfg.max) : 100,
        range: 0,
        repeatCallback: cfg && cfg.repeatCallback,
        tooltip_prefix: cfg && cfg.tooltip_prefix ? cfg.tooltip_prefix : ''
      };
	  this.pos = 0;
      this.sliderCfg.range = this.sliderCfg.max - this.sliderCfg.min + 1;
      if (!this.id) {
        this.id = '' + (new Date()).getTime();
      }

	  var handleId = this.id + '-shi';
	  var lineId = this.id + '-sli';
	  var btnUpId = this.id + '-bup';
	  var btnPlayUpId = this.id + '-bpup';
	  var btnDownId = this.id + '-bdn';
	  var btnPlayDownId = this.id + '-bpdn';

      var slider_container = jQuery(this);

      /* Are we horizontal or vertical? */
      if (this.sliderCfg.orientation == 'width') {
        slider_container.addClass('hslider');
      } else {
        slider_container.addClass('vslider');
      }

      /* Create the scale and handle */
      slider_container.append('<div id="'+handleId+'" class="slider-handle draggable"></div>');
      var handle = jQuery('#'+handleId);
      handle.wrap('<div id="'+lineId+'" class="slider-line"></div>');
      var handle_rel = 100.0 / this.sliderCfg.range;
      handle.css(this.sliderCfg.orientation, handle_rel + '%');
      if (handle_rel == 100) {
        handle.addClass('disabled');
      } else {
	handle.removeClass('disabled');
      }
      var slider = jQuery("#"+lineId);

      /* The buttons */
      slider.before('<div id="'+btnPlayUpId+'" class="slider-btn-playup"></div>');//&nbsp;</div>');
      slider.before('<div id="'+btnUpId+'" class="slider-btn-up"></div>');//&nbsp;</div>');
      var btnup = jQuery("#"+btnUpId);
      var btnplayup = jQuery("#"+btnPlayUpId);
      slider.after('<div id="'+btnDownId+'" class="slider-btn-down"></div>');//&nbsp;</div>');
      slider.after('<div id="'+btnPlayDownId+'" class="slider-btn-playdown"></div>');//&nbsp;</div>');
      var btndown = jQuery("#"+btnDownId);
      var btnplaydown = jQuery("#"+btnPlayDownId);

      /* Are we horizontal or vertical? */
      if (this.sliderCfg.orientation == 'width') {
        slider_container.addClass('hslider');
      } else {
        slider_container.addClass('vslider');
      }

      var self = this;

      /**
       * Gets an event (like a mouse click) and calculates the position within the slider set range.
       * Pos is relative (0..max-min-1)
       */
      var posFromEvent = function (e) {
        var xypos;
        if (self.sliderCfg.orientation == 'width') {
          xypos = e.pageX - slider.offset().left; // + (handle.get(0).clientWidth);
          xypos = xypos * 100.0 / slider.get(0).clientWidth;
        } else {
          xypos = e.pageY - slider.offset().top + (handle.get(0).clientHeight);
          xypos = xypos * 100.0 / slider.get(0).clientHeight ;
        }
        
        var pos = parseInt(xypos * (self.sliderCfg.range) / 100.0);
        if (self.sliderCfg.direction < 0) {
          pos = self.sliderCfg.range - pos;
        }
        return pos;
      };


      /**
       * Set the slider position: moves the handle and signals a 'change'.
       * The value 'pos' is expected within the allowed range set for the slider.
       * @param {Integer} pos The slider position to set.
       * @param {Tristate} trigger If null will trigger 'change' if changed. True forces the 'change' event, false inhibits it.
       */
      this.setSliderPos = function (pos, trigger) {
        var pos = Math.min(Math.max(pos, this.sliderCfg.min), this.sliderCfg.max) - this.sliderCfg.min;
        if (pos != this.pos || trigger) {
          this.pos = pos;
	  if (self.sliderCfg.range == 1) {
            handle.css(this.sliderCfg.anchor, '-1px');
	  } else if (self.sliderCfg.direction < 0) {
            handle.css(this.sliderCfg.anchor, (100.0-handle_rel-(this.pos*100.0/this.sliderCfg.range))+'%');
          } else {
            handle.css(this.sliderCfg.anchor, (this.pos*100.0/this.sliderCfg.range)+'%');
          }
	  if (trigger != false) {
	    slider_container.trigger('change', [this.sliderCfg.min + this.pos]);
	  }
	  return true;
        }
	return false;
        //handle.attr('title', this.pos + self.sliderCfg.min);
      };

      /**
       * Set the slider range and position.
       * The value 'current' is expected between the supplied 'min' and 'max'.
       * @param {Integer} min The minimal slider position to set.
       * @param {Integer} pos The maximum slider position to set.
       * @param {Integer} pos The slider position to set.
       * @param {Tristate} trigger If null will trigger 'change' if changed. True forces the 'change' event, false inhibits it.
       */
      this.setSliderRange = function (min, max, current, trigger) {
	this.sliderCfg.max = max;
	this.sliderCfg.min = min;
	this.sliderCfg.range = max - min + 1;
	handle_rel = 100.0 / this.sliderCfg.range;
	handle.css(this.sliderCfg.orientation, handle_rel + '%');
	if (handle_rel == 100) {
	  handle.addClass('disabled');
	} else {
	  handle.removeClass('disabled');
	}
	if (current != null) {
          this.setSliderPos(current, trigger);
	}
      };

      /************************/
      /* Event handling below */

      var handlesliderpos = function (e) {
        var rpos = posFromEvent(e);
        self.stoprepeat()
        self.setSliderPos(rpos+self.sliderCfg.min);
      }

      slider.click(handlesliderpos);

      var onrepeat = false;
      var repeat_timer;
      var ondrag = false;

      var startrepeat = function (additive) {
        onrepeat = true;
        self.setSliderPos(self.pos+self.sliderCfg.min+additive);
        var repeat_func = function (timeout) {
          repeat_timer = setTimeout(function () {
            if (onrepeat) {
              if (self.sliderCfg.repeatCallback != null && !self.sliderCfg.repeatCallback()) {
                /* The callback says we can't update to a new value yet... */
                repeat_func(20);
                return;
              }
              if (self.setSliderPos(self.pos+self.sliderCfg.min+additive)) {
                repeat_func(20);
	      } else {
		self.stoprepeat();
	      }
            }
          }, timeout);
        };
        repeat_func(500);
      }

      this.stoprepeat = function () {
        onrepeat = false;
        clearTimeout(repeat_timer);
        btnup.unbind('mouseout', self.stoprepeat);
        btnup.unbind('mouseup', self.stoprepeat);
        btndown.unbind('mouseout', self.stoprepeat);
        btndown.unbind('mouseup', self.stoprepeat);
	btnplayup.removeClass('onplay');
	btnplaydown.removeClass('onplay');
      }

      btnplayup.click(function () {
        var onplay = btnplayup.is('.onplay');
        self.stoprepeat();
  	if (!onplay) {
          btnplayup.toggleClass('onplay');
          startrepeat(-self.sliderCfg.direction);
	}
      });

      btnup.mousedown(function () {
	self.stoprepeat();
	btnup.mouseup(self.stoprepeat);
	btnup.mouseout(self.stoprepeat);
        startrepeat(-self.sliderCfg.direction);
        return false;
      });


      btnplaydown.click(function () {
        var onplay = btnplaydown.is('.onplay');
        self.stoprepeat();
  	if (!onplay) {
          btnplaydown.toggleClass('onplay');
          startrepeat(self.sliderCfg.direction);
	}
      });

      btndown.mousedown(function () {
	self.stoprepeat();
        btndown.mouseup(self.stoprepeat);
        btndown.mouseout(self.stoprepeat);
        startrepeat(self.sliderCfg.direction);
        return false;
      });


      handle.mousedown(function (e) {
        /* Start handle drag */
        jQuery(document).mousemove(domove);
        jQuery(document).mouseup(stopdrag);
        ondrag = true;
        handle.addClass('ondrag');
        handle.removeClass('draggable');
        return false;
      });

      var stopdrag = function (e) {
        /* Stop handle drag */
        clearTimeout(repeat_timer);
        jQuery(document).unbind('mousemove', domove);
        jQuery(document).unbind('mouseup', stopdrag);
        ondrag = false;
        handle.addClass('draggable');
        handle.removeClass('ondrag');
	handlesliderpos(e);
      }

      var domove = function (e) {
        self.stoprepeat()
        if (ondrag) {
          var xypos, sliderSize;
          if (self.sliderCfg.orientation == 'width') {
            sliderSize = slider.get(0).clientWidth;
            xypos = e.pageX - slider.offset().left - (handle.get(0).clientWidth / 2);
          } else {
            sliderSize = slider.get(0).clientHeight;
            xypos = e.pageY - slider.offset().top - (handle.get(0).clientHeight / 2);
          }
          xypos = xypos * 100.0 / sliderSize ;
          handle.css(self.sliderCfg.anchor, Math.min(Math.max(0,xypos),(self.sliderCfg.range-1)*100.0/self.sliderCfg.range)+'%');
          //self.setSliderPos(posFromEvent(e));
        }
      };

      //jQuery(document).mousemove(domove);

      function follow_pos (e) {
        var pos = posFromEvent(e);
        if (pos>=0) {
          if (ondrag) {
            if (repeat_timer) {
              clearTimeout(repeat_timer);
            }
            repeat_timer = setTimeout(function () {self.setSliderPos(pos+1);}, 2000);
          }
          slider.attr('title', self.sliderCfg.tooltip_prefix + (pos+1));
        }
      }

      function ttshow (e) {
        slider.mousemove(follow_pos);
      }

      function tthide (e) {
        slider.unbind('mousemove', follow_pos);
      }

      slider.hover(ttshow, tthide);
  });
}
