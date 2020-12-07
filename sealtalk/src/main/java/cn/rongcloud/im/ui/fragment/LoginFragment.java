package cn.rongcloud.im.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Looper;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import cn.rongcloud.im.R;
import cn.rongcloud.im.common.IntentExtra;
import cn.rongcloud.im.model.CountryInfo;
import cn.rongcloud.im.model.Resource;
import cn.rongcloud.im.model.Status;
import cn.rongcloud.im.model.UserCacheInfo;
import cn.rongcloud.im.ui.Parent.ConfigUtil.ConfigUtil;
import cn.rongcloud.im.ui.Parent.Entities.Parent;
import cn.rongcloud.im.ui.Parent.Entities.Teacher;
import cn.rongcloud.im.ui.Parent.Entities.Variable;
import cn.rongcloud.im.ui.activity.MainActivity;
import cn.rongcloud.im.ui.activity.SelectCountryActivity;
import cn.rongcloud.im.ui.widget.ClearWriteEditText;
import cn.rongcloud.im.viewmodel.LoginViewModel;

public class LoginFragment extends BaseFragment implements RadioGroup.OnCheckedChangeListener{
    private static final int REQUEST_CODE_SELECT_COUNTRY = 1000;
    private ClearWriteEditText phoneNumberEdit;
    private ClearWriteEditText passwordEdit;
    private TextView countryNameTv;
    private TextView countryCodeTv;
    private RadioGroup radioGroup;
    private LoginViewModel loginViewModel;

