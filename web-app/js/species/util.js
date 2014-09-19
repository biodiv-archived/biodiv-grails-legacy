/**
 * 
 */

var serverTimeDiff = null;
var alwaysRelativeTime = false;
var is_touch_device = ("ontouchstart" in window) || window.DocumentTouch && document instanceof DocumentTouch;

$(function() {
	var spt = $('span.mailme');
	var at = /\(at\)/;
	var dot = /\(dot\)/g;
	$(spt).each(function() {
		var addr = $(this).text().replace(at, "@").replace(dot, ".");
		$(this).after(
				'<a href="mailto:' + addr + '" title=window.i8ln.species.util.sem>' + addr
						+ '</a>').hover(function() {
			window.status = window.i8ln.species.util.sem;
		}, function() {
			window.status = "";
		});
		$(this).remove();	
	});
	
});

function getBackgroundPos(obj) {
	var pos = obj.css("background-position");
	if (pos == 'undefined' || pos == null) {
		pos = [obj.css("background-position-x"),obj.css("background-position-y")];//i hate IE!!
	} else {
		pos = pos.split(" ");
	}
	return {
		x: parseFloat(pos[0]),
		xUnit: pos[0].replace(/[0-9-.]/g, ""),
		y: parseFloat(pos[1]),
		yUnit: pos[1].replace(/[0-9-.]/g, "")
	};
}

// to convert http to link
function dcorateCommentBody(comp){
	//var text = $(comp).text().replace(/\n\r?/g, '<br />');
	//$(comp).html(text);
	$(comp).linkify();
}


function initRelativeTime(url){
	if(!serverTimeDiff){
		$.ajax({
	 		url: url,
			dataType: "json",
			success: function(data) {
				serverTimeDiff = parseInt(data) - new Date().getTime();
				$('body').timeago({serverTimeDiff:serverTimeDiff, alwaysRelativeTime:alwaysRelativeTime});
			}, error: function(xhr, status, error) {
				//alert(xhr.responseText);
		   	}
		});	
	}
}

function updateRelativeTime(){
	$('.timeago').timeago({serverTimeDiff:serverTimeDiff, alwaysRelativeTime:alwaysRelativeTime});
}

function feedPostProcess(){
	$(".ellipsis.multiline").trunk8({
		lines:2,
                tooltip:false,
                fill: '&hellip; <a id="read-more" href="#">more</a>'
	});
        $('#read-more').on('click', function (event) {
              $(this).parent().trunk8('revert').append(' <a id="read-less" href="#">read less</a>');
                
                return false;
        });

        $('#read-less').on('click', function (event) {
              $(this).parent().trunk8();
                
                return false;
        });
	
	$('.linktext').linkify();  
	$('.yj-message-body').linkify();
	updateRelativeTime();
        rating();
	$(".youtube_container").each(function(){
		loadYoutube(this);
	});
}

//to show relative date
//function updateRelativeTime(currentTime){
//	//toRelativeTime('.activityfeed .timestamp', currentTime);
//}
//
//function getRelativeTime(diff) {
//	  var v = Math.floor(diff / 86400); diff -= v * 86400;
//	  if (v > 0) return (v == 1 ? 'Yesterday' : v + ' days ago');
//	  v = Math.floor(diff / 3600); diff -= v * 3600;
//	  if (v > 0) return v + ' hour' + (v > 1 ? 's' : '') + ' ago';
//	  v = Math.floor(diff / 60); diff -= v * 60;
//	  if (v > 0) return v + ' minute' + (v > 1 ? 's' : '') + ' ago';
//	  return 'Just now';
//}
//
//function toRelativeTime(s, currentTime) { 
//		$(s).each(function() {
//			var t = $(this);
//			var creationTime = $(t).children('input[name="creationTime"]').val();
//			var x = Math.round(parseInt(creationTime) / 1000);
//			if (x){ 
//			  $(t).children('span').text(getRelativeTime(Math.round(parseInt(currentTime) / 1000) - x));
//			} 
//		}); 
//}

function initLoader() {
    var script = document.createElement("script");
    script.src = "https://www.google.com/jsapi?callback=loadMaps";
    script.type = "text/javascript";
    document.getElementsByTagName("head")[0].appendChild(script);
}

function loadGoogleMapsAPI(callback) {
    //if (typeof google === 'object' && typeof google.maps === 'object') {
    //    console.log("google maps already loaded")
    //} else {
        if(google != undefined) {
            console.log("loading google maps")
            google.load("maps", "3.12", {'callback':function() {
                google.maps.visualRefresh = true;
                callback();
            }, other_params: "sensor=true"});
        }
    //}
}

var isVisualizationLibLoaded = false;
function loadGoogleVisualizationAPI(callback) {
    if(!isVisualizationLibLoaded) {
        google.load('visualization', '1', {packages: ['corechart', 'table'], callback:function(){
            isVisualizationLibLoaded = true;
            callback();
        }});
    } else {
        callback();
    }
}

if (typeof String.prototype.startsWith != 'function') {
    // see below for better implementation!
    String.prototype.startsWith = function (str){
        if($.type(this) === 'string')
            return this.indexOf(str) == 0;
    };
}

function stringTrim(s){
    return $.trim(s);
}


function getParameterByName(name) {
    name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
    var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
        results = regex.exec(location.search);
    return results == null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
}
