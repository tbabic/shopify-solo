$(document).on("click", ".clickable-row", function(event) {
	let element = $(this);
	if (element.hasClass("highlight")) {
		element.removeClass("highlight");
	} else {
		element.addClass("highlight")
	}
});