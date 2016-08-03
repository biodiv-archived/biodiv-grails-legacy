var accDLContent, accWLContent, accCLContent;
var synDLContent, synWLContent, synCLContent;
var comDLContent, comWLContent, comCLContent;
var speciesDLContent, speciesWLContent, speciesCLContent;
var oldName = '', oldRank = '' , oldStatus = '';
var genusMatchResult, taxonRanks, nameRank, genusTaxonMsg
var taxonGridSelectedRow = {};

function createListHTML(list, nameType, isOrphanList) {
    var listContent = "<ul class='"+nameType+"'>";
    var selectedName = $('.name').val().toLowerCase();
    $.each(list, function(index, value){
        var x = "";
        if(value.isflagged == true){
            var temp = value.flaggingreason + '';
            x = "<i class='icon-flag' title='"+temp+"'></i>";
        }
        var klass = "";
        if(selectedName && value.name.toLowerCase() == selectedName) {
            klass = 'taxon-highlight';
        }
        listContent += "<li class='nameDetails "+value.position+" taxon"+value.rank+" "+klass+"' onclick='getNameDetails("+value.taxonid +","+ value.classificationid+",&quot;"+value.status+"&quot;, this,"+isOrphanList+")'><a>" +value.name +"</a>";
        if(value.id)
            listContent += "<input type='hidden' value='"+value.id+"'>"+x+"</li>"
    });
    listContent += "</ul>";
    return listContent;
}

function processingStart() {
    $("body").css("cursor", "progress");
    $("#searching").show();
    $("HTML").mousemove(function(e) {
        $("#searching").css({
            "top" : e.pageY,
            "left" : e.pageX + 15
        });
    });
}

function processingStop() {
    $("#searching").hide();
    $("body").css("cursor", "default");
}

var taxonGrid;
var positionFilter='';
var statusFilter=[];
var searchString='';
var showCheckBox = false;

function initTaxonGrid(ele) {
    var selectedName = $('.name').val();
    if(selectedName != undefined){
        selectedName = selectedName.toLowerCase();
    }
    var hyperlinkSlickFormatter = function(row, cell, value, columnDef, dataContext) {
        value = dataContext;
        var listContent = '';
        var x = "";
        if(value.isflagged == true){
            var temp = value.flaggingreason + '';
            x = "<i class='icon-flag' title='"+temp+"'></i>";
        }
        var klass = "";
        if(selectedName && value.name.toLowerCase() == selectedName) {
            klass = 'taxon-highlight';
        }
        listContent += "<li class='nameDetails "+value.position+" taxon"+value.rank+" "+klass+" "+value.status+"' onclick='getNameDetails("+value.taxonid +","+ value.classificationid+",&quot;"+value.status+"&quot;, this,false)'><a>" +(value.status=='SYNONYM' ? '<i class="icon-pause"></i>':'')+value.italicisedform +"</a>";
        if(value.id)
            listContent += "<input type='hidden' value='"+value.id+"'>"+x+"</li>";

        return listContent;

    }

    var taxonRankFormatter = function(row, cell, value, columnDef, dataContext) {
        return taxonRanks[value].text;
    }

    var checkboxSelector = new Slick.CheckboxSelectColumn({
        cssClass: "slick-cell-checkboxsel"
    });

    var selectorColumnDef = checkboxSelector.getColumnDefinition();
    var currentFormatter = selectorColumnDef.formatter;
    selectorColumnDef.formatter = function(row, cell, value, columnDef, dataContext) {
        if (window.params.isAdmin || showCheckBox) {
            return currentFormatter(row, cell, value, columnDef, dataContext);
        }
        return "";
    };
        //{id: "taxonid", name: "Id", field: "taxonid", maxWidth:80, resizble:false, sortable:true},
    //{id: "isflagged", name: "Flagged", field: "isflagged", width: 40, cssClass: "cell-effort-driven", formatter: Slick.Formatters.Checkmark, resizable:false}
    var taxonGridColumns = [];
    if(window.params.isAdmin) {
        taxonGridColumns.push(selectorColumnDef);
    }

    taxonGridColumns.push(
    {id: "rank", name: "Rank", field: "rank", width:60, resizable:false, formatter:taxonRankFormatter, sortable:false},
    {id: "name", name: "Name", field: "italicisedform", minWidth:150, cssClass: "cell-title", formatter: hyperlinkSlickFormatter, sortable:false},
    {id: "status", name: "Status", field: "status", width:80, resizable:false, sortable:false},
    {id: "position", name: "Position", field: "position", width:80, resizable:false, sortable:false}
    );
    var taxonGridOptions = {
        enableCellNavigation: true,
        editable: false,
        enableAddRow: false,
        forceFitColumns: true,
        explicitInitialization: true,
        enableColumnReorder: false
    };
    var taxonGridDataView = new Slick.Data.DataView({inlineFilters:true});
    taxonGrid = new Slick.Grid(ele, taxonGridDataView, taxonGridColumns, taxonGridOptions);
    taxonGrid.setSelectionModel(new Slick.RowSelectionModel());

    //var pager = new Slick.Controls.Pager(taxonGridDataView, taxonGrid, $("#taxonPager"));
    //var columnpicker = new Slick.Controls.ColumnPicker(taxonGridColumns, taxonGrid, taxonGridOptions);


    taxonGridDataView.onRowCountChanged.subscribe(function (e, args) {
        taxonGrid.updateRowCount();
        taxonGrid.render();
    });

    taxonGridDataView.onRowsChanged.subscribe(function (e, args) {
        taxonGrid.invalidateRows(args.rows);
        taxonGrid.render();
    });

    taxonGrid.onSort.subscribe(function (e, args) {
         // args.multiColumnSort indicates whether or not this is a multi-column sort.
         //   // If it is, args.sortCols will have an array of {sortCol:..., sortAsc:...} objects.
         //     // If not, the sort column and direction will be in args.sortCol & args.sortAsc.
         //
        var comparer = function(a, b) {
            return (a[args.sortCol.field] > b[args.sortCol.field]) ? 1 : -1;
        }
        taxonGrid.getData().sort(comparer, args.sortAsc);
    });
    
    
    function nameFilter(item, args) {
        if (args.positionFilter != '' && item["position"] != args.positionFilter) {
            return false;
        }
        if (args.statusFilter.length != 0 && args.statusFilter.indexOf(item["status"]) == -1) {
            return false;
        }
        if (args.searchString != "" && item["name"].indexOf(args.searchString) == -1) {
            return false;
        }
        return true;
    }

    taxonGridDataView.setFilterArgs({
        positionFilter: positionFilter,
        statusFilter: statusFilter,
        searchString: searchString
    });

    taxonGridDataView.setFilter(nameFilter);
//    taxonGrid.showTopPanel();

    taxonGrid.registerPlugin(checkboxSelector);
    taxonGrid.init();
    return taxonGrid;
}

function getSelectedStatus() {
    var selectedOptions = $('.filter input[name="taxonStatus"]:checked');

    statusFilter = [];
    for (var i=0; i< selectedOptions.size(); i++) {
        statusFilter[i] = $(selectedOptions[i]).val();
    }
    return statusFilter;
}

function getSelectedPosition() {
    var selectedOptions = $('.filter input[name="taxonPosition"]:checked');

    var positions = [];
    for (var i=0; i< selectedOptions.size(); i++) {
        positions[i] = $(selectedOptions[i]).val();
    }
    return positions;
}


function getSelectedRanks() {
    var ranksToFetch = $('.filter input[name="taxonRank"]:checked ');
    var ranks = [];
    for(var i=0; i < ranksToFetch.length; i++) {
        for (var key=0; key < taxonRanks.length; key++) {
            var rank = taxonRanks[key].text.toLowerCase();
            if($(ranksToFetch[i]).val() == rank) {
                ranks.push(parseInt(key));
                break;
            }
        }
    }
    return ranks; 
}


