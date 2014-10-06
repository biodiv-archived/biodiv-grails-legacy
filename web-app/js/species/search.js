
$(document).ready(function() {
    
    $('#filterPanel').on("change", ".searchFilter", function() {
        console.log($(this));
        if($(this).hasClass('active')){
            $(this).removeClass('active');
        } else
            $(this).addClass('active');

        updateGallery(undefined, window.params.queryParamsMax, window.params.offset, undefined, window.params.isGalleryUpdate);
        return false;
    });

});
