<%@page import="species.dataset.DataPackage"%>
<style>
    .observation .prop .value {
        margin-left:260px;
    }
</style>
<div class="">

	<!-- main_content -->
	<div class="list" style="margin-left:0px;clear:both">
		<div class="filters" style="position: relative;">
		<div class="filters" style="position: relative;">
		<%--
            <obv:showGroupFilter
			    model="[forObservations:false, hideHabitatFilter:true]" />
        --%>
		</div>
	
		</div>
		<div class="observation thumbwrap">
			<div class="observation">
				<div>
					<obv:showObservationFilterMessage
						model="['instanceList':instanceList, 'instanceTotal':instanceTotal, 'queryParams':queryParams, resultType:'dataset']" />
						
				</div>
				<div style="clear: both;"></div>
				

                <div class="btn-group pull-left" style="z-index: 10">
                    <button id="selected_sort" class="btn dropdown-toggle"
                        data-toggle="dropdown" href="#" rel="tooltip"
                        data-original-title="${g.message(code:'showobservationlistwrapertemp.sort')}">
                        <g:if test="${params.sort == 'createdOn'}">
                        <g:message code="button.latest" />
                        </g:if>
                        <g:else>
                        <g:message code="button.last.updated" />
                        </g:else>
                        <span class="caret"></span>
                    </button>
                    <ul id="sortFilter" class="dropdown-menu" style="width: auto;">
                        <li class="group_option"><a class=" sort_filter_label"
                            value="createdOn"> <g:message code="button.latest" /> </a></li>
						<li class="group_option"><a class=" sort_filter_label"
							value="lastRevised"> <g:message code="button.last.updated" /> </a></li>
					</ul>


				</div>

					

                <div class="btn-group pull-left" style="z-index: 10">
                    <button id="selected_dp" class="btn dropdown-toggle"
                        data-toggle="dropdown" href="#" rel="tooltip"
                        data-original-title="Filter by data package">
                        All Data Packages 
                        <span class="caret"></span>
                    </button>
                    <ul id="dataPackageFilter" class="dropdown-menu" style="width: auto;">
                        <g:each in="${DataPackage.findAllByIsDeleted(false)}" var="dataPackage">
                            <li class="group_option">
                                <a class=" dp_filter_label" value="${dataPackage.id}"> ${dataPackage.title} </a>
                            </li>
                        </g:each>
					</ul>


				</div>

			</div>
            <div class="span12 right-shadow-box" style="margin:0px;clear:both;">
                <g:render template="/dataset/showDatasetListTemplate"/>
            </div>
            <!--div class="span4" style="position:relative;top:20px">

                <div id="observations_list_map" class="observation sidebar_section"
                    style="clear:both;overflow:hidden;display:none;">
                    <h5><g:message code="default.species.distribution.label" /></h5>
                </div>
             </div-->
        </div>
    </div>

	<!-- main_content end -->
</div>
<asset:script type="text/javascript">
$(document).ready(function() {
    window.params.tagsLink = "${uGroup.createLink(controller:'dataset', action: 'tags')}"

    $("#map_view_bttn a").click(function(){
        $(this).parent().css('background-color', '#9acc57');
        $('#observations_list_map').slideToggle(mapViewSlideToggleHandler);
    });
    <g:if test="${params.isMapView?.equalsIgnoreCase('true') || params.bounds}">
    </g:if>
        $("#map_view_bttn a").click();

    $('#big_map_canvas').on('maploaded', function(){
        /*map.on('viewreset', function() {
            refreshList(getSelectedBounds());
        });*/
    });
    
    $("#refreshListForBounds").click(function() {
        var mapLocationPicker = $('#big_map_canvas').data('maplocationpicker');
        refreshList(mapLocationPicker.getSelectedBounds());
    });

    $("#resetMap").click(function() {
        var mapLocationPicker = $('#big_map_canvas').data('maplocationpicker');
        //refreshList(mapLocationPicker.getSelectedBounds());
        $("#bounds").val('');
        refreshMapBounds(mapLocationPicker);
    });
    mapViewSlideToggleHandler();


    $('.dp_filter_label').click(function(){
        var caret = '<span class="caret"></span>'
        if(stringTrim(($(this).html())) == stringTrim($("#selected_dp").html().replace(caret, ''))){
            return true;
        }
	    $('.dp_filter_label.active').removeClass('active');
	    $(this).addClass('active');
	    $("#selected_dp").html($(this).html() + caret);
	    updateGallery(undefined, window.params.queryParamsMax, window.params.offset, undefined, window.params.isGalleryUpdate);
	    return true;   
    });

	$('.dp_filter_label[value="${params.dataPackage}"]').trigger('click');
});
</asset:script>

