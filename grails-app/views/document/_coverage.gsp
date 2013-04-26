
<div class="span12 super-section" style="clear: both; margin-left:0px;">

	<div class="section"
		style="position: relative; overflow: visible; clear: both;">

		<h3>Taxonomic Coverage</h3>



		<label class="control-label">Species Groups & Habitats </label>

		<div class="filters controls textbox" style="position: relative;">
			<obv:showGroupFilter
				model="['observationInstance':observationInstance, 'hideAdvSearchBar':true]" />
		</div>
	</div>


	<div class="section">
		<h3>Geographical Coverage</h3>

		<div class="span6" style="margin: 0px";>


			<div class="row control-group">

				<label for="place_name" class="control-label"> <i
					class="icon-map-marker"></i> <g:message code="eml.location.label"
						default="Location title" />
				</label>

				<div class="controls textbox">
					<input id="place_name" type="text" name="coverage.placeName"
						class="input-block-level" value="${coverageInstance?.placeName}"></input>
				</div>

			</div>

			<div class="row control-group">
				<%
	                              	def defaultAccuracy = (coverageInstance?.locationAccuracy) ? coverageInstance.locationAccuracy : "Approximate"
	                                def isAccurateChecked = (defaultAccuracy == "Accurate")? "checked" : ""
	                                def isApproxChecked = (defaultAccuracy == "Approximate")? "checked" : ""
	                            %>
				<label for="location_accuracy" class="control-label"
					style="padding: 0px"><g:message code="em.accuracy.label"
						default="Accuracy" /> </label>

				<div class="controls">
					<input type="radio" name="coverage.location_accuracy"
						value="Accurate" ${isAccurateChecked}>Accurate <input
						type="radio" name="coverage.location_accuracy" value="Approximate"
						${isApproxChecked}>Approximate<br />
				</div>
			</div>

			<div class="row control-group">
				<label for="location_accuracy" class="control-label"
					style="padding: 0px"><g:message code="em.geoprivacy.label"
						default="Geoprivacy" /> </label>

				<div class="controls">
					<input type="checkbox" class="input-block-level"
						name="coverage.geo_privacy" value="geo_privacy" /> Hide precise
					location
				</div>
			</div>
			<hr>
			<div class="row control-group">
				<label for="location_accuracy" class="control-label"
					style="padding: 0px"><g:message code="em.geocode.label"
						default="Geocode name" /> </label>
				<div class="controls">
					<div class="location_picker_value" id="reverse_geocoded_name"></div>
					<input id="reverse_geocoded_name_field" type="hidden"
						class="input-block-level" name="coverage.reverse_geocoded_name">
					</input>
				</div>
			</div>
			<div>
				<input id="use_dms" class="input-block-level" type="checkbox"
					name="coverage.use_dms" value="use_dms" /> Use deg-min-sec format
				for lat/long
			</div>

			<div
				class="row control-group  ${hasErrors(bean: emInstance, field: 'latitude', 'error')}">
				<label for="location_accuracy" class="control-label"><g:message
						code="em.latitude.label" default="Latitude" /> </label>
				<div class="controls textbox">

					<input class="degree_field input-block-level" id="latitude_field"
						type="text" name="coverage.latitude"></input> <input
						class="dms_field" id="latitude_deg_field" type="text"
						name="coverage.latitude_deg" placeholder="deg"></input> <input
						class="dms_field" id="latitude_min_field" type="text"
						name="coverage.latitude_min" placeholder="min"></input> <input
						class="dms_field" id="latitude_sec_field" type="text"
						name="coverage.latitude_sec" placeholder="sec"></input> <input
						class="dms_field" id="latitude_direction_field" type="text"
						name="coverage.latitude_direction" placeholder="direction"></input>
					<div class="help-inline">
						<g:hasErrors bean="${coverageInstance}" field="latitude">
							<g:renderErrors bean="${coverageInstance}" as="list"
								field="latitude" />
						</g:hasErrors>
					</div>
				</div>
			</div>
			<div
				class="row control-group ${hasErrors(bean: coverageInstance, field: 'longitude', 'error')}">
				<label for="location_accuracy" class="control-label"><g:message
						code="em.longitude.label" default="Longitude" /> </label>
				<div class="controls textbox">

					<input class="degree_field input-block-level" id="longitude_field"
						type="text" name="coverage.longitude"></input> <input
						class="dms_field" id="longitude_deg_field" type="text"
						name="coverage.longitude_deg" placeholder="deg"></input> <input
						class="dms_field" id="longitude_min_field" type="text"
						name="coverage.longitude_min" placeholder="min"></input> <input
						class="dms_field" id="longitude_sec_field" type="text"
						name="coverage.longitude_sec" placeholder="sec"></input> <input
						class="dms_field" id="longitude_direction_field" type="text"
						name="coverage.longitude_direction" placeholder="direction"></input>
					<div class="help-inline">
						<g:hasErrors bean="${coverageInstance}" field="longitude">
							<g:renderErrors bean="${coverageInstance}" as="list"
								field="longitude" />
						</g:hasErrors>
					</div>
				</div>
			</div>

		</div>
		<div class=" span6 sidebar-section section"
			style="padding: 0; width: 430px;">
			<div class="map_search">
				<div id="geotagged_images" style="padding: 10px;">
					<div class="title" style="display: none">Use location from
						geo-tagged image:</div>
					<div class="msg" style="display: none">Select image if you
						want to use location information embedded in it</div>
				</div>

				<div id="current_location" class="section-item">
					<div class="location_picker_button">
						<a href="#" onclick="return false;">Use current location</a>
					</div>
				</div>
				<input id="address" type="text" title="Find by place name"
					class="input-block-level" class="section-item" />



				<div id="map_area">
					<div id="map_canvas"></div>
				</div>
			</div>
		</div>
	</div>




</div>


<r:script>
	$(document).ready(function() {
		intializesSeciesHabitatInterest();
		
	});
</r:script>

