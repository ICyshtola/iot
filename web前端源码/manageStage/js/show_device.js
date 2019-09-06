window.onload = function() {
	//添加设备add
	var addDevice_submit = document.getElementById("addDevice_submit");
	var device_add = document.getElementById("device_add");
	//获取ul
	var ullist = document.getElementById("device_list");
	//调试命令提交
    var debug_btn = document.getElementById("debug");
	var index;

	device_add.onclick = showAddDevice;
	addDevice_submit.onclick = addDevice;

	getUserDevicesFromServer();
	/**
	 * 根据数据库，动态添加开发板
	 */
	//静态测试
	//ullist.appendChild(addDeviceLi("F4:5E:AB:57:F0:00", "单元1"));

	/**
	 * 请求服务器本人的开发板信息
	 */
	function getUserDevicesFromServer() {
		var info = {};
		info['token'] = getCookie("token");
		info['email'] = getCookie("current_email");
		console.log("send getUserDevicesFromServer:" + info['token']);
		jQuery.ajax({
			async: false,
			type: "POST",
			url: "http://192.168.100.32:8036/user/getDeviceInfo",
			data: info,
			dataType: "text",
			xhrFields: {
				withCredentials: true
			},
			success: function(data) {
				var result = JSON.parse(data);
				if(result["msgType"] == "UserGetDeviceInfo") {
					if(result["message"] == "success") {
						var content = JSON.parse(result["content"]);
						for(var i = 0; i < content.length; i++) {
							var data = content[i];
							if(data['deviceUnit'] == "") {
								data['deviceUnit'] = "无";
							}
							ullist.appendChild(addDeviceLi(data['deviceMac'], data['deviceUnit']));
						}
					} else if(result["message"] == "noDevice") {
						alert("还没有开发板设备呢，赶快去添加吧！");
					} else {
						console.log("tokenError");
						layer.alert("身份验证失败!", {
							title: "提示",
							icon: 5,
							time: 2000,
						});
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
	 * 动态创建一列开发板
	 * @param {Object} mac
	 * @param {Object} unit
	 */
	function addDeviceLi(mac, unit) {
		var li = document.createElement("li");
		var div = document.createElement("div");
		var label_mac = document.createElement("label");
		var btn_delete = document.createElement("i");
		var btn_data = document.createElement("i");
		var btn_info = document.createElement("i");
		var btn_set = document.createElement("i");
		var p_unit = document.createElement("p");
		var a_mac = document.createElement("a");

		a_mac.href = "info_device.html";
		a_mac.className = "device_item_name";
		a_mac.innerHTML = mac;
		a_mac.title = "开发板的MAC地址";
		a_mac.onclick = function() {
			/**
			 * 传参数给详情界面，四个参数
			 */
			var toInfoDevice = "?deviceMacAddress=" + mac + "&deviceUnit=" + unit;
			a_mac.href += toInfoDevice;
			console.log(a_mac.href);
		}

		p_unit.className = "device_item_sence";
		p_unit.innerHTML = "采集单元："+unit;
		p_unit.title = "采集单元："+unit;
		
		btn_delete.className = "device_item_delete fa fa-trash";
		btn_delete.title = "删除";
		btnDeleteMySelf(btn_delete);

		btn_info.className = "device_item_info fa fa-power-off";
		btn_info.title = "发送数据";
		sendSwitchToServer(btn_info);

		btn_set.className = "device_item_set fa fa-cogs";
		btn_set.title = "调试传感器的命令";
		sendSettingsToServer(btn_set);
		
		btn_data.className = "device_item_showData fa fa-line-chart";
		btn_data.title = "数据显示";
		btn_data.onclick = function() {
			/**
			 * 跳转到数据显示界面，传递Unit和deviceMac
			 */
			var device = "show_current_data.html" + "?deviceMacAddress=" + mac +
				"&deviceUnit=" + unit;
			window.location.href = device;
			console.log(a_mac.href);
		}

		div.className = "device_item";

		li.className = "device-list-item";

		label_mac.appendChild(a_mac);
		div.appendChild(label_mac);
		div.appendChild(btn_delete);
		div.appendChild(btn_info);
		div.appendChild(btn_set);
		div.appendChild(btn_data);
		div.appendChild(p_unit);
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
			var choice = window.confirm("警告：删除该设备会把采集的数据全部删除，不可恢复，确定删除吗？");
			if(choice == true) {
				var delinfo = {};
				var mac = this.parentNode.firstChild.firstChild.innerHTML;
				delinfo['mac'] = mac;
				delinfo['email'] = getCookie("current_email");
				delinfo['token'] = getCookie("token");
				jQuery.ajax({
					type: "POST",
					url: "http://192.168.100.32:8036/user/delDevice",
					data: delinfo, //token,email,mac
					dataType: "text",
					xhrFields: {
						withCredentials: true
					},
					success: function(data) {
						var result = JSON.parse(data);
						if(result["msgType"] == "UserDelDevice") {
							if(result["content"] == "success") {
								/**
								 * 删除
								 */
								ullist.removeChild(li);
							} else if(result["content"] == "fail") {
								layer.alert("删除失败!已删除!", {
									title: "提示",
									icon: 7,
									time: 2000,
								});
							} else {
								console.log("tokenError");
								layer.alert("身份验证失败!", {
									title: "提示",
									icon: 5,
									time: 2000,
								});
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
	/**
	 * 绑定响应事件，控制开发板的发送数据的开关
	 */
	function sendSwitchToServer(obj) {
		obj.onclick = function() {
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
								time: 1800,
								shade: 0.8
							});
						} else if(result["content"] == "noSensor") {
							layer.close(index);
							layer.msg('该开发板下没有传感器', {
								time: 2000,
								shade: 0.8
							});
						} else {
							console.log("tokenError");
							layer.close(index);
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
					layer.close(index);
					layer.msg('打开失败', {
						time: 2000,
						shade: 0.8
					});
				}
			})
		}
	}
	/**
	 * 绑定调试传感器命令的响应事件
	 */
	function sendSettingsToServer(obj){
		var tips = document.getElementById("tips");
		obj.onclick = function(){
			layer.open({
				type: 1,
				title: "调试命令",
				area: ["800px", "330px"],
				content: $("#testOrder"),
				shadeClose: true,
				end:function(){
					tips.innerText = "";
				}
			});	
		}
		
		/**
		 * 点击确定调试按钮，向服务器发送信息，并接受返回数据显示
		 */
		debug_btn.onclick = function(){
			//var device_mac = document.getElementsByClassName("device_top")[0].getElementsByTagName("label")[0];
				// 获取传感器命令	
			var order_list = new Array();
			order_list = link_order(order_list);
			if(order_list.length == 8)
			{
				var info = new FormData();
				info.append("token",getCookie("token"));
				info.append("mac",obj.parentNode.firstChild.firstChild.innerHTML);
				info.append("order",order_list);
				
				jQuery.ajax({
						type: "POST",
						url: "http://192.168.100.32:8036/user/transOrder",
						processData: false,//不处理参数,图片，不加这一行会出现illegal错误
						contentType: false,//不加这一行会报400错误
						data: info,
						dataType: "text",
						xhrFields: {
							withCredentials: true
						},
						success:function(data) {
							var result = JSON.parse(data);
							if(result.content == "tokenError")	{
								alert("token错误");
							}
							else{
								tips.innerText = result.content;
								console.log(result);
	
							}
							setCookie("token",result.token,100);
						}
					})
			}
			else{
				layer.msg("请将命令填写完整");
			}
			
		}
		
		
		
		
	}
	/**
	 * 获取命令列表
	 * @param {Object} result_order
	 */
	function link_order(result_order)
	{
		var order_input_items = document.getElementsByClassName("input_items")[0].getElementsByTagName("input");
		[].forEach.call(order_input_items,function(order_input_item){
			if(order_input_item.value.length != 2)
			{
				return "";
			}
			else{
				var str = order_input_item.value;
				str = str.toUpperCase(str);
				result_order.push(str);
			}
		});
		return result_order;
	}

	//
	function showAddDevice() {
		index = layer.open({
			type: 1,
			title: "添加设备",
			area: ["395px", "180px"],
			content: $("#addDevice"),
			shadeClose: true,
		});
	}

	//添加设备
	function addDevice() {
		var serialNum = $.trim($("#addDevice-InputSerial").val()); //获取序列号trim是去掉空格
		if(serialNum == "") {
			layer.alert("序列号不能为空!", {
				title: "提示",
				icon: 5,
				time: 2000,
			});
		} else if(serialNum.length > 8 || serialNum.length < 8) {
			/**
			 * 检查是否=8
			 */
			layer.alert("序列号输入不正确!", {
				title: "提示",
				icon: 5,
				time: 2000,
			});
		} else {
			/**
			 * 连接服务器
			 */
			var info = {};
			info['token'] = getCookie("token");
			info['email'] = getCookie("current_email");
			info['serial'] = serialNum;
			jQuery.ajax({
				type: "POST",
				url: "http://192.168.100.32:8036/user/addDevice",
				data: info,
				dataType: "text",
				xhrFields: {
					withCredentials: true
				},
				success: function(data) {
					var result = JSON.parse(data);
					if(result["msgType"] == "UserAddDevice") {
						if(result["content"] == "success") {
							layer.alert("添加成功!", {
								title: "提示",
								icon: 6, //
								time: 1000, //1秒后自动关闭
							});
							layer.close(index);
							location.reload();
						} else if(result["content"] == "repeat") {
							layer.alert("开发板已被添加!", {
								title: "提示",
								icon: 5,
								time: 2000,
							});
						} else if(result["content"] == "fail") {
							layer.alert("开发板不存在!", {
								title: "提示",
								icon: 5,
								time: 2000,
							});
						} else {
							console.log("tokenError");
							layer.alert("身份验证失败!", {
								title: "提示",
								icon: 5,
								time: 2000,
							});
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
	}
	iframeResize();
}