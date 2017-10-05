
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
    $('.loginMessage').html('').removeClass().hide();
    reloadLoginInfo();
}

function handleError(xhr, textStatus, errorThrown, successHandler, errorHandler, cancelHandler) {
    if (xhr.status == 401) {
        var now = new Date().getTime();
        var brToken = $.cookie("BRToken");
        var baTokenExpiresCookie = $.cookie("BATokenExpires");
        baTokenExpiresCookie = baTokenExpiresCookie ? baTokenExpiresCookie : 0;
        var timeleft = baTokenExpiresCookie + (5*24*60*60*1000) - now;
        if(brToken == null || timeleft <= 0) {
            show_login_dialog(successHandler, errorHandler, cancelHandler);
        } else {
            ajaxLoginSuccessCallbackFunction = successHandler;
            ajaxLoginErrorCallbackFunction = errorHandler;
            ajaxLoginCancelCallbackFunction = cancelHandler;
            //get new accessToken with existing refreshtoken
            getNewAccessToken();
            
        }//window.location.href = "/biodiv/login?spring-security-redirect="+window.location.href;
    } else {
        if (errorHandler)
            errorHandler();
        else
            console.log(errorThrown);
    }
}

function getNewAccessToken() {
    var brTokenCookie = $.cookie("BRToken");
    var isAjax = $("#ajaxLogin").is(':visible'); 
    $.ajax({
        url: window.params.login.tokenUrl,
        method:'POST',
        data:{grant_type:'access_token', 'refresh_token':brTokenCookie},
        type:'json',
        success : function(data) {
            console.log(data);
            setLoginInfo(data, isAjax);
        }, error: function (xhr, ajaxOptions, thrownError) {
            $('.loginMessage').html(xhr.responseJSON.message).removeClass().addClass('alert alert-error').show();
            if(isAjax == true && ajaxLoginErrorCallbackFunction) {
                ajaxLoginErrorCallbackFunction();
                updateLoginInfo();
                alert(xhr.responseJSON.message);
            }
        }

    });
}

/*function adjustHeight() {
    console.log('adjustHeight start');
//    $(".ellipsis").trunk8();
    $('.snippet .observation_story_image').each(function() {
        console.log($(this).next())
        $(this).css({
            'height': $(this).next().height()
        });
    });
    console.log('adjustHeight end');
}*/

// Callback to execute whenever ajax login is successful.
// Todo some thing meaningful with the response data
var ajaxLoginSuccessCallbackFunction, ajaxLoginErrorCallbackFunction, ajaxLoginCancelCallbackFunction;

var reloadLoginInfo = function() {
    if(typeof window.appContext == 'undefined')
        window.appContext = '';
    $.ajax({
        url : window.appContext+"/SUser/loginTemplate",
        success : function(data) {
            $('.header_userInfo').replaceWith(data);
        }, error: function (xhr, ajaxOptions, thrownError){
            alert("Error while getting login information : "+xhr.responseText);
        }
    });
}

var ajaxLoginSuccessHandler = function(json, statusText, xhr, $form) {

    if (json.success || json.status == 'success' || json.access_token != null) {		
        if (ajaxLoginSuccessCallbackFunction) {
            ajaxLoginSuccessCallbackFunction(json,
                    statusText, xhr);
            ajaxLoginSuccessCallbackFunction = undefined;
        }
        updateLoginInfo();
    } else if(json.error && json.status === 401) {
        $('.loginMessage').html("Resending previous request").removeClass().addClass('alter alert-info').show();
        ajaxLoginErrorCallbackFunction(json);		
        //updateLoginInfo()                
    } else if (json.error || json.status == 'error') {
        $('.loginMessage').html(json.error).removeClass().addClass('alter alert-error').show();
    } else {
        $('.loginMessage').html(json).removeClass().addClass('alter alert-info').show();
    }
}

