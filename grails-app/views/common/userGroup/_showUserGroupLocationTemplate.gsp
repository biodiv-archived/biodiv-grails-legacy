<div class="observation_location">
    <div id="map_canvas" style="width:100%; height: 300px;"></div>
    <div class="alert alert-danger hide" style="margin:0px;padding-left:3px;"></div>
<script type="text/javascript">
$(document).ready(function() {
    loadUserGroupLocation("${userGroupInstance.ne_latitude}","${userGroupInstance.ne_longitude}","${userGroupInstance.sw_latitude}","${userGroupInstance.sw_longitude}");
});
</script>
</div>
