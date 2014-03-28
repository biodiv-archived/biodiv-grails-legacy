/**
  @class SpeciesField
  @extends abstracttype
 **/
(function ($) {
    "use strict";

    var SpeciesField = function (div, options) {
        this.init($(div), options);
    };

    $.fn.editableutils.inherit(SpeciesField, $.fn.components.abstracteditabletype);

    $.extend(SpeciesField.prototype, {
        constructor: SpeciesField
    });

    /*
       Initialize speciesfield. Applied to jQuery object.

       @method $().speciesField(options)
       @params {Object} options
       */
    $.fn.speciesfield = function (options) {
        var speciesfields = new Array();
        this.each(function () {
            speciesfields.push(new SpeciesField(this, options));
        });
        return speciesfields
    };

}(window.jQuery)); 



