package co.mobilemakers.openmovieinfo;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * A placeholder fragment containing a simple view.
 */
public class OMInfoFragment extends Fragment {

    final static String LOG_TAG = OMInfoFragment.class.getSimpleName();

    EditText mEditTextMovieTitle;
    TextView mTextViewMovieInfo;

    public OMInfoFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_ominfo, container, false);

        mEditTextMovieTitle = (EditText)rootView.findViewById(R.id.edit_text_movie_title);
        mTextViewMovieInfo = (TextView)rootView.findViewById(R.id.text_view_movie_info);
        Button buttonGetMovieInfo = (Button)rootView.findViewById(R.id.button_get_movie_info);
        buttonGetMovieInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String movieTitle = mEditTextMovieTitle.getText().toString();
                String message = String.format(getString(R.string.getting_movie_info), movieTitle);
                Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();

                new FetchReposTask().execute(movieTitle);
            }
        });

        return rootView;
    }

    private URL constructURLQuery(String movieTitle) throws MalformedURLException {
        final String OMDB_BASE_URL = "www.omdbapi.com";
        final String OMDB_PATH = "";
        final String OMDB_TITLE_PARAMETER = "t";

        Uri.Builder builder = new Uri.Builder();
        builder.scheme("http").authority(OMDB_BASE_URL)
                .appendPath(OMDB_PATH)
                .appendQueryParameter(OMDB_TITLE_PARAMETER, movieTitle);
        Uri uri = builder.build();
        Log.d(LOG_TAG, "Built URI: " + uri.toString());

        return new URL(uri.toString());
    }

    private String readFullResponse(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder stringBuilder = new StringBuilder();
        String response = "";
        String line;

        while ((line = bufferedReader.readLine()) != null) {
            stringBuilder.append(line).append("\n");
        }
        if (stringBuilder.length() > 0) {
            response = stringBuilder.toString();
        }

        return response;
    }

    private String parseResponse(String response) {
        final String RESPONSE = "Response";
        final String ERROR = "Error";

        final String TITLE = "Title";
        final String YEAR = "Year";
        final String GENRE = "Genre";
        final String DIRECTOR = "Director";
        final String ACTORS = "Actors";
        final String PLOT = "Plot";

        String result = getResources().getString(R.string.error_getting_info);

        try {
            JSONObject jsonResponse = new JSONObject(response);
            if (!jsonResponse.getBoolean(RESPONSE)) {
                result = jsonResponse.getString(ERROR);
            }
            else {
                result = TITLE + ": " + jsonResponse.getString(TITLE) + "\n";
                result += YEAR + ": " + jsonResponse.getString(YEAR) + "\n";
                result += GENRE + ": " + jsonResponse.getString(GENRE) + "\n";
                result += DIRECTOR + ": " + jsonResponse.getString(DIRECTOR) + "\n";
                result += ACTORS + ": " + jsonResponse.getString(ACTORS) + "\n\n";
                result += PLOT + ": " + jsonResponse.getString(PLOT);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return result;
/*
        final String REPO_NAME = "name";
        List<String> repos = new ArrayList<>();
        try {
            JSONArray responseJsonArray = new JSONArray(response);
            JSONObject object;
            for (int i = 0; i < responseJsonArray.length(); i++) {
                object = responseJsonArray.getJSONObject(i);
                repos.add(object.getString(REPO_NAME));
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        return TextUtils.join(", ", repos);
*/
    }

    class FetchReposTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String movieTitle;
            String response;
            String movieInfo = "";
            if (params.length > 0) {
                movieTitle = params[0];

                try {
                    URL url = constructURLQuery(movieTitle);

                    HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
                    try {
                        response = readFullResponse(httpConnection.getInputStream());
                        movieInfo = parseResponse(response);
                    } catch (java.io.IOException e) {
                        e.printStackTrace();
                    } finally {
                        httpConnection.disconnect();
                    }
                } catch (java.io.IOException e) {
                    e.printStackTrace();
                }
            } else {
                movieInfo = getResources().getString(R.string.no_movie);
            }

            return movieInfo;
        }

        @Override
        protected void onPostExecute(String response) {
            super.onPostExecute(response);
            mTextViewMovieInfo.setText(response);
        }
    }
}
