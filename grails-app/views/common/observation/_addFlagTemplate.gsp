<%@ page import="species.participation.ObservationFlag.FlagType"%>
<%@ page import="species.participation.Observation"%>
<%@page import="species.utils.ImageType"%>
<div>
	<div class="btn-group">
		<button id="flag-action" class="btn btn-link" data-toggle="dropdown"
                        title="Report an inappropriate or erroneous contribution"
			href="#"> <g:if test="${observationInstance.flagCount>0}">
				<i class="icon-flag icon-red"></i>
			</g:if> <g:else>
				<i class="icon-flag"></i>
			</g:else> Flag
		</button>


		<div id="flag-options" class="popup-form" style="display: none">
			<h5>Why?</h5>
			<form id="flag-form"
				action="${uGroup.createLink(controller:'observation', action:'flagObservation', id:observationInstance.id)}">
				<g:each in="${FlagType.list() }" var="flag" status="i">
					<g:if test="${i > 0}">
						<input type="radio" style="margin-top: 0px;" name="obvFlag" value="${flag}"/>
						${flag.value()}
						<br />
					</g:if>
					<g:else>
						<input type="radio" style="margin-top: 0px;" name="obvFlag" value="${flag}" CHECKED/>
						${flag.value()}
						<br />
					</g:else>
				</g:each>
				<br/>
				<textarea class="comment-textbox" placeholder="Any other reason" name="notes"></textarea>
<%--				<input type="text" name="notes" placeholder="Any other reason"></input><br />--%>
				<input class="btn btn-danger pull-right" type="submit" value="Flag"/>
				<div id="flag-close" class="popup-form-close" value="close">
					<i class="icon-remove"></i>
				</div>
			</form>

			<div id="flagged">
				<g:if test="${observationInstance.flagCount>0}">
					<span id="flagHeadings" style="font-weight: bold">Who flagged and why:</span>
				</g:if>
				<div>
					<g:each var="flagInstance" in="${observationInstance.fetchAllFlags()}">
						<li style="padding: 0 5px; clear: both;">
							<span class="flagInstanceClass">
							<a href="${uGroup.createLink(controller:"SUser", action:"show", id:flagInstance.author?.id)}">
							<img class="small_profile_pic"
								src="${flagInstance.author?.profilePicture(ImageType.SMALL)}"
								title="${flagInstance.author.name}"/></a> : ${flagInstance.flag.value()} ${flagInstance.notes ? ": " + flagInstance.notes : ""}</span>
							<sUser:ifOwns model="['user':flagInstance.author]">
								<a href="#" onclick="removeFlag(${flagInstance.id}, ${flagInstance.observation.id}, $(this).parent()); return false;"><span class="deleteFlagIcon" data-original-title="Remove this flag" ><i class="icon-trash"></i></span></a>
							</sUser:ifOwns>
							
						</li>
					</g:each>
				</div>
			</div>
			<div id="flagMessage">
			</div>

		</div>
	</div>
</div>

<r:script>
$(document).ready(function(){
	$(".deleteFlagIcon").tooltip({"placement":"right"});
});

$('#flag-action').click(function(){
	$('#flag-options').show();
});
$('#flag-form').submit(function(){
	$('#flag-options').hide();
	
});
$('#flag-close').click(function(){
	$('#flag-options').hide();
});

function removeFlag(flagId, obvId, flagComponent){ 
	$.ajax({
		url: "${uGroup.createLink(controller:'observation', action:'deleteObvFlag')}",
		data:{"id":flagId, "obvId":obvId},
		
		success: function(data){
			if(parseInt(data) == 0){
				$("#flagHeadings").hide();
				$("#flag-action>i").removeClass("icon-red");
			}
			$(".deleteFlagIcon").tooltip('hide');
			flagComponent.remove();
			$("#flagMessage").html('');
		},
		
		statusCode: {
			401: function() {
				show_login_dialog();
			}	    				    			
		},
		error: function(xhr, status, error) {
			$(".deleteFlagIcon").tooltip('hide');
			var msg = $.parseJSON(xhr.responseText);
			$("#flagMessage").html(msg["error"]).show().removeClass().addClass('alert alert-error');
		}
	});
}

</r:script>

<style>

#flagged {
	clear: both;
	padding-top: 10px;
}
</style>