function getNamesFromTaxon(ele , parentId, statusToFetch, positionsToFetch, ranksToFetch, limit, offset) {
    processingStart();
    changeEditingMode(true);
    //var taxonId = $("input#taxon").val();//$(ele).parent("span").find(".taxDefIdVal").val();
    var classificationId = $('#taxaHierarchy option:selected').val();

    //$('.name').val(ele.text());
   
    $('#txtSearch').val('');


    populateNameDetails();
    var url = window.params.curation.getNamesFromTaxonUrl;
    var params = {'taxon':$(ele).data('taxonid'), parentId:parentId, 'classificationId':classificationId};
    if(typeof limit != 'undefined') params['limit'] = limit;
    if(typeof offset != 'undefined') params['offset'] = offset;
    if(typeof ranksToFetch != 'undefined') params['ranksToFetch'] = ranksToFetch.join(',');
    if(typeof statusToFetch != 'undefined') params['statusToFetch'] = statusToFetch.join(',');
    if(typeof positionsToFetch != 'undefined') params['positionsToFetch'] = positionsToFetch.join(',');

    var History = window.History;
    History.pushState({state:1}, "Portal", '?'+decodeURIComponent($.param(params))); 
    $.ajax({
        url: url,
        dataType: "json",
        type: "POST",
        data: params,	
        success: function(data) {
            if(data.success) {
                data = data.model;
                params['ranksToFetch'] = data.ranksToFetch.join(',');
                params['statusToFetch'] = data.statusToFetch.join(',');
                params['positionsToFetch'] = data.positionsToFetch.join(',');
                History.pushState({state:1}, "Portal", '?'+decodeURIComponent($.param(params))); 
                $(".filter input").each(function(){
                    var isChecked = false;
                    for(var i=0; i< data.statusToFetch.length; i++) {
                        if($(this).val() == data.statusToFetch[i]) {
                            isChecked = true;
                            $(this).attr('checked', 'checked');
                        } 
                    }
                    for(var i=0; i< data.positionsToFetch.length; i++) {
                        if($(this).val() == data.positionsToFetch[i]) {
                            isChecked = true
                            $(this).attr('checked', 'checked');
                        } 
                    }
                    for(var i=0; i< data.ranksToFetch.length; i++) {
                        if($(this).data('ordinal') == taxonRanks[data.ranksToFetch[i]].value) {
                            isChecked = true;
                            $(this).attr('checked', 'checked');
                        }
                    }
                    if(!isChecked) {        
                        $(this).removeAttr('checked');
                    }
                });
                $(".filter input[name='taxonRank']").each(function(){
                    var input = $(this);//$('#inlineFilterPanel input[value="' + $(this).val() + '"]');
                    if($(this).data('ordinal') <= $(ele).data('rank')) {
                        input.prop('disabled', true);
                    } else {
                        input.removeProp('disabled');
                    }
                });

                $('.listSelector').find('option:eq(1)').prop('selected', true);

                //DIRTY LIST 
                $('.dl_content ul').remove();
                $('#listCounts #instanceCount').html('<b>Total</b> : ' + data.instanceTotal);
                /*$('#acceptedCount').html('&nbsp;<b>Accepted</b> : ' + data.acceptedCount);
                $('#synonymCount').html('&nbsp;<b>Synonyms</b> : ' + data.synonymCount);
                $('#listCounts #dirtyListCount').html('<b>Raw</b> : ' + data.dirtyListCount);
                $('#listCounts #workingListCount').html('&nbsp;<b>Working</b> : ' + data.workingListCount);
                $('#listCounts #cleanListCount').html('&nbsp;<b>Clean</b> : ' + data.cleanListCount);
                */
               //*************************************************************8
                var taxonGridDataView = taxonGrid.getData();

                var taxonData = data.namesList//.accDL.concat(data.dirtyList.synDL);

                taxonGridDataView.setItems(taxonData, 'id');
                taxonGrid.invalidateAllRows();
                taxonGrid.updateRowCount();
                taxonGrid.gotoCell(0,0);
                showCheckBox = data.isAdmin;
                taxonGrid.render();

                var resultCount = data.instanceTotal;
                $('#taxonPager').html('Showing '+((data.limit <= data.acceptedCount)? data.offset+'-'+(data.offset+data.limit < data.acceptedCount?data.offset+data.limit:data.acceptedCount) +'/':'')+data.acceptedCount+' accepted names and their synonyms').append('<div class="pull-right">(<a id="fetchFirst" class="btn-link '+((data.offset - data.limit < 0)?'disabled':'')+'"><i class="icon-backward"></i></a><a id="fetchPrev" class="btn-link '+((data.offset - data.limit < 0)?'disabled':'')+'"><i class="icon-chevron-left"></i></a><a id="fetchNext" class="btn-link '+((data.limit+data.offset >= data.acceptedCount)?'disabled':'')+'"><i class="icon-chevron-right"></i></a><a id="fetchLast" class="btn-link '+((data.limit+data.offset >= data.acceptedCount)?'disabled':'')+'"><i class="icon-forward"></i></a>)');

                if(data.offset == 0) { 
                    $("#fetchPrev").addClass('disabled').children().first().removeClass().addClass('icon-chevron-left-gray');
                    $("#fetchFirst").addClass('disabled').children().first().removeClass().addClass('icon-backward-gray');
                }
                if(data.offset+data.limit > data.acceptedCount) {
                    $("#fetchNext").addClass('disabled').children().first().removeClass().addClass('icon-chevron-right-gray');
                    $("#fetchLast").addClass('disabled').children().first().removeClass().addClass('icon-forward-gray');
                }

                $('#fetchFirst').click(function() {
                    getNamesFromTaxon(ele, parentId, statusToFetch, positionsToFetch, ranksToFetch, data.limit, 0);
                });


                $('#fetchPrev').click(function() {
                    if(data.offset - data.limit < 0) {
                        $(this).addClass('disabled').children().first().removeClass().addClass('icon-chevron-left-gray');
                        return;
                    }
                    $(this).removeClass('disabled').children().first().removeClass().addClass('icon-chevron-left');
                    getNamesFromTaxon(ele, parentId, statusToFetch, positionsToFetch, ranksToFetch, data.limit, data.offset - data.limit);
                });

                $('#fetchNext').click(function() {
                    if(data.offset + data.limit > data.acceptedCount) {
                        $(this).addClass('disabled').children().first().removeClass().addClass('icon-chevron-right-gray');
                        return;
                    }
                    $(this).removeClass('disabled').children().first().removeClass().addClass('icon-chevron-right');
                    getNamesFromTaxon(ele, parentId, statusToFetch, positionsToFetch, ranksToFetch, data.limit, data.offset + data.limit);
               });

                $('#fetchLast').click(function() {
                    $(this).addClass('disabled').children().first().removeClass().addClass('icon-forward-gray');
                    getNamesFromTaxon(ele, parentId, statusToFetch, positionsToFetch, ranksToFetch, data.limit, Math.floor(data.acceptedCount/data.limit)*data.limit);
               });

                // if you don't want the items that are not visible (due to being filtered out
                // or being on a different page) to stay selected, pass 'false' to the second arg
                taxonGridDataView.syncGridSelection(taxonGrid, true);
                $("#taxonGrid").resizable();


                //*************************************************************8
/*                if(data.dirtyList.accDL){
                    accDLContent = createListHTML(data.dirtyList.accDL, "accDLContent", false); 
                    $(accDLContent).appendTo('.dl_content').show();
                }
                if(data.dirtyList.synDL){
                    //synDLContent = createListHTML(data.dirtyList.synDL, "synDLContent", false); 
                    $(synDLContent).appendTo('.dl_content').hide();
                }
                if(data.dirtyList.comDL){
                    //comDLContent = createListHTML(data.dirtyList.comDL, "comDLContent", false); 
                    $(comDLContent).appendTo('.dl_content').hide();
                }
                if(data.dirtyList.speciesDL){
                    speciesDLContent = createListHTML(data.dirtyList.speciesDL, "speciesDLContent", false); 
                    $(speciesDLContent).appendTo('.dl_content').hide();
                }

                //WORKING LIST
                $('.wl_content ul').remove();
                if(data.workingList.accWL){
                    accWLContent = createListHTML(data.workingList.accWL, 'accWLContent', false);
                    $(accWLContent).appendTo('.wl_content').show();
                }
                if(data.workingList.synWL){
                    synWLContent = createListHTML(data.workingList.synWL, 'synWLContent', false); 
                    $(synWLContent).appendTo('.wl_content').hide();
                }
                if(data.workingList.comWL){
                    //comWLContent = createListHTML(data.workingList.comWL, 'comWLContent', false); 
                    $(comWLContent).appendTo('.wl_content').hide();
                }
                if(data.workingList.speciesWL){
                    speciesWLContent = createListHTML(data.workingList.speciesWL, 'speciesWLContent', false); 
                    $(speciesWLContent).appendTo('.wl_content').hide();
                }

                //CLEAN LIST
                $('.cl_content ul').remove();
                if(data.cleanList.accCL){
                    accCLContent = createListHTML(data.cleanList.accCL, 'accCLContent', false);
                    $(accCLContent).appendTo('.cl_content').show();
                }
                if(data.cleanList.synCL){
                    synCLContent = createListHTML(data.cleanList.synCL, 'synCLContent', false);
                    $(synCLContent).appendTo('.cl_content').hide();
                }
                if(data.cleanList.comCL){
                    //comCLContent = createListHTML(data.cleanList.comCL, 'comCLContent', false);
                    $(comCLContent).appendTo('.cl_content').hide();
                }
                if(data.cleanList.speciesCL){
                    speciesCLContent = createListHTML(data.cleanList.speciesCL, 'speciesCLContent', false);
                    $(speciesCLContent).appendTo('.cl_content').hide();
                }

                if($('.nameDetails.taxon-highlight').length > 0) {
                    var selectedList = $('.nameDetails.taxon-highlight').parent();
                    selectedList.parent().prev().val(selectedList.attr('class')).trigger('change');
                    $('.nameDetails.taxon-highlight').click();
                }
*/            } else {
                alert(data.msg);
            }
            processingStop(); 
        }, error: function(xhr, status, error) {
            processingStop();
            alert(xhr.responseText);
        } 
    });

}

function getNameDetails(taxonId, classificationId, nameType, ele, isOrphanName) {

    var params = {'taxon':taxonId, 'classification':classificationId};
    params['ranksToFetch'] = getSelectedRanks().join(',');
    params['statusToFetch'] = getSelectedStatus().join(',');
    params['positionsToFetch'] = getSelectedPosition().join(',');
    var History = window.History;
    History.pushState({state:1}, "Portal", '?'+decodeURIComponent($.param(params))); 


    $("#externalDbResults").modal('hide');
    $('html, body').animate({
        scrollTop: $(".metadataDetails").offset().top
    }, 1000);
    processingStart();
    $(ele).parent("ul").find("a").css('background-color','inherit');
    $('.nameDetails').removeClass('taxon-highlight');
    $(ele).addClass('taxon-highlight');
    if(isOrphanName) {
        $('.recoId').val(taxonId);
        $('.isOrphanName').val(true);
        $('.name').val($(ele).text());
        $('.queryDatabase option[value="col"]').attr("selected", "selected");
        $('.queryString').trigger("click");
    } else {
        $('.taxonId').val(taxonId);
        $('.isOrphanName').val(false);
        var url = window.params.curation.getNameDetailsUrl;
        var choosenName = ''
            if(ele) {
            //if(nameType.toLowerCase() == 'synonym' || nameType.toLowerCase() == 'common') {
                choosenName = $(ele).text();
            }
        $.ajax({
            url: url,
            dataType: "json",
            type: "POST",
            data: {taxonId:taxonId, nameType:nameType, classificationId:classificationId, choosenName: choosenName},	
            success: function(data) {
                if(!data.success) {
                    processingStop();
                    alert(data.msg);
                    return;
                }
                updateSpecificTargetComp(data.rootHolderType, data.rootHolderId);
                //$('.feedComment').html(data.feedCommentHtml);
                changeEditingMode(false);
                populateNameDetails(data);
                populateTabDetails(data, false);
                populateConnections(data,taxonId);
                showProperTabs();
                $(".countSp").text(data["countSp"]);
                $(".countObv").text(data["countObv"]);
                $(".countCKL").text(data["countCKL"]);
                $(".taxonRegId").val(data['taxonRegId']);
                if($("#statusDropDown").val() == 'synonym' || $("#nameStatus").val()== 'common') {
                    $('.lt_family input').val('');
                    $('.lt_family input').prop("disabled", true);
                }
                processingStop();
                if(ele == undefined) {
                    return;
                }
                /*if($(ele).parents(".dl_content").length) {
                    $(".dialogMsgText").html("Auto-querying CoL for up-to-date name attributes.");
                    $("#dialogMsg").modal('show');
                    $('.queryDatabase option[value="col"]').attr("selected", "selected");
                    $('.queryString').trigger("click");
                }*/
                oldStatus = $("#statusDropDown").val();
                oldName = $("."+$("#rankDropDown").val()).val();
                oldRank = $("#rankDropDown").val();
            }, error: function(xhr, status, error) {
                processingStop()
                alert(xhr.responseText);
            } 
        });
    }
}