var getCookies = function(){
    var pairs = document.cookie.split(";");
    var cookies = {};
    for (var i=0; i<pairs.length; i++){
        var pair = pairs[i].split("=");
        cookies[pair[0].trim()] = unescape(pair[1]);
    }
    return cookies;
}

//used on logout in ajax call in hidden frame
function clearAllCookies() {
    var cookies = document.cookie.split(";");
    for(var i=0; i < cookies.length; i++) {
        var equals = cookies[i].indexOf("=");
        var name = equals > -1 ? cookies[i].substr(0, equals) : cookies[i];
        document.cookie = name + "=;expires=Thu, 01 Jan 1970 00:00:00 GMT";
    }

}

function callAuthSuccessUrl(url, p) {
    $.ajax({
        url: url,
        method:'POST',
        data:p,
        success: function(data, statusText, xhr) {
            if(data.success) {
                window.top.postMessage( JSON.stringify({signin: 'success', 'cookies':getCookies(), 'isAjax':window.isAjax, 'data':data, 'statusText':statusText}), '*' );
                ajaxLoginSuccessHandler(data, statusText, xhr);
            } else {
                window.top.postMessage( JSON.stringify({signin: 'error', 'error':data.msg, 'isAjax':window.isAjax}), '*' );
                $('.loginMessage').html(data.msg).removeClass().addClass('alter alert-error').show();
            }
        },  error: function(xhr, ajaxOptions, thrownError) {
                window.top.postMessage( JSON.stringify({signin: 'error', 'error':xhr.responseText}), '*' );
                $('.loginMessage').html(xhr.responseText).removeClass().addClass('alter alert-error').show();
        }
    });
}

function closeHandler() {
    $('.loginMessage').html("Logging in ...").removeClass().addClass('alter alert-info').show();
    var authParams = window.mynewparams;
    $.ajax({
        url:  window.params.login.springOpenIdSecurityUrl,
        method: "POST",
        data: authParams,	
        success: function(data, statusText, xhr) {
            window.top.postMessage( JSON.stringify({signin: 'success', 'cookies':getCookies(), 'isAjax':window.isAjax, 'data':data, 'statusText':statusText }), '*' );
            ajaxLoginSuccessHandler(data, statusText, xhr);
        },
        error: function(xhr, ajaxOptions, thrownError) {
            window.top.postMessage( JSON.stringify({signin: 'error', 'error':xhr.responseText, 'isAjax':window.isAjax}), '*' );
            $('.loginMessage').html(xhr.responseText).removeClass().addClass('alter alert-error').show();
        }
    });
};

function isLoggedIn() {
    if($.cookie("BAToken")) {
        reloadLoginInfo();
        return true;
    } else {
        if($.cookie("BRToken")) {
            getNewAccessToken();
        } else {
            return false;
        }
    }    
}

