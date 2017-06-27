
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

var getCookies = function(){
    var pairs = document.cookie.split(";");
    var cookies = {};
    for (var i=0; i<pairs.length; i++){
        var pair = pairs[i].split("=");
        console.log(pair);
        cookies[pair[0].trim()] = unescape(pair[1]);
    }
    return cookies;
}

function callAuthSuccessUrl(url, p) {
    $.ajax({
        url: url,
        method:'POST',
        data:p,
        success: function(data, statusText, xhr) {
            console.log(data);
            if(data.success) {
                window.top.postMessage( JSON.stringify({signin: 'success', 'cookies':getCookies(), 'isAjax':window.isAjax, 'data':data, 'statusText':statusText}), '*' );
                ajaxLoginSuccessHandler(data, statusText, xhr);
            } else {
                window.top.postMessage( JSON.stringify({signin: 'error', 'error':xhr.responseText, 'isAjax':window.isAjax}), '*' );
                $('#loginMessage').html(data.msg).removeClass().addClass('alter alert-error').show();
            }
        },  error: function(xhr, ajaxOptions, thrownError) {
                window.top.postMessage( JSON.stringify({signin: 'error', 'error':xhr.responseText}), '*' );
                $('#loginMessage').html(xhr.responseText).removeClass().addClass('alter alert-error').show();
        }
    });
}

function closeHandler() {
    $('#loginMessage').html("Logging in ...").removeClass().addClass('alter alert-info').show();
    var authParams = window.mynewparams;
    console.log(authParams);
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
            $('#loginMessage').html(xhr.responseText).removeClass().addClass('alter alert-error').show();
        }
    });
};


 
$(document).ready(function() {
   $('.isParentGroup .fbJustConnect').click(function() {
        var clickedObject = this;
        var scope = { scope: "" };
        scope.scope = "email,user_about_me,user_location,user_hometown,user_website";

        window.fbEnsure(function() {
            FB.login(function(response) {
                if (response.status == 'connected') {
                    $.cookie("fb_login", "true", { path: '/', domain:"."+window.params.login.ibpServerCookieDomain});
                    if($(clickedObject).hasClass('ajaxForm')) {
                        $('#loginMessage').html("Logging in ...").removeClass().addClass('alter alert-info').show();
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
        });
    });
    

}); 

//////////////////////// FB RELATED CALLS ///////////////////////

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

    $('.isParentGroup .googleConnect').click(function(e) { 
		//googleOpener.popup(450,500);
        handleAuthClick(e);
		return true; 
	});
	
	$('.isParentGroup .yahooConnect').click(function() { 
		yahooOpener.popup(450,500);
		return true; 
	});

    //https://developers.google.com/api-client-library/javascript/start/start-js#how-it-looks-in-javascript
    function handleAuthResult(authResult) {
        console.log(authResult);
        if (authResult && !authResult.error) {
            $('.loginMessage').html("Logging in ...").removeClass().addClass('alter alert-info').show();
            delete authResult['g-oauth-window'];
            var authParams = {'response': JSON.stringify(authResult).replace(/:/g,' : ')};
            console.log( window.params.login.googleOAuthSuccessUrl);
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

});

function loadBiodivLoginIframe(callback) {
    if(!$(document.getElementsByName("biodiv_iframe")[0]).attr("src")) {
        console.log('loading authIframe-------------------------------');
        $(document.getElementsByName("biodiv_iframe")[0]).attr("src", window.params.login.authIframeUrl).load(function(e) {
        console.log('loading authIframe callllllllllllllbaaaaaaaaaaaackkkkkkkkk-------------------------------');
            callback();
        });
    }

}
