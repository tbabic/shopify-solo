Vue.component('spinbutton', {
	data: function () {
		return {
			spinner : false
		}
	},
	methods : {
		startSpinner : function() {
			console.log("start spinner");
			this.spinner = true;
			this.$emit('click', () => {
				this.spinner = false;
			});
			
			return;
			
		}
	},
	template: '<button type="button" class="btn" v-on:click="startSpinner"> <span v-if="spinner"  class="spinner-border spinner-border-sm"> </span> <slot></slot> </button>'
})