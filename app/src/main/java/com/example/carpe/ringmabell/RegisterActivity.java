package com.example.carpe.ringmabell;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    private EditText edit_email, edit_name, edit_password, edit_c_password, edit_phone;
    private Button btn_register_success;
    private CheckBox chx_all, chx_first, chx_second;

    private static String URL_REGISTER = "http://ec2-52-78-18-104.ap-northeast-2.compute.amazonaws.com/ringmabell/register.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        edit_email = findViewById(R.id.edit_email);
        edit_name = findViewById(R.id.edit_name);
        edit_password = findViewById(R.id.edit_password);
        edit_c_password = findViewById(R.id.edit_c_password);
        edit_phone = findViewById(R.id.edit_phone);

        // 핸드폰 번호 입력시 자동으로 하이픈("-")을 넣어준다.
        edit_phone.addTextChangedListener(new PhoneNumberFormattingTextWatcher());

        // 이용 약관, 개인정보동의를 위한 체크박스
        chx_all = findViewById(R.id.chx_all);
        chx_first = findViewById(R.id.chx_first);
        chx_second = findViewById(R.id.chx_second);

        btn_register_success = findViewById(R.id.btn_register_success);

        chx_all.setOnClickListener(new CheckBox.OnClickListener(){

            @Override
            public void onClick(View v) {

                // toggle 은 현재 상태에서 반대로 체크를 하게 해준다.
                chx_first.toggle();
                chx_second.toggle();
            }
        });

        btn_register_success.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String str_email = edit_email.getText().toString().trim();
                String str_name = edit_name.getText().toString().trim();
                String str_pass = edit_password.getText().toString().trim();
                String str_c_pass = edit_c_password.getText().toString().trim();
                String str_phone = edit_phone.getText().toString().trim();

                // 입력란이 하나라도 비어있는 경우 Toast 아니면, 비밀번호와 비밀번호 확인란이 같은 값인지 확인하고 register 메소드 실행한다.
                if(str_email.isEmpty() || str_name.isEmpty() || str_pass.isEmpty() || str_c_pass.isEmpty() || str_phone.isEmpty()) {

                    Toast.makeText(RegisterActivity.this, "빈 칸을 모두 채워주세요.", Toast.LENGTH_SHORT).show();

                } else if ((!str_email.isEmpty() && !str_name.isEmpty() && !str_pass.isEmpty() && !str_c_pass.isEmpty()
                        && !str_phone.isEmpty()) && !chx_first.isChecked() || !chx_second.isChecked()) {

                    Toast.makeText(RegisterActivity.this, "전체 동의를 해주세요.", Toast.LENGTH_SHORT).show();

                    // 이메일 형식을 쉽게 확인할 수 있게 해준다.
                } else if (!Patterns.EMAIL_ADDRESS.matcher(str_email).matches()) {

                    Toast.makeText(RegisterActivity.this, "이메일을 다시 확인해주세요.", Toast.LENGTH_SHORT).show();

                    // 전화번호 형식 확인
                } else if(!Pattern.matches("^01(?:0|1|[6-9])-(?:\\d{3}|\\d{4})-\\d{4}$", str_phone)) {

                    Toast.makeText(RegisterActivity.this, "연락처를 다시 확인해주세요.", Toast.LENGTH_SHORT).show();

                } else {

                    if(!str_pass.equals(str_c_pass)){

                        Toast.makeText(RegisterActivity.this, "비밀번호가 같지 않습니다.", Toast.LENGTH_SHORT).show();
                    } else {

                        // register 메소드로 입력값 보내기
                        register(str_email, str_name, str_pass, str_phone);

                    }
                }
            }
        });

    }

    // 모두 바르게 입력했을때, email, name, password 를 파라미터로 넣는다.
    private void register(final String str_email, final String str_name, final String str_pass, final String str_phone) {

        // StringRequest - URL을 지정해주고, POST 로 값을 보내서 PHP 응답값을 받는다.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_REGISTER,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try{
                            // JSONObject 를 인스턴스화해서 response(응답) 값을 가져온다.
                            JSONObject jsonObject = new JSONObject(response);

                            // success 변수에 jsonObject 의 key 값을 담는다.
                            String success = jsonObject.getString("success");

                            // key 의 value 값이 "1"과 같다면, 가입완료 -> 로그인 화면으로 이동.
                            if(success.equals("1")){
                                Toast.makeText(RegisterActivity.this, "가입이 완료되었습니다.", Toast.LENGTH_LONG).show();

                                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                startActivity(intent);

                                // key 의 value 값이 "2" 와 같다면, 이미 가입된 이메일
                            } else if(success.equals("2")) {
                                Toast.makeText(RegisterActivity.this, "이미 가입된 이메일이 있습니다.", Toast.LENGTH_LONG).show();

                            }

                        } catch (JSONException e){
                            e.printStackTrace();
                            Toast.makeText(RegisterActivity.this, "가입에 실패했습니다." + e.toString(), Toast.LENGTH_LONG).show();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(RegisterActivity.this, "가입에 실패했습니다." + error.toString(), Toast.LENGTH_LONG).show();

            }
        })

        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                // key, value 값으로 php 에 던져준다.
                Map<String, String> params = new HashMap<>();
                params.put("email", str_email);
                params.put("name", str_name);
                params.put("password", str_pass);
                params.put("phone", str_phone);

                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);

    }
}
