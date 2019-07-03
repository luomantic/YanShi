package com.luomantic.yanshi.adapter;

import android.graphics.Color;
import android.support.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.luomantic.yanshi.R;

import java.util.List;

import static com.luomantic.yanshi.app.Constant.wifi_username;

public class SendWifiAdapter extends BaseQuickAdapter<String, BaseViewHolder> {

    public SendWifiAdapter(int layoutResId, @Nullable List<String> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, String item) {
        helper.setText(R.id.tv_item_send_wifi, item);
        if (null != wifi_username) {
            if (item.equals(wifi_username)) {
                helper.getView(R.id.tv_item_send_wifi).setBackgroundColor(mContext.getResources().getColor(R.color.commonBlue));
            }else {
                helper.getView(R.id.tv_item_send_wifi).setBackgroundColor(Color.parseColor("#ffffff"));
            }
        }
    }
}
