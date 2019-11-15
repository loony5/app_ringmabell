package com.example.carpe.ringmabell;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private EditText edit_email, edit_password;
    private Button btn_login_success;

    SessionManager sessionManager;

    private static String URL_LOGIN = "http://ec2-52-78-18-104.ap-northeast-2.compute.amazonaws.com/ringmabell/login.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sessionManager = new SessionManager(this);

        edit_email = findViewById(R.id.edit_email);
        edit_password = findViewById(R.id.edit_password);

        btn_login_success = findViewById(R.id.btn_login_success);

        // 로그인 버튼을 선택했을 때,
        btn_login_success.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String str_email = edit_email.getText().toString().trim();
                String str_pass = edit_password.getText().toString().trim();

                // 입력란이 비어있지 않으면, 로그인 메소드 실행
                if(!str_email.isEmpty() && !str_pass.isEmpty()) {

                    Login(str_email, str_pass);

                    // 비어 있으면,
                } else {

                    Toast.makeText(LoginActivity.this, "알맞게 입력해주세요.", Toast.LENGTH_LONG).show();

                }
            }
        });
    }

    // 모두 입력했을 때, 입력한 이메일과 패스워드를 파라미터로 전달받는다.
    private void Login(final String str_email, final String str_pass) {

        // StringRequest - URL을 지정해주고, POST 로 파라미터 값을 보내서 응답을 받는다.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_LOGIN,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {

                            // JSONObject 를 인스턴스. response(응답) 값을 가져온다.
                            JSONObject jsonObject = new JSONObject(response);
                            String success = jsonObject.getString("success");
                            JSONArray jsonArray = jsonObject.getJSONArray("login");

                            // php 로 받은 값이 "1" 일 경우 (회원 데이터가 있다는 뜻)
                            if(success.equals("1")) {

                                for(int i = 0; i < jsonArray.length(); i++) {

                                    // JSONObject 를 생성해서 받은 값을 넣는다.
                                    JSONObject object = jsonArray.getJSONObject(i);

                                    // login.php 에서 얻은 값을 변수에 넣음.
                                    String name = object.getString("name").trim();
                                    String email = object.getString("email").trim();
                                    String phone = object.getString("phone").trim();
                                    String photo = object.getString("photo").trim();
                                    String id = object.getString("id").trim();

                                    // SessionManager.java 에 createSession 메소드로 값을 전달한다.
                                    sessionManager.createSession(name,email,phone,photo,id);
                                    Log.e("LoginActivity.java", "sessionManager 로 넘길 파라미터 값은 " + name + email + phone + photo + id);

                                    Toast.makeText(LoginActivity.this, "'"+name+"'님 환영합니다.", Toast.LENGTH_LONG).show();

                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                    startActivity(intent);

                                }
                            } else {

                                Toast.makeText(LoginActivity.this, "이메일과 비밀번호를 확인하세요.", Toast.LENGTH_SHORT).show();

                            }

                        } catch (JSONException e){

                            e.printStackTrace();
                            Toast.makeText(LoginActivity.this, "이메일과 비밀번호를 확인하세요.", Toast.LENGTH_LONG).show();

                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Toast.makeText(LoginActivity.this, "로그인에 실패했습니다." + error.toString(), Toast.LENGTH_LONG).show();

            }
        })
        {
            // 입력값 str_email, str_pass 를 php 로 보낸다.
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("email", str_email);
                params.put("password", str_pass);
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);

    }
}
