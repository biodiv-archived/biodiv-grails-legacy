(function($) {

    var window = this, options = {}, defaults = {};

    $.fn.autofillUsers = function(_options) {

        if (typeof _options === 'string'
            && $.isFunction(autofillUsers[_options])) {
                var args = Array.prototype.slice.call(arguments, 1), value = autofillUsers[_options]
    .apply(autofillUsers, args);

return value === autofillUsers || value === undefined ? this
    : value;
            }

        _options = $.extend({}, defaults, _options);
        this.options = _options;

        return this
    .each(function() {
        this.validEmailAndIdList = [];
        this.addUserId = function(ui) {
            var userId = ui.item.userId;
            this.addLiChoice(ui.item.value, userId);
            this.validEmailAndIdList.push("" + userId);
            this.removeErrorClass();
            $(this).val("");
        };
        this.addLiChoice = function(itemValue, id) {
            var close = $(
                '<li class="userOrEmail-choice">'
                + itemValue
                + '<span id="'
                + id
                + '" class="userOrEmail-close"> x</span></li>')
                .insertBefore($(this));
            var autofillUsers = this;
            close.find('.userOrEmail-close').click(function() {
                autofillUsers.removeChoice(this);
                return false;
            });
        };
        this.removeErrorClass = function() {
            if ($(this).hasClass('alert alert-error')) {
                $(this).removeClass(('alert alert-error'));
            }
        };
        this.getEmailAndIdsList = function() {
            this.validateAndAddEmail($.trim($(this).val()));
            return this.validEmailAndIdList;
        };
        this.validateAndAddEmail = function(lastEntry) {
            if (this.isEmail(lastEntry)) {
                this.addLiChoice(lastEntry, lastEntry);
                this.validEmailAndIdList.push("" + lastEntry);
                this.removeErrorClass();
            }
            $(this).val("");
        };
        this.isEmail = function(email) {
            var regex = /^([a-zA-Z0-9_\.\-\+])+\@(([a-zA-Z0-9\-])+\.)+([a-zA-Z0-9]{2,4})+$/;
            return regex.test(email);
        };
        this.removeChoice = function(ele){
            var removedIndex = this.validEmailAndIdList.indexOf("" + $(ele).attr("id"));
            this.validEmailAndIdList.splice(removedIndex, 1);
            $(ele).parent().remove();
        };

        $(this)
            .bind(
                    "keydown",
                    function(event) {
                        // don't navigate away from the field on
                        // tab when selecting
                        // an item
                        if (event.keyCode === $.ui.keyCode.TAB
                            && $(this).data("autocomplete").menu.active) {
                                event.preventDefault();
                            } else if (event.keyCode === $.ui.keyCode.COMMA
                                || event.keyCode === $.ui.keyCode.ENTER) {
                                    // storing email after validation
                                    this.validateAndAddEmail(
                                        $.trim(this.value));
                                    event.preventDefault();
                                }
                    }).autocomplete({
                        //appendTo:_options.appendTo,
                        source : function(request, response) {
                            $.getJSON(_options.usersUrl, {
                                term : request.term
                            }, response);
                        },
                        search : function() {
                            // custom minLength
                            var term = this.value;
                            if (term.length < 2) {
                                return false;
                            }
                        },
                        focus : function() {
                            return false;
                        },
                        select : function(event, ui) {
                            // adding user ids from suggestion
                            this.addUserId(ui);
                            return false;
                        }
                    });

    });
    };
})(jQuery);
