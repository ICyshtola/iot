window.onload = function() {
	var ullist = document.getElementById("unit_list");

	var btn_add = document.getElementById("unit_add");
	btn_add.onclick = function() {
		window.location.href = "add_unit.html";
	};
	getUserUnitsFromServer();
	/**
	 * 根据数据库，动态添加采集单元
	 */
	//静态测试
	//ullist.appendChild(addUnitLi("新建的", 10, 10, "大棚"));

	/**
	 * 请求服务器本人的采集单元信息
	 */
	function getUserUnitsFromServer() {
		var info = {};
		info['token'] = getCookie("token");
		info['email'] = getCookie("current_email");
		jQuery.ajax({
			async : false,
			type: "POST",
			url: "http://192.168.100.32:8036/user/getUnitInfo",
			data: info,
			dataType: "text",
			xhrFields: {
				withCredentials: true
			},
			success: function(data) {
				var result = JSON.parse(data);
				if(result["msgType"] == "UserGetUnitInfo") {
					if(result["message"] == "success") {
						var content = JSON.parse(result["content"]);
						for (var i = 0; i < content.length; i++){
							var data = content[i];
							if (data['unitDescription'].length == 0){
								ullist.appendChild(addUnitLi(data['unitName'], data['unitLength'], data['unitWidth'], "无"));
							} else {
								ullist.appendChild(addUnitLi(data['unitName'], data['unitLength'], data['unitWidth'], data['unitDescription']));
							}
						}
					} else if(result["message"] == "noUnit"){
						alert("还没有采集单元呢，赶快去添加吧！");
					} else {
						console.log("tokenError");
					}
				}
				setCookie("token", result["token"], 100);
			},
			error: function() {
				console.log("error");
			},
			complete: function() {
				iframeResize();
			}
		})
	}

	/**
	 * 添加一个采集单元
	 * @param {Object} name 
	 * @param {Object} length
	 * @param {Object} width
	 * @param {Object} sence
	 */
	function addUnitLi(name, length, width, sence) {
		var li = document.createElement("li");
		var div = document.createElement("div");
		var label_name = document.createElement("label");
		var label_lengthWidth = document.createElement("label");
		var btn_delete = document.createElement("button");
		var p = document.createElement("p");
		var a = document.createElement("a");
		a.className = "unit_item_name";
		a.title = "单元名称";
		a.href = "info_unit.html";
		a.onclick = function() {
			var unit = "?unitName=" +  name
						+"&unitLength=" + length
						+"&unitWidth=" + width
						+"&unitSence=" + sence;
			a.href += unit;
		};
		a.innerHTML = name;

		li.className = "unit-list-item";
		label_lengthWidth.className = "unit_item_lengthAndWidth";
		label_lengthWidth.innerHTML = length + "x" + width;
		label_lengthWidth.title = "长x宽";
		btn_delete.className = 'unit_item_delete';
		btn_delete.innerHTML = "x";
		btn_delete.title = "删除";
		btnDeleteMySelf(btn_delete);
		p.className = "unit_item_sence";
		p.innerHTML = "应用场景："+sence;
		p.title = "应用场景："+sence;

		
		div.className = "unit_item";

		label_name.appendChild(a);
		div.appendChild(label_name);
		div.appendChild(label_lengthWidth);
		div.appendChild(btn_delete);
		div.appendChild(p);
		li.appendChild(div);
		return li;
	}

	/**
	 * 删除按钮绑定点击事件，
	 * 点击后，弹出是否删除，
	 * yes.发送数据给服务器，
	 * 		a.成功，删除显示
	 * no.没有操作
	 * @param {Object} obj
	 */
	function btnDeleteMySelf(obj) {
		obj.onclick = function() {
			var li = this.parentNode.parentNode;

			var choice = window.confirm("确定删除吗？");

			if(choice == true) {
				var delinfo = {};
				var name = this.parentNode.firstChild.firstChild.innerHTML;

				delinfo['name'] = name;
				delinfo['email'] = getCookie("current_email");
				delinfo['token'] = getCookie("token");
				jQuery.ajax({
					type: "POST",
					url: "http://192.168.100.32:8036/user/delUnit",
					data: delinfo, //token,email,name(单元名称)
					dataType: "text",
					xhrFields: {
						withCredentials: true
					},
					success: function(data) {
						console.log(data);
						var result = JSON.parse(data);
						if(result["msgType"] == "UserDelUnit") {
							if(result["content"] == "success") {
								/**
								 * 删除
								 */
								ullist.removeChild(li);
							} else {
								alert("删除失败");
							}
						}
						setCookie("token", result["token"], 100);
					},
					error: function() {
						console.log("error");
						alert("删除失败");
					},
			complete: function() {
				iframeResize();
			}
				})
			}
		}
	}
	iframeResize();
}