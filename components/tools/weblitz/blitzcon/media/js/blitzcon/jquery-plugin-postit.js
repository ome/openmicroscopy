/**
 * Post-it plugin for JQuery.
 *
 * Called on a container, creates a post-it like thingy
 *
 * Todo: better documentation and example usage.
 *
 * Depends on jquery, jquery.dimensions.js, jqDnR.js
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

$.fn.postit = function(cfg) {
  return this.each(function(){
    /* The basic setup */
    var self = jQuery(this);
    self
      .append('<div class="postit-toggle-btn" />')
      .append('<div class="postit-close-btn" />')
      .addClass('postit');
    self._hide = self.hide;
    self.hide = function (speed, callback) {self.trigger('closing'); self._hide(speed, callback);};
    /* The buttons */
    jQuery(this).find('.postit-toggle-btn').click(function(e) { self.toggleClass('collapsed'); });
    jQuery(this).find('.postit-close-btn').click(function(e) { self.hide(); });
    /* Some extra details on the dragbar */
    var dragbar = jQuery(this).find('h1:first');
    dragbar.dblclick(function(e) { self.toggleClass('collapsed'); });
    jQuery(this).bind("mousewheel", function(e){
      // Respond to mouse wheel in IE. (It returns up/dn motion in multiples of 120)
      if (e.wheelDelta >= 120)
        self.addClass('collapsed');
      else if (e.wheelDelta <= -120)
        self.removeClass('collapsed');
      e.preventDefault();
    })

    if (dragbar.get(0).addEventListener) {
      // Respond to mouse wheel in Firefox
      dragbar.get(0).addEventListener('DOMMouseScroll', function(e) { 
      if (e.detail > 0)
        self.removeClass('collapsed');
      else if (e.detail < 0)
        self.addClass('collapsed');
      e.preventDefault();
      }, false);
    }

    /* We're done, make it draggable */
    jQuery(this).jqDrag('h1:first');
  });
}
