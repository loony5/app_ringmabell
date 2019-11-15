package com.example.carpe.ringmabell;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditActivity extends AppCompatActivity {

    private static final String TAG = EditActivity.class.getSimpleName();

    private EditText txt_name, txt_email, txt_phone;
    private Button btn_logout;
    private TextView btn_image_edit;

    private ImageView img_check;

    CircleImageView img_profile;
    Bitmap bitmap;

    SessionManager sessionManager;
    String getId;

    private static String URL_READ = "http://ec2-52-78-18-104.ap-northeast-2.compute.amazonaws.com/ringmabell/read_detail.php";
    private static String URL_UPLOAD = "http://ec2-52-78-18-104.ap-northeast-2.compute.amazonaws.com/ringmabell/upload_image.php";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        sessionManager = new SessionManager(this);

        txt_name = findViewById(R.id.txt_name);
        txt_email = findViewById(R.id.txt_email);
        txt_phone = findViewById(R.id.txt_phone);

        img_profile = findViewById(R.id.img_profile);
        btn_image_edit = findViewById(R.id.btn_image_edit);

        HashMap<String, String> user = sessionManager.getUserDetail();
        getId = user.get(sessionManager.ID);

        btn_logout = findViewById(R.id.btn_logout);

        img_check = findViewById(R.id.img_check);

        btn_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                sessionManager.logout();

            }
        });

        btn_image_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseFile();
            }
        });

        img_check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // UploadPicture 메소드로 이동 (id 와 이미지를 파라미터로 전달
                // -> 전달할 이미지는 getStringImage 메소드를 거쳐서 넣는다.)
                // 데이터 베이스에 업로드 시키고 로그인 정보 shared 에 저장
                UploadPicture(getId, getStringImage(bitmap));



            }
        });

//        resetGlide();
        getUserDetail();

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
                                    String strEmail = object.getString("email").trim();
                                    String strPhone = object.getString("phone").trim();
                                    String strPhoto = object.getString("photo").trim();
                                    Log.e("EditActivity", "strPhoto 는 " + strPhoto);

                                    // id 에 해당하는 name, phone, photo 의 값을 가져와서 화면에 보여준다.
                                    txt_name.setText(strName);
                                    txt_email.setText(strEmail);
                                    txt_phone.setText(strPhone);

                                    // 이미지가 null 값일때, 조건을 가져와야하는데
                                    // strPhoto.isEmpty() / strPhoto.equals("") / strPhoto.equals(null) / strPhoto == null / strPhoto.length == 0 /
                                    // TextUtils.isEmpty(strPhoto) / 까지 왜 다 null 값인데 이 조건을 못가져오지??
                                    // strPhoto.length() == 4 -> 이것만 null 인 조건이 불러와진다. 이상해...
                                    if(strPhoto.length() == 4) {

                                        Log.e("EditActivity", "현재 이미지 없음");

                                    } else {

                                        Uri filePath = Uri.parse(strPhoto);
                                        Log.e("EditActivity.java", "로그인 된 회원의 현재 이미지는 " + filePath);
//                                        Glide.with(EditActivity.this).load(filePath).into(img_profile);

                                        Glide.with(EditActivity.this).load(filePath).apply(
                                                new RequestOptions().diskCacheStrategy(DiskCacheStrategy.NONE
                                                ).skipMemoryCache(true)).into(img_profile);

                                    }

                                }
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();

                            Toast.makeText(EditActivity.this, "데이터를 불러오는데 실패했습니다." + e.toString(), Toast.LENGTH_SHORT).show();

                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Toast.makeText(EditActivity.this, "데이터를 불러오는데 실패했습니다." + error.toString(), Toast.LENGTH_SHORT).show();

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

    // 프로필 사진을 선택했을 때, 갤러리 호출
    private void chooseFile() {

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1);

    }

    // chooseFile 메소드에서 호출한 갤러리에서 사진을 선택하면,
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) {

            // 이미지뷰에 보여준다.
            Uri filePath = data.getData();
            Glide.with(this).load(filePath).into(img_profile);

            try {

                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
//                img_profile.setImageBitmap(bitmap);

            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    // 데이터베이스에 이미지를 업로드하는 메소드이다.
    private void UploadPicture(final  String id, final String photo) {


        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_UPLOAD,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        Log.i(TAG, response);

                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String success = jsonObject.getString("success");

                            // php 에서 파일 저장이 됐으면 응답 값 "1"을 가져온다.
                            if(success.equals("1")) {

                                Toast.makeText(EditActivity.this, "프로필 사진이 변경되었습니다.", Toast.LENGTH_SHORT).show();
                                Log.e("EditActivity.java", "response 는 " + response);
                                Intent intent = new Intent(EditActivity.this, MenuActivity.class);
                                startActivity(intent);

                            } else {

                                Toast.makeText(EditActivity.this, "다시 시도하세요.", Toast.LENGTH_SHORT).show();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(EditActivity.this, "다시 시도하세요." + e.toString(), Toast.LENGTH_SHORT).show();
                        }


                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Toast.makeText(EditActivity.this, "다시 시도하세요." + error.toString(), Toast.LENGTH_SHORT).show();

            }
        })

        {
            // id 값과 photo 값을 php 로 보낸다.
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("id", id);
                params.put("photo", photo);
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);


//        SaveEdit(getId, getStringImage(bitmap));

    }

    private void SaveEdit(String id, String photo) {

        String name = txt_name.getText().toString().trim();
        String email = txt_email.getText().toString().trim();
        String phone = txt_phone.getText().toString().trim();

        sessionManager.createSession(name, email, phone, photo, id);
        Log.e("EditActivity.java", "session 에 저장할 photo 는 " + photo);
    }

    // 갤러리에서 선택한 이미지의 용량을 줄이고 결과 값을 돌려준다.
    public String getStringImage(Bitmap bitmap) {

        // ByteArrayOutputStream 은 바이트 배열을 출력해주는 스트림이다.
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        // bitmap.compress -> 이미지 용량을 압축한다.
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);

        byte[] imageByteArray = byteArrayOutputStream.toByteArray();
        String encodedImage = Base64.encodeToString(imageByteArray, Base64.DEFAULT);

        return encodedImage;
    }

    private void resetGlide() {
        Glide.get(this).clearMemory();
        new AsyncTask<Integer, Void, Void>() {

            @Override
            protected Void doInBackground(Integer... integers) {
                Glide.get(EditActivity.this).clearDiskCache();
                Log.e("EditActivity.java", "Glide 메모리, 디스크 캐쉬 삭제!!");
                return null;
            }
        }.execute();
    }
}
