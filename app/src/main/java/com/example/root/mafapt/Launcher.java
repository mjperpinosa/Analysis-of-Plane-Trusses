package com.example.root.mafapt;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import java.util.ArrayList;

public class Launcher extends AppCompatActivity {

    private Button btnSubmitInputs;

    public static ArrayList<Integer[]> jointDataList;
    public static ArrayList<Integer[]> jointLoadDataList;
    public static ArrayList<Integer[]> supportDataList;
    public static ArrayList<Integer> materialPropertyDataList;
    public static ArrayList<Integer> crossSectionalPropertyDataList;
    public static ArrayList<Integer[]> memberDataList;
    StringBuilder out;
    StringBuilder sizes;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);

        btnSubmitInputs = (Button) findViewById(R.id.btnSubmitInputs);
        btnSubmitInputs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                jointDataList = new ArrayList<>();
                jointLoadDataList = new ArrayList<>();
                supportDataList = new ArrayList<>();
                materialPropertyDataList = new ArrayList<>();
                crossSectionalPropertyDataList = new ArrayList<>();
                memberDataList = new ArrayList<>();

                out = new StringBuilder();
                sizes = new StringBuilder();
                try {
                    EditText etJointData = (EditText) findViewById(R.id.etJD);
                    EditText etJointLoadData = (EditText) findViewById(R.id.etJLD);
                    EditText etSupportData = (EditText) findViewById(R.id.etSD);
                    EditText etMaterialPropertyData = (EditText) findViewById(R.id.etMPD);
                    EditText etCrossSectionalPropertyData = (EditText) findViewById(R.id.etCSPD);
                    EditText etMemberData = (EditText) findViewById(R.id.etMD);

                    String jointData = etJointData.getText().toString();
                    String jointLoadData = etJointLoadData.getText().toString();
                    String supportData = etSupportData.getText().toString();
                    String materialPropertyData = etMaterialPropertyData.getText().toString();
                    String crossSectionalPropertyData = etCrossSectionalPropertyData.getText().toString();
                    String memberData = etMemberData.getText().toString();

                    if(jointData.trim().equals("") ||
                            jointLoadData.trim().equals("") ||
                            supportData.trim().equals("") ||
                            materialPropertyData.trim().equals("") ||
                            crossSectionalPropertyData.trim().equals("") ||
                            memberData.trim().equals("")) {
                        Toast.makeText(getApplicationContext(), "Please do not leave an empty data.", Toast.LENGTH_LONG).show();
                        return;
                    }

                    String[] tempStringArray0;
                    String[] tempStringArray1;
                    Integer[] tempList;

                    try {

                        // Joint Data

                        tempStringArray0 = jointData.split("\n");
                        tempList = new Integer[2];
                        //Toast.makeText(getApplicationContext(), "Length of tempStringArray0 = " + tempStringArray0.length, Toast.LENGTH_LONG).show();

                        if(tempStringArray0.length == 1) {
                            tempStringArray1 = tempStringArray0[0].split(" ");
                            out.append(tempStringArray1[0] + " " + tempStringArray1[1]);
                            sizes.append("1 ");
                        } else {
                            for(int i = 0; i < tempStringArray0.length; i++) {
                                //Toast.makeText(getApplicationContext(), "loop = " + i, Toast.LENGTH_LONG).show();
                                tempStringArray1 = tempStringArray0[i].split(" ");
                                if(tempStringArray1.length != 2) {
                                    Toast.makeText(getApplicationContext(), "Check Joint Data. Please input valid data and follow format.", Toast.LENGTH_LONG).show();
                                    return;
                                }
                                tempList[0] = Integer.parseInt(tempStringArray1[0]);
                                tempList[1] = Integer.parseInt(tempStringArray1[1]);

                                jointDataList.add(tempList);

                                out.append(tempStringArray1[0] + " " + tempStringArray1[1]);
                                if(i < tempStringArray0.length-1) out.append(":");
                            }
                            sizes.append(jointDataList.size() + " ");
                        }
                        out.append("\n");

                        // Joint Load Data

                        tempStringArray0 = jointLoadData.split("\n");
                        tempList = new Integer[3];

                        if(tempStringArray0.length == 1) {
                            tempStringArray1 = tempStringArray0[0].split(" ");
                            out.append(tempStringArray1[0] + " " + tempStringArray1[1] + " " + tempStringArray1[2]);
                            sizes.append("1 ");
                        } else {

                            for (int i = 0; i < tempStringArray0.length; i++) {
                                tempStringArray1 = tempStringArray0[i].split(" ");
                                if (tempStringArray1.length != 3) {
                                    Toast.makeText(getApplicationContext(), "Check Joint Load Data. Please input valid data and follow format.", Toast.LENGTH_LONG).show();
                                    return;
                                }
                                tempList[0] = Integer.parseInt(tempStringArray1[0]);
                                tempList[1] = Integer.parseInt(tempStringArray1[1]);
                                tempList[2] = Integer.parseInt(tempStringArray1[2]);

                                jointLoadDataList.add(tempList);

                                out.append(tempStringArray1[0] + " " + tempStringArray1[1] + " " + tempStringArray1[2]);
                                if (i < tempStringArray0.length - 1) out.append(":");
                            }
                            sizes.append(jointLoadDataList.size() + " ");
                        }
                        out.append("\n");


                        // Support Data

                        tempStringArray0 = supportData.split("\n");
                        tempList = new Integer[3];

                        if(tempStringArray0.length == 1) {
                            tempStringArray1 = tempStringArray0[0].split(" ");
                            out.append(tempStringArray1[0] + " " + tempStringArray1[1] + " " + tempStringArray1[2]);
                            sizes.append("1 ");
                        } else {
                            for (int i = 0; i < tempStringArray0.length; i++) {
                                tempStringArray1 = tempStringArray0[i].split(" ");
                                if (tempStringArray1.length != 3) {
                                    Toast.makeText(getApplicationContext(), "Check Support Data. Please input valid data and follow format.", Toast.LENGTH_LONG).show();
                                    return;
                                }

                                tempList[0] = Integer.parseInt(tempStringArray1[0]);
                                tempList[1] = Integer.parseInt(tempStringArray1[1]);
                                tempList[2] = Integer.parseInt(tempStringArray1[2]);

                                supportDataList.add(tempList);

                                out.append(tempStringArray1[0] + " " + tempStringArray1[1] + " " + tempStringArray1[2]);
                                if (i < tempStringArray0.length - 1) out.append(":");
                            }
                            sizes.append(supportDataList.size() + " ");
                        }
                        out.append("\n");

                        // Material Property Data

                        tempStringArray0 = materialPropertyData.split("\n");
                        if(tempStringArray0.length == 1) {
                            out.append(tempStringArray0[0]);
                            sizes.append("1 ");
                        } else {
                            for (int i = 0; i < tempStringArray0.length; i++) {
                                materialPropertyDataList.add(Integer.parseInt(tempStringArray0[i]));
                                out.append(tempStringArray0[i]);
                                if (i < tempStringArray0.length - 1) out.append(" ");
                            }
                            sizes.append(materialPropertyDataList.size() + " ");
                        }
                        out.append("\n");


                        // Cross-Sectional Property Data

                        tempStringArray0 = crossSectionalPropertyData.split("\n");

                        if(tempStringArray0.length == 1) {
                            out.append(tempStringArray0[0]);
                            sizes.append("1 ");
                        } else {
                            for (int i = 0; i < tempStringArray0.length; i++) {
                                crossSectionalPropertyDataList.add(Integer.parseInt(tempStringArray0[i]));
                                out.append(tempStringArray0[i]);
                                if (i < tempStringArray0.length - 1) out.append(" ");
                            }
                            sizes.append(crossSectionalPropertyDataList.size() + " ");
                        }
                        out.append("\n");


                        // Member Data

                        tempStringArray0 = memberData.split("\n");
                        tempList = new Integer[4];
                        if(tempStringArray0.length == 1) {
                            tempStringArray1 = tempStringArray0[0].split(" ");
                            out.append(tempStringArray1[0] + " " + tempStringArray1[1] + " " + tempStringArray1[2] + " " + tempStringArray1[3]);
                            sizes.append("1 ");
                        } else {
                            for (int i = 0; i < tempStringArray0.length; i++) {
                                tempStringArray1 = tempStringArray0[i].split(" ");

                                if (tempStringArray1.length != 4) {
                                    Toast.makeText(getApplicationContext(), "Check Member Data. Please input valid data and follow format.", Toast.LENGTH_LONG).show();
                                    return;
                                }

                                tempList[0] = Integer.parseInt(tempStringArray1[0]);
                                tempList[1] = Integer.parseInt(tempStringArray1[1]);
                                tempList[2] = Integer.parseInt(tempStringArray1[2]);
                                tempList[3] = Integer.parseInt(tempStringArray1[3]);

                                memberDataList.add(tempList);

                                out.append(tempStringArray1[0] + " " + tempStringArray1[1] + " " + tempStringArray1[2] + " " + tempStringArray1[3]);
                                if (i < tempStringArray0.length - 1) out.append(":");
                            }
                        }
                        sizes.append(memberDataList.size());

                        Intent summariseInputIntent = new Intent(getApplicationContext(), InputsSummary.class);
                        Bundle extras = new Bundle();
                        extras.putString("data", out.toString());
                        extras.putString("sizes", sizes.toString());
                        summariseInputIntent.putExtras(extras);
                        startActivity(summariseInputIntent);
                    }
                    catch(NumberFormatException e) {
                        Toast.makeText(getApplicationContext(), "Please make sure that the data you entered are valid.", Toast.LENGTH_LONG).show();
                    }
                } catch(IndexOutOfBoundsException   e) {
                    Toast.makeText(getApplicationContext(), "Please make sure that the data you entered are valid.", Toast.LENGTH_LONG).show();
                    //Toast.makeText(getApplicationContext(), e.getMessage() + " = " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                }

            }
        });
    }
}
