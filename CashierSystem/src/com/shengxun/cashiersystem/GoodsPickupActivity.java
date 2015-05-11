package com.shengxun.cashiersystem;

import java.util.ArrayList;
import java.util.List;

import net.tsz.afinal.http.AjaxCallBack;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.shengxun.adapter.CashierPickupGoodsAdapter;
import com.shengxun.constant.C;
import com.shengxun.entity.OrderInfo;
import com.shengxun.entity.ProductInfo;
import com.shengxun.util.ConnectManager;
import com.zvezda.android.utils.AppManager;
import com.zvezda.android.utils.BaseUtils;
import com.zvezda.android.utils.JSONParser;
import com.zvezda.android.utils.LG;

/**
 * 订单提货界面
 * 
 * @author sw
 * @date 2015-5-4
 */
public class GoodsPickupActivity extends BaseActivity {

	private EditText et_order_no, et_card_no;
	private Button ok, exit, swing_card, check_order;
	private String card_no, order_no;
	private ArrayList<ProductInfo> product_list;
	private CashierPickupGoodsAdapter cpga;
	private ListView lv;
	private double total_money = 0;
	private TextView show_money;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.cashier_goods_pickup_view);

		initWidget();
	}

	private void initWidget() {
		product_list = new ArrayList<ProductInfo>();
		et_order_no = (EditText) findViewById(R.id.cashier_goods_pickup_order_no);
		ok = (Button) findViewById(R.id.cashier_goods_pickup_ok);
		exit = (Button) findViewById(R.id.cashier_goods_pickup_exit);
		et_card_no = (EditText) findViewById(R.id.cashier_goods_pickup_card_no);
		swing_card = (Button) findViewById(R.id.cashier_goods_pickup_swing_card);
		check_order = (Button) findViewById(R.id.cashier_goods_pickup_checkorder);
		lv = (ListView) findViewById(R.id.cashier_goods_pickup_lv);
		show_money = (TextView) findViewById(R.id.cashier_goods_pickup_money);

		ok.setOnClickListener(myclick);
		check_order.setOnClickListener(myclick);
		exit.setOnClickListener(myclick);
		swing_card.setOnClickListener(myclick);
	}

	OnClickListener myclick = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			// 开始订单提货
			case R.id.cashier_goods_pickup_ok:
				if (BaseUtils.IsNotEmpty(applicationCS.cashier_card_no)) {
					ConnectManager.getInstance().getOrderFormPickUpResult(
							order_no, card_no,applicationCS.cashier_card_no, ajaxCallBack);
				}
				break;
			// 退出
			case R.id.cashier_goods_pickup_exit:
				AppManager.getAppManager().finishActivity(mActivity);
				break;
			case R.id.cashier_goods_pickup_swing_card:
				// 验证卡号
				break;
			case R.id.cashier_goods_pickup_checkorder:
				order_no = et_order_no.getText().toString().trim();
				card_no = et_card_no.getText().toString().trim();
				// 验证消费者卡号非空
				if (BaseUtils.IsNotEmpty(card_no)) {
					// 验证订单号非空
					if (BaseUtils.IsNotEmpty(order_no)) {
						// 查询该订单是否存在
						ConnectManager.getInstance().getOrderFormDetailResult(
								order_no, ordercheck);
					} else {
						C.showShort("请输入订单号", mActivity);
					}
				} else {
					C.showShort("请刷卡", mActivity);
				}
				break;
			default:
				break;
			}
		}
	};

	/**
	 * 订单提货接口回调
	 */
	AjaxCallBack<String> ajaxCallBack = new AjaxCallBack<String>() {

		public void onSuccess(String t) {
			super.onSuccess(t);
			LG.i(getClass(), "t=====>" + t);
			if (JSONParser.getStringFromJsonString("status", t).equals("1")) {
				String data = JSONParser.getStringFromJsonString("data", t);
				if (JSONParser.getStringFromJsonString("result", data).equals(
						"ok")) {
					C.showShort("订单提货成功", mActivity);
					AppManager.getAppManager().finishActivity(mActivity);
				} else {
					C.showShort("订单提货失败", mActivity);
				}
			} else {
				C.showShort(JSONParser.getStringFromJsonString("error_dec", t),
						mActivity);
			}
		};

		public void onFailure(Throwable t, int errorNo, String strMsg) {
			super.onFailure(t, errorNo, strMsg);
			C.showShort("订单提货失败", mActivity);
		};
	};

	/**
	 * 刷新商品列表信息
	 * 
	 * @param list
	 * @auth shouwei
	 */
	private void refreshGoodsData(ArrayList<ProductInfo> list) {
		cpga = new CashierPickupGoodsAdapter(mActivity, list);
		lv.setAdapter(cpga);
		total_money = 0;
		for (int i = 0; i < list.size(); i++) {
			total_money += list.get(i).cop_number * list.get(i).cop_price;
		}
		ok.setEnabled(true);
		show_money.setText("总额:" + total_money);
	}

	/**
	 * 订单信息回调
	 */
	AjaxCallBack<String> ordercheck = new AjaxCallBack<String>() {
		public void onFailure(Throwable t, int errorNo, String strMsg) {
			super.onFailure(t, errorNo, strMsg);
			C.showShort("订单错误", mActivity);
		};

		public void onSuccess(String t) {
			super.onSuccess(t);
			LG.i(getClass(), "pick up t ====>" + t);
			if (JSONParser.getStringFromJsonString("status", t).equals("1")) {
				String data = JSONParser.getStringFromJsonString("data", t);
				String order_detail = JSONParser.getStringFromJsonString(
						"order_detail", data);
				String product_detail = JSONParser.getStringFromJsonString(
						"product_list", data);
				OrderInfo od = (OrderInfo) JSONParser.JSON2Object(order_detail,
						OrderInfo.class);
				product_list = (ArrayList<ProductInfo>) JSONParser.JSON2Array(
						product_detail, ProductInfo.class);
				refreshGoodsData(product_list);
			} else {
				C.showShort(JSONParser.getStringFromJsonString("error_dec", t),
						mActivity);
			}
		};
	};
}