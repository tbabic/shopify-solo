var calendarComponent = new Vue({
	el:"#calendar-app",
	data:{
		calendar : null,
		
		post : {
			id : null,
			date : null,
			content : null,
			orderPosition : null,
			isDone : false
		}
		
		
	},
	

	methods: {
		
		loadEvents : function(info) {
			
			this.calendar.getEvents().forEach(event => {
				event.remove();
				console.log("event removed");
			});
			
			this.loadPosts(info).then(() => {
				console.log("success");
			})
		},
		
		loadPosts : function(info) {
			params = {
				start: info.start,
				end : info.end
			};
			console.log(params);
			console.log(info);
			return axios.get('/manager/posts', {
				params: params
			}).then(response => {
				console.log(response.data);
				response.data.forEach(post => {
					let event = this.postToEvent(post);
					this.calendar.addEvent(event);
					console.log("event added");
				});				
				

			});
			
		},
		
		postToEvent : function(post) {
			let event = {
				title : post.content,
				start : new Date(post.date),
				id : "POST-"+post.id,
				type : "POST",
				allDay : true,
				backendId : post.id,
				orderPosition : post.orderPosition,
				isDone: post.isDone
			};
			if (event.isDone) {
				event["classNames"] = "event-done";
			}
			
			return event;
		},
		
		savePost : function(stopSpinning) {
			
			
			this.startLoader();
			return axios.post('/manager/posts', this.post).then(response => {
				console.log(response);
				let event = this.postToEvent(response.data);
				this.calendar.addEvent(event);
				if (this.post.id != null) {
					this.calendar.getEventById("POST-"+this.post.id).remove();
				}
				
				$("#editPostModal").modal('hide');
			}).finally(() => {
				stopSpinning();
			});
			
		},
		
		deletePost : function(stopSpinning) {
			
			
			this.startLoader();
			return axios.delete('/manager/posts/'+this.post.id).then(response => {
				console.log(response);
				this.calendar.getEventById("POST-"+this.post.id).remove();
				$("#confirmDeleteModal").modal('hide');
			}).finally(() => {
				stopSpinning();
			});
			
		},
		
		newEvent: function(info) {
			
			
			this.post.id = null;
			this.post.content = null;
			this.post.date = info.start;
			this.post.isDone = false;
			
			let length = this.calendar.getEvents().filter(p => p.start.getTime() == info.start.getTime()).length;
			this.post.orderPosition = length+1;
			
			$("#editPostModal").modal('show');
			
		},
		
		showEvent : function(info) {
			console.log(info);
			
			this.post.id = info.event.extendedProps.backendId;
			this.post.content = info.event.title;
			this.post.date = info.event.start;
			this.post.orderPosition = info.event.extendedProps.orderPosition;
			this.post.isDone = info.event.extendedProps.isDone;
			
			$("#editPostModal").modal('show');
		},
		
		formatDate : function(date) {
			if (date == null) {
				return null;
			}
			return (new Date(date)).toLocaleDateString('hr');
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
			height: 'auto',
			initialView: 'dayGridWeek',
			eventOrder : "orderPosition",
			eventOrderStrict : true,
			headerToolbar: {
				left: 'prev,next today',
				center: 'title',
				right: 'dayGridMonth,dayGridWeek,dayGrid'
	    	},
			firstDay : 1,
			locale : 'hr',
			eventClick : this.showEvent,
			select : this.newEvent,
			datesSet: this.loadEvents,
						
  		});
		
        

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
			this.calendar.render();
		});
		
		
		
		
	}
		
});