function setLoginInfo(data, isAjax) {
    var date = new Date();
    date.setTime(date.getTime()+data.expires_in);
    $.cookie("BAToken", data.access_token, {path : '/', expires : date});
    console.log(date);
    $.cookie("BATokenExpires", date.getTime(), {path : '/', expires : 60});
    $.cookie("BRToken", data.refresh_token, {path : '/', expires : 60});//setting for 60 days
    if(typeof isAjax == undefined)
       isAjax = $("#ajaxLogin").is(':visible'); 
    if(isAjax == false) 
        window.location = window.params.login.authSuccessUrl;
    else {
        ajaxLoginSuccessHandler(data);
    }
}
var loginPopupWindow;
$(document).ready(function() {
   $('.fbJustConnect').click(function() {
        loginPopupWindow = window.open("https://www.facebook.com/dialog/oauth?response_type=code&client_id=115305755799166&redirect_uri=http%3A%2F%2Fapi.local.ibp.org%2Flogin%2Fcallback%3Fclient_name%3DfacebookClient&scope=user_likes%2Cuser_about_me%2Cuser_birthday%2Cuser_education_history%2Cemail%2Cuser_hometown%2Cuser_relationship_details%2Cuser_location%2Cuser_religion_politics%2Cuser_relationships%2Cuser_website%2Cuser_work_history&state=biodiv-api-state");
        /*$.ajax({
            async: true,
            crossDomain: true,
            url: "https://www.facebook.com/dialog/oauth?response_type=code&client_id=115305755799166&redirect_uri=http%3A%2F%2Fapi.local.ibp.org%2Flogin%2Fcallback%3Fclient_name%3DfacebookClient&scope=user_likes%2Cuser_about_me%2Cuser_birthday%2Cuser_education_history%2Cemail%2Cuser_hometown%2Cuser_relationship_details%2Cuser_location%2Cuser_religion_politics%2Cuser_relationships%2Cuser_website%2Cuser_work_history&state=biodiv-api-state",
            method: "GET",
            headers: {
                "cache-control": "no-cache",
            },
            type:'json',
           success : function(data) {
               console.log(data);
               var date = new Date();
               date.setTime(date.getTime()+data.expires_in);
               $.cookie("BAToken", data.access_token, {path : '/', expires : date});
               console.log(date);
                $.cookie("BATokenExpires", date.getTime(), {path : '/', expires : 60});
               $.cookie("BRToken", data.refresh_token, {path : '/', expires : 60});//setting for 60 days
               if(isAjax == false) 
                window.location = window.params.login.authSuccessUrl;
               else {
                ajaxLoginSuccessHandler(data);
               }
           }, error: function (xhr, ajaxOptions, thrownError) {
                $('.loginMessage').html(xhr.responseJSON.message).removeClass().addClass('alert alert-error').show();
                if(isAjax == true && ajaxLoginErrorCallbackFunction) {
                    ajaxLoginErrorCallbackFunction();
                    updateLoginInfo();
                }
           }
           });*/

        /*
         var clickedObject = this;
         var scope = { scope: "" };
        scope.scope = "email,user_about_me,user_location,user_hometown,user_website";

        window.fbEnsure(function() {
            FB.login(function(response) {
                if (response.status == 'connected') {
                    $.cookie("fb_login", "true", { path: '/', domain:"."+window.params.login.ibpServerCookieDomain});
                    if($(clickedObject).hasClass('ajaxForm')) {
                        $('.loginMessage').html("Logging in ...").removeClass().addClass('alter alert-info').show();
                        var p = {};
                        p['uid'] = response.authResponse.userID;
                        //p['spring-security-redirect'] = '${targetUrl}'
                        callAuthSuccessUrl( window.params.login.authSuccessUrl, p);
                   } else{
                        //var redirectTarget = "${targetUrl?'spring-security-redirect='+targetUrl:''}";
                        window.location = window.params.login.authSuccessUrl+"?uid="+response.authResponse.userID;//+'&'+redirectTarget
                    }
                } else {
                    alert("Failed to connect to Facebook");
                }
            }, scope);
        });*/
    });

   $('#loginForm, .openid-loginbox form').on('submit', function(e) {
       $('body').addClass('busy');
       e.stopPropagation();
        $('.loginMessage').html("Logging in ...").removeClass().addClass('alert alert-info').show();
       
       var isAjax = $("#ajaxLogin").is(':visible'); 
       var username = isAjax ? $($('.isSubGroup input[name="j_username"]')[0]).val() : $($('.isSubGroup input[name="j_username"]')[1]).val();
       var password = isAjax ? $($('.isSubGroup input[name="j_password"]')[0]).val() : $($('.isSubGroup input[name="j_password"]')[1]).val();
       $.ajax({
           url: window.params.login.loginUrl,
           method:'POST',
           data:{'username':username, 'password':password},
           type:'json',
           success : function(data) {
               console.log(data);
               setLoginInfo(data, isAjax);
            }, error: function (xhr, ajaxOptions, thrownError) {
                $('.loginMessage').html(xhr.responseJSON.message).removeClass().addClass('alert alert-error').show();
                if(isAjax == true && ajaxLoginErrorCallbackFunction) {
                    ajaxLoginErrorCallbackFunction();
                    updateLoginInfo();
                }
           }
       });
       /*loadBiodivLoginIframe(function() {
           var iframe = document.getElementsByName("biodiv_iframe")[0];
           if(iframe) {
            var iframeWindow = (iframe.contentWindow || iframe.contentDocument);
            iframeWindow.postMessage(JSON.stringify({'regular_login':'true', 'j_username':username,'j_password':password, isAjax:isAjax}), '*');
           }
       });*/
       return false;
   });
   /*
   $('.isSubGroup .fbJustConnect, .isSubGroup .googleConnect, .isSubGroup .yahooConnect' ).click(function(event) {
       $('body').addClass('busy');
       event.stopPropagation();
       var isAjax = $("#ajaxLogin").is(':visible'); 
       loadBiodivLoginIframe(function() {
           var iframe = document.getElementsByName("biodiv_iframe")[0];
           if(iframe) {
               var iframeWindow = (iframe.contentWindow || iframe.contentDocument);
               var className = event.currentTarget.className.split(' ')[0]+'_login';
               iframeWindow.postMessage(JSON.stringify({[className]:'true', 'isAjax':isAjax}), '*');
           }
       });
       return false;
   });
*/
   $('#logout').on('click', function() {
        clearAllCookies();
/*       $('body').addClass('busy');
       event.stopPropagation();
       var isAjax = $("#ajaxLogin").is(':visible'); 
       loadBiodivLoginIframe(function() {
           var iframe = document.getElementsByName("biodiv_iframe")[0];
           if(iframe) {
               var iframeWindow = (iframe.contentWindow || iframe.contentDocument);
               iframeWindow.postMessage(JSON.stringify({logout:'true', 'isAjax':isAjax}), '*');
           }
       }, true);
       return false;
*/
   });


}); 

