
var orderTableComponent = new Vue({
	el:"#ordersTable",
	data: {
		filters: {
			paid : true,
			status : null,
			shippingSearchStatus : null
		},
		listFilters : {
			
		},
		searchFilter : {
			value : ""
		},
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
		selectedOrders: {},
		shippingOrders: {},
		paymentOrder: {},
		r1 : false,
		oib : null,
		editingOrder: {
			shippingInfo: {
				
			}
		},
		refund : {
			order : {
				shippingInfo: {
				
				}
			},
			
			standard : {
				items : []
			},
			
			custom : {
				items : [],
			},
			
			type : "standard",
			amount : 0
		},
		fulfillment: {
			order : {},
			params : {
				sendNotification : true,
				trackingNumber : false
			}
		},
		statusChange : {
			newStatus : "",
			statusField : null,
			count : 0
		},
		createdOrder: {
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
		epk : {
			inputId : '',
			orders:{},
		},
		loadingCount: 0,
		authToken : null,
		role : null
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
		epkOrdersSize : function() {
			let count = 0;
			for (let order in this.epk.orders) {
				count++;
			}
			return count;
		}
	},
	methods: {
		filtering(property, event) {
			let value = $(event.target).data('value');
			if(this.filters[property] == value) {
				Vue.delete(this.filters, property);
			} else {
				Vue.set(this.filters, property, value);
			}
			this.pagination.page = 0;
			this.deselectAll();
			this.loadOrders();
	
		},
		listFiltering(property, event) {
			if (!this.listFilters.hasOwnProperty(property)) {
				Vue.set(this.listFilters, property, []);
			}
			let list = this.listFilters[property];
			let value = $(event.target).data('value');
			if (list.includes(value)) {
				list.splice( list.indexOf(value), 1 );
			} else {
				list.push(value);
			}
			this.pagination.page = 0;
			this.deselectAll();
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
			this.deselectAll();
			return this.loadOrders();
		},
		search : function() {
			this.deselectAll();
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

		findNextOrderForEpk(id) {
			findOrderBy(id).
			then(response => {
				this.epk.orders[id].trackingNumber = '';
			});
		},
		
		findOrderById : function(id) {
			if(id == undefined || id == null || id == '') {
				return;
			}
			this.startLoader();
			return axios.get('/manager/orders/'+id).then(response => {
				response.data.trackingNumber = '';
				Vue.set(this.epk.orders, response.data.id, response.data);
				this.epk.inputId = '';
				setTimeout(() => {
					$("#epkOrderTracking_"+response.data.id).focus();
				}, 1);
				
			}).finally(() => {
				this.endLoader();
			});
		},
		clearEpk : function() {
			this.epk.inputId = '';
			this.epk.orders = {};
		},
		
		refocusEpk : function() {
			$("#epkInputOrderId").focus();
		},
		

		
		formatDate : function(dateString) {
			if (dateString == undefined || dateString == null) {
				return "";
			}
			return (new Date(dateString)).toLocaleString('hr');
		},
		selectOrder : function(order) {
			if(this.isSelected(order)) {
				Vue.delete(this.selectedOrders, order.id);
			} else {
				Vue.set(this.selectedOrders, order.id, order);
			}
		},
		selectAll : function() {
			this.orders.forEach(order => { 
				Vue.set(this.selectedOrders, order.id, order);
			});
		},
		deselectAll : function() {
			for (orderId in this.selectedOrders) {
				Vue.delete(this.selectedOrders, orderId);
			}
		},
		isSelected : function(order) {
			return this.selectedOrders.hasOwnProperty(order.id);
		},
		hasNote : function(order) {
			return order.note != undefined && order.note != null && order.note.length > 0;
		},
		addOrdersForShipping : function() {
			for (let orderId in this.selectedOrders) {
				if (this.shippingOrders[orderId] == this.selectedOrders[orderId]) {
					Vue.delete(this.selectedOrders, orderId);
					$("#shippingDrawer").collapse("show");
					return;
				}
				this.changeStatus(this.selectedOrders[orderId], 'IN_PROCESS').then(() => {
					Vue.set(this.shippingOrders, orderId, this.selectedOrders[orderId]);
					Vue.delete(this.selectedOrders, orderId);
					$("#shippingDrawer").collapse("show");
				});
			}
			
		},
		removeShippingOrder : function(shippingOrder) {
			if (this.shippingOrders.hasOwnProperty(shippingOrder.id)) {
				this.revertStatus(shippingOrder).then(() => {
					Vue.delete(this.shippingOrders, shippingOrder.id);
				});
				
			}
			
		},
		changeSelectedOrdersStatus : function(status, field) {
			this.statusChange.newStatus = status;
			this.statusChange.statusField = field;
		},
		processStatusChange : function(status, statusField) {
			this.startLoader();
			for (let orderId in this.selectedOrders) {
				this.startLoader();
				this.changeStatus(this.selectedOrders[orderId], status, statusField).finally(() => {
					this.endLoader();
				});
			}
			this.endLoader();
		},
		ordersInPost : function() {
			for (let orderId in this.shippingOrders) {
				let order = this.shippingOrders[orderId];
				this.changeStatus(order, "IN_POST").then(() => {
					Vue.delete(this.shippingOrders, shippingOrder.id);
				});
			}
		},
		saveAllEpkOrders : function() {
			let promise = null;
			this.startLoader();
			for (let id in this.epk.orders) {
				let orderId = id;
				if (promise == null) {
					promise = this.saveOrder(this.epk.orders[orderId]);
				} else {
					promise = promise.then(() => {
						return this.saveOrder(this.epk.orders[orderId]); 
					});
				}
			}
			return promise.then(() => {
				console.log("all orders saved");
			}).finally(() => {
				this.endLoader();
			});
			
		},
		
		createEpk : function() {
			this.startLoader();
			let orderIdList = [];
			for (let orderId in this.epk.orders) {
				order = this.epk.orders[orderId];
				orderIdList.push(order.id)
			}
			
			return axios.post("/manager/epk", orderIdList).then(function(response) {
							
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
			});
			
		},
		createEpkFromSelectedOrders : function() {
			this.startLoader();
			let orderIdList = [];
			for (let orderId in this.selectedOrders) {
				order = this.selectedOrders[orderId];
				orderIdList.push(order.id)
			}
			
			axios.post("/manager/epk", orderIdList).then(function(response) {
							
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
			});
		},
		
		createPostalForm : function() {
			this.startLoader();
			let addressList = [];
			for (let orderId in this.selectedOrders) {
				order = this.selectedOrders[orderId];
				addressList.push(order.shippingInfo)
			}
			
			axios.post("/adresses/postal-form", addressList).then(function(response) {
							
				var binaryString = window.atob(response.data.value);
			    var binaryLen = binaryString.length;
			    var bytes = new Uint8Array(binaryLen);
			    for (var i = 0; i < binaryLen; i++) {
			       var ascii = binaryString.charCodeAt(i);
			       bytes[i] = ascii;
			    }
				
			    var blob = new Blob([bytes], {type: "application/pdf"});
			    var link = document.createElement('a');
			    link.href = window.URL.createObjectURL(blob);
			    var fileName = "Prijamna knjiga";
			    link.download = fileName;
			    link.click();
			}).catch(error => {
				this.showError(error.response.data.message);
			}).finally(() => {
				this.endLoader();
			});
			
		},
		print : function() {
			$('#printSection').empty();
			$('.printable').each( (i, el) => {
				$('#printSection').append($(el).clone());
			});
			window.print();
			//$('#printSection').empty();
		},
		selectOrderForFulfillment : function(order, modalId) {
			this.fulfillment.order = order;
			if (order.trackingNumber != null && order.trackingNumber != undefined) {
				this.fulfillment.params.trackingNumber = order.trackingNumber + "";
			} else {
				this.fulfillment.params.trackingNumber = "";
			}
			
			this.fulfillment.params.sendNotification = true;
			$(modalId).modal('show');
		},
		reissueInvoice: function(order) {
			this.startLoader();
			let url = '/manager/orders/' + order.id + '/reissue-invoice'
			axios.post(url, null, {
				params : {
					r1 : this.r1,
					oib : this.oib
				}
			}).then(response => {
				console.log(response);
				axios.get('/manager/orders/' + order.id).then(response => {
					for (let prop in order) {
						Vue.delete(order, prop);
						
					}
					for (let prop in response.data) {
						Vue.set(order, prop, response.data[prop]);
					}
					this.endLoader();
					
				}).catch(error => {
					this.endLoader();
					this.showError(error.response.data.message);
				});
			}).catch(error => {
				this.endLoader();
				this.showError(error.response.data.message);
			});
		},
		
		downloadInvoice: function(order) {
			this.startLoader();
			let url = '/manager/orders/' + order.id + '/download-invoice'
			axios.post(url, null, {
				params : {
					r1 : this.r1,
					oib : this.oib
				}
			}).then(response => {
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
		
		fulfillOrder : function() {
			this.startLoader();
			let url = '/manager/orders/' + this.fulfillment.order.id + "/process-fulfillment"
			return axios.post(url, null, { 
				params: this.fulfillment.params
			}).then(response => {
				console.log(response);
				this.updateOrderFromBackend(this.fulfillment.order);
			}).catch(error => {
				this.showError(error.response.data.message);
			}).finally(() => {
				this.endLoader();
			});
		},
		
		processFulfillmentInPost : function() {
			this.startLoader();
			return axios.post('/manager/orders/process-fulfillment-in-post-async')
			.then(response => {
				console.log(response);
			}).catch(error => {
				this.showError(error.response.data.message);
			}).finally(() => {
				this.endLoader();
			});
		},
		setRefundOrder: function(order) {
			this.refund.order = order;
			this.refund.type = 'standard';
			this.refund.standard.items.splice(0, this.refund.standard.items.length);
			this.refund.amount = 0;
			
			this.refund.order.items.forEach(item => {
				this.refund.standard.items.push({
					item : item,
					refund : false
				});
			});
			
		},
		
		calculateRefund : function() {
			let amount = 0;
			if (this.refund.type == 'standard') {
				this.refund.standard.items
					.filter(item => item.refund == true)
					.forEach(item => {
						amount += +item.item.totalPrice; 
					});
			}
			this.refund.amount = amount;
		},
		
		addRefundCustomOrder : function() {
			this.refund.custom.items.push(
				{
					"name": "",
					"price": 0,
					"priceWithTaxRate": 0,
					"quantity": 1,
					"taxRate": 25
    			}
			);
		},
		
		refundCustomItemPriceChange(item) {
			item.price = +item.priceWithTaxRate / 1.25;
		},
		
		saveRefund : function() {
			this.startLoader();
			if (this.refund.type == 'standard') {
				this.saveStandardRefund();
			}
			this.endLoader();
		},
		
		saveStandardRefund : function() {
			
		},
		
		saveCustomRefund : function() {
			
		},
		
		selectOrderForEditing : function(order, modalId) {
			this.editingOrder = order;
			$(modalId).modal('show');
		},
		saveEditOrder : function() {
			this.saveOrder(this.editingOrder)
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
		selectOrderForPayment : function(order, modalId) {
			this.paymentOrder = order;
			this.r1 = false;
			this.oib = null;
			$(modalId).modal('show');
		},
		confirmPayment: function(order) {
			this.startLoader();
			let url = '/manager/orders/' + order.id + '/process-payment'
			axios.post(url, null, {
				params : {
					r1 : this.r1,
					oib : this.oib
				}
			}).then(response => {
				console.log(response);
				axios.get('/manager/orders/' + order.id).then(response => {
					for (let prop in order) {
						Vue.delete(order, prop);
						
					}
					for (let prop in response.data) {
						Vue.set(order, prop, response.data[prop]);
					}
					this.endLoader();
					
				}).catch(error => {
					this.endLoader();
					this.showError(error.response.data.message);
				});
			}).catch(error => {
				this.endLoader();
				this.showError(error.response.data.message);
			});
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
		changeStatus(order, newStatus, statusField) {
			if (statusField == 'shippingSearchStatus') {
				return this.changeShippingSearchStatus(order, newStatus);
			}
			let previousStatus = order.status;
			order.status = newStatus;
			return this.saveOrder(order).then(result => {
				order.previousStatus=previousStatus;
			}).catch(error => {
				order.status = previousStatus;
				delete order.previousStatus;
			});
		},
		changeShippingSearchStatus(order, newStatus) {
			let previousStatus = order.shippingSearchStatus;
			order.shippingSearchStatus = newStatus;
			return this.saveOrder(order).then(result => {
				delete order.previousStatus;
			}).catch(error => {
				order.status = previousStatus;
				delete order.previousStatus;
			});
		},
		revertStatus(order) {
			let revertingStatus = order.status
			if (order.previousStatus != undefined && order.previousStatus != null) {
				order.status = order.previousStatus;
			} else {
				order.status = 'INITIAL';
			}
			
			delete order.previousStatus;
			return this.saveOrder(order).catch(error => {
				order.previousStatus = order.status;
				order.status = revertingStatus;
			});
		},
		addNewItemToCreatedOrder : function(){
			this.createdOrder.items.push({
				name: null,
				quantity: null,
				price: null
			});
		},
		deleteItemFromCreatedOrder : function(index){
			this.createdOrder.items.splice( index, 1 );
		},
		saveNewOrder : function(){
			this.saveOrder(this.createdOrder)
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
		statusCssClass : function(status) {
			if (status == "INITIAL") {
				return "status-initial"
			}
			if (status == "IN_PROCESS") {
				return "status-in-preparation"
			}
			if (status == "IN_PROCESS") {
				return "status-in-process"
			}
			if (status == "IN_POST") {
				return "status-in-post"
			}
			if (status == "FULFILLED") {
				return "status-fulfilled"
			}
			if (status == "CANCELED") {
				return "status-canceled"
			}
			return "";
		},
		
		orderStatusCssClass : function(order) {
			if (order.status == "INITIAL") {
				return "order-initial"
			}
			if (order.status == "IN_PREPARATION") {
				return "order-in-preparation"
			}
			if (order.status == "IN_PROCESS") {
				return "order-in-process"
			}
			if (order.status == "IN_POST") {
				return "order-in-post"
			}
			if (order.status == "FULFILLED") {
				return "order-fulfilled"
			}
			if (order.status == "CANCELED") {
				return "order-canceled"
			}
			if (order.status == "REQUEST_SEARCH") {
				return "order-request-search"
			}
			if (order.status == "SEARCH_PROCESS") {
				return "order-search-process"
			}
			return "";
		},
		statusLabel : function(status, statusField) {
			if (statusField == 'shippingSearchStatus') {
				if (status == 'IN_PROCESS') {
					return "U potražnom";
				}
				if (status == 'REQUESTED') {
					return "Za potražni";
				}
			}
			if (status == "IN_PREPARATION") {
				return "U pripremi"
			}
			if (status == "IN_PROCESS") {
				return "U izradi"
			}
			if (status == "IN_POST") {
				return "U pošti"
			}
			if (status == "FULFILLED") {
				return "Poslano"
			}
			if (status == "CANCELED") {
				return "Otkazano"
			}
			return "";
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
			return axios.get('/manager/orders', {
				params : {
					status : 'IN_PROCESS',
					page : 0,
					size : 1000
				}
			});
		}).then(response => {
			if (this.role != 'ROLE_LIMITED_USER')  {
				response.data.content.forEach(order => { 
					Vue.set(this.shippingOrders, order.id, order);
				});
			}
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

