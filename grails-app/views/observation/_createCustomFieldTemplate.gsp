<%@ page import="species.groups.CustomField.DataType"%>
<%@ page import="species.groups.CustomField"%>


<ul class="customFieldList" style="list-style:none;">
<g:each in="${customFields}" var="customField" status="i">
<li>
<div class="btn btn-primary" style="float:right;" onclick="$(this).parent().remove();">Remove</div>
<div class="control-group customField"  style="margin-top:5px;">
      <label for="customField" class="control-label"> <g:message
          code="customField.name.label" default="Name" /><span class="req">*</span>
      </label>
      <div class="controls">
          <div class="textbox">
              <input type="text" class="${CustomField.PREFIX + 'name'}" value="${customField?.name}" placeholder="Enter Name"/>
          </div>
      </div>
</div>
<div class="control-group customField"  style="margin-top:5px;">
      <label for="customField" class="control-label"> <g:message
          code="customField.descriptoin.label" default="Description" />
      </label>
      <div class="controls">
          <div class="textbox">
              <input type="text" class="${CustomField.PREFIX + 'description'}" value="${customField?.notes}" placeholder="Add Description"/>
          </div>
      </div>
</div>



<div class="control-group customField"  style="margin-top:5px;">
      <label for="customField" class="control-label"> <g:message
          code="customField.type.label" default="Type" /><span class="req">*</span>
      </label>
      <div class="controls">
          <div>
          	<select class="${CustomField.PREFIX + 'dataType'}">
                 	<g:each in="${DataType.toList()}" var="colType">
                     	<g:if test="${colType.ordinal() == (customField?customField.dataType.ordinal():DataType.TEXT.ordinal())}">
                         	<option value="${colType}" selected>${colType}</option>
                         </g:if>
                         <g:else>
                         	<option value="${colType}">${colType}</option>
                         </g:else> 
                     </g:each>
            </select>
          </div>
      </div>
</div>

<div class="control-group customField formType"  style="margin-top:5px;">
      <label for="customField" class="control-label"> <g:message
          code="customField.multiselect.label" default="Form" />
      </label>
      <div class="controls">
          <div>
			  <div class="defTextbox"><input type="radio" name="cfradioGroup_${supportingModule}_${i}"  class="cfRaidioButtonSelector" ${customField?(customField.options?'':'checked'):'checked'}> Text box <br></div>
			  <div class="oneFromList"><input type="radio" name="cfradioGroup_${supportingModule}_${i}" class="cfRaidioButtonSelector"  ${customField?(customField.options && !customField.allowedMultiple ?'checked':''):''}> Select one from list <br></div> 
			  <div class="multipleFromList"><input type="radio" name="cfradioGroup_${supportingModule}_${i}" class="${'cfRaidioButtonSelector '+CustomField.PREFIX + 'allowedMultiple'}"  ${customField?(customField.options && customField.allowedMultiple ?'checked':''):''}> Select multiple from list</div>
          </div>
      </div>
</div>

<div class="control-group customField listOptions"  style="${customField && customField.options?:'display:none'};margin-top:5px;">
      <label for="customField" class="control-label"> <g:message
          code="customField.options.label" default="Options" />
      </label>
      <div class="controls">
          <div class="textbox">
              <input type="text" class="${CustomField.PREFIX + 'options'}" value="${customField?.options}" placeholder="Comma separated options for custom field"/>
          </div>
      </div>
</div>

<div class="control-group customField"  style="margin-top:5px;">
      <label for="customField" class="control-label"> <g:message
          code="customField.defalutValue.label" default="Default Value" />
      </label>
      <div class="controls">
          <div class="textbox">
              <input type="text" class="${CustomField.PREFIX + 'defaultValue'}" value="${customField?.defaultValue}" placeholder="Enter Default Value"/>
          </div>
      </div>
</div>

<div class="control-group customField"  style="margin-top:5px;">
      <label for="customField" class="control-label"> <g:message
          code="customField.isMandatory.label" default="Mandatory" />
      </label>
      <div class="controls">
          <div>
          	<input type="checkbox" style="margin-left:0px;" class="${CustomField.PREFIX + 'isMandatory'}" value="${customField?customField.isMandatory:false}"  ${customField?.isMandatory?'checked':''} />
          </div>
      </div>
</div>

<div class="control-group customField"  style="margin-top:5px;">
      <label for="customField" class="control-label"> <g:message
          code="customField.allowedParticipation.label" default="Allow Participation" />
      </label>
      <div class="controls">
          <div>
          	<input type="checkbox" style="margin-left:0px;" class="${CustomField.PREFIX + 'allowedParticipation'}" value="${customField?customField.allowedParticipation:false}"  ${customField?.allowedParticipation?'checked':''} />
          </div>
      </div>
