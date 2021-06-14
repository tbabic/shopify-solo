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
		
		invoiceOverviewMonth : (new Date()).getMonth()+1,
		invoiceOverviewYear : (new Date()).getFullYear(),
		useArchive : false,
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
		
		downloadInvoiceOverview : function() {
			this.startLoader();
			
			
			return axios.get("/manager/overview", {
				params: {
					month : this.invoiceOverviewMonth,
					year : this.invoiceOverviewYear,
					useArchive : this.useArchive
				}
			}).then(function(response) {
							
				var binaryString = window.atob(response.data.base64Data);
			    var binaryLen = binaryString.length;
			    var bytes = new Uint8Array(binaryLen);
			    for (var i = 0; i < binaryLen; i++) {
			       var ascii = binaryString.charCodeAt(i);
			       bytes[i] = ascii;
			    }
				
			    var blob = new Blob([bytes], {type: "application/csv"});
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