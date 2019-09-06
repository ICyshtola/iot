window.onload = function() {
	//从device_list得到显示的deviceMac和deviceUnit
	var deviceName = document.getElementById("device_name");
	var deviceUnit = document.getElementById("device_select_unit");
	var deviceInterval = document.getElementsByTagName("input")[0];

	//获取基本信息的编辑按钮，提交
	var btnEditDeviceUnit = document.getElementsByTagName("button")[0];
	var btnSubmitDeviceUnit = document.getElementsByTagName("button")[1];

	var tableDeviceSensors = [];
	var tableDeviceEquipments = []; //实时更新的
	var device = {}; //记录当前值
	var allSensors = []; //记录当前所有的传感器

	//初始化
	deviceName.innerHTML = GetQueryString("deviceMacAddress");
	device.unit = GetQueryString("deviceUnit");
	getDeviceInteralFromServer();
	deviceInterval.value = device.dInterval; //时间频率

	if(device.unit != '无') { //需要添加完option之后才可以更改select的值
		var opt = document.createElement('option');
		opt.innerHTML = device.unit;
		deviceUnit.appendChild(opt);
		//或者deviceUnit.value = device.unit; 
		deviceUnit.options[1].selected = true;
	}


	/**
	 * 编辑按钮点击事件
	 */
	btnEditDeviceUnit.onclick = function() {
		if(btnSubmitDeviceUnit.style.display == 'none') {
			btnSubmitDeviceUnit.style.display = 'block';
			deviceUnit.disabled = false;
			deviceInterval.readOnly = false;
			this.innerHTML = '取消';
			/**
			 * 请求服务器的所有采集单元信息，给用户选择
			 * 添加到下拉框
			 */
			getUserUnitsFromServer();
		} else {
			btnSubmitDeviceUnit.style.display = 'none';
			deviceUnit.disabled = true;
			deviceInterval.readOnly = true;
			this.innerHTML = '编辑';
			deviceUnit.value = device.unit;
			deviceInterval.value = device.dInterval;
		}
	};
	/**
	 * 提交按钮点击事件
	 */
	btnSubmitDeviceUnit.onclick = function() {
		var info = {};
		info['token'] = getCookie("token");
		info['mac'] = deviceName.innerHTML;
		if(deviceUnit.value == '无') {
			info['unit'] = "";
		} else {
			info['unit'] = deviceUnit.value;
		}
		if(deviceInterval.value > 60 ||
			deviceInterval.value < 1 ||
			parseInt(deviceInterval.value) != deviceInterval.value) {
			alert("频率是在1-60之间的整数！");
		} else {
			info['interval'] = deviceInterval.value;
			info['email'] = getCookie("current_email");
			console.log("send resetDeviceUnitandInterval：" + info);
			jQuery.ajax({
				type: "POST",
				url: "http://192.168.100.32:8036/device/resetDeviceUnitandInterval",
				data: info,
				dataType: "text",
				xhrFields: {
					withCredentials: true
				},
				success: function(data) {
				
					var result = JSON.parse(data);
					if(result["msgType"] == "UserResetDeviceUnitandInterval") {
						if(result["content"] == "success") {
							device.unit = deviceUnit.value;
							device.dInterval = deviceInterval.value;
							btnEditDeviceUnit.click();
							layer.alert("添加成功!", {
								title: "提示",
								icon: 6,
								time: 2000, //1秒后自动关闭
							});
						} else {
							console.log("tokenError");
							alert("error"); //为了测试
						}
					}
					setCookie("token", result["token"], 100);
				},
				error: function() {
					console.log("error");
				}
			})
		}

	};

	getDeviceSensorsFromServer(); //初始化数据
	getDeviceEquipmentsFromServer(); //初始化数据
	var sensorsTable;
	var equipmentsTable;
	//列表的初始化显示，添加、修改、删除操作
	/**
	 * 传感器操作步骤：
	 * 	1初始化，服务器拉取数据
	 *  2添加：（3个接口，至少两个）
	 * 		a.下拉框的组装
	 * 		b.选中之后，发送
	 * 		c.添加成功，查找全部传感器列表，得到传感器的全部数据，显示
	 *  3删除：
	 * 		a.发送
	 * 		b.删除成功
	 */
	/**
	 * 强电设备操作：
	 * 	1初始化，服务器拉取数据
	 *  2添加：
	 *      a.发送
	 * 		b.删除成功
	 *  3删除：
	 * 		a.发送
	 * 		b.删除成功
	 */
	layui.use(['table', 'jquery', 'form'], function() {

		sensorsTable = layui.table;
		equipmentsTable = layui.table;
		var form = layui.form;
		//添加按钮，提交
		var device_sensor_add = document.getElementById("device_sensor_add");
		var deviceAddSensor_submit = document.getElementById("deviceAddSensor_submit");
		var device_equipment_add = document.getElementById("device_equipment_add");
		var deviceAddEquipment_submit = document.getElementById("deviceAddEquipment_submit");
		//sensor的下拉框
		var deviceSelectSensor = document.getElementById("deviceAddSensor-InputSensor");
		var index; //自动关闭弹出窗口
		//监控传感器
		var selectMiniSensor = document.getElementById("deviceMonitorSensor-SelectMiniSensor");
		var miniSensorMax = document.getElementById("deviceMonitorSensor_max");
		var miniSensorMin = document.getElementById("deviceMonitorSensor_min");
		var ulInputSensorMaxOrder = document.getElementById("deviceMonitorSensor_addMaxOrder_list");
		var ulInputSensorMinOrder = document.getElementById("deviceMonitorSensor_addMinOrder_list");
		var ulSelectEquipment = document.getElementById("deviceMonitorSensor_addEquipment_list");
		var btnAddMaxOrder = document.getElementById("deviceMonitorSensor_addMaxOrder");
		var btnAddMinOrder = document.getElementById("deviceMonitorSensor_addMinOrder");
		var btnAddEquipment = document.getElementById("deviceMonitorSensor_addEquipment");
		var btnMonitorSensor = document.getElementById("deviceMonitorSensor_submit");
		var sensorWhichMonitor;
		/**
		 * 传感器列表
		 */
		sensorsTable.render({
			elem: '#device_sensors_table'
				//      , url: 'index.json'
				,
			data: tableDeviceSensors,
			id: "device_sensors_table",
			cellMinWidth: 200,
			page: true,
			cols: [
				[{
					type: 'numbers'
				}, {
					field: 'sensorName',
					title: '名称',
					width: 230,
				}, {
					field: 'sensorOrder',
					width: 250,
					title: '命令'
				}, {
					field: 'sensorMiniSensor',
					width: 430,
					title: '最小单位的传感器',
					templet: function(d) {
						var minis = JSON.parse(d.sensorMinisensor);
						//						console.log(minis);
						var list = " ";
						for(var i = 0; i < minis.length; i++) {
							var mini = JSON.parse(minis[i]);
							//							console.log(mini);
							list = list + mini.name + "(" + mini.unit + ")" + ";    ";
						}
						return list;
					}
				}, {
					fixed: 'right',
					width: 160,
					title: '其他操作',
					align: 'center',
					toolbar: '#device_sensors_table_barDemo'
				}]
			],
			done: function(res, curr, count) {
				iframeResize();
			}
		});
		//监听行工具事件
		sensorsTable.on('tool(device_sensors_table)', function(obj) { //注：tool 是工具条事件名，test 是 table 原始容器的属性 lay-filter="对应的值"
			var data = obj.data,
				layEvent = obj.event; //获得 lay-event 对应的值 //获得当前行数据
			//			console.log(obj.data);
			if(layEvent === 'del') {
				layer.confirm('真的删除行么', function(index) {
					//向服务端发送删除指令
					var info = {};
					info['token'] = getCookie("token");
					info['mac'] = deviceName.innerHTML;
					info['email'] = getCookie("current_email");
					info['sensor'] = obj.data.sensorName;
					jQuery.ajax({
						type: "POST",
						url: "http://192.168.100.32:8036/device/delDeviceSensor",
						data: info,
						dataType: "text",
						xhrFields: {
							withCredentials: true
						},
						success: function(data) {
							
							var result = JSON.parse(data);
							if(result["msgType"] == "UserDelDeviceSensor") {
								if(result["content"] == "success") {
									obj.del(); //删除对应行（tr）的DOM结构
									layer.close(index);
									for(var i = 0; i < tableDeviceSensors.length; i++) {	
										if(tableDeviceSensors[i]["sensorName"] == obj.data["sensorName"]) {
											tableDeviceSensors.splice(i, 1);
											break;
										}
									}
								} else {
									console.log("tokenError");
									alert("身份验证失败！");
								}
							}
							setCookie("token", result["token"], 100);
						},
						error: function() {
							console.log("error");
							alert("删除失败！");
						}
					})
				});
			} else
			if(layEvent === 'monitor') {
				sensorWhichMonitor = obj.data.sensorName;
				showMonitorSensor(JSON.parse(obj.data.sensorMinisensor), tableDeviceEquipments);
			}
		});
		//按钮点击事件的绑定
		device_sensor_add.onclick = showAddSensor;
		deviceAddSensor_submit.onclick = deviceAddSensor;
		device_equipment_add.onclick = showAddEquipment;
		deviceAddEquipment_submit.onclick = deviceAddEquipment;
		btnMonitorSensor.onclick = deviceMonitorSensor;

		/**
		 * 强电设备列表
		 */
		equipmentsTable.render({
			elem: '#device_equipments_table'
				//      , url: 'index.json'
				,
			data: tableDeviceEquipments,
			id: "device_equipments_table",
			cellMinWidth: 200,
			page: true,
			cols: [
				[{
					type: 'numbers'
				}, {
					field: 'equipmentName',
					title: '名称',
					width: 110,
					align: 'center'
				}, {
					field: 'equipmentRelay',
					width: 120,
					title: '继电器线路',
					edit: 'text',
					align: 'center'
				}, {
					field: 'equipmentOpenOrder',
					width: 150,
					title: '开启命令',
					edit: 'text',
					align: 'center'
				}, {
					field: 'equipmentCloseOrder',
					width: 150,
					title: '关闭命令',
					edit: 'text',
					align: 'center'
				}, {
					field: 'equipmentQueryOrder',
					width: 150,
					title: '查询命令',
					edit: 'text',
					align: 'center'
				}, {
					field: 'equipmentStatus',
					width: 105,
					title: '状态',
					align: 'center',
					left: '关'
				}, {
					field: 'equipmentSwitch',
					width: 120,
					title: '开关操作',
					align: 'center',
					templet: '#device_equipments_table_switchTpl'
				}, {
					fixed: 'right',
					width: 160,
					title: '其他操作',
					align: 'center',
					toolbar: '#device_equipments_table_barDemo'
				}]
			],
			done: function(res, curr, count) {
				iframeResize();
			}
		});
		//监听行工具事件
		equipmentsTable.on('tool(device_equipments_table)', function(obj) { //注：tool 是工具条事件名，test 是 table 原始容器的属性 lay-filter="对应的值"
			var data = obj.data,
				layEvent = obj.event; //获得 lay-event 对应的值 //获得当前行数据
			if(layEvent === 'del') {
				layer.confirm('真的删除行么', function(index) {
					//向服务端发送删除指令
					var info = {};
					info['token'] = getCookie('token');
					info['email'] = getCookie('current_email');
					info['mac'] = deviceName.innerHTML;
					info['name'] = obj.data.equipmentName;
					jQuery.ajax({
						type: "POST",
						url: "http://192.168.100.32:8036/user/delEquipment",
						data: info,
						dataType: "text",
						xhrFields: {
							withCredentials: true
						},
						success: function(data) {
							
							var result = JSON.parse(data);
							if(result["msgType"] == "UserDelDeviceEquipment") {
								if(result["content"] == "success") {
									obj.del(); //删除对应行（tr）的DOM结构
									layer.close(index);
									
									for(var i = 0; i < tableDeviceEquipments.length; i++) {
										if(tableDeviceEquipments[i]["equipmentName"] == obj.data["equipmentName"]) {
											tableDeviceEquipments.splice(i, 1);
											break;
										}
									}
								} else if(result["message"] == "noEquipment") {
									alert("该强电设备不存在！");
								} else {
									console.log("tokenError");
									layer.close(index);
									alert("身份验证失败！"); //为了测试
								}
							}
							setCookie("token", result["token"], 100);
						},
						error: function() {
							console.log("error");
							alert("删除失败！");
						}
					})
				});
			}
		});
		//监听单元格编辑
		equipmentsTable.on('edit(device_equipments_table)', function(obj) {
			var value = obj.value,
				newE = obj.data,
				field = obj.field; //得到修改后的值//得到所在行所有键值//得到字段

			if(field == 'equipmentRelay' ||
				field == 'equipmentOpenOrder' ||
				field == 'equipmentCloseOrder' ||
				field == 'equipmentQueryOrder') {

				/**
				 * 比较前后的继电器电路和开关操作
				 */
				//继电器和命令
				if(newE.equipmentRelay > 16 || newE.equipmentRelay < 1) {
					/**
					 * 判断字符是否为1-16
					 */
					layer.alert("强电设备的继电器线路是1-16的整数", {
						title: "提示",
						icon: 5,
						time: 2000,
					});
				} else if(!orderIsTrueOrNot(newE.equipmentOpenOrder) ||
					!orderIsTrueOrNot(newE.equipmentCloseOrder) ||
					!orderIsTrueOrNot(newE.equipmentQueryOrder)) {
					layer.alert("命令输入格式为[xx,xx,xx,xx,xx,xx,xx,xx]!", {
						title: "提示",
						icon: 5,
						time: 2000,
					});
				} else {
					layer.confirm('确认继电器信息、3个命令信息修改?', {
						btn: ['确定', '取消'],
						icon: 3,
						title: "提示"
					}, function() {
						/**
						 * 比较命令是否更改否则不予更改,反之发送给服务器更新
						 */
						var info = {};
						info['token'] = getCookie('token');
						info['email'] = getCookie('current_email');
						info['mac'] = deviceName.innerHTML;
						info['name'] = newE.equipmentName;
						info['relay'] = newE.equipmentRelay;
						info['open_order'] = newE.equipmentOpenOrder;
						info['close_order'] = newE.equipmentCloseOrder;
						info['query_order'] = newE.equipmentQueryOrder;
						console.log(info);
						jQuery.ajax({
							type: "POST",
							url: "http://192.168.100.32:8036/user/resetEquipment",
							data: info,
							dataType: "text",
							xhrFields: {
								withCredentials: true
							},
							success: function(data) {
								var result = JSON.parse(data);
								if(result["msgType"] == "UserResetDeviceEquipment") {
									if(result["content"] == "success") {
										/**
										 * 刷新界面
										 */
										layer.alert("修改成功！", {
											title: "提示",
											icon: 6,
											time: 2000,
										});
									} else {
										console.log("tokenError");
										layer.close(index);
										getDeviceEquipmentsFromServer();
										equipmentsTable.reload("device_equipments_table", {
											data: tableDeviceEquipments,
											done: function(res, curr, count) {
												iframeResize();
											}
										});
										alert("身份验证失败！");
									}
								}
								setCookie("token", result["token"], 100);
							},
							error: function() {
								console.log("error");
								layer.close(index);
								getDeviceEquipmentsFromServer();
								equipmentsTable.reload("device_equipments_table", {
									data: tableDeviceEquipments,
									done: function(res, curr, count) {
										iframeResize();
									}
								});
								alert("修改失败！");
							}
						})
					}, function(index, layero) {
						layer.close(index);
						getDeviceEquipmentsFromServer();
						equipmentsTable.reload("device_equipments_table", {
							data: tableDeviceEquipments,
							done: function(res, curr, count) {
								iframeResize();
							}
						});
					});
				}
			}

		});
		//监听开关
		form.on('switch(statusDemo)', function(data) {
			var index_sms;
			var switchData = data;
			var alert_value = switchData.elem.checked;
			// 获取当前控件                                                                  
			var selectIfKey = switchData.othis;
			// 获取当前所在行                                                                 
			var parentTr = selectIfKey.parents("tr");
			//eq(2): 代表的是表头字段位置    .layui-table-cell: 这个元素是我找表格div找出来的..
			var dataField = $(parentTr).find("td:eq(2)").find(".layui-table-cell").text();
			//			console.log(tableDeviceEquipments[dataField - 1]);
			var equip = tableDeviceEquipments[dataField - 1];
			var str = "确定切换" + equip.equipmentName + "的状态吗？";
			layer.open({
				content: str,
				btn: ['确定', '取消'],
				yes: function(index, layero) {
					layer.close(index);
					//通知状态更新
					var info = {};
					info['token'] = getCookie("token");
					info['email'] = getCookie("current_email");
					info['mac'] = deviceName.innerHTML;
					info['status'] = alert_value;
					info['name'] = equip.equipmentName;
					jQuery.ajax({
						type: "POST",
						url: "http://192.168.100.32:8036/user/controlEquipment",
						data: info,
						dataType: "text",
						xhrFields: {
							withCredentials: true
						},
						beforeSend: function() {
							index_sms = layer.msg('正在切换中，请稍候', {
								icon: 16,
								time: false,
								shade: 0.8
							});
						},
						success: function(data) {
							var result = JSON.parse(data);
							if(result["msgType"] == "UserControlEquipment") {
								if(result["content"] == "close" ||
									result["content"] == "open") {
									setTimeout(function() {
										layer.close(index_sms);
										layer.msg('操作成功！');
										switchData.elem.checked = alert_value;
										tableDeviceEquipments[dataField - 1].equipmentStatus = alert_value ? '开' : '关';
										equipmentsTable.reload("device_equipments_table", {
											data: tableDeviceEquipments
										});
										form.render();
									}, 2000);
								} else if(result["content"] == "closeFail" ||
									result["content"] == "openFail") {
									setTimeout(function() {
										layer.close(index_sms);
										layer.msg('操作失败！');
										switchData.elem.checked = !alert_value;
										form.render();
									}, 2000);
								} else if(result["content"] == "deviceNoResponse") {
									setTimeout(function() {
										layer.close(index_sms);
										layer.msg('设备无响应！');
										switchData.elem.checked = !alert_value;
										form.render();
									}, 2000);
								} else {
									console.log("tokenError");
									setTimeout(function() {
										layer.close(index_sms);
										layer.msg('身份验证失败！');
										switchData.elem.checked = !alert_value;
										form.render();
									}, 2000);
								}
							}
							setCookie("token", result["token"], 100);
						},
						error: function(data) {
							console.log(data);
							layer.msg('数据异常，操作失败！');
						},
					});
					//按钮【按钮一】的回调
				},
				btn2: function(index, layero) {
					//按钮【按钮二】的回调
					data.elem.checked = !alert_value;
					form.render();
					layer.close(index);
					//return false 开启该代码可禁止点击该按钮关闭
				},
				cancel: function() {
					//右上角关闭回调
					data.elem.checked = !alert_value;
					form.render();
					//return false 开启该代码可禁止点击该按钮关闭
				}
			});

			return false;
		});
		//添加传感器的弹出层
		function showAddSensor() {
			getUserSensorsFromServer(); //得到该用户的全部的传感器
			//			console.log(allSensors.length);
			//			console.log(tableDeviceSensors.length);
			for(var i = 0; i < allSensors.length; i++) {
				var isIn = false; //是否已经在列表中
				for(var j = 0; j < tableDeviceSensors.length; j++) {
					if(allSensors[i]['sensorName'] == tableDeviceSensors[j]['sensorName']) {
						isIn = true;
						break;
					}
				}
				if(!isIn) {
					var oldOpt = deviceSelectSensor.children;
					var isInSelect = false;
					for(var j = 0; j < oldOpt.length; j++) {
						if(oldOpt[j].innerHTML == allSensors[i]['sensorName']) {
							isInSelect = true;
							break;
						}
					}
					if(!isInSelect) {
						var opt = document.createElement("option");
						opt.innerHTML = allSensors[i]['sensorName'];
						deviceSelectSensor.appendChild(opt);
					}
				}
			}
			if(deviceSelectSensor.options.length == 0) {
				alert("还没有可以添加的传感器！");
			} else {
				index = layer.open({
					type: 1,
					title: "为设备添加传感器",
					area: ["362px", "134px"],
					content: $("#deviceAddSensor"),
					shadeClose: true,
				});
			}
		}
		//监控传感器的弹出层
		function showMonitorSensor(miniSensors, equipments) {
			/**
			 * 组装下拉框
			 */
			if(equipments.length == 0) {
				layer.msg("您还没有强电设备可以控制！赶快去添加吧！");
			} else {
				/**
				 * 初始化
				 */
				selectMiniSensor.innerHTML = "";
				ulInputSensorMaxOrder.innerHTML = "";
				ulInputSensorMinOrder.innerHTML = "";
				ulSelectEquipment.innerHTML = "";
				miniSensorMax.value = undefined;
				miniSensorMin.value = undefined;
				for(var i = 0; i < miniSensors.length; i++) {
					var d = JSON.parse(miniSensors[i]);
					var isIn = false;
					for(var j = 0; j < selectMiniSensor.children.length; j++) {
						if(selectMiniSensor.children[j].innerHTML == d.name) {
							isIn = true;
							break;
						}
					}
					if(!isIn) {
						var opt = document.createElement("option");
						opt.innerHTML = d.name;
						selectMiniSensor.appendChild(opt);
					}
				}
				var n = {
					maxOrder: equipments.length,
					minOrder: equipments.length,
					equipment: equipments.length
				}; //最多可添加数量
				//点击事件的绑定
				btnAddMaxOrder.onclick = function() {
					if(n.maxOrder == 0) {
						layer.msg("您最多可添加的数量等于" + equipments.length + "！");
					} else {
						/**
						 * ulInputSensorMaxOrder表中添加input元素
						 */
						var li = document.createElement("li");
						var label = document.createElement("label");
						var inputOrder = document.createElement("input");
						inputOrder.type = "text";
						inputOrder.placeholder = "[00,00,00,00,00,00,00,00]";
						li.appendChild(label);
						li.appendChild(inputOrder);
						label.innerHTML = "最大值命令：";
						inputOrder.className = "deviceMonitorSensor_MaxOrder";
						ulInputSensorMaxOrder.appendChild(li);
						n.maxOrder -= 1;
					}
				};
				btnAddMinOrder.onclick = function() {
					if(n.minOrder == 0) {
						layer.msg("您最多可添加的数量等于" + equipments.length + "！");
					} else {
						/**
						 * ulInputSensorMinOrder表中添加input元素
						 */
						var li = document.createElement("li");
						var label = document.createElement("label");
						var inputOrder = document.createElement("input");
						inputOrder.type = "text";
						inputOrder.placeholder = "[00,00,00,00,00,00,00,00]";
						li.appendChild(label);
						li.appendChild(inputOrder);
						label.innerHTML = "最小值命令：";
						inputOrder.className = "deviceMonitorSensor_MinOrder";
						ulInputSensorMinOrder.appendChild(li);
						n.minOrder -= 1;
					}
				};
				btnAddEquipment.onclick = function() {
					if(n.equipment == 0) {
						layer.msg("您最多可添加的数量等于" + equipments.length + "！");
					} else {
						/**
						 * ulSelectEquipment表中添加selectEquipment元素
						 */
						var selectEquipment = document.createElement("select");
						var lSelect = document.createElement("li");
						for(var i = 0; i < equipments.length; i++) {
							var opt = document.createElement("option");
							opt.innerHTML = equipments[i].equipmentName;
							selectEquipment.appendChild(opt);
						}
						selectEquipment.className = "deviceMonitorSensor_selectEquipment";
						lSelect.appendChild(selectEquipment);
						ulSelectEquipment.appendChild(lSelect);
						n.equipment -= 1;
					}
				};
				btnAddMaxOrder.click();
				btnAddMinOrder.click();
				btnAddEquipment.click();
				
				index = layer.open({
					type: 1,
					title: "监控传感器设置",
					area: ['400px', '520px'],
					content: $("#deviceMonitorSensor"),
					shadeClose: true
				});
			}
		}

		//添加强电设备的弹出层
		function showAddEquipment() {
			index = layer.open({
				type: 1,
				title: "添加强电设备",
				area: ["393px", "367px"],
				content: $("#deviceAddEquipment"),
				shadeClose: true,
			});
		}

		//添加传感器的提交事件
		function deviceAddSensor() {
			var optIndex = deviceSelectSensor.value;
			/**
			 * 发送给服务器
			 */
			var info = {};
			info['token'] = getCookie("token");
			info['mac'] = deviceName.innerHTML;
			info['email'] = getCookie("current_email");
			info['sensor'] = optIndex;
			jQuery.ajax({
				type: "POST",
				url: "http://192.168.100.32:8036/device/addDeviceSensor",
				data: info,
				dataType: "text",
				xhrFields: {
					withCredentials: true
				},
				success: function(data) {
					var result = JSON.parse(data);
					if(result["msgType"] == "UserAddDeviceSensor") {
						if(result["content"] == "success") {
							allSensors.forEach(function(v) {
								if(v.sensorName == info['sensor']) {
									tableDeviceSensors.push(v);
									return false;
								}
							});

							sensorsTable.reload("device_sensors_table", {
								data: tableDeviceSensors,
								done: function(res, curr, count) {
									iframeResize();
								}
							});

							layer.close(index);
							/**
							 * 从下拉框中把它删除
							 */
							var opts = deviceSelectSensor.options;
							for(var i = 0; i < opts.length; i++) {
								console.log(opts[i].innerHTML);
								if(opts[i].innerHTML == info['sensor']) {
									deviceSelectSensor.removeChild(opts[i]);
									break;
								}
							}
						} else {
							console.log("tokenError");
							alert("身份验证失败！"); //为了测试
						}
					}
					setCookie("token", result["token"], 100);
				},
				error: function() {
					console.log("error");
					alert("添加失败！");
				}
			})

		}

		//添加强电设备的提交事件
		function deviceAddEquipment() {
			var equipmentName = $.trim($("#deviceAddEquipment_name").val());
			var equipmentRelay = $.trim($("#deviceAddEquipment_relay").val()); //转为Int类型
			var equipmentOpenOrder = $.trim($("#deviceAddEquipment_openOrder").val());
			var equipmentCloseOrder = $.trim($("#deviceAddEquipment_closeOrder").val());
			var equipmentQueryOrder = $.trim($("#deviceAddEquipment_queryOrder").val());
			var info = {};
			if(equipmentName.length <= 0) {
				layer.alert("强电设备名称未填写", {
					title: "提示",
					icon: 5,
					time: 2000,
				});
				return false;
			}
			if(equipmentRelay > 16 || equipmentRelay < 1) {
				/**
				 * 判断字符是否为1-16
				 */
				layer.alert("强电设备的继电器线路是1-16的整数", {
					title: "提示",
					icon: 5,
					time: 2000,
				});
				return false;
			}
			if(!orderIsTrueOrNot(equipmentOpenOrder) ||
				!orderIsTrueOrNot(equipmentCloseOrder) ||
				!orderIsTrueOrNot(equipmentQueryOrder)) {
				layer.alert("命令输入格式不正确!", {
					title: "提示",
					icon: 5,
					time: 2000,
				});
				return false;
			}
			/**
			 * 检查输入的名称是否重复 
			 */
			var ishave = false;
			for(var i = 0; i < tableDeviceEquipments.length; i++) {
				if(tableDeviceEquipments[i]['equipmentName'] == equipmentName) {
					ishave = true;
					break;
				}
			}
			if(ishave) {
				layer.alert("强电设备名重复!", {
					title: "提示",
					icon: 5,
					time: 2000,
				});
				return false;
			}
			equipmentOpenOrder = equipmentOpenOrder.toUpperCase(equipmentOpenOrder);
			equipmentCloseOrder = equipmentCloseOrder.toUpperCase(equipmentCloseOrder);
			equipmentQueryOrder = equipmentQueryOrder.toUpperCase(equipmentQueryOrder);
			
			info['token'] = getCookie("token");
			info['email'] = getCookie("current_email");
			info['mac'] = deviceName.innerHTML;
			info['name'] = equipmentName;
			info['relay'] = equipmentRelay;
			info['open_order'] = equipmentOpenOrder;
			info['close_order'] = equipmentCloseOrder;
			info['query_order'] = equipmentQueryOrder;
			jQuery.ajax({
				type: "POST",
				url: "http://192.168.100.32:8036/device/addDeviceEquipment",
				data: info,
				dataType: "text",
				xhrFields: {
					withCredentials: true
				},
				success: function(data) {
					console.log(data);
					var result = JSON.parse(data);
					if(result["msgType"] == "UserAddDeviceEquipment") {
						if(result["content"] == "success") {
							var str = new Object;
							str.equipmentName = equipmentName;
							str.equipmentRelay = equipmentRelay;
							str.equipmentOpenOrder = equipmentOpenOrder;
							str.equipmentCloseOrder = equipmentCloseOrder;
							str.equipmentQueryOrder = equipmentQueryOrder;
							str.equipmentStatus = '关'; //默认
							tableDeviceEquipments.push(str);
							equipmentsTable.reload("device_equipments_table", {
								data: tableDeviceEquipments,
								done: function(res, curr, count) {
									iframeResize();
								}
							});
							layer.close(index);
							layer.alert("添加成功！", {
								title: "提示",
								icon: 6,
								time: 2000,
							});
						} else if(result["message"] == "repeat") {
							alert("该强电设备名重复！");
						} else {
							console.log("tokenError");
							alert("身份验证失败！"); //为了测试
						}
					}
					setCookie("token", result["token"], 100);
				},
				error: function() {
					console.log("error");
					alert("添加失败！");
				}
			})

		}

		//监控传感器的提交事件
		function deviceMonitorSensor() {
			/**
			 * 得到框中的值，进行逻辑判断，
			 * 1.最大值和最小值必填一个
			 * 2.最值填写，命令必写一个，
			 * 3.强电设备必选一个
			 */
			var minis = selectMiniSensor.value;
			var minisMax = miniSensorMax.value;
			var minisMin = miniSensorMin.value;
			//一组填写
			var lInputMax = document.getElementsByClassName("deviceMonitorSensor_MaxOrder"); //命令判断
			var lInputMin = document.getElementsByClassName("deviceMonitorSensor_MinOrder"); //命令判断
			var lSelectEquip = document.getElementsByClassName("deviceMonitorSensor_selectEquipment"); //得到强电设备的选择
			
			if((minisMax != '' || minisMin != '') && lSelectEquip.length > 0) {
				/**
				 * 准备判断并且封装数据
				 */
				var info = {};
				info['mac'] = deviceName.innerHTML;
				info['token'] = getCookie('token');
				info['email'] = getCookie('current_email');
				info['sensorName'] = sensorWhichMonitor;
				info['minisensorName'] = selectMiniSensor.value;
				info['equipmentName'] = "";
				var es = new Array;
				
				for(var i = 0; i < lSelectEquip.length; i++) {
					if(es.indexOf(lSelectEquip[i].value) == -1) {
						es.push(lSelectEquip[i].value);
					}
				}
				info['equipmentName'] = JSON.stringify(es);
				if(minisMax != '') {
					info['max'] = parseFloat(minisMax);
					if(lInputMax.length == 0) {
						layer.msg("设置了最大值，必须要设置最大值命令！");
						return;
					} else {
						/**
						 * 判断命令是否准确
						 */
						var maxO = "";
						for(var i = 0; i < lInputMax.length; i++) {
							if(orderIsTrueOrNot(lInputMax[i].value)) {
								if(maxO.length == 0) {
									maxO += '[' + lInputMax[i].value.slice(1, 24);
								} else {
									maxO += ',' + lInputMax[i].value.slice(1, 24);
								}
								continue;
							} else {
								layer.msg("最小值命令格式设置错误！");
								return;
							}
						}
						maxO += ']';
						maxO = maxO.toUpperCase(maxO);
						info['maxOrder'] = maxO;
						if(minisMin != '') {
							info['min'] = parseFloat(minisMin);
							if(lInputMin.length == 0) {
								layer.msg("设置了最小值，必须要设置最小值命令！");
								return;
							} else {
								var minO = "";
								for(var i = 0; i < lInputMin.length; i++) {
									if(orderIsTrueOrNot(lInputMin[i].value)) {
										if(minO.length == 0) {
											minO += '[' + lInputMin[i].value.slice(1, 24);
										} else {
											minO += ',' + lInputMin[i].value.slice(1, 24);
										}
										continue;
									} else {
										layer.msg("最小值命令格式设置错误！");
										return;
									}
								}
								minO += ']';
								minO = minO.toUpperCase(minO);
								info['minOrder'] = minO;
								sendMonitorInfoToServer(info);
							}
						} else {
							info['min'] = parseFloat(minisMax);
							info['minOrder'] = "";
							sendMonitorInfoToServer(info);
						}
					}
				} else {
					info['max'] = parseFloat(minisMin);
					info['maxOrder'] = "";
					info['min'] = parseFloat(minisMin);
					if(lInputMin.length == 0) {
						layer.msg("设置了最小值，必须要设置最小值命令！");
						return;
					} else {
						var minO = "";
						for(var i = 0; i < lInputMin.length; i++) {
							if(orderIsTrueOrNot(lInputMin[i].value)) {
								if(minO.length == 0) {
									minO += '[' + lInputMin[i].value.slice(1, 24);
								} else {
									minO += ',' + lInputMin[i].value.slice(1, 24);
								}
								continue;
							} else {
								layer.msg("最小值命令格式设置错误！");
								return;
							}
						}
						minO += ']';
						minO = minO.toUpperCase(minO);
						info['minOrder'] = minO;
						sendMonitorInfoToServer(info);
					}
				}
			} else {
				layer.msg("您的填写无效！最值必填一个并且强电设备必须选择一个，此次监控才有效！");
				return;
			}

		}
		/**
		 * 发送监控的数据给服务器
		 */
		function sendMonitorInfoToServer(info) {
			
			jQuery.ajax({
				type: "POST",
				url: "http://192.168.100.32:8036/user/setLocalControl",
				data: info,
				dataType: "text",
				xhrFields: {
					withCredentials: true
				},
				success: function(data) {
					var result = JSON.parse(data);
					if(result["msgType"] == "UserSetLocalControl") {
						layer.close(index);
						if(result["content"] == "insertSuccess") {
							layer.msg("监控设置成功！");
						} else if (result["content"] == "updateSuccess"){
							layer.msg("监控更新成功！");
						} else {
							alert("身份验证失败！");
						}
					}
					setCookie("token", result["token"], 100);
				},
				error: function() {
					console.log("error");
				}
			})
		}
		/**
		 * 检查强电设备命令输入是否正确
		 * [xx,xx,xx,xx,xx,xx,xx,xx]
		 */
		function orderIsTrueOrNot(order) {
			var regu = /^\[([\d|a-f|A-F]{2}\,){7}[\d|a-f|A-F]{2}\]$/;
			var re = new RegExp(regu);
			if(order.length > 25 || order.length == 0 ||
				order[0] != '[' || order[24] != ']' ||
				!re.test(order)) {
				return false;
			} else {
				return true;
			}
		}
	});
	/**
	 * 请求服务器本人的采集单元信息
	 */
	function getUserUnitsFromServer() {
		var info = {};
		info['token'] = getCookie("token");
		info['email'] = getCookie("current_email");
		jQuery.ajax({
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
						for(var i = 0; i < content.length; i++) {
							var oldOpt = deviceUnit.children;
							var isInSelect = false;
							for(var j = 0; j < oldOpt.length; j++) {
								if(oldOpt[j].innerHTML == content[i]['unitName']) {
									isInSelect = true;
									break;
								}
							}
							if(!isInSelect) {
								var opt = document.createElement("option");
								console.log(content[i]['unitName']);
								opt.innerHTML = content[i]['unitName'];
								deviceUnit.appendChild(opt);
							}
						}
					}
				}
				setCookie("token", result["token"], 100);
			},
			error: function() {
				console.log("error");
			}
		})
	}
	/**
	 * 请求服务器本人的传感器信息
	 */
	function getUserSensorsFromServer() {
		var info = {};
		info['token'] = getCookie("token");
		info['email'] = getCookie("current_email");
		jQuery.ajax({
			async: false,
			type: "POST",
			url: "http://192.168.100.32:8036/user/getSensorInfo",
			data: info,
			dataType: "text",
			xhrFields: {
				withCredentials: true
			},
			success: function(data) {
				var result = JSON.parse(data);
				if(result["msgType"] == "UserGetSensorInfo") {
					if(result["message"] == "success") {
						var content = JSON.parse(result['content']);
						allSensors = [];
						for(var i = 0; i < content.length; i++) {
							var isInSelect = false;
							for(var j = 0; j < allSensors.length; j++) {
								if(allSensors[j]['sensorName'] == content[i]["sensorName"]) {
									isInSelect = true;
									break;
								}
							}
							if(!isInSelect) {
								var newSensor = content[i];
								allSensors.push(newSensor);
							}
						}
					} else if(result["message"] == "noSensor") {
						alert("还没有加入传感器！");
					} else {
						console.log("tokenError");
						alert("身份验证失败！"); //为了测试
					}
				}
				setCookie("token", result["token"], 100);
			},
			error: function() {
				console.log("error");
			}
		})

	}

	/**
	 * 获取该设备的传感器信息
	 */
	function getDeviceSensorsFromServer() {
		var info = {};
		info['token'] = getCookie("token");
		info['email'] = getCookie("current_email");
		info['mac'] = deviceName.innerHTML;
		jQuery.ajax({
			async: false,
			type: "POST",
			url: "http://192.168.100.32:8036/user/getSensorInDevs",
			data: info,
			dataType: "text",
			xhrFields: {
				withCredentials: true
			},
			success: function(data) {
				var result = JSON.parse(data);
				if(result["msgType"] == "UserGetSensorInDevs") {
					if(result["message"] == "success") {
						var content = JSON.parse(result['content']);
						tableDeviceSensors = [];
						for(var i = 0; i < content.length; i++) {
							var newSensor = content[i];
							tableDeviceSensors.push(newSensor);
						}
					} else if(result["message"] == "noSensor") {
						alert("还没有加入传感器！");
					} else {
						console.log("tokenError");
						alert("身份验证失败！"); //为了测试
					}
				}
				setCookie("token", result["token"], 100);
			},
			error: function() {
				console.log("error");
			}
		})
	}
	/**
	 * 获取该设备的强电设备信息
	 */
	function getDeviceEquipmentsFromServer() {
		var info = {};
		info['token'] = getCookie("token");
		info['email'] = getCookie("current_email");
		info['mac'] = deviceName.innerHTML;
		jQuery.ajax({
			async: false,
			type: "POST",
			url: "http://192.168.100.32:8036/user/getEquipmentInDevs",
			data: info,
			dataType: "text",
			xhrFields: {
				withCredentials: true
			},
			success: function(data) {
				var result = JSON.parse(data);
				if(result["msgType"] == "UserGetEquipmentInDevs") {
					if(result["message"] == "success") {
						var j = 0;
						var content = JSON.parse(result['content']);
						tableDeviceEquipments = [];
						for(var i = 0; i < content.length; i++) {
							var newE = new Object;

							newE.equipmentName = content[i]['equipmentName'];
							newE.equipmentRelay = content[i]['equipmentRelay'] + "";
							if(content[i]['equipmentStatus']) {
								newE['equipmentStatus'] = '开';
							} else {
								newE['equipmentStatus'] = '关';
							}
							newE.equipmentOpenOrder = content[i]['equipmentOpenOrder'].replace(/\s*/g, "");
							newE.equipmentCloseOrder = content[i]['equipmentCloseOrder'].replace(/\s*/g, "");
							newE.equipmentQueryOrder = content[i]['equipmentQueryOrder'].replace(/\s*/g, "");
							tableDeviceEquipments.push(newE);
						}
					} else if(result["message"] == "noEquipment") {
						alert("还没有加入强电设备！");
					} else {
						console.log("tokenError");
						alert("身份验证失败！"); //为了测试
					}
				}
				setCookie("token", result["token"], 100);
			},
			error: function() {
				console.log("error");
			}
		})
	}
	/**
	 * 获取该设备的频率
	 */
	function getDeviceInteralFromServer() {
		var info = {};
		info['token'] = getCookie("token");
		info['mac'] = deviceName.innerHTML;
		jQuery.ajax({
			async: false,
			type: "POST",
			url: "http://192.168.100.32:8036/device/getInterval",
			data: info,
			dataType: "text",
			xhrFields: {
				withCredentials: true
			},
			success: function(data) {
				var result = JSON.parse(data);
				if(result["msgType"] == "DeviceGetInterval") {
					if(result["message"] == "success") {
						device.dInterval = result["content"];
					} else {
						console.log("tokenError");
						alert("身份验证失败！"); //为了测试
					}
				}
				setCookie("token", result["token"], 100);
			},
			error: function() {
				console.log("error");
			}
		})
	}
	iframeResize();
}