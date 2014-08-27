
<div id="addResourcesModal" class="modal hide fade span8" tabindex='-1' role="dialog">
     <div class="modal-header">
         <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
         <h4><g:message code="checklist.addphoto.add.edit.media" /></h4>
     </div>
     <div class="modal-body">
         <%
            def resCount = 0 
         %>
        <g:render template="/observation/addPhotoWrapper" model="['observationInstance':observationInstance, 'resourceListType':'ofChecklist', 'resCount': resCount]"/>
    </div>
     <div class="modal-footer">
         <a id="addResourcesModalSubmit" class="btn btn-primary"><g:message code="checklist.addphoto.add.resources" /></a>
     </div>
</div>
