<%@ page import="species.utils.Utils"%>
<div
    class="row control-group">

    <label for="topology" class="control-label">
        <i class="icon-map-marker"></i>
        <g:message
        code="observation.topology.label" default="Observed At" /> </label>
    <div class="controls ">
 
    <div style="margin-left:0px;">

        <div id="map_area">
            <div class="map_search">
                <div id="geotagged_images" style="display:none">
                    <div class="title" style="display: none">Use location and date
                        from geo-tagged image:</div>
                    <div class="msg" style="display: none">Select image if
                        you want to use location and date information embedded in it</div>
                </div>
                <div id="current_location" class="section-item" style="display:none">
                    <div class="location_picker_button"><a href="#" onclick="return false;">Use current location</a></div>
                </div>
                <div  style="position:relative; text-align:center;width:100%">
                    <div class="address input-append control-group ${hasErrors(bean: observationInstance, field: 'placeName', 'error')} ${hasErrors(bean: observationInstance, field: 'topology', 'error')} " style="z-index:3;margin-bottom:0px;">
                        <input id="placeName" name="placeName" type="text" title="Find by place name"  class="input-block-level" style="width:96%;"
                        class="section-item" value="${observationInstance?.placeName}"/>
                        <span class="add-on" style="vertical-align:middle;"><i class="icon-chevron-down"></i></span>
                        <div id="suggestions" style="display: block;white-space:normal;font-size:14px;text-align:left;z-index:3;"></div>
                        <div class="help-inline" style="display: block;white-space:normal;font-size:14px;text-align:left;z-index:3;">
                            <g:hasErrors bean="${observationInstance}" field="placeName">
                            <g:renderErrors bean="${observationInstance}" as="list" field="placeName"/>
                            </g:hasErrors>
                        </div>
                        <input id='areas' type='hidden' name='areas' value='${observationInstance?.topology?Utils.GeometryAsWKT(observationInstance?.topology):params.areas}'/>


                    </div>
                    <div id="latlng" class="${hasErrors(bean: observationInstance, field: 'placeName', 'error')}" style="display:none;">
                        <g:if test="${params.controller != 'checklist'}">
                        <div class="input-prepend pull-left control-group  ${hasErrors(bean: observationInstance, field: 'topology', 'error')}" style="width:250px;">
                            <span class="add-on" style="vertical-align:middle;">Lat</span>
                            <input class="degree_field" id="latitude_field" type="text" name="latitude" value="${params.latitude}"/>
                            <input class="dms_field" id="latitude_deg_field" type="text" name="latitude_deg" placeholder="deg"/>
                            <input class="dms_field" id="latitude_min_field" type="text" name="latitude_min" placeholder="min"/>
                            <input class="dms_field" id="latitude_sec_field" type="text" name="latitude_sec" placeholder="sec"/>
                            <input class="dms_field" id="latitude_direction_field" type="text" name="latitude_direction" placeholder="N/E"/>
                            <div class="help-inline">
                                <g:hasErrors bean="${observationInstance}" field="topology">
                                <g:message code="observation.suggest.location" />
                                </g:hasErrors>
                            </div>
                        </div>
                        <div class="input-prepend pull-left control-group ${hasErrors(bean: observationInstance, field: 'topology', 'error')}" style="width:240px;">
                            <span class="add-on" style="vertical-align:middle;">Long</span>
                            <input class="degree_field" id="longitude_field" type="text" name="longitude" style="width:193px;" value="${params.longitude}"></input>
                            <input class="dms_field" id="longitude_deg_field" type="text" name="longitude_deg" placeholder="deg"/>
                            <input class="dms_field" id="longitude_min_field" type="text" name="longitude_min" placeholder="min"/>
                            <input class="dms_field" id="longitude_sec_field" type="text" name="longitude_sec" placeholder="sec"/>
                            <input class="dms_field" id="longitude_direction_field" type="text" name="longitude_direction" placeholder="N/E"/>
                            <div class="help-inline">
                                <g:hasErrors bean="${observationInstance}" field="topology">
                                </g:hasErrors>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="pull-left" style="text-align:center; font-weight:normal;"> <g:checkBox id="use_dms" class="pull-left"
                                name="use_dms" value="${use_dms}" />
                                Use deg-min-sec </label>
                        </div>
                        </g:if>

                        <div class="control-group">

                                <%
                                def defaultAccuracy = (obvInfoFeeder?.locationAccuracy) ? obvInfoFeeder.locationAccuracy : "Approximate"
                                def isAccurateChecked = (defaultAccuracy == "Accurate")? "checked" : ""
                                def isApproxChecked = (defaultAccuracy == "Approximate")? "checked" : ""
                                %>
                                <!--label for="location_accuracy" class="control-label" style="padding:0px"><g:message
                                code="observation.accuracy.label"
                                default="Accuracy" /> </label-->

                                <input type="radio" name="location_accuracy" value="Accurate" ${isAccurateChecked} />Accurate 
                                <input type="radio" name="location_accuracy" value="Approximate" ${isApproxChecked} />Approximate
                                <input type="checkbox" class="input-block-level" name="geo_privacy" value="geo_privacy" />
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

                    <div id="map_canvas" style="display:none;">
                        <center>
                            <div id="spinner" class="spinner">
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

<r:script>
function loadMapInput() {
console.log('loadMapInput');
    $("#map_canvas").show();
    $("#latlng").show();
    if(!isMapViewLoaded) {
        loadGoogleMapsAPI(function() {
            initialize(document.getElementById("map_canvas"), true);
            $('#spinner').hide();
            var drawControls, editControls;
            <g:if test="${params.controller == 'checklist'}">
                drawControls = {
                    rectangle:true,
                    circle:true,
                    polygon:true,
                    polyline:true,
                    marker:false
                }

                editControls = {featureGroup: new L.FeatureGroup()}
            </g:if>
            initArea(true, drawControls, editControls);
            $('.geotagged_image').each(function(index){
                update_geotagged_images_list($(this));		
            });
        });
    } 
}

$(document).ready(function() {
    $(".address .add-on").click(function(){
        if($("#map_canvas").is(':visible')) {
            $("#map_canvas").hide();
            $("#latlng").hide();
            return false;
        }
    });

    $(".address").click(loadMapInput);
});
</r:script>
</div>
