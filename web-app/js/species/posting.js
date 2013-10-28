var selectedObjects = new Array();
var rejectedObjects = new Array();

function updateObjSelection(id, comp){
        $('#action-tabs a').click(function (e) {
        var tab = $(this);
        if(tab.parent('li').hasClass('active')){
                $("#action-tab-content .tab-pane").removeClass('active');
                tab.parent('li').removeClass('active');
        }
    });

	//$('.post-to-groups .post-main-content').show(1000);
	//$('.post-to-groups').parent().slideDown(1000);
	$(comp).parent().removeClass('mouseover').addClass('mouseoverfix');
	if($(comp).hasClass('selectedItem')){
		$(comp).removeClass('selectedItem');
		if($('.post-to-groups .select-all').hasClass('active')){
			rejectedObjects.push(id);
		}else{
			selectedObjects.splice(array.indexOf(id), 1);	
		}
	}else{
		$(comp).addClass('selectedItem');
		if($('.post-to-groups .select-all').hasClass('active')){
			rejectedObjects.splice(array.indexOf(id), 1);
		}else{
			selectedObjects.push(id);	
		}
	}
}

function updateListSelection(comp){
	selectedObjects = new Array();
	if($(comp).hasClass('select-all')){
		if(confirm('This will select all the resoures from list. Are you sure ?')){
			$('.post-to-groups .select-all').addClass('active')
			$('.post-to-groups .reset').removeClass('active')
			$('.mainContentList .selectable input[type="checkbox"]').prop('checked', true);
			rejectedObjects = new Array();
		}
	}else{
		$('.post-to-groups .select-all').removeClass('active')
		$('.post-to-groups .reset').addClass('active')
		$('.mainContentList .selectable input[type="checkbox"]').prop('checked', false);
	}
}

