package com.devniel.photo_uploader_softlayer.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.devniel.photo_uploader_softlayer.R;
import com.devniel.photo_uploader_softlayer.listeners.HttpResponseListener;
import com.devniel.photo_uploader_softlayer.utils.HttpFileUploader;

import org.apache.http.message.BasicNameValuePair;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class PhotoFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "PhotoFragment";

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String SOFTLAYER_PHOTO_NAME = null;
    private String SOFTLAYER_CONTAINER_URL = null;
    private String SOFTLAYER_AUTH_TOKEN = null;

    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    private ImageView photo;

    public static PhotoFragment newInstance(String param1, String param2) {
        PhotoFragment fragment = new PhotoFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public PhotoFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        SOFTLAYER_PHOTO_NAME = getResources().getString(R.string.softlayer_photo_name);
        SOFTLAYER_CONTAINER_URL = getResources().getString(R.string.softlayer_container_url);
        SOFTLAYER_AUTH_TOKEN = getResources().getString(R.string.softlayer_auth_token);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_photo, container, false);

        Button btn_take_photo = (Button) view.findViewById(R.id.btn_take_photo);
        btn_take_photo.setOnClickListener(this);

        Button btn_upload_photo = (Button) view.findViewById(R.id.btn_upload_photo);
        btn_upload_photo.setOnClickListener(this);

        photo = (ImageView) view.findViewById(R.id.photo);

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_take_photo:
                takePhoto();
                break;
            case R.id.btn_upload_photo:
                uploadPhoto();
                break;
            default:
                break;
        }
    }

    /**
     * Interface
     */

    public interface OnFragmentInteractionListener {
        public void onPhoto(Uri uri);
    }

    /**
     * Button On Click Listener
     */

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_TAKE_PHOTO = 1;


    public void takePhoto(){

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(this.getActivity().getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Log.e(TAG, ex.getMessage());
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            getPhoto();
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    String mCurrentPhotoPath;

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void getPhoto() {

        Log.d(TAG, "PHOTO : " + photo.toString());

        // Get the dimensions of the View
        int targetW = photo.getWidth();
        int targetH = photo.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);

        Log.d(TAG, "Current photo path : " + mCurrentPhotoPath);

        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = 1;
        if(targetW > 0 && targetH > 0)
            scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        photo.setImageBitmap(bitmap);

        File f = new File(Environment.getExternalStorageDirectory()
                + File.separator + SOFTLAYER_PHOTO_NAME + ".jpg");

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 40, bytes);

        try {
            f.createNewFile();
            FileOutputStream fo = new FileOutputStream(f);
            fo.write(bytes.toByteArray());
            fo.close();
            mCurrentPhotoPath = f.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void uploadPhoto() {

        HttpFileUploader request = new HttpFileUploader();
        request.setUrl(SOFTLAYER_CONTAINER_URL + SOFTLAYER_PHOTO_NAME + ".jpg");
        request.addHeader(new BasicNameValuePair("X-Auth-Token", SOFTLAYER_AUTH_TOKEN));
        request.setFilePath(mCurrentPhotoPath);
        request.setFileType("image/jpeg");
        request.setResponseListener(new HttpResponseListener() {
            @Override
            public void onResponse(String result, Integer status) {
                Log.d(TAG, "RESPONSE : " + status);
            }

            @Override
            public void onError(Exception error) {

            }
        });

        request.execute();


    }
}
