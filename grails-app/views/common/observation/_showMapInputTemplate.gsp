<%@ page import="species.utils.Utils"%>
<div class="control-group map_class">

    <label for="topology" class="control-label">
        <i class="icon-map-marker"></i>
        <g:message
        code="observation.topology.label" default="Observed At" /> <span class="req">*</span></label>
    <div class="controls ">
 
    <div style="margin-left:0px;">

        <div class="map_area">
            <div class="map_search">
                <div class="geotagged_images" style="display:none">
                    <div class="title" style="display: none">Use location and date
                        from geo-tagged image:</div>
                    <div class="msg" style="display: none">Select image if
                        you want to use location and date information embedded in it</div>
                </div>
                <div id="current_location" class="section-item" style="display:none">
                    <div class="location_picker_button"><a href="#" onclick="return false;">Use current location</a></div>
                </div>
                <div class="wrapperParent"  style="text-align:center;width:100%">
                    <div class="address input-append control-group ${hasErrors(bean: sourceInstance, field:placeNameField, 'error')} ${hasErrors(bean: sourceInstance, field: topologyNameField, 'error')} " style="z-index:2;margin-bottom:0px;">
                        <input class="placeName" name="placeName" type="text" title="Find by place name"  class="input-block-level" style="width:94%;"
                        class="section-item" value="${observationInstance?.placeName}"/>

                        <span class="add-on" style="vertical-align:middle;"><i class="icon-chevron-down"></i></span>
                        
                        <div class="help-inline" style="display: block;white-space:normal;font-size:14px;text-align:left;z-index:3;">
                            <g:hasErrors bean="${sourceInstance}" field="${placeNameField}">
                            <g:renderErrors bean="${sourceInstance}" as="list" field="${placeNameField}"/>
                            </g:hasErrors>
                        </div>
                        <input class='areas' type='hidden' name='areas' value='${observationInstance?.topology?Utils.GeometryAsWKT(observationInstance?.topology):params.areas}'/>

                        <div class='suggestions' class='dropdown'></div>
                    </div>
                    <div class="latlng ${hasErrors(bean: sourceInstance, field:placeNameField, 'error')}" style="display:none;">
                        <div class="input-prepend pull-left control-group ${hasErrors(bean: sourceInstance, field: topologyNameField, 'error')}">
	                        <g:if test="${params.controller != 'checklist'}">
	                            <div class="input-prepend pull-left control-group" style="width:250px;">
		                            <span class="add-on" style="vertical-align:middle;">Lat</span>
		                            <input class="degree_field latitude_field" type="text" name="latitude" value="${params.latitude}"/>
		                            <input class="dms_field latitude_deg_field" type="text" name="latitude_deg" placeholder="deg"/>
		                            <input class="dms_field latitude_min_field" type="text" name="latitude_min" placeholder="min"/>
		                            <input class="dms_field latitude_sec_field" type="text" name="latitude_sec" placeholder="sec"/>
		                            <input class="dms_field latitude_direction_field" type="text" name="latitude_direction" placeholder="N/E"/>
		                        </div>
		                        <div class="input-prepend pull-left control-group" style="width:250px;">
		                            <span class="add-on" style="vertical-align:middle;">Long</span>
		                            <input class="degree_field longitude_field" type="text" name="longitude" style="width:193px;" value="${params.longitude}"></input>
		                            <input class="dms_field longitude_deg_field" type="text" name="longitude_deg" placeholder="deg"/>
		                            <input class="dms_field longitude_min_field" type="text" name="longitude_min" placeholder="min"/>
		                            <input class="dms_field longitude_sec_field" type="text" name="longitude_sec" placeholder="sec"/>
		                            <input class="dms_field longitude_direction_field" type="text" name="longitude_direction" placeholder="N/E"/>
		                        </div>
		                        <div class="control-group">
		                            <label class="pull-left" style="text-align:center; font-weight:normal;"> <g:checkBox class="use_dms pull-left"
		                                name="use_dms" value="${use_dms}" />
		                                Use deg-min-sec </label>
		                        </div>
		                 	</g:if>
	                        <div class="help-inline" style="white-space: normal;">
	                               <g:hasErrors bean="${sourceInstance}" field="${topologyNameField}">
	                               		<g:eachError bean="${sourceInstance}" field="${topologyNameField}">
	                               			<g:message error="${it}" />
	                               		</g:eachError>
	                               </g:hasErrors>
	                    	</div>
	                    </div>
                        
                        <div class="control-group">
                                <%
                                def defaultAccuracy = (obvInfoFeeder?.locationAccuracy) ? obvInfoFeeder.locationAccuracy : "Approximate"
                                def isAccurateChecked = (defaultAccuracy == "Accurate")? "checked" : ""
                                def isApproxChecked = (defaultAccuracy == "Approximate")? "checked" : ""
								def isGeoPrivacyChecked = (observationInstance?.geoPrivacy) ? "checked" : ""
                                %>
                                <!--label for="location_accuracy" class="control-label" style="padding:0px"><g:message
                                code="observation.accuracy.label"
                                default="Accuracy" /> </label-->

                                <input type="radio" name="location_accuracy" value="Accurate" ${isAccurateChecked} />Accurate 
                                <input type="radio" name="location_accuracy" value="Approximate" ${isApproxChecked} />Approximate
                                <input type="checkbox" class="input-block-level" name="geoPrivacy" value="${observationInstance?.geoPrivacy}" onclick="$(this).val('' + $(this).prop('checked'))" ${isGeoPrivacyChecked} />
                                Hide precise location


                        </div>
                        <div class="row control-group" style="display:none;" >
                            <label for="location_accuracy" class="control-label" style="padding:0px"><g:message
                                code="observation.geocode.label"
                                default="Geocode name" /> </label>
                            <div class="controls">                
                                <div class="location_picker_value"id="reverse_geocoded_name"></div>
                                <input id="reverse_geocoded_name_field" type="hidden"  class="input-block-level"
                                    name="reverse_geocoded_name" />
                            </div>
                        </div>



                    </div>

                    <div class="map_canvas" style="display:none;">
                        <center>
                            <div class="spinner">
                                <img src="${resource(dir:'images',file:'spinner.gif', absolute:true)}"
                                alt="${message(code:'spinner.alt',default:'Loading...')}" />
                            </div>
                        </center>
                    </div>
                </div>
            </div>
        </div>
    </div>

