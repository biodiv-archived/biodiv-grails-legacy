<%@ page import="species.participation.Flag.FlagType"%>
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
			</g:else> <g:message code="msg.Flag" />
		</button>

               		<div id="flag-options" class="popup-form" style="display: none">
			<h5><g:message code="msg.Why" />?</h5>
			<form id="flag-form"
                            action="javascript:flag('${observationInstance.id}', '${observationInstance.class.getCanonicalName()}','${uGroup.createLink(controller:'action', action:'flagIt', id:observationInstance.id)}')">
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
				<textarea id ="flagNotes" class="comment-textbox" placeholder="Tell Why??" name="notes"></textarea>
<%--				<input type="text" name="notes" placeholder="Any other reason"></input><br />--%>
				<input class="btn btn-danger pull-right" type="submit" value="Flag"/>
				<div id="flag-close" class="popup-form-close" value="close">
					<i class="icon-remove"></i>
				</div>
			</form>

			<div id="flagged">
				<g:if test="${observationInstance.flagCount> -1}">
					<span id="flagHeadings" style="font-weight: bold"><g:message code="msg.Who.Why" />:</span>
                                        </g:if>
                                        <div class = "flag-list-users">
                                            <g:render template="/common/observation/flagListUsersTemplate" model="['observationInstance':observationInstance]"/>
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

function removeFlag(flagId, flagComponent){ 
	$.ajax({
		url: "${uGroup.createLink(controller:'action', action:'deleteFlag')}",
		data:{"id":flagId},
		
                success: function(data){

                    if(parseInt(data.flagCount) == 0){
                        $("#flagHeadings").hide();
			$("#flag-action>i").removeClass("icon-red");
			}
			$(".deleteFlagIcon").tooltip('hide');
			flagComponent.remove();
                        $("#flagMessage").html('');
                        updateFeeds();  
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
