package ulquiomaru.weatherapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

public class TodayFragment extends Fragment {

    View view;
    ImageView imgWeather;
    TextView tvWeather;
    TextView tvTemp;
    TextView tvTempRange;
    TextView tvLastUpdated;
    TextView tvWeatherDesc;
    TextView tvLocation;
    TextView tvFeelsLike;
    GPSHelper gpsHelper;

    final String apiKey = "8da4cd59d6c44edabf001220182012";
    final String baseUrl = "https://api.worldweatheronline.com/premium/v1/weather.ashx?key=%s&q=%f,%f&num_of_days=%d&tp=24&format=json&includelocation=yes&extra=localObsTime";
    // args: api + lat + lon + num_of_days

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_today, container, false);

        imgWeather = view.findViewById(R.id.imgWeather);
        tvWeather = view.findViewById(R.id.tvWeather);
        tvTemp = view.findViewById(R.id.tvTemp);
        tvWeather = view.findViewById(R.id.tvWeather);
        tvTempRange = view.findViewById(R.id.tvTempRange);
        tvLastUpdated = view.findViewById(R.id.tvLastUpdated);
        tvWeatherDesc = view.findViewById(R.id.tvWeatherDesc);
        tvLocation = view.findViewById(R.id.tvLocation);
        tvFeelsLike = view.findViewById(R.id.tvFeelsLike);

        gpsHelper = new GPSHelper(requireActivity());

        if (checkNetworkConnection() && gpsHelper.isGPSenabled()) {
            gpsHelper.getMyLocation();
            new HTTPAsyncTask().execute(String.format(Locale.getDefault(), baseUrl, apiKey, gpsHelper.getLatitude(), gpsHelper.getLongitude(), 1));
        }
        else {
            Toast.makeText(requireActivity(), "GPS or INTERNET not enabled.!", Toast.LENGTH_SHORT).show();
        }

        return view;
    }

    private boolean checkNetworkConnection() {
        ConnectivityManager connMgr = (ConnectivityManager) requireActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    private class HTTPAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            try {
                return HttpGet(urls[0]);
            } catch (IOException e) {
                return "Unable to retrieve web page. URL may be invalid." + urls[0];
            }
        }

        @Override
        protected void onPostExecute(String result) {
            try { if (isAdded()) JSONParser(result); }
            catch (JSONException e) { e.printStackTrace(); }
        }
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        private DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            Bitmap weatherIcon = null;
            try {
                InputStream in = new java.net.URL(urls[0]).openStream();
                weatherIcon = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return weatherIcon;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }

    private static String HttpGet(String myUrl) throws IOException {
        String result;

        URL url = new URL(myUrl);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.connect();
        InputStream inputStream = conn.getInputStream();

        if (inputStream != null) {
            result = convertInputStreamToString(inputStream);
        }
        else
            result = "Did not work!";

        return result;
    }

    private static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader= new BufferedReader(new InputStreamReader(inputStream));
        String line;
        StringBuilder sb = new StringBuilder();

        while ((line = bufferedReader.readLine()) != null)
            sb.append(line);

        inputStream.close();

        return sb.toString();
    }

    private void JSONParser (String input) throws JSONException {
        String separator = System.getProperty("line.separator");

        JSONObject root = new JSONObject(input).getJSONObject("data");
        JSONObject nearest_area =  root.getJSONArray("nearest_area").getJSONObject(0);
        JSONObject current_condition =  root.getJSONArray("current_condition").getJSONObject(0);
        JSONObject weather =  root.getJSONArray("weather").getJSONObject(0);

        String areaName = nearest_area.getJSONArray("areaName").getJSONObject(0).getString("value");
        String city = nearest_area.getJSONArray("region").getJSONObject(0).getString("value");

        String localObsDateTime = current_condition.getString("localObsDateTime");
        String temp_C = current_condition.getString("temp_C");
        String temp_F = current_condition.getString("temp_F");
        String FeelsLikeC = current_condition.getString("FeelsLikeC");
        String FeelsLikeF = current_condition.getString("FeelsLikeF");
        String weatherCode = current_condition.getString("weatherCode");
        String weatherDesc = current_condition.getJSONArray("weatherDesc").getJSONObject(0).getString("value");
        String weatherIconUrl = current_condition.getJSONArray("weatherIconUrl").getJSONObject(0).getString("value");

        String windspeedMiles = current_condition.getString("windspeedMiles");
        String windspeedKmph = current_condition.getString("windspeedKmph");
        String winddirDegree = current_condition.getString("winddirDegree");

        String maxtempC = weather.getString("maxtempC");
        String maxtempF = weather.getString("maxtempF");
        String mintempC = weather.getString("mintempC");
        String mintempF = weather.getString("mintempF");

        new DownloadImageTask(imgWeather).execute(weatherIconUrl);

        SharedPreferences sharedPref = requireActivity().getPreferences(Context.MODE_PRIVATE);
        int degree = sharedPref.getInt(getString(R.string.key_degree), 0);
        int speed = sharedPref.getInt(getString(R.string.key_speed), 0);
        String temp = null;
        String temp_min = null;
        String temp_max = null;
        String temp_feels_like = null;
        if (degree == 0) {
            String celsius = getString(R.string.celsius);
            temp = temp_C + celsius;
            temp_min = mintempC + celsius;
            temp_max = maxtempC + celsius;
            temp_feels_like = FeelsLikeC + celsius;
        }
        else if (degree == 1) {
            String fahrenheit = getString(R.string.fahrenheit);
            temp = temp_F + fahrenheit;
            temp_min = mintempF + fahrenheit;
            temp_max = maxtempF + fahrenheit;
            temp_feels_like = FeelsLikeF + fahrenheit;
        }

        tvWeather.setText("Today");
        tvLastUpdated.setText("Last Updated" + separator + localObsDateTime.split(" ", 2)[1]);
        tvLocation.setText(areaName + ", " + city);
        tvTemp.setText("Temperature" + separator + temp);
        tvTempRange.setText("H: " + temp_max + separator + "L: " + temp_min);
        tvWeatherDesc.setText(weatherDesc);
        tvFeelsLike.setText("FeelsLike" + separator + temp_feels_like);
    }
}
