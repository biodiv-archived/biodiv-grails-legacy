<% 
//    newSpeciesFieldInstance.id = 'f'+fieldInstance.id
    newSpeciesFieldInstance.field = fieldInstance
%>

<g:showSpeciesField
            model="['speciesInstance':speciesInstance, 'speciesFieldInstance':newSpeciesFieldInstance, 'speciesId':speciesInstance.id, 'fieldInstance':fieldInstance, 'isSpeciesContributor':isSpeciesContributor]" />

