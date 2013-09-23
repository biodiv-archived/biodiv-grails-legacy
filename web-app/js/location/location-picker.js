  
if(!Array.prototype.last) {
    Array.prototype.last = function() {
        return this[this.length - 1];
    }
}


var pointIcon, checklistIcon, geoPrivacyPointIcon, geoPrivacyChecklistIcon;
var geocoder;
var map;
var latitude;
var longitude;
var allowedBounds;
var selectedMarker;
var G, M;
var layersControl;
var overlays = {}; 
var selectedIcon;//,prevIcon;
var markers,searchMarker;
var drawnItems;
var isMapViewLoaded = false;

function initialize(element, drawable){
    console.log('initializing map');
    G = google.maps;
    M = L;
    M.Icon.Default.imagePath = window.params.defaultMarkerIcon;
    allowedBounds = new M.LatLngBounds(new M.LatLng('6.74678', '68.03215'), new M.LatLng('35.51769', '97.40238'));
    //var viewBounds = new M.LatLngBounds(new M.LatLng('8', '59'), new M.LatLng('45', '105'));
    var viewBounds = new M.LatLngBounds(new M.LatLng('8', '69'), new M.LatLng('36', '98'));
    var nagpur_latlng = new M.LatLng('21.07', '79.27');                

    var ggl = new M.Google('HYBRID');
    map = new M.Map(element, {
//        crs:L.CRS.EPSG4326,
        center:allowedBounds.getCenter(),
//        maxBounds:viewBounds,
        zoom:4,
        minZoom:4,
//       maxZoom:15,
        noWrap:true
    });
    map.addLayer(ggl).fitBounds(allowedBounds);
    layersControl = M.control.layers({'Google':ggl, 'OpenStreetMap':L.tileLayer('http://{s}.tile.osm.org/{z}/{x}/{y}.png', {
                        attribution: '&copy; <a href="http://osm.org/copyright">OpenStreetMap</a> contributors',
                                noWrap: true
                                        })}, {}, {collapsed:true}).addTo(map)
    
//    L.marker(new M.LatLng('6', '68')).addTo(map);
//    L.marker(new M.LatLng('35', '97')).addTo(map);
    initControls();
    //initLocation(drawable);
    adjustBounds();
    isMapViewLoaded=true;
    
    pointIcon = M.AwesomeMarkers.icon({
        icon: undefined, 
        color: 'blue'
    });
    checklistIcon = M.AwesomeMarkers.icon({
        icon: 'list', 
        color: 'green'
    });
    geoPrivacyPointIcon = M.AwesomeMarkers.icon({
        icon: undefined, 
        color: 'purple'
    });
    geoPrivacyChecklistIcon = M.AwesomeMarkers.icon({
        icon: 'list', 
        color: 'purple'
    });
 }

function initControls() {
    geocoder = new G.Geocoder();
//  M.control.coordinates().addTo(map);
//  M.control.locate().addTo(map);
    selectedIcon = M.AwesomeMarkers.icon({
        icon: 'ok', 
        color: 'red'
    });
    
//    markers = new M.MarkerClusterGroup();
//    markers.addTo(map);
    
    M.control.fullscreen({
          position: 'topleft',
          title: 'View fullscreen !'
    }).addTo(map);

    map.on('enterFullscreen', function(){
        if(searchMarker) {
            console.log("enterFullscreen : panning to ");
            console.log(searchMarker.getLatLng());
            map.panTo(searchMarker.getLatLng());
        }
        else resetMap()
    });

    map.on('exitFullscreen', function(){
        if(searchMarker) {
            map.panTo(searchMarker.getLatLng());
            console.log(searchMarker.getLatLng());
            console.log("enterFullscreen : panning to "+searchMarker)
        }
        else resetMap();
    });

}

function initLocation(drawable) {
    var latitude = $('#latitude_field').val();
    var longitude = $('#longitude_field').val();
    if(latitude && longitude) {
        addSearchMarker({lat:latitude, lng:longitude}, {label:'Selected Location', opacity:1, draggable:drawable, selected:drawable, clickable:drawable});
    }
}

