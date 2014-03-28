(function ($) {
    "use strict";

    //types
    $.fn.components = {};

    var AbstractType = function () { 
    };

    AbstractType.prototype = {
        init : function(element, options) {
            this.options = $.extend({}, options);
            this.$element = $(element);
            if(!this.options.scope) {
                this.options.scope = this;
            }
        }
    }

    $.extend($.fn.components, {abstracttype: AbstractType});

}(window.jQuery));


