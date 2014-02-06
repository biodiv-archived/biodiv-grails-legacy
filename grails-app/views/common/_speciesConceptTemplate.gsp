<%@page import="species.participation.ActivityFeedService"%>
<%@page import="species.Reference"%>
<%@page import="species.TaxonomyDefinition.TaxonomyRank"%>

<div class="sidebar_section  <%=sparse?'':'menubutton'%>">
    <g:set var="fieldCounter" value="${1}" />
    <a href="#content" <%=sparse?'style=\"display:none\"':''%>> ${concept.key} </a>

    <!-- speciesConcept section -->

    <div
        class="speciesConcept <%=concept.key.equals(grailsApplication.config.speciesPortal.fields.OVERVIEW)?'defaultSpeciesConcept':''%>"
        id="speciesConcept${conceptCounter}" <%=sparse?'':'style=\"display:none\"'%>>
        <a class="speciesFieldHeader" data-toggle="collapse" data-parent="#speciesConcept${conceptCounter++}"  href="#speciesField${conceptCounter}_${fieldCounter}"> <h5>${concept.key}</h5></a> 

        <!-- speciesField section -->
        <div id="speciesField${conceptCounter}_${fieldCounter++}"
            class="speciesField collapse in">
            <g:if test="${concept.value.containsKey('speciesFieldInstance')}">

            <s:isContributor model="['speciesFieldInstance':speciesFieldInstance, 'fieldInstance':concept.value.get('field')]">
            <a href="#" class="addField"  data-pk="${concept.value.get('field').id}" data-type="wysihtml5" data-url="${uGroup.createLink(controller:'species', action:'update') }" data-name="newdescription" data-params="{'speciesId':${speciesInstance.id}}" data-original-title="Add new description" data-placeholder="Add new description"></a>
            </s:isContributor>
            <g:each in="${ concept.value.get('speciesFieldInstance')}" var="speciesFieldInstance">
            <g:showSpeciesField
            model="['speciesFieldInstance':speciesFieldInstance, 'speciesId':speciesInstance.id, 'fieldInstance':concept.value.get('field')]" />
            </g:each>
            </g:if>
            <g:else>
            <g:each in="${concept.value}" var="category">

            <s:hasContent model="['map':category.value]">
            <g:if test="${!category.key.equalsIgnoreCase(grailsApplication.config.speciesPortal.fields.SUMMARY)}">

            <div id="speciesField${conceptCounter}_${fieldCounter++}" class="clearfix speciesCategory">

                <h6><a class="category-header-heading speciesFieldHeader" href="#speciesField${conceptCounter}_${fieldCounter}"> ${category.key}</a></h6>
                <g:if test="${category.value.containsKey('field') && !category.key.equalsIgnoreCase(grailsApplication.config.speciesPortal.fields.OCCURRENCE_RECORDS) && !category.key.equalsIgnoreCase(grailsApplication.config.speciesPortal.fields.REFERENCES)}">
                    <s:isContributor model="['speciesFieldInstance':speciesFieldInstance, 'fieldInstance':category.value.get('field')]">
                        <a href="#" class="addField"  data-pk="${category.value.get('field').id}" data-type="wysihtml5" data-url="${uGroup.createLink(controller:'species', action:'update') }" data-name="newdescription" data-params="{'speciesId':${speciesInstance.id}}" data-original-title="Add new description" data-placeholder="Add new description"></a>
                    </s:isContributor>
                </g:if>

                <div 
                    class="<%=category.key.equals(grailsApplication.config.speciesPortal.fields.BRIEF)?'defaultSpeciesField':''%> speciesField">
                    <div>   
                    <g:if test="${category.value.containsKey('speciesFieldInstance') || category.key.equalsIgnoreCase(grailsApplication.config.speciesPortal.fields.OCCURRENCE_RECORDS) || category.key.equalsIgnoreCase(grailsApplication.config.speciesPortal.fields.REFERENCES)}">
                    <g:if
                    test="${category.key.equalsIgnoreCase(grailsApplication.config.speciesPortal.fields.OCCURRENCE_RECORDS)}">
                    <g:render template="/species/showSpeciesOccurences" model="['speciesInstance':speciesInstance, 'userGroupInstance':userGroupInstance, 'category':category]"/> 
                    </g:if>

                    <g:elseif test="${category.key.equalsIgnoreCase(grailsApplication.config.speciesPortal.fields.REFERENCES)}">
                    <g:render template="/species/showSpeciesReferences" model="['speciesInstance':speciesInstance, 'userGroupInstance':userGroupInstance, 'category':category]"/> 
                    </g:elseif>

                    <g:else>
                    <g:each in="${category.value.get('speciesFieldInstance')}" var="speciesFieldInstance">
                    <g:showSpeciesField
                    model="['speciesInstance' : speciesInstance, 'speciesFieldInstance':speciesFieldInstance, 'speciesId':speciesInstance.id, 'fieldInstance':category.value.get('field')]" />
                    </g:each>
                    </g:else>
                    </g:if>

                    <g:each in="${category.value}">
                    <g:if test="${((it.key.equals(grailsApplication.config.speciesPortal.fields.GLOBAL_DISTRIBUTION_GEOGRAPHIC_ENTITY))||
                    (it.key.equals(grailsApplication.config.speciesPortal.fields.GLOBAL_ENDEMICITY_GEOGRAPHIC_ENTITY))||
                    (it.key.equals(grailsApplication.config.speciesPortal.fields.INDIAN_DISTRIBUTION_GEOGRAPHIC_ENTITY)) ||
                    (it.key.equals(grailsApplication.config.speciesPortal.fields.INDIAN_ENDEMICITY_GEOGRAPHIC_ENTITY))
                    ) && category.value }">
                    <g:showSpeciesField
                    model="['speciesInstance': speciesInstance, 'speciesFieldInstance':it.value.get('speciesFieldInstance')?.getAt(0), 'speciesId':speciesInstance.id, 'fieldInstance':it.value.get('field')]" />
                    </g:if>
                    <g:elseif
                    test="${!it.key.equals('field') && !it.key.equals('speciesFieldInstance') && !it.key.equals('hasContent')}">

                    <g:each in="${ it.value.get('speciesFieldInstance')}" var="speciesFieldInstance">
                    <g:showSpeciesField
                    model="['speciesInstance': speciesInstance, 'speciesFieldInstance':speciesFieldInstance, 'speciesId':speciesInstance.id, 'fieldInstance':it.value.get('field')]" />
                    </g:each>
                    </g:elseif>
                    </g:each>
                </div>
                </div>
                <br/>
            </div>
            </g:if>
            </s:hasContent>
            </g:each>

            </g:else>

        </div>

    </div>
</div>

