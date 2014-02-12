<div>
<s:isSpeciesFieldContributor model="['speciesFieldInstance':speciesFieldInstance]">
    <g:set var="isSpeciesFieldContributor" value="${Boolean.TRUE}"/>
    </s:isSpeciesFieldContributor>
        <g:if test="${speciesFieldInstance?.field?.subCategory}">
        <h6 style="margin-bottom: 0px">
            ${speciesFieldInstance?.field?.subCategory}
        </h6>
        <g:if test="${isSpeciesContributor}">
        <div>
        <a href="#" class="addField"  data-pk="${fieldInstance.id}" data-type="wysihtml5" data-url="${uGroup.createLink(controller:'species', action:'update') }" data-name="newdescription" data-params="{'speciesId':${speciesInstance.id}}" data-original-title="Add new description" data-placeholder="Add new description"></a>
    </div>
        </g:if>
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
                    <a target="_blank"
                        href="${createLinkTo(file: r.fileName.trim(), base:grailsApplication.config.speciesPortal.resources.serverURL)}">
                        <span class="wrimg"> <span></span> <img
                            class="galleryImage"
                            src="${createLinkTo(file: imagePath, base:grailsApplication.config.speciesPortal.resources.serverURL)}"
                            title="${r?.description}" /> </span> </a> <span class="caption">
                        ${r?.description} </span>
                </div>
                </li>

                </g:if>
                </g:each>
            </ul>
            </g:if>
            <!-- content -->

<div class="${isSpeciesFieldContributor?'editField ':' '} description" data-type="wysihtml5"
    data-pk="${speciesFieldInstance.id}"
    data-url="${uGroup.createLink(controller:'species', action:'update') }"
    data-name="description" data-original-title="Edit description">
    <g:each in="${speciesFieldInstance?.description.split('\n')}"
    var="para">
    <g:if test="${para}">
    <p>
    ${para.trim()}
    </p>
    </g:if>

    </g:each>

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
model="['speciesFieldInstance':speciesFieldInstance, 'isSpeciesContributor':isSpeciesContributor, 'isSpeciesFieldContributor':isSpeciesFieldContributor, 'isCurator':isCurator]" />
<g:if test="${speciesFieldInstance != null}">
<comment:showCommentPopup
model="['commentHolder':speciesFieldInstance, 'rootHolder':speciesInstance]" />
</g:if>


        </div>


    </div>
