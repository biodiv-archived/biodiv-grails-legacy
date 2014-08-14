<div id="addSpFieldResourcesModal" class="modal hide fade span8" data-spfieldid="" tabindex='-1' role="dialog">
     <div class="modal-header">
         <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
         <h4>Add/Edit Media</h4>
     </div>
     <div class="modal-body">
        <g:render template="/species/speciesFieldImageUpload" model="['observationInstance':speciesInstance, 'isSpeciesContributor':isSpeciesContributor]"/>
    </div>
     <div class="modal-footer">
     </div>
</div>
