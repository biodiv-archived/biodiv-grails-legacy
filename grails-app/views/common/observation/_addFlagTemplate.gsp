<%@ page import="species.participation.ObservationFlag.FlagType"%>

<div>
    <div class="btn-group">
    	<a id="flag-action" class="btn btn-mini" data-toggle="dropdown" href="#">
	    <g:if test="${observationInstance.flagCount>0}">
     		<i class="icon-flag icon-red"></i>
            </g:if>    
            <g:else>
     		<i class="icon-flag"></i>
            </g:else>
    		Flag
    	</a>
    	
    	
	    <div id="flag-options" style="display:none">
                <h5>Why?</h5>
	    	<form id="flag-form" action="${createLink(controller:'observation', action:'flagObservation', id:observationInstance.id)}">
		    	<g:each in="${FlagType.list() }" var="flag" status="i">
		    		<g:if test="${i > 0}">
		    			<input type="radio" name="obvFlag" value="${flag}">${flag.value()}</input><br/>
		    		</g:if>
		    		<g:else>
		    			<input type="radio" name="obvFlag" value="${flag}" CHECKED>${flag.value()}</input><br/>
		    		</g:else>	
				</g:each>
		    	
		    	<input type="text" name="notes" placeholder="Any other reason"></input><br/>
		    	<input class="btn btn-danger" type="submit" value="Flag"></input>
		    	<div id="flag-close" value="close"><i class="icon-remove"></i></div>
		  	</form>

                        <div id="flagged">
		  	<g:if test="${observationInstance.flagCount>0}">
		  		<span style="font-weight:bold">Who flagged and why:</span>
		  	</g:if>
		  	<div>
	    	<g:each in="${observationInstance.fetchAllFlags()}">
				<li style="padding: 0 5px; clear: both;">
					${it.author.username} : ${it.flag.value()} ${it.notes ? ": " + it.notes : ""}
				</li>
			</g:each>
		</div>
                        </div>
    	
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
    min-width: 400px;
    padding: 10px;
    position: absolute;
    top: 100%;
    z-index: 1000;
    color: #000000;
}

#flag-close {
    position:absolute;
    top: 0; 
    right: 0;     
}

#flagged {
clear: both;
padding-top:10px;       
}
</style>
