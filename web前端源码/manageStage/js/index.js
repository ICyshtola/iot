window.onload = function(){
	var obj = document.getElementsByClassName("content1")[0];
	var btn = obj.getElementsByTagName("button")[0];
	btn.onclick = function(){
		window.location.href="login.html";
	}
	var intr = document.getElementsByClassName("introduce")[0];
	
	var item = intr.getElementsByClassName("item")[0];
	item.style.width="50%";
	item.style.height="inherit";
	item.style.marginTop="0";
	
	var items = intr.getElementsByClassName("item");
	[].forEach.call(items,function(item){
		item.addEventListener("mouseover",function(){
			  //    遍历所有兄弟节点this.parentNode.children
			Array.prototype.forEach.call(this.parentNode.children, function (child) {
				child.style.width="25%";
				child.style.height="300px";
				child.style.marginTop="8%";
			})
			item.style.width="50%";
			item.style.height="inherit";
			item.style.marginTop="0";
		});
		
	})	
			
	
	
}
