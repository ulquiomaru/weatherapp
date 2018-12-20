package ulquiomaru.weatherapp;

import android.content.Context;
import android.util.SparseArray;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

class Util {

    private static SparseArray<String> weatherIcons;

    static void initWeatherIcons(Context context) {
        String[] stringArray = context.getResources().getStringArray(R.array.weatherData);
        weatherIcons = new SparseArray<String>(stringArray.length);
        for (String entry : stringArray) {
            String[] splitResult = entry.split("\\|", 2);
            weatherIcons.put(Integer.valueOf(splitResult[0]), splitResult[1]);
        }
    }

    static String weatherIconRes(String weatherCode) {
        return "ic_" + weatherIcons.get(Integer.valueOf(weatherCode));
    }

    static String HttpGet(String myUrl) throws IOException {
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

}
