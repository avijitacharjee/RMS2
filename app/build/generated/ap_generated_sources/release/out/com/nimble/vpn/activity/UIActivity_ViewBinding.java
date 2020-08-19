// Generated code from Butter Knife. Do not modify!
package com.nimble.vpn.activity;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.CallSuper;
import androidx.annotation.UiThread;
import butterknife.Unbinder;
import butterknife.internal.DebouncingOnClickListener;
import butterknife.internal.Utils;
import com.nimble.vpn.R;
import java.lang.IllegalStateException;
import java.lang.Override;

public class UIActivity_ViewBinding implements Unbinder {
  private UIActivity target;

  private View view7f0900a2;

  private View view7f0900e0;

  private View view7f0900a9;

  @UiThread
  public UIActivity_ViewBinding(UIActivity target) {
    this(target, target.getWindow().getDecorView());
  }

  @UiThread
  public UIActivity_ViewBinding(final UIActivity target, View source) {
    this.target = target;

    View view;
    target.server_ip = Utils.findRequiredViewAsType(source, R.id.server_ip, "field 'server_ip'", TextView.class);
    view = Utils.findRequiredView(source, R.id.img_connect, "field 'img_connect' and method 'onConnectBtnClick'");
    target.img_connect = Utils.castView(view, R.id.img_connect, "field 'img_connect'", ImageView.class);
    view7f0900a2 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onConnectBtnClick(p0);
      }
    });
    target.connectBtnTextView = Utils.findRequiredViewAsType(source, R.id.connect_btn, "field 'connectBtnTextView'", TextView.class);
    target.connectionStateTextView = Utils.findRequiredViewAsType(source, R.id.connection_state, "field 'connectionStateTextView'", TextView.class);
    target.connectionProgressBar = Utils.findRequiredViewAsType(source, R.id.connection_progress, "field 'connectionProgressBar'", ProgressBar.class);
    target.trafficLimitTextView = Utils.findRequiredViewAsType(source, R.id.traffic_limit, "field 'trafficLimitTextView'", TextView.class);
    view = Utils.findRequiredView(source, R.id.optimal_server_btn, "field 'currentServerBtn' and method 'onServerChooserClick'");
    target.currentServerBtn = Utils.castView(view, R.id.optimal_server_btn, "field 'currentServerBtn'", LinearLayout.class);
    view7f0900e0 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onServerChooserClick(p0);
      }
    });
    target.selectedServerTextView = Utils.findRequiredViewAsType(source, R.id.selected_server, "field 'selectedServerTextView'", TextView.class);
    target.country_flag = Utils.findRequiredViewAsType(source, R.id.country_flag, "field 'country_flag'", ImageView.class);
    target.txt_download = Utils.findRequiredViewAsType(source, R.id.txt_download, "field 'txt_download'", TextView.class);
    target.txt_upload = Utils.findRequiredViewAsType(source, R.id.txt_upload, "field 'txt_upload'", TextView.class);
    target.lin_spped = Utils.findRequiredViewAsType(source, R.id.lin_spped, "field 'lin_spped'", LinearLayout.class);
    view = Utils.findRequiredView(source, R.id.lay_pro, "field 'lay_pro' and method 'openPro'");
    target.lay_pro = Utils.castView(view, R.id.lay_pro, "field 'lay_pro'", LinearLayout.class);
    view7f0900a9 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.openPro(p0);
      }
    });
  }

  @Override
  @CallSuper
  public void unbind() {
    UIActivity target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.server_ip = null;
    target.img_connect = null;
    target.connectBtnTextView = null;
    target.connectionStateTextView = null;
    target.connectionProgressBar = null;
    target.trafficLimitTextView = null;
    target.currentServerBtn = null;
    target.selectedServerTextView = null;
    target.country_flag = null;
    target.txt_download = null;
    target.txt_upload = null;
    target.lin_spped = null;
    target.lay_pro = null;

    view7f0900a2.setOnClickListener(null);
    view7f0900a2 = null;
    view7f0900e0.setOnClickListener(null);
    view7f0900e0 = null;
    view7f0900a9.setOnClickListener(null);
    view7f0900a9 = null;
  }
}
