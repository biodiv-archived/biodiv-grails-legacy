<a
    href="${uGroup.createLink([controller:'species', action:'show', id:speciesInstance.id, userGroup:userGroupInstance])}" style="display:block;">
    <span class="species_story ellipsis multiline sci_name"
        style="display: block;height:6=50px;" title="${speciesInstance.taxonConcept.name.replaceAll('<.*>','')}">${speciesInstance.taxonConcept.name.trim()}</span> </a>
<div class="poor_species_content" style="display: none;">No
    information yet</div>