    private String flagLogin=null;

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId){
            case R.id.login_radio_teacher:
                //如果为买家身份，则将标识符置为1
                Variable.loginIdentity=1;
                Log.e("教师","1");
                break;
            case R.id.login_radio_parent:
                //如果为卖家身份，则将标识符置为2
                Variable.loginIdentity=2;
                Log.e("家长","2");
                break;
        }
    }
    @Override
    protected int getLayoutResId() {
        return R.layout.login_fragment_login;
    }

    @Override
    protected void onInitView(Bundle savedInstanceState, Intent intent) {

        phoneNumberEdit = findView(R.id.cet_login_phone);
        passwordEdit = findView(R.id.cet_login_password);
        radioGroup=findView(R.id.login_identity);
        countryNameTv = findView(R.id.tv_country_name);
        countryCodeTv = findView(R.id.tv_country_code);
        findView(R.id.btn_login, true);
        findView(R.id.ll_country_select, true);
        radioGroup.setOnCheckedChangeListener(this);
        phoneNumberEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 11) {
                    phoneNumberEdit.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(phoneNumberEdit.getWindowToken(), 0);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    @Override
    protected void onInitViewModel() {
        loginViewModel = ViewModelProviders.of(getActivity()).get(LoginViewModel.class);
        loginViewModel.getLoginResult().observe(this, new Observer<Resource<String>>() {
            @Override
            public void onChanged(Resource<String> resource) {
                if (resource.status == Status.SUCCESS) {
                    dismissLoadingDialog(new Runnable() {
                        @Override
                        public void run() {
                            showToast(R.string.seal_login_toast_success);
                            toMain(resource.data);
                        }
                    });

                } else if (resource.status == Status.LOADING) {
                    showLoadingDialog(R.string.seal_loading_dialog_logining);
                } else {
                    dismissLoadingDialog(new Runnable() {
                        @Override
                        public void run() {
                            showToast(resource.message);
                        }
                    });
                }
            }
        });

        loginViewModel.getLastLoginUserCache().observe(this, new Observer<UserCacheInfo>() {
            @Override
            public void onChanged(UserCacheInfo userInfo) {
                phoneNumberEdit.setText(userInfo.getPhoneNumber());
                String region = userInfo.getRegion();
                if (!region.startsWith("+")) {
                    region = "+" + region;
                }
                countryCodeTv.setText(region);
                CountryInfo countryInfo = userInfo.getCountryInfo();
                if (countryInfo != null && !TextUtils.isEmpty(countryInfo.getCountryName())) {
                    countryNameTv.setText(countryInfo.getCountryName());
                }
                passwordEdit.setText(userInfo.getPassword());
            }
        });
    }


    @Override
    protected void onClick(View v, int id) {
        switch (id) {
            case R.id.btn_login:
                String phoneStr = phoneNumberEdit.getText().toString().trim();
                String passwordStr = passwordEdit.getText().toString().trim();
                String countryCodeStr = countryCodeTv.getText().toString().trim();
                sendLoginInfo();
                if(Variable.loginIdentity==1){
                    System.out.println("1111111111111");
                    Log.e("111111111111","q");
                    receiveTeacherLoginInfo(ConfigUtil.SERVER_ADDR+"ReturnTeacherLoginServlet");
                }else if(Variable.loginIdentity==2){
                    System.out.println("22222222222222222");
                    Log.e("2222222222222222","q");
                    receiveParentLoginInfo(ConfigUtil.SERVER_ADDR+"ReturnParentLoginServlet");
                }
                if (TextUtils.isEmpty(phoneStr)) {
                    showToast(R.string.seal_login_toast_phone_number_is_null);
                    phoneNumberEdit.setShakeAnimation();
                    break;
                }

                if (TextUtils.isEmpty(passwordStr)) {
                    showToast(R.string.seal_login_toast_password_is_null);
                    passwordEdit.setShakeAnimation();
                    return;
                }

                if (passwordStr.contains(" ")) {
                    showToast(R.string.seal_login_toast_password_cannot_contain_spaces);
                    passwordEdit.setShakeAnimation();
                    return;
                }
                if(TextUtils.isEmpty(countryCodeStr)){
                    countryCodeStr = "86";
                }else if(countryCodeStr.startsWith("+")){
                    countryCodeStr = countryCodeStr.substring(1);
                }
//                if(flagLogin.equals("true")){
//                    Log.e("收到确认的数据：",flagLogin+"");
                    login(countryCodeStr, phoneStr, passwordStr);
//                }

                break;
            case R.id.ll_country_select:
                // 跳转区域选择界面
                startActivityForResult(new Intent(getActivity(), SelectCountryActivity.class), REQUEST_CODE_SELECT_COUNTRY);
                break;
            default:
                break;
        }
    }

    /**
     * 登录到 业务服务器，以获得登录 融云 IM 服务器所必须的 token
     *
     * @param region 国家区号
     * @param phone  电话号/帐号
     * @param pwd    密码
     */
    private void login(String region, String phone, String pwd) {
        loginViewModel.login(region, phone, pwd);
    }

    private void toMain(String userId) {
        Intent intent = new Intent(getActivity(), MainActivity.class);
        intent.putExtra(IntentExtra.USER_ID, userId);
        startActivity(intent);
        getActivity().finish();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == getActivity().RESULT_OK && requestCode == REQUEST_CODE_SELECT_COUNTRY) {
            CountryInfo info = data.getParcelableExtra(SelectCountryActivity.RESULT_PARAMS_COUNTRY_INFO);
            countryNameTv.setText(info.getCountryName());
            countryCodeTv.setText(info.getZipCode());
        }
    }

    /**
     * 设置上参数
     * @param phone
     * @param region
     * @param countryName
     */
    public void setLoginParams(String phone, String region, String countryName) {
        phoneNumberEdit.setText(phone);
        countryNameTv.setText(countryName);
        countryCodeTv.setText(region);
    }

    public void sendLoginInfo(){
        Thread t1=new Thread(){
            @Override
            public void run() {
                super.run();
                try {
                    String phone=phoneNumberEdit.getText().toString().trim();
                    String pwd=passwordEdit.getText().toString().trim();
                    URL url=null;
                    if(Variable.loginIdentity==1){
                        url=new URL(ConfigUtil.SERVER_ADDR +"ReceiveTeacherLoginServlet"+"?teacherPhone="+phone+"&teacherPwd="+pwd+"&teacherIdentity="+Variable.loginIdentity);
                        System.out.println("登录给服务端的字符串："+ConfigUtil.SERVER_ADDR+"?userName="+phone+"&userPwd="+pwd+"&userIdentity="+Variable.loginIdentity);
                    }else if(Variable.loginIdentity==2){
                        url=new URL(ConfigUtil.SERVER_ADDR +"ReceiveParentLoginServlet"+"?parentPhone="+phone+"&parentPwd="+pwd+"&parentIdentity="+Variable.loginIdentity);
                        System.out.println("登录给服务端的字符串："+ConfigUtil.SERVER_ADDR+"?userName="+phone+"&userPwd="+pwd+"&userIdentity="+Variable.loginIdentity);
                    }

                    url.openStream();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        t1.start();
        try {
            t1.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    public void receiveTeacherLoginInfo(final String str){
        Thread t2=new Thread(){
            @Override
            public void run() {
                super.run();
                //使用网络连接，接收服务端发送的字符串
                try {
                    Log.e("111111111111","111111111111111111111111111");
                    //创建url对象
                    URL url=new URL(str);
                    //获取URLConnection连接对象
                    URLConnection conn=url.openConnection();
                    //获取网络输入流
                    InputStream in=conn.getInputStream();
                    //使用字符流读取
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(in, "utf-8"));
                    //读取字符信息
                    String teacherInfo =reader.readLine();
                    Teacher teacher=new Teacher();
                    teacher.setId(teacherInfo.split("&")[0]);
                    teacher.setName(teacherInfo.split("&")[1].split("&&")[0]);
                    teacher.setSex(teacherInfo.split("&&")[1].split("&&&")[0]);
                    teacher.setAge(teacherInfo.split("&&&")[1].split("&&&&")[0]);
                    teacher.setSchoolId(teacherInfo.split("&&&&")[1].split("&&&&&")[0]);
                    teacher.setGradeId(teacherInfo.split("&&&&&")[1].split("&&&&&&")[0]);
                    teacher.setClassId(teacherInfo.split("&&&&&&")[1].split("&&&&&&&")[0]);
                    teacher.setPoints(teacherInfo.split("&&&&&&&")[1].split("&&&&&&&&")[0]);
                    teacher.setPhone(teacherInfo.split("&&&&&&&&")[1].split("&&&&&&&&&")[0]);
                    teacher.setPwd(teacherInfo.split("&&&&&&&&&")[1].split("&&&&&&&&&&")[0]);
                    Variable.teacher=teacher;
                    reader.close();
                    in.close();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        t2.start();
        try {
            t2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    public void receiveParentLoginInfo(final String str){
        Thread t3=new Thread(){
            @Override
            public void run() {
                super.run();
                //使用网络连接，接收服务端发送的字符串
                try {
                    Log.e("111111111111","111111111111111111111111111");
                    //创建url对象
                    URL url=new URL(str);
                    //获取URLConnection连接对象
                    URLConnection conn=url.openConnection();
                    //获取网络输入流
                    InputStream in=conn.getInputStream();
                    //使用字符流读取
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(in, "utf-8"));
                    //读取字符信息
                    String parentInfo =reader.readLine();
                    Parent parent=new Parent();
                    parent.setId(parentInfo.split("&")[0]);
                    parent.setName(parentInfo.split("&")[1].split("&&")[0]);
                    parent.setSex(parentInfo.split("&&")[1].split("&&&")[0]);
                    parent.setAge(parentInfo.split("&&&")[1].split("&&&&")[0]);
                    parent.setPhone(parentInfo.split("&&&&")[1].split("&&&&&")[0]);
                    parent.setPwd(parentInfo.split("&&&&&")[1].split("&&&&&&")[0]);
                    parent.setPoints(parentInfo.split("&&&&&&")[1].split("&&&&&&&")[0]);
                    parent.setChildName(parentInfo.split("&&&&&&&")[1].split("&&&&&&&&")[0]);
                    parent.setChildSex(parentInfo.split("&&&&&&&&")[1].split("&&&&&&&&&")[0]);
                    parent.setSchoolId(parentInfo.split("&&&&&&&&&")[1].split("&&&&&&&&&&")[0]);
                    parent.setClassId(parentInfo.split("&&&&&&&&&&")[1].split("&&&&&&&&&&&")[0]);
                    Variable.parent=parent;
                    reader.close();
                    in.close();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        t3.start();
        try {
            t3.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
