<%@ page import="species.groups.CustomField"%>

<g:if test="${userGroupInstance}"> 
  <div class="section" style="position: relative; overflow: visible;">
      <h3><g:message code="heading.customfields.create" /></h3>
      <div>
        <g:each var="customFieldInstance" in="${CustomField.fetchCustomFields(userGroupInstance)}">
        	<g:render template="customFieldTemplate" model="['observationInstance':observationInstance, 'customFieldInstance':customFieldInstance]"/>
		 </g:each>
      </div>
  </div>
</g:if>