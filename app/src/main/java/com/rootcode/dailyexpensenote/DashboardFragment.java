package com.rootcode.dailyexpensenote;

import android.app.DatePickerDialog;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * A simple {@link Fragment} subclass.
 */
public class DashboardFragment extends Fragment {

    private Spinner typeSpinner;
    private TextView fromDateTV, toDateTV, totalExpenseTV;
    private ImageView fromDatePickerBtn, toDatePickerBtn;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
    private SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm aa");
    private String type;
    String[] typeExpense;

    private Calendar calendar;
    private int year, month, fromDay, toDay, hour, minute;
    private Context context;
    private DatabaseHelper helper;
    private int totalAmount = 0;
    private int count = 0;
    int currentPosition = 0;
    private long fromDate=0;
    private long toDate=9;

    public DashboardFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            currentPosition = savedInstanceState.getInt("currentSpinnerPosition");
        }
        context = container.getContext();

        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        typeSpinner = view.findViewById(R.id.dashBoardTypeSpinner);
        fromDatePickerBtn = view.findViewById(R.id.dashBoardFromDateCalenderBtn);
        toDatePickerBtn = view.findViewById(R.id.dashBoardToDateCalenderBtn);
        fromDateTV = view.findViewById(R.id.dashBoardFromDateTV);
        toDateTV = view.findViewById(R.id.dashBoardToDateTV);
        totalExpenseTV = view.findViewById(R.id.totalExpenseTV);
        helper = new DatabaseHelper(context);

        try {
            process();
        } catch (Exception e) {
           // Log.d("prob", "ParseException: ");
            e.printStackTrace();
        }

        try {
            pullData();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return view;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putInt("currentSpinnerPosition", currentPosition);
    }

    private void process() throws ParseException {

        typeExpense = new String[]{"All", "Rent", "Food", "Utility bills", "Medicine", "Clothing", "Transport", "Health", "Gift"};
        ArrayAdapter arrayAdapter = new ArrayAdapter(getContext(), android.R.layout.simple_list_item_activated_1, typeExpense);
        typeSpinner.setAdapter(arrayAdapter);
        type = typeExpense[currentPosition];
        typeSpinner.setSelection(currentPosition);

        typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {

                currentPosition = position;
                type = typeExpense[currentPosition];

                if (count > 0) {
                    try {
                        pullData();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        int indexMonth = month + 1;
        fromDay = 1;
        toDay = calendar.get(Calendar.DAY_OF_MONTH);
        hour = calendar.get(Calendar.HOUR);
        minute = calendar.get(Calendar.MINUTE);

        String nFromDate = year + "/" + indexMonth + "/" + fromDay;
        fromDateTV.setText(nFromDate);

        final DatePickerDialog.OnDateSetListener fromDateListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int fromDay) {
                calendar.set(year, month, fromDay);
                String userFromDate = dateFormat.format(calendar.getTime());
                //Toast.makeText(context,userFromDate+" is selected",Toast.LENGTH_LONG).show();
                fromDateTV.setText(userFromDate);
                if (count > 0) {                     //set listener to pull modyifiying data when second time data set
                    try {
                        pullData();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        fromDatePickerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), fromDateListener, year, month, fromDay);
                datePickerDialog.show();
            }
        });

        String nToDate = year + "/" + indexMonth + "/" + toDay;
        toDateTV.setText(nToDate);

        final DatePickerDialog.OnDateSetListener toDateListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int toDay) {
                calendar.set(year, month, toDay);
                String userFromDate = dateFormat.format(calendar.getTime());
                toDateTV.setText(userFromDate);
                if (count > 0) {                     //set listener to pull modyifiying data when second time data set
                    try {
                        pullData();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        toDatePickerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), toDateListener, year, month, toDay);
                datePickerDialog.show();
            }
        });

        Cursor cursor = helper.showTotalExpense(fromDate, toDate, type);

        while (cursor.moveToNext()) {
            int totalExpense = cursor.getInt(cursor.getColumnIndex(helper.TOT_EXPENSE));
            totalExpenseTV.setText("BDT " + totalExpense);
        }
    }

    private void pullData() throws ParseException {

        Date d1 = dateFormat.parse(fromDateTV.getText().toString());
        Date d2 = dateFormat.parse(toDateTV.getText().toString());
        fromDate = d1.getTime();
        toDate = d2.getTime();

        int dbAmount = 0;

        Cursor cursor = helper.showAllData();

        while (cursor.moveToNext()) {

            long dateFromDB = cursor.getLong(cursor.getColumnIndex(helper.COL_Date));
            String dbtype = cursor.getString(cursor.getColumnIndex(helper.COL_TYPE));

            count++;

            if (type.equals(typeExpense[0]) && dateFromDB >= fromDate && dateFromDB <= toDate) {       //when selected All in types
                dbAmount = cursor.getInt(cursor.getColumnIndex(helper.COL_Amount));
                totalAmount = totalAmount + dbAmount;
            } else if (type.equals(dbtype) && dateFromDB >= fromDate && dateFromDB <= toDate) {          //when selected specific in types
                dbAmount = cursor.getInt(cursor.getColumnIndex(helper.COL_Amount));
                totalAmount = totalAmount + dbAmount;
            }
        }

        totalExpenseTV.setText(totalAmount + "Tk");
        totalAmount = 0;
    }
}
