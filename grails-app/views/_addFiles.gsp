<script type="text/javascript">
	var childCount = ${uFiles?.size()} + 0;

    $('.addUFile').live('click', function() {
      var clone = $("#ufile_clone").clone();
      var htmlId = 'ufilesList['+childCount+'].';

      var childList = $(this).siblings("#${id}");
     
      var ufileInput = clone.find("input[id$=number]");

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
      ufileInput.attr('id',htmlId + 'number')
              .attr('name',htmlId + 'number');
      clone.find("select[id$=type]")
              .attr('id',htmlId + 'type')
              .attr('name',htmlId + 'type');

      clone.attr('id', 'ufile'+childCount);
      childList.append(clone);
      clone.show();
      ufileInput.focus();
      childCount++;
    });

    
    //bind click event on delete buttons using jquery live
    $('.del-ufile').live('click', function() {
        //find the parent div
        var prnt = $(this).parents(".ufile-div");
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

<div id="${id}">
    <g:each var="uFile" in="${uFiles}" status="i">
    
     <!-- Render the addFile template (_addFile.gsp) here -->
        <g:render template='/addFile' model="['uFile':uFile,'i':i,'hidden':false,'name':${id}]"/>
        <!-- Render the phone template (_phone.gsp) here -->


        </g:each>
</div>
 <!-- Render the phone template (_phone.gsp) hidden so we can clone it -->
 <g:render template='/addFile' model="['uFile':null,'i':'_clone','hidden':true,'name':${id}]"/>
<input type="button" value="Add File" class="addUFile" />      