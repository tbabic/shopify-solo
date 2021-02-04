var createOrderComponent = new Vue({
	el:"#reports",
	data: {
		
		startDate : new Date(),
		endDate : new Date(),
		
		report : {
			orderSum : 0,
			orderCount : 0,
			refundSum : 0,
			refundCount : 0,
			totalSum : 0
			
		},
		
		loadingCount: 0,
		role : null
		
		
	},
	computed : {
		
	},
	components: {
		vuejsDatepicker
	},
	methods: {
		
		startChange : function() {
			this.startDate.setHours(0,0,0,0);
		},
		
		endChange : function() {
			this.endDate.setHours(23,59,59,999);
		},
		
		loadData : function() {
			this.startLoader();
			return axios.get('/manager/reports', {
				params: {
					start : this.startDate,
					end : this.endDate
				}
			}).then(response => {
				this.report = response.data;
			}).finally(() => {
				this.endLoader();
			});
		},
		
		dateFormat : function() {
			
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
		this.startDate.setHours(0,0,0,0);
		this.endDate.setHours(23,59,59,999);
		
		
		
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
			this.loadData();
		}).then(() => {
			this.endLoader();
		});
		
		
		
	}
});