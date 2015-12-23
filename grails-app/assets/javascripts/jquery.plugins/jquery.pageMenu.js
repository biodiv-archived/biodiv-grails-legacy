/**
 * @name jQuery pageMenu plugin
 * @license GPL
 * @version 0.0.2
 * @date 06.30.2011
 * @category jQuery plugin
 * @author Kotelnitskiy Evgeniy (evgennniy@gmail.com)
 * @copyright (c) 2011 Kotelnitskiy Evgeniy (http://4coder.info/en/)
 * @example Visit http://4coder.info/en/ for more informations about this jQuery plugin
 */
(function($, d) {
	jQuery.fn.pageMenu = function(settings) {
	
		// Settings
		settings = jQuery.extend({
			'title' : 'Table of contents'
		}, settings);

        var last_id = 0;
        function next_id() {
            return ++last_id;
        }
		
		// loop each element
		jQuery(this).each(function() {
			// Set element
			var $header = jQuery(this);
			var hl = parseInt(this.tagName[1]);
			var items_count = 0;
			var menu_level = parseInt(this.nodeName[1]) + 1;
			var prev_level = parseInt(this.nodeName[1]) + 1;
			var cur_level = 0;
			
			// Current 
			function getHash() {
				var hash = window.location.hash;
				return hash.substring(1); // remove #
			}
			var current_hash = getHash();
			var current_selected = false;
			
			var menu_html = '';
			
			var selector = '';
            for (var i = hl+1; i < 6; i++) {
                selector += 'h'+i+',';
            }	
			selector = selector + 'h6';
			$items = $(selector, this.parentNode);

			function check_name(name) {
				var hl = parseInt(name[1]);
				return (hl > 0);
			}
			
			var next_node = this.nextSibling;
			while (next_node) {			
				if ((next_node.nodeType == 1) && (check_name(next_node.nodeName))) {
					//console.log(next_node); //**********					
					$item = $(next_node);
					var item_level = parseInt(next_node.nodeName[1]);
					
					if (item_level >= menu_level) {
						var item_class = 'sm-' + next_id();
						$item.addClass(item_class);
						//alert('item_level: ' + item_level + '; prev_level: ' + prev_level + '; cur_level: ' + cur_level); //////

						// Menu element
						var $menu_item_li = $(d.createElement('li'));
						var $menu_item_a = $(d.createElement('a'));
						$menu_item_li.append($menu_item_a);
						$menu_item_a.text($item.text());
						$menu_item_a.attr('rel', item_class);
						$menu_item_a.attr('href', '#'+item_class);
						if (current_hash == item_class) {
							$menu_item_a.addClass('current');
							current_selected = true;
						}
						
						// Anchor
						var $anchor = $(d.createElement('a'))
										.attr('name', item_class)
										.css('visibility', 'hidden')
										.text('*');
										
						$item.append($anchor);
						
						var item_html = '';
						if (item_level == prev_level) {
							if (items_count) item_html += '</li>';
							item_html += '<li>' + $menu_item_li.html();
						}
						else {
							if (item_level > prev_level) {						
								for (var i = 0; i < (item_level - prev_level); i++) {
									item_html += '<ul><li>' + $menu_item_li.html();
									cur_level ++;
								}
							}
							if (item_level < prev_level) {
								for (var i = 0; i < (prev_level - item_level); i++) {
									if (items_count) item_html += '</li>';
									item_html += '</ul></li><li>' + $menu_item_li.html();
									cur_level --;
								}
							}
						}
						
						prev_level = item_level;
						menu_html += item_html;
						items_count ++;
					}
					else {
						return;
					}
				}
				next_node = next_node.nextSibling;
			}
			
			if (items_count > 2) {
				var $menu = $(d.createElement('div')).addClass('submenu-container');
				var $open_button_a = $(d.createElement('a')).addClass('submenu-open');
				$open_button_a.text(settings['title'] + ':');
				$open_button_a.attr('href', '#');
				var $menu_list = $(d.createElement('ul')).addClass('submenu-list');
				$menu_list.html(menu_html);
				$menu.append($open_button_a);
				$menu.append($menu_list);
				$menu.insertAfter($header);
				
				// Add open button
				function menu_button_click() {
					if ($menu_list.css('display') == 'none') {
						$menu_list.css('display', 'block');
						$open_button_a.text(settings['title'] + ':');
					}
					else {
						$menu_list.css('display', 'none');
						$open_button_a.text(settings['title'] + '...');
					}
					return false;
				}
				
				$open_button_a.click(function(){
					menu_button_click();
					return false;
				});
				
				$('a', $menu_list[0]).click(function () {
					$('a', $menu_list[0]).removeClass('current');
					$(this).addClass('current');
					menu_button_click();
					return true;
				});
				
				if (current_selected) {
					menu_button_click();
				}
			}			
		});
	};
})(jQuery, document);