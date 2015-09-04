var accDLContent, accWLContent, accCLContent;
var synDLContent, synWLContent, synCLContent;
var comDLContent, comWLContent, comCLContent;
var oldName = '', oldRank = '' , oldStatus = '';

function createListHTML(list, nameType, isOrphanList) {
    var listContent = "<ul>";
    $.each(list, function(index, value){
        var x = "";
        if(value.isflagged == true){
            var temp = value.flaggingreason + '';
            x = "<i class='icon-flag' title='"+temp+"'></i>";
        }
        listContent += "<li onclick='getNameDetails("+value.taxonid +","+ value.classificationid+","+nameType+",this,"+isOrphanList+")'><a>" +value.name +"</a><input type='hidden' value='"+value.id+"'>"+x+"</li>"
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

function getNamesFromTaxon(ele , parentId) {
    processingStart();
    if($("#taxonHierarchy tr").hasClass("clickedEle")) {
        $("#taxonHierarchy tr").removeClass("clickedEle");
    }
    $(ele).parents("tr").addClass("clickedEle");
    $("#taxonHierarchy tr").css('background', 'white');
    $(ele).parents("tr").css('background', '#3399FF');
    //var taxonId = $("input#taxon").val();//$(ele).parent("span").find(".taxDefIdVal").val();
    var classificationId = $('#taxaHierarchy option:selected').val();
    var url = window.params.curation.getNamesFromTaxonUrl;
    $.ajax({
        url: url,
        dataType: "json",
        type: "POST",
        data: {parentId:parentId, classificationId:classificationId},	
        success: function(data) {
            $('.listSelector option:eq(0)').prop('selected', true);
            //DIRTY LIST 
            if(data.dirtyList.accDL){
                accDLContent = createListHTML(data.dirtyList.accDL, 1, false); 
                $(".dl_content ul").remove();
                $(".dl_content").append(accDLContent);
            }
            if(data.dirtyList.synDL){
                synDLContent = createListHTML(data.dirtyList.synDL, 2, false); 
            }
            if(data.dirtyList.comDL){
                //comDLContent = createListHTML(data.dirtyList.comDL, 3, false); 
            }
            //WORKING LIST
            if(data.workingList.accWL){
                accWLContent = createListHTML(data.workingList.accWL, 1, false); 
                $(".wl_content ul").remove();
                $(".wl_content").append(accWLContent);
            }
            if(data.workingList.synWL){
                synWLContent = createListHTML(data.workingList.synWL, 2, false); 
            }
            if(data.workingList.comWL){
                //comWLContent = createListHTML(data.workingList.comWL, 3, false); 
            }
            //CLEAN LIST
            if(data.cleanList.accCL){
                accCLContent = createListHTML(data.cleanList.accCL, 1, false);
                $(".cl_content ul").remove();
                $(".cl_content").append(accCLContent);
            }
            if(data.cleanList.synCL){
                synCLContent = createListHTML(data.cleanList.synCL, 2, false);
            }
            if(data.cleanList.comCL){
                //comCLContent = createListHTML(data.cleanList.comCL, 3, false);
            }
            processingStop(); 
        }, error: function(xhr, status, error) {
            processingStop();
            alert(xhr.responseText);
        } 
    });

}

function getNameDetails(taxonId, classificationId, nameType, ele, isOrphanName) {
    $("#externalDbResults").modal('hide');
    processingStart();
    $(ele).parent("ul").find("a").css('background-color','inherit');
    $(ele).find("a").css('background-color','#3399FF');
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
            if(nameType == 2 || nameType == 3) {
                choosenName = $(ele).text();
            }
        $.ajax({
            url: url,
            dataType: "json",
            type: "POST",
            data: {taxonId:taxonId, nameType:nameType, classificationId:classificationId, choosenName: choosenName},	
            success: function(data) {
                $('.feedComment').html(data.feedCommentHtml);
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
                if($(ele).parents(".dl_content").length) {
                    $(".dialogMsgText").html("Existing name attributes from IBP displayed below. Catalogue of Life (CoL) is the preferred taxonomic reference for IBP, auto-querying CoL for up-to-date name attributes.");
                    $("#dialogMsg").modal('show');
                    $('.queryDatabase option[value="col"]').attr("selected", "selected");
                    $('.queryString').trigger("click");
                }
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
            setOption($(ele).find(".languageDropDown")[0], value["language"]);
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

function setOption(selectElement, value) {
    var options = selectElement.options;
    for (var i = 0, optionsLength = options.length; i < optionsLength; i++) {
        if (options[i].value == value) {
            selectElement.selectedIndex = i;
            return true;
        }
    }
    return false;
}

function populateNameDetails(data){
    $(".canBeDisabled input[type='text']").val('');
    $('.rankDropDown option:first-child').attr("selected", "selected");
    $('.statusDropDown option:first-child').attr("selected", "selected");
    for (var key in data) {
        if(key != "rank" && key!= "status"){
            $("."+key).val(data[key]);
        }
    }
    if($(".source").val() == 'COL' || $(".source").val() == 'CatalogueOfLife') {
        changeEditingMode(true);
    }
    $(".via").val(data["sourceDatabase"]);
    $(".id").val(data["matchId"]);
    if(data["externalId"]) {
        $(".source").val($("#queryDatabase option:selected ").text());
        $(".id").val(data["externalId"]);
    }
    setOption(document.getElementById("rankDropDown"), data["rank"]);
    setOption(document.getElementById("statusDropDown"), data["nameStatus"]);
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
                $("#externalDbResults h6").html(name +"(IBP status : "+$("#statusDropDown").val()+")");
                fillPopupTable(data , $("#externalDbResults"), "externalData", true, source);
            }else {
                var oldText = $(".dialogMsgText").html();
                if (oldText.indexOf("Sorry") >= 0) {
                    oldText = "";//arr[0];
                    $(".dialogMsgText").html("Sorry no results found from "+ $("#queryDatabase option:selected").text() + ". Please query an alternative database or input name-attributes manually.");
                } else {
                    $(".dialogMsgText").html(oldText + "<hr><br /> <b>RESPONSE</b> <br /> Sorry no results found from "+ $("#queryDatabase option:selected").text() + ". Please query an alternative database or input name-attributes manually.");
                }
                
                //alert("Sorry no results found from "+ $("#queryDatabase option:selected").text() + ". Please query an alternative database or input name-attributes manually.");
                $("#dialogMsg").modal('show');
                if(addNewName) {
                    alert("Searching name in IBP Database");
                    searchIBP(name);
                }
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

function fillPopupTable(data, $ele, dataFrom, showNameDetails, source ) {
    if(data.length == 0) {
        alert("Sorry No results found!!");
    }
    var classificationId = $('#taxaHierarchy option:selected').val();
    //clear table
    $ele.find("table tr td").remove();
    var rows = "";
    $.each(data, function(index, value) {
    	if(dataFrom == "externalData") {
    		var onclickEvent = (source ==  "onlinSpeciesCreation") ? 'openSpeciesPage(' + value['id'] + ',"' + value['externalId'] + '")' : 'getExternalDbDetails(this, ' +showNameDetails+')'
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
            
            rows += "<td>"+value['rank']+"</td><td>"+nameStatus+"</td><td>"+value['group']+"</td><td>"+value['sourceDatabase']+"</td><td><div class='btn' onclick='"+ onclickEvent + "'>Select this</div></td></tr>"        
        }
        else {
        	var onclickEvent = (source ==  "onlinSpeciesCreation") ? 'openSpeciesPage(' + value['id'] + ',' + value['externalId'] + ')' : 'getNameDetails(' +value['taxonId'] + ',' + classificationId + ',1, undefined)' 
            rows += "<tr><td>"+value['name'] +"</td><td>"+value['rank']+"</td><td>"+value['nameStatus']+"</td><td>"+value['group']+"</td><td>"+value['sourceDatabase']+"</td><td><div class='btn' onclick='"+ onclickEvent + "'>Select this</div></td></tr>"
        }
    });
    $ele.find("table").append(rows);
    return
}

function openSpeciesPage(taxonId, colId){
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

function validateSpeciesSuccessHandler(data, search){
	
	function showSearchPopup(data){
		// show the popup
		var tList = data.taxonList
		if (tList && tList.length != 0) {
			$("#dialogMsg").modal('hide');
			$("#externalDbResults").addClass('IBPResult');
			$("#externalDbResults").modal('show');
			$("#externalDbResults h6").html(name);
			fillPopupTable(tList, $("#externalDbResults"), "IBPData", true, "onlinSpeciesCreation");
		} else {
			$("#dialogMsg").modal('show');
			$(".dialogMsgText").html("Sorry no results found from IBP Database. Fill in details manually");
		}

		if ($("#externalDbResults").hasClass('IBPResult')) {
			$('#externalDbResults .modal-dialog').on('hidden', function(event) {
				$(this).unbind();
				$("#dialogMsg").modal('show');
				$(".dialogMsgText").html("Searching in COL...");
				searchAndPopupResult(data.requestParams.page, "col", false, "onlinSpeciesCreation");
			});
		} else {
			$("#dialogMsg").modal('show');
			$(".dialogMsgText").html("Searching in COL...");
			searchAndPopupResult(data.requestParams.page, "col", false, "onlinSpeciesCreation")
		}
		$("#externalDbResults").removeClass('IBPResult');

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
		var taxonRanks = data.taxonRanks;
		for (var i = 0; i < data.rank; i++) {
			var taxonValue = (taxonRegistry && taxonRegistry[i]) ? taxonRegistry[i]
					: taxonRanks[i].taxonValue;
			$(
					'<div class="input-prepend"><span class="add-on">'
							+ taxonRanks[i].text
							+ (taxonRanks[i].mandatory ? '*' : '')
							+ '</span><input data-provide="typeahead" data-rank ="'
							+ taxonRanks[i].value
							+ '" type="text" class="taxonRank" name="taxonRegistry.'
							+ taxonRanks[i].value + '" value="' + taxonValue
							+ '" placeholder="Add ' + taxonRanks[i].text
							+ '" /></div>').appendTo($hier);
		}
		if (data.rank > 0)
			$('#taxonHierarchyInputForm').show();

		if ($(".taxonRank:not(#page)").length > 0)
			$(".taxonRank:not(#page)").autofillNames();
		
	}
	
	if (data.success == true) {
		//if species page id returned then open in edit mode
		if (data.id) {
			window.location.href = '/species/show/' + data.id + '?editMode=true'
			return;
		}
		
		$('#errorMsg').removeClass('alert-error hide').addClass('alert-info').html(data.msg);
		
		updateHirInput(data);
		
		//showing parser info
		$('#parserInfo').children('.canonicalName').html(data.canonicalForm);
		$('#parserInfo').children('.authorYear').html(data.authorYear);
		$('#parserInfo').show();
		
		if(search)
			showSearchPopup(data);
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
            if(!showNameDetails) {
                //create accepted match and saveAcceptedName
                $(".fromCOL").val(true);
                saveAcceptedName(createNewAcceptedNameData(data));
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
                    $(".id_details").val(JSON.stringify(data['id_details']));
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

function saveHierarchy(moveToWKG) {
    
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

    if(oldStatus == 'accepted') {
        postProcessOnAcceptedName();
    }
    var url = window.params.curation.curateNameURL;
    var acceptedMatch = JSON.stringify(dataToProcess(moveToWKG));
    $.ajax({
        url: url,
        type: "POST",
        dataType: "json",
        data: {acceptedMatch: acceptedMatch},	
        success: function(data) {
            console.log("============YUHU ===");
            console.log(data);
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
                //postProcessOnAcceptedName();
            } else {
                $(".dialogMsgText").html(data['msg']);
                $("#dialogMsg").modal('show');
                processingStop();
            }
        }, error: function(xhr, status, error) {
            processingStop();
            alert(xhr.responseText);
        } 
    });
}

function fetchTaxonRegistryData() {
    var result = {}
    result['taxonRegistry.0'] = $('.kingdom').val();
    result['taxonRegistry.1'] = $('.phylum').val();
    result['taxonRegistry.2'] = $('.class').val();
    result['taxonRegistry.3'] = $('.order').val();
    result['taxonRegistry.4'] = $('.superfamily').val();
    result['taxonRegistry.5'] = $('.family').val();
    result['taxonRegistry.6'] = $('.subfamily').val();
    result['taxonRegistry.7'] = $('.genus').val();
    result['taxonRegistry.8'] = $('.subgenus').val();
    result['taxonRegistry.9'] = $('.species').val();
    
    result['reg'] = $(".taxonRegId").val()          //$('#taxaHierarchy option:selected').val();
    result['classification'] = 817; //for author contributed
    
    var res = {};
    res['0'] = $('.kingdom').val();
    res['1'] = $('.phylum').val();
    res['2'] = $('.class').val();
    res['3'] = $('.order').val();
    res['4'] = $('.superfamily').val();
    res['5'] = $('.family').val();
    res['6'] = $('.subfamily').val();
    res['7'] = $('.genus').val();
    res['8'] = $('.subgenus').val();
    res['9'] = $('.species').val();
    result['taxonRegistry'] = res;

    var metadata1 = {};
    metadata1['name'] = $('.name').val();
    metadata1['rank'] = $('.rankDropDown').val();
    metadata1['authorString'] = $('.authorString').val();
    metadata1['nameStatus'] = $('.statusDropDown').val();
    metadata1['source'] = $('.source').val();
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
    return;
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
    for(var i = 0; i< 4; i++) {
        $context.find(".add_new_row").trigger("click");
    }
    $context.find(".tab_div:lt("+numRows+")").remove();
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

function dataToProcess(moveToWKG) {
    var result = {}
    result['kingdom'] = $('.kingdom').val();
    result['phylum'] = $('.phylum').val();
    result['class'] = $('.class').val();
    result['order'] = $('.order').val();
    result['superfamily'] =$('.superfamily').val();
    result['family'] =$('.family').val();
    result['subfamily'] = $('.subfamily').val();
    result['genus'] = $('.genus').val();
    result['subgenus'] = $('.subgenus').val();
    result['species'] =$('.species').val();

    result['name'] = $('.name').val();
    result['group'] = $('.kingdom').val();
    result['rank'] = $('.rankDropDown').val();
    result['authorString'] = $('.authorString').val();
    result['nameStatus'] = $('.statusDropDown').val();
    result['source'] = $('.source').val();
    result['sourceDatabase'] = $('.source').val();
    result['via'] = $('.via').val();
    result['id'] = $('.id').val(); 
    result['externalId'] = $('.id').val();
    result['abortOnNewName'] = true;
    result['fromCOL'] = $('.fromCOL').val();
    result['isOrphanName'] = $('.isOrphanName').val();
    if($('.fromCOL').val() == "true") {
        result['abortOnNewName'] = false;
        result['id_details'] = JSON.parse($(".id_details").val());
    }
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
    return;
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
    result['kingdom'] = data['kingdom'];
    result['phylum'] = data['phylum'];
    result['class'] = data['class'];
    result['order'] = data['order'];
    result['superfamily'] = data['superfamily'];
    result['family'] = data['family'];
    result['subfamily'] = data['subfamily']; 
    result['genus'] = data['genus'];
    result['subgenus'] = data['subgenus'];
    result['species'] = data['species'];

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
        result['id_details'] = data['id_details'];
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
            $(".validating").val(data.acceptedNameId);
            $(".validating").removeClass('validating');
        }, error: function(xhr, status, error) {
            processingStop();
            alert(xhr.responseText);
        } 
    });
}


$(document).ready(function() {

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
        var taxon = 94899;//$("input#filterByTaxon").val();
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
});