function populateTabDetails(data, appendData) {
    if(appendData == false) {
        //clearing synonyms
        reinitializeRows($("#names-tab1"));
    }
    var synonymsList = data['synonymsList']
    if(synonymsList && synonymsList.length > 0) {
        var e = $("#names-tab1 .singleRow").first().clone();
        $("#names-tab1 .singleRow").remove();
        $.each(synonymsList, function(index, value){
            var f = $(e).clone();
            $(f).insertBefore("#names-tab1 .add_new_row");
            var ele = $("#names-tab1 .singleRow").last();
            $(ele).find("input[name='sid']").val(value["id"]);
            $(ele).find("input[name='value']").val(value["name"]);
            $(ele).find("input[name='source']").val(value["source"]);
            $(ele).find("input[name='contributor']").val(value["contributors"]);
            if(value["source"] == 'CatalogueOfLife') {
                $(ele).find("input").prop("disabled", true);
            }
        })
    }

    if(appendData == false) {
        //clearing common names
        reinitializeRows($("#names-tab2"));
    }
    var commonNamesList = data['commonNamesList'];
    if(commonNamesList && commonNamesList.length > 0) {
        var e = $("#names-tab2 .singleRow").first().clone();
        $("#names-tab2 .singleRow").remove();
        $.each(commonNamesList, function(index, value){
            var f = $(e).clone();
            $(f).insertBefore("#names-tab2 .add_new_row");
            var ele = $("#names-tab2 .singleRow").last();
            $(ele).find("input[name='cid']").val(value["id"]);
            $(ele).find("input[name='value']").val(value["name"]);
            $(ele).find("input[name='source']").val(value["source"]);
            $(ele).find("input[name='contributor']").val(value["contributors"]);
            setOption($(ele).find(".languageDropDown")[0], value["language"], false);
        })
    }
    
    if(appendData == false) {
        //clearing accepted names
        reinitializeRows($("#names-tab0"));
    }
    var acceptedNamesList = data['acceptedNamesList']
    if(acceptedNamesList && acceptedNamesList.length > 0) {
        var e = $("#names-tab0 .singleRow").first().clone();
        $("#names-tab0 .singleRow").remove();
        $.each(acceptedNamesList, function(index, value){
            var f = $(e).clone();
            $(f).insertBefore("#names-tab0 .add_new_row");
            var ele = $("#names-tab0 .singleRow").last();
            $(ele).find("input[name='aid']").val(value["id"]);
            $(ele).find("input[name='value']").val(value["name"]);
            $(ele).find("input[name='source']").val(value["source"]);
            $(ele).find("input[name='contributor']").val(value["contributors"]);
        })
    }
}

function setOption(selectElement, value, ignoreCase) {
    if(typeof value == 'undefined') return;

    ignoreCase = typeof ignoreCase !== 'undefined' ? ignoreCase : true;

    if(ignoreCase)
        value = value.toLowerCase();
    $(selectElement).val(value);
   /*var options = selectElement.options;
    for (var i = 0, optionsLength = options.length; i < optionsLength; i++) {
        if (options[i].value == value) {
            selectElement.selectedIndex = i;
            return true;
        }
    }
    return false;*/
}

function populateNameDetails(data){
    if(data == undefined || data.length == 0) return;

    $(".canBeDisabled input[type='text']").val('');
    $('#rankDropDown option:first-child').attr("selected", "selected");
    $('#statusDropDown option:first-child').attr("selected", "selected");

    var taxonRank;
    for (var key=0; key < taxonRanks.length; key++) {
        var rank = taxonRanks[key].text.toLowerCase();
        var taxonValue = data[rank];
        if(taxonValue) {
            $(".taxon"+key).val(taxonValue);
        }
        if(data['rank'] == rank) {
            taxonRank = parseInt(key);
            break;
        }
    }

    /*for(var i=taxonRank; i<taxonRanks.length; i++) {
        $(".taxon"+i).prop("disabled", true);
    }*/

    for (var key in data) {
        if(key != "rank" && key!= "status" && $("."+key).length){
            $("."+key).val(data[key]);
        }
    }

    if(data["externalId"]) { //FROM EXTERNAL DB
        //data['source'] = $("#queryDatabase option:selected ").text();
        data['matchId'] = data['externalId'];
    }

    //if($(".source").val() == 'COL' || $(".source").val() == 'CatalogueOfLife') {
    var source = data['source']?data['source'].toLowerCase():undefined;
    console.log(data);
    if(source == 'col' || source == 'catalogueoflife' || source == 'catalogue of life' || (data['position'] == 'Clean' && data['isTaxonEditor'] == false)) {
        changeEditingMode(true);
        if(!data['position'])
            $(".position").val("working");
        if(data['position'] == 'Clean' && data['isTaxonEditor'] == false) {
            $(".canBeDisabled select.position").prop("disabled", true);
        }
    } else {
        $(".position").val(data["position"]);
    }

    $('.source').val(data['source']);
    $(".via").val(data["sourceDatabase"]);
    $(".id").val(data["matchId"]);

    setOption(document.getElementById("rankDropDown"), data["rank"]);
    setOption(document.getElementById("statusDropDown"), data["nameStatus"]);
    setOption(document.getElementById("positionDropDown"), data["position"]);

    var position = data['position']?data['position'].toLowerCase():undefined;
    $('.save_button').addClass('disabled');
    $('.remove_button').addClass('disabled');
    /*if(position == 'clean') {
        $('#removeFromClean').removeClass('disabled');
    } else if(position == 'working') {
        $('#moveToClean').removeClass('disabled');
        $('#removeFromClean').removeClass('disabled');
        $('#removeFromWKG').removeClass('disabled');
    } else {
        $('#moveToWKG').removeClass('disabled');
    } */
}

function populateConnections(data, taxonId){
    $('#speciesInstanceTotal').html(data.speciesInstanceTotal).parent().attr('href', $('#speciesInstanceTotal').parent().data('href')+'?taxon='+taxonId);
    $('#observationInstanceTotal').html(data.observationInstanceTotal).parent().attr('href', $('#observationInstanceTotal').parent().data('href')+'?taxon='+taxonId);
    $('#checklistInstanceTotal').html(data.checklistInstanceTotal).parent().attr('href', $('#checklistInstanceTotal').parent().data('href')+'?taxon='+taxonId);
    $('#documentInstanceTotal').html(data.documentInstanceTotal).parent().attr('href', $('#documentInstanceTotal').parent().data('href')+'?taxon='+taxonId);
}

//takes name for search
function searchDatabase(addNewName) {
    processingStart()
    var name = "";
    if(addNewName) {
        name = $(".newName").val();
        $("#newNamePopup").modal("hide");
        $('.queryDatabase option[value="col"]').attr("selected", "selected");
    } else {
        name = $(".name").val();
    }
    if(name == "") {
        alert("Please provide a name to search!!");
        return;
    }
    var dbName = $("#queryDatabase").val();
    if(dbName == "databaseName") {
        alert("Please select a database to query from!!");
        return;
    }
    searchAndPopupResult(name, dbName, addNewName);
}

function searchAndPopupResult(name, dbName, addNewName, source){
    var url = window.params.curation.searchExternalDbUrl;
    var ibpStatus = $("#statusDropDown").val() ?$("#statusDropDown").val():""
    var diplayDBName =  $("#queryDatabase option:selected").text() ? $("#queryDatabase option:selected").text() : "Catalogue of Life"
    $.ajax({
        url: url,
        dataType: "json",
        type: "POST",
        data: {name:name, dbName:dbName},	
        success: function(data) {
            processingStop()
            //show the popup
            if(data.length != 0) {
                $("#dialogMsg").modal('hide');
                $("#externalDbResults").modal('show');
                //TODO : for synonyms
                $("#externalDbResults h6").html(name +"(IBP status : "+ ibpStatus +")");
                fillPopupTable(data , $("#externalDbResults"), "externalData", true, source);
            	$('#externalDbResults .modal-dialog').on('hidden', function(event) {
            		$(this).unbind();
            		if(genusTaxonMsg){
            			alert(genusTaxonMsg);
            			genusTaxonMsg = undefined;
             		}
            	});
            }else {
                var oldText = $(".dialogMsgText").html();
                if (oldText.indexOf("Sorry") >= 0) {
                    oldText = "";//arr[0];
                    $(".dialogMsgText").html("Sorry no results found from "+ diplayDBName + ". Please dismiss this message and proceed with adding a species page.");
                } else {
                    $(".dialogMsgText").html(oldText + "<hr><br /> <b>RESPONSE</b> <br /> Sorry no results found from "+ diplayDBName + ". Please dismiss this message and proceed with adding a species page.");
                }
                
                //alert("Sorry no results found from "+ $("#queryDatabase option:selected").text() + ". Please query an alternative database or input name-attributes manually.");
                $("#dialogMsg").modal('show');
                if(addNewName) {
                    alert("Searching name in IBP Database");
                    searchIBP(name);
                }
                
                $('#dialogMsg').on('hidden', function(event) {
            		//$(this).unbind();
            		if(genusTaxonMsg){
            			alert(genusTaxonMsg);
            			genusTaxonMsg = undefined;
             		}
            	});
            }
          
        }, error: function(xhr, status, error) {
            processingStop()
            alert(xhr.responseText);
        } 
    });
}

