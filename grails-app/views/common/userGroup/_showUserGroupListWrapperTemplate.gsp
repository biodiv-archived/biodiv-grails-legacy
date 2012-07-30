<%@page import="species.auth.SUser"%>
<%@ page import="species.groups.UserGroup"%>
<div class="filters" style="position: relative;">
</div>
<div class="tags_section span3" style="float: right;">
	<g:if test="${params.action == 'search' }">
		<obv:showAllTags
			model="['tags':tags , 'count':tags?tags.size():0, 'isAjaxLoad':true]" />
	</g:if>
	<g:else>
		<uGroup:showAllTags
			model="['tagFilterByProperty':'All' , 'params':params, 'isAjaxLoad':true]" />
	</g:else>
</div>
<div class="row">
	<!-- main_content -->
	<div class="list span9">

		<div class="observations thumbwrap">
			<div class="observation">

				<obv:identificationByEmail
					model="['source':'userGroupList', 'requestObject':request]" />
				
			</div>

			<uGroup:showUserGroupsList/>
		</div>
	</div>

	<!-- main_content end -->
</div>


<!--container end-->
<r:script>	
        $(document).ready( function() {
        					
			function getSelectedTag() {
				var tag = ''; 
				tag = $("li.tagit-choice.active").contents().first().text();
				if(!tag){
					tag = $("#tc_tagcloud a.active").contents().first().text();	
				}
				if(tag) {
                	tag = stringTrim(tag.replace(/\s*\,\s*$/,''));
                	return tag;
                }	
            } 
            
	        function getFilterParameters(url, limit, offset, removeUser) {
                    var params = url.param();
            
                    if(limit != undefined) {
                        params['max'] = limit.toString();
                    }

                    if(offset != undefined) {
                        params['offset'] = offset.toString();
                    }
					
					var tag = getSelectedTag();
					if(tag){
						params['tag'] = tag;
					}else{
						//removing old tag from url
						if(params['tag'] != undefined){
							delete params['tag'];
						}
					}
					
					var query = $( "#searchTextField" ).val();
					if(query){
						params['query'] = query;
					}else{
						//removing old tag from url
						if(params['query'] != undefined){
							delete params['query'];
						}
					}
					
					if(removeUser){
						if(params['user'] != undefined){
							delete params['user'];
						}
					}
					
					return params;
                }	

				function setActiveTag(activeTag){
					if(activeTag != undefined){
 							$('li.tagit-choice').each (function() {
 								if(stringTrim($(this).contents().first().text()) == stringTrim(activeTag)) {
                       				$(this).addClass('active');
                       			}
                       			else{
                       				if($(this).hasClass('active')){
                       					$(this).removeClass('active');
                       				}
                       			}
                       		});
                       		
                       		$('#tc_tagcloud a').each(function() {
 								if(stringTrim($(this).contents().first().text()) == stringTrim(activeTag)) {
                       				$(this).addClass('active');
                       			}else{
                       				if($(this).hasClass('active')){
                       					$(this).removeClass('active');
                       				}
                       			}
               				});
               				
				 		}
				}
				
                function updateListPage(activeTag) {
  					return function (data) {
  						$('.observations_list').replaceWith(data.obvListHtml);
						$('#info-message').replaceWith(data.obvFilterMsgHtml);
  						$('#tags_section').replaceWith(data.tagsHtml);
  						$('.observation_location_wrapper').replaceWith(data.mapViewHtml);
  						setActiveTag(activeTag);
					}
				}
                
                function updateGallery(target, limit, offset, removeUser, isGalleryUpdate) {
                    if(target === undefined) {
                            target = window.location.pathname + window.location.search;
                    }
                    
                    var a = $('<a href="'+target+'"></a>');
                    var url = a.url();
                    var href = url.attr('path');
                    var params = getFilterParameters(url, limit, offset, removeUser);
                    //alert(" tag in params " + params['tag'] );
                    isGalleryUpdate = (isGalleryUpdate == undefined)?true:isGalleryUpdate
                    if(isGalleryUpdate)
                    	params["isGalleryUpdate"] = isGalleryUpdate;
                    var recursiveDecoded = decodeURIComponent($.param(params));
                    
                    var doc_url = href+'?'+recursiveDecoded;
                    var History = window.History;
                    delete params["isGalleryUpdate"]
                    History.pushState({state:1}, "Species Portal", '?'+decodeURIComponent($.param(params))); 
                    //alert("doc_url " + doc_url);
                    if(isGalleryUpdate) {
	                   	$.ajax({
	  						url: doc_url,
	  						dataType: 'json',
	  						
	  						beforeSend : function(){
	  							$('.observations_list').css({"opacity": 0.5});
	  							$('#tags_section').css({"opacity": 0.5});
	  						},
	  						
	  						success: updateListPage(params["tag"]),
							statusCode: {
		    					401: function() {
		    						show_login_dialog();
		    					}	    				    			
		    				},
		    				error: function(xhr, status, error) {
		    					var msg = $.parseJSON(xhr.responseText);
		    					$('.message').html(msg);
							}
						});
					} else {
						window.location = doc_url;
					}
                }
           
                $( "#search" ).click(function() {                		
						updateGallery(undefined, ${queryParams.max}, 0);
                		return false;
				});
				
</r:script>
