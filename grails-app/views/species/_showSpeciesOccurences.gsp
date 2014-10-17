<%@page import="species.participation.ActivityFeedService"%>
<%@page import="grails.plugin.springsecurity.SpringSecurityUtils"%>


<g:showSpeciesFieldToolbar model="${category.value[0]}" />
<br />
<div>
    <div id="map">
    <div id="map1311326056727" class="occurenceMap"
        style="height: 600px; width: 100%"></div>
    </div>

    <div class="alert alert-info">
        <g:message code="showspeciesoccurences.map.indicative" />
        <img src="${resource(dir:'images',file:'maplegend.png', absolute:true)}"/>
    </div>

    <comment:showCommentPopup model="['commentHolder':[objectType:ActivityFeedService.SPECIES_MAPS, id:speciesInstance.id], 'rootHolder':speciesInstance]" />       

</div>

<div class="sidebar_section">
    <h5><g:message code="observation.show.related.observations" /></h5>
    <div class="tile" style="clear: both">
        <obv:showRelatedStory
        model="['speciesId':speciesInstance.id, 'controller':'observation', 'action':'related', 'filterProperty': 'taxonConcept',  'filterPropertyValue': speciesInstance.taxonConcept.id, 'id':'a','userGroupInstance':userGroupInstance]" />
    </div>
</div>

