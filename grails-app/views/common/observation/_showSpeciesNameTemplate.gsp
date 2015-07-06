<%@page import="species.NamesMetadata.NamePosition"%>
<%@page import="species.NamesMetadata.NameStatus"%>
<%@page import="species.Species"%>


<div class="species_title">
	<%
		def commonName = observationInstance.isChecklist ? observationInstance.title :observationInstance.fetchSuggestedCommonNames()
		def speciesId = observationInstance.maxVotedReco?.taxonConcept?.findSpeciesId();
		def speciesLink = " "
        def see_checklists=g.message(code:"button.see.checklist")
        def sourcechecklists=g.message(code:"showspeciesnametemp.title.source")
        def see_species=g.message(code:"button.see.species")
		if(speciesId && !isHeading){
			speciesLink += '<a class="species-page-link" style="font-style: normal;" href="' + uGroup.createLink(controller:'species', action:'show', id:speciesId, 'userGroupWebaddress':params?.webaddress, absolute:true) + '">' + "<i class='icon-info-sign' style='margin-right: 1px; margin-left: 10px;'></i>"+see_species+"</a>"
		} 
		if(observationInstance.id != observationInstance.sourceId && !isHeading){
			speciesLink += '<a class="species-page-link" title="'+g.message(code:"showspeciesnametemp.title.source")+'" style="font-style: normal;" href="' + uGroup.createLink(controller:'checklist', action:'show', id:observationInstance.sourceId, 'userGroupWebaddress':params?.webaddress, absolute:true) + '">' + "<i class='icon-info-sign' style='margin-right: 1px; margin-left: 10px;'></i>"+see_checklists+"</a>"
		}
	%>
	<g:set var="speciesLinkHtml" value="${speciesLink.replaceAll('"','\\\\"').encodeAsRaw()}" />
	<g:set var="sName" value="${raw(observationInstance.fetchFormattedSpeciesCall())}" />
	<g:set var="sNameTitle" value="${observationInstance.fetchSpeciesCall()}" />

    <% def status = ''; def position='' %>
    <g:if test="${observationInstance.maxVotedReco?.taxonConcept && isHeading}">
    <% status = observationInstance.maxVotedReco.taxonConcept.status.label(); %>
    <% position = observationInstance.maxVotedReco.taxonConcept.position; %>
    <g:if test="${observationInstance.maxVotedReco.taxonConcept.status == NameStatus.SYNONYM}">
    <% status += ' of ';%>
    <g:each in="${observationInstance.maxVotedReco.taxonConcept.fetchAcceptedNames()}" var="acceptedName">
    <% def s = acceptedName.findSpecies()%>
    <g:if test="${s}">
    <% status += "<a href='"+uGroup.createLink(controller:'species', action:'show', id:s.id)+"'>"+acceptedName.italicisedForm+"</a>"%> 
    </g:if>
    <g:else>
    <% status += acceptedName.italicisedForm%> 
    </g:else>
    </g:each>
    </g:if>
    </g:if>

	<g:if test="${observationInstance.isChecklist}">
		<div class="ellipsis" title="${commonName}">
			${commonName}
		</div>
	</g:if>
	<g:else>
	<g:if test="${sName == 'Unknown'}">
		<div class="sci_name ellipsis" title="${sNameTitle}">
			${sName} <a
				href="${uGroup.createLink(controller:'observation', action:'show', id:observationInstance.id, 'userGroupWebaddress':userGroup?userGroup.webaddress:userGroupWebaddress) }"><g:message code="link.help.identify" />
				</a>
		</div>
	</g:if>
	<g:elseif test="${isListView}">
		<g:if test="${commonName}">
			<div class="common_name ellipsis" title="${commonName }">
				${commonName}
			</div>
		</g:if>
		<g:elseif test="${observationInstance.maxVotedReco.isScientificName}">
			<div class="sci_name ellipsis" title="${sNameTitle}">
				 ${sName}
                <g:render template="/namelist/statusTemplate" model="[position:position, status:status]"/>
			</div>
		</g:elseif>
		<g:else>
                        <div class="ellipsis" title="${sNameTitle}">
                            ${sName}
                            <g:render template="/namelist/statusTemplate" model="[position:position, status:status]"/>
                        </div>
		</g:else>
	</g:elseif>
	<g:else>
		<g:if test="${observationInstance.maxVotedReco.isScientificName}">
			<div class="sci_name ellipsis" title="${sNameTitle }">
                ${sName} ${speciesLinkHtml}
                <g:render template="/namelist/statusTemplate" model="[position:position, status:status]"/>
			</div>
			<div class="common_name ellipsis" title="${commonName }">
				${commonName}
			</div>
		</g:if>
		<g:else>
			<div class="ellipsis" title="${sNameTitle}">
                ${sName} ${speciesLinkHtml}
                <g:render template="/namelist/statusTemplate" model="[position:position, status:status]"/>
			</div>
		</g:else>
	</g:else>
	</g:else>
</div>
