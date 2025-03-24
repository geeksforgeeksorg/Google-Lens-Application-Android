package org.geeksforgeeks.demo

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabel
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import org.geeksforgeeks.demo.databinding.ActivityMainBinding
import org.json.JSONException

class MainActivity : AppCompatActivity() {
    private var imageBitmap: Bitmap? = null
    private lateinit var adapter: Adapter
    private var dataModalArrayList: ArrayList<DataModel> = ArrayList()
    private lateinit var title: String
    private lateinit var link: String
    private lateinit var displayedLink: String
    private lateinit var snippet: String
    private var takeImageLauncher: ActivityResultLauncher<Intent>? = null

    private val binding:ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        dataModalArrayList = ArrayList()

        adapter = Adapter(dataModalArrayList, this@MainActivity)

        binding.snap.setOnClickListener {
            dispatchTakePictureIntent()
        }

        binding.getSearchResults.setOnClickListener {
            results
        }

        takeImageLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { o: ActivityResult ->
            if (o.resultCode == RESULT_OK) {
                val data = o.data
                val extras = data!!.extras
                imageBitmap = extras!!["data"] as Bitmap?
                Glide.with(this).load(imageBitmap).into(binding.image)
            }
        }
    }

    private val results: Unit
        get() {
            dataModalArrayList.clear()

            if (imageBitmap == null) {
                Toast.makeText(this, "No image found. Please capture an image first.", Toast.LENGTH_SHORT).show()
                return
            }

            val image = InputImage.fromBitmap(imageBitmap!!, 0)
            val labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)

            labeler.process(image)
                .addOnSuccessListener { labels: List<ImageLabel> ->
                    if (labels.isNotEmpty()) {
                        val searchQuery = labels[0].text
                        Toast.makeText(this@MainActivity, searchQuery, Toast.LENGTH_SHORT).show()
                        searchData(searchQuery)
                    } else {
                        Toast.makeText(this@MainActivity, "No labels detected in the image.", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this@MainActivity, "Failed to detect image.", Toast.LENGTH_SHORT).show()
                }
        }

    @SuppressLint("NotifyDataSetChanged")
    private fun searchData(searchQuery: String) {
        val apiKey = "170fb0db05f01c335e943db5dd65d21be48c5a841cbcfb6a623181db6a36e7b8"
        val url =
            "https://serpapi.com/search.json?q=" + searchQuery.trim { it <= ' ' } + "&location=Delhi,India&hl=en&gl=us&google_domain=google.com&api_key=" + apiKey
        println(searchQuery.trim { it <= ' ' })

        val queue = Volley.newRequestQueue(this@MainActivity)
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                try {
                    val organicResultsArray = response.getJSONArray("organic_results")
                    for (i in 0 until organicResultsArray.length()) {
                        val organicObj = organicResultsArray.getJSONObject(i)
                        if (organicObj.has("title")) {
                            title = organicObj.getString("title")
                        }
                        if (organicObj.has("link")) {
                            link = organicObj.getString("link")
                        }
                        if (organicObj.has("displayed_link")) {
                            displayedLink = organicObj.getString("displayed_link")
                        }
                        if (organicObj.has("snippet")) {
                            snippet = organicObj.getString("snippet")
                        }
                        dataModalArrayList.add(DataModel(title, link, displayedLink, snippet))
                    }

                    adapter.notifyDataSetChanged()
                    binding.recyclerView.adapter = adapter
                } catch (e: JSONException) {
                    println(e)
                }
            }, { error ->
                println(error.toString())
                Toast.makeText(
                    this@MainActivity,
                    "No Result found for the search query..",
                    Toast.LENGTH_SHORT
                ).show()
            })
        queue.add(jsonObjectRequest)
    }

    // method to capture image.
    @SuppressLint("QueryPermissionsNeeded")
    private fun dispatchTakePictureIntent() {
        // inside this method we are calling an implicit intent to capture an image.
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            takeImageLauncher!!.launch(takePictureIntent)
        }
    }
}