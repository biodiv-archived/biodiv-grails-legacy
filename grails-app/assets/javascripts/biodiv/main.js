if (typeof (console) == "undefined") {
    console = {};
}
if (typeof (console.log) == "undefined") {
    console.log = function() {
        return 0;
    }
}

/**
 * show status update message for AJAX calls as text in $ele and 
 * handle ele class according to data.status 
 */
function showUpdateStatus(msg, type, $ele) {
    if(!msg) return;

    if($ele == undefined) $ele = $("#seeMoreMessage");

    if(type === 'info') {
        $ele.html(msg).show().removeClass().addClass('alert alert-info');
    } else if(type === 'success') {
        $ele.html(msg).show().removeClass().addClass('alert alert-success');
    } else if(type === 'error') {
        $ele.html(msg).show().removeClass().addClass('alert alert-error');
    } else {
        $ele.hide();
    }
}


jQuery(document).ready(function($) {




    var domain = document.domain.replace('http://','').replace('www.','').replace(':8080','');
    $("#menu .navigation li").hover(
        function () {
            $(".subnavigation", this).show();
        }, 
        function () {
            $(".subnavigation", this).hide();
        }
    );
    $.widget( "custom.catcomplete", $.ui.autocomplete, {
        _renderMenu: function( ul, items ) {
            var self = this,
            currentCategory = "";
            $.each( items, function( index, item ) {
                if ( item.category != currentCategory ) {
                    ul.append( "<li class='ui-autocomplete-category'>" +item.category + "</li>" );
                    currentCategory = item.category;
                }
                self._renderItem( ul, item );
            });
        }
    });

    // IE caching the request in ajax so setting it false globally for all browser
    $.ajaxSetup({cache:false});
    $(document).bind("ajaxStart", function(){
        $('body').addClass('busy');
    }).bind("ajaxStop", function(){
        $('body').removeClass('busy');
    });


    /////////////////////////////////////////////////////////////////////////
    $("#viewThumbnails").click(function() {
        $(".galleria-thumbnails-container").slideToggle("slow");
    });
    /////////////////////////////////////////////////////////////////////////

    var cache = {},
        lastXhr;
    $("#searchTextField").catcomplete({
        appendTo: '#nameSuggestionsMain',
        source:function( request, response ) {
            var term = request.term;
            if ( term in cache ) {
                response( cache[ term ] );
                return;
            }

            lastXhr = $.getJSON( window.params.nameTermsUrl, request, function( data, status, xhr ) {
                cache[ term ] = data;
                if ( xhr === lastXhr ) {
                    response( data );
                }
            });
        },focus: function( event, ui ) {
            $("#canName").val("");
            $( "#searchTextField" ).val( ui.item.label.replace(/<.*?>/g,"") );
            return false;
        },
        select: function( event, ui ) {
            /*if( ui.item.category == 'Names' && ui.item.value != null) {
              console.log(ui.item.value);
              if(ui.item.value != 'null') {
              $( "#searchTextField" ).val( 'canonical_name:"'+ui.item.value+'" '+ui.item.label.replace(/<.*?>/g,'') );
              }
              } else {*/
            $( "#searchTextField" ).val( ui.item.label.replace(/<.*?>/g,'') );
            //}

            if(ui.item.category == 'Species Pages') {
                $("#category").val('species');
            } else if(ui.item.category == 'Observations') {
                $("#category").val('observation');
            } else if(ui.item.category == 'Groups') {
                $("#category").val('group');
            } else if(ui.item.category == 'Members') {
                $("#category").val('SUser');
            } else if(ui.item.category == 'Pages') {
                $("#category").val('newsletter');
            } else {
                $("#category").val('species');
            }
            $( "#canName" ).val( ui.item.value );

            //$( "#name-description" ).html( ui.item.value ? ui.item.label.replace(/<.*?>/g,"")+" ("+ui.item.value+")" : "" );
            //ui.item.icon ? $( "#name-icon" ).attr( "src",  ui.item.icon).show() : $( "#name-icon" ).hide();
            $( "#search" ).click();
            return false;
        },open: function(event, ui) {
            $("#nameSuggestionsMain ul").addClass('dropdown-menu');
            //$("#nameSuggestionsMain .dropdown-toggle").dropdown('open');	
        }
    }).data( "customCatcomplete" )._renderItem = function( ul, item ) {
        ul.removeClass().addClass("dropdown-menu")
            if(item.category != "Names") {
                return $( "<li class='span6'  style='list-style:none;'></li>" )
                    .data( "ui-autocomplete-item", item )
                    .append( "<a>" + item.label + "</a>" )
                    .appendTo( ul );
            } else {
                if(!item.icon) {
                    item.icon =  window.params.noImageUrl
                }  
                return $( "<li class='span6' style='list-style:none;'></li>" )
                    .data( "ui-autocomplete-item", item )
                    //.append( "<img class='group_icon' style='float:left; background:url(" + item.icon+" no-repeat); background-position:0 -100px; width:50px; height:50px;opacity:0.4;' class='ui-state-default icon'/><a>" + item.label + ((item.desc)?'<br>(' + item.desc + ')':'')+"</a>" )
                    .append( "<a title='"+item.label.replace(/<.*?>/g,"")+"'><img src='" + item.icon+"' class='group_icon' style='float:left; background:url(" + item.icon+" no-repeat); background-position:0 -100px; width:50px; height:50px;opacity:0.4;'/>" + item.label + ((item.desc)?'<br>(' + item.desc + ')':'')+"</a>" )
                    .appendTo( ul );
            }
    };


    $("#search").click(function() {
        if($('#searchTextField').val()) {
            $( "#searchbox" ).submit();
        }
    });
    $( "#searchbox" ).submit(function() {
        var action = $( "#searchbox" ).attr('action');
        var category = $("#category").val();
        if(category) {
            action = action.replace(window.params.searchController, category);
        }

        if($("#userGroupSelectFilter").val() == 'ALL') {
            action = window.params.IBPDomainUrl+action;
        }

        //updateGallery(action, undefined, undefined, undefined, false,undefined,undefined,true);
        //return false;
    });

    $("#searchToggle").click(function() {
        $(this).hide();		
        $('#searchToggleBox').slideToggle();
    });

    $('.clickcontent').click(function() {
        var target = $(this).data('target');
        $(this).parent().siblings('.'+target).toggle();
    });


    $(".ui-icon-control").click(function() {
        var div = $(this).siblings("div.toolbarIconContent");
        if (div.is(":visible")) {
            div.hide(400);
        } else {
            div.slideDown("slow");	
            if(div.offset().left < 0) {
                div.offset({left:div.parent().offset().left});					
            }
        }
    });

    $("a.ui-icon-close").click(function() {
        $(this).parent().hide("slow");
    });


});