//////////////////////// FB RELATED CALLS ///////////////////////
/*
window.fbInitCalls = Array();
window.fbAsyncInit = function() {	
    if (!window.facebookInitialized) { 
        FB.init({
            appId  : window.params.login.fbAppId,
            channelUrl : window.params.login.channelUrl,
            status : true,
            cookie : true,
            xfbml: true,
            oauth  : true,
            version: 'v2.8',
            logging : true
        });
        window.facebookInitialized = true;
    }
    $.each(window.fbInitCalls, function(index, fn) {
        fn();
    });
    window.fbInitCalls = [];
};

// make sure facebook is initialized before calling the facebook JS api
window.fbEnsure = function(callback) {
    if (window.facebookInitialized) { callback(); return; }

    if(!window.FB) {
        //alert("Facebook script all.js could not be loaded for some reason. Either its not available or is blocked.")
        window.fbInitCalls.push(callback);
    } else {
        window.fbAsyncInit();
        callback();
    }
};

(function(d, s, id) {
    var js, fjs = d.getElementsByTagName(s)[0];
    if (d.getElementById(id))
        return;
    js = d.createElement(s);
    js.id = id;
    js.src = "https://connect.facebook.net/en_US/sdk.js";//#xfbml=1&appId=window.params.login.fbAppId

fjs.parentNode.insertBefore(js, fjs);
}(document, 'script', 'facebook-jssdk'));
*/
////////////////////////FB RELATED CALLS END HERE ///////////////////////



function setAttribute(node, name, value) {
    var attr = document.createAttribute(name);
    attr.nodeValue = value;
    node.setAttributeNode(attr);
}

