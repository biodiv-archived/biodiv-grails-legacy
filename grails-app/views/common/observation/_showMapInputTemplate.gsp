<%@ page import="species.utils.Utils"%>

<div class="control-group locationScale ${hasErrors(bean: observationInstance, field: 'locationScale', 'error')}" >
	<label for="locationScale" class="control-label"> <g:message
    	code="observation.locationScale.label" default="Location Scale" /><span class="req">*</span>
    </label>
	<div class="controls"  style="margin-top:5px;">
		<%
        	def defaultAccuracy = (obvInfoFeeder?.locationScale) ? obvInfoFeeder.locationScale.value().toLowerCase() : "approximate"
            def isAccurateChecked = (defaultAccuracy == "accurate")? "checked" : ""
            def isApproxChecked = (defaultAccuracy == "approximate")? "checked" : ""
			def isLocalChecked = (defaultAccuracy == "local")? "checked" : ""
			def isRegionChecked = (defaultAccuracy == "region")? "checked" : ""
			def isCountryChecked = (defaultAccuracy == "country")? "checked" : ""
			
			def isGeoPrivacyChecked = (observationInstance?.geoPrivacy) ? "checked" : ""
						
         %>
		<g:if test="${sourceType == 'observation'  || ((params.action == 'edit') && (sourceType == 'checklist-obv'))}">
        	<input type="radio" style="margin-bottom: 6px;" name="locationScale" value="Accurate" ${isAccurateChecked} /><g:message code="default.accurate.label" /> 
            <input type="radio" style="margin-bottom: 6px;"  name="locationScale" value="Approximate" ${isApproxChecked} /><g:message code="default.approximate.label" />
        </g:if>
        <g:if test="${(sourceType == 'checklist') || ((params.action == 'edit') && (sourceType == 'checklist-obv'))}">
        	<input type="radio" style="margin-bottom: 6px;" name="locationScale" value="Local" ${isLocalChecked} /><g:message code="default.local.label" /> 
            <input type="radio" style="margin-bottom: 6px;" name="locationScale" value="Region" ${isRegionChecked} /><g:message code="default.regional.label" /> 
			<input type="radio" style="margin-bottom: 6px;"  name="locationScale" value="Country" ${isCountryChecked} /><g:message code="default.country.label" /> 
		</g:if>	
		

	    <div class="help-inline">
        	<g:hasErrors bean="${observationInstance}" field="locationScale">
        		<g:message code="observation.locationScale.not_selected" />
        	</g:hasErrors>
	    </div>
		
		 
	</div>
	
	
</div>




<div class="control-group map_class">

    <label for="topology" class="control-label">
        <i class="icon-map-marker"></i>
        <g:message
        code="observation.topology.label" default="${g.message(code:'default.label.at')}" /> <span class="req">*</span></label>
    <div class="controls ">
 
    <div style="margin-left:0px;">

        <div class="map_area">
            <div class="map_search">
                <div class="geotagged_images" style="display:none">
                    <div class="title" style="display: none"><g:message code="default.use.location.date.label" /></div>
                    <div class="msg" style="display: none"><g:message code="default.select.to.use.label" /></div>
                </div>
                <div id="current_location" class="section-item" style="display:none">
                    <div class="location_picker_button"><a href="#" onclick="return false;"><g:message code="default.use.current.location.label" /></a></div>
                </div>
                <div class="wrapperParent"  style="text-align:center;width:100%">
                    <div class="address input-append control-group ${hasErrors(bean: sourceInstance, field:placeNameField, 'error')} ${hasErrors(bean: sourceInstance, field: topologyNameField, 'error')} " style="z-index:2;margin-bottom:0px;">
                        <input class="placeName" name="placeName" type="text" title="${g.message(code:'showmapinput.find.place')}"  class="input-block-level" style="width:94%;"
                        class="section-item" value="${observationInstance?.placeName}" rel="${g.message(code:'default.search')}" />

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
		                            <span class="add-on" style="vertical-align:middle;"><g:message code="default.lat.label" /></span>
		                            <input class="degree_field latitude_field" type="text" name="latitude" value="${params.latitude}"/>
		                            <input class="dms_field latitude_deg_field" type="text" name="latitude_deg" placeholder="${g.message(code:'placeholder.deg')}"/>
		                            <input class="dms_field latitude_min_field" type="text" name="latitude_min" placeholder="${g.message(code:'placeholder.min')}"/>
		                            <input class="dms_field latitude_sec_field" type="text" name="latitude_sec" placeholder="${g.message(code:'placeholder.sec')}"/>
		                            <input class="dms_field latitude_direction_field" type="text" name="latitude_direction" placeholder="${g.message(code:'placeholder.n.e')}"/>
		                        </div>
		                        <div class="input-prepend pull-left control-group" style="width:250px;">
		                            <span class="add-on" style="vertical-align:middle;"><g:message code="default.long.label" /></span>
		                            <input class="degree_field longitude_field" type="text" name="longitude" style="width:193px;" value="${params.longitude}"></input>
		                            <input class="dms_field longitude_deg_field" type="text" name="longitude_deg" placeholder="${g.message(code:'placeholder.deg')}"/>
		                            <input class="dms_field longitude_min_field" type="text" name="longitude_min" placeholder="${g.message(code:'placeholder.min')}"/>
		                            <input class="dms_field longitude_sec_field" type="text" name="longitude_sec" placeholder="${g.message(code:'placeholder.sec')}"/>
		                            <input class="dms_field longitude_direction_field" type="text" name="longitude_direction" placeholder="${g.message(code:'placeholder.n.e')}"/>
		                        </div>
		                        <div class="control-group">
		                            <label class="pull-left" style="text-align:center; font-weight:normal;"> <g:checkBox class="use_dms pull-left"
		                                name="use_dms" value="${use_dms}" />
		                                <g:message code="default.use.deg-min-sec.label" /> </label>
		                            <g:if test="${sourceType != 'checklist'}">    
										<label class="pull-left" style="text-align:center; font-weight:normal;margin-left: 20px;"><input type="checkbox" class="pull-left" name="geoPrivacy" value="${observationInstance?.geoPrivacy}" onclick="$(this).val('' + $(this).prop('checked'))" ${isGeoPrivacyChecked} /><g:message code="default.hide.location.label" /></label>
									</g:if>	  
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
                        
                         <div class="row control-group" style="display:none;" >
                            <label for="locationScale" class="control-label" style="padding:0px"><g:message
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
</script>
</div>
