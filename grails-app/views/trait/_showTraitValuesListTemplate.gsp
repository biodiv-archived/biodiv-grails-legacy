<%@page import="species.trait.Trait.TraitTypes"%>
<%@page import="species.trait.Trait.DataTypes"%>
<%@page import="species.trait.TraitValue"%>

<div class="row btn-group" style="white-space:inherit;margin-left:20px;">
<g:if test="${traitValues && traitValues.size()>0}">
            <g:if test="${displayAny && trait.isNotObservationTrait}">
            <div data-tvid='all' data-tid='${traitValues?trait?.id:''}'
                class="btn span2 all ${queryParams.trait && traitValues && queryParams.trait[trait?.id+'']?'':'active btn-success'}"
                value="all"
                style="padding: 0px; height: 36px; border-radius: 6px; margin:5px;}; line-height:30px;text-align:left;">
                <img
                    class="${showDetails ? 'normal_profile_pic' : 'user-icon small_profile_pic'}"
                    src="${grailsApplication.config.speciesPortal.traits.serverURL}/32/32_any.png" title="All"
                    alt="all" /> 
                    All
           </div> 
            </g:if>
            <g:if test="${fromTraitShow}">
            <div data-tvid='any' data-tid='${traitValues&&trait?trait?.id:''}'
                class="btn span2 any ${queryParams.trait && traitValues && queryParams.trait[trait?.id+'']?'':'active btn-success'}"
                value="any"
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
                    <button type="button" id="value_btn_${traitValue.id}" data-tvid='${traitValue.id}' data-tid='${traitValue?.trait.id}' data-isNotObservation='${traitValue.trait.isNotObservationTrait}'
                    class="btn span2 input-prepend single-post ${traitTypes} ${queryParams && queryParams.trait && traitValue && queryParams.trait[traitValue?.trait.id]?.contains(traitValue.id+'')?'active btn-success':''}"
                        value="${traitValue.value}"
                        style="padding: 0px; height: 36px; border-radius: 6px; margin:5px;width:inherit;">
                        <g:render template="/trait/showTraitValueSignatureTemplate" model="['traitValue':traitValue]"/>
                    </button>
            </g:each> 
        </g:if>
        
        <g:if test="${fromSpeciesShow==true}">
            <g:each in="${traitValues}" var="traitValue" status="i">
            <g:if test="${(traitValue instanceof TraitValue)}">
                <% String link="${"/trait/show/"+trait.id+"?trait."+trait.id+"="+traitValue.id}" %>
                <a href='${link}'>
                    <button type="button" id="value_btn_${traitValue.id}" data-tvid='${traitValue.id}' data-tid='${traitValue.trait.id}' data-isnotobservation='${traitValue.trait.isNotObservationTrait}'
                    class="btn span2 input-prepend single-post ${queryParams && queryParams.trait && traitValue && queryParams.trait[traitValue.trait.id]?.contains(traitValue.id+'')?'active btn-success':''}"
                        value="${traitValue.id}"
                        style="padding: 0px; height: 36px; border-radius: 6px; margin:5px;">

                        <g:render template="/trait/showTraitValueSignatureTemplate" model="['traitValue':traitValue]"/>
                    </button> 
                </a>
                </g:if>
                <g:else>
                    <g:if test="${trait.traitTypes == TraitTypes.RANGE && trait.dataTypes == DataTypes.DATE}">
                    <span data-tid='${trait.id}' data-isnotobservation='${trait.isNotObservationTrait}'
                    class="btn span2 input-prepend single-post disabled"
                        style="padding: 0px; height: 36px; border-radius: 6px; margin:5px;">
                        ${traitValue}
                    </span>
                    </g:if>
                    <g:else>
                    <span data-tid='${trait.id}' data-isnotobservation='${trait.isNotObservationTrait}'
                    class="btn span2 input-prepend single-post disabled"
                        style="padding: 0px; height: 36px; border-radius: 6px; margin:5px;">
                        ${traitValue}
                    </span>
                    </g:else>
                    </g:else>
            </g:each>
        </g:if>
        
            <g:if test="${displayAny && trait.isNotObservationTrait}">
            <div data-tvid='none' data-tid='${traitValues? trait?.id:''}'
                class="btn span2 none ${queryParams.trait && traitValues && queryParams.trait[trait.id+'']=='none'?'active btn-success':''}"
                value="none"
                style="padding: 0px; height: 36px; border-radius: 6px; margin:5px;line-height:30px;text-align:left;">
                <img
                    class="${showDetails ? 'normal_profile_pic' : 'user-icon small_profile_pic'}" style="float:left;"
                    src="${grailsApplication.config.speciesPortal.traits.serverURL}/32/32_none.png" title="Undefined"
                    alt="Undefined" />
                Undefined
            </div> 
            </g:if>
            </g:if>
</div>

