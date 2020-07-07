$(document).on("click", ".clickable-row", function(event) {
	let element = $(this);
	if (element.hasClass("highlight")) {
		element.removeClass("highlight");
	} else {
		element.addClass("highlight")
	}
});


$(document).on('shown.bs.modal', '#epkModal', function () {
    $(this).find("#epkInputOrderId").focus();
});

