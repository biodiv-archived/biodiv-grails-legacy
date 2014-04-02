/**
  @class TaxonHierarchy
  @extends abstracttype
 **/
(function ($) {
    "use strict";

    var TaxonHierarchy = function (div, selector, options) {
        this.init($(div), options);
        this.initEditables(selector);
    };

    $.fn.editableutils.inherit(TaxonHierarchy, $.fn.components.abstracttype);

    $.extend(TaxonHierarchy.prototype, {
        constructor: TaxonHierarchy,
        onAdd : function(e){    
            e.stopPropagation();
            e.preventDefault();
            $(e.currentTarget).hide();
            console.log('add');
        }, 
        onEdit : function(e){    
            e.stopPropagation();
            e.preventDefault();
            $(e.currentTarget).hide();
        },
        onDelete : function(e){    
            e.stopPropagation();
            e.preventDefault();
            $(e.currentTarget).hide();
            console.log('delete');
        },
        initEditableForm : function() {
            this.$element.find('#taxonHierachyInput').show();
            //Submit
            $form.on('submit', {'editor':editor}, onFormSubmit);

            //Cancel
            $form.find('.editable-cancel').click(function(){
                //can even remove form
                $form.hide();
                $conEntry.show();
            });


        }
    });

    /*
       Initialize taxonHierarchy. Applied to jQuery object.

       @method $().taxonHierarchy(options)
       @params {Object} options
       */
    $.fn.taxonhierarchy = function (options) {
        var args = arguments;
        return this.each(function () {
            var $this = $(this);
            new TaxonHierarchy(this, '#taxaHierarchy', options);
        });
    };
}(window.jQuery)); 