function searchIBP(name) {
    processingStart();
    var url = window.params.curation.searchIBPURL;
    $.ajax({
        url: url,
        dataType: "json",
        type: "POST",
        data: {name:name},	
        success: function(data) {
            processingStop();
            //show the popup
            if(data.length != 0) {
                $("#dialogMsg").modal('hide');
                $("#externalDbResults").modal('show');
                //TODO : for synonyms
                $("#externalDbResults h6").html(name +"(IBP status : "+$("#statusDropDown").val()+")");
                fillPopupTable(data , $("#externalDbResults"), "IBPData", true);
            } else {
                alert("Sorry no results found from IBP Database. Fill in details manually");
            }
        }, error: function(xhr, status, error) {
            processingStop();
            alert(xhr.responseText);
        } 
    });

}

function fillPopupTable(data, $ele, dataFrom, showNameDetails, source) {
    if(data.length == 0) {
        alert("Sorry No results found!!");
    }
    var classificationId = $('#taxaHierarchy option:selected').val();
    //clear table
    $ele.find("table tr td").remove();
    var rows = "";
    $.each(data, function(index, value) {
    	if(dataFrom == "externalData") {
    		var onclickEvent = (source ==  "onlinSpeciesCreation") ? 'openSpeciesPage(' + value["id"] + ',"' + value["externalId"] + '", "' + value["name"] + '")' : 'getExternalDbDetails(this, ' +showNameDetails+')'
            var nameStatus = value['nameStatus'];
            var colLink = 'http://www.catalogueoflife.org/annual-checklist/2015/details/species/id/'+value['externalId']
            if(nameStatus == 'synonym') {
                colLink = 'http://www.catalogueoflife.org/annual-checklist/2015/details/species/id/'+value['acceptedNamesList'][0]['id']
                if(value['rank'] == 'species' || value['rank'] == 'infraspecies') {
                    nameStatus = nameStatus + " for <a style = 'color:blue;' target='_blank' href='"+colLink+"'>" + value['acceptedNamesList'][0]['name']+"</a>";
                }else {
                    nameStatus = nameStatus + " for " + value['acceptedNamesList'][0]['name'];
                }
                $.each(value['acceptedNamesList'], function(index,value) {
                    if(index > 0) {
                        nameStatus = nameStatus + " and " + value['name'];
                    }
                });
            }
            if(value['rank'] == 'species' || value['rank'] == 'infraspecies') {
                rows+= "<tr><input type = 'hidden' value = '"+value['externalId']+"'><td><a style = 'color:blue;' target='_blank' href='"+colLink+"'>"+value['name'] +"</a></td>"
            }else {
                colLink = 'http://catalogueoflife.org/annual-checklist/2015/browse/tree/id/'+value['externalId']
                rows+= "<tr><input type = 'hidden' value = '"+value['externalId']+"'><td><a style = 'color:blue;' target='_blank' href='"+colLink+"'>"+value['name'] +"</a></td>"
            }
            
            rows += "<td>"+value['rank']+"</td><td>"+nameStatus+"</td><td>"+value['group']+"</td><td>"+value['parentTaxon']+"</td><td>"+value['sourceDatabase']+"</td><td><div class='btn' onclick='"+ onclickEvent + "'>Select this</div></td></tr>"        
        }
        else {
        	var onclickEvent = (source ==  "onlinSpeciesCreation") ? 'openSpeciesPage(' + value['id'] + ',"' + value['externalId'] + '", "' + value['name'] + '")' : 'getNameDetails(' +value['taxonId'] + ',' + classificationId + ',1, undefined)' 
            rows += "<tr><td>"+value['name'] +"</td><td>"+value['rank']+"</td><td>"+value['nameStatus'] + "/" + value['position'] +"</td><td>"+value['group']+"</td><td>" + value['parentName'] + "</td><td>"+value['sourceDatabase']+"</td><td><div class='btn' onclick='"+ onclickEvent + "'>Select this</div></td></tr>"
        }
    });
    $ele.find("table").append(rows);
    return
}

function openSpeciesPage(taxonId, colId, name){
	var sourceComp = $(".input-prepend.currentTargetName");
	if(sourceComp.length > 0){
		var inputComp = sourceComp.children("input");
		taxonId = ((taxonId == undefined ) || (taxonId == 'undefined') || (taxonId == ''))?'': taxonId
		colId = ((colId == undefined ) || (colId == 'undefined') || (colId == ''))?'': colId 
		inputComp.attr('data-ibpid', taxonId);
		inputComp.attr('data-colid', colId);
		inputComp.val(name);
		$("#externalDbResults").modal('hide');
		return;
	}
	var url = window.params.curation.editSpeciesPageURL;
	$("#externalDbResults").modal('hide');
	$.ajax({
	        url: url,
	        dataType: "json",
	        type: "POST",
	        data: {taxonId:taxonId, colId:colId},	
	        success: function(data) {
	            processingStop();
	            validateSpeciesSuccessHandler(data, false);
	        }, error: function(xhr, status, error) {
	            processingStop();
	            alert(xhr.responseText);
	        } 
	    });
	
	return false;
}


function addSpeciesPage(url){
    var allValidated = true;
	$("#taxonHierachyInput .input-prepend").each(function(index, ele) {
		allValidated = (allValidated && ($(ele).children('div').hasClass('disabled')));
	});
	
	if(!allValidated){
		alert("Some names are not validated in the Taxon Hierarchy. Please validated them before submit.")
		return; 
	}
	
	var params = {};
    $("#addSpeciesPage input").each(function(index, ele) {
        if($(ele).val().trim())
        	params[$(ele).attr('name')] = $(ele).val().trim();
    });
    params['rank'] = $('#rank').find(":selected").val();
    
    //adding ibpid and colid if any
    $("#taxonHierachyInput .input-prepend input").each(function(index, ele) {
    	var ibpId = $(ele).attr('data-ibpid');
    	var colId = $(ele).attr('data-colid');
    	var rank = $(ele).attr('data-rank');
		ibpId = ((ibpId == undefined ) || (ibpId == 'undefined') || (ibpId == ''))?'': ibpId
		colId = ((colId == undefined ) || (colId == 'undefined') || (colId == ''))?'': colId 

    	if(ibpId || colId){
    		params['taxonHirMatch.' + rank + '.ibpId'] = ibpId;
    		params['taxonHirMatch.' + rank + '.colId'] = colId;
    	}
    });
    
    $.ajax({
        url:url,
        data:params,
        method:'POST',
        dataType:'json',
        success:function(data) {
        	if (data.instance.id) {
    			window.location.href = '/species/show/' + data.instance.id + '?editMode=true'
    			return;
    		}
    		$('#errorMsg').removeClass('alert-error hide').addClass('alert-info').html(data.msg);
        }, error: function(xhr, status, error) {
            handleError(xhr, status, error, this.success, function() {
                var msg = $.parseJSON(xhr.responseText);
                $(".alertMsg").html(msg.msg).removeClass('alert-success').addClass('alert-error');
            });
        }
    });
	
	
}

function showSearchPopup(data){
	// show the popup
	var colMsg = "";
	var tList = data.taxonList
	if (tList && tList.length != 0) {
		$("#dialogMsg").modal('hide');
		$("#externalDbResults").modal('show');
		$("#externalDbResults h6").html(name);
		fillPopupTable(tList, $("#externalDbResults"), "IBPData", true, "onlinSpeciesCreation");
		$('#externalDbResults .modal-dialog').on('hidden', function(event) {
			$(this).unbind();
			$("#dialogMsg").modal('show');
			colMsg = colMsg + "Querying the Catalogue of Life for matches..."
			$(".dialogMsgText").html(colMsg);
			searchAndPopupResult(data.requestParams.page, "col", false, "onlinSpeciesCreation");
		});
	} else {
		$("#dialogMsg").modal('show');
		colMsg = "No results found for taxon name on IBP. Querying the Catalogue of Life for matches..."
		$(".dialogMsgText").html(colMsg);
		searchAndPopupResult(data.requestParams.page, "col", false, "onlinSpeciesCreation");	
	}
	

}

function validateHirName(comp){
	var vButton = $(comp).children('div');
	if(vButton.hasClass('disabled'))
		return;
	
	$("#taxonHierachyInput .input-prepend").each(function(index, ele) {
        $(ele).removeClass("currentTargetName");
    });
	
	comp.addClass("currentTargetName");
	var inputCom = $(comp).children('input');
	inputCom.attr('data-ibpid', "");
	inputCom.attr('data-colid', "");
	var rank = inputCom.attr('data-rank');
	var page = inputCom.attr('value').trim();
	//if not a mandatory field and name is empty text then leaving  
	if( (rank == "4"  || rank == "6" ||rank == "8") && (page == "") ){
		vButton.removeClass('btn-primary').addClass('btn-success disabled');
		vButton.html('Validated');
		return;
	}
	
	var params = {'rank':rank, 'page':page};
	
	$.ajax({
        url:'/species/validate',
        data:params,
        method:'POST',
        dataType:'json',
        success:function(data) {
        	if (data.success == true) {
        		$('#errorMsg').removeClass('alert-error hide').addClass('alert-info').html(data.msg);
        		showSearchPopup(data);
        		vButton.removeClass('btn-primary').addClass('btn-success disabled');
        		vButton.html('Validated');
        	}else{
        		$("#errorMsg").html(data.msg).removeClass('alert-success').addClass('alert-error');
        	}
        }, error: function(xhr, status, error) {
            handleError(xhr, status, error, this.success, function() {
                var msg = $.parseJSON(xhr.responseText);
                $(".alertMsg").html(msg.msg).removeClass('alert-success').addClass('alert-error');
            });
        }
    });
}

