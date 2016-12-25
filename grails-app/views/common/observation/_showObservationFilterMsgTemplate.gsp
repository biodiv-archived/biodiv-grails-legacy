<%@page import="species.auth.SUser"%>
<%@ page import="species.participation.Observation"%>
<%@ page import="species.Species"%>
<%@ page import="species.groups.SpeciesGroup"%>
<%@ page import="species.Habitat"%>
<%@ page import="species.ScientificName.TaxonomyRank"%>
<%@ page import="species.NamesMetadata"%>
<%@ page import="species.TaxonomyDefinition"%>
<%@ page import="species.dataset.Dataset"%>
<%@ page import="species.trait.Trait"%>
<%@ page import="species.trait.TraitValue"%>

<div class="info-message" ${hideId?'':'id=info-message'}>
		<g:if test="${speciesCountWithContent }"><span class="name" style="color: #b1b1b1;"><i
                class="icon-search"></i></span> ${speciesCountWithContent} <g:message code="common.observation.species.pages" /> <g:if test="${speciesCountWithContent>1}"></g:if> <g:if test="${instanceTotal- speciesCountWithContent>0}"><g:message code="text.and" /> ${instanceTotal- speciesCountWithContent} <g:message code="common.observation.species.stubs" /> </g:if> <g:message code="text.found1" /><g:if test="${params.hasMedia == 'true'}"> <g:message code="with.media" /></g:if><g:if test="${params.hasMedia == 'false'}"> <g:message code="without.media" /></g:if></g:if>
		<g:else>
			<span class="name" style="color: #b1b1b1;"><i
				class="icon-search"></i></span>
				<g:if test="${instanceTotal==0}"><g:message code="text.no.result" /> </g:if>
				<g:elseif test="${resultType != 'observation' }">
					${instanceTotal} 
					<g:if test="${resultType != 'user'}">${resultType}</g:if><g:else><g:message code="title.user" /></g:else><g:if test="${instanceTotal>1 && resultType != 'species'}"><g:message code="text.s" /></g:if>
				</g:elseif>
				<g:else>
					<g:if test="${observationCount}">
						<g:message code="text.observation" args="${observationCount}" /><g:if test="${observationCount>1}"><g:message code="text.s" /></g:if>
					</g:if>
					<g:if test="${params.isChecklistOnly && checklistCount}">
						<g:if test="${observationCount}"> <g:message code="text.and" /> </g:if>
						<g:message code="text.checklists" args="${checklistCount}" /><g:if test="${checklistCount>1}"><g:message code="text.s" /></g:if>
					</g:if>
                </g:else> 
                <g:if test="${params.filterProperty == 'speciesName' || params.filterProperty == 'nearByRelated' || params.filterProperty == 'taxonConcept'}">
                    <g:if test="${params.filterProperty == 'speciesName'}">
                        <% def obv = Observation.read(params.parentId?.toLong()) %>
                        of <a href="${uGroup.createLink( action:"show", controller:"observation", id:params.parentId, userGroupWebaddress:params.webaddress, absolute:true)}"> ${raw(obv.fetchSpeciesCall())}</a>
                    </g:if>
                    <g:elseif test="${params.filterProperty == 'taxonConcept'}">
                        <% def sp = Species.read(params.parentId?.toLong()) %>
                        of <a href="${uGroup.createLink(action:"show", controller:"species", id:params.parentId, userGroupWebaddress:params.webaddress, absolute:true)}"> ${raw(sp.title)}</a>
                    </g:elseif>
                    <g:elseif test="${params.filterProperty == 'nearByRelated'}">
                        <% def obv = Observation.read(params.parentId?.toLong()) %>
                        <g:message code="text.radius.km" /> ${obv.latitude} , ${obv.longitude}  
                    </g:elseif>
     
                </g:if>
                <g:else>
                    <g:message code="text.found" /> 
                </g:else>
		</g:else>
		<%
			boolean dateRangeSet = false	
                        %>
                <g:each in="${queryParams}" var="queryParam">
                            <g:if
				test="${queryParam.key == 'groupId' && queryParam.value instanceof Long }">
				<g:if
					test="${queryParam.value && SpeciesGroup.get(queryParam.value)}">
                                    <g:message code="text.of" /> <span class="highlight"> <a
						href="${uGroup.createLink(
						controller:"observation", action:"list",
						params:[sGroup: queryParam.value])}">
							${SpeciesGroup.get(queryParam.value).name} <a href="#"
							onclick="setDefaultGroup(); return false;">[X]</a> </a> </span><g:message code="default.group.label" /> 
                            </g:if>
			</g:if>
			<g:elseif test="${queryParam.key == 'groupId' && queryParam.value }">
                           		<g:message code="text.of" /> <span class="highlight"><a
					href="${uGroup.createLink(
					mapping:"userGroupGeneric", action:"list",
					params:[sGroup: queryParam.value])}">
						${queryParam.value } <a href="#"
						onclick="setDefaultGroup(); return false;">[X]</a> </a> </span> <g:message code="default.species.groups.label" />
                           </g:elseif>

			<g:if
				test="${queryParam.key == 'habitat' && queryParam.value instanceof Long }">
				<g:if test="${queryParam.value && Habitat.get(queryParam.value)}">
                                  <g:message code="text.in" />   <span class="highlight"><a
						href="${uGroup.createLink(
						controller:"observation", action:"list",
						params:[habitat: queryParam.value])}">
							${Habitat.get(queryParam.value).name} <a href="#"
							onclick="setDefaultHabitat(); return false;">[X]</a> </a> </span>  <g:message code="default.habitats.label" />
                            </g:if>
			</g:if>
			<g:elseif test="${queryParam.key == 'habitat' && queryParam.value}">
                           		 <g:message code="text.in" /> <span class="highlight"><a
					href="${uGroup.createLink(
					mapping:"userGroupGeneric", action:"list",
					params:[habitat: queryParam.value])}">
						${queryParam.value } <a href="#"
						onclick="setDefaultHabitat(); return false;">[X]</a> </a> </span> <g:message code="default.habitats.label" />
                           </g:elseif>



			<g:if
				test="${queryParam.key == 'taxon' && queryParam.value instanceof Long }">
				<g:if test="${queryParam.value && TaxonomyDefinition.read(queryParam.value)}">
                                  <g:message code="text.in" />   <span class="highlight"><a
						href="${uGroup.createLink(
						controller:params.controller, action:params.action,
						params:[taxon: queryParam.value])}">
                        ${TaxonomyDefinition.read(queryParam.value).name} </a>
                    
                        <a class="removeQueryFilter" data-target="${queryParam.key}" href="#">[X]</a>
                        </span>
                            </g:if>
			</g:if>
			<g:elseif test="${queryParam.key == 'taxon' && queryParam.value}">
                           		 <g:message code="text.in" /> <span class="highlight"><a
					href="${uGroup.createLink(
					controller:params.controller, action:params.action,
					params:[taxon: queryParam.value.id])}">
						${queryParam.value.name } </a>  
                 <a class="removeQueryFilter" data-target="${queryParam.key}" href="#">[X]</a>
                </span>
                           </g:elseif>

			<g:if
				test="${queryParam.key == 'dataset' && queryParam.value instanceof Long }">
				<g:if test="${queryParam.value && Dataset.read(queryParam.value)}">
                                  <g:message code="text.in" />   <span class="highlight"><a
						href="${uGroup.createLink(
						controller:params.controller, action:params.action,
						params:[dataset: queryParam.value, isMediaFilter:false])}">
                        ${Dataset.read(queryParam.value).title} </a>
                    
                        <a class="removeQueryFilter" data-target="${queryParam.key}" href="#">[X]</a>
                        </span>
                            </g:if>
			</g:if>
			<g:elseif test="${queryParam.key == 'dataset' && queryParam.value}">
                           		 <g:message code="text.in" /> <span class="highlight"><a
					href="${uGroup.createLink(
					controller:params.controller, action:params.action,
					params:[dataset: queryParam.value.id, isMediaFilter:false])}">
						${queryParam.value.title } </a>  
                 <a class="removeQueryFilter" data-target="${queryParam.key}" href="#">[X]</a>
                </span>
            </g:elseif>

			<g:if
				test="${queryParam.key == 'taxonRank' && queryParam.value instanceof Integer }">
				<g:if test="${queryParam.value && TaxonomyRank.values()[queryParam.value]}">
                                  <g:message code="text.in" />   <span class="highlight"><a
						href="${uGroup.createLink(
						controller:params.controller, action:"list",
						params:[taxonRank: queryParam.value])}">
                        ${TaxonomyRank.values()[queryParam.value].value()} </a>
                    
                        <a class="removeQueryFilter" data-target="${queryParam.key}" href="#">[X]</a>
                        </span>
                            </g:if>
			</g:if>
			<g:elseif test="${queryParam.key == 'taxonRank' && queryParam.value}">
                           		 <g:message code="text.in" /> <span class="highlight"><a
					href="${uGroup.createLink(
					controller:params.controller, action:'list',
					params:[taxonRank: queryParam.value])}">
						${queryParam.value } </a>  
                 <a class="removeQueryFilter" data-target="${queryParam.key}" href="#">[X]</a>
                </span>
                           </g:elseif>

            <g:if
                    test="${queryParam.key == 'status'}">
                    <g:if test="${queryParam.value}">
                    <g:message code="text.in" />   <span class="highlight"><a
                            href="${uGroup.createLink(
                            controller:params.controller, action:"list",
                            params:[status: queryParam.value])}">
                            ${queryParam.value} </a>

                        <a class="removeQueryFilter" data-target="${queryParam.key}" href="#">[X]</a>
                    </span>
                    </g:if>
            </g:if>



			<g:if test="${queryParam.key == 'tag' && queryParam.value}">
                                    <g:message code="text.tagged" />  <span class="highlight">
					<a
					href="${uGroup.createLink(controller:params.controller, action:"list",
					params:[tag: queryParam.value])}">
						${queryParam.value} <a id="removeTagFilter" href="#">[X]</a> </a> </span>
                        </g:if>
                                               <g:if test="${queryParam.key == 'featureBy' && queryParam.value}">
                                     <span class="highlight">
					<a
					href="${uGroup.createLink(controller:params.controller, action:"list",
					params:[featureBy: queryParam.value])}">
						 <g:message code="text.featured" /> <a class="removeQueryFilter" data-target="featureBy"
						href="#">[X]</a> </a> </span>
                        </g:if>


			<g:if
				test="${queryParam.key == 'user' && SUser.read(queryParam.value)}">
                                    <g:message code="text.by.user" />  <span class="highlight">
					<a
					href="${uGroup.createLink(controller:"user", action:"show", id:queryParam.value)}">
						${SUser.read(queryParam.value).name.encodeAsHTML()} <a
						id="removeUserFilter" href="#">[X]</a> </a> </span>
			</g:if>
			<g:if
				test="${!dateRangeSet && (queryParam.key == 'daterangepicker_start' || queryParam.key == 'daterangepicker_end')}">
                                    <g:message code="text.on.date" />  <span class="highlight">
                    <%
						dateRangeSet = true
						def startDate = queryParams.daterangepicker_start
						def endDate =  queryParams.daterangepicker_end 			
					%>                
					<a
					href="${uGroup.createLink(controller:params.controller, action:"list", params:[daterangepicker_start:startDate,daterangepicker_end:endDate])}">
						${'' + startDate + ' - ' + endDate} <a
						id="removeDateRange" href="#">[X]</a> </a> </span>
			</g:if>
			<g:if test="${queryParam.key == 'observation' && queryParam.value}">
                                     <g:message code="text.for" />  <span class="highlight">
					<a
					href="${uGroup.createLink(controller:"observation", action:"show",
					id:queryParam.value)}">
						 <g:message code="default.observation.label" /> <a id="removeObvFilter" href="#">[X]</a> </a> </span>
			</g:if>
			<g:if test="${(queryParam.key == 'query' || queryParam.key == 'q') && queryParam.value}">
                                    <g:message code="text.for.key" />  <span
					class="highlight"> <a
					href="${uGroup.createLink(controller:params.controller,
					action:params.action, params:[query: queryParam.value])}">
						${queryParam.value.encodeAsHTML()} <a class="removeQueryFilter" data-target="#searchTextField"
						href="#">[X]</a> </span>
			</g:if>
			
			<g:if test="${queryParam.key.startsWith('aq.') && queryParam.value && !queryParam.key.equalsIgnoreCase('aq.object_type')}">
                                    ${queryParam.key.replace('aq.','')}:<span
					class="highlight"> <a
					href="${uGroup.createLink(controller:params.controller,
					action:params.action, params:[(queryParam.key): queryParam.value])}">
						${queryParam.value.encodeAsHTML()} <a class="removeQueryFilter" data-target="${queryParam.key}"
						href="#">[X]</a> </span>
			</g:if>
			<g:if test="${queryParam.key=='trait' && queryParam.value && hackTohideTraits != true}"> 
            <g:message code="trait.for.key" />  
            <g:each in="${queryParam.value}" var="trait">
            <g:each in="${trait.value.split(',')}" var="tv">
                                    <span
					class="highlight"> <a
					href="${uGroup.createLink(controller:params.controller,
					action:params.action, params:[('trait.'+trait.key): tv])}">
						${Trait.read(trait.key)?.name}:${tv.equalsIgnoreCase('none')||tv.equalsIgnoreCase('all')||tv.equalsIgnoreCase('any')?tv.capitalize():TraitValue.read(tv)?.value} 
                        <a class="removeQueryFilter" data-target="trait.${trait.key}=${tv}" href="#">[X]</a> 
                        </span>
                        </g:each>
                        </g:each>
			</g:if>


		</g:each>

</div>
