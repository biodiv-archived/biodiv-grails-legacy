<%@page import="species.Resource"%>
<%@page import="species.Resource.ResourceType"%>
<s:isSpeciesFieldContributor model="['speciesFieldInstance':speciesFieldInstance]">
    <g:if test="${isSpeciesContributor}">
        <g:set var="isSpeciesFieldContributor" value="${Boolean.TRUE}"/>
    </g:if>
</s:isSpeciesFieldContributor>

<g:if test="${speciesFieldInstance.language.id != userLanguage?.id}">
<div class="alert alert-info"><g:message code="default.content.msg" />  <a href="javascript:void(0);" class="clickcontent btn btn-mini" data-target="clickcontent_${speciesFieldInstance.language.id}">${speciesFieldInstance.language.threeLetterCode?.toUpperCase()}</a></div>
</g:if>


<div class="speciesField ${(speciesFieldInstance.description)?'':'dummy hide'} ${speciesFieldInstance.language.id == userLanguage?.id ?:'hide clickcontent_'+speciesFieldInstance.language.id}" data-name="speciesField" data-act ="${speciesFieldInstance.description ? (speciesFieldInstance.language.id == userLanguage.id ? 'edit':''):'add'}" data-speciesid="${speciesInstance?.id}" data-pk="${speciesFieldInstance.id?:speciesFieldInstance.field.id}">
    <g:if test="${isSpeciesContributor}">
    <!--a style="margin-right: 5px;" class="pull-right speciesFieldMedia btn" onclick='getSpeciesFieldMedia("${speciesInstance?.id}","${speciesFieldInstance.id?:speciesFieldInstance.field.id}", "fromSingleSpeciesField","${createLink(controller:'species',  action:'getSpeciesFieldMedia')}" )'>Add Media</a-->
    </g:if>
    <div class="contributor_entry">
        <!-- buttons -->
        <div class="pull-right">
        </div>

        <!-- icons -->
        <div class="icons">
            <g:each in="${speciesFieldInstance.resources}" var="r">
            <g:if test="${r.type == species.Resource.ResourceType.ICON}">

            <%def imagePath = r.fileName.trim().replaceFirst(/\.[a-zA-Z]{3,4}$/, grailsApplication.config.speciesPortal.resources.images.thumbnail.suffix)%>

            <img class="icon"
            src="${createLinkTo(file: imagePath, base:grailsApplication.config.speciesPortal.resources.serverURL)}"
            title="${r?.description}" />

            </g:if>
            </g:each>
        </div>



        <!-- images -->

        <g:if test="${speciesFieldInstance.resources?.size()>0}">
        <g:if test="${speciesFieldInstance.resources?.size() > 1 }">
        <ul class="thumbwrap" style="width: 50%; float: right;">
            </g:if>
            <g:else>
            <ul class="thumbwrap" style="float: right;">
                </g:else>
                <g:each in="${speciesFieldInstance.resources}" var="r">
                <g:if test="${r.type == species.Resource.ResourceType.IMAGE}">
                <li class="figure" style="list-style: none;">


                <div class="attributionBlock dropdown"
                    style="text-align: right; margin-right: 3px;">
                    <span href="#" class="dropdown-toggle" data-toggle="dropdown"
                        title="Show details"><i class=" icon-info-sign"></i> </span>

                    <div class="dropdown-menu">
                        <g:imageAttribution model="['resource':r]" />
                    </div>
                </div>
                <div>
                    <%def imagePath = r.fileName.trim().replaceFirst(/\.[a-zA-Z]{3,4}$/, grailsApplication.config.speciesPortal.resources.images.thumbnail.suffix)%>
                    <%  def basePath 
                        if(r.context.value() == Resource.ResourceContext.OBSERVATION.toString()){
                            basePath = grailsApplication.config.speciesPortal.observations.serverURL
                        }
                        else if(r.context.value() == Resource.ResourceContext.SPECIES.toString() || r.context.value() == Resource.ResourceContext.SPECIES_FIELD.toString()){
                            basePath = grailsApplication.config.speciesPortal.resources.serverURL
                        }
                    %>

                    <a target="_blank"
                        href="${createLinkTo(file: r.fileName.trim(), base:basePath)}">
                        <span class="wrimg"> <span></span> <img
                            class="galleryImage"
                            src="${createLinkTo(file: imagePath, base:basePath)}"
                            title="${r?.description}" /> </span> </a> <span class="caption">
                        ${r?.description} </span>
                </div>
                </li>

                </g:if>
                </g:each>
            </ul>
            </g:if>
            <!-- content -->
            <textarea id="description_${speciesFieldInstance.id?speciesFieldInstance.id:'f'+speciesFieldInstance.field.id}" name="description" 
                class="${(isSpeciesFieldContributor && speciesFieldInstance.language.id == userLanguage?.id)?'ck_desc':''}"
                data-pk="${speciesFieldInstance.id}"
                data-type="ckeditor"
                data-url="${uGroup.createLink(controller:'species', action:'update') }"
                data-name="description" 
                placeholder="Write a small descripiton about the field." style="display:none;">
                ${raw(speciesFieldInstance?.description)}

            </textarea>        

            <div class="description">
                ${raw(speciesFieldInstance?.description)}
            </div>
<!-- description -->
<g:if
test="${speciesFieldInstance?.field.subCategory?.equalsIgnoreCase('Global Distribution Geographic Entity') && speciesInstance.globalDistributionEntities.size()>0}">
<div>
    <h6 style="margin-bottom: 0px">
        ${speciesFieldInstance?.field?.subCategory}
    </h6>
</div>

<g:each in="${speciesInstance.globalDistributionEntities}">
<p>
<span class=""> ${it?.country.countryName} (${it?.country.twoLetterCode})
</span>
</p>
</g:each>
</g:if>
<g:elseif
test="${speciesFieldInstance?.field.subCategory?.equalsIgnoreCase('Global Endemicity Geographic Entity') && speciesInstance.globalEndemicityEntities.size() > 0}">
<div>
    <h6 style="margin-bottom: 0px">
        ${speciesFieldInstance?.field?.subCategory}
    </h6>
</div>

<g:each in="${speciesInstance.globalEndemicityEntities}">
<p>
<span class=""> ${it?.country.countryName} (${it?.country.twoLetterCode})
</span>
</p>
</g:each>
</g:elseif>
<g:elseif
test="${!speciesFieldInstance?.description && !speciesFieldInstance?.field?.subCategory}">

</g:elseif>

<g:showSpeciesFieldToolbar
model="['speciesFieldInstance':speciesFieldInstance, 'isSpeciesFieldContributor':isSpeciesFieldContributor, 'isCurator':isCurator]" />
<g:if test="${speciesFieldInstance != null}">
<comment:showCommentPopup
model="['commentHolder':speciesFieldInstance, 'rootHolder':speciesInstance]" />
</g:if>


        </div>


    </div>
