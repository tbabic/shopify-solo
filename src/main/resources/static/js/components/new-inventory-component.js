var newInventoryComponent = new Vue({
	el:"#new-inventory",
	data: {
		
		
		
		products : [],
		
		selectedProduct : {
			id: null,
			item : '',
			quantity : 0,
			webshopInfo : {
				id : null,
				quantity : null
			},
			partDistributions: []
		},
		
		spinning : [],
		
		searchFilter : {
			value: ''
		},
		
		
		productsFilter : {
			value : "",
			filtered : []
		},
		
		sorting : [{
			value: "normal",
			text: "Normalno",
		}, {
			value: "quantity.webshop",
			text: "Webshop količina"
		},{
			value: "quantity.inventory",
			text: "Zalihe količina"
		}],
		
		selectedSorting : "normal",
		
		connectionFilters : {
			connected : true,
			webshopOnly: true,
			materialsOnly: true
		},
		
		loadingCount: 0,
		debounceCounter: 0
	},
	
	computed : {
		filteredProducts : function() {
			
			let filtered;
			
			console.log("sorting " + this.selectedSorting);
			if ((this.searchFilter == null || this.searchFilter.value.trim().length =='')
				&& this.connectionFilters.connected && this.connectionFilters.webshopOnly && this.connectionFilters.materialsOnly) {
				filtered = this.products.filter(() => true);
			} else {
				filtered = this.products.filter(product => {
					
					let b= product.name != null && product.name.toLowerCase().includes(this.searchFilter.value.toLowerCase().trim());
					
					let connected = product.webshopInfo != null && product.webshopInfo.id != null;
					let webshopOnly = product.id == null && product.webshopInfo != null && product.webshopInfo.id != null;
					let materialsOnly = product.id != null && (product.webshopInfo == null || product.webshopInfo.id == null);
					
					if (connected && this.connectionFilters.connected) {
						return b;
					}
					
					if (webshopOnly && this.connectionFilters.webshopOnly) {
						return b;
					}
					
					if (materialsOnly && this.connectionFilters.materialsOnly) {
						return b;
					}
									
					return false;
					
				});
			}
			
			
				
			
			if(this.selectedSorting == "normal") {
				return filtered;
			}
			else if (this.selectedSorting == "quantity.webshop") {
				filtered.sort((a, b) => { 
					if (a.shopifyQuantity != b.shopifyQuantity) {
						return a.shopifyQuantity - +b.shopifyQuantity;
					}
					else {
						return a.quantity - +b.quantity;
					}
				 } )
			}
			else if (this.selectedSorting == "quantity.inventory") {
				filtered.sort((a, b) => {
					value = +a.shopifyQuantity + +a.quantity - +b.shopifyQuantity - +b.quantity;
					if (value == 0) {
						value = +a.shopifyQuantity - +b.shopifyQuantity
					}
					if (value == 0) {
						value = +a.quantity - +b.quantity;
					}
					return value;
				 })
			}
			
			return filtered;
		},
		
		filterConnectedClass : function() {
			let style = "btn btn-connected";
			if (this.connectionFilters.connected == false) {
				style += " strikethrough";
			}
			return style;
		},
		filterWebshopOnlyClass : function() {
			let style = "btn btn-warning";
			if (this.connectionFilters.webshopOnly == false) {
				style += " strikethrough";
			}
			return style;
		},
		filterMaterialsOnlyClass : function() {
			let style = "btn btn-danger";
			if (this.connectionFilters.materialsOnly == false) {
				style += " strikethrough";
			}
			return style;
		},
	},
	
	methods: {
				
		adjustLeft : function(inventoryItem) {			
			let adjustment = {
				inventoryId : inventoryItem.adjustment.inventoryId,
				shopifyInventoryItemId : inventoryItem.adjustment.shopifyInventoryItemId,
				quantity : inventoryItem.adjustment.quantity
			}
			
			this.adjust(inventoryItem, adjustment);

			
		},
		
		adjustRight : function(inventoryItem) {
			let adjustment = {
				inventoryId : inventoryItem.adjustment.inventoryId,
				shopifyInventoryItemId : inventoryItem.adjustment.shopifyInventoryItemId,
				quantity : -inventoryItem.adjustment.quantity
			}
			
			this.adjust(inventoryItem, adjustment);
			
		},
		
		adjust : function(product, adjustment) {
			this.startSpinning(product);
			
			
			this.showError("not-implemented");
			
			this.stopSpinning(product);
			
			
		},
		
		quickSave : function(product) {
			this.startSpinning(product);
			
			
			this.showError("not-implemented");
			
			this.stopSpinning(product);
			
		},
		
		quantityChanged(inventory) {
			return inventory.oldQuantity != inventory.quantity;
		},
		
		startSpinning(product) {
			if (this.isSpinning(product)) {
				return;
			}
			this.spinning.push(product);
		},
		
		stopSpinning(product) {
			index = this.spinning.findIndex(el => el == product);
			if (index >= 0) {
				this.spinning.splice(index,1);
			}
		},
		
		isSpinning(product) {
			index = this.spinning.findIndex(el => el == product);
			return index >= 0;
				
		},
		
		copyProduct : function() {
			this.selectedProduct.id = null;
			this.selectedProduct.webshopInfo.id = null;
			this.selectedProduct.webshopInfo.quantity = null;
		},
		
		saveProduct : function() {
			this.startSpinning(product);
			
			
			this.showError("not-implemented");
			
			this.stopSpinning(product);
		},
		deleteProduct : function(product) {
			this.startSpinning(product);
			
			
			this.showError("not-implemented");
			
			this.stopSpinning(product);
		},
		
		select : function(product) {
			this.selectedProduct.id = product.id;
			this.selectedProduct.item = product.name;
			this.selectedProduct.quantity = product.quantity;
			this.selectedProduct.webshopInfo.id = product.webshopInfo.id;
			
			
			this.selectedProduct.partDistributions.splice(0);
			if (product.partDistributions != null) {
				product.partDistributions.forEach(distribution => {
					this.selectedProduct.partDistributions.push(distribution);
				});
			}
			
		},
		cleanSelected : function() {
			this.selectedProduct.id = null,
			this.selectedProduct.item = '';
			this.selectedProduct.quantity = 0;
			this.selectedProduct.webshopInfo.id = null;
			this.selectedProduct.webshopInfo.quantity = null;
			this.selectedProduct.partDistributions = [];
			
			this.clearFilterProducts();
		},
		newPart : function() {
			this.selectedProduct.partDistributions.push({
				productId : this.selectedProduct.id,
				assignedQuantity : 0,
				productPart : {
					quantity : 0,
					description: null,
					link: null,
					alternativeLink: null,
					alternativeDescription: null,
					alternativeLink2: null,
					alternativeDescription2: null,
				}
			 });
		},
		removePart : function(index) {
			this.selectedProduct.partDistributions.splice(index, 1);
		},
		
		loadProducts : function() {
			this.startLoader();
			return axios.get('/manager/products', {
				params: {
					webshopInfo : true
				}
			})
			.then(response => {
				console.log("foundProducts");
				this.products.splice(0,this.products.length);
				response.data.forEach(product => {
					if(product.name.includes("POKLON")) {
						return;
					}
					this.products.push(product);
				});
				
				
			}).finally(() => {
				this.endLoader();
			});
		},
		
		filterProducts : function() {
			this.productsFilter.filtered.splice(0,this.productsFilter.filtered.length);
			if (this.selectedProduct.item == null || this.selectedProduct.item.trim().length < 3 ) {
				return;
			}
			
			
			
			let filteredProducts = this.products.filter(product => {
				return product.title.toLowerCase().includes(this.selectedProduct.item.toLowerCase());
			});
			
			filteredProducts.forEach( fv => this.productsFilter.filtered.push(fv));
			this.productsFilter.filtered.splice(10);
			return;
		},
		
		clearFilterProducts : function(event) {
			if (event != null && event.relatedTarget != null) {
				if($(event.relatedTarget).closest(event.currentTarget).length > 0) {
					return;
				}
			}
			this.productsFilter.filtered.splice(0,this.productsFilter.filtered.length);
			return;
		},
		
		connection : function(product) {
			this.selectedProduct.item = product.title;
			this.selectedProduct.webshopInfo.id = product.id;
			this.selectedProduct.webshopInfo.quantity = product.quantity;
			this.productsFilter.filtered.splice(0);
		},
		
		disconnect : function() {
			this.selectedProduct.webshopInfo.id  = null;
			this.selectedProduct.webshopInfo.quantity  = null;
			
		},
		
		connectedClass:function(inventory) {
			if (inventory.id == null) {
				return 'btn btn-warning';
			}
			if (inventory.shopifyProductId == null) {
				return 'btn btn-danger'
			}
			return "btn btn-connected";
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
			return this.loadProducts();
		}).then(() => {
			this.endLoader();
		})
	}
});