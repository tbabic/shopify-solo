
var orderTableComponent = new Vue({
	el:"#searchProcedure",
	data: {
		filters : {
			paid : true,
			shippingSearchStatus : null,
			searchProcedureStatus : "FOR_SEARCH"
		},
		
		listFilters: {
			
			
		},
		
		procedureStatusList : ["NONE", "FOR_SEARCH","SEARCH_REQUESTED", "REFUND_REQUESTED","REFUND_APPROVED","REFUND_DENIED","REFUNDED", "COMPLETED"],
		
		shippingStatusList : ["SENT","UNKNOWN", "LOST", "DELIVERED", "RESENT", "RESENT_FIRST_DELIVERED", "RESENT_SECOND_DELIVERED", "RESENT_BOTH_DELIVERED"],
		
		searchFilter : {
			value : ""
		},
		
		searchDateId : null,
		searchDate : null,
		
		
		pagination: {
			page: 0,
			size: 50,
			startElement: 0,
			endElement: 0,
			totalElements: 0,
			totalPages: 0,
			isFirst: true,
			isLast: true
		},
		sorting : {
			sortBy: "id",
			direction: "ASC"
		},
		orders: [],
		selectedOrder : {
			shippingInfo : {}
		},
		loadingCount: 0,
		authToken : null,
		role : null
	},
	components: {
		vuejsDatepicker
	},
	computed : {
		itemsOfSelectedOrders : function() {
			let itemsMap = {};
			for (let orderId in this.selectedOrders) {
				this.selectedOrders[orderId].items.forEach( item => {
					if (!itemsMap.hasOwnProperty(item.name)) {
						Vue.set(itemsMap, item.name, {
							name : item.name,
							quantity : item.quantity
						});
					} else {
						itemsMap[item.name].quantity += +item.quantity;
					}
				});
			}
			return Object.values(itemsMap).sort(function(item1, item2){
				if (item1.name > item2.name) {
			        return 1;
			    }
			    if (item2.name > item1.name) {
			        return -1;
			    }
			    return 0;
			});
			
		},
		selectedOrderHistory : function() {
			let statusList = [];
			if (this.selectedOrder == null || this.selectedOrder.searchProcedureHistory == null || this.selectedOrder.searchProcedureHistory.trackingNumbers == null) {
				return {
					statusList: statusList
				}
			}
			
			
			for (trackingNumber in this.selectedOrder.searchProcedureHistory.trackingNumbers) {
				let shippingHistory = this.selectedOrder.searchProcedureHistory.trackingNumbers[trackingNumber].shippingHistory;
				if (shippingHistory != null) {
					for (shippingStatus in shippingHistory) {
						let date = shippingHistory[shippingStatus];
						statusList.push({
							trackingNumber : trackingNumber,
							shippingSearchStatus : shippingStatus,
							date : date
						})
					}
				}
				let procedureHistory = this.selectedOrder.searchProcedureHistory.trackingNumbers[trackingNumber].procedureHistory;
				if (procedureHistory != null) {
					for (procedureStatus in procedureHistory) {
						let date = procedureHistory[procedureStatus];
						statusList.push({
							trackingNumber : trackingNumber,
							searchProcedureStatus : procedureStatus,
							date : date
						})
					}
				}
				
			}
			
			statusList.push({
				trackingNumber : trackingNumber,
				shippingSearchStatus : this.selectedOrder.shippingSearchStatus,
				date : this.selectedOrder.shippingSearchStatusDate
			});
			
			statusList.push({
				trackingNumber : trackingNumber,
				searchProcedureStatus : this.selectedOrder.searchProcedureStatus,
				date : this.selectedOrder.searchProcedureStatusDate
			})
			
			statusList.sort((a, b) => ((new Date (a.date)) < (new Date (b.date))) ? 1 : -1);
			
			
			
			return {
				statusList: statusList
			}
		}
	},
	methods: {
		filtering(property, value) {
			if(this.filters[property] == value) {
				Vue.delete(this.filters, property);
			} else {
				Vue.set(this.filters, property, value);
			}
			this.pagination.page = 0;
			this.loadOrders();
	
		},
		listFiltering(property, value) {
			if (!this.listFilters.hasOwnProperty(property)) {
				Vue.set(this.listFilters, property, []);
			}
			let list = this.listFilters[property];
			if (list.includes(value)) {
				list.splice( list.indexOf(value), 1 );
			} else {
				list.push(value);
			}
			this.pagination.page = 0;
			this.loadOrders();
		},
		nextPage : function() {
			if (this.pagination.isLast) {
				return;
			}
			this.pagination.page++;
			this.loadOrders();
		},
		previousPage : function() {
			if (this.pagination.isFirst) {
				return;
			}
			this.pagination.page--;
			this.loadOrders();
		},
		sort : function(sortBy, direction) {
			this.sorting.sortBy = sortBy;
			this.sorting.direction = direction;
			this.pagination.page = 0;
			return this.loadOrders();
		},
		search : function() {
			this.loadOrders(true);
		},
		loadOrders : function(useSearch) {
			this.startLoader();
			
			params = {
				page : this.pagination.page,
				size : this.pagination.size,
				sortBy : this.sorting.sortBy,
				sortDirection: this.sorting.direction
			}
			if (useSearch == undefined || this.searchFilter.value.length == 0) {
				if (this.role == 'ROLE_LIMITED_USER') {
					this.endLoader();
					return new Promise(function () {});
				}
				this.searchFilter.value = "";
				for (let prop in this.filters){
					if(this.filters.hasOwnProperty(prop)){
						params[prop] = this.filters[prop];
					}
				}
				for (let prop in this.listFilters){
					if(this.listFilters.hasOwnProperty(prop)){
						params[prop] = this.listFilters[prop].join();
					}
				}
			} else {
				params.search = this.searchFilter.value;
				this.filters.shippingSearchStatus = null;
				this.filters.searchProcedureStatus = null;
			}
			
			console.log('get orders');
			return axios.get('/manager/orders', {
				params: params
			}).then(response => {
				this.pagination.page = response.data.number;
				this.pagination.size = response.data.size;
				this.pagination.totalElements = response.data.totalElements;
				this.pagination.totalPages = response.data.totalPages;
				this.pagination.isLast = response.data.last;
				this.pagination.isFirst = response.data.first;
				
				this.pagination.startElement = this.pagination.page * this.pagination.size +1; 
				
				this.pagination.endElement = (!this.pagination.isLast) ? this.pagination.startElement +this.pagination.size-1 : this.pagination.totalElements;
				
				this.orders.splice(0,this.orders.length);
				response.data.content.forEach(order => { 
					this.orders.push(order);
					if (order.status === 'IN_PROCESS') {
						Vue.set(this.shippingOrders, order.id, order);
					}
				});
			}).finally(() => {
				this.endLoader();
			});
			
		},
		
		formatDate : function(dateString) {
			if (dateString == undefined || dateString == null) {
				return "";
			}
			return (new Date(dateString)).toLocaleString('hr');
		},
		
		formatDateShort : function(dateString) {
			if (dateString == undefined || dateString == null) {
				return "";
			}
			return (new Date(dateString)).toLocaleDateString('hr');
		},
		hasNote : function(order) {
			return order.note != undefined && order.note != null && order.note.length > 0;
		},
		
		changeOrderStatus : function(order, statusField, status) {
			if(order[statusField] == status) {
				return;
			}
			
			this.startLoader();
			let url = '/manager/orders/' + order.id + '/search-procedure-change';
			let params = {};
			params[statusField] = status;
			return axios.post(url, null, {
				params :params
			}).then(response => {
				console.log(response);
				order.searchProcedureHistory = response.data.searchProcedureHistory;
				order.shippingSearchStatus = response.data.shippingSearchStatus;
				order.shippingSearchStatusDate = response.data.shippingSearchStatusDate;
				order.searchProcedureStatus = response.data.searchProcedureStatus;
				order.searchProcedureStatusDate = response.data.searchProcedureStatusDate;
				
				this.endLoader();
			}).catch(error => {
				this.endLoader();
				this.showError(error.response.data.message);
			});
		},
		

		
		
		selectOrder : function(order, modalId) {
			this.selectedOrder = order;
			$(modalId).modal('show');
		},
		
		clearHistory : function() {
			this.selectedOrder.searchProcedureHistory = null;
			this.saveEditOrder();
		},
		
		saveEditOrder : function() {
			this.saveOrder(this.selectedOrder)
		},
		
		saveOrder(order) {
			this.startLoader();
			return axios.post('/manager/orders/', order).then(response => {
				console.log(response);
				this.endLoader();
			}).catch(error => {
				this.endLoader();
				this.showError(error.response.data.message);
			});
		},
		
		downloadRefundRequest: function(order) {
			this.startLoader();
			let url = '/manager/orders/' + order.id + '/search-refund'
			axios.get(url).then(response => {
				var binaryString = window.atob(response.data.base64Data);
			    var binaryLen = binaryString.length;
			    var bytes = new Uint8Array(binaryLen);
			    for (var i = 0; i < binaryLen; i++) {
			       var ascii = binaryString.charCodeAt(i);
			       bytes[i] = ascii;
			    }
				
			    var blob = new Blob([bytes], {type: "application/pdf"});
			    var link = document.createElement('a');
			    link.href = window.URL.createObjectURL(blob);
			    var fileName = response.data.fileName;
			    link.download = fileName;
			    link.click();
			}).catch(error => {
				this.showError(error.response.data.message);
			}).finally(() => {
				this.endLoader();
			})
		},

		updateOrderFromBackend : function(order) {
			this.startLoader();
			axios.get('/manager/orders/' + order.id).then(response => {
				for (let prop in order) {
					Vue.delete(order, prop);
					
				}
				for (let prop in response.data) {
					Vue.set(order, prop, response.data[prop]);
				}
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


		copyDivContent : function(selector) {
			let text = $(selector).text();
			text = text.trim();
			
			function listener(e) {
				e.clipboardData.setData("text/html", text);
				e.clipboardData.setData("text/plain", text);
				e.preventDefault();
			}
			document.addEventListener("copy", listener);
			document.execCommand("copy");
			document.removeEventListener("copy", listener);
		},

		linkifyItem: function(item) {
			let link = item.toLowerCase();
			link = link.replaceAll("š", "s").trim().replaceAll("&", "").replaceAll("  ", " ").replaceAll(" ", "-");
			if (link.search("nausnice") == -1 && link.search("prsten") == -1 && link.search("ogrlica") == -1 && link.search("narukvica") == -1) {
				link = "https://www.kragrlica.com/search?q=" + item.replace(" ", "+");
			} else {
				link = "https://www.kragrlica.com/products/" + link; 
			}
			return link;
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
			
		},
		procedureStatusLabel : function(status) {
			if (status == 'NONE') {
				return "Nije u potražnom";
			}
			if (status == 'FOR_SEARCH') {
				return "Za potražni";
			}
			if (status == 'SEARCH_REQUESTED') {
				return "Potražni pokrenut";
			}
			if (status == 'REFUND_REQUESTED') {
				return "Poslan zahtjev za naknadu";
			}
			
			if (status == 'REFUND_APPROVED') {
				return "Naknada odobrena";
			}
			
			if (status == 'REFUND_DENIED') {
				return "Naknada odbijena";
			}
			
			if (status == 'REFUNDED') {
				return "Dobivena naknada";
			}
			if (status == 'COMPLETED') {
				return "Potražni završen";
			}
			return status;

		},
		
		shippingStatusLabel : function(status) {
			if (status == 'NONE') {
				return "Nije poslan";
			}
			
			
			if (status == 'LOST') {
				return "Izgubljen";
			}
			
			if (status == 'UNKNOWN') {
				return "Nepoznato";
			}
			
			if (status == 'DELIVERED') {
				return "Dostavljen / preuzet";
			}
			
			if (status == 'SENT') {
				return "Poslan";
			}
			
			if (status == 'RESENT') {
				return "Ponovno poslan";
			}
			
			if (status == 'RESENT_FIRST_DELIVERED') {
				return "Ponovno poslan, prethodni dostavljen";
			}
			
			if (status == 'RESENT_SECOND_DELIVERED') {
				return "Ponovno poslan i dostavljen / preuzet";
			}
			
			if (status == 'RESENT_BOTH_DELIVERED') {
				return "Ponovno poslan, oba dostavljena";
			}
			
			return status;

		},
		
		orderStatusTooltip : function(order) {
			if (order.status == "IN_PREPARATION") {
				return "U pripremi"
			}
			if (order.status == "IN_PROCESS") {
				return "Za izradu"
			}
			if (order.status == "IN_POST") {
				return "U pošti"
			}
			if (order.status == "FULFILLED") {
				return "Poslano"
			}
			if (order.status == "CANCELED") {
				return "Otkazano"
			}
			return "";
		},
		orderContactTooltip : function(order) {
			return order.shippingInfo.fullName;
		},
		
		saveSearchDate : function() {
			this.startLoader()
			let body = {
				date : this.searchDate
			}
			if (this.searchDateId != null) {
				body.id = this.searchDateId;
			}
			return axios.post('/manager/search-procedure-date', body).then((response) => {
				this.searchDate = response.data.date;
				this.searchDateId = response.data.id;
			}).finally(() => {
				this.endLoader();
			})
			
		},
		
		getLastSearchDate : function() {
			this.startLoader()
			return axios.get('/manager/search-procedure-date/last').then((response) => {
				if (response.data.date != null) {
					this.searchDate = response.data.date;
					this.searchDateId = response.data.id;
				} 
				
			}).finally(() => {
				this.endLoader();
			})
			
		},
		
		logout : function() {
			localStorage.removeItem("token");
			axios.defaults.headers.common['Authorization'] = null;
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
			return this.getLastSearchDate();
		}).then( () => {
			if (this.role != 'ROLE_LIMITED_USER')  {
				this.loadOrders(0,50).finally( () => {
					this.endLoader();
				});
			}
			else {
				this.endLoader();
			}
		});
		
	}
});

