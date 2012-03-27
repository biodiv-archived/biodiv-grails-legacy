<%@ page import="groovy.time.TimeCategory"%>
<div class="value">
	<% 
		def serverDate
		if(propertyName == "createdOn")
			serverDate =  observationInstance.createdOn
		else
			serverDate =  observationInstance.lastUpdated
	 %>
	<g:if test="${(new Date()).minus(serverDate)}">
		<g:formatDate date="${serverDate}" type="datetime" style="LONG" timeStyle="SHORT"/>
	</g:if>
	<g:elseif test="${propertyName == 'lastUpdated' && (!(serverDate - observationInstance.createdOn))}">
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
</div>

