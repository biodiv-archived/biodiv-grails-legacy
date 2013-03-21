<script type="text/javascript">
    var childCount = ${projectInstance?.locations.size()} + 0;

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
      locationInput.attr('id',htmlId + 'number')
              .attr('name',htmlId + 'number');
      clone.find("select[id$=type]")
              .attr('id',htmlId + 'type')
              .attr('name',htmlId + 'type');

      clone.attr('id', 'location'+childCount);
      $("#childList").append(clone);
      clone.show();
      locationInput.focus();
      childCount++;
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

"grails-app/views/contact/_locations.gsp" 62L, 2133C                                                                                                                                  1,1           Top
