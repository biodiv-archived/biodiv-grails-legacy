<%@ page import="species.groups.CustomField"%>

<g:if test="${userGroupInstance && !CustomField.fetchCustomFields(userGroupInstance).isEmpty()}"> 
  <div class="section customFieldForm" style="position: relative; overflow: visible;">
      <h3><g:message code="heading.customfields.create" /></h3>
      <div>
        <g:each var="customFieldInstance" in="${CustomField.fetchCustomFields(userGroupInstance)}">
        	<g:render template="customFieldTemplate" model="['observationInstance':observationInstance, 'customFieldInstance':customFieldInstance]"/>
		 </g:each>
      </div>
  </div>
</g:if>