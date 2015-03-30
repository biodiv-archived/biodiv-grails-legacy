<%@ page import="species.groups.CustomField"%>
<div class="control-group customField"  style="margin-top:5px;">
      <label for="customField" class="control-label"> <g:message
          code="observation.customField.label" default="${customFieldInstance.name}" /><g:if test="${customFieldInstance.isMandatory}"><span class="req">*</span></g:if>
      </label>
      <%
		def paramCfValue = params.get(CustomField.PREFIX + customFieldInstance.name) 
		def cValue = paramCfValue?:customFieldInstance.fetchValue(observationInstance?.id)
		if(customFieldInstance.allowedMultiple){
		  	if(cValue && cValue instanceof String)
		  		cValue = cValue.tokenize(",")
		}  
		def options =  customFieldInstance.fetchOptions()
		boolean isNumber =  (customFieldInstance.dataType == CustomField.DataType.INTEGER || customFieldInstance.dataType == CustomField.DataType.DECIMAL)
		def mandatoryFieldClass = customFieldInstance.isMandatory ? ' mandatoryField ':''
	  %>
      <div class="controls">
      <g:if test="${options}">
      	<g:if test="${customFieldInstance.allowedMultiple}">
      		<select class="${CustomField.PREFIX + 'multiselectcombo' + mandatoryFieldClass}" multiple="multiple" title="${customFieldInstance.notes}" name="${CustomField.PREFIX + customFieldInstance.name}" >
   			<g:each in="${options}">
    				<g:if test="${it in cValue}">
        				<option selected="selected" value="${it}"> ${it}</option>
        			</g:if>
        			<g:else>
        				<option value="${it}"> ${it}</option>
        			</g:else>
    			</g:each>
   			</select>
      	</g:if>
      	<g:else>
      		<select class="${'combobox' + mandatoryFieldClass}" name="${CustomField.PREFIX + customFieldInstance.name}" title="${customFieldInstance.notes}" >
      			<option></option>
    			<g:each in="${options}">
      				<g:if test="${cValue == it}">
        				<option selected="selected" value="${it}"> ${it}</option>
        			</g:if>
        			<g:else>
        				<option value="${it}"> ${it}</option>
        			</g:else>
    			</g:each>
   			</select>
   		</g:else>		
    		 			
<%--		<b style="margin-left:3%;">Or</b> <input style="float:right; width:60%;" type="text" name="${CustomField.PREFIX + customFieldInstance.name}" placeholder="Add new value" class="input-block-level"/>--%>
	  </g:if>
	  <g:else>
	  	<g:if test="${CustomField.DataType.PARAGRAPH_TEXT == customFieldInstance.dataType}">
	  		<textarea style="resize: vertical;" class="${'input-block-level' + mandatoryFieldClass}" title="${customFieldInstance.notes}" name="${CustomField.PREFIX + customFieldInstance.name}">${cValue}</textarea>
        </g:if>
        <g:elseif test="${CustomField.DataType.DATE == customFieldInstance.dataType}">
        	<%
				if(cValue instanceof String)
					cValue = species.UtilsService.parseDate(cValue)
			%>
        	<input name="${CustomField.PREFIX + customFieldInstance.name}" title="${customFieldInstance.notes}" type="text" class="${'date input-block-level' + mandatoryFieldClass}"
        			value="${cValue?.format('dd/MM/yyyy')}"/>
        </g:elseif>
        <g:else>	
          <div class="textbox">
              <input type="text" title="${customFieldInstance.notes}" name="${CustomField.PREFIX + customFieldInstance.name}" value="${cValue}" class="${'input-block-level ' + (isNumber?'CustomField_number':'') + mandatoryFieldClass}"/>
          </div>
        </g:else>  
      </g:else>    
      </div>
</div>