function enableValidButton(comp){
	var vButton = $(comp).children('div');
	if($(comp).children('input').val()){
		vButton.addClass('btn-primary').removeClass('btn-success disabled');
		vButton.html('Validate Name');
	}
}

function updateHirInput(data){
	var $ul = $('<ul></ul>');
	$('#existingHierarchies').empty().append($ul);
	if (data.taxonRegistry) {
		$.each(data.taxonRegistry, function(index, value) {
			var $c = $('<li></li>');
			$ul.append($c);
			var $u = $('<ul><b>' + index + '</b></ul>');
			$c.append($u);
			$.each(value[0], function(i, v) {
				$u.append('<li>' + v.rank + ' : ' + v.name + '</li>');
			});
		});
	}

	$('#existingHierarchies').append('<div>If you have a new or a different classification please provide it below.</div>');
	var $hier = $('#taxonHierachyInput');
	$hier.empty()
	var taxonRegistry = data.requestParams ? data.requestParams.taxonRegistry: undefined;
	var taxonIBPHirMatch = data.requestParams ? data.requestParams.taxonIBPHirMatch: undefined;
	var taxonCOLHirMatch = data.requestParams ? data.requestParams.taxonCOLHirMatch: undefined;
	
	for (var i = 0; i < nameRank; i++) {
		var isTaxon = (taxonRegistry && taxonRegistry[i]);
		var taxonValue = isTaxon ? taxonRegistry[i] : taxonRanks[i].taxonValue;
		var ibpMatch = (taxonIBPHirMatch && taxonIBPHirMatch[i])?taxonIBPHirMatch[i]:undefined;
		var colMatch = (taxonCOLHirMatch && taxonCOLHirMatch[i])?taxonCOLHirMatch[i]:undefined;
		
		var bClass = 'btn-success disabled';
		var bText = 'Validated';
		if(taxonRanks[i].mandatory &&  !isTaxon ){
			bClass = 'btn-primary';
			bText = 'Validate Name';
		}
		$(
				'<div class="input-prepend"><span class="add-on">'
						+ taxonRanks[i].text
						+ (taxonRanks[i].mandatory ? '*' : '')
						+ '</span><input data-provide="typeahead" data-rank ="'
						+ taxonRanks[i].value
						+ '" data-ibpid="' + ibpMatch + '" data-colid="' + colMatch
						+ '" type="text" class="taxonRank" name="taxonRegistry.'
						+ taxonRanks[i].value + '" value="' + taxonValue
						+ '" placeholder="Add ' + taxonRanks[i].text
						+ '" onchange="enableValidButton($(this).parent());"' 
						+ '" /><div class="btn btn-mini ' + bClass + '" onclick=validateHirName($(this).parent());> ' + bText + ' </div></div>').appendTo($hier);
	}
	if (nameRank > 0){
		$('#taxonHierarchyInputForm').show();
		$('html, body').animate({scrollTop:400}, 1000);
	}
	
	if ($(".taxonRank:not(#page)").length > 0)
		$(".taxonRank:not(#page)").autofillNames();
	
}

function updateGenusAutoPopulate(index){
	var item = genusMatchResult[index];
	var data = {'requestParams':item};
	updateHirInput(data);
}

function updateGenusSelector(data){
	var gComp = $('.genusSelector');
	var $gtl = $('.genusSelector .genusItemList');
	$gtl.empty();
	
	genusMatchResult = data.requestParams.genusMatchResult
	if(!genusMatchResult || genusMatchResult.length <=1){
		gComp.hide();
		return
	}
	
	$.each(genusMatchResult, function(index, item) {
		$(
				'<a href="#" class="genusItem" onclick=updateGenusAutoPopulate(' + index + ')><span class="add-on">'
				+ item.namePath 
				+ '</span></a><br>').appendTo($gtl);
	});
	
	gComp.show();
}


function validateSpeciesSuccessHandler(data, search){
	if (data.success == true) {
		//if species page id returned then open in edit mode
		if (data.id) {
			window.location.href = '/species/show/' + data.id + '?editMode=true'
			return;
		}
		
		$('#errorMsg').removeClass('alert-error hide').addClass('alert-info').html(data.msg);
		
		//showing parser info
		$('#parserInfo').children('.canonicalName').html(data.canonicalForm);
		$('#parserInfo').children('.authorYear').html(data.authorYear);
		$('#parserInfo').show();
		
		if(!data.authorYear){
			alert("Author and Year information is essential to distinguish taxon name from synonyms. Please input these details in the recommended nomenclatural format for the phylum and re-validate; eg: Cuon alpinus (Pallas,1811).");
		}
		
		taxonRanks = data.taxonRanks;
		nameRank = data.rank;
		
		updateGenusSelector(data);
		
		updateHirInput(data);
		
		genusTaxonMsg = data.requestParams.genusTaxonMsg
		
		if(search){
			showSearchPopup(data);
		}
		else{
			//updating new rank
			 var text1 = data.rank;
		     $('#rank option').filter(function() {
		            return $(this).val() == text1; 
		     }).prop('selected', true);

		    //updating name and colId
 		    $("#page").val(data.requestParams.speciesName);
 		    $( "input[name='colId']" ).val(data.requestParams.colId); 		    
 		   
 		}
		
		
		
		$('#addSpeciesPageSubmit').show();

	} else {
		if (data.status == 'requirePermission')
			window.location.href = '/species/contribute'
		else
			$('#errorMsg').removeClass('alert-info hide').addClass(
					'alert-error').text(data.msg);
	}
}



// takes COL id
function getExternalDbDetails(ele, showNameDetails) {
    var externalId = $(ele).parents('tr').find('input').val();
    var url = window.params.curation.getExternalDbDetailsUrl;
    var dbName = $("#queryDatabase").val();
    if(externalId == undefined && !showNameDetails) {
        alert("Could not find details as no ID present");
        return;
    }
    // IN case of TNRS no id comes
    //so search by name
    if(externalId == undefined || externalId == "") {
        externalId = $(".name").val();
    }
    $.ajax({
        url: url,
        dataType: "json",
        type: "POST",
        data: {externalId:externalId, dbName:dbName},	
        success: function(data) {
            //show the popup
            $("#externalDbResults").modal('hide');
            data['source'] = dbName;
            if(!showNameDetails) {
                //create accepted match and saveAcceptedName
                $(".fromCOL").val(true);
                saveAcceptedName(createNewAcceptedNameData(data));
                processingStop();
                return;
            } else {
                if(jQuery.isEmptyObject(data)) {
                    alert("Sorry no details found");
                    return;
                }
                populateNameDetails(data);
                populateTabDetails(data, true);
                showProperTabs();
                if(dbName == 'col') {
                    changeEditingMode(true);
                    //$(".id_details").val(JSON.stringify(data['id_details']));
                }else {
                    changeEditingMode(false);
                }
                oldStatus = $("#statusDropDown").val();
                oldName = $("."+$("#rankDropDown").val()).val();
                oldRank = $("#rankDropDown").val();
                if($("#statusDropDown").val() == 'synonym' || $("#nameStatus").val()== 'common') {
                    $('.lt_family input').val('');
                    $('.lt_family input').prop("disabled", true);
                }
            }
        }, error: function(xhr, status, error) {
            alert(xhr.responseText);
        } 
    });
}

function saveNameDetails(moveToRaw, moveToWKG, moveToClean) {
    if($(event.target).hasClass('disabled')) {
        return false;
    }
    if(!$('.taxonId').val()) {
        alert("Please select a name")
            return false;
    }
    
    processingStart();
    /*if($("#statusDropDown").val() == 'accepted') {
        var taxonRegistryData = fetchTaxonRegistryData();
        taxonRegistryData['abortOnNewName'] = true;
        taxonRegistryData['fromCOL'] = $('.fromCOL').val();
        if($('.fromCOL').val() == "true") {
            taxonRegistryData['abortOnNewName'] = false;
            taxonRegistryData['id_details'] = JSON.parse($(".id_details").val());
        }
        var url =  window.params.taxon.classification.updateUrl;
        if(moveToWKG == true) {
            taxonRegistryData['moveToWKG'] = true;
        }
        //check for spell check
        if(oldName == $("."+$("#rankDropDown").val()).val()) {
            taxonRegistryData['spellCheck'] = false;
        }else if(oldName != $("."+$("#rankDropDown").val()).val() && oldRank == $("#rankDropDown").val()){
            taxonRegistryData['spellCheck'] = true;
            taxonRegistryData['oldTaxonId'] = $('.taxonId').val();
        }
        console.log("check this data ");
        console.log(JSON.stringify(taxonRegistryData));
        console.log(JSON.stringify(dataToProcess()));
        return
        $.ajax({
            url: url,
            type: "POST",
            dataType: "json",
            data: {taxonData: JSON.stringify(taxonRegistryData)},	
            success: function(data) {
                if(data['success']) {
                    if(data["newlyCreated"]) {
                        alert(data["newlyCreatedName"] +" is a new uncurated name on the portal. Hierarchy saved is -- " + data['activityType'] +" .Please explicitly curate "+ data["newlyCreatedName"] +" from dirty list to continue.");
                    } else {
                        var resMsg = "Successfully " + data['activityType'];
                        if(data['spellCheckMsg']) {
                            resMsg = resMsg + " . " + data['spellCheckMsg'];
                        }
                        alert(resMsg);
                    }
                    if(moveToWKG == true) {
                        $(".clickedEle .taxDefIdSelect").trigger("click");
                    }
                    processingStop();
                    postProcessOnAcceptedName();
                } else {
                    alert(data['msg']);
                }
            }, error: function(xhr, status, error) {
                processingStop();
                alert(xhr.responseText);
            } 
        });
    }else if($("#statusDropDown").val() == 'synonym') {
        preProcessOnSynonym(); 
    }
    */

    /*if(oldStatus == 'accepted') {
        postProcessOnAcceptedName();
    }*/
    var url = window.params.curation.curateNameURL;
    var dataToProcessData = dataToProcess(moveToRaw, moveToWKG, moveToClean);
    var acceptedNamePresent = false;
    if(dataToProcessData['nameStatus'] == 'synonym' || dataToProcessData['name'] == 'common') {
        //there shd be atleast one accepted name
        var accNameRows = $("#names-tab0 input[name='aid']");
        $.each(accNameRows, function(index, value){
            if($(value).val() == '') {
                acceptedNamePresent = false;
                //TODO chk if there is atleast on valid accepted name
                //break;
            }
        }) 
        if(!acceptedNamePresent) {
            alert("Please validate all accepted names. If any name is invalid please remove it from the collection.")
            return false;
        }
    }
    var acceptedMatch = JSON.stringify(dataToProcessData);
    $.ajax({
        url: url,
        type: "POST",
        dataType: "json",
        data: {acceptedMatch: acceptedMatch},
        success: function(data) {
            if(data['success']) {
                if(data["newlyCreated"]) {
                    alert(data["newlyCreatedName"] +" is a new uncurated name on the portal. Hierarchy saved is -- " + data['activityType'] +" .Please explicitly curate "+ data["newlyCreatedName"] +" from dirty list to continue.");
                } else {
                    var resMsg = "Successfull operation. ";
                    if(data['activityType'])
                        resMsg += data['activityType'];
                    if(data['spellCheckMsg']) {
                        resMsg = resMsg + " . " + data['spellCheckMsg'];
                    }
                    alert(resMsg);
                }

                //if(oldStatus == 'accepted' && acceptedMatch.nameStatus != oldStatus) {
                //    postProcessOnAcceptedName();
                //}

                //if(moveToRaw == true || moveToWKG == true || moveToClean == true) {
                    var $selectedTaxon = $('#taxaHierarchy .taxon-highlight'); 
                    getNamesFromTaxon($selectedTaxon, $selectedTaxon.attr('id').replace('_anchor',''), getSelectedStatus(), getSelectedPosition(), getSelectedRanks());
                    //$(".clickedEle").trigger("click");
                //}
                updateFeeds();    
                processingStop();
                //postProcessOnAcceptedName();
            } else {
                $(".dialogMsgText").html(data['msg']);
                $("#dialogMsg").modal('show');
                processingStop();
            }
        }, error:function (xhr, ajaxOptions, thrownError){
                //successHandler is used when ajax login succedes
                var successHandler = this.success;
                handleError(xhr, ajaxOptions, thrownError, successHandler, function() {
                    processingStop();
                    alert(xhr.responseText);

                });
            }
        });
}

