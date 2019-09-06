window.onload = function() {
	//获取对象，依次是单元名称、长、宽;应用场景;提交按钮
	var inputs = document.getElementsByTagName("input");
	var textArea_sence = document.getElementsByTagName("textarea")[0];
	var btn_submit = document.getElementsByTagName("button")[0];
	var errors = document.getElementsByClassName("unit-item-error");
	var nameError = errors[0],
		lenWidthError = errors[1]
	senceError = errors[2];
	var unitName = inputs[0],
		unitLength = inputs[1],
		unitWidth = inputs[2];
	nameError.innerHTML = "";
	lenWidthError.innerHTML = "";

	unitName.onkeyup = myKeyUp;
	unitName.onchange = checkName;
	unitLength.onchange = checkNumber;
	unitWidth.onchange = checkNumber;
	textArea_sence.onchange = checkSence;

	btn_submit.onclick = function() {
		var info = {};
		info['name'] = unitName.value;
		if (unitLength.value){
			info['length'] = parseFloat(unitLength.value);
		} else {
			info['length'] = 0.0;
		}
		if (unitWidth.value){
			info['width'] = parseFloat(unitWidth.value);
		} else {
			info['width'] = 0.0;
		}
		info['description'] = textArea_sence.value;
		
		if(!checkName() || !checkNumber() || !checkSence()) {
			alert("填写错误！！！");
		} else {
			/**
			 * 发送数据给服务器
			 */
			info['email'] = getCookie("email");
			info['token'] = getCookie('token');
			jQuery.ajax({
				async: false,
				type: "POST",
				url: "http://192.168.100.32:8036/user/addUnit",
				data: info,
				dataType: "text",
				xhrFields: {
					withCredentials: true
				},
				success: function(data) {
					
					var result = JSON.parse(data);
					if(result["msgType"] == "UserAddUnit") {
						if(result["content"] == "success") {
							alert("添加成功！");
							window.location.href = "show_unit.html";
							window.event.returnValue = false;  
						} else if(result["content"] == "repeat") {
							alert("单元名重复！已添加！");
						} else {
							alert("身份认证失败！");
						}
					}
					console.log(result["token"]);
					setCookie("token", result["token"], 100);
				},
				error: function() {
					console.log("error");
					alert("添加失败！");
				}
			})
		}

	}

	/**
	 * 当单元名称有值，就可以点击按钮
	 */
	function myKeyUp() {
		if(unitName.value) {
			btn_submit.disabled = false;
		} else {
			btn_submit.disabled = true;
		}
	}
	/**
	 * 检查字符是否小于等于20字以内
	 */
	function checkName() {
		var str = unitName.value;
		if(str.length > 20 || str.length == 0) {
			nameError.innerHTML = "单元名称在1-20字内";
			return false;
		} else {
			nameError.innerHTML = "";
		}
		return true;
	}
	/**
	 * 检查字符是否小于等于20字以内
	 */
	function checkNumber() {
		var num1 = unitLength.value;
		var num2 = unitWidth.value;
		if(num1 < 0 || num2 < 0) {
			lenWidthError.innerHTML = "数值大于等于0";
			return false;
		} else {
			lenWidthError.innerHTML = "";
		}
		return true;
	}
	/**
	 * 检查字符是否小于等于100字以内
	 */
	function checkSence() {
		var str = textArea_sence.value;
		if(str.length > 100) {
			senceError.innerHTML = "应用场景在1-100字内";
			return false;
		} else {
			senceError.innerHTML = "";
		}
		return true;
	}
	iframeResize();
}