var extensions = {
    'openid.ns.ax' : 'http://openid.net/srv/ax/1.0',
    'openid.ax.mode' : 'fetch_request',
    'openid.ax.type.email' : 'http://axschema.org/contact/email',
    'openid.ax.type.first' : 'http://axschema.org/namePerson/first',
    'openid.ax.type.last' : 'http://axschema.org/namePerson/last',
    'openid.ax.type.country' : 'http://axschema.org/contact/country/home',
    'openid.ax.type.lang' : 'http://axschema.org/pref/language',
    'openid.ax.type.web' : 'http://axschema.org/contact/web/default',
    'openid.ax.required' : 'email,first,last,country,lang,web',
    'openid.ns.oauth' : 'http://specs.openid.net/extensions/oauth/1.0',
    'openid.oauth.consumer' : 'www.puffypoodles.com',
    'openid.oauth.scope' : 'http://www.google.com/m8/feeds/',
    'openid.ui.icon' : 'true'
};
/*
   var googleOpener = popupManager.createPopupOpener({
   'realm' : 'http://*.'+window.params.login.ibpServerCookieDomain,
   'opEndpoint' : 'https://www.google.com/accounts/o8/ud',
   'returnToUrl' :	"${uGroup.createLink(controller:'openId', action:'checkauth', base:Utils.getDomainServerUrl(request))}",
   'onCloseHandler' : closeHandler,
   'shouldEncodeUrls' : true,
   'extensions' : extensions
   });
*/

$(document).ready(function() {

    var yahooOpener = popupManager.createPopupOpener({
        'realm' : 'http://*.'+window.params.login.ibpServerCookieDomain,
        'opEndpoint' : 'https://open.login.yahooapis.com/openid/op/auth',
        'returnToUrl' :	window.params.login.checkauthUrl,
        'onCloseHandler' : closeHandler,
        'shouldEncodeUrls' : true,
        'extensions' : extensions
    });


    ////////////////////////GOOGLE RELATED CALLS START HERE ///////////////////////
    var apiKey = window.params.login.googleApiKey;
    var clientId = window.params.login.googleClientID;
    //var scopes = 'https://www.googleapis.com/auth/userinfo.profile https://www.googleapis.com/auth/userinfo.email'
    var scopes = 'https://www.googleapis.com/auth/plus.me';
    gapi.load('auth2', gapiInit);
    function gapiInit() {
        if(gapi.client)
            gapi.client.setApiKey(apiKey);
    }
    ////////////////////////GOOGLE RELATED CALLS END HERE ///////////////////////

    $('.googleConnect').click(function(e) { 
        //handleAuthClick(e);

        window.open("https://accounts.google.com/o/oauth2/auth?response_type=code&client_id=317806372709-roromqiujiji1po5jh8adpcr5um895mb.apps.googleusercontent.com&redirect_uri=http://api.local.ibp.org/login/callback?client_name=google2Client&access_type=offline&scope=email");
		return true; 
	});
	
/*	$('.isParentGroup .yahooConnect').click(function() { 
		yahooOpener.popup(450,500);
		return true; 
	});

    //https://developers.google.com/api-client-library/javascript/start/start-js#how-it-looks-in-javascript
    function handleAuthResult(authResult) {
        if (authResult && !authResult.error) {
            $('.loginMessage').html("Logging in ...").removeClass().addClass('alert alert-info').show();
            delete authResult['g-oauth-window'];
            var authParams = {'response': JSON.stringify(authResult).replace(/:/g,' : ')};
            callAuthSuccessUrl( window.params.login.googleOAuthSuccessUrl, authParams);
        } else {
            //authorizeButton.onclick = handleAuthClick;
            alert('Failed to connect to Google');
        }
    }
    function handleAuthClick(event) {
        gapi.auth.authorize({client_id: clientId, scope: scopes, immediate: false}, handleAuthResult);
        return false;
    }
*/
});

/*function loadBiodivLoginIframe(callback, logout=false) {
    if(!$(document.getElementsByName("biodiv_iframe")[0]).attr("src")) {
        if(logout) {
            $(document.getElementsByName("biodiv_iframe")[0]).attr("src", window.params.login.authIframeUrl+'?logout='+logout).load(function(e) {
            callback();
        });
        } else {
             $(document.getElementsByName("biodiv_iframe")[0]).attr("src", window.params.login.authIframeUrl).load(function(e) {
                callback();
            });
        }
    } else {
        callback();
    }
}*/
