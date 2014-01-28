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
	<hr>
	<obv:showMapInput model="[observationInstance:coverageInstance, obvInfoFeeder:coverageInstance, locationHeading:'Geographical Coverage', 'sourceInstance':sourceInstance]"></obv:showMapInput>
</div>

<r:script>
$(document).ready(function() {
	intializesSpeciesHabitatInterest();
	
});
</r:script>

