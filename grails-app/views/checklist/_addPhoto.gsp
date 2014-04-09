
<div id="addResourcesModal" class="modal hide fade span8" tabindex='-1' role="dialog">
     <div class="modal-header">
         <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
         <h4>Add/Edit Media</h4>
     </div>
     <div class="modal-body">
        <g:render template="/observation/addPhotoWrapper" model="['observationInstance':observationInstance, 'resourceListType':'ofChecklist']"/>
    </div>
     <div class="modal-footer">
         <a id="addResourcesModalSubmit" class="btn btn-primary">Add Resources</a>
     </div>
</div>
