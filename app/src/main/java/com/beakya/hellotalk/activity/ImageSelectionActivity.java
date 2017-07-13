package com.beakya.hellotalk.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.beakya.hellotalk.R;
import com.beakya.hellotalk.objs.User;
import com.beakya.hellotalk.retrofit.PhotoProfileService;
import com.beakya.hellotalk.utils.Utils;
import com.linchaolong.android.imagepicker.ImagePicker;
import com.linchaolong.android.imagepicker.cropper.CropImage;
import com.linchaolong.android.imagepicker.cropper.CropImageView;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

import static com.beakya.hellotalk.retrofit.RetrofitCreator.retrofit;
import static com.beakya.hellotalk.utils.Utils.getToken;


/**
 * Created by cheolho on 2017. 4. 11..
 */

public class ImageSelectionActivity extends AppCompatActivity {
    private static final String TAG = ImageSelectionActivity.class.getSimpleName();
    private ImagePicker imagePicker;
    private ImageView profileView;
    private Button imageChooserButton;
    private Bitmap bitmap = null;
    private TextView alertView;
    private ProgressBar progressBar;
    private Menu menu;
    private Toolbar mToolbar;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_selection);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        imagePicker = new ImagePicker();

        profileView = (ImageView) findViewById(R.id.default_profile_image_view);
        imageChooserButton = (Button) findViewById(R.id.profile_image_change_button);
        progressBar = (ProgressBar) findViewById(R.id.show_image_transfer);
        alertView = (TextView) findViewById(R.id.profile_skip_text_view);

        imagePicker.setTitle(getString(R.string.image_chooser_title));
        imagePicker.setCropImage(true);

        imageChooserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startChooser();
            }
        });
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        imagePicker.onActivityResult(this, requestCode, resultCode, data);
    }

    @Override public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                                     @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        imagePicker.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }
    void startChooser() {
        imagePicker.startChooser(this, new ImagePicker.Callback() {
            @Override
            public void onPickImage(Uri imageUri) {

            }
            @Override
            public void onCropImage(Uri imageUri) {
                try {

                    Bitmap bm = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                    ImageSelectionActivity.this.bitmap = bm;
                    profileView.setImageBitmap(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void cropConfig(CropImage.ActivityBuilder builder) {
                builder.setMultiTouchEnabled(false)
                        .setGuidelines(CropImageView.Guidelines.OFF)
                        .setCropShape(CropImageView.CropShape.RECTANGLE)
                        .setRequestedSize(480, 480)
                        .setAspectRatio(1, 1);
            }
            @Override
            public void onPermissionDenied(int requestCode, String[] permissions, int[] grantResults) {
                super.onPermissionDenied(requestCode, permissions, grantResults);
            }
        });
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        /* Use AppCompatActivity's method getMenuInflater to get a handle on the menu inflater */
        MenuInflater inflater = getMenuInflater();
        /* Use the inflater's inflate method to inflate our menu layout to this menu */
        inflater.inflate(R.menu.image_setting_menu, menu);
        /* Return true so that the menu is displayed in the Toolbar */
        this.menu = menu;
        return true;
    }

    /**
     * Callback invoked when a menu item was selected from this Activity's menu.
     *
     * @param item The menu item that was selected by the user
     *
     * @return true if you handle the menu click here, false otherwise
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if ( id == R.id.image_action_next ) {
            if( bitmap != null ) {
                new ImageSendAsyncTask().execute(bitmap);
            } else {
                startActivity( new Intent( this, MainActivity.class));
                finish();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class ImageSendAsyncTask extends AsyncTask<Bitmap, Integer, Response<ResponseBody>> {
        @Override
        protected void onPreExecute() {
            showProgressBar();
            Log.d(TAG, "onPreExecute: " +  bitmap.toString());
            super.onPreExecute();
        }

        @Override
        protected Response<ResponseBody> doInBackground(Bitmap... params) {
            Bitmap mBitmap = params[0];
            String fileName = getString(R.string.setting_friends_profile_img_name);
            String extension = getString(R.string.setting_profile_img_extension);
            String directory = getString(R.string.setting_friends_img_directory);
            Context context = ImageSelectionActivity.this;
            User myInfo = Utils.getMyInfo(context);
            Utils.saveToInternalStorage(ImageSelectionActivity.this, mBitmap, fileName, extension, Arrays.asList(new String[]{ directory, myInfo.getId()}));

            File directoryFile = new File ( context.getFilesDir(), directory);
            File test = new File ( directoryFile, myInfo.getId());
            File originalImageFile = new File(test, fileName + '.' + extension);
            Uri mUri = Uri.fromFile(originalImageFile);
            Log.d(TAG, "doInBackground: " + mUri.toString());
            String mimeType = Utils.getMimeType(ImageSelectionActivity.this, mUri);
            RequestBody filePart = RequestBody.create(
                    MediaType.parse(mimeType),
                    originalImageFile
            );
            MultipartBody.Part file = MultipartBody.Part.createFormData(
                    "image",
                    fileName,
                    filePart
            );

            String token = getToken(ImageSelectionActivity.this);
            PhotoProfileService service = retrofit.create(PhotoProfileService.class);
            Call<ResponseBody> call = service.upload(token, file);
            Response<ResponseBody> req = null;
            try {
                req = call.execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return req;
        }

        @Override
        protected void onPostExecute(Response<ResponseBody> req) {
            hideProgressBar();
            super.onPostExecute(req);
            if( req.isSuccessful() ) {
                SharedPreferences tokenStorage = getSharedPreferences(getString(R.string.my_info), MODE_PRIVATE);
                SharedPreferences.Editor editor = tokenStorage.edit();
                editor.putBoolean(getString(R.string.user_img_boolean), true);
                editor.commit();
                Intent intent = new Intent(ImageSelectionActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }
    }
    private void showProgressBar() {
        profileView.setVisibility(View.INVISIBLE);
        imageChooserButton.setVisibility(View.INVISIBLE);
        alertView.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        menu.findItem(R.id.image_action_next).setVisible(false);
    }
    private void hideProgressBar() {
        profileView.setVisibility(View.VISIBLE);
        imageChooserButton.setVisibility(View.VISIBLE);
        alertView.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.INVISIBLE);
        menu.findItem(R.id.image_action_next).setVisible(true);

    }

}
