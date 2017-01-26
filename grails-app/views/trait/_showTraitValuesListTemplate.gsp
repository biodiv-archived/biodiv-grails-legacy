<%@page import="species.trait.Trait.TraitTypes"%>
<%@page import="species.trait.Trait.DataTypes"%>
<%@page import="species.trait.TraitValue"%>
<%@page import="java.text.SimpleDateFormat" %>
<%@page import="java.util.Date" %>

<div class="row btn-group" style="white-space:inherit;margin-left:20px;${trait.dataTypes == DataTypes.DATE?'width:100%;':''}">
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
            <div data-tvid='any' data-tid='${traitValues?traitValues[0]?.trait?.id:''}'
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
                    <g:elseif test="${trait.dataTypes == DataTypes.COLOR}">
                    <span data-tid='${trait.id}' data-isnotobservation='${trait.isNotObservationTrait}'
                    class="btn span2 input-prepend single-post disabled" title="${traitValue}"
                        style="padding: 0px; height: 36px; border-radius: 6px; margin:5px; background-color:${traitValue};">
                    </span>
                    </g:elseif>

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
            <g:elseif test="${trait.traitTypes == TraitTypes.RANGE && trait.dataTypes == DataTypes.DATE}">
            <div id="${trait.id}_datePicker" class="dropdown" style="position: relative;overflow:visible;font-size:initial;">
                <div class="pull-left" style="text-align:left;padding:5px;" >
                    <i class="icon-calendar icon-large"></i> 
                    <%
                    def df = new SimpleDateFormat('DD/MM/YYYY')
                    def date_str =   queryParams && queryParams.trait && queryParams.trait[trait.id] ? queryParams.trait[trait.id].split(':') : '';
                    def startDate = date_str && date_str[0] ? df.parse(date_str[0]) : null
                    def endDate = date_str && date_str[1]? df.parse(date_str[1]): null
                    String dateRange = (startDate && endDate)?date_str[0]+':'+date_str[1] : ''
                    %>
                    <input type="text" name="${trait.id}_date" data-tid='${trait.id}' data-isNotObservation='${trait.isNotObservationTrait}'
                        class="single-post ${traitTypes} trait_date_range" value="${dateRange}"
                        style="padding: 0px; height: 36px; border-radius: 6px; margin:5px;width:inherit;"
                    />
                </div>
            </div>
            </g:elseif>
            <g:elseif test="${trait.traitTypes == TraitTypes.RANGE && trait.dataTypes == DataTypes.NUMERIC}">
            <input 
            type="text" data-tid='${trait.id}' data-isNotObservation='${trait.isNotObservationTrait}'
            class="span2 input-prepend single-post ${traitTypes} trait_range_slider" value="${queryParams && queryParams.trait && queryParams.trait[trait.id] ? queryParams.trait[trait.id].replace(':',','):''}"
            style="padding: 0px; height: 36px; border-radius: 6px; margin:5px;width:inherit;"
            data-slider-value="${queryParams && queryParams.trait && queryParams.trait[trait.id] ? '['+queryParams.trait[trait.id].replace(':',',')+']':'['+(numericTraitMinMax?numericTraitMinMax.min:0)+','+(numericTraitMinMax?numericTraitMinMax.max:100)+']'}" data-slider-min="${numericTraitMinMax?numericTraitMinMax.min:0}" data-slider-max="${numericTraitMinMax?numericTraitMinMax.max:100}" data-slider-step="1">
            </g:elseif>
            <g:elseif test="${trait.dataTypes == DataTypes.COLOR}">
            <div class="input-group colorpicker-component" style="width:220px;display:inline-block;">
            <input 
            type="text" data-tid='${trait.id}' data-isNotObservation='${trait.isNotObservationTrait}'
            class="form-control single-post ${traitTypes} trait_color_picker" value="${queryParams && queryParams.trait && queryParams.trait[trait.id] ? queryParams.trait[trait.id].replace(':',','):''}"
            style="display:none;">
            </div>
            </g:elseif>
           <g:else>
            </g:else>
</div>

