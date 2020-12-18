package com.example.gascalculator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    //reg variables
    private EditText etTripDistance, etFuelEconomy, etFuelPrice, etNumberPeople, etTripName;
    private TextView txtFuelUsed, txtCost, txtCostPerPerson;

    private ConstraintLayout main;
    String distance, economy, price, people, name;

    //variables for external storage save
    private static final int REQUEST_CODE_WRITE_EXTERNAL_STORAGE_PERMISSION=1;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //initializing variables
        main=findViewById(R.id.mainView);
        etTripDistance=findViewById(R.id.editTripDistance);
        etFuelEconomy=findViewById(R.id.editFuelEconomy);
        etFuelPrice=findViewById(R.id.editFuelPrice);
        etNumberPeople=findViewById(R.id.editNumberPeople);
        etTripName=findViewById(R.id.editTripName);

        txtFuelUsed=findViewById(R.id.textView);
        txtCost=findViewById(R.id.textView2);
        txtCostPerPerson=findViewById(R.id.textView8);
        //check if permissions are granted
        if(isExternalStorageWriteable()){
            int writeExPerm = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if(writeExPerm != PackageManager.PERMISSION_GRANTED)
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_CODE_WRITE_EXTERNAL_STORAGE_PERMISSION);
            if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
                Log.i("External:", getResources().getString(R.string.mounted));
            else Log.i("External: ", getResources().getString(R.string.notMounted));
        }else Snackbar.make(main,getResources().getString(R.string.exNonGrant),Snackbar.LENGTH_LONG).show();

    }

