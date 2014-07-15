  
if(!Array.prototype.last) {
    Array.prototype.last = function() {
        return this[this.length - 1];
    }
}


/**
  @class Map Location Picker
 **/
(function ($) {
    "use strict";
    
   
    var MapLocationPicker = function (ele, options) {
        this.overlays = {}; 
        this.$ele = $(ele);
        this.initialize(options);
    }

    MapLocationPicker.prototype = {

        initialize : function(options) {
            console.log('initializing map');
            console.log(this.$ele.context);
            var G = google.maps;
            this.M= L;
            this.M.Icon.Default.imagePath = window.params.defaultMarkerIcon;
            this.allowedBounds = new this.M.LatLngBounds(new this.M.LatLng('6.74678', '68.03215'), new this.M.LatLng('35.51769', '97.40238'));
            //var viewBounds = new this.M.LatLngBounds(new this.M.LatLng('8', '59'), new this.M.LatLng('45', '105'));
            var viewBounds = new this.M.LatLngBounds(new this.M.LatLng('8', '69'), new this.M.LatLng('36', '98'));
            var nagpur_latlng = new this.M.LatLng('21.07', '79.27');                

            var ggl = new this.M.Google('HYBRID');
            this.map = new this.M.Map(this.$ele.context, {
                //        crs:L.CRS.EPSG4326,
                center: this.allowedBounds.getCenter(),
                //        maxBounds:viewBounds,
                zoom:4,
                minZoom:4,
                //       maxZoom:15,
                noWrap:true
            });
            this.map.addLayer(ggl).fitBounds( this.allowedBounds);
            //var layersControl = 
            this.M.control.layers({'Google':ggl, 'OpenStreetMap':L.tileLayer('http://{s}.tile.osm.org/{z}/{x}/{y}.png', {
                        attribution: '&copy; <a href="http://osm.org/copyright">OpenStreetMap</a> contributors',
                        noWrap: true
                        })}, {}, {collapsed:true}).addTo( this.map)

            //    this.M.marker(new this.M.LatLng('6', '68')).addTo(map);
            //    this.M.marker(new this.M.LatLng('35', '97')).addTo(map);
            this.initControls();
            this.adjustBounds();
            this.isMapViewLoaded=true;

            this.pointIcon = this.M.AwesomeMarkers.icon({
                icon: undefined, 
                color: 'blue'
            });
            this.checklistIcon = this.M.AwesomeMarkers.icon({
                icon: 'list', 
                color: 'green'
            });
            this.geoPrivacyPointIcon = this.M.AwesomeMarkers.icon({
                icon: undefined, 
                color: 'purple'
            });
            this.geoPrivacyChecklistIcon = this.M.AwesomeMarkers.icon({
                icon: 'list', 
                color: 'purple'
            });
        },

        initControls : function() {
           this.geocoder = new google.maps.Geocoder();
            //  this.M.control.coordinates().addTo(map);
            //  this.M.control.locate().addTo(map);
            this.selectedIcon = this.M.AwesomeMarkers.icon({
                icon: 'ok', 
                color: 'red'
            });

            //    markers = new this.M.MarkerClusterGroup();
            //    markers.addTo(map);

            this.M.control.fullscreen({
                position: 'topleft',
                title: 'View fullscreen !'
            }).addTo(this.map);

            this.map.on('enterFullscreen', function(){
                /*        if(searchMarker) {
                          console.log("enterFullscreen : panning to ");
                          console.log(searchMarker.getLatLng());
                //            this.map.panTo(searchMarker.getLatLng());
                }
                else resetMap()
                */    });

                this.map.on('exitFullscreen', function(){
                    /*        
                              if(searchMarker) {
                              this.map.panTo(searchMarker.getLatLng());
                              console.log("exitFullscreen : panning to ")
                              console.log(searchMarker.getLatLng());
                              }
                              else resetMap();
                              */    
                });

        }, 
        initLocation : function(drawable) {
            var me = this;
            console.log("CALLED");
            var map_class = me.$ele.closest(".map_class");
            console.log(map_class);
            var latitude = $(map_class).find('.latitude_field').val();
            var longitude = $(map_class).find('.longitude_field').val();
            console.log(latitude);
            console.log(longitude);
            if(latitude && longitude) {
                console.log("BOTH PRESNET");
                me.addSearchMarker({lat:latitude, lng:longitude}, {label:'Selected Location', opacity:1, draggable:drawable, selected:drawable, clickable:drawable});
            }
        },

        initArea : function(drawable, drawControls, editControls, areas, areaOptions) {
            console.log("======INIT AREA====");
            this.drawnItems = (editControls != undefined) ? editControls.featureGroup : new this.M.FeatureGroup();

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
                    drawControl = new this.M.Control.Draw({
                        draw:drawControls,
                        edit:editControls
                    });
                } else {
                    drawControl = new this.M.Control.Draw({
                        draw:drawControls
                    });
                }
                drawControl.addTo( this.map);
                var me = this;
                me.map.on('draw:drawstart', me.clearDrawnItems);
                me.map.on('draw:created', $.proxy(me.addDrawnItems, me));


//                this.map.on('draw:drawstart', this.clearDrawnItems);
  //              this.map.on('draw:created', this.addDrawnItems);

            }
            this.map.addLayer(this.drawnItems);
            if(areas) {
                this.drawArea(areas, drawable, drawable, drawable, areaOptions);
            }
        },

        drawArea : function(areas, drawable, selected, clickable, areaOptions) {
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
            var obj = wkt.toObject(); 
            /*    //TODO:For now assuming ui will restrict creation of geometry collection
                  if (Wkt.isArray(obj)) { // Distinguish multigeometries (Arrays) from objects
                  for (i in obj) {
                  if (obj.hasOwnProperty(i) && !Wkt.isArray(obj[i])) {
                  drawnItems.addLayer(obj[i]);
                  }
                  }
                  } else {*/
            if(obj) {
                if(obj.constructor === this.M.Marker || obj.constructor === this.M.marker) {
                    var latlng = obj.getLatLng();

                    if(areaOptions == undefined) areaOptions = {};
                    areaOptions = $.extend({}, {
                        draggable:drawable, 
                        layer:'Search Marker. Drag Me to set location',
                        selected:selected,
                        clickable:clickable
                    }, areaOptions);

                    this.addSearchMarker(latlng, areaOptions);
                } else {
                    this.drawnItems.addLayer(obj);
                    $(this.$ele).find('input.areas').val(areas);
                    this.map.fitBounds(obj.getBounds());                   
                }
            }
        }, 

        adjustBounds : function() {
            var me = this;
            this.map.on('dragend', function () {
                if (me.allowedBounds.contains(me.map.getCenter())) return;

                var c = me.map.getCenter(),
                x = c.lng,
                y = c.lat,
                maxX =  me.allowedBounds.getNorthEast().lng,
                maxY =  me.allowedBounds.getNorthEast().lat,
                minX =  me.allowedBounds.getSouthWest().lng,
                minY =  me.allowedBounds.getSouthWest().lat;

                if (x < minX) x = minX;
                if (x > maxX) x = maxX;
                if (y < minY) y = minY;
                if (y > maxY) y = maxY;

                me.map.panTo(new me.M.LatLng(y, x));
            });
        }, 

        clearDrawnItems : function() {
            var me = this;
            console.log('clear drawn items');
            if(me.drawnItems) {
                me.drawnItems.eachLayer(function (layer) {
                    me.map.removeLayer(layer)
                });
                me.drawnItems.clearLayers();
                if(me.searchMarker)
                    me.map.removeLayer(me.searchMarker);
            }
        },

        addDrawnItems:function(e) {
            console.log("add Drawn Items");
            console.log(this);
            var me = this;
            var type = e.layerType;
            var layer = e.layer;
//            var ele = event.data.ele;
            if (type === 'marker') {
                me.addSearchMarker(layer.getLatLng(),undefined);
            } else {
                me.setLatLngFields('','');
                me.drawnItems.addLayer(layer);
            }
        },

        //TODO:remove this 
        addSearchMarker : function(latlng, options) {
            console.log("called add search marker");
            options = $.extend({}, {
                draggable:true, 
                    selected:true, 
                    clickable:true
            }, options);

            this.searchMarker = this.set_location(latlng.lat, latlng.lng, this.searchMarker, options);
            this.drawnItems.addLayer(this.searchMarker);
            this.setLatLngFields(latlng.lat, latlng.lng);
        },

        addMarker : function(lat, lng, options) {
            var marker = this.createMarker(lat, lng, options)
                if(marker)
                    marker.addTo( this.map);
            return marker;
        },

        createMarker : function(lat, lng, options) {
            console.log("=========CRAETE MARKER=======================")
            if(!lat || !lng) return;
            if(options == undefined) options = {};

            var me = this;
            var location = new me.M.LatLng(lat, lng);

            options = $.extend({}, {
                title:options.layer?options.layer:'',
                    clickable:true
            }, options);

            //var marker = new L.Draw.Marker(map, {})._mouseMarker;

            var marker = new me.M.marker(location, options)
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
                    if(!me.allowedBounds.contains(this.getLatLng())){
                        marker.setLatLng(lastPosition);
                    }else {
                        lastPosition = marker.getLatLng();
                    };
                    me.select_location(marker);
                });
            }

            if(options.clickable) {
                marker.on('click', function() {
                    if($.isFunction(options.clickable)) {
                        options.clickable.call(me, options.data);
                    } else {
                        me.select_location(marker);
                    }
                });
            }

            return marker;
        },

        set_location : function(lat, lng, marker, markerOptions) {
            if(!lat || !lng) return;

            $(this.$ele).closest(".map_class").find(".location_picker_button").removeClass("active_location_picker_button");
            if(marker == undefined) {
                //Dirty HACK to draw either a marker or a polygon
                this.clearDrawnItems();
                marker = this.addMarker(lat, lng, markerOptions);
            } else {
                marker.setLatLng(new this.M.LatLng(lat, lng));
            }

            this.map.setView(marker.getLatLng(), 13).panBy([0,-60]); 

            if(markerOptions && markerOptions.selected) {
                this.select_location(marker);
            }
            return marker;
        },

        select_location : function(marker) {
            var me = this;
            if(marker == undefined) return;
            me.selectedMarker = marker;

            //centers the selectedMarker at zoom level 13
            //map.setView(selectedMarker.getLatLng(), 13);

            var position = me.selectedMarker.getLatLng();
            me.geocoder.geocode({'latLng': new google.maps.LatLng(position.lat, position.lng)}, function(results, status) {
                if (status == google.maps.GeocoderStatus.OK) {
                    if (results) {
                        var content = '<ul>';
                        for(var i=0; i<Math.min(results.length,2); i++) {
                            content += '<li><span>'+results[i].formatted_address+'</span> <a onclick="useLocation(this);">Use as title</a></li>'
                        }
                        content += '</ul>';
                        me.selectedMarker.bindPopup(content).openPopup();

                        if (results[0]) {
                            //$('#placeName').val(results[0].formatted_address);
                            //$('#reverse_geocoded_name').html(results[0].formatted_address);
                            //$('#latitude').html(marker.getLatLng().lat.toFixed(2));
                            //$('#longitude').html(marker.getLatLng().lng.toFixed(2));
                            //$('#reverse_geocoded_name_field').val(results[0].formatted_address);
                        }

                    }
                }
            });
            console.log("calling this ");
            me.setLatLngFields(marker.getLatLng().lat, marker.getLatLng().lng);
            $(this.$ele).closest(".map_class").find('.latlng').show();
        },

        setLatLngFields : function(lat, lng) {
            var map_class = $(this.$ele).closest(".map_class");
            $(map_class).find('.latitude_field').val(lat);
            $(map_class).find('.longitude_field').val(lng);
            var dms_lat = this.convert_DD_to_DMS(lat, 'lat');
            var dms_lng = this.convert_DD_to_DMS(lng, 'lng');
            $(map_class).find('.latitude_deg_field').val(dms_lat['deg']);
            $(map_class).find('.latitude_min_field').val(dms_lat['min']);
            $(map_class).find('.latitude_sec_field').val(dms_lat['sec']);
            $(map_class).find('.latitude_direction_field').val(dms_lat['dir']);
            $(map_class).find('.longitude_deg_field').val(dms_lng['deg']);
            $(map_class).find('.longitude_min_field').val(dms_lng['min']);
            $(map_class).find('.longitude_sec_field').val(dms_lng['sec']);
            $(map_class).find('.longitude_direction_field').val(dms_lng['dir']);
        },

        set_date : function(date) {
            $(this.$ele).closest(".map_class").find(".location_picker_button").removeClass("active_location_picker_button");
            $(this.$ele).closest(".addObservation").find('.fromDate').datepicker("setDate", Date.parse(date));
        },


        get_integer_part : function(n) {
            if (n < 0) {
                return Math.ceil(n);
            }

            return Math.floor(n);
        },

        convert_DD_to_DMS : function(decimal_degree, type) {
            var dms = {};
            var deg = this.get_integer_part(decimal_degree);
            var decimal_min = (decimal_degree % 1) * 60;
            var min = this.get_integer_part(decimal_min);
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
        },

        convert_DMS_to_DD : function(deg, minutes, seconds, direction) {
            var dd = parseInt(deg) + minutes/60 + seconds/(60*60);

            if (direction == "S" || direction == "W") {
                dd = dd * -1;

            } // Don't do anything for N or E
            return dd;
        },

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

        update_geotagged_images_list : function(image) {
            console.log(image);
            var me = this;
            $(image).exifLoad(function() {
                var latlng = me.get_latlng_from_image(image);
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
                //func += "setInfoFromImage($(this));";
                html = '<div  class="' + $(image).attr("id") +' leaflet-control location_picker_button " style="display:inline-block;">' + inputHtml + '<div style="width:40px; height:40px;float:left;"><img style="width:100%; height:100%;" src="' + $(image).attr('src') + '"/></div></div>';
                var $closestAddObservation = $(image).closest(".addObservation");
                
                $closestAddObservation.find(".geotagged_images>.title").show();
                $closestAddObservation.find(".geotagged_images>.msg").show();
                $closestAddObservation.find(".geotagged_images").append(html);
                if(latlng) {
                    var iconUrl = $(image).attr('src').replace(/_th.jpg$/, '_gall_th.jpg');
                    $closestAddObservation.find(".leaflet-control-container .leaflet-top.leaflet-left").append(html);
                    //    this.addMarker(latlng.lat, latlng.lng, {label:display, icon:new L.Icon({'iconUrl':iconUrl,  iconSize: [50, 50],iconAnchor: [0, 94],popupAnchor: [-3, -76], shadowUrl: window.params.defaultMarkerIcon+"marker-icon.png", shadowAnchor: [12, 44], className:'geotaggedImage'}), draggable:false, layer:'Geotagged Image'});
                }
                console.log("printing image==========");
                console.log(image);
                var appendedImage = $closestAddObservation.find(".leaflet-control-container .leaflet-top.leaflet-left")
                $closestAddObservation.find(".geotagged_images").find(".location_picker_button").click(me.setInfoFromImage(appendedImage)).trigger('update_map');
            }    		
            });
        },

        get_latlng_from_image : function(img) {
            var gps_lat = $(img).exif("GPSLatitude");
            var gps_lng = $(img).exif("GPSLongitude");
            var gps_lat_ref = $(img).exif("GPSLatitudeRef");
            var gps_lng_ref = $(img).exif("GPSLongitudeRef");

            var latlng;

            if (gps_lat != '' && gps_lng != ''){
                var lat_dms = gps_lat.last();
                var lng_dms = gps_lng.last();
                var lat = this.convert_DMS_to_DD(lat_dms[0], lat_dms[1], lat_dms[2], gps_lat_ref);
                var lng = this.convert_DMS_to_DD(lng_dms[0], lng_dms[1], lng_dms[2], gps_lng_ref);
                var latitude = lat;
                var longitude = lng;
                return {lat:lat ,lng: lng}
            }
        },

        onSuccess : function(position) {
            var lat = position.coords.latitude;
            var lng = position.coords.longitude;
            var marker = this.set_location(lat, lng, undefined, {label:'Current Location', layer:'Current Location'});
            $('#current_location').addClass('active_location_picker_button');  
            $('#location_info').html('Using auto-detected current location');
        },

        onError : function(position) {
            if (google.loader.ClientLocation) {
                ipLocated = true;
                var lat = google.loader.ClientLocation.latitude;
                var lng = google.loader.ClientLocation.longitude;
                var marker = this.set_location(lat, lng, undefined, {label:'Current location', layer:'Current Location'});
                $('#location_info').html('Using auto-detected current location');
            } else {
                alert("Unable to detect current location");
            }
        },

        locate : function() {
            if (navigator.geolocation) {
                navigator.geolocation.getCurrentPosition(onSuccess, onError);
            } else {
                alert("Unable to detect current location");
            }
        },

        useLocation : function(obj) {
            $(obj).closest(".map_class").find('.placeName').val($(obj).prev().text());
        },


        getSelectedBounds : function() {
            var bounds = '';
            var swLat = this.map.getBounds().getSouthWest().lat;
            var swLng = this.map.getBounds().getSouthWest().lng;
            var neLat = this.map.getBounds().getNorthEast().lat;
            var neLng = this.map.getBounds().getNorthEast().lng;

            bounds = [swLat, swLng, neLat, neLng].join()
                return bounds;    
        },

        resetMap : function(){
            this.map.fitBounds( this.allowedBounds)//.setView( this.allowedBounds.getCenter());
        },

        setInfoFromImage : function(image){
            var me = this;
            console.log("SETTING INFO FROM IMAGE first");
            console.log(image)
            var map_class = $(image).closest(".map_class");
            console.log($(image).find('input[name="dateFromImage"]'));
            var date = $(image).find('input[name="dateFromImage"]').val();
            console.log(date);
            if(date){
                me.set_date(date, image);
            }

            var lat = $(image).find('input[name="latitudteFromImage"]').val();
            var lng = $(image).find('input[name="longitudeFromImage"]').val();
            if(lat && lng){
                console.log("LAT LONG PRESENT ");
                if(me.isMapViewLoaded){
                    console.log("mapview is loaded");
                    me.addSearchMarker({lat:lat,lng:lng},undefined);
                }else{
                    $(".address").trigger("click");
                }		
            }

            if(date || (lat && lng)){
                $(image).addClass('active_location_picker_button');
            }
        }

    } // end of prototype


    //making object visible outside
    $.fn.components.MapLocationPicker = MapLocationPicker;

}(window.jQuery)); 

