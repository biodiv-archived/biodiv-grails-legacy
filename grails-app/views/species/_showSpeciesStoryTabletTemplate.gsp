<div class="poor_species_content" style="display: none;">No
    information yet</div>
<a
    href="${uGroup.createLink([controller:'species', action:'show', id:speciesInstance.id, userGroup:userGroup])}" style="display:inline-block;width:125px;">
    <span class="species_story ellipsis multiline sci_name"
        style="display: block;height:50px;padding:0px" title="${speciesInstance.taxonConcept.name.replaceAll('<.*>','')}">${speciesInstance.taxonConcept.name.trim()}</span> </a>

