<div id="addSpFieldResourcesModal" class="modal hide fade span8" data-spfieldid="" tabindex='-1' role="dialog">
     <div class="modal-header">
         <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
         <h4><g:message code="checklist.addphoto.add.edit.media" /> </h4>
     </div>
     <div class="modal-body">
        <g:render template="/species/speciesFieldImageUpload" model="['observationInstance':speciesInstance, 'speciesFieldFlag' :true, 'isSpeciesContributor':isSpeciesContributor]"/>
    </div>
    <div class="modal-footer">
         <a id="addSpFieldResourcesModalSubmit" class="btn btn-primary"><g:message code="checklist.addphoto.add.resources" /></a>
     </div>
</div>
