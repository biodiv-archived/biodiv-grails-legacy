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
            <g:each in="${ concept.value.get('speciesFieldInstance')}" var="speciesFieldInstance">
            <g:showSpeciesField
            model="['speciesFieldInstance':speciesFieldInstance, 'speciesId':speciesInstance.id]" />
            </g:each>
            </g:if>
            <g:else>
            <g:each in="${concept.value}" var="category">
            <g:if test="${!category.key.equalsIgnoreCase(grailsApplication.config.speciesPortal.fields.SUMMARY)}">

            <div id="speciesField${conceptCounter}_${fieldCounter++}" class="clearfix speciesCategory">
                <h6>
                    <a class="category-header-heading speciesFieldHeader" href="#speciesField${conceptCounter}_${fieldCounter}"> ${category.key}</a></h6>
                    <a class="addFieldButton btn btn-link">Add</a>
                <div 
                    class="<%=category.key.equals(grailsApplication.config.speciesPortal.fields.BRIEF)?'defaultSpeciesField':''%> speciesField  ">

                    <g:if test="${category.value.containsKey('speciesFieldInstance') || category.key.equalsIgnoreCase(grailsApplication.config.speciesPortal.fields.OCCURRENCE_RECORDS) || category.key.equalsIgnoreCase(grailsApplication.config.speciesPortal.fields.REFERENCES)}">
                    <g:if
                    test="${category.key.equalsIgnoreCase(grailsApplication.config.speciesPortal.fields.OCCURRENCE_RECORDS)}">
                    <g:render template="/species/showSpeciesOccurences" model="['speciesInstance':speciesInstance, 'userGroupInstance':userGroupInstance, 'category':category]"/> 
                    </g:if>

                    <g:elseif test="${category.key.equalsIgnoreCase(grailsApplication.config.speciesPortal.fields.REFERENCES)}">
                    <g:render template="/species/showSpeciesReferences" model="['speciesInstance':speciesInstance, 'userGroupInstance':userGroupInstance, 'category':category]"/> 
                    </g:elseif>

                    <g:else>
                    <g:each in="${ category.value.get('speciesFieldInstance')}" var="speciesFieldInstance">
                    <g:showSpeciesField
                    model="['speciesInstance' : speciesInstance, 'speciesFieldInstance':speciesFieldInstance, 'speciesId':speciesInstance.id]" />
                    </g:each>
                    </g:else>
                    </g:if>

                    <g:each in="${category.value}">
                    <g:if test="${((it.key.equals(grailsApplication.config.speciesPortal.fields.GLOBAL_DISTRIBUTION_GEOGRAPHIC_ENTITY))||
                    (it.key.equals(grailsApplication.config.speciesPortal.fields.GLOBAL_ENDEMICITY_GEOGRAPHIC_ENTITY))||
                    (it.key.equals(grailsApplication.config.speciesPortal.fields.INDIAN_DISTRIBUTION_GEOGRAPHIC_ENTITY)) ||
                    (it.key.equals(grailsApplication.config.speciesPortal.fields.INDIAN_ENDEMICITY_GEOGRAPHIC_ENTITY))
                    ) && category.value}">
                    <g:showSpeciesField
                    model="['speciesInstance': speciesInstance, 'speciesFieldInstance':it.value.get('speciesFieldInstance')?.getAt(0), 'speciesId':speciesInstance.id]" />
                    </g:if>
                    <g:elseif
                    test="${!it.key.equals('field') && !it.key.equals('speciesFieldInstance')}">
                    <g:each in="${ it.value.get('speciesFieldInstance')}" var="speciesFieldInstance">

                    ${speciesFieldInstance}
                    <g:showSpeciesField
                    model="['speciesInstance': speciesInstance, 'speciesFieldInstance':speciesFieldInstance, 'speciesId':speciesInstance.id]" />
                    </g:each>
                    </g:elseif>
                    </g:each>
                </div>
                <br/>
            </div>
            </g:if>
            </g:each>

            </g:else>

        </div>

    </div>
</div>

