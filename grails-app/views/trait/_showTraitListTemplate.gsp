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
                 <ul id="trait${j}" class="grid_view thumbnails obvListwrapper}">
				<g:each in="${inst.value}" status="i" var="instance">
					<li class="thumbnail" style="clear: both;margin-left:0px;width:100%;">
                    <g:render template="/trait/showTraitTemplate" model="['trait':instance, 'factInstance':factInstance, 'fromSpeciesShow':fromSpeciesShow]"/>
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
	
    $('.editFact').click(function(){
        $(this).parent().parent().find('.row:first').hide();
        $(this).hide();
        $(this).parent().find('.submitFact, .cancelFact').show();
        $(this).parent().parent().find('.editFactPanel').show();
        console.log($(this).parent().parent().find('.editFactPanel'));
        //$(this).parent().find('.editFact').show();
	});

	$('.cancelFact').click(function(){
        $(this).parent().parent().find('.editFactPanel').hide();
        $(this).parent().find('.submitFact, .cancelFact').hide();
        $(this).parent().parent().find('.row:first').show();
        $(this).hide();
        $(this).parent().find('.editFact').show();
	});

	$(document).on('click', '.submitFact', function () {
		var id = $(this).data("id")
		var hbt = '',trait='',value='', selTrait={}; 
        var selectTrait;
        $(this).parent().parent().find('.editFactPanel:first').find('.traitFilter button, .traitFilter .none, .traitFilter .any, .trait button, .trait .none, .trait .any').each(function() {
            console.log($(this))
            if($(this).hasClass('active')) {
                selectedTrait = this;
                trait = $(this).attr('data-tid');
                tValue = $(this).attr('data-tvid');
                selTrait[trait] += $(this).attr('value')+',';
                console.log($(this).attr('value'));
            }
        });
		var traits = selTrait;
		var traitsStr = '';
		for(var m in traits) {
		    traitsStr += m+':'+traits[m]+';';
		}
        var params = {};
        params['traits'] = traitsStr;
        params['objectId'] = "${instance.id}";
        params['objectType'] = "${instance.class.getCanonicalName()}";
		$.ajax({ 
			url:'${uGroup.createLink(controller:'fact', action:'update', 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)}',
			data:params,
			success: function(data, statusText, xhr, form) {
                //TODO:update traits panel
                $(this).parent().parent().find('.row:first').show();
                $(this).parent().parent().find('.editFactPanel').hide();
                $(this).parent().find('.submitFact, .cancelFact').hide();
                $(this).parent().parent().find('.row:first').show();
                $(this).hide();
                $(this).parent().find('.editFact').show();//.css("position","");
			},
			error:function (xhr, ajaxOptions, thrownError){
			    console.log('error');
		    }
		});

		/*
		$('#trait_'+id).show();
		$('#editFactPanel_'+id).hide();
		$(this).hide();
		$('#cancelFact_'+id).hide();
		$('#editFact_'+id).show();
		$('#edit_btn_'+id).css("position","absolute");
        */
	});

    $(document).on('click', '.trait button, .trait .all, .trait .any, .trait .none', function(){
        if($(this).hasClass('MULTIPLE_CATEGORICAL')) {
            $(this).parent().parent().find('.all, .any, .none').removeClass('active btn-success');
            if($(this).hasClass('active')) 
                $(this).removeClass('active btn-success');
            else
                $(this).addClass('active btn-success');
        } else {
            $(this).parent().parent().find('button, .all, .any, .none').removeClass('active btn-success');
            $(this).addClass('active btn-success');
        }

    });
});
</asset:script>