function fetchTaxonRegistryData() {
    var result = {}
    for (var key=0; key < taxonRanks.length; key++) {
        result['taxonRegistry.'+key] = $(".taxon"+key).val();
        res[key]  = $(".taxon"+key).val();
    }
   
    result['reg'] = $(".taxonRegId").val()          //$('#taxaHierarchy option:selected').val();
    result['classification'] = 817; //for author contributed
    
    result['taxonRegistry'] = res;

    var metadata1 = {};
    metadata1['name'] = $('.name').val();
    metadata1['rank'] = $('#rankDropDown').val();
    metadata1['authorString'] = $('.authorString').val();
    metadata1['nameStatus'] = $('#statusDropDown').val();
    metadata1['source'] = $('.source').val();
    metadata1['sourceDatabase'] = $('.via').val();
    metadata1['via'] = $('.via').val();
    metadata1['id'] = $('.id').val();
    result['metadata'] = metadata1;
    
    return result;
}

function changeEditingMode(mode) {
    if(mode == false) {
        $(".fromCOL").val(mode);
    } else {
        $(".fromCOL").val(mode);
    }
    $(".canBeDisabled input").prop("disabled", mode); 
    $(".canBeDisabled select").prop("disabled", mode); 
    $(".canBeDisabled button").prop("disabled", mode); 
    $(".canBeDisabled input.canonicalForm, .canBeDisabled input.authorString ").prop("disabled", true); 
    
    $('#saveNameDetails').prop('disabled', false);
    $(".canBeDisabled select.position").prop("disabled", false);
}

/*function modifySourceOnEdit() {
    $(".canBeDisabled").click(function() {
        if(!($(".canBeDisabled input").prop("disabled"))) {
            $(".source").val('user entered');
            $(".via").val('');
        }
    });
}*/


//====================== SYNONYM/COMMON NAME RELATED ===============================
function modifyContent(ele, type) {
    //return;
    processingStart();
    var typeName = '';
    var relationship = '';
    var p = {};
    if(type == 'a' || type == 'aid') {
        typeName = 'accepted';
        relationship = 'accepted';
        p['synComName'] = $(".name").val();
        p['synComSource'] = $(".source").val();
        p['modifyingFor'] = $("#statusDropDown").val();
    } else if( type == 's'|| type == 'sid') {
        typeName = 'synonym';
        relationship = 'synonym';
    } else if( type == 'c' || type == 'cid') {
        typeName = 'commonname';
        relationship = 'commonname';
    } else {
        typeName = 'reference';
        relationship = 'reference';
    }
    var that = $(ele);
    var url = window.params.species.updateUrl;
    var  modifyType = that.attr('rel');
    var form_var = that.closest('.tab_form');   

    if(modifyType == "edit"){
        form_var.find('input').attr("disabled", false);
        that.html("<i class='icon-ok icon-white'></i>").attr('rel','update');
        processingStop();
        return false;
    }   

    if(modifyType == "delete"){
        var modify = that.prev().attr('rel');
        if(modify == "update"){
            form_var.find('input').attr("disabled", true);
            that.prev().html("<i class='icon-edit icon-white'></i>").attr('rel','edit');
            that.html("<i class='icon-trash'></i>");
            processingStop();
            return false;
        }else{
            if(!confirm("Are you sure to delete?")) {
                processingStop();
                return false;
            } else {
                form_var.find('input').attr("disabled", false);
            }
        }    
    }
    var inputs = form_var.find("input");
    $.each(inputs, function(index, value){
        p[$(value).attr('name')] = $(value).val();
    });
    p['name']  = typeName;
    p['act'] = modifyType;    
    p['relationship'] = relationship;
    p['language'] = that.parents(".tab_div").find('.languageDropDown').val();
    var otherParams = {};
    otherParams['atAnyLevel'] = true;
    otherParams['taxonId'] = $(".taxonId").val();  //272991;
    p['otherParams'] = otherParams    
    form_var.find('input').attr("disabled", true);
    if(modifyType != 'delete') {
        that.html("<i class='icon-edit icon-white'></i>").attr('rel','edit');
    }
    $.ajax({
        url: url,
        type: "POST",
        dataType: "json",
        data: {dataFromCuration: JSON.stringify(p)},	
        success: function(data) {
            if(data['success']) {
                form_var.find("."+type).val(data['dataId']);
                that.next().html("<i class='icon-trash'></i>");
                if(modifyType == 'delete') {
                    form_var.parent().hide();
                }
                if(type == 'a' || type == 'aid') {
                    $(".taxonId").val(data['newSynComId']);
                    $(".clickedEle .taxDefIdSelect").trigger("click");
                }
            } else {
                alert("Error in saving - "+data['msg']);
            }
            processingStop();
            //return false;
        }, error: function(xhr, status, error) {
            processingStop();
            alert(xhr.responseText);
        } 
    });
 
}

$('.add_new_row').click(function(){
    var me = this;
    var typeClass = $(me).prev().find("input[type='hidden']").attr('name')
    var p = new Object();
    p['typeClass']= typeClass;
    var html = $('#newRowTmpl').render(p);
    $(me).before(html);
});

function showNewNamePopup() {
    $("#newNamePopup").modal("show");
}

function reinitializeRows($context) {
    var numRows = $context.find(".tab_div").length;
    console.log(numRows);
    for(var i = 0; i< 4; i++) {
        $context.find(".add_new_row").trigger("click");
    }
//    $context.find(".tab_div:lt("+(numRows-1)+")").remove();
}

function showProperTabs() {
    var nameStatus = $("#statusDropDown").val();
    if(nameStatus == 'accepted') {
        $('#names-li1 a').removeClass('not_in_use').addClass('btn');
        $('#names-li2 a').removeClass('not_in_use').addClass('btn');
        $('#names-li1 a').attr('data-toggle', 'tab');
        $('#names-li2 a').attr('data-toggle', 'tab');
        $('#names-li0 a').removeAttr('data-toggle');
        $('#names-li0 a').removeClass('btn').addClass('not_in_use');
        $('#names-li1 a').tab('show');
    }else if(nameStatus == 'synonym' || nameStatus == 'common') {
        $('#names-li1 a').removeClass('btn').addClass('not_in_use');
        $('#names-li2 a').removeClass('btn').addClass('not_in_use');
        $('#names-li1 a').removeAttr('data-toggle');
        $('#names-li2 a').removeAttr('data-toggle');
        $('#names-li0 a').attr('data-toggle', 'tab');
        $('#names-li0 a').removeClass('not_in_use').addClass('btn');
        $('#names-li0 a').tab('show');
    }else {
        $('#names-li0 a').removeClass('not_in_use').addClass('btn');
        $('#names-li1 a').removeClass('not_in_use').addClass('btn');
        $('#names-li2 a').removeClass('not_in_use').addClass('btn');
        $('#names-li0 a').attr('data-toggle', 'tab');
        $('#names-li1 a').attr('data-toggle', 'tab');
        $('#names-li2 a').attr('data-toggle', 'tab');
        $('#names-li0 a').tab('show');
    }
}

function postProcessOnAcceptedName() {
    //fetch all synonyms and common names and refrences to be saved
    var synNameRows = $("#names-tab1 input[name='value']");
    $.each(synNameRows, function(index, value){
        if($(value).val() != ''){
            $(value).parents(".tab_form").find(".addEdit").trigger("click");
        }
    });
    var comNameRows = $("#names-tab2 input[name='value']");
    $.each(comNameRows, function(index, value){
        if($(value).val() != ''){
            $(value).parents(".tab_form").find(".addEdit").trigger("click");
        }
    });
}