</div>

<script type="text/javascript">
function update_geotagged_images_list_for_bulkUpload(ele){
    var imgs = $(ele).closest(".addObservation").find('.geotagged_image')
    $.each(imgs, function(index, value){
        $(ele).data('locationpicker').mapLocationPicker.update_geotagged_images_list($(value));		
    });
}

function loadMapInput() {
    //$(".address .add-on").trigger("click"); 
    var drawControls, editControls;
    var map_class = $(this).closest(".map_class");
    $(map_class).find(".map_canvas").show();
    $(map_class).find(".latlng").show();
    var me = $(map_class).find(".address .add-on");
    if($(map_class).find(".map_canvas").is(':visible')) {
        $(me).find("i").addClass("icon-remove").removeClass("icon-chevron-down");
        $(me).css("border","2px solid rgba(82,168,236,0.8)");
    }
    if($(map_class).data('locationpicker') == undefined) {
        loadGoogleMapsAPI(function() {
            var locationPicker = new $.fn.components.LocationPicker(map_class);
            locationPicker.initialize();
            $(map_class).data('locationpicker', locationPicker);
            $(map_class).find('.spinner').hide();
            
            <g:if test="${params.controller == 'checklist'}">
                drawControls = {
                    rectangle:true,
                    polygon:true,
                    polyline:true,
                    marker:false
                }

                editControls = {featureGroup: new L.FeatureGroup()}
            </g:if>
            locationPicker.initArea(drawControls, editControls, undefined);
            update_geotagged_images_list_for_bulkUpload(map_class);
        });
    
    }else {
        $(map_class).data('locationpicker').mapLocationPicker.addSearchMarker({lat:$(map_class).find('.latitude_field').val(), lng:$(map_class).find('.longitude_field').val()}, {selected:true, draggable:true});
        update_geotagged_images_list_for_bulkUpload(map_class);
    }
}

$(document).ready(function() {
    $(".address .add-on").unbind('click').click(function(){
        var me = this;
        var map_class = $(this).closest(".map_class");
        if($(map_class).find(".map_canvas").is(':visible')) {
            $(me).find("i").removeClass("icon-remove").addClass("icon-chevron-down");
            $(me).css("border","0px solid rgba(82,168,236,0.8)");
            $(map_class).find(".map_canvas").hide();
            $(map_class).find(".latlng").hide();
            return false;
        } else{
            $(me).css("border","2px solid rgba(82,168,236,0.8)");
            $(me).find("i").removeClass("icon-chevron-down").addClass("icon-remove");
        }

    });
    $(".address").unbind('click').click(loadMapInput);

});
</script>
</div>
