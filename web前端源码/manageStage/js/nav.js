window.onload = function() {
	//获取add和user,及相应的menu
	var btn_add = document.getElementById("details-add");
	var btn_user = document.getElementById("details-user");
	var menu_add = document.getElementById("dropDownAdd");
	var menu_user = document.getElementById("dropDownUser");
	//获取头像
	var nav_userimage = document.getElementById("nav_userimage");
	//获取对象，依次单元、设备、数据管理
	var navigations = document.getElementsByClassName("js-selected-navigation-item");

	//获取menu中的元素,
	var menus_add = document.getElementsByClassName("dropdown-item-add");
	var menus_user = document.getElementsByClassName("dropdown-item-user");
	var menus_deviceManager = document.getElementsByClassName("dropDeviceManager_item");
	var menus_dataManager = document.getElementsByClassName("dropDataManager_item");
	var deviceManager = document.getElementById("dropDeviceManager");
	var dataManager = document.getElementById("dropDataManager");

	var iframe = document.getElementById("main_iframe");
	/**
	 * 设置点击add或者user,menu的显示与否
	 */
	function displayNoneOrBlock(btn, menu1, menu2, menu3, menu4) {
		btn.onclick = function() {
			if(menu1.style.display == 'none') {
				if(menu2.style.display == 'block') {
					menu2.style.display = 'none';
				}
				if(menu3.style.display == 'block') {
					menu3.style.display = 'none';
				}
				if(menu4.style.display == 'block') {
					menu4.style.display = 'none';
				}
				menu1.style.display = 'block';
			} else {
				menu1.style.display = 'none';
			}
		};
	}
	displayNoneOrBlock(btn_add, menu_add, menu_user, deviceManager, dataManager);
	displayNoneOrBlock(btn_user, menu_user, menu_add, deviceManager, dataManager);
	displayNoneOrBlock(navigations[1], deviceManager, menu_user, menu_add, dataManager);
	displayNoneOrBlock(navigations[2], dataManager, deviceManager, menu_user, menu_add);
	/**
	 * 设置点击具体元素，iframe的显示
	 */
	var iframe_arr = ["show_unit.html", "show_device.html", "show_sensor.html",
		"show_current_data.html", "show_history_data.html", "add_unit.html", "add_sensor.html",
		"show_settings.html", "show_log.html"
	];
	setIframeHeight(iframe);

	function displayIframe(btn, n) {
		btn.onclick = function() {
			/**
			 * 加载页面等待
			 */
			iframe.src = iframe_arr[n];

			setIframeHeight(iframe);
			/**
			 * 点击其他元素，使menu消失
			 */
			if(menu_add.style.display == 'block') {
				menu_add.style.display = 'none';
			}
			if(menu_user.style.display == 'block') {
				menu_user.style.display = 'none';
			}
			if(deviceManager.style.display == 'block') {
				deviceManager.style.display = 'none';
			}
			if(dataManager.style.display == 'block') {
				dataManager.style.display = 'none';
			}
		};
	}
	displayIframe(navigations[0], 0);
	displayIframe(menus_deviceManager[0], 1);
	displayIframe(menus_deviceManager[1], 2);
	displayIframe(menus_dataManager[0], 3);
	displayIframe(menus_dataManager[1], 4);
	displayIframe(menus_add[0], 5);
	displayIframe(menus_add[2], 6);
	displayIframe(menus_user[2], 7);
	displayIframe(menus_user[3], 8);
	/**
	 * iframe高度自适应
	 * @param {Object} iframe
	 */
	function setIframeHeight(iframe) {
		if(iframe) {
			var iframeWin = iframe.contentWindow || iframe.contentDocument.parentWindow;
			if(iframeWin.document.body) {
				iframe.height = iframeWin.document.documentElement.scrollHeight || iframeWin.document.body.scrollHeight;
			}
		}
	}

	getUserImageName();
	/**
	 * 用户名和头像的显示(menus_user[0]、btn_user.src)
	 */
	function getUserImageName() {
		var info = {};
		info['token'] = getCookie("token");
		info['email'] = getCookie("current_email");
		console.log("send getUserImageName:" + info['token']);
		jQuery.ajax({
			async: false,
			type: "POST",
			url: "http://192.168.100.32:8036/user/getUserInfo",
			data: info,
			dataType: "text",
			xhrFields: {
				withCredentials: true
			},
			success: function(data) {
				var result = JSON.parse(data);
				if(result["msgType"] == "UserGetUserInfo") {
					if(result["message"] == "success") {
						/**
						 * 得到用户名和头像
						 */
						var information = JSON.parse(result.content);
						nav_userimage.src = information['userAvatar'];
						menus_user[0].innerHTML = information['userName'];
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

	var addDevice_submit = document.getElementById("addDevice_submit");
	var index;

	menus_add[1].onclick = function() {
		menu_add.style.display = 'none';
		showAddDevice();
	};
	addDevice_submit.onclick = addDevice;

	function showAddDevice() {
		index = layer.open({
			type: 1,
			title: "添加设备",
			area: ["395px", "180px"],
			content: $("#addDevice"),
			shadeClose: true,
		});
	}

	function addDevice() {
		var serialNum = $.trim($("#addDevice-InputSerial").val()); //获取序列号trim是去掉空格
		menu_add.style.display = 'none';
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
				async: false,
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
								icon: 5,
								time: 1000, //1秒后自动关闭
							});
							layer.close(index);
							/**
							 * 加载页面等待
							 */
							iframe.src = iframe_arr[1];
							setIframeHeight(iframe);
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
				}
			})
		}
	}

}