function initArea(drawable, drawControls, editControls, areaOptions) {
    drawnItems = (editControls != undefined) ? editControls.featureGroup : new L.FeatureGroup();
    
    if(drawable) {
        if(drawControls == undefined) drawControls = {};
        
        drawControls = $.extend({}, {
            marker:true,
            circle:false,
            rectangle:false,
            polyline:false,
            polygon:false
        }, drawControls);

        var drawControl;
        if(editControls) {
            drawControl = new M.Control.Draw({
                draw:drawControls,
                edit:editControls
            });
        } else {
            drawControl = new M.Control.Draw({
                draw:drawControls
            });
        }
        drawControl.addTo(map);
        map.on('draw:drawstart', clearDrawnItems);
        map.on('draw:created', addDrawnItems);
    }
    map.addLayer(drawnItems);

    var areas = $('input#areas').val()
    if(areas) {
        drawArea(areas, drawable, drawable, drawable, areaOptions);
     }
}

function drawArea(areas, drawable, selected, clickable, areaOptions) {
    if(!areas) return;
    var wkt = new Wkt.Wkt();
    try { 
        wkt.read(areas);
    } catch (e1) {
        try {
            wkt.read(el.value.replace('\n', '').replace('\r', '').replace('\t', ''));
        } catch (e2) {
            if (e2.name === 'WKTError') {
                console.log('Wicket could not understand the WKT string you entered. Check that you have parentheses balanced, and try removing tabs and newline characters.');
                return;
            }
        }
    }
    obj = wkt.toObject(); 
/*    //TODO:For now assuming ui will restrict creation of geometry collection
    if (Wkt.isArray(obj)) { // Distinguish multigeometries (Arrays) from objects
        for (i in obj) {
            if (obj.hasOwnProperty(i) && !Wkt.isArray(obj[i])) {
                drawnItems.addLayer(obj[i]);
            }
        }
    } else {*/
    if(obj) {
        if(obj.constructor === L.Marker || obj.constructor === L.marker) {
            var latlng = obj.getLatLng();
            
            if(areaOptions == undefined) areaOptions = {};
            areaOptions = $.extend({}, {
            	draggable:drawable, 
            	layer:'Search Marker. Drag Me to set location',
            	selected:selected,
            	clickable:clickable
            	}, areaOptions);
            
            addSearchMarker(latlng, areaOptions);
        } else {
            drawnItems.addLayer(obj);
            $('input#areas').val(areas);
            map.fitBounds(obj.getBounds());                   
        }
    }
}

function adjustBounds() {
    map.on('dragend', function () {
        if (allowedBounds.contains(map.getCenter())) return;

        var c = map.getCenter(),
            x = c.lng,
            y = c.lat,
            maxX = allowedBounds.getNorthEast().lng,
            maxY = allowedBounds.getNorthEast().lat,
            minX = allowedBounds.getSouthWest().lng,
            minY = allowedBounds.getSouthWest().lat;

        if (x < minX) x = minX;
        if (x > maxX) x = maxX;
        if (y < minY) y = minY;
        if (y > maxY) y = maxY;

        map.panTo(new M.LatLng(y, x));
    });



}

function clearDrawnItems() {
    if(drawnItems) {
        drawnItems.eachLayer(function (layer) {
            map.removeLayer(layer)
        });
        drawnItems.clearLayers();
        if(searchMarker)
            map.removeLayer(searchMarker);
    }
}

function addDrawnItems(e) {
    var type = e.layerType,
        layer = e.layer;

    if (type === 'marker') {
        addSearchMarker(layer.getLatLng());
    } else {
        setLatLngFields('','');
        drawnItems.addLayer(layer);
    }
}

//TODO:remove this 
function addSearchMarker(latlng, options) {
    options = $.extend({}, {
        draggable:true, 
        selected:true, 
        clickable:true
    }, options);

    searchMarker = set_location(latlng.lat, latlng.lng, searchMarker, options);
    drawnItems.addLayer(searchMarker);
    setLatLngFields(latlng.lat, latlng.lng);
}

function addMarker(lat, lng, options) {
    var marker = createMarker(lat, lng, options)
    if(marker)
        marker.addTo(map);
    return marker;
}

