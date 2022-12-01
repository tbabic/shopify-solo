var inventoryComponent = new Vue({
	el:"#inventory",
	data: {
		
		variants : [],
		
		inventoryList : [],
		
		selectedInventoryItem : {
			id: null,
			item : '',
			quantity : 0,
			shopifyVariantId : null,
			links: []
		},
		
		spinning : [],
		
		searchFilter : {
			value: ''
		},
		
		connectionFilters : {
			connected : true,
			webshopOnly : true,
			materialsOnly : true
		},
		
		variantsFilter : {
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
		
		loadingCount: 0,
		debounceCounter: 0
	},
	
	computed : {
		filteredInventory : function() {
			
			let filtered;
			
			console.log("sorting " + this.selectedSorting);
			if ((this.searchFilter == null || this.searchFilter.value.trim().length =='')
				&& this.connectionFilters.connected && this.connectionFilters.webshopOnly && this.connectionFilters.materialsOnly) {
				filtered = this.inventoryList.filter(() => true);
			} else {
				filtered = this.inventoryList.filter(inventory => {
					
					let b= inventory.item != null && inventory.item.toLowerCase().includes(this.searchFilter.value.toLowerCase().trim());
					
					let connected = inventory.id != null && inventory.shopifyVariantId != null;
					let webshopOnly = inventory.id == null && inventory.shopifyVariantId != null;
					let materialsOnly = inventory.id != null && inventory.shopifyVariantId == null;
					
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
		
		loadInventory : function() {
			if (this.role == 'ROLE_SMC_MANAGER') {
				return;
			}
			this.startLoader();
			
			params = {
				search : this.searchFilter.value
			};
			
			return axios.get('/manager/inventory').then(response => {
				this.inventoryList.splice(0,this.inventoryList.length);
				response.data.forEach(inventory => { 
					this.inventoryList.push(inventory);
				});
				
				this.inventoryList.forEach(inventory => {
					Vue.set(inventory, "adjustment", {
						inventoryId : inventory.id,
						shopifyInventoryItemId : null,
						quantity : 0
					});
					Vue.set(inventory, "oldQuantity", inventory.quantity);
				})
				
				this.variants.forEach(variant => {
					let foundInventory = this.inventoryList.find( inventoryItem => inventoryItem.shopifyVariantId == variant.id) 
					if (foundInventory == null){
						this.inventoryList.push({
							id: null,
							item : variant.title,
							quantity : 0,
							oldQuantity : 0,
							shopifyVariantId : variant.id,
							shopifyQuantity : +variant.inventory_quantity,
							links: [],
							adjustment : {
								inventoryId : null,
								shopifyInventoryItemId : variant.inventory_item_id,
								quantity : 0
							}
						});
					} else {
						Vue.set(foundInventory, "shopifyQuantity", +variant.inventory_quantity);
						Vue.set(foundInventory.adjustment, "shopifyInventoryItemId", variant.inventory_item_id);
					}
					
					
					
				});
				
				
				
				
			}).finally(() => {
				this.endLoader();
			});
		},
		
		
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
		
		adjust : function(inventoryItem, adjustment) {
			this.startSpinning(inventoryItem);
			
			return axios.post('/manager/inventory/move-quantity', adjustment).then(response => {
				inventoryItem.quantity -= adjustment.quantity;
				inventoryItem.oldQuantity -= adjustment.quantity;
				inventoryItem.shopifyQuantity +=+adjustment.quantity;
				if (inventoryItem.quantity < 0) {
					inventoryItem.quantity = 0;
				}
				console.log(response);
			}).catch(error => {
				this.showError(error.response.data.message);
			}).finally(() => {
				this.stopSpinning(inventoryItem)
			});
		},
		
		quickSave : function(inventoryItem) {
			this.startSpinning(inventoryItem);
			
			
			let inventoryToSave = {
				id : inventoryItem.id,
				item : inventoryItem.item,
				quantity : inventoryItem.quantity,
				shopifyVariantId : inventoryItem.shopifyVariantId,
				links : []
			}
			
			inventoryItem.links.forEach(link => {
				inventoryToSave.links.push(link);
			});
			
			
			return axios.post('/manager/inventory', inventoryToSave).then(response => {
				console.log(response);
				inventoryItem.oldQuantity = inventoryItem.quantity;
			}).catch(error => {
				this.showError(error.response.data.message);
			})
			.finally(() => {
				this.stopSpinning(inventoryItem);
			});
		},
		
		quantityChanged(inventory) {
			return inventory.oldQuantity != inventory.quantity;
		},
		
		startSpinning(inventoryItem) {
			if (this.isSpinning(inventoryItem)) {
				return;
			}
			this.spinning.push(inventoryItem);
		},
		
		stopSpinning(inventoryItem) {
			index = this.spinning.findIndex(el => el == inventoryItem);
			if (index >= 0) {
				this.spinning.splice(index,1);
			}
		},
		
		isSpinning(inventoryItem) {
			index = this.spinning.findIndex(el => el == inventoryItem);
			return index >= 0;
				
		},
		
		copyInventory : function() {
			this.selectedInventoryItem.id = null;
			this.selectedInventoryItem.shopifyVariantId = null;
		},
		
		saveInventory : function() {
			this.startLoader();
			
			let inventoryToSave = {
				id : this.selectedInventoryItem.id,
				item : this.selectedInventoryItem.item,
				quantity : this.selectedInventoryItem.quantity,
				shopifyVariantId : this.selectedInventoryItem.shopifyVariantId,
				links : []
			}
			
			this.selectedInventoryItem.links.forEach(link => {
				inventoryToSave.links.push(link);
			});
			
			
			return axios.post('/manager/inventory', inventoryToSave).then(response => {
				console.log(response);
			}).then(() => {
				return this.loadInventory();
			}).catch(error => {
				this.showError(error.response.data.message);
			}).finally(() => {
				this.endLoader();
			});
		},
		deleteInventory : function(inventory) {
			if(inventory.id == null) {
				return;
			}
			this.startLoader();

			return axios.delete('/manager/inventory/' + inventory.id)
			.then(response => {
				console.log(response);
			}).then(() => {
				return this.loadInventory();
			}).catch(error => {
				this.showError(error.response.data.message);
			}).finally(() => {
				this.endLoader();
			});
		},
		
		select : function(inventory) {
			this.selectedInventoryItem.id = inventory.id;
			this.selectedInventoryItem.item = inventory.item;
			this.selectedInventoryItem.quantity = inventory.quantity;
			this.selectedInventoryItem.shopifyVariantId = inventory.shopifyVariantId;
			
			
			this.selectedInventoryItem.links.splice(0, this.selectedInventoryItem.links.length);
			inventory.links.forEach(link => {
				this.selectedInventoryItem.links.push({
					name : link.name,
					link : link.link
				});
			});
		},
		cleanSelected : function() {
			this.selectedInventoryItem.id = null,
			this.selectedInventoryItem.item = '';
			this.selectedInventoryItem.quantity = 0;
			this.selectedInventoryItem.shopifyVariantId = null;
			this.selectedInventoryItem.links = [];
			
			this.clearFilterVariants();
		},
		newLink : function() {
			this.selectedInventoryItem.links.push({ value : 'link ovdje' });
		},
		removeLink : function(index) {
			this.selectedInventoryItem.links.splice(index, 1);
		},
		
		loadProducts : function() {
			this.startLoader();
			return axios.get('/manager/shopify-products/all')
			.then(response => {
				console.log("foundProducts");
				this.processResponse(response);
				
				
			}).finally(() => {
				this.endLoader();
			});
		},
		
		processResponse : function(response) {
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
					
					this.variants.push(variant);
				});
			});
		},
		
		
		filterVariants : function() {
			this.variantsFilter.filtered.splice(0,this.variantsFilter.filtered.length);
			if (this.selectedInventoryItem.item == null || this.selectedInventoryItem.item.trim().length < 3 ) {
				return;
			}
			
			
			
			let filteredVariants = this.variants.filter(variant => {
				return variant.title.toLowerCase().includes(this.selectedInventoryItem.item.toLowerCase());
			});
			
			filteredVariants.forEach( fv => this.variantsFilter.filtered.push(fv));
			this.variantsFilter.filtered.splice(10);
			return;
		},
		
		clearFilterVariants : function(event) {
			if (event != null && event.relatedTarget != null) {
				if($(event.relatedTarget).closest(event.currentTarget).length > 0) {
					return;
				}
			}
			this.variantsFilter.filtered.splice(0,this.variantsFilter.filtered.length);
			return;
		},
		
		connection : function(variant) {
			this.selectedInventoryItem.item = variant.title;
			this.selectedInventoryItem.shopifyVariantId = variant.id;
			this.variantsFilter.filtered.splice(0);
		},
		
		disconnect : function() {
			this.selectedInventoryItem.shopifyVariantId = null;
			
		},
		
		connectedClass:function(inventory) {
			if (inventory.id == null) {
				return 'btn btn-warning';
			}
			if (inventory.shopifyVariantId == null) {
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
			return this.loadInventory();
		}).then(() => {
			this.endLoader();
		})
	}
});