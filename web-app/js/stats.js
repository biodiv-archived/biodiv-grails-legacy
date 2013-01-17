function getStatistics(group) {
    var url = 'http://' + document.domain + '/ml_orchestrator.php?action=getStatistics&group=' + group;

    var stats;
    $.ajax({
        url: url,
        type: 'GET',
        async: false,
        cache: false,
        timeout: 30000,
        dataType: 'text',
        error: function(){
            return true;
        },
        success: function(data, msg){
            if (parseFloat(msg)){
                return false;
            } else {
                stats = eval('(' + data + ')');
                return true;
            }
        }
    });

    return stats;
}

function getStatisticsHTML() {
        
    var titles = {
                species_page_count:'<span class="stats_normal">Number of</span><br><span class="stats_big_bold">SPECIES</span> <span class="stats_big">PAGES</span>',
layer_count:'<span class="stats_normal">Number of</span><br><span class="stats_big">MAP</span> <span class="stats_big_bold">LAYERS</span>',
                checklist_count:'<span class="stats_normal">Number of</span><br><span class="stats_big_bold">CHECKLISTS</span>',
                species_occurrence_count:'<span class="stats_normal">Number of</span><br><span class="stats_big_bold">SPECIES</span> <span class="stats_big">WITH</span> <span class="stats_big_bold">OCCURRENCE</span> <span class="stats_big">RECORDS</span>',
                occurrence_count:'<span class="stats_normal">Number of</span><br><span class="stats_big_bold">OCCURRENCE</span> <span class="stats_big">RECORDS</span>',
                observation_count:'<span class="stats_normal">Number of</span><br><span class="stats_big_bold">OBSERVATIONS</span></span>'
		};

    var stats = getStatistics();

    var html = '';

    var i=0;	
    for (var key in stats){
        html +=  "<div class='entry span2'" +((false)?"style='clear:both;'>":">") + titles[key] + "<div class='stats_number'>" + stats[key] + '</div></div>';
        i++;
    }

    return html;

}

$(window).load(function(){
	if ($("#statistics_box").length > 0){
                var group = 'default';
		var stats = getStatisticsHTML(group);
		$("#statistics_box").html(stats);
	}

});
