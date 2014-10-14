<%@page import="species.participation.ActivityFeedService"%>
<%@page import="species.Reference"%>
<%@page import="species.TaxonomyDefinition.TaxonomyRank"%>

<div class="sidebar_section  <%=sparse?'':'menubutton'%>  ${concept.value.hasContent?'':'emptyField'}" <%=concept.value.hasContent?'':'style=\"display:none\"'%>  ">
    <g:set var="fieldCounter" value="${1}" />
    <a href="#content" <%=sparse?'style=\"display:none\"':''%>> ${concept.key} </a>

    <!-- speciesConcept section -->

    <div
        class="speciesConcept ${concept.value.hasContent?'':'emptyField'}"
        id="speciesConcept${conceptCounter}" <%=sparse|| concept.value.hasContent?'':'style=\"display:none\"'%>>
        <a class="speciesFieldHeader" data-toggle="collapse" data-parent="#speciesConcept${conceptCounter++}"  href="#speciesField${conceptCounter}_${fieldCounter}"> <h5>${concept.key}</h5></a> 

        <!-- speciesField section -->
        <div id="speciesField${conceptCounter}_${fieldCounter++}"
            class="speciesField collapse in">
            <g:if test="${concept.value.containsKey('speciesFieldInstance')}">

            <g:if test="${isSpeciesContributor && concept.value.isContributor!=2}">
            <g:render template="/species/newSpeciesFieldTemplate" model="[fieldInstance:concept.value.get('field'), speciesInstance:speciesInstance, newSpeciesFieldInstance:newSpeciesFieldInstance, isSpeciesContributor:isSpeciesContributor]"/>
            </g:if>
            <g:each in="${ concept.value.get('speciesFieldInstance')}" var="speciesFieldInstance">
            <g:showSpeciesField
            model="['speciesInstance':speciesInstance, 'speciesFieldInstance':speciesFieldInstance, 'speciesId':speciesInstance.id, 'fieldInstance':concept.value.get('field'), 'isSpeciesContributor':isSpeciesContributor]" />
            </g:each>
            </g:if>
            <g:else>
            <g:each in="${concept.value}" var="category">
            <s:hasContent model="['map':category.value]">
            <g:if test="${!category.key.equalsIgnoreCase(fieldFromName.summary)}">

            <div id="speciesField${conceptCounter}_${fieldCounter++}" class="clearfix speciesCategory ${category.value.hasContent?'':'emptyField'}" <%=category.value.hasContent?'':'style=\"display:none\"'%> >
                <h6>
                    <a class="category-header-heading speciesFieldHeader" href="#speciesField${conceptCounter}_${fieldCounter}"> ${category.key}</a>
                </h6>
                <div>
                <g:if test="${category.value.containsKey('field') && !category.key.equalsIgnoreCase(fieldFromName.occurrenceRecords) && !category.key.equalsIgnoreCase(fieldFromName.references) && isSpeciesContributor && category.value.isContributor!=2}">
                <g:render template="/species/newSpeciesFieldTemplate" model="[fieldInstance:category.value.get('field'), speciesInstance:speciesInstance, newSpeciesFieldInstance:newSpeciesFieldInstance,  isSpeciesContributor:isSpeciesContributor]"/>
                </g:if>



                <div 
                    class="<%=category.key.equals(brief)?'defaultSpeciesField':''%> speciesField">
                    <div>   
                    <g:if test="${category.value.containsKey('speciesFieldInstance') || category.key.equalsIgnoreCase(fieldFromName.occurrenceRecords) || category.key.equalsIgnoreCase(fieldFromName.references)}">
                    <g:if
                    test="${category.key.equalsIgnoreCase(fieldFromName.occurrenceRecords)}">
                    <g:render template="/species/showSpeciesOccurences" model="['speciesInstance':speciesInstance, 'userGroupInstance':userGroupInstance, 'category':category]"/> 
                    </g:if>

                    <g:elseif test="${category.key.equalsIgnoreCase(fieldFromName.references)}">
                    <g:render template="/species/showSpeciesReferences" model="['speciesInstance':speciesInstance, 'userGroupInstance':userGroupInstance, 'category':category, 'isSpeciesContributor':isSpeciesContributor, 'isSpeciesFieldContributor':isSpeciesFieldContributor]"/> 
                    </g:elseif>

                    <g:else>
                    <g:each in="${category.value.get('speciesFieldInstance')}" var="speciesFieldInstance">
                    <g:showSpeciesField
                    model="['speciesInstance' : speciesInstance, 'speciesFieldInstance':speciesFieldInstance, 'speciesId':speciesInstance.id, 'fieldInstance':category.value.get('field'), 'isSpeciesContributor':isSpeciesContributor]" />
                    </g:each>
                    </g:else>
                    </g:if>

                    <g:each in="${category.value}">
                    <g:if test="${((it.key.equals(fieldFromName.gdge))|| (it.key.equals(fieldFromName.gege))|| (it.key.equals(fieldFromName.idge)) || (it.key.equals(fieldFromName.iege)) ) && category.value }">
                    <g:showSpeciesField
                    model="['speciesInstance': speciesInstance, 'speciesFieldInstance':it.value.get('speciesFieldInstance')?.getAt(0), 'speciesId':speciesInstance.id, 'fieldInstance':it.value.get('field'), 'isSpeciesContributor':isSpeciesContributor]" />
                    </g:if>
                    <g:elseif
                    test="${!it.key.equals('field') && !it.key.equals('speciesFieldInstance') && !it.key.equals('hasContent') && !it.key.equals('isContributor')  && !it.key.equals('lang') }">

                    <div class="clearfix speciesSubCategory ${it.value.hasContent?'':'emptyField'}" <%=it.value.hasContent?'':'style=\"display:none\"'%> >
                        <g:if test="${it.value.field?.subCategory}">
                        <h6 style="margin-bottom: 0px">
                            ${it.value.field?.subCategory}
                        </h6>
                        <div>
                            <g:if test="${isSpeciesContributor && it.value.isContributor != 2}">

                                <g:render template="/species/newSpeciesFieldTemplate" model="[fieldInstance:it.value.get('field'), speciesInstance:speciesInstance, newSpeciesFieldInstance:newSpeciesFieldInstance,  isSpeciesContributor:isSpeciesContributor]"/>
                           </g:if>
                        </div>
                        </g:if>
                        <g:each in="${ it.value.get('speciesFieldInstance')}" var="speciesFieldInstance">
                        <g:showSpeciesField
                        model="['speciesInstance': speciesInstance, 'speciesFieldInstance':speciesFieldInstance, 'speciesId':speciesInstance.id, 'fieldInstance':it.value.get('field'), 'isSpeciesContributor':isSpeciesContributor, 'userLanguage':userLanguage]" />
                        </g:each>
                    </div>
                    </g:elseif>
                    </g:each>
                </div>
                </div>
                <br/>
            </div>
            </div>
            </g:if>
            </s:hasContent>
            </g:each>

            </g:else>

        </div>

    </div>
</div>

