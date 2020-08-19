// Generated code from Butter Knife. Do not modify!
package com.nimble.vpn.dialog;

import android.view.View;
import android.widget.EditText;
import androidx.annotation.CallSuper;
import androidx.annotation.UiThread;
import butterknife.Unbinder;
import butterknife.internal.DebouncingOnClickListener;
import butterknife.internal.Utils;
import com.nimble.vpn.R;
import java.lang.IllegalStateException;
import java.lang.Override;

public class LoginDialog_ViewBinding implements Unbinder {
  private LoginDialog target;

  private View view7f0900b3;

  @UiThread
  public LoginDialog_ViewBinding(final LoginDialog target, View source) {
    this.target = target;

    View view;
    target.hostUrlEditText = Utils.findRequiredViewAsType(source, R.id.host_url_ed, "field 'hostUrlEditText'", EditText.class);
    target.carrierIdEditText = Utils.findRequiredViewAsType(source, R.id.carrier_id_ed, "field 'carrierIdEditText'", EditText.class);
    view = Utils.findRequiredView(source, R.id.login_btn, "method 'onLoginBtnClick'");
    view7f0900b3 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onLoginBtnClick(p0);
      }
    });
  }

  @Override
  @CallSuper
  public void unbind() {
    LoginDialog target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.hostUrlEditText = null;
    target.carrierIdEditText = null;

    view7f0900b3.setOnClickListener(null);
    view7f0900b3 = null;
  }
}
