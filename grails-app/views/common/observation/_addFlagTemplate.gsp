<%@ page import="species.participation.ObservationFlag.FlagType"%>

<div>
    <div class="btn-group">
    	<a id="flag-action" class="btn btn-mini" data-toggle="dropdown" href="#">
     		<i class="icon-flag"></i>
    		Flag
    	</a>
    
	    <div id="flag-options" style="display:none">
	    	<form id="flag-form" action="${createLink(controller:'observation', action:'flagObservation', id:observationInstance.id)}">
		    	<g:each in="${FlagType.list() }" var="flag" status="i">
		    		<g:if test="${i > 0}">
		    			<input type="radio" name="obvFlag" value="${flag}">${flag.value()}</input><br/>
		    		</g:if>
		    		<g:else>
		    			<input type="radio" name="obvFlag" value="${flag}" CHECKED>${flag.value()}</input><br/>
		    		</g:else>	
				</g:each>
		    	
		    	<input type="text" name="notes"></input><br/>
		    	<input type="submit" value="Flag"></input>
		    	<button type="button" id="flag-close" value="close">close</button>
		  	</form>
	    </div>
    </div>
</div>

<script>
$('#flag-action').click(function(){
	$('#flag-options').show();
});
$('#flag-form').submit(function(){
	$('#flag-options').hide();
	
});
$('#flag-close').click(function(){
	$('#flag-options').hide();
});
</script>

<style>
#flag-options{
	background-clip: padding-box;
    background-color: #FFFFFF;
    border-color: rgba(0, 0, 0, 0.2);
    border-radius: 0 0 5px 5px;
    border-style: solid;
    border-width: 1px;
    box-shadow: 0 5px 10px rgba(0, 0, 0, 0.2);
    display: none;
    float: left;
    left: 0;
    list-style: none outside none;
    margin: 0;
    min-width: 160px;
    padding: 4px 0;
    position: absolute;
    top: 100%;
    z-index: 1000;
}
</style>