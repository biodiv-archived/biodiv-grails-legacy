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

 <div class="observations_list observation" style="clear: both;">
	<div class="mainContentList">
		<div class="mainContent" name="p${params?.offset}">
		    <div class="filters">
            <g:each in="${instanceFieldList}" status="j" var="inst">                
                <div class="sidebar_section">
                <g:if test="${fromObservationShow!='show'}">
	              <a class="speciesFieldHeader"  data-toggle="collapse" href="#trait${j}">
                    	<h5>${inst.key}</h5>
                   </a> 
                   </g:if>
                 <ul id="trait${j}" class="grid_view thumbnails obvListwrapper">
				<g:each in="${inst.value}" status="i" var="instance">
					<li class="thumbnail" style="clear: both;margin-left:0px;width:100%;">
                    <g:render template="/trait/showTraitTemplate" model="['trait':instance, 'factInstance':factInstance, 'fromSpeciesShow':fromSpeciesShow, 'queryParams':queryParams, 'editable':editable]"/>
					</li>
				</g:each>
			</ul>               
			</div>
			</g:each>
			</div>			
		</div>
	</div>
        
<% /*    <g:if test="${instanceTotal > (queryParams?.max?:0)}">
		<div class="centered">
			<div class="btn loadMore">
				<span class="progress" style="display: none;"><g:message code="msg.loading" /></span> <span
					class="buttonTitle"><g:message code="msg.load.more" /></span>
			</div>
		</div>
	</g:if>*/ %>
	
	<%
		activeFilters?.loadMore = true
		activeFilters?.webaddress = userGroupInstance?.webaddress
	%>
	<div class="paginateButtons" style="visibility: hidden; clear: both">
		<p:paginate total="${instanceTotal?:0}" action="${params.action}" controller="${params.controller?:'observation'}"
			userGroup="${userGroupInstance}" userGroupWebaddress="${userGroupWebaddress?:params.webaddress}"
			 max="${queryParams?.max}" params="${activeFilters}" />
	</div>	
</div>

<asset:script>
$(document).ready(function(){
	$('.icon-question-sign').tooltip();
	
	$(document).on('click', '.editFact', function () {
        $(this).parent().parent().find('.row:first').hide();
        $(this).hide();
        $(this).parent().find('.submitFact, .cancelFact').show();
        $(this).parent().parent().find('.editFactPanel').show();
        console.log($(this).parent().parent().find('.editFactPanel'));
        return false;
	});

	$(document).on('click', '.cancelFact', function () {

        $(this).parent().parent().find('.editFactPanel').hide();
        $(this).parent().find('.submitFact, .cancelFact').hide();
        $(this).parent().parent().find('.row:first').show();
        $(this).hide();
        $(this).parent().find('.editFact').show();
        $(this).parent().parent().find('.alert').removeClass('alert alert-error').hide();
	});

    function onSubmit($me) {
    	var id = $me.data("id");
        var traitsStr = getSelectedTraitStr($me.parent().parent().find('.trait button, .trait .none, .trait .any'), true);
        var params = {};
        params['traits'] = traitsStr;
        params['traitId'] = id;
        params['objectId'] = "${instance?.id}";
        params['objectType'] = "${instance?.class?.getCanonicalName()}";
		$.ajax({ 
			url:'${uGroup.createLink(controller:'fact', action:'update', 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)}',
            method:'POST',
			data:params,
			success: function(data, statusText, xhr, form) {
                //TODO:update traits panel
                console.log(data);
                if(data.success) {
                    $me.parent().parent().find('.alert').removeClass('alert alert-error').addClass('alert alert-info').html(data.msg).show();
                    $me.parent().parent().replaceWith(data.model.traitHtml);
                    $me.parent().parent().find('.row:first').show();
                    $me.parent().parent().find('.editFactPanel').hide();
                    $me.parent().find('.submitFact, .cancelFact').hide();
                    $me.parent().parent().find('.row:first').show();
                    $me.hide();
                    $me.parent().find('.editFact').show();//.css("position","");
                } else {
                    $me.parent().parent().find('.alert').removeClass('alert alert-info').addClass('alert alert-error').html(data.msg).show();
                }
			},
            error:function (xhr, ajaxOptions, thrownError){
                //successHandler is used when ajax login succedes
                var successHandler = this.success, errorHandler = function() {
                    //TODO:show error msg
                    console.log(arguments);
                    $me.parent().parent().find('.alert').removeClass('alert alert-info').addClass('alert alert-error').html(arguments.msg).show();
                }
                handleError(xhr, ajaxOptions, thrownError, successHandler, errorHandler);
            } 
		});
    }

	$(document).on('click', '.submitFact', function () {
        var $me = $(this);
        onSubmit($me);
    });

});
</asset:script>

