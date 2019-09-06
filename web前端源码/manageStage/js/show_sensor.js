window.onload = function() {
	var add_sensor = document.getElementById("sensor_add");
	add_sensor.onclick = function() {
		window.location.href = "add_sensor.html";
	}
	layui.use('table', function() {
		var table = layui.table;
		//向服务器发送请求，获取数据
		var info = {};
		info['token'] = getCookie("token");
		info['email'] = getCookie("current_email");
		var data = new Array();
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
				if(result.message == "success") {
					data = JSON.parse(result.content);
					table.render({
						elem: '#all_sensor',
						data: data,
						id: "123",
						cellMinWidth: 80 //全局定义常规单元格的最小宽度，layui 2.2.1 新增
							,
						page: true //开启分页
							,
						cols: [
							[{
									field: 'sensorName',
									width: '20%',
									minWidth: 200,
									title: '传感器名称'
								}, {
									field: 'sensorOrder',
									width: '21%',
									minWidth: 200,
									title: '传感器命令'
								}, {
									field: 'sensorMinisensor',
									width: '43%',
									minWidth: 100,
									title: '内嵌小单位传感器',
									templet: function(d) {
										var minis = JSON.parse(d.sensorMinisensor);
										var list = " ";
										for(var i = 0; i < minis.length; i++) {
											var mini = JSON.parse(minis[i]);
											list = list + mini.name + "(" + mini.unit + ")" + ";    ";
										}
										return list;
									}

								}, {
									fixed: 'right',
									width: '16%',
									minWidth: 100,
									align: 'center',
									toolbar: '#barDemo'
								} //toobar:绑定列工具条
							]
						],
						done: function(res, curr, count) {
							iframeResize();
						}
					});

				} else if(result.message == "tokenError") {
					alert("获取全部传感器信息失败：token错误");
				} else if(result.message == "noSensor") {
					alert("获取全部传感器信息：当前没有传感器哦，赶紧去添加吧!!");
				}
				setCookie("token", result.token, 100);
			}
		})
		console.log(data);

		//监听行工具事件
		table.on('tool(sensor)', function(obj) { //注：tool 是工具条事件名，test 是 table 原始容器的属性 lay-filter="对应的值"
			var data = obj.data; //获得当前行数据
			var layEvent = obj.event; //获得 lay-event 对应的值
			var tr = obj.tr;
			if(layEvent === 'del') {
				//点击删除按钮，删除指定传感器
				layer.confirm('确认要删除吗，删除了之后就找不回来了呦~', function(index) {

					//向服务端发送删除指令
					var del_sensor = {};
					del_sensor['token'] = getCookie("token");
					del_sensor['name'] = data.sensorName;
					del_sensor['email'] = getCookie("current_email");
					jQuery.ajax({
						async: false,
						type: "POST",
						url: "http://192.168.100.32:8036/device/delSensor",
						data: del_sensor,
						dataType: "text",
						xhrFields: {
							withCredentials: true
						},
						success: function(data) {
							var result = JSON.parse(data);
							if(result.content == "success") {
								obj.del(); //删除对应行（tr）的DOM结构
								layer.close(index);
								layer.msg('删除成功');
							} else if(result.content == "fail") {
								layer.msg('删除失败');
							} else if(result.content == "tokenError") {
								layer.msg('token错误');
							}

							setCookie("token", result.token, 100);
						},
						complete: function() {
							iframeResize();
						}
					})

				});
			} else if(layEvent === 'edit') {
				//修改指定传感器
				var path = "modify_sensor.html" + "?name=" + data.sensorName;
				window.location.href = path;

			}
		});
	});
	iframeResize();
}