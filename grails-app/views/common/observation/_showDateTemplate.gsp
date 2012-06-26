<%@ page import="groovy.time.TimeCategory"%>
<div class="value">
	<% 
		def serverDate
		if(propertyName == "createdOn")
			serverDate =  observationInstance.createdOn
		else if(propertyName == "observedOn")
			serverDate =  observationInstance.observedOn
		else if(propertyName == "lastRevised")
			serverDate =  observationInstance.lastRevised
		else if(propertyName == "foundedOn")
			serverDate =  userGroupInstance.foundedOn
	 %>
	 <g:if test="${serverDate}">
	<g:if test="${(new Date()).minus(serverDate)}">
		<g:formatDate date="${serverDate}" type="datetime" style="LONG" timeStyle="SHORT"/>
	</g:if>
	<g:elseif test="${propertyName == 'lastRevised' && (!(serverDate - observationInstance.createdOn))}">
		None	
	</g:elseif>
	<g:else>
	<%
		def timeDuration = TimeCategory.minus(new Date(), serverDate)
		def timeString = ""
		int hours = timeDuration.hours
		int minutes = timeDuration.minutes
		if(hours > 0){
			timeString += hours
			if(hours > 1){
				timeString += " hours "
			}else{
				timeString += " hour "
			}
		}
		if(minutes >= 0){
			timeString += minutes
			if(minutes > 1){
				timeString += " minutes "
			}else{
				timeString += " minute "
			}
		}
		timeString += "ago"
	%>
		${timeString}
	</g:else>
	</g:if>
</div>

