
$(document).ready(function() {
    
    $('#filterPanel').on("change", ".searchFilter", function() {
        if($(this).hasClass('active')){
            $(this).removeClass('active');
        } else
            $(this).addClass('active');

        updateGallery(undefined, window.params.queryParamsMax, window.params.offset, undefined, window.params.isGalleryUpdate, undefined, undefined, true);
        return false;
    });

    $('.resetFilter').click(function() {
        resetSearchFilters($(this).parent().parent().parent());
        updateGallery($( "#advSearchForm" ).attr('action'), undefined, undefined, undefined, false);
        return false;
    });
});

function resetSearchFilters($ele) {
    if($ele == undefined) $ele = $('#filterPanel');
    $ele.find('input').removeClass('active').removeAttr('checked').addClass('active').prop('checked',true);
}
