var textRecordsComponent = new Vue({
	el:"#text-records",
	data: {
		categories : [],
		textRecords : [],
		selectedRecord : {
			id: null,
			value : '',
			category : '',
			title: '',
			extra: ''
		},
		editingCategory : {
			oldCategory : null,
			newCategory : null
		},
		selectedCategory : '',
		loadingCount: 0
	},
	methods: {
		loadAllCategories : function() {
			this.startLoader();
			return axios.get('/manager/texts/categories').then(response => {
				this.categories.splice(0,this.categories.length);
				response.data.forEach(category => { 
					this.categories.push(category);
				});
			}).finally(() => {
				this.endLoader();
			});
		},
		loadRecordsForCategory : function(selectedCategory) {
			this.startLoader();
			this.selectedCategory = selectedCategory;
			return axios.get('/manager/texts/records', {
				params : {
					category : selectedCategory
				}
			}).then(response => {
				this.textRecords.splice(0,this.textRecords.length);
				response.data.forEach(textRecord => { 
					this.textRecords.push(textRecord);
				});
			}).finally(() => {
				this.endLoader();
			});
		},
		editCategory : function() {
			this.editingCategory.newCategory = this.selectedCategory;
			this.editingCategory.oldCategory = this.selectedCategory;
		},
		saveCategory : function() {
			this.startLoader();
			return axios.post('/manager/texts/categories', this.editingCategory).then(response => {
				console.log(response);
				this.selectedCategory = this.editingCategory.newCategory;
				this.editingCategory.newCategory = '';
				this.editingCategory.oldCategory = '';
			}).then(() => {
				return this.loadRecordsForCategory(this.selectedCategory);
			}).then(()=>{
				return this.loadAllCategories();
			}).catch(error => {
				this.showError(error.response.data.message);
			}).finally(() => {
				this.endLoader();
			});
		},
		selectRecord : function(record) {
			this.selectedRecord.id = record.id;
			this.selectedRecord.value = record.value;
			this.selectedRecord.category = record.category;
			this.selectedRecord.title = record.title;
			this.selectedRecord.extra = record.extra;
		},
		cleanSelectedRecord : function() {
			this.selectedRecord.id = null;
			this.selectedRecord.value = '';
			this.selectedRecord.category = '';
			this.selectedRecord.title = '';
			this.selectedRecord.extra = '';
		},
		saveSelectedRecord : function() {
			this.startLoader();
			return axios.post('/manager/texts/records', this.selectedRecord).then(response => {
				console.log(response);
			}).then(() => {
				return this.loadRecordsForCategory(this.selectedCategory);
			}).then(()=>{
				return this.loadAllCategories();
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
			this.loadAllCategories();
		}).then(() => {
			this.endLoader();
		})
		
		
		
	}
});