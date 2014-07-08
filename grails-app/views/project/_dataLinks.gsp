<script type="text/javascript">
    var dataLinksCount = ${(projectInstance?.dataLinks?.size()>0)?projectInstance?.dataLinks?.size():1} 

    function adddataLink(){
      var clone = $("#dataLink_clone").clone()
      var htmlId = 'dataLinksList['+dataLinksCount+'].';
      var dataLinkInput = clone.find("input[id$=number]");

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
      clone.find("textarea[name$=description]")
      .attr('id',htmlId + 'description')
      .attr('name',htmlId + 'description')
      clone.find("input[id$=url]")
      .attr('id',htmlId + 'url')
      .attr('name',htmlId + 'url')

      clone.attr('id', 'dataLink'+dataLinksCount);
      $("#dataLinksList").append(clone);
      clone.show();
      dataLinkInput.focus();
      dataLinksCount++;

    }

    //bind click event on delete buttons using jquery live
    $('.del-dataLink').live('click', function() {
        //find the parent div
        var prnt = $(this).parents(".dataLink-div");
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

<div id="dataLinksList">
	<g:if test="${projectInstance?.dataLinks?.size()>0}">
		<g:each var="dataLink" in="${projectInstance.dataLinks}" status="i">

			<!-- Render the dataLink template (_dataLink.gsp) here -->
			<g:render template='dataLink'
				model="['dataLink':dataLink,'i':i,'hidden':false]" />
			<!-- Render the dataLink template (_dataLink.gsp) here -->

		</g:each>
	</g:if>
	<g:else>
		<g:render template='dataLink' model="['i':0,'hidden':false]" />
	</g:else>
</div>
<div style="text-align: center; clear:both;">
	<input type="button" class="btn btn-primary" value="Add Data Link"
		onclick="adddataLink();" />
</div>