//checking if SD card is mounted and if possible to write on it
    private boolean isExternalStorageWriteable() {
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())){
            Log.i("State: ", "Writeable");
            return true;
        }else return false;
    }

    //method onRequestPermissinResult needs to be overwitten-> this method is invoked after user clicks button
    //in permission grant popup dialog
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode==REQUEST_CODE_WRITE_EXTERNAL_STORAGE_PERMISSION){
            int grantResultLength=grantResults.length;
            if (grantResultLength>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                Snackbar.make(main,getResources().getString(R.string.exGrant),Snackbar.LENGTH_LONG).show();
            }else{
                Snackbar.make(main,getResources().getString(R.string.exNonGrant),Snackbar.LENGTH_LONG).show();

            }
        }
    }

    //btn clear //using string array & for-cycle to set all input fields
    public void onClear(View view) {
        String[] fields={"etTripDistance","etFuelEconomy","etFuelPrice","etNumberPeople","etTripName"};
        for(String s : fields){
            int id=getResources().getIdentifier(s,"id",getPackageName());
            EditText field=findViewById(id);
            field.setText("");
        }
        etTripDistance.requestFocus();
        txtFuelUsed.setText(getResources().getString(R.string.fuelused));
        txtCost.setText(getResources().getString(R.string.costblank));
        txtCostPerPerson.setText(getResources().getString(R.string.costPerPers));
    }

 //method for checking the user has input all the needed values
    private boolean checkValidation(){
        distance=etTripDistance.getText().toString().trim();
        economy=etFuelEconomy.getText().toString().trim();
        price=etFuelPrice.getText().toString().trim();
        people=etNumberPeople.getText().toString().trim();

        if(TextUtils.isEmpty(distance)){
            etTripDistance.requestFocus();
            etTripDistance.setError("This field cannot be empty!");
            return false;
        }else if (TextUtils.isEmpty(economy)){
            etFuelEconomy.requestFocus();
            etFuelEconomy.setError("This field cannot be empty!");
            return false;
        }else if(TextUtils.isEmpty(price)){
            etFuelPrice.requestFocus();
            etFuelPrice.setError("This field cannot be empty!");
            return false;
        }else if(TextUtils.isEmpty(people)){
            etNumberPeople.requestFocus();
            etNumberPeople.setError("This field cannot be empty!");
            return false;
        }else return true;
 }

    public void onCalculate(View view) {
        if(checkValidation()){
            //get fuel amount used
            double fuelUsed=Double.parseDouble(etTripDistance.getText().toString())/100*Double.parseDouble(etFuelEconomy.getText().toString());
            //calculate total trip cost
            double cost=fuelUsed*Double.parseDouble(etFuelPrice.getText().toString());
            //calculate per person coast
            double perPerson=cost/Double.parseDouble(etNumberPeople.getText().toString());
            //set text fields*/
            txtFuelUsed.setText(String.format(getString(R.string.fuelused),fuelUsed));
            txtCost.setText(String.format(getString(R.string.costPerPers),cost));
            txtCostPerPerson.setText(String.format(getString(R.string.costPerPers),perPerson));
            //txtCostPerPerson.setText("Fuel cost is: " +cost+"per person is: " +perPerson);
        }
    }

    //helper method for name field check
    private boolean checkName(){
        name=etTripName.getText().toString().trim();
        if(TextUtils.isEmpty(name)){
            etTripName.requestFocus();
            etTripName.setError(getString(R.string.inputError));
            return false;
        }else return true;
    }

    //btn save, will call forth an alert where user can choose whether internal or external save
    public void onSave(View view) {
        //name field must not be empty
        if(checkName()){
            if(checkValidation()){
                onCalculate(view);
                new AlertDialog.Builder(this)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle(getString(R.string.saveFileTitle))
                        .setMessage(getString(R.string.saveFileMessage))
                        .setPositiveButton(getString(R.string.intr), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                internalSave();

                            }
                        })
                        .setNegativeButton(getString(R.string.extr), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                externalSave();

                            }
                        }).show();
            }

        }

    }

    //internal save-doesn`t need any permissions
    private void internalSave() {
        String FILE_NAME="GAS_INTERNAL";//variable name for file that will be saved-> GAS_INTERNAL.txt

        //variables to hold relevant info from text views
        name=etTripName.getText().toString();
        String total=txtCost.getText().toString().trim();
        String perPerson=txtCostPerPerson.getText().toString().trim();
        String fuel=txtFuelUsed.getText().toString().trim();

        //variables,that will hold data in one line,separated by coma
        String data=name + "," + fuel + "," + total + "," + perPerson;

        //try catch block, incase error occurs
        try {
            //creating a new file to save, we aren`t using a specific directory rather the default one
            File file=new File(getFilesDir(),FILE_NAME);
            if(!file.exists()){
                Snackbar.make(main,getResources().getString(R.string.saveError),Snackbar.LENGTH_LONG).show();
            }
            //there are a number of ways to write text to file, here we use filewriter
            FileWriter fileWriter=new FileWriter(file, true);
            //adding data & a new line, otherwise new text will be rigth after the old one
            fileWriter.append(data).append("\n");
            //after we`re done adding data, flushing filewriter of data and closing it
            fileWriter.flush();
            fileWriter.close();
            //FIX ERROR:snackbar displays message if successful, add to variables to string end so it will display relevant info in snackbar message
            Snackbar.make(main,getResources().getString(R.string.saveSuccess,getFilesDir(),FILE_NAME),Snackbar.LENGTH_LONG).show();
        }catch (IOException ex){ ex.printStackTrace();}
    }

    //external save
    private void externalSave() {
        String FILE_NAME="GAS_EXTERNAL";//variable name for file that will be saved-> GAS_INTERNAL.txt
        name=etTripName.getText().toString();
        String total=txtCost.getText().toString().trim();
        String perPerson=txtCostPerPerson.getText().toString().trim();
        String fuel=txtFuelUsed.getText().toString().trim();
        String data=name + "," + fuel + "," + total + "," + perPerson;
        try {
            File file=new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),FILE_NAME);
            if(!file.exists()){
                Snackbar.make(main,getResources().getString(R.string.saveError),Snackbar.LENGTH_LONG).show();
            }
            FileWriter fileWriter=new FileWriter(file, true);
            fileWriter.append(data).append("\n");
            fileWriter.flush();
            fileWriter.close();
            Snackbar.make(main,getResources().getString(R.string.saveSuccess,getFilesDir(),FILE_NAME),Snackbar.LENGTH_LONG).show();
        }catch (IOException ex){ ex.printStackTrace();}
    }


}