function createMarker(lat, lng, options) {
    if(!lat || !lng) return;
    if(options == undefined) options = {};
    var location = new M.LatLng(lat, lng);
    
    options = $.extend({}, {
        title:options.layer?options.layer:'',
        clickable:true
    }, options);

    //var marker = new L.Draw.Marker(map, {})._mouseMarker;

    var marker = new L.marker(location, options)
    if(options.label) {
        //marker.bindLabel(options.label).showLabel();
    }

/*    if(options.layer) {
        if(!overlays[optionsa
            aayersControl.addOverlay(layerGroup, options.layer);
            overlays[options.layer] = layerGroup
        } else {
            overlays[options.layer].addLayer(marker);
        }
        //this prop is not needed inside plugin
        delete options['layer'];
    }
*/
    if(options.draggable) {
        var lastPosition = marker.getLatLng();
        marker.on("dragend", function(event) {
            if(!allowedBounds.contains(this.getLatLng())){
                marker.setLatLng(lastPosition);
            }else {
                lastPosition = marker.getLatLng();
            };
            select_location(marker);
        });
    }

    if(options.clickable) {
        marker.on('click', function() {
            if($.isFunction(options.clickable)) {
                options.clickable.call(this, options.data);
            } else {
                select_location(marker);
            }
        });
    }

    return marker;
}

function set_location(lat, lng, marker, markerOptions) {
    if(!lat || !lng) return;

    $(".location_picker_button").removeClass("active_location_picker_button");
    if(marker == undefined) {
        //Dirty HACK to draw either a marker or a polygon
        clearDrawnItems();
        marker = addMarker(lat, lng, markerOptions);
    } else {
        marker.setLatLng(new M.LatLng(lat, lng));
    }

    map.setView(marker.getLatLng(), 13).panBy([0,-60]); 

    if(markerOptions && markerOptions.selected) {
        select_location(marker);
    }
    return marker;
}

function select_location(marker) {
    if(marker == undefined) return;
    selectedMarker = marker;
    
    //centers the selectedMarker at zoom level 13
    //map.setView(selectedMarker.getLatLng(), 13);
    
    var position = selectedMarker.getLatLng();
    geocoder.geocode({'latLng': new google.maps.LatLng(position.lat, position.lng)}, function(results, status) {
    	if (status == G.GeocoderStatus.OK) {
        	if (results) {
                var content = '<ul>';
                for(var i=0; i<Math.min(results.length,2); i++) {
                    content += '<li><span>'+results[i].formatted_address+'</span> <a onclick="useLocation(this);">Use as title</a></li>'
                }
                content += '</ul>';
                selectedMarker.bindPopup(content).openPopup();

                if (results[0]) {
                    $('#placeName').val(results[0].formatted_address);
                    //$('#reverse_geocoded_name').html(results[0].formatted_address);
                    //$('#latitude').html(marker.getLatLng().lat.toFixed(2));
                    //$('#longitude').html(marker.getLatLng().lng.toFixed(2));
                    //$('#reverse_geocoded_name_field').val(results[0].formatted_address);
                }

            }
        }
    });

    setLatLngFields(marker.getLatLng().lat, marker.getLatLng().lng);
   $('#latlng').show();
}

function setLatLngFields(lat, lng) {
    $('#latitude_field').val(lat);
    $('#longitude_field').val(lng);
    var dms_lat = convert_DD_to_DMS(lat, 'lat');
    var dms_lng = convert_DD_to_DMS(lng, 'lng');
    $('#latitude_deg_field').val(dms_lat['deg']);
    $('#latitude_min_field').val(dms_lat['min']);
    $('#latitude_sec_field').val(dms_lat['sec']);
    $('#latitude_direction_field').val(dms_lat['dir']);
    $('#longitude_deg_field').val(dms_lng['deg']);
    $('#longitude_min_field').val(dms_lng['min']);
    $('#longitude_sec_field').val(dms_lng['sec']);
    $('#longitude_direction_field').val(dms_lng['dir']);
}

function set_date(date){
	$(".location_picker_button").removeClass("active_location_picker_button");
	$('#fromDate').datepicker("setDate", Date.parse(date));
}


function get_integer_part(n) {
    if (n < 0) {
        return Math.ceil(n);
    }
    
    return Math.floor(n);
}

