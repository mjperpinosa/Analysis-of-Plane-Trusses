package com.example.root.mafapt;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;

public class InputsSummary extends AppCompatActivity {

    private TextView tvNumberOfJoints;
    private TextView tvNumberOfMembers;
    private TextView tvNumberOfMaterialProperty;
    private TextView tvNumberOfCrossSectionalProperty;

    private Button btnAnalyse;

    private TableLayout tlJointCoordinates;
    private TableLayout tlSupports;
    private TableLayout tlMaterialProperties;
    private TableLayout tlCrossSectionProperties;
    private TableLayout tlMembers;
    private TableLayout tlJointLoads;

    public static ArrayList<Double[]> jointCoordinates;
    public static ArrayList<Double[]> jointLoads;
    public static ArrayList<Integer[]> supports;
    public static ArrayList<Double> materialProperties;
    public static ArrayList<Double> crossSectionalProperties;
    public static ArrayList<Integer[]> members;

    private LinearLayout llContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inputs_summary);

        jointCoordinates = new ArrayList<>();
        jointLoads = new ArrayList<>();
        supports = new ArrayList<>();
        materialProperties = new ArrayList<>();
        crossSectionalProperties = new ArrayList<>();
        members = new ArrayList<>();

        //Toast.makeText(getApplicationContext(), "Came on InputSummary.java", Toast.LENGTH_LONG).show();

        Intent l = getIntent();
        Bundle extras = l.getExtras();

        final String rawSizes = extras.getString("sizes");
        final String rawData = extras.getString("data");

        String[] sizes = rawSizes.split(" ");
        String[] data = rawData.split("\n");

        btnAnalyse = (Button) findViewById(R.id.btnStartAnalysing);
        btnAnalyse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            // Start Computation here...
            try {
                int NCJT = 2;
                int NDOF = 0;

                // Determining Number of Degrees of Freedom

                int NR = 0;
                for (int i = 0; i < supports.size(); i++) {

                    for (int i1 = 1; i1 < NCJT + 1; i1++) {
                        if (supports.get(i)[i1] == 1) {
                            NR = NR + 1;
                        }
                    }
                }
                NDOF = NCJT * jointCoordinates.size() - NR;

                Toast.makeText(getApplicationContext(), "jointCoords = " + jointCoordinates.size() + " supports = " + supports.size() + " loads = " + jointLoads.size() + " material = " + materialProperties.size() + " cs = " + crossSectionalProperties.size() + " members = " + members.size(), Toast.LENGTH_LONG).show();

                // Generating Structure Coordinate Numbers

                int[] NSC = new int[NCJT * jointCoordinates.size()];
                int J = 0, K = NDOF, iCount;
                for (int i = 1; i <= jointCoordinates.size(); i++) {
                    iCount = 0;

                    for (int i1 = 1; i1 <= supports.size(); i1++) {
                        if (supports.get(i1-1)[0] == i) { // if jointNo. == current joint i
                            iCount = 1;

                            for (int i2 = 1; i2 <= NCJT; i2++) {
                                int i3 = (i-1) * NCJT + i2;

                                if (supports.get(i1-1)[i2+1-1] == 1) {
                                    K = K + 1;
                                    NSC[i3-1] = K;
                                    //Toast.makeText(InputsSummary.this, "NSC at position " + i3 + " = " + K, Toast.LENGTH_LONG).show();
                                } else {
                                    J = J + 1;
                                    NSC[i3-1] = J;
                                }
                            }
                        }
                    }

                    if (iCount == 0) {
                        for (int i2 = 1; i2 <= NCJT; i2++) {
                            int i3 = (i-1) * NCJT + i2;

                            J = J + 1;
                            NSC[i3-1] = J;
                            //Toast.makeText(InputsSummary.this, "NSC at position " + i3 + " = " + J, Toast.LENGTH_LONG).show();
                        }
                    }
                }

                Toast.makeText(getApplicationContext(), "NSC Okay = " + NSC.toString(), Toast.LENGTH_LONG ).show();

                // Generating Structure Stiffness Matrix for Plane Trusses

                double[][] S = new double[NDOF][NDOF];
                double[][] GK = new double[2 * NCJT][2 * NCJT];

                for (int i = 0; i < NDOF; i++) {
                    for (int j = 0; j < NDOF; j++) {
                        S[i][j] = 0;
                    }
                }

                //Toast.makeText(getApplicationContext(), "NDOF = " + NDOF, Toast.LENGTH_LONG).show();
                //Toast.makeText(getApplicationContext(), "member0Size = " + members.get(0).length, Toast.LENGTH_LONG).show();
                double E, A, XB, YB, XE, YE, BL, CX, CY;
                int I, JB, JE;
                for (int IM = 0; IM < members.size(); IM++) {
                    JB = members.get(IM)[0];
                    JE = members.get(IM)[1];
                    I = members.get(IM)[2];
                    E = materialProperties.get(I-1);
                    I = members.get(IM)[3];
                    A = crossSectionalProperties.get(I-1);

                    XB = jointCoordinates.get(JB-1)[0];
                    YB = jointCoordinates.get(JB-1)[1];

                    XE = jointCoordinates.get(JE-1)[0];
                    YE = jointCoordinates.get(JE-1)[1];

                    BL = Math.sqrt(Math.pow(XE - XB, 2) + Math.pow(YE - YB, 2));

                    CX = (XE - XB) / BL;
                    CY = (YE - YB) / BL;
                    //Toast.makeText(getApplicationContext(), "Inside loop " + IM + " CY = " + CY, Toast.LENGTH_LONG).show();

                   // Call subroutine MSTIFFG
                    MSTIFFG(E, A, BL, CX, CY, GK);
//error

                    // Call subroutine STORES
                    STORES(JB, JE, NCJT, NDOF, NSC, GK, S);
                }

               /* for(int i = 0; i < NSC.length; i++) {
                    Toast.makeText(getApplicationContext(), "NSC["+i+1+"] = " + NSC[i], Toast.LENGTH_LONG).show();
                }
*/


                // Forming Joint Load Vector

                double[] P = new double[NDOF];

                for (int i = 0; i < NDOF; i++) {
                    P[i] = 0;
                }

                for (int i = 1; i <= jointLoads.size(); i++) {
                    int i1 = (int) Math.floor(jointLoads.get(i-1)[0]);
                    int i2 = (i1 - 1) * NCJT;

                    for (int j = 1; j <= NCJT; j++) {
                        i2 = i2 + 1;
                        int n = NSC[i2-1];

                        if (n <= NDOF) {
                            P[n-1] = P[n-1] + jointLoads.get(i-1)[j]; // Check here. Might be OutOfBounds
                        }
                    }
                }

                // Calculation for Joint Displacements by Gauss-Jordan Method

                double Z, Z1;
                for (int i = 1; i <= NDOF; i++) {
                    Z1 = S[i-1][i-1];

                    for (int j = 1; j <= NDOF; j++) {
                        S[i-1][j-1] = S[i-1][j-1] / Z1;
                    }

                    P[i-1] = P[i-1] / Z1;

                    for (int k = 1; k <= NDOF; k++) {
                        if (k != i) {
                            Z = S[k-1][i-1];

                            for (int m = i; m <= NDOF; m++) {
                                S[k-1][m-1] = S[k-1][m-1] - S[i-1][m-1] * Z;
                            }

                            P[k-1] = P[k-1] - P[i-1] * Z; // Display P on output
                        }
                    }
                }

                /*for(int i = 0; i < P.length; i++) {
                    Toast.makeText(getApplicationContext(), "P["+(i+1)+"] = " + P[i], Toast.LENGTH_LONG).show();
                }

                for(int i = 0; i < NDOF; i++) {
                    for(int j = 0; j < NDOF; j++) {
                        Toast.makeText(getApplicationContext(), "S["+i+"]["+j+"] = " + S[i][j], Toast.LENGTH_LONG).show();
                    }
                }*/


                // Display the Contents of P

                // Determination of Member Forces and Support Reaction for Plane Trusses

                double[][] BK = new double[2 * NCJT][2 * NCJT],
                            T = new double[2 * NCJT][2 * NCJT];
                double[] V = new double[2 * NCJT],
                         U = new double[2 * NCJT],
                         Q = new double[2 * NCJT],
                         F = new double[2 * NCJT],
                          R = new double[NR];

                for (int i = 0; i < NR; i++) {
                    R[i] = 0;
                }

                for (int im = 0; im < members.size(); im++) {
                    JB = members.get(im)[0];
                    JE = members.get(im)[1];
                    I = members.get(im)[2];
                    E = materialProperties.get(I-1);
                    I = members.get(im)[3];
                    A = crossSectionalProperties.get(I-1);

                    XB = jointCoordinates.get(JB-1)[0];
                    YB = jointCoordinates.get(JB-1)[1];

                    XE = jointCoordinates.get(JE-1)[0];
                    YE = jointCoordinates.get(JE-1)[1];

                    BL = Math.sqrt(Math.pow(XE - XB, 2) + Math.pow(YE - YB, 2));

                    CX = (XE - XB) / BL;
                    CY = (YE - YB) / BL;

                    // Call Subroutine MDISPG
                    // Determining Member Global Displacement Vector
                    MDISPG(JB, JE, NCJT, NDOF, NSC, P, V);

                    // Call Subroutine MTRANS
                    // Determining Member Transformation Matrix for Plane Trusses
                    MTRANS(CX, CY, NCJT, T);

                    // Call Subroutine MDISPL
                    MDISPL(NCJT, V, T, U);

                    // Call Subroutine MSTIFFL
                    MSTIFFL(E, A, BL, NCJT, BK);

                    // Call Subroutine MFORCEL
                    MFORCEL(NCJT, BK, U, Q);

                    // Call Subroutine MFORCEG
                    MFORCEG(NCJT, T, Q, F);
                    // Print Member Forces F


                    // Call Subroutine STORER
                    STORER(JB, JE, NCJT, NDOF, NSC, F, R);

                    // Display Reactions R

                }

                for(int i = 0; i < P.length; i++) {
                    Toast.makeText(getApplicationContext(), "P["+i+"] = " + P[i], Toast.LENGTH_SHORT).show();
                }
                for(int i = 0; i < F.length; i++) {
                    Toast.makeText(getApplicationContext(), "F["+i+"] = " + F[i], Toast.LENGTH_SHORT).show();
                }
                for(int i = 0; i < R.length; i++) {
                    Toast.makeText(getApplicationContext(), "R["+i+"] = " + R[i], Toast.LENGTH_SHORT).show();
                }

                // Display P, R

                StringBuilder out = new StringBuilder();

                for (int i = 0; i < P.length; i++) {
                    out.append(P[i]);
                    if (i < P.length - 1) {
                        out.append(" ");
                    }
                }

                out.append("\n");


                for (int i = 0; i < F.length; i++) {
                    out.append(F[i]);
                    if (i < F.length - 1) {
                        out.append(" ");
                    }
                }

                out.append("\n");

                for (int i = 0; i < R.length; i++) {
                    out.append(R[i]);
                    if (i < R.length - 1) {
                        out.append(" ");
                    }
                }

                if (!out.toString().equals(" ")) {
                    Intent outputIntent = new Intent(getApplicationContext(), Output.class);
                    outputIntent.putExtra("sizes", rawSizes);
                    outputIntent.putExtra("data", rawData);
                    outputIntent.putExtra("out", out.toString());
                    startActivity(outputIntent);
                    finish();
                }

            } catch(IndexOutOfBoundsException e) {
                Toast.makeText(getApplicationContext(), "IndexOutOfBounds: " + e.getMessage(), Toast.LENGTH_LONG).show();
            } catch(Exception e) {
                Toast.makeText(getApplicationContext(), "On Exception: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
            Toast.makeText(getApplicationContext(), "Still working on it...", Toast.LENGTH_LONG).show();
            }
        });

        tvNumberOfJoints = (TextView) findViewById(R.id.tvNumberOfJoints);
        tvNumberOfJoints.setText(sizes[0]);
        tvNumberOfMaterialProperty = (TextView) findViewById(R.id.tvNumberOfMaterialProperty);
        tvNumberOfMaterialProperty.setText(sizes[3]);

        tvNumberOfCrossSectionalProperty = (TextView) findViewById(R.id.tvNumberOfCrossSectionalProperty);
        tvNumberOfCrossSectionalProperty.setText(sizes[4]);

        tvNumberOfMembers = (TextView) findViewById(R.id.tvNumberOfMembers);
        tvNumberOfMembers.setText(sizes[5]);

        llContainer = (LinearLayout) findViewById(R.id.llContainer);
        llContainer.setOrientation(LinearLayout.VERTICAL);

        tlJointCoordinates = new TableLayout(this);
        tlSupports = new TableLayout(this);
        tlMaterialProperties = new TableLayout(this);
        tlCrossSectionProperties = new TableLayout(this);
        tlMembers = new TableLayout(this);
        tlJointLoads = new TableLayout(this);

        tlJointCoordinates.setLayoutParams(new TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT
        ));
        tlSupports.setLayoutParams(new TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT
        ));
        tlMaterialProperties.setLayoutParams(new TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT
        ));
        tlCrossSectionProperties.setLayoutParams(new TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT
        ));
        tlMembers.setLayoutParams(new TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT
        ));
        tlJointLoads.setLayoutParams(new TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT
        ));

        GradientDrawable gd = new GradientDrawable();
        gd.setStroke(1, Color.WHITE);

        /*tlJointCoordinates.setBackground(gd);
        tlJointLoads.setBackground(gd);
        tlSupports.setBackground(gd);
        tlMaterialProperties.setBackground(gd);
        tlCrossSectionProperties.setBackground(gd);
        tlMembers.setBackground(gd);*/

        //Toast.makeText(this, sizes[0] + ", " + sizes[1] + ", " + sizes[2] + ", " + sizes[3] + ", " + sizes[4] + ", " + sizes[5], Toast.LENGTH_SHORT).show();

        String[] tempStringArray0;
        String[] tempStringArray1;
        Double[] tempDoubleArray;
        Integer[] tempIntArray;

        try {

            // Joint Coordinates

            if(Integer.parseInt(sizes[0]) > 1) {
                tempStringArray0 = data[0].split(":"); // Joint Coordinates
                for(int i = 0; i < tempStringArray0.length; i++) {
                    tempStringArray1 = tempStringArray0[i].split(" ");
                    tempDoubleArray = new Double[2];
                    tempDoubleArray[0] = Double.parseDouble(tempStringArray1[0]);
                    tempDoubleArray[1] = Double.parseDouble(tempStringArray1[1]);

                    jointCoordinates.add(tempDoubleArray);
                }
            } else if(Integer.parseInt(sizes[0]) == 1) {
                tempStringArray0 = data[0].split(" ");
                tempDoubleArray = new Double[2];
                tempDoubleArray[0] = Double.parseDouble(tempStringArray0[0]);
                tempDoubleArray[1] = Double.parseDouble(tempStringArray0[1]);

                jointCoordinates.add(tempDoubleArray);
            }

            // Joint Loads

            if(Integer.parseInt(sizes[1]) > 1) {
                tempStringArray0 = data[1].split(":"); // Joint Loads
                for(int i = 0; i < tempStringArray0.length; i++) {
                    tempStringArray1 = tempStringArray0[i].split(" ");
                    tempDoubleArray = new Double[3];
                    tempDoubleArray[0] = Double.parseDouble(tempStringArray1[0]);
                    tempDoubleArray[1] = Double.parseDouble(tempStringArray1[1]);
                    tempDoubleArray[2] = Double.parseDouble(tempStringArray1[2]);

                    jointLoads.add(tempDoubleArray);
                }
            } else if(Integer.parseInt(sizes[1]) == 1) {
                tempStringArray0 = data[1].split(" "); // Joint Loads
                tempDoubleArray = new Double[3];
                tempDoubleArray[0] = Double.parseDouble(tempStringArray0[0]);
                tempDoubleArray[1] = Double.parseDouble(tempStringArray0[1]);
                tempDoubleArray[2] = Double.parseDouble(tempStringArray0[2]);

                jointLoads.add(tempDoubleArray);
            }

            // Supports

            if(Integer.parseInt(sizes[2]) > 1) {
                tempStringArray0 = data[2].split(":"); // Supports
                for(int i = 0; i < tempStringArray0.length; i++) {
                    tempStringArray1 = tempStringArray0[i].split(" ");
                    tempIntArray = new Integer[3];
                    tempIntArray[0] = Integer.parseInt(tempStringArray1[0]);
                    tempIntArray[1] = Integer.parseInt(tempStringArray1[1]);
                    tempIntArray[2] = Integer.parseInt(tempStringArray1[2]);

                    supports.add(tempIntArray);
                }
            } else if(Integer.parseInt(sizes[2]) == 1){
                tempStringArray0 = data[2].split(" ");
                tempIntArray = new Integer[3];
                tempIntArray[0] = Integer.parseInt(tempStringArray0[0]);
                tempIntArray[1] = Integer.parseInt(tempStringArray0[1]);
                tempIntArray[2] = Integer.parseInt(tempStringArray0[2]);

                supports.add(tempIntArray);
            }

            // Material Properties

            if(Integer.parseInt(sizes[3]) > 1) {
                tempStringArray0 = data[3].split(" "); // Materials Properties
                for(int i = 0; i < tempStringArray0.length; i++) {
                    materialProperties.add(Double.parseDouble(tempStringArray0[i]));
                }
            } else {
                materialProperties.add(Double.parseDouble(data[3]));
            }

            // Cross-Sectional Properties

            if(Integer.parseInt(sizes[4]) > 1) {
                tempStringArray0 = data[4].split(" "); // Materials Properties
                for(int i = 0; i < tempStringArray0.length; i++) {
                    crossSectionalProperties.add(Double.parseDouble(tempStringArray0[i]));
                }
            } else {
                crossSectionalProperties.add(Double.parseDouble(data[4]));
            }

            // Members Data

            if(Integer.parseInt(sizes[5]) > 1) {
                tempStringArray0 = data[5].split(":"); // Supports
                for(int i = 0; i < tempStringArray0.length; i++) {
                    tempStringArray1 = tempStringArray0[i].split(" ");
                    tempIntArray = new Integer[4];
                    tempIntArray[0] = Integer.parseInt(tempStringArray1[0]);
                    tempIntArray[1] = Integer.parseInt(tempStringArray1[1]);
                    tempIntArray[2] = Integer.parseInt(tempStringArray1[2]);
                    tempIntArray[3] = Integer.parseInt(tempStringArray1[3]);

                    members.add(tempIntArray);
                }
            } else if(Integer.parseInt(sizes[2]) == 1){
                tempStringArray0 = data[5].split(" ");
                tempIntArray = new Integer[4];
                tempIntArray[0] = Integer.parseInt(tempStringArray0[0]);
                tempIntArray[1] = Integer.parseInt(tempStringArray0[1]);
                tempIntArray[2] = Integer.parseInt(tempStringArray0[2]);
                tempIntArray[3] = Integer.parseInt(tempStringArray0[3]);

                members.add(tempIntArray);
            }
        } catch(Exception e) {
            Toast.makeText(getApplicationContext(), "An exception: " + e.getMessage() + " == " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            //Toast.makeText(this, sizes[0] + ", " + sizes[1] + ", " + sizes[2] + ", " + sizes[3] + ", " + sizes[4] + ", " + sizes[5], Toast.LENGTH_SHORT).show();
        }

        // Adding data to Table Layout

        TextView tempTV0, tempTV1, tempTV2, tempTV3, tempTV4;

        TableRow.LayoutParams tlCellParams = new TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT
        );

        tlCellParams.setMargins(3, 3, 3, 3);

        LinearLayout.LayoutParams llTitleParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

        TableLayout.LayoutParams tlRowParams = new TableLayout.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT
        );

        // adding joint coordinates to table layout

        tempTV0 = new TextView(this);
        tempTV0.setText("JOINT COORDINATES");
        tempTV0.setLayoutParams(llTitleParams);
        tempTV0.setTypeface(null, Typeface.BOLD);
        tempTV0.setGravity(Gravity.CENTER);
        //tempTV0.setGravity(Gravity.CENTER);
        //tempTV0.setWidth(Typeface.BOLD);
        llContainer.addView(tempTV0);

        TableRow row;

        row = new TableRow(this);
        row.setLayoutParams(tlRowParams);

        tempTV0 = new TextView(this);
        tempTV1 = new TextView(this);
        tempTV2 = new TextView(this);

        tempTV0.setText("Joint No.");
        tempTV1.setText("X Coordinate");
        tempTV2.setText("Y Coordinate");

        tempTV0.setLayoutParams(tlCellParams);
        tempTV1.setLayoutParams(tlCellParams);
        tempTV2.setLayoutParams(tlCellParams);

        tempTV0.setPadding(4, 4, 4, 4);
        tempTV1.setPadding(4, 4, 4, 4);
        tempTV2.setPadding(4, 4, 4, 4);

        tempTV0.setBackground(gd);
        tempTV1.setBackground(gd);
        tempTV2.setBackground(gd);

        row.addView(tempTV0);
        row.addView(tempTV1);
        row.addView(tempTV2);

        tlJointCoordinates.addView(row);

        for(int i = 0 ;i < jointCoordinates.size(); i++) {
            row = new TableRow(this);
            row.setLayoutParams(tlRowParams);

            tempTV0 = new TextView(this);
            tempTV1 = new TextView(this);
            tempTV2 = new TextView(this);

            tempTV0.setText(String.valueOf(i+1));
            tempTV1.setText(String.valueOf(jointCoordinates.get(i)[0]));
            tempTV2.setText(String.valueOf(jointCoordinates.get(i)[1]));

            tempTV0.setLayoutParams(tlCellParams);
            tempTV1.setLayoutParams(tlCellParams);
            tempTV2.setLayoutParams(tlCellParams);

            tempTV0.setPadding(4, 4, 4, 4);
            tempTV1.setPadding(4, 4, 4, 4);
            tempTV2.setPadding(4, 4, 4, 4);

            tempTV0.setBackground(gd);
            tempTV1.setBackground(gd);
            tempTV2.setBackground(gd);

            row.addView(tempTV0);
            row.addView(tempTV1);
            row.addView(tempTV2);


            tlJointCoordinates.addView(row);
        }

        llContainer.addView(tlJointCoordinates);

        // Adding Joint Loads to table layout

        tempTV0 = new TextView(this);
        tempTV0.setText("\nJOINT LOADS");
        tempTV0.setLayoutParams(llTitleParams);
        tempTV0.setTypeface(null, Typeface.BOLD);
        tempTV0.setGravity(Gravity.CENTER);
        llContainer.addView(tempTV0);

        row = new TableRow(this);
        row.setLayoutParams(tlRowParams);

        tempTV0 = new TextView(this);
        tempTV1 = new TextView(this);
        tempTV2 = new TextView(this);

        tempTV0.setText("Joint\nNo.");
        tempTV1.setText("Force in\nX Direction");
        tempTV2.setText("Force in\nY Direction");

        tempTV0.setLayoutParams(tlCellParams);
        tempTV1.setLayoutParams(tlCellParams);
        tempTV2.setLayoutParams(tlCellParams);

        tempTV0.setBackground(gd);
        tempTV1.setBackground(gd);
        tempTV2.setBackground(gd);

        tempTV0.setPadding(4, 4, 4, 4);
        tempTV1.setPadding(4, 4, 4, 4);
        tempTV2.setPadding(4, 4, 4, 4);

        row.addView(tempTV0);
        row.addView(tempTV1);
        row.addView(tempTV2);

        tlJointLoads.addView(row);

        for(int i = 0; i < jointLoads.size(); i++) {
            row = new TableRow(this);
            row.setLayoutParams(tlRowParams);

            tempTV0 = new TextView(this);
            tempTV1 = new TextView(this);
            tempTV2 = new TextView(this);

            tempTV0.setText(String.valueOf((int) Math.floor(jointLoads.get(i)[0])));
            tempTV1.setText(String.valueOf(jointLoads.get(i)[1]));
            tempTV2.setText(String.valueOf(jointLoads.get(i)[2]));

            tempTV0.setLayoutParams(tlCellParams);
            tempTV1.setLayoutParams(tlCellParams);
            tempTV2.setLayoutParams(tlCellParams);

            tempTV0.setBackground(gd);
            tempTV1.setBackground(gd);
            tempTV2.setBackground(gd);

            tempTV0.setPadding(4, 4, 4, 4);
            tempTV1.setPadding(4, 4, 4, 4);
            tempTV2.setPadding(4, 4, 4, 4);

            row.addView(tempTV0);
            row.addView(tempTV1);
            row.addView(tempTV2);

            tlJointLoads.addView(row);
        }

        llContainer.addView(tlJointLoads);

        // Adding Supports Data to Table layout

        tempTV0 = new TextView(this);
        tempTV0.setText("\nSUPPORTS");
        tempTV0.setTypeface(null, Typeface.BOLD);
        tempTV0.setGravity(Gravity.CENTER);
        tempTV0.setLayoutParams(llTitleParams);
        llContainer.addView(tempTV0);

        row = new TableRow(this);
        row.setLayoutParams(tlRowParams);

        tempTV0 = new TextView(this);
        tempTV1 = new TextView(this);
        tempTV2 = new TextView(this);

        tempTV0.setText("Joint\nNo.");
        tempTV1.setText("Restraint in\nX Direction");
        tempTV2.setText("Restraint in\nY Direction");

        tempTV0.setLayoutParams(tlCellParams);
        tempTV1.setLayoutParams(tlCellParams);
        tempTV2.setLayoutParams(tlCellParams);

        tempTV0.setBackground(gd);
        tempTV1.setBackground(gd);
        tempTV2.setBackground(gd);

        tempTV0.setPadding(4, 4, 4, 4);
        tempTV1.setPadding(4, 4, 4, 4);
        tempTV2.setPadding(4, 4, 4, 4);

        row.addView(tempTV0);
        row.addView(tempTV1);
        row.addView(tempTV2);

        tlSupports.addView(row);

        String x, y;
        for(int i = 0; i < supports.size(); i++) {
            row = new TableRow(this);
            row.setLayoutParams(tlRowParams);

            tempTV0 = new TextView(this);
            tempTV1 = new TextView(this);
            tempTV2 = new TextView(this);

            x = supports.get(i)[1] == 1 ? "Yes" : "No";
            y = supports.get(i)[2] == 1 ? "Yes" : "No";

            tempTV0.setText(String.valueOf(supports.get(i)[0]));
            tempTV1.setText(x);
            tempTV2.setText(y);

            tempTV0.setLayoutParams(tlCellParams);
            tempTV1.setLayoutParams(tlCellParams);
            tempTV2.setLayoutParams(tlCellParams);

            tempTV0.setBackground(gd);
            tempTV1.setBackground(gd);
            tempTV2.setBackground(gd);

            tempTV0.setPadding(4, 4, 4, 4);
            tempTV1.setPadding(4, 4, 4, 4);
            tempTV2.setPadding(4, 4, 4, 4);

            row.addView(tempTV0);
            row.addView(tempTV1);
            row.addView(tempTV2);

            tlSupports.addView(row);
        }

        llContainer.addView(tlSupports);

        // Adding material properties to table layout

        tempTV0 = new TextView(this);
        tempTV0.setText("\nMATERIAL PROPERTIES");
        tempTV0.setTypeface(null, Typeface.BOLD);
        tempTV0.setGravity(Gravity.CENTER);
        tempTV0.setLayoutParams(llTitleParams);
        llContainer.addView(tempTV0);

        row = new TableRow(this);
        row.setLayoutParams(tlRowParams);

        tempTV0 = new TextView(this);
        tempTV1 = new TextView(this);
        tempTV2 = new TextView(this);

        tempTV0.setText("Material\nNo.");
        tempTV1.setText("Modulus of\nElasticity (E)");
        tempTV2.setText("Co-efficient of\nThermal Expansion");

        tempTV0.setBackground(gd);
        tempTV1.setBackground(gd);
        tempTV2.setBackground(gd);

        tempTV0.setPadding(4, 4, 4, 4);
        tempTV1.setPadding(4, 4, 4, 4);
        tempTV2.setPadding(4, 4, 4, 4);

        tempTV0.setLayoutParams(tlCellParams);
        tempTV1.setLayoutParams(tlCellParams);
        tempTV2.setLayoutParams(tlCellParams);

        row.addView(tempTV0);
        row.addView(tempTV1);
        row.addView(tempTV2);

        tlMaterialProperties.addView(row);

        for(int i = 0; i < materialProperties.size(); i++) {
            row = new TableRow(this);
            row.setLayoutParams(tlCellParams);

            tempTV0 = new TextView(this);
            tempTV1 = new TextView(this);
            tempTV2 = new TextView(this);

            tempTV0.setText(String.valueOf(i+1));
            tempTV1.setText(String.valueOf(materialProperties.get(i)));
            tempTV2.setText("0");

            tempTV0.setLayoutParams(tlCellParams);
            tempTV1.setLayoutParams(tlCellParams);
            tempTV2.setLayoutParams(tlCellParams);

            tempTV0.setBackground(gd);
            tempTV1.setBackground(gd);
            tempTV2.setBackground(gd);

            tempTV0.setPadding(4, 4, 4, 4);
            tempTV1.setPadding(4, 4, 4, 4);
            tempTV2.setPadding(4, 4, 4, 4);

            row.addView(tempTV0);
            row.addView(tempTV1);
            row.addView(tempTV2);

            tlMaterialProperties.addView(row);
        }
        llContainer.addView(tlMaterialProperties);

        // Adding Cross-Sectional Property to Table Layout

        tempTV0 = new TextView(this);
        tempTV0.setText("\nCROSS-SECTIONAL PROPERTIES");
        tempTV0.setLayoutParams(llTitleParams);
        tempTV0.setTypeface(null, Typeface.BOLD);
        tempTV0.setGravity(Gravity.CENTER);
        llContainer.addView(tempTV0);

        row = new TableRow(this);
        row.setLayoutParams(tlRowParams);

        tempTV0 = new TextView(this);
        tempTV1 = new TextView(this);

        tempTV0.setText("Property No.");
        tempTV1.setText("Area (A)");

        tempTV0.setLayoutParams(tlCellParams);
        tempTV1.setLayoutParams(tlCellParams);

        tempTV0.setBackground(gd);
        tempTV1.setBackground(gd);

        tempTV0.setPadding(4, 4, 4, 4);
        tempTV1.setPadding(4, 4, 4, 4);

        row.addView(tempTV0);
        row.addView(tempTV1);

        tlCrossSectionProperties.addView(row);

        for(int i = 0; i < crossSectionalProperties.size(); i++) {
            row = new TableRow(this);
            row.setLayoutParams(tlRowParams);

            tempTV0 = new TextView(this);
            tempTV1 = new TextView(this);

            tempTV0.setText(String.valueOf(i+1));
            tempTV1.setText(String.valueOf(crossSectionalProperties.get(i)));

            tempTV0.setLayoutParams(tlCellParams);
            tempTV1.setLayoutParams(tlCellParams);

            tempTV0.setBackground(gd);
            tempTV1.setBackground(gd);

            tempTV0.setPadding(4, 4, 4, 4);
            tempTV1.setPadding(4, 4, 4, 4);

            row.addView(tempTV0);
            row.addView(tempTV1);

            tlCrossSectionProperties.addView(row);
        }
        llContainer.addView(tlCrossSectionProperties);

        // Adding Members to table layout

        tempTV0 = new TextView(this);
        tempTV0.setText("\nMEMBER DATA");
        tempTV0.setTypeface(null, Typeface.BOLD);
        tempTV0.setGravity(Gravity.CENTER);
        tempTV0.setLayoutParams(llTitleParams);
        llContainer.addView(tempTV0);

        row = new TableRow(this);
        row.setLayoutParams(tlRowParams);

        tempTV0 = new TextView(this);
        tempTV1 = new TextView(this);
        tempTV2 = new TextView(this);
        tempTV3 = new TextView(this);
        tempTV4 = new TextView(this);

        tempTV0.setText("Member\nNo.\n");
        tempTV1.setText("Beginning\nJoint\n");
        tempTV2.setText("Ending\nJoint\n");
        tempTV3.setText("Material\nNo.\n");
        tempTV4.setText("CS\nProperty\nNo.");

        tempTV0.setLayoutParams(tlCellParams);
        tempTV1.setLayoutParams(tlCellParams);
        tempTV2.setLayoutParams(tlCellParams);
        tempTV3.setLayoutParams(tlCellParams);
        tempTV4.setLayoutParams(tlCellParams);

        tempTV0.setBackground(gd);
        tempTV1.setBackground(gd);
        tempTV2.setBackground(gd);
        tempTV3.setBackground(gd);
        tempTV4.setBackground(gd);

        tempTV0.setPadding(4, 4, 4, 4);
        tempTV1.setPadding(4, 4, 4, 4);
        tempTV2.setPadding(4, 4, 4, 4);
        tempTV3.setPadding(4, 4, 4, 4);
        tempTV4.setPadding(4, 4, 4, 4);

        row.addView(tempTV0);
        row.addView(tempTV1);
        row.addView(tempTV2);
        row.addView(tempTV3);
        row.addView(tempTV4);

        tlMembers.addView(row);

        for(int i = 0; i < members.size(); i++) {
            row = new TableRow(this);
            row.setLayoutParams(tlCellParams);

            tempTV0 = new TextView(this);
            tempTV1 = new TextView(this);
            tempTV2 = new TextView(this);
            tempTV3 = new TextView(this);
            tempTV4 = new TextView(this);

            tempTV0.setText(String.valueOf(i+1));
            tempTV1.setText(String.valueOf(members.get(i)[0]));
            tempTV2.setText(String.valueOf(members.get(i)[1]));
            tempTV3.setText(String.valueOf(members.get(i)[2]));
            tempTV4.setText(String.valueOf(members.get(i)[3]));

            tempTV0.setBackground(gd);
            tempTV1.setBackground(gd);
            tempTV2.setBackground(gd);
            tempTV3.setBackground(gd);
            tempTV4.setBackground(gd);

            tempTV0.setPadding(4, 4, 4, 4);
            tempTV1.setPadding(4, 4, 4, 4);
            tempTV2.setPadding(4, 4, 4, 4);
            tempTV3.setPadding(4, 4, 4, 4);
            tempTV4.setPadding(4, 4, 4, 4);

            row.addView(tempTV0);
            row.addView(tempTV1);
            row.addView(tempTV2);
            row.addView(tempTV3);
            row.addView(tempTV4);

            tlMembers.addView(row);
        }
        llContainer.addView(tlMembers);

        //llContainer.invalidate();

        //Toast.makeText(getApplicationContext(), "llContainer already invalidated; length of joint data = " + jointCoordinates.size(), Toast.LENGTH_LONG).show();
    }

    public void MSTIFFG(double E, double A, double BL, double CX, double CY, double[][] GK) {
        double Z = E * A / BL,
                Z1 = Z * (Math.pow(CX, 2)),
                Z2 = Z * (Math.pow(CY, 2)),
                Z3 = Z * CX * CY;

        GK[0][0] = Z1;
        GK[1][0] = Z3;
        GK[2][0] = -Z1;
        GK[3][0] = -Z3;
        GK[0][1] = Z3;
        GK[1][1] = Z2;
        GK[2][1] = -Z3;
        GK[3][1] = -Z2;
        GK[0][2] = -Z1;
        GK[1][2] = -Z3;
        GK[2][2] = Z1;
        GK[3][2] = Z3;
        GK[0][3] = -Z3;
        GK[1][3] = -Z2;
        GK[2][3] = Z3;
        GK[3][3] = Z2;
    }

    public void STORES(int JB, int JE, int NCJT, int NDOF, int[] NSC, double[][] GK, double[][] S) {
        int i1, n1, n2;
        for(int i = 1; i <= 2 * NCJT; i++) {
            if(i <= NCJT) {
               i1 = (JB - 1) * NCJT + i;
            } else {
                i1 = (JE - 1) * NCJT + (i - NCJT);
            }

            n1 = NSC[i1-1];

            if(n1 <= NDOF) {
                for(int j = 1; j <= 2 * NCJT; j++) {
                    if(j <= NCJT) {
                        i1 = (JB - 1) * NCJT + j;
                    } else {
                        i1 = (JE - 1) * NCJT + (j - NCJT);
                    }

                    n2 = NSC[i1-1];

                    if(n2 <= NDOF) {
                        S[n1-1][n2-1] = S[n1-1][n2-1] + GK[i-1][j-1];
                    }
                }
            }
        }
    }

    // Determining Member Global Displacement Vector

    public void MDISPG(int JB, int JE, int NCJT, int NDOF, int[] NSC, double[] P, double[] V) {
        for(int i = 0; i < 2 * NCJT; i++) {
            V[i] = 0;
        }

        int j = (JB - 1) * NCJT;

        for(int i = 1; i <= NCJT; i++) {
            j = j + 1;
            int n = NSC[j-1];

            if(n <= NDOF) {
                V[i-1] = P[n-1];
            }
        }

        j = (JE - 1) * NCJT;

        for(int i = NCJT + 1; i <= 2 * NCJT; i++) {
            j = j + 1;
            int n = NSC[j-1];

            if(n <= NDOF) {
                V[i-1] = P[n-1];
            }
        }
    }

    // Determining Member Transformation Matrix for Plane Trusses

    public void MTRANS(double CX, double CY, int NCJT, double[][] T) {
        for(int i = 0; i < 2 * NCJT; i++) {
            for(int j = 0; j < 2 * NCJT; j++) {
                T[i][j] = 0;
            }
        }

        T[0][0] = CX;
        T[1][0] = -CY;
        T[0][1] = CY;
        T[1][1] = CX;
        T[2][2] = CX;
        T[3][2] = -CY;
        T[2][3] = CY;
        T[3][3] = CX;
    }

    // Determining Member Local Displacement Vector

    public void MDISPL(int NCJT, double[] V, double[][] T, double[] U) {
        for(int i = 0; i < 2 * NCJT; i++) {
            U[i] = 0;
        }

        for(int i = 0; i < 2 * NCJT; i++) {
            for(int j = 0; j < 2 * NCJT; j++) {
                U[i] = U[i] + T[i][j] * V[j];
            }
        }
    }

    // Determining Local Stiffness Matrix for Plane Truss

    public void MSTIFFL(double E, double A, double BL, int NCJT, double[][] BK) {
        for(int i = 0; i < 2 * NCJT; i++) {
            for(int j = 0; j < 2 * NCJT; j++) {
                BK[i][j] = 0;
            }
        }

        double Z = E * A / BL;

        BK[0][0] = Z;
        BK[2][0] = -Z;
        BK[0][2] = -Z;
        BK[2][2] = Z;
    }

    // Determining Member Local Force Vector

    public void MFORCEL(int NCJT, double[][] BK, double[] U, double[] Q) {
        for (int i = 0; i < 2 * NCJT; i++) {
            Q[i] = 0;
        }

        for (int i = 0; i < 2 * NCJT; i++) {
            for (int j = 0; j < 2 * NCJT; j++) {
                Q[i] = Q[i] + BK[i][j] * U[j];
            }
        }

        // Display Member Forces Q
    }

    // Determine Member Global Forces Vector

    public void MFORCEG(int NCJT, double[][] T, double[] Q, double[] F) {
        for(int i = 0; i < 2 * NCJT; i++) {
            F[i] = 0;
        }

        for(int i = 0; i < 2 * NCJT; i++) {
            for(int j = 0; j < 2 * NCJT; j++) {
                F[i] = F[i] + T[j][i] * Q[j];
            }
        }

        // Display MFORCEG for output
    }

    // Storing Member Global Forces in Support Reaction Vector

    public void STORER(int JB, int JE, int NCJT, int NDOF, int[] NSC, double[] F, double[] R) {
        int i1, n;
        for(int i = 1; i <= 2 * NCJT; i++) {
            if(i <= NCJT) {
                i1 = (JB - 1) * NCJT + i;
            } else {
                i1 = (JE - 1) * NCJT + (i - NCJT);
            }

            n = NSC[i1-1];

            if(n > NDOF) {
                R[n - NDOF -1] = R[n - NDOF - 1] + F[i - 1];
            }
        }
    }



}