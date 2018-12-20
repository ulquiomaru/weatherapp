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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Locale;

public class WeekFragment extends Fragment {

    View view;
    TextView tvWeek;
    ArrayList<TextView> dayList;
    GPSHelper gpsHelper;

    final String apiKey = "8da4cd59d6c44edabf001220182012";
    final String baseUrl = "https://api.worldweatheronline.com/premium/v1/weather.ashx?key=%s&q=%f,%f&num_of_days=%d&tp=24&format=json&includelocation=yes&extra=localObsTime";
    // args: api + lat + lon + num_of_days

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_week, container, false);

        tvWeek = view.findViewById(R.id.tvWeek);
        dayList = new ArrayList<>();
        dayList.add((TextView) view.findViewById(R.id.tvDay1));
        dayList.add((TextView) view.findViewById(R.id.tvDay2));
        dayList.add((TextView) view.findViewById(R.id.tvDay3));
        dayList.add((TextView) view.findViewById(R.id.tvDay4));
        dayList.add((TextView) view.findViewById(R.id.tvDay5));
        dayList.add((TextView) view.findViewById(R.id.tvDay6));
        dayList.add((TextView) view.findViewById(R.id.tvDay7));

        gpsHelper = new GPSHelper(requireActivity());

        if (checkNetworkConnection() && gpsHelper.isGPSenabled()) {
            gpsHelper.getMyLocation();
            new HTTPAsyncTask().execute(String.format(Locale.getDefault(), baseUrl, apiKey, gpsHelper.getLatitude(), gpsHelper.getLongitude(), 7));
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
        String tab = "\t";

        JSONObject root = new JSONObject(input).getJSONObject("data");

        SharedPreferences sharedPref = requireActivity().getPreferences(Context.MODE_PRIVATE);
        int degree = sharedPref.getInt(getString(R.string.key_degree), 0);

        for (int i = 0; i < 7; i++) {
            JSONObject weather =  root.getJSONArray("weather").getJSONObject(i);
            String date = weather.getString("date");
            String maxtempC = weather.getString("maxtempC");
            String maxtempF = weather.getString("maxtempF");
            String mintempC = weather.getString("mintempC");
            String mintempF = weather.getString("mintempF");
            JSONObject hourly = weather.getJSONArray("hourly").getJSONObject(0);
            String tempC = hourly.getString("tempC");
            String tempF = hourly.getString("tempF");
            String weatherCode = hourly.getString("weatherCode");
            String weatherDesc = hourly.getJSONArray("weatherDesc").getJSONObject(0).getString("value");
            String weatherIconUrl = hourly.getJSONArray("weatherIconUrl").getJSONObject(0).getString("value");
            String FeelsLikeC = hourly.getString("FeelsLikeC");
            String FeelsLikeF = hourly.getString("FeelsLikeF");

            String temp_feels_like = null;
            String temp_min = null;
            String temp_max = null;
            if (degree == 0) {
                String celsius = getString(R.string.celsius);
                temp_min = mintempC + celsius;
                temp_max = maxtempC + celsius;
                temp_feels_like = FeelsLikeC + celsius;
            }
            else if (degree == 1) {
                String fahrenheit = getString(R.string.fahrenheit);
                temp_min = mintempF + fahrenheit;
                temp_max = maxtempF + fahrenheit;
                temp_feels_like = FeelsLikeF + fahrenheit;
            }
            String day = date.split("-")[2] + "/" + date.split("-")[1];

            String sb = day + separator +
                    weatherDesc + separator +
                    "H: " + temp_max + tab +
                    "L: " + temp_min + tab +
                    "FeelsLike: " + temp_feels_like;
            dayList.get(i).setText(sb);
        }

//        new DownloadImageTask(imgWeather).execute(weatherIconUrl);

        tvWeek.setText("Weekly Forecast");
    }
}
