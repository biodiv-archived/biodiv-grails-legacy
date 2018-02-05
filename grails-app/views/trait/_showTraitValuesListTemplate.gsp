<%@page import="species.trait.Trait.TraitTypes"%>
<%@page import="species.trait.Trait.DataTypes"%>
<%@page import="species.trait.Trait.Units"%>
<%@page import="species.trait.TraitValue"%>
<%@page import="java.text.SimpleDateFormat" %>
<%@page import="java.util.Date" %>
<%@page import="species.UtilsService"%>

<div class="row btn-group" style="white-space:inherit;margin-left:20px;${traitInstance.dataTypes == DataTypes.DATE?'width:100%;':''}">
<g:if test="${traitValues && traitValues.size()>0}">
            <g:if test="${displayAny && traitInstance.isNotObservationTrait}">
            <div data-tvid='all' data-tid='${traitValues?traitInstance?.id:''}'
                class="btn span2 all ${queryParams.trait && traitValues && queryParams.trait[traitInstance?.id+'']?'':'active btn-success'}"
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
            <div data-tvid='any' data-tid='${traitValues?traitValues[0]?.traitInstance?.id:''}'
                class="btn span2 any ${queryParams.trait && traitValues && queryParams.trait[traitInstance?.id+'']?'':'active btn-success'}"
                value="any"
                style="padding: 0px; height: 36px; border-radius: 6px; margin:5px;}; line-height:30px;text-align:left;">
                <img
                    class="${showDetails ? 'normal_profile_pic' : 'user-icon small_profile_pic'}"
                    src="${grailsApplication.config.speciesPortal.traits.serverURL}/32/32_any.png" title="All"
                    alt="all" /> 
                    All
           </div> 
            </g:if>
        
        <g:if test="${fromSpeciesShow!=true || editable}">
            <g:each in="${traitValues}" var="traitValue" status="i">
                <g:if test="${(traitValue instanceof TraitValue)}">
                    <button type="button" id="value_btn_${traitValue.id}" data-tvid='${traitValue.id}' data-tid='${traitValue?.traitInstance.id}' data-isNotObservation='${traitValue.traitInstance.isNotObservationTrait}'
                    class="btn span2 input-prepend single-post ${traitTypes} ${queryParams && queryParams.trait && traitValue && queryParams.trait[traitValue?.traitInstance.id]?.contains(traitValue.id+'')?'active btn-success':''}"
                        value="${traitValue.value}"
                        style="padding: 0px; height: 36px; border-radius: 6px; margin:5px;width:inherit;">
                        <g:render template="/trait/showTraitValueSignatureTemplate" model="['traitValue':traitValue]"/>
                    </button>
                </g:if>
                <g:else>
                    <g:if test="${trait.traitTypes == TraitTypes.RANGE && trait.dataTypes == DataTypes.DATE}">
                        <span data-tid='${trait.id}' data-isnotobservation='${trait.isNotObservationTrait}'
                        class="btn span2 input-prepend single-post disabled"
                            style="">

                            <g:if test="${trait.units == Units.MONTH}">
                                ${UtilsService.getMonthName(traitValue.split(';')[0])} 
                                - ${UtilsService.getMonthName(traitValue.split(';')[1])} 
                            </g:if>
                            <g:else>
                                ${traitValue}
                            </g:else>
                        </span>
                        </g:if>
                        <g:elseif test="${trait.dataTypes == DataTypes.COLOR}">
                        <span data-tid='${trait.id}' data-isnotobservation='${trait.isNotObservationTrait}'
                        class="btn span2 input-prepend single-post disabled" title="${traitValue}"
                            style="">
                        </span>
                        </g:elseif>

                        <g:else>
                        <span data-tid='${trait.id}' data-isnotobservation='${trait.isNotObservationTrait}'
                        class="btn span2 input-prepend single-post disabled"
                            style="">
                            ${traitValue} 
                        </span>
                        </g:else>
                    
                    </g:else>
            </g:each> 
        </g:if>
        
        <g:if test="${fromSpeciesShow==true && !editable}">
            <g:each in="${traitValues}" var="traitValue" status="i">
            <g:if test="${(traitValue instanceof TraitValue)}">
                <% String link="${"/trait/show/"+traitInstance.id+"?trait."+traitInstance.id+"="+traitValue.id}" %>
                <a href='${link}'>
                    <button type="button" id="value_btn_${traitValue.id}" data-tvid='${traitValue.id}' data-tid='${traitValue.traitInstance.id}' data-isnotobservation='${traitValue.traitInstance.isNotObservationTrait}'
                    class="btn span2 input-prepend single-post ${queryParams && queryParams.trait && traitValue && queryParams.trait[traitValue.traitInstance.id]?.contains(traitValue.id+'')?'active btn-success':''}"
                        value="${traitValue.value}"
                        style="padding: 0px; height: 36px; border-radius: 6px; margin:5px;">

                        <g:render template="/trait/showTraitValueSignatureTemplate" model="['traitValue':traitValue]"/>
                    </button> 
                </a>
                </g:if>
                <g:else>
                    <g:if test="${traitInstance.traitTypes == TraitTypes.RANGE && traitInstance.dataTypes == DataTypes.DATE}">
                    <span data-tid='${traitInstance.id}' data-isnotobservation='${traitInstance.isNotObservationTrait}'
                    class="btn span2 input-prepend single-post disabled"
                        style="padding: 0px; height: 36px; border-radius: 6px; margin:5px;font-weight:bold;">

                        <g:if test="${traitInstance.units == Units.MONTH}">
                            ${UtilsService.getMonthName(traitValue.split(';')[0])} 
                            - ${UtilsService.getMonthName(traitValue.split(';')[1])} 
                        </g:if>
                        <g:else>
                            ${traitValue}
                        </g:else>
                    </span>
                    </g:if>
                    <g:elseif test="${traitInstance.dataTypes == DataTypes.COLOR}">
                    <span data-tid='${traitInstance.id}' data-isnotobservation='${traitInstance.isNotObservationTrait}'
                    class="btn span2 input-prepend single-post disabled" title="${traitValue}"
                        style="padding: 0px; height: 36px; border-radius: 6px; margin:5px; background-color:${traitValue};">
                    </span>
                    </g:elseif>

                    <g:else>
                    <span data-tid='${traitInstance.id}' data-isnotobservation='${traitInstance.isNotObservationTrait}'
                    class="btn span2 input-prepend single-post disabled"
                        style="padding: 0px; height: 36px; border-radius: 6px; margin:5px;font-weight:bold;">
                        ${traitValue}
                    </span>
                    </g:else>
                    </g:else>
            </g:each>
        </g:if>
        
            <g:if test="${displayAny && traitInstance.isNotObservationTrait}">
            <div data-tvid='none' data-tid='${traitValues? traitInstance?.id:''}'
                class="btn span2 none ${queryParams.trait && traitValues && queryParams.trait[traitInstance.id+'']=='none'?'active btn-success':''}"
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


            <g:elseif test="${traitInstance.traitTypes == TraitTypes.RANGE && traitInstance.dataTypes == DataTypes.DATE}">
            <g:if test="${traitInstance.units == Units.MONTH}">
            <div style="width:280px;">
            <%
                def fromDate1 = (queryParams && queryParams.trait && queryParams.trait[traitInstance.id] &&!queryParams.trait[traitInstance.id].equalsIgnoreCase('any')) ? UtilsService.getMonthIndex(queryParams.trait[traitInstance.id].split(':')[0]) : 0;
                def toDate1 = (queryParams && queryParams.trait && queryParams.trait[traitInstance.id] && !queryParams.trait[traitInstance.id].equalsIgnoreCase('any')) ? UtilsService.getMonthIndex(queryParams.trait[traitInstance.id].split(':')[1]) : 11;
            %>
            <input 
            type="text" data-tid='${traitInstance.id}' data-isNotObservation='${traitInstance.isNotObservationTrait}'
            class="span2 input-prepend single-post ${traitTypes} trait_date_range_slider" value="${queryParams && queryParams.trait && queryParams.trait[traitInstance.id] ? queryParams.trait[traitInstance.id].replace(':',';'):''}"
            style="padding: 0px; height: 36px; border-radius: 6px; margin:5px;width:inherit;"
            data-type="double" data-from="${fromDate1}" data-to="${toDate1}"
            data-min="January" data-max="December" data-step="1">
            </div>

            </g:if>
            <g:else>
            <div id="${traitInstance.id}_datePicker" class="dropdown" style="position: relative;overflow:visible;font-size:initial;">
                <div class="pull-left" style="text-align:left;padding:5px;" >
                    <i class="icon-calendar icon-large"></i> 
                    <%
                    def df = new SimpleDateFormat('DD/MM/YYYY')
                    def date_str =   queryParams && queryParams.trait && queryParams.trait[traitInstance.id] ? queryParams.trait[traitInstance.id].split(':') : '';
                    def startDate = date_str && date_str[0] ? df.parse(date_str[0]) : null
                    def endDate = date_str && date_str[1]? df.parse(date_str[1]): null
                    String dateRange = (startDate && endDate)?date_str[0]+':'+date_str[1] : ''
                    %>
                    <input type="text" name="${traitInstance.id}_date" data-tid='${traitInstance.id}' data-isNotObservation='${traitInstance.isNotObservationTrait}'
                        class="single-post ${traitTypes} trait_date_range" value="${dateRange}"
                        style="padding: 0px; height: 36px; border-radius: 6px; margin:5px;width:inherit;"
                    />
                </div>
            </div>
            </g:else>
            </g:elseif>
            <g:elseif test="${traitInstance.traitTypes == TraitTypes.RANGE && traitInstance.dataTypes == DataTypes.NUMERIC}">
            <div style="width:280px;">
            <%
                def from = queryParams && queryParams.trait && queryParams.trait[traitInstance.id]  && !queryParams.trait[traitInstance.id].equalsIgnoreCase('any') ? queryParams.trait[traitInstance.id].split(':')[0]:(numericTraitMinMax?numericTraitMinMax.min:0)
                def to =queryParams && queryParams.trait && queryParams.trait[traitInstance.id] && !queryParams.trait[traitInstance.id].equalsIgnoreCase('any')  ? queryParams.trait[traitInstance.id].split(':')[1]:(numericTraitMinMax?numericTraitMinMax.max:100)
            %>
            <input 
            type="text" data-tid='${traitInstance.id}' data-isNotObservation='${traitInstance.isNotObservationTrait}'
            class="span2 input-prepend single-post ${traitTypes} trait_range_slider" value="${queryParams && queryParams.trait && queryParams.trait[traitInstance.id] ? queryParams.trait[traitInstance.id].replace(':',','):''}"
            style="padding: 0px; height: 36px; border-radius: 6px; margin:5px;width:inherit;"
            data-type="double" data-from="${from}" data-to="${to}"
            data-min="${numericTraitMinMax?numericTraitMinMax.min:0}" data-max="${numericTraitMinMax?numericTraitMinMax.max:100}" data-step="1">
            </div>
            </g:elseif>
            <g:elseif test="${traitInstance.dataTypes == DataTypes.COLOR}">
            <div class="input-group colorpicker-component" style="width:280px;display:inline-block;">
            <input 
            type="text" data-tid='${traitInstance.id}' data-isNotObservation='${traitInstance.isNotObservationTrait}'
            class="form-control single-post ${traitTypes} trait_color_picker" value="${queryParams && queryParams.trait && queryParams.trait[traitInstance.id] ? queryParams.trait[traitInstance.id].replace(':',','):''}"
            style="display:none;">
            </div>
            </g:elseif>
           <g:else>
            </g:else>
</div>

