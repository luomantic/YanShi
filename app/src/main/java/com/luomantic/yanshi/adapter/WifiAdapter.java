package com.luomantic.yanshi.adapter;

import android.support.annotation.Nullable;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.luomantic.yanshi.R;
import com.luomantic.yanshi.bean.CardBean;

import java.util.List;

public class WifiAdapter extends BaseQuickAdapter<CardBean, BaseViewHolder> {

    public WifiAdapter(int layoutResId, @Nullable List<CardBean> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, CardBean item) {
        helper.setText(R.id.tv_card_num, item.getCardNum());
        helper.setText(R.id.tv_ip_address, item.getIpAddress());

        if (item.isOnline() == 0) { // 绿色表示在线，红色表示不在线
            ((TextView)helper.getView(R.id.tv_card_num)).setTextColor(mContext.getResources().getColor(R.color.colorAccent));
            ((TextView)helper.getView(R.id.tv_ip_address)).setTextColor(mContext.getResources().getColor(R.color.colorAccent));
        }else {
            ((TextView)helper.getView(R.id.tv_card_num)).setTextColor(mContext.getResources().getColor(R.color.colorPrimary));
            ((TextView)helper.getView(R.id.tv_ip_address)).setTextColor(mContext.getResources().getColor(R.color.colorPrimary));
        }
    }

}
