var createOrderComponent = new Vue({
	el:"#auditLogs",
	data: {
		logs : [],
		
		id : null,
		shopifyOrderId : null,
		changedBy : null,
		startDate : null,
		endDate : null,
		previousStatus : null,
		nextStatus : null,
		
		users : [null, "ana", "martina", "miro", "tomi"],
		
		statusList : [
			{
				text:"",
				value:null,
			},
			{
				text:"Novo",
				value:"INITIAL",
			},
			{
				text:"U izradi",
				value:"IN_PROCESS",
			},
			{
				text:"U pošti",
				value:"IN_POST",
			},
			{
				text:"Odrađeno",
				value:"FULFILLED",
			},
			{
				text:"Otkazano",
				value:"CANCELED",
			},
			{
				text:"Povrat",
				value:"REFUNDED",
			}
		],
		
		pagination: {
			page: 0,
			size: 50,
			startElement: 0,
			endElement: 0,
			totalElements: 0,
			totalPages: 0,
			isFirst: true,
			isLast: true,
		},
		sorting : {
			sortBy: "logTime",
			direction: "ASC"
		},
	},
	components: {
		vuejsDatepicker
	},
	methods: {
		
		
		cleanup : function() {
			this.startLoader();
			
			
			return axios.delete('/manager/audit-logs/cleanup').then(response => {
				this.loadLogs();
			}).finally(() => {
				this.endLoader();
			});
		},
		
		loadLogs : function() {
			if (this.role == 'ROLE_SMC_MANAGER') {
				return;
			}
			this.startLoader();
			
			params = {
				id : this.id,
				shopifyOrderId : this.shopifyOrderId,
				changedBy : this.changedBy,
				start : this.startDate,
				end : this.endDate,
				previousStatus : this.previousStatus,
				nextStatus : this.nextStatus,
				page : this.pagination.page,
				size : this.pagination.size,
				sortBy : this.sorting.sortBy,
				sortDirection: this.sorting.direction
			}
			
			console.log('get audit logs');
			return axios.get('/manager/audit-logs', {
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
				
				this.logs.splice(0,this.logs.length);
				response.data.content.forEach(log => { 
					this.logs.push(log);
				});
			}).finally(() => {
				this.endLoader();
			});
		},
		
		orderStatusDisplay(status) {
			for (i = 0; i<this.statusList.length; i++) {
				if (this.statusList[i].value == status) {
					return this.statusList[i].text;
				}	
			}
			return "NEPOZNATO";
		},
		
		nextPage : function() {
			if (this.pagination.isLast) {
				return;
			}
			this.pagination.page++;
			this.loadLogs();
		},
		previousPage : function() {
			if (this.pagination.isFirst) {
				return;
			}
			this.pagination.page--;
			this.loadLogs();
		},
		startChange : function() {
			this.startDate.setHours(0,0,0,0);
		},
		
		endChange : function() {
			this.endDate.setHours(23,59,59,999);
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
	
	computed : {
		
	},
	
	mounted : function() {
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
			this.loadLogs();
		}).then(() => {
			this.endLoader();
		})
		
	}
});