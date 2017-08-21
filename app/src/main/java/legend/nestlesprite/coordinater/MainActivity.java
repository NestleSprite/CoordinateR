package legend.nestlesprite.coordinater;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;

public class MainActivity extends AppCompatActivity implements AMapLocationListener {
    Button button;
    TextView txt;
    Button exit;


    public AMapLocationClientOption mLocationOption = null;

    AMapLocationClient mlocationClient;
    double lat = 0;
    double lng = 0;
    AlertDialog dialog;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_m);
        button = (Button) findViewById(R.id.button);
        txt = (TextView) findViewById(R.id.txt);
        exit = (Button) findViewById(R.id.send);
        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        button.setEnabled(false);
        button.setAlpha(0.3f);
        txt.setVisibility(View.INVISIBLE);

        dialog = new AlertDialog.Builder(this).setCancelable(false).setMessage(getString(R.string.warrning)).setPositiveButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                doInitAction();
            }
        }).setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        }).create();

        progressDialog = new ProgressDialog(this, R.style.WaitingProgressDialog);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("等5秒钟，谢谢");
        //这里以ACCESS_COARSE_LOCATION为例
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            //申请WRITE_EXTERNAL_STORAGE权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    666);//自定义的code
        } else {
            dialog.show();


        }


        mlocationClient = new AMapLocationClient(this);
//初始化定位参数
        mLocationOption = new AMapLocationClientOption();
//设置定位监听
        mlocationClient.setLocationListener(this);
//设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
//设置定位间隔,单位毫秒,默认为2000ms
        mLocationOption.setInterval(1000);

//设置定位参数
        mlocationClient.setLocationOption(mLocationOption);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.show();
                mlocationClient.startLocation();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                        txt.setVisibility(View.VISIBLE);
                    }
                }, 5000);

            }
        });
    }

    private void doInitAction() {
        button.setEnabled(true);
        button.setAlpha(1f);
    }

    @Override
    public void onLocationChanged(AMapLocation amapLocation) {
        if (amapLocation != null) {
            if (amapLocation.getErrorCode() == 0) {
                //定位成功回调信息，设置相关消息
                amapLocation.getLocationType();//获取当前定位结果来源，如网络定位结果，详见定位类型表
                double slat = amapLocation.getLatitude();//获取纬度
                double slng = amapLocation.getLongitude();//获取经度
                amapLocation.getAccuracy();//获取精度信息


                lat = GCJ2WGS.delta(slat, slng).get("lat");
                lng = GCJ2WGS.delta(slat, slng).get("lon");

                txt.setText("纬度： " + lat + "\n经度： " + lng + "\n" + amapLocation.getAddress()+"\n\n\n 上述显示的经纬度是真实GPS坐标（WGS-84）");


            } else {
                //显示错误信息ErrCode是错误码，errInfo是错误信息，详见错误码表。
                Log.e("AmapError", "location Error, ErrCode:"
                        + amapLocation.getErrorCode() + ", errInfo:"
                        + amapLocation.getErrorInfo());
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == 0) {
            dialog.show();
        } else {
            finish();
        }

    }


}