function submitToGroups(submitType, objectType, url, isBulkPull, id){
	if(isBulkPull){
		if(!$('.post-to-groups .select-all').hasClass('active') && selectedObjects.length === 0){
			alert('Please select at least one object');
			return;
		}	
	}else{
		selectedObjects = rejectedObjects = [id];
	}
	
	userGroups = getSelectedUserGroups();
	if(userGroups.length === 0){
		alert('Please select at least one group')
		return; 
	}
	
//	if(submitType === 'post'){
//		console.log("posting " + selectedObjects +  ' on groups ' + userGroups);
//	}else{
//		console.log("unposting " + selectedObjects +  ' on groups ' + userGroups);
//	}
	
	var pullType = (isBulkPull) ? 'bulk' : 'single'
	var selectionType = $('.post-to-groups .select-all').hasClass('active') ? 'selectAll' : 'reset'
	var objectIds = (selectionType === 'selectAll') ?  rejectedObjects : selectedObjects
	var filterUrl = window.location.href
	if(pullType !== 'single'){
		$(".alertMsg").removeClass('alert alert-error').removeClass('alert alert-success').addClass('alert alert-info').html("Processing...");
		$("html, body").animate({ scrollTop: 0 });
	}
	$.ajax({
 		url: url,
 		type: 'POST',
		dataType: "json",
		data:{'pullType':pullType, 'selectionType':selectionType, 'objectType':objectType, 'objectIds':objectIds.join(","), 'submitType':submitType, 'userGroups':userGroups.join(","), 'filterUrl':filterUrl},
		success: function(data) {
			if(data.success){
				if(pullType === 'single'){
					$(".resource_in_groups").replaceWith(data.resourceGroupHtml);
                                        $(".feature-user-groups").replaceWith(data.featureGroupHtml);
                                        $(".userGroups button").click(function(e){
                                            if($(this).hasClass('active')) {
                                                //trying to unselect group

                                                //if on obv create page	and one group is coming as parent group		
                                                if($("#userGroups").hasClass('create') && ($("#userGroups button.create").length > 0)){
                                                    //this group is parent group
                                                    if($(this).hasClass('create') && ${parentGroupId != ''} && $(this).hasClass('${parentGroupId}')){
                                                        alert("Can't unselect parent group");
                                                    }else{
                                                        //un selecting other group
                                                        $(this).removeClass('btn-success');
                                                        $(this).find(".icon-ok").removeClass("icon-black").addClass("icon-white");
                                                    }	
                                                }else{
                                                    $(this).removeClass('btn-success');
                                                    $(this).find(".icon-ok").removeClass("icon-black").addClass("icon-white");
                                                    if($(this).hasClass("single-post")) {
                                                        $("#groupsWithSharingNotAllowed button.single-post").removeClass('disabled')
                                            $("#groupsWithSharingAllowed button.multi-post").removeClass('disabled')
                                                    } else {
                                                        if($("#groupsWithSharingAllowed button.active").length == 0) {
                                                            $("#groupsWithSharingAllowed button.multi-post").removeClass('disabled')
                                                        }
                                                    }
                                                }
                                            } else {
                                                //trying to select new group

                                                //if on obv create page and one group is coming as parent group
                                                if($("#userGroups").hasClass('create') && ($("#userGroups button.create").length > 0)){
                                                    //either current one belongs to exclusive group or parent group is exclusive group
                                                    if($(this).hasClass("single-post") ||($("#groupsWithSharingNotAllowed button.create").length > 0)){
                                                        alert("Can't select this group because it will unselect parent group");
                                                    }else{
                                                        //parent group is multipost one and this new group is also belong to multi select so selecting it
                                                        $(this).removeClass('disabled').addClass('btn-success');
                                                        $(this).find(".icon-ok").removeClass("icon-white").addClass("icon-black");
                                                    }
                                                }else{
                                                    //on obv edit page
                                                    if($(this).hasClass("single-post")) {
                                                        $("#groupsWithSharingAllowed button.multi-post").addClass('disabled').removeClass('active btn-success').find(".icon-ok").removeClass("icon-black").addClass("icon-white");
                                                        $("#groupsWithSharingNotAllowed button.single-post").addClass('disabled').removeClass('active btn-success').find(".icon-ok").removeClass("icon-black").addClass("icon-white");
                                                        $(this).removeClass('disabled').addClass('btn-success');
                                                    } else {
                                                        $("#groupsWithSharingNotAllowed button.single-post").addClass('disabled').removeClass('active btn-success').find(".icon-ok").removeClass("icon-black").addClass("icon-white");
                                                        $(this).removeClass('disabled').addClass('btn-success');
                                                    }
                                                    $(this).find(".icon-ok").removeClass("icon-white").addClass("icon-black");
                                                }
                                            }
                                            e.preventDefault();
                                        });
                                        $('#featureNotes').keydown(function(){

                                            if(this.value.length > 400){
                                                return false;
                                            }
                                            $("#remainingC").html("Remaining characters : " +(400 - this.value.length));
                                        });

				}else{
					$(".alertMsg").removeClass('alert alert-info').addClass('alert alert-success').html(data.msg);
				}
                                $('#myTab a:first').tab('show');
				//$('.post-to-groups .post-main-content').slideToggle(150);
			}else{
				$(".alertMsg").removeClass('alert alert-info').addClass('alert alert-error').html(data.msg);
			}
			updateFeeds();
			return false;
		}, error: function(xhr, status, error) {
			console.log(xhr.responseText);
	   	}
	});
}

//this will be called in last action of load more
function updateGroupPostSelection(){
	var comp = $('.post-to-groups .select-all')
	if(comp && comp.hasClass('active')){
		$('.mainContentList .selectable input[type="checkbox"]').prop('checked', true);
	}
}

function getSelectedUserGroups($context){
    var userGroups = [], $selector; 
    if($context == undefined) {
        $selector = $('.userGroups button[class~="btn-success"]')
    } else {
        $selector = $context.find('.userGroups button[class~="btn-success"]')
    }
    $selector.each (function() {
        userGroups.push($(this).attr('value'));
    });
    return userGroups;	
}

function reInitializeGroupPost(){
	var selectedObjects = new Array();
	var rejectedObjects = new Array();
}