function preProcessOnSynonym() {
    var accNameRows = $("#names-tab0 input[name='value']");
    $.each(accNameRows, function(index, value){
        if($(value).val() != ''){
            $(value).parents(".tab_form").find(".addEdit").trigger("click");
        }
    });
}

function dataToProcess(moveToRaw, moveToWKG, moveToClean) {
    var result = {};

    result['name'] = $('.name').val();
    result['rank'] = $('#rankDropDown').val();
    var flag = false;
    for (var key=0; key < taxonRanks.length; key++) {
        var taxonVal = $(".taxon"+key).val();

        if(!flag && taxonRanks[key].text.toLowerCase() == result['rank']) {
            flag = true;
            taxonVal = result['name'];
            $(".taxon"+key).val(result['name']);
        } else if(flag) {
            taxonVal = '';
        }
        result[taxonRanks[key].text.toLowerCase()] = taxonVal;
    }
  
    result['group'] = $('.kingdom').val();
    //result['authorString'] = $('.authorString').val();
    result['nameStatus'] = $('#statusDropDown').val();
    result['source'] = $('.source').val();
    result['sourceDatabase'] = $('.sourceDatabase').val();
    result['via'] = $('.via').val();
    result['id'] = $('.id').val(); 
    result['externalId'] = $('.id').val();
    result['abortOnNewName'] = true;
    result['fromCOL'] = $('.fromCOL').val();
    result['isOrphanName'] = $('.isOrphanName').val();
    if($('.fromCOL').val() == "true") {
        result['abortOnNewName'] = false;
        //result['id_details'] = JSON.parse($(".id_details").val());
    }
    result['taxonId'] = $('.taxonId').val();
    result['recoId'] = $('.recoId').val();
    result['position'] = $('#positionDropDown').val();
    /*result['moveToRaw'] = moveToRaw;
    result['moveToWKG'] = moveToWKG;
    result['moveToClean'] = moveToClean;
    */
    //check for spell check
    if(oldName == $("."+$("#rankDropDown").val()).val()) {
        result['spellCheck'] = false;
    }else if(oldName != $("."+$("#rankDropDown").val()).val() && oldRank == $("#rankDropDown").val()){
        result['spellCheck'] = true;
        result['oldTaxonId'] = $('.taxonId').val();
    }

    return result;
}


///////////////////OBV RECO NAMES///////////////////////

function getOrphanRecoNames(){
    processingStart();
    var url = window.params.curation.getOrphanRecoNamesURL;
    $.ajax({
        url: url,
        dataType: "json",
        type: "POST",
        data: {},	
        success: function(data) {
            //DIRTY LIST 
            if(data){
                orphanDLContent = createListHTML(data, 1, true); 
                $(".dl_content ul").remove();
                $(".wl_content ul").remove();
                $(".cl_content ul").remove();
                $(".dl_content").append(orphanDLContent);
            }
            processingStop(); 
        }, error: function(xhr, status, error) {
            processingStop();
            alert(xhr.responseText);
        } 
    });
}

function validateName(ele, showNameDetails) {
    processingStart();
    $(ele).parents(".singleRow").find("input[name='aid']").addClass('validating');
    $('.queryDatabase option[value="col"]').attr("selected", "selected");
    var name = "";
    name = $(ele).parents(".singleRow").find("input[name='value']").val();
    if(name == "") {
        alert("Please provide a name to validate!!");
        processingStop();
        return;
    }
    var dbName = $("#queryDatabase").val();
    var url = window.params.curation.searchExternalDbUrl;
    $.ajax({
        url: url,
        dataType: "json",
        type: "POST",
        data: {name:name, dbName:dbName},	
        success: function(data) {
            processingStop();
            //show the popup
            if(data.length != 0) {
                $("#dialogMsg").modal('hide');
                $("#externalDbResults").modal('show');
                //TODO : for synonyms
                $("#externalDbResults h6").html(name +"(IBP status : "+$("#statusDropDown").val()+")");
                fillPopupTable(data , $("#externalDbResults"), "externalData", showNameDetails);
            }else {
                var oldText = $(".dialogMsgText").html();
                $(".dialogMsgText").html(oldText + "<br /> <b>RESPONSE</b> <br />  Sorry no results found from COL. Please query an alternative database or input name-attributes manually.");
                $("#dialogMsg").modal('show');
                //alert("Sorry no results found from "+ $("#queryDatabase option:selected").text() + ". Please query an alternative database or input name-attributes manually.");
            }
        }, error: function(xhr, status, error) {
            processingStop();
            alert(xhr.responseText);
        } 
    }); 
}

function createNewAcceptedNameData(data) {
    var result = {}
    for (var key=0; key < taxonRanks.length; key++) {
        var rank = taxonRanks[key].text.toLowerCase();
        result[rank] = data[rank];
    }

    result['name'] = data['name'];
    result['group'] = data['kingdom'];
    result['rank'] = data['rank'];
    result['authorString'] = data['authorString'];
    result['nameStatus'] = data['nameStatus'];
    result['source'] = data['sourceDatabase'];
    result['sourceDatabase'] = data['sourceDatabase'];
    result['via'] = data['sourceDatabase'];
    result['id'] = data['externalId']; 
    result['externalId'] = data['externalId'];
    result['abortOnNewName'] = true;
    result['fromCOL'] = $('.fromCOL').val();
    result['isOrphanName'] = $('.isOrphanName').val();
    if($('.fromCOL').val() == "true") {
        result['abortOnNewName'] = false;
        //result['id_details'] = data['id_details'];
    }
    /*
    result['taxonId'] = $('.taxonId').val();
    result['recoId'] = $('.recoId').val();
    result['moveToWKG'] = moveToWKG
    //check for spell check
    if(oldName == $("."+$("#rankDropDown").val()).val()) {
        result['spellCheck'] = false;
    }else if(oldName != $("."+$("#rankDropDown").val()).val() && oldRank == $("#rankDropDown").val()){
        result['spellCheck'] = true;
        result['oldTaxonId'] = $('.taxonId').val();
    }
    */
    return result;
}

function saveAcceptedName(acceptedMatch) {
    //call namelist controller saveAcceptedName
    processingStart();
    var url = window.params.curation.saveAcceptedNameURL;
    $.ajax({
        url: url,
        dataType: "json",
        type: "POST",
        data: {acceptedMatch: JSON.stringify(acceptedMatch)},	
        success: function(data) {
            if(data.acceptedNameId != "") {
                alert("Successfully validated name");
            } else {
                alert("Failed validating name");
            }
            $(".validating").val(data.acceptedNameId).parent().parent().find('.addEdit').html('Validated');
            $(".validating").removeClass('validating');
            processingStop();
        }, error: function(xhr, status, error) {
            processingStop();
            alert(xhr.responseText);
        } 
    });
}


