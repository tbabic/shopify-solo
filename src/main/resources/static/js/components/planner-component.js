var calendarComponent = new Vue({
	el:"#calendar-app",
	data:{
		calendar : null,
		
		
	},
	methods: {
		
		loadEvents : function() {
			this.loadPosts().then(() => {
				console.log("success");
			})
		},
		
		loadPosts : function() {
			params = {
				start: this.calendar.currentData.dateProfile.activeRange.start,
				end : this.calendar.currentData.dateProfile.activeRange.end
			};
			
			return axios.get('/manager/posts', {
				params: params
			}).then(response => {
				response.data.forEach(post => {
					let event ={
					title : "Naslov",
					start : new Date(post.date),
					id : "POST-"+post.id,
					type : "POST",
					allDay : true,
					backendId : post.id
				};
				this.calendar.addEvent(event);
				});				
				console.log(response.data);

			});
			
		},
		
		newEvent: function(info) {
			console.log(info);
		},
		
		showEvent : function(info) {
			console.log(info);
		},
		
		startLoader() {
			
		},
		
		endLoader() {
			
		},

		
	},
	mounted : function () {
		
		
		
		
		FullCalendar.globalLocales.forEach(locale => {
			if (locale.code == 'hr') {
				console.log("found");
				locale.buttonText['dayGrid'] = 'Dan'
			}
		});
		
		let calendarEl = document.getElementById('calendar');
        this.calendar = new FullCalendar.Calendar(calendarEl, {
			selectable: true,
			initialView: 'dayGridWeek',
			headerToolbar: {
				left: 'prev,next today',
				center: 'title',
				right: 'dayGridMonth,dayGridWeek,dayGrid'
	    	},
			firstDay : 1,
			locale : 'hr',
			eventClick : this.showEvent,
			select : this.newEvent
						
  		});
		
        this.calendar.render();
		
		
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
			this.endLoader();
			this.loadEvents();
		});
		
	}
		
});

