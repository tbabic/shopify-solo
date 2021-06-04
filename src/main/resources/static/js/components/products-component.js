var createOrderComponent = new Vue({
	el:"#products",
	data: {
		variants : [],
		discountOptions:[0, 10, 15, 20, 25, 30, 35, 40, 45, 50],
		
		bulkDiscount : 0,
		selectAll : false,
		
		filter : "",
		
		loadingCount: 0,
		role : null
	},
	methods: {
		
		applyBulkDiscount : function() {
			this.variants.forEach((variant) => {
				if (variant.selected == true || variant.selected == "true") {
					variant.discount = this.bulkDiscount;
					this.applyDiscount(variant);
				}
			});
		},
		
		changeAllSelection : function() {
			this.variants.forEach((variant) => {
				variant.selected = this.selectAll;
			});
		},
		
		applyDiscount : function(variant) {
			if (variant.discount == 0) {
				if (variant.compare_at_price == undefined || variant.compare_at_price == null || variant.compare_at_price == 0) {
					variant.regularPrice = variant.price;
					variant.compare_at_price = 0;
				} else {
					variant.price = variant.compare_at_price;
					variant.compare_at_price = 0;
					variant.change = true;
				}
				
			}
			
			else {
				if (variant.compare_at_price == undefined || variant.compare_at_price == null || variant.compare_at_price == 0) {		
					variant.compare_at_price = variant.price;
					variant.regularPrice = variant.price;
				}
				variant.price = (100- +variant.discount) * +variant.regularPrice / 100;
			}
			
			variant.change = variant.loadedPrice != variant.price;
			
		},
		
		loadProducts : function() {
			this.startLoader();
			axios.get('/manager/products/all')
			.then(response => {
				console.log("foundProducts");
				this.variants.splice(0,this.variants.length);
				response.data.forEach(product => {
					product.variants.forEach(variant => {
						if (variant.title == 'Default Title') {
							variant.title = product.title;
						} else {
							variant.title = product.title + " / " + variant.title;
						}
						if(variant.title.includes("POKLON")) {
							return;
						}
						variant.change = false;
						if (variant.compare_at_price == undefined || variant.compare_at_price == null || variant.compare_at_price == 0) {
							variant.discount = 0;
							Vue.set(variant, "regularPrice", variant.price)
						} else {
							Vue.set(variant, "regularPrice", variant.compare_at_price)
							variant.discount = (100*+variant.regularPrice - 100*+variant.price) / +variant.regularPrice;
						}
						variant.loadedPrice = variant.price;
						variant.selected = false;
						this.variants.push(variant);
					});
				});
				
				// sort by title
				this.variants.sort(function(a, b) {
				  var nameA = a.title.toUpperCase(); // ignore upper and lowercase
				  var nameB = b.title.toUpperCase(); // ignore upper and lowercase
				  if (nameA < nameB) {
				    return -1;
				  }
				  if (nameA > nameB) {
				    return 1;
				  }
				  return 0;
				});
				
				
			}).finally(() => {
				this.endLoader();
			});
		},
		
		saveAll : function() {
			
			let promise = null;
			this.startLoader();
			for (let i = 0; i < this.variants.length; i++) {
				let variant = this.variants[i];
				if (promise == null) {
					promise = this.updateVariant(variant);
				} else {
					promise = promise.then(() => {
						return this.updateVariant(variant);
					});
				}
			}
			return promise.then(() => {
				console.log("all variants saved");
			}).finally(() => {
				this.endLoader();
				alert("Svi proizvodi spremljeni")
			});
			

			
		},
		
		updateVariant : function(variant) {
			if (variant.change == false) {
				return;
			}
			this.startLoader();
			return axios.post('/manager/products/variants/'+variant.id, variant).then(response => {
				console.log(response);
				variant.loadedPrice = variant.price;
				variant.change = false;
			}).catch(error => {
				this.showError(error.response.data.message);
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
	
	computed : {
		filtered : function() {
			if (this.filter.trim() == "" || this.filter == undefined || this.filter == null ) {
				return this.variants;
			}
			
			let filteredVariants = this.variants.filter(variant => {
				return variant.title.toLowerCase().includes(this.filter.toLowerCase());
			});
			return filteredVariants;
		}
	},
	
	mounted : function() {
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
			this.loadProducts();
		}).then(() => {
			this.endLoader();
		})
		
	}
});