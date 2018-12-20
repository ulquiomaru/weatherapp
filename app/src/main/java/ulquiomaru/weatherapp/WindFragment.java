package ulquiomaru.weatherapp;

import android.content.Context;
import android.content.SharedPreferences;
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
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Locale;

public class WindFragment extends Fragment {

    View view;
    TextView tvWind;
    TextView tvChill;
    TextView tvSpeed;
    TextView tvGust;
    TextView tvDirection;
    GPSHelper gpsHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_wind, container, false);

        tvWind = view.findViewById(R.id.tvWind);
        tvChill = view.findViewById(R.id.tvChill);
        tvSpeed = view.findViewById(R.id.tvSpeed);
        tvGust = view.findViewById(R.id.tvGust);
        tvDirection = view.findViewById(R.id.tvDirection);

        gpsHelper = new GPSHelper(requireActivity());

        if (checkNetworkConnection() && gpsHelper.isGPSenabled()) {
            gpsHelper.getMyLocation();
            new HTTPAsyncTask().execute(String.format(Locale.getDefault(), Util.baseUrl, Util.apiKey, gpsHelper.getLatitude(), gpsHelper.getLongitude(), 1));
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
                return Util.HttpGet(urls[0]);
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

    private void JSONParser (String input) throws JSONException {
        String separator = System.getProperty("line.separator");

        JSONObject root = new JSONObject(input).getJSONObject("data");
        JSONObject hourly = root.getJSONArray("weather").getJSONObject(0).getJSONArray("hourly").getJSONObject(0);

        String WindChillC = hourly.getString("WindChillC");
        String WindChillF = hourly.getString("WindChillF");
        String windspeedMiles = hourly.getString("windspeedMiles");
        String windspeedKmph = hourly.getString("windspeedKmph");
        String WindGustMiles = hourly.getString("WindGustMiles");
        String WindGustKmph = hourly.getString("WindGustKmph");
        String winddir16Point = hourly.getString("winddir16Point");

        SharedPreferences sharedPref = requireActivity().getPreferences(Context.MODE_PRIVATE);
        int degree = sharedPref.getInt(getString(R.string.key_degree), 0);
        int speed = sharedPref.getInt(getString(R.string.key_speed), 0);

        if (degree == 0)
            tvChill.setText("Chill:" + separator + WindChillC + getString(R.string.celsius));
        else if (degree == 1)
            tvChill.setText("Chill:" + separator + WindChillF + getString(R.string.fahrenheit));

        if (speed == 0) {
            tvSpeed.setText("Speed:" + separator + windspeedKmph + " Km per hour");
            tvGust.setText("Gust:" + separator + WindGustKmph + " Km per hour");
        }
        else if (speed == 1) {
            tvSpeed.setText("Speed:" + separator + windspeedMiles + " Miles per hour");
            tvGust.setText("Gust:" + separator + WindGustMiles + " Miles per hour");
        }
        tvWind.setText("Wind Statistics");
        tvDirection.setText("Direction:" + separator + winddir16Point);
    }
}
