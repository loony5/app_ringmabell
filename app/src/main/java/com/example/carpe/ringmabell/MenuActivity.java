package com.example.carpe.ringmabell;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class MenuActivity extends AppCompatActivity {

    private static final String TAG = MenuActivity.class.getSimpleName();

    private TextView txt_name, txt_phone;
    CircleImageView img_profile;
    private ImageView img_close;

    SessionManager sessionManager;

    String getId;

    private static String URL_READ = "http://ec2-52-78-18-104.ap-northeast-2.compute.amazonaws.com/ringmabell/read_detail.php";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        txt_name = findViewById(R.id.txt_name);
        txt_phone = findViewById(R.id.txt_phone);
        img_profile = findViewById(R.id.img_profile);

        img_close = findViewById(R.id.img_close);

        // SessionManager.java 에서 checkLogin() 메소드로 로그인 여부를 확인해서
        // 로그인 상태가 아니면 MenuActivity.java 를 종료 후, 회원가입 & 로그인 화면(IntoActivity.java)로 이동한다.
        sessionManager = new SessionManager(this);
        sessionManager.checkLogin();

        // 로그인 상태이면, SessionManager.java 의 getUserDetail() 메소드에 HashMap 으로 저장한
        // id 값을 가져와서 getId 변수에 담는다.
        HashMap<String, String> user = sessionManager.getUserDetail();
        getId = user.get(sessionManager.ID);


        // 프로필 사진이 보여지는 이미지 뷰 img_profile 을 선택하면 chooseFile() 메소드 실행.
        img_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MenuActivity.this, EditActivity.class);
                startActivity(intent);

            }
        });

//        getUserDetail();

        img_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MenuActivity.this, MainActivity.class);

                // 새로운 task 를 만들어서 root activity 가 된다.
                // 화면을 이동하면서 이전 화면 기록 삭제
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });

    }



    // 로그인 상태일 때, 회원 정보를 불러오는 메소드
    private  void getUserDetail() {

        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_READ,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        Log.i(TAG,response);

                        try {

                            JSONObject jsonObject = new JSONObject(response);
                            String success = jsonObject.getString("success");
                            JSONArray jsonArray = jsonObject.getJSONArray("read");

                            // POST 로 보낸 id 값의 데이터가 있으면, 응답 값 "1"이다.
                            // success 가 "1" 과 같을때, (검색한 데이터가 있을때)
                            if(success.equals("1")) {

                                for(int i = 0; i < jsonArray.length(); i++) {

                                    JSONObject object = jsonArray.getJSONObject(i);

                                    String strName = object.getString("name").trim();
                                    String strPhone = object.getString("phone").trim();
                                    String strPhoto = object.getString("photo").trim();
                                    Log.e("str photo", "strPhoto 는 " + strPhoto);

                                    // id 에 해당하는 name, phone, photo 의 값을 가져와서 화면에 보여준다.
                                    txt_name.setText(strName);
                                    txt_phone.setText(strPhone);

                                    // 이미지가 null 값일때, 조건을 가져와야하는데
                                    // strPhoto.isEmpty() / strPhoto.equals("") / strPhoto.equals(null) / strPhoto == null / strPhoto.length == 0 /
                                    // TextUtils.isEmpty(strPhoto) / 까지 왜 다 null 값인데 이 조건을 못가져오지??
                                    // strPhoto.length() == 4 -> 이것만 null 인 조건이 불러와진다. 이상해...
                                    if(strPhoto.length() == 4) {

                                        Log.e("MenuActivity", "현재 이미지 없음");

                                    } else {

                                        Uri filePath = Uri.parse(strPhoto);
                                        Log.e("MenuActivity.java", "로그인 된 회원의 현재 이미지는 " + filePath);

//                                        Glide.with(MenuActivity.this).load(filePath).into(img_profile);
                                        Glide.with(MenuActivity.this).load(filePath).apply(
                                                new RequestOptions().diskCacheStrategy(DiskCacheStrategy.NONE
                                                ).skipMemoryCache(true)).into(img_profile);

                                    }

                                }
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();

                            Toast.makeText(MenuActivity.this, "데이터를 불러오는데 실패했습니다." + e.toString(), Toast.LENGTH_SHORT).show();

                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Toast.makeText(MenuActivity.this, "데이터를 불러오는데 실패했습니다." + error.toString(), Toast.LENGTH_SHORT).show();

            }
        })

        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                Map<String, String> params = new HashMap<>();
                params.put("id", getId);
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);

    }

    // onResume 상태일 때, geUserDetail() 메소드를 실행한다.
    @Override
    protected  void onResume() {
        super.onResume();
        getUserDetail();
    }


//    private void resetGlide() {
//        Glide.get(this).clearMemory();
//        new AsyncTask<Integer, Void, Void>() {
//
//            @Override
//            protected Void doInBackground(Integer... integers) {
//                Glide.get(MenuActivity.this).clearDiskCache();
//                Log.e("MenuActivity.java", "Glide 메모리, 디스크 캐쉬 삭제!!");
//                return null;
//            }
//        }.execute();
//    }

}
