import groovyx.net.http.ContentType;
import groovyx.net.http.HTTPBuilder;
import groovyx.net.http.Method;

def http = new HTTPBuilder( 'http://premailer.dialect.ca' )

// perform a GET request, expecting JSON response data
http.request( Method.POST, ContentType.JSON ) {
 uri.path = '/api/0.1/documents'
 requestContentType = ContentType.URLENC
 body = [ base_url:'http://indiabiodiversity.org', html:'''
<html><head>
<link href="http://indiabiodiversity.org/biodiv/static/5klQm7BcDgRFAaSce5SDTSEJwP4zsuHTL55ubbySJqw.css" type="text/css" rel="stylesheet" media="screen, projection" /></head>
<link href="http://indiabiodiversity.org/biodiv/static/EbV3G7pUE13A3szJU5ANnfrzOrrkAUxdFKwH61So68N.css" type="text/css" rel="stylesheet" media="screen, projection" />
<body><ul><li class="species.participation.Observation270893 activity_post">




<div class="activityFeed-container">

	
<div class="yj-message-container">
	<div class="yj-avatar">
		<a href="/user/show/3121">
			<img title="Ravi Vaidyanathan" src="http://pamba.strandls.com/biodiv/images/users/user.png" class="small_profile_pic">
		</a>
	</div>
	






<div class="activityFeedContext thumbnails">
	<div class="feedParentContext thumbnail clearfix" style="display:table">
		
			
			


<div title="" style="display: table-cell;height:220px;float:left;vertical-align:middle" class="figure span3 observation_story_image">
	<a name="lnull" href="/observation/show/270893?pos=">
		
			<img src="http://thewesternghats.in/biodiv/observations/f7893380-7eb0-438f-bb4b-bec35782a5f0/blue_oak_leaf_th.jpg" class="img-polaroid">
		
	</a>

</div>


<style>


.observation .prop .value {
	margin-left:10px;
}


</style>
<div style="overflow:visible;" class="observation_story">
	<div class="observation-icons">
		<span title="Arthropods" class="group_icon species_groups_sprites active arthropods_gall_th"></span>

		
			<span title="Forest" class="habitat_icon group_icon habitats_sprites active forest_gall_th"></span>
		
	</div>
	<div class="span7">

		<div class="prop">
			
				<i class="pull-left icon-share-alt"></i>
			
			<div class="value">
				<div class="species_title">
	
	
		

		
			<div title="Blue Oak Leaf" class="common_name ellipsis">
				Blue Oak Leaf
			</div>
		
	
</div>
			</div>
		</div>


		<div class="prop">
			
				<i class="pull-left icon-map-marker"></i>
			
			<div class="value ellipsis">
				
					Mumbai
				
				<!-- <br /> Lat:
				19.24
				, Long:
				72.94
				-->
			</div>
		</div>
		
		
		
		
		
		

		<div class="prop">
			
				<i class="pull-left icon-time"></i>
			
			<div class="value">
				<time datetime="1320431400000" class="timeago">November  5, 2011</time>
			</div>
		</div>

		
		

		
			<div class="prop">
				<i class="pull-left icon-eye-open"></i>
				<div class="value">
					9
				</div>
			</div>

			
		
		</div>
		<div style="margin-left:0px;" class="row">
		
<div style="width: 100%" class="story-footer">
	
</div>

			<div style="float: right; clear: both;">
		


<div class="signature clearfix thumbnail">
		<div style="display:table;height:40px;" class="figure user-icon pull-left">
			<a href="/user/show/3121"> <img title="Ravi Vaidyanathan" class="small_profile_pic" src="http://pamba.strandls.com/biodiv/images/users/user.png" style="float: left;"></a>
		</div>
		<div style="margin-left:35px" class="story">

		<a href="/user/show/3121" title="Ravi Vaidyanathan">
			<span style="display:block;" class="ellipsis"> Ravi Vaidyanathan
		</span>
			 </a>

		






			<div style="position:static;" class="story-footer">
				<div title="No of Observations" class="footer-item">
					<i class="icon-screenshot"></i>
					73
				</div>





				
				<div title="No of Identifications" class="footer-item">
					<i class="icon-check"></i>
					109
				</div>





				
			</div>

		</div>

	</div>

		</div>
	</div>


	


	
</div>

		
	</div>
	
	
	

<div class="activityfeed activityfeedSpecific">
	<a onclick="loadNewerFeedsInAjax($(this).closest(&quot;.activityfeedSpecific&quot;), false);return false;" title="load new feeds" href="#" style="display:none;" class="activiyfeednewermsg yj-thread-replies-container yj-show-older-replies">Click to see new feeds</a>

	<input type="hidden" value="1360046747091" name="newerTimeRef">
	<input type="hidden" value="1360046745727" name="olderTimeRef">
	<input type="hidden" value="Specific" name="feedType">
	<input type="hidden" value="" name="feedCategory">
	<input type="hidden" value="" name="feedClass">
	<input type="hidden" value="oldestFirst" name="feedOrder">
	<input type="hidden" value="readOnly" name="feedPermission">
	<input type="hidden" value="manual" name="refreshType">
	<input type="hidden" value="270893" name="rootHolderId">
	<input type="hidden" value="species.participation.Observation" name="rootHolderType">
	<input type="hidden" value="" name="activityHolderId">
	<input type="hidden" value="" name="activityHolderType">
	<input type="hidden" value="/activityFeed/getFeeds" name="feedUrl">
	<input type="hidden" value="" name="webaddress">
	
	<input type="hidden" value="false" name="isCommentThread">
	<input type="hidden" value="270893" name="subRootHolderId">
	<input type="hidden" value="species.participation.Observation" name="subRootHolderType">
	<input type="hidden" value="270893" name="feedHomeObjectId">
	<input type="hidden" value="species.participation.Observation" name="feedHomeObjectType">
	
		
		
			<a onclick="loadOlderFeedsInAjax($(this).closest(&quot;.activityfeedSpecific&quot;));return false;" title="show feeds" href="#" class="activiyfeedoldermsg yj-thread-replies-container yj-show-older-replies">Show 3 older feeds &gt;&gt;</a>
		
		<ul>
			




	
	<li class="species.participation.Observation270893 activity_post">




<div class="activityFeed-container">

	


	<div class="yj-message-container">
		<div class="yj-avatar">
			<a href="/user/show/3121">
				<img title="Ravi Vaidyanathan" src="http://pamba.strandls.com/biodiv/images/users/user.png" class="small_profile_pic">
			</a>
		</div>
		<b> Ravi Vaidyanathan :<span class="yj-context"> Observation updated</span></b>

	<div class="feedActivityHolderContext yj-message-body">
		<pre>User updated the observation details</pre>
	</div>


		<div>
			<time datetime="1360046745727" class="timeago">about 7 hours ago</time>
		</div>
	</div>



</div>
</li>

	
	<li class="species.participation.Observation270893 activity_post">




<div class="activityFeed-container">

	


	<div class="yj-message-container">
		<div class="yj-avatar">
			<a href="/user/show/3121">
				<img title="Ravi Vaidyanathan" src="http://pamba.strandls.com/biodiv/images/users/user.png" class="small_profile_pic">
			</a>
		</div>
		<b> Ravi Vaidyanathan :<span class="yj-context"> Suggested species name <a href="/biodiv/species/show/235632"><i>Kallima horsfieldii</i></a></span></b>


		<div>
			<time datetime="1360046747091" class="timeago">about 7 hours ago</time>
		</div>
	</div>



</div>
</li>


		</ul>
	
</div>

</div>
</div>


</div>
</li></ul></body></html>''' ]

 headers.'User-Agent' = 'Mozilla/5.0 Ubuntu/8.10 Firefox/3.0.4'

 // response handler for a success response code:
 response.success = { resp, json ->
   println resp.status
   println json

  }

 // handler for any failure status code:
 response.failure = { resp ->
   println "Unexpected error: ${resp.status} : ${resp.statusLine.reasonPhrase}"
 }
}