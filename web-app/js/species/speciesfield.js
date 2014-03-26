/**
  @class SpeciesField
  @extends abstracttype
 **/
(function ($) {
    "use strict";

    var SpeciesField = function (div, selector, options) {
        this.init($(div), options);
        //nothing shown after init
        this.initEditables(selector);
    };

    $.fn.editableutils.inherit(SpeciesField, $.fn.speciesComponents.abstracttype);

    $.extend(SpeciesField.prototype, {
        constructor: SpeciesField
    });

    /*
       Initialize speciesfield. Applied to jQuery object.

       @method $().speciesField(options)
       @params {Object} options
       */
    $.fn.speciesfield = function (options) {
        var args = arguments;
        return this.each(function () {
            var $this = $(this);
            new SpeciesField(this, '.ck_desc', options);
        });
    };

}(window.jQuery)); 



