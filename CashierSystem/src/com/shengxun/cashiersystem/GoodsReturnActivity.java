package com.shengxun.cashiersystem;

import java.util.ArrayList;

import net.tsz.afinal.http.AjaxCallBack;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.shengxun.adapter.CashierReturnGoodsAdapter;
import com.shengxun.constant.C;
import com.shengxun.entity.OrderInfo;
import com.shengxun.entity.ProductInfo;
import com.shengxun.util.ConnectManager;
import com.zvezda.android.utils.AppManager;
import com.zvezda.android.utils.BaseUtils;
import com.zvezda.android.utils.JSONParser;
import com.zvezda.android.utils.LG;

/**
 * 退货功能界面
 * 
 * @author sw
 * @date 2015-5-4
 */
public class GoodsReturnActivity extends BaseActivity {

	// 订单号输入框,卡号
	EditText et_order_no, et_card_no;
	// 确认退款，返回和查询订单按钮
	Button return_ok, back, search_order, swing_card;
	// 支付方式，1、现金，2、信用卡，3、储蓄卡，4、储值卡，目前只支持现金支付
	String pay_way = "1";
	// 收银员卡号,订单号，总额
	String card_no, cashier_card_no, order_no, refund_order_no;
	// 退款金额文本框
	TextView return_money;
	ListView lv;
	// 商品列表adapter
	CashierReturnGoodsAdapter crga;
	// 保存需要退货的商品列表
	ArrayList<ProductInfo> product_list;
	ArrayList<ProductInfo> refund_product_list;
	OrderInfo od;
	double order_money = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.cashier_goods_return_view);

		initWidget();
	}

	/**
	 * 初始化控件
	 * 
	 * @auth shouwei
	 */
	private void initWidget() {
		product_list = new ArrayList<ProductInfo>();
		et_order_no = (EditText) findViewById(R.id.cashier_goods_return_order_no);
		et_card_no = (EditText) findViewById(R.id.cashier_goods_return_card_no);
		return_ok = (Button) findViewById(R.id.cashier_goods_return_ok);
		back = (Button) findViewById(R.id.cashier_goods_return_back);
		search_order = (Button) findViewById(R.id.cashier_goods_return_search_order);
		swing_card = (Button) findViewById(R.id.cashier_goods_return_swing_card);
		return_money = (TextView) findViewById(R.id.cashier_goods_return_money);
		lv = (ListView) findViewById(R.id.cashier_goods_return_lv);

		return_ok.setOnClickListener(myclick);
		back.setOnClickListener(myclick);
		search_order.setOnClickListener(myclick);
		swing_card.setOnClickListener(myclick);
	}

	OnClickListener myclick = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			// 确认退款
			case R.id.cashier_goods_return_ok:
				if (BaseUtils.IsNotEmpty(applicationCS.cashier_card_no)) {
					cashier_card_no = applicationCS.cashier_card_no;
					// 创建退货订单
					ConnectManager.getInstance().getOrderFormRefundResult(
							 order_no, product_list, cashier_card_no,
							pay_way,card_no, createReturnOrder);
				} else {
					C.showShort("收银员卡号失效", mActivity);
				}
				break;
			// 返回
			case R.id.cashier_goods_return_back:
				AppManager.getAppManager().finishActivity(mActivity);
				break;
			// 查询订单
			case R.id.cashier_goods_return_search_order:
				order_no = et_order_no.getText().toString().trim();
				card_no = et_card_no.getText().toString().trim();
				// 验证消费者卡号非空
				if (BaseUtils.IsNotEmpty(card_no)) {
					// 验证订单号非空
					if (BaseUtils.IsNotEmpty(order_no)) {
						// 查询该订单是否存在
						ConnectManager.getInstance().getOrderFormDetailResult(
								order_no, searchorder);
					} else {
						C.showShort("请输入订单号", mActivity);
					}
				} else {
					C.showShort("请刷卡", mActivity);
				}
				break;
			// 验证卡号
			case R.id.cashier_goods_return_swing_card:
				break;
			default:
				break;
			}
		}
	};

	/**
	 * 刷新商品列表
	 * 
	 * @param list
	 * @auth shouwei
	 */
	private void refreshGoodsData(ArrayList<ProductInfo> list) {

		for (int i = 0; i < list.size(); i++) {
			list.get(i).isChecked = false;
		}
		crga = new CashierReturnGoodsAdapter(mActivity, list, cbcl);
		crga.setOrderInfo(od);
		lv.setAdapter(crga);

	}

	/**
	 * 检查订单状态
	 * 
	 * @param status
	 * @auth shouwei
	 */
	private void checkOrderStatus(int status) {
		// 未付款状态
		if (status == 1) {
			AlertDialog d = new AlertDialog.Builder(mActivity).create();
			d.setTitle("订单尚未付款，无需退款");
			d.show();
			// 已付款状态
		} else if (status == 2) {
			// 已退货状态
		} else if (status == 3) {
			AlertDialog d = new AlertDialog.Builder(mActivity).create();
			d.setTitle("订单已退货，无需重复退货");
			d.show();
		}
		// 刷新商品列表
		refreshGoodsData(product_list);
	}

	/**
	 * 退货订单退款回调
	 */
	AjaxCallBack<String> ajaxcallback = new AjaxCallBack<String>() {
		public void onSuccess(String t) {
			super.onSuccess(t);
			if (JSONParser.getStringFromJsonString("status", t).equals("1")) {
				String data = JSONParser.getStringFromJsonString("data", t);
				if (JSONParser.getStringFromJsonString("return", data).equals(
						"ok")) {
					C.showShort("退款成功", mActivity);
				} else {
					C.showShort("退款失败", mActivity);
				}
			} else {
				C.showShort(JSONParser.getStringFromJsonString("error_dec", t),
						mActivity);
			}
		};

		public void onFailure(Throwable t, int errorNo, String strMsg) {
			super.onFailure(t, errorNo, strMsg);
			C.showShort("退款失败", mActivity);
		};
	};
	/**
	 * 查询订单回调
	 */
	AjaxCallBack<String> searchorder = new AjaxCallBack<String>() {
		@SuppressWarnings("unchecked")
		public void onSuccess(String t) {
			super.onSuccess(t);
			LG.i(getClass(), "t============>" + t);
			if (JSONParser.getStringFromJsonString("status", t).equals("1")) {
				String data = JSONParser.getStringFromJsonString("data", t);
				String order_detail = JSONParser.getStringFromJsonString(
						"order_detail", data);
				String product_detail = JSONParser.getStringFromJsonString(
						"product_list", data);
				od = (OrderInfo) JSONParser.JSON2Object(order_detail,
						OrderInfo.class);
				product_list = (ArrayList<ProductInfo>) JSONParser.JSON2Array(
						product_detail, ProductInfo.class);
				checkOrderStatus(od.co_status);
			} else {
				C.showShort(JSONParser.getStringFromJsonString("error_dec", t),
						mActivity);
			}
		};

		public void onFailure(Throwable t, int errorNo, String strMsg) {
			super.onFailure(t, errorNo, strMsg);
			C.showShort("订单错误f", mActivity);
		};
	};
	/**
	 * 创建退货订单回调
	 */
	AjaxCallBack<String> createReturnOrder = new AjaxCallBack<String>() {
		public void onSuccess(String t) {
			super.onSuccess(t);
			LG.i(getClass(), "T=======>" + t);
			if (JSONParser.getStringFromJsonString("status", t).equals("q")) {
				String data = JSONParser.getStringFromJsonString("data", t);
				refund_order_no = JSONParser.getStringFromJsonString(
						"refund_order_id", data);
				// 创建退货订单成功
				return_ok.setEnabled(true);
				// 退货订单退款
				ConnectManager.getInstance().getReturnOrderFormResult(
						refund_order_no, refundordercallback);
			} else {
				C.showShort(JSONParser.getStringFromJsonString("error_dec", t),
						mActivity);
			}
		};

		public void onFailure(Throwable t, int errorNo, String strMsg) {
			super.onFailure(t, errorNo, strMsg);
			C.showShort("创建退货订单失败", mActivity);
			LG.i(getClass(), "strMsg===>"+strMsg);
		};
	};
	/**
	 * 退款回调
	 */
	AjaxCallBack<String> refundordercallback = new AjaxCallBack<String>() {
		public void onSuccess(String t) {
			super.onSuccess(t);
			if (JSONParser.getStringFromJsonString("status", t).equals("1")) {
				String data = JSONParser.getStringFromJsonString("data", t);
				if (JSONParser.getStringFromJsonString("result", data).equals(
						"ok")) {
					C.showShort("退款成功", mActivity);
					AppManager.getAppManager().finishActivity(mActivity);
				} else {
					C.showShort("退款失败", mActivity);
				}
			} else {
				C.showShort(JSONParser.getStringFromJsonString("error_dec", t),
						mActivity);
			}
		};

		public void onFailure(Throwable t, int errorNo, String strMsg) {
			super.onFailure(t, errorNo, strMsg);
			C.showShort("退款失败", mActivity);
		};
	};

	CheckBoxChangeListener cbcl = new CheckBoxChangeListener() {

		@Override
		public void setCheckedPosition(ArrayList<ProductInfo> dataList) {
			refund_product_list = new ArrayList<ProductInfo>();
			refund_product_list.clear();
			for (int i = 0; i < dataList.size(); i++) {
				if(dataList.get(i).isChecked){
					refund_product_list.add(dataList.get(i));
				}
			}
			order_money = 0;
			for (int i = 0; i < dataList.size(); i++) {
				if (dataList.get(i).isChecked) {
					order_money += (product_list.get(i).cop_number * product_list
							.get(i).cop_price);
				}
			}
			return_money.setText("退款金额:" + order_money);
		}
	};

}