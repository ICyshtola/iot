window.onload = function() {
	//从url得到的参数，显示当前的数据的折线图
	//获取时间对象
	var startTime = document.getElementById("history_start_time");
	var endTime = document.getElementById("history_end_time");
	//获取两个按钮
	var btnRealTime = document.getElementsByTagName("button")[0];
	var btnSearch = document.getElementById("search_button");
	var divShow = document.getElementById("history_chart");
	var index; //弹窗
	//初始化折线图数据
	var myX = [];
	var maxY = null,
		minY = null; //max, min
	var myLegend = [];
	var allData = [];
	var mySeries = [];
	//初始化默认时间
	var myDate = new Date();
	var myYear = myDate.getFullYear(); // 获取完整的年份(4位,1970-????)
	var myMonth = myDate.getMonth() + 1; // 获取当前月份(0-11,0代表1月)
	var myDay = myDate.getDate(); // 获取当前日(1-31)
	var myHour = myDate.getHours(); // 获取当前小时数(0-23)
	var myMinute = myDate.getMinutes(); // 获取当前分钟数(0-59)
	var mySecond = myDate.getSeconds(); // 获取当前秒数(0-59)
	var stringStartTime, stringEndTime;

	var checked_unit_name;
	var checked_device_name;
	var unit_data = new Array();
	var device_data = new Array();
	var all_device_data = new Array();

	var unitName = GetQueryString("unitName");
	var deviceName = GetQueryString("deviceName");

	unit_data = get_unit_data(unit_data);
	all_device_data = get_device(all_device_data);

	myMonth = (myMonth < 10) ? ('0' + myMonth) : myMonth;
	myDay = (myDay < 10) ? ('0' + myDay) : myDay;
	myHour = (myHour < 10) ? ('0' + myHour) : myHour;
	myMinute = (myMinute < 10) ? ('0' + myMinute) : myMinute;
	mySecond = (mySecond < 10) ? ('0' + mySecond) : mySecond;

	stringEndTime = myYear + '-' + myMonth + '-' + myDay + 'T' + myHour + ":" + myMinute + ":" + mySecond;
	myHour -= 1;
	myHour = (myHour < 10) ? ('0' + myHour) : myHour;

	stringStartTime = myYear + '-' + myMonth + '-' + myDay + 'T' + myHour + ":" + myMinute + ":" + mySecond;
	
	startTime.value = stringStartTime;
	endTime.value = stringEndTime;
	if(unit_data.length > 0) {
		add_unit_item(unit_data);
	}
	if(unitName == null && deviceName == null) {
		if(all_device_data.length > 0) {
			add_device_item(all_device_data);
			set_unit_checked();
			after_click_device_or_default();
		}
	} else {
		if(all_device_data.length > 0) {
			add_device_item(all_device_data);
			checked_device_name = deviceName;
			//寻找到deviceName的设备，并将其的按钮设为checked
			find_device();
			set_unit_checked();
			after_click_device_or_default();
		}
	}

	btnRealTime.onclick = function() {
		window.location.href = "show_current_data.html";
	}

	btnSearch.onclick = function() {
		getHistoryDataFromServer(); //请求历史数据
	}
	
	startTime.onkeydown = function(e) {
		var evt = window.event || e;
		if(evt.keyCode == 13) {
			getHistoryDataFromServer(); //请求历史数据
		}
	};
	endTime.onkeydown = function(e) {
		var evt = window.event || e;
		if(evt.keyCode == 13) {
			getHistoryDataFromServer(); //请求历史数据
		}
	};
	/**
	 * 请求数据库数据
	 */
	function getHistoryDataFromServer() {
		var info = {};
		info['token'] = getCookie("token");
		info['email'] = getCookie("current_email");
		info['mac'] = checked_device_name;
		/**
		 * 比较时间, 0 < 时间相差  < 24小时 
		 */
		var compareSandT = checkTime(startTime.value, endTime.value);
		if(compareSandT < -86400000 || compareSandT > 86400000) {
			alert("时间段在24小时以内！");
		} else {
			if(compareSandT < 0) {
				info['start_time'] = endTime.value.replace('T', ' ');
				info['end_time'] = startTime.value.replace('T', ' ');
			} else {
				info['start_time'] = startTime.value.replace('T', ' ');
				info['end_time'] = endTime.value.replace('T', ' ');
			}
			index = layer.msg('正在加载中，请稍候', {
				icon: 16,
				time: false,
				shade: 0.8
			});
			jQuery.ajax({
				async: false,
				type: "POST",
				url: "http://192.168.100.32:8036/user/getHistoryData",
				data: info,
				dataType: "text",
				xhrFields: {
					withCredentials: true
				},
				success: function(data) {
				
					var result = JSON.parse(data);
					if(result["msgType"] == "UserGetHistoryData") {
						if(result["message"] == "success") {
							getDataToLineChart(result);
						} else if(result["message"] == "noData") {
							var p = document.createElement('p');
							p.innerHTML = "设备(" + info['mac'] + ")在" + info['start_time'] + "-" + info['end_time'] + "之间没有数据";
							divShow.innerHTML = '';
							divShow.appendChild(p);
							layer.close(index);

						} else {
							console.log("tokenError");
							layer.close(index);
							alert("身份验证失败！"); //为了测试
						}
					}
					setCookie("token", result["token"], 100);
				},
				error: function() {
					layer.close(index);
					console.log("error");
					alert("连接失败！"); //为了测试
				}
			})
		}
	}
	/**
	 * 整理数据，画图
	 */
	function getDataToLineChart(result) {
		var content = JSON.parse(result["content"]);
		//清空数组
		myX = [];
		maxY = null;
		minY = null; //max, min
		myLegend = [];
		allData = [];
		mySeries = [];
		
		//通过得到myLegend,myX
		for(var i = 0; i < content.length; i++) {
			var time = content[i]['record_time'];
			var d = JSON.parse(content[i]['data']);
			//得到所有时刻
			myX.push(time);
			for(var j = 0; j < d.length; j++) {
				//得到所有图例
				var str = d[j].sensorName + "(" + d[j].sensorUnit + ")";
				if(myLegend.indexOf(str) == -1) {
					myLegend.push(str);
				}
			}
		}
		for(var i = 0; i < content.length; i++) {
			var d = JSON.parse(content[i]['data']);
			for(var j = 0; j < d.length; j++) {
				var str = d[j].sensorName + "(" + d[j].sensorUnit + ")";
				var indexC = myLegend.indexOf(str);
				if(allData[indexC] == undefined) {
					//没有出现过，前面补空值
					allData[indexC] = [];
					/**
					 * 根据时间长度，增加0
					 */
					for(var k = 0; k < i; k++) {
						allData[indexC].push(undefined);
					}
					if(d[j].data > 1e100) {
						layer.msg("数据异常！！", {
							time: 2000
						});
						allData[indexC].push(undefined);
					} else {
						allData[indexC].push(d[j].data);
					}
				} else {
					if(d[j].data > 1e100) {
						layer.msg("数据异常！！", {
							time: 2000
						});
						allData[indexC].push(undefined);
					} else {
						allData[indexC].push(d[j].data);
					}
				}
			}
		}
		for(var i = myLegend.length - 1; i >= 0; i--) {
			var obj = {
				lineStyle: {
					normal: {
						width: 5,
						type: 'solid',
					}
				},
				symbolSize: 10, //设定实心点的大小
				markPoint: {     //显示一定区域内的最大值和最小值                             
					data: [{
						type: 'max',
						name: '最大值'
					}, {
						type: 'min',
						name: '最小值'
					}]                           
				},
				symbol: 'circle',
			};
			obj.name = myLegend[i];
			obj.type = 'line';
			obj.data = allData[i];
			mySeries.push(obj);
		}
		//画图
		StartLineChartDraw();
		layer.close(index);
	}
	/**
	 * 根据数据画折线图
	 */
	function StartLineChartDraw() {
		// 基于准备好的dom，初始化echarts图表
		divShow.innerHTML = "";
		var myChart = echarts.init(divShow);
		var title = checked_device_name + "的传感器数据折线图";
		var gTop = parseInt(myLegend.length / 8 + 0.5) * 40 + 100;
		if(parseInt(myLegend.length / 8 + 0.5) >= 1) {
			divShow.parentNode.height += parseInt(myLegend.length / 8 + 0.5) * 40;
			iframeResize();
		}
		var optionLine = {
			textStyle: {
				color: "#fff",
				fontSize: 20,
			},
			title: {
				text: title,
				textStyle: {
					color: "#fff",
					fontSize: 30,
				},
				x: 'center',
				y: 'top',
				backgroundColor: 'rgba(0,0,0,0)',
				borderColor: '#ccc',
				borderWidth: 0,
				padding: 20,
			},
			tooltip: {
				trigger: 'axis',
				axisPointer: {
					animation: false
				},
				textStyle: {
					color: "#fff",
					fontSize: 20,
				},
				backgroundColor: '#000',
			},
			legend: {
				data: myLegend,
				textStyle: {
					color: "#fff",
					fontSize: 17,
					fontWeight: 'bolder',
				},
				orient: 'horizontal',
				x: 'right',
				y: 'top',
				backgroundColor: 'rgba(255,255,255,0)',
				borderColor: '#fff',
				borderWidth: 0,
				padding: 90,
				itemGap: 10,
				itemWidth: 20,
				itemHeight: 14,
			},
			dataZoom: [{
				id: 'dataZoomX',
				type: 'slider',
				xAxisIndex: [0],
				filterMode: 'filter',
				backgroundColor: 'rgba(255,255,255,1)', // 背景颜色
				dataBackgroundColor: '#eee', // 数据背景颜色
				fillerColor: 'rgba(144,197,237,0.5)', // 填充颜色
				handleColor: 'rgba(70,130,180,0.8)', // 手柄颜色
				padding: 15,
				height: 40,
				textStyle: {
					color: "rgba(70,130,180,1)",
					fontSize: 10,
					fontWeight: 'bolder',
				},
			}],
			grid: {
				x: 80,
				y: 60,
				x2: 80,
				y2: 60,
				top: gTop,
				bottom: '90px',
				backgroundColor: 'rgba(255,255,255,1)',
				borderWidth: 1,
				borderColor: '#ccc'
			},
			xAxis: {
				type: 'category',
				data: myX, //x_time
				axisLabel: {
					textStyle: {
						color: '#fff',
						fontSize: 20,
					}
				},
				axisLine: {
					lineStyle: {
						color: '#fff',
					} // x轴坐标轴颜色
				},

				axisTick: {
					lineStyle: {
						color: '#fff',
					} // x轴刻度的颜色
				},
				//设置网格线颜色
				splitLine: {
					show: true,
					lineStyle: {
						color: ['#fff'],
						width: 1,
						type: 'solid'
					}　　
				}
			},
			yAxis: {
				type: 'value',
				scale: true,
				boundaryGap: [0, '100%'],
				axisLabel: {
					textStyle: {
						color: '#fff',
						fontSize: 20,
					}
				},
				axisLine: {
					lineStyle: {
						color: '#fff',
					} // x轴坐标轴颜色
				},
				axisTick: {
					lineStyle: {
						color: '#fff',
					} // x轴刻度的颜色
				},
				//设置网格线颜色
				splitLine: {
					show: true,
					lineStyle: {
						color: ['#fff'],
						width: 1,
						type: 'solid'
					}　　
				}
			},
			series: mySeries
		};

		myChart.setOption(optionLine);
	}
	/**
	 * 保证时间在合理范围之内,24小时
	 */
	function checkTime(a, b) {
		a = a.replace('T', ' ');
		b = b.replace('T', ' ');
		var d1 = new Date(a.replace(/-/g, "\/"));
		var d2 = new Date(b.replace(/-/g, "\/"));
		return(d2 - d1);
	}

	function find_device() {
		var getbox_device_items = document.getElementsByClassName("device_items")[0];
		var device_items = getbox_device_items.getElementsByClassName("right_item");
		[].forEach.call(device_items, function(item) {
			var flag = false;
			var device_label = item.getElementsByTagName("label")[0];
			var device_radio = item.getElementsByTagName("input")[0];
			if(device_label.innerText == checked_device_name) {
				device_radio.checked = "checked";
			}

		})

	}

	//根据checked_device_name获取其采集单元，将其对应的采集单元的按钮设为checked
	function set_unit_checked() {
		//寻找checked_device_name的采集单元
		var found_unit_name = null; //采集单元名
		for(var i = 0; i < all_device_data.length; i++) {
			if(all_device_data[i].deviceMac == checked_device_name) {
				found_unit_name = all_device_data[i].deviceUnit;
			}
		}
		if(found_unit_name == null) {
			return "";
		}
		var getbox_unit_items = document.getElementsByClassName("unit_items")[0];
		var unit_items = getbox_unit_items.getElementsByClassName("right_item");
		[].forEach.call(unit_items, function(item) {
			var unit_label = item.getElementsByTagName("label")[0];
			var unit_radio = item.getElementsByTagName("input")[0];
			if(unit_label.innerText == found_unit_name) //找到了采集单元，将其radio设为选中
			{
				unit_radio.checked = "checked";
				checked_unit_name = unit_label.innerText;

			}
		})

	}

	//添加采集单元项，并为其添加点击事件
	function add_unit_item(arr_unit_data) {
		var getbox_unit_items = document.getElementsByClassName("unit_items")[0];
		if(arr_unit_data.length > 0) {
			for(var i = 0; i < arr_unit_data.length; i++) {
				var new_unit_data = arr_unit_data[i];
				var item = document.createElement("div");
				item.className = "right_item";
				var item_label = document.createElement("label");
				item_label.innerText = new_unit_data.unitName;
				var item_input = document.createElement("input");
				item_input.type = "radio";
				item_input.name = "unit_item_show";
				item.appendChild(item_input);
				item.appendChild(item_label);
				getbox_unit_items.appendChild(item);
				click_unit(item_input, item_label);
			}
		}

	}

	function click_unit(item_input, item_label) {
		item_input.onclick = function() {
			checked_unit_name = item_label.innerText;
			after_click_unit_or_default();
		}
	}

	function click_device(item_input, item_label) {
		item_input.onclick = function() {
			checked_device_name = item_label.innerText;
			after_click_device_or_default();
		}
	}

	//每次点击单元，都会调用的函数:根据checked_unit_name的值去寻找其对应的设备
	function after_click_unit_or_default() {
		//每一次点击都会获取设备信息
		device_data = [];
		device_data = get_device_by_unit_data(device_data, checked_unit_name);
		/*	//删除上一次获取的设备（原来的逻辑）
			var getbox_device_items= document.getElementsByClassName("device_items")[0];
			getbox_device_items.innerHTML="";
			//增加新的设备
			add_device_item(device_data);*/

		//遍历全部设备信息，如果不是该采集单元下的设备，就将其按钮设为不可点击,并且将当前选中的设备设为不选中
		var getbox_device_items = document.getElementsByClassName("device_items")[0];
		var device_items = getbox_device_items.getElementsByClassName("right_item");
		[].forEach.call(device_items, function(item) {
			var flag = false;
			var device_label = item.getElementsByTagName("label")[0];
			var device_radio = item.getElementsByTagName("input")[0];
			if(device_radio.checked == true) {
				device_radio.checked = false;
			}
			for(var i = 0; i < device_data.length; i++) {
				if(device_label.innerText == device_data[i].deviceMac) {
					flag = true;
					break;
				}
			}
			if(flag == false) {
				device_radio.disabled = "disabled";
			}
			if(flag == true) {
				device_radio.disabled = false;
			}
		})

	}

	//每次点击单元，都会调用的函数:根据checked_unit_name的值去寻找其对应的设备
	function after_click_device_or_default() {
		getHistoryDataFromServer();
	}
	//添加设备项，并为其添加点击事件
	function add_device_item(arr_device_data) {
		var getbox_device_items = document.getElementsByClassName("device_items")[0];
		if(arr_device_data.length > 0) {
			for(var i = 0; i < arr_device_data.length; i++) {
				var new_device_data = arr_device_data[i];
				var item = document.createElement("div");
				item.className = "right_item";
				var item_label = document.createElement("label");
				item_label.innerText = new_device_data.deviceMac;
				var item_input = document.createElement("input");
				item_input.type = "radio";
				item_input.name = "device_item_show";
				if(i == 0) {
					item_input.checked = "checked";
					checked_device_name = item_label.innerText;
				}
				item.appendChild(item_input);
				item.appendChild(item_label);
				getbox_device_items.appendChild(item);
				click_device(item_input, item_label);

			}

		}

	}

	//获取所拥有的所有采集单元
	function get_unit_data(unit_data) {
		var info = {};
		info['token'] = getCookie("token");
		info['email'] = getCookie("current_email");
		jQuery.ajax({
			async: false,
			type: "POST",
			url: "http://192.168.100.32:8036/user/getUnitInfo",
			data: info,
			dataType: "text",
			xhrFields: {
				withCredentials: true
			},
			success: function(data) {
				var result = JSON.parse(data);
				if(result.msgType == "UserGetUnitInfo") {
					if(result.message == "success") {
						unit_data = JSON.parse(result.content);
					} else if(result.message == "tokenError") {
						alert("获取数据失败:token错误");
					} else if(result.message == "noUnit") {
						alert("你还没有采集单元，没有数据显示哦，赶紧去添加吧！！");
					}
				}
				setCookie("token", result.token, 100);
			}
		})
		return unit_data;

	}
	//获取全部设备
	function get_device(all_device_data) {
		var info = {};
		info['token'] = getCookie("token");
		info['email'] = getCookie("current_email");
		jQuery.ajax({
			async: false,
			type: "POST",
			url: "http://192.168.100.32:8036//user/getDeviceInfo",
			data: info,
			dataType: "text",
			xhrFields: {
				withCredentials: true
			},
			success: function(data) {
				var result = JSON.parse(data);
				if(result.msgType == "UserGetDeviceInfo") {
					if(result.message == "success") {
						all_device_data = JSON.parse(result.content);
					} else if(result.message == "tokenError") {
						alert("获取设备数据失败:token错误");
					} else if(result.message == "noDevice") {
						alert("您还还没有设备，没有数据显示哦，赶紧去添加吧！！");
					}
				}
				setCookie("token", result.token, 100);
			}
		})
		return all_device_data;
	}

	//根据采集单元获取某单元下所有的设备
	function get_device_by_unit_data(device_data, unit_name) {
		var info = {};
		info['token'] = getCookie("token");
		info['email'] = getCookie("current_email");
		info['unit'] = unit_name;
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
				if(result.msgType == "UserGetDevsByUnit") {
					if(result.message == "success") {
						device_data = JSON.parse(result.content);
					} else if(result.message == "tokenError") {
						alert("获取设备数据失败:token错误");
					} else if(result.message == "noDevice") {
						alert("该采集单元还没有设备，没有数据显示哦，赶紧去添加吧！！");
					}
				}
				setCookie("token", result.token, 100);
			}
		})
		console.log(device_data);
		return device_data;
	}

	function getDeviceInteralFromServer(dev_Interval) {
		var info = {};
		info['token'] = getCookie("token");
		info['mac'] = checked_device_name;
		info['email'] = getCookie("current_email");
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
					if(result.message == "success") {
						dev_Interval = result["content"];
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
		return dev_Interval;
	}

	iframeResize();
}