</div>

<g:if test="${supportingModule}">
<input type="hidden" class="${CustomField.PREFIX + 'supportingModule'}" value="${supportingModule}"/>
</g:if>

<hr/>
</g:each>

<script class="newCustomField" type="text/x-jquery-tmpl">
<li>
<div class="btn btn-primary" style="float:right;" onclick="$(this).parent().remove();">Remove</div>
<div class="control-group customField"  style="margin-top:5px;">
      <label for="customField" class="control-label"> <g:message
          code="customField.name.label" default="Name" /><span class="req">*</span>
      </label>
      <div class="controls">
          <div class="textbox">
              <input type="text" class="${CustomField.PREFIX + 'name'}" value="" placeholder="Enter Name"/>
          </div>
      </div>
</div>
<div class="control-group customField"  style="margin-top:5px;">
      <label for="customField" class="control-label"> <g:message
          code="customField.descriptoin.label" default="Description" />
      </label>
      <div class="controls">
          <div class="textbox">
              <input type="text" class="${CustomField.PREFIX + 'description'}" value="" placeholder="Add Description"/>
          </div>
      </div>
</div>



<div class="control-group customField"  style="margin-top:5px;">
      <label for="customField" class="control-label"> <g:message
          code="customField.type.label" default="Type" /><span class="req">*</span>
      </label>
      <div class="controls">
          <div>
          	<select class="${CustomField.PREFIX + 'dataType'}">
                 	<g:each in="${DataType.toList()}" var="colType">
                     	<g:if test="${colType == DataType.TEXT}">
                         	<option value="${colType}" selected>${colType}</option>
                         </g:if>
                         <g:else>
                         	<option value="${colType}">${colType}</option>
                         </g:else> 
                     </g:each>
            </select>
          </div>
      </div>
</div>

<div class="control-group customField formType"  style="margin-top:5px;">
      <label for="customField" class="control-label"> <g:message
          code="customField.multiselect.label" default="Form" />
      </label>
      <div class="controls">
          <div>
			  <div class="defTextbox"><input type="radio" name="{{>radioGroupName}}"  class="cfRaidioButtonSelector" checked> Text box <br></div>
			  <div class="oneFromList"><input type="radio" name="{{>radioGroupName}}" class="cfRaidioButtonSelector" > Select one from list <br></div> 
			  <div class="multipleFromList"><input type="radio" name="{{>radioGroupName}}" class="${'cfRaidioButtonSelector '+CustomField.PREFIX + 'allowedMultiple'}" > Select multiple from list</div>
          </div>
      </div>
</div>

<div class="control-group customField listOptions"  style="display:none;margin-top:5px;">
      <label for="customField" class="control-label"> <g:message
          code="customField.options.label" default="Options" />
      </label>
      <div class="controls">
          <div class="textbox">
              <input type="text" class="${CustomField.PREFIX + 'options'}" value="" placeholder="Comma separated options for custom field"/>
          </div>
      </div>
</div>

<div class="control-group customField"  style="margin-top:5px;">
      <label for="customField" class="control-label"> <g:message
          code="customField.defalutValue.label" default="Default Value" />
      </label>
      <div class="controls">
          <div class="textbox">
              <input type="text" class="${CustomField.PREFIX + 'defaultValue'}" value="" placeholder="Enter Default Value"/>
          </div>
      </div>
</div>

<div class="control-group customField"  style="margin-top:5px;">
      <label for="customField" class="control-label"> <g:message
          code="customField.isMandatory.label" default="Mandatory" />
      </label>
      <div class="controls">
          <div>
          	<input type="checkbox" style="margin-left:0px;" class="${CustomField.PREFIX + 'isMandatory'}" value="${false}"/>
          </div>
      </div>
</div>

<div class="control-group customField"  style="margin-top:5px;">
      <label for="customField" class="control-label"> <g:message
          code="customField.allowedParticipation.label" default="Allow Participation" />
      </label>
      <div class="controls">
          <div>
          	<input type="checkbox" style="margin-left:0px;" class="${CustomField.PREFIX + 'allowedParticipation'}" value="${false}"/>
          </div>
      </div>
</div>

<g:if test="${supportingModule}">
<input type="hidden" class="${CustomField.PREFIX + 'supportingModule'}" value="${supportingModule}"/>
</g:if>
<hr>
</li>
</script>
<div class="addNewCustomField btn btn-primary">Add another custom field</div>
</ul>
