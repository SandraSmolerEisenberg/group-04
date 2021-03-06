package se.healthrover.conectivity;

import android.app.Activity;
import android.util.Log;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import se.healthrover.R;
import se.healthrover.entities.Car;
import se.healthrover.entities.ObjectFactory;
import se.healthrover.ui_activity_controller.utilities.UserInterfaceUtilities;

public class OkHttpWebService implements HealthRoverWebService {


    private OkHttpClient client;
    private String responseData;
    private static final String HTTP_STATUS_RESPONSE = "status";
    private UserInterfaceUtilities userInterfaceUtilities;
    private ResponseHandler responseHandler;


    public OkHttpWebService(){
        client = ObjectFactory.getInstance().getOkHttpClient();
        userInterfaceUtilities = ObjectFactory.getInstance().getInterfaceUtilities();
        responseHandler = ObjectFactory.getInstance().getResponseHandler();
    }


    @Override
    public void createHttpRequest(final String url, final Activity activity, final Car car) {
        //Builds a GET request to a given url
        final Request request = new Request.Builder()
                .url(url)
                .build();

        //enqueue the request and run it on a thread, Logging the failures into the log and on success handling the response depending of the response body
        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(@NotNull final Call call, @NotNull final IOException e) {

                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //If the status request fails a message is displayed in the application
                        if (url.contains(HTTP_STATUS_RESPONSE)){
                            responseHandler.handleFailure(activity, car);
                        }
                        Log.i(activity.getString(R.string.log_title_error),activity.getString(R.string.log_connection_fail) + e.getMessage());
                        client.dispatcher().cancelAll();
                    }
                });

            }
            @Override
            public void onResponse(@NotNull final Call call, @NotNull final Response response) {
                if(response.isSuccessful()) {
                    try {
                        responseData = Objects.requireNonNull(response.body()).string();
                        Log.i(activity.getString(R.string.log_success), activity.getString(R.string.log_success) + response.code());
                        responseHandler.handleSuccess(responseData, activity, car);
                    } catch (IOException e) {
                        Log.i(activity.getString(R.string.log_title_error), activity.getString(R.string.log_title_error) + e.getMessage());
                        client.dispatcher().cancelAll();
                    }
                }
            }});


    }
}
