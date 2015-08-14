<div class="observations_list observation" style="clear: both;">

	<!--div class="btn-group button-bar" data-toggle="buttons-radio"
		style="float: right;">
                
		<button class="list_view_bttn btn list_style_button active">
			<i class="icon-align-justify"></i>
		</button>
		<button class="grid_view_bttn btn grid_style_button">
			<i class="icon-th-large"></i>
		</button>
	</div-->

	<div class="btn-group pull-right button-bar">
        <a href="javascript:void(0);" id="obvList" class="btn btn-default btn-small">
        	<i class="icon-th-list"></i>List
        </a>
       <a href="javascript:void(0);" id="obvGrid" class="btn btn-default btn-small active">
       		<i class="icon-th"></i>Grid
       </a>
    </div>


        <div class="btn-group button-bar pull-right" style="z-index: 10; margin-right:3px;">
                    <div class="controls">
                        <g:select name="limit" class="input-mini"
                            placeholder="${g.message(code:'showobservationlisttemp.select.show')}"
                            from="${[12,24,36,48,60]}"
                            value="${queryParams?.max}" />

                    </div>

                </div>


	<div class="mainContentList">
		<div class="mainContent" name="p${params?.offset}">
		
			<%
				def observationPos = (queryParams.offset != null) ? queryParams.offset : params?.offset
			%>
			<ul class="grid_view thumbnails obvListwrapper">
			
				<g:each in="${observationInstanceList}" status="i"
					var="observationInstance">

					<g:if test="${i%4 == 0}">
						<li class="thumbnail" style="clear: both;margin-left:0px;${!inGroupMap || inGroupMap[observationInstance.id]?'':'background-color:transparent;'}">
					</g:if>
					<g:else>
						<li class="thumbnail" style="${!inGroupMap || inGroupMap[observationInstance.id]?'':'background-color:transparent;'}">
					</g:else>
					<obv:showSnippetTablet
						model="['observationInstance':observationInstance, 'obvTitle':obvTitleList?.get(i), 'pos': ((observationPos != null)?observationPos+i:0), 'userGroup':userGroupInstance, canPullResource:canPullResource]"></obv:showSnippetTablet>
					</li>

				</g:each>
			</ul>			
		</div>
	</div>
        
        <g:if test="${instanceTotal > (queryParams.max?:0)}">
		<div class="centered">
			<div class="btn loadMore">
				<span class="progress" style="display: none;"><g:message code="msg.loading" /></span> <span
					class="buttonTitle"><g:message code="msg.load.more" /></span>
			</div>
		</div>
	</g:if>
	
	<%
		activeFilters?.loadMore = true
		activeFilters?.webaddress = userGroupInstance?.webaddress
	%>
	<div class="paginateButtons" style="visibility: hidden; clear: both">
		<p:paginate total="${instanceTotal?:0}" action="${params.action}" controller="${params.controller?:'observation'}"
			userGroup="${userGroupInstance}" userGroupWebaddress="${userGroupWebaddress?:params.webaddress}"
			 max="${queryParams.max}" params="${activeFilters}" />
	</div>
	

</div>

<script type="text/javascript">
function loadSpeciesnameReco(){
	$('.showObvDetails').each(function(){
		var observationId = $(this).attr('rel');
		$(".recoSummary_"+observationId).html('<li style="text-align: center;"><img src="/biodiv/images/spinner.gif" /></li>')
		preLoadRecos(3, 0, false,observationId);
	});
}
function addListLayout(){
	$('.thumbnails>li').css({'width':'100%','margin':'10px 0px'});
	$('.prop').css('clear','inherit');
	$('.showObvDetails, .view_bootstrap_gallery').show();
	$('.species_title_wrapper').hide();
	loadSpeciesnameReco();
	initializeLanguage();

}

function addGridLayout(){
	$('.thumbnails>li').css({'width':'inherit','margin':'0px 5px'});
	$('.prop').css('clear','both');
	$('.species_title_wrapper').show();
	$('.showObvDetails, .view_bootstrap_gallery').hide();
}

$(document).ready(function(){

	$('#obvList').click(function(){
			$(this).addClass('active');
			$('#obvGrid').removeClass('active');
			addListLayout();
	});

	$('#obvGrid').click(function(){
			$(this).addClass('active');
			$('#obvList').removeClass('active');
			addGridLayout();
	});

 	$(document).on('click','.clickSuggest',function(){	
		$(this).next().toggle('slow');
	});

	   $('.addRecommendation').bind('submit', function(event) {
	   		var that = $(this);
            $(this).ajaxSubmit({ 
                url:"${uGroup.createLink(controller:'observation', action:'addRecommendationVote')}",
                dataType: 'json', 
                type: 'GET',
                beforeSubmit: function(formData, jqForm, options) {
                	console.log(formData);
                    updateCommonNameLanguage();
                    return true;
                }, 
                success: function(data, statusText, xhr, form) {
                    if(data.status == 'success' || data.success == true) {
                    	console.log(data);
                        if(data.canMakeSpeciesCall === 'false'){
                            $('#selectedGroupList').modal('show');
                        } else{
                            preLoadRecos(3, 0, false,data.instance.observation);
                            setFollowButton();
                            showUpdateStatus(data.msg, data.success?'success':'error');
                        }
                    } else {
                        showUpdateStatus(data.msg, data.success?'success':'error');
                    }
                    $(".addRecommendation_"+data.instance.observation)[0].reset();
                    $("#canName").val("");
                    return false;
                },
                error:function (xhr, ajaxOptions, thrownError){
                    //successHandler is used when ajax login succedes
                    var successHandler = this.success, errorHandler = showUpdateStatus;
                    handleError(xhr, ajaxOptions, thrownError, successHandler, errorHandler);
                } 
            });
            event.preventDefault();
        });

});
</script>