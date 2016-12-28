<style>
.sidebar_section{margin-bottom:0px;}
</style>
<% 
def ref=[];
def instanceFieldList=[:]
instanceList.each{ iL ->
    if(ref.contains(iL.field)){           
        instanceFieldList[iL.field] << iL
    }else{
        ref << iL.field
        instanceFieldList[iL.field] = [iL]
    }
}
%>                   
 <div class="observations_list trait_list" style="clear: both;">
	<div class="mainContentList">
		<div class="mainContent trait_list_content" name="p${params?.offset}">
		    <div class="filters">
            <g:each in="${instanceFieldList}" status="j" var="inst">                
                <div class="sidebar_section">
                <g:if test="${fromObservationShow!='show'}">
	              <a class="speciesFieldHeader"  data-toggle="collapse" href="#trait${j}">
                    	<h5>${inst.key}</h5>
                   </a> 
                   </g:if>
                   
                 <ul id="trait${j}" class="grid_view thumbnails obvListwrapper">
				<g:each in="${inst.value}" status="i" var="trait_instance">
				<div data-isNotObservation="${trait_instance.isNotObservationTrait}">
					<li class="thumbnail" style="clear: both;margin-left:0px;width:100%;border:0px !important;">
                    <g:render template="/trait/showTraitTemplate" model="['trait':trait_instance, 'factInstance':factInstance, object:instance, 'fromSpeciesShow':fromSpeciesShow, 'queryParams':queryParams, 'editable':editable, 'ifOwns':ifOwns, 'filterable':filterable]"/>
					</li>
					</div>
				</g:each>
			</ul>              
			</div>
			</g:each>
			</div>			
		</div>
	</div>
       
</div>

<asset:script>
$(document).ready(function(){
	$('.icon-question-sign').tooltip({'placement': 'top','container':'body'});
});
</asset:script>

