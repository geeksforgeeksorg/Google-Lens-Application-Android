package com.example.googlelensapplication;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    // variables for our image view, image bitmap,
    // buttons, recycler view, adapter and array list.
    private ImageView img;
    private Button snap, searchResultsBtn;
    private Bitmap imageBitmap;
    private RecyclerView resultRV;
    private SearchResultsRVAdapter searchResultsRVAdapter;
    private ArrayList<dataModal> dataModalArrayList;
    private String title, link, displayed_link, snippet;
    ActivityResultLauncher<Intent> takeImageLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // initializing all our variables for views
        img = findViewById(R.id.image);
        snap = findViewById(R.id.snapbtn);
        searchResultsBtn = findViewById(R.id.idBtnSearchResuts);
        resultRV = findViewById(R.id.idRVSearchResults);

        // initializing our array list
        dataModalArrayList = new ArrayList<>();

        // initializing our adapter class.
        searchResultsRVAdapter = new SearchResultsRVAdapter(dataModalArrayList, MainActivity.this);

        // adding on click listener for our snap button.
        snap.setOnClickListener(v -> {
            // calling a method to capture an image.
            dispatchTakePictureIntent();
        });

        // adding on click listener for our button to search data.
        searchResultsBtn.setOnClickListener(v -> {
            // calling a method to get search results.
            getResults();
        });

        // Variable to initiate camera and take a snap
        takeImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                o -> {
                    if (o.getResultCode() == Activity.RESULT_OK) {
                        Intent data = o.getData();
                        Bundle extras = data.getExtras();
                        imageBitmap = (Bitmap) extras.get("data");
                        img.setImageBitmap(imageBitmap);
                    }
                }
        );
    }

    private void getResults() {
        // We will be using the Google's ML library to get a label of the image
        // The label will be any describing word about the image you took.
        dataModalArrayList.clear();

        InputImage image = InputImage.fromBitmap(imageBitmap, 0);

        ImageLabeler labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS);

        labeler.process(image)
                .addOnSuccessListener(labels -> {
                    // Task completed successfully
                    String searchQuery = labels.get(0).getText();
                    searchData(searchQuery);
                })
                .addOnFailureListener(e -> {
                    // Task failed with an exception
                    Toast.makeText(MainActivity.this, "Fail to detect image..", Toast.LENGTH_SHORT).show();
                });
    }

    private void searchData(String searchQuery) {
        // Creating a URL to call the API
        String apiKey = "YOUR_API_KEY";
        String url = "https://serpapi.com/search.json?q=" + searchQuery.trim() + "&location=Delhi,India&hl=en&gl=us&google_domain=google.com&api_key=" + apiKey;
        System.out.println(searchQuery.trim());
        // creating a new variable for our request queue
        RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    // on below line we are extracting data from our json.
                    JSONArray organicResultsArray = response.getJSONArray("organic_results");
                    for (int i = 0; i < organicResultsArray.length(); i++) {
                        JSONObject organicObj = organicResultsArray.getJSONObject(i);
                        if (organicObj.has("title")) {
                            title = organicObj.getString("title");
                        }
                        if (organicObj.has("link")) {
                            link = organicObj.getString("link");
                        }
                        if (organicObj.has("displayed_link")) {
                            displayed_link = organicObj.getString("displayed_link");
                        }
                        if (organicObj.has("snippet")) {
                            snippet = organicObj.getString("snippet");
                        }
                        // on below line we are adding data to our array list.
                        dataModalArrayList.add(new dataModal(title, link, displayed_link, snippet));
                    }
                    // notifying our adapter class
                    // on data change in array list.
                    //searchResultsRVAdapter.notifyDataSetChanged();

                    // layout manager for our recycler view.
                    LinearLayoutManager manager = new LinearLayoutManager(MainActivity.this, LinearLayoutManager.HORIZONTAL, false);

                    // on below line we are setting layout manager
                    // and adapter to our recycler view.
                    resultRV.setLayoutManager(manager);
                    resultRV.setAdapter(searchResultsRVAdapter);

                } catch (JSONException e) {
                    System.out.println(e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // displaying error message.
                System.out.println(error.toString());
                Toast.makeText(MainActivity.this, "No Result found for the search query..", Toast.LENGTH_SHORT).show();
            }
        });
        // adding json object request to our queue.
        queue.add(jsonObjectRequest);
    }

    // method to capture image.
    private void dispatchTakePictureIntent() {
        // inside this method we are calling an implicit intent to capture an image.
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            takeImageLauncher.launch(takePictureIntent);
        }
    }
}