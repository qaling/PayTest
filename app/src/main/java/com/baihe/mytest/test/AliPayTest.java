package com.baihe.mytest.test;

import com.yungouos.pay.alipay.AliPay;
import com.yungouos.pay.entity.AliPayH5Biz;
import com.yungouos.pay.entity.AliPayJsPayBiz;
import com.yungouos.pay.entity.HbFqBiz;
import com.yungouos.pay.entity.RefundOrder;
import com.yungouos.pay.entity.RefundSearch;

/**
 * 
 * 支付宝SDK调用演示
 * 
 * @author YunGouOS技术部-029
 *
 */
public class AliPayTest {

	public static void main(String[] args) {
		String result;
		// 支付宝商户号，登录www.yungouos.com-》支付宝-》商户管理 获取
		String mch_id = "签约后的商户号";
		// 商户密钥
		String key = "签约后的密钥";
		// 回调地址
		String notify = "http://www.baidu.com";

		// 同步回调地址
		String returnUrl = "http://www.baidu.com";

		try {

			// 花呗分期业务参数示例
			HbFqBiz hbFqBiz = new HbFqBiz();
			hbFqBiz.setNum(3); // 分3期
			hbFqBiz.setPercent(0);// 花呗手续费商户承担比例为0，也就是全部由消费者承担花呗分期手续费

			// 花呗分期需要支付金额超过100元，此处方便演示就将该对象置位null，正常使用时候根据自身业务情况设置该对象即可
			hbFqBiz = null;

			// 支付宝扫码支付
			result = AliPay.nativePay(System.currentTimeMillis() + "", "0.01", mch_id, "测试订单", "2", null, notify, null, null, null, hbFqBiz, key);
			System.out.println("支付宝扫码支付返回结果：" + result);

			// 支付宝wap支付
			result = AliPay.wapPay(System.currentTimeMillis() + "", "0.01", mch_id, "支付测试", null, notify, null, null, null, hbFqBiz, key);
			System.out.println("支付宝wap支付返回结果：" + result);

			String buyer_id = "支付宝买家唯一编号，通过支付宝授权接口获取";

			// 支付宝JS支付
			AliPayJsPayBiz aliPayJsPayBiz = AliPay.jsPay(System.currentTimeMillis() + "", "0.01", mch_id, buyer_id, "支付测试", null, notify, null, null, null, hbFqBiz, key);
			System.out.println("支付宝JS支付返回结果：" + aliPayJsPayBiz.toString());

			// 支付宝H5支付
			AliPayH5Biz aliPayH5Biz = AliPay.h5Pay(System.currentTimeMillis() + "", "0.01", mch_id, "接口测试", null, notify, returnUrl, null, null, null, hbFqBiz, key);
			//form表单需要自行输出跳转
			System.out.println("支付宝H5支付返回form表单：" + aliPayH5Biz.getForm());
			//url直接重定向访问即可
			System.out.println("支付宝H5支付返回url：" + aliPayH5Biz.getUrl());

			// 支付宝appPay支付
			String appPay = AliPay.appPay(System.currentTimeMillis() + "", "0.01", mch_id, "接口测试", null, notify, null, null, null, hbFqBiz, key);
			System.out.println("支付宝APP支付返回结果：" + appPay);

			// 发起退款
			RefundOrder orderRefund = AliPay.orderRefund("Y194506551713811", mch_id, "0.01", "测试退款", key);
			System.out.println("支付宝发起退款返回结果：" + orderRefund.toString());

			// 退款查询
			RefundSearch refundSearch = AliPay.getRefundResult("R09441868126739", mch_id, key);
			System.out.println("支付宝退款结果查询返回结果：" + refundSearch.toString());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