function convert_DD_to_DMS(decimal_degree, type) {
    var dms = {};
    var deg = get_integer_part(decimal_degree);
    var decimal_min = (decimal_degree % 1) * 60;
    var min = get_integer_part(decimal_min);
    var sec = (decimal_min % 1) * 60;
    
    dms['deg'] = deg;
    dms['min'] = min;
    dms['sec'] = sec;

    if (type === 'lat' && deg < 0) {
       dms['dir'] = 'S'; 
    } else if (type === 'lat') {
       dms['dir'] = 'N';
    } else if (type === 'lng' && deg < 0) {
       dms['dir'] = 'W'; 
    } else if (type === 'lng'){
       dms['dir'] = 'E'; 
    }

    return dms;
}

function convert_DMS_to_DD(deg, minutes, seconds, direction) {
    var dd = parseInt(deg) + minutes/60 + seconds/(60*60);

    if (direction == "S" || direction == "W") {
        dd = dd * -1;

    } // Don't do anything for N or E
    return dd;
}

/*function update_geotagged_images_list() {
    var html = '';

    $('.geotagged_image').each(function() {

    	var image = $(this);
    	$(this).exifLoad(function() {
    		var latlng = get_latlng_from_image(image); 
            if (latlng) {
                var func = "set_location(" + latlng.lat+"," +latlng.lng+ ")";
                html = html + '<div class="location_picker_button" onclick="' + func + '"><div style="width:40px; height:40px;float:left;"><img style="width:100%; height:100%;" src="' + this.src + '"/></div><div style="float:left; padding:10px;">Use this geotagged image to detect location</div></div>';
            	//set_location(latlng.lat, latlng.lng);
            }
    	})
            
     });
    $('#geotagged_images').html(html);
}*/

function update_geotagged_images_list(image) {
    $(image).exifLoad(function() {
        var latlng = get_latlng_from_image(image);
        var imageDate =  $(image).exif("DateTimeOriginal")[0];
        var display = "";
        var html = "";
        var func = "";
        var inputHtml = ""
        if (latlng) {            	
            display += "Lat: " + latlng.lat.toFixed(2) + ", Lon: " + latlng.lng.toFixed(2);
            inputHtml += '<input type="hidden" name="latitudteFromImage" value="' +  latlng.lat + '"/>'
            inputHtml += '<input type="hidden" name="longitudeFromImage" value="' +  latlng.lng + '"/>'
        }


        if(imageDate){
            var date = imageDate.split(" ")[0];
            var time = imageDate.split(" ")[1];
            date = date.replace(/:/g, "-");
            if(display.length > 0){
                display += " and "  
            }
            display += $.datepicker.formatDate('dd M yy', Date.parse(date));
            inputHtml += '<input type="hidden" name="dateFromImage" value="' + date + " " + time + '"/>'
        }

        if(latlng || imageDate){
            //func += "$(this).addClass('active_location_picker_button');";
        	func += "setInfoFromImage($(this));";
            html = '<div  class="' + $(image).attr("id") +' leaflet-control location_picker_button " style="display:inline-block;" onclick="' + func + '">' + inputHtml + '<div style="width:40px; height:40px;float:left;"><img style="width:100%; height:100%;" src="' + $(image).attr('src') + '"/></div></div>';
            $("#geotagged_images>.title").show();
            $("#geotagged_images>.msg").show();
            $("#geotagged_images").append(html);
            if(latlng) {
                var iconUrl = $(image).attr('src').replace(/_th.jpg$/, '_gall_th.jpg');
                $(".leaflet-control-container .leaflet-top.leaflet-left").append(html);
            //    addMarker(latlng.lat, latlng.lng, {label:display, icon:new L.Icon({'iconUrl':iconUrl,  iconSize: [50, 50],iconAnchor: [0, 94],popupAnchor: [-3, -76], shadowUrl: window.params.defaultMarkerIcon+"marker-icon.png", shadowAnchor: [12, 44], className:'geotaggedImage'}), draggable:false, layer:'Geotagged Image'});
            }
            $("#geotagged_images").trigger('update_map');
        }    		
    });
}

