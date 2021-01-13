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

			type: "GIVEAWAY",
			contact: null,
			shippingInfo: {
				fullName: null,
				companyName: null,
				streetAndNumber: null,
				other: null,
				city: null,
				postalCode: null,
				country: "Croatia",
				phoneNumber: null
			},
			personalTakeover: false,
			items:[],
			note: null,
			giveawayPlatform: null
		},
		type: "PAYMENT",
		productSearch : "",
		productList : [],
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
				axios.get('/manager/products', {
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
				this.shopifyOrder.line_items.push({
					variant_id : product.id,
					quantity : 1,
					title : product.title,
					originalPrice : product.price,
					discountedPrice : product.price
				});
			} else {
				filtered[0].quantity++;
			}
			this.productList.splice(0,this.productList.length);
			this.productSearch = "";
		},
		
		removeLineItem : function(index) {
			this.shopifyOrder.line_items.splice(index,1);
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
		
		saveShopifyOrder : function() {
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
		
		addItemToGiveaway : function() {
			if (this.giveawayItemName == undefined || this.giveawayItemName == null || this.giveawayItemName.length < 1) {
				return;
			}
			
			let filtered = this.giveawayOrder.items.filter((item) => {
				return item.name == this.giveawayItemName;
			});
			
			if (filtered.length < 1) {
			
				this.giveawayOrder.items.push({
					name: this.giveawayItemName,
					quantity: 1,
					price: null
				});
			} else {
				filtered[0].quantity++;
			}
			this.giveawayItemName = "";
		},
		
		removeGiveawayItem : function(index) {
			this.giveawayOrder.items.splice(index,1);
		},
		
		saveGiveawayOrder : function() {			
			this.startLoader();
			return axios.post('/manager/orders/', this.giveawayOrder).then(response => {
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