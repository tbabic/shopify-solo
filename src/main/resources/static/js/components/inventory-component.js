var inventoryComponent = new Vue({
	el:"#inventory",
	data: {
		inventoryList : [],
		selectedInventoryItem : {
			id: null,
			item : '',
			quantity : 0,
			links: []
		},
		
		searchFilter : {
			value: ''
		},
		loadingCount: 0
	},
	methods: {
		loadInventory : function() {
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
				links : []
			}
			
			this.selectedInventoryItem.links.forEach(link => {
				inventoryToSave.links.push(link.value);
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
			
			
			this.selectedInventoryItem.links.splice(0, this.selectedInventoryItem.links.length);
			inventory.links.forEach(link => {
				this.selectedInventoryItem.links.push({
					value : link
				});
			});
		},
		cleanSelected : function() {
			this.selectedInventoryItem.id = null,
			this.selectedInventoryItem.item = '';
			this.selectedInventoryItem.quantity = 0;
			this.selectedInventoryItem.links = [];
		},
		newLink : function() {
			this.selectedInventoryItem.links.push({ value : 'link ovdje' });
		},
		removeLink : function(index) {
			this.selectedInventoryItem.links.splice(index, 1);
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
			return this.loadInventory();
		}).then(() => {
			this.endLoader();
		})
	}
});