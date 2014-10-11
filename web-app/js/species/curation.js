function getNamesFromTaxon(taxonId) {
    console.log("called--------------");
    taxonId = $(".taxDefIdCheck:checked").parent("span").find(".taxDefIdVal").val();
    var url = window.params.curation.getNamesFromTaxonUrl;
    $.ajax({
        url: url,
        dataType: "json",
        data: {taxonId:taxonId},	
        success: function(data) {
            console.log("======SUCCESS====");
            if(data.dirtyList){
                var dlContent = "<ul>";
                $.each(data.dirtyList, function(index, value){
                    dlContent += "<li onclick='getNameDetails("+value.id+")'><a>" +value.name +"</a><input type='hidden' value='"+value.id+"'></li>"
                    console.log(value.name);
                });
                dlContent += "</ul>";
                $(".dl_content ul").remove();
                $(".dl_content").append(dlContent);
            }
            if(data.workingList){
                var wlContent = "<ul>";
                $.each(data.workingList, function(index, value){
                    wlContent +="<li onclick='getNameDetails("+value.id+")'><a>" + value.name +"</a><input type='hidden' value='"+value.id+"'></li>"
                    console.log(value.name);
                });
                wlContent += "</ul>";
                $(".wl_content ul").remove();
                $(".wl_content").append(dlContent);

            }
            if(data.cleanList){
                var clContent = "<ul><li>";
                $.each(data.cleanList, function(index, value){
                    clContent +="<li onclick='getNameDetails("+value.id+")'><a>" + value.name +"</a><input type='hidden' value='"+value.id+"'></li>"
                    console.log(value.name);
                });
                clContent += "</ul>";
                $(".cl_content ul").remove();
                $(".cl_content").append(dlContent);

            }

            console.log(dlContent);
            console.log(wlContent);
            console.log(clContent);
            console.log(data);
        }, error: function(xhr, status, error) {
            alert(xhr.responseText);
        } 
    });

}

function getNameDetails(taxonId) {
    console.log("=======NAME DEATILS=======" + taxonId);
    var url = window.params.curation.getNameDetailsUrl;
    $.ajax({
        url: url,
        dataType: "json",
        data: {taxonId:taxonId},	
        success: function(data) {
            console.log("======SUCCESS====");
            /*if(data.dirtyList){
                var dlContent = "<ul>";
                $.each(data.dirtyList, function(index, value){
                    dlContent += "<li onclick='getNameDetails("+value.id+")'><a>" +value.name +"</a><input type='hidden' value='"+value.id+"'></li>"
                    console.log(value.name);
                });
                dlContent += "</ul>";
                $(".dl_content ul").remove();
                $(".dl_content").append(dlContent);
            }
            if(data.workingList){
                var wlContent = "<ul>";
                $.each(data.workingList, function(index, value){
                    wlContent +="<li onclick='getNameDetails("+value.id+")'><a>" + value.name +"</a><input type='hidden' value='"+value.id+"'></li>"
                    console.log(value.name);
                });
                wlContent += "</ul>";
                $(".wl_content ul").remove();
                $(".wl_content").append(dlContent);

            }
            if(data.cleanList){
                var clContent = "<ul><li>";
                $.each(data.cleanList, function(index, value){
                    clContent +="<li onclick='getNameDetails("+value.id+")'><a>" + value.name +"</a><input type='hidden' value='"+value.id+"'></li>"
                    console.log(value.name);
                });
                clContent += "</ul>";
                $(".cl_content ul").remove();
                $(".cl_content").append(dlContent);

            }
*/
            console.log(dlContent);
            console.log(wlContent);
            console.log(clContent);
            console.log(data);
        }, error: function(xhr, status, error) {
            alert(xhr.responseText);
        } 
    });
}
