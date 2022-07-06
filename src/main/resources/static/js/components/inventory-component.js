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
		
		
		
		searchFilter : {
			value: ''
		},
		
		variantsFilter : {
			value : "",
			filtered : []
		},
		
		loadingCount: 0,
		debounceCounter: 0
	},
	
	computed : {
		filteredInventory : function() {
			if (this.searchFilter == null || this.searchFilter.value.trim().length =='') {
				return this.inventoryList;
			}
			let filtered = this.inventoryList.filter(inventory => {
				
				let b= inventory.item != null && inventory.item.toLowerCase().includes(this.searchFilter.value.toLowerCase().trim());
				return b;
				
			});
			return filtered;
		}
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
			
			return axios.get('/manager/inventory',{
				params: params
			}).then(response => {
				this.inventoryList.splice(0,this.inventoryList.length);
				response.data.forEach(inventory => { 
					this.inventoryList.push(inventory);
				});
				
				this.variants.forEach(variant => {
					let foundInventory = this.inventoryList.find( inventoryItem => inventoryItem.shopifyVariantId == variant.id) 
					if (foundInventory == null){
						this.inventoryList.push({
							id: null,
							item : variant.title,
							quantity : 0,
							shopifyVariantId : variant.id,
							shopifyQuantity : variant.inventory_quantity,
							links: []
						});
					} else {
						Vue.set(foundInventory, "shopifyQuantity", variant.inventory_quantity);
					}
				});
				
				
			}).finally(() => {
				this.endLoader();
			});
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
			return axios.get('/manager/products/all')
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
		
		clearFilterVariants : function() {
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
			return "btn btn-primary";
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