$(document).ready(function() {
    $('#statusDropDown').change(showProperTabs);
    $(".loadConnection").click(function() { 
        var $me = $(this);
        var controller = $me.data('controller');
        var url = '';
        switch(controller) {
            case 'species' : url = window.params.species.listUrl; break;
            case 'observation' : url = window.params.observation.listUrl; break;
            case 'document' : url = window.params.document.listUrl; break;
            default : console.log("no url");
        }
        var taxon = $("input#filterByTaxon").val();
        var params = {};
        params['taxon'] = taxon;
        params['max'] = $(this).data('max');
        params['offset'] = $(this).data('offset');
        params['format'] = 'json';
        var $connTable = $('#'+controller+'_connTable');
        $.ajax({
            url:url,
            dataType: "json",
            data:params,
            success: function(data) {
                var instanceList, instanceCount;
                switch(controller) {
                    case 'observation' : 
                        instanceList = data.model.observationInstanceList;
                        instanceCount = data.model.instanceTotal;
                        break;
                    case 'species' : 
                        instanceList = data.model.speciesInstanceList;
                        instanceCount = data.model.instanceTotal;
                        break;
                    case 'document' : 
                        instanceList = data.model.documentInstanceList;
                        instanceCount = data.model.instanceTotal;
                        break;
                    default : console.log("no url");
                }

                $('#'+controller+'_connList .'+controller+'InstanceTotal').html(' <a href="'+url+'">'+instanceCount+'</a>');
                /*if(data.success == true) {
                    $.each(instanceList, function(index, item) {
                        $connTable.append('<tr><td><a href="/'+controller+'/show/'+item.id+'">'+item.title+'</a></td></tr>');
                    });
                    $me.data('offset', data.model.next);
                    if(!data.model.next){
                        $me.hide();
                    }
                } else {
                    $me.hide();
                }*/
            }
        });

   });

    //modifySourceOnEdit();
    //initializeLanguage();
    $(".listSelector").change(function () {
        var selectedList = $(this).val();
        var list_content = $(this).parents(".listarea").find(".listarea_content");
        $(list_content).find("ul").hide();
        $(list_content).find('ul.'+selectedList).show();
        /*switch (selectedList) {
            case 'accDLContent':
                break;
            case 'synDLContent':
                $(list_content).append(synDLContent);
                break;
            case 'comDLContent':
                //$(list_content).append(comDLContent);
                break;
            case 'speciesDLContent':
                $(list_content).append(speciesDLContent);
                break;

            case 'accWLContent':
                $(list_content).append(accWLContent);
                break;
            case 'synWLContent':
                $(list_content).append(synWLContent);
                break;
            case 'comWLContent':
                //$(list_content).append(comWLContent);
                break;
            case 'speciesWLContent':
                $(list_content).append(speciesWLContent);
                break;

            case 'accCLContent':
                $(list_content).append(accCLContent);
                break;
            case 'synCLContent':
                $(list_content).append(synCLContent);
                break;
            case 'comCLContent':
                //$(list_content).append(comCLContent);
                break;
            case 'speciesCLContent':
                $(list_content).append(speciesCLContent);
                break;

            default: alert('Wrong option selected!!')
        }*/
    });

    function updateFilter() {
        var taxonGridDataView = taxonGrid.getData()
        taxonGridDataView.setFilterArgs({
            positionFilter: positionFilter,
            statusFilter: statusFilter,
            searchString: searchString
        });
        taxonGridDataView.refresh();
    }
    if($("#taxonGrid").length !=0)
        taxonGrid = initTaxonGrid($('#taxonGrid'));
//    $("#inlineFilterPanel").appendTo(taxonGrid.getTopPanel()).show();
    /*$("#inlineFilterPanel").on('keyup', '#txtSearch', function (e) {
        Slick.GlobalEditorLock.cancelCurrentEdit();
        if (e.which == 27) {
            this.value = "";
        }
        searchString = this.value;
        updateFilter();
    });*/

    $('.filter input').change ( function() {
        var $selectedTaxon = $('#taxaHierarchy .taxon-highlight'); 
        getNamesFromTaxon($selectedTaxon, $selectedTaxon.attr('id').replace('_anchor',''), getSelectedStatus(), getSelectedPosition(), getSelectedRanks());
    });

    /*$('#taxonStatusSelect').multiselect({
        nonSelectedText: 'Choose Status',
        numberDisplayed: 2,
        nSelectedText:' status selected',
        allSelectedText:'All status selected',
        onChange: function(option, checked, select) {
            var $selectedTaxon = $('#taxaHierarchy .taxon-highlight'); 
            getNamesFromTaxon($selectedTaxon, $selectedTaxon.attr('id').replace('_anchor',''), getSelectedStatus(), getSelectedPosition(), getSelectedRanks());
        }
    });

    $('#taxonPositionSelect').multiselect({
        nonSelectedText: 'Choose Position',
        numberDisplayed: 3,
        nSelectedText:' lists selected',
        allSelectedText:'All lists selected',
        onChange: function(option, checked, select) {
            var $selectedTaxon = $('#taxaHierarchy .taxon-highlight'); 
            getNamesFromTaxon($selectedTaxon, $selectedTaxon.attr('id').replace('_anchor',''), getSelectedStatus(), getSelectedPosition(), getSelectedRanks());
        }
    });

    $('#inlineFilterPanel').append($('#rankDropDown').clone().prop('id', 'taxonRankId'));
    $('#taxonRankId').removeClass('span5').addClass('input-block-level').attr('multiple', 'multiple').find("option").eq(0).remove();
    $('#taxonRankId').multiselect({
        nonSelectedText: 'Choose rank to show',
        numberDisplayed: 1,
        nSelectedText:' ranks selected',
        allSelectedText:'All ranks selected',
        onChange: function(option, checked, select) {
            var $selectedTaxon = $('#taxaHierarchy .taxon-highlight'); 
            getNamesFromTaxon($selectedTaxon, $selectedTaxon.attr('id').replace('_anchor',''), getSelectedStatus(), getSelectedPosition(), getSelectedRanks());
        }
    });*/

    $('.selectAll').click(function() {
       $(this).parent().next().find('input:checkbox:not([disabled])').prop('checked', 'checked').trigger('change');
    });
    $('.selectNone').click(function() {
       $(this).parent().next().find('input:checkbox:not([disabled])').removeProp('checked').trigger('change');
    });





    function generateselectedData(selectedData){        
        var out ='<div class="row-fluid">';
        var selOption = "<option value=''>Choose Name</option>";
        var selSourceAccOnly ="<option value=''>Choose Name</option>";        
                out +="<div class='row-fluid rowSel header'>";
                out +="<div class='span2'>Rank</div>"
                out +="<div class='span6'>Scientific Name</div>";
                out +="<div class='span2'>Position</div>";
                out +="<div class='span2'>Status</div>";
                out +="</div>";
        $.each(selectedData, function (key,selectName) {
                selOption +="<option value='"+selectName.taxonid+"'>"+selectName.name+"</option>";
                if(selectName.status=='ACCEPTED'){  
                    selSourceAccOnly +="<option value='"+selectName.taxonid+"'>"+selectName.name+"</option>";
                }
                out +="<div class='row-fluid rowSel rowSel_"+selectName.taxonid+"'>";
                out +="<div class='span2'>"+taxonRanks[selectName.rank].text+"</div>"
                out +="<div class='span6'>"+selectName.italicisedform+"</div>";
                out +="<div class='span2'>"+selectName.position+"</div>";
                out +="<div class='span2'>"+selectName.status+"</div>";
                out +="</div>";
            });
        $('.mergeTarget').html(selOption);
        $('.changeSynTarget').html(selSourceAccOnly);
        out+="</div>";
      //  alert(out);
        return out;

    }


    function checkRankStatusFun(){
        var result = { rankCheck:true,statusCheck:true};
        var rankCheckArr = [];
        var statusCheckArr =[];
        console.log(taxonGridSelectedRow);

        $.each(taxonGridSelectedRow, function (index, value) {
            console.log("=====================================");
            console.log(index);
            console.log(value);
            if(rankCheckArr.length >0 && result.rankCheck){
                if($.inArray(value.rank,rankCheckArr) == -1){
                    result.rankCheck=false;
                }
            }
            rankCheckArr.push(value.rank);

            if(statusCheckArr.length >0 && result.statusCheck){
                if($.inArray(value.status,statusCheckArr) == -1){
                    result.statusCheck=false;
                }
            }
            statusCheckArr.push(value.status);
        });
        console.log(result);
        return result
    }

    $('.clickSelectedRow').click(function(){
       // alert(taxonGrid.getSelectedRows());
        var selectedRow = taxonGrid.getSelectedRows();        
        if(selectedRow.length > 0){
            var taxonGridData = "";
            taxonGridSelectedRow = {};
            $.each(selectedRow, function (index, value) {
              taxonGridData = taxonGrid.getData().getItem(value);
              taxonGridSelectedRow[taxonGridData.taxonid] = taxonGridData;
            });
            $("#myModal .selectedNamesWrapper").html(generateselectedData(taxonGridSelectedRow));
            $('#myModal').modal('show');
        }
    });

    $(document).on('click','.rowSel',function(){
        var that = $(this);
        if(that.hasClass('header') ||  that.hasClass('disableSciName'))
            return false;

        if(that.hasClass('rowSelActive')){
            that.removeClass('rowSelActive');
        }else{
            that.addClass('rowSelActive');
        }  

        if($('.rowSelActive').length == 0){
            $('.removeSelName').addClass('disabled');     
        }else{
            $('.removeSelName').removeClass('disabled');
        }
    });

    $(document).on('change','.mergeWrapper select',function(){
        var that = $(this);

        if(that.hasClass('mergeTarget')){
            var checkRankStatus = checkRankStatusFun();
            if(!checkRankStatus.rankCheck || !checkRankStatus.statusCheck){
                var msg = '';
                if(!checkRankStatus.rankCheck)
                    msg += " Rank must be same level";
                if(!checkRankStatus.statusCheck)
                    msg += " Status must be same level";

                alert(msg);
                return false;
            }
        }

        var value = that.val();
        $(".mergeWrapper").css('height','50px');
        that.parent().css('height','80px');
        $(".rowSel").removeClass('disableSciName');
        $(".rowSel_"+value).addClass('disableSciName').removeClass('rowSelActive');
        $('.selSub').hide();
        if(value != ''){            
            that.next().show();
        }
    });


if(taxonGrid){
    taxonGrid.onSelectedRowsChanged.subscribe(function(){
     var selectedRows = taxonGrid.getSelectedRows();     
     if (selectedRows.length === 0) {          
        $('.clickSelectedRowWrap').slideUp();
     }else{
        $('.clickSelectedRowWrap').slideDown();
     }
    }); 
}

});
 function mergeWithSource(me){
        var newSourceId  = me.parent().find('.mergeTarget').val();
        var oldSoureceId = ''; 
        $.each(taxonGridSelectedRow, function (index, value) {
            if(index != newSourceId){
               oldSoureceId = value.taxonid; 
            }
        });
        //alert("newSourceId "+newSourceId+" oldSoureceId "+oldSoureceId);
        if(newSourceId != '' && oldSoureceId != ''){ 
           var params ={ sourceId:oldSoureceId, targetId:newSourceId} 
            $.ajax({
                url: '/namelist/mergeNames',
                dataType: "json",
                type: "POST",
                data: params,   
                success: function(data) {
                    if(data.status){
                        location.reload()
                    }else{
                        alert('Error Merge Names');
                    }
                }
            });
        }

        return false;
}

function deleteSourceName(me){
    var params ={ id:taxonGridSelectedRow[Object.keys(taxonGridSelectedRow)[0]].taxonid}
        $.ajax({
            url: '/namelist/deleteName',
            dataType: "json",
            type: "POST",
            data: params,   
            success: function(data) {
                if(data.status){
                    location.reload()
                }else{
                        alert('Error Delete Names');
                }
            }
        });
    return false;
}

function changeAccToSyn(me){
    var newSourceId  = me.parent().find('.changeSynTarget').val();
    var oldSoureceId = ''; 
    $.each(taxonGridSelectedRow, function (index, value) {
        if(index != newSourceId){
           oldSoureceId = value.taxonid; 
        }
    });
   // alert("newSourceId "+newSourceId+" oldSoureceId "+oldSoureceId);
    if(newSourceId != '' && oldSoureceId != ''){ 
        var params ={ sourceAcceptedId:oldSoureceId,targetAcceptedId:newSourceId}
            $.ajax({
                url: '/namelist/changeAccToSyn',
                dataType: "json",
                type: "POST",
                data: params,   
                success: function(data) {
                    if(data.status){
                        location.reload()
                    }else{
                        alert('Error changeAccToSyn');
                    }
                }
            });
        return false;
    }
}


function updatePosition(me){
    var position  = me.parent().find('.movePosition').val();
    var id = taxonGridSelectedRow[Object.keys(taxonGridSelectedRow)[0]].taxonid;
   // alert("id "+id+" position "+position);
    if(id != '' && position != ''){ 
        var params ={ id:id,position:position}
            $.ajax({
                url: '/namelist/updatePosition',
                dataType: "json",
                type: "POST",
                data: params,   
                success: function(data) {
                    if(data.status){
                        location.reload()
                    }else{
                        alert('Error changeAccToSyn');
                    }
                }
            });
        return false;
    }
}