(function ($) {
    "use strict";


    var LocationPicker = function (ele) {
        this.$ele = $(ele);
        this.mapLocationPicker = new $.fn.components.MapLocationPicker(this.$ele.find(".map_canvas")[0]);
    }

    LocationPicker.prototype = {
        initialize : function() {
            var me = this;
            console.log("==========================================");
            console.log(this.$ele.find(".placeName"));
            var temp =  me.$ele.find(".address .add-on");
            me.$ele.find(".placeName").click(function(){
                console.log("hewfhrsfsfewsf============================sedf");
                $("#suggestions").remove();
                var placeName = this
                $(temp).after("<div id='suggestions' class='dropdown'></div>");
                $("#suggestions ul").addClass("dropdown-menu");
                var cacheSN = {};
                $(placeName).catcomplete({
                    appendTo:"#suggestions",
                    source: function(request, response) {
                        console.log("===============IN SOURCE=====");
                        var term = request.term;
                        if ( term in cacheSN ) {
                            response( cacheSN[ term ] );
                            return;
                        }
                        console.log(me.mapLocationPicker);
                        me.mapLocationPicker.geocoder.geocode( {'address': request.term +'+india', 'region':'in'}, function(results, status) {
                            console.log("in here============");
                            var r = [];
                            $.each(results, function(index, item) {
                                if(r.length >= 5) return;
                                r.push( {
                                    label:  item.formatted_address,
                                    value: item.formatted_address,
                                    latitude: item.geometry.location.lat(),
                                    longitude: item.geometry.location.lng(),
                                    category:''
                                })
                            })        
                            
                            $.getJSON( window.params.locationsUrl, request, function( data, status, xhr ) {
                                console.log("==========" + window.params.locationsUrl);
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
                            me.mapLocationPicker.drawArea(ui.item.topology, true, true, true);
                            me.$ele.closest(".map_class").find('input.areas').val(ui.item.topology);
                        } else {
                            me.mapLocationPicker.addSearchMarker({lat:ui.item.latitude, lng:ui.item.longitude}, {label:ui.item.label, draggable:true, layer:'Search Marker. Drag Me to set location', selected:true});
                        }
                    },

                    focus: function(event, ui) {
                        //this.mapLocationPicker.set_location(ui.item.latitude, ui.item.longitude);
                    },open: function(event, ui) {
                        $("#suggestions ul").removeAttr('style').css({'display': 'block','width':'100%','z-index':'1001'}); 
                    }


                });
            });


            me.$ele.find(".placeName,.latitude_field,.longitude_field").keypress(function(e) {
                var code = (e.keyCode ? e.keyCode : e.which);
                if (code == 13) {
                    console.log("++++++++++++" + code);
                    me.mapLocationPicker.initLocation(undefined); 
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

        me.$ele.find('.geotagged_images').on('update_map', function() {
            console.log("triggered first");
            var $geotagged_images = $(this)
            if($geotagged_images.children(".location_picker_button").length >0){
                console.log("gdsssssssssssss");
                $geotagged_images.children(":last").trigger("click");
            }else{
                console.log("gggggggggggggggggggggggggggg");
                $geotagged_images.find(".title").hide();
                $geotagged_images.find(".msg").hide();
            }
        });

        me.$ele.find('.latitude_field').change(function(){
            me.mapLocationPicker.addSearchMarker({lat:$(this).val(), lng:$(this).closest('.map_class').find('.longitude_field').val()}, {selected:true, draggable:true}); 
        });

        me.$ele.find('.longitude_field').change(function(){
            me.mapLocationPicker.addSearchMarker({lat:$(this).closest('.map_class').find('.latitude_field').val(), lng:$(this).val()}, {selected:true, draggable:true});
        });


        function set_dms_latitude(ele) {
            var map_class = $(ele).closest('.map_class');
            var lat = me.mapLocationPicker.convert_DMS_to_DD($(map_class).find('.latitude_deg_field').val(), $(map_class).find('.latitude_min_field').val(), $(map_class).find('.latitude_sec_field').val(), $(map_class).find('.latitude_direction_field').val());
            me.mapLocationPicker.addSearchMarker({'lat':lat, 'lng':$(map_class).find('.longitude_field').val()}, {selected:true, draggable:true});
        }
        function set_dms_longitude(ele) {
            var lng = me.mapLocationPicker.convert_DMS_to_DD($(map_class).find('.longitude_deg_field').val(), $(map_class).find('.longitude_min_field').val(), $(map_class).find('.longitude_sec_field').val(), $(map_class).find('.longitude_direction_field').val());
            me.mapLocationPicker.addSearchMarker({'lat':$(map_class).find('.latitude_field').val(), 'lng':lng}, {selected:true, draggable:true});
        }

        me.$ele.find('.latitude_deg_field').change(function(){
            set_dms_latitude(this);
        });
        me.$ele.find('.latitude_min_field').change(function(){
            set_dms_latitude(this);
        });
        me.$ele.find('.latitude_sec_field').change(function(){
            set_dms_latitude(this);
        });
        me.$ele.find('.latitude_direction_field').change(function(){
            set_dms_latitude(this);
        });
        me.$ele.find('.longitude_deg_field').change(function(){
            set_dms_longitude(this);
        });
        me.$ele.find('.longitude_min_field').change(function(){
            set_dms_longitude(this);
        });
        me.$ele.find('.longitude_sec_field').change(function(){
            set_dms_longitude(this);
        });
        me.$ele.find('.longitude_direction_field').change(function(){
            set_dms_longitude(this);
        });
        me.$ele.find('.geotagged_image').each(function(index){
            locationPicker.mapLocationPicker.update_geotagged_images_list($(this));		
        });

        },
        initArea : function(drawControls, editControls, areaOptions) {
            var areas = $(this.$ele).find('input.areas').val();
            if(!areas) {
                console.log("no area");
                console.log($('input#areas').val());
                areas = $('input#areas').val();
            }
            this.mapLocationPicker.initArea(true, drawControls, editControls, areas, areaOptions);
        }

    } // end of prototype


    //making object visible outside
    $.fn.components.LocationPicker = LocationPicker;

}(window.jQuery)); 

$(document).ready(function() { 
  
  $('.placeName').watermark('Search');
 
  $(function() {

  });
});

