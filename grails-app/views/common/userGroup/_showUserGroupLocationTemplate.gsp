<div class="observation_location">
    <div id="map_canvas" style="width:100%; height: 300px;"></div>
<g:javascript>
$(document).ready(function() {
    loadUserGroupLocation("${userGroupInstance.ne_latitude}","${userGroupInstance.ne_longitude}","${userGroupInstance.sw_latitude}","${userGroupInstance.sw_longitude}");
});
</g:javascript>
</div>
