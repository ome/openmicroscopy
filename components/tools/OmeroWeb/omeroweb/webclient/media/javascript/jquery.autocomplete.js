(function($) {

    /**
     * autor: CTAPbIu_MABP
     * email: ctapbiumabp@gmail.com
     * site: http://mabp.kiev.ua/content/2008/04/08/autocomplete_by_your_own_hands/
     * license: MIT & GPL
     * last update: 01.05.2008
     */

    var ac = function(c, o) {
        this.cache = {};
        this.store = {};
        this.init(c, o)
    }
    ac.prototype = {

    // html elements
        ac : null, // main input
        ul : null, // autocomplete list
        img : null, // image
        container : null, // outer div

    // timeouts
        close : null, // ac hide
        timeout : null, // ac search

    // sys defs
        chars : 0, // previous search string lenght

    // user defs
        url : null, // url for ajax request
        source : null, // <select/>, [], {} jQuery
        minchar : null, // minchars for search 1.2.6 fix
        delay : 50, // for search
        fillin : false, // pre fill-in
        type : 'xml', // ajax data type
        width : 200, // width
        top : false,

    // events
        onSelect : function () {
        },
        onKeyPress : function () {
        },
        onSuggest : function () {
        }, 
        onError : function () {
        },
        onSuccess : function () {
        },
        onDisplay : function () {
        },

        init : function (ac, options) {
            $.extend(this, options)

            this.container = $("<div/>")
                .css({width:this.width})
                .addClass('ac_conteiner')
                .insertBefore(ac);
            this.ac = $(ac)
                .attr({autocomplete:"off"})
                .bind("blur", this, function(e) {
                    var self = e.data;
                    clearTimeout(self.close);
                    self.close = setTimeout(function() {
                        self.ul.hide();
                    }, 500);
                }) // IE bug self.ul[.hide()] = undefined
                .addClass('ac_input')
                .css({width:this.width - ($.browser.mozilla?20:22)}) // 18 img + 2 margin + 2 IE?
                .appendTo(this.container);
            this.img = $("<div/>")
                .bind("click", this, function(e) {
                    var self = e.data;
                    clearTimeout(self.close);
                    self.scroll();
                    self.ul.toggle();
                    self.ac.focus();
                })
                .addClass('ac_img')
                .appendTo(this.container);
            this.ul = $("<div/>")
                .bind("mousedown", this, function(e) {
                    var self = e.data;
                    setTimeout(function() {
                        clearTimeout(self.close);
                        self.ac.focus();
                    }, 50);
                })// IE scroll bug
                .addClass('ac_results')
                .appendTo(this.container)
                .css({
                    width:this.width,
                    top:(this.container.offset().top + this.container.height() + parseInt(this.container.css("border-top-width"))),
                    left:(this.container.offset().left + parseInt(this.container.css("border-left-width")))
                });  // Safari window.onLoad bug


            $(window).bind("resize load", this, function(e) {
                var self = e.data, c = self.container;
                self.ul.css({
                    width:self.width,
                    top:(   self.top ?
                            c.offset().top - self.ul.height() - parseInt(c.css("border-top-width")) :
                            c.offset().top + c.height() + parseInt(c.css("border-top-width")) ),
                    left:(c.offset().left + parseInt(c.css("border-left-width")))
                });
            });

            if ($.browser.mozilla)
                this.ac.bind("keypress", this, this.process);
            else
                this.ac.bind("keydown", this, this.process);

            if (this.fillin)
                this.suggest("hide");
        },

        process : function (e) {
            var self = e.data, len = self.ac.val().length;
            self.onKeyPress.apply(self,arguments);
            if ((/27$|38$|40$/.test(e.keyCode) && self.ul.is(':visible')) || (/^13$|^9$/.test(e.keyCode) && self.get())) {
                e.preventDefault();
                e.stopPropagation();
                switch (e.keyCode) {
                    case 38: // up
                        self.prev();
                        break;
                    case 40: // down
                        self.next();
                        break;
                    case 9:  // tab
                    case 13: // return
                        self.select();
                        break;
                    case 27: // escape
                        self.ul.hide();
                        break;
                }
            } else if (len != self.chars || !len) {
                self.chars = len;

                if (self.timeout)
                    clearTimeout(self.timeout);
                self.timeout = setTimeout(function() {
                    self.suggest("show")
                }, self.delay);
            }
        },

        get : function() {
            if (!this.ul.is(':visible'))
                return false;
            var current = this.ul.children('div.ac_over');
            if (!current.length)
                return false;
            return current;
        },

        prev : function () {
            var current = this.get();
            var prev = current.prev();
            if (current) {
                current.removeClass('ac_over');
                if (prev.text())
                    prev.addClass('ac_over');
                }
            if (!current || !prev.text())
                this.ul.children('div:last-child').addClass('ac_over');
            this.scroll();
        },

        next : function () {
            var current = this.get(), next = current.next();
            if (current) {
                current.removeClass('ac_over');
                if (next.text())
                    next.addClass('ac_over');
            }
            if (!current || !next.text())
                this.ul.children('div:first-child').addClass('ac_over');
            this.scroll();
        },

        scroll : function(){
            var current = this.get();
            if (!current)
                return; // quick return
            var el = current.get(0), list = this.ul.get(0);
            if(el.offsetTop + el.offsetHeight > list.scrollTop + list.clientHeight)
                list.scrollTop = el.offsetTop + el.offsetHeight - list.clientHeight;
            else if(el.offsetTop < list.scrollTop)
                list.scrollTop = el.offsetTop;
        },

        select : function () {
            var current = this.get();
            if (current) {
                this.ac.val(current.text());
                this.ul.hide();
                this.onSelect.apply(this);
            }
        },

        suggest : function (show) {
            var self = this, mask = $.trim(this.ac.val());
            this.ul.empty().hide();
            if (mask.length >= this.minchar) {
                self.onSuggest.apply(self,arguments);
                if (this.check(mask))
                    this.display(this.grab(mask))[show]();
                else if (this.url) // use ajax
                    $.ajax({type: "GET", url:this.url, data:{mask:mask},
                        success:function(xml) {
                            self.onSuccess.apply(self,arguments);
                            self.prepare(xml,mask)[show]();
                        },
                        error:function(){
                            self.onError.apply(self,arguments);
                        },
                        dataType:self.type
                    });
                else if (this.source) // use source
                    this.prepare(this.source,mask)[show]();
            }
        },

        check: function (mask){
            if (this.store[mask])
                return this.store[mask]; // quick return
            mask = mask.toLowerCase();
            for(var it in this.cache)
                if (it && !mask.indexOf(it.toLowerCase()))
                    return true;
            return false;
        },

        grab: function (mask){
            if (this.store[mask])
                return this.store[mask]; // quick return
            var list = [], map = [], array = [];
            for(var it in this.cache)
                array.push(it);
            array = array.reverse();
            mask = mask.toLowerCase();
            for(var item in array)
                if(!mask.indexOf(array[item].toLowerCase())){
                    for(var word in this.cache[array[item]])
                        if (!this.cache[array[item]][word].toLowerCase().indexOf(mask)){
                            map.push(this.cache[array[item]][word]);
                            list.push(this.mark(this.cache[array[item]][word],mask))
                        }
                    break;
                }
            this.cache[mask] = map;
            this.store[mask] = list.join("");
            return this.store[mask];
        },

        prepare : function(xml,mask){
            var self = this, list = [], map = [];
            if(xml.constructor != Array && ($.browser.opera ? $(xml).find("option").length : xml.constructor != Object))
                $.each($(xml).find("option"), function(i, n) {  // use selectbox or ajax result
                    n = $(n).text();
                    map.push(n);
                    list.push(self.mark(n,mask));
                });
            else
                $.each(xml, function(i, n) { // use array or array-like object
                    map.push(n);
                    list.push(self.mark(n,mask));
                });

            this.cache[mask] = map;
            this.store[mask] = list.join("");
            return this.display(this.store[mask]);
        },

        mark : function(text,mask){
            if (new RegExp('^' + mask, 'ig').test(text))
                return '<div>' + text.replace(new RegExp('^' + mask, 'ig'), function(mask) {
                    return '<span class="ac_match">' + mask + '</span>';
                }) + '</div>';
        },

        display : function (list) {
            var self = this;
            self.onDisplay.apply(self,arguments);
            if (!list)
                return this.ul;
            return this.ul.append(list).children('div').mouseover(function() {
                    $(this).siblings().removeClass('ac_over').end().addClass('ac_over');
                }).click(function(e) {
                    e.preventDefault();
                    e.stopPropagation();
                    self.select();
                }).end().children('div:first-child').addClass('ac_over').end(); // ul
        }
    }

    $.fn.autocomplete = function(options) {
        this.each(function() {
            new ac(this, options);
        });
        return this;
    };

})(jQuery);


