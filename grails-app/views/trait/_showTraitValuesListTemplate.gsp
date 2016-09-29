<div class="groupsWithSharingNotAllowed btn-group userGroups" style="white-space:inherit;">
            <g:if test="${!displayAny}">
            <div data-tvid='all' data-tid='${traitValues[0].trait.id}'
                class="btn all ${queryParams.trait && traitValues && queryParams.trait[traitValues[0].trait.id+'']?:'active btn-success'}"
                value="all"
                style="padding: 0px; height: 36px; border-radius: 6px; margin:5px;width:${showDetails?'auto':'165px;'} max-width:${showDetails?'auto':'165px;'}; line-height:40px;">
                <img
                    class="${showDetails ? 'normal_profile_pic' : 'user-icon small_profile_pic'}" style="float:left;"
                    src="${grailsApplication.config.speciesPortal.traits.serverURL}/32/32_any.png" title="All"
                    alt="all" />                     
                    <span class="ellipsis  " style="display: block; text-align: left; width: 100px; float: left; margin-left: 6px; position: static;line-height: 30px;" title="Erected"> Any </span>
            </div> 
            </g:if>
        <g:each in="${traitValues}" var="traitValue" status="i">
                    <button type="button" data-tvid='${traitValue.id}' data-tid='${traitValue.trait.id}'
                    class="btn input-prepend single-post ${queryParams?.trait?(queryParams.trait[traitValue.trait.id+'']==traitValue.id+''?'active btn-success':''):''}"
                        value="${traitValue.id}"
                        style="padding: 0px; height: 36px; border-radius: 6px; margin:5px;">

                        <g:render template="/trait/showTraitValueSignatureTemplate" model="['traitValue':traitValue]"/>
                    </button> 
        </g:each>
</div>


