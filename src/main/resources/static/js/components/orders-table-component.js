var orderTableComponent = new Vue({
	el:"#ordersTable",
	data () {
		return {
			orders: null
		}
	},
	mounted () {
		axios
			.get('/manager/orders')
			.then(response => this.orders = response.data);
	}
});