function get_latlng_from_image(img) {
    var gps_lat = $(img).exif("GPSLatitude");
    var gps_lng = $(img).exif("GPSLongitude");
    var gps_lat_ref = $(img).exif("GPSLatitudeRef");
    var gps_lng_ref = $(img).exif("GPSLongitudeRef");

    var latlng;

    if (gps_lat != '' && gps_lng != ''){
        var lat_dms = gps_lat.last();
        var lng_dms = gps_lng.last();
        var lat = convert_DMS_to_DD(lat_dms[0], lat_dms[1], lat_dms[2], gps_lat_ref);
        var lng = convert_DMS_to_DD(lng_dms[0], lng_dms[1], lng_dms[2], gps_lng_ref);
        latitude = lat;
        longitude = lng;
        return {lat:lat ,lng: lng}
    }
}


function onSuccess(position) {
    var lat = position.coords.latitude;
    var lng = position.coords.longitude;
    var marker = set_location(lat, lng, undefined, {label:'Current Location', layer:'Current Location'});
    $('#current_location').addClass('active_location_picker_button');  
    $('#location_info').html('Using auto-detected current location');
}

function onError(position) {
    if (google.loader.ClientLocation) {
        ipLocated = true;
        var lat = google.loader.ClientLocation.latitude;
        var lng = google.loader.ClientLocation.longitude;
        var marker = set_location(lat, lng, undefined, {label:'Current location', layer:'Current Location'});
        $('#location_info').html('Using auto-detected current location');
    } else {
        alert("Unable to detect current location");
    }
}

function locate() {
    if (navigator.geolocation) {
        navigator.geolocation.getCurrentPosition(onSuccess, onError);
    } else {
        alert("Unable to detect current location");
    }
}

function useLocation(obj) {
    $('#placeName').val($(obj).prev().text());
}

