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
import java.util.ArrayList;
import java.util.Locale;

public class WeekFragment extends Fragment {

    View view;
    TextView tvWeek;
    ArrayList<TextView> dayList;
    GPSHelper gpsHelper;

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
            new HTTPAsyncTask().execute(String.format(Locale.getDefault(), Util.baseUrl, Util.apiKey, gpsHelper.getLatitude(), gpsHelper.getLongitude(), 7));
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
            String weatherCode = hourly.getString("weatherCode");
            String weatherDesc = hourly.getJSONArray("weatherDesc").getJSONObject(0).getString("value");
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
                    "L: " + temp_min;
            dayList.get(i).setText(sb);
            dayList.get(i).setCompoundDrawablesWithIntrinsicBounds(getResources().getIdentifier(Util.weatherIconRes(weatherCode), "drawable", requireActivity().getPackageName()), 0, 0, 0);
        }

        tvWeek.setText("Weekly Forecast");
    }
}
