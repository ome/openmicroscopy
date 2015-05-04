{% comment %}
/**
  Copyright (C) 2012 University of Dundee & Open Microscopy Environment.
  All rights reserved.

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU Affero General Public License as
  published by the Free Software Foundation, either version 3 of the
  License, or (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Affero General Public License for more details.

  You should have received a copy of the GNU Affero General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/


/*
    Hotkeys code below is for use as a jsTree plugin. 
    By putting the code in an include, we can reuse it in several jsTrees on different pages.
*/
{% endcomment %}


// default hotkeys only do 'hover' on up/down/left/right. We want to 'select'
// up/down just move selection, don't expand/collapse
// left/right expand and collapse. If right and no children, select next.
// shift+up/down to select range.
"hotkeys" : {
    "right" : function () {
        // starting point is the last-selected...
        var o = this.data.ui.last_selected;
        // ...unless hover appears selected (we're in the process of rapidly traversing the tree)
        if (this.data.ui.hovered && this.data.ui.hovered.children("a").hasClass("jstree-clicked") ) {
            o = this.data.ui.hovered;
        }
        if(o && o.length) {
            if(o.hasClass("jstree-closed")) { this.open_node(o); }
            // if we are on a leaf and we have more siblings, select them
            else if (this.is_leaf(o) && (o.nextAll("li").size() > 0)){
                var new_select = this._get_next(o);
                // make the 'next' node appear selected
                o.children("a").removeClass("jstree-clicked");
                new_select.children("a").addClass("jstree-clicked");
                this.hover_node(new_select);  // also add 'hover' as our marker
                var datatree = this;
                // our *actual* selection occurs after timeout, if selection hasn't moved yet
                setTimeout(function (){
                    if (new_select.children("a").hasClass("jstree-hovered")) {
                        datatree.data.ui.selected = $();    // clears any previous selection
                        datatree.select_node(new_select);   // trigger selection event
                    }
                }, 100);
            }
        }
        return false;
    },
    "down" : function () {
        // remove selection
        this.data.ui.selected && this.data.ui.selected.children("a").removeClass("jstree-clicked");
        // starting point is the last-selected...
        var o = this.data.ui.last_selected;
        // ...unless hover appears selected (we're in the process of rapidly traversing the tree)
        if (this.data.ui.hovered && this.data.ui.hovered.children("a").hasClass("jstree-clicked") ) {
            o = this.data.ui.hovered;
        }
        if(o && o.length) {
            var new_select = this._get_next(o);
            // make the 'next' node appear selected
            o.children("a").removeClass("jstree-clicked");
            new_select.children("a").addClass("jstree-clicked");
            this.hover_node(new_select);  // also add 'hover' as our marker
            var datatree = this;
            // our *actual* selection occurs after timeout, if selection hasn't moved yet
            setTimeout(function (){
                if (new_select.children("a").hasClass("jstree-hovered")) {
                    datatree.data.ui.selected = $();    // clears any previous selection
                    datatree.select_node(new_select);   // trigger selection event
                }
            }, 100);
        }
        return false;
    },
    "up" : function () {
        // remove selection
        this.data.ui.selected && this.data.ui.selected.children("a").removeClass("jstree-clicked");
        // starting point is the last-selected...
        var o = this.data.ui.last_selected;
        // ...unless hover appears selected (we're in the process of rapidly traversing the tree)
        if (this.data.ui.hovered && this.data.ui.hovered.children("a").hasClass("jstree-clicked") ) {
            o = this.data.ui.hovered;
        }
        if(o && o.length) {
            var new_select = this._get_prev(o);
            // make the 'previous' node appear selected
            o.children("a").removeClass("jstree-clicked");
            new_select.children("a").addClass("jstree-clicked");
            this.hover_node(new_select);  // also add 'hover' as our marker
            var datatree = this;
            // our *actual* selection occurs after timeout, if selection hasn't moved yet
            setTimeout(function (){
                if (new_select.children("a").hasClass("jstree-hovered")) {
                    datatree.data.ui.selected = $();    // clears any previous selection
                    datatree.select_node(new_select);   // trigger selection event
                }
            }, 100);
        }
        return false;
    },
    "left" : function () {
        var o = this.data.ui.last_selected || -1;
        if(o && o.length) {
            // if node is expanded, simply collapse
            if (o.hasClass("jstree-open")) {
                this.close_node(o);
            } else if (this._get_parent(o)) {
                var new_select = this._get_parent(o);
                this.hover_node(new_select);
                new_select.children("a:eq(0)").click();
            }
        }
        return false;
    },
    "shift+down" : function (e) {
        var o = this.data.ui.selected.last();
        if(o && o.length) {
            var new_select = this._get_next(o);
            this.select_node(new_select, true, e); // tree handles shift events for multi-select
        }
        return false;
    },
    "shift+up" : function (e) {
        var o = this.data.ui.selected.first();
        if(o && o.length) {
            var new_select = this._get_prev(o);
            this.select_node(new_select, true, e); // tree handles shift events for multi-select
        }
        return false;
    },
    // Disable delete key
    "del" : function (e) {
        return false;
    }
}