
var orderTableComponent = new Vue({
	el:"#ordersTable",
	data: {
		filters: {},
		orders: [],
		selectedOrders: {},
		shippingOrders: {}
	},
	methods: {
		filtering(property, event) {
			let value = $(event.target).data('value');
			if(this.filters[property] == value) {
				Vue.delete(this.filters, property);
			} else {
				Vue.set(this.filters, property, value);
			}
	
		},
		loadOrders : function(page, size) {
			params = {
				page : page,
				size : size
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
				this.orders.splice(0,this.orders.length);
				response.data.content.forEach(order => this.orders.push(order));
			});
			
		},
		formatDate : function(dateString) {
			if (dateString == undefined || dateString == null) {
				return "";
			}
			return (new Date(dateString)).toLocaleDateString('hr');
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
		print : function() {
			window.print();
		}
	},
	mounted : function () {
		this.loadOrders(0,20);
	}
});

