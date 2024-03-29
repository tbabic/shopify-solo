var newInventoryComponent = new Vue({
	el:"#new-inventory",
	data: {
		
		
		
		products : [],
		
		parts : [],
		
		partsFilterView : "",
		
		filterPartsViewByProductsName : true,
		
		partsFilterModal : "",
		
		dropdownPartsFilterFocused : false,
		
		modalViewProducts : true,
		
		globalViewProducts : true,
		
		modal : {
			originalObject : null,
		},
		
		changeDetection : false,
		
		selectedProduct : {
			name : '',
			quantity : 0,
			webshopInfo : {
				id : null,
				quantity : null,
				status : null
			},
			partDistributions: []
		},
		
		addingPart : {
			title : null,
			description : null,
			link : null,
			alternativeDescription : null,
			alternativeLink : null,
			alternativeDescription2 : null,
			alternativeLink2 : null,
			partsUsed : 2,
			assignedQuantity : 0,
			assignedToProducts : 0,
			totalQuantity : 0,
			freeForProducts : 0,
			disabled : false,
			optional : false,
		},
		
		selectedPart : {
			id : null,
			title : null,
			description : null,
			link : null,
			alternativeDescription : null,
			alternativeLink : null,
			alternativeDescription2 : null,
			alternativeLink2 : null,
			quantity : null,
			assignedQuantity : 0,
			spareQuantity : 0,
			partDistributions : [],
			optional : false
		},
		
		spinning : [],
		
		searchFilter : {
			value: '',
		},
		
		
		productsFilter : {
			value : "",
			filtered : []
		},
		
		sorting : [{
			value: "normal",
			text: "Normalno",
		}, {
			value: "alphabet",
			text: "Abeceda"
		},{
			value: "quantity.webshop",
			text: "Webshop količina"
		},{
			value: "quantity.inventory",
			text: "Zalihe količina"
		}],
		
		selectedSorting : "alphabet",
		
		connectionFilters : {
			connected : true,
			webshopOnly: true,
			materialsOnly: true,
			activeOnly : false
		},
		
		
		
		loadingCount: 0,
		debounceCounter: 0,
		
		confirmationModal : {
			content : "",
			promise : null,
			confirmButton : {
				content : "Da",
				style: "btn btn-success"
			},
			cancelButton : {
				content : "Ne",
				style: "btn btn-danger"
			},
		}
	},
	
	computed : {
		filteredProducts : function() {
			
			let filtered;
			
			console.log("sorting " + this.selectedSorting);
			if ((this.searchFilter == null || this.searchFilter.value.trim().length =='')
				&& this.connectionFilters.connected && this.connectionFilters.webshopOnly && this.connectionFilters.materialsOnly && !this.connectionFilters.activeOnly) {
				filtered = this.products.filter(() => true);
			} else {
				filtered = this.products.filter(product => {
					
					let b= product.name != null && product.name.toLowerCase().includes(this.searchFilter.value.toLowerCase().trim());
					
					let connected = product.id != null && product.webshopInfo != null && product.webshopInfo.id != null;
					let webshopOnly = product.id == null && product.webshopInfo != null && product.webshopInfo.id != null;
					let materialsOnly = product.id != null && (product.webshopInfo == null || product.webshopInfo.id == null);
					
					if (this.connectionFilters.activeOnly && ( product.webshopInfo == null || product.webshopInfo.status != 'active')) {
						return false;
					}
					
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
			else if (this.selectedSorting == "alphabet") {
				filtered.sort((a, b) => { 
					if (a.name < b.name) {
						return -1;
					} else if (a.name > b.name) {
						return 1;
					}
					return 0;
				 } );
			}
			else if (this.selectedSorting == "quantity.webshop") {
				filtered.sort((a, b) => { 
					if (a.shopifyQuantity != b.shopifyQuantity) {
						return a.shopifyQuantity - +b.shopifyQuantity;
					}
					else {
						return a.quantity - +b.quantity;
					}
				 } );
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
				 });
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
		
		filterActiveOnlyClass : function() {
			let style = "btn btn-secondary";
			if (this.connectionFilters.activeOnly == false) {
				style += " strikethrough";
			}
			return style;
		},
		
		filteredPartsView : function() {
			if (this.partsFilterView == null || this.partsFilterView.trim().length ==0) {
				return this.parts;
			}
			return this.parts.filter(part => {
				let filteringValue = this.partsFilterView.toLowerCase().trim();
				let valuesToFilter = [part.title, 
				part.description, part.link, 
				part.alternativeDescription, part.alternativeLink, 
				part.alternativeDescription2, part.alternativeDescription2];
				if (this.filterPartsViewByProductsName) {
					part.partDistributions.forEach(d => {
					valuesToFilter.push(d.productName);
				});
				}
				
				for (let i = 0; i < valuesToFilter.length; i++) {
					if (valuesToFilter[i] != null && valuesToFilter[i].toLowerCase().includes(filteringValue)) {
						return true;
					}
				}
				return false;
			});
		},
		
		filteredPartsModal : function() {
			if (this.partsFilterModal == null || this.partsFilterModal.trim().length < 3) {
				return [];
			}
			fp =  this.parts.filter(part => {
				
				let existingPart = this.selectedProduct.partDistributions.find(d => d.productPart.id == part.id)
				if (existingPart) {
					return false;
				}
				
				let filteringValue = this.partsFilterModal.toLowerCase().trim();
				let valuesToFilter = [part.title, 
				part.description, part.link, 
				part.alternativeDescription, part.alternativeLink, 
				part.alternativeDescription2, part.alternativeDescription2];
				for (let i = 0; i < valuesToFilter.length; i++) {
					if (valuesToFilter[i] != null && valuesToFilter[i].toLowerCase().includes(filteringValue)) {
						return true;
					}
				}
				return false;
			});
			fp = fp.slice(0,5);
			return fp;
		},
		
		filteredPartsModalEmpty : function() {
			let filtered = this.filteredPartsModal;
			return filtered.length == 0;
		},
		
		isPartsDropdownFocused : function() {
			return this.dropdownPartsFilterFocused;
		},
	},
	
	methods: {
		
		changeView : function() {
			this.globalViewProducts = !this.globalViewProducts;
			//return this.loadAllData();
		},
		
		changeDetectionYes : function() {
			this.changeDetection = true;
		},
		
		partsDropdownFocus : function() {
			this.dropdownPartsFilterFocused = true;
		},
		
		partsDropdownFocusOut : function(event) {
			if (event != null && event.relatedTarget != null) {
				if($(event.relatedTarget).closest(event.currentTarget).length > 0) {
					return;
				}
			}
			
			this.dropdownPartsFilterFocused = false;
			
		},
		
		adjustLeftDisabled : function(product) {
			if (this.isSpinning(product)) {
				return true;
			}
			if (product.adjustment.quantity == 0) {
				return true;
			}
			if (product.adjustment.quantity > product.maxFreeAssignments) {
				return true;
			}
			
			return false;
		},
				
		adjustLeft : function(inventoryItem) {			
			let adjustment = {
				productId : inventoryItem.adjustment.productId,
				quantity : inventoryItem.adjustment.quantity
			}
			
			this.adjust(inventoryItem, adjustment);
		},
		
		adjustRightDisabled : function(product) {
			return this.isSpinning(product) || product.adjustment.quantity == 0 ||  product.webshopInfo == null || product.webshopInfo.quantity <= 0;
		},
		
		adjustRight : function(inventoryItem) {
			let adjustment = {
				productId : inventoryItem.adjustment.productId,
				quantity : -inventoryItem.adjustment.quantity
			}
			
			this.adjust(inventoryItem, adjustment);
			
		},
		
		adjust : function(product, adjustment) {
			this.startSpinning(product);
			let relatedProducts = this.findAllRelatedProducts(product);
			relatedProducts.forEach(p => {
				this.startSpinning(p);
			});
			
			
			axios.post('/manager/inventory/move-quantity', adjustment).then(response => {
				
				let query = {
					productIds : relatedProducts.map(p => p.id)
				}
				
				return axios.post('/manager/inventory/query', query).then(response => {
					response.data.products.forEach(p => this.replaceProduct(p));
					response.data.products.forEach(p => this.stopSpinning(p));
				});
			}).catch(error => {
				this.showError(error.response.data.message);
			})
			.finally(() => {
				
			});
		},
		
		refreshProduct : function(productId) {
			return axios.get('/manager/inventory/products/' + productId)
			.then(response => {
				let product = response.data;
				this.replaceProduct(product);
				this.stopSpinning(product);			
			});
		},
		
		replaceProduct : function(product) {
			let index = this.products.findIndex(p => p.id == product.id);
			this.products.splice(index, 1);
			this.products.push(product);
			Vue.set(product, "adjustment", {
				productId : product.id,
				quantity : 0,
			});
		},
		
		findAllRelatedProducts : function(product) {
			let parts = product.partDistributions.map(d => d.productPart);
			let productIds = parts.flatMap(p => p.partDistributions.map(d => d.productId));
			let relatedProducts = this.products.filter(p => productIds.indexOf(p.id) >= 0);
			return relatedProducts;
			
			
		},
		
		startSpinning(product) {
			if (this.isSpinning(product)) {
				return;
			}
			this.spinning.push(product);
		},
		
		stopSpinning(product) {
			index = this.spinning.findIndex(el => el == product || el.id == product.id);
			if (index >= 0) {
				this.spinning.splice(index,1);
			}
		},
		
		isSpinning(product) {
			index = this.spinning.findIndex(el => el == product || el.id == product.id);
			return index >= 0;
				
		},
		
		copyProduct : function() {
			this.selectedProduct.id = null;
			this.selectedProduct.webshopInfo.id = null;
			this.selectedProduct.webshopInfo.quantity = null;
			this.selectedProduct.webshopInfo.status = null;
		},
		
		saveProduct : function() {
			this.startLoader();

			if(this.selectedProduct.id == null) {
				delete this.selectedProduct.id;
			}

			return axios.post('/manager/inventory/products', this.selectedProduct).then(response => {
				console.log(response);
			}).catch(error => {
				this.showError(error.response.data.message);
			}).finally(() => {
				this.loadAllData();
			}).finally(() => {
				this.endLoader();
			});

		},
		deleteProduct : function(product) {
			this.startSpinning(product);
			
			
			this.showError("not-implemented");
			
			this.stopSpinning(product);
		},
		
		selectProductById(productId) {
			let product = this.products.find(p => p.id == productId);
			this.select(product);
		},
		
		
		select : function(product) {
			this.partsFilterModal = "";
			
			
			this.selectedProduct.id = product.id;
			this.selectedProduct.name = product.name;
			this.selectedProduct.quantity = product.quantity;
			this.selectedProduct.webshopInfo.id = product.webshopInfo.id;
			this.selectedProduct.webshopInfo.quantity = product.webshopInfo.quantity;
			this.selectedProduct.webshopInfo.status = product.webshopInfo.status;
			
			
			this.selectedProduct.partDistributions.splice(0);
			if (product.partDistributions != null) {
				product.partDistributions.forEach(distribution => {
					this.selectedProduct.partDistributions.push(JSON.parse(JSON.stringify(distribution)));
				});
			}
			
			this.modal.originalObject = product;
			this.changeDetection = false;
			this.modalViewProducts = true;
			
		},
		cleanSelected : function() {
			delete this.selectedProduct.id;
			this.selectedProduct.name = '';
			this.selectedProduct.quantity = 0;
			this.selectedProduct.webshopInfo.id = null;
			this.selectedProduct.webshopInfo.quantity = null;
			this.selectedProduct.webshopInfo.status = null;
			this.selectedProduct.partDistributions = [];
			
			this.clearFilterProducts();
		},
		
		addNewPart : function() {
			delete this.selectedPart.id;
			this.selectedPart.title = null;
			this.selectedPart.description = null;
			this.selectedPart.link = null;
			this.selectedPart.alternativeDescription = null;
			this.selectedPart.alternativeLink = null;
			this.selectedPart.alternativeDescription2 = null;
			this.selectedPart.alternativeLink2 = null;
			this.selectedPart.quantity = 0;
			this.selectedPart.assignedQuantity = 0;
			this.selectedPart.spareQuantity = 0;
			this.selectedPart.optional = false;
			
			this.selectedPart.partDistributions.splice(0);

			this.changeDetection = false;
			this.modal.originalObject = null;
			
			this.modalViewProducts = false;
		},
		
		clearAddingPart : function() {
			this.addingPart.disabled = false;
			delete this.addingPart.id;
			this.addingPart.title = null;
			this.addingPart.description = null;
			this.addingPart.link = null;
			this.addingPart.alternativeDescription = null;
			this.addingPart.alternativeLink = null;
			this.addingPart.alternativeDescription2 = null;
			this.addingPart.alternativeLink2 = null;
			this.addingPart.partsUsed = 2;
			this.addingPart.assignedQuantity = 0;
			this.addingPart.assignedToProducts = 0;
			this.addingPart.totalQuantity = 0;
			this.addingPart.freeForProducts = 0;
		},
		
		logTest : function() {
			console.log("test");
		},
		
		connectProductAndPart : function(part) {
			this.dropdownPartsFilterFocused = false;
			this.partsFilterModal = "";
			
			console.log("connecting product and part");
			this.addingPart.disabled = true;
			this.addingPart.id = part.id;
			this.addingPart.title = part.title;
			this.addingPart.description = part.description;
			this.addingPart.link = part.link;
			this.addingPart.alternativeDescription = part.alternativeDescription;
			this.addingPart.alternativeDescription2 = part.alternativeDescription2;
			this.addingPart.alternativeLink = part.alternativeLink;
			this.addingPart.alternativeLink2 = part.alternativeLink2;
			this.addingPart.partsUsed = 2;
			this.addingPart.assignedQuantity = 0;
			this.addingPart.assignedToProducts = 0;
			this.addingPart.totalQuantity = part.quantity;
			this.addingPart.optional = part.optional;
			
			let sumTotalAssignedQuantity = 0;
			part.partDistributions.forEach(d => {
				sumTotalAssignedQuantity += d.assignedQuantity;
			});
			
			
			this.addingPart.freeForProducts = Math.floor((this.addingPart.totalQuantity - sumTotalAssignedQuantity) / this.addingPart.partsUsed);
		},
		
		addNewPartToProduct : function() {
			let newPart = {
				title : this.addingPart.title,
				description : this.addingPart.description,
				link : this.addingPart.link,
				alternativeDescription : this.addingPart.alternativeDescription,
				alternativeLink : this.addingPart.alternativeLink,
				alternativeDescription2 : this.addingPart.alternativeDescription2,
				alternativeLink2 : this.addingPart.alternativeLink2,
				quantity : +this.addingPart.partsUsed * (+this.addingPart.freeForProducts + +this.addingPart.assignedToProducts),
				optional : false
			};
			
			let existingAssignedQuantity = 0;
			let existingPart = this.parts.find(part => part.id == this.addingPart.id);
			
			if (existingPart != null) {
				newPart.id = existingPart.id;
				newPart.title = existingPart.title;
				newPart.description = existingPart.description;
				newPart.link = existingPart.link;
				newPart.alternativeDescription = existingPart.alternativeDescription;
				newPart.alternativeLink = existingPart.alternativeLink;
				newPart.alternativeDescription2 = existingPart.alternativeDescription2;
				newPart.alternativeLink2 = existingPart.alternativeLink2;
				newPart.quantity = existingPart.quantity;
				newPart.optional = existingPart.optional;
				
				existingPart.partDistributions.forEach(d => {
					existingAssignedQuantity+= +d.assignedQuantity;
				});
			}
			
			
			
			
			
			this.selectedProduct.partDistributions.push({
				productPart : newPart,
				partsUsed : this.addingPart.partsUsed,
				assignedQuantity : (+this.addingPart.assignedToProducts + +existingAssignedQuantity)*(+this.addingPart.partsUsed),
				assignedToProducts : this.addingPart.assignedToProducts,
				freeForProducts : this.addingPart.freeForProducts,
			});
			
			this.addingPart.disabled = false;
			
			this.addingPart.id = null;
			this.addingPart.title = null;
			this.addingPart.description = null;
			this.addingPart.link = null;
			this.addingPart.alternativeDescription = null;
			this.addingPart.alternativeLink = null;
			this.addingPart.alternativeDescription2 = null;
			this.addingPart.alternativeLink2 = null;
			this.addingPart.assignedQuantity = 0;
			this.addingPart.assignedToProducts = 0;
			this.addingPart.totalQuantity = 0;
			this.addingPart.freeForProducts = 0;
			this.addingPart.partsUsed = 2;
			
			this.changeDetection = true;
		},
		
		removePart : function(index) {
			this.selectedProduct.partDistributions.splice(index, 1);
			
			this.changeDetection = true;
		},
		
		refreshDistribution : function(distribution) {
			distribution.assignedQuantity = distribution.assignedToProducts*distribution.partsUsed;
			if (this.modalViewProducts) {
				distribution.freeForProducts = Math.floor((distribution.productPart.quantity - distribution.assignedQuantity) / distribution.partsUsed);
			} else {
				distribution.freeForProducts = Math.floor((this.selectedPart.quantity - distribution.assignedQuantity) / distribution.partsUsed);
				let totalAssigned = 0;
				this.selectedPart.partDistributions.forEach((distro) => {
					totalAssigned += distro.assignedQuantity;
				});
				this.selectedPart.spareQuantity=this.selectedPart.quantity-totalAssigned;
			}
			this.changeDetection = true;
		},
		
		partQuantityChanged : function() {
			let totalAssigned = 0;
			this.selectedPart.partDistributions.forEach((distro) => {
				distro.freeForProducts = Math.floor((this.selectedPart.quantity - distro.assignedQuantity) / distro.partsUsed);
				totalAssigned += distro.assignedQuantity;
			});
			this.selectedPart.spareQuantity=this.selectedPart.quantity-totalAssigned;
			
			this.changeDetection = true;
		},
		
		selectPart : function(part) {
			this.selectedPart.id = part.id;
			this.selectedPart.title = part.title;
			this.selectedPart.description = part.description;
			this.selectedPart.link = part.link;
			this.selectedPart.alternativeDescription = part.alternativeDescription;
			this.selectedPart.alternativeLink = part.alternativeLink;
			this.selectedPart.alternativeDescription2 = part.alternativeDescription2;
			this.selectedPart.alternativeLink2 = part.alternativeLink2;
			this.selectedPart.quantity = part.quantity;
			this.selectedPart.assignedQuantity = part.assignedQuantity;
			this.selectedPart.spareQuantity = part.spareQuantity;
			this.selectedPart.optional = part.optional;
			
			this.selectedPart.partDistributions.splice(0);
			part.partDistributions.forEach(distribution => {
				this.selectedPart.partDistributions.push(JSON.parse(JSON.stringify(distribution)));
			});
			
			this.changeDetection = false;
			this.modal.originalObject = part;
			
			this.modalViewProducts = false;
		},
		
		savePart : function() {
			this.startLoader();

			if(this.selectedPart.id == null) {
				delete this.selectedPart.id;
			}
			
			
			
			let productPartAndDistribution = {
				productPart: this.selectedPart,
				distributions : this.selectedPart.partDistributions
			};

			return axios.post('/manager/inventory/parts', productPartAndDistribution).then(response => {
				console.log(response);
			}).catch(error => {
				this.showError(error.response.data.message);
			}).finally(() => {
				this.loadAllData();
			}).finally(() => {
				this.endLoader();
			});

		},
		
		changeCheck : function(action) {
			if (this.changeDetection) {
				this.confirmationModal.content="Sve promjene će biti izgubljene. Želite li nastaviti?";
				this.confirmationModal.confirmButton.content="Nastavi";
				this.confirmationModal.confirmButton.style="btn btn-success";
				this.confirmationModal.cancelButton.content="Vrati me";
				this.confirmationModal.cancelButton.style="btn btn-danger";
				this.confirmAction(action);
			} else {
				action();
			}
			
		},
		
		cancelCheck : function(action) {
			if (this.changeDetection) {
				this.confirmationModal.content="Sve promjene će biti izgubljene. Želite li otkazati promjene?";
				this.confirmationModal.confirmButton.content="Otkaži";
				this.confirmationModal.confirmButton.style="btn btn-success";
				this.confirmationModal.cancelButton.content="Vrati me";
				this.confirmationModal.cancelButton.style="btn btn-danger";
				this.confirmAction(action);
			} else {
				action();
			}
		},
		
		saveCheck : function(action) {
			this.confirmationModal.content="Želite li stvarno spremiti podatke. Ova akcija se ne može poništiti";
			this.confirmationModal.confirmButton.content="Spremi";
			this.confirmationModal.confirmButton.style="btn btn-success";
			this.confirmationModal.cancelButton.content="Ne";
			this.confirmationModal.cancelButton.style="btn btn-danger";
			this.confirmAction(action);
		},
		
		confirmAction : function(action) {
			$('#confirmModal').modal('show');
			
			this.confirmationModal.promise = Promise.resolve(action);
		},
		
		confirm : function() {
			this.confirmationModal.promise.then((action) => {
				let promise = action();
				if (promise != null && promise.then != null) {
					promise.then(() => {
						$('#editInventoryModal').modal('hide');
					});
				}
			})
		},
		
		cancelConfirm : function() {
			
		},
		
		dismissModal : function() {
			$('#editInventoryModal').modal('hide');
		},
		
		loadViewData: function() {
			if (this.globalViewProducts) {
				return this.loadProducts();
			} else {
				return this.loadParts();
			}
		},
		
		loadAllData: function() {
			return this.loadProducts().then(()=> {
				return this.loadParts();
			});
		
		},
		
		loadParts : function() {
			this.startLoader();
			return axios.get('/manager/inventory/parts', )
			.then(response => {
				console.log("foundParts");
				this.parts.splice(0,this.parts.length);
				response.data.forEach(part => {
					
					this.parts.push(part);
				});
				
				
			}).finally(() => {
				this.endLoader();
			});
		},
		
		loadProducts : function() {
			this.startLoader();
			return axios.get('/manager/inventory/products', {
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
					
					Vue.set(product, "adjustment", {
						productId : product.id,
						quantity : 0,
					});
					
					this.products.push(product);
				});
				
				
			}).finally(() => {
				this.endLoader();
			});
		},
		
		filterProducts : function() {
			this.productsFilter.filtered.splice(0,this.productsFilter.filtered.length);
			if (this.selectedProduct.name == null || this.selectedProduct.name.trim().length < 3 ) {
				return;
			}
			
			
			
			let filteredProducts = this.products.filter(product => {
				return product.id == null && product.name.toLowerCase().includes(this.selectedProduct.name.toLowerCase());
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
			this.selectedProduct.name = product.name;
			this.selectedProduct.webshopInfo.id = product.webshopInfo.id;
			this.selectedProduct.webshopInfo.quantity = product.webshopInfo.quantity;
			this.selectedProduct.webshopInfo.status = product.webshopInfo.status;
			this.productsFilter.filtered.splice(0);
		},
		
		disconnect : function() {
			this.selectedProduct.webshopInfo.id  = null;
			this.selectedProduct.webshopInfo.quantity  = null;
			this.selectedProduct.webshopInfo.status  = null;
			this.changeDetection = true;
			
		},
		
		connectedClass:function(product) {
			if (product.id == null) {
				return 'btn btn-warning';
			}
			if (product.webshopInfo == null || product.webshopInfo.id == null) {
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
			if (this.loadingCount == 0) {
				console.log("loader started");
			}
			this.loadingCount++;
			if (this.loadingCount > 0) {
				document.getElementById("overlay").style.display = "flex";
			}
			
		},
		endLoader : function() {
			this.loadingCount--;
			if (this.loadingCount <= 0) {
				document.getElementById("overlay").style.display = "none";
				console.log("loader finished");
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
			return axios.post('/manager/inventory/sync-products');
		}).then(() => {
			return this.loadProducts();
		}).then(() => {
			return this.loadParts();
		}).then(() => {
			this.endLoader();
		})
	}
});