package se.healthrover.ui_activity_controller;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import io.github.controlwear.virtual.joystick.android.JoystickView;
import se.healthrover.R;
import se.healthrover.car_service.CarManagement;
import se.healthrover.entities.Car;
import se.healthrover.entities.HealthRoverJoystick;
import se.healthrover.entities.ObjectFactory;
import se.healthrover.ui_activity_controller.car_selection.CarSelect;
import se.healthrover.ui_activity_controller.voice_control.SpeechRecognition;


public class ManualControl extends AppCompatActivity {

    private TextView header;
    private String carName;
    private TextView angleText;
    private TextView strengthText;
    private CarManagement carManagement;
    private int speed;
    private int turningAngle;
    private Button voiceControl;
    private Car healthRoverCar;
    private HealthRoverJoystick healthRoverJoystick;
    private static final int JOYSTICK_CENTER = 50;
    private static final String CONTROL_TYPE = "manual";
    // Can be used to reduce number of request send (1 of 4 blocks of code)
    // private int[] lastSpeedAndAngleValues;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(ObjectFactory.getInstance().getExceptionHandler(this, healthRoverCar));
        initialize();
    }
    //On restart refresh the content
    @Override
    protected void onRestart() {
        super.onRestart();
        Thread.setDefaultUncaughtExceptionHandler(ObjectFactory.getInstance().getExceptionHandler(this, healthRoverCar));
        initialize();
    }

    public ManualControl(){
        carManagement =ObjectFactory.getInstance().getCarManagement();
    }


    // Using the method to load and initialize the content
    private void initialize(){
        setContentView(R.layout.manual_control);
        healthRoverJoystick = ObjectFactory.getInstance().getHealthRoverJoystick(this);
        header = findViewById(R.id.manualControlHeaderText);
        voiceControl = findViewById(R.id.voiceControl);
        healthRoverCar = (Car) getIntent().getSerializableExtra(getString(R.string.car_name));
        carName = healthRoverCar.getName();
        header.setText(carName);
        angleText = findViewById(R.id.textView_angle);
        strengthText = findViewById(R.id.textView_strength);
        // Can be used to reduce number of request (2 of 4 blocks of code)
        // lastSpeedAndAngleValues = new int[]{0, 0};

        final JoystickView joystickController = findViewById(R.id.joystick);

        joystickController.setOnMoveListener(new HealthRoverJoystick.OnMoveListener() {
            @SuppressLint({"SetTextI18n", "DefaultLocale"})
            @Override
            public void onMove(int angle, int strength) {
                //Calculate the angle and speeed
                speed = healthRoverJoystick.convertSpeed(strength, angle);
                turningAngle = healthRoverJoystick.convertAngle(angle);
                /* Can be used to reduce the number of request send (3 of 4 blocks of code)
                If the speed and angle has changed, needed to reduce the number of request send to the server

                if(lastSpeedAndAngleValues[0] != speed || lastSpeedAndAngleValues[1] != turningAngle){
                    carManagement.moveCar(healthRoverCar, speed, turningAngle, ManualControl.this);
                    lastSpeedAndAngleValues[0] = speed;
                    lastSpeedAndAngleValues[1] = turningAngle;
                }
                */
                //if the joystick button is in the middle(x=50 and y=50) reset the speed
                if (joystickController.getNormalizedX() == JOYSTICK_CENTER && joystickController.getNormalizedY() == JOYSTICK_CENTER){
                    speed = 0;
                    turningAngle = 0;
                }
                //If reduce request is used this statement needs to be moved inside the if statement above (4 of 4 blocks of code)
                carManagement.moveCar(healthRoverCar, speed, turningAngle, CONTROL_TYPE, ManualControl.this);

                //Update the UI speed and angle
                angleText.setText(turningAngle + getString(R.string.degree_symbol));
                strengthText.setText(speed + getString(R.string.percentage_sign));
            }
        });

        //Change activity to voice control
        voiceControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                carManagement.stopCar(healthRoverCar, CONTROL_TYPE, ManualControl.this);
                Intent intent = ObjectFactory.getInstance().getIntent(ManualControl.this, SpeechRecognition.class);
                intent.putExtra(getString(R.string.car_name), healthRoverCar);
                startActivity(intent);
            }
        });


    }

    // Using back button to return to Car select page
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        carManagement.stopCar(healthRoverCar, CONTROL_TYPE, ManualControl.this);
        Intent intent = ObjectFactory.getInstance().getIntent(ManualControl.this, CarSelect.class);
        carManagement.getCars().clear();
        startActivity(intent);
    }
}
