
$(document).ready(function() {
    
    $('#filterPanel').on("change", ".searchFilter", function() {
        if($(this).hasClass('active')){
            $(this).removeClass('active');
        } else
            $(this).addClass('active');

        var allEle = $(this).parent().parent().find('input[value="all"]');
        if(allEle.length > 0) {
            allEle.removeClass('active').removeAttr('checked')
        }
        updateGallery(undefined, window.params.queryParamsMax, window.params.offset, undefined, window.params.isGalleryUpdate, undefined, undefined, true);
        return false;
    });

    $('.resetFilter').click(function() {
        resetSearchFilters($(this).parent().parent().parent());
        updateGallery(undefined, window.params.queryParamsMax, window.params.offset, undefined, window.params.isGalleryUpdate, undefined, undefined, true);
        return false;
    });
});

function resetSearchFilters($ele) {
    if($ele == undefined) $ele = $('#filterPanel');
    if($ele.find('input[value="all"]').length > 0) {
        $ele.find('input').removeClass('active').removeAttr('checked');
        $ele.find('input[value="all"]').removeClass('active').removeAttr('checked').addClass('active').prop('checked',true);
    } else {
        $ele.find('input').removeClass('active').removeAttr('checked').addClass('active').prop('checked',true);
    }
}