$(document).ready(function() { 
  
  $('#placeName').watermark('Search');
 
  $(function() {

    var cacheSN = {};
    $("#placeName").catcomplete({
      appendTo:"#suggestions",
      source: function(request, response) {
          var term = request.term;
          if ( term in cacheSN ) {
              response( cacheSN[ term ] );
              return;
          }

        geocoder.geocode( {'address': request.term +'+india', 'region':'in'}, function(results, status) {
            var r = [];
            $.each(results, function(index, item) {
                if(r.length >= 5) return;
                r.push( {
                    label:  item.formatted_address,
                    value: item.formatted_address,
                    latitude: item.geometry.location.jb,
                    longitude: item.geometry.location.kb,
                    category:''
                })
            })        

            $.getJSON( window.params.locationsUrl, request, function( data, status, xhr ) {
                $.each(data, function(index, item) {
                    r.push( {
                        label: item.location[0]+' ('+item.location[1]+')',
                        value: item.location[0],
                        topology:item.topology,
                        category:item.category
                    })
                })
                response(r);
            });
            cacheSN[ term ] = r;
        })

      },

      select: function(event, ui) {
        var latitude='', longitude='';
        if(ui.item.topology) {
            drawArea(ui.item.topology, true, true, true);
            $('input#areas').val(ui.item.topology);
        } else {
            addSearchMarker({lat:ui.item.latitude, lng:ui.item.longitude}, {label:ui.item.label, draggable:true, layer:'Search Marker. Drag Me to set location', selected:true});
        }
      },

    focus: function(event, ui) {
        //set_location(ui.item.latitude, ui.item.longitude);
    },open: function(event, ui) {
        $("#suggestions ul").removeAttr('style').css({'display': 'block','width':'100%','z-index':'1001'}); 
    }


    });
  });

  $("#placeName,#latitude_field,#longitude_field").keypress(function(e) {
        code= (e.keyCode ? e.keyCode : e.which);
        if (code == 13) {
           initLocation(); 
            e.preventDefault();
        }
  });

//  $('#current_location').click(locate); 
  
  $('#image_location').click(function() {
      $(".geotagged_image").each(function() {
          var gps_lat = $(this).exif("GPSLatitude");
          var gps_lng = $(this).exif("GPSLongitude");
          var gps_lat_ref = $(this).exif("GPSLatitudeRef");
          var gps_lng_ref = $(this).exif("GPSLongitudeRef");

          if (gps_lat != '' && gps_lng != ''){
              var lat_dms = gps_lat.last();
              var lng_dms = gps_lng.last();
              var lat = convert_DMS_to_DD(lat_dms[0], lat_dms[1], lat_dms[2], gps_lat_ref);
              var lng = convert_DMS_to_DD(lng_dms[0], lng_dms[1], lng_dms[2], gps_lng_ref);
              var marker = set_location(lat, lng);

              //CHK:shd this be outside if & if this needs to be run for all images
              /*geocoder.geocode({'latLng': marker.getLatLng()}, function(results, status) {
                  if (status == G.GeocoderStatus.OK) {
                      if (results[0]) {
                          $('#place_name').val(results[0].formatted_address);
                      }
                  }
              });*/
          }
      });
  });

    /*
     $('#map_area').hover(function(){
    	$(this).animate({right: -10, top: -10}, 600);
    	$('#map_canvas').animate({height: 300, width: 300}, 600);
    }, function(){
    	$(this).animate({right: 10, top: 10}, 600);
    	$('#map_canvas').animate({height: 250, width: 250}, 600);
    });
    */
    
    $('#geotagged_images').on('update_map', function() {
        if($(this).children(".location_picker_button").length >0){
            var $geotagged_images = $(this)
            $geotagged_images.children(":last").trigger("click");
        }else{
            $("#geotagged_images>.title").hide();
            $("#geotagged_images>.msg").hide();
        }
    });

    $('#latitude_field').change(function(){
        addSearchMarker({lat:$(this).val(), lng:$('#longitude_field').val()}, {selected:true, draggable:true}); 
    });

    $('#longitude_field').change(function(){
        addSearchMarker({lat:$('#latitude_field').val(), lng:$(this).val()}, {selected:true, draggable:true});
    });
    

    function set_dms_latitude() {
            var lat = convert_DMS_to_DD($('#latitude_deg_field').val(), $('#latitude_min_field').val(), $('#latitude_sec_field').val(), $('#latitude_direction_field').val());
            addSearchMarker({'lat':lat, 'lng':$('#longitude_field').val()}, {selected:true, draggable:true});
    }
    function set_dms_longitude() {
            var lng = convert_DMS_to_DD($('#longitude_deg_field').val(), $('#longitude_min_field').val(), $('#longitude_sec_field').val(), $('#longitude_direction_field').val());
            addSearchMarker({'lat':$('#latitude_field').val(), 'lng':lng}, {selected:true, draggable:true});
    }

    $('#latitude_deg_field').change(function(){
            set_dms_latitude();
    });
    $('#latitude_min_field').change(function(){
            set_dms_latitude();
    });
    $('#latitude_sec_field').change(function(){
            set_dms_latitude();
    });
    $('#latitude_direction_field').change(function(){
            set_dms_latitude();
    });
    $('#longitude_deg_field').change(function(){
            set_dms_longitude();
    });
    $('#longitude_min_field').change(function(){
            set_dms_longitude();
    });
    $('#longitude_sec_field').change(function(){
            set_dms_longitude();
    });
    $('#longitude_direction_field').change(function(){
            set_dms_longitude();
    });

});

function getSelectedBounds() {
    var bounds = '';
    var swLat = map.getBounds().getSouthWest().lat;
    var swLng = map.getBounds().getSouthWest().lng;
    var neLat = map.getBounds().getNorthEast().lat;
    var neLng = map.getBounds().getNorthEast().lng;

    bounds = [swLat, swLng, neLat, neLng].join()
    return bounds;    
}

function resetMap(){
    map.fitBounds(allowedBounds)//.setView(allowedBounds.getCenter());
}

function setInfoFromImage(image){
	var date = $(image).children('input[name="dateFromImage"]').val();
	if(date){
		set_date(date);
	}
	
	var lat = $(image).children('input[name="latitudteFromImage"]').val();
	var lng = $(image).children('input[name="longitudeFromImage"]').val();
	if(lat && lng){
		if(isMapViewLoaded){
			addSearchMarker({lat:lat,lng:lng});
		}else{
			$(".address").trigger("click");
		}		
	}
	
	if(date || (lat && lng)){
		$(image).addClass('active_location_picker_button');
	}
}
