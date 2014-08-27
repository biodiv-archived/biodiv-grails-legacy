<%@ page import="species.Synonyms"%>
<%@ page import="species.CommonNames"%>
<%@page import="species.participation.ActivityFeedService"%>
<%def nameRecords = fields.get(grailsApplication.config.speciesPortal.fields.NOMENCLATURE_AND_CLASSIFICATION)?.get(grailsApplication.config.speciesPortal.fields.TAXON_RECORD_NAME).collect{if(it.value && !it.key.equals('hasContent') &&  !it.key.equals('isContributor') && it.value.containsKey('speciesFieldInstance')){ return it.value.speciesFieldInstance[0]}} %>
<g:if test="${nameRecords}">
<div class="sidebar_section" style="clear:both;">
    <a class="speciesFieldHeader"  data-toggle="collapse" href="#taxonRecordName">
        <h5><g:message code="showspeciesnames.taxon.record.name" /></h5>
    </a>

    <div id="taxonRecordName" class="speciesField collapse in">
        <table>
            <tr class="prop">
                <td><span class="grid_3 name">${grailsApplication.config.speciesPortal.fields.SCIENTIFIC_NAME }</span></td><td> ${raw(speciesInstance.taxonConcept.italicisedForm)}</td>
            </tr>
            <g:each in="${nameRecords}">
            <g:if test="${it}">
            <tr class="prop">

                <g:if test="${it?.field?.subCategory?.equalsIgnoreCase(grailsApplication.config.speciesPortal.fields.REFERENCES)}">
                <td><span class="grid_3 name">${it?.field?.subCategory} </span></td> <td class="linktext">${raw(it?.description)}</td>
                </g:if> 
                <g:elseif test="${it?.field?.subCategory?.equalsIgnoreCase(grailsApplication.config.speciesPortal.fields.GENERIC_SPECIFIC_NAME)}">

                </g:elseif> 
                <g:elseif test="${it?.field?.subCategory?.equalsIgnoreCase(grailsApplication.config.speciesPortal.fields.SCIENTIFIC_NAME)}">

                </g:elseif> 
                <g:elseif test="${it?.field?.subCategory?.equalsIgnoreCase('year')}">
                <td><span class="grid_3 name">${it?.field?.subCategory} </span></td> <td> ${it?.description}</td>
                </g:elseif> 
                <g:else>
                <td><span class="grid_3 name">${it?.field?.subCategory} </span></td> <td> ${it?.description}</td>
                </g:else> 
            </tr>
            </g:if>
            </g:each>
        </table>
    </div>

    <comment:showCommentPopup model="['commentHolder':[objectType:ActivityFeedService.SPECIES_TAXON_RECORD_NAME, id:speciesInstance.id], 'rootHolder':speciesInstance]" />
</div>
<br/>
</g:if>

<!-- Synonyms -->
<%def synonyms = Synonyms.findAllByTaxonConcept(speciesInstance.taxonConcept) %>
<g:if test="${synonyms}">
<div class="sidebar_section">
    <a class="speciesFieldHeader"  data-toggle="collapse" href="#synonyms"> 
        <h5><g:message code="showspeciesnames.synonyms" /></h5>
    </a> 
    <ul id="synonyms" class="speciesField collapse in" style="list-style:none;overflow:hidden;margin-left:0px;padding:0px;">
            <g:each in="${synonyms}" var="synonym">
            <li>
            <div class="span3">
                <span class="synRel  ${isSpeciesContributor && synonym.isContributor() ?'selector':''}" data-type="select" data-name="relationship" data-original-title="Edit Synonym Relationship">
                    ${synonym?.relationship?.value()}</span> 
            </div>
            <div class="span8">
                <span class="sci_name ${isSpeciesContributor && synonym.isContributor() ?'editField':''}" data-type="text" data-pk="${speciesInstance.id}" data-sid="${synonym.id}" data-url="${uGroup.createLink(controller:'species', action:'update') }" data-name="synonym" data-original-title="Edit synonym name" title="Click to edit">  ${(synonym?.italicisedForm)?raw(synonym.italicisedForm):raw('<i>'+(synonym?.name)+'</i>')} </span>
            </div>    
            </li>
            </g:each>
            <g:if test="${isSpeciesContributor}">
            <li>
            <div class="span3">
                <span class="synRel add_selector ${isSpeciesContributor?'selector':''}" data-type="select" data-name="relationship" data-original-title="Edit Synonym Relationship"></span>
            </div>
            <div class="span8">
                <span class="addField"  data-pk="${speciesInstance.id}" data-type="text"  data-url="${uGroup.createLink(controller:'species', action:'update') }" data-name="synonym" data-original-title="Add Synonym" data-placeholder="Add Synonym"></span>
            </div>
            </li>
            </g:if>

    </ul>
    <comment:showCommentPopup model="['commentHolder':[objectType:ActivityFeedService.SPECIES_SYNONYMS, id:speciesInstance.id], 'rootHolder':speciesInstance]" />
