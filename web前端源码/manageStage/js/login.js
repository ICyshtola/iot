window.onload = function() {
	//获取报错、邮箱、密码、登录按钮
	var error = document.getElementById("login-form-error");
	var content = document.getElementsByTagName("input");
	var login = document.getElementsByTagName("button")[1];
	var email = content[0],
		passwd = content[1],
		check = content[2];
	var reg = /^([a-zA-Z]|[0-9])(\w|\-)+@[a-zA-Z0-9]+\.([a-zA-Z]{2,4})$/;
	var fromCookie = false;

	/**
	 * 查看上次是否记住我，如果记住则获取数据显示
	 */
	if(getCookie("remember") == "true") {
		email.value = getCookie("email");
		passwd.value = getCookie("password");
		check.checked = true;
		if(email.value && passwd.value) {
			fromCookie = true;
		}
		myKeyUp();
	}

	document.onkeyup = myKeyUp;
	/**
	 * 检测到content中的邮箱、密码都填上，button设置可以点击
	 */
	function myKeyUp() {
		if(email.value && passwd.value) {
			login.disabled = false;
		} else {
			login.disabled = true;
		}
	}

	login.onclick = function() {
		var e = email.value;
		var p = passwd.value;
		/**
		 * 是否从cookie中读出
		 */
		if(fromCookie) {
			if(!check.checked) {
				setCookie("remember", "false", 100);
			}
			/* 判断是否是上次记住的账号
			 * yes: 直接发出去
			 * no: 密码加密发送
			 */
			if(e == getCookie("email")) {
				p = getCookie("password");
				sendToServer(e, p);
				return ;
			} else {
				fromCookie = false;
			}
		}
		if (!fromCookie){
			/**
			 * 检查邮箱地址是否正确
			 */
			if(!reg.test(e)) {
				error.innerText = "邮箱格式不正确";
			} else {
				/**
				 * 检查密码长度8~16
				 */
				if(p.length > 16 || p.length < 8) {
					error.innerText = "密码长度为8-16";
				} else {
					error.innerHTML = "";
					p = MD5.createMD5String(p);
					isRememberMe(e, p);
					/**
					 * 发送数据给服务器
					 */
					sendToServer(e, p);
				}
			}
		}
	}
	/**
	 * 发送数据给服务器
	 */
	function sendToServer(e, p) {
		var info = {};
		info['email'] = e;
		info['password'] = p;
		console.log(info);
		jQuery.ajax({
			type: "POST",
			url: "http://192.168.100.32:8036/user/login",
			data: info,
			dataType: "text",
			xhrFields: {
				withCredentials: true
			},
			success: function(data) {
				var result = JSON.parse(data);
				if(result["msgType"] == "UserLogin") {
					if(result["content"] == "success") {
						window.location.href = "nav.html";
						setCookie("current_email", e, 100);
					} else {
						error.innerHTML = "登录失败";
					}
				}
				setCookie("token", result["token"], 100);
			},
			error: function() {
				console.log("error");
				error.innerHTML = "登录失败";
			}
		})

	}
	/**
	 * 浏览器是否需要记录我的值
	 */
	function isRememberMe(email, passwd) {
		if(check.checked == true) {
			console.log(check.checked);
			console.log("加密以后是：" + email + "\n加密以后是:" + passwd);
			setCookie("remember", "true", 100);
			setCookie("email", email, 100);
			setCookie("password", passwd, 100);
		} else {
			setCookie("remember", "false", 100);
		}
	}
}