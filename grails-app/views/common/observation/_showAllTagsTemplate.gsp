<script type="text/javascript">
	
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
		
		if ($.cookie("tags_view") == "list") {
			$('#tagCloud').hide();
			$('#tagList').show();
			$('#as_cloud').removeClass('active');
			$('#as_list').addClass('active');
		}else{
			$('#tagList').hide();
			$('#tagCloud').show();
			$('#as_cloud').addClass('active');
			$('#as_list').removeClass('active');
		
		}
	});
</script>

<div id="tags_section">
	<a data-toggle="collapse" href="#tags"><h5>
		<i class="icon-tags"></i>
		<g:message code="default.tags.title" default="Tags" />
	</h5></a>
	<div id="tags">
		<div class="tag-tools">
			<g:message code="msg.View as" /> <span id="as_list" class="btn"><g:message code="msg.List" /></span> <span id="as_cloud"
				class="btn active"><g:message code="msg.Cloud" /></span>
		</div>
		<div id="tagCloud" class="tagsView">
			<obv:showTagsCloud model="['tags': tags, 'isAjaxLoad':isAjaxLoad]" />
		</div>
		<div id="tagList" class="tagsView" style="display: none;">
			<obv:showTagsList model="['tags': tags, 'isAjaxLoad':isAjaxLoad, controller:params.controller]" />
		</div>
	</div>
</div>