</div>
<br/>
</g:if>
<g:elseif test="${isSpeciesContributor}">
<div class="sidebar_section emptyField" style="display:none;">
    <a class="speciesFieldHeader"  data-toggle="collapse" href="#synonyms"> 
        <h5><g:message code="showspeciesnames.synonyms" /></h5>
    </a> 
    <ul id="synonyms" class="speciesField collapse in" style="list-style:none;overflow:hidden;margin-left:0px;">
           <li>
            <div class="span3">
                <span class="synRel add_selector ${isSpeciesContributor?'selector':''}" data-type="select" data-name="relationship" data-original-title="Edit Synonym Relationship"></span>
            </div>
            <div class="span8">
                <span class="addField"  data-pk="${speciesInstance.id}" data-type="text"  data-url="${uGroup.createLink(controller:'species', action:'update') }" data-name="synonym" data-original-title="Add Synonym" data-placeholder="Add Synonym"></span>
            </div>
            </li>

    </ul>
</div>
<br/>
</g:elseif>

<!-- Common Names -->
<%
Map names = new LinkedHashMap();
CommonNames.findAllByTaxonConcept(speciesInstance.taxonConcept).each() {
String languageName = it?.language?.name ?: "Others";

/*if(it?.language?.isDirty) {
languageName = "Others";	
}*/
if(!names.containsKey(languageName)) {
names.put(languageName, new ArrayList());
}
names.get(languageName).add(it)
};

names = names.sort();
names.each { key, list ->
list.sort();						
}

%>
<g:if test="${names}">
<div class="sidebar_section">
    <a class="speciesFieldHeader" data-toggle="collapse" href="#commonNames"><h5> <g:message code="showspeciesnames.common.names" /></h5></a> 
    <ul id="commonNames" class="speciesField collapse in" style="list-style:none;overflow:hidden;margin-left:0px;padding:0px;">
        <g:each in="${names}">
        <li>
        <div class="span3">
            <!-- TODO: language selector shd be seperated out -->
            <span class="lang ${isSpeciesContributor ? 'selector':''}" data-type="select" data-name="language" data-original-title="Edit common name language">
                ${it.key}</span>
        </div> 
        <div class="span8" style="display:table;">
            <g:each in="${it.value}"  status="i" var ="n">
                <div class="entry pull-left" style="display:table-row;"> 
                <span class="common_name ${isSpeciesContributor && n.isContributor() ?'editField':''}" data-type="text" data-pk="${speciesInstance.id}" data-cid="${n.id}" data-url="${uGroup.createLink(controller:'species', action:'update') }" data-name="commonname" data-original-title="Edit common name" title="Click to edit">${n.name}</span><g:if test="${i < it.value.size()-1}">,</g:if>
                </div>
            </g:each>
        </div>
        </li>
        </g:each>

        <g:if test="${isSpeciesContributor}">
            <li>
                <div class="span3">
                    <span class="lang add_selector ${isSpeciesContributor?'selector':''}" data-type="select" data-name="language" data-original-title="Edit common name language">
                        </span>
                </div> 
                <div class="span8" style="display:table;">
                    <div style="display:table-row;"> 
                        <span class="common_name ${isSpeciesContributor?'addField':''}" data-type="text" data-pk="${speciesInstance.id}" data-url="${uGroup.createLink(controller:'species', action:'update') }" data-name="commonname" data-original-title="Add Common Name" data-placeholder="Add Common Name">  
                            </span>
                    </div>
                </div>
            </li>
        </g:if>
    </ul>
    <comment:showCommentPopup model="['commentHolder':[objectType:ActivityFeedService.SPECIES_COMMON_NAMES, id:speciesInstance.id], 'rootHolder':speciesInstance]" />
</div>
<br/>
</g:if>
<g:elseif test="${isSpeciesContributor}">
<div class="sidebar_section emptyField" style="display:none;">
    <a class="speciesFieldHeader" data-toggle="collapse" href="#commonNames"><h5> <g:message code="showspeciesnames.common.names" /></h5></a> 
    <ul id="commonNames" class="speciesField collapse in" style="list-style:none;overflow:hidden;margin-left:0px;">
        <li>
        <div class="span3">
            <span class="lang add_selector ${isSpeciesContributor?'selector':''}" data-type="select" data-name="language" data-original-title="Edit common name language">
                </span>
        </div> 
        <div class="span8" style="display:table;">
            <div style="display:table-row;"> 
                <span class="addField" data-type="text" data-pk="${speciesInstance.id}" data-url="${uGroup.createLink(controller:'species', action:'update') }" data-name="commonname" data-original-title="Add Common Name" data-placeholder="Add Common Name">  
                    </span>
            </div>
        </div>
        </li>
    </ul>
</div>
</g:elseif>
<!-- Common Names End-->

