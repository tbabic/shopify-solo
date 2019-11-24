var multipleAddressComponent = new Vue({
	el: '#app',
	data: {
		multipleAddressString: '',
		addressList: []
	},
	methods: {
		processMultipleAddressString: function () {
			let body = {};
			body.value = this.multipleAddressString;
			$.ajax(
					{
						url: "/adresses/parse",
						type: "POST",
						data: JSON.stringify(body),
						dataType: 'json',
						contentType: 'application/json; charset=utf-8',
						async: false,
						success: (response) => {
							this.addressList.splice(0,this.addressList.length);
							response.forEach(address => this.addressList.push(address));
						}
					}
			);
		},
		printToPdf: function () {
			$.ajax(
					{
						url: "/adresses/print",
						type: "POST",
						data: JSON.stringify(this.addressList),
						dataType: 'json',
						contentType: 'application/json; charset=utf-8',  
						async: false,
						success: function(response) {
							
							var binaryString = window.atob(response.value);
						    var binaryLen = binaryString.length;
						    var bytes = new Uint8Array(binaryLen);
						    for (var i = 0; i < binaryLen; i++) {
						       var ascii = binaryString.charCodeAt(i);
						       bytes[i] = ascii;
						    }
							
						    var blob = new Blob([bytes], {type: "application/pdf"});
						    var link = document.createElement('a');
						    link.href = window.URL.createObjectURL(blob);
						    var fileName = "Prijamna knjiga";
						    link.download = fileName;
						    link.click();
						}
					}
			);

		}
	}
});
