<div class="groupsWithSharingNotAllowed btn-group userGroups" style="white-space:inherit;">
            <g:if test="${!displayAny}">
            <div data-tvid='all' data-tid='${traitValues[0].trait.id}'
                class="btn all ${queryParams.trait && traitValues && queryParams.trait[traitValues[0].trait.id+'']?'':'active btn-success'}"
                value="all"
                style="padding: 0px; height: 36px; border-radius: 6px; margin:5px;width:${showDetails?'auto':'165px;'} max-width:${showDetails?'auto':'165px;'}; line-height:30px;text-align:left;">
                <img
                    class="${showDetails ? 'normal_profile_pic' : 'user-icon small_profile_pic'}" style="float:left;"
                    src="http://pamba.strandls.com/biodiv/traits/32/32_any.png" title="All"
                    alt="all" /> 
                    All
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
            <g:if test="${!displayAny}">
            <div data-tvid='none' data-tid='${traitValues[0].trait.id}'
                class="btn none ${queryParams.trait && traitValues && queryParams.trait[traitValues[0].trait.id+'']=='none'?'active btn-success':''}"
                value="none"
                style="padding: 0px; height: 36px; border-radius: 6px; margin:5px;width:${showDetails?'auto':'165px;'} max-width:${showDetails?'auto':'165px;'}; line-height:30px;text-align:left;">
                <img
                    class="${showDetails ? 'normal_profile_pic' : 'user-icon small_profile_pic'}" style="float:left;"
                    src="http://pamba.strandls.com/biodiv/traits/32/32_none.png" title="Undefined"
                    alt="Undefined" />
                Undefined
            </div> 
            </g:if>
</div>


