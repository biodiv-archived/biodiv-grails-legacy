<g:javascript src="jquery.cookie.js"
	base="${grailsApplication.config.grails.serverURL+'/js/jquery/'}"></g:javascript>
<g:javascript>
	
	$(document).ready(function(){
		$('#as_list').click(function(){
			$('#tagCloud').hide();
			$('#tagList').show();
			$(this).addClass('active');
			$('#as_cloud').removeClass('active');
			$.cookie("tags_view", "list");
		});
		
		$('#as_cloud').click(function(){
			$('#tagCloud').show();
			$('#tagList').hide();
			$(this).addClass('active');
			$('#as_list').removeClass('active');
			$.cookie("tags_view", "cloud");
		});
		
		if ($.cookie("tags_view") == "cloud") {
			$('#tagCloud').show();
			$('#tagList').hide();
			$('#as_cloud').addClass('active');
			$('#as_list').removeClass('active');
		}else{
			$('#tagList').show();
			$('#tagCloud').hide();
			$('#as_cloud').removeClass('active');
			$('#as_list').addClass('active');
		
		}
	});
</g:javascript>
<h5>${count} <g:message code="default.tags.title" default="Tags" /></h5>
View as 
<span id="as_list" class="btn btn-mini">List</span> 
<span id="as_cloud" class="btn btn-mini active">Cloud</span>
<div id="tagCloud" class="grid_4 sidebar_section">
	<obv:showTagsCloud/>
</div>
<div id="tagList" class="grid_4 sidebar_section" style="display:none;">
	<obv:showTagsList/>
</div>	

