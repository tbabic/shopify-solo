
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
		paymentOrder: {}
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
			axios.get('/manager/orders', {
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
				response.data.content.forEach(order => this.orders.push(order));
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
		addOrdersForShipping : function() {
			for (let orderId in this.selectedOrders) {
				Vue.set(this.shippingOrders, orderId, this.selectedOrders[orderId]);
			}
			$("#shippingDrawer").collapse("show");
		},
		removeShippingOrder : function(shippingOrder) {
			if (this.shippingOrders.hasOwnProperty(shippingOrder.id)) {
				Vue.delete(this.shippingOrders, shippingOrder.id);
			}
			
		},
		createPostalForm : function() {
			let addressList = [];
			for (let orderId in this.shippingOrders) {
				order = this.shippingOrders[orderId];
				addressList.push(order.shippingInfo)
			}
			$.ajax(
					{
						url: "/adresses/postal-form",
						type: "POST",
						data: JSON.stringify(addressList),
						dataType: 'json',
						contentType: 'application/json; charset=utf-8',  
						async: false,
						success: function(response) {
							
							var binaryString = window.atob(response.value);
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
						}
					}
			);
			
		},
		print : function() {
			window.print();
		},
		selectOrderForPayment : function(order, modalId) {
			this.paymentOrder = order;
			$(modalId).modal('show');
		},
		confirmPayment: function(order) {
			let url = '/manager/orders/' + order.id + '/process-payment'
			axios.post(url).then(function(response) {
				console.log(response);
			});
		}
	},
	mounted : function () {
		this.loadOrders(0,50);
	}
});

