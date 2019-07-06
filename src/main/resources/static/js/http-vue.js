//simple wrapper for loading async vue components via http;
//requires vue and axios;

var httpVue = {};

httpVue.component = function(name, options) {
	let optionsCopy = {};
	if (options !== undefined) {
		for(let prop in options) {
			optionsCopy[prop] = options[prop];
		}
	}
	
	if (optionsCopy.template !== undefined) {
		console.log("Template defined, using normal view component")
		return Vue.component(name, optionsCopy);
	}
	let templateUrl = name + ".html";
	if (optionsCopy.templateUrl !== undefined) {
		templateUrl = optionsCopy.templateUrl;
		delete optionsCopy['templateUrl'];
	} 
	return Vue.component(name, function(resolve, reject) {
		axios.get(templateUrl+"?time="+(new Date()).getTime()).then(function(response) {
			optionsCopy.template = response.data;
			resolve(optionsCopy);
		});
	});
}