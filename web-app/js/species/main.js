if (typeof (console) == "undefined") {
    console = {};
}
if (typeof (console.log) == "undefined") {
    console.log = function() {
        return 0;
    }
}


function show_login_dialog(successHandler, errorHandler, cancelHandler) {
    ajaxLoginSuccessCallbackFunction = successHandler;
    ajaxLoginErrorCallbackFunction = errorHandler;
    ajaxLoginCancelCallbackFunction = cancelHandler;
    $('#ajaxLogin').modal({'keyboard':true, 'show':true});
}

function cancelLogin() {
    $('#ajaxLogin').modal('hide');
}


function updateLoginInfo(){
    $('#ajaxLogin').modal('hide');
    $('#loginMessage').html('').removeClass().hide();
    reloadLoginInfo();
}

function handleError(xhr, textStatus, errorThrown, successHandler, errorHandler, cancelHandler) {
    if (xhr.status == 401) {
        show_login_dialog(successHandler, errorHandler, cancelHandler);
        //window.location.href = "/biodiv/login?spring-security-redirect="+window.location.href;
    } else {
        if (errorHandler)
            errorHandler();
        else
            console.log(errorThrown);
    }
}

function adjustHeight() {
    $(".ellipsis").ellipsis();
    $('.snippet .observation_story_image').each(function() {
        $(this).css({
            'height': $(this).next().height()
        });
    });
}
// Callback to execute whenever ajax login is successful.
// Todo some thing meaningful with the response data
var ajaxLoginSuccessCallbackFunction, ajaxLoginErrorCallbackFunction, ajaxLoginCancelCallbackFunction;

var reloadLoginInfo = function() {
    $.ajax({
        url : window.appContext+"/SUser/login",
        success : function(data) {
            $('.header_userInfo').replaceWith(data);
        }, error: function (xhr, ajaxOptions, thrownError){
            alert("Error while getting login information : "+xhr.responseText);
        }
    });
}

var ajaxLoginSuccessHandler = function(json, statusText, xhr, $form) {
    if (json.success || json.status == 'success') {		
        if (ajaxLoginSuccessCallbackFunction) {
            ajaxLoginSuccessCallbackFunction(json,
                    statusText, xhr);
            ajaxLoginSuccessCallbackFunction = undefined;
        }
        updateLoginInfo()
    } else if(json.error && json.status === 401) {
        $('#loginMessage').html("Resending previous request").removeClass().addClass('alter alert-info').show();
        ajaxLoginErrorCallbackFunction(json);		
        //updateLoginInfo()                
    } else if (json.error || json.status == 'error') {
        $('#loginMessage').html(json.error).removeClass().addClass('alter alert-error').show();
    } else {
        $('#loginMessage').html(json).removeClass().addClass('alter alert-info').show();
    }
}

/**
 * show status update message for AJAX calls as text in $ele and 
 * handle ele class according to data.status 
 */
function showUpdateStatus(msg, type, $ele) {
    if(!msg) return;

    if($ele == undefined) $ele = $("#seeMoreMessage")

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
    //	if (domain == appWGPDomain){
    //        $('#ibp-header').hide();
    //        $('#wgp-header').show();
    //        $('#ibp-footer').hide();
    //        $('#wgp-footer').show();
    //    }
    //
    //    if (domain == appIBPDomain){
    //        $('#wgp-header').hide();
    //        $('#ibp-header').show();
    //        $('#wgp-footer').hide();
    //        $('#ibp-footer').show();
    //    }

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
        $(this).addClass('busy');
    }).bind("ajaxStop", function(){
        $(this).removeClass('busy');
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
            $("#nameSuggestionsMain ul").removeAttr('style').addClass('dropdown-menu');
            $("#nameSuggestionsMain .dropdown-toggle").dropdown('toggle');				
        }
    }).data( "customCatcomplete" )._renderItem = function( ul, item ) {
        ul.removeClass().addClass("dropdown-menu")
            if(item.category != "Names") {
                return $( "<li class='span3'  style='list-style:none;'></li>" )
                    .data( "item.autocomplete", item )
                    .append( "<a>" + item.label + "</a>" )
                    .appendTo( ul );
            } else {
                if(!item.icon) {
                    item.icon =  window.params.noImageUrl
                }  
                return $( "<li class='span3' style='list-style:none;'></li>" )
                    .data( "item.autocomplete", item )
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

        updateGallery(action, undefined, undefined, undefined, false,undefined,undefined,true);
        return false;
    });

    $("#searchToggle").click(function() {
        $(this).hide();		
        $('#searchToggleBox').slideToggle();
    });

});
