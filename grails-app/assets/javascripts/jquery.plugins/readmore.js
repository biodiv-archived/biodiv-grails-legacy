/**
 * jquery.readmore - Substring long paragraphs and make expandable with "more" link
 * @date 7 July 2010
 * @author Jake Trent  http://www.jtsnake.com/
 * @version 1.1
 */
(function ($) {
  $.fn.readmore = function (settings) {

    var opts =  $.extend({}, $.fn.readmore.defaults, settings);

    this.each(function () {
      $(this).data("opts", opts);
      if ($(this).html().length > opts.substr_len) {
        abridge($(this));
        linkage($(this));
      }
    });

    function linkage(elem) {
      elem.append(elem.data("opts").more_link);
      elem.children(".more").click( function () {
    	if($(this).html() == window.i8ln.text.hide)
    		$(this).html(window.i8ln.text.more);
    	else
    		$(this).html(window.i8ln.text.hide);
        $(this).siblings("span:not(.hidden)").toggle().siblings("span.hidden").animate({'opacity' : 'toggle'},1000);
      });
    }

    function abridge(elem) {
      var opts = elem.data("opts");
      var txt = elem.text();
      var len = opts.substr_len;
      var dots = "<span>" + opts.ellipses + "</span>";  
      var shown = txt.substring(0, len) + dots;
      var hidden = '<span class="hidden" style="display:none;">' + txt.substring(len, txt.length) + '</span>';
      elem.html(shown + hidden);
    }
    
    return this;
  };

  $.fn.readmore.defaults = {
    substr_len: 500,
    ellipses: '&#8230;',
    more_link: '<a class="more">Read&nbsp;More</a>'
  };

})(jQuery);