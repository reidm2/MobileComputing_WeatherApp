/* CSCI 4176 Assignment 3
Matthew Reid
B00728822

Some of the material used to complete this assignment was used from the Lab 6 material on JSON.
I have indicated in the code where this was applied.

 */

package com.example.reidm.weatherapp;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

public class MainActivity extends AppCompatActivity {

    private EditText name;
    private TextView currentTempText, maxTempText, minTempText, cloudText, humidityText, city, conditions, description;
    private String cityEntered;
    private Runnable runnable;
    private Button search;
    private ImageView weatherImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().hide();

        name = findViewById(R.id.name);
        search = findViewById(R.id.weatherButton);
        currentTempText = findViewById(R.id.currentTemp);
        maxTempText = findViewById(R.id.maxTemp);
        minTempText = findViewById(R.id.minTemp);
        cloudText = findViewById(R.id.clouds);
        humidityText = findViewById(R.id.humidity);
        city = findViewById(R.id.currentCity);
        conditions = findViewById(R.id.weatherCond);
        description = findViewById(R.id.weatherDesc);
        weatherImage = findViewById(R.id.weatherImg);

        /* Some of the Lab 6 material is used here.
        Fetch the name from the text field, set up a new thread, and call the method to fetch the JSON data
         */
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                cityEntered = name.getText().toString();

                //This was taken from Lab 6: set up a Runnable and use it to perform the getWeatherData method
                runnable = new Runnable() {
                    @Override
                    public void run() {
                        getWeatherData();
                    }
                };

                //Retrieve data on separate thread (also from Lab 6)
                Thread thread = new Thread(null, runnable, "background");
                thread.start();

                //Close the soft keyboard (also from Lab 6)
                InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

                inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        });

    }

    /* This method uses a GET Request to fetch the JSON data from the API, indicated in the url String
    A lot of this logic was taken from the Lab 6 material on HTTP GET requests, and using JSON Objects.
     */
    public void getWeatherData() {

        //Append the city name that was entered, along with the API key generated from my account
        final String url="http://api.openweathermap.org/data/2.5/weather?q=" + cityEntered + "&appid=5afb8f8872a58800fe70aee1851959db&units=metric";

        //Build the request (from Lab 6)
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {

                    //Set up JSON Objects to retrieve the data from the JSON
                    JSONObject main = response.getJSONObject("main");
                    JSONObject sys = response.getJSONObject("sys");
                    JSONObject clouds = response.getJSONObject("clouds");
                    JSONArray weather = response.getJSONArray("weather");
                    JSONObject weatherAtZero = weather.getJSONObject(0);

                    city.setText(String.valueOf(response.getString("name")) + ", " + String.valueOf(sys.getString("country")));
                    currentTempText.setText(String.valueOf(main.getString("temp")) + "°C");
                    minTempText.setText("Min: " + String.valueOf(main.getString("temp_min")) + "°C");
                    maxTempText.setText("Max: " + String.valueOf(main.getString("temp_max")) + "°C");
                    humidityText.setText(String.valueOf(main.getString("humidity")) + "% Humidity");
                    cloudText.setText(String.valueOf(clouds.getString("all")) + "% Clouds");
                    conditions.setText(String.valueOf(weatherAtZero.getString("main")));

                    //Fetch the description of the weather from the JSON, and then ensure that the first characters in each word are capitalized.
                    String desc = String.valueOf(weatherAtZero.getString("description"));
                    desc = desc.substring(0, 1).toUpperCase() + desc.substring(1);
                    for (int i = 0; i < desc.length(); i++)
                    {
                        if (desc.charAt(i) == ' ')
                        {
                            String upperCase = "" + desc.charAt(i+1);
                            upperCase = upperCase.toUpperCase();
                            desc = desc.substring(0, i+1) + upperCase + desc.substring(i+2);
                        }
                    }
                    description.setText(desc);

                    /*Using the "icon" value from the JSON, display the appropriate picture for the current weather conditions.
                    The icons and their associated weather status can be found here: https://openweathermap.org/weather-conditions

                    This also makes use of the Picasso library to quickly and easily display the image.
                    I am using the Picasso library for my project in this course, which is where I first learned how to use it.
                    However, for the benefit of the TAs, here is a Stack Overflow reference to how Picasso can be used (see the first reply to the question):
                    https://stackoverflow.com/questions/43971819/android-how-to-set-an-image-to-an-imageview-from-a-url-programatically
                     */
                    String imgURL = "http://openweathermap.org/img/w/" + String.valueOf(weatherAtZero.getString("icon")) + ".png";
                    Picasso.get().load(imgURL).into(weatherImage);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            //The onErrorResponse is also taken from Lab 6, and indicates an error fetching the JSON data:
        }, new Response.ErrorListener()  {
            @Override
            public void onErrorResponse(VolleyError e) {
                e.printStackTrace();

                Toast.makeText(getApplicationContext(), "Error retrieving data", Toast.LENGTH_SHORT).show();

            }
        }
        );

        //Ddd the request to queue (from Lab 6)
        RequestQueueSingleton.getInstance(getApplicationContext()).addToRequestQueue(request);

    }
}