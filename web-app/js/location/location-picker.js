  
if(!Array.prototype.last) {
    Array.prototype.last = function() {
        return this[this.length - 1];
    }
}

var geocoder;
var map;
var marker;
var latitude;
var longitude;

var swRestriction = new google.maps.LatLng('8', '69');
var neRestriction = new google.maps.LatLng('36', '98');
var allowedBounds = new google.maps.LatLngBounds(swRestriction, neRestriction);
    
function initialize(){
  var latlng = new google.maps.LatLng(21.07,79.27);

  var options = {
    zoom: 4,
    center: latlng,
    mapTypeId: google.maps.MapTypeId.HYBRID
  };
        
  map = new google.maps.Map(document.getElementById("map_canvas"), options);
        
  geocoder = new google.maps.Geocoder();
        
  marker = new google.maps.Marker({
    map: map,
    draggable: true
  });
    
  google.maps.event.addListener(map, 'dragend', function() { checkBounds(); });
        
        function checkBounds() {
             if (allowedBounds.contains(map.getCenter())) return;

             var c = map.getCenter(),
             x = c.lng(),
             y = c.lat(),
             maxX = allowedBounds.getNorthEast().lng(),
             maxY = allowedBounds.getNorthEast().lat(),
             minX = allowedBounds.getSouthWest().lng(),
             minY = allowedBounds.getSouthWest().lat();

             if (x < minX) x = minX;
             if (x > maxX) x = maxX;
             if (y < minY) y = minY;
             if (y > maxY) y = maxY;

             map.setCenter(new google.maps.LatLng(y, x));
    }
}

function set_location(lat, lng) {
	
		$(".location_picker_button").removeClass("active_location_picker_button");
        $("#latitude").html(lat.toFixed(2));
        $("#longitude").html(lng.toFixed(2));
        var location = new google.maps.LatLng(lat, lng);
        marker.setPosition(location);
        map.setCenter(location);

        geocoder.geocode({'latLng': marker.getPosition()}, function(results, status) {
            if (status == google.maps.GeocoderStatus.OK) {
                if (results[0]) {
                    //$('#place_name').val(results[0].formatted_address);
                    $('#reverse_geocoded_name').html(results[0].formatted_address);
                    $('#latitude').html(marker.getPosition().lat().toFixed(2));
                    $('#longitude').html(marker.getPosition().lng().toFixed(2));
                    $('#reverse_geocoded_name_field').val(results[0].formatted_address);
                    $('#latitude_field').val(marker.getPosition().lat());
                    $('#longitude_field').val(marker.getPosition().lng());
                }
            }
        });

}

function convert_DMS_to_DD(days, minutes, seconds, direction) {
    var dd = days + minutes/60 + seconds/(60*60);

    if (direction == "S" || direction == "W") {
        dd = dd * -1;
    } // Don't do anything for N or E
    return dd;
}

function update_geotagged_images_list() {
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
}

function update_geotagged_images_list(image) {
		$(image).exifLoad(function() {
    		var latlng = get_latlng_from_image(image); 
            if (latlng) {            	
            	var func = "set_location(" + latlng.lat+"," +latlng.lng+ "); $(this).addClass('active_location_picker_button')";
                var html = '<div id=' + $(image).attr("id") +' class="location_picker_button" style="display:inline-block; width:40px;" onclick="' + func + '"><div style="width:40px; height:40px;float:left;"><img style="width:100%; height:100%;" src="' + $(image).attr('src') + '"/></div><div style="float:left; padding:2px;font-size:"></div></div>';
                $("#geotagged_images>.title").show();
                $("#geotagged_images>.msg").show();
                $("#geotagged_images").append(html);
                $("#geotagged_images").trigger('update_map');
            }
    	})
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
            //set_location(lat, lng);
            return {lat:lat ,lng: lng}

        }
        

}


$(document).ready(function() { 
  
  $('#address').watermark('Search');

  initialize();
  //window.setTimeout(update_geotagged_images_list, 10);
    
  /*window.setTimeout(function() {
      if (latitude && longitude){
        set_location(latitude, longitude);
      } else {
        set_location(21.07, 79.27);
      } 
  }, 10);*/
  set_location(21.07, 79.27);				  
  $(function() {
    $("#address").autocomplete({
      source: function(request, response) {
        geocoder.geocode( {'address': request.term +'+india', 'region':'in'}, function(results, status) {
          response($.map(results, function(item) {
            return {
              label:  item.formatted_address,
              value: item.formatted_address,
              latitude: item.geometry.location.lat(),
              longitude: item.geometry.location.lng()
            }
          }));
        })
      },

      select: function(event, ui) {
        set_location(ui.item.latitude, ui.item.longitude);
        $('#location_info').html('You have selected this location');
      },

    focus: function(event, ui) {
        //set_location(ui.item.latitude, ui.item.longitude);
        }

    });
  });

  var lastPosition = marker.getPosition();
  //add listener to marker for reverse geocoding
  google.maps.event.addListener(marker, 'drag', function() {
    
    if(!allowedBounds.contains(marker.getPosition())){
        marker.setPosition(lastPosition);
    }else {
        lastPosition = marker.getPosition();
    };

    geocoder.geocode({'latLng': marker.getPosition()}, function(results, status) {
      if (status == google.maps.GeocoderStatus.OK) {
        if (results[0]) {
          //$('#place_name').val(results[0].formatted_address);
          $('#reverse_geocoded_name').html(results[0].formatted_address);
          $('#latitude').html(marker.getPosition().lat().toFixed(2));
          $('#longitude').html(marker.getPosition().lng().toFixed(2));
          $('#reverse_geocoded_name_field').val(results[0].formatted_address);
          $('#latitude_field').val(marker.getPosition().lat());
          $('#longitude_field').val(marker.getPosition().lng());
        }
      }
    });

    $('#location_info').html('You have selected this location');
  });
  
  function onSuccess(position) {
        var lat = position.coords.latitude;
        var lng = position.coords.longitude;
        set_location(lat, lng);
        $('#current_location').addClass('active_location_picker_button');  
        $('#location_info').html('Using auto-detected current location');
   geocoder.geocode({'latLng': marker.getPosition()}, function(results, status) {
      if (status == google.maps.GeocoderStatus.OK) {
        if (results[0]) {
          //$('#place_name').val(results[0].formatted_address);
        }
      }
    });
  }

  function onError(position) {
     if (google.loader.ClientLocation) {
      ipLocated = true;
      var lat = google.loader.ClientLocation.latitude;
      var lng = google.loader.ClientLocation.longitude;
      set_location(lat, lng);
      $('#location_info').html('Using auto-detected current location');

      geocoder.geocode({'latLng': marker.getPosition()}, function(results, status) {
      if (status == google.maps.GeocoderStatus.OK) {
        if (results[0]) {
          $('#place_name').val(results[0].formatted_address);
        }
      }
    });

    } else {
      alert("Unable to detect current location");
    }
  }

  $('#current_location').click(function() {
	
    if (navigator.geolocation) {
        /* geolocation is available */
        navigator.geolocation.getCurrentPosition(onSuccess, onError);
    } else {
      alert("Unable to detect current location");
    }
    }); 


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
            set_location(lat, lng);
        }
        
      geocoder.geocode({'latLng': marker.getPosition()}, function(results, status) {
      if (status == google.maps.GeocoderStatus.OK) {
        if (results[0]) {
          $('#place_name').val(results[0].formatted_address);
        }
      }
    });
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
    		$(this).children(":last").trigger("click");
    	}else{
    		$("#geotagged_images>.title").hide();
            $("#geotagged_images>.msg").hide();
    	}
    });
});
