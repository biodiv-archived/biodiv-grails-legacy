<div class="locations-block">
<script type="text/javascript">
    var childCount = ${projectInstance?.locations?.size()} + 0;

    function addlocation(){
      var clone = $("#location_clone").clone()
      var htmlId = 'locationsList['+childCount+'].';
      var locationInput = clone.find("input[id$=number]");

      clone.find("input[id$=id]")
             .attr('id',htmlId + 'id')
             .attr('name',htmlId + 'id');
      clone.find("input[id$=deleted]")
              .attr('id',htmlId + 'deleted')
              .attr('name',htmlId + 'deleted');
      clone.find("input[id$=new]")
              .attr('id',htmlId + 'new')
              .attr('name',htmlId + 'new')
              .attr('value', 'true');
      clone.find("input[id$=siteName]")
      .attr('id',htmlId + 'siteName')
      .attr('name',htmlId + 'siteName')
      clone.find("input[id$=corridor]")
      .attr('id',htmlId + 'corridor')
      .attr('name',htmlId + 'corridor')

      clone.attr('id', 'location'+childCount);
      $("#childList").append(clone);
      clone.show();
      locationInput.focus();
      childCount++;

  	$(".site-name").autocomplete({
  		source: '/project/locationSites',
  		minLength: 2
  		
  		 
  	});

  		
  		$(".corridor").autocomplete({
  		source: '/project/locationCorridors',
  		minLength: 2
  		
  		});
    }

    //bind click event on delete buttons using jquery live
    $('.del-location').live('click', function() {
        //find the parent div
        var prnt = $(this).parents(".location-div");
        //find the deleted hidden input
        var delInput = prnt.find("input[id$=deleted]");
        //check if this is still not persisted
        var newValue = prnt.find("input[id$=new]").attr('value');
        //if it is new then i can safely remove from dom
        if(newValue == 'true'){
            prnt.remove();
        }else{
            //set the deletedFlag to true
            delInput.attr('value','true');
            //hide the div
            prnt.hide();
        }
    });

	

		

    </script>
    
<div id="childList">
    <g:each var="location" in="${projectInstance.locations}" status="i">

        <!-- Render the location template (_location.gsp) here -->
        <g:render template='location' model="['location':location,'i':i,'hidden':false]"/>
        <!-- Render the location template (_location.gsp) here -->

    </g:each>
</div>
<div style="text-align:center;">
<input type="button" class="btn btn-primary" value="Add location" onclick="addlocation();" />
</div>
</div>