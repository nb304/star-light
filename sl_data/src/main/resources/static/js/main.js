/* ==================判断当前字符串是否包含非法字符================== */
function illegalCharacter(str) {
	var regEn = /[`~!@#$%^&*_+<>?:"{},\/;'[\]]/im, regCn = /[·！#￥（——）：；“”‘、，|《。》？、【】[\]]/im;
	return regEn.test(str) || regCn.test(str); // true 包含非法字符 false 不包含
}

/* ==================判断当前字符串是否是数字================== */
function regNumber(number) {
	var reg = new RegExp(/^\d+$/);
	return reg.test(number); // true 表示是数字 false表示不是
}

/* ==================判断当前字符串是否是正整数================== */
function isPositive(number) {
	return (/(^[1-9]\d*$)/.test(number));// true 表示是正整数 false表示不是
}

/* ==================判断当前字符串是否是正数================== */
function isPositive(number) {
	if ("0" == number) {
		return false;
	}
	var flag = /(^[1-9]\d*$)/.test(number);// true 表示是正整数 false表示不是
	// true 表示正浮点数 false表示不是
	var falg = /^\d+(\.\d+)?$/.test(number);

	return flag || falg;// true表明为正数，false表明不是
}

/* ==================将时间戳转化成yyyy-MM-dd格式字符串================== */
function timeToString(time) {
	var date = new Date(time);
	year = date.getFullYear() + '-';
	month = (date.getMonth() + 1 < 10 ? '0' + (date.getMonth() + 1) : date
			.getMonth() + 1)
			+ '-';
	date = (date.getDate() < 10 ? '0' + date.getDate() : date.getDate()) + '';
	return year + month + date;
}

/* ==================将时间戳转化成yyyy年MM月dd日格式字符串================== */
function fomartTime(time) {
	var date = new Date(time);
	year = date.getFullYear() + '年';
	month = (date.getMonth() + 1 < 10 ? '0' + (date.getMonth() + 1) : date
			.getMonth() + 1)
			+ '月';
	date = (date.getDate() < 10 ? '0' + date.getDate() : date.getDate()) + '日';

	return year + month + date;
}

/* ==================将时间戳转化成yyyy年MM月dd日 HH:mm格式字符串================== */
function fomartDateTime(time) {
	var date = new Date(myTime(time));
	year = date.getFullYear() + '-';
	month = (date.getMonth() + 1 < 10 ? '0' + (date.getMonth() + 1) : date
			.getMonth() + 1)
			+ '-';
	dat = (date.getDate() < 10 ? '0' + date.getDate() : date.getDate()) + '';
	hour = ((date.getHours() + 8) < 10 ? "0" + (date.getHours() + 8) : (date
			.getHours() + 8))
			+ ':';
	minute = (date.getMinutes() < 10 ? "0" + date.getMinutes() : date
			.getMinutes());

	var second = date.getSeconds();// 秒
	return year + month + dat + " " + hour + minute + ":"
			+ (second < 10 ? "0" + second : second);
}

function myTime(date) {
	var arr = date.split("T");
	var d = arr[0];
	var darr = d.split('-');

	var t = arr[1];
	var tarr = t.split('.000');
	var marr = tarr[0].split(':');

	var dd = parseInt(darr[0]) + "/" + parseInt(darr[1]) + "/"
			+ parseInt(darr[2]) + " " + parseInt(marr[0]) + ":"
			+ parseInt(marr[1]) + ":" + parseInt(marr[2]);
	return dd;
}

/*******************************************************************************
 * 获取当前浏览器类型
 */
function getBrowser(n) {
	var ua = navigator.userAgent.toLowerCase(), s, name = '', ver = 0;
	// 探测浏览器
	(s = ua.match(/msie ([\d.]+)/)) ? _set("ie", _toFixedVersion(s[1]))
			: (s = ua.match(/firefox\/([\d.]+)/)) ? _set("firefox",
					_toFixedVersion(s[1]))
					: (s = ua.match(/chrome\/([\d.]+)/)) ? _set("chrome",
							_toFixedVersion(s[1])) : (s = ua
							.match(/opera.([\d.]+)/)) ? _set("opera",
							_toFixedVersion(s[1])) : (s = ua
							.match(/version\/([\d.]+).*safari/)) ? _set(
							"safari", _toFixedVersion(s[1])) : 0;

	function _toFixedVersion(ver, floatLength) {
		ver = ('' + ver).replace(/_/g, '.');
		floatLength = floatLength || 1;
		ver = String(ver).split('.');
		ver = ver[0] + '.' + (ver[1] || '0');
		ver = Number(ver).toFixed(floatLength);
		return ver;
	}

	function _set(bname, bver) {
		name = bname;
		ver = bver;
	}

	return (n == 'n' ? name : (n == 'v' ? ver : name + ver));
};

/* ==================将数字转化为中文(1000以内)================== */
function numberToChina(section) {
	var chnNumChar = [ "零", "一", "二", "三", "四", "五", "六", "七", "八", "九" ];
	var chnUnitChar = [ "", "十", "百", "千" ];
	var strIns = '', chnStr = '';
	var unitPos = 0;
	var zero = true;
	while (section > 0) {
		var v = section % 10;
		if (v === 0) {
			if (!zero) {
				zero = true;
				chnStr = chnNumChar[v] + chnStr;
			}
		} else {
			zero = false;
			strIns = chnNumChar[v];
			strIns += chnUnitChar[unitPos];
			chnStr = strIns + chnStr;
		}
		unitPos++;
		section = Math.floor(section / 10);
	}
	return chnStr;
}

/* =========去除字符串中的&nbsp;字符============= */
function myTrim(str) {
	str = str.replace(/&nbsp;/gi, '');
	return str;
}

/* ==================将数字转字符串，不足位数前面补0================== */
function fomartNumber(numerbr, length) {
	var str = String(numerbr);
	if (str.length < length) {
		var zero = "";
		for (var i = str.length; i < length; i++) {
			zero += "0";
		}
		str = zero + str;
	}
	return str;
}

/* =========构建一个map============= */
function Map() {
	/** 存放键的数组(遍历用到) */
	this.keys = new Array();
	/** 存放数据 */
	this.data = new Object();

	/**
	 * 放入一个键值对
	 * 
	 * @param {String}
	 *            key
	 * @param {Object}
	 *            value
	 */
	this.put = function(key, value) {
		if (this.data[key] == null) {
			this.keys.push(key);
		}
		this.data[key] = value;
	};

	/**
	 * 获取某键对应的值
	 * 
	 * @param {String}
	 *            key
	 * @return {Object} value
	 */
	this.get = function(key) {
		return this.data[key];
	};

	/**
	 * 删除一个键值对
	 * 
	 * @param {String}
	 *            key
	 */
	this.remove = function(key) {
		this.keys.remove(key);
		this.data[key] = null;
	};

	/**
	 * 遍历Map,执行处理函数
	 * 
	 * @param {Function}
	 *            回调函数 function(key,value,index){..}
	 */
	this.each = function(fn) {
		if (typeof fn != 'function') {
			return;
		}
		var len = this.keys.length;
		for (var i = 0; i < len; i++) {
			var k = this.keys[i];
			fn(k, this.data[k], i);
		}
	};

	/**
	 * 获取键值数组(类似Java的entrySet())
	 * 
	 * @return 键值对象{key,value}的数组
	 */
	this.entrys = function() {
		var len = this.keys.length;
		var entrys = new Array(len);
		for (var i = 0; i < len; i++) {
			entrys[i] = {
				key : this.keys[i],
				value : this.data[i]
			};
		}
		return entrys;
	};

	/**
	 * 判断Map是否为空
	 */
	this.isEmpty = function() {
		return this.keys.length == 0;
	};

	/**
	 * 获取键值对数量
	 */
	this.size = function() {
		return this.keys.length;
	};

	/**
	 * 重写toString
	 */
	this.toString = function() {
		var s = "{";
		for (var i = 0; i < this.keys.length; i++, s += ',') {
			var k = this.keys[i];
			s += k + "=" + this.data[k];
		}
		s += "}";
		return s;
	};
}

/**
 * 
 * 校验时间
 * 
 */
function checkTime() {

	// initialTime获取初始时间
	var initialTime = $("input[name=minTime]").val();
	// endTime获取结束时间
	var endTime = $("input[name=maxTime]").val();

	// 如果结束时间为空 说明查询起始时间后的所有数据
	if (endTime != "") {
		// 校验开始
		if (initialTime > endTime) {

			// 返回失败
			return false;
		}
	}
	// 返回成功
	return true;
}

// 退出系统
function logOut(url) {
	// 退出当前用户
	// 询问框
	layer.confirm('您确定要退出当前用户吗？', {
		btn : [ '确定', '取消' ]
	// 按钮
	}, function(index) {
		layer.close(index);
		// 发送POST请求 退出当前系统
		$.post(url + "/sys/userManage/exit", function(data) {
			if (data.status == 200) {
				toastr.success("退出成功");
				var loading = layer.load(1, {
					shade : [ 0.1, '#fff' ]
				// 0.1透明度的白色背景
				});
				setTimeout(function() {
					location.href = url;
				}, 1000);
			} else {
				toastr.error(data.msg);
			}
		});
	});
}

// 判断用户是否访问系统成功
// 返回true说明访问成功 false 访问不成功
function SystemVisitState(data) {
	if (data == 401) {
		layer.alert('抱歉,你没有权限访问', {
			icon : 5,
			skin : 'layer-ext-moon' // 该皮肤由layer.seaning.com友情扩展。关于皮肤的扩展规则，去这里查阅
		})

		return false;
	}

	if (data == 400) {

		layer.confirm('用户未登入或登入失效,请重新登录', {
			btn : [ '重新登录' ]
		// 按钮
		}, function() {
			top.document.location.reload();
		});
		return false;
	}

	if (data == 403) {
		layer.alert('权限树初始化异常', {
			icon : 5,
			skin : 'layer-ext-moon' // 该皮肤由layer.seaning.com友情扩展。关于皮肤的扩展规则，去这里查阅
		})

		return false;
	}

	return true;
}

function clear() {
	$(".toast-top-right").html("");
}
