package cn.rongcloud.im.ui.Parent.SmallActivity.SchoolActivity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import cn.rongcloud.im.R;

public class ListDetailActivity3 extends AppCompatActivity {

    private Button btn_open;
    private boolean status=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_detail3);

        btn_open.setText("点击参与");
        btn_open= (Button) findViewById(R.id.activity_btn_good3);

    }

    public void onClick(View view) {
        if(!status){
            btn_open.setText("已参与");
            status=true;
        }else{
            btn_open.setText("点击参与");
            status=false;
        }
    }
}
