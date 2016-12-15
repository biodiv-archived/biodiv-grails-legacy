<div class="row btn-group" style="white-space:inherit;margin-left:20px;">
            <g:if test="${displayAny}">
            <div data-tvid='all' data-tid='${traitValues?traitValues[0]?.trait?.id:''}'
                class="btn span2 all ${queryParams.trait && traitValues && queryParams.trait[traitValues[0]?.trait?.id+'']?'':'active btn-success'}"
                value="all"
                style="padding: 0px; height: 36px; border-radius: 6px; margin:5px;}; line-height:30px;text-align:left;">
                <img
                    class="${showDetails ? 'normal_profile_pic' : 'user-icon small_profile_pic'}"
                    src="${grailsApplication.config.speciesPortal.traits.serverURL}/32/32_any.png" title="All"
                    alt="all" /> 
                    All
           </div> 
            </g:if>
        
        <g:if test="${fromSpeciesShow!=true}">
            <g:each in="${traitValues}" var="traitValue" status="i">
                    <button type="button" id="value_btn_${traitValue.id}" data-tvid='${traitValue.id}' data-tid='${traitValue?.trait.id}'
                    class="btn span2 input-prepend single-post ${traitTypes} ${queryParams && queryParams.trait && traitValue && queryParams.trait[traitValue?.trait.id]?.contains(traitValue.id+'')?'active btn-success':''}"
                        value="${traitValue.value}"
                        style="padding: 0px; height: 36px; border-radius: 6px; margin:5px;">
                        <g:render template="/trait/showTraitValueSignatureTemplate" model="['traitValue':traitValue]"/>
                    </button>
            </g:each> 
        </g:if>
        
        <g:if test="${fromSpeciesShow==true}">
            <g:each in="${traitValues}" var="traitValue" status="i">
                <% String link="${"/trait/show/"+traitValue.trait.id+"?trait."+traitValue.trait.id+"="+traitValue.id}" %>
                <a href='${link}'>
                    <button type="button" id="value_btn_${traitValue.id}" data-tvid='${traitValue.id}' data-tid='${traitValue.trait.id}'
                    class="btn span2 input-prepend single-post ${queryParams && queryParams.trait && traitValue && queryParams.trait[traitValue.trait.id]?.contains(traitValue.id+'')?'active btn-success':''}"
                        value="${traitValue.id}"
                        style="padding: 0px; height: 36px; border-radius: 6px; margin:5px;">

                        <g:render template="/trait/showTraitValueSignatureTemplate" model="['traitValue':traitValue]"/>
                    </button> 
                </a>
            </g:each>
        </g:if>
        
            <g:if test="${displayAny}">
            <div data-tvid='none' data-tid='${traitValues? traitValues[0]?.trait?.id:''}'
                class="btn span2 none ${queryParams.trait && traitValues && queryParams.trait[traitValues[0].trait.id+'']=='none'?'active btn-success':''}"
                value="none"
                style="padding: 0px; height: 36px; border-radius: 6px; margin:5px;line-height:30px;text-align:left;">
                <img
                    class="${showDetails ? 'normal_profile_pic' : 'user-icon small_profile_pic'}" style="float:left;"
                    src="${grailsApplication.config.speciesPortal.traits.serverURL}/32/32_none.png" title="Undefined"
                    alt="Undefined" />
                Undefined
            </div> 
            </g:if>
</div>

