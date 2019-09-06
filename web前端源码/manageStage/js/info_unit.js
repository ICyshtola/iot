window.onload = function() {

	//获取对象
	//名称，基本信息，长度、宽度、应用场景
	var unitName = document.getElementById("unit_name");
	var unitLength = document.getElementsByTagName("input")[0];
	var unitWidth = document.getElementsByTagName("input")[1];
	var unitSence = document.getElementsByTagName("textarea")[0];

	//设备ul, 动态添加,删除,开关和删除按钮
	var ulDevice = document.getElementById("unit_device_content_list");
	//获取三个按钮,提交、编辑信息、编辑开发板
	var btnSubmitInfo = document.getElementById("submit_info");
	var btnEditInfo = document.getElementsByClassName("unit_info_edit")[0];
	var btnEditDevice = document.getElementsByClassName("unit_info_edit")[1];
	//获取编辑时的报错信息
	var errors = document.getElementsByClassName("unit_info_item_error");
	var lengthError = errors[0],
		widthError = errors[1],
		senceError = errors[2];

	//初始化数据
	unitName.innerText = GetQueryString("unitName");
	unitLength.value = GetQueryString("unitLength");
	unitWidth.value = GetQueryString("unitWidth");
	unitSence.value = GetQueryString("unitSence");
	getUnitDevicesFromServer();
	/**
	 * 向服务器申请数据显示
	 * 数据：
	 * 		1.所含的设备名mac
	 * 		2.设备的状态
	 */
	//ulDevice.appendChild(addUnitDeviceLi("F4:5E:AB:57:F0:00", "off"));

	/**
	 * 请求服务器采集单元的绑定设备信息
	 */
	function getUnitDevicesFromServer() {
		var info = {};
		info['token'] = getCookie("token");
		info['email'] = getCookie("current_email");
		info['unit'] = unitName.innerText;
		jQuery.ajax({
			async: false,
			type: "POST",
			url: "http://192.168.100.32:8036/user/getDevsByUnit",
			data: info,
			dataType: "text",
			xhrFields: {
				withCredentials: true
			},
			success: function(data) {
				var result = JSON.parse(data);
				if(result["msgType"] == "UserGetDevsByUnit") {
					if(result["message"] == "success") {
						var content = JSON.parse(result["content"]);
						for(var i = 0; i < content.length; i++) {
							var data = content[i];
							ulDevice.appendChild(addUnitDeviceLi(data['deviceMac'], "on"));
						}
					} else if(result["message"] == "noDevice") {
						alert("还没有绑定设备呢，赶快去添加吧！");
					} else {
						console.log("tokenError");
					}
				}
				setCookie("token", result["token"], 100);
			},
			error: function() {
				console.log("error");
				alert("获取数据失败");
			},
			complete: function() {
				iframeResize();
			}
		})
	}

	function addUnitDeviceLi(name, status) {
		var li = document.createElement("li");
		var div = document.createElement("div");
		var label_name = document.createElement("label");
		var a_name = document.createElement("a");
		var btn_delete = document.createElement("i");
		var btn_info = document.createElement("i");
		var btn_data = document.createElement("i");
		a_name.href = "info_device.html";
		a_name.className = "device_item_name";
		a_name.innerText = name;
		a_name.onclick = function() {
			//传参给info_device.html, 比如设备名，采集单元，mac地址
			var device = "?deviceMacAddress=" + name +
				"&deviceUnit=" + unitName.innerText;
			a_name.href += device;
		};

		btn_delete.className = "device_item_delete fa fa-trash";
		btn_delete.style.display = 'none';
		btn_delete.title = "解绑";
		btnDeleteFromUnit(btn_delete);

		btn_info.className = "device_item_info fa fa-power-off";
		btn_info.title = "发送数据";
		sendSwitchToServer(btn_info);

		btn_data.className = "device_item_showData fa fa-line-chart";
		btn_data.title = "数据展示";
		btn_data.onclick = function() {
			//点击跳转到show_data界面，采集单元、设备名
			var unitDevice = "show_current_data.html" + "?deviceName=" + name +
				"&unitName=" + unitName.innerText;
			window.location.href = unitDevice;
		}
		li.className = "device-list-item";
		div.className = "device_item";

		label_name.appendChild(a_name);
		div.appendChild(label_name);
		div.appendChild(btn_info);
		div.appendChild(btn_data);
		div.appendChild(btn_delete);
		li.appendChild(div);
		return li;
	}

	/**
	 * 填写长度、宽度、应用场景的数值和长度检查
	 */
	unitLength.onchange = function() {
		if(unitLength.value < 0) {
			lengthError.innerHTML = "数值大于等于0";
		} else {
			lengthError.innerHTML = "";
		}
	};
	unitWidth.onchange = function() {
		if(unitWidth.value < 0) {
			widthError.innerHTML = "数值大于等于0";
		} else {
			widthError.innerHTML = "";
		}
	};
	unitSence.onchange = function() {
		if(unitWidth.value.length > 100) {
			senceError.innerHTML = "应用场景在1-100字内";
		} else {
			senceError.innerHTML = "";
		}
	};
	var unit = {
		'length': unitLength.value,
		'width': unitWidth.value,
		'sence': unitSence.value,
	}; //记录当前值
	/**
	 * 编辑按钮点击事件
	 */
	btnEditInfo.onclick = function() {
		if(this.innerText == "编辑") {
			this.innerText = "取消";
			unitLength.disabled = false;
			unitWidth.disabled = false;
			unitSence.disabled = false;
			btnSubmitInfo.style.display = "block";
		} else {
			this.innerText = "编辑";
			unitLength.disabled = true;
			unitWidth.disabled = true;
			unitSence.disabled = true;
			btnSubmitInfo.style.display = "none";
			/**
			 * 当前值赋值，防止没有提交的修改
			 */
			unitLength.value = unit.length;
			unitWidth.value = unit.width;
			unitSence.value = unit.sence;
		}
	};
	btnEditDevice.onclick = function() {
		var btnDeleteDevices = document.getElementsByClassName("device_item_delete");
		var btnInfoDevices = document.getElementsByClassName("device_item_info");
		var btnDataDevices = document.getElementsByClassName("device_item_showData");

		if(this.innerText == "编辑") {
			this.innerText = "取消";
			/**
			 * 获取delete按钮，等，设置style
			 */
			ifEditImgDisplay(btnDeleteDevices, "block");
			ifEditImgDisplay(btnInfoDevices, "none");
			ifEditImgDisplay(btnDataDevices, "none");
		} else {
			this.innerText = "编辑";
			/**
			 * 获取delete按钮，等，设置style
			 */
			ifEditImgDisplay(btnDeleteDevices, "none");
			ifEditImgDisplay(btnInfoDevices, "block");
			ifEditImgDisplay(btnDataDevices, "block");
		}
	};
	/**
	 * 提交按钮点击事件
	 */
	btnSubmitInfo.onclick = function() {
		var info = {};
		info['token'] = getCookie("token");
		info['email'] = getCookie("current_email");
		if(lengthError.innerHTML == "" &&
			widthError.innerHTML == "" &&
			senceError.innerHTML == "") {
			info['name'] = unitName.innerText;
			console.log(unitName.innerText);
			console.log(info['name']);
			if(unitLength.value) {
				info['length'] = parseFloat(unitLength.value);
			} else {
				info['length'] = 0.0;
			}
			if(unitWidth.value) {
				info['width'] = parseFloat(unitWidth.value);
			} else {
				info['width'] = 0.0;
			}
			info['description'] = unitSence.value;
			console.log(info);
			jQuery.ajax({
				type: "POST",
				url: "http://192.168.100.32:8036/user/resetUnit",
				data: info,
				dataType: "text",
				xhrFields: {
					withCredentials: true
				},
				success: function(data) {
					var result = JSON.parse(data);
					if(result["msgType"] == "UserResetUnit") {
						if(result["content"] == "success") {
							unit.length = unitLength.value;
							unit.width = unitWidth.value;
							unit.sence = unitSence.value;
							btnEditInfo.click();
						} else {
							console.log("tokenError");
						}
					}
					setCookie("token", result["token"], 100);
				},
				error: function() {
					console.log("error");
				}
			})
		} else {
			alert("填写错误！");
		}

	};

	/**
	 * 设置imag的显示与否
	 */
	function ifEditImgDisplay(objs, status) {
		for(var i = 0; i < objs.length; i++) {
			objs[i].style.display = status;
		}
	}

	/**
	 * 绑定响应事件，是否解绑设备
	 */
	function btnDeleteFromUnit(obj) {
		obj.onclick = function() {
			/*
			 * 确定删除？？
			 * yes：发送给服务器
			 */
			var choice = window.confirm("确定删除吗？");
			var info = {};
			info['token'] = getCookie("token");
			info['email'] = getCookie("current_email");
			if(choice == true) {
				info['unit'] = unitName.innerText;
				info['mac'] = this.parentNode.firstChild.firstChild.innerHTML;
				var li = this.parentNode.parentNode;
				jQuery.ajax({
					type: "POST",
					url: "http://192.168.100.32:8036/user/delDevsInUnit",
					data: info,
					dataType: "text",
					xhrFields: {
						withCredentials: true
					},
					success: function(data) {
						var result = JSON.parse(data);
						if(result["msgType"] == "UserDelDevsInUnit") {
							if(result["content"] == "success") {
								ulDevice.removeChild(li); //删除这个节点
								btnEditDevice.innerText = "编辑";
							} else {
								console.log("tokenError");
							}
						}
						setCookie("token", result["token"], 100);
					},
					error: function() {
						console.log("error");
					}
				})
			}
		}
	}
	/**
	 * 绑定响应事件，控制开发板的发送数据的开关
	 */
	function sendSwitchToServer(obj) {
		obj.onclick = function() {
			//加载层-默认风格
			var index;
			var info = {};
			
			info['mac'] = this.parentNode.firstChild.firstChild.innerHTML;
			info['token'] = getCookie("token");
			info['email'] = getCookie("current_email");
			jQuery.ajax({
				type: "POST",
				url: "http://192.168.100.32:8036/user/sendSensorOrder",
				data: info,
				dataType: "text",
				xhrFields: {
					withCredentials: true
				},
				beforeSend: function() {
					index = layer.msg('正在打开中，请稍候', {
						icon: 16,
						time: false,
						shade: 0.8
					});
				},
				success: function(data) {
					var result = JSON.parse(data);
					if(result["msgType"] == "UserSendSensorOrder") {
						if(result["content"] == "success") {
							layer.close(index);
							layer.msg('打开成功', {
								time: 2000,
								shade: 0.8
							});
						} else if(result["content"] == "noSensor") {
							layer.msg('该开发板下没有传感器', {
								time: 2000,
								shade: 0.8
							});
						} else {
							console.log("tokenError");
							layer.msg('身份认证失败', {
								time: 2000,
								shade: 0.8
							});
						}
					}
					setCookie("token", result["token"], 100);
				},
				error: function() {
					console.log("error");
					layer.msg('打开失败', {
						time: 2000,
						shade: 0.8
					});
				}
			})
		}
	}
	iframeResize();
}