
<div class="grid_5" class="observation_story sidebar">
	<div class="rating" style="float: right">
		<input class="star" type="radio" name="orating" value="1"
			title="Worst" /> <input class="star" type="radio" name="orating"
			value="2" title="Bad" /> <input class="star" type="radio"
			name="orating" value="3" title="OK" /> <input class="star"
			type="radio" name="orating" value="4" title="Good" /> <input
			class="star" type="radio" name="orating" value="5" title="Best" />
	</div>
	<div>
		<p class="prop">
			<span class="name">By </span>
			<g:link controller="sUser" action="show"
				id="${observationInstance.author.id}">
				${observationInstance.author.username}
			</g:link>
		</p>
		<p class="prop">
			<span class="name">Observed on</span> <span class="value"><g:formatDate
					format="MMMMM dd, yyyy" date="${observationInstance.observedOn}" />
			</span>
		</p>
		<p class="prop">
			<span class="name">Group</span> <span class="value"><g:link
					controller="speciesGroup" action="show" id="${observationInstance.group?.id }">${observationInstance.group?.name }</g:link>
			</span>
		</p>
		<p class="prop readmore">
			<span class="name">Description </span> <span class="value"> ${observationInstance.notes}
			</span>
		</p>
		<p class="prop">
			<span class="name">Place name</span> <span class="value"> ${observationInstance.placeName}
			</span>
		</p>
                <p class="prop">
			<span class="name">Latitude</span> <span class="value"> ${observationInstance.latitude}
			</span>
		</p>
                <p class="prop">
			<span class="name">Longitude</span> <span class="value"> ${observationInstance.longitude}
			</span>
		</p>
                <script type="text/javascript" src="http://maps.google.com/maps/api/js?sensor=true"></script>
                <script>
                $(document).ready(function() {
                  var latlng = new google.maps.LatLng(${observationInstance.latitude}, ${observationInstance.longitude});
                  var options = {
                    zoom: 4,
                    center: latlng,
                    mapTypeId: google.maps.MapTypeId.HYBRID
                  };
                  var map = new google.maps.Map(document.getElementById("map_canvas_${observationInstance.id}"), options);
                  var marker = new google.maps.Marker({
                    map: map,
                    draggable: false
                  });
                
                  marker.setPosition(latlng);
                  map.setCenter(latlng);

                });
                </script>
                <div id="map_canvas_${observationInstance.id}" style="height:170px;"></div>


	</div>
</div>
