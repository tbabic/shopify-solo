var homeComponent = new Vue({
	el:"#discounts",
	data: {
		categories : {
			50 : {
				inputCode: "",
				codes : []
			},
			150 : {
				inputCode: "",
				codes : []
			},
			250 :{
				inputCode: "",
				codes : []
			},
			400 : {
				inputCode: "",
				codes : []
			},
		},
		loadingCount : 0
	},
	methods: {
		insertCode : function(category) {
			this.categories[category].codes.push(this.categories[category].inputCode);
			this.categories[category].inputCode = "";
		},
		deleteCode : function(index, category) {
			this.categories[category].codes.splice(index, 1);
		},
		
		saveCode : function(discountCode) {
			this.startLoader();
			return axios.post('/manager/discounts/', discountCode).then(response => {
				console.log(response);
				this.endLoader();
			}).catch(error => {
				this.endLoader();
				this.showError(error.response.data.message);
			});
		},
		
		saveAllCodes : function() {
			this.startLoader();
			let promise = null;
			for (let amount in this.categories) {
				
				// reverse order
				for (var i = this.categories[amount].codes.length - 1; i >= 0; i--) {
					let discountCode = {
						amount : amount,
						code: this.categories[amount].codes[i]
					};
					if (promise == null) {
						promise = this.saveCode(discountCode);
					} else {
						promise = promise.then(() => {
							return this.saveCode(discountCode); 
						});
					}
					promise = promise.then(() => {
						this.categories[amount].codes.splice(i, 1);
					});
				}
			}

			return promise.then(() => {
				console.log("all discounts saved");
				alert("Kodovi spremljeni");
			}).finally(() => {
				this.endLoader();
			});
			
		},
		
		showError: function(errorMsg) {
			if (errorMsg === undefined) {
				errorMsg = "Unexpected error";
			}
			$("#errorContent").text(errorMsg);
			$("#errorModal").modal('show');
		},
		
		startLoader : function() {
			this.loadingCount++;
			if (this.loadingCount > 0) {
				document.getElementById("overlay").style.display = "flex";
			}
			
		},
		endLoader : function() {
			this.loadingCount--;
			if (this.loadingCount <= 0) {
				document.getElementById("overlay").style.display = "none";
				this.loadingCount = 0;
			}
			
		}
	},
	mounted : function () {
		this.startLoader();
		let token = localStorage.getItem("token");
		
		let loginPromise = null;
		if (token == null) {
			loginPromise = axios.post('/manager/login', null);
		} else {
			loginPromise = axios.post('/manager/login', null, {
				headers : {
					Authorization : token
				}
			});
		}
		
		
		loginPromise.then(response => {
			this.authToken = response.data.authToken;
			this.role = response.data.role;
			localStorage.setItem("token", this.authToken);
			axios.defaults.headers.common['Authorization'] = this.authToken;
		}).then(() => {
			this.endLoader();
		})
		
		
		
	}
});