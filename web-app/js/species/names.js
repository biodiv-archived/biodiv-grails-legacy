(function($) {

    var window = this, options = {}, defaults = {};
    //var cache = {}, 
    //var lastXhr;

    $.fn.autofillNames = function(_options) {
        if (typeof _options === 'string' && $.isFunction(autofillNames[_options])) {
            var args = Array.prototype.slice.call(arguments, 1);
            var value = autofillNames[_options].apply(autofillNames, args);
            return value === autofillNames || value === undefined ? this : value;
        }

        _options = $.extend({}, defaults, _options);
        this.options = _options;
        //cache[this.options.nameFilter] = {}
        var appendTo = this.parent().find('.nameSuggestions');
        if(_options.appendTo == undefined && appendTo.length > 0) {
            _options.appendTo = appendTo;
        }
        var result = $(this).catcomplete({
               appendTo:_options.appendTo,
               source:function( request, response ) {
                   var term = request.term;
                   
                   /*if ( term in cache[_options.nameFilter] ) {
                       response( cache[_options.nameFilter][ term ] );
                       return;
                   }*/
                   request.nameFilter = _options.nameFilter;

                   //lastXhr = 
                    $.ajax({
                       url:  window.params.recommendation.suggest,
                           dataType: "json",
                           data: {
                                term : request.term,
                                rank : $(this)[0].element.data('rank')
                           },
                           success: function(data, status, xhr) {
                               //cache[_options.nameFilter][ term ] = data;
                               //if ( xhr === lastXhr ) {
                                   response( data );
                               //}

                           }
                   });
                   /*lastXhr = $.getJSON( window.params.recommendation.suggest, request, function( data, status, xhr ) {
                       cache[_options.nameFilter][ term ] = data;
                       if ( xhr === lastXhr ) {
                           response( data );
                       }
                   });*/
               },
               focus: _options.focus,
               select: _options.select,
               open: _options.open
        }).data( "customCatcomplete" )._renderItem = function( ul, item ) {
            ul.removeClass().addClass("dropdown-menu")
                if(item.category == "General") {
                    return $( "<li class='span3'></li>" )
                        .data( "ui-autocomplete-item", item )
                        .append( "<a>" + item.label + "</a>" )
                        .appendTo( ul );
                } else {
                    if(!item.icon) {
                        item.icon =  window.params.noImageUrl
                    }  
                    return $( "<li class='span3'></li>" )
                        .data( "ui-autocomplete-item", item )
                        .append( "<a title='"+item.label.replace(/<.*?>/g,"")+"'><img src='" + item.icon+"' class='group_icon' style='float:left; background:url(" + item.icon+" no-repeat); background-position:0 -100px; width:50px; height:50px;opacity:0.4;'/>" + item.label + ((item.desc)?'<br>(' + item.desc + ')':'')+"</a>" )
                        .appendTo( ul );
                }
        };
        return result;
    }
}(window.jQuery));
