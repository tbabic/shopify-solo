var createOrderComponent = new Vue({
	el:"#create-order",
	data: {
		shopifyOrder : {
			shipping_address : {
				country: "Croatia"
			},
			line_items : [],			
		},
		giveawayOrder : {

			shipping_address : {
				country: "Croatia"
			},
			line_items : [],
		},
		giveawayPlatform : null,
		type: "PAYMENT",
		productSearch : "",
		productList : [],
		customItem : {
			title : null,
			price : null,
			quantity : 1,
			
		},
		lineItemDiscount : {
			valueType : "",
			value : "",
			lineItem : null
		},
		giveawayItemName : "",
		debounceCounter: 0,
		loadingCount: 0,
		role : null
		
		
	},
	computed : {
		totalOrderPrice : function() {
			let sum = 0;
			this.shopifyOrder.line_items.forEach(lineItem => {
				sum+= lineItem.discountedPrice * lineItem.quantity
			});
			return sum;
		},
	},
	methods: {
		
		orderType: function(type) {
			this.type = type;
		},
		
		searchProduct: function() {
			
			if(this.productSearch.trim().length < 3) {
				this.productList.splice(0,this.productList.length);
				return;
			} 
			
			this.debounceCounter++;
			
			setTimeout(() => {
				if (this.debounceCounter > 1) {
					this.debounceCounter--;
					return;
				}
				
				let queryParams = {
					title : this.productSearch
				};
				
				let options = {
					params: queryParams
				};
				
				
				console.log("searchingProducts");
				axios.get('/manager/shopify-products', {
					params: queryParams
				}).then(response => {
					console.log("foundProducts");
					this.productList.splice(0,this.productList.length);
					response.data.forEach(product => {
						product.variants.forEach(variant => {
							if (variant.title == 'Default Title') {
								variant.title = product.title;
							} else {
								variant.title = product.title + " / " + variant.title;
							}
							this.productList.push(variant);
						});
						
						
					});
					if (this.productList.length > 10) {
						this.productList.splice(10,this.productList.length);
					}
					
					
				});
				this.debounceCounter--;
				executed = true;
			}, 300);
		},
		
		selectProduct : function(product) {
			let filtered = this.shopifyOrder.line_items.filter((lineItem) => {
				return lineItem.variant_id == product.id;
			});
			
			if (filtered.length < 1) {
				
				lineItem = {
					variant_id : product.id,
					quantity : 1,
					title : product.title,
					originalPrice : this.type == "PAYMENT" ? product.price: 0,
					discountedPrice : this.type == "PAYMENT" ? product.price: 0
				};
				if (this.type == "PAYMENT") {
						this.shopifyOrder.line_items.push({
						variant_id : product.id,
						quantity : 1,
						title : product.title,
						originalPrice : product.price,
						discountedPrice : product.price
					});
				} else {
					this.giveawayOrder.line_items.push({
						variant_id : product.id,
						quantity : 1,
						title : product.title,
						price : 0,
					});
				}
				
			} else {
				filtered[0].quantity++;
			}
			this.productList.splice(0,this.productList.length);
			this.productSearch = "";
		},
		
		removeLineItem : function(index) {
			if (this.type == "PAYMENT") {
				this.shopifyOrder.line_items.splice(index,1);
			} else {
				this.giveawayOrder.line_items.splice(index,1);
			}
		},
		
		
		setDiscountType : function(type) {
			this.lineItemDiscount.valueType = type;
		},
		
		selectLineItemDiscount : function(lineItem) {
			if (lineItem.applied_discount != undefined && lineItem.applied_discount != null) {
				this.lineItemDiscount.valueType = lineItem.applied_discount.value_type;
				this.lineItemDiscount.value= lineItem.applied_discount.value;
			} else {
				this.lineItemDiscount.valueType = "fixed_amount";
				this.lineItemDiscount.value= "";
			}
			this.lineItemDiscount.lineItem = lineItem;
		},
		
		applyDiscount : function() {
			let lineItem = this.lineItemDiscount.lineItem;
			
			if (this.lineItemDiscount.value.trim().length == 0 || this.lineItemDiscount.value== 0) {
				Vue.delete(lineItem, lineItem.applied_discount);
				lineItem.discountedPrice = lineItem.originalPrice;
			} else {
				Vue.set(lineItem, "applied_discount", {});
				lineItem.applied_discount.value_type = this.lineItemDiscount.valueType;
				lineItem.applied_discount.value = this.lineItemDiscount.value;
				let discountAmount = lineItem.applied_discount.value;
				if (lineItem.applied_discount.value_type == 'percentage') {
					discountAmount = 0.01*(+lineItem.applied_discount.value)*(+lineItem.originalPrice);
				}
				lineItem.discountedPrice = +lineItem.originalPrice - +discountAmount;
				
			}
		},
		
		addCustomItem : function() {
			let lineItem = {
					title : this.customItem.title,
					price : this.customItem.price,
					quantity : this.customItem.quantity,
					originalPrice : this.customItem.price,
					discountedPrice : this.customItem.price
			}
			
			this.customItem.title = null;
			this.customItem.price = null;
			this.customItem.quantity = 1;
			
			if(this.type == 'GIVEAWAY') {
				lineItem.price = 0;
				lineItem.originalPrice = 0;
				lineItem.discountedPrice = 0;
				this.giveawayOrder.line_items.push(lineItem);
			}
			
			this.shopifyOrder.line_items.push(lineItem);
		},
		
		saveShopifyOrder : function() {
			if (this.role == 'ROLE_SMC_MANAGER') {
				return;
			}
			this.startLoader();
			return axios.post('/manager/orders/create-shopify-order', this.shopifyOrder).then(response => {
				console.log(response);
				alert("Narudžba uspješno napravljena");
				this.shopifyOrder = {
					shipping_address : {
						
					},
					line_items : []			
				};
				
			}).catch(error => {
				this.showError(error.response.data.message);
			}).finally(() => {
				this.endLoader();
			});
		},
		
		saveGiveawayOrder : function() {
			if (this.role == 'ROLE_SMC_MANAGER') {
				return;
			}			
			this.startLoader();
			return axios.post('/manager/orders/create-shopify-giveaway', this.giveawayOrder, {
				params : {
					giveawayPlatform : this.giveawayPlatform
				}
			}).then(response => {
				console.log(response);
				alert("Giveaway uspješno napravljen");
				this.endLoader();
			}).catch(error => {
				this.endLoader();
				this.showError(error.response.data.message);
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