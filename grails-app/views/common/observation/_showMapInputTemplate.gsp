<div class="section">
	<h3>${locationHeading}</h3>
		<div class="span6" style="margin-left:0px;">
                               <div class="map_search">
                                   <div id="geotagged_images">
                                           <div class="title" style="display: none">Use location
                                                   from geo-tagged image:</div>
                                           <div class="msg" style="display: none">Select image if
                                                   you want to use location information embedded in it</div>
                                   </div>

                                   <div id="current_location" class="section-item">
                                           <div class="location_picker_button"><a href="#" onclick="return false;">Use current location</a></div>
                                   </div>
                                   <input id="address" type="text" title="Find by place name"  class="input-block-level"
                                           class="section-item" />
                                                                         
					<div id="map_area">
                   					<div id="map_canvas"></div>
               					</div>
                	</div>
            </div>
            
	<div class="span6 sidebar-section section block" style="margin:0px; width:430px; padding-top:10px;">
	
		<div class="row control-group">
			<%
                          	def defaultAccuracy = (obvInfoFeeder?.locationAccuracy) ? obvInfoFeeder.locationAccuracy : "Approximate"
                            def isAccurateChecked = (defaultAccuracy == "Accurate")? "checked" : ""
                            def isApproxChecked = (defaultAccuracy == "Approximate")? "checked" : ""
                        %>
                         <label for="location_accuracy" class="control-label" style="padding:0px"><g:message
				code="observation.accuracy.label"
				default="Accuracy" /> </label>
				
                        <div class="controls">                
                            <input type="radio" name="location_accuracy" value="Accurate" ${isAccurateChecked} >Accurate 
                            <input type="radio" name="location_accuracy" value="Approximate" ${isApproxChecked} >Approximate<br />
                        </div>
                    </div>

                    <div class="row control-group">
                    	<label for="location_accuracy" class="control-label" style="padding:0px"><g:message
				code="observation.geoprivacy.label"
				default="Geoprivacy" /> </label>
                            
                        <div class="controls">  
           						<input type="checkbox" class="input-block-level"
                                    name="geo_privacy" value="geo_privacy" />
           						Hide precise location
                        </div>
                    </div>
                    <hr>
                    <div class="row control-group">
                    	<label for="location_accuracy" class="control-label" style="padding:0px"><g:message
				code="observation.geocode.label"
				default="Geocode name" /> </label>
			<div class="controls">                
                            <div class="location_picker_value" id="reverse_geocoded_name"></div>
                            <input id="reverse_geocoded_name_field" type="hidden"  class="input-block-level"
                                    name="reverse_geocoded_name" > </input>
                        </div>
                    </div>
                           <div><input id="use_dms" class="input-block-level" type="checkbox" name="use_dms" value="use_dms" />
                               Use deg-min-sec format for lat/long
                    </div>

                    <div class="row control-group  ${hasErrors(bean: observationInstance, field: 'latitude', 'error')}">
                    	<label for="location_accuracy" class="control-label"><g:message
				code="observation.latitude.label"
				default="Latitude" /> </label>
                        <div class="controls textbox">             
                            <!-- div class="location_picker_value" id="latitude"></div>
                            <input id="latitude_field" type="hidden" name="latitude"></input-->
                            <input class="degree_field input-block-level" style="width: 260px;" id="latitude_field" type="text" name="latitude"></input>
                            <input class="dms_field" id="latitude_deg_field" type="text" name="latitude_deg" placeholder="deg"></input>
                            <input class="dms_field" id="latitude_min_field" type="text" name="latitude_min" placeholder="min"></input>
                            <input class="dms_field" id="latitude_sec_field" type="text" name="latitude_sec" placeholder="sec"></input>
                            <input class="dms_field" id="latitude_direction_field" type="text" name="latitude_direction" placeholder="direction"></input>
                            <div class="help-inline">
						<g:hasErrors bean="${observationInstance}" field="latitude">
							<g:renderErrors bean="${observationInstance}" as="list" field="latitude"/>
						</g:hasErrors>
				</div>
                        </div>
                    </div>
                    <div class="row control-group ${hasErrors(bean: observationInstance, field: 'longitude', 'error')}">
                  	  <label for="location_accuracy" class="control-label"><g:message
				code="observation.longitude.label"
				default="Longitude" /> </label>
                        <div class="controls textbox">               
                            <!--div class="location_picker_value" id="longitude"></div>
                            <input id="longitude_field" type="hidden" name="longitude"></input-->
                            <input class="degree_field input-block-level" style="width: 260px;" id="longitude_field" type="text" name="longitude"></input>
                            <input class="dms_field" id="longitude_deg_field" type="text" name="longitude_deg" placeholder="deg"></input>
                            <input class="dms_field" id="longitude_min_field" type="text" name="longitude_min" placeholder="min"></input>
                            <input class="dms_field" id="longitude_sec_field" type="text" name="longitude_sec" placeholder="sec"></input>
                            <input class="dms_field" id="longitude_direction_field" type="text" name="longitude_direction" placeholder="direction"></input>
                             <div class="help-inline">
						<g:hasErrors bean="${observationInstance}" field="longitude">
							<g:renderErrors bean="${observationInstance}" as="list" field="longitude"/>
						</g:hasErrors>
				</div>
                        </div>
                    </div>
              
            </div>
     </div>
          
<r:script>
$(document).ready(function() {
	loadGoogleMapsAPI(function() {
    	initialize();
    	if(${obvInfoFeeder?.latitude && obvInfoFeeder?.longitude}){
    		//alert("setting location " + ${obvInfoFeeder?obvInfoFeeder.latitude:21.07} + "  " + ${obvInfoFeeder?obvInfoFeeder.longitude: 79.27});
        	set_location(${obvInfoFeeder?obvInfoFeeder.latitude:21.07}, ${obvInfoFeeder?obvInfoFeeder.longitude: 79.27});
        }
	});
});
</r:script>
          