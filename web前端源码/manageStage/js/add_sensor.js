window.onload = function(){
		var add_sensor_name = document.getElementsByClassName("sensor_name")[0].getElementsByTagName("input")[0];
		//获取传感器命令，并依此将其放进数组中
		
		
		var debug_btn = document.getElementById("debug_order");
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
	
		var add_btn = document.getElementsByClassName("add_item")[0];
		//点击添加按钮，在页面上生成一行编辑区域
		function add_mini_sensor(){
			var sensor_list = document.getElementsByClassName("sensor_minis")[0];
			var new_mini = document.createElement("div");
			new_mini.className="mini_sensor";
			var mini_namelabel = document.createElement("label");
			mini_namelabel.innerText = "传感器名称：";
			var mini_nameinput = document.createElement("input");
			mini_nameinput.type="text";
			var mini_unitlabel = document.createElement("label");
			mini_unitlabel.innerText = "传感器单位：";
			var mini_unitinput = document.createElement("input");
			mini_unitinput.type ="text";
			var mini_calculabel = document.createElement("label");
			mini_calculabel.innerText = "计算方式：";
			var mini_calcuinput = document.createElement("input");
			mini_calcuinput.type ="text";
			var delete_mini_item = document.createElement("button");
			delete_mini_item.className = "delete_item";
			delete_mini_item.type = "button";
			delete_mini_item.innerText = "-";
			new_mini.appendChild(mini_namelabel);
			new_mini.appendChild(mini_nameinput);
			new_mini.appendChild(mini_unitlabel);
			new_mini.appendChild(mini_unitinput);
			new_mini.appendChild(mini_calculabel);
			new_mini.appendChild(mini_calcuinput);
			new_mini.appendChild(delete_mini_item);
			sensor_list.appendChild(new_mini);		
			delete_mini_item.onclick = function(){
				sensor_list.removeChild(new_mini);
			}
			iframeResize();
		}
		//点击添加按钮，在页面上生成编辑框
		add_btn.onclick = function(){
			add_mini_sensor();		
		}
		//点击删除按钮，在页面上删除指定小传感器编辑框
		var delete_btn = document.getElementsByClassName("delete_item")[0];
		delete_btn.onclick = function(){
			var div_mini = delete_btn.parentNode;
			var div_minis = div_mini.parentNode;
			div_minis.removeChild(div_mini);
	
		}
		function judge_name_repeat(name,arr_list)
		{
			if(arr_list.length == 0)
			{
				return true;
			}
			for(var i=0; i<arr_list.length;i++){
				var data = JSON.parse(arr_list[i]);
				if(name == data.name)
				{
					return false;
				}
			}
			return true;
		}
		

		//点击提交按钮，获取用户编辑的内容，向服务端发送数据。
		var submit_btn = document.getElementById("add_submit");	
		submit_btn.onclick = function(){
			var minisensors = new Array();
			var mini_calcu = new Array();
			var result_order = new Array();
			var sensor_items = document.getElementsByClassName("mini_sensor");
			var flag = false;
			[].forEach.call(sensor_items,function(sensor_item){
				var minisensor_name  = sensor_item.getElementsByTagName("input")[0].value;
				var minisensor_unit  = sensor_item.getElementsByTagName("input")[1].value;
				var minisensor_calcu = sensor_item.getElementsByTagName("input")[2].value;
				if(minisensor_name.length > 0 && minisensor_unit.length > 0 && minisensor_calcu.length > 0){
					mini_calcu.push(minisensor_calcu);
					var minisensor = {
						name:minisensor_name,
						unit:minisensor_unit
					}
					//判断迷你传感器的名字，不能相同
					flag = judge_name_repeat(minisensor.name,minisensors);
					//判断传感器名字是否存在下划线，如果有，将其替换成空格
					if(minisensor.name.indexOf("_") != -1)//含有“_”
					{
						minisensor.name = minisensor.name.replace(/_/g,"");
					}
					
					
					if(flag)
					{
						var minisensor_json = JSON.stringify(minisensor);
						minisensors.push(minisensor_json);
					}
					else{
						return false;
					}
				}
			});
			
			result_order =  link_order(result_order);
			console.log(result_order);
			if(minisensors.length == 0)
			{
				alert("小传感器信息不能为空！！！");
			}
			else if(flag == false)
			{
				alert("小传感器名不能相同！！！");
			}
			else if(minisensors.length > 0 && result_order.length == 8 && add_sensor_name.value.length >0 && mini_calcu.length == minisensors.length)
			{
				var info = new FormData();
				info.append("token",getCookie("token"));
				info.append("email",getCookie("current_email"));
				info.append("name",add_sensor_name.value);
				info.append("order",result_order);
				info.append("calculation",mini_calcu);
				info.append("mini_sensor",JSON.stringify(minisensors));
				jQuery.ajax({
					//	async:false,
						type: "POST",
						url: "http://192.168.100.32:8036/device/addSensor",
						processData: false,//不处理参数,图片，不加这一行会出现illegal错误
						contentType: false,//不加这一行会报400错误
						data: info,
						dataType: "text",
						xhrFields: {
							withCredentials: true
						},
						success:function(data) {
							var result = JSON.parse(data);
							if(result.content == "success")	{
								alert("增加传感器成功");
								window.location.href = "show_sensor.html";
							}
							else if(result.content == "repeat"){
								alert("增加传感器失败：传感器名称重复");
							
							}
							else if(result.content == "tokenError"){
								alert("增加传感器失败：token错误");
	
							}
							setCookie("token",result.token,100);
						}
					})
				
				
			}
			else{
				alert("请将数据填完整！");
			}
			
			
		}

			
	iframeResize();
	
}
