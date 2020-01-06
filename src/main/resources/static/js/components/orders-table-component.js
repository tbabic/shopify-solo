
var orderTableComponent = new Vue({
	el:"#ordersTable",
	data: {
		filters: {},
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
		orders: [],
		selectedOrders: {},
		shippingOrders: {},
		paymentOrder: {},
		editingOrder: {},
		loadingCount: 0
	},
	methods: {
		filtering(property, event) {
			let value = $(event.target).data('value');
			if(this.filters[property] == value) {
				Vue.delete(this.filters, property);
			} else {
				Vue.set(this.filters, property, value);
			}
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
		loadOrders : function() {
			params = {
				page : this.pagination.page,
				size : this.pagination.size
			}
			for (let prop in this.filters){
				if(this.filters.hasOwnProperty(prop)){
					params[prop] = this.filters[prop];
				}
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
			});
			
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
		ordersInPost : function() {
			for (let orderId in this.shippingOrders) {
				let order = this.shippingOrders[orderId];
				this.changeStatus(order, "IN_POST").then(() => {
					Vue.delete(this.shippingOrders, shippingOrder.id);
				});
			}
		},
		createPostalForm : function() {
			let addressList = [];
			for (let orderId in this.shippingOrders) {
				order = this.shippingOrders[orderId];
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
			});
			
		},
		print : function() {
			window.print();
		},
		selectOrderForEditing : function(order, modalId) {
			this.editingOrder = order;
			$(modalId).modal('show');
		},
		saveEditOrder : function() {
			this.saveOrder(this.editOrder)
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
			$(modalId).modal('show');
		},
		confirmPayment: function(order) {
			this.startLoader();
			let url = '/manager/orders/' + order.id + '/process-payment'
			axios.post(url).then(response => {
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
		showError: function(errorMsg) {
			if (errorMsg === undefined) {
				errorMsg = "Unexpected error";
			}
			$("#errorContent").text(errorMsg);
			$("#errorModal").modal('show');
		},
		changeStatus(order, newStatus) {
			let previousStatus = order.status;
			order.status = newStatus;
			return this.saveOrder(order).then(result => {
				order.previousStatus=previousStatus;
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
		orderStatusCssClass : function(order) {
			if (order.status == "INITIAL") {
				return "order-initial"
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
			return "";
		},
		orderStatusTooltip : function(order) {
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
	},
	mounted : function () {
		this.startLoader();
		return axios.get('/manager/orders', {
			params : {
				status : 'IN_PROCESS',
				page : 0,
				size : 1000
			}
		}).then(response => {
			response.data.content.forEach(order => { 
				Vue.set(this.shippingOrders, order.id, order);
			});
			
		}).then( () => {
			this.loadOrders(0,50).finally( () => {
				this.endLoader();
			});
		});
		
	}
});

