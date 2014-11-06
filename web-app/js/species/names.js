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
//               appendTo:_options.appendTo,
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
        })

        if(result.length > 0) {
            result.each(function() {
                $(this).data( "customCatcomplete" )._renderItem = function( ul, item ) {
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

                $(this).data( "customCatcomplete" )._resizeMenu = function() {
                    this.menu.element.outerWidth( 300 );
                }
            });

        }

        return result;
    }
}(window.jQuery));

$(document).ready(function() {
    initializeNameSuggestion();
});

function cancelRecoComment() {
    $('#recoComment').val('');
    $('#reco-options').hide();
    $('#reco-action').show();
}

function initializeNameSuggestion() {
    $('.commonName').autofillNames({
        'nameFilter':'commonNames',
        focus: function( event, ui ) {
            //$(this).val( ui.item.label.replace(/<.*?>/g,"") );
            //            $(this).parent().find(".nameSuggestions li a").css('border', 0);
            return false;
        }, select: function( event, ui ) {
            $(this).val( ui.item.label.replace(/<.*?>/g,"") );
            $(this).closest(".commonNameDiv").next().find(".canName").val( ui.item.value );
            $(this).closest(".commonNameDiv").next().find(".recoName").val( ui.item.value );
            if(ui.item.languageName !== null){
                $(this).closest(".commonNameDiv").find(".languageComboBox").val(ui.item.languageName).attr("selected",true);
                $(this).closest(".commonNameDiv").find(".languageComboBox").data('combobox').refresh();
            }
            return false;
        }, open: function(event, ui) {
            //            $(this).parent().find(".nameSuggestions ul").removeAttr('style').css({'display': 'block','width':'300px'}); 
        }

    });


    $('.recoName').autofillNames({
        'nameFilter':'scientificNames',
        focus: function( event, ui ) {
            $(this).closest(".sciNameDiv").find(".canName").val("");
            //$(this).val( ui.item.label.replace(/<.*?>/g,"") );
            //            $(this).parent().find(".nameSuggestions li a").css('border', 0);
            return false;
        },
        select: function( event, ui ) {
            $(this).val( ui.item.label.replace(/<.*?>/g,"") );
            $(this).closest(".sciNameDiv").find(".canName").val( ui.item.value );
            $(this).closest(".sciNameDiv").find(".mappedRecoNameForcanName").val(ui.item.label.replace(/<.*?>/g,""));
            return false;
        },open: function(event, ui) {
            //            $(this).parent().find(".nameSuggestions ul").removeAttr('style').css({'display': 'block','width':'300px'}); 
        }
    });

    $(".recoName").keypress(function() {
        if ($(this).closest(".sciNameDiv").prev().find(".mappedRecoNameForcanName").val() !== $(this).val()) {
            $(this).closest(".sciNameDiv").find(".canName").val('');
        }
    });
}
