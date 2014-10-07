
$(document).ready(function() {
    
    $('#filterPanel').on("change", ".searchFilter", function() {
        if($(this).hasClass('active')){
            $(this).removeClass('active');
        } else
            $(this).addClass('active');

        updateGallery(undefined, window.params.queryParamsMax, window.params.offset, undefined, window.params.isGalleryUpdate, undefined, undefined, true);
        return false;
    });

});

function resetSearchFilters() {
    $('#filterPanel .active').each (function() {
        $(this).removeClass('active').removeAttr('checked